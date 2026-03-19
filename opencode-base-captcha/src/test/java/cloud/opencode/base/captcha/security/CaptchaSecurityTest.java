package cloud.opencode.base.captcha.security;

import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaSecurity Test - Unit tests for security utilities
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaSecurityTest {

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("should not be instantiable")
        void shouldNotBeInstantiable() throws NoSuchMethodException {
            Constructor<CaptchaSecurity> constructor = CaptchaSecurity.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("GenerateSecureId Tests")
    class GenerateSecureIdTests {

        @Test
        @DisplayName("should generate non-null ID")
        void shouldGenerateNonNullId() {
            String id = CaptchaSecurity.generateSecureId();

            assertThat(id).isNotNull();
        }

        @Test
        @DisplayName("should generate non-empty ID")
        void shouldGenerateNonEmptyId() {
            String id = CaptchaSecurity.generateSecureId();

            assertThat(id).isNotEmpty();
        }

        @Test
        @DisplayName("should generate unique IDs")
        void shouldGenerateUniqueIds() {
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < 1000; i++) {
                ids.add(CaptchaSecurity.generateSecureId());
            }

            assertThat(ids).hasSize(1000);
        }

        @Test
        @DisplayName("should generate URL-safe IDs")
        void shouldGenerateUrlSafeIds() {
            for (int i = 0; i < 100; i++) {
                String id = CaptchaSecurity.generateSecureId();
                assertThat(id).matches("[A-Za-z0-9_-]+");
            }
        }

        @Test
        @DisplayName("should generate IDs without padding")
        void shouldGenerateIdsWithoutPadding() {
            for (int i = 0; i < 100; i++) {
                String id = CaptchaSecurity.generateSecureId();
                assertThat(id).doesNotContain("=");
            }
        }
    }

    @Nested
    @DisplayName("GenerateSecureToken Tests")
    class GenerateSecureTokenTests {

        @Test
        @DisplayName("should generate token of appropriate length")
        void shouldGenerateTokenOfAppropriateLength() {
            String token = CaptchaSecurity.generateSecureToken(32);

            assertThat(token).isNotEmpty();
            // 32 bytes -> ~43 base64 chars without padding
            assertThat(token.length()).isGreaterThan(30);
        }

        @Test
        @DisplayName("should generate different tokens each time")
        void shouldGenerateDifferentTokensEachTime() {
            String token1 = CaptchaSecurity.generateSecureToken(16);
            String token2 = CaptchaSecurity.generateSecureToken(16);

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("should generate URL-safe tokens")
        void shouldGenerateUrlSafeTokens() {
            String token = CaptchaSecurity.generateSecureToken(32);

            assertThat(token).matches("[A-Za-z0-9_-]+");
        }

        @Test
        @DisplayName("should generate tokens of varying lengths")
        void shouldGenerateTokensOfVaryingLengths() {
            String short8 = CaptchaSecurity.generateSecureToken(8);
            String long64 = CaptchaSecurity.generateSecureToken(64);

            assertThat(short8.length()).isLessThan(long64.length());
        }
    }

    @Nested
    @DisplayName("HashAnswer Tests")
    class HashAnswerTests {

        @Test
        @DisplayName("should produce non-null hash")
        void shouldProduceNonNullHash() {
            String hash = CaptchaSecurity.hashAnswer("answer", "salt");

            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("should produce non-empty hash")
        void shouldProduceNonEmptyHash() {
            String hash = CaptchaSecurity.hashAnswer("answer", "salt");

            assertThat(hash).isNotEmpty();
        }

        @Test
        @DisplayName("should produce same hash for same input")
        void shouldProduceSameHashForSameInput() {
            String hash1 = CaptchaSecurity.hashAnswer("answer", "salt");
            String hash2 = CaptchaSecurity.hashAnswer("answer", "salt");

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("should produce different hash for different answers")
        void shouldProduceDifferentHashForDifferentAnswers() {
            String hash1 = CaptchaSecurity.hashAnswer("answer1", "salt");
            String hash2 = CaptchaSecurity.hashAnswer("answer2", "salt");

            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("should produce different hash for different salts")
        void shouldProduceDifferentHashForDifferentSalts() {
            String hash1 = CaptchaSecurity.hashAnswer("answer", "salt1");
            String hash2 = CaptchaSecurity.hashAnswer("answer", "salt2");

            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("should normalize to lowercase before hashing")
        void shouldNormalizeToLowercaseBeforeHashing() {
            String hash1 = CaptchaSecurity.hashAnswer("ANSWER", "salt");
            String hash2 = CaptchaSecurity.hashAnswer("answer", "salt");

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("should produce base64-encoded hash")
        void shouldProduceBase64EncodedHash() {
            String hash = CaptchaSecurity.hashAnswer("answer", "salt");

            // Base64 uses A-Z, a-z, 0-9, +, /, =
            assertThat(hash).matches("[A-Za-z0-9+/=]+");
        }
    }

    @Nested
    @DisplayName("VerifyHashedAnswer Tests")
    class VerifyHashedAnswerTests {

        @Test
        @DisplayName("should verify correct answer")
        void shouldVerifyCorrectAnswer() {
            String salt = CaptchaSecurity.generateSalt();
            String hash = CaptchaSecurity.hashAnswer("answer", salt);

            assertThat(CaptchaSecurity.verifyHashedAnswer("answer", hash, salt)).isTrue();
        }

        @Test
        @DisplayName("should reject incorrect answer")
        void shouldRejectIncorrectAnswer() {
            String salt = CaptchaSecurity.generateSalt();
            String hash = CaptchaSecurity.hashAnswer("correct", salt);

            assertThat(CaptchaSecurity.verifyHashedAnswer("wrong", hash, salt)).isFalse();
        }

        @Test
        @DisplayName("should be case-insensitive for answers")
        void shouldBeCaseInsensitiveForAnswers() {
            String salt = CaptchaSecurity.generateSalt();
            String hash = CaptchaSecurity.hashAnswer("Answer", salt);

            assertThat(CaptchaSecurity.verifyHashedAnswer("ANSWER", hash, salt)).isTrue();
            assertThat(CaptchaSecurity.verifyHashedAnswer("answer", hash, salt)).isTrue();
        }

        @Test
        @DisplayName("should reject when salt is different")
        void shouldRejectWhenSaltIsDifferent() {
            String hash = CaptchaSecurity.hashAnswer("answer", "salt-A");

            assertThat(CaptchaSecurity.verifyHashedAnswer("answer", hash, "salt-B")).isFalse();
        }
    }

    @Nested
    @DisplayName("ConstantTimeEquals Tests")
    class ConstantTimeEqualsTests {

        @Test
        @DisplayName("should return true for equal strings")
        void shouldReturnTrueForEqualStrings() {
            assertThat(CaptchaSecurity.constantTimeEquals("hello", "hello")).isTrue();
        }

        @Test
        @DisplayName("should return false for different strings")
        void shouldReturnFalseForDifferentStrings() {
            assertThat(CaptchaSecurity.constantTimeEquals("hello", "world")).isFalse();
        }

        @Test
        @DisplayName("should return false for different length strings")
        void shouldReturnFalseForDifferentLengthStrings() {
            assertThat(CaptchaSecurity.constantTimeEquals("short", "longer")).isFalse();
        }

        @Test
        @DisplayName("should handle both null as equal")
        void shouldHandleBothNullAsEqual() {
            assertThat(CaptchaSecurity.constantTimeEquals(null, null)).isTrue();
        }

        @Test
        @DisplayName("should return false for null and non-null")
        void shouldReturnFalseForNullAndNonNull() {
            assertThat(CaptchaSecurity.constantTimeEquals(null, "hello")).isFalse();
            assertThat(CaptchaSecurity.constantTimeEquals("hello", null)).isFalse();
        }

        @Test
        @DisplayName("should return true for empty strings")
        void shouldReturnTrueForEmptyStrings() {
            assertThat(CaptchaSecurity.constantTimeEquals("", "")).isTrue();
        }

        @Test
        @DisplayName("should return false for strings differing by one char")
        void shouldReturnFalseForStringsDifferingByOneChar() {
            assertThat(CaptchaSecurity.constantTimeEquals("abcd", "abce")).isFalse();
        }

        @Test
        @DisplayName("should return false for strings differing at first char")
        void shouldReturnFalseForStringsDifferingAtFirstChar() {
            assertThat(CaptchaSecurity.constantTimeEquals("Abcd", "abcd")).isFalse();
        }
    }

    @Nested
    @DisplayName("GenerateSalt Tests")
    class GenerateSaltTests {

        @Test
        @DisplayName("should generate non-null salt")
        void shouldGenerateNonNullSalt() {
            assertThat(CaptchaSecurity.generateSalt()).isNotNull();
        }

        @Test
        @DisplayName("should generate non-empty salt")
        void shouldGenerateNonEmptySalt() {
            assertThat(CaptchaSecurity.generateSalt()).isNotEmpty();
        }

        @Test
        @DisplayName("should generate unique salts")
        void shouldGenerateUniqueSalts() {
            Set<String> salts = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                salts.add(CaptchaSecurity.generateSalt());
            }

            assertThat(salts).hasSize(100);
        }

        @Test
        @DisplayName("should generate base64-encoded salt")
        void shouldGenerateBase64EncodedSalt() {
            String salt = CaptchaSecurity.generateSalt();

            assertThat(salt).matches("[A-Za-z0-9+/=]+");
        }
    }
}
