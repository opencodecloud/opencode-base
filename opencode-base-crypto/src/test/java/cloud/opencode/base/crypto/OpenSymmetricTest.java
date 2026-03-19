package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.key.KeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for OpenSymmetric
 *
 * @author Leon Soo
 * @since JDK 25, OpenCode-Base-Crypto V1.0.0
 */
@DisplayName("OpenSymmetric Tests")
class OpenSymmetricTest {

    private static final String PLAINTEXT = "Hello, OpenCode Crypto!";
    private static final byte[] PLAINTEXT_BYTES = PLAINTEXT.getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("AES-CBC Tests")
    class AesCbcTests {

        @Test
        @DisplayName("Should encrypt and decrypt with AES-CBC")
        void testAesCbcEncryptDecrypt() {
            SecretKey key = KeyGenerator.generateAes256Key();
            OpenSymmetric cipher = OpenSymmetric.aesCbc();
            byte[] iv = cipher.generateIv();

            cipher.setKey(key).setIv(iv);

            byte[] ciphertext = cipher.encrypt(PLAINTEXT_BYTES);
            assertNotNull(ciphertext);
            assertNotEquals(PLAINTEXT_BYTES.length, ciphertext.length); // Padding added

            byte[] decrypted = cipher.decrypt(ciphertext);
            assertArrayEquals(PLAINTEXT_BYTES, decrypted);
        }

        @Test
        @DisplayName("Should encrypt and decrypt string with AES-CBC")
        void testAesCbcEncryptDecryptString() {
            SecretKey key = KeyGenerator.generateAes256Key();
            OpenSymmetric cipher = OpenSymmetric.aesCbc();
            byte[] iv = cipher.generateIv();

            cipher.setKey(key).setIv(iv);

            byte[] ciphertext = cipher.encrypt(PLAINTEXT);
            String decrypted = cipher.decryptToString(ciphertext);

            assertEquals(PLAINTEXT, decrypted);
        }

        @Test
        @DisplayName("Should encrypt to hex and decrypt from hex")
        void testAesCbcHexEncoding() {
            SecretKey key = KeyGenerator.generateAes256Key();
            OpenSymmetric cipher = OpenSymmetric.aesCbc();
            byte[] iv = cipher.generateIv();

            cipher.setKey(key).setIv(iv);

            String hexCiphertext = cipher.encryptHex(PLAINTEXT);
            assertNotNull(hexCiphertext);
            assertTrue(hexCiphertext.matches("[0-9a-f]+"));

            String decrypted = cipher.decryptHexToString(hexCiphertext);
            assertEquals(PLAINTEXT, decrypted);
        }

        @Test
        @DisplayName("Should encrypt to Base64 and decrypt from Base64")
        void testAesCbcBase64Encoding() {
            SecretKey key = KeyGenerator.generateAes256Key();
            OpenSymmetric cipher = OpenSymmetric.aesCbc();
            byte[] iv = cipher.generateIv();

            cipher.setKey(key).setIv(iv);

            String base64Ciphertext = cipher.encryptBase64(PLAINTEXT);
            assertNotNull(base64Ciphertext);

            String decrypted = cipher.decryptBase64ToString(base64Ciphertext);
            assertEquals(PLAINTEXT, decrypted);
        }
    }

    @Nested
    @DisplayName("AES-CTR Tests")
    class AesCtrTests {

        @Test
        @DisplayName("Should encrypt and decrypt with AES-CTR")
        void testAesCtrEncryptDecrypt() {
            SecretKey key = KeyGenerator.generateAes256Key();
            OpenSymmetric cipher = OpenSymmetric.aesCtr();
            byte[] iv = cipher.generateIv();

            cipher.setKey(key).setIv(iv);

            byte[] ciphertext = cipher.encrypt(PLAINTEXT_BYTES);
            assertNotNull(ciphertext);
            // CTR no padding, but IV is prepended (16 bytes)
            assertEquals(PLAINTEXT_BYTES.length + 16, ciphertext.length);

            byte[] decrypted = cipher.decrypt(ciphertext);
            assertArrayEquals(PLAINTEXT_BYTES, decrypted);
        }

        @Test
        @DisplayName("Should encrypt and decrypt string with AES-CTR")
        void testAesCtrEncryptDecryptString() {
            SecretKey key = KeyGenerator.generateAes256Key();
            OpenSymmetric cipher = OpenSymmetric.aesCtr();
            byte[] iv = cipher.generateIv();

            cipher.setKey(key).setIv(iv);

            String hexCiphertext = cipher.encryptHex(PLAINTEXT);
            String decrypted = cipher.decryptHexToString(hexCiphertext);

            assertEquals(PLAINTEXT, decrypted);
        }
    }

    @Nested
    @DisplayName("Key Generation Tests")
    class KeyGenerationTests {

        @Test
        @DisplayName("Should generate valid IV for AES-CBC")
        void testGenerateIvAesCbc() {
            OpenSymmetric cipher = OpenSymmetric.aesCbc();
            byte[] iv = cipher.generateIv();

            assertNotNull(iv);
            assertEquals(16, iv.length); // AES block size
        }

        @Test
        @DisplayName("Should generate valid IV for AES-CTR")
        void testGenerateIvAesCtr() {
            OpenSymmetric cipher = OpenSymmetric.aesCtr();
            byte[] iv = cipher.generateIv();

            assertNotNull(iv);
            assertEquals(16, iv.length);
        }

        @Test
        @DisplayName("Should generate 128-bit key")
        void testGenerateKey128() {
            OpenSymmetric cipher = OpenSymmetric.aesCbc();
            SecretKey key = cipher.generateKey(128);

            assertNotNull(key);
            assertEquals(16, key.getEncoded().length);
        }

        @Test
        @DisplayName("Should generate 256-bit key")
        void testGenerateKey256() {
            OpenSymmetric cipher = OpenSymmetric.aesCbc();
            SecretKey key = cipher.generateKey(256);

            assertNotNull(key);
            assertEquals(32, key.getEncoded().length);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw when key not set")
        void testEncryptWithoutKey() {
            OpenSymmetric cipher = OpenSymmetric.aesCbc();
            byte[] iv = cipher.generateIv();
            cipher.setIv(iv);

            assertThrows(Exception.class, () -> cipher.encrypt(PLAINTEXT_BYTES));
        }

        @Test
        @DisplayName("Should throw on null plaintext")
        void testEncryptNullPlaintext() {
            OpenSymmetric cipher = OpenSymmetric.aesCbc();

            assertThrows(NullPointerException.class, () -> cipher.encrypt((byte[]) null));
        }

        @Test
        @DisplayName("Should throw on null key")
        void testSetNullKey() {
            OpenSymmetric cipher = OpenSymmetric.aesCbc();

            assertThrows(NullPointerException.class, () -> cipher.setKey((SecretKey) null));
        }

        @Test
        @DisplayName("Should throw on null IV")
        void testSetNullIv() {
            OpenSymmetric cipher = OpenSymmetric.aesCbc();

            assertThrows(NullPointerException.class, () -> cipher.setIv(null));
        }
    }

    @Nested
    @DisplayName("Info Methods Tests")
    class InfoMethodsTests {

        @Test
        @DisplayName("Should return correct algorithm name for AES-CBC")
        void testGetAlgorithmAesCbc() {
            OpenSymmetric cipher = OpenSymmetric.aesCbc();
            assertTrue(cipher.getAlgorithm().contains("AES"));
            assertTrue(cipher.getAlgorithm().contains("CBC"));
        }

        @Test
        @DisplayName("Should return correct algorithm name for AES-CTR")
        void testGetAlgorithmAesCtr() {
            OpenSymmetric cipher = OpenSymmetric.aesCtr();
            assertTrue(cipher.getAlgorithm().contains("AES"));
            assertTrue(cipher.getAlgorithm().contains("CTR"));
        }

        @Test
        @DisplayName("Should return correct IV length")
        void testGetIvLength() {
            OpenSymmetric cbcCipher = OpenSymmetric.aesCbc();
            assertEquals(16, cbcCipher.getIvLength());

            OpenSymmetric ctrCipher = OpenSymmetric.aesCtr();
            assertEquals(16, ctrCipher.getIvLength());
        }
    }
}
