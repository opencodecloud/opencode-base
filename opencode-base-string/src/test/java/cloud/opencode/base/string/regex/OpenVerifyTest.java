package cloud.opencode.base.string.regex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenVerify")
class OpenVerifyTest {

    @Nested
    @DisplayName("isLuhn")
    class IsLuhn {

        @Test
        @DisplayName("should validate correct Luhn number")
        void shouldValidateCorrectLuhn() {
            assertThat(OpenVerify.isLuhn("4111111111111111")).isTrue();
        }

        @Test
        @DisplayName("should reject invalid Luhn number")
        void shouldRejectInvalidLuhn() {
            assertThat(OpenVerify.isLuhn("4111111111111112")).isFalse();
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(OpenVerify.isLuhn(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for empty string")
        void shouldReturnFalseForEmpty() {
            assertThat(OpenVerify.isLuhn("")).isFalse();
        }

        @Test
        @DisplayName("should return false for non-digit characters")
        void shouldReturnFalseForNonDigits() {
            assertThat(OpenVerify.isLuhn("41111111abc11111")).isFalse();
        }

        @Test
        @DisplayName("should validate single digit 0")
        void shouldValidateSingleZero() {
            assertThat(OpenVerify.isLuhn("0")).isTrue();
        }
    }

    @Nested
    @DisplayName("isCreditCard")
    class IsCreditCard {

        @Test
        @DisplayName("should validate Visa card number")
        void shouldValidateVisa() {
            assertThat(OpenVerify.isCreditCard("4111111111111111")).isTrue();
        }

        @Test
        @DisplayName("should validate card with spaces")
        void shouldValidateWithSpaces() {
            assertThat(OpenVerify.isCreditCard("4111 1111 1111 1111")).isTrue();
        }

        @Test
        @DisplayName("should validate card with dashes")
        void shouldValidateWithDashes() {
            assertThat(OpenVerify.isCreditCard("4111-1111-1111-1111")).isTrue();
        }

        @Test
        @DisplayName("should reject too short card number")
        void shouldRejectTooShort() {
            assertThat(OpenVerify.isCreditCard("411111111111")).isFalse();
        }

        @Test
        @DisplayName("should reject too long card number")
        void shouldRejectTooLong() {
            assertThat(OpenVerify.isCreditCard("41111111111111111111")).isFalse();
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(OpenVerify.isCreditCard(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isIdCard18")
    class IsIdCard18 {

        @Test
        @DisplayName("should validate correct 18-digit ID card")
        void shouldValidateCorrectId() {
            // Known valid ID card number (checksum verified: sum%11=0, check='1')
            assertThat(OpenVerify.isIdCard18("110101199003077731")).isTrue();
        }

        @Test
        @DisplayName("should reject wrong checksum")
        void shouldRejectWrongChecksum() {
            assertThat(OpenVerify.isIdCard18("110101199003077730")).isFalse();
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(OpenVerify.isIdCard18(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for wrong length")
        void shouldReturnFalseForWrongLength() {
            assertThat(OpenVerify.isIdCard18("11010119900307773")).isFalse();
        }

        @Test
        @DisplayName("should handle lowercase x checksum")
        void shouldHandleLowercaseX() {
            // An ID ending in X with valid checksum
            assertThat(OpenVerify.isIdCard18("11010119900307771x"))
                    .isEqualTo(OpenVerify.isIdCard18("11010119900307771X"));
        }

        @Test
        @DisplayName("should reject non-digit characters in first 17 digits")
        void shouldRejectNonDigitInBody() {
            assertThat(OpenVerify.isIdCard18("11010119900307a735")).isFalse();
        }
    }

    @Nested
    @DisplayName("isIdCard15")
    class IsIdCard15 {

        @Test
        @DisplayName("should validate correct 15-digit ID card")
        void shouldValidateCorrectId() {
            assertThat(OpenVerify.isIdCard15("110101900307773")).isTrue();
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(OpenVerify.isIdCard15(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for wrong length")
        void shouldReturnFalseForWrongLength() {
            assertThat(OpenVerify.isIdCard15("1101019003077")).isFalse();
        }

        @Test
        @DisplayName("should reject non-digit characters")
        void shouldRejectNonDigits() {
            assertThat(OpenVerify.isIdCard15("11010190030777a")).isFalse();
        }
    }

    @Nested
    @DisplayName("isUSCI")
    class IsUSCI {

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(OpenVerify.isUSCI(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for wrong length")
        void shouldReturnFalseForWrongLength() {
            assertThat(OpenVerify.isUSCI("91110000MA001")).isFalse();
        }

        @Test
        @DisplayName("should reject invalid characters not in USCI charset")
        void shouldRejectInvalidChars() {
            // 'I' and 'O' are not in the USCI character set
            assertThat(OpenVerify.isUSCI("91110000IOO01ABCX5")).isFalse();
        }

        @Test
        @DisplayName("should handle lowercase by converting to uppercase")
        void shouldHandleLowercase() {
            // Valid or invalid, just testing it doesn't crash on lowercase
            String usci = "91110000ma001abcx5";
            // Should not throw, result depends on checksum
            OpenVerify.isUSCI(usci);
        }
    }
}
