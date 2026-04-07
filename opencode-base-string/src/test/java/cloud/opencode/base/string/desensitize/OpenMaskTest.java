package cloud.opencode.base.string.desensitize;

import cloud.opencode.base.string.desensitize.strategy.DesensitizeType;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenMaskTest Tests
 * OpenMaskTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenMask Tests")
class OpenMaskTest {

    @Nested
    @DisplayName("mobile Tests")
    class MobileTests {

        @Test
        @DisplayName("Should mask mobile number")
        void shouldMaskMobileNumber() {
            assertThat(OpenMask.mobile("13812345678")).isEqualTo("138****5678");
            assertThat(OpenMask.maskMobile("13812345678")).isEqualTo("138****5678");
        }

        @Test
        @DisplayName("Should handle short numbers")
        void shouldHandleShortNumbers() {
            assertThat(OpenMask.mobile("1234567")).isEqualTo("1234567");
        }

        @Test
        @DisplayName("Should handle null")
        void shouldHandleNull() {
            assertThat(OpenMask.mobile(null)).isNull();
        }
    }

    @Nested
    @DisplayName("idCard Tests")
    class IdCardTests {

        @Test
        @DisplayName("Should mask ID card")
        void shouldMaskIdCard() {
            assertThat(OpenMask.idCard("110101199001011234")).isEqualTo("110101********1234");
            assertThat(OpenMask.maskIdCard("110101199001011234")).isEqualTo("110101********1234");
        }

        @Test
        @DisplayName("Should handle short ID cards")
        void shouldHandleShortIdCards() {
            assertThat(OpenMask.idCard("1234567890")).isEqualTo("1234567890");
        }
    }

    @Nested
    @DisplayName("email Tests")
    class EmailTests {

        @Test
        @DisplayName("Should mask email")
        void shouldMaskEmail() {
            assertThat(OpenMask.email("test@example.com")).isEqualTo("t***t@example.com");
            assertThat(OpenMask.maskEmail("test@example.com")).isEqualTo("t***t@example.com");
        }

        @Test
        @DisplayName("Should mask single-char username for security (regression fix)")
        void shouldHandleShortUsername() {
            // Single-char prefix must be masked — returning it unmasked leaks the address.
            // 单字符前缀必须掩码，原样返回会泄漏地址。
            assertThat(OpenMask.email("a@example.com")).isEqualTo("*@example.com");
            assertThat(OpenMask.email("ab@example.com")).isEqualTo("a*@example.com");
        }

        @Test
        @DisplayName("Should handle invalid email")
        void shouldHandleInvalidEmail() {
            assertThat(OpenMask.email("invalid")).isEqualTo("invalid");
            assertThat(OpenMask.email(null)).isNull();
        }
    }

    @Nested
    @DisplayName("bankCard Tests")
    class BankCardTests {

        @Test
        @DisplayName("Should mask bank card")
        void shouldMaskBankCard() {
            assertThat(OpenMask.bankCard("6222021234567890123")).isEqualTo("6222***********0123");
            assertThat(OpenMask.maskBankCard("6222021234567890123")).isEqualTo("6222***********0123");
        }

        @Test
        @DisplayName("Should handle short card numbers")
        void shouldHandleShortCardNumbers() {
            assertThat(OpenMask.bankCard("12345678")).isEqualTo("12345678");
        }
    }

    @Nested
    @DisplayName("chineseName Tests")
    class ChineseNameTests {

        @Test
        @DisplayName("Should mask Chinese name")
        void shouldMaskChineseName() {
            assertThat(OpenMask.chineseName("张三")).isEqualTo("张*");
            assertThat(OpenMask.maskName("李四五")).isEqualTo("李**");
        }

        @Test
        @DisplayName("Should handle single character name")
        void shouldHandleSingleCharacterName() {
            assertThat(OpenMask.chineseName("张")).isEqualTo("张");
        }

        @Test
        @DisplayName("Should handle null")
        void shouldHandleNull() {
            assertThat(OpenMask.chineseName(null)).isNull();
        }
    }

    @Nested
    @DisplayName("maskAddress Tests")
    class MaskAddressTests {

        @Test
        @DisplayName("Should mask address")
        void shouldMaskAddress() {
            // "北京市朝阳区xxx街道xxx号" = 15 chars, keep first 6, mask rest (9 chars)
            assertThat(OpenMask.maskAddress("北京市朝阳区xxx街道xxx号")).isEqualTo("北京市朝阳区*********");
        }

        @Test
        @DisplayName("Should handle short addresses")
        void shouldHandleShortAddresses() {
            assertThat(OpenMask.maskAddress("北京")).isEqualTo("北京");
        }
    }

    @Nested
    @DisplayName("mask Tests")
    class MaskTests {

        @Test
        @DisplayName("Should mask with custom parameters")
        void shouldMaskWithCustomParameters() {
            assertThat(OpenMask.mask("1234567890", 2, 2, '*')).isEqualTo("12******90");
            assertThat(OpenMask.mask("1234567890", 3, 3, '#')).isEqualTo("123####890");
        }

        @Test
        @DisplayName("Should return original if too short")
        void shouldReturnOriginalIfTooShort() {
            assertThat(OpenMask.mask("123", 2, 2, '*')).isEqualTo("123");
        }

        @Test
        @DisplayName("Should handle null")
        void shouldHandleNull() {
            assertThat(OpenMask.mask(null, 2, 2, '*')).isNull();
        }
    }

    @Nested
    @DisplayName("maskMiddle Tests")
    class MaskMiddleTests {

        @Test
        @DisplayName("Should mask middle symmetrically")
        void shouldMaskMiddleSymmetrically() {
            assertThat(OpenMask.maskMiddle("1234567890", 3, '*')).isEqualTo("123****890");
        }
    }

    @Nested
    @DisplayName("maskByPattern Tests")
    class MaskByPatternTests {

        @Test
        @DisplayName("Should mask by pattern")
        void shouldMaskByPattern() {
            assertThat(OpenMask.maskByPattern("abc123def", "\\d", '*')).isEqualTo("abc***def");
        }

        @Test
        @DisplayName("Should handle null")
        void shouldHandleNull() {
            assertThat(OpenMask.maskByPattern(null, "\\d", '*')).isNull();
        }
    }

    @Nested
    @DisplayName("desensitize Tests")
    class DesensitizeTests {

        @Test
        @DisplayName("Should desensitize by type")
        void shouldDesensitizeByType() {
            assertThat(OpenMask.desensitize("13812345678", DesensitizeType.MOBILE_PHONE))
                .isEqualTo("138****5678");
            assertThat(OpenMask.desensitize("110101199001011234", DesensitizeType.ID_CARD))
                .isEqualTo("110101********1234");
            assertThat(OpenMask.desensitize("test@example.com", DesensitizeType.EMAIL))
                .isEqualTo("t***t@example.com");
            assertThat(OpenMask.desensitize("6222021234567890123", DesensitizeType.BANK_CARD))
                .isEqualTo("6222***********0123");
            assertThat(OpenMask.desensitize("张三", DesensitizeType.CHINESE_NAME))
                .isEqualTo("张*");
        }

        @Test
        @DisplayName("Should handle PASSWORD type")
        void shouldHandlePasswordType() {
            assertThat(OpenMask.desensitize("mypassword", DesensitizeType.PASSWORD))
                .isEqualTo("******");
        }

        @Test
        @DisplayName("Should handle CUSTOM type")
        void shouldHandleCustomType() {
            assertThat(OpenMask.desensitize("value", DesensitizeType.CUSTOM))
                .isEqualTo("value");
        }

        @Test
        @DisplayName("Should handle null value")
        void shouldHandleNullValue() {
            assertThat(OpenMask.desensitize(null, DesensitizeType.MOBILE_PHONE)).isNull();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenMask.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
