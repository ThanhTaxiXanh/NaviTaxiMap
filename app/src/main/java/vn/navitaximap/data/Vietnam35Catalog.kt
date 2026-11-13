package vn.navitaximap.data

/** Exact SHA-pinned OSM Shortbread 1.0 visual-preview catalog.
 * There is no approved durable 35-pack HTTPS host; all entries are manual-import only.
 * Recognition is NOT a navigation, sovereignty, ODbL redistribution, or border approval.
 */
data class VietnamRegionSpec(
    val code: String, val label: String, val fileName: String,
    val sha256: String, val minZoom: Int, val maxZoom: Int
) {
    init {
        require(code == "VN" || code.matches(Regex("[0-9]{2}")))
        require(fileName.matches(Regex("[0-9]{2}-[a-z0-9-]+\\.pmtiles")))
        require(OfflinePackVerifier.isValidSha256(sha256))
        require(minZoom in 0..14 && maxZoom == 14)
    }
    fun matches(pack: LocalPackStore.Pack): Boolean =
        pack.name == fileName && pack.sha256.equals(sha256, ignoreCase = true) &&
            pack.minZoom == minZoom && pack.maxZoom == maxZoom
}

object Vietnam35Catalog {
    const val schema = "Geofabrik Shortbread 1.0"
    const val attribution = "© OpenStreetMap contributors"
    const val license = "ODbL-1.0"
    const val navigationApproved = false
    const val distributionApproved = false

    val packages: List<VietnamRegionSpec> = listOf(
        VietnamRegionSpec("VN", "Toàn Việt Nam", "00-viet-nam-all.pmtiles", "678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55", 0, 14),
        VietnamRegionSpec("01", "Hà Nội", "01-ha-noi.pmtiles", "ad6f2a78d2d7ca3d5763028965cc914195e61ec40ec4ad0b46db5d766c484139", 8, 14),
        VietnamRegionSpec("04", "Cao Bằng", "04-cao-bang.pmtiles", "f5bb84d3b6f7df10b50dab86a988a90deeb46c09126f1c67f5fa0a156b396f89", 8, 14),
        VietnamRegionSpec("08", "Tuyên Quang", "08-tuyen-quang.pmtiles", "c1cd6af9d017628e75d62dab5b68114030bafd0ca226fb74b8387877607ad389", 8, 14),
        VietnamRegionSpec("11", "Điện Biên", "11-dien-bien.pmtiles", "ec65dd49a52273eb5fc8baf8aaa0a3d7840e6a64ca03bf5d6fc1fbddb338f435", 8, 14),
        VietnamRegionSpec("12", "Lai Châu", "12-lai-chau.pmtiles", "a3b25557a13adae03b0898b1672c3d89951e16594ee3916133f62d66c490adce", 8, 14),
        VietnamRegionSpec("14", "Sơn La", "14-son-la.pmtiles", "a6ce48847dc6a88a306ef96a888abe9cfd9d86485e2014436816d9b1238dbc65", 8, 14),
        VietnamRegionSpec("15", "Lào Cai", "15-lao-cai.pmtiles", "bcb55cf3217d5510cee1ac0b5dff24a59026efbe14566eef4241eb5ae944b19f", 8, 14),
        VietnamRegionSpec("19", "Thái Nguyên", "19-thai-nguyen.pmtiles", "5221bd12823fadde7dcd2a80d6a318b29b12ba9f5b4ed4a61804f0767d8d5824", 8, 14),
        VietnamRegionSpec("20", "Lạng Sơn", "20-lang-son.pmtiles", "89ea18af7678d7116263a26132e2dca48cdb39aee5dfa3be683256073a6c1a3e", 8, 14),
        VietnamRegionSpec("22", "Quảng Ninh", "22-quang-ninh.pmtiles", "00c5bb1a0d4b347f68305f689826f4ab3c6b5daaeaed7d5918589651f37afda2", 8, 14),
        VietnamRegionSpec("24", "Bắc Ninh", "24-bac-ninh.pmtiles", "6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f", 8, 14),
        VietnamRegionSpec("25", "Phú Thọ", "25-phu-tho.pmtiles", "47d45365a7c48d29cc544aca1173f36465c361b6cb44adbef2aeba3c24d56ae7", 8, 14),
        VietnamRegionSpec("31", "Hải Phòng", "31-hai-phong.pmtiles", "e8de939a3550f2854b18cc162c1477cc06e31cbc63633d0584d41dba9d035b61", 8, 14),
        VietnamRegionSpec("33", "Hưng Yên", "33-hung-yen.pmtiles", "4f030715264ad9ad743525e5f7f0b7cfda635df65c816b63d73ea1effc8c7c5e", 8, 14),
        VietnamRegionSpec("37", "Ninh Bình", "37-ninh-binh.pmtiles", "ac78bc087c880c44f60532a7ea5361405d797f91b5a3eaa9dd8c51151d31a2cf", 8, 14),
        VietnamRegionSpec("38", "Thanh Hóa", "38-thanh-hoa.pmtiles", "c5d6806f919858a18e519b5ac88e7ea200b13b818fcb474ebdea9ca2308ddb66", 8, 14),
        VietnamRegionSpec("40", "Nghệ An", "40-nghe-an.pmtiles", "76233dbd3c7c1cf1d211c6ca74b227603bcb60a30430dbc72253e042168ea6b9", 8, 14),
        VietnamRegionSpec("42", "Hà Tĩnh", "42-ha-tinh.pmtiles", "0351197dd2f45efdd44fbb8f1522468af52659abcf4e57ddd65dd150eb78559a", 8, 14),
        VietnamRegionSpec("44", "Quảng Trị", "44-quang-tri.pmtiles", "f263cd7801333e700da7af19c36ed68a790b180e32ea7cd962ee655ced2e8169", 8, 14),
        VietnamRegionSpec("46", "Huế", "46-hue.pmtiles", "e19f3bbda33d13eb92a199c559c1cee1a1591fc50ae5fdbbdebe99e1221c8530", 8, 14),
        VietnamRegionSpec("48", "Đà Nẵng", "48-da-nang.pmtiles", "5af82e965177320433ddc6f5d45281c157d0232910e1d27c13d1fd9479fed8ed", 8, 14),
        VietnamRegionSpec("51", "Quảng Ngãi", "51-quang-ngai.pmtiles", "9bdf9617b5e681a27113efadc69295aae16941599151774d31449e5e07ac366f", 8, 14),
        VietnamRegionSpec("52", "Gia Lai", "52-gia-lai.pmtiles", "dafd0a3134417ff4f59b430561b090cda9d8343c662d3f14e518f767c39e2735", 8, 14),
        VietnamRegionSpec("56", "Khánh Hòa", "56-khanh-hoa.pmtiles", "22d04ceaf3096244f07a9ab34ec2a9e80dae0060ebe881853e8d9715521fe7a3", 8, 14),
        VietnamRegionSpec("66", "Đắk Lắk", "66-dak-lak.pmtiles", "dad4cebbc08fd7b1344bb4b0a630745d021bf3f902ff1b1267ac90010717c759", 8, 14),
        VietnamRegionSpec("68", "Lâm Đồng", "68-lam-dong.pmtiles", "8fc0b91652b1dfdeda06c6958dcee66f95b6f88fbcdba2e9202f95d6c0c062be", 8, 14),
        VietnamRegionSpec("75", "Đồng Nai", "75-dong-nai.pmtiles", "343492655dbd9ee04effe9474aa7e81717daa8d01344f6a102ac8a3276e4bf52", 8, 14),
        VietnamRegionSpec("79", "Hồ Chí Minh", "79-ho-chi-minh.pmtiles", "8a8634f1414f9953ca1fe30ce9e52cb5c8e897e3211a4637392f30094b6c214d", 8, 14),
        VietnamRegionSpec("80", "Tây Ninh", "80-tay-ninh.pmtiles", "4bd04d0054762f095f591d120d827c92bccd9f7ac4ee46334590cc70621bb0e8", 8, 14),
        VietnamRegionSpec("82", "Đồng Tháp", "82-dong-thap.pmtiles", "55a8c6985586acd8dc0cd2772b0f45639f763d8b648a16936f14d26bf2e591b3", 8, 14),
        VietnamRegionSpec("86", "Vĩnh Long", "86-vinh-long.pmtiles", "663ffe6ac9872b2d31132ccac54298abb74c1fbef3440c6cc55cf22ceef043ce", 8, 14),
        VietnamRegionSpec("91", "An Giang", "91-an-giang.pmtiles", "b588bd38ccca57aa96ee5fa8e867cd75454a02683a55b8dcba6fae9b85a48a1c", 8, 14),
        VietnamRegionSpec("92", "Cần Thơ", "92-can-tho.pmtiles", "80bc9c4e87cc0cb3d696c88c0f042f4287525b7d10a9d2574090e69778184289", 8, 14),
        VietnamRegionSpec("96", "Cà Mau", "96-ca-mau.pmtiles", "95134a5d7cad6ae10596cb34a8046bfc4aab0f5f83d87a2126ef31f19552055f", 8, 14)
    )
}
