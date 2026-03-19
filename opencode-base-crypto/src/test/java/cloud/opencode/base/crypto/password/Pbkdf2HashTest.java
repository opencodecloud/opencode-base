package cloud.opencode.base.crypto.password;

import cloud.opencode.base.crypto.enums.DigestAlgorithm;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Pbkdf2Hash}.
 * PBKDF2密码哈希单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Pbkdf2Hash Tests / PBKDF2密码哈希测试")
class Pbkdf2HashTest {

    private static final String TEST_PASSWORD = "TestPassword123!";
    private static final char[] TEST_PASSWORD_CHARS = "TestPassword123!".toCharArray();

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("sha256()创建SHA-256哈希器")
        void testSha256() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).contains("SHA256");
        }

        @Test
        @DisplayName("sha512()创建SHA-512哈希器")
        void testSha512() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha512();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).contains("SHA512");
        }

        @Test
        @DisplayName("owaspRecommended()创建OWASP推荐参数哈希器")
        void testOwaspRecommended() {
            Pbkdf2Hash hasher = Pbkdf2Hash.owaspRecommended();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).contains("SHA256");
        }

        @Test
        @DisplayName("builder()创建构建器")
        void testBuilder() {
            Pbkdf2Hash.Builder builder = Pbkdf2Hash.builder();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder Tests / 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder构建默认实例")
        void testBuilderDefault() {
            Pbkdf2Hash hasher = Pbkdf2Hash.builder().build();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).contains("SHA256");
        }

        @Test
        @DisplayName("builder设置SHA-384算法")
        void testBuilderSha384() {
            Pbkdf2Hash hasher = Pbkdf2Hash.builder()
                    .algorithm(DigestAlgorithm.SHA384)
                    .build();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).contains("SHA384");
        }

        @Test
        @DisplayName("builder设置SHA-512算法")
        void testBuilderSha512() {
            Pbkdf2Hash hasher = Pbkdf2Hash.builder()
                    .algorithm(DigestAlgorithm.SHA512)
                    .build();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).contains("SHA512");
        }

        @Test
        @DisplayName("builder设置SHA-1算法")
        void testBuilderSha1() {
            Pbkdf2Hash hasher = Pbkdf2Hash.builder()
                    .algorithm(DigestAlgorithm.SHA1)
                    .build();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).contains("SHA1");
        }

        @Test
        @DisplayName("builder设置迭代次数")
        void testBuilderIterations() {
            Pbkdf2Hash hasher = Pbkdf2Hash.builder()
                    .iterations(100000)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置密钥长度")
        void testBuilderKeyLength() {
            Pbkdf2Hash hasher = Pbkdf2Hash.builder()
                    .keyLength(512)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置盐长度")
        void testBuilderSaltLength() {
            Pbkdf2Hash hasher = Pbkdf2Hash.builder()
                    .saltLength(32)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置SecureRandom")
        void testBuilderSecureRandom() {
            Pbkdf2Hash hasher = Pbkdf2Hash.builder()
                    .secureRandom(new SecureRandom())
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder(null algorithm)抛出异常")
        void testBuilderNullAlgorithm() {
            assertThatThrownBy(() -> Pbkdf2Hash.builder().algorithm(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder(unsupported algorithm)抛出异常")
        void testBuilderUnsupportedAlgorithm() {
            assertThatThrownBy(() -> Pbkdf2Hash.builder().algorithm(DigestAlgorithm.MD5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("supported");
        }

        @Test
        @DisplayName("builder(iterations < 10000)抛出异常")
        void testBuilderTooFewIterations() {
            assertThatThrownBy(() -> Pbkdf2Hash.builder().iterations(9999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("10,000");
        }

        @Test
        @DisplayName("builder(keyLength < 128)抛出异常")
        void testBuilderTooSmallKeyLength() {
            assertThatThrownBy(() -> Pbkdf2Hash.builder().keyLength(64))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("128");
        }

        @Test
        @DisplayName("builder(saltLength < 8)抛出异常")
        void testBuilderTooSmallSaltLength() {
            assertThatThrownBy(() -> Pbkdf2Hash.builder().saltLength(4))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("8");
        }

        @Test
        @DisplayName("builder(null SecureRandom)抛出异常")
        void testBuilderNullSecureRandom() {
            assertThatThrownBy(() -> Pbkdf2Hash.builder().secureRandom(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Hash Tests / 哈希测试")
    class HashTests {

        @Test
        @DisplayName("hash(char[])生成有效哈希")
        void testHashCharArray() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash(TEST_PASSWORD_CHARS.clone());
            assertThat(hash).isNotNull();
            assertThat(hash).startsWith("$pbkdf2-sha256$");
            assertThat(hash).contains("$i=");
        }

        @Test
        @DisplayName("hash(String)生成有效哈希")
        void testHashString() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThat(hash).isNotNull();
            assertThat(hash).startsWith("$pbkdf2-sha256$");
        }

        @Test
        @DisplayName("hash相同密码生成不同哈希(盐随机)")
        void testHashDifferentSalts() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String hash1 = hasher.hash(TEST_PASSWORD);
            String hash2 = hasher.hash(TEST_PASSWORD);
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("hash(null char[])抛出异常")
        void testHashNullCharArray() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            assertThatThrownBy(() -> hasher.hash((char[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash(null String)抛出异常")
        void testHashNullString() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            assertThatThrownBy(() -> hasher.hash((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash(empty char[])抛出异常")
        void testHashEmptyCharArray() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            assertThatThrownBy(() -> hasher.hash(new char[0]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("hash SHA-512生成有效哈希")
        void testHashSha512() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha512();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThat(hash).isNotNull();
            assertThat(hash).startsWith("$pbkdf2-sha512$");
        }
    }

    @Nested
    @DisplayName("Verify Tests / 验证测试")
    class VerifyTests {

        @Test
        @DisplayName("verify(char[], hash)验证正确密码")
        void testVerifyCharArrayCorrect() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash(TEST_PASSWORD_CHARS.clone());
            boolean valid = hasher.verify(TEST_PASSWORD_CHARS.clone(), hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("verify(String, hash)验证正确密码")
        void testVerifyStringCorrect() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash(TEST_PASSWORD);
            boolean valid = hasher.verify(TEST_PASSWORD, hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("verify验证错误密码返回false")
        void testVerifyIncorrectPassword() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash(TEST_PASSWORD);
            boolean valid = hasher.verify("WrongPassword", hash);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("verify(null char[])抛出异常")
        void testVerifyNullCharArray() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThatThrownBy(() -> hasher.verify((char[]) null, hash))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(null String)抛出异常")
        void testVerifyNullString() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThatThrownBy(() -> hasher.verify((String) null, hash))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(password, null hash)抛出异常")
        void testVerifyNullHash() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            assertThatThrownBy(() -> hasher.verify(TEST_PASSWORD, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify无效哈希格式返回false")
        void testVerifyInvalidHashFormat() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            boolean valid = hasher.verify(TEST_PASSWORD, "invalid-hash-format");
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("verify SHA-512哈希")
        void testVerifySha512() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha512();
            String hash = hasher.hash(TEST_PASSWORD);
            boolean valid = hasher.verify(TEST_PASSWORD, hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("verify使用不同实例验证")
        void testVerifyDifferentInstance() {
            Pbkdf2Hash hasher1 = Pbkdf2Hash.sha256();
            Pbkdf2Hash hasher2 = Pbkdf2Hash.sha256();
            String hash = hasher1.hash(TEST_PASSWORD);
            boolean valid = hasher2.verify(TEST_PASSWORD, hash);
            assertThat(valid).isTrue();
        }
    }

    @Nested
    @DisplayName("NeedsRehash Tests / 需要重新哈希测试")
    class NeedsRehashTests {

        @Test
        @DisplayName("needsRehash相同参数返回false")
        void testNeedsRehashSameParams() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash(TEST_PASSWORD);
            boolean needsRehash = hasher.needsRehash(hash);
            assertThat(needsRehash).isFalse();
        }

        @Test
        @DisplayName("needsRehash不同迭代次数返回true")
        void testNeedsRehashDifferentIterations() {
            Pbkdf2Hash hasher1 = Pbkdf2Hash.builder().iterations(100000).build();
            Pbkdf2Hash hasher2 = Pbkdf2Hash.builder().iterations(200000).build();
            String hash = hasher1.hash(TEST_PASSWORD);
            boolean needsRehash = hasher2.needsRehash(hash);
            assertThat(needsRehash).isTrue();
        }

        @Test
        @DisplayName("needsRehash不同算法返回true")
        void testNeedsRehashDifferentAlgorithm() {
            Pbkdf2Hash hasher1 = Pbkdf2Hash.sha256();
            Pbkdf2Hash hasher2 = Pbkdf2Hash.sha512();
            String hash = hasher1.hash(TEST_PASSWORD);
            boolean needsRehash = hasher2.needsRehash(hash);
            assertThat(needsRehash).isTrue();
        }

        @Test
        @DisplayName("needsRehash(null)抛出异常")
        void testNeedsRehashNull() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            assertThatThrownBy(() -> hasher.needsRehash(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("needsRehash无效哈希格式返回true")
        void testNeedsRehashInvalidFormat() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            boolean needsRehash = hasher.needsRehash("invalid-hash-format");
            assertThat(needsRehash).isTrue();
        }
    }

    @Nested
    @DisplayName("GetAlgorithm Tests / 获取算法测试")
    class GetAlgorithmTests {

        @Test
        @DisplayName("getAlgorithm返回SHA-256算法名称")
        void testGetAlgorithmSha256() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String algorithm = hasher.getAlgorithm();
            assertThat(algorithm).contains("PBKDF2");
            assertThat(algorithm).contains("SHA256");
        }

        @Test
        @DisplayName("getAlgorithm返回SHA-512算法名称")
        void testGetAlgorithmSha512() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha512();
            String algorithm = hasher.getAlgorithm();
            assertThat(algorithm).contains("PBKDF2");
            assertThat(algorithm).contains("SHA512");
        }
    }

    @Nested
    @DisplayName("Hash Format Tests / 哈希格式测试")
    class HashFormatTests {

        @Test
        @DisplayName("哈希格式包含正确部分")
        void testHashFormatParts() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash(TEST_PASSWORD);
            String[] parts = hash.split("\\$");
            // Format: $pbkdf2-sha256$i=600000$salt$hash
            assertThat(parts).hasSize(5);
            assertThat(parts[1]).isEqualTo("pbkdf2-sha256");
            assertThat(parts[2]).startsWith("i=");
        }

        @Test
        @DisplayName("SHA-512哈希格式")
        void testHashFormatSha512() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha512();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThat(hash).startsWith("$pbkdf2-sha512$");
        }
    }

    @Nested
    @DisplayName("Special Password Tests / 特殊密码测试")
    class SpecialPasswordTests {

        @Test
        @DisplayName("hash处理Unicode密码")
        void testHashUnicodePassword() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String unicodePassword = "密码测试🔐";
            String hash = hasher.hash(unicodePassword);
            assertThat(hash).isNotNull();
            boolean valid = hasher.verify(unicodePassword, hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("hash处理长密码")
        void testHashLongPassword() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String longPassword = "a".repeat(1000);
            String hash = hasher.hash(longPassword);
            assertThat(hash).isNotNull();
            boolean valid = hasher.verify(longPassword, hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("hash处理包含特殊字符的密码")
        void testHashSpecialCharacters() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String specialPassword = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            String hash = hasher.hash(specialPassword);
            assertThat(hash).isNotNull();
            boolean valid = hasher.verify(specialPassword, hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("hash处理单字符密码")
        void testHashSingleCharacter() {
            Pbkdf2Hash hasher = Pbkdf2Hash.sha256();
            String singleChar = "a";
            String hash = hasher.hash(singleChar);
            assertThat(hash).isNotNull();
            boolean valid = hasher.verify(singleChar, hash);
            assertThat(valid).isTrue();
        }
    }
}
