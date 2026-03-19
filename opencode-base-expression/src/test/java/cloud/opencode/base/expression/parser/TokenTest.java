package cloud.opencode.base.expression.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Token Tests
 * Token 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("Token Tests | Token 测试")
class TokenTest {

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of with type and position | of 使用类型和位置")
        void testOfWithTypeAndPosition() {
            Token token = Token.of(TokenType.PLUS, 5);
            assertThat(token.type()).isEqualTo(TokenType.PLUS);
            assertThat(token.value()).isNull();
            assertThat(token.position()).isEqualTo(5);
            assertThat(token.length()).isEqualTo(1);
        }

        @Test
        @DisplayName("of with type, position and length | of 使用类型、位置和长度")
        void testOfWithTypePositionAndLength() {
            Token token = Token.of(TokenType.EQ, 10, 2);
            assertThat(token.type()).isEqualTo(TokenType.EQ);
            assertThat(token.value()).isNull();
            assertThat(token.position()).isEqualTo(10);
            assertThat(token.length()).isEqualTo(2);
        }

        @Test
        @DisplayName("of with all parameters | of 使用所有参数")
        void testOfWithAllParameters() {
            Token token = Token.of(TokenType.NUMBER, 42, 0, 2);
            assertThat(token.type()).isEqualTo(TokenType.NUMBER);
            assertThat(token.value()).isEqualTo(42);
            assertThat(token.position()).isEqualTo(0);
            assertThat(token.length()).isEqualTo(2);
        }

        @Test
        @DisplayName("of with string value | of 使用字符串值")
        void testOfWithStringValue() {
            Token token = Token.of(TokenType.STRING, "hello", 0, 7);
            assertThat(token.type()).isEqualTo(TokenType.STRING);
            assertThat(token.value()).isEqualTo("hello");
            assertThat(token.position()).isEqualTo(0);
            assertThat(token.length()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("is Method Tests | is 方法测试")
    class IsMethodTests {

        @Test
        @DisplayName("is returns true for matching type | is 匹配类型返回 true")
        void testIsMatchingType() {
            Token token = Token.of(TokenType.PLUS, 0);
            assertThat(token.is(TokenType.PLUS)).isTrue();
        }

        @Test
        @DisplayName("is returns false for non-matching type | is 不匹配类型返回 false")
        void testIsNonMatchingType() {
            Token token = Token.of(TokenType.PLUS, 0);
            assertThat(token.is(TokenType.MINUS)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAny Method Tests | isAny 方法测试")
    class IsAnyMethodTests {

        @Test
        @DisplayName("isAny returns true for one matching type | isAny 有一个匹配类型返回 true")
        void testIsAnyOneMatching() {
            Token token = Token.of(TokenType.PLUS, 0);
            assertThat(token.isAny(TokenType.PLUS, TokenType.MINUS, TokenType.STAR)).isTrue();
        }

        @Test
        @DisplayName("isAny returns true for middle matching type | isAny 中间匹配类型返回 true")
        void testIsAnyMiddleMatching() {
            Token token = Token.of(TokenType.MINUS, 0);
            assertThat(token.isAny(TokenType.PLUS, TokenType.MINUS, TokenType.STAR)).isTrue();
        }

        @Test
        @DisplayName("isAny returns true for last matching type | isAny 最后匹配类型返回 true")
        void testIsAnyLastMatching() {
            Token token = Token.of(TokenType.STAR, 0);
            assertThat(token.isAny(TokenType.PLUS, TokenType.MINUS, TokenType.STAR)).isTrue();
        }

        @Test
        @DisplayName("isAny returns false for no matching types | isAny 无匹配类型返回 false")
        void testIsAnyNoMatching() {
            Token token = Token.of(TokenType.SLASH, 0);
            assertThat(token.isAny(TokenType.PLUS, TokenType.MINUS, TokenType.STAR)).isFalse();
        }

        @Test
        @DisplayName("isAny with single type | isAny 单个类型")
        void testIsAnySingleType() {
            Token token = Token.of(TokenType.PLUS, 0);
            assertThat(token.isAny(TokenType.PLUS)).isTrue();
            assertThat(token.isAny(TokenType.MINUS)).isFalse();
        }

        @Test
        @DisplayName("isAny with empty types | isAny 空类型数组")
        void testIsAnyEmptyTypes() {
            Token token = Token.of(TokenType.PLUS, 0);
            assertThat(token.isAny()).isFalse();
        }
    }

    @Nested
    @DisplayName("stringValue Method Tests | stringValue 方法测试")
    class StringValueMethodTests {

        @Test
        @DisplayName("stringValue returns value toString | stringValue 返回值的 toString")
        void testStringValueWithValue() {
            Token token = Token.of(TokenType.STRING, "hello", 0, 7);
            assertThat(token.stringValue()).isEqualTo("hello");
        }

        @Test
        @DisplayName("stringValue returns empty for null value | stringValue null 值返回空字符串")
        void testStringValueWithNullValue() {
            Token token = Token.of(TokenType.PLUS, 0);
            assertThat(token.stringValue()).isEmpty();
        }

        @Test
        @DisplayName("stringValue converts number to string | stringValue 转换数字为字符串")
        void testStringValueWithNumber() {
            Token token = Token.of(TokenType.NUMBER, 42, 0, 2);
            assertThat(token.stringValue()).isEqualTo("42");
        }

        @Test
        @DisplayName("stringValue converts boolean to string | stringValue 转换布尔值为字符串")
        void testStringValueWithBoolean() {
            Token token = Token.of(TokenType.BOOLEAN_TRUE, true, 0, 4);
            assertThat(token.stringValue()).isEqualTo("true");
        }
    }

    @Nested
    @DisplayName("numberValue Method Tests | numberValue 方法测试")
    class NumberValueMethodTests {

        @Test
        @DisplayName("numberValue returns number | numberValue 返回数字")
        void testNumberValueWithNumber() {
            Token token = Token.of(TokenType.NUMBER, 42, 0, 2);
            assertThat(token.numberValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("numberValue returns double | numberValue 返回双精度数")
        void testNumberValueWithDouble() {
            Token token = Token.of(TokenType.NUMBER, 3.14, 0, 4);
            assertThat(token.numberValue()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("numberValue returns long | numberValue 返回长整数")
        void testNumberValueWithLong() {
            Token token = Token.of(TokenType.NUMBER, 123456789012345L, 0, 15);
            assertThat(token.numberValue()).isEqualTo(123456789012345L);
        }

        @Test
        @DisplayName("numberValue returns 0 for null value | numberValue null 值返回 0")
        void testNumberValueWithNullValue() {
            Token token = Token.of(TokenType.PLUS, 0);
            assertThat(token.numberValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("numberValue returns 0 for non-number value | numberValue 非数字值返回 0")
        void testNumberValueWithNonNumber() {
            Token token = Token.of(TokenType.STRING, "hello", 0, 7);
            assertThat(token.numberValue()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTests {

        @Test
        @DisplayName("Accessors | 访问器")
        void testAccessors() {
            Token token = new Token(TokenType.NUMBER, 42, 5, 2);
            assertThat(token.type()).isEqualTo(TokenType.NUMBER);
            assertThat(token.value()).isEqualTo(42);
            assertThat(token.position()).isEqualTo(5);
            assertThat(token.length()).isEqualTo(2);
        }

        @Test
        @DisplayName("Equals and hashCode | equals 和 hashCode")
        void testEqualsAndHashCode() {
            Token token1 = Token.of(TokenType.NUMBER, 42, 0, 2);
            Token token2 = Token.of(TokenType.NUMBER, 42, 0, 2);
            assertThat(token1).isEqualTo(token2);
            assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
        }

        @Test
        @DisplayName("Not equals with different type | 不同类型不相等")
        void testNotEqualsType() {
            Token token1 = Token.of(TokenType.NUMBER, 42, 0, 2);
            Token token2 = Token.of(TokenType.STRING, 42, 0, 2);
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Not equals with different value | 不同值不相等")
        void testNotEqualsValue() {
            Token token1 = Token.of(TokenType.NUMBER, 42, 0, 2);
            Token token2 = Token.of(TokenType.NUMBER, 43, 0, 2);
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Not equals with different position | 不同位置不相等")
        void testNotEqualsPosition() {
            Token token1 = Token.of(TokenType.NUMBER, 42, 0, 2);
            Token token2 = Token.of(TokenType.NUMBER, 42, 1, 2);
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("toString | toString 方法")
        void testToString() {
            Token token = Token.of(TokenType.NUMBER, 42, 0, 2);
            assertThat(token.toString()).contains("NUMBER").contains("42");
        }
    }
}
