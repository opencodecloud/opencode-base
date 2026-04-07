package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.Hasher;
import cloud.opencode.base.hash.exception.OpenHashException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.*;

/**
 * HmacHashFunction tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.3
 */
@DisplayName("HmacHashFunction Tests")
class HmacHashFunctionTest {

    private static final byte[] TEST_KEY = "secret-key-for-testing".getBytes(StandardCharsets.UTF_8);
    private static final byte[] TEST_MESSAGE = "Hello, World!".getBytes(StandardCharsets.UTF_8);
    private static final HexFormat HEX = HexFormat.of();

    /**
     * Computes reference HMAC using javax.crypto.Mac directly
     */
    private static byte[] referenceHmac(String algorithm, byte[] key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data);
    }

    @Nested
    @DisplayName("HMAC-SHA256 Tests")
    class HmacSha256Test {

        @Test
        @DisplayName("should produce correct HMAC-SHA256 for known input")
        void testKnownVector() throws Exception {
            HashFunction hmac = HmacHashFunction.hmacSha256(TEST_KEY);
            HashCode hash = hmac.hashBytes(TEST_MESSAGE);

            byte[] expected = referenceHmac("HmacSHA256", TEST_KEY, TEST_MESSAGE);
            assertThat(hash.asBytes()).isEqualTo(expected);
        }

        @Test
        @DisplayName("should have 256 bits output")
        void testBits() {
            HashFunction hmac = HmacHashFunction.hmacSha256(TEST_KEY);
            assertThat(hmac.bits()).isEqualTo(256);
        }

        @Test
        @DisplayName("different keys should produce different results")
        void testDifferentKeys() {
            HashFunction hmac1 = HmacHashFunction.hmacSha256("key-one".getBytes(StandardCharsets.UTF_8));
            HashFunction hmac2 = HmacHashFunction.hmacSha256("key-two".getBytes(StandardCharsets.UTF_8));

            HashCode h1 = hmac1.hashBytes(TEST_MESSAGE);
            HashCode h2 = hmac2.hashBytes(TEST_MESSAGE);

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("should handle empty input")
        void testEmptyInput() throws Exception {
            HashFunction hmac = HmacHashFunction.hmacSha256(TEST_KEY);
            HashCode hash = hmac.hashBytes(new byte[0]);

            byte[] expected = referenceHmac("HmacSHA256", TEST_KEY, new byte[0]);
            assertThat(hash.asBytes()).isEqualTo(expected);
        }

        @Test
        @DisplayName("streaming via Hasher should match direct hash")
        void testStreamingHasher() {
            HashFunction hmac = HmacHashFunction.hmacSha256(TEST_KEY);

            HashCode direct = hmac.hashBytes(TEST_MESSAGE);

            Hasher hasher = hmac.newHasher();
            hasher.putBytes(TEST_MESSAGE);
            HashCode streamed = hasher.hash();

            assertThat(streamed).isEqualTo(direct);
        }

        @Test
        @DisplayName("streaming byte-by-byte should match direct hash")
        void testStreamingByteByByte() {
            HashFunction hmac = HmacHashFunction.hmacSha256(TEST_KEY);

            HashCode direct = hmac.hashBytes(TEST_MESSAGE);

            Hasher hasher = hmac.newHasher();
            for (byte b : TEST_MESSAGE) {
                hasher.putByte(b);
            }
            HashCode streamed = hasher.hash();

            assertThat(streamed).isEqualTo(direct);
        }

        @Test
        @DisplayName("hashUtf8 should match hashBytes with UTF-8 encoding")
        void testHashUtf8() throws Exception {
            HashFunction hmac = HmacHashFunction.hmacSha256(TEST_KEY);

            HashCode hash = hmac.hashUtf8("Hello, World!");
            byte[] expected = referenceHmac("HmacSHA256", TEST_KEY, TEST_MESSAGE);

            assertThat(hash.asBytes()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("HMAC-SHA512 Tests")
    class HmacSha512Test {

        @Test
        @DisplayName("should produce correct HMAC-SHA512 for known input")
        void testKnownVector() throws Exception {
            HashFunction hmac = HmacHashFunction.hmacSha512(TEST_KEY);
            HashCode hash = hmac.hashBytes(TEST_MESSAGE);

            byte[] expected = referenceHmac("HmacSHA512", TEST_KEY, TEST_MESSAGE);
            assertThat(hash.asBytes()).isEqualTo(expected);
        }

        @Test
        @DisplayName("should have 512 bits output")
        void testBits() {
            HashFunction hmac = HmacHashFunction.hmacSha512(TEST_KEY);
            assertThat(hmac.bits()).isEqualTo(512);
        }

        @Test
        @DisplayName("should handle empty input")
        void testEmptyInput() throws Exception {
            HashFunction hmac = HmacHashFunction.hmacSha512(TEST_KEY);
            HashCode hash = hmac.hashBytes(new byte[0]);

            byte[] expected = referenceHmac("HmacSHA512", TEST_KEY, new byte[0]);
            assertThat(hash.asBytes()).isEqualTo(expected);
        }

        @Test
        @DisplayName("streaming via Hasher should match direct hash")
        void testStreamingHasher() {
            HashFunction hmac = HmacHashFunction.hmacSha512(TEST_KEY);

            HashCode direct = hmac.hashBytes(TEST_MESSAGE);

            Hasher hasher = hmac.newHasher();
            hasher.putBytes(TEST_MESSAGE);
            HashCode streamed = hasher.hash();

            assertThat(streamed).isEqualTo(direct);
        }
    }

    @Nested
    @DisplayName("HMAC-MD5 Tests")
    class HmacMd5Test {

        @Test
        @DisplayName("should produce correct HMAC-MD5 for known input")
        void testKnownVector() throws Exception {
            HashFunction hmac = HmacHashFunction.hmacMd5(TEST_KEY);
            HashCode hash = hmac.hashBytes(TEST_MESSAGE);

            byte[] expected = referenceHmac("HmacMD5", TEST_KEY, TEST_MESSAGE);
            assertThat(hash.asBytes()).isEqualTo(expected);
        }

        @Test
        @DisplayName("should have 128 bits output")
        void testBits() {
            HashFunction hmac = HmacHashFunction.hmacMd5(TEST_KEY);
            assertThat(hmac.bits()).isEqualTo(128);
        }

        @Test
        @DisplayName("streaming via Hasher should match direct hash")
        void testStreamingHasher() {
            HashFunction hmac = HmacHashFunction.hmacMd5(TEST_KEY);

            HashCode direct = hmac.hashBytes(TEST_MESSAGE);

            Hasher hasher = hmac.newHasher();
            hasher.putBytes(TEST_MESSAGE);
            HashCode streamed = hasher.hash();

            assertThat(streamed).isEqualTo(direct);
        }
    }

    @Nested
    @DisplayName("HMAC-SHA1 Tests")
    class HmacSha1Test {

        @Test
        @DisplayName("should produce correct HMAC-SHA1 for known input")
        void testKnownVector() throws Exception {
            HashFunction hmac = HmacHashFunction.hmacSha1(TEST_KEY);
            HashCode hash = hmac.hashBytes(TEST_MESSAGE);

            byte[] expected = referenceHmac("HmacSHA1", TEST_KEY, TEST_MESSAGE);
            assertThat(hash.asBytes()).isEqualTo(expected);
        }

        @Test
        @DisplayName("should have 160 bits output")
        void testBits() {
            HashFunction hmac = HmacHashFunction.hmacSha1(TEST_KEY);
            assertThat(hmac.bits()).isEqualTo(160);
        }

        @Test
        @DisplayName("streaming via Hasher should match direct hash")
        void testStreamingHasher() {
            HashFunction hmac = HmacHashFunction.hmacSha1(TEST_KEY);

            HashCode direct = hmac.hashBytes(TEST_MESSAGE);

            Hasher hasher = hmac.newHasher();
            hasher.putBytes(TEST_MESSAGE);
            HashCode streamed = hasher.hash();

            assertThat(streamed).isEqualTo(direct);
        }
    }

    @Nested
    @DisplayName("HMAC-SHA384 Tests")
    class HmacSha384Test {

        @Test
        @DisplayName("should produce correct HMAC-SHA384 for known input")
        void testKnownVector() throws Exception {
            HashFunction hmac = HmacHashFunction.hmacSha384(TEST_KEY);
            HashCode hash = hmac.hashBytes(TEST_MESSAGE);

            byte[] expected = referenceHmac("HmacSHA384", TEST_KEY, TEST_MESSAGE);
            assertThat(hash.asBytes()).isEqualTo(expected);
        }

        @Test
        @DisplayName("should have 384 bits output")
        void testBits() {
            HashFunction hmac = HmacHashFunction.hmacSha384(TEST_KEY);
            assertThat(hmac.bits()).isEqualTo(384);
        }

        @Test
        @DisplayName("streaming via Hasher should match direct hash")
        void testStreamingHasher() {
            HashFunction hmac = HmacHashFunction.hmacSha384(TEST_KEY);

            HashCode direct = hmac.hashBytes(TEST_MESSAGE);

            Hasher hasher = hmac.newHasher();
            hasher.putBytes(TEST_MESSAGE);
            HashCode streamed = hasher.hash();

            assertThat(streamed).isEqualTo(direct);
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTest {

        @Test
        @DisplayName("toString should not contain key bytes")
        void testToStringDoesNotContainKey() {
            byte[] key = "my-super-secret-key".getBytes(StandardCharsets.UTF_8);
            HashFunction hmac = HmacHashFunction.hmacSha256(key);

            String str = hmac.toString();

            assertThat(str).doesNotContain("my-super-secret-key");
            assertThat(str).doesNotContain(HEX.formatHex(key));
            assertThat(str).contains("HmacSHA256");
            assertThat(str).contains("256");
        }

        @Test
        @DisplayName("toString format should be name[bits]")
        void testToStringFormat() {
            HashFunction hmac = HmacHashFunction.hmacSha512(TEST_KEY);
            assertThat(hmac.toString()).isEqualTo("HmacSHA512[512]");
        }

        @Test
        @DisplayName("should reject null key")
        void testNullKey() {
            assertThatThrownBy(() -> HmacHashFunction.hmacSha256(null))
                    .isInstanceOf(OpenHashException.class);
        }

        @Test
        @DisplayName("should reject empty key")
        void testEmptyKey() {
            assertThatThrownBy(() -> HmacHashFunction.hmacSha256(new byte[0]))
                    .isInstanceOf(OpenHashException.class);
        }

        @Test
        @DisplayName("modifying original key should not affect hash function")
        void testKeyCloned() throws Exception {
            byte[] key = "mutable-key".getBytes(StandardCharsets.UTF_8);
            HashFunction hmac = HmacHashFunction.hmacSha256(key);

            HashCode before = hmac.hashBytes(TEST_MESSAGE);

            // Modify the original key array
            key[0] = 0;
            key[1] = 0;

            HashCode after = hmac.hashBytes(TEST_MESSAGE);

            assertThat(after).isEqualTo(before);

            // Verify against reference with original key
            byte[] originalKey = "mutable-key".getBytes(StandardCharsets.UTF_8);
            byte[] expected = referenceHmac("HmacSHA256", originalKey, TEST_MESSAGE);
            assertThat(after.asBytes()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTest {

        @Test
        @DisplayName("multiple threads should compute same HMAC for same input")
        void testConcurrentHashing() throws Exception {
            HashFunction hmac = HmacHashFunction.hmacSha256(TEST_KEY);
            HashCode expected = hmac.hashBytes(TEST_MESSAGE);

            int threadCount = 16;
            CyclicBarrier barrier = new CyclicBarrier(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            try {
                List<Future<HashCode>> futures = new ArrayList<>();
                for (int i = 0; i < threadCount; i++) {
                    futures.add(executor.submit(() -> {
                        barrier.await();
                        return hmac.hashBytes(TEST_MESSAGE);
                    }));
                }

                for (Future<HashCode> future : futures) {
                    assertThat(future.get()).isEqualTo(expected);
                }
            } finally {
                executor.shutdown();
            }
        }

        @Test
        @DisplayName("multiple threads using separate Hashers should produce correct results")
        void testConcurrentHashers() throws Exception {
            HashFunction hmac = HmacHashFunction.hmacSha256(TEST_KEY);
            HashCode expected = hmac.hashBytes(TEST_MESSAGE);

            int threadCount = 16;
            CyclicBarrier barrier = new CyclicBarrier(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            try {
                List<Future<HashCode>> futures = new ArrayList<>();
                for (int i = 0; i < threadCount; i++) {
                    futures.add(executor.submit(() -> {
                        barrier.await();
                        Hasher hasher = hmac.newHasher();
                        hasher.putBytes(TEST_MESSAGE);
                        return hasher.hash();
                    }));
                }

                for (Future<HashCode> future : futures) {
                    assertThat(future.get()).isEqualTo(expected);
                }
            } finally {
                executor.shutdown();
            }
        }
    }

    @Nested
    @DisplayName("RFC 4231 Test Vectors")
    class Rfc4231Test {

        private static final HexFormat HEX = HexFormat.of();

        @Test
        @DisplayName("HMAC-SHA256 RFC 4231 Test Case 2 - 'Jefe' key")
        void testHmacSha256Rfc4231Case2() {
            // RFC 4231 Test Case 2
            byte[] key = "Jefe".getBytes(StandardCharsets.US_ASCII);
            byte[] data = "what do ya want for nothing?".getBytes(StandardCharsets.US_ASCII);
            String expected = "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843";

            HashCode hash = HmacHashFunction.hmacSha256(key).hashBytes(data);
            assertThat(hash.toHex()).isEqualTo(expected);
        }

        @Test
        @DisplayName("HMAC-SHA512 RFC 4231 Test Case 2 - 'Jefe' key")
        void testHmacSha512Rfc4231Case2() {
            byte[] key = "Jefe".getBytes(StandardCharsets.US_ASCII);
            byte[] data = "what do ya want for nothing?".getBytes(StandardCharsets.US_ASCII);
            String expected = "164b7a7bfcf819e2e395fbe73b56e0a387bd64222e831fd610270cd7ea250554"
                    + "9758bf75c05a994a6d034f65f8f0e6fdcaeab1a34d4a6b4b636e070a38bce737";

            HashCode hash = HmacHashFunction.hmacSha512(key).hashBytes(data);
            assertThat(hash.toHex()).isEqualTo(expected);
        }

        @Test
        @DisplayName("HMAC-SHA1 RFC 2202 Test Case 2 - 'Jefe' key")
        void testHmacSha1Rfc2202Case2() {
            byte[] key = "Jefe".getBytes(StandardCharsets.US_ASCII);
            byte[] data = "what do ya want for nothing?".getBytes(StandardCharsets.US_ASCII);
            String expected = "effcdf6ae5eb2fa2d27416d5f184df9c259a7c79";

            HashCode hash = HmacHashFunction.hmacSha1(key).hashBytes(data);
            assertThat(hash.toHex()).isEqualTo(expected);
        }

        @Test
        @DisplayName("HMAC-SHA384 RFC 4231 Test Case 2 - 'Jefe' key")
        void testHmacSha384Rfc4231Case2() {
            byte[] key = "Jefe".getBytes(StandardCharsets.US_ASCII);
            byte[] data = "what do ya want for nothing?".getBytes(StandardCharsets.US_ASCII);
            String expected = "af45d2e376484031617f78d2b58a6b1b9c7ef464f5a01b47e42ec3736322445e"
                    + "8e2240ca5e69e2c78b3239ecfab21649";

            HashCode hash = HmacHashFunction.hmacSha384(key).hashBytes(data);
            assertThat(hash.toHex()).isEqualTo(expected);
        }
    }
}
