package cloud.opencode.base.json.util;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import cloud.opencode.base.json.util.JsonTruncator.TruncateConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonTruncator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.3
 */
@DisplayName("JsonTruncator 测试")
class JsonTruncatorTest {

    @Nested
    @DisplayName("字符串级截断测试")
    class StringTruncationTests {

        @Test
        @DisplayName("短JSON原样返回")
        void shortJsonReturnedAsIs() {
            String json = "{\"a\":1}";
            assertThat(JsonTruncator.truncate(json, 100)).isEqualTo(json);
        }

        @Test
        @DisplayName("长JSON截断并添加标记")
        void longJsonTruncatedWithMarker() {
            String json = "{\"key\":\"" + "x".repeat(100) + "\"}";
            String result = JsonTruncator.truncate(json, 20);
            assertThat(result).hasSize(20 + "...(truncated)".length());
            assertThat(result).endsWith("...(truncated)");
            assertThat(result).startsWith("{\"key\":\"");
        }

        @Test
        @DisplayName("null输入返回null字符串")
        void nullInputReturnsNullString() {
            assertThat(JsonTruncator.truncate((String) null, 100)).isEqualTo("null");
        }

        @Test
        @DisplayName("非正maxLength抛出异常")
        void nonPositiveMaxLengthThrows() {
            assertThatThrownBy(() -> JsonTruncator.truncate("test", 0))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("恰好等于maxLength的字符串不截断")
        void exactLengthNotTruncated() {
            String json = "12345";
            assertThat(JsonTruncator.truncate(json, 5)).isEqualTo("12345");
        }
    }

    @Nested
    @DisplayName("树级截断测试")
    class TreeTruncationTests {

        @Test
        @DisplayName("数组截断到最大元素数")
        void arraysTrimmed() {
            JsonNode.ArrayNode arr = JsonNode.array();
            for (int i = 0; i < 10; i++) {
                arr.add(i);
            }
            TruncateConfig config = new TruncateConfig(4096, 3, 100, 5, "...");
            String result = JsonTruncator.truncate(arr, config);
            assertThat(result).contains("...(7 more)");
            assertThat(result).startsWith("[");
            assertThat(result).endsWith("]");
        }

        @Test
        @DisplayName("字符串截断到最大长度")
        void stringsTrimmed() {
            JsonNode node = JsonNode.of("a".repeat(200));
            TruncateConfig config = new TruncateConfig(4096, 3, 50, 5, "...");
            String result = JsonTruncator.truncate(node, config);
            // The truncated string value should be 50 chars + "..."
            assertThat(result).contains("...");
            assertThat(result.length()).isLessThan(200 + 10); // much shorter than original
        }

        @Test
        @DisplayName("深度限制")
        void depthLimited() {
            JsonNode inner = JsonNode.object().put("deep", "value");
            JsonNode mid = JsonNode.object().put("mid", inner);
            JsonNode outer = JsonNode.object().put("outer", mid);

            TruncateConfig config = new TruncateConfig(4096, 3, 100, 2, "...");
            String result = JsonTruncator.truncate(outer, config);
            assertThat(result).contains("...(Object with 1 property)");
        }

        @Test
        @DisplayName("null节点抛出异常")
        void nullNodeThrows() {
            assertThatThrownBy(() -> JsonTruncator.truncate((JsonNode) null, TruncateConfig.DEFAULT))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("null配置抛出异常")
        void nullConfigThrows() {
            assertThatThrownBy(() -> JsonTruncator.truncate(JsonNode.object(), null))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("简单对象不截断")
        void simpleObjectNotTruncated() {
            JsonNode node = JsonNode.object().put("key", "value");
            String result = JsonTruncator.truncate(node, TruncateConfig.DEFAULT);
            assertThat(result).isEqualTo("{\"key\":\"value\"}");
        }
    }

    @Nested
    @DisplayName("自定义配置测试")
    class CustomConfigTests {

        @Test
        @DisplayName("自定义截断标记")
        void customTruncationMarker() {
            JsonNode node = JsonNode.of("a".repeat(200));
            TruncateConfig config = new TruncateConfig(4096, 3, 50, 5, "~~~");
            String result = JsonTruncator.truncate(node, config);
            assertThat(result).contains("~~~");
        }

        @Test
        @DisplayName("自定义数组元素限制")
        void customArrayLimit() {
            JsonNode.ArrayNode arr = JsonNode.array();
            for (int i = 0; i < 10; i++) {
                arr.add(i);
            }
            TruncateConfig config = new TruncateConfig(4096, 5, 100, 5, "...");
            String result = JsonTruncator.truncate(arr, config);
            assertThat(result).contains("...(5 more)");
        }

        @Test
        @DisplayName("配置参数验证")
        void configValidation() {
            assertThatThrownBy(() -> new TruncateConfig(0, 3, 100, 5, "..."))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new TruncateConfig(1024, -1, 100, 5, "..."))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new TruncateConfig(1024, 3, 0, 5, "..."))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new TruncateConfig(1024, 3, 100, -1, "..."))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new TruncateConfig(1024, 3, 100, 0, "..."))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new TruncateConfig(1024, 3, 100, 5, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("摘要测试")
    class SummaryTests {

        @Test
        @DisplayName("对象摘要")
        void objectSummary() {
            JsonNode node = JsonNode.object().put("a", 1).put("b", 2).put("c", 3);
            assertThat(JsonTruncator.summary(node)).isEqualTo("Object{3 properties}");
        }

        @Test
        @DisplayName("单属性对象摘要")
        void singlePropertyObjectSummary() {
            JsonNode node = JsonNode.object().put("a", 1);
            assertThat(JsonTruncator.summary(node)).isEqualTo("Object{1 property}");
        }

        @Test
        @DisplayName("数组摘要")
        void arraySummary() {
            JsonNode.ArrayNode arr = JsonNode.array();
            for (int i = 0; i < 100; i++) {
                arr.add(i);
            }
            assertThat(JsonTruncator.summary(arr)).isEqualTo("Array[100 elements]");
        }

        @Test
        @DisplayName("单元素数组摘要")
        void singleElementArraySummary() {
            JsonNode node = JsonNode.array().add(1);
            assertThat(JsonTruncator.summary(node)).isEqualTo("Array[1 element]");
        }

        @Test
        @DisplayName("字符串摘要")
        void stringSummary() {
            JsonNode node = JsonNode.of("hello world! this is a test");
            assertThat(JsonTruncator.summary(node)).isEqualTo("String(27 chars)");
        }

        @Test
        @DisplayName("单字符字符串摘要")
        void singleCharStringSummary() {
            JsonNode node = JsonNode.of("x");
            assertThat(JsonTruncator.summary(node)).isEqualTo("String(1 char)");
        }

        @Test
        @DisplayName("数字摘要")
        void numberSummary() {
            JsonNode node = JsonNode.of(42);
            assertThat(JsonTruncator.summary(node)).isEqualTo("Number(42)");
        }

        @Test
        @DisplayName("布尔值摘要")
        void booleanSummary() {
            assertThat(JsonTruncator.summary(JsonNode.of(true))).isEqualTo("Boolean(true)");
            assertThat(JsonTruncator.summary(JsonNode.of(false))).isEqualTo("Boolean(false)");
        }

        @Test
        @DisplayName("null摘要")
        void nullSummary() {
            assertThat(JsonTruncator.summary(JsonNode.nullNode())).isEqualTo("null");
        }

        @Test
        @DisplayName("null参数摘要")
        void nullParameterSummary() {
            assertThat(JsonTruncator.summary(null)).isEqualTo("null");
        }
    }
}
