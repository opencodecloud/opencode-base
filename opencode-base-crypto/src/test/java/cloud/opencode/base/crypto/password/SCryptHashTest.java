package cloud.opencode.base.crypto.password;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link SCryptHash}.
 * SCrypt密码哈希单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("SCryptHash Tests / SCrypt密码哈希测试")
class SCryptHashTest {

    private static final String TEST_PASSWORD = "TestPassword123!";
    private static final char[] TEST_PASSWORD_CHARS = "TestPassword123!".toCharArray();

    /**
     * Check if Bouncy Castle is available
     */
    private static boolean isBouncyCastleAvailable() {
        try {
            Class.forName("org.bouncycastle.crypto.generators.SCrypt");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of()创建默认SCrypt哈希器")
        void testOf() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.of();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).isEqualTo("SCrypt");
        }

        @Test
        @DisplayName("owaspRecommended()创建OWASP推荐参数哈希器")
        void testOwaspRecommended() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.owaspRecommended();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder()创建构建器")
        void testBuilder() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash.Builder builder = SCryptHash.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("of()无BC库抛出异常")
        void testOfNoBouncyCastle() {
            if (isBouncyCastleAvailable()) {
                // Skip this test if BC is available
                return;
            }
            assertThatThrownBy(SCryptHash::of)
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("Bouncy Castle");
        }
    }

    @Nested
    @DisplayName("Builder Tests / 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder构建默认实例")
        void testBuilderDefault() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置工作因子")
        void testBuilderWorkFactor() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder()
                    .workFactor(16384)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置块大小")
        void testBuilderBlockSize() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder()
                    .blockSize(8)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置并行化参数")
        void testBuilderParallelism() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder()
                    .parallelism(2)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置密钥长度")
        void testBuilderKeyLength() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder()
                    .keyLength(64)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置SecureRandom")
        void testBuilderSecureRandom() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder()
                    .secureRandom(new SecureRandom())
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder(non-power-of-2 workFactor)抛出异常")
        void testBuilderInvalidWorkFactor() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            assertThatThrownBy(() -> SCryptHash.builder().workFactor(100))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("power of 2");
        }

        @Test
        @DisplayName("builder(workFactor < 2)抛出异常")
        void testBuilderTooSmallWorkFactor() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            assertThatThrownBy(() -> SCryptHash.builder().workFactor(1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("builder(blockSize < 1)抛出异常")
        void testBuilderInvalidBlockSize() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            assertThatThrownBy(() -> SCryptHash.builder().blockSize(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("builder(parallelism < 1)抛出异常")
        void testBuilderInvalidParallelism() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            assertThatThrownBy(() -> SCryptHash.builder().parallelism(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("builder(keyLength < 16)抛出异常")
        void testBuilderInvalidKeyLength() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            assertThatThrownBy(() -> SCryptHash.builder().keyLength(8))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("16");
        }

        @Test
        @DisplayName("builder(null SecureRandom)抛出异常")
        void testBuilderNullSecureRandom() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            assertThatThrownBy(() -> SCryptHash.builder().secureRandom(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Hash Tests / 哈希测试")
    class HashTests {

        @Test
        @DisplayName("hash(char[])生成有效哈希")
        void testHashCharArray() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            // Use smaller work factor for testing speed
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String hash = hasher.hash(TEST_PASSWORD_CHARS.clone());
            assertThat(hash).isNotNull();
            assertThat(hash).startsWith("$scrypt$");
        }

        @Test
        @DisplayName("hash(String)生成有效哈希")
        void testHashString() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThat(hash).isNotNull();
            assertThat(hash).startsWith("$scrypt$");
        }

        @Test
        @DisplayName("hash相同密码生成不同哈希(盐随机)")
        void testHashDifferentSalts() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String hash1 = hasher.hash(TEST_PASSWORD);
            String hash2 = hasher.hash(TEST_PASSWORD);
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("hash(null char[])抛出异常")
        void testHashNullCharArray() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            assertThatThrownBy(() -> hasher.hash((char[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash(null String)抛出异常")
        void testHashNullString() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            assertThatThrownBy(() -> hasher.hash((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash(empty char[])抛出异常")
        void testHashEmptyCharArray() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            assertThatThrownBy(() -> hasher.hash(new char[0]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");
        }
    }

    @Nested
    @DisplayName("Verify Tests / 验证测试")
    class VerifyTests {

        @Test
        @DisplayName("verify(char[], hash)验证正确密码")
        void testVerifyCharArrayCorrect() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String hash = hasher.hash(TEST_PASSWORD_CHARS.clone());
            boolean valid = hasher.verify(TEST_PASSWORD_CHARS.clone(), hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("verify(String, hash)验证正确密码")
        void testVerifyStringCorrect() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String hash = hasher.hash(TEST_PASSWORD);
            boolean valid = hasher.verify(TEST_PASSWORD, hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("verify验证错误密码返回false")
        void testVerifyIncorrectPassword() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String hash = hasher.hash(TEST_PASSWORD);
            boolean valid = hasher.verify("WrongPassword", hash);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("verify(null char[])抛出异常")
        void testVerifyNullCharArray() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThatThrownBy(() -> hasher.verify((char[]) null, hash))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(null String)抛出异常")
        void testVerifyNullString() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThatThrownBy(() -> hasher.verify((String) null, hash))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(password, null hash)抛出异常")
        void testVerifyNullHash() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            assertThatThrownBy(() -> hasher.verify(TEST_PASSWORD, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify无效哈希格式返回false")
        void testVerifyInvalidHashFormat() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            boolean valid = hasher.verify(TEST_PASSWORD, "invalid-hash-format");
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("verify使用不同实例验证")
        void testVerifyDifferentInstance() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher1 = SCryptHash.builder().workFactor(1024).build();
            SCryptHash hasher2 = SCryptHash.builder().workFactor(1024).build();
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
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String hash = hasher.hash(TEST_PASSWORD);
            boolean needsRehash = hasher.needsRehash(hash);
            assertThat(needsRehash).isFalse();
        }

        @Test
        @DisplayName("needsRehash不同工作因子返回true")
        void testNeedsRehashDifferentWorkFactor() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher1 = SCryptHash.builder().workFactor(1024).build();
            SCryptHash hasher2 = SCryptHash.builder().workFactor(2048).build();
            String hash = hasher1.hash(TEST_PASSWORD);
            boolean needsRehash = hasher2.needsRehash(hash);
            assertThat(needsRehash).isTrue();
        }

        @Test
        @DisplayName("needsRehash(null)抛出异常")
        void testNeedsRehashNull() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            assertThatThrownBy(() -> hasher.needsRehash(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("needsRehash无效哈希格式返回true")
        void testNeedsRehashInvalidFormat() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            boolean needsRehash = hasher.needsRehash("invalid-hash-format");
            assertThat(needsRehash).isTrue();
        }
    }

    @Nested
    @DisplayName("GetAlgorithm Tests / 获取算法测试")
    class GetAlgorithmTests {

        @Test
        @DisplayName("getAlgorithm返回SCrypt")
        void testGetAlgorithm() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.of();
            String algorithm = hasher.getAlgorithm();
            assertThat(algorithm).isEqualTo("SCrypt");
        }
    }

    @Nested
    @DisplayName("Hash Format Tests / 哈希格式测试")
    class HashFormatTests {

        @Test
        @DisplayName("哈希格式包含正确部分")
        void testHashFormatParts() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String hash = hasher.hash(TEST_PASSWORD);
            // Format: $scrypt$n=1024,r=8,p=1$salt$hash
            String[] parts = hash.split("\\$");
            assertThat(parts).hasSize(5);
            assertThat(parts[1]).isEqualTo("scrypt");
            assertThat(parts[2]).contains("n=");
            assertThat(parts[2]).contains("r=");
            assertThat(parts[2]).contains("p=");
        }
    }

    @Nested
    @DisplayName("Special Password Tests / 特殊密码测试")
    class SpecialPasswordTests {

        @Test
        @DisplayName("hash处理Unicode密码")
        void testHashUnicodePassword() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String unicodePassword = "密码测试🔐";
            String hash = hasher.hash(unicodePassword);
            assertThat(hash).isNotNull();
            boolean valid = hasher.verify(unicodePassword, hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("hash处理长密码")
        void testHashLongPassword() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String longPassword = "a".repeat(1000);
            String hash = hasher.hash(longPassword);
            assertThat(hash).isNotNull();
            boolean valid = hasher.verify(longPassword, hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("hash处理包含特殊字符的密码")
        void testHashSpecialCharacters() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            SCryptHash hasher = SCryptHash.builder().workFactor(1024).build();
            String specialPassword = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            String hash = hasher.hash(specialPassword);
            assertThat(hash).isNotNull();
            boolean valid = hasher.verify(specialPassword, hash);
            assertThat(valid).isTrue();
        }
    }
}
