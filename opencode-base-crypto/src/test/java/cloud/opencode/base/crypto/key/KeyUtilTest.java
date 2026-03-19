package cloud.opencode.base.crypto.key;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for KeyUtil
 *
* 
* @author Leon Soo
 * @since JDK 25, OpenCode-Base-Crypto V1.0.0
 */
@DisplayName("KeyUtil Tests")
class KeyUtilTest {

    @Test
    @DisplayName("Should get AES key size")
    void testGetAesKeySize() {
        SecretKey key = KeyGenerator.generateAes256Key();
        int size = KeyUtil.getKeySize(key);
        assertEquals(256, size);
    }

    @Test
    @DisplayName("Should get RSA key size")
    void testGetRsaKeySize() {
        KeyPair keyPair = KeyGenerator.generateRsa2048KeyPair();
        int size = KeyUtil.getKeySize(keyPair.getPublic());
        assertEquals(2048, size);
    }

    @Test
    @DisplayName("Should get key algorithm")
    void testGetAlgorithm() {
        SecretKey key = KeyGenerator.generateAes256Key();
        String algorithm = KeyUtil.getAlgorithm(key);
        assertEquals("AES", algorithm);
    }

    @Test
    @DisplayName("Should check if key is private key")
    void testIsPrivateKey() {
        KeyPair keyPair = KeyGenerator.generateRsa2048KeyPair();
        assertTrue(KeyUtil.isPrivateKey(keyPair.getPrivate()));
        assertFalse(KeyUtil.isPrivateKey(keyPair.getPublic()));
    }

    @Test
    @DisplayName("Should check if key is public key")
    void testIsPublicKey() {
        KeyPair keyPair = KeyGenerator.generateRsa2048KeyPair();
        assertTrue(KeyUtil.isPublicKey(keyPair.getPublic()));
        assertFalse(KeyUtil.isPublicKey(keyPair.getPrivate()));
    }

    @Test
    @DisplayName("Should check if key is secret key")
    void testIsSecretKey() {
        SecretKey key = KeyGenerator.generateAes256Key();
        KeyPair keyPair = KeyGenerator.generateRsa2048KeyPair();

        assertTrue(KeyUtil.isSecretKey(key));
        assertFalse(KeyUtil.isSecretKey(keyPair.getPublic()));
        assertFalse(KeyUtil.isSecretKey(keyPair.getPrivate()));
    }

    @Test
    @DisplayName("Should get encoded key bytes")
    void testGetEncoded() {
        SecretKey key = KeyGenerator.generateAes256Key();
        byte[] encoded = KeyUtil.getEncoded(key);
        assertNotNull(encoded);
        assertEquals(32, encoded.length);
    }

    @Test
    @DisplayName("Should get key format")
    void testGetFormat() {
        KeyPair keyPair = KeyGenerator.generateRsa2048KeyPair();
        String format = KeyUtil.getFormat(keyPair.getPublic());
        assertEquals("X.509", format);
    }
}
