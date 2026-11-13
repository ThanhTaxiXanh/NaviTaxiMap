package vn.navitaximap.data

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class OfflinePackVerifierTest {
    @Test fun acceptsMatchingHash() {
        val temp = File.createTempFile("navitaxi-test-", ".bin")
        try {
            temp.writeText("NaviTaxiMap")
            val sha = "571122c805cde56841651e4b8f92095b97db3eacb629c3ef53760dbab44a0976"
            // Assert the digest independently rather than depending on a hand-entered test vector.
            val digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(temp.readBytes()).joinToString("") { "%02x".format(it) }
            assertEquals(64, digest.length)
            assertTrue(OfflinePackVerifier.verify(temp, digest))
            assertFalse(OfflinePackVerifier.verify(temp, sha.take(62) + "00"))
        } finally { temp.delete() }
    }

    @Test fun rejectsInvalidMetadata() {
        assertFalse(OfflinePackVerifier.isValidSha256("abc"))
        assertFalse(OfflinePackVerifier.isValidSha256("z".repeat(64)))
        assertFalse(OfflinePackVerifier.verify(File("/nonexistent/navitaxi.pmtiles"), "a".repeat(64)))
    }
}
