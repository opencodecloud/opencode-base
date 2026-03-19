package cloud.opencode.base.yml;

import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlNodeTest Tests
 * YmlNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlNode 接口测试")
class YmlNodeTest {

    @Nested
    @DisplayName("节点类型检查测试")
    class NodeTypeTests {

        @Test
        @DisplayName("标量节点isScalar返回true")
        void testIsScalar() {
            YmlNode node = DefaultYmlNode.of("hello");

            assertThat(node.isScalar()).isTrue();
            assertThat(node.isMapping()).isFalse();
            assertThat(node.isSequence()).isFalse();
            assertThat(node.isNull()).isFalse();
        }

        @Test
        @DisplayName("映射节点isMapping返回true")
        void testIsMapping() {
            Map<String, Object> map = new HashMap<>();
            map.put("key", "value");
            YmlNode node = DefaultYmlNode.of(map);

            assertThat(node.isMapping()).isTrue();
            assertThat(node.isScalar()).isFalse();
            assertThat(node.isSequence()).isFalse();
        }

        @Test
        @DisplayName("序列节点isSequence返回true")
        void testIsSequence() {
            YmlNode node = DefaultYmlNode.of(List.of("a", "b"));

            assertThat(node.isSequence()).isTrue();
            assertThat(node.isScalar()).isFalse();
            assertThat(node.isMapping()).isFalse();
        }

        @Test
        @DisplayName("null节点isNull返回true")
        void testIsNull() {
            YmlNode node = DefaultYmlNode.nullNode();

            assertThat(node.isNull()).isTrue();
            assertThat(node.isScalar()).isFalse();
        }

        @Test
        @DisplayName("getType返回正确的NodeType")
        void testGetType() {
            assertThat(DefaultYmlNode.of("text").getType()).isEqualTo(YmlNode.NodeType.SCALAR);
            assertThat(DefaultYmlNode.of(List.of(1)).getType()).isEqualTo(YmlNode.NodeType.SEQUENCE);
            assertThat(DefaultYmlNode.nullNode().getType()).isEqualTo(YmlNode.NodeType.NULL);
        }
    }

    @Nested
    @DisplayName("值访问测试")
    class ValueAccessTests {

        @Test
        @DisplayName("asText返回字符串值")
        void testAsText() {
            YmlNode node = DefaultYmlNode.of("hello");

            assertThat(node.asText()).isEqualTo("hello");
        }

        @Test
        @DisplayName("asText对null节点返回null")
        void testAsTextNull() {
            YmlNode node = DefaultYmlNode.nullNode();

            assertThat(node.asText()).isNull();
        }

        @Test
        @DisplayName("asText带默认值")
        void testAsTextWithDefault() {
            YmlNode nullNode = DefaultYmlNode.nullNode();

            assertThat(nullNode.asText("default")).isEqualTo("default");
        }

        @Test
        @DisplayName("asInt返回整数值")
        void testAsInt() {
            YmlNode node = DefaultYmlNode.of(42);

            assertThat(node.asInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("asLong返回长整数值")
        void testAsLong() {
            YmlNode node = DefaultYmlNode.of(100L);

            assertThat(node.asLong()).isEqualTo(100L);
        }

        @Test
        @DisplayName("asBoolean返回布尔值")
        void testAsBoolean() {
            YmlNode node = DefaultYmlNode.of(true);

            assertThat(node.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("asDouble返回双精度值")
        void testAsDouble() {
            YmlNode node = DefaultYmlNode.of(3.14);

            assertThat(node.asDouble()).isCloseTo(3.14, within(0.001));
        }
    }

    @Nested
    @DisplayName("子节点访问测试")
    class ChildAccessTests {

        @Test
        @DisplayName("get通过键获取子节点")
        void testGetByKey() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "test");
            YmlNode node = DefaultYmlNode.of(map);

            assertThat(node.get("name").asText()).isEqualTo("test");
        }

        @Test
        @DisplayName("get通过索引获取子节点")
        void testGetByIndex() {
            YmlNode node = DefaultYmlNode.of(List.of("a", "b", "c"));

            assertThat(node.get(0).asText()).isEqualTo("a");
            assertThat(node.get(2).asText()).isEqualTo("c");
        }

        @Test
        @DisplayName("has检查键是否存在")
        void testHas() {
            Map<String, Object> map = new HashMap<>();
            map.put("key", "value");
            YmlNode node = DefaultYmlNode.of(map);

            assertThat(node.has("key")).isTrue();
            assertThat(node.has("missing")).isFalse();
        }

        @Test
        @DisplayName("size返回正确大小")
        void testSize() {
            Map<String, Object> map = new HashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            YmlNode mapNode = DefaultYmlNode.of(map);
            YmlNode listNode = DefaultYmlNode.of(List.of(1, 2, 3));

            assertThat(mapNode.size()).isEqualTo(2);
            assertThat(listNode.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("keys返回映射节点的所有键")
        void testKeys() {
            Map<String, Object> map = new HashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            YmlNode node = DefaultYmlNode.of(map);

            assertThat(node.keys()).containsExactlyInAnyOrder("a", "b");
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("YmlNode.of创建节点")
        void testOfStaticMethod() {
            YmlNode node = YmlNode.of("test");

            assertThat(node.asText()).isEqualTo("test");
            assertThat(node.isScalar()).isTrue();
        }

        @Test
        @DisplayName("YmlNode.of传null创建null节点")
        void testOfNull() {
            YmlNode node = YmlNode.of(null);

            assertThat(node.isNull()).isTrue();
        }
    }
}
