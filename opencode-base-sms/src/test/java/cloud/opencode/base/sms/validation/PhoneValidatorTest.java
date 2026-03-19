package cloud.opencode.base.sms.validation;

import cloud.opencode.base.sms.exception.SmsException;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PhoneValidatorTest Tests
 * PhoneValidatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("PhoneValidator 测试")
class PhoneValidatorTest {

    @Nested
    @DisplayName("isValidChinaMobile方法测试")
    class IsValidChinaMobileTests {

        @Test
        @DisplayName("有效中国手机号返回true")
        void testValidChinaMobile() {
            assertThat(PhoneValidator.isValidChinaMobile("13800138000")).isTrue();
            assertThat(PhoneValidator.isValidChinaMobile("15912345678")).isTrue();
            assertThat(PhoneValidator.isValidChinaMobile("18888888888")).isTrue();
        }

        @Test
        @DisplayName("带+86前缀返回false-纯数字格式不支持前缀")
        void testChinaMobileWithPrefixReturnsFalse() {
            // CHINA_MOBILE_PATTERN only matches pure 11-digit numbers starting with 1[3-9]
            assertThat(PhoneValidator.isValidChinaMobile("+8613800138000")).isFalse();
        }

        @Test
        @DisplayName("无效手机号返回false")
        void testInvalidChinaMobile() {
            assertThat(PhoneValidator.isValidChinaMobile("12345678901")).isFalse(); // 不以1[3-9]开头
            assertThat(PhoneValidator.isValidChinaMobile("1380013800")).isFalse();  // 少一位
            assertThat(PhoneValidator.isValidChinaMobile("138001380001")).isFalse(); // 多一位
        }

        @Test
        @DisplayName("null返回false")
        void testNullChinaMobile() {
            assertThat(PhoneValidator.isValidChinaMobile(null)).isFalse();
        }

        @Test
        @DisplayName("空字符串返回false")
        void testEmptyChinaMobile() {
            assertThat(PhoneValidator.isValidChinaMobile("")).isFalse();
        }
    }

    @Nested
    @DisplayName("isValidE164方法测试")
    class IsValidE164Tests {

        @Test
        @DisplayName("有效E.164格式返回true")
        void testValidE164() {
            assertThat(PhoneValidator.isValidE164("+8613800138000")).isTrue();
            assertThat(PhoneValidator.isValidE164("+14155552671")).isTrue();
        }

        @Test
        @DisplayName("缺少+号返回false")
        void testE164WithoutPlus() {
            assertThat(PhoneValidator.isValidE164("8613800138000")).isFalse();
        }

        @Test
        @DisplayName("太短返回false")
        void testE164TooShort() {
            assertThat(PhoneValidator.isValidE164("+1")).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testE164Null() {
            assertThat(PhoneValidator.isValidE164(null)).isFalse();
        }

        @Test
        @DisplayName("空字符串返回false")
        void testE164Empty() {
            assertThat(PhoneValidator.isValidE164("")).isFalse();
        }
    }

    @Nested
    @DisplayName("isValid方法测试")
    class IsValidTests {

        @Test
        @DisplayName("中国手机号返回true")
        void testValidChinese() {
            assertThat(PhoneValidator.isValid("13800138000")).isTrue();
        }

        @Test
        @DisplayName("E.164格式返回true")
        void testValidE164Format() {
            assertThat(PhoneValidator.isValid("+8613800138000")).isTrue();
        }

        @Test
        @DisplayName("无效号码返回false")
        void testInvalid() {
            assertThat(PhoneValidator.isValid("123")).isFalse();
        }
    }

    @Nested
    @DisplayName("validate方法测试")
    class ValidateTests {

        @Test
        @DisplayName("有效号码不抛出异常")
        void testValidateValid() {
            assertThatNoException().isThrownBy(() -> PhoneValidator.validate("13800138000"));
        }

        @Test
        @DisplayName("无效号码抛出SmsException")
        void testValidateInvalid() {
            assertThatThrownBy(() -> PhoneValidator.validate("123"))
                    .isInstanceOf(SmsException.class);
        }

        @Test
        @DisplayName("null抛出SmsException")
        void testValidateNull() {
            assertThatThrownBy(() -> PhoneValidator.validate(null))
                    .isInstanceOf(SmsException.class);
        }
    }

    @Nested
    @DisplayName("validateAll方法测试")
    class ValidateAllTests {

        @Test
        @DisplayName("所有有效不抛出异常")
        void testValidateAllValid() {
            List<String> phones = List.of("13800138000", "15912345678", "+8618888888888");

            assertThatNoException().isThrownBy(() -> PhoneValidator.validateAll(phones));
        }

        @Test
        @DisplayName("包含无效抛出SmsException")
        void testValidateAllWithInvalid() {
            List<String> phones = List.of("13800138000", "invalid", "15912345678");

            assertThatThrownBy(() -> PhoneValidator.validateAll(phones))
                    .isInstanceOf(SmsException.class);
        }
    }

    @Nested
    @DisplayName("mask方法测试")
    class MaskTests {

        @Test
        @DisplayName("遮盖中间数字")
        void testMask() {
            String masked = PhoneValidator.mask("13800138000");

            assertThat(masked).startsWith("138");
            assertThat(masked).endsWith("8000");
            assertThat(masked).contains("*");
        }

        @Test
        @DisplayName("短号码返回***")
        void testMaskShort() {
            String masked = PhoneValidator.mask("123");

            assertThat(masked).isEqualTo("***");
        }

        @Test
        @DisplayName("null返回***")
        void testMaskNull() {
            assertThat(PhoneValidator.mask(null)).isEqualTo("***");
        }
    }

    @Nested
    @DisplayName("normalize方法测试")
    class NormalizeTests {

        @Test
        @DisplayName("移除空格和破折号")
        void testNormalizeSpacesAndDashes() {
            String normalized = PhoneValidator.normalize("138-0013-8000");

            assertThat(normalized).isEqualTo("13800138000");
        }

        @Test
        @DisplayName("移除括号")
        void testNormalizeParentheses() {
            String normalized = PhoneValidator.normalize("(138) 0013 8000");

            assertThat(normalized).isEqualTo("13800138000");
        }

        @Test
        @DisplayName("保留+号")
        void testNormalizeKeepPlus() {
            String normalized = PhoneValidator.normalize("+86 138 0013 8000");

            assertThat(normalized).isEqualTo("+8613800138000");
        }

        @Test
        @DisplayName("null返回null")
        void testNormalizeNull() {
            assertThat(PhoneValidator.normalize(null)).isNull();
        }
    }

    @Nested
    @DisplayName("extractCountryCode方法测试")
    class ExtractCountryCodeTests {

        @Test
        @DisplayName("提取中国区号")
        void testExtractChina() {
            String code = PhoneValidator.extractCountryCode("+8613800138000");

            assertThat(code).isEqualTo("8613");
        }

        @Test
        @DisplayName("提取美国区号")
        void testExtractUS() {
            String code = PhoneValidator.extractCountryCode("+14155552671");

            assertThat(code).isEqualTo("1415");
        }

        @Test
        @DisplayName("无+号返回null")
        void testExtractNoPlus() {
            String code = PhoneValidator.extractCountryCode("13800138000");

            assertThat(code).isNull();
        }

        @Test
        @DisplayName("null返回null")
        void testExtractNull() {
            assertThat(PhoneValidator.extractCountryCode(null)).isNull();
        }
    }
}
