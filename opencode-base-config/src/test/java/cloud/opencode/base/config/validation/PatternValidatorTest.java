package cloud.opencode.base.config.validation;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.assertj.core.api.Assertions.*;

/**
 * PatternValidator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("PatternValidator 测试")
class PatternValidatorTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用正则表达式字符串")
        void testConstructorWithRegex() {
            PatternValidator validator = new PatternValidator("email", "^[A-Za-z]+$");
            assertThat(validator.getPattern()).isNotNull();
        }

        @Test
        @DisplayName("使用正则表达式和自定义消息")
        void testConstructorWithRegexAndMessage() {
            PatternValidator validator = new PatternValidator(
                    "email", "^[A-Za-z]+$", "Must be letters only");
            assertThat(validator.getErrorMessage()).isEqualTo("Must be letters only");
        }

        @Test
        @DisplayName("使用预编译的Pattern")
        void testConstructorWithPattern() {
            Pattern pattern = Pattern.compile("^\\d+$");
            PatternValidator validator = new PatternValidator("number", pattern, "Must be digits");

            assertThat(validator.getPattern()).isSameAs(pattern);
        }

        @Test
        @DisplayName("无效正则表达式 - 抛出异常")
        void testConstructorInvalidRegex() {
            assertThatThrownBy(() -> new PatternValidator("key", "[invalid"))
                    .isInstanceOf(PatternSyntaxException.class);
        }
    }

    @Nested
    @DisplayName("validate测试")
    class ValidateTests {

        @Test
        @DisplayName("值匹配模式 - 验证通过")
        void testValidateMatch() {
            PatternValidator validator = new PatternValidator("name", "^[A-Za-z]+$");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("name", "John"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("值不匹配模式 - 验证失败")
        void testValidateNoMatch() {
            PatternValidator validator = new PatternValidator("name", "^[A-Za-z]+$");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("name", "John123"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0)).contains("name");
        }

        @Test
        @DisplayName("键不存在 - 验证通过")
        void testValidateKeyNotExists() {
            PatternValidator validator = new PatternValidator("missing", "^[A-Za-z]+$");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("other", "value"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("空值 - 验证通过")
        void testValidateEmptyValue() {
            PatternValidator validator = new PatternValidator("key", "^[A-Za-z]+$");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("key", ""))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("邮箱验证测试")
    class EmailValidationTests {

        @Test
        @DisplayName("有效邮箱地址")
        void testValidEmail() {
            PatternValidator validator = PatternValidator.email("user.email");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("user.email", "test@example.com"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("无效邮箱地址")
        void testInvalidEmail() {
            PatternValidator validator = PatternValidator.email("user.email");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("user.email", "invalid-email"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("URL验证测试")
    class UrlValidationTests {

        @Test
        @DisplayName("有效HTTP URL")
        void testValidHttpUrl() {
            PatternValidator validator = PatternValidator.url("website.url");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("website.url", "http://example.com"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("有效HTTPS URL")
        void testValidHttpsUrl() {
            PatternValidator validator = PatternValidator.url("website.url");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("website.url", "https://example.com/path"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("无效URL")
        void testInvalidUrl() {
            PatternValidator validator = PatternValidator.url("website.url");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("website.url", "not-a-url"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("IPv4验证测试")
    class Ipv4ValidationTests {

        @Test
        @DisplayName("有效IPv4地址")
        void testValidIpv4() {
            PatternValidator validator = PatternValidator.ipv4("server.ip");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("server.ip", "192.168.1.1"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("边界IPv4地址")
        void testBoundaryIpv4() {
            PatternValidator validator = PatternValidator.ipv4("server.ip");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("server.ip", "255.255.255.255"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("无效IPv4地址")
        void testInvalidIpv4() {
            PatternValidator validator = PatternValidator.ipv4("server.ip");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("server.ip", "256.1.1.1"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("电话号码验证测试")
    class PhoneValidationTests {

        @Test
        @DisplayName("有效国际电话号码")
        void testValidPhone() {
            PatternValidator validator = PatternValidator.phone("contact.phone");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("contact.phone", "+8613812345678"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("无加号的电话号码")
        void testPhoneWithoutPlus() {
            PatternValidator validator = PatternValidator.phone("contact.phone");
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("contact.phone", "13812345678"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("常量模式测试")
    class PatternConstantsTests {

        @Test
        @DisplayName("EMAIL_PATTERN常量")
        void testEmailPatternConstant() {
            assertThat(PatternValidator.EMAIL_PATTERN).isNotNull();
            assertThat(PatternValidator.EMAIL_PATTERN).contains("@");
        }

        @Test
        @DisplayName("URL_PATTERN常量")
        void testUrlPatternConstant() {
            assertThat(PatternValidator.URL_PATTERN).isNotNull();
            assertThat(PatternValidator.URL_PATTERN).contains("http");
        }

        @Test
        @DisplayName("IPV4_PATTERN常量")
        void testIpv4PatternConstant() {
            assertThat(PatternValidator.IPV4_PATTERN).isNotNull();
        }

        @Test
        @DisplayName("PHONE_PATTERN常量")
        void testPhonePatternConstant() {
            assertThat(PatternValidator.PHONE_PATTERN).isNotNull();
        }
    }
}
