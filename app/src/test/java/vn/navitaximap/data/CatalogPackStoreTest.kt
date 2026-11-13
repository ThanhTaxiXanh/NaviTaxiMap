package vn.navitaximap.data

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InterruptedIOException
import java.security.MessageDigest
import java.util.Properties

class CatalogPackStoreTest {
    private val sample by lazy { File("src/main/assets/hanoi_real_roads.pmtiles").readBytes() }
    private val spec get() = PilotCatalog.hanoiRoads

    private fun withStore(test: (CatalogPackStore, File) -> Unit) {
        val root = createTempDir(prefix = "navitaxi-catalog-")
        try { test(CatalogPackStore(root), root) } finally { root.deleteRecursively() }
    }

    @Test fun sourceIsRealPinnedOsmAndHasExpectedHash() {
        val actual = MessageDigest.getInstance("SHA-256").digest(sample)
            .joinToString("") { "%02x".format(it) }
        assertEquals(53_956L, sample.size.toLong())
        assertEquals(spec.sha256, actual)
        assertEquals("ODbL-1.0", spec.license)
        assertEquals("© OpenStreetMap contributors", spec.attribution)
        assertEquals(14, PmtilesHeader.inspect(File("src/main/assets/hanoi_real_roads.pmtiles")).minZoom)
        assertFalse(spec.downloadUrl.contains("/NaviTaxiMap/main/"))
        assertTrue(spec.downloadUrl.contains("/554cfe62013c80146842aea85cb3625e583f2ced/"))
    }

    @Test fun installsAndPersistsVersionProvenanceWithoutNavigationApproval() = withStore { store, root ->
        val first = store.install(spec, ByteArrayInputStream(sample), "BUNDLED")
        assertEquals(spec.version, first.version)
        assertEquals(spec.sha256, first.sha256)
        assertEquals("BUNDLED", first.origin)
        assertTrue(first.file.isFile)
        assertEquals(spec.archiveName, first.file.name)
        assertEquals(spec.version, store.versionInstalled(spec))
        assertNotNull(store.verifiedCurrent(spec))
        val properties = Properties().apply {
            File(root, spec.indexName).inputStream().use { load(it) }
        }
        assertEquals("false", properties.getProperty("navigationApproved"))
        assertEquals("UNREVIEWED_VISUAL_PREVIEW", properties.getProperty("sourceApproval"))
        assertEquals(spec.sourceXmlSha256, properties.getProperty("sourceXmlSha256"))
        assertEquals("ODbL-1.0", properties.getProperty("license"))
        assertEquals(spec.attribution, properties.getProperty("attribution"))
    }

    @Test fun failedUpgradeKeepsPreviousVersionAndCleansStaging() = withStore { store, root ->
        val stable = store.install(spec, ByteArrayInputStream(sample), "BUNDLED")
        val wrong = spec.copy(version = 2, sha256 = "0".repeat(64))
        assertThrows(IllegalArgumentException::class.java) {
            store.install(wrong, ByteArrayInputStream(sample), "PINNED_HTTPS")
        }
        assertEquals(1, store.versionInstalled(spec))
        assertTrue(stable.file.isFile)
        assertNotNull(store.verifiedCurrent(spec))
        assertEquals(2, root.listFiles()!!.size)
        assertFalse(root.listFiles()!!.any { it.name.endsWith(".partial") })
    }

    @Test fun completedUpgradeAtomicallyPromotesNewVersionAndRetiresOld() = withStore { store, root ->
        val old = store.install(spec, ByteArrayInputStream(sample), "BUNDLED")
        val newer = spec.copy(version = 2)
        val upgraded = store.install(newer, ByteArrayInputStream(sample), "PINNED_HTTPS")
        assertEquals(2, store.versionInstalled(spec))
        assertEquals(2, upgraded.version)
        assertEquals("PINNED_HTTPS", upgraded.origin)
        assertTrue(store.verifiedCurrent(newer)?.file?.isFile == true)
        assertNull(store.verifiedCurrent(spec))
        assertFalse(old.file.exists())
        assertEquals(2, root.listFiles()!!.size)
    }

    @Test fun corruptedBytesRejectedBeforeActivationAndCanBeReinstalled() = withStore { store, _ ->
        val original = store.install(spec, ByteArrayInputStream(sample), "BUNDLED")
        val broken = original.file.readBytes()
        broken[broken.lastIndex] = (broken.last().toInt() xor 0x01).toByte()
        original.file.writeBytes(broken)
        assertNull(store.verifiedCurrent(spec))
        val repaired = store.install(spec, ByteArrayInputStream(sample), "BUNDLED")
        assertNotNull(store.verifiedCurrent(spec))
        assertArrayEquals(sample, repaired.file.readBytes())
    }

    @Test fun cancellationDoesNotExposePartialPack() = withStore { store, root ->
        assertThrows(InterruptedIOException::class.java) {
            store.install(spec, ByteArrayInputStream(sample), "PINNED_HTTPS",
                cancelled = { true })
        }
        assertNull(store.versionInstalled(spec))
        assertNull(store.verifiedCurrent(spec))
        assertTrue(root.listFiles().isNullOrEmpty())
    }

    @Test fun diskPressureRejectsStreamWithoutOrphan() {
        val root = createTempDir(prefix = "navitaxi-catalog-space-")
        try {
            val store = CatalogPackStore(root, minimumFreeBytes = 1000, freeBytes = { 1000L })
            assertThrows(IllegalArgumentException::class.java) {
                store.install(spec, ByteArrayInputStream(sample), "PINNED_HTTPS")
            }
            assertTrue(root.listFiles().isNullOrEmpty())
        } finally { root.deleteRecursively() }
    }

    @Test fun recoveryKeepsValidEntryAndRemovesGeneratedArtifactsOnly() = withStore { store, root ->
        val pack = store.install(spec, ByteArrayInputStream(sample), "BUNDLED")
        val orphan = File(root, "${spec.id}-v2-${spec.sha256.take(12)}.pmtiles")
        orphan.writeBytes(sample)
        val staged = File(root, "${spec.archiveName}.partial")
        staged.writeText("failed copy")
        val stagedIndex = File(root, "${spec.indexName}.partial")
        stagedIndex.writeText("failed index")
        val unrelated = File(root, "someone-else.partial")
        unrelated.writeText("leave me alone")
        assertEquals(3, store.recoverOrphans(spec))
        assertEquals(0, store.recoverOrphans(spec))
        assertNotNull(store.verifiedCurrent(spec))
        assertTrue(pack.file.exists())
        assertTrue(unrelated.exists())
    }

    @Test fun metadataMustRemainUnreviewedAndSafe() = withStore { store, root ->
        store.install(spec, ByteArrayInputStream(sample), "BUNDLED")
        val file = File(root, spec.indexName)
        val props = Properties().apply { file.inputStream().use { load(it) } }
        props.setProperty("navigationApproved", "true")
        file.outputStream().use { props.store(it, "tampered") }
        assertNull(store.verifiedCurrent(spec))
        assertNull(store.versionInstalled(spec))
        props.setProperty("navigationApproved", "false")
        props.setProperty("file", "../secrets.pmtiles")
        file.outputStream().use { props.store(it, "tampered") }
        assertNull(store.verifiedCurrent(spec))
    }

    @Test fun downloadedBytesArePinnedAndNoSecondFetchIsRequired() = withStore { store, root ->
        var opened = 0
        val downloader = PinnedHttpsPackDownloader(store) {
            opened++
            ByteArrayInputStream(sample)
        }
        val progress = ArrayList<Long>()
        val first = downloader.download(spec, onProgress = { copied, _ -> progress.add(copied) })
        assertEquals("PINNED_HTTPS", first.origin)
        assertTrue(progress.last() == spec.sizeBytes)
        val second = downloader.download(spec)
        assertEquals(first.file, second.file)
        assertEquals(1, opened)
        assertEquals(2, root.listFiles()!!.size)
    }

    @Test fun forbidsNonImmutableOrInsecureCatalogUrls() {
        val source = spec.downloadUrl
        val mutated = listOf(
            source.replace("https://", "http://"),
            source.replace("raw.githubusercontent.com", "example.com"),
            source.replace("554cfe62013c80146842aea85cb3625e583f2ced", "main"),
            "$source?download=1",
            source.replace("https://", "https://user:pass@"),
            source.replace(".pmtiles", ".zip")
        )
        for (url in mutated) {
            assertThrows(IllegalArgumentException::class.java) { spec.copy(downloadUrl = url) }
        }
    }

    @Test fun deletionOnlyAffectsCatalogCopy() = withStore { store, root ->
        val unrelated = File(root, "external-user-map.pmtiles")
        unrelated.writeText("not a catalog file")
        val pack = store.install(spec, ByteArrayInputStream(sample), "BUNDLED")
        assertTrue(store.delete(spec))
        assertNull(store.verifiedCurrent(spec))
        assertFalse(pack.file.exists())
        assertTrue(unrelated.isFile)
        assertFalse(store.delete(spec))
    }
}
