package vn.navitaximap.data

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * Performs package-integrity checks before activation. Not a PMTiles decoder.
 * An SHA-256 verified file is not automatically an approved or usable map.
 */
object OfflinePackVerifier {
    fun isValidSha256(hex: String): Boolean =
        hex.length == 64 && hex.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }

    fun verify(file: File, expectedSha256: String): Boolean {
        if (!file.isFile || !isValidSha256(expectedSha256)) return false
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val bytes = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(bytes)
                if (count < 0) break
                digest.update(bytes, 0, count)
            }
        }
        val hash = digest.digest().joinToString("") { "%02x".format(it) }
        return hash.equals(expectedSha256, ignoreCase = true)
    }
}
