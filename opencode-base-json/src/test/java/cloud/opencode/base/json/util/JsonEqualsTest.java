package cloud.opencode.base.json.util;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonEquals 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.3
 */
@DisplayName("JsonEquals 测试")
class JsonEqualsTest {

    @Nested
    @DisplayName("equals(JsonNode, JsonNode) 方法测试")
    class EqualsNodeTests {

        @Test
        @DisplayName("相同对象相等")
        void testSameObjectsEqual() {
            JsonNode a = JsonNode.object().put("x", 1).put("y", 2);
            assertThat(JsonEquals.equals(a, a)).isTrue();
        }

        @Test
        @DisplayName("不同键顺序的对象相等")
        void testObjectsDifferentKeyOrderEqual() {
            JsonNode a = JsonNode.object().put("x", 1).put("y", 2);
            JsonNode b = JsonNode.object().put("y", 2).put("x", 1);

            assertThat(JsonEquals.equals(a, b)).isTrue();
        }

        @Test
        @DisplayName("不同值的对象不相等")
        void testObjectsDifferentValuesNotEqual() {
            JsonNode a = JsonNode.object().put("x", 1).put("y", 2);
            JsonNode b = JsonNode.object().put("x", 1).put("y", 3);

            assertThat(JsonEquals.equals(a, b)).isFalse();
        }

        @Test
        @DisplayName("不同键的对象不相等")
        void testObjectsDifferentKeysNotEqual() {
            JsonNode a = JsonNode.object().put("x", 1);
            JsonNode b = JsonNode.object().put("y", 1);

            assertThat(JsonEquals.equals(a, b)).isFalse();
        }

        @Test
        @DisplayName("不同大小的对象不相等")
        void testObjectsDifferentSizeNotEqual() {
            JsonNode a = JsonNode.object().put("x", 1);
            JsonNode b = JsonNode.object().put("x", 1).put("y", 2);

            assertThat(JsonEquals.equals(a, b)).isFalse();
        }

        @Test
        @DisplayName("相同顺序的数组相等")
        void testArraysSameOrderEqual() {
            JsonNode a = JsonNode.array().add(1).add(2).add(3);
            JsonNode b = JsonNode.array().add(1).add(2).add(3);

            assertThat(JsonEquals.equals(a, b)).isTrue();
        }

        @Test
        @DisplayName("不同顺序的数组不相等")
        void testArraysDifferentOrderNotEqual() {
            JsonNode a = JsonNode.array().add(1).add(2).add(3);
            JsonNode b = JsonNode.array().add(3).add(2).add(1);

            assertThat(JsonEquals.equals(a, b)).isFalse();
        }

        @Test
        @DisplayName("数字比较: int 与 double 相等 (1 == 1.0)")
        void testNumberComparisonIntVsDouble() {
            JsonNode a = JsonNode.of(1);
            JsonNode b = JsonNode.of(1.0);

            assertThat(JsonEquals.equals(a, b)).isTrue();
        }

        @Test
        @DisplayName("数字比较: 不同值不相等 (1.0 != 1.1)")
        void testNumberComparisonDifferentValues() {
            JsonNode a = JsonNode.of(1.0);
            JsonNode b = JsonNode.of(1.1);

            assertThat(JsonEquals.equals(a, b)).isFalse();
        }

        @Test
        @DisplayName("null 节点相等")
        void testNullNodesEqual() {
            JsonNode a = JsonNode.nullNode();
            JsonNode b = JsonNode.nullNode();

            assertThat(JsonEquals.equals(a, b)).isTrue();
        }

        @Test
        @DisplayName("Java null 引用相等")
        void testJavaNullReferencesEqual() {
            assertThat(JsonEquals.equals((JsonNode) null, (JsonNode) null)).isTrue();
        }

        @Test
        @DisplayName("Java null 与非 null 不相等")
        void testJavaNullVsNonNullNotEqual() {
            assertThat(JsonEquals.equals((JsonNode) null, JsonNode.of(1))).isFalse();
            assertThat(JsonEquals.equals(JsonNode.of(1), (JsonNode) null)).isFalse();
        }

        @Test
        @DisplayName("null 节点与其他类型不相等")
        void testNullVsOtherTypeNotEqual() {
            assertThat(JsonEquals.equals(JsonNode.nullNode(), JsonNode.of(0))).isFalse();
            assertThat(JsonEquals.equals(JsonNode.nullNode(), JsonNode.of(""))).isFalse();
            assertThat(JsonEquals.equals(JsonNode.nullNode(), JsonNode.of(false))).isFalse();
        }

        @Test
        @DisplayName("字符串值相等")
        void testStringValuesEqual() {
            assertThat(JsonEquals.equals(JsonNode.of("hello"), JsonNode.of("hello"))).isTrue();
        }

        @Test
        @DisplayName("字符串值不相等")
        void testStringValuesNotEqual() {
            assertThat(JsonEquals.equals(JsonNode.of("hello"), JsonNode.of("world"))).isFalse();
        }

        @Test
        @DisplayName("布尔值相等")
        void testBooleanValuesEqual() {
            assertThat(JsonEquals.equals(JsonNode.of(true), JsonNode.of(true))).isTrue();
            assertThat(JsonEquals.equals(JsonNode.of(false), JsonNode.of(false))).isTrue();
        }

        @Test
        @DisplayName("布尔值不相等")
        void testBooleanValuesNotEqual() {
            assertThat(JsonEquals.equals(JsonNode.of(true), JsonNode.of(false))).isFalse();
        }

        @Test
        @DisplayName("不同类型不相等")
        void testDifferentTypesNotEqual() {
            assertThat(JsonEquals.equals(JsonNode.of(1), JsonNode.of("1"))).isFalse();
            assertThat(JsonEquals.equals(JsonNode.of(true), JsonNode.of(1))).isFalse();
            assertThat(JsonEquals.equals(JsonNode.object(), JsonNode.array())).isFalse();
        }

        @Test
        @DisplayName("嵌套结构比较")
        void testNestedStructureComparison() {
            JsonNode a = JsonNode.object()
                    .put("users", JsonNode.array()
                            .add(JsonNode.object().put("name", "Alice").put("age", 30))
                            .add(JsonNode.object().put("name", "Bob").put("age", 25)));

            JsonNode b = JsonNode.object()
                    .put("users", JsonNode.array()
                            .add(JsonNode.object().put("age", 30).put("name", "Alice"))
                            .add(JsonNode.object().put("age", 25).put("name", "Bob")));

            assertThat(JsonEquals.equals(a, b)).isTrue();
        }

        @Test
        @DisplayName("空对象与空数组不相等")
        void testEmptyObjectVsEmptyArrayNotEqual() {
            assertThat(JsonEquals.equals(JsonNode.object(), JsonNode.array())).isFalse();
        }

        @Test
        @DisplayName("空对象相等")
        void testEmptyObjectsEqual() {
            assertThat(JsonEquals.equals(JsonNode.object(), JsonNode.object())).isTrue();
        }

        @Test
        @DisplayName("空数组相等")
        void testEmptyArraysEqual() {
            assertThat(JsonEquals.equals(JsonNode.array(), JsonNode.array())).isTrue();
        }
    }

    @Nested
    @DisplayName("equals(String, String) 方法测试")
    class EqualsStringTests {

        @Test
        @DisplayName("JSON 字符串比较")
        void testJsonStringComparison() {
            assertThat(JsonEquals.equals("{\"a\":1,\"b\":2}", "{\"b\":2,\"a\":1}")).isTrue();
        }

        @Test
        @DisplayName("JSON 字符串不相等")
        void testJsonStringNotEqual() {
            assertThat(JsonEquals.equals("{\"a\":1}", "{\"a\":2}")).isFalse();
        }

        @Test
        @DisplayName("null 字符串输入抛出异常")
        void testNullStringInputThrows() {
            assertThatThrownBy(() -> JsonEquals.equals(null, "{}"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> JsonEquals.equals("{}", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("无效 JSON 字符串抛出异常")
        void testInvalidJsonStringThrows() {
            assertThatThrownBy(() -> JsonEquals.equals("{invalid}", "{}"))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }
    }

    @Nested
    @DisplayName("equalsIgnoreArrayOrder 方法测试")
    class EqualsIgnoreArrayOrderTests {

        @Test
        @DisplayName("忽略数组顺序相等")
        void testIgnoreArrayOrderEqual() {
            JsonNode a = JsonNode.array().add(1).add(2).add(3);
            JsonNode b = JsonNode.array().add(3).add(2).add(1);

            assertThat(JsonEquals.equalsIgnoreArrayOrder(a, b)).isTrue();
        }

        @Test
        @DisplayName("忽略数组顺序 — 不同元素不相等")
        void testIgnoreArrayOrderDifferentElements() {
            JsonNode a = JsonNode.array().add(1).add(2).add(3);
            JsonNode b = JsonNode.array().add(1).add(2).add(4);

            assertThat(JsonEquals.equalsIgnoreArrayOrder(a, b)).isFalse();
        }

        @Test
        @DisplayName("忽略数组顺序 — 不同长度不相等")
        void testIgnoreArrayOrderDifferentSize() {
            JsonNode a = JsonNode.array().add(1).add(2);
            JsonNode b = JsonNode.array().add(1).add(2).add(3);

            assertThat(JsonEquals.equalsIgnoreArrayOrder(a, b)).isFalse();
        }

        @Test
        @DisplayName("忽略数组顺序 — 嵌套对象")
        void testIgnoreArrayOrderNestedObjects() {
            JsonNode a = JsonNode.array()
                    .add(JsonNode.object().put("id", 1))
                    .add(JsonNode.object().put("id", 2));

            JsonNode b = JsonNode.array()
                    .add(JsonNode.object().put("id", 2))
                    .add(JsonNode.object().put("id", 1));

            assertThat(JsonEquals.equalsIgnoreArrayOrder(a, b)).isTrue();
        }

        @Test
        @DisplayName("忽略数组顺序 — 重复元素正确匹配")
        void testIgnoreArrayOrderDuplicates() {
            JsonNode a = JsonNode.array().add(1).add(1).add(2);
            JsonNode b = JsonNode.array().add(2).add(1).add(1);

            assertThat(JsonEquals.equalsIgnoreArrayOrder(a, b)).isTrue();
        }

        @Test
        @DisplayName("忽略数组顺序 — 重复元素数量不同不相等")
        void testIgnoreArrayOrderDuplicateCountMismatch() {
            JsonNode a = JsonNode.array().add(1).add(1).add(2);
            JsonNode b = JsonNode.array().add(1).add(2).add(2);

            assertThat(JsonEquals.equalsIgnoreArrayOrder(a, b)).isFalse();
        }

        @Test
        @DisplayName("忽略数组顺序 — 对象仍忽略键顺序")
        void testIgnoreArrayOrderObjectsStillIgnoreKeyOrder() {
            JsonNode a = JsonNode.object().put("x", 1).put("y", 2);
            JsonNode b = JsonNode.object().put("y", 2).put("x", 1);

            assertThat(JsonEquals.equalsIgnoreArrayOrder(a, b)).isTrue();
        }

        @Test
        @DisplayName("忽略数组顺序 — 深层嵌套数组")
        void testIgnoreArrayOrderDeepNested() {
            JsonNode a = JsonNode.object()
                    .put("data", JsonNode.array()
                            .add(JsonNode.array().add(3).add(2).add(1))
                            .add(JsonNode.array().add(6).add(5).add(4)));

            JsonNode b = JsonNode.object()
                    .put("data", JsonNode.array()
                            .add(JsonNode.array().add(4).add(5).add(6))
                            .add(JsonNode.array().add(1).add(2).add(3)));

            assertThat(JsonEquals.equalsIgnoreArrayOrder(a, b)).isTrue();
        }
    }
}
