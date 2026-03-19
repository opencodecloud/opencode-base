package cloud.opencode.base.crypto.key;

import cloud.opencode.base.crypto.enums.CurveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for KeyGenerator
 *
* 
* @author Leon Soo
 * @since JDK 25, OpenCode-Base-Crypto V1.0.0
 */
@DisplayName("KeyGenerator Tests")
class KeyGeneratorTest {

    @Nested
    @DisplayName("Symmetric Key Generation Tests")
    class SymmetricKeyTests {

        @Test
        @DisplayName("Should generate AES-128 key")
        void testGenerateAes128Key() {
            SecretKey key = KeyGenerator.generateAes128Key();
            assertNotNull(key);
            assertEquals("AES", key.getAlgorithm());
            assertEquals(128 / 8, key.getEncoded().length);
        }

        @Test
        @DisplayName("Should generate AES-256 key")
        void testGenerateAes256Key() {
            SecretKey key = KeyGenerator.generateAes256Key();
            assertNotNull(key);
            assertEquals("AES", key.getAlgorithm());
            assertEquals(256 / 8, key.getEncoded().length);
        }

        @Test
        @DisplayName("Should generate ChaCha20 key")
        void testGenerateChacha20Key() {
            SecretKey key = KeyGenerator.generateChacha20Key();
            assertNotNull(key);
            assertEquals("ChaCha20", key.getAlgorithm());
        }

        @Test
        @DisplayName("Should create secret key from bytes")
        void testSecretKeyFromBytes() {
            byte[] keyBytes = new byte[32];
            SecretKey key = KeyGenerator.secretKey(keyBytes, "AES");
            assertNotNull(key);
            assertEquals("AES", key.getAlgorithm());
            assertArrayEquals(keyBytes, key.getEncoded());
        }
    }

    @Nested
    @DisplayName("Asymmetric Key Generation Tests")
    class AsymmetricKeyTests {

        @Test
        @DisplayName("Should generate RSA-2048 key pair")
        void testGenerateRsa2048KeyPair() {
            KeyPair keyPair = KeyGenerator.generateRsa2048KeyPair();
            assertNotNull(keyPair);
            assertNotNull(keyPair.getPublic());
            assertNotNull(keyPair.getPrivate());
            assertEquals("RSA", keyPair.getPublic().getAlgorithm());
        }

        @Test
        @DisplayName("Should generate P-256 key pair")
        void testGenerateP256KeyPair() {
            KeyPair keyPair = KeyGenerator.generateP256KeyPair();
            assertNotNull(keyPair);
            assertNotNull(keyPair.getPublic());
            assertNotNull(keyPair.getPrivate());
            assertEquals("EC", keyPair.getPublic().getAlgorithm());
        }

        @Test
        @DisplayName("Should generate Ed25519 key pair")
        void testGenerateEd25519KeyPair() {
            KeyPair keyPair = KeyGenerator.generateEd25519KeyPair();
            assertNotNull(keyPair);
            assertNotNull(keyPair.getPublic());
            assertNotNull(keyPair.getPrivate());
            // JDK 25 reports Ed25519 keys as "EdDSA"
            assertTrue(keyPair.getPublic().getAlgorithm().equals("Ed25519") ||
                       keyPair.getPublic().getAlgorithm().equals("EdDSA"));
        }
    }

    @Nested
    @DisplayName("Key Import/Export Tests")
    class ImportExportTests {

        @Test
        @DisplayName("Should export and import RSA public key PEM")
        void testRsaPublicKeyPemRoundTrip() {
            KeyPair keyPair = KeyGenerator.generateRsa2048KeyPair();
            String pem = KeyGenerator.exportPublicKeyPem(keyPair.getPublic());

            assertNotNull(pem);
            assertTrue(pem.contains("BEGIN PUBLIC KEY"));
            assertTrue(pem.contains("END PUBLIC KEY"));

            var importedKey = KeyGenerator.importPublicKeyPem(pem);
            assertNotNull(importedKey);
            assertEquals("RSA", importedKey.getAlgorithm());
        }

        @Test
        @DisplayName("Should export and import RSA private key PEM")
        void testRsaPrivateKeyPemRoundTrip() {
            KeyPair keyPair = KeyGenerator.generateRsa2048KeyPair();
            String pem = KeyGenerator.exportPrivateKeyPem(keyPair.getPrivate());

            assertNotNull(pem);
            assertTrue(pem.contains("BEGIN PRIVATE KEY"));
            assertTrue(pem.contains("END PRIVATE KEY"));

            var importedKey = KeyGenerator.importPrivateKeyPem(pem);
            assertNotNull(importedKey);
            assertEquals("RSA", importedKey.getAlgorithm());
        }

        @Test
        @DisplayName("Should export and import key pair PEM")
        void testKeyPairPemRoundTrip() {
            KeyPair originalKeyPair = KeyGenerator.generateRsa2048KeyPair();
            String pem = KeyGenerator.exportKeyPairPem(originalKeyPair);

            assertNotNull(pem);
            assertTrue(pem.contains("BEGIN PUBLIC KEY"));
            assertTrue(pem.contains("BEGIN PRIVATE KEY"));
        }
    }
}
