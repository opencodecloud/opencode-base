package cloud.opencode.base.crypto.key;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SecretKeyUtil
 *
* 
* @author Leon Soo
 * @since JDK 25, OpenCode-Base-Crypto V1.0.0
 */
@DisplayName("SecretKeyUtil Tests")
class SecretKeyUtilTest {

    @Test
    @DisplayName("Should generate AES key")
    void testGenerateAesKey() {
        SecretKey key = SecretKeyUtil.generate("AES", 256);
        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
        assertEquals(32, key.getEncoded().length);
    }

    @Test
    @DisplayName("Should create secret key from bytes")
    void testFromBytes() {
        byte[] keyBytes = new byte[32];
        SecretKey key = SecretKeyUtil.fromBytes(keyBytes, "AES");
        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
        assertArrayEquals(keyBytes, key.getEncoded());
    }

    @Test
    @DisplayName("Should convert secret key to bytes")
    void testToBytes() {
        SecretKey key = SecretKeyUtil.generate("AES", 256);
        byte[] bytes = SecretKeyUtil.toBytes(key);
        assertNotNull(bytes);
        assertEquals(32, bytes.length);
    }

    @Test
    @DisplayName("Should derive key from master key")
    void testDeriveKey() {
        byte[] masterKey = "test-master-key".getBytes();
        byte[] salt = "test-salt".getBytes();

        SecretKey derivedKey = SecretKeyUtil.derive(masterKey, salt, "AES", 256);
        assertNotNull(derivedKey);
        assertEquals("AES", derivedKey.getAlgorithm());
        assertEquals(32, derivedKey.getEncoded().length);

        // Deriving again with same inputs should produce same key
        SecretKey derivedKey2 = SecretKeyUtil.derive(masterKey, salt, "AES", 256);
        assertArrayEquals(derivedKey.getEncoded(), derivedKey2.getEncoded());

        // Different salt should produce different key
        byte[] salt2 = "different-salt".getBytes();
        SecretKey derivedKey3 = SecretKeyUtil.derive(masterKey, salt2, "AES", 256);
        assertFalse(java.util.Arrays.equals(derivedKey.getEncoded(), derivedKey3.getEncoded()));
    }

    @Test
    @DisplayName("Should derive key from password")
    void testDeriveFromPassword() {
        char[] password = "test-password".toCharArray();
        byte[] salt = "test-salt".getBytes();

        SecretKey derivedKey = SecretKeyUtil.deriveFromPassword(password, salt, "AES", 256);
        assertNotNull(derivedKey);
        assertEquals("AES", derivedKey.getAlgorithm());
        assertEquals(32, derivedKey.getEncoded().length);
    }

    @Test
    @DisplayName("Should check key equality")
    void testEquals() {
        byte[] keyBytes = new byte[32];
        SecretKey key1 = SecretKeyUtil.fromBytes(keyBytes, "AES");
        SecretKey key2 = SecretKeyUtil.fromBytes(keyBytes, "AES");

        assertTrue(SecretKeyUtil.equals(key1, key2));

        SecretKey key3 = SecretKeyUtil.generate("AES", 256);
        assertFalse(SecretKeyUtil.equals(key1, key3));
    }
}
