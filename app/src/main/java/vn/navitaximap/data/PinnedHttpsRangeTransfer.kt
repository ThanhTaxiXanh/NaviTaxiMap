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
        require(!Files.isSymbolicLink(target.toPath())) { "Symlinked partial archive forbidden" }
        val offset = if (target.isFile) target.length() else 0L
        require(offset in 0L..pin.bytes) { "Invalid existing partial length" }

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
                val advertised = connection.getHeaderField("Content-Length")?.toLongOrNull()
                require(advertised == null || advertised == remaining) { "HTTP body size mismatch" }

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
 * Host/source approval gate. The current catalog explicitly has
 * distributionApproved=false, so this entry point refuses all transfers.
 * A source's exact HTTPS host must also be independently reviewed; no default
 * provider URL is present and none is invented here.
 */
internal class ApprovedCatalogPackDownloader(
    private val transfer: PinnedHttpsRangeTransfer,
    private val store: LocalPackStore
) {
    fun fetchApproved(code: String, url: String, reviewedHost: String): LocalPackStore.Pack {
        check(Vietnam35Catalog.distributionApproved) {
            "OSM Shortbread redistribution/download host has not been approved"
        }
        val region = Vietnam35Catalog.packages.singleOrNull { it.code == code }
            ?: throw IllegalArgumentException("Unknown pinned map region")
        val pin = PinnedHttpsRangeTransfer.Pin(region.fileName, region.sizeBytes, region.sha256)
        val staged = transfer.stage(pin, url, reviewedHost)
        return try {
            store.importPack(staged.file.inputStream(), region.fileName,
                region.sha256, region.sizeBytes)
        } finally {
            // Do not leave a duplicate verified archive after an import attempt.
            transfer.discard(pin)
        }
    }
}
