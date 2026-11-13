package vn.navitaximap.data

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InterruptedIOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Properties

/**
 * Single-writer, app-private catalog preview store. Index is commit marker.
 * Failed updates preserve the previous version. Not a multi-process or true
 * power-loss transaction; a verified checksum does not approve navigation.
 */
class CatalogPackStore(
    private val directory: File,
    private val minimumFreeBytes: Long = 8L * 1024 * 1024,
    private val freeBytes: () -> Long = { directory.usableSpace }
) {
    init { require(minimumFreeBytes >= 0) { "Negative storage reserve" } }

    data class Installed(
        val id: String,
        val version: Int,
        val file: File,
        val sizeBytes: Long,
        val sha256: String,
        val origin: String,
        val attribution: String
    )

    private val safeArchive = Regex("[a-z0-9][a-z0-9-]{2,39}-v[1-9][0-9]*-[a-f0-9]{12}\\.pmtiles")
    private fun ensureDir() {
        check(directory.isDirectory || directory.mkdirs()) { "Cannot create catalog storage" }
    }
    private fun index(spec: CatalogPackSpec) = File(directory, spec.indexName)
    private fun archive(spec: CatalogPackSpec) = File(directory, spec.archiveName)

    private fun readIndex(spec: CatalogPackSpec): Installed? {
        val metadata = index(spec)
        if (!metadata.isFile || Files.isSymbolicLink(metadata.toPath())) return null
        return runCatching {
            val props = Properties().apply { metadata.inputStream().use { load(it) } }
            val name = props.getProperty("file")
            require(name != null && safeArchive.matches(name))
            val id = props.getProperty("id")
            require(id == spec.id)
            val version = props.getProperty("version").toInt()
            val size = props.getProperty("size").toLong()
            val sha = props.getProperty("sha256")
            require(version >= 1 && size >= 127 && OfflinePackVerifier.isValidSha256(sha))
            require(OfflinePackVerifier.isValidSha256(props.getProperty("sourceXmlSha256")))
            require(name == "$id-v$version-${sha.take(12)}.pmtiles") { "Index path mismatch" }
            require(props.getProperty("sourceApproval") == "UNREVIEWED_VISUAL_PREVIEW")
            require(props.getProperty("navigationApproved") == "false")
            require(props.getProperty("license") == "ODbL-1.0")
            require(props.getProperty("attribution") == "© OpenStreetMap contributors")
            val file = File(directory, name)
            require(file.isFile && !Files.isSymbolicLink(file.toPath()) && file.length() == size)
            Installed(id, version, file, size, sha,
                props.getProperty("origin") ?: "UNKNOWN", props.getProperty("attribution"))
        }.getOrNull()
    }

    private fun verifiedFile(installed: Installed): Boolean {
        if (Files.isSymbolicLink(installed.file.toPath())) return false
        return OfflinePackVerifier.verify(installed.file, installed.sha256) &&
            runCatching { PmtilesHeader.inspect(installed.file) }.isSuccess
    }

    @Synchronized
    fun verifiedCurrent(spec: CatalogPackSpec): Installed? {
        val current = readIndex(spec) ?: return null
        if (current.version != spec.version || current.sha256 != spec.sha256 ||
            current.sizeBytes != spec.sizeBytes) return null
        return current.takeIf { runCatching { verifiedFile(it) }.getOrDefault(false) }
    }

    @Synchronized
    fun versionInstalled(spec: CatalogPackSpec): Int? = readIndex(spec)?.version

    /**
     * Caller owns a background thread. Closed source, bounded stream and reserved
     * free space, plus staged metadata -> atomic index commit after data validation.
     */
    @Synchronized
    fun install(
        spec: CatalogPackSpec,
        source: InputStream,
        origin: String,
        cancelled: () -> Boolean = { false },
        onProgress: (Long, Long) -> Unit = { _, _ -> }
    ): Installed {
        ensureDir()
        require(origin == "BUNDLED" || origin == "PINNED_HTTPS") { "Invalid origin" }
        val previous = readIndex(spec)
        require(previous == null || previous.version <= spec.version) { "Refusing version downgrade" }
        verifiedCurrent(spec)?.let { source.close(); return it }
        val staged = File(directory, spec.archiveName + ".partial")
        val stagedIndex = File(directory, spec.indexName + ".partial")
        val destination = archive(spec)
        var committed = false
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            var count = 0L
            source.use { input ->
                FileOutputStream(staged).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        if (cancelled() || Thread.currentThread().isInterrupted) {
                            throw InterruptedIOException("Transfer cancelled")
                        }
                        val n = input.read(buffer)
                        if (n < 0) break
                        if (n == 0) continue
                        require(count <= spec.sizeBytes - n) { "Pack exceeds pinned size" }
                        val available = freeBytes()
                        require(available >= minimumFreeBytes &&
                            available - minimumFreeBytes >= n.toLong()) { "Insufficient free space" }
                        digest.update(buffer, 0, n)
                        output.write(buffer, 0, n)
                        count += n
                        onProgress(count, spec.sizeBytes)
                    }
                    output.fd.sync()
                }
            }
            val sha = digest.digest().joinToString("") { "%02x".format(it) }
            require(count == spec.sizeBytes && sha.equals(spec.sha256, true)) {
                "Pack differs from locally pinned size or SHA-256"
            }
            val header = PmtilesHeader.inspect(staged)
            require(header.minZoom == spec.minZoom && header.maxZoom == spec.maxZoom) {
                "Unexpected PMTiles zoom range"
            }
            val record = Installed(spec.id, spec.version, destination, count,
                spec.sha256, origin, spec.attribution)
            val props = Properties().apply {
                setProperty("id", spec.id)
                setProperty("version", spec.version.toString())
                setProperty("file", spec.archiveName)
                setProperty("name", spec.name)
                setProperty("size", spec.sizeBytes.toString())
                setProperty("sha256", spec.sha256)
                setProperty("origin", origin)
                setProperty("sourceXmlSha256", spec.sourceXmlSha256)
                setProperty("attribution", spec.attribution)
                setProperty("license", spec.license)
                setProperty("sourceApproval", "UNREVIEWED_VISUAL_PREVIEW")
                setProperty("navigationApproved", "false")
            }
            FileOutputStream(stagedIndex).use { output ->
                props.store(output, "Visual preview: NOT approved for navigation")
                output.fd.sync()
            }
            Files.move(staged.toPath(), destination.toPath(),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            Files.move(stagedIndex.toPath(), index(spec).toPath(),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            committed = true
            recoverOrphans(spec)
            return record
        } finally {
            staged.delete()
            stagedIndex.delete()
            if (!committed && previous?.file?.name != spec.archiveName) destination.delete()
        }
    }

    /** Only generated names for this catalog ID; never an arbitrary user-selected file. */
    @Synchronized
    fun recoverOrphans(spec: CatalogPackSpec): Int {
        ensureDir()
        val current = readIndex(spec)?.file?.name
        var removed = 0
        for (file in directory.listFiles().orEmpty()) {
            if (!file.isFile || Files.isSymbolicLink(file.toPath())) continue
            val name = file.name
            val generated = name.startsWith("${spec.id}-v") &&
                (safeArchive.matches(name) ||
                    (name.endsWith(".partial") &&
                        safeArchive.matches(name.removeSuffix(".partial"))))
            if ((generated && name != current) || name == spec.indexName + ".partial") {
                check(file.delete()) { "Cannot recover interrupted catalog install" }
                removed++
            }
        }
        return removed
    }

    @Synchronized
    fun delete(spec: CatalogPackSpec): Boolean {
        val current = readIndex(spec) ?: return false
        if (!index(spec).delete()) return false
        val removed = !current.file.exists() || current.file.delete()
        recoverOrphans(spec)
        return removed
    }
}
