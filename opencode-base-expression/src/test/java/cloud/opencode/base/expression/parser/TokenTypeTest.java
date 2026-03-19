package cloud.opencode.base.expression.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * TokenType Tests
 * TokenType 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("TokenType Tests | TokenType 测试")
class TokenTypeTest {

    @Nested
    @DisplayName("Literal Types Tests | 字面量类型测试")
    class LiteralTypesTests {

        @Test
        @DisplayName("NUMBER exists | NUMBER 存在")
        void testNumberExists() {
            assertThat(TokenType.NUMBER).isNotNull();
            assertThat(TokenType.valueOf("NUMBER")).isEqualTo(TokenType.NUMBER);
        }

        @Test
        @DisplayName("STRING exists | STRING 存在")
        void testStringExists() {
            assertThat(TokenType.STRING).isNotNull();
            assertThat(TokenType.valueOf("STRING")).isEqualTo(TokenType.STRING);
        }

        @Test
        @DisplayName("BOOLEAN_TRUE exists | BOOLEAN_TRUE 存在")
        void testBooleanTrueExists() {
            assertThat(TokenType.BOOLEAN_TRUE).isNotNull();
            assertThat(TokenType.valueOf("BOOLEAN_TRUE")).isEqualTo(TokenType.BOOLEAN_TRUE);
        }

        @Test
        @DisplayName("BOOLEAN_FALSE exists | BOOLEAN_FALSE 存在")
        void testBooleanFalseExists() {
            assertThat(TokenType.BOOLEAN_FALSE).isNotNull();
            assertThat(TokenType.valueOf("BOOLEAN_FALSE")).isEqualTo(TokenType.BOOLEAN_FALSE);
        }

        @Test
        @DisplayName("NULL exists | NULL 存在")
        void testNullExists() {
            assertThat(TokenType.NULL).isNotNull();
            assertThat(TokenType.valueOf("NULL")).isEqualTo(TokenType.NULL);
        }
    }

    @Nested
    @DisplayName("Identifier Type Tests | 标识符类型测试")
    class IdentifierTypeTests {

        @Test
        @DisplayName("IDENTIFIER exists | IDENTIFIER 存在")
        void testIdentifierExists() {
            assertThat(TokenType.IDENTIFIER).isNotNull();
            assertThat(TokenType.valueOf("IDENTIFIER")).isEqualTo(TokenType.IDENTIFIER);
        }
    }

    @Nested
    @DisplayName("Operator Types Tests | 运算符类型测试")
    class OperatorTypesTests {

        @Test
        @DisplayName("Arithmetic operators exist | 算术运算符存在")
        void testArithmeticOperators() {
            assertThat(TokenType.PLUS).isNotNull();
            assertThat(TokenType.MINUS).isNotNull();
            assertThat(TokenType.STAR).isNotNull();
            assertThat(TokenType.SLASH).isNotNull();
            assertThat(TokenType.PERCENT).isNotNull();
            assertThat(TokenType.POWER).isNotNull();
        }

        @Test
        @DisplayName("Comparison operators exist | 比较运算符存在")
        void testComparisonOperators() {
            assertThat(TokenType.EQ).isNotNull();
            assertThat(TokenType.NE).isNotNull();
            assertThat(TokenType.LT).isNotNull();
            assertThat(TokenType.LE).isNotNull();
            assertThat(TokenType.GT).isNotNull();
            assertThat(TokenType.GE).isNotNull();
        }

        @Test
        @DisplayName("Logical operators exist | 逻辑运算符存在")
        void testLogicalOperators() {
            assertThat(TokenType.AND).isNotNull();
            assertThat(TokenType.OR).isNotNull();
            assertThat(TokenType.NOT).isNotNull();
        }

        @Test
        @DisplayName("Special operators exist | 特殊运算符存在")
        void testSpecialOperators() {
            assertThat(TokenType.MATCHES).isNotNull();
            assertThat(TokenType.INSTANCEOF).isNotNull();
            assertThat(TokenType.ASSIGN).isNotNull();
        }
    }

    @Nested
    @DisplayName("Punctuation Types Tests | 标点符号类型测试")
    class PunctuationTypesTests {

        @Test
        @DisplayName("Parentheses exist | 括号存在")
        void testParentheses() {
            assertThat(TokenType.LPAREN).isNotNull();
            assertThat(TokenType.RPAREN).isNotNull();
        }

        @Test
        @DisplayName("Brackets exist | 方括号存在")
        void testBrackets() {
            assertThat(TokenType.LBRACKET).isNotNull();
            assertThat(TokenType.RBRACKET).isNotNull();
        }

        @Test
        @DisplayName("Braces exist | 花括号存在")
        void testBraces() {
            assertThat(TokenType.LBRACE).isNotNull();
            assertThat(TokenType.RBRACE).isNotNull();
        }

        @Test
        @DisplayName("Other punctuation exists | 其他标点符号存在")
        void testOtherPunctuation() {
            assertThat(TokenType.DOT).isNotNull();
            assertThat(TokenType.SAFE_NAV).isNotNull();
            assertThat(TokenType.COMMA).isNotNull();
            assertThat(TokenType.COLON).isNotNull();
            assertThat(TokenType.QUESTION).isNotNull();
            assertThat(TokenType.HASH).isNotNull();
        }
    }

    @Nested
    @DisplayName("Collection Operator Types Tests | 集合运算符类型测试")
    class CollectionOperatorTypesTests {

        @Test
        @DisplayName("PROJECT exists | PROJECT 存在")
        void testProjectExists() {
            assertThat(TokenType.PROJECT).isNotNull();
            assertThat(TokenType.valueOf("PROJECT")).isEqualTo(TokenType.PROJECT);
        }

        @Test
        @DisplayName("FILTER exists | FILTER 存在")
        void testFilterExists() {
            assertThat(TokenType.FILTER).isNotNull();
            assertThat(TokenType.valueOf("FILTER")).isEqualTo(TokenType.FILTER);
        }
    }

    @Nested
    @DisplayName("EOF Type Tests | EOF 类型测试")
    class EofTypeTests {

        @Test
        @DisplayName("EOF exists | EOF 存在")
        void testEofExists() {
            assertThat(TokenType.EOF).isNotNull();
            assertThat(TokenType.valueOf("EOF")).isEqualTo(TokenType.EOF);
        }
    }

    @Nested
    @DisplayName("Enum Standard Methods Tests | 枚举标准方法测试")
    class EnumStandardMethodsTests {

        @Test
        @DisplayName("values returns all types | values 返回所有类型")
        void testValues() {
            TokenType[] values = TokenType.values();
            assertThat(values).isNotEmpty();
            assertThat(values).contains(
                    TokenType.NUMBER, TokenType.STRING, TokenType.IDENTIFIER,
                    TokenType.PLUS, TokenType.MINUS, TokenType.STAR,
                    TokenType.EQ, TokenType.NE, TokenType.LT,
                    TokenType.AND, TokenType.OR, TokenType.NOT,
                    TokenType.LPAREN, TokenType.RPAREN, TokenType.DOT,
                    TokenType.FILTER, TokenType.PROJECT, TokenType.EOF
            );
        }

        @Test
        @DisplayName("valueOf works for all types | valueOf 对所有类型有效")
        void testValueOf() {
            for (TokenType type : TokenType.values()) {
                assertThat(TokenType.valueOf(type.name())).isEqualTo(type);
            }
        }

        @Test
        @DisplayName("valueOf throws for invalid name | valueOf 对无效名称抛出异常")
        void testValueOfInvalid() {
            assertThatThrownBy(() -> TokenType.valueOf("INVALID_TYPE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("ordinal is unique | ordinal 是唯一的")
        void testOrdinalUnique() {
            TokenType[] values = TokenType.values();
            for (int i = 0; i < values.length; i++) {
                assertThat(values[i].ordinal()).isEqualTo(i);
            }
        }

        @Test
        @DisplayName("name returns correct string | name 返回正确字符串")
        void testName() {
            assertThat(TokenType.PLUS.name()).isEqualTo("PLUS");
            assertThat(TokenType.NUMBER.name()).isEqualTo("NUMBER");
            assertThat(TokenType.IDENTIFIER.name()).isEqualTo("IDENTIFIER");
        }
    }
}
