package vn.navitaximap.data

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest
import java.nio.file.Files

/**
 * A dormant, host-allowlisted HTTPS Range *staging* engine. No built-in server,
 * manifest, scheduler, background job, auto-download or distribution permission.
 *
 * Production catalog transfers MUST go through [ApprovedCatalogPackDownloader].
 * Staging can be resumed after process death, but no partial data enters the
 * app's indexed offline catalog. Do not use this engine on the Android UI thread.
 */
internal class PinnedHttpsRangeTransfer(
    private val scratchDirectory: File,
    private val connectionFactory: (URL) -> HttpURLConnection =
        { url -> url.openConnection() as HttpURLConnection },
    private val availableBytes: () -> Long = { scratchDirectory.usableSpace }
) {
    data class Pin(val fileName: String, val bytes: Long, val sha256: String) {
        init {
            require(fileName.matches(Regex("[0-9]{2}-[a-z0-9-]+\\.pmtiles")))
            require(bytes in 127L..(4L * 1024L * 1024L * 1024L))
            require(OfflinePackVerifier.isValidSha256(sha256))
        }
    }

    data class VerifiedStage(val file: File, val resumedBytes: Long)

    /**
     * Untrusted local progress snapshot for eventual UI. A full-size .partial
     * is NOT verified and MUST NEVER be offered as an imported map.
     * This method does not connect, fetch, hash, or activate distribution.
     */
    data class StagingProgress(val bytesStored: Long, val expectedBytes: Long) {
        val completeButUnverified: Boolean get() = bytesStored == expectedBytes
    }

    @Synchronized
    fun inspectProgress(pin: Pin): StagingProgress {
        if (!scratchDirectory.exists()) return StagingProgress(0L, pin.bytes)
        require(scratchDirectory.isDirectory && !Files.isSymbolicLink(scratchDirectory.toPath())) {
            "Staging directory is not a regular private directory"
        }
        val target = part(pin)
        require(!Files.isSymbolicLink(target.toPath())) { "Symlinked partial archive forbidden" }
        require(!target.exists() || target.isFile) { "Unexpected partial archive type" }
        val bytes = if (target.isFile) target.length() else 0L
        require(bytes in 0L..pin.bytes) { "Oversized existing partial archive" }
        return StagingProgress(bytes, pin.bytes)
    }

    private fun validateEndpoint(pin: Pin, url: String, approvedHost: String): URL {
        val uri = URI(url)
        require(uri.scheme.equals("https", ignoreCase = true)) { "HTTPS is required" }
        require(uri.rawUserInfo == null && uri.rawQuery == null && uri.rawFragment == null) {
            "Credentials, query strings and fragments are not approved for this pack"
        }
        require(uri.port == -1 || uri.port == 443) { "Unexpected HTTPS port" }
        val host = uri.host ?: throw IllegalArgumentException("HTTPS host missing")
        require(approvedHost.matches(Regex("[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+"))) {
            "A reviewed DNS hostname is required"
        }
        require(host.equals(approvedHost, ignoreCase = true)) { "Host does not match reviewed origin" }
        require(uri.rawPath.endsWith("/" + pin.fileName) &&
            uri.rawPath.split('/').none { it == "." || it == ".." || it.contains('%') }) {
            "Archive URL must use its exact SHA-pinned file name without encoded paths"
        }
        return uri.toURL()
    }

    private fun part(pin: Pin): File =
        File(scratchDirectory, "shortbread-${pin.sha256.lowercase()}.partial")

    private fun ensureScratch() {
        check(scratchDirectory.isDirectory || scratchDirectory.mkdirs()) {
            "Cannot create app-private download staging"
        }
        require(!Files.isSymbolicLink(scratchDirectory.toPath())) {
            "Symlinked staging directory forbidden"
        }
    }

    /**
     * One bounded HTTP 206 request per call. On transient truncated streams,
     * retains only app-private partial bytes for the next explicit invocation.
     * A malformed range, bad final SHA, unexpected encoding or redirect fails
     * CLOSED and cannot enter LocalPackStore.
     *
     * Enough capacity is required for both partial download and the subsequent
     * atomic LocalPackStore import. This intentionally uses extra disk space.
     */
    @Synchronized
    fun stage(pin: Pin, url: String, approvedHost: String): VerifiedStage {
        val endpoint = validateEndpoint(pin, url, approvedHost)
        ensureScratch()
        val target = part(pin)
        // Use the same read-only progress gate to reject malformed, oversized
        // or symlinked partials before any network or file write.
        val offset = inspectProgress(pin).bytesStored

        // The final import makes a second copy before atomic commit.
        val remaining = pin.bytes - offset
        val margin = 16L * 1024L * 1024L
        val needed = Math.addExact(Math.addExact(remaining, pin.bytes), margin)
        require(availableBytes() >= needed) { "Not enough space to download and verify archive" }

        if (remaining > 0L) {
            val connection = connectionFactory(endpoint)
            try {
                connection.instanceFollowRedirects = false
                connection.useCaches = false
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.setRequestProperty("Accept-Encoding", "identity")
                connection.setRequestProperty("Range", "bytes=$offset-${pin.bytes - 1}")
                val status = connection.responseCode
                require(status == HttpURLConnection.HTTP_PARTIAL) {
                    "Server must honor exact HTTP Range, not a redirect or whole-file response"
                }
                val range = connection.getHeaderField("Content-Range")
                    ?: throw IOException("Missing Content-Range")
                val match = Regex("bytes (\\d+)-(\\d+)/(\\d+)").matchEntire(range)
                    ?: throw IOException("Invalid Content-Range")
                val start = match.groupValues[1].toLongOrNull()
                val end = match.groupValues[2].toLongOrNull()
                val total = match.groupValues[3].toLongOrNull()
                require(start == offset && end == pin.bytes - 1 && total == pin.bytes) {
                    "Content-Range differs from pinned bytes or resume offset"
                }
                val encoding = connection.getHeaderField("Content-Encoding")
                require(encoding == null || encoding.equals("identity", ignoreCase = true)) {
                    "Compressed transfer body cannot be range verified"
                }
                val lengthHeader = connection.getHeaderField("Content-Length")
                if (lengthHeader != null) {
                    // A present but malformed/overflowing HTTP length is never
                    // equivalent to an intentionally absent length header.
                    val advertised = lengthHeader.toLongOrNull()
                    require(advertised != null && advertised == remaining) {
                        "HTTP body length is missing, malformed or differs from pinned Range"
                    }
                }

                var written = 0L
                connection.inputStream.use { input ->
                    FileOutputStream(target, offset > 0).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            if (Thread.currentThread().isInterrupted) {
                                throw InterruptedIOException("Download interrupted")
                            }
                            val n = input.read(buffer)
                            if (n < 0) break
                            if (n == 0) continue
                            require(written <= remaining - n) { "Response exceeded pinned content length" }
                            written += n
                            output.write(buffer, 0, n)
                        }
                        output.fd.sync()
                    }
                }
                if (written != remaining) throw IOException("Truncated HTTP Range body; partial retained")
            } finally {
                connection.disconnect()
            }
        }

        val digest = MessageDigest.getInstance("SHA-256")
        target.inputStream().buffered().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                if (n > 0) digest.update(buffer, 0, n)
            }
        }
        val hash = digest.digest().joinToString("") { "%02x".format(it) }
        if (target.length() != pin.bytes || !hash.equals(pin.sha256, ignoreCase = true)) {
            target.delete()
            throw IllegalArgumentException("Downloaded archive failed pinned size or SHA-256")
        }
        return VerifiedStage(target, offset)
    }

    @Synchronized
    fun discard(pin: Pin): Boolean {
        ensureScratch()
        val target = part(pin)
        require(!Files.isSymbolicLink(target.toPath())) { "Symlinked partial archive forbidden" }
        return !target.exists() || target.delete()
    }
}

/**
 * Reviewed map origins are COMPILED configuration, not untrusted caller-supplied
 * "approvedHost" strings. There are currently NO reviewed redistribution origins.
 * Future activation requires an explicit reviewed code change, ODbL audit and
 * independent host ownership/TLS verification. No URL is shipped in the app.
 */
internal object ReviewedShortbreadSources {
    data class Source(val exactHttpsUrl: String, val reviewedHost: String)
    val byCode: Map<String, Source> = emptyMap()
}

/**
 * The only intended transfer-to-catalog entry point. BOTH distribution and a
 * reviewed origin are mandatory. User input is limited to the known region code;
 * callers cannot bless an arbitrary domain by passing it as a method argument.
 */
internal class ApprovedCatalogPackDownloader(
    private val transfer: PinnedHttpsRangeTransfer,
    private val store: LocalPackStore
) {
    fun fetchApproved(code: String): LocalPackStore.Pack {
        check(Vietnam35Catalog.distributionApproved) {
            "OSM Shortbread redistribution/download host has not been approved"
        }
        val region = Vietnam35Catalog.packages.singleOrNull { it.code == code }
            ?: throw IllegalArgumentException("Unknown pinned map region")
        val source = ReviewedShortbreadSources.byCode[region.code]
            ?: throw IllegalStateException("No audited HTTPS source configured for this region")
        val pin = PinnedHttpsRangeTransfer.Pin(region.fileName, region.sizeBytes, region.sha256)
        val staged = transfer.stage(pin, source.exactHttpsUrl, source.reviewedHost)
        return try {
            store.importPack(staged.file.inputStream(), region.fileName,
                region.sha256, region.sizeBytes)
        } finally {
            // Do not leave a duplicate verified archive after an import attempt.
            transfer.discard(pin)
        }
    }
}
