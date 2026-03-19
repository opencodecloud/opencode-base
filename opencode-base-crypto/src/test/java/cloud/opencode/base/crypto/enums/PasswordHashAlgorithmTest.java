package cloud.opencode.base.crypto.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PasswordHashAlgorithm 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("PasswordHashAlgorithm 测试")
class PasswordHashAlgorithmTest {

    @Nested
    @DisplayName("getName 测试")
    class GetNameTests {

        @Test
        @DisplayName("Argon2id名称")
        void testArgon2idName() {
            assertThat(PasswordHashAlgorithm.ARGON2ID.getName()).isEqualTo("argon2id");
        }

        @Test
        @DisplayName("Argon2d名称")
        void testArgon2dName() {
            assertThat(PasswordHashAlgorithm.ARGON2D.getName()).isEqualTo("argon2d");
        }

        @Test
        @DisplayName("Argon2i名称")
        void testArgon2iName() {
            assertThat(PasswordHashAlgorithm.ARGON2I.getName()).isEqualTo("argon2i");
        }

        @Test
        @DisplayName("BCrypt名称")
        void testBcryptName() {
            assertThat(PasswordHashAlgorithm.BCRYPT.getName()).isEqualTo("bcrypt");
        }

        @Test
        @DisplayName("SCrypt名称")
        void testScryptName() {
            assertThat(PasswordHashAlgorithm.SCRYPT.getName()).isEqualTo("scrypt");
        }

        @Test
        @DisplayName("PBKDF2-SHA256名称")
        void testPbkdf2Sha256Name() {
            assertThat(PasswordHashAlgorithm.PBKDF2_SHA256.getName()).isEqualTo("pbkdf2-sha256");
        }

        @Test
        @DisplayName("PBKDF2-SHA512名称")
        void testPbkdf2Sha512Name() {
            assertThat(PasswordHashAlgorithm.PBKDF2_SHA512.getName()).isEqualTo("pbkdf2-sha512");
        }
    }

    @Nested
    @DisplayName("isRecommended 测试")
    class IsRecommendedTests {

        @Test
        @DisplayName("Argon2id被推荐")
        void testArgon2idRecommended() {
            assertThat(PasswordHashAlgorithm.ARGON2ID.isRecommended()).isTrue();
        }

        @Test
        @DisplayName("Argon2d不被推荐")
        void testArgon2dNotRecommended() {
            assertThat(PasswordHashAlgorithm.ARGON2D.isRecommended()).isFalse();
        }

        @Test
        @DisplayName("Argon2i不被推荐")
        void testArgon2iNotRecommended() {
            assertThat(PasswordHashAlgorithm.ARGON2I.isRecommended()).isFalse();
        }

        @Test
        @DisplayName("BCrypt被推荐")
        void testBcryptRecommended() {
            assertThat(PasswordHashAlgorithm.BCRYPT.isRecommended()).isTrue();
        }

        @Test
        @DisplayName("SCrypt被推荐")
        void testScryptRecommended() {
            assertThat(PasswordHashAlgorithm.SCRYPT.isRecommended()).isTrue();
        }

        @Test
        @DisplayName("PBKDF2不被推荐")
        void testPbkdf2NotRecommended() {
            assertThat(PasswordHashAlgorithm.PBKDF2_SHA256.isRecommended()).isFalse();
            assertThat(PasswordHashAlgorithm.PBKDF2_SHA512.isRecommended()).isFalse();
        }
    }

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("所有枚举值存在")
        void testAllValuesExist() {
            assertThat(PasswordHashAlgorithm.values()).hasSize(7);
        }

        @Test
        @DisplayName("valueOf返回正确枚举")
        void testValueOf() {
            assertThat(PasswordHashAlgorithm.valueOf("ARGON2ID")).isEqualTo(PasswordHashAlgorithm.ARGON2ID);
            assertThat(PasswordHashAlgorithm.valueOf("BCRYPT")).isEqualTo(PasswordHashAlgorithm.BCRYPT);
            assertThat(PasswordHashAlgorithm.valueOf("SCRYPT")).isEqualTo(PasswordHashAlgorithm.SCRYPT);
        }

        @Test
        @DisplayName("包含所有算法")
        void testContainsAllAlgorithms() {
            assertThat(PasswordHashAlgorithm.values()).contains(
                    PasswordHashAlgorithm.ARGON2ID,
                    PasswordHashAlgorithm.ARGON2D,
                    PasswordHashAlgorithm.ARGON2I,
                    PasswordHashAlgorithm.BCRYPT,
                    PasswordHashAlgorithm.SCRYPT,
                    PasswordHashAlgorithm.PBKDF2_SHA256,
                    PasswordHashAlgorithm.PBKDF2_SHA512
            );
        }
    }
}
