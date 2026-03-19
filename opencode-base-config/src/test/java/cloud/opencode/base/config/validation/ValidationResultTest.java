package cloud.opencode.base.config.validation;

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
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ValidationResult 测试")
class ValidationResultTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("valid - 创建有效结果")
        void testValid() {
            ValidationResult result = ValidationResult.valid();

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("valid - 返回相同实例")
        void testValidSameInstance() {
            ValidationResult result1 = ValidationResult.valid();
            ValidationResult result2 = ValidationResult.valid();

            assertThat(result1).isSameAs(result2);
        }

        @Test
        @DisplayName("invalid - 单个错误")
        void testInvalidSingleError() {
            ValidationResult result = ValidationResult.invalid("Error message");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).containsExactly("Error message");
        }

        @Test
        @DisplayName("invalid - 多个错误")
        void testInvalidMultipleErrors() {
            List<String> errors = List.of("Error 1", "Error 2", "Error 3");
            ValidationResult result = ValidationResult.invalid(errors);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).containsExactly("Error 1", "Error 2", "Error 3");
        }
    }

    @Nested
    @DisplayName("isValid测试")
    class IsValidTests {

        @Test
        @DisplayName("有效结果返回true")
        void testIsValidTrue() {
            ValidationResult result = ValidationResult.valid();
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("无效结果返回false")
        void testIsValidFalse() {
            ValidationResult result = ValidationResult.invalid("error");
            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("getErrors测试")
    class GetErrorsTests {

        @Test
        @DisplayName("有效结果 - 空错误列表")
        void testGetErrorsEmpty() {
            ValidationResult result = ValidationResult.valid();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("无效结果 - 返回错误列表")
        void testGetErrorsNotEmpty() {
            ValidationResult result = ValidationResult.invalid(List.of("Error 1", "Error 2"));
            assertThat(result.getErrors()).hasSize(2);
        }

        @Test
        @DisplayName("错误列表是不可变的")
        void testGetErrorsImmutable() {
            ValidationResult result = ValidationResult.invalid("error");

            assertThatThrownBy(() -> result.getErrors().add("new error"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("有效结果")
        void testToStringValid() {
            ValidationResult result = ValidationResult.valid();
            assertThat(result.toString()).isEqualTo("Valid");
        }

        @Test
        @DisplayName("无效结果")
        void testToStringInvalid() {
            ValidationResult result = ValidationResult.invalid("Error message");

            String str = result.toString();
            assertThat(str).contains("Invalid");
            assertThat(str).contains("Error message");
        }
    }
}
