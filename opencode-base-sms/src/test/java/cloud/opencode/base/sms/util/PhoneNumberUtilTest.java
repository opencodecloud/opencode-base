package cloud.opencode.base.sms.util;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PhoneNumberUtilTest Tests
 * PhoneNumberUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("PhoneNumberUtil 测试")
class PhoneNumberUtilTest {

    @Nested
    @DisplayName("isValid方法测试")
    class IsValidTests {

        @Test
        @DisplayName("有效号码返回true")
        void testValid() {
            assertThat(PhoneNumberUtil.isValid("13800138000")).isTrue();
            assertThat(PhoneNumberUtil.isValid("+8613800138000")).isTrue();
        }

        @Test
        @DisplayName("无效号码返回false")
        void testInvalid() {
            assertThat(PhoneNumberUtil.isValid("123")).isFalse();
            assertThat(PhoneNumberUtil.isValid("")).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testNull() {
            assertThat(PhoneNumberUtil.isValid(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isChinaMobile方法测试")
    class IsChinaMobileTests {

        @Test
        @DisplayName("有效中国手机号返回true")
        void testValidChinaMobile() {
            assertThat(PhoneNumberUtil.isChinaMobile("13800138000")).isTrue();
            assertThat(PhoneNumberUtil.isChinaMobile("15912345678")).isTrue();
        }

        @Test
        @DisplayName("非中国手机号返回false")
        void testNotChinaMobile() {
            assertThat(PhoneNumberUtil.isChinaMobile("+14155552671")).isFalse();
        }
    }

    @Nested
    @DisplayName("normalize方法测试")
    class NormalizeTests {

        @Test
        @DisplayName("移除格式字符")
        void testNormalize() {
            assertThat(PhoneNumberUtil.normalize("138-0013-8000")).isEqualTo("13800138000");
            assertThat(PhoneNumberUtil.normalize("(138) 0013 8000")).isEqualTo("13800138000");
        }

        @Test
        @DisplayName("保留+号")
        void testNormalizeKeepPlus() {
            assertThat(PhoneNumberUtil.normalize("+86 138 0013 8000")).isEqualTo("+8613800138000");
        }
    }

    @Nested
    @DisplayName("formatWithCountryCode方法测试")
    class FormatWithCountryCodeTests {

        @Test
        @DisplayName("添加中国区号")
        void testFormatWithChinaCode() {
            // Uses country code "CN", not dialing code "86"
            String formatted = PhoneNumberUtil.formatWithCountryCode("13800138000", "CN");

            assertThat(formatted).isEqualTo("+8613800138000");
        }

        @Test
        @DisplayName("已有区号不重复添加")
        void testFormatAlreadyHasCode() {
            String formatted = PhoneNumberUtil.formatWithCountryCode("+8613800138000", "CN");

            assertThat(formatted).isEqualTo("+8613800138000");
        }

        @Test
        @DisplayName("未知国家代码返回原号码")
        void testFormatUnknownCountryCode() {
            // When country code is not in the map, returns normalized phone number
            String formatted = PhoneNumberUtil.formatWithCountryCode("13800138000", "86");

            assertThat(formatted).isEqualTo("13800138000");
        }
    }

    @Nested
    @DisplayName("formatChina方法测试")
    class FormatChinaTests {

        @Test
        @DisplayName("格式化为中国格式")
        void testFormatChina() {
            String formatted = PhoneNumberUtil.formatChina("13800138000");

            assertThat(formatted).isEqualTo("+8613800138000");
        }
    }

    @Nested
    @DisplayName("mask方法测试")
    class MaskTests {

        @Test
        @DisplayName("遮盖中间数字")
        void testMask() {
            String masked = PhoneNumberUtil.mask("13800138000");

            assertThat(masked).contains("*");
            assertThat(masked).hasSize(11);
        }

        @Test
        @DisplayName("null返回null")
        void testMaskNull() {
            assertThat(PhoneNumberUtil.mask(null)).isNull();
        }
    }

    @Nested
    @DisplayName("getCountryDialingCode方法测试")
    class GetCountryDialingCodeTests {

        @Test
        @DisplayName("返回中国区号")
        void testChinaCode() {
            String code = PhoneNumberUtil.getCountryDialingCode("CN");

            assertThat(code).isEqualTo("+86");
        }

        @Test
        @DisplayName("返回美国区号")
        void testUSCode() {
            String code = PhoneNumberUtil.getCountryDialingCode("US");

            assertThat(code).isEqualTo("+1");
        }
    }

    @Nested
    @DisplayName("extractCountryCode方法测试")
    class ExtractCountryCodeTests {

        @Test
        @DisplayName("提取区号返回国家代码")
        void testExtractCode() {
            String code = PhoneNumberUtil.extractCountryCode("+8613800138000");

            assertThat(code).isEqualTo("CN");
        }

        @Test
        @DisplayName("提取美国区号")
        void testExtractUSCode() {
            String code = PhoneNumberUtil.extractCountryCode("+14155552671");

            assertThat(code).isEqualTo("US");
        }

        @Test
        @DisplayName("无区号返回null")
        void testExtractNoCode() {
            String code = PhoneNumberUtil.extractCountryCode("13800138000");

            assertThat(code).isNull();
        }
    }
}
