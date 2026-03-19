package cloud.opencode.base.config.validation;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * RangeValidator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("RangeValidator 测试")
class RangeValidatorTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用整数范围")
        void testConstructorWithIntegerRange() {
            RangeValidator validator = new RangeValidator("port", 1, 65535);
            assertThat(validator).isNotNull();
        }

        @Test
        @DisplayName("使用双精度范围")
        void testConstructorWithDoubleRange() {
            RangeValidator validator = new RangeValidator("percentage", 0.0, 100.0);
            assertThat(validator).isNotNull();
        }

        @Test
        @DisplayName("使用包含/排除边界")
        void testConstructorWithInclusiveExclusive() {
            RangeValidator validator = new RangeValidator("value", 0, 100, false, true);
            assertThat(validator).isNotNull();
        }

        @Test
        @DisplayName("只有最小值")
        void testConstructorMinOnly() {
            RangeValidator validator = new RangeValidator("value", 0, null);
            assertThat(validator).isNotNull();
        }

        @Test
        @DisplayName("只有最大值")
        void testConstructorMaxOnly() {
            RangeValidator validator = new RangeValidator("value", null, 100);
            assertThat(validator).isNotNull();
        }
    }

    @Nested
    @DisplayName("包含边界测试")
    class InclusiveBoundsTests {

        @Test
        @DisplayName("值在范围内 - 验证通过")
        void testValueInRange() {
            RangeValidator validator = new RangeValidator("port", 1, 65535);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("port", "8080"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("值等于最小值 - 验证通过")
        void testValueEqualsMin() {
            RangeValidator validator = new RangeValidator("port", 1, 65535);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("port", "1"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("值等于最大值 - 验证通过")
        void testValueEqualsMax() {
            RangeValidator validator = new RangeValidator("port", 1, 65535);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("port", "65535"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("值小于最小值 - 验证失败")
        void testValueBelowMin() {
            RangeValidator validator = new RangeValidator("port", 1, 65535);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("port", "0"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0)).contains("port");
        }

        @Test
        @DisplayName("值大于最大值 - 验证失败")
        void testValueAboveMax() {
            RangeValidator validator = new RangeValidator("port", 1, 65535);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("port", "65536"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("排除边界测试")
    class ExclusiveBoundsTests {

        @Test
        @DisplayName("值等于排除的最小值 - 验证失败")
        void testValueEqualsExclusiveMin() {
            RangeValidator validator = new RangeValidator("value", 0, 100, false, true);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("value", "0"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("值等于排除的最大值 - 验证失败")
        void testValueEqualsExclusiveMax() {
            RangeValidator validator = new RangeValidator("value", 0, 100, true, false);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("value", "100"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("值在排除边界内 - 验证通过")
        void testValueInsideExclusiveBounds() {
            RangeValidator validator = new RangeValidator("value", 0, 100, false, false);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("value", "50"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("单边界测试")
    class SingleBoundTests {

        @Test
        @DisplayName("只有最小值 - 值大于最小值")
        void testMinOnlyValueAbove() {
            RangeValidator validator = new RangeValidator("value", 0, null);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("value", "100"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("只有最小值 - 值小于最小值")
        void testMinOnlyValueBelow() {
            RangeValidator validator = new RangeValidator("value", 0, null);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("value", "-1"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("只有最大值 - 值小于最大值")
        void testMaxOnlyValueBelow() {
            RangeValidator validator = new RangeValidator("value", null, 100);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("value", "50"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("只有最大值 - 值大于最大值")
        void testMaxOnlyValueAbove() {
            RangeValidator validator = new RangeValidator("value", null, 100);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("value", "150"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("双精度范围测试")
    class DoubleRangeTests {

        @Test
        @DisplayName("双精度值在范围内")
        void testDoubleInRange() {
            RangeValidator validator = new RangeValidator("percentage", 0.0, 100.0);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("percentage", "50.5"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("双精度值等于边界")
        void testDoubleAtBoundary() {
            RangeValidator validator = new RangeValidator("percentage", 0.0, 100.0);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("percentage", "100.0"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("键不存在 - 验证通过")
        void testKeyNotExists() {
            RangeValidator validator = new RangeValidator("missing", 0, 100);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("other", "value"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("空值 - 验证通过")
        void testEmptyValue() {
            RangeValidator validator = new RangeValidator("value", 0, 100);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("value", ""))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("非数字值 - 验证失败")
        void testNonNumericValue() {
            RangeValidator validator = new RangeValidator("value", 0, 100);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("value", "not-a-number"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0)).contains("not a valid number");
        }

        @Test
        @DisplayName("负数范围")
        void testNegativeRange() {
            RangeValidator validator = new RangeValidator("temp", -50, 50);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("temp", "-25"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("错误消息测试")
    class ErrorMessageTests {

        @Test
        @DisplayName("错误消息包含键名")
        void testErrorMessageContainsKey() {
            RangeValidator validator = new RangeValidator("server.port", 1, 65535);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("server.port", "70000"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.getErrors().get(0)).contains("server.port");
        }

        @Test
        @DisplayName("错误消息包含边界值")
        void testErrorMessageContainsBounds() {
            RangeValidator validator = new RangeValidator("port", 1, 65535);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("port", "70000"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.getErrors().get(0)).contains("65535");
        }

        @Test
        @DisplayName("错误消息包含实际值")
        void testErrorMessageContainsActualValue() {
            RangeValidator validator = new RangeValidator("port", 1, 65535);
            Config config = new ConfigBuilder()
                    .addProperties(Map.of("port", "70000"))
                    .build();

            ValidationResult result = validator.validate(config);

            assertThat(result.getErrors().get(0)).contains("70000");
        }
    }
}
