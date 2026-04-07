package cloud.opencode.base.json.util;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonCanonicalizer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.3
 */
@DisplayName("JsonCanonicalizer 测试")
class JsonCanonicalizerTest {

    @Nested
    @DisplayName("对象规范化测试")
    class ObjectTests {

        @Test
        @DisplayName("空对象")
        void emptyObject() {
            JsonNode node = JsonNode.object();
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("{}");
        }

        @Test
        @DisplayName("对象键排序")
        void objectKeysSorted() {
            JsonNode node = JsonNode.object()
                    .put("z", "last")
                    .put("a", "first")
                    .put("m", "middle");
            String result = JsonCanonicalizer.canonicalize(node);
            assertThat(result).isEqualTo("{\"a\":\"first\",\"m\":\"middle\",\"z\":\"last\"}");
        }

        @Test
        @DisplayName("嵌套对象递归排序")
        void nestedObjectsSortedRecursively() {
            JsonNode inner = JsonNode.object()
                    .put("b", 2)
                    .put("a", 1);
            JsonNode outer = JsonNode.object()
                    .put("y", inner)
                    .put("x", "value");
            String result = JsonCanonicalizer.canonicalize(outer);
            assertThat(result).isEqualTo("{\"x\":\"value\",\"y\":{\"a\":1,\"b\":2}}");
        }
    }

    @Nested
    @DisplayName("数组规范化测试")
    class ArrayTests {

        @Test
        @DisplayName("空数组")
        void emptyArray() {
            JsonNode node = JsonNode.array();
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("[]");
        }

        @Test
        @DisplayName("数组元素保持顺序")
        void arrayElementsPreserveOrder() {
            JsonNode node = JsonNode.array().add(3).add(1).add(2);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("[3,1,2]");
        }
    }

    @Nested
    @DisplayName("数字规范化测试")
    class NumberTests {

        @Test
        @DisplayName("整数无小数点")
        void integerNoDecimalPoint() {
            JsonNode node = JsonNode.of(42);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("42");
        }

        @Test
        @DisplayName("长整数")
        void longInteger() {
            JsonNode node = JsonNode.of(9007199254740993L);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("9007199254740993");
        }

        @Test
        @DisplayName("小数输出")
        void decimalOutput() {
            JsonNode node = JsonNode.of(1.5);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("1.5");
        }

        @Test
        @DisplayName("-0变为0")
        void negativeZeroBecomesZero() {
            JsonNode node = JsonNode.of(-0.0);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("0");
        }

        @Test
        @DisplayName("大数使用指数表示")
        void largeNumberExponential() {
            JsonNode node = JsonNode.of(1e21);
            String result = JsonCanonicalizer.canonicalize(node);
            assertThat(result).isEqualTo("1e+21");
        }

        @Test
        @DisplayName("负数")
        void negativeNumber() {
            JsonNode node = JsonNode.of(-42);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("-42");
        }

        @Test
        @DisplayName("零")
        void zero() {
            JsonNode node = JsonNode.of(0);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("0");
        }

        @Test
        @DisplayName("NaN抛出异常")
        void nanThrowsException() {
            JsonNode node = JsonNode.of(Double.NaN);
            assertThatThrownBy(() -> JsonCanonicalizer.canonicalize(node))
                    .isInstanceOf(OpenJsonProcessingException.class)
                    .hasMessageContaining("NaN");
        }

        @Test
        @DisplayName("Infinity抛出异常")
        void infinityThrowsException() {
            JsonNode node = JsonNode.of(Double.POSITIVE_INFINITY);
            assertThatThrownBy(() -> JsonCanonicalizer.canonicalize(node))
                    .isInstanceOf(OpenJsonProcessingException.class)
                    .hasMessageContaining("Infinity");
        }
    }

    @Nested
    @DisplayName("字符串规范化测试")
    class StringTests {

        @Test
        @DisplayName("简单字符串")
        void simpleString() {
            JsonNode node = JsonNode.of("hello");
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("\"hello\"");
        }

        @Test
        @DisplayName("控制字符转义")
        void controlCharEscaping() {
            JsonNode node = JsonNode.of("line1\nline2\ttab");
            assertThat(JsonCanonicalizer.canonicalize(node))
                    .isEqualTo("\"line1\\nline2\\ttab\"");
        }

        @Test
        @DisplayName("特殊字符转义")
        void specialCharEscaping() {
            JsonNode node = JsonNode.of("quote\"and\\backslash");
            assertThat(JsonCanonicalizer.canonicalize(node))
                    .isEqualTo("\"quote\\\"and\\\\backslash\"");
        }

        @Test
        @DisplayName("低位控制字符使用\\uXXXX")
        void lowControlCharUnicodeEscape() {
            JsonNode node = JsonNode.of("null\u0000char");
            String result = JsonCanonicalizer.canonicalize(node);
            assertThat(result).isEqualTo("\"null\\u0000char\"");
        }
    }

    @Nested
    @DisplayName("原始类型测试")
    class PrimitiveTests {

        @Test
        @DisplayName("null节点")
        void nullNode() {
            JsonNode node = JsonNode.nullNode();
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("null");
        }

        @Test
        @DisplayName("true布尔值")
        void trueBoolean() {
            JsonNode node = JsonNode.of(true);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("true");
        }

        @Test
        @DisplayName("false布尔值")
        void falseBoolean() {
            JsonNode node = JsonNode.of(false);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("false");
        }
    }

    @Nested
    @DisplayName("幂等性和便捷方法测试")
    class MiscTests {

        @Test
        @DisplayName("规范化是幂等的")
        void idempotent() {
            JsonNode node = JsonNode.object()
                    .put("b", 2)
                    .put("a", 1);
            String first = JsonCanonicalizer.canonicalize(node);
            String second = JsonCanonicalizer.canonicalize(first);
            assertThat(second).isEqualTo(first);
        }

        @Test
        @DisplayName("字符串便捷方法")
        void stringConvenienceMethod() {
            String result = JsonCanonicalizer.canonicalize("{\"b\":2,\"a\":1}");
            assertThat(result).isEqualTo("{\"a\":1,\"b\":2}");
        }

        @Test
        @DisplayName("null节点参数抛出异常")
        void nullNodeThrowsException() {
            assertThatThrownBy(() -> JsonCanonicalizer.canonicalize((JsonNode) null))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("null字符串参数抛出异常")
        void nullStringThrowsException() {
            assertThatThrownBy(() -> JsonCanonicalizer.canonicalize((String) null))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }
    }

    @Nested
    @DisplayName("RFC 8785 数字边界测试")
    class Rfc8785NumberEdgeCaseTests {

        @Test
        @DisplayName("1e-7 (很小的双精度)")
        void verySmallDouble() {
            JsonNode node = JsonNode.of(1e-7);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("1e-7");
        }

        @Test
        @DisplayName("1e20 (大整数在long范围内)")
        void largeIntegerWithinLongRange() {
            JsonNode node = JsonNode.of(1e20);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("100000000000000000000");
        }

        @Test
        @DisplayName("0.1")
        void pointOne() {
            JsonNode node = JsonNode.of(0.1);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("0.1");
        }

        @Test
        @DisplayName("0.2")
        void pointTwo() {
            JsonNode node = JsonNode.of(0.2);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("0.2");
        }

        @Test
        @DisplayName("1.0 作为double序列化为整数")
        void onePointZeroAsInteger() {
            JsonNode node = JsonNode.of(1.0);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("1");
        }

        @Test
        @DisplayName("负指数")
        void negativeExponential() {
            JsonNode node = JsonNode.of(-1e21);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("-1e+21");
        }

        @Test
        @DisplayName("1.5e21")
        void decimalExponential() {
            JsonNode node = JsonNode.of(1.5e21);
            assertThat(JsonCanonicalizer.canonicalize(node)).isEqualTo("1.5e+21");
        }

        @Test
        @DisplayName("9999999999999998e7")
        void largeDoubleExponential() {
            JsonNode node = JsonNode.of(9.999999999999998e22);
            String result = JsonCanonicalizer.canonicalize(node);
            // Verify it round-trips
            assertThat(Double.parseDouble(result)).isEqualTo(9.999999999999998e22);
        }

        @Test
        @DisplayName("极小正数 5e-324")
        void minPositiveDouble() {
            JsonNode node = JsonNode.of(5e-324);
            String result = JsonCanonicalizer.canonicalize(node);
            assertThat(Double.parseDouble(result)).isEqualTo(5e-324);
        }
    }

    @Nested
    @DisplayName("深度限制测试")
    class DepthTests {

        @Test
        @DisplayName("超过深度限制抛出异常")
        void depthLimitExceeded() {
            // Build a deeply nested structure (1002 levels)
            JsonNode current = JsonNode.of("leaf");
            for (int i = 0; i < 1002; i++) {
                JsonNode.ArrayNode arr = JsonNode.array();
                arr.add(current);
                current = arr;
            }
            JsonNode deepNode = current;
            assertThatThrownBy(() -> JsonCanonicalizer.canonicalize(deepNode))
                    .isInstanceOf(OpenJsonProcessingException.class)
                    .hasMessageContaining("depth");
        }
    }
}
