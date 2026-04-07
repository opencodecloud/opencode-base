package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.store.CaptchaStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link PowCaptchaValidator}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
@DisplayName("PowCaptchaValidator Tests")
class PowCaptchaValidatorTest {

    private CaptchaStore store;
    private PowCaptchaValidator validator;

    @BeforeEach
    void setUp() {
        store = CaptchaStore.memory();
        validator = PowCaptchaValidator.create(store);
    }

    @Nested
    @DisplayName("Validate Tests")
    class ValidateTests {

        @Test
        @DisplayName("should return SUCCESS when valid nonce is submitted")
        void should_returnSuccess_whenValidNonce() {
            String challenge = "test-challenge-001";
            int difficulty = 8; // Low difficulty for fast test
            store.store("pow-1", challenge + ":" + difficulty, Duration.ofMinutes(5));

            String nonce = findValidNonce(challenge, difficulty);
            ValidationResult result = validator.validate("pow-1", nonce);

            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUCCESS);
        }

        @Test
        @DisplayName("should return MISMATCH when invalid nonce is submitted")
        void should_returnMismatch_whenInvalidNonce() {
            String challenge = "test-challenge-002";
            int difficulty = 20;
            store.store("pow-2", challenge + ":" + difficulty, Duration.ofMinutes(5));

            ValidationResult result = validator.validate("pow-2", "definitely-wrong-nonce");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }

        @Test
        @DisplayName("should return NOT_FOUND when id is not in store")
        void should_returnNotFound_whenIdNotInStore() {
            ValidationResult result = validator.validate("nonexistent-id", "some-nonce");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.NOT_FOUND);
        }

        @Test
        @DisplayName("should return INVALID_INPUT when id is null")
        void should_returnInvalidInput_whenNullId() {
            ValidationResult result = validator.validate(null, "some-nonce");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("should return INVALID_INPUT when answer is blank")
        void should_returnInvalidInput_whenBlankAnswer() {
            store.store("pow-3", "challenge:8", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("pow-3", "   ");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }
    }

    @Nested
    @DisplayName("HasLeadingZeros Tests")
    class HasLeadingZerosTests {

        @Test
        @DisplayName("should return true when all zero bytes for any difficulty")
        void should_returnTrue_whenAllZeroBytes() {
            byte[] allZeros = new byte[32];

            assertThat(PowCaptchaValidator.hasLeadingZeros(allZeros, 8)).isTrue();
            assertThat(PowCaptchaValidator.hasLeadingZeros(allZeros, 16)).isTrue();
            assertThat(PowCaptchaValidator.hasLeadingZeros(allZeros, 24)).isTrue();
        }

        @Test
        @DisplayName("should return false when first byte is non-zero for difficulty 8")
        void should_returnFalse_whenFirstByteNonZero() {
            byte[] hash = new byte[32];
            hash[0] = 0x01; // First byte is non-zero

            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 8)).isFalse();
        }

        @Test
        @DisplayName("should return true when partial bits match for difficulty 4")
        void should_returnTrue_whenPartialBitsMatch() {
            byte[] hash = new byte[32];
            hash[0] = 0x0F; // Upper 4 bits are zero, lower 4 bits are set

            // difficulty=4 requires upper 4 bits of first byte to be zero
            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 4)).isTrue();
            // difficulty=5 requires upper 5 bits to be zero, but bit 4 is set
            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 5)).isFalse();
        }
    }

    @Nested
    @DisplayName("Single Use Tests")
    class SingleUseTests {

        @Test
        @DisplayName("should remove challenge from store after validation")
        void should_removeFromStore_afterValidation() {
            String challenge = "single-use-challenge";
            int difficulty = 8;
            store.store("pow-single", challenge + ":" + difficulty, Duration.ofMinutes(5));

            String nonce = findValidNonce(challenge, difficulty);
            validator.validate("pow-single", nonce);

            // Second attempt should return NOT_FOUND because challenge was consumed
            ValidationResult second = validator.validate("pow-single", nonce);
            assertThat(second.success()).isFalse();
            assertThat(second.code()).isEqualTo(ValidationResult.ResultCode.NOT_FOUND);

            // Store should no longer contain the entry
            assertThat(store.exists("pow-single")).isFalse();
        }
    }

    @Nested
    @DisplayName("Validate Edge Case Tests")
    class ValidateEdgeCaseTests {

        @Test
        @DisplayName("should return INVALID_INPUT when id is blank (whitespace)")
        void should_returnInvalidInput_when_idIsBlank() {
            ValidationResult result = validator.validate("   ", "some-nonce");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("should return INVALID_INPUT when answer is null")
        void should_returnInvalidInput_when_answerIsNull() {
            store.store("pow-null-ans", "challenge:8", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("pow-null-ans", null);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("should return MISMATCH when stored value has no colon")
        void should_returnMismatch_when_storedValueHasNoColon() {
            store.store("pow-no-colon", "challengewithoutdifficulty", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("pow-no-colon", "some-nonce");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }

        @Test
        @DisplayName("should return MISMATCH when difficulty is not a number")
        void should_returnMismatch_when_difficultyNotNumber() {
            store.store("pow-bad-diff", "challenge:notanumber", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("pow-bad-diff", "some-nonce");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }
    }

    @Nested
    @DisplayName("HasLeadingZeros Edge Case Tests")
    class HasLeadingZerosEdgeCaseTests {

        @Test
        @DisplayName("should return false when bits is 0")
        void should_returnFalse_when_bitsIsZero() {
            byte[] hash = new byte[32];

            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 0)).isFalse();
        }

        @Test
        @DisplayName("should return false when bits is negative")
        void should_returnFalse_when_bitsIsNegative() {
            byte[] hash = new byte[32];

            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, -1)).isFalse();
        }

        @Test
        @DisplayName("should return false when bits exceeds hash length * 8")
        void should_returnFalse_when_bitsExceedsHashLength() {
            byte[] hash = new byte[4]; // 32 bits

            // Request 40 bits (more than 32)
            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 40)).isFalse();
        }

        @Test
        @DisplayName("should handle remainingBits > 0 when partial byte has zeros")
        void should_returnTrue_when_remainingBitsPartialMatch() {
            byte[] hash = new byte[32];
            // 9 bits = 1 full zero byte + 1 remaining bit in second byte
            // Second byte = 0x00 → upper 1 bit is zero → should pass
            hash[1] = 0x00;

            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 9)).isTrue();
        }

        @Test
        @DisplayName("should return false when remainingBits partial byte has set bit")
        void should_returnFalse_when_remainingBitsPartialFail() {
            byte[] hash = new byte[32];
            // 9 bits = 1 full zero byte + 1 remaining bit in second byte
            // Second byte = 0x80 → upper 1 bit is set → should fail
            hash[1] = (byte) 0x80;

            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 9)).isFalse();
        }

        @Test
        @DisplayName("should handle exact full byte boundary (no remainingBits)")
        void should_returnTrue_when_exactBytesBoundary() {
            byte[] hash = new byte[32];
            hash[2] = (byte) 0xFF; // non-zero after the 16 zero bits

            // 16 bits = 2 full zero bytes, no remaining bits
            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 16)).isTrue();
        }

        @Test
        @DisplayName("should handle bits equal to hash length * 8 with all zeros")
        void should_returnTrue_when_bitsEqualsHashBitLength() {
            byte[] hash = new byte[4]; // 32 bits, all zero

            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 32)).isTrue();
        }

        @Test
        @DisplayName("should handle various remainingBits values")
        void should_handleVariousRemainingBits() {
            byte[] hash = new byte[32];
            hash[0] = 0x07; // binary: 00000111 → upper 5 bits are zero

            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 1)).isTrue();
            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 2)).isTrue();
            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 3)).isTrue();
            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 4)).isTrue();
            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 5)).isTrue();
            assertThat(PowCaptchaValidator.hasLeadingZeros(hash, 6)).isFalse();
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Brute-force searches for a valid nonce that satisfies the PoW requirement.
     * Uses low difficulty (e.g., 8) for fast test execution.
     */
    private String findValidNonce(String challenge, int difficulty) {
        for (int i = 0; i < 1_000_000; i++) {
            String nonce = String.valueOf(i);
            byte[] hash = sha256(challenge + nonce);
            if (PowCaptchaValidator.hasLeadingZeros(hash, difficulty)) {
                return nonce;
            }
        }
        throw new AssertionError("Could not find valid nonce within 1,000,000 attempts");
    }

    /**
     * Computes SHA-256 hash of the input string.
     */
    private static byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("SHA-256 not available", e);
        }
    }
}
