package cloud.opencode.base.crypto.password;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link Argon2Hash}.
 * Argon2密码哈希单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Argon2Hash Tests / Argon2密码哈希测试")
class Argon2HashTest {

    private static final String TEST_PASSWORD = "TestPassword123!";
    private static final char[] TEST_PASSWORD_CHARS = "TestPassword123!".toCharArray();

    /**
     * Check if Bouncy Castle is available
     */
    private static boolean isBouncyCastleAvailable() {
        try {
            Class.forName("org.bouncycastle.crypto.generators.Argon2BytesGenerator");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("argon2id()创建Argon2id哈希器")
        void testArgon2id() {
            Argon2Hash hasher = Argon2Hash.argon2id();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).isEqualTo("argon2id");
        }

        @Test
        @DisplayName("argon2i()创建Argon2i哈希器")
        void testArgon2i() {
            Argon2Hash hasher = Argon2Hash.argon2i();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).isEqualTo("argon2i");
        }

        @Test
        @DisplayName("argon2d()创建Argon2d哈希器")
        void testArgon2d() {
            Argon2Hash hasher = Argon2Hash.argon2d();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).isEqualTo("argon2d");
        }

        @Test
        @DisplayName("builder()创建构建器")
        void testBuilder() {
            Argon2Hash.Builder builder = Argon2Hash.builder();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder Tests / 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder构建默认Argon2id实例")
        void testBuilderDefault() {
            Argon2Hash hasher = Argon2Hash.builder().build();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).isEqualTo("argon2id");
        }

        @Test
        @DisplayName("builder设置类型")
        void testBuilderType() {
            Argon2Hash hasher = Argon2Hash.builder()
                    .type(Argon2Type.ARGON2I)
                    .build();
            assertThat(hasher).isNotNull();
            assertThat(hasher.getAlgorithm()).isEqualTo("argon2i");
        }

        @Test
        @DisplayName("builder设置盐长度")
        void testBuilderSaltLength() {
            Argon2Hash hasher = Argon2Hash.builder()
                    .saltLength(32)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置哈希长度")
        void testBuilderHashLength() {
            Argon2Hash hasher = Argon2Hash.builder()
                    .hashLength(64)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置迭代次数")
        void testBuilderIterations() {
            Argon2Hash hasher = Argon2Hash.builder()
                    .iterations(4)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置内存")
        void testBuilderMemory() {
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(32768)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置并行度")
        void testBuilderParallelism() {
            Argon2Hash hasher = Argon2Hash.builder()
                    .parallelism(2)
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder设置SecureRandom")
        void testBuilderSecureRandom() {
            Argon2Hash hasher = Argon2Hash.builder()
                    .secureRandom(new SecureRandom())
                    .build();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("builder(null type)抛出异常")
        void testBuilderNullType() {
            assertThatThrownBy(() -> Argon2Hash.builder().type(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder(null SecureRandom)抛出异常")
        void testBuilderNullSecureRandom() {
            assertThatThrownBy(() -> Argon2Hash.builder().secureRandom(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder(saltLength <= 0)抛出异常")
        void testBuilderInvalidSaltLength() {
            assertThatThrownBy(() -> Argon2Hash.builder().saltLength(0).build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("builder(hashLength <= 0)抛出异常")
        void testBuilderInvalidHashLength() {
            assertThatThrownBy(() -> Argon2Hash.builder().hashLength(0).build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("builder(iterations <= 0)抛出异常")
        void testBuilderInvalidIterations() {
            assertThatThrownBy(() -> Argon2Hash.builder().iterations(0).build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("builder(memory <= 0)抛出异常")
        void testBuilderInvalidMemory() {
            assertThatThrownBy(() -> Argon2Hash.builder().memory(0).build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("builder(parallelism <= 0)抛出异常")
        void testBuilderInvalidParallelism() {
            assertThatThrownBy(() -> Argon2Hash.builder().parallelism(0).build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("builder链式调用")
        void testBuilderChaining() {
            Argon2Hash hasher = Argon2Hash.builder()
                    .type(Argon2Type.ARGON2ID)
                    .saltLength(16)
                    .hashLength(32)
                    .iterations(3)
                    .memory(65536)
                    .parallelism(4)
                    .secureRandom(new SecureRandom())
                    .build();
            assertThat(hasher).isNotNull();
        }
    }

    @Nested
    @DisplayName("Hash Tests / 哈希测试")
    class HashTests {

        @Test
        @DisplayName("hash(char[])生成有效哈希")
        void testHashCharArray() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024) // Lower memory for testing
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher.hash(TEST_PASSWORD_CHARS.clone());
            assertThat(hash).isNotNull();
            assertThat(hash).startsWith("$argon2id$");
        }

        @Test
        @DisplayName("hash(String)生成有效哈希")
        void testHashString() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThat(hash).isNotNull();
            assertThat(hash).startsWith("$argon2id$");
        }

        @Test
        @DisplayName("hash相同密码生成不同哈希(盐随机)")
        void testHashDifferentSalts() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash1 = hasher.hash(TEST_PASSWORD);
            String hash2 = hasher.hash(TEST_PASSWORD);
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("hash(null char[])抛出异常")
        void testHashNullCharArray() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            assertThatThrownBy(() -> hasher.hash((char[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash(null String)抛出异常")
        void testHashNullString() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            assertThatThrownBy(() -> hasher.hash((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash无BC库抛出异常")
        void testHashNoBouncyCastle() {
            if (isBouncyCastleAvailable()) {
                // Skip this test if BC is available
                return;
            }
            Argon2Hash hasher = Argon2Hash.argon2id();
            assertThatThrownBy(() -> hasher.hash(TEST_PASSWORD))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("Bouncy Castle");
        }

        @Test
        @DisplayName("hash Argon2i生成有效哈希")
        void testHashArgon2i() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .type(Argon2Type.ARGON2I)
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThat(hash).isNotNull();
            assertThat(hash).startsWith("$argon2i$");
        }

        @Test
        @DisplayName("hash Argon2d生成有效哈希")
        void testHashArgon2d() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .type(Argon2Type.ARGON2D)
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThat(hash).isNotNull();
            assertThat(hash).startsWith("$argon2d$");
        }
    }

    @Nested
    @DisplayName("Verify Tests / 验证测试")
    class VerifyTests {

        @Test
        @DisplayName("verify(char[], hash)验证正确密码")
        void testVerifyCharArrayCorrect() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher.hash(TEST_PASSWORD_CHARS.clone());
            boolean valid = hasher.verify(TEST_PASSWORD_CHARS.clone(), hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("verify(String, hash)验证正确密码")
        void testVerifyStringCorrect() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher.hash(TEST_PASSWORD);
            boolean valid = hasher.verify(TEST_PASSWORD, hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("verify验证错误密码返回false")
        void testVerifyIncorrectPassword() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher.hash(TEST_PASSWORD);
            boolean valid = hasher.verify("WrongPassword", hash);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("verify(null char[])抛出异常")
        void testVerifyNullCharArray() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThatThrownBy(() -> hasher.verify((char[]) null, hash))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(null String)抛出异常")
        void testVerifyNullString() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher.hash(TEST_PASSWORD);
            assertThatThrownBy(() -> hasher.verify((String) null, hash))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(password, null hash)抛出异常")
        void testVerifyNullHash() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            assertThatThrownBy(() -> hasher.verify(TEST_PASSWORD, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify无效哈希格式返回false")
        void testVerifyInvalidHashFormat() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            boolean valid = hasher.verify(TEST_PASSWORD, "invalid-hash-format");
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("verify使用不同实例验证")
        void testVerifyDifferentInstance() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher1 = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            Argon2Hash hasher2 = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
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
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher.hash(TEST_PASSWORD);
            boolean needsRehash = hasher.needsRehash(hash);
            assertThat(needsRehash).isFalse();
        }

        @Test
        @DisplayName("needsRehash不同迭代次数返回true")
        void testNeedsRehashDifferentIterations() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher1 = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            Argon2Hash hasher2 = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(2)
                    .parallelism(1)
                    .build();
            String hash = hasher1.hash(TEST_PASSWORD);
            boolean needsRehash = hasher2.needsRehash(hash);
            assertThat(needsRehash).isTrue();
        }

        @Test
        @DisplayName("needsRehash不同内存返回true")
        void testNeedsRehashDifferentMemory() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher1 = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            Argon2Hash hasher2 = Argon2Hash.builder()
                    .memory(2048)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher1.hash(TEST_PASSWORD);
            boolean needsRehash = hasher2.needsRehash(hash);
            assertThat(needsRehash).isTrue();
        }

        @Test
        @DisplayName("needsRehash不同类型返回true")
        void testNeedsRehashDifferentType() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher1 = Argon2Hash.builder()
                    .type(Argon2Type.ARGON2ID)
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            Argon2Hash hasher2 = Argon2Hash.builder()
                    .type(Argon2Type.ARGON2I)
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher1.hash(TEST_PASSWORD);
            boolean needsRehash = hasher2.needsRehash(hash);
            assertThat(needsRehash).isTrue();
        }

        @Test
        @DisplayName("needsRehash(null)抛出异常")
        void testNeedsRehashNull() {
            Argon2Hash hasher = Argon2Hash.argon2id();
            assertThatThrownBy(() -> hasher.needsRehash(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("needsRehash无效哈希格式返回true")
        void testNeedsRehashInvalidFormat() {
            Argon2Hash hasher = Argon2Hash.argon2id();
            boolean needsRehash = hasher.needsRehash("invalid-hash-format");
            assertThat(needsRehash).isTrue();
        }
    }

    @Nested
    @DisplayName("GetAlgorithm Tests / 获取算法测试")
    class GetAlgorithmTests {

        @Test
        @DisplayName("getAlgorithm返回argon2id")
        void testGetAlgorithmArgon2id() {
            Argon2Hash hasher = Argon2Hash.argon2id();
            assertThat(hasher.getAlgorithm()).isEqualTo("argon2id");
        }

        @Test
        @DisplayName("getAlgorithm返回argon2i")
        void testGetAlgorithmArgon2i() {
            Argon2Hash hasher = Argon2Hash.argon2i();
            assertThat(hasher.getAlgorithm()).isEqualTo("argon2i");
        }

        @Test
        @DisplayName("getAlgorithm返回argon2d")
        void testGetAlgorithmArgon2d() {
            Argon2Hash hasher = Argon2Hash.argon2d();
            assertThat(hasher.getAlgorithm()).isEqualTo("argon2d");
        }
    }

    @Nested
    @DisplayName("Hash Format Tests / 哈希格式测试")
    class HashFormatTests {

        @Test
        @DisplayName("哈希格式符合PHC标准")
        void testHashFormat() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String hash = hasher.hash(TEST_PASSWORD);
            // Format: $argon2id$v=19$m=1024,t=1,p=1$salt$hash
            assertThat(hash).matches("^\\$argon2id\\$v=\\d+\\$m=\\d+,t=\\d+,p=\\d+\\$[A-Za-z0-9+/]+\\$[A-Za-z0-9+/]+$");
        }
    }

    @Nested
    @DisplayName("Special Password Tests / 特殊密码测试")
    class SpecialPasswordTests {

        @Test
        @DisplayName("hash处理Unicode密码")
        void testHashUnicodePassword() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
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
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
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
            Argon2Hash hasher = Argon2Hash.builder()
                    .memory(1024)
                    .iterations(1)
                    .parallelism(1)
                    .build();
            String specialPassword = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            String hash = hasher.hash(specialPassword);
            assertThat(hash).isNotNull();
            boolean valid = hasher.verify(specialPassword, hash);
            assertThat(valid).isTrue();
        }
    }
}
