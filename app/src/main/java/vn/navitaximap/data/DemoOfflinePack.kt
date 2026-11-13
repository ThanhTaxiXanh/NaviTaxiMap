package vn.navitaximap.data

import android.content.Context
import android.util.Base64
import java.io.File
import java.security.MessageDigest

/**
 * Only for the synthetic 560-byte PMTiles v3 fixture in debug/prototype builds.
 * Installs it to app-private storage because MapLibre's PMTiles reader needs
 * seekable file:// storage, not an Android asset stream.
 */
object DemoOfflinePack {
    private const val ASSET = "offline_demo.pmtiles.b64"
    private const val HASH = "860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407"

    fun install(context: Context): File {
        val target = File(context.filesDir, "offline_demo.pmtiles")
        if (OfflinePackVerifier.verify(target, HASH)) return target

        val encoded = context.assets.open(ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val content = Base64.decode(encoded.trim(), Base64.DEFAULT)
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(content).joinToString("") { "%02x".format(it) }
        require(digest == HASH) { "Offline diagnostic fixture checksum mismatch" }

        val temporary = File(context.filesDir, "offline_demo.pmtiles.tmp")
        try {
            temporary.outputStream().use { it.write(content) }
            check(OfflinePackVerifier.verify(temporary, HASH)) { "PMTiles write validation failed" }
            if (target.exists()) check(target.delete()) { "Old invalid fixture could not be replaced" }
            check(temporary.renameTo(target)) { "Unable to activate fixture" }
            return target
        } finally {
            if (temporary.exists()) temporary.delete()
        }
    }
}
