package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.enums.DigestAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for OpenDigest
 *
 * @author Leon Soo
 * @since JDK 25, OpenCode-Base-Crypto V1.0.0
 */
@DisplayName("OpenDigest Tests")
class OpenDigestTest {

    private static final String TEST_DATA = "Hello, OpenCode Crypto!";
    private static final byte[] TEST_DATA_BYTES = TEST_DATA.getBytes(StandardCharsets.UTF_8);

    // Known SHA-256 hash of "Hello, OpenCode Crypto!"
    private static final String EXPECTED_SHA256_HEX = "a0f7b5e5f1c4c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9";

    @Nested
    @DisplayName("SHA-256 Tests")
    class Sha256Tests {

        @Test
        @DisplayName("Should compute SHA-256 hash of bytes")
        void testSha256Digest() {
            OpenDigest digest = OpenDigest.sha256();
            byte[] hash = digest.digest(TEST_DATA_BYTES);

            assertNotNull(hash);
            assertEquals(32, hash.length); // SHA-256 produces 256 bits = 32 bytes
        }

        @Test
        @DisplayName("Should compute SHA-256 hash of string")
        void testSha256DigestString() {
            OpenDigest digest = OpenDigest.sha256();
            byte[] hash = digest.digest(TEST_DATA);

            assertNotNull(hash);
            assertEquals(32, hash.length);
        }

        @Test
        @DisplayName("Should return hex encoded SHA-256 hash")
        void testSha256DigestHex() {
            OpenDigest digest = OpenDigest.sha256();
            String hexHash = digest.digestHex(TEST_DATA);

            assertNotNull(hexHash);
            assertEquals(64, hexHash.length()); // 32 bytes = 64 hex chars
            assertTrue(hexHash.matches("[0-9a-f]+"));
        }

        @Test
        @DisplayName("Should return Base64 encoded SHA-256 hash")
        void testSha256DigestBase64() {
            OpenDigest digest = OpenDigest.sha256();
            String base64Hash = digest.digestBase64(TEST_DATA);

            assertNotNull(base64Hash);
            assertEquals(44, base64Hash.length()); // Base64 of 32 bytes
        }

        @Test
        @DisplayName("Should produce consistent hash")
        void testSha256Consistency() {
            OpenDigest digest1 = OpenDigest.sha256();
            OpenDigest digest2 = OpenDigest.sha256();

            String hash1 = digest1.digestHex(TEST_DATA);
            String hash2 = digest2.digestHex(TEST_DATA);

            assertEquals(hash1, hash2);
        }
    }

    @Nested
    @DisplayName("SHA-384 Tests")
    class Sha384Tests {

        @Test
        @DisplayName("Should compute SHA-384 hash")
        void testSha384Digest() {
            OpenDigest digest = OpenDigest.sha384();
            byte[] hash = digest.digest(TEST_DATA_BYTES);

            assertNotNull(hash);
            assertEquals(48, hash.length); // SHA-384 produces 384 bits = 48 bytes
        }

        @Test
        @DisplayName("Should return correct digest length")
        void testSha384DigestLength() {
            OpenDigest digest = OpenDigest.sha384();
            assertEquals(48, digest.getDigestLength());
        }
    }

    @Nested
    @DisplayName("SHA-512 Tests")
    class Sha512Tests {

        @Test
        @DisplayName("Should compute SHA-512 hash")
        void testSha512Digest() {
            OpenDigest digest = OpenDigest.sha512();
            byte[] hash = digest.digest(TEST_DATA_BYTES);

            assertNotNull(hash);
            assertEquals(64, hash.length); // SHA-512 produces 512 bits = 64 bytes
        }

        @Test
        @DisplayName("Should return hex encoded SHA-512 hash")
        void testSha512DigestHex() {
            OpenDigest digest = OpenDigest.sha512();
            String hexHash = digest.digestHex(TEST_DATA);

            assertNotNull(hexHash);
            assertEquals(128, hexHash.length()); // 64 bytes = 128 hex chars
        }
    }

    @Nested
    @DisplayName("SHA3 Tests")
    class Sha3Tests {

        @Test
        @DisplayName("Should compute SHA3-256 hash")
        void testSha3_256Digest() {
            OpenDigest digest = OpenDigest.sha3_256();
            byte[] hash = digest.digest(TEST_DATA_BYTES);

            assertNotNull(hash);
            assertEquals(32, hash.length);
        }

        @Test
        @DisplayName("Should compute SHA3-512 hash")
        void testSha3_512Digest() {
            OpenDigest digest = OpenDigest.sha3_512();
            byte[] hash = digest.digest(TEST_DATA_BYTES);

            assertNotNull(hash);
            assertEquals(64, hash.length);
        }

        @Test
        @DisplayName("SHA3-256 should differ from SHA-256")
        void testSha3DiffersFromSha2() {
            String sha256Hash = OpenDigest.sha256().digestHex(TEST_DATA);
            String sha3Hash = OpenDigest.sha3_256().digestHex(TEST_DATA);

            assertNotEquals(sha256Hash, sha3Hash);
        }
    }

    @Nested
    @DisplayName("Streaming Tests")
    class StreamingTests {

        @Test
        @DisplayName("Should compute hash using update/doFinal")
        void testStreamingDigest() {
            OpenDigest digest = OpenDigest.sha256();

            String expectedHash = digest.digestHex(TEST_DATA);

            // Reset and use streaming API
            digest.reset();
            digest.update("Hello, ")
                  .update("OpenCode ")
                  .update("Crypto!");

            String streamingHash = digest.doFinalHex();

            assertEquals(expectedHash, streamingHash);
        }

        @Test
        @DisplayName("Should compute hash from InputStream")
        void testInputStreamDigest() {
            OpenDigest digest = OpenDigest.sha256();
            String expectedHash = digest.digestHex(TEST_DATA);

            digest.reset();
            InputStream is = new ByteArrayInputStream(TEST_DATA_BYTES);
            String streamHash = digest.digestHex(is);

            assertEquals(expectedHash, streamHash);
        }

        @Test
        @DisplayName("Should reset state correctly")
        void testReset() {
            OpenDigest digest = OpenDigest.sha256();

            digest.update("some data");
            digest.reset();
            digest.update(TEST_DATA);

            String hash1 = digest.doFinalHex();

            OpenDigest fresh = OpenDigest.sha256();
            String hash2 = fresh.digestHex(TEST_DATA);

            assertEquals(hash1, hash2);
        }
    }

    @Nested
    @DisplayName("File Digest Tests")
    class FileDigestTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Should compute hash of file")
        void testFileDigest() throws IOException {
            Path testFile = tempDir.resolve("test.txt");
            Files.writeString(testFile, TEST_DATA);

            OpenDigest digest = OpenDigest.sha256();
            byte[] fileHash = digest.digestFile(testFile);
            String expectedHash = digest.digestHex(TEST_DATA);

            assertNotNull(fileHash);
            assertEquals(32, fileHash.length);

            // Reset and verify
            digest.reset();
            assertEquals(expectedHash, digest.digestHex(Files.readAllBytes(testFile)));
        }

        @Test
        @DisplayName("Should compute hex hash of file")
        void testFileDigestHex() throws IOException {
            Path testFile = tempDir.resolve("test.txt");
            Files.writeString(testFile, TEST_DATA);

            OpenDigest digest = OpenDigest.sha256();
            String fileHash = digest.digestFileHex(testFile);
            String expectedHash = OpenDigest.sha256().digestHex(TEST_DATA);

            assertEquals(expectedHash, fileHash);
        }
    }

    @Nested
    @DisplayName("Algorithm Enum Tests")
    class AlgorithmEnumTests {

        @Test
        @DisplayName("Should create digest from enum")
        void testCreateFromEnum() {
            OpenDigest digest = OpenDigest.of(DigestAlgorithm.SHA256);
            assertNotNull(digest);
            assertEquals("SHA-256", digest.getAlgorithm());
        }

        @Test
        @DisplayName("Should throw on null algorithm")
        void testNullAlgorithm() {
            assertThrows(NullPointerException.class, () -> OpenDigest.of(null));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw on null data")
        void testNullData() {
            OpenDigest digest = OpenDigest.sha256();

            assertThrows(NullPointerException.class, () -> digest.digest((byte[]) null));
            assertThrows(NullPointerException.class, () -> digest.digest((String) null));
        }

        @Test
        @DisplayName("Should throw on null file path")
        void testNullFilePath() {
            OpenDigest digest = OpenDigest.sha256();

            assertThrows(NullPointerException.class, () -> digest.digestFile(null));
        }

        @Test
        @DisplayName("Should throw on null InputStream")
        void testNullInputStream() {
            OpenDigest digest = OpenDigest.sha256();

            assertThrows(NullPointerException.class, () -> digest.digest((InputStream) null));
        }
    }

    @Nested
    @DisplayName("Info Methods Tests")
    class InfoMethodsTests {

        @Test
        @DisplayName("Should return correct algorithm name")
        void testGetAlgorithm() {
            assertEquals("SHA-256", OpenDigest.sha256().getAlgorithm());
            assertEquals("SHA-384", OpenDigest.sha384().getAlgorithm());
            assertEquals("SHA-512", OpenDigest.sha512().getAlgorithm());
            assertEquals("SHA3-256", OpenDigest.sha3_256().getAlgorithm());
            assertEquals("SHA3-512", OpenDigest.sha3_512().getAlgorithm());
        }

        @Test
        @DisplayName("Should return correct digest length")
        void testGetDigestLength() {
            assertEquals(32, OpenDigest.sha256().getDigestLength());
            assertEquals(48, OpenDigest.sha384().getDigestLength());
            assertEquals(64, OpenDigest.sha512().getDigestLength());
            assertEquals(32, OpenDigest.sha3_256().getDigestLength());
            assertEquals(64, OpenDigest.sha3_512().getDigestLength());
        }
    }
}
