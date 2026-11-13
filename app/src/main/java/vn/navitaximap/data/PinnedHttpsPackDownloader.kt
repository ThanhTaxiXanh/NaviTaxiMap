package vn.navitaximap.data

import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Explicit user-initiated HTTPS download. No polling, redirects, mutable branch
 * URLs, remote catalogs or public tile scraping. APK pins identity and SHA-256.
 */
class PinnedHttpsPackDownloader(
    private val store: CatalogPackStore,
    private val opener: (CatalogPackSpec) -> java.io.InputStream = { spec ->
        val uri = URI(spec.downloadUrl)
        val connection = URL(uri.toString()).openConnection() as HttpsURLConnection
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 15_000
        connection.readTimeout = 20_000
        connection.setRequestProperty("Accept-Encoding", "identity")
        connection.setRequestProperty("User-Agent", "NaviTaxiMap-road-preview/0.1")
        try {
            check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "Pinned source returned HTTP ${connection.responseCode}; redirects blocked"
            }
            val length = connection.contentLengthLong
            require(length == -1L || length == spec.sizeBytes) { "Unexpected HTTP size" }
            connection.inputStream
        } catch (error: Exception) {
            connection.disconnect()
            throw error
        }
    }
) {
    fun download(
        spec: CatalogPackSpec,
        cancelled: () -> Boolean = { false },
        onProgress: (Long, Long) -> Unit = { _, _ -> }
    ): CatalogPackStore.Installed {
        store.verifiedCurrent(spec)?.let { return it }
        return store.install(spec, opener(spec), "PINNED_HTTPS", cancelled, onProgress)
    }
}
