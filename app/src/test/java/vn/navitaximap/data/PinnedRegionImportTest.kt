package vn.navitaximap.data

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

/** Pin checking must reject wrong size/checksum before committing an archive/index. */
class PinnedRegionImportTest {
    private val archive: ByteArray by lazy {
        File("src/main/assets/hanoi_real_roads.pmtiles").readBytes()
    }
    @Test fun expectedSizeAndShaMustBothMatchForStagedImport() {
        val dir = createTempDir(prefix = "navitaxi-pinned-region-")
        try {
            val store = LocalPackStore(dir)
            val hash = "ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d"
            assertThrows(IllegalArgumentException::class.java) {
                store.importPack(ByteArrayInputStream(archive), "test.pmtiles",
                    expectedSha256 = hash, expectedSizeBytes = archive.size.toLong() - 1L)
            }
            assertTrue(dir.listFiles().isNullOrEmpty())
            assertThrows(IllegalArgumentException::class.java) {
                store.importPack(ByteArrayInputStream(archive), "test.pmtiles",
                    expectedSha256 = "0".repeat(64), expectedSizeBytes = archive.size.toLong())
            }
            assertTrue(dir.listFiles().isNullOrEmpty())
            val pack = store.importPack(ByteArrayInputStream(archive), "test.pmtiles",
                expectedSha256 = hash, expectedSizeBytes = archive.size.toLong())
            assertEquals(archive.size.toLong(), pack.sizeBytes)
            assertTrue(store.verifyPack(pack.id))
        } finally { dir.deleteRecursively() }
    }
}
