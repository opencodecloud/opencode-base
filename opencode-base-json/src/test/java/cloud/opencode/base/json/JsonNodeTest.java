package cloud.opencode.base.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonNode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonNode 测试")
class JsonNodeTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("nullNode创建空节点")
        void testNullNode() {
            JsonNode node = JsonNode.nullNode();

            assertThat(node).isNotNull();
            assertThat(node.isNull()).isTrue();
            assertThat(node).isInstanceOf(JsonNode.NullNode.class);
        }

        @Test
        @DisplayName("nullNode是单例")
        void testNullNodeSingleton() {
            JsonNode node1 = JsonNode.nullNode();
            JsonNode node2 = JsonNode.nullNode();

            assertThat(node1).isSameAs(node2);
        }

        @Test
        @DisplayName("of(Object)从null创建NullNode")
        void testOfNull() {
            JsonNode node = JsonNode.of((Object) null);

            assertThat(node.isNull()).isTrue();
        }

        @Test
        @DisplayName("of(Object)从JsonNode返回自身")
        void testOfJsonNode() {
            JsonNode original = JsonNode.of("test");
            JsonNode result = JsonNode.of(original);

            assertThat(result).isSameAs(original);
        }

        @Test
        @DisplayName("of(Object)从String创建StringNode")
        void testOfString() {
            JsonNode node = JsonNode.of((Object) "test");

            assertThat(node.isString()).isTrue();
            assertThat(node.asString()).isEqualTo("test");
        }

        @Test
        @DisplayName("of(Object)从Number创建NumberNode")
        void testOfNumber() {
            JsonNode node = JsonNode.of((Object) 42);

            assertThat(node.isNumber()).isTrue();
            assertThat(node.asInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("of(Object)从Boolean创建BooleanNode")
        void testOfBoolean() {
            JsonNode node = JsonNode.of((Object) true);

            assertThat(node.isBoolean()).isTrue();
            assertThat(node.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("of(Object)从不支持类型抛出异常")
        void testOfUnsupportedType() {
            assertThatThrownBy(() -> JsonNode.of(new Object()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create JsonNode");
        }

        @Test
        @DisplayName("of(String)创建StringNode")
        void testOfStringDirect() {
            JsonNode node = JsonNode.of("hello");

            assertThat(node.isString()).isTrue();
            assertThat(node.asString()).isEqualTo("hello");
        }

        @Test
        @DisplayName("of(String)null创建NullNode")
        void testOfStringNull() {
            JsonNode node = JsonNode.of((String) null);

            assertThat(node.isNull()).isTrue();
        }

        @Test
        @DisplayName("of(Number)创建NumberNode")
        void testOfNumberDirect() {
            JsonNode node = JsonNode.of(3.14);

            assertThat(node.isNumber()).isTrue();
            assertThat(node.asDouble()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("of(Number)null创建NullNode")
        void testOfNumberNull() {
            JsonNode node = JsonNode.of((Number) null);

            assertThat(node.isNull()).isTrue();
        }

        @Test
        @DisplayName("of(boolean)创建BooleanNode")
        void testOfBooleanPrimitive() {
            JsonNode nodeTrue = JsonNode.of(true);
            JsonNode nodeFalse = JsonNode.of(false);

            assertThat(nodeTrue.isBoolean()).isTrue();
            assertThat(nodeTrue.asBoolean()).isTrue();
            assertThat(nodeFalse.isBoolean()).isTrue();
            assertThat(nodeFalse.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("object()创建空ObjectNode")
        void testObject() {
            JsonNode.ObjectNode node = JsonNode.object();

            assertThat(node).isNotNull();
            assertThat(node.isObject()).isTrue();
            assertThat(node.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("array()创建空ArrayNode")
        void testArray() {
            JsonNode.ArrayNode node = JsonNode.array();

            assertThat(node).isNotNull();
            assertThat(node.isArray()).isTrue();
            assertThat(node.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("类型检查测试")
    class TypeCheckTests {

        @Test
        @DisplayName("默认类型检查返回false")
        void testDefaultTypeChecks() {
            // 使用NullNode测试默认实现
            JsonNode node = JsonNode.nullNode();

            assertThat(node.isObject()).isFalse();
            assertThat(node.isArray()).isFalse();
            assertThat(node.isString()).isFalse();
            assertThat(node.isNumber()).isFalse();
            assertThat(node.isBoolean()).isFalse();
        }

        @Test
        @DisplayName("isValue检查值类型")
        void testIsValue() {
            assertThat(JsonNode.of("test").isValue()).isTrue();
            assertThat(JsonNode.of(42).isValue()).isTrue();
            assertThat(JsonNode.of(true).isValue()).isTrue();
            assertThat(JsonNode.nullNode().isValue()).isTrue();
            assertThat(JsonNode.object().isValue()).isFalse();
            assertThat(JsonNode.array().isValue()).isFalse();
        }

        @Test
        @DisplayName("isContainer检查容器类型")
        void testIsContainer() {
            assertThat(JsonNode.object().isContainer()).isTrue();
            assertThat(JsonNode.array().isContainer()).isTrue();
            assertThat(JsonNode.of("test").isContainer()).isFalse();
            assertThat(JsonNode.of(42).isContainer()).isFalse();
            assertThat(JsonNode.nullNode().isContainer()).isFalse();
        }
    }

    @Nested
    @DisplayName("默认值访问测试")
    class DefaultValueAccessTests {

        @Test
        @DisplayName("默认asString返回null")
        void testDefaultAsString() {
            assertThat(JsonNode.nullNode().asString()).isNull();
        }

        @Test
        @DisplayName("默认asInt返回默认值")
        void testDefaultAsInt() {
            assertThat(JsonNode.nullNode().asInt()).isEqualTo(0);
            assertThat(JsonNode.nullNode().asInt(99)).isEqualTo(99);
        }

        @Test
        @DisplayName("默认asLong返回默认值")
        void testDefaultAsLong() {
            assertThat(JsonNode.nullNode().asLong()).isEqualTo(0L);
            assertThat(JsonNode.nullNode().asLong(99L)).isEqualTo(99L);
        }

        @Test
        @DisplayName("默认asDouble返回默认值")
        void testDefaultAsDouble() {
            assertThat(JsonNode.nullNode().asDouble()).isEqualTo(0.0);
            assertThat(JsonNode.nullNode().asDouble(1.5)).isEqualTo(1.5);
        }

        @Test
        @DisplayName("默认asBoolean返回默认值")
        void testDefaultAsBoolean() {
            assertThat(JsonNode.nullNode().asBoolean()).isFalse();
            assertThat(JsonNode.nullNode().asBoolean(true)).isTrue();
        }

        @Test
        @DisplayName("默认asBigDecimal返回null")
        void testDefaultAsBigDecimal() {
            assertThat(JsonNode.nullNode().asBigDecimal()).isNull();
        }

        @Test
        @DisplayName("默认asBigInteger返回null")
        void testDefaultAsBigInteger() {
            assertThat(JsonNode.nullNode().asBigInteger()).isNull();
        }
    }

    @Nested
    @DisplayName("默认对象/数组访问测试")
    class DefaultObjectArrayAccessTests {

        @Test
        @DisplayName("默认get(String)返回null")
        void testDefaultGetByKey() {
            assertThat(JsonNode.nullNode().get("key")).isNull();
        }

        @Test
        @DisplayName("默认get(int)返回null")
        void testDefaultGetByIndex() {
            assertThat(JsonNode.nullNode().get(0)).isNull();
        }

        @Test
        @DisplayName("默认has返回false")
        void testDefaultHas() {
            assertThat(JsonNode.nullNode().has("key")).isFalse();
        }

        @Test
        @DisplayName("默认keys返回空集")
        void testDefaultKeys() {
            assertThat(JsonNode.nullNode().keys()).isEmpty();
        }

        @Test
        @DisplayName("默认size返回0")
        void testDefaultSize() {
            assertThat(JsonNode.nullNode().size()).isEqualTo(0);
        }

        @Test
        @DisplayName("默认isEmpty返回true")
        void testDefaultIsEmpty() {
            assertThat(JsonNode.nullNode().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("ObjectNode测试")
    class ObjectNodeTests {

        @Test
        @DisplayName("isObject返回true")
        void testIsObject() {
            JsonNode.ObjectNode node = JsonNode.object();

            assertThat(node.isObject()).isTrue();
        }

        @Test
        @DisplayName("put和get操作")
        void testPutAndGet() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.put("name", "John");

            assertThat(node.get("name").asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("put(JsonNode)操作")
        void testPutJsonNode() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.put("child", JsonNode.object());

            assertThat(node.get("child").isObject()).isTrue();
        }

        @Test
        @DisplayName("put(null)存储NullNode")
        void testPutNull() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.put("key", (JsonNode) null);

            assertThat(node.get("key").isNull()).isTrue();
        }

        @Test
        @DisplayName("put(String,Number)操作")
        void testPutNumber() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.put("value", 42);

            assertThat(node.get("value").asInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("put(String,boolean)操作")
        void testPutBoolean() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.put("active", true);

            assertThat(node.get("active").asBoolean()).isTrue();
        }

        @Test
        @DisplayName("putNull操作")
        void testPutNullMethod() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.putNull("empty");

            assertThat(node.get("empty").isNull()).isTrue();
        }

        @Test
        @DisplayName("putObject创建嵌套对象")
        void testPutObject() {
            JsonNode.ObjectNode node = JsonNode.object();
            JsonNode.ObjectNode child = node.putObject("child");
            child.put("name", "nested");

            assertThat(node.get("child").isObject()).isTrue();
            assertThat(node.get("child").get("name").asString()).isEqualTo("nested");
        }

        @Test
        @DisplayName("putArray创建嵌套数组")
        void testPutArray() {
            JsonNode.ObjectNode node = JsonNode.object();
            JsonNode.ArrayNode child = node.putArray("items");
            child.add("item1");

            assertThat(node.get("items").isArray()).isTrue();
            assertThat(node.get("items").get(0).asString()).isEqualTo("item1");
        }

        @Test
        @DisplayName("has检查属性存在")
        void testHas() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.put("key", "value");

            assertThat(node.has("key")).isTrue();
            assertThat(node.has("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("keys返回所有键")
        void testKeys() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.put("a", 1);
            node.put("b", 2);

            Set<String> keys = node.keys();

            assertThat(keys).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("keys返回不可修改集合")
        void testKeysUnmodifiable() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.put("key", "value");

            assertThatThrownBy(() -> node.keys().add("new"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("size返回属性数量")
        void testSize() {
            JsonNode.ObjectNode node = JsonNode.object();
            assertThat(node.size()).isEqualTo(0);

            node.put("a", 1);
            node.put("b", 2);

            assertThat(node.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty检查空对象")
        void testIsEmpty() {
            JsonNode.ObjectNode node = JsonNode.object();
            assertThat(node.isEmpty()).isTrue();

            node.put("key", "value");
            assertThat(node.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("remove删除属性")
        void testRemove() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.put("key", "value");
            node.remove("key");

            assertThat(node.has("key")).isFalse();
        }

        @Test
        @DisplayName("toMap转换为Map")
        void testToMap() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.put("a", 1);
            node.put("b", "test");

            Map<String, JsonNode> map = node.toMap();

            assertThat(map).hasSize(2);
            assertThat(map.get("a").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("equals比较")
        void testEquals() {
            JsonNode.ObjectNode node1 = JsonNode.object();
            node1.put("key", "value");

            JsonNode.ObjectNode node2 = JsonNode.object();
            node2.put("key", "value");

            JsonNode.ObjectNode node3 = JsonNode.object();
            node3.put("key", "different");

            assertThat(node1).isEqualTo(node2);
            assertThat(node1).isNotEqualTo(node3);
            assertThat(node1).isEqualTo(node1);
            assertThat(node1).isNotEqualTo(null);
            assertThat(node1).isNotEqualTo("string");
        }

        @Test
        @DisplayName("hashCode一致")
        void testHashCode() {
            JsonNode.ObjectNode node1 = JsonNode.object();
            node1.put("key", "value");

            JsonNode.ObjectNode node2 = JsonNode.object();
            node2.put("key", "value");

            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }

        @Test
        @DisplayName("toString返回字符串表示")
        void testToString() {
            JsonNode.ObjectNode node = JsonNode.object();
            node.put("key", "value");

            assertThat(node.toString()).contains("key");
        }

        @Test
        @DisplayName("方法链可用")
        void testMethodChaining() {
            JsonNode.ObjectNode node = JsonNode.object()
                .put("name", "John")
                .put("age", 30)
                .put("active", true);

            assertThat(node.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("ArrayNode测试")
    class ArrayNodeTests {

        @Test
        @DisplayName("isArray返回true")
        void testIsArray() {
            JsonNode.ArrayNode node = JsonNode.array();

            assertThat(node.isArray()).isTrue();
        }

        @Test
        @DisplayName("add和get操作")
        void testAddAndGet() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add("first");

            assertThat(node.get(0).asString()).isEqualTo("first");
        }

        @Test
        @DisplayName("add(JsonNode)操作")
        void testAddJsonNode() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add(JsonNode.of(42));

            assertThat(node.get(0).asInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("add(null)存储NullNode")
        void testAddNull() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add((JsonNode) null);

            assertThat(node.get(0).isNull()).isTrue();
        }

        @Test
        @DisplayName("add(Number)操作")
        void testAddNumber() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add(3.14);

            assertThat(node.get(0).asDouble()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("add(boolean)操作")
        void testAddBoolean() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add(true);

            assertThat(node.get(0).asBoolean()).isTrue();
        }

        @Test
        @DisplayName("addNull操作")
        void testAddNullMethod() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.addNull();

            assertThat(node.get(0).isNull()).isTrue();
        }

        @Test
        @DisplayName("addObject创建嵌套对象")
        void testAddObject() {
            JsonNode.ArrayNode node = JsonNode.array();
            JsonNode.ObjectNode child = node.addObject();
            child.put("name", "nested");

            assertThat(node.get(0).isObject()).isTrue();
            assertThat(node.get(0).get("name").asString()).isEqualTo("nested");
        }

        @Test
        @DisplayName("addArray创建嵌套数组")
        void testAddArray() {
            JsonNode.ArrayNode node = JsonNode.array();
            JsonNode.ArrayNode child = node.addArray();
            child.add("nested");

            assertThat(node.get(0).isArray()).isTrue();
            assertThat(node.get(0).get(0).asString()).isEqualTo("nested");
        }

        @Test
        @DisplayName("get越界返回null")
        void testGetOutOfBounds() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add("item");

            assertThat(node.get(-1)).isNull();
            assertThat(node.get(100)).isNull();
        }

        @Test
        @DisplayName("size返回元素数量")
        void testSize() {
            JsonNode.ArrayNode node = JsonNode.array();
            assertThat(node.size()).isEqualTo(0);

            node.add("a").add("b").add("c");

            assertThat(node.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty检查空数组")
        void testIsEmpty() {
            JsonNode.ArrayNode node = JsonNode.array();
            assertThat(node.isEmpty()).isTrue();

            node.add("item");
            assertThat(node.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("set更新元素")
        void testSet() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add("original");
            node.set(0, JsonNode.of("updated"));

            assertThat(node.get(0).asString()).isEqualTo("updated");
        }

        @Test
        @DisplayName("set(null)存储NullNode")
        void testSetNull() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add("value");
            node.set(0, null);

            assertThat(node.get(0).isNull()).isTrue();
        }

        @Test
        @DisplayName("remove删除元素")
        void testRemove() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add("a").add("b").add("c");
            node.remove(1);

            assertThat(node.size()).isEqualTo(2);
            assertThat(node.get(0).asString()).isEqualTo("a");
            assertThat(node.get(1).asString()).isEqualTo("c");
        }

        @Test
        @DisplayName("toList转换为List")
        void testToList() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add("a").add("b");

            List<JsonNode> list = node.toList();

            assertThat(list).hasSize(2);
            assertThat(list.get(0).asString()).isEqualTo("a");
        }

        @Test
        @DisplayName("iterator可迭代")
        void testIterator() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add("a").add("b").add("c");

            Iterator<JsonNode> it = node.iterator();
            int count = 0;
            while (it.hasNext()) {
                it.next();
                count++;
            }

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("for-each循环")
        void testForEach() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add("a").add("b").add("c");

            int count = 0;
            for (JsonNode item : node) {
                assertThat(item.isString()).isTrue();
                count++;
            }

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("equals比较")
        void testEquals() {
            JsonNode.ArrayNode node1 = JsonNode.array();
            node1.add("a").add("b");

            JsonNode.ArrayNode node2 = JsonNode.array();
            node2.add("a").add("b");

            JsonNode.ArrayNode node3 = JsonNode.array();
            node3.add("a").add("c");

            assertThat(node1).isEqualTo(node2);
            assertThat(node1).isNotEqualTo(node3);
            assertThat(node1).isEqualTo(node1);
            assertThat(node1).isNotEqualTo(null);
            assertThat(node1).isNotEqualTo("string");
        }

        @Test
        @DisplayName("hashCode一致")
        void testHashCode() {
            JsonNode.ArrayNode node1 = JsonNode.array();
            node1.add("a").add("b");

            JsonNode.ArrayNode node2 = JsonNode.array();
            node2.add("a").add("b");

            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }

        @Test
        @DisplayName("toString返回字符串表示")
        void testToString() {
            JsonNode.ArrayNode node = JsonNode.array();
            node.add("item");

            assertThat(node.toString()).contains("item");
        }

        @Test
        @DisplayName("方法链可用")
        void testMethodChaining() {
            JsonNode.ArrayNode node = JsonNode.array()
                .add("one")
                .add(2)
                .add(true);

            assertThat(node.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("StringNode测试")
    class StringNodeTests {

        @Test
        @DisplayName("isString返回true")
        void testIsString() {
            JsonNode node = JsonNode.of("test");

            assertThat(node.isString()).isTrue();
        }

        @Test
        @DisplayName("asString返回值")
        void testAsString() {
            JsonNode node = JsonNode.of("hello");

            assertThat(node.asString()).isEqualTo("hello");
        }

        @Test
        @DisplayName("asInt解析数字字符串")
        void testAsInt() {
            JsonNode node = JsonNode.of("42");

            assertThat(node.asInt()).isEqualTo(42);
            assertThat(node.asInt(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("asInt非数字返回默认值")
        void testAsIntInvalid() {
            JsonNode node = JsonNode.of("not a number");

            assertThat(node.asInt(99)).isEqualTo(99);
        }

        @Test
        @DisplayName("asLong解析数字字符串")
        void testAsLong() {
            JsonNode node = JsonNode.of("9999999999");

            assertThat(node.asLong()).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("asLong非数字返回默认值")
        void testAsLongInvalid() {
            JsonNode node = JsonNode.of("not a number");

            assertThat(node.asLong(99L)).isEqualTo(99L);
        }

        @Test
        @DisplayName("asDouble解析数字字符串")
        void testAsDouble() {
            JsonNode node = JsonNode.of("3.14");

            assertThat(node.asDouble()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("asDouble非数字返回默认值")
        void testAsDoubleInvalid() {
            JsonNode node = JsonNode.of("not a number");

            assertThat(node.asDouble(1.5)).isEqualTo(1.5);
        }

        @Test
        @DisplayName("asBoolean解析布尔字符串")
        void testAsBoolean() {
            JsonNode nodeTrue = JsonNode.of("true");
            JsonNode nodeFalse = JsonNode.of("false");
            JsonNode nodeOther = JsonNode.of("other");

            assertThat(nodeTrue.asBoolean()).isTrue();
            assertThat(nodeFalse.asBoolean()).isFalse();
            assertThat(nodeOther.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("record方法")
        void testRecordMethods() {
            JsonNode.StringNode node = new JsonNode.StringNode("test");

            assertThat(node.value()).isEqualTo("test");
            assertThat(node.toString()).contains("test");
            assertThat(node.hashCode()).isEqualTo(new JsonNode.StringNode("test").hashCode());
            assertThat(node.equals(new JsonNode.StringNode("test"))).isTrue();
        }
    }

    @Nested
    @DisplayName("NumberNode测试")
    class NumberNodeTests {

        @Test
        @DisplayName("isNumber返回true")
        void testIsNumber() {
            JsonNode node = JsonNode.of(42);

            assertThat(node.isNumber()).isTrue();
        }

        @Test
        @DisplayName("asString返回字符串")
        void testAsString() {
            JsonNode node = JsonNode.of(42);

            assertThat(node.asString()).isEqualTo("42");
        }

        @Test
        @DisplayName("asInt返回整数")
        void testAsInt() {
            JsonNode node = JsonNode.of(42);

            assertThat(node.asInt()).isEqualTo(42);
            assertThat(node.asInt(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("asLong返回长整数")
        void testAsLong() {
            JsonNode node = JsonNode.of(9999999999L);

            assertThat(node.asLong()).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("asDouble返回浮点数")
        void testAsDouble() {
            JsonNode node = JsonNode.of(3.14);

            assertThat(node.asDouble()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("asBigDecimal从各种类型转换")
        void testAsBigDecimal() {
            // From BigDecimal
            JsonNode node1 = JsonNode.of(new BigDecimal("123.456"));
            assertThat(node1.asBigDecimal()).isEqualTo(new BigDecimal("123.456"));

            // From BigInteger
            JsonNode node2 = JsonNode.of(new BigInteger("12345"));
            assertThat(node2.asBigDecimal()).isEqualTo(new BigDecimal("12345"));

            // From other number
            JsonNode node3 = JsonNode.of(42);
            assertThat(node3.asBigDecimal()).isEqualTo(new BigDecimal("42"));
        }

        @Test
        @DisplayName("asBigInteger从各种类型转换")
        void testAsBigInteger() {
            // From BigInteger
            JsonNode node1 = JsonNode.of(new BigInteger("12345"));
            assertThat(node1.asBigInteger()).isEqualTo(new BigInteger("12345"));

            // From BigDecimal
            JsonNode node2 = JsonNode.of(new BigDecimal("123.456"));
            assertThat(node2.asBigInteger()).isEqualTo(new BigInteger("123"));

            // From other number
            JsonNode node3 = JsonNode.of(42L);
            assertThat(node3.asBigInteger()).isEqualTo(BigInteger.valueOf(42));
        }

        @Test
        @DisplayName("record方法")
        void testRecordMethods() {
            JsonNode.NumberNode node = new JsonNode.NumberNode(42);

            assertThat(node.value()).isEqualTo(42);
            assertThat(node.toString()).contains("42");
            assertThat(node.hashCode()).isEqualTo(new JsonNode.NumberNode(42).hashCode());
            assertThat(node.equals(new JsonNode.NumberNode(42))).isTrue();
        }
    }

    @Nested
    @DisplayName("BooleanNode测试")
    class BooleanNodeTests {

        @Test
        @DisplayName("isBoolean返回true")
        void testIsBoolean() {
            JsonNode node = JsonNode.of(true);

            assertThat(node.isBoolean()).isTrue();
        }

        @Test
        @DisplayName("asString返回字符串")
        void testAsString() {
            assertThat(JsonNode.of(true).asString()).isEqualTo("true");
            assertThat(JsonNode.of(false).asString()).isEqualTo("false");
        }

        @Test
        @DisplayName("asBoolean返回布尔值")
        void testAsBoolean() {
            assertThat(JsonNode.of(true).asBoolean()).isTrue();
            assertThat(JsonNode.of(false).asBoolean()).isFalse();
            assertThat(JsonNode.of(true).asBoolean(false)).isTrue();
        }

        @Test
        @DisplayName("asInt返回0或1")
        void testAsInt() {
            assertThat(JsonNode.of(true).asInt()).isEqualTo(1);
            assertThat(JsonNode.of(false).asInt()).isEqualTo(0);
        }

        @Test
        @DisplayName("record方法")
        void testRecordMethods() {
            JsonNode.BooleanNode node = new JsonNode.BooleanNode(true);

            assertThat(node.value()).isTrue();
            assertThat(node.toString()).contains("true");
            assertThat(node.hashCode()).isEqualTo(new JsonNode.BooleanNode(true).hashCode());
            assertThat(node.equals(new JsonNode.BooleanNode(true))).isTrue();
        }
    }

    @Nested
    @DisplayName("NullNode测试")
    class NullNodeTests {

        @Test
        @DisplayName("isNull返回true")
        void testIsNull() {
            JsonNode node = JsonNode.nullNode();

            assertThat(node.isNull()).isTrue();
        }

        @Test
        @DisplayName("asString返回null")
        void testAsString() {
            JsonNode node = JsonNode.nullNode();

            assertThat(node.asString()).isNull();
        }

        @Test
        @DisplayName("INSTANCE是单例")
        void testInstance() {
            assertThat(JsonNode.NullNode.INSTANCE).isSameAs(JsonNode.nullNode());
        }

        @Test
        @DisplayName("record方法")
        void testRecordMethods() {
            JsonNode.NullNode node = new JsonNode.NullNode();

            assertThat(node.toString()).isNotNull();
            assertThat(node.hashCode()).isEqualTo(new JsonNode.NullNode().hashCode());
            assertThat(node.equals(new JsonNode.NullNode())).isTrue();
        }
    }

    @Nested
    @DisplayName("sealed接口测试")
    class SealedInterfaceTests {

        @Test
        @DisplayName("JsonNode是sealed接口")
        void testIsSealed() {
            assertThat(JsonNode.class.isSealed()).isTrue();
        }

        @Test
        @DisplayName("所有permit类存在")
        void testPermittedSubclasses() {
            Class<?>[] permitted = JsonNode.class.getPermittedSubclasses();

            assertThat(permitted).contains(
                JsonNode.ObjectNode.class,
                JsonNode.ArrayNode.class,
                JsonNode.StringNode.class,
                JsonNode.NumberNode.class,
                JsonNode.BooleanNode.class,
                JsonNode.NullNode.class
            );
        }
    }

    @Nested
    @DisplayName("综合测试")
    class IntegrationTests {

        @Test
        @DisplayName("构建复杂JSON结构")
        void testComplexStructure() {
            JsonNode.ObjectNode root = JsonNode.object()
                .put("name", "John Doe")
                .put("age", 30)
                .put("active", true);

            root.putNull("middleName");

            JsonNode.ObjectNode address = root.putObject("address");
            address.put("street", "123 Main St");
            address.put("city", "Springfield");

            JsonNode.ArrayNode tags = root.putArray("tags");
            tags.add("developer").add("designer").add("manager");

            // Verify structure
            assertThat(root.get("name").asString()).isEqualTo("John Doe");
            assertThat(root.get("age").asInt()).isEqualTo(30);
            assertThat(root.get("active").asBoolean()).isTrue();
            assertThat(root.get("middleName").isNull()).isTrue();
            assertThat(root.get("address").get("city").asString()).isEqualTo("Springfield");
            assertThat(root.get("tags").size()).isEqualTo(3);
            assertThat(root.get("tags").get(0).asString()).isEqualTo("developer");
        }

        @Test
        @DisplayName("嵌套数组结构")
        void testNestedArrays() {
            JsonNode.ArrayNode matrix = JsonNode.array();

            matrix.addArray().add(1).add(2).add(3);
            matrix.addArray().add(4).add(5).add(6);
            matrix.addArray().add(7).add(8).add(9);

            assertThat(matrix.size()).isEqualTo(3);
            assertThat(matrix.get(0).get(0).asInt()).isEqualTo(1);
            assertThat(matrix.get(1).get(1).asInt()).isEqualTo(5);
            assertThat(matrix.get(2).get(2).asInt()).isEqualTo(9);
        }
    }
}
