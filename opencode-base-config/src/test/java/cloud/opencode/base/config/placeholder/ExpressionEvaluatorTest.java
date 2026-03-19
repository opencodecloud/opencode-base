package cloud.opencode.base.config.placeholder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ExpressionEvaluator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ExpressionEvaluator 测试")
class ExpressionEvaluatorTest {

    @Nested
    @DisplayName("evaluate测试")
    class EvaluateTests {

        @Test
        @DisplayName("简单表达式 - 原样返回")
        void testSimpleExpression() {
            String result = ExpressionEvaluator.evaluate("hello");
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("空字符串")
        void testEmptyExpression() {
            String result = ExpressionEvaluator.evaluate("");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null表达式")
        void testNullExpression() {
            String result = ExpressionEvaluator.evaluate(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("复杂字符串")
        void testComplexExpression() {
            String result = ExpressionEvaluator.evaluate("${some.key} + ${other.key}");
            assertThat(result).isEqualTo("${some.key} + ${other.key}");
        }
    }

    @Nested
    @DisplayName("canEvaluate测试")
    class CanEvaluateTests {

        @Test
        @DisplayName("非空字符串 - 可评估")
        void testNonBlankCanEvaluate() {
            assertThat(ExpressionEvaluator.canEvaluate("hello")).isTrue();
        }

        @Test
        @DisplayName("空字符串 - 不可评估")
        void testBlankCannotEvaluate() {
            assertThat(ExpressionEvaluator.canEvaluate("")).isFalse();
            assertThat(ExpressionEvaluator.canEvaluate("   ")).isFalse();
        }

        @Test
        @DisplayName("null - 不可评估")
        void testNullCannotEvaluate() {
            assertThat(ExpressionEvaluator.canEvaluate(null)).isFalse();
        }
    }
}
