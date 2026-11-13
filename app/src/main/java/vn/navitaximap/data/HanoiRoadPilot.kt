package vn.navitaximap.data

import android.content.Context
import java.io.File

/**
 * Tiny real OSM Hanoi road-only sample. No routing/navigation approval.
 *
 * Source: OSM API bbox 105.849,21.031,105.858,21.037,
 * snapshot 2026-11-13, ODbL 1.0, © OpenStreetMap contributors.
 * Raw source and deterministic vector build: HANOI_OSM_PILOT.md.
 */
object HanoiRoadPilot {
    private const val ASSET = "hanoi_real_roads.pmtiles"
    private var cachedStore: CatalogPackStore? = null

    @Synchronized
    private fun store(context: Context): CatalogPackStore {
        return cachedStore ?: CatalogPackStore(
            File(context.applicationContext.filesDir, "catalog-packs")
        ).also { cachedStore = it }
    }

    val spec: CatalogPackSpec get() = PilotCatalog.hanoiRoads

    /** Existing verified catalog file or locally bundled hash-pinned copy. */
    fun install(context: Context): File {
        val storage = store(context)
        storage.verifiedCurrent(spec)?.let { return it.file }
        return context.assets.open(ASSET).use { input ->
            storage.install(spec, input, "BUNDLED").file
        }
    }

    /** Explicit user-requested network action; no automatic background downloads. */
    fun download(
        context: Context,
        cancelled: () -> Boolean = { false },
        onProgress: (Long, Long) -> Unit = { _, _ -> }
    ): CatalogPackStore.Installed =
        PinnedHttpsPackDownloader(store(context)).download(spec, cancelled, onProgress)

    fun installed(context: Context): CatalogPackStore.Installed? =
        store(context).verifiedCurrent(spec)

    fun versionInstalled(context: Context): Int? =
        store(context).versionInstalled(spec)

    fun delete(context: Context): Boolean = store(context).delete(spec)

    fun recover(context: Context): Int = store(context).recoverOrphans(spec)
}
