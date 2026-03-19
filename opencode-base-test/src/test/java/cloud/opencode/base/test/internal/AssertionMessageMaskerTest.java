package cloud.opencode.base.test.internal;

import org.junit.jupiter.api.*;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

/**
 * AssertionMessageMaskerTest Tests
 * AssertionMessageMaskerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("AssertionMessageMasker Tests")
class AssertionMessageMaskerTest {

    @Nested
    @DisplayName("Static mask() Tests")
    class StaticMaskTests {

        @Test
        @DisplayName("Should mask email addresses")
        void shouldMaskEmailAddresses() {
            String message = "Expected email: john@example.com";
            String masked = AssertionMessageMasker.mask(message);
            assertThat(masked).doesNotContain("john@example.com");
            assertThat(masked).contains("j***@***.com");
        }

        @Test
        @DisplayName("Should mask phone numbers")
        void shouldMaskPhoneNumbers() {
            String message = "Expected phone: (555) 123-4567";
            String masked = AssertionMessageMasker.mask(message);
            assertThat(masked).doesNotContain("123-4567");
            assertThat(masked).contains("555****");
        }

        @Test
        @DisplayName("Should mask credit card numbers")
        void shouldMaskCreditCardNumbers() {
            String message = "Card: 1234-5678-9012-3456";
            String masked = AssertionMessageMasker.mask(message);
            assertThat(masked).doesNotContain("1234-5678");
            assertThat(masked).contains("****3456");
        }

        @Test
        @DisplayName("Should mask SSN")
        void shouldMaskSsn() {
            String message = "SSN: 123-45-6789";
            String masked = AssertionMessageMasker.mask(message);
            assertThat(masked).doesNotContain("123-45");
            assertThat(masked).contains("***-**-6789");
        }

        @Test
        @DisplayName("Should mask API keys")
        void shouldMaskApiKeys() {
            String message = "api_key=abcdefghijklmnopqrstuvwxyz";
            String masked = AssertionMessageMasker.mask(message);
            assertThat(masked).doesNotContain("abcdefghijklmnopqrstuvwxyz");
            assertThat(masked).contains("***MASKED***");
        }

        @Test
        @DisplayName("Should mask passwords")
        void shouldMaskPasswords() {
            String message = "password=secret123";
            String masked = AssertionMessageMasker.mask(message);
            assertThat(masked).doesNotContain("secret123");
            assertThat(masked).contains("password=********");
        }

        @Test
        @DisplayName("Should mask IP addresses")
        void shouldMaskIpAddresses() {
            String message = "IP: 192.168.1.100";
            String masked = AssertionMessageMasker.mask(message);
            assertThat(masked).doesNotContain("192.168.1.100");
            assertThat(masked).contains("192.***.***.100");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            String masked = AssertionMessageMasker.mask(null);
            assertThat(masked).isNull();
        }

        @Test
        @DisplayName("Should return empty for empty input")
        void shouldReturnEmptyForEmptyInput() {
            String masked = AssertionMessageMasker.mask("");
            assertThat(masked).isEmpty();
        }

        @Test
        @DisplayName("Should not modify text without sensitive data")
        void shouldNotModifyTextWithoutSensitiveData() {
            String message = "Expected value: 42";
            String masked = AssertionMessageMasker.mask(message);
            assertThat(masked).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create masker with custom pattern")
        void shouldCreateMaskerWithCustomPattern() {
            AssertionMessageMasker masker = AssertionMessageMasker.builder()
                .addPattern(Pattern.compile("secret-\\w+"), "***REDACTED***")
                .build();

            String result = masker.apply("Token: secret-abc123");
            assertThat(result).contains("***REDACTED***");
            assertThat(result).doesNotContain("secret-abc123");
        }

        @Test
        @DisplayName("Should create masker with regex string")
        void shouldCreateMaskerWithRegexString() {
            AssertionMessageMasker masker = AssertionMessageMasker.builder()
                .addPattern("\\d{4}", "****")
                .build();

            String result = masker.apply("PIN: 1234");
            assertThat(result).contains("****");
        }

        @Test
        @DisplayName("Should create masker with custom masker function")
        void shouldCreateMaskerWithCustomMaskerFunction() {
            AssertionMessageMasker masker = AssertionMessageMasker.builder()
                .addRule(Pattern.compile("\\d+"), s -> "*".repeat(s.length()))
                .build();

            String result = masker.apply("Number: 12345");
            assertThat(result).contains("*****");
        }

        @Test
        @DisplayName("maskEmails should add email masking")
        void maskEmailsShouldAddEmailMasking() {
            AssertionMessageMasker masker = AssertionMessageMasker.builder()
                .maskEmails()
                .build();

            String result = masker.apply("Email: john@example.com");
            assertThat(result).contains("@***.com");
        }

        @Test
        @DisplayName("maskPhones should add phone masking")
        void maskPhonesShouldAddPhoneMasking() {
            AssertionMessageMasker masker = AssertionMessageMasker.builder()
                .maskPhones()
                .build();

            String result = masker.apply("Phone: 555-123-4567");
            assertThat(result).contains("555****");
        }

        @Test
        @DisplayName("maskCreditCards should add credit card masking")
        void maskCreditCardsShouldAddCreditCardMasking() {
            AssertionMessageMasker masker = AssertionMessageMasker.builder()
                .maskCreditCards()
                .build();

            String result = masker.apply("Card: 1234567890123456");
            assertThat(result).contains("****3456");
        }

        @Test
        @DisplayName("maskIpAddresses should add IP masking")
        void maskIpAddressesShouldAddIpMasking() {
            AssertionMessageMasker masker = AssertionMessageMasker.builder()
                .maskIpAddresses()
                .build();

            String result = masker.apply("IP: 10.0.0.1");
            assertThat(result).contains("10.***.***.1");
        }
    }

    @Nested
    @DisplayName("builderWithDefaults Tests")
    class BuilderWithDefaultsTests {

        @Test
        @DisplayName("Should include default rules")
        void shouldIncludeDefaultRules() {
            AssertionMessageMasker masker = AssertionMessageMasker.builderWithDefaults()
                .build();

            String result = masker.apply("Email: test@example.com");
            assertThat(result).doesNotContain("test@example.com");
        }

        @Test
        @DisplayName("Should allow adding custom rules")
        void shouldAllowAddingCustomRules() {
            AssertionMessageMasker masker = AssertionMessageMasker.builderWithDefaults()
                .addPattern("custom-\\w+", "***CUSTOM***")
                .build();

            String result = masker.apply("Value: custom-value Email: test@example.com");
            assertThat(result).contains("***CUSTOM***");
            assertThat(result).doesNotContain("test@example.com");
        }
    }

    @Nested
    @DisplayName("apply() Tests")
    class ApplyTests {

        @Test
        @DisplayName("Should apply multiple rules")
        void shouldApplyMultipleRules() {
            String message = "User: john@example.com, Phone: 555-123-4567, IP: 192.168.1.1";
            String masked = AssertionMessageMasker.mask(message);

            assertThat(masked).doesNotContain("john@example.com");
            assertThat(masked).doesNotContain("123-4567");
            assertThat(masked).doesNotContain("168.1");
        }

        @Test
        @DisplayName("Should handle multiple occurrences")
        void shouldHandleMultipleOccurrences() {
            String message = "From: john@a.com To: jane@b.com";
            String masked = AssertionMessageMasker.mask(message);

            assertThat(masked).doesNotContain("john@a.com");
            assertThat(masked).doesNotContain("jane@b.com");
        }
    }
}
