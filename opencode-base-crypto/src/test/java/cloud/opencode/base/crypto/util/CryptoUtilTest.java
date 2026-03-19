package cloud.opencode.base.crypto.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link CryptoUtil}.
 * CryptoUtil单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("CryptoUtil Tests / CryptoUtil测试")
class CryptoUtilTest {

    @Nested
    @DisplayName("Constant Time Comparison Tests / 常量时间比较测试")
    class ConstantTimeComparisonTests {

        @Test
        @DisplayName("constantTimeEquals(byte[], byte[])相等数组返回true")
        void testConstantTimeEqualsBytesEqual() {
            byte[] a = "Hello".getBytes();
            byte[] b = "Hello".getBytes();
            boolean result = CryptoUtil.constantTimeEquals(a, b);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("constantTimeEquals(byte[], byte[])不等数组返回false")
        void testConstantTimeEqualsBytesNotEqual() {
            byte[] a = "Hello".getBytes();
            byte[] b = "World".getBytes();
            boolean result = CryptoUtil.constantTimeEquals(a, b);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("constantTimeEquals(byte[], byte[])不同长度数组返回false")
        void testConstantTimeEqualsBytesDifferentLength() {
            byte[] a = "Hello".getBytes();
            byte[] b = "Hi".getBytes();
            boolean result = CryptoUtil.constantTimeEquals(a, b);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("constantTimeEquals(null, null)返回true")
        void testConstantTimeEqualsBytesNullNull() {
            boolean result = CryptoUtil.constantTimeEquals((byte[]) null, null);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("constantTimeEquals(null, byte[])返回false")
        void testConstantTimeEqualsBytesNullArray() {
            boolean result = CryptoUtil.constantTimeEquals(null, "test".getBytes());
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("constantTimeEquals(String, String)相等字符串返回true")
        void testConstantTimeEqualsStringsEqual() {
            boolean result = CryptoUtil.constantTimeEquals("Hello", "Hello");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("constantTimeEquals(String, String)不等字符串返回false")
        void testConstantTimeEqualsStringsNotEqual() {
            boolean result = CryptoUtil.constantTimeEquals("Hello", "World");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("constantTimeEquals(String null, String null)返回true")
        void testConstantTimeEqualsStringsNullNull() {
            boolean result = CryptoUtil.constantTimeEquals((String) null, null);
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Secure Erase Tests / 安全擦除测试")
    class SecureEraseTests {

        @Test
        @DisplayName("secureErase(byte[])将数组置零")
        void testSecureEraseBytes() {
            byte[] data = "SensitiveData".getBytes();
            CryptoUtil.secureErase(data);
            for (byte b : data) {
                assertThat(b).isEqualTo((byte) 0);
            }
        }

        @Test
        @DisplayName("secureErase(null byte[])不抛出异常")
        void testSecureEraseBytesNull() {
            assertThatNoException().isThrownBy(() -> CryptoUtil.secureErase((byte[]) null));
        }

        @Test
        @DisplayName("secureErase(char[])将数组置零")
        void testSecureEraseChars() {
            char[] data = "Password123!".toCharArray();
            CryptoUtil.secureErase(data);
            for (char c : data) {
                assertThat(c).isEqualTo('\0');
            }
        }

        @Test
        @DisplayName("secureErase(null char[])不抛出异常")
        void testSecureEraseCharsNull() {
            assertThatNoException().isThrownBy(() -> CryptoUtil.secureErase((char[]) null));
        }

        @Test
        @DisplayName("secureErase(ByteBuffer)将缓冲区置零")
        void testSecureEraseByteBuffer() {
            ByteBuffer buffer = ByteBuffer.wrap("SensitiveData".getBytes());
            CryptoUtil.secureErase(buffer);
            buffer.rewind();
            while (buffer.hasRemaining()) {
                assertThat(buffer.get()).isEqualTo((byte) 0);
            }
        }

        @Test
        @DisplayName("secureErase(null ByteBuffer)不抛出异常")
        void testSecureEraseByteBufferNull() {
            assertThatNoException().isThrownBy(() -> CryptoUtil.secureErase((ByteBuffer) null));
        }
    }

    @Nested
    @DisplayName("Random Generation Tests / 随机生成测试")
    class RandomGenerationTests {

        @Test
        @DisplayName("getSecureRandom返回非空SecureRandom")
        void testGetSecureRandom() {
            SecureRandom random = CryptoUtil.getSecureRandom();
            assertThat(random).isNotNull();
        }

        @Test
        @DisplayName("randomBytes生成指定长度随机字节")
        void testRandomBytes() {
            byte[] random = CryptoUtil.randomBytes(32);
            assertThat(random).hasSize(32);
        }

        @Test
        @DisplayName("randomBytes(0)返回空数组")
        void testRandomBytesZero() {
            byte[] random = CryptoUtil.randomBytes(0);
            assertThat(random).isEmpty();
        }

        @Test
        @DisplayName("randomBytes负数长度抛出异常")
        void testRandomBytesNegative() {
            assertThatThrownBy(() -> CryptoUtil.randomBytes(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("randomBytes生成不同的随机数据")
        void testRandomBytesUnique() {
            byte[] random1 = CryptoUtil.randomBytes(32);
            byte[] random2 = CryptoUtil.randomBytes(32);
            assertThat(random1).isNotEqualTo(random2);
        }

        @Test
        @DisplayName("randomNonce生成指定长度nonce")
        void testRandomNonce() {
            byte[] nonce = CryptoUtil.randomNonce(12);
            assertThat(nonce).hasSize(12);
        }

        @Test
        @DisplayName("randomIv生成指定长度IV")
        void testRandomIv() {
            byte[] iv = CryptoUtil.randomIv(16);
            assertThat(iv).hasSize(16);
        }

        @Test
        @DisplayName("randomSalt生成指定长度盐值")
        void testRandomSalt() {
            byte[] salt = CryptoUtil.randomSalt(16);
            assertThat(salt).hasSize(16);
        }
    }

    @Nested
    @DisplayName("Key Strength Tests / 密钥强度测试")
    class KeyStrengthTests {

        @Test
        @DisplayName("isKeyStrengthSufficient检测足够强度的密钥")
        void testIsKeyStrengthSufficient() throws Exception {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey key = keyGen.generateKey();

            boolean sufficient = CryptoUtil.isKeyStrengthSufficient(key, 256);
            assertThat(sufficient).isTrue();
        }

        @Test
        @DisplayName("isKeyStrengthSufficient检测强度不足的密钥")
        void testIsKeyStrengthInsufficient() throws Exception {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey key = keyGen.generateKey();

            boolean sufficient = CryptoUtil.isKeyStrengthSufficient(key, 256);
            assertThat(sufficient).isFalse();
        }

        @Test
        @DisplayName("isKeyStrengthSufficient(null, int)抛出异常")
        void testIsKeyStrengthSufficientNullKey() {
            assertThatThrownBy(() -> CryptoUtil.isKeyStrengthSufficient(null, 128))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("isKeyStrengthSufficient(key, negative)抛出异常")
        void testIsKeyStrengthSufficientNegativeBits() throws Exception {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey key = keyGen.generateKey();

            assertThatThrownBy(() -> CryptoUtil.isKeyStrengthSufficient(key, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("isKeyPairStrengthSufficient检测足够强度的密钥对")
        void testIsKeyPairStrengthSufficient() throws Exception {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            boolean sufficient = CryptoUtil.isKeyPairStrengthSufficient(keyPair, 2048);
            assertThat(sufficient).isTrue();
        }

        @Test
        @DisplayName("isKeyPairStrengthSufficient检测EC密钥对")
        void testIsKeyPairStrengthSufficientEc() throws Exception {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            keyGen.initialize(256);
            KeyPair keyPair = keyGen.generateKeyPair();

            boolean sufficient = CryptoUtil.isKeyPairStrengthSufficient(keyPair, 256);
            assertThat(sufficient).isTrue();
        }

        @Test
        @DisplayName("isKeyPairStrengthSufficient(null, int)抛出异常")
        void testIsKeyPairStrengthSufficientNullKeyPair() {
            assertThatThrownBy(() -> CryptoUtil.isKeyPairStrengthSufficient(null, 2048))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("isKeyPairStrengthSufficient(keyPair, negative)抛出异常")
        void testIsKeyPairStrengthSufficientNegativeBits() throws Exception {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            assertThatThrownBy(() -> CryptoUtil.isKeyPairStrengthSufficient(keyPair, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Algorithm Availability Tests / 算法可用性测试")
    class AlgorithmAvailabilityTests {

        @Test
        @DisplayName("isAlgorithmAvailable检测AES算法")
        void testIsAlgorithmAvailableAes() {
            boolean available = CryptoUtil.isAlgorithmAvailable("AES");
            assertThat(available).isTrue();
        }

        @Test
        @DisplayName("isAlgorithmAvailable检测SHA-256算法")
        void testIsAlgorithmAvailableSha256() {
            boolean available = CryptoUtil.isAlgorithmAvailable("SHA-256");
            assertThat(available).isTrue();
        }

        @Test
        @DisplayName("isAlgorithmAvailable检测HmacSHA256算法")
        void testIsAlgorithmAvailableHmacSha256() {
            boolean available = CryptoUtil.isAlgorithmAvailable("HmacSHA256");
            assertThat(available).isTrue();
        }

        @Test
        @DisplayName("isAlgorithmAvailable检测RSA算法")
        void testIsAlgorithmAvailableRsa() {
            boolean available = CryptoUtil.isAlgorithmAvailable("RSA");
            assertThat(available).isTrue();
        }

        @Test
        @DisplayName("isAlgorithmAvailable检测不存在的算法")
        void testIsAlgorithmAvailableNotExist() {
            boolean available = CryptoUtil.isAlgorithmAvailable("NonExistentAlgorithm");
            assertThat(available).isFalse();
        }

        @Test
        @DisplayName("isAlgorithmAvailable(null)抛出异常")
        void testIsAlgorithmAvailableNull() {
            assertThatThrownBy(() -> CryptoUtil.isAlgorithmAvailable(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("isAlgorithmAvailable(empty)抛出异常")
        void testIsAlgorithmAvailableEmpty() {
            assertThatThrownBy(() -> CryptoUtil.isAlgorithmAvailable(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("getAvailableAlgorithms获取Cipher算法")
        void testGetAvailableAlgorithmsCipher() {
            Set<String> algorithms = CryptoUtil.getAvailableAlgorithms("Cipher");
            assertThat(algorithms).isNotEmpty();
            assertThat(algorithms).contains("AES");
        }

        @Test
        @DisplayName("getAvailableAlgorithms获取MessageDigest算法")
        void testGetAvailableAlgorithmsMessageDigest() {
            Set<String> algorithms = CryptoUtil.getAvailableAlgorithms("MessageDigest");
            assertThat(algorithms).isNotEmpty();
            assertThat(algorithms).contains("SHA-256");
        }

        @Test
        @DisplayName("getAvailableAlgorithms获取Mac算法")
        void testGetAvailableAlgorithmsMac() {
            Set<String> algorithms = CryptoUtil.getAvailableAlgorithms("Mac");
            assertThat(algorithms).isNotEmpty();
            assertThat(algorithms).contains("HmacSHA256");
        }

        @Test
        @DisplayName("getAvailableAlgorithms获取Signature算法")
        void testGetAvailableAlgorithmsSignature() {
            Set<String> algorithms = CryptoUtil.getAvailableAlgorithms("Signature");
            assertThat(algorithms).isNotEmpty();
        }

        @Test
        @DisplayName("getAvailableAlgorithms(null)抛出异常")
        void testGetAvailableAlgorithmsNull() {
            assertThatThrownBy(() -> CryptoUtil.getAvailableAlgorithms(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("getAvailableAlgorithms(empty)抛出异常")
        void testGetAvailableAlgorithmsEmpty() {
            assertThatThrownBy(() -> CryptoUtil.getAvailableAlgorithms(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests / 工具类测试")
    class UtilityClassTests {

        @Test
        @DisplayName("CryptoUtil不能被实例化")
        void testCannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = CryptoUtil.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(AssertionError.class);
        }
    }
}
