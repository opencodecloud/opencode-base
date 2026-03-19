package cloud.opencode.base.graph.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ValidationResult 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("ValidationResult 测试")
class ValidationResultTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建带警告和错误的结果")
        void testConstructor() {
            List<String> warnings = List.of("warning1", "warning2");
            List<String> errors = List.of("error1");

            ValidationResult result = new ValidationResult(warnings, errors);

            assertThat(result.warnings()).containsExactly("warning1", "warning2");
            assertThat(result.errors()).containsExactly("error1");
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("success()创建成功结果")
        void testSuccess() {
            ValidationResult result = ValidationResult.success();

            assertThat(result.isValid()).isTrue();
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.hasWarnings()).isFalse();
        }

        @Test
        @DisplayName("error()创建单错误结果")
        void testError() {
            ValidationResult result = ValidationResult.error("Something went wrong");

            assertThat(result.isValid()).isFalse();
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).containsExactly("Something went wrong");
        }

        @Test
        @DisplayName("warning()创建单警告结果")
        void testWarning() {
            ValidationResult result = ValidationResult.warning("Be careful");

            assertThat(result.isValid()).isTrue();
            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.warnings()).containsExactly("Be careful");
        }
    }

    @Nested
    @DisplayName("状态检查测试")
    class StatusCheckTests {

        @Test
        @DisplayName("isValid()无错误时返回true")
        void testIsValidNoErrors() {
            ValidationResult result = new ValidationResult(List.of("warning"), List.of());

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("isValid()有错误时返回false")
        void testIsValidWithErrors() {
            ValidationResult result = new ValidationResult(List.of(), List.of("error"));

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("hasErrors()检测是否有错误")
        void testHasErrors() {
            ValidationResult withErrors = ValidationResult.error("error");
            ValidationResult noErrors = ValidationResult.success();

            assertThat(withErrors.hasErrors()).isTrue();
            assertThat(noErrors.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("hasWarnings()检测是否有警告")
        void testHasWarnings() {
            ValidationResult withWarnings = ValidationResult.warning("warning");
            ValidationResult noWarnings = ValidationResult.success();

            assertThat(withWarnings.hasWarnings()).isTrue();
            assertThat(noWarnings.hasWarnings()).isFalse();
        }
    }

    @Nested
    @DisplayName("计数方法测试")
    class CountTests {

        @Test
        @DisplayName("errorCount()返回错误数量")
        void testErrorCount() {
            ValidationResult result = new ValidationResult(List.of(), List.of("e1", "e2", "e3"));

            assertThat(result.errorCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("warningCount()返回警告数量")
        void testWarningCount() {
            ValidationResult result = new ValidationResult(List.of("w1", "w2"), List.of());

            assertThat(result.warningCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("merge测试")
    class MergeTests {

        @Test
        @DisplayName("合并两个结果")
        void testMerge() {
            ValidationResult r1 = new ValidationResult(List.of("w1"), List.of("e1"));
            ValidationResult r2 = new ValidationResult(List.of("w2"), List.of("e2"));

            ValidationResult merged = r1.merge(r2);

            assertThat(merged.warnings()).containsExactly("w1", "w2");
            assertThat(merged.errors()).containsExactly("e1", "e2");
        }

        @Test
        @DisplayName("合并不影响原结果")
        void testMergeImmutability() {
            ValidationResult r1 = new ValidationResult(List.of("w1"), List.of("e1"));
            ValidationResult r2 = new ValidationResult(List.of("w2"), List.of("e2"));

            r1.merge(r2);

            assertThat(r1.warningCount()).isEqualTo(1);
            assertThat(r1.errorCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("包含关键信息")
        void testToString() {
            ValidationResult result = new ValidationResult(
                List.of("warning"),
                List.of("error")
            );

            String str = result.toString();

            assertThat(str).contains("ValidationResult");
            assertThat(str).contains("valid=false");
            assertThat(str).contains("errors=1");
            assertThat(str).contains("warnings=1");
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("构建空结果")
        void testBuildEmpty() {
            ValidationResult result = new ValidationResult.Builder().build();

            assertThat(result.isValid()).isTrue();
            assertThat(result.warningCount()).isEqualTo(0);
            assertThat(result.errorCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("添加警告")
        void testAddWarning() {
            ValidationResult result = new ValidationResult.Builder()
                .addWarning("w1")
                .addWarning("w2")
                .build();

            assertThat(result.warnings()).containsExactly("w1", "w2");
        }

        @Test
        @DisplayName("添加错误")
        void testAddError() {
            ValidationResult result = new ValidationResult.Builder()
                .addError("e1")
                .addError("e2")
                .build();

            assertThat(result.errors()).containsExactly("e1", "e2");
        }

        @Test
        @DisplayName("链式调用")
        void testFluentApi() {
            ValidationResult result = new ValidationResult.Builder()
                .addWarning("w1")
                .addError("e1")
                .addWarning("w2")
                .addError("e2")
                .build();

            assertThat(result.warningCount()).isEqualTo(2);
            assertThat(result.errorCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("不可变性测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("warnings列表不可修改")
        void testWarningsImmutable() {
            ValidationResult result = new ValidationResult(List.of("w1"), List.of());

            assertThatThrownBy(() -> result.warnings().add("w2"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("errors列表不可修改")
        void testErrorsImmutable() {
            ValidationResult result = new ValidationResult(List.of(), List.of("e1"));

            assertThatThrownBy(() -> result.errors().add("e2"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
