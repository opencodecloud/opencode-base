package cloud.opencode.base.crypto.password;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link PasswordHash} interface.
 * PasswordHash接口单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("PasswordHash Interface Tests / PasswordHash接口测试")
class PasswordHashTest {

    @Nested
    @DisplayName("SecureErase Tests / 安全擦除测试")
    class SecureEraseTests {

        @Test
        @DisplayName("secureErase(char[])将数组置零")
        void testSecureEraseCharArray() {
            char[] password = "TestPassword123!".toCharArray();
            PasswordHash.secureErase(password);

            for (char c : password) {
                assertThat(c).isEqualTo('\0');
            }
        }

        @Test
        @DisplayName("secureErase(null char[])不抛出异常")
        void testSecureEraseNullCharArray() {
            assertThatNoException().isThrownBy(() -> PasswordHash.secureErase(null));
        }

        @Test
        @DisplayName("secureErase(empty char[])不抛出异常")
        void testSecureEraseEmptyCharArray() {
            char[] empty = new char[0];
            assertThatNoException().isThrownBy(() -> PasswordHash.secureErase(empty));
        }

        @Test
        @DisplayName("secureErase擦除长密码")
        void testSecureEraseLongPassword() {
            char[] longPassword = "a".repeat(1000).toCharArray();
            PasswordHash.secureErase(longPassword);

            for (char c : longPassword) {
                assertThat(c).isEqualTo('\0');
            }
        }

        @Test
        @DisplayName("secureErase擦除Unicode字符")
        void testSecureEraseUnicode() {
            char[] unicode = "密码测试🔐".toCharArray();
            PasswordHash.secureErase(unicode);

            for (char c : unicode) {
                assertThat(c).isEqualTo('\0');
            }
        }
    }

    @Nested
    @DisplayName("Interface Contract Tests / 接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("Pbkdf2Hash实现PasswordHash接口")
        void testPbkdf2HashImplementsInterface() {
            PasswordHash hasher = Pbkdf2Hash.sha256();
            assertThat(hasher).isInstanceOf(PasswordHash.class);
        }

        @Test
        @DisplayName("BCryptHash实现PasswordHash接口")
        void testBCryptHashImplementsInterface() {
            PasswordHash hasher = BCryptHash.create();
            assertThat(hasher).isInstanceOf(PasswordHash.class);
        }

        @Test
        @DisplayName("通过接口调用hash方法")
        void testHashThroughInterface() {
            PasswordHash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash("TestPassword123!");
            assertThat(hash).isNotNull();
            assertThat(hash).isNotEmpty();
        }

        @Test
        @DisplayName("通过接口调用verify方法")
        void testVerifyThroughInterface() {
            PasswordHash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash("TestPassword123!");
            boolean valid = hasher.verify("TestPassword123!", hash);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("通过接口调用needsRehash方法")
        void testNeedsRehashThroughInterface() {
            PasswordHash hasher = Pbkdf2Hash.sha256();
            String hash = hasher.hash("TestPassword123!");
            boolean needsRehash = hasher.needsRehash(hash);
            assertThat(needsRehash).isFalse();
        }

        @Test
        @DisplayName("通过接口调用getAlgorithm方法")
        void testGetAlgorithmThroughInterface() {
            PasswordHash hasher = Pbkdf2Hash.sha256();
            String algorithm = hasher.getAlgorithm();
            assertThat(algorithm).isNotNull();
            assertThat(algorithm).contains("PBKDF2");
        }
    }

    @Nested
    @DisplayName("Polymorphism Tests / 多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("使用Pbkdf2Hash实现验证接口行为")
        void testPbkdf2Implementation() {
            PasswordHash hasher = Pbkdf2Hash.sha256();
            String password = "TestPassword123!";

            String hash = hasher.hash(password);
            assertThat(hash).isNotNull();

            boolean valid = hasher.verify(password, hash);
            assertThat(valid).isTrue();

            boolean invalid = hasher.verify("WrongPassword", hash);
            assertThat(invalid).isFalse();

            String algorithm = hasher.getAlgorithm();
            assertThat(algorithm).isNotNull();
        }

        @Test
        @DisplayName("BCryptHash实现接口")
        void testBCryptImplementsInterface() {
            PasswordHash hasher = BCryptHash.withCost(4);
            assertThat(hasher).isInstanceOf(PasswordHash.class);
            assertThat(hasher.getAlgorithm()).isEqualTo("BCrypt");
        }
    }

    @Nested
    @DisplayName("Hash and Verify Contract Tests / 哈希和验证契约测试")
    class HashVerifyContractTests {

        @Test
        @DisplayName("hash(char[])和verify(char[])配对使用")
        void testHashVerifyCharArray() {
            PasswordHash hasher = Pbkdf2Hash.sha256();
            char[] password = "TestPassword123!".toCharArray();

            String hash = hasher.hash(password.clone());
            boolean valid = hasher.verify(password.clone(), hash);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("hash(String)和verify(String)配对使用")
        void testHashVerifyString() {
            PasswordHash hasher = Pbkdf2Hash.sha256();
            String password = "TestPassword123!";

            String hash = hasher.hash(password);
            boolean valid = hasher.verify(password, hash);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("hash(String)生成的哈希可以用verify(char[])验证")
        void testCrossVerification() {
            PasswordHash hasher = Pbkdf2Hash.sha256();
            String passwordString = "TestPassword123!";
            char[] passwordChars = passwordString.toCharArray();

            String hash = hasher.hash(passwordString);
            boolean valid = hasher.verify(passwordChars.clone(), hash);

            assertThat(valid).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Method Tests / 默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("secureErase是静态方法")
        void testSecureEraseIsStatic() {
            // Can call without instance
            char[] data = "test".toCharArray();
            PasswordHash.secureErase(data);
            assertThat(data).containsOnly('\0');
        }
    }
}
