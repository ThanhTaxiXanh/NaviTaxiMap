package vn.navitaximap.data

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Bundled small real OSM road-only preview. Never mark as navigation-approved.
 *
 * Frozen OSM bbox: 105.849..105.858 E, 21.031..21.037 N
 * Snapshot SHA-256: db180ccb4a19e287888af400513dff2104d4c692b32d060a8416f1b6c08c7697
 * © OpenStreetMap contributors, Open Database License 1.0 (ODbL).
 * The road-only MVT archive does not include a route graph or border assertions.
 */
object HanoiRoadPilot {
    private const val ASSET = "hanoi_real_roads.pmtiles"
    private const val HASH = "ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d"

    fun install(context: Context): File {
        val destination = File(context.filesDir, "hanoi_real_roads_preview.pmtiles")
        if (OfflinePackVerifier.verify(destination, HASH)) return destination
        val partial = File(context.filesDir, "hanoi_real_roads_preview.pmtiles.partial")
        try {
            context.assets.open(ASSET).use { input ->
                FileOutputStream(partial).use { output ->
                    input.copyTo(output, bufferSize = 64 * 1024)
                    output.fd.sync()
                }
            }
            check(OfflinePackVerifier.verify(partial, HASH)) {
                "Hanoi OSM preview package checksum mismatch"
            }
            val header = PmtilesHeader.inspect(partial)
            check(header.minZoom == 14 && header.maxZoom == 17) {
                "Unexpected zoom range in OSM pilot"
            }
            Files.move(partial.toPath(), destination.toPath(),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            return destination
        } finally {
            partial.delete()
        }
    }
}
