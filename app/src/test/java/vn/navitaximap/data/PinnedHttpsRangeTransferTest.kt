package vn.navitaximap.data

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class PinnedHttpsRangeTransferTest {
    private fun sha(data: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(data).joinToString("") { "%02x".format(it) }

    private val payload = ("NaviTaxiMap original Range fixture! ".repeat(32)).toByteArray()

    private class FakeRange(
        url: URL,
        val status: Int,
        private val body: ByteArray,
        private val range: String?,
        private val advertisedLength: String? = body.size.toString(),
        private val encoding: String? = null
    ) : HttpURLConnection(url) {
        override fun connect() { connected = true }
        override fun disconnect() { connected = false }
        override fun usingProxy() = false
        override fun getResponseCode() = status
        override fun getInputStream(): InputStream = ByteArrayInputStream(body)
        override fun getHeaderField(name: String): String? = when (name) {
            "Content-Range" -> range
            "Content-Length" -> advertisedLength
            "Content-Encoding" -> encoding
            else -> null
        }
    }

    @Test fun partialStreamCanResumeButCannotCommitUntilFullShaMatches() {
        val root = createTempDir(prefix = "navitaxi-range-test-")
        try {
            val pin = PinnedHttpsRangeTransfer.Pin("24-bac-ninh.pmtiles",
                payload.size.toLong(), sha(payload))
            val exactUrl = "https://maps.example.test/previews/24-bac-ninh.pmtiles"
            val pieces = mutableListOf<FakeRange>()
            var n = 0
            val engine = PinnedHttpsRangeTransfer(root, { url ->
                val connection = if (n++ == 0) {
                    FakeRange(url, 206, payload.copyOfRange(0, 300),
                        "bytes 0-${payload.size - 1}/${payload.size}", null)
                } else {
                    FakeRange(url, 206, payload.copyOfRange(300, payload.size),
                        "bytes 300-${payload.size - 1}/${payload.size}")
                }
                pieces += connection
                connection
            }, { Long.MAX_VALUE })
            assertThrows(IOException::class.java) {
                engine.stage(pin, exactUrl, "maps.example.test")
            }
            val part = File(root, "shortbread-${pin.sha256}.partial")
            assertEquals(300L, part.length())
            val verified = engine.stage(pin, exactUrl, "maps.example.test")
            assertEquals(300L, verified.resumedBytes)
            assertArrayEquals(payload, verified.file.readBytes())
            assertEquals("bytes=0-${payload.size - 1}", pieces[0].getRequestProperty("Range"))
            assertEquals("bytes=300-${payload.size - 1}", pieces[1].getRequestProperty("Range"))
            assertTrue(engine.discard(pin))
            assertFalse(part.exists())
        } finally { root.deleteRecursively() }
    }

    @Test fun endpointValidationAndDistributionGateNeverIssueNetworkCalls() {
        val root = createTempDir(prefix = "navitaxi-range-blocked-")
        try {
            val pin = PinnedHttpsRangeTransfer.Pin("24-bac-ninh.pmtiles",
                payload.size.toLong(), sha(payload))
            var calls = 0
            val transfer = PinnedHttpsRangeTransfer(root, { url ->
                calls++
                FakeRange(url, 200, payload, null)
            }, { Long.MAX_VALUE })
            for (url in listOf(
                "http://maps.example.test/24-bac-ninh.pmtiles",
                "https://evil.example.test/24-bac-ninh.pmtiles",
                "https://maps.example.test/24-bac-ninh.pmtiles?token=secret",
                "https://maps.example.test/%32%34-bac-ninh.pmtiles",
                "https://maps.example.test/previews/24-bac-ninh.pmtiles#fragment"
            )) {
                assertThrows(IllegalArgumentException::class.java) {
                    transfer.stage(pin, url, "maps.example.test")
                }
            }
            val store = LocalPackStore(File(root, "offline-packs"))
            val gated = ApprovedCatalogPackDownloader(transfer, store)
            assertThrows(IllegalStateException::class.java) {
                gated.fetchApproved("24",
                    "https://maps.example.test/24-bac-ninh.pmtiles", "maps.example.test")
            }
            assertEquals(0, calls)
            assertFalse(File(root, "offline-packs").exists())
        } finally { root.deleteRecursively() }
    }

    @Test fun redirectIncorrectContentRangeAndCorruptionNeverProduceVerifiedArchive() {
        val root = createTempDir(prefix = "navitaxi-range-reject-")
        try {
            val pin = PinnedHttpsRangeTransfer.Pin("24-bac-ninh.pmtiles",
                payload.size.toLong(), sha(payload))
            val url = "https://maps.example.test/24-bac-ninh.pmtiles"
            for (status in listOf(200, 302, 416)) {
                val engine = PinnedHttpsRangeTransfer(root, { uri ->
                    FakeRange(uri, status, payload, null)
                }, { Long.MAX_VALUE })
                assertThrows(IllegalArgumentException::class.java) {
                    engine.stage(pin, url, "maps.example.test")
                }
            }
            val invalidRange = PinnedHttpsRangeTransfer(root, { uri ->
                FakeRange(uri, 206, payload, "bytes 1-${payload.size - 1}/${payload.size}")
            }, { Long.MAX_VALUE })
            assertThrows(IllegalArgumentException::class.java) {
                invalidRange.stage(pin, url, "maps.example.test")
            }
            val brokenBody = payload.clone().also { it[0] = (it[0].toInt() xor 0x1).toByte() }
            val badDigest = PinnedHttpsRangeTransfer(root, { uri ->
                FakeRange(uri, 206, brokenBody, "bytes 0-${payload.size - 1}/${payload.size}")
            }, { Long.MAX_VALUE })
            assertThrows(IllegalArgumentException::class.java) {
                badDigest.stage(pin, url, "maps.example.test")
            }
            assertFalse(File(root, "shortbread-${pin.sha256}.partial").exists())
        } finally { root.deleteRecursively() }
    }
}
