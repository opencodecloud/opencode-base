package cloud.opencode.base.string.regex;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * RegexPatternTest Tests
 * RegexPatternTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("RegexPattern Tests")
class RegexPatternTest {

    @Nested
    @DisplayName("Number Pattern Tests")
    class NumberPatternTests {

        @Test
        @DisplayName("INTEGER should match integers")
        void integerShouldMatchIntegers() {
            assertThat(RegexPattern.INTEGER.matcher("123").matches()).isTrue();
            assertThat(RegexPattern.INTEGER.matcher("-456").matches()).isTrue();
            assertThat(RegexPattern.INTEGER.matcher("12.34").matches()).isFalse();
        }

        @Test
        @DisplayName("POSITIVE_INTEGER should match positive integers")
        void positiveIntegerShouldMatchPositiveIntegers() {
            assertThat(RegexPattern.POSITIVE_INTEGER.matcher("123").matches()).isTrue();
            assertThat(RegexPattern.POSITIVE_INTEGER.matcher("-123").matches()).isFalse();
        }

        @Test
        @DisplayName("DECIMAL should match decimals")
        void decimalShouldMatchDecimals() {
            assertThat(RegexPattern.DECIMAL.matcher("12.34").matches()).isTrue();
            assertThat(RegexPattern.DECIMAL.matcher("-12.34").matches()).isTrue();
            assertThat(RegexPattern.DECIMAL.matcher("123").matches()).isFalse();
        }

        @Test
        @DisplayName("NUMBER should match integers and decimals")
        void numberShouldMatchIntegersAndDecimals() {
            assertThat(RegexPattern.NUMBER.matcher("123").matches()).isTrue();
            assertThat(RegexPattern.NUMBER.matcher("12.34").matches()).isTrue();
            assertThat(RegexPattern.NUMBER.matcher("-12.34").matches()).isTrue();
        }
    }

    @Nested
    @DisplayName("String Pattern Tests")
    class StringPatternTests {

        @Test
        @DisplayName("LETTERS should match only letters")
        void lettersShouldMatchOnlyLetters() {
            assertThat(RegexPattern.LETTERS.matcher("hello").matches()).isTrue();
            assertThat(RegexPattern.LETTERS.matcher("Hello").matches()).isTrue();
            assertThat(RegexPattern.LETTERS.matcher("hello123").matches()).isFalse();
        }

        @Test
        @DisplayName("LOWER_LETTERS should match lowercase letters")
        void lowerLettersShouldMatchLowercaseLetters() {
            assertThat(RegexPattern.LOWER_LETTERS.matcher("hello").matches()).isTrue();
            assertThat(RegexPattern.LOWER_LETTERS.matcher("Hello").matches()).isFalse();
        }

        @Test
        @DisplayName("UPPER_LETTERS should match uppercase letters")
        void upperLettersShouldMatchUppercaseLetters() {
            assertThat(RegexPattern.UPPER_LETTERS.matcher("HELLO").matches()).isTrue();
            assertThat(RegexPattern.UPPER_LETTERS.matcher("Hello").matches()).isFalse();
        }

        @Test
        @DisplayName("ALPHANUMERIC should match letters and digits")
        void alphanumericShouldMatchLettersAndDigits() {
            assertThat(RegexPattern.ALPHANUMERIC.matcher("hello123").matches()).isTrue();
            assertThat(RegexPattern.ALPHANUMERIC.matcher("hello_123").matches()).isFalse();
        }

        @Test
        @DisplayName("CHINESE should match Chinese characters")
        void chineseShouldMatchChineseCharacters() {
            assertThat(RegexPattern.CHINESE.matcher("中文").matches()).isTrue();
            assertThat(RegexPattern.CHINESE.matcher("hello").matches()).isFalse();
        }
    }

    @Nested
    @DisplayName("Identifier Pattern Tests")
    class IdentifierPatternTests {

        @Test
        @DisplayName("IDENTIFIER should match valid identifiers")
        void identifierShouldMatchValidIdentifiers() {
            assertThat(RegexPattern.IDENTIFIER.matcher("myVar").matches()).isTrue();
            assertThat(RegexPattern.IDENTIFIER.matcher("_private").matches()).isTrue();
            assertThat(RegexPattern.IDENTIFIER.matcher("my_var_1").matches()).isTrue();
            assertThat(RegexPattern.IDENTIFIER.matcher("1var").matches()).isFalse();
        }

        @Test
        @DisplayName("UUID should match valid UUIDs")
        void uuidShouldMatchValidUuids() {
            assertThat(RegexPattern.UUID.matcher("550e8400-e29b-41d4-a716-446655440000").matches()).isTrue();
            assertThat(RegexPattern.UUID.matcher("invalid-uuid").matches()).isFalse();
        }
    }

    @Nested
    @DisplayName("Network Pattern Tests")
    class NetworkPatternTests {

        @Test
        @DisplayName("EMAIL should match valid emails")
        void emailShouldMatchValidEmails() {
            assertThat(RegexPattern.EMAIL.matcher("test@example.com").matches()).isTrue();
            assertThat(RegexPattern.EMAIL.matcher("user.name+tag@domain.co").matches()).isTrue();
            assertThat(RegexPattern.EMAIL.matcher("invalid").matches()).isFalse();
        }

        @Test
        @DisplayName("URL should match valid URLs")
        void urlShouldMatchValidUrls() {
            assertThat(RegexPattern.URL.matcher("https://example.com").matches()).isTrue();
            assertThat(RegexPattern.URL.matcher("http://example.com/path?query=1").matches()).isTrue();
            assertThat(RegexPattern.URL.matcher("invalid").matches()).isFalse();
        }

        @Test
        @DisplayName("IPV4 should match valid IPv4 addresses")
        void ipv4ShouldMatchValidIpv4Addresses() {
            assertThat(RegexPattern.IPV4.matcher("192.168.1.1").matches()).isTrue();
            assertThat(RegexPattern.IPV4.matcher("255.255.255.255").matches()).isTrue();
            assertThat(RegexPattern.IPV4.matcher("256.1.1.1").matches()).isFalse();
        }

        @Test
        @DisplayName("DOMAIN should match valid domains")
        void domainShouldMatchValidDomains() {
            assertThat(RegexPattern.DOMAIN.matcher("example.com").matches()).isTrue();
            assertThat(RegexPattern.DOMAIN.matcher("sub.example.com").matches()).isTrue();
        }
    }

    @Nested
    @DisplayName("China Pattern Tests")
    class ChinaPatternTests {

        @Test
        @DisplayName("MOBILE_CN should match Chinese mobile numbers")
        void mobileCnShouldMatchChineseMobileNumbers() {
            assertThat(RegexPattern.MOBILE_CN.matcher("13812345678").matches()).isTrue();
            assertThat(RegexPattern.MOBILE_CN.matcher("12345678901").matches()).isFalse();
        }

        @Test
        @DisplayName("ID_CARD_CN should match Chinese ID cards")
        void idCardCnShouldMatchChineseIdCards() {
            assertThat(RegexPattern.ID_CARD_CN.matcher("11010519491231002X").matches()).isTrue();
            assertThat(RegexPattern.ID_CARD_CN.matcher("123456").matches()).isFalse();
        }

        @Test
        @DisplayName("POSTAL_CODE_CN should match Chinese postal codes")
        void postalCodeCnShouldMatchChinesePostalCodes() {
            assertThat(RegexPattern.POSTAL_CODE_CN.matcher("100000").matches()).isTrue();
            assertThat(RegexPattern.POSTAL_CODE_CN.matcher("12345").matches()).isFalse();
        }
    }

    @Nested
    @DisplayName("DateTime Pattern Tests")
    class DateTimePatternTests {

        @Test
        @DisplayName("DATE should match date format")
        void dateShouldMatchDateFormat() {
            assertThat(RegexPattern.DATE.matcher("2024-01-15").matches()).isTrue();
            assertThat(RegexPattern.DATE.matcher("01-15-2024").matches()).isFalse();
        }

        @Test
        @DisplayName("TIME should match time format")
        void timeShouldMatchTimeFormat() {
            assertThat(RegexPattern.TIME.matcher("12:30:45").matches()).isTrue();
            assertThat(RegexPattern.TIME.matcher("12:30").matches()).isFalse();
        }

        @Test
        @DisplayName("DATETIME should match datetime format")
        void datetimeShouldMatchDatetimeFormat() {
            assertThat(RegexPattern.DATETIME.matcher("2024-01-15 12:30:45").matches()).isTrue();
        }
    }

    @Nested
    @DisplayName("Validation Method Tests")
    class ValidationMethodTests {

        @Test
        @DisplayName("isEmail should validate emails")
        void isEmailShouldValidateEmails() {
            assertThat(RegexPattern.isEmail("test@example.com")).isTrue();
            assertThat(RegexPattern.isEmail("invalid")).isFalse();
            assertThat(RegexPattern.isEmail(null)).isFalse();
        }

        @Test
        @DisplayName("isUrl should validate URLs")
        void isUrlShouldValidateUrls() {
            assertThat(RegexPattern.isUrl("https://example.com")).isTrue();
            assertThat(RegexPattern.isUrl("invalid")).isFalse();
            assertThat(RegexPattern.isUrl(null)).isFalse();
        }

        @Test
        @DisplayName("isMobile should validate mobile numbers")
        void isMobileShouldValidateMobileNumbers() {
            assertThat(RegexPattern.isMobile("13812345678")).isTrue();
            assertThat(RegexPattern.isMobile("12345678901")).isFalse();
            assertThat(RegexPattern.isMobile(null)).isFalse();
        }

        @Test
        @DisplayName("isIdCard should validate ID cards")
        void isIdCardShouldValidateIdCards() {
            assertThat(RegexPattern.isIdCard("11010519491231002X")).isTrue();
            assertThat(RegexPattern.isIdCard("123")).isFalse();
            assertThat(RegexPattern.isIdCard(null)).isFalse();
        }

        @Test
        @DisplayName("isIpv4 should validate IPv4 addresses")
        void isIpv4ShouldValidateIpv4Addresses() {
            assertThat(RegexPattern.isIpv4("192.168.1.1")).isTrue();
            assertThat(RegexPattern.isIpv4("invalid")).isFalse();
            assertThat(RegexPattern.isIpv4(null)).isFalse();
        }

        @Test
        @DisplayName("isUuid should validate UUIDs")
        void isUuidShouldValidateUuids() {
            assertThat(RegexPattern.isUuid("550e8400-e29b-41d4-a716-446655440000")).isTrue();
            assertThat(RegexPattern.isUuid("invalid")).isFalse();
            assertThat(RegexPattern.isUuid(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = RegexPattern.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
