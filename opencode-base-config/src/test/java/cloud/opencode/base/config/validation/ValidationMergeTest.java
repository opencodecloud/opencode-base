package cloud.opencode.base.config.validation;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ValidationResult.merge() and RequiredValidator batch error reporting test
 * ValidationResult.merge()和RequiredValidator批量错误报告测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.3
 */
@DisplayName("ValidationResult.merge 测试")
class ValidationMergeTest {

    @Nested
    @DisplayName("合并有效结果测试")
    class MergeValidResultsTests {

        @Test
        @DisplayName("合并多个有效结果返回有效")
        void testMergeAllValid() {
            List<ValidationResult> results = List.of(
                    ValidationResult.valid(),
                    ValidationResult.valid(),
                    ValidationResult.valid()
            );

            ValidationResult merged = ValidationResult.merge(results);

            assertThat(merged.isValid()).isTrue();
            assertThat(merged.getErrors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("合并混合结果测试")
    class MergeMixedResultsTests {

        @Test
        @DisplayName("合并有效和无效结果")
        void testMergeMixed() {
            List<ValidationResult> results = List.of(
                    ValidationResult.valid(),
                    ValidationResult.invalid("Error A"),
                    ValidationResult.valid(),
                    ValidationResult.invalid(List.of("Error B", "Error C"))
            );

            ValidationResult merged = ValidationResult.merge(results);

            assertThat(merged.isValid()).isFalse();
            assertThat(merged.getErrors()).containsExactly("Error A", "Error B", "Error C");
        }

        @Test
        @DisplayName("合并全部无效结果")
        void testMergeAllInvalid() {
            List<ValidationResult> results = List.of(
                    ValidationResult.invalid("Error 1"),
                    ValidationResult.invalid("Error 2")
            );

            ValidationResult merged = ValidationResult.merge(results);

            assertThat(merged.isValid()).isFalse();
            assertThat(merged.getErrors()).hasSize(2);
            assertThat(merged.getErrors()).containsExactly("Error 1", "Error 2");
        }
    }

    @Nested
    @DisplayName("合并空列表测试")
    class MergeEmptyListTests {

        @Test
        @DisplayName("合并空列表返回有效")
        void testMergeEmptyList() {
            ValidationResult merged = ValidationResult.merge(List.of());

            assertThat(merged.isValid()).isTrue();
            assertThat(merged.getErrors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("RequiredValidator批量错误报告测试")
    class RequiredValidatorBatchErrorTests {

        @Test
        @DisplayName("多个缺失键全部报告")
        void testAllMissingKeysReported() {
            RequiredValidator validator = new RequiredValidator(
                    "db.url", "db.username", "db.password", "api.key"
            );

            Config config = new ConfigBuilder()
                    .addProperties(Map.of("other.key", "value"))
                    .disablePlaceholders()
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(4);
            assertThat(result.getErrors())
                    .anyMatch(e -> e.contains("db.url"))
                    .anyMatch(e -> e.contains("db.username"))
                    .anyMatch(e -> e.contains("db.password"))
                    .anyMatch(e -> e.contains("api.key"));
        }

        @Test
        @DisplayName("部分缺失键全部报告")
        void testPartialMissingKeysReported() {
            RequiredValidator validator = new RequiredValidator(
                    "db.url", "db.username", "db.password"
            );

            Config config = new ConfigBuilder()
                    .addProperties(Map.of("db.url", "jdbc:h2:mem"))
                    .disablePlaceholders()
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(2);
            assertThat(result.getErrors())
                    .anyMatch(e -> e.contains("db.username"))
                    .anyMatch(e -> e.contains("db.password"));
            assertThat(result.getErrors())
                    .noneMatch(e -> e.contains("db.url"));
        }
    }

    @Nested
    @DisplayName("ConfigBuilder合并验证测试")
    class ConfigBuilderMergeTests {

        @Test
        @DisplayName("多个验证器的错误合并报告")
        void testMultipleValidatorsErrorsMerged() {
            assertThatThrownBy(() ->
                    new ConfigBuilder()
                            .addProperties(Map.of("other", "value"))
                            .disablePlaceholders()
                            .required("key.a")
                            .required("key.b")
                            .build()
            )
                    .isInstanceOf(cloud.opencode.base.config.OpenConfigException.class)
                    .hasMessageContaining("key.a")
                    .hasMessageContaining("key.b");
        }

        @Test
        @DisplayName("所有验证通过不抛异常")
        void testAllValidatorsPass() {
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("key.a", "1", "key.b", "2"))
                    .disablePlaceholders()
                    .required("key.a")
                    .required("key.b")
                    .build();

            assertThat(config).isNotNull();
        }
    }
}
