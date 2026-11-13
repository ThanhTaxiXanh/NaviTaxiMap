package vn.navitaximap.data

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class Vietnam35CatalogTest {
    @Test fun all35HashPinsMatchTheSourceChecksumFile() {
        val checks = File("../tools/vietnam34/SHA256SUMS.txt").readLines()
            .filter { it.isNotBlank() }.associate { row ->
                val parts = row.trim().split(Regex("\\s+"))
                parts[1] to parts[0]
            }
        val entries = Vietnam35Catalog.packages
        assertEquals(35, entries.size)
        assertEquals(35, checks.size)
        assertEquals(checks.keys, entries.map { it.fileName }.toSet())
        entries.forEach {
            assertEquals(checks[it.fileName], it.sha256)
            assertTrue(it.sizeBytes > 127L)
        }
        assertEquals(556_229_998L, entries.first { it.code == "VN" }.sizeBytes)
        assertEquals(287_504_805L, entries.filter { it.code != "VN" }.sumOf { it.sizeBytes })
        assertFalse(Vietnam35Catalog.navigationApproved)
        assertFalse(Vietnam35Catalog.distributionApproved)
    }

    @Test fun mustMatchExactNameHashAndZoom() {
        val entry = Vietnam35Catalog.packages.first { it.code == "24" }
        val good = LocalPackStore.Pack("id", entry.fileName, entry.sizeBytes, entry.sha256,
            entry.minZoom, entry.maxZoom, false)
        assertTrue(entry.matches(good))
        assertFalse(entry.matches(good.copy(name = "fake.pmtiles")))
        assertFalse(entry.matches(good.copy(sizeBytes = entry.sizeBytes - 1)))
        assertFalse(entry.matches(good.copy(sha256 = "0".repeat(64))))
        assertFalse(entry.matches(good.copy(minZoom = 0)))
    }

    @Test fun styleUsesOnlyLocalSources() {
        val json = File("src/main/assets/shortbread_offline_style.json").readText()
        assertTrue(json.contains("\"__PMTILES_URL__\""))
        assertTrue(json.contains("\"streets\""))
        assertTrue(json.contains("\"water_polygons\""))
        assertFalse(json.contains("http://"))
        assertFalse(json.contains("https://"))
        assertFalse(json.contains("\"glyphs\""))
        assertFalse(json.contains("\"sprite\""))
        assertFalse(json.contains("\"source-layer\": \"boundaries\""))
    }
}
