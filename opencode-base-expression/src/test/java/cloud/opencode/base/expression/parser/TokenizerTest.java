package cloud.opencode.base.expression.parser;

import cloud.opencode.base.expression.OpenExpressionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tokenizer Tests
 * Tokenizer 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("Tokenizer Tests | Tokenizer 测试")
class TokenizerTest {

    @Nested
    @DisplayName("Constructor Tests | 构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("Create tokenizer with expression | 使用表达式创建词法分析器")
        void testCreateWithExpression() {
            Tokenizer tokenizer = new Tokenizer("1 + 2");
            List<Token> tokens = tokenizer.tokenize();
            assertThat(tokens).isNotEmpty();
        }

        @Test
        @DisplayName("Create tokenizer with null | 使用 null 创建词法分析器")
        void testCreateWithNull() {
            Tokenizer tokenizer = new Tokenizer(null);
            List<Token> tokens = tokenizer.tokenize();
            assertThat(tokens).hasSize(1);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.EOF);
        }

        @Test
        @DisplayName("Create tokenizer with empty string | 使用空字符串创建词法分析器")
        void testCreateWithEmpty() {
            Tokenizer tokenizer = new Tokenizer("");
            List<Token> tokens = tokenizer.tokenize();
            assertThat(tokens).hasSize(1);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.EOF);
        }
    }

    @Nested
    @DisplayName("Static Tokenize Tests | 静态 tokenize 测试")
    class StaticTokenizeTests {

        @Test
        @DisplayName("tokenize static method | tokenize 静态方法")
        void testStaticTokenize() {
            List<Token> tokens = Tokenizer.tokenize("1 + 2");
            assertThat(tokens).isNotEmpty();
            assertThat(tokens.getLast().type()).isEqualTo(TokenType.EOF);
        }
    }

    @Nested
    @DisplayName("Number Tokenization Tests | 数字词法分析测试")
    class NumberTokenizationTests {

        @Test
        @DisplayName("Tokenize integer | 分析整数")
        void testTokenizeInteger() {
            List<Token> tokens = Tokenizer.tokenize("42");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.getFirst().value()).isEqualTo(42);
        }

        @Test
        @DisplayName("Tokenize large integer as long | 分析大整数为长整数")
        void testTokenizeLargeInteger() {
            List<Token> tokens = Tokenizer.tokenize("9999999999");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.getFirst().value()).isInstanceOf(Long.class);
        }

        @Test
        @DisplayName("Tokenize double | 分析双精度数")
        void testTokenizeDouble() {
            List<Token> tokens = Tokenizer.tokenize("3.14");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.getFirst().value()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("Tokenize double starting with dot | 分析以点开头的双精度数")
        void testTokenizeDoubleStartingWithDot() {
            List<Token> tokens = Tokenizer.tokenize(".5");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.getFirst().value()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("Tokenize scientific notation | 分析科学记数法")
        void testTokenizeScientificNotation() {
            List<Token> tokens = Tokenizer.tokenize("1e10");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NUMBER);
            assertThat(((Number) tokens.getFirst().value()).doubleValue()).isEqualTo(1e10);
        }

        @Test
        @DisplayName("Tokenize scientific notation with sign | 分析带符号的科学记数法")
        void testTokenizeScientificNotationWithSign() {
            List<Token> tokens = Tokenizer.tokenize("1E+5");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NUMBER);

            List<Token> tokens2 = Tokenizer.tokenize("1e-3");
            assertThat(tokens2).hasSize(2);
            assertThat(tokens2.getFirst().type()).isEqualTo(TokenType.NUMBER);
        }

        @Test
        @DisplayName("Tokenize long suffix | 分析长整数后缀")
        void testTokenizeLongSuffix() {
            List<Token> tokens = Tokenizer.tokenize("100L");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.getFirst().value()).isEqualTo(100L);

            List<Token> tokens2 = Tokenizer.tokenize("100l");
            assertThat(tokens2.getFirst().value()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Tokenize double suffix | 分析双精度后缀")
        void testTokenizeDoubleSuffix() {
            List<Token> tokens = Tokenizer.tokenize("3.14D");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.getFirst().value()).isInstanceOf(Double.class);

            List<Token> tokens2 = Tokenizer.tokenize("3.14d");
            assertThat(tokens2.getFirst().value()).isInstanceOf(Double.class);
        }

        @Test
        @DisplayName("Tokenize float suffix | 分析浮点数后缀")
        void testTokenizeFloatSuffix() {
            List<Token> tokens = Tokenizer.tokenize("3.14F");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.getFirst().value()).isInstanceOf(Double.class);

            List<Token> tokens2 = Tokenizer.tokenize("3.14f");
            assertThat(tokens2.getFirst().value()).isInstanceOf(Double.class);
        }
    }

    @Nested
    @DisplayName("String Tokenization Tests | 字符串词法分析测试")
    class StringTokenizationTests {

        @Test
        @DisplayName("Tokenize single-quoted string | 分析单引号字符串")
        void testTokenizeSingleQuotedString() {
            List<Token> tokens = Tokenizer.tokenize("'hello'");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.STRING);
            assertThat(tokens.getFirst().value()).isEqualTo("hello");
        }

        @Test
        @DisplayName("Tokenize double-quoted string | 分析双引号字符串")
        void testTokenizeDoubleQuotedString() {
            List<Token> tokens = Tokenizer.tokenize("\"hello\"");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.STRING);
            assertThat(tokens.getFirst().value()).isEqualTo("hello");
        }

        @Test
        @DisplayName("Tokenize empty string | 分析空字符串")
        void testTokenizeEmptyString() {
            List<Token> tokens = Tokenizer.tokenize("''");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.STRING);
            assertThat(tokens.getFirst().value()).isEqualTo("");
        }

        @Test
        @DisplayName("Tokenize string with escape sequences | 分析带转义序列的字符串")
        void testTokenizeStringWithEscapes() {
            List<Token> tokens = Tokenizer.tokenize("'hello\\nworld'");
            assertThat(tokens.getFirst().value()).isEqualTo("hello\nworld");

            List<Token> tokens2 = Tokenizer.tokenize("'tab\\there'");
            assertThat(tokens2.getFirst().value()).isEqualTo("tab\there");

            List<Token> tokens3 = Tokenizer.tokenize("'return\\rhere'");
            assertThat(tokens3.getFirst().value()).isEqualTo("return\rhere");

            List<Token> tokens4 = Tokenizer.tokenize("'back\\\\slash'");
            assertThat(tokens4.getFirst().value()).isEqualTo("back\\slash");
        }

        @Test
        @DisplayName("Tokenize string with escaped quotes | 分析带转义引号的字符串")
        void testTokenizeStringWithEscapedQuotes() {
            List<Token> tokens = Tokenizer.tokenize("'it\\'s'");
            assertThat(tokens.getFirst().value()).isEqualTo("it's");

            List<Token> tokens2 = Tokenizer.tokenize("\"say \\\"hello\\\"\"");
            assertThat(tokens2.getFirst().value()).isEqualTo("say \"hello\"");
        }

        @Test
        @DisplayName("Tokenize string with unknown escape | 分析带未知转义的字符串")
        void testTokenizeStringWithUnknownEscape() {
            List<Token> tokens = Tokenizer.tokenize("'hello\\xworld'");
            assertThat(tokens.getFirst().value()).isEqualTo("helloxworld");
        }

        @Test
        @DisplayName("Unterminated string throws | 未终止字符串抛出异常")
        void testUnterminatedString() {
            assertThatThrownBy(() -> Tokenizer.tokenize("'hello"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("Unterminated string");
        }
    }

    @Nested
    @DisplayName("Identifier Tokenization Tests | 标识符词法分析测试")
    class IdentifierTokenizationTests {

        @Test
        @DisplayName("Tokenize simple identifier | 分析简单标识符")
        void testTokenizeSimpleIdentifier() {
            List<Token> tokens = Tokenizer.tokenize("foo");
            assertThat(tokens).hasSize(2);
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.getFirst().value()).isEqualTo("foo");
        }

        @Test
        @DisplayName("Tokenize identifier with underscore | 分析带下划线的标识符")
        void testTokenizeIdentifierWithUnderscore() {
            List<Token> tokens = Tokenizer.tokenize("_foo_bar");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.getFirst().value()).isEqualTo("_foo_bar");
        }

        @Test
        @DisplayName("Tokenize identifier with dollar | 分析带美元符的标识符")
        void testTokenizeIdentifierWithDollar() {
            List<Token> tokens = Tokenizer.tokenize("$var");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.getFirst().value()).isEqualTo("$var");
        }

        @Test
        @DisplayName("Tokenize identifier with numbers | 分析带数字的标识符")
        void testTokenizeIdentifierWithNumbers() {
            List<Token> tokens = Tokenizer.tokenize("var123");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.getFirst().value()).isEqualTo("var123");
        }
    }

    @Nested
    @DisplayName("Keyword Tokenization Tests | 关键字词法分析测试")
    class KeywordTokenizationTests {

        @Test
        @DisplayName("Tokenize true | 分析 true")
        void testTokenizeTrue() {
            List<Token> tokens = Tokenizer.tokenize("true");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.BOOLEAN_TRUE);
            assertThat(tokens.getFirst().value()).isEqualTo(true);
        }

        @Test
        @DisplayName("Tokenize TRUE (case insensitive) | 分析 TRUE（不区分大小写）")
        void testTokenizeTrueUppercase() {
            List<Token> tokens = Tokenizer.tokenize("TRUE");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.BOOLEAN_TRUE);
        }

        @Test
        @DisplayName("Tokenize false | 分析 false")
        void testTokenizeFalse() {
            List<Token> tokens = Tokenizer.tokenize("false");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.BOOLEAN_FALSE);
            assertThat(tokens.getFirst().value()).isEqualTo(false);
        }

        @Test
        @DisplayName("Tokenize null | 分析 null")
        void testTokenizeNull() {
            List<Token> tokens = Tokenizer.tokenize("null");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NULL);
        }

        @Test
        @DisplayName("Tokenize and | 分析 and")
        void testTokenizeAnd() {
            List<Token> tokens = Tokenizer.tokenize("and");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.AND);
        }

        @Test
        @DisplayName("Tokenize or | 分析 or")
        void testTokenizeOr() {
            List<Token> tokens = Tokenizer.tokenize("or");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.OR);
        }

        @Test
        @DisplayName("Tokenize not | 分析 not")
        void testTokenizeNot() {
            List<Token> tokens = Tokenizer.tokenize("not");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NOT);
        }

        @Test
        @DisplayName("Tokenize matches | 分析 matches")
        void testTokenizeMatches() {
            List<Token> tokens = Tokenizer.tokenize("matches");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.MATCHES);
        }

        @Test
        @DisplayName("Tokenize instanceof | 分析 instanceof")
        void testTokenizeInstanceof() {
            List<Token> tokens = Tokenizer.tokenize("instanceof");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.INSTANCEOF);
        }
    }

    @Nested
    @DisplayName("Operator Tokenization Tests | 运算符词法分析测试")
    class OperatorTokenizationTests {

        @Test
        @DisplayName("Tokenize arithmetic operators | 分析算术运算符")
        void testTokenizeArithmeticOperators() {
            assertThat(Tokenizer.tokenize("+").getFirst().type()).isEqualTo(TokenType.PLUS);
            assertThat(Tokenizer.tokenize("-").getFirst().type()).isEqualTo(TokenType.MINUS);
            assertThat(Tokenizer.tokenize("*").getFirst().type()).isEqualTo(TokenType.STAR);
            assertThat(Tokenizer.tokenize("/").getFirst().type()).isEqualTo(TokenType.SLASH);
            assertThat(Tokenizer.tokenize("%").getFirst().type()).isEqualTo(TokenType.PERCENT);
        }

        @Test
        @DisplayName("Tokenize power operator | 分析幂运算符")
        void testTokenizePowerOperator() {
            List<Token> tokens = Tokenizer.tokenize("**");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.POWER);
            assertThat(tokens.getFirst().length()).isEqualTo(2);
        }

        @Test
        @DisplayName("Tokenize comparison operators | 分析比较运算符")
        void testTokenizeComparisonOperators() {
            assertThat(Tokenizer.tokenize("<").getFirst().type()).isEqualTo(TokenType.LT);
            assertThat(Tokenizer.tokenize("<=").getFirst().type()).isEqualTo(TokenType.LE);
            assertThat(Tokenizer.tokenize(">").getFirst().type()).isEqualTo(TokenType.GT);
            assertThat(Tokenizer.tokenize(">=").getFirst().type()).isEqualTo(TokenType.GE);
        }

        @Test
        @DisplayName("Tokenize equality operators | 分析相等运算符")
        void testTokenizeEqualityOperators() {
            assertThat(Tokenizer.tokenize("==").getFirst().type()).isEqualTo(TokenType.EQ);
            assertThat(Tokenizer.tokenize("!=").getFirst().type()).isEqualTo(TokenType.NE);
            assertThat(Tokenizer.tokenize("=").getFirst().type()).isEqualTo(TokenType.ASSIGN);
        }

        @Test
        @DisplayName("Tokenize logical operators | 分析逻辑运算符")
        void testTokenizeLogicalOperators() {
            assertThat(Tokenizer.tokenize("&&").getFirst().type()).isEqualTo(TokenType.AND);
            assertThat(Tokenizer.tokenize("||").getFirst().type()).isEqualTo(TokenType.OR);
            assertThat(Tokenizer.tokenize("!").getFirst().type()).isEqualTo(TokenType.NOT);
        }

        @Test
        @DisplayName("Single ampersand is bitwise AND | 单个 & 是位与运算符")
        void testSingleAmpersandIsBitwiseAnd() {
            assertThat(Tokenizer.tokenize("&").getFirst().type()).isEqualTo(TokenType.BIT_AND);
        }

        @Test
        @DisplayName("Single pipe is bitwise OR | 单个 | 是位或运算符")
        void testSinglePipeIsBitwiseOr() {
            assertThat(Tokenizer.tokenize("|").getFirst().type()).isEqualTo(TokenType.BIT_OR);
        }
    }

    @Nested
    @DisplayName("Punctuation Tokenization Tests | 标点符号词法分析测试")
    class PunctuationTokenizationTests {

        @Test
        @DisplayName("Tokenize parentheses | 分析括号")
        void testTokenizeParentheses() {
            assertThat(Tokenizer.tokenize("(").getFirst().type()).isEqualTo(TokenType.LPAREN);
            assertThat(Tokenizer.tokenize(")").getFirst().type()).isEqualTo(TokenType.RPAREN);
        }

        @Test
        @DisplayName("Tokenize brackets | 分析方括号")
        void testTokenizeBrackets() {
            assertThat(Tokenizer.tokenize("[").getFirst().type()).isEqualTo(TokenType.LBRACKET);
            assertThat(Tokenizer.tokenize("]").getFirst().type()).isEqualTo(TokenType.RBRACKET);
        }

        @Test
        @DisplayName("Tokenize braces | 分析花括号")
        void testTokenizeBraces() {
            assertThat(Tokenizer.tokenize("{").getFirst().type()).isEqualTo(TokenType.LBRACE);
            assertThat(Tokenizer.tokenize("}").getFirst().type()).isEqualTo(TokenType.RBRACE);
        }

        @Test
        @DisplayName("Tokenize dot | 分析点号")
        void testTokenizeDot() {
            assertThat(Tokenizer.tokenize(".").getFirst().type()).isEqualTo(TokenType.DOT);
        }

        @Test
        @DisplayName("Tokenize safe navigation | 分析安全导航")
        void testTokenizeSafeNav() {
            List<Token> tokens = Tokenizer.tokenize("?.");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.SAFE_NAV);
            assertThat(tokens.getFirst().length()).isEqualTo(2);
        }

        @Test
        @DisplayName("Tokenize comma | 分析逗号")
        void testTokenizeComma() {
            assertThat(Tokenizer.tokenize(",").getFirst().type()).isEqualTo(TokenType.COMMA);
        }

        @Test
        @DisplayName("Tokenize colon | 分析冒号")
        void testTokenizeColon() {
            assertThat(Tokenizer.tokenize(":").getFirst().type()).isEqualTo(TokenType.COLON);
        }

        @Test
        @DisplayName("Tokenize question mark | 分析问号")
        void testTokenizeQuestion() {
            assertThat(Tokenizer.tokenize("?").getFirst().type()).isEqualTo(TokenType.QUESTION);
        }

        @Test
        @DisplayName("Tokenize hash | 分析井号")
        void testTokenizeHash() {
            assertThat(Tokenizer.tokenize("#").getFirst().type()).isEqualTo(TokenType.HASH);
        }
    }

    @Nested
    @DisplayName("Collection Operator Tokenization Tests | 集合运算符词法分析测试")
    class CollectionOperatorTokenizationTests {

        @Test
        @DisplayName("Tokenize filter operator | 分析过滤运算符")
        void testTokenizeFilter() {
            List<Token> tokens = Tokenizer.tokenize(".?[");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.FILTER);
            assertThat(tokens.getFirst().length()).isEqualTo(3);
        }

        @Test
        @DisplayName("Tokenize project operator | 分析投影运算符")
        void testTokenizeProject() {
            List<Token> tokens = Tokenizer.tokenize(".![");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.PROJECT);
            assertThat(tokens.getFirst().length()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Whitespace Handling Tests | 空白处理测试")
    class WhitespaceHandlingTests {

        @Test
        @DisplayName("Skip leading whitespace | 跳过前导空白")
        void testSkipLeadingWhitespace() {
            List<Token> tokens = Tokenizer.tokenize("   42");
            assertThat(tokens.getFirst().type()).isEqualTo(TokenType.NUMBER);
        }

        @Test
        @DisplayName("Skip trailing whitespace | 跳过尾部空白")
        void testSkipTrailingWhitespace() {
            List<Token> tokens = Tokenizer.tokenize("42   ");
            assertThat(tokens).hasSize(2);
        }

        @Test
        @DisplayName("Skip whitespace between tokens | 跳过词法单元之间的空白")
        void testSkipWhitespaceBetween() {
            List<Token> tokens = Tokenizer.tokenize("1  +  2");
            assertThat(tokens).hasSize(4);
        }

        @Test
        @DisplayName("Handle tabs and newlines | 处理制表符和换行符")
        void testHandleTabsAndNewlines() {
            List<Token> tokens = Tokenizer.tokenize("1\t+\n2");
            assertThat(tokens).hasSize(4);
        }
    }

    @Nested
    @DisplayName("Complex Expression Tests | 复杂表达式测试")
    class ComplexExpressionTests {

        @Test
        @DisplayName("Tokenize arithmetic expression | 分析算术表达式")
        void testTokenizeArithmeticExpression() {
            List<Token> tokens = Tokenizer.tokenize("1 + 2 * 3");
            assertThat(tokens).hasSize(6);
            assertThat(tokens.get(0).type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.get(1).type()).isEqualTo(TokenType.PLUS);
            assertThat(tokens.get(2).type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.get(3).type()).isEqualTo(TokenType.STAR);
            assertThat(tokens.get(4).type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.get(5).type()).isEqualTo(TokenType.EOF);
        }

        @Test
        @DisplayName("Tokenize function call | 分析函数调用")
        void testTokenizeFunctionCall() {
            List<Token> tokens = Tokenizer.tokenize("upper('hello')");
            assertThat(tokens.get(0).type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.get(1).type()).isEqualTo(TokenType.LPAREN);
            assertThat(tokens.get(2).type()).isEqualTo(TokenType.STRING);
            assertThat(tokens.get(3).type()).isEqualTo(TokenType.RPAREN);
        }

        @Test
        @DisplayName("Tokenize method call | 分析方法调用")
        void testTokenizeMethodCall() {
            List<Token> tokens = Tokenizer.tokenize("str.toUpperCase()");
            assertThat(tokens.get(0).type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.get(1).type()).isEqualTo(TokenType.DOT);
            assertThat(tokens.get(2).type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.get(3).type()).isEqualTo(TokenType.LPAREN);
            assertThat(tokens.get(4).type()).isEqualTo(TokenType.RPAREN);
        }

        @Test
        @DisplayName("Tokenize ternary expression | 分析三元表达式")
        void testTokenizeTernaryExpression() {
            List<Token> tokens = Tokenizer.tokenize("a ? b : c");
            assertThat(tokens.get(0).type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.get(1).type()).isEqualTo(TokenType.QUESTION);
            assertThat(tokens.get(2).type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.get(3).type()).isEqualTo(TokenType.COLON);
            assertThat(tokens.get(4).type()).isEqualTo(TokenType.IDENTIFIER);
        }

        @Test
        @DisplayName("Tokenize variable reference | 分析变量引用")
        void testTokenizeVariableReference() {
            List<Token> tokens = Tokenizer.tokenize("#myVar");
            assertThat(tokens.get(0).type()).isEqualTo(TokenType.HASH);
            assertThat(tokens.get(1).type()).isEqualTo(TokenType.IDENTIFIER);
        }

        @Test
        @DisplayName("Tokenize list literal | 分析列表字面量")
        void testTokenizeListLiteral() {
            List<Token> tokens = Tokenizer.tokenize("{1, 2, 3}");
            assertThat(tokens.get(0).type()).isEqualTo(TokenType.LBRACE);
            assertThat(tokens.get(1).type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.get(2).type()).isEqualTo(TokenType.COMMA);
            assertThat(tokens.get(3).type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.get(4).type()).isEqualTo(TokenType.COMMA);
            assertThat(tokens.get(5).type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.get(6).type()).isEqualTo(TokenType.RBRACE);
        }

        @Test
        @DisplayName("Tokenize index access | 分析索引访问")
        void testTokenizeIndexAccess() {
            List<Token> tokens = Tokenizer.tokenize("list[0]");
            assertThat(tokens.get(0).type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.get(1).type()).isEqualTo(TokenType.LBRACKET);
            assertThat(tokens.get(2).type()).isEqualTo(TokenType.NUMBER);
            assertThat(tokens.get(3).type()).isEqualTo(TokenType.RBRACKET);
        }

        @Test
        @DisplayName("Tokenize safe navigation chain | 分析安全导航链")
        void testTokenizeSafeNavigationChain() {
            List<Token> tokens = Tokenizer.tokenize("a?.b?.c");
            assertThat(tokens.get(0).type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.get(1).type()).isEqualTo(TokenType.SAFE_NAV);
            assertThat(tokens.get(2).type()).isEqualTo(TokenType.IDENTIFIER);
            assertThat(tokens.get(3).type()).isEqualTo(TokenType.SAFE_NAV);
            assertThat(tokens.get(4).type()).isEqualTo(TokenType.IDENTIFIER);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests | 错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Unexpected character throws | 意外字符抛出异常")
        void testUnexpectedCharacter() {
            assertThatThrownBy(() -> Tokenizer.tokenize("@"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("Unexpected character");
        }

        @Test
        @DisplayName("Unterminated string at EOF | EOF 处未终止字符串")
        void testUnterminatedStringAtEof() {
            assertThatThrownBy(() -> Tokenizer.tokenize("\"hello"))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("Unterminated string");
        }
    }

    @Nested
    @DisplayName("Position Tracking Tests | 位置跟踪测试")
    class PositionTrackingTests {

        @Test
        @DisplayName("Track token positions | 跟踪词法单元位置")
        void testTrackTokenPositions() {
            List<Token> tokens = Tokenizer.tokenize("1 + 2");
            assertThat(tokens.get(0).position()).isEqualTo(0);
            assertThat(tokens.get(1).position()).isEqualTo(2);
            assertThat(tokens.get(2).position()).isEqualTo(4);
        }

        @Test
        @DisplayName("Track multi-character token length | 跟踪多字符词法单元长度")
        void testTrackTokenLength() {
            List<Token> tokens = Tokenizer.tokenize("hello");
            assertThat(tokens.getFirst().position()).isEqualTo(0);
            assertThat(tokens.getFirst().length()).isEqualTo(5);
        }

        @Test
        @DisplayName("Track string literal length | 跟踪字符串字面量长度")
        void testTrackStringLiteralLength() {
            List<Token> tokens = Tokenizer.tokenize("'hello'");
            assertThat(tokens.getFirst().position()).isEqualTo(0);
            assertThat(tokens.getFirst().length()).isEqualTo(7);
        }
    }
}
