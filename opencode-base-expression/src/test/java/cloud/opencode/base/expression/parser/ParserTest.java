package cloud.opencode.base.expression.parser;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.ast.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Parser Tests
 * Parser 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("Parser Tests | Parser 测试")
class ParserTest {

    @Nested
    @DisplayName("Constructor Tests | 构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("Create parser with tokens | 使用词法单元创建解析器")
        void testCreateWithTokens() {
            List<Token> tokens = Tokenizer.tokenize("1 + 2");
            Parser parser = new Parser(tokens);
            Node node = parser.parse();
            assertThat(node).isNotNull();
        }
    }

    @Nested
    @DisplayName("Static Parse Tests | 静态 parse 测试")
    class StaticParseTests {

        @Test
        @DisplayName("parse static method | parse 静态方法")
        void testStaticParse() {
            Node node = Parser.parse("1 + 2");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
        }
    }

    @Nested
    @DisplayName("Literal Parsing Tests | 字面量解析测试")
    class LiteralParsingTests {

        @Test
        @DisplayName("Parse integer literal | 解析整数字面量")
        void testParseIntegerLiteral() {
            Node node = Parser.parse("42");
            assertThat(node).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) node).value()).isEqualTo(42);
        }

        @Test
        @DisplayName("Parse double literal | 解析双精度字面量")
        void testParseDoubleLiteral() {
            Node node = Parser.parse("3.14");
            assertThat(node).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) node).value()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("Parse string literal | 解析字符串字面量")
        void testParseStringLiteral() {
            Node node = Parser.parse("'hello'");
            assertThat(node).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) node).value()).isEqualTo("hello");
        }

        @Test
        @DisplayName("Parse true literal | 解析 true 字面量")
        void testParseTrueLiteral() {
            Node node = Parser.parse("true");
            assertThat(node).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) node).value()).isEqualTo(true);
        }

        @Test
        @DisplayName("Parse false literal | 解析 false 字面量")
        void testParseFalseLiteral() {
            Node node = Parser.parse("false");
            assertThat(node).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) node).value()).isEqualTo(false);
        }

        @Test
        @DisplayName("Parse null literal | 解析 null 字面量")
        void testParseNullLiteral() {
            Node node = Parser.parse("null");
            assertThat(node).isInstanceOf(LiteralNode.class);
            assertThat(((LiteralNode) node).value()).isNull();
        }
    }

    @Nested
    @DisplayName("Identifier Parsing Tests | 标识符解析测试")
    class IdentifierParsingTests {

        @Test
        @DisplayName("Parse simple identifier | 解析简单标识符")
        void testParseSimpleIdentifier() {
            Node node = Parser.parse("foo");
            assertThat(node).isInstanceOf(IdentifierNode.class);
            assertThat(((IdentifierNode) node).name()).isEqualTo("foo");
        }

        @Test
        @DisplayName("Parse variable with hash | 解析带井号的变量")
        void testParseVariableWithHash() {
            Node node = Parser.parse("#myVar");
            assertThat(node).isInstanceOf(IdentifierNode.class);
            assertThat(((IdentifierNode) node).name()).isEqualTo("#myVar");
        }

        @Test
        @DisplayName("Parse special variables | 解析特殊变量")
        void testParseSpecialVariables() {
            assertThat(((IdentifierNode) Parser.parse("#root")).name()).isEqualTo("#root");
            assertThat(((IdentifierNode) Parser.parse("#this")).name()).isEqualTo("#this");
        }
    }

    @Nested
    @DisplayName("Arithmetic Operation Parsing Tests | 算术运算解析测试")
    class ArithmeticOperationParsingTests {

        @Test
        @DisplayName("Parse addition | 解析加法")
        void testParseAddition() {
            Node node = Parser.parse("1 + 2");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode bin = (BinaryOpNode) node;
            assertThat(bin.operator()).isEqualTo("+");
        }

        @Test
        @DisplayName("Parse subtraction | 解析减法")
        void testParseSubtraction() {
            Node node = Parser.parse("5 - 3");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode bin = (BinaryOpNode) node;
            assertThat(bin.operator()).isEqualTo("-");
        }

        @Test
        @DisplayName("Parse multiplication | 解析乘法")
        void testParseMultiplication() {
            Node node = Parser.parse("2 * 3");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode bin = (BinaryOpNode) node;
            assertThat(bin.operator()).isEqualTo("*");
        }

        @Test
        @DisplayName("Parse division | 解析除法")
        void testParseDivision() {
            Node node = Parser.parse("10 / 2");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode bin = (BinaryOpNode) node;
            assertThat(bin.operator()).isEqualTo("/");
        }

        @Test
        @DisplayName("Parse modulo | 解析取模")
        void testParseModulo() {
            Node node = Parser.parse("10 % 3");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode bin = (BinaryOpNode) node;
            assertThat(bin.operator()).isEqualTo("%");
        }

        @Test
        @DisplayName("Parse power | 解析幂运算")
        void testParsePower() {
            Node node = Parser.parse("2 ** 3");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode bin = (BinaryOpNode) node;
            assertThat(bin.operator()).isEqualTo("**");
        }
    }

    @Nested
    @DisplayName("Comparison Operation Parsing Tests | 比较运算解析测试")
    class ComparisonOperationParsingTests {

        @Test
        @DisplayName("Parse less than | 解析小于")
        void testParseLessThan() {
            Node node = Parser.parse("1 < 2");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) node).operator()).isEqualTo("<");
        }

        @Test
        @DisplayName("Parse less than or equal | 解析小于等于")
        void testParseLessThanOrEqual() {
            Node node = Parser.parse("1 <= 2");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) node).operator()).isEqualTo("<=");
        }

        @Test
        @DisplayName("Parse greater than | 解析大于")
        void testParseGreaterThan() {
            Node node = Parser.parse("2 > 1");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) node).operator()).isEqualTo(">");
        }

        @Test
        @DisplayName("Parse greater than or equal | 解析大于等于")
        void testParseGreaterThanOrEqual() {
            Node node = Parser.parse("2 >= 1");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) node).operator()).isEqualTo(">=");
        }

        @Test
        @DisplayName("Parse equal | 解析相等")
        void testParseEqual() {
            Node node = Parser.parse("1 == 1");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) node).operator()).isEqualTo("==");
        }

        @Test
        @DisplayName("Parse not equal | 解析不相等")
        void testParseNotEqual() {
            Node node = Parser.parse("1 != 2");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) node).operator()).isEqualTo("!=");
        }

        @Test
        @DisplayName("Parse matches | 解析正则匹配")
        void testParseMatches() {
            Node node = Parser.parse("'test' matches '^t.*'");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) node).operator()).isEqualTo("matches");
        }
    }

    @Nested
    @DisplayName("Logical Operation Parsing Tests | 逻辑运算解析测试")
    class LogicalOperationParsingTests {

        @Test
        @DisplayName("Parse and with && | 解析 && 与运算")
        void testParseAndWithSymbol() {
            Node node = Parser.parse("true && false");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) node).operator()).isEqualTo("&&");
        }

        @Test
        @DisplayName("Parse and with keyword | 解析 and 关键字")
        void testParseAndWithKeyword() {
            Node node = Parser.parse("true and false");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) node).operator()).isEqualTo("&&");
        }

        @Test
        @DisplayName("Parse or with || | 解析 || 或运算")
        void testParseOrWithSymbol() {
            Node node = Parser.parse("true || false");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) node).operator()).isEqualTo("||");
        }

        @Test
        @DisplayName("Parse or with keyword | 解析 or 关键字")
        void testParseOrWithKeyword() {
            Node node = Parser.parse("true or false");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) node).operator()).isEqualTo("||");
        }

        @Test
        @DisplayName("Parse not with ! | 解析 ! 非运算")
        void testParseNotWithSymbol() {
            Node node = Parser.parse("!true");
            assertThat(node).isInstanceOf(UnaryOpNode.class);
            assertThat(((UnaryOpNode) node).operator()).isEqualTo("!");
        }

        @Test
        @DisplayName("Parse not with keyword | 解析 not 关键字")
        void testParseNotWithKeyword() {
            Node node = Parser.parse("not true");
            assertThat(node).isInstanceOf(UnaryOpNode.class);
            assertThat(((UnaryOpNode) node).operator()).isEqualTo("!");
        }
    }

    @Nested
    @DisplayName("Unary Operation Parsing Tests | 一元运算解析测试")
    class UnaryOperationParsingTests {

        @Test
        @DisplayName("Parse unary minus | 解析一元负号")
        void testParseUnaryMinus() {
            Node node = Parser.parse("-5");
            assertThat(node).isInstanceOf(UnaryOpNode.class);
            UnaryOpNode unary = (UnaryOpNode) node;
            assertThat(unary.operator()).isEqualTo("-");
            assertThat(unary.operand()).isInstanceOf(LiteralNode.class);
        }

        @Test
        @DisplayName("Parse nested unary | 解析嵌套一元运算")
        void testParseNestedUnary() {
            Node node = Parser.parse("--5");
            assertThat(node).isInstanceOf(UnaryOpNode.class);
            UnaryOpNode outer = (UnaryOpNode) node;
            assertThat(outer.operand()).isInstanceOf(UnaryOpNode.class);
        }

        @Test
        @DisplayName("Parse not with not | 解析双重否定")
        void testParseDoubleNot() {
            Node node = Parser.parse("!!true");
            assertThat(node).isInstanceOf(UnaryOpNode.class);
            UnaryOpNode outer = (UnaryOpNode) node;
            assertThat(outer.operand()).isInstanceOf(UnaryOpNode.class);
        }
    }

    @Nested
    @DisplayName("Ternary Operation Parsing Tests | 三元运算解析测试")
    class TernaryOperationParsingTests {

        @Test
        @DisplayName("Parse ternary expression | 解析三元表达式")
        void testParseTernary() {
            Node node = Parser.parse("true ? 1 : 2");
            assertThat(node).isInstanceOf(TernaryOpNode.class);
            TernaryOpNode ternary = (TernaryOpNode) node;
            assertThat(ternary.condition()).isInstanceOf(LiteralNode.class);
            assertThat(ternary.trueValue()).isInstanceOf(LiteralNode.class);
            assertThat(ternary.falseValue()).isInstanceOf(LiteralNode.class);
        }

        @Test
        @DisplayName("Parse nested ternary | 解析嵌套三元表达式")
        void testParseNestedTernary() {
            Node node = Parser.parse("a ? b ? c : d : e");
            assertThat(node).isInstanceOf(TernaryOpNode.class);
        }
    }

    @Nested
    @DisplayName("Property Access Parsing Tests | 属性访问解析测试")
    class PropertyAccessParsingTests {

        @Test
        @DisplayName("Parse property access | 解析属性访问")
        void testParsePropertyAccess() {
            Node node = Parser.parse("obj.property");
            assertThat(node).isInstanceOf(PropertyAccessNode.class);
            PropertyAccessNode prop = (PropertyAccessNode) node;
            assertThat(prop.property()).isEqualTo("property");
            assertThat(prop.nullSafe()).isFalse();
        }

        @Test
        @DisplayName("Parse safe property access | 解析安全属性访问")
        void testParseSafePropertyAccess() {
            Node node = Parser.parse("obj?.property");
            assertThat(node).isInstanceOf(PropertyAccessNode.class);
            PropertyAccessNode prop = (PropertyAccessNode) node;
            assertThat(prop.property()).isEqualTo("property");
            assertThat(prop.nullSafe()).isTrue();
        }

        @Test
        @DisplayName("Parse chained property access | 解析链式属性访问")
        void testParseChainedPropertyAccess() {
            Node node = Parser.parse("a.b.c");
            assertThat(node).isInstanceOf(PropertyAccessNode.class);
            PropertyAccessNode prop = (PropertyAccessNode) node;
            assertThat(prop.property()).isEqualTo("c");
            assertThat(prop.target()).isInstanceOf(PropertyAccessNode.class);
        }
    }

    @Nested
    @DisplayName("Index Access Parsing Tests | 索引访问解析测试")
    class IndexAccessParsingTests {

        @Test
        @DisplayName("Parse index access | 解析索引访问")
        void testParseIndexAccess() {
            Node node = Parser.parse("list[0]");
            assertThat(node).isInstanceOf(IndexAccessNode.class);
            IndexAccessNode idx = (IndexAccessNode) node;
            assertThat(idx.target()).isInstanceOf(IdentifierNode.class);
            assertThat(idx.index()).isInstanceOf(LiteralNode.class);
        }

        @Test
        @DisplayName("Parse nested index access | 解析嵌套索引访问")
        void testParseNestedIndexAccess() {
            Node node = Parser.parse("list[0][1]");
            assertThat(node).isInstanceOf(IndexAccessNode.class);
            IndexAccessNode idx = (IndexAccessNode) node;
            assertThat(idx.target()).isInstanceOf(IndexAccessNode.class);
        }

        @Test
        @DisplayName("Parse index with expression | 解析表达式索引")
        void testParseIndexWithExpression() {
            Node node = Parser.parse("list[i + 1]");
            assertThat(node).isInstanceOf(IndexAccessNode.class);
            IndexAccessNode idx = (IndexAccessNode) node;
            assertThat(idx.index()).isInstanceOf(BinaryOpNode.class);
        }
    }

    @Nested
    @DisplayName("Method Call Parsing Tests | 方法调用解析测试")
    class MethodCallParsingTests {

        @Test
        @DisplayName("Parse method call no args | 解析无参方法调用")
        void testParseMethodCallNoArgs() {
            Node node = Parser.parse("str.toUpperCase()");
            assertThat(node).isInstanceOf(MethodCallNode.class);
            MethodCallNode method = (MethodCallNode) node;
            assertThat(method.methodName()).isEqualTo("toUpperCase");
            assertThat(method.arguments()).isEmpty();
            assertThat(method.nullSafe()).isFalse();
        }

        @Test
        @DisplayName("Parse method call with args | 解析带参方法调用")
        void testParseMethodCallWithArgs() {
            Node node = Parser.parse("str.substring(0, 5)");
            assertThat(node).isInstanceOf(MethodCallNode.class);
            MethodCallNode method = (MethodCallNode) node;
            assertThat(method.methodName()).isEqualTo("substring");
            assertThat(method.arguments()).hasSize(2);
        }

        @Test
        @DisplayName("Parse safe method call | 解析安全方法调用")
        void testParseSafeMethodCall() {
            Node node = Parser.parse("str?.toUpperCase()");
            assertThat(node).isInstanceOf(MethodCallNode.class);
            MethodCallNode method = (MethodCallNode) node;
            assertThat(method.nullSafe()).isTrue();
        }

        @Test
        @DisplayName("Parse chained method calls | 解析链式方法调用")
        void testParseChainedMethodCalls() {
            Node node = Parser.parse("str.trim().toUpperCase()");
            assertThat(node).isInstanceOf(MethodCallNode.class);
            MethodCallNode outer = (MethodCallNode) node;
            assertThat(outer.methodName()).isEqualTo("toUpperCase");
            assertThat(outer.target()).isInstanceOf(MethodCallNode.class);
        }
    }

    @Nested
    @DisplayName("Function Call Parsing Tests | 函数调用解析测试")
    class FunctionCallParsingTests {

        @Test
        @DisplayName("Parse function call no args | 解析无参函数调用")
        void testParseFunctionCallNoArgs() {
            Node node = Parser.parse("now()");
            assertThat(node).isInstanceOf(FunctionCallNode.class);
            FunctionCallNode func = (FunctionCallNode) node;
            assertThat(func.functionName()).isEqualTo("now");
            assertThat(func.arguments()).isEmpty();
        }

        @Test
        @DisplayName("Parse function call with args | 解析带参函数调用")
        void testParseFunctionCallWithArgs() {
            Node node = Parser.parse("upper('hello')");
            assertThat(node).isInstanceOf(FunctionCallNode.class);
            FunctionCallNode func = (FunctionCallNode) node;
            assertThat(func.functionName()).isEqualTo("upper");
            assertThat(func.arguments()).hasSize(1);
        }

        @Test
        @DisplayName("Parse function call with multiple args | 解析多参函数调用")
        void testParseFunctionCallWithMultipleArgs() {
            Node node = Parser.parse("substring('hello', 0, 3)");
            assertThat(node).isInstanceOf(FunctionCallNode.class);
            FunctionCallNode func = (FunctionCallNode) node;
            assertThat(func.arguments()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("List Literal Parsing Tests | 列表字面量解析测试")
    class ListLiteralParsingTests {

        @Test
        @DisplayName("Parse empty list | 解析空列表")
        void testParseEmptyList() {
            Node node = Parser.parse("{}");
            assertThat(node).isInstanceOf(ListLiteralNode.class);
            assertThat(((ListLiteralNode) node).elements()).isEmpty();
        }

        @Test
        @DisplayName("Parse list with elements | 解析有元素的列表")
        void testParseListWithElements() {
            Node node = Parser.parse("{1, 2, 3}");
            assertThat(node).isInstanceOf(ListLiteralNode.class);
            assertThat(((ListLiteralNode) node).elements()).hasSize(3);
        }

        @Test
        @DisplayName("Parse list with expressions | 解析带表达式的列表")
        void testParseListWithExpressions() {
            Node node = Parser.parse("{1 + 1, 2 * 2, 3}");
            assertThat(node).isInstanceOf(ListLiteralNode.class);
            ListLiteralNode list = (ListLiteralNode) node;
            assertThat(list.elements().get(0)).isInstanceOf(BinaryOpNode.class);
            assertThat(list.elements().get(1)).isInstanceOf(BinaryOpNode.class);
        }
    }

    @Nested
    @DisplayName("Collection Filter Parsing Tests | 集合过滤解析测试")
    class CollectionFilterParsingTests {

        @Test
        @DisplayName("Parse collection filter | 解析集合过滤")
        void testParseCollectionFilter() {
            Node node = Parser.parse("list.?[#this > 5]");
            assertThat(node).isInstanceOf(CollectionFilterNode.class);
            CollectionFilterNode filter = (CollectionFilterNode) node;
            assertThat(filter.mode()).isEqualTo(CollectionFilterNode.FilterMode.ALL);
        }
    }

    @Nested
    @DisplayName("Collection Project Parsing Tests | 集合投影解析测试")
    class CollectionProjectParsingTests {

        @Test
        @DisplayName("Parse collection projection | 解析集合投影")
        void testParseCollectionProjection() {
            Node node = Parser.parse("list.![#this * 2]");
            assertThat(node).isInstanceOf(CollectionProjectNode.class);
        }
    }

    @Nested
    @DisplayName("Parenthesized Expression Parsing Tests | 括号表达式解析测试")
    class ParenthesizedExpressionParsingTests {

        @Test
        @DisplayName("Parse parenthesized expression | 解析括号表达式")
        void testParseParenthesized() {
            Node node = Parser.parse("(1 + 2)");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
        }

        @Test
        @DisplayName("Parse nested parentheses | 解析嵌套括号")
        void testParseNestedParentheses() {
            Node node = Parser.parse("((1 + 2))");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
        }

        @Test
        @DisplayName("Parentheses change precedence | 括号改变优先级")
        void testParenthesesChangePrecedence() {
            Node node = Parser.parse("(1 + 2) * 3");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode bin = (BinaryOpNode) node;
            assertThat(bin.operator()).isEqualTo("*");
            assertThat(bin.left()).isInstanceOf(BinaryOpNode.class);
        }
    }

    @Nested
    @DisplayName("Operator Precedence Tests | 运算符优先级测试")
    class OperatorPrecedenceTests {

        @Test
        @DisplayName("Multiplication before addition | 乘法优先于加法")
        void testMultiplicationBeforeAddition() {
            Node node = Parser.parse("1 + 2 * 3");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode bin = (BinaryOpNode) node;
            assertThat(bin.operator()).isEqualTo("+");
            assertThat(bin.right()).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) bin.right()).operator()).isEqualTo("*");
        }

        @Test
        @DisplayName("Power before multiplication | 幂运算优先于乘法")
        void testPowerBeforeMultiplication() {
            Node node = Parser.parse("2 * 3 ** 2");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode bin = (BinaryOpNode) node;
            assertThat(bin.operator()).isEqualTo("*");
            assertThat(bin.right()).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) bin.right()).operator()).isEqualTo("**");
        }

        @Test
        @DisplayName("Comparison before logical | 比较优先于逻辑运算")
        void testComparisonBeforeLogical() {
            Node node = Parser.parse("1 < 2 && 3 > 2");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode bin = (BinaryOpNode) node;
            assertThat(bin.operator()).isEqualTo("&&");
        }

        @Test
        @DisplayName("And before or | And 优先于 Or")
        void testAndBeforeOr() {
            Node node = Parser.parse("a || b && c");
            assertThat(node).isInstanceOf(BinaryOpNode.class);
            BinaryOpNode bin = (BinaryOpNode) node;
            assertThat(bin.operator()).isEqualTo("||");
            assertThat(bin.right()).isInstanceOf(BinaryOpNode.class);
            assertThat(((BinaryOpNode) bin.right()).operator()).isEqualTo("&&");
        }

        @Test
        @DisplayName("Ternary lowest precedence | 三元运算最低优先级")
        void testTernaryLowestPrecedence() {
            Node node = Parser.parse("a + b ? c : d");
            assertThat(node).isInstanceOf(TernaryOpNode.class);
            TernaryOpNode ternary = (TernaryOpNode) node;
            assertThat(ternary.condition()).isInstanceOf(BinaryOpNode.class);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests | 错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Unexpected token throws | 意外词法单元抛出异常")
        void testUnexpectedToken() {
            assertThatThrownBy(() -> Parser.parse("1 + + 2"))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("Missing closing paren throws | 缺少右括号抛出异常")
        void testMissingClosingParen() {
            assertThatThrownBy(() -> Parser.parse("(1 + 2"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("')'");
        }

        @Test
        @DisplayName("Missing closing bracket throws | 缺少右方括号抛出异常")
        void testMissingClosingBracket() {
            assertThatThrownBy(() -> Parser.parse("list[0"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("']'");
        }

        @Test
        @DisplayName("Missing closing brace throws | 缺少右花括号抛出异常")
        void testMissingClosingBrace() {
            assertThatThrownBy(() -> Parser.parse("{1, 2"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("'}'");
        }

        @Test
        @DisplayName("Missing colon in ternary throws | 三元表达式缺少冒号抛出异常")
        void testMissingColonInTernary() {
            assertThatThrownBy(() -> Parser.parse("true ? 1"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("':'");
        }

        @Test
        @DisplayName("Missing property name throws | 缺少属性名抛出异常")
        void testMissingPropertyName() {
            assertThatThrownBy(() -> Parser.parse("obj."))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("Missing variable name after hash throws | 井号后缺少变量名抛出异常")
        void testMissingVariableNameAfterHash() {
            assertThatThrownBy(() -> Parser.parse("#"))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("Extra tokens at end throws | 结尾额外词法单元抛出异常")
        void testExtraTokensAtEnd() {
            assertThatThrownBy(() -> Parser.parse("1 2"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("Unexpected token");
        }
    }

    @Nested
    @DisplayName("Complex Expression Tests | 复杂表达式测试")
    class ComplexExpressionTests {

        @Test
        @DisplayName("Parse complex arithmetic | 解析复杂算术表达式")
        void testParseComplexArithmetic() {
            Node node = Parser.parse("(1 + 2) * (3 - 4) / 5 + 6 ** 2");
            assertThat(node).isNotNull();
        }

        @Test
        @DisplayName("Parse method chain with filter | 解析带过滤的方法链")
        void testParseMethodChainWithFilter() {
            Node node = Parser.parse("users.?[age > 18].![name]");
            assertThat(node).isInstanceOf(CollectionProjectNode.class);
        }

        @Test
        @DisplayName("Parse conditional property access | 解析条件属性访问")
        void testParseConditionalPropertyAccess() {
            Node node = Parser.parse("(isAdmin ? admin : user).permissions");
            assertThat(node).isInstanceOf(PropertyAccessNode.class);
        }

        @Test
        @DisplayName("Parse function in ternary | 解析三元表达式中的函数")
        void testParseFunctionInTernary() {
            Node node = Parser.parse("isEmpty(str) ? 'default' : upper(str)");
            assertThat(node).isInstanceOf(TernaryOpNode.class);
        }
    }
}
