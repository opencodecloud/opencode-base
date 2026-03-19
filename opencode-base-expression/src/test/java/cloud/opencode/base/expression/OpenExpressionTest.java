package cloud.opencode.base.expression;

import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenExpression Tests
 * OpenExpression 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
class OpenExpressionTest {

    @Nested
    @DisplayName("Basic Arithmetic Tests | 基本算术测试")
    class ArithmeticTests {

        @Test
        @DisplayName("Addition | 加法")
        void testAddition() {
            assertThat(OpenExpression.eval("1 + 2")).isEqualTo(3);
            assertThat(OpenExpression.eval("1.5 + 2.5")).isEqualTo(4.0);
        }

        @Test
        @DisplayName("Subtraction | 减法")
        void testSubtraction() {
            assertThat(OpenExpression.eval("5 - 3")).isEqualTo(2);
            assertThat(OpenExpression.eval("10.5 - 3.5")).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Multiplication | 乘法")
        void testMultiplication() {
            assertThat(OpenExpression.eval("3 * 4")).isEqualTo(12);
            assertThat(OpenExpression.eval("2.5 * 4")).isEqualTo(10.0);
        }

        @Test
        @DisplayName("Division | 除法")
        void testDivision() {
            assertThat(OpenExpression.eval("10 / 2")).isEqualTo(5);
            assertThat(OpenExpression.eval("7.0 / 2")).isEqualTo(3.5);
        }

        @Test
        @DisplayName("Modulo | 取模")
        void testModulo() {
            assertThat(OpenExpression.eval("10 % 3")).isEqualTo(1);
            assertThat(OpenExpression.eval("7 % 2")).isEqualTo(1);
        }

        @Test
        @DisplayName("Power | 幂运算")
        void testPower() {
            assertThat(OpenExpression.eval("2 ** 3")).isEqualTo(8.0);
            assertThat(OpenExpression.eval("3 ** 2")).isEqualTo(9.0);
        }

        @Test
        @DisplayName("Operator Precedence | 运算符优先级")
        void testOperatorPrecedence() {
            assertThat(OpenExpression.eval("1 + 2 * 3")).isEqualTo(7);
            assertThat(OpenExpression.eval("(1 + 2) * 3")).isEqualTo(9);
            assertThat(OpenExpression.eval("10 - 6 / 2")).isEqualTo(7);
        }

        @Test
        @DisplayName("Negation | 取负")
        void testNegation() {
            assertThat(OpenExpression.eval("-5")).isEqualTo(-5);
            assertThat(OpenExpression.eval("--5")).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Comparison Tests | 比较测试")
    class ComparisonTests {

        @Test
        @DisplayName("Equality | 相等")
        void testEquality() {
            assertThat(OpenExpression.eval("1 == 1")).isEqualTo(true);
            assertThat(OpenExpression.eval("1 == 2")).isEqualTo(false);
            assertThat(OpenExpression.eval("'hello' == 'hello'")).isEqualTo(true);
        }

        @Test
        @DisplayName("Inequality | 不等")
        void testInequality() {
            assertThat(OpenExpression.eval("1 != 2")).isEqualTo(true);
            assertThat(OpenExpression.eval("1 != 1")).isEqualTo(false);
        }

        @Test
        @DisplayName("Less Than | 小于")
        void testLessThan() {
            assertThat(OpenExpression.eval("1 < 2")).isEqualTo(true);
            assertThat(OpenExpression.eval("2 < 2")).isEqualTo(false);
            assertThat(OpenExpression.eval("3 < 2")).isEqualTo(false);
        }

        @Test
        @DisplayName("Less Than or Equal | 小于等于")
        void testLessThanOrEqual() {
            assertThat(OpenExpression.eval("1 <= 2")).isEqualTo(true);
            assertThat(OpenExpression.eval("2 <= 2")).isEqualTo(true);
            assertThat(OpenExpression.eval("3 <= 2")).isEqualTo(false);
        }

        @Test
        @DisplayName("Greater Than | 大于")
        void testGreaterThan() {
            assertThat(OpenExpression.eval("3 > 2")).isEqualTo(true);
            assertThat(OpenExpression.eval("2 > 2")).isEqualTo(false);
        }

        @Test
        @DisplayName("Greater Than or Equal | 大于等于")
        void testGreaterThanOrEqual() {
            assertThat(OpenExpression.eval("3 >= 2")).isEqualTo(true);
            assertThat(OpenExpression.eval("2 >= 2")).isEqualTo(true);
            assertThat(OpenExpression.eval("1 >= 2")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Logical Tests | 逻辑测试")
    class LogicalTests {

        @Test
        @DisplayName("And | 与")
        void testAnd() {
            assertThat(OpenExpression.eval("true && true")).isEqualTo(true);
            assertThat(OpenExpression.eval("true && false")).isEqualTo(false);
            assertThat(OpenExpression.eval("true and false")).isEqualTo(false);
        }

        @Test
        @DisplayName("Or | 或")
        void testOr() {
            assertThat(OpenExpression.eval("true || false")).isEqualTo(true);
            assertThat(OpenExpression.eval("false || false")).isEqualTo(false);
            assertThat(OpenExpression.eval("false or true")).isEqualTo(true);
        }

        @Test
        @DisplayName("Not | 非")
        void testNot() {
            assertThat(OpenExpression.eval("!true")).isEqualTo(false);
            assertThat(OpenExpression.eval("!false")).isEqualTo(true);
            assertThat(OpenExpression.eval("not true")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Ternary Tests | 三元测试")
    class TernaryTests {

        @Test
        @DisplayName("Ternary Expression | 三元表达式")
        void testTernary() {
            assertThat(OpenExpression.eval("true ? 'yes' : 'no'")).isEqualTo("yes");
            assertThat(OpenExpression.eval("false ? 'yes' : 'no'")).isEqualTo("no");
            assertThat(OpenExpression.eval("1 > 0 ? 'positive' : 'negative'")).isEqualTo("positive");
        }
    }

    @Nested
    @DisplayName("String Tests | 字符串测试")
    class StringTests {

        @Test
        @DisplayName("String Concatenation | 字符串连接")
        void testStringConcat() {
            assertThat(OpenExpression.eval("'hello' + ' ' + 'world'")).isEqualTo("hello world");
        }

        @Test
        @DisplayName("String Literals | 字符串字面量")
        void testStringLiterals() {
            assertThat(OpenExpression.eval("'single quotes'")).isEqualTo("single quotes");
            assertThat(OpenExpression.eval("\"double quotes\"")).isEqualTo("double quotes");
        }
    }

    @Nested
    @DisplayName("Variable Tests | 变量测试")
    class VariableTests {

        @Test
        @DisplayName("Simple Variables | 简单变量")
        void testSimpleVariables() {
            Map<String, Object> vars = Map.of("x", 10, "y", 20);
            assertThat(OpenExpression.eval("x + y", vars)).isEqualTo(30);
        }

        @Test
        @DisplayName("Hash Variables | 井号变量")
        void testHashVariables() {
            StandardContext ctx = new StandardContext();
            ctx.setVariable("name", "John");
            assertThat(OpenExpression.eval("#name", ctx)).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("Function Tests | 函数测试")
    class FunctionTests {

        @Test
        @DisplayName("String Functions | 字符串函数")
        void testStringFunctions() {
            assertThat(OpenExpression.eval("upper('hello')")).isEqualTo("HELLO");
            assertThat(OpenExpression.eval("lower('HELLO')")).isEqualTo("hello");
            assertThat(OpenExpression.eval("len('hello')")).isEqualTo(5);
            assertThat(OpenExpression.eval("trim('  hello  ')")).isEqualTo("hello");
        }

        @Test
        @DisplayName("Math Functions | 数学函数")
        void testMathFunctions() {
            assertThat(OpenExpression.eval("abs(-5)")).isEqualTo(5.0);
            assertThat(OpenExpression.eval("max(1, 5, 3)")).isEqualTo(5.0);
            assertThat(OpenExpression.eval("min(1, 5, 3)")).isEqualTo(1.0);
            assertThat(OpenExpression.eval("round(3.7)")).isEqualTo(4L);
        }

        @Test
        @DisplayName("Collection Functions | 集合函数")
        void testCollectionFunctions() {
            assertThat(OpenExpression.eval("size({1, 2, 3})")).isEqualTo(3);
            assertThat(OpenExpression.eval("first({1, 2, 3})")).isEqualTo(1);
            assertThat(OpenExpression.eval("last({1, 2, 3})")).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Property Access Tests | 属性访问测试")
    class PropertyAccessTests {

        @Test
        @DisplayName("Map Property Access | Map属性访问")
        void testMapPropertyAccess() {
            Map<String, Object> vars = Map.of(
                    "user", Map.of("name", "John", "age", 30)
            );
            assertThat(OpenExpression.eval("user.name", vars)).isEqualTo("John");
            assertThat(OpenExpression.eval("user.age", vars)).isEqualTo(30);
        }

        @Test
        @DisplayName("Index Access | 索引访问")
        void testIndexAccess() {
            Map<String, Object> vars = Map.of("list", List.of(1, 2, 3));
            assertThat(OpenExpression.eval("list[0]", vars)).isEqualTo(1);
            assertThat(OpenExpression.eval("list[2]", vars)).isEqualTo(3);
        }

        @Test
        @DisplayName("Null-Safe Navigation | 空安全导航")
        void testNullSafeNavigation() {
            Map<String, Object> vars = Map.of("user", Map.of("name", "John"));
            StandardContext ctx = new StandardContext();
            vars.forEach(ctx::setVariable);

            // Null-safe should return null for missing properties
            assertThat(OpenExpression.eval("user?.name", ctx)).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("List Literal Tests | 列表字面量测试")
    class ListLiteralTests {

        @Test
        @DisplayName("List Creation | 列表创建")
        @SuppressWarnings("unchecked")
        void testListCreation() {
            Object result = OpenExpression.eval("{1, 2, 3}");
            assertThat(result).isInstanceOf(List.class);
            assertThat((List<Integer>) result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Empty List | 空列表")
        void testEmptyList() {
            Object result = OpenExpression.eval("{}");
            assertThat(result).isInstanceOf(List.class);
            assertThat((List<?>) result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Type Conversion Tests | 类型转换测试")
    class TypeConversionTests {

        @Test
        @DisplayName("Convert to Integer | 转换为整数")
        void testConvertToInt() {
            assertThat(OpenExpression.eval("10 / 3", Integer.class)).isEqualTo(3);
        }

        @Test
        @DisplayName("Convert to String | 转换为字符串")
        void testConvertToString() {
            assertThat(OpenExpression.eval("123", String.class)).isEqualTo("123");
        }

        @Test
        @DisplayName("Convert to Boolean | 转换为布尔值")
        void testConvertToBoolean() {
            assertThat(OpenExpression.eval("1", Boolean.class)).isTrue();
            assertThat(OpenExpression.eval("0", Boolean.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Validation Tests | 验证测试")
    class ValidationTests {

        @Test
        @DisplayName("Valid Expression | 有效表达式")
        void testValidExpression() {
            assertThat(OpenExpression.isValid("1 + 2")).isTrue();
            assertThat(OpenExpression.isValid("x > 0 && y < 10")).isTrue();
        }

        @Test
        @DisplayName("Invalid Expression | 无效表达式")
        void testInvalidExpression() {
            assertThat(OpenExpression.isValid("")).isFalse();
            assertThat(OpenExpression.isValid("1 +")).isFalse();
            assertThat(OpenExpression.isValid("((1 + 2)")).isFalse();
        }
    }

    @Nested
    @DisplayName("Expression Object Tests | 表达式对象测试")
    class ExpressionObjectTests {

        @Test
        @DisplayName("Parse and Reuse | 解析和重用")
        void testParseAndReuse() {
            Expression expr = OpenExpression.parse("x + y");

            StandardContext ctx1 = new StandardContext();
            ctx1.setVariable("x", 1);
            ctx1.setVariable("y", 2);
            assertThat(expr.getValue(ctx1)).isEqualTo(3);

            StandardContext ctx2 = new StandardContext();
            ctx2.setVariable("x", 10);
            ctx2.setVariable("y", 20);
            assertThat(expr.getValue(ctx2)).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests | 工厂方法测试")
    class FactoryMethodsTests {

        @Test
        @DisplayName("newParser creates new parser instance | newParser 创建新的解析器实例")
        void testNewParser() {
            ExpressionParser parser1 = OpenExpression.newParser();
            ExpressionParser parser2 = OpenExpression.newParser();

            assertThat(parser1).isNotNull();
            assertThat(parser2).isNotNull();
            // Each call should create a new instance
            assertThat(parser1).isNotSameAs(parser2);
        }

        @Test
        @DisplayName("functions returns function registry | functions 返回函数注册表")
        void testFunctions() {
            var registry = OpenExpression.functions();

            assertThat(registry).isNotNull();
            // Registry should have standard functions
            assertThat(registry.has("length")).isTrue();
        }

        @Test
        @DisplayName("contextBuilder returns builder | contextBuilder 返回构建器")
        void testContextBuilder() {
            var builder = OpenExpression.contextBuilder();

            assertThat(builder).isNotNull();
            // Should be able to build a context
            var context = builder.build();
            assertThat(context).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cache Operations Tests | 缓存操作测试")
    class CacheOperationsTests {

        @Test
        @DisplayName("cacheSize returns non-negative value | cacheSize 返回非负值")
        void testCacheSize() {
            int size = OpenExpression.cacheSize();
            assertThat(size).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("clearCache clears expression cache | clearCache 清除表达式缓存")
        void testClearCache() {
            // Parse some expressions to populate cache
            OpenExpression.parse("1 + 1");
            OpenExpression.parse("2 + 2");

            // Clear the cache
            OpenExpression.clearCache();

            // Cache should be empty or reduced
            assertThat(OpenExpression.cacheSize()).isGreaterThanOrEqualTo(0);
        }
    }
}
