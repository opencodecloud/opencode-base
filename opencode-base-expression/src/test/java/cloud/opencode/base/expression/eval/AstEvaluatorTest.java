package cloud.opencode.base.expression.eval;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.ast.LiteralNode;
import cloud.opencode.base.expression.ast.Node;
import cloud.opencode.base.expression.context.StandardContext;
import cloud.opencode.base.expression.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * AstEvaluator Tests
 * AstEvaluator 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("AstEvaluator Tests | AstEvaluator 测试")
class AstEvaluatorTest {

    private AstEvaluator evaluator;
    private StandardContext context;

    @BeforeEach
    void setup() {
        evaluator = new AstEvaluator();
        context = new StandardContext();
    }

    @Nested
    @DisplayName("Singleton Tests | 单例测试")
    class SingletonTests {

        @Test
        @DisplayName("getInstance returns same instance | getInstance 返回相同实例")
        void testGetInstance() {
            AstEvaluator e1 = AstEvaluator.getInstance();
            AstEvaluator e2 = AstEvaluator.getInstance();
            assertThat(e1).isSameAs(e2);
        }
    }

    @Nested
    @DisplayName("Evaluate Tests | evaluate 测试")
    class EvaluateTests {

        @Test
        @DisplayName("evaluate null returns null | evaluate null 返回 null")
        void testEvaluateNull() {
            assertThat(evaluator.evaluate(null, context)).isNull();
        }

        @Test
        @DisplayName("evaluate literal | evaluate 字面量")
        void testEvaluateLiteral() {
            Node node = Parser.parse("42");
            assertThat(evaluator.evaluate(node, context)).isEqualTo(42);
        }

        @Test
        @DisplayName("evaluate expression | evaluate 表达式")
        void testEvaluateExpression() {
            Node node = Parser.parse("1 + 2 * 3");
            assertThat(evaluator.evaluate(node, context)).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("EvaluateAll Tests | evaluateAll 测试")
    class EvaluateAllTests {

        @Test
        @DisplayName("evaluateAll returns all results | evaluateAll 返回所有结果")
        void testEvaluateAll() {
            List<Node> nodes = List.of(
                    Parser.parse("1"),
                    Parser.parse("2"),
                    Parser.parse("3")
            );
            List<Object> results = evaluator.evaluateAll(nodes, context);
            assertThat(results).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("EvaluateAsBoolean Tests | evaluateAsBoolean 测试")
    class EvaluateAsBooleanTests {

        @Test
        @DisplayName("evaluateAsBoolean converts result | evaluateAsBoolean 转换结果")
        void testEvaluateAsBoolean() {
            assertThat(evaluator.evaluateAsBoolean(Parser.parse("true"), context)).isTrue();
            assertThat(evaluator.evaluateAsBoolean(Parser.parse("false"), context)).isFalse();
            assertThat(evaluator.evaluateAsBoolean(Parser.parse("1"), context)).isTrue();
            assertThat(evaluator.evaluateAsBoolean(Parser.parse("0"), context)).isFalse();
        }
    }

    @Nested
    @DisplayName("EvaluateAsNumber Tests | evaluateAsNumber 测试")
    class EvaluateAsNumberTests {

        @Test
        @DisplayName("evaluateAsNumber returns number | evaluateAsNumber 返回数字")
        void testEvaluateAsNumber() {
            Node node = Parser.parse("42");
            assertThat(evaluator.evaluateAsNumber(node, context)).isEqualTo(42);
        }

        @Test
        @DisplayName("evaluateAsNumber parses string | evaluateAsNumber 解析字符串")
        void testEvaluateAsNumberParsesString() {
            context.setVariable("x", "42");
            Node node = Parser.parse("x");
            assertThat(evaluator.evaluateAsNumber(node, context)).isEqualTo(42L);  // String parsed to Long

            context.setVariable("y", "3.14");
            Node floatNode = Parser.parse("y");
            assertThat(evaluator.evaluateAsNumber(floatNode, context)).isEqualTo(3.14);
        }

        @Test
        @DisplayName("evaluateAsNumber throws for invalid | evaluateAsNumber 对无效值抛出异常")
        void testEvaluateAsNumberThrows() {
            context.setVariable("x", "not a number");
            Node node = Parser.parse("x");
            assertThatThrownBy(() -> evaluator.evaluateAsNumber(node, context))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("evaluateAsNumber throws for non-numeric | evaluateAsNumber 对非数字抛出异常")
        void testEvaluateAsNumberThrowsForNonNumeric() {
            context.setVariable("x", List.of(1, 2, 3));
            Node node = Parser.parse("x");
            assertThatThrownBy(() -> evaluator.evaluateAsNumber(node, context))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("EvaluateAsString Tests | evaluateAsString 测试")
    class EvaluateAsStringTests {

        @Test
        @DisplayName("evaluateAsString returns string | evaluateAsString 返回字符串")
        void testEvaluateAsString() {
            Node node = Parser.parse("'hello'");
            assertThat(evaluator.evaluateAsString(node, context)).isEqualTo("hello");
        }

        @Test
        @DisplayName("evaluateAsString converts to string | evaluateAsString 转换为字符串")
        void testEvaluateAsStringConverts() {
            Node node = Parser.parse("42");
            assertThat(evaluator.evaluateAsString(node, context)).isEqualTo("42");
        }

        @Test
        @DisplayName("evaluateAsString returns null for null | evaluateAsString 对 null 返回 null")
        void testEvaluateAsStringNull() {
            Node node = Parser.parse("null");
            assertThat(evaluator.evaluateAsString(node, context)).isNull();
        }
    }

    @Nested
    @DisplayName("EvaluateWithTimeout Tests | evaluateWithTimeout 测试")
    class EvaluateWithTimeoutTests {

        @Test
        @DisplayName("evaluateWithTimeout returns result | evaluateWithTimeout 返回结果")
        void testEvaluateWithTimeout() {
            Node node = Parser.parse("1 + 2");
            assertThat(evaluator.evaluateWithTimeout(node, context, 1000)).isEqualTo(3);
        }

        @Test
        @DisplayName("evaluateWithTimeout with zero timeout | evaluateWithTimeout 零超时")
        void testEvaluateWithZeroTimeout() {
            Node node = Parser.parse("1 + 2");
            assertThat(evaluator.evaluateWithTimeout(node, context, 0)).isEqualTo(3);
        }

        @Test
        @DisplayName("evaluateWithTimeout with negative timeout | evaluateWithTimeout 负超时")
        void testEvaluateWithNegativeTimeout() {
            Node node = Parser.parse("1 + 2");
            assertThat(evaluator.evaluateWithTimeout(node, context, -1)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Static Eval Tests | 静态 eval 测试")
    class StaticEvalTests {

        @Test
        @DisplayName("eval static method | eval 静态方法")
        void testStaticEval() {
            Node node = Parser.parse("1 + 2");
            assertThat(AstEvaluator.eval(node, context)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Evaluator Interface Tests | Evaluator 接口测试")
    class EvaluatorInterfaceTests {

        @Test
        @DisplayName("evaluate with target type | evaluate 使用目标类型")
        void testEvaluateWithTargetType() {
            Node node = Parser.parse("42");
            assertThat(evaluator.evaluate(node, context, String.class)).isEqualTo("42");
        }

        @Test
        @DisplayName("canEvaluate returns true | canEvaluate 返回 true")
        void testCanEvaluate() {
            Node node = Parser.parse("1");
            assertThat(evaluator.canEvaluate(node)).isTrue();
        }
    }
}
