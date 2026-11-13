package vn.navitaximap.data

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Base64

class LocalPackStoreTest {
    private fun fixture(): ByteArray {
        val source = File("src/main/assets/offline_demo.pmtiles.b64")
        return Base64.getMimeDecoder().decode(source.readText().trim())
    }

    private fun withStore(test: (LocalPackStore, File) -> Unit) {
        val root = createTempDir(prefix = "navitaxi-packs-")
        try { test(LocalPackStore(root), root) } finally { root.deleteRecursively() }
    }

    @Test fun importsValidFixtureAndVerifiesChecksum() = withStore { store, root ->
        val data = fixture()
        val pack = store.importPack(ByteArrayInputStream(data), "Bản đồ thử nghiệm")
        assertEquals(data.size.toLong(), pack.sizeBytes)
        assertEquals(0, pack.minZoom)
        assertEquals(1, pack.maxZoom)
        assertFalse(pack.providedChecksumMatched)
        assertEquals(1, store.listPacks().size)
        assertTrue(store.verifyPack(pack.id))

        val file = File(root, "${pack.id}.pmtiles")
        val mutated = file.readBytes()
        mutated[mutated.lastIndex] = (mutated.last().toInt() xor 1).toByte()
        file.writeBytes(mutated)
        assertFalse(store.verifyPack(pack.id))
        assertTrue(store.deletePack(pack.id))
        assertTrue(store.listPacks().isEmpty())
    }

    @Test fun acceptsSuppliedMatchingShaAndRejectsWrongOne() = withStore { store, root ->
        val bytes = fixture()
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(bytes).joinToString("") { "%02x".format(it) }
        val pack = store.importPack(ByteArrayInputStream(bytes), "verified", digest)
        assertTrue(pack.providedChecksumMatched)
        assertTrue(store.verifyPack(pack.id))
        assertThrows(IllegalArgumentException::class.java) {
            store.importPack(ByteArrayInputStream(bytes), "wrong", "f".repeat(64))
        }
        assertEquals(2, root.listFiles()?.size)
    }

    @Test fun rejectsTruncatedMagicAndInvalidHeaderBounds() = withStore { store, root ->
        assertThrows(IllegalArgumentException::class.java) {
            store.importPack(ByteArrayInputStream(byteArrayOf(0, 1, 2)), "bad")
        }
        val bad = fixture()
        bad[0] = 0
        assertThrows(IllegalArgumentException::class.java) {
            store.importPack(ByteArrayInputStream(bad), "bad magic")
        }
        val badRange = fixture()
        ByteBuffer.wrap(badRange).order(ByteOrder.LITTLE_ENDIAN).putLong(8, 100_000L)
        assertThrows(IllegalArgumentException::class.java) {
            store.importPack(ByteArrayInputStream(badRange), "bad root offset")
        }
        assertEquals(0, root.listFiles()?.size)
    }

    @Test fun rejectsOversizeWithoutLeavingPartialFiles() {
        val root = createTempDir(prefix = "navitaxi-limit-")
        try {
            val store = LocalPackStore(root, maxBytes = 100)
            assertThrows(IllegalArgumentException::class.java) {
                store.importPack(ByteArrayInputStream(fixture()), "too large")
            }
            assertTrue(root.listFiles().isNullOrEmpty())
        } finally { root.deleteRecursively() }
    }

    @Test fun refusesPathTraversalId() = withStore { store, root ->
        assertFalse(store.deletePack("../outside"))
        assertFalse(store.verifyPack("../outside"))
        assertTrue(root.listFiles().isNullOrEmpty())
    }

    @Test fun cleansPartialOrphanArchiveAndDanglingIndexButPreservesValidPack() = withStore { store, root ->
        val pack = store.importPack(ByteArrayInputStream(fixture()), "valid")
        val stageId = "11111111-1111-1111-1111-111111111111"
        val orphanId = "22222222-2222-2222-2222-222222222222"
        val indexId = "33333333-3333-3333-3333-333333333333"
        File(root, "$stageId.partial").writeText("interrupted stream")
        File(root, "$stageId.properties.partial").writeText("interrupted metadata")
        File(root, "$orphanId.pmtiles").writeBytes(fixture())
        File(root, "$indexId.properties").writeText("orphaned index")
        File(root, "somebody-else.partial").writeText("unrelated")
        val result = store.recoverInterruptedImports()
        assertEquals(2, result.discardedStaging)
        assertEquals(1, result.discardedOrphanArchives)
        assertEquals(1, result.discardedOrphanIndexes)
        assertTrue(result.reclaimedBytes > 0)
        assertEquals(1, store.listPacks().size)
        assertTrue(store.verifyPack(pack.id))
        assertTrue(File(root, "somebody-else.partial").exists())
        assertEquals(0, store.recoverInterruptedImports().removedCount)
    }

    @Test fun refusesInsufficientFreeSpaceAndCleansStaging() {
        val root = createTempDir(prefix = "navitaxi-disk-")
        try {
            val store = LocalPackStore(root, minimumFreeBytes = 128, freeBytes = { 128L })
            val error = assertThrows(IllegalArgumentException::class.java) {
                store.importPack(ByteArrayInputStream(fixture()), "disk full")
            }
            assertTrue(error.message?.contains("dung lượng") == true)
            assertTrue(root.listFiles().isNullOrEmpty())
        } finally { root.deleteRecursively() }
    }

    @Test fun partialStreamFailureLeavesNoVisiblePackOrLeftovers() = withStore { store, root ->
        val bytes = fixture()
        val failing = object : java.io.InputStream() {
            var pos = 0
            override fun read(): Int {
                if (pos >= 70) throw java.io.IOException("simulated SAF read failure")
                return bytes[pos++].toInt() and 0xff
            }
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                if (pos >= 70) throw java.io.IOException("simulated SAF read failure")
                val n = minOf(length, 70 - pos)
                bytes.copyInto(buffer, offset, pos, pos + n)
                pos += n
                return n
            }
        }
        assertThrows(java.io.IOException::class.java) {
            store.importPack(failing, "interrupted")
        }
        assertTrue(root.listFiles().isNullOrEmpty())
        assertTrue(store.listPacks().isEmpty())
    }

    @Test fun storageUsageReflectsOnlyCommittedPacks() = withStore { store, root ->
        val before = store.storageUsage()
        assertEquals(0L, before.first)
        val pack = store.importPack(ByteArrayInputStream(fixture()), "size")
        val after = store.storageUsage()
        assertEquals(pack.sizeBytes, after.first)
        assertTrue(after.second > 0)
        store.deletePack(pack.id)
        assertEquals(0L, store.storageUsage().first)
    }
}
