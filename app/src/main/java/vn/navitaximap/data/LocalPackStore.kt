package vn.navitaximap.data

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InterruptedIOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Properties
import java.util.UUID

/**
 * Import-only prototype for app-private PMTiles packs.
 * No background downloading, map style inference or provenance approval is implied.
 * All large-file copying is streaming and MUST be called off the Android UI thread.
 */
class LocalPackStore(
    private val directory: File,
    private val maxBytes: Long = 4L * 1024 * 1024 * 1024,
    private val minimumFreeBytes: Long = 8L * 1024 * 1024,
    private val freeBytes: () -> Long = { directory.usableSpace }
) {
    init {
        require(maxBytes > 0) { "Pack size limit must be positive" }
        require(minimumFreeBytes >= 0) { "Storage safety margin must be nonnegative" }
    }

    /** Files recovered from an interrupted import, never user-selected originals. */
    data class RecoveryReport(
        val discardedStaging: Int,
        val discardedOrphanArchives: Int,
        val discardedOrphanIndexes: Int,
        val reclaimedBytes: Long
    ) {
        val removedCount: Int get() = discardedStaging +
            discardedOrphanArchives + discardedOrphanIndexes
    }

    data class Pack(
        val id: String,
        val name: String,
        val sizeBytes: Long,
        val sha256: String,
        val minZoom: Int,
        val maxZoom: Int,
        val providedChecksumMatched: Boolean
    )

    private fun ensureDir() {
        check(directory.isDirectory || directory.mkdirs()) { "Cannot create offline storage" }
    }

    /**
     * Recover from termination between copying, fsync and two separate atomic moves.
     * The .properties index is the commit marker; a file without its matching
     * archive/index is never a visible pack. Do not touch arbitrary unrelated files.
     *
     * All current writes use this store's synchronized methods. Cross-process
     * writers are NOT supported yet; never run a second service against this folder.
     */
    @Synchronized
    fun recoverInterruptedImports(): RecoveryReport {
        ensureDir()
        val all = directory.listFiles().orEmpty().filter {
            it.isFile && !Files.isSymbolicLink(it.toPath())
        }
        var staging = 0
        var archives = 0
        var indexes = 0
        var reclaimed = 0L
        for (file in all) {
            val name = file.name
            val partialId = when {
                name.endsWith(".properties.partial") -> name.removeSuffix(".properties.partial")
                name.endsWith(".partial") -> name.removeSuffix(".partial")
                else -> null
            }
            val orphanArchiveId = if (name.endsWith(".pmtiles")) name.removeSuffix(".pmtiles") else null
            val orphanIndexId = if (name.endsWith(".properties")) name.removeSuffix(".properties") else null
            val kind = when {
                partialId != null && validId(partialId) -> 1
                orphanArchiveId != null && validId(orphanArchiveId) &&
                    !index(orphanArchiveId).isFile -> 2
                orphanIndexId != null && validId(orphanIndexId) &&
                    !archive(orphanIndexId).isFile -> 3
                else -> 0
            }
            if (kind == 0) continue
            val bytes = file.length()
            check(file.delete()) { "Unable to clean up interrupted import: $name" }
            reclaimed += bytes
            when (kind) {
                1 -> staging++
                2 -> archives++
                3 -> indexes++
            }
        }
        return RecoveryReport(staging, archives, indexes, reclaimed)
    }

    /** Per-file availability check; keep room for OS/metadata, don't allocate large RAM. */
    private fun checkStorageFor(nextBytes: Int) {
        val available = freeBytes()
        require(available >= minimumFreeBytes &&
            available - minimumFreeBytes >= nextBytes.toLong()) {
            "Không đủ dung lượng trống để nhập PMTiles"
        }
    }


    private fun validId(id: String) =
        id.matches(Regex("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"))

    private fun archive(id: String): File {
        require(validId(id)) { "Invalid pack ID" }
        return File(directory, "$id.pmtiles")
    }
    private fun index(id: String): File {
        require(validId(id)) { "Invalid pack ID" }
        return File(directory, "$id.properties")
    }

    @Synchronized
    fun importPack(
        source: InputStream,
        suggestedName: String,
        expectedSha256: String? = null
    ): Pack {
        recoverInterruptedImports()
        if (expectedSha256 != null) {
            require(OfflinePackVerifier.isValidSha256(expectedSha256)) { "Invalid expected SHA-256" }
        }

        val id = UUID.randomUUID().toString()
        val staging = File(directory, "$id.partial")
        val stagingIndex = File(directory, "$id.properties.partial")
        val destination = archive(id)
        val metadata = index(id)
        var committed = false
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            var count = 0L
            source.use { input ->
                FileOutputStream(staging).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        if (Thread.currentThread().isInterrupted) throw InterruptedIOException("Import cancelled")
                        val n = input.read(buffer)
                        if (n < 0) break
                        if (n == 0) continue
                        require(count <= maxBytes - n) { "Offline pack exceeds the size limit" }
                        checkStorageFor(n)
                        count += n
                        digest.update(buffer, 0, n)
                        output.write(buffer, 0, n)
                    }
                    output.fd.sync()
                }
            }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            if (expectedSha256 != null) {
                require(hash.equals(expectedSha256, ignoreCase = true)) { "Source checksum mismatch" }
            }

            val header = PmtilesHeader.inspect(staging)
            val safeName = suggestedName
                .replace(Regex("[\\p{Cntrl}]"), " ")
                .trim()
                .take(72)
                .ifBlank { "imported.pmtiles" }
            val record = Pack(id, safeName, count, hash, header.minZoom,
                header.maxZoom, expectedSha256 != null)
            val properties = Properties().apply {
                setProperty("id", id)
                setProperty("name", record.name)
                setProperty("size", record.sizeBytes.toString())
                setProperty("sha256", record.sha256)
                setProperty("minZoom", record.minZoom.toString())
                setProperty("maxZoom", record.maxZoom.toString())
                setProperty("providedChecksumMatched", record.providedChecksumMatched.toString())
                setProperty("sourceApproval", "UNVERIFIED")
            }

            // Two same-filesystem atomic renames: the index is the visibility marker.
            FileOutputStream(stagingIndex).use { output ->
                properties.store(output, "NaviTaxiMap local archive; source is not approved")
                output.fd.sync()
            }
            Files.move(staging.toPath(), destination.toPath(), StandardCopyOption.ATOMIC_MOVE)
            Files.move(stagingIndex.toPath(), metadata.toPath(), StandardCopyOption.ATOMIC_MOVE)
            committed = true
            return record
        } finally {
            staging.delete()
            stagingIndex.delete()
            if (!committed) {
                destination.delete()
                metadata.delete()
            }
        }
    }

    @Synchronized
    fun listPacks(): List<Pack> {
        if (!directory.isDirectory) return emptyList()
        return directory.listFiles().orEmpty()
            .filter { it.isFile && it.name.endsWith(".properties") }
            .mapNotNull { file ->
                runCatching {
                    val id = file.name.removeSuffix(".properties")
                    if (!validId(id) || !archive(id).isFile) return@runCatching null
                    val props = Properties()
                    file.inputStream().use { props.load(it) }
                    require(props.getProperty("id") == id)
                    val size = props.getProperty("size").toLong()
                    val sha = props.getProperty("sha256")
                    require(size > 0 && OfflinePackVerifier.isValidSha256(sha) &&
                            archive(id).length() == size)
                    Pack(id, props.getProperty("name") ?: id, size, sha,
                        props.getProperty("minZoom").toInt(),
                        props.getProperty("maxZoom").toInt(),
                        props.getProperty("providedChecksumMatched") == "true")
                }.getOrNull()
            }
            .sortedBy { it.name }
    }

    /** Byte accounting is for locally indexed packs, not the app's total cache. */
    @Synchronized
    fun storageUsage(): Pair<Long, Long> {
        val importedBytes = listPacks().sumOf { it.sizeBytes }
        return importedBytes to freeBytes()
    }

    /** Recomputes SHA-256 before anyone uses this source for display. */
    @Synchronized
    fun verifyPack(id: String): Boolean {
        val pack = listPacks().firstOrNull { it.id == id } ?: return false
        return OfflinePackVerifier.verify(archive(id), pack.sha256) &&
            runCatching { PmtilesHeader.inspect(archive(id)) }.isSuccess
    }

    @Synchronized
    fun deletePack(id: String): Boolean {
        if (!validId(id)) return false
        val file = archive(id)
        val info = index(id)
        if (!file.exists() && !info.exists()) return false
        val removedData = !file.exists() || file.delete()
        val removedIndex = !info.exists() || info.delete()
        return removedData && removedIndex
    }
}
