package cloud.opencode.base.config.validation;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import cloud.opencode.base.config.source.InMemoryConfigSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * RequiredValidator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("RequiredValidator 测试")
class RequiredValidatorTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("空必填键")
        void testEmptyRequiredKeys() {
            RequiredValidator validator = new RequiredValidator();

            Config config = new ConfigBuilder()
                    .addProperties(Map.of())
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("单个必填键")
        void testSingleRequiredKey() {
            RequiredValidator validator = new RequiredValidator("app.name");

            assertThat(validator).isNotNull();
        }

        @Test
        @DisplayName("多个必填键")
        void testMultipleRequiredKeys() {
            RequiredValidator validator = new RequiredValidator(
                    "database.url",
                    "database.username",
                    "database.password"
            );

            assertThat(validator).isNotNull();
        }
    }

    @Nested
    @DisplayName("validate测试")
    class ValidateTests {

        @Test
        @DisplayName("所有必填键存在 - 验证通过")
        void testAllKeysPresent() {
            RequiredValidator validator = new RequiredValidator("key1", "key2");

            Config config = new ConfigBuilder()
                    .addProperties(Map.of("key1", "value1", "key2", "value2"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("缺少一个必填键 - 验证失败")
        void testMissingOneKey() {
            RequiredValidator validator = new RequiredValidator("key1", "key2");

            Config config = new ConfigBuilder()
                    .addProperties(Map.of("key1", "value1"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0)).contains("key2");
        }

        @Test
        @DisplayName("缺少多个必填键 - 验证失败")
        void testMissingMultipleKeys() {
            RequiredValidator validator = new RequiredValidator("key1", "key2", "key3");

            Config config = new ConfigBuilder()
                    .addProperties(Map.of("key1", "value1"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(2);
        }

        @Test
        @DisplayName("所有必填键缺失 - 验证失败")
        void testAllKeysMissing() {
            RequiredValidator validator = new RequiredValidator("key1", "key2");

            Config config = new ConfigBuilder()
                    .addProperties(Map.of("other", "value"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(2);
        }

        @Test
        @DisplayName("空配置 - 验证失败（如果有必填键）")
        void testEmptyConfig() {
            RequiredValidator validator = new RequiredValidator("required.key");

            Config config = new ConfigBuilder()
                    .addProperties(Map.of())
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("空配置 - 验证通过（无必填键）")
        void testEmptyConfigNoRequiredKeys() {
            RequiredValidator validator = new RequiredValidator();

            Config config = new ConfigBuilder()
                    .addProperties(Map.of())
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("错误消息测试")
    class ErrorMessageTests {

        @Test
        @DisplayName("错误消息包含缺失的键名")
        void testErrorMessageContainsKeyName() {
            RequiredValidator validator = new RequiredValidator("database.url");

            Config config = new ConfigBuilder()
                    .addProperties(Map.of())
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.getErrors().get(0)).contains("database.url");
            assertThat(result.getErrors().get(0)).containsIgnoringCase("required");
        }
    }
}
