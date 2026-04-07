package cloud.opencode.base.csv.validator;

import cloud.opencode.base.csv.CsvDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CsvValidator 测试
 */
@DisplayName("CsvValidator 测试")
class CsvValidatorTest {

    private CsvDocument sampleDoc() {
        return CsvDocument.builder()
                .header("name", "age", "status")
                .addRow("Alice", "30", "active")
                .addRow("Bob", "25", "inactive")
                .addRow("Charlie", "35", "active")
                .build();
    }

    @Nested
    @DisplayName("Builder 参数校验")
    class BuilderValidation {

        @Test
        @DisplayName("notBlank - column为null时抛出异常")
        void notBlankNullColumn() {
            assertThatThrownBy(() -> CsvValidator.builder().notBlank(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("range - min > max时抛出异常")
        void rangeMinGreaterThanMax() {
            assertThatThrownBy(() -> CsvValidator.builder().range("age", 100, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("min");
        }

        @Test
        @DisplayName("pattern - null regex时抛出异常")
        void patternNullRegex() {
            assertThatThrownBy(() -> CsvValidator.builder().pattern("name", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("minLength - 负数min时抛出异常")
        void minLengthNegative() {
            assertThatThrownBy(() -> CsvValidator.builder().minLength("name", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("maxLength - 负数max时抛出异常")
        void maxLengthNegative() {
            assertThatThrownBy(() -> CsvValidator.builder().maxLength("name", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("oneOf - 空允许值时抛出异常")
        void oneOfEmpty() {
            assertThatThrownBy(() -> CsvValidator.builder().oneOf("status"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("custom - null参数时抛出异常")
        void customNullArgs() {
            assertThatThrownBy(() -> CsvValidator.builder().custom(null, v -> true, "msg"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvValidator.builder().custom("col", null, "msg"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CsvValidator.builder().custom("col", v -> true, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("validate - 空文档")
    class EmptyDocument {

        @Test
        @DisplayName("空文档验证返回成功")
        void emptyDocReturnsSuccess() {
            CsvDocument empty = CsvDocument.builder().header("name").build();
            CsvValidator validator = CsvValidator.builder().notBlank("name").build();
            CsvValidationResult result = validator.validate(empty);
            assertThat(result.valid()).isTrue();
            assertThat(result.errors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("validate - null参数")
    class NullDoc {

        @Test
        @DisplayName("null文档抛出异常")
        void nullDocThrows() {
            CsvValidator validator = CsvValidator.builder().notBlank("name").build();
            assertThatThrownBy(() -> validator.validate(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("notBlank 规则")
    class NotBlankRule {

        @Test
        @DisplayName("所有值非空 - 验证通过")
        void allNonBlankPass() {
            CsvValidator validator = CsvValidator.builder().notBlank("name").build();
            CsvValidationResult result = validator.validate(sampleDoc());
            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("空值 - 验证失败")
        void blankValueFails() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .addRow("", "25")
                    .addRow("  ", "20")
                    .build();
            CsvValidator validator = CsvValidator.builder().notBlank("name").build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).hasSize(2);
            assertThat(result.errors().get(0).rowIndex()).isEqualTo(1);
            assertThat(result.errors().get(0).rule()).isEqualTo("notBlank");
            assertThat(result.errors().get(1).rowIndex()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("range 规则")
    class RangeRule {

        @Test
        @DisplayName("数值在范围内 - 验证通过")
        void inRangePass() {
            CsvValidator validator = CsvValidator.builder().range("age", 0, 150).build();
            CsvValidationResult result = validator.validate(sampleDoc());
            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("数值超出范围 - 验证失败")
        void outOfRangeFails() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "score")
                    .addRow("Alice", "50")
                    .addRow("Bob", "150")
                    .addRow("Charlie", "-5")
                    .build();
            CsvValidator validator = CsvValidator.builder().range("score", 0, 100).build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).hasSize(2);
        }

        @Test
        @DisplayName("非数字值 - 验证失败")
        void nonNumericFails() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "score")
                    .addRow("Alice", "abc")
                    .build();
            CsvValidator validator = CsvValidator.builder().range("score", 0, 100).build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().value()).isEqualTo("abc");
        }

        @Test
        @DisplayName("空白值 - 验证失败")
        void blankValueFails() {
            CsvDocument doc = CsvDocument.builder()
                    .header("score")
                    .addRow("")
                    .build();
            CsvValidator validator = CsvValidator.builder().range("score", 0, 100).build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
        }

        @Test
        @DisplayName("边界值 - 验证通过")
        void boundaryValuesPass() {
            CsvDocument doc = CsvDocument.builder()
                    .header("score")
                    .addRow("0.0")
                    .addRow("100.0")
                    .build();
            CsvValidator validator = CsvValidator.builder().range("score", 0.0, 100.0).build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isTrue();
        }
    }

    @Nested
    @DisplayName("pattern 规则")
    class PatternRule {

        @Test
        @DisplayName("匹配模式 - 验证通过")
        void matchingPass() {
            CsvDocument doc = CsvDocument.builder()
                    .header("code")
                    .addRow("ABC123")
                    .addRow("XYZ999")
                    .build();
            CsvValidator validator = CsvValidator.builder().pattern("code", "[A-Z]{3}\\d{3}").build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("不匹配模式 - 验证失败")
        void nonMatchingFails() {
            CsvDocument doc = CsvDocument.builder()
                    .header("code")
                    .addRow("abc")
                    .build();
            CsvValidator validator = CsvValidator.builder().pattern("code", "\\d+").build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
            assertThat(result.errors().getFirst().rule()).startsWith("pattern[");
        }
    }

    @Nested
    @DisplayName("minLength 规则")
    class MinLengthRule {

        @Test
        @DisplayName("满足最小长度 - 验证通过")
        void meetsMinLengthPass() {
            CsvValidator validator = CsvValidator.builder().minLength("name", 3).build();
            CsvValidationResult result = validator.validate(sampleDoc());
            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("不满足最小长度 - 验证失败")
        void belowMinLengthFails() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name")
                    .addRow("AB")
                    .addRow("A")
                    .build();
            CsvValidator validator = CsvValidator.builder().minLength("name", 3).build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("maxLength 规则")
    class MaxLengthRule {

        @Test
        @DisplayName("满足最大长度 - 验证通过")
        void meetsMaxLengthPass() {
            CsvValidator validator = CsvValidator.builder().maxLength("name", 10).build();
            CsvValidationResult result = validator.validate(sampleDoc());
            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("超出最大长度 - 验证失败")
        void exceedsMaxLengthFails() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name")
                    .addRow("VeryLongName")
                    .build();
            CsvValidator validator = CsvValidator.builder().maxLength("name", 5).build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
        }

        @Test
        @DisplayName("null值不算超出最大长度")
        void nullValuePassesMaxLength() {
            // maxLength allows null (column may have fewer fields)
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "extra")
                    .addRow("A")
                    .build();
            CsvValidator validator = CsvValidator.builder().maxLength("extra", 5).build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isTrue();
        }
    }

    @Nested
    @DisplayName("oneOf 规则")
    class OneOfRule {

        @Test
        @DisplayName("允许值 - 验证通过")
        void allowedValuePass() {
            CsvValidator validator = CsvValidator.builder()
                    .oneOf("status", "active", "inactive")
                    .build();
            CsvValidationResult result = validator.validate(sampleDoc());
            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("不在允许值中 - 验证失败")
        void disallowedValueFails() {
            CsvDocument doc = CsvDocument.builder()
                    .header("status")
                    .addRow("active")
                    .addRow("unknown")
                    .build();
            CsvValidator validator = CsvValidator.builder()
                    .oneOf("status", "active", "inactive")
                    .build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().value()).isEqualTo("unknown");
        }

        @Test
        @DisplayName("大小写敏感")
        void caseSensitive() {
            CsvDocument doc = CsvDocument.builder()
                    .header("status")
                    .addRow("Active")
                    .build();
            CsvValidator validator = CsvValidator.builder()
                    .oneOf("status", "active")
                    .build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
        }
    }

    @Nested
    @DisplayName("custom 规则")
    class CustomRule {

        @Test
        @DisplayName("自定义规则通过")
        void customRulePass() {
            CsvValidator validator = CsvValidator.builder()
                    .custom("name", v -> v != null && v.length() > 2, "Name too short")
                    .build();
            CsvValidationResult result = validator.validate(sampleDoc());
            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("自定义规则失败")
        void customRuleFails() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name")
                    .addRow("OK")
                    .addRow("X")
                    .build();
            CsvValidator validator = CsvValidator.builder()
                    .custom("name", v -> v != null && v.length() >= 3, "Name too short")
                    .build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("列不存在")
    class MissingColumn {

        @Test
        @DisplayName("列不存在时报告单个错误")
        void missingColumnReportsSingleError() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name")
                    .addRow("Alice")
                    .addRow("Bob")
                    .build();
            CsvValidator validator = CsvValidator.builder().notBlank("email").build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().message()).contains("not found");
            assertThat(result.errors().getFirst().rowIndex()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("组合规则")
    class MultipleRules {

        @Test
        @DisplayName("多规则收集所有错误")
        void multipleRulesCollectAllErrors() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age", "status")
                    .addRow("", "200", "unknown")
                    .build();
            CsvValidator validator = CsvValidator.builder()
                    .notBlank("name")
                    .range("age", 0, 150)
                    .oneOf("status", "active", "inactive")
                    .build();
            CsvValidationResult result = validator.validate(doc);
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).hasSize(3);
        }

        @Test
        @DisplayName("所有规则通过")
        void allRulesPass() {
            CsvValidator validator = CsvValidator.builder()
                    .notBlank("name")
                    .range("age", 0, 150)
                    .oneOf("status", "active", "inactive")
                    .build();
            CsvValidationResult result = validator.validate(sampleDoc());
            assertThat(result.valid()).isTrue();
        }
    }

    @Nested
    @DisplayName("CsvValidationResult")
    class ResultTest {

        @Test
        @DisplayName("success()返回valid=true和空errors")
        void successResult() {
            CsvValidationResult result = CsvValidationResult.success();
            assertThat(result.valid()).isTrue();
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("failure()返回valid=false和非空errors")
        void failureResult() {
            var error = new CsvValidationError(0, "col", "val", "rule", "msg");
            CsvValidationResult result = CsvValidationResult.failure(List.of(error));
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).hasSize(1);
        }

        @Test
        @DisplayName("failure()空列表时抛出异常")
        void failureEmptyErrors() {
            assertThatThrownBy(() -> CsvValidationResult.failure(List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("failure() null时抛出异常")
        void failureNullErrors() {
            assertThatThrownBy(() -> CsvValidationResult.failure(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("CsvValidationError 记录")
    class ErrorRecordTest {

        @Test
        @DisplayName("记录字段可访问")
        void recordFieldsAccessible() {
            var error = new CsvValidationError(2, "age", "abc", "range[0.0,100.0]", "Invalid age");
            assertThat(error.rowIndex()).isEqualTo(2);
            assertThat(error.column()).isEqualTo("age");
            assertThat(error.value()).isEqualTo("abc");
            assertThat(error.rule()).isEqualTo("range[0.0,100.0]");
            assertThat(error.message()).isEqualTo("Invalid age");
        }
    }

    @Nested
    @DisplayName("无规则验证器")
    class NoRules {

        @Test
        @DisplayName("无规则时验证通过")
        void noRulesAlwaysPass() {
            CsvValidator validator = CsvValidator.builder().build();
            CsvValidationResult result = validator.validate(sampleDoc());
            assertThat(result.valid()).isTrue();
        }
    }
}
