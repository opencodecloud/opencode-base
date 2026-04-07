package cloud.opencode.base.json.util;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonFlattener 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.3
 */
@DisplayName("JsonFlattener 测试")
class JsonFlattenerTest {

    @Nested
    @DisplayName("flatten 方法测试")
    class FlattenTests {

        @Test
        @DisplayName("扁平对象保持不变")
        void testFlatObjectStaysSame() {
            JsonNode obj = JsonNode.object()
                    .put("name", "John")
                    .put("age", 30)
                    .put("active", true);

            Map<String, JsonNode> result = JsonFlattener.flatten(obj);

            assertThat(result).hasSize(3);
            assertThat(result.get("name").asString()).isEqualTo("John");
            assertThat(result.get("age").asInt()).isEqualTo(30);
            assertThat(result.get("active").asBoolean()).isTrue();
        }

        @Test
        @DisplayName("嵌套对象正确扁平化")
        void testNestedObjectsFlattened() {
            JsonNode obj = JsonNode.object()
                    .put("a", JsonNode.object()
                            .put("b", 1)
                            .put("c", JsonNode.object().put("d", 2)));

            Map<String, JsonNode> result = JsonFlattener.flatten(obj);

            assertThat(result).hasSize(2);
            assertThat(result.get("a.b").asInt()).isEqualTo(1);
            assertThat(result.get("a.c.d").asInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("数组使用方括号表示法")
        void testArraysWithBracketNotation() {
            JsonNode obj = JsonNode.object()
                    .put("c", JsonNode.array().add(2).add(3));

            Map<String, JsonNode> result = JsonFlattener.flatten(obj);

            assertThat(result).hasSize(2);
            assertThat(result.get("c[0]").asInt()).isEqualTo(2);
            assertThat(result.get("c[1]").asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("数组使用点表示法 (自定义配置)")
        void testArraysWithDotNotation() {
            JsonNode obj = JsonNode.object()
                    .put("c", JsonNode.array().add(2).add(3));

            JsonFlattener.FlattenConfig config = new JsonFlattener.FlattenConfig(".", false, 1000);
            Map<String, JsonNode> result = JsonFlattener.flatten(obj, config);

            assertThat(result).hasSize(2);
            assertThat(result.get("c.0").asInt()).isEqualTo(2);
            assertThat(result.get("c.1").asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("自定义分隔符")
        void testCustomSeparator() {
            JsonNode obj = JsonNode.object()
                    .put("a", JsonNode.object().put("b", 1));

            Map<String, JsonNode> result = JsonFlattener.flatten(obj, "/");

            assertThat(result).hasSize(1);
            assertThat(result.get("a/b").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("空对象被保留")
        void testEmptyObjectPreserved() {
            JsonNode obj = JsonNode.object()
                    .put("a", JsonNode.object())
                    .put("b", 1);

            Map<String, JsonNode> result = JsonFlattener.flatten(obj);

            assertThat(result).hasSize(2);
            assertThat(result.get("a").isObject()).isTrue();
            assertThat(result.get("a").size()).isEqualTo(0);
            assertThat(result.get("b").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("空数组被保留")
        void testEmptyArrayPreserved() {
            JsonNode obj = JsonNode.object()
                    .put("items", JsonNode.array())
                    .put("count", 0);

            Map<String, JsonNode> result = JsonFlattener.flatten(obj);

            assertThat(result).hasSize(2);
            assertThat(result.get("items").isArray()).isTrue();
            assertThat(result.get("items").size()).isEqualTo(0);
            assertThat(result.get("count").asInt()).isEqualTo(0);
        }

        @Test
        @DisplayName("null 值被保留")
        void testNullValuesPreserved() {
            JsonNode obj = JsonNode.object()
                    .put("a", JsonNode.nullNode())
                    .put("b", JsonNode.object().put("c", JsonNode.nullNode()));

            Map<String, JsonNode> result = JsonFlattener.flatten(obj);

            assertThat(result).hasSize(2);
            assertThat(result.get("a").isNull()).isTrue();
            assertThat(result.get("b.c").isNull()).isTrue();
        }

        @Test
        @DisplayName("超过最大深度抛出异常")
        void testMaxDepthExceeded() {
            // Build deeply nested structure
            JsonNode.ObjectNode current = JsonNode.object();
            JsonNode root = current;
            for (int i = 0; i < 5; i++) {
                JsonNode.ObjectNode child = JsonNode.object();
                current.put("level", child);
                current = child;
            }
            current.put("value", 42);

            JsonFlattener.FlattenConfig config = new JsonFlattener.FlattenConfig(".", true, 3);

            assertThatThrownBy(() -> JsonFlattener.flatten(root, config))
                    .isInstanceOf(OpenJsonProcessingException.class)
                    .hasMessageContaining("Maximum flatten depth exceeded");
        }

        @Test
        @DisplayName("null 输入抛出 NullPointerException")
        void testNullInputThrows() {
            assertThatThrownBy(() -> JsonFlattener.flatten(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("混合嵌套结构正确扁平化")
        void testMixedNestedStructures() {
            JsonNode obj = JsonNode.object()
                    .put("user", JsonNode.object()
                            .put("name", "Alice")
                            .put("tags", JsonNode.array().add("admin").add("dev")))
                    .put("scores", JsonNode.array()
                            .add(JsonNode.object().put("subject", "math").put("grade", 95)));

            Map<String, JsonNode> result = JsonFlattener.flatten(obj);

            assertThat(result).hasSize(5);
            assertThat(result.get("user.name").asString()).isEqualTo("Alice");
            assertThat(result.get("user.tags[0]").asString()).isEqualTo("admin");
            assertThat(result.get("user.tags[1]").asString()).isEqualTo("dev");
            assertThat(result.get("scores[0].subject").asString()).isEqualTo("math");
            assertThat(result.get("scores[0].grade").asInt()).isEqualTo(95);
        }

        @Test
        @DisplayName("完整示例: 对象+数组混合")
        void testFullExample() {
            JsonNode obj = JsonNode.object()
                    .put("a", JsonNode.object().put("b", 1))
                    .put("c", JsonNode.array().add(2).add(3));

            Map<String, JsonNode> result = JsonFlattener.flatten(obj);

            assertThat(result).hasSize(3);
            assertThat(result.get("a.b").asInt()).isEqualTo(1);
            assertThat(result.get("c[0]").asInt()).isEqualTo(2);
            assertThat(result.get("c[1]").asInt()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("unflatten 方法测试")
    class UnflattenTests {

        @Test
        @DisplayName("unflatten 反转 flatten 操作")
        void testUnflattenReversesFlatten() {
            JsonNode original = JsonNode.object()
                    .put("a", JsonNode.object().put("b", 1))
                    .put("c", JsonNode.array().add(2).add(3));

            Map<String, JsonNode> flat = JsonFlattener.flatten(original);
            JsonNode restored = JsonFlattener.unflatten(flat);

            assertThat(restored.isObject()).isTrue();
            assertThat(restored.get("a").isObject()).isTrue();
            assertThat(restored.get("a").get("b").asInt()).isEqualTo(1);
            assertThat(restored.get("c").isArray()).isTrue();
            assertThat(restored.get("c").get(0).asInt()).isEqualTo(2);
            assertThat(restored.get("c").get(1).asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("unflatten 扁平对象")
        void testUnflattenFlatObject() {
            Map<String, JsonNode> map = new LinkedHashMap<>();
            map.put("name", JsonNode.of("John"));
            map.put("age", JsonNode.of(30));

            JsonNode result = JsonFlattener.unflatten(map);

            assertThat(result.isObject()).isTrue();
            assertThat(result.get("name").asString()).isEqualTo("John");
            assertThat(result.get("age").asInt()).isEqualTo(30);
        }

        @Test
        @DisplayName("unflatten 嵌套路径")
        void testUnflattenNestedPaths() {
            Map<String, JsonNode> map = new LinkedHashMap<>();
            map.put("a.b.c", JsonNode.of(42));
            map.put("a.b.d", JsonNode.of("hello"));

            JsonNode result = JsonFlattener.unflatten(map);

            assertThat(result.get("a").get("b").get("c").asInt()).isEqualTo(42);
            assertThat(result.get("a").get("b").get("d").asString()).isEqualTo("hello");
        }

        @Test
        @DisplayName("unflatten 带方括号数组索引")
        void testUnflattenWithBracketArrayIndex() {
            Map<String, JsonNode> map = new LinkedHashMap<>();
            map.put("items[0]", JsonNode.of("a"));
            map.put("items[1]", JsonNode.of("b"));

            JsonNode result = JsonFlattener.unflatten(map);

            assertThat(result.get("items").isArray()).isTrue();
            assertThat(result.get("items").get(0).asString()).isEqualTo("a");
            assertThat(result.get("items").get(1).asString()).isEqualTo("b");
        }

        @Test
        @DisplayName("unflatten 自定义分隔符")
        void testUnflattenCustomSeparator() {
            Map<String, JsonNode> map = new LinkedHashMap<>();
            map.put("a/b", JsonNode.of(1));
            map.put("a/c", JsonNode.of(2));

            JsonNode result = JsonFlattener.unflatten(map, "/");

            assertThat(result.get("a").get("b").asInt()).isEqualTo(1);
            assertThat(result.get("a").get("c").asInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("unflatten 空映射返回空对象")
        void testUnflattenEmptyMap() {
            Map<String, JsonNode> map = new LinkedHashMap<>();

            JsonNode result = JsonFlattener.unflatten(map);

            assertThat(result.isObject()).isTrue();
            assertThat(result.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("unflatten null 输入抛出 NullPointerException")
        void testUnflattenNullInput() {
            assertThatThrownBy(() -> JsonFlattener.unflatten(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("unflatten 保留 null 值")
        void testUnflattenPreservesNullValues() {
            Map<String, JsonNode> map = new LinkedHashMap<>();
            map.put("a.b", JsonNode.nullNode());
            map.put("a.c", JsonNode.of(1));

            JsonNode result = JsonFlattener.unflatten(map);

            assertThat(result.get("a").get("b").isNull()).isTrue();
            assertThat(result.get("a").get("c").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("flatten + unflatten 使用点表示法数组的往返一致性")
        void testRoundTripWithDotNotation() {
            JsonNode original = JsonNode.object()
                    .put("list", JsonNode.array().add(10).add(20).add(30));

            JsonFlattener.FlattenConfig config = new JsonFlattener.FlattenConfig(".", false, 1000);
            Map<String, JsonNode> flat = JsonFlattener.flatten(original, config);

            // Dot notation: "list.0", "list.1", "list.2"
            assertThat(flat.get("list.0").asInt()).isEqualTo(10);
            assertThat(flat.get("list.1").asInt()).isEqualTo(20);
            assertThat(flat.get("list.2").asInt()).isEqualTo(30);
        }

        @Test
        @DisplayName("unflatten 混合对象和数组嵌套")
        void testUnflattenMixedNesting() {
            Map<String, JsonNode> map = new LinkedHashMap<>();
            map.put("users[0].name", JsonNode.of("Alice"));
            map.put("users[0].age", JsonNode.of(30));
            map.put("users[1].name", JsonNode.of("Bob"));
            map.put("users[1].age", JsonNode.of(25));

            JsonNode result = JsonFlattener.unflatten(map);

            assertThat(result.get("users").isArray()).isTrue();
            assertThat(result.get("users").size()).isEqualTo(2);
            assertThat(result.get("users").get(0).get("name").asString()).isEqualTo("Alice");
            assertThat(result.get("users").get(0).get("age").asInt()).isEqualTo(30);
            assertThat(result.get("users").get(1).get("name").asString()).isEqualTo("Bob");
            assertThat(result.get("users").get(1).get("age").asInt()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("FlattenConfig 测试")
    class FlattenConfigTests {

        @Test
        @DisplayName("默认配置")
        void testDefaultConfig() {
            JsonFlattener.FlattenConfig config = JsonFlattener.FlattenConfig.DEFAULT;

            assertThat(config.separator()).isEqualTo(".");
            assertThat(config.bracketArrayNotation()).isTrue();
            assertThat(config.maxDepth()).isEqualTo(1000);
        }

        @Test
        @DisplayName("null 分隔符抛出异常")
        void testNullSeparator() {
            assertThatThrownBy(() -> new JsonFlattener.FlattenConfig(null, true, 1000))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空分隔符抛出异常")
        void testEmptySeparator() {
            assertThatThrownBy(() -> new JsonFlattener.FlattenConfig("", true, 1000))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("非正 maxDepth 抛出异常")
        void testNonPositiveMaxDepth() {
            assertThatThrownBy(() -> new JsonFlattener.FlattenConfig(".", true, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }

    @Nested
    @DisplayName("分隔符冲突保护测试")
    class SeparatorCollisionTests {

        @Test
        @DisplayName("键包含分隔符抛出异常")
        void keyContainingSeparatorThrows() {
            JsonNode node = JsonNode.object()
                    .put("a.b", JsonNode.of(1));
            assertThatThrownBy(() -> JsonFlattener.flatten(node))
                    .isInstanceOf(OpenJsonProcessingException.class)
                    .hasMessageContaining("separator");
        }

        @Test
        @DisplayName("键包含分隔符但使用不同分隔符成功")
        void keyContainingDotWithDifferentSeparator() {
            JsonNode node = JsonNode.object()
                    .put("a.b", JsonNode.of(1));
            var result = JsonFlattener.flatten(node, "/");
            assertThat(result).containsKey("a.b");
            assertThat(result.get("a.b")).isEqualTo(JsonNode.of(1));
        }

        @Test
        @DisplayName("键包含方括号抛出异常")
        void keyContainingBracketThrows() {
            JsonNode node = JsonNode.object()
                    .put("a[0]", JsonNode.of(1));
            assertThatThrownBy(() -> JsonFlattener.flatten(node))
                    .isInstanceOf(OpenJsonProcessingException.class)
                    .hasMessageContaining("[");
        }
    }
}
