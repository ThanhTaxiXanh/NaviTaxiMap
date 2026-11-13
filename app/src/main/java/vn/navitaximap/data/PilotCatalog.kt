package vn.navitaximap.data

import java.net.URI

/** APK-pinned metadata; byte identity is NOT map legality or navigation approval. */
data class CatalogPackSpec(
    val id: String,
    val version: Int,
    val name: String,
    val sizeBytes: Long,
    val sha256: String,
    val minZoom: Int,
    val maxZoom: Int,
    val sourceXmlSha256: String,
    val downloadUrl: String,
    val license: String,
    val attribution: String
) {
    init {
        require(id.matches(Regex("[a-z0-9][a-z0-9-]{2,39}"))) { "Unsafe catalog ID" }
        require(version in 1..100_000) { "Invalid catalog version" }
        require(name.isNotBlank() && name.length <= 120) { "Invalid name" }
        require(sizeBytes in 127L..(4L * 1024 * 1024 * 1024)) { "Invalid size" }
        require(OfflinePackVerifier.isValidSha256(sha256)) { "Invalid SHA-256" }
        require(OfflinePackVerifier.isValidSha256(sourceXmlSha256)) { "Invalid source hash" }
        require(minZoom in 0..22 && maxZoom in minZoom..22) { "Invalid zooms" }
        require(license == "ODbL-1.0") { "Unrecognized license" }
        require(attribution == "© OpenStreetMap contributors") { "Missing attribution" }
        val uri = URI(downloadUrl)
        require(uri.scheme == "https" && uri.host == "raw.githubusercontent.com" &&
            uri.port == -1 && uri.userInfo == null && uri.query == null && uri.fragment == null) {
            "Catalog needs pinned HTTPS GitHub URL"
        }
        // Mutable develop/main branch URLs are forbidden. Require immutable 40-hex commit ref.
        require(uri.path.matches(Regex(
            "/ThanhTaxiXanh/NaviTaxiMap/[a-f0-9]{40}/app/src/main/assets/[a-z0-9_]+\\.pmtiles"
        ))) { "Catalog URL is not pinned to a commit" }
    }
    val archiveName: String get() = "$id-v$version-${sha256.take(12)}.pmtiles"
    val indexName: String get() = "$id.properties"
}

object PilotCatalog {
    val hanoiRoads = CatalogPackSpec(
        id = "hanoi-roads-visual",
        version = 1,
        name = "Hà Nội: đường OSM mẫu nhỏ (không dẫn đường)",
        sizeBytes = 53_956L,
        sha256 = "ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d",
        minZoom = 14,
        maxZoom = 17,
        sourceXmlSha256 = "db180ccb4a19e287888af400513dff2104d4c692b32d060a8416f1b6c08c7697",
        downloadUrl = "https://raw.githubusercontent.com/ThanhTaxiXanh/NaviTaxiMap/" +
            "554cfe62013c80146842aea85cb3625e583f2ced/app/src/main/assets/hanoi_real_roads.pmtiles",
        license = "ODbL-1.0",
        attribution = "© OpenStreetMap contributors"
    )
    /** New regions require explicit new vetted entries; never infer download URLs. */
    val previews: List<CatalogPackSpec> = listOf(hanoiRoads)
}
