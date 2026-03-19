package cloud.opencode.base.hash.consistent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * HashNode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("HashNode 测试")
class HashNodeTest {

    @Nested
    @DisplayName("of工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("创建带默认权重的节点")
        void testOfWithDefaultWeight() {
            HashNode<String> node = HashNode.of("node1", "data1");

            assertThat(node.id()).isEqualTo("node1");
            assertThat(node.data()).isEqualTo("data1");
            assertThat(node.weight()).isEqualTo(1);
        }

        @Test
        @DisplayName("创建带自定义权重的节点")
        void testOfWithCustomWeight() {
            HashNode<String> node = HashNode.of("node1", "data1", 5);

            assertThat(node.id()).isEqualTo("node1");
            assertThat(node.data()).isEqualTo("data1");
            assertThat(node.weight()).isEqualTo(5);
        }

        @Test
        @DisplayName("零权重抛出异常")
        void testZeroWeight() {
            assertThatThrownBy(() -> HashNode.of("node1", "data1", 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负权重抛出异常")
        void testNegativeWeight() {
            assertThatThrownBy(() -> HashNode.of("node1", "data1", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("id方法测试")
    class IdTests {

        @Test
        @DisplayName("获取节点ID")
        void testGetId() {
            HashNode<String> node = HashNode.of("myNodeId", "data");

            assertThat(node.id()).isEqualTo("myNodeId");
        }
    }

    @Nested
    @DisplayName("data方法测试")
    class DataTests {

        @Test
        @DisplayName("获取节点数据")
        void testGetData() {
            HashNode<String> node = HashNode.of("node1", "myData");

            assertThat(node.data()).isEqualTo("myData");
        }

        @Test
        @DisplayName("数据可以是任意类型")
        void testGenericData() {
            HashNode<Integer> intNode = HashNode.of("node1", 42);
            HashNode<Double> doubleNode = HashNode.of("node2", 3.14);

            assertThat(intNode.data()).isEqualTo(42);
            assertThat(doubleNode.data()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("数据可以是复杂对象")
        void testComplexData() {
            record ServerInfo(String host, int port) {}
            HashNode<ServerInfo> node = HashNode.of("server1", new ServerInfo("localhost", 8080));

            assertThat(node.data().host()).isEqualTo("localhost");
            assertThat(node.data().port()).isEqualTo(8080);
        }
    }

    @Nested
    @DisplayName("weight方法测试")
    class WeightTests {

        @Test
        @DisplayName("获取默认权重")
        void testDefaultWeight() {
            HashNode<String> node = HashNode.of("node1", "data");

            assertThat(node.weight()).isEqualTo(1);
        }

        @Test
        @DisplayName("获取自定义权重")
        void testCustomWeight() {
            HashNode<String> node = HashNode.of("node1", "data", 10);

            assertThat(node.weight()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Record equals和hashCode测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同字段的节点相等")
        void testEquals() {
            HashNode<String> node1 = HashNode.of("node1", "data1", 2);
            HashNode<String> node2 = HashNode.of("node1", "data1", 2);

            assertThat(node1).isEqualTo(node2);
        }

        @Test
        @DisplayName("不同ID的节点不相等")
        void testNotEqualsDifferentId() {
            HashNode<String> node1 = HashNode.of("node1", "data1");
            HashNode<String> node2 = HashNode.of("node2", "data1");

            assertThat(node1).isNotEqualTo(node2);
        }

        @Test
        @DisplayName("不同数据的节点不相等")
        void testNotEqualsDifferentData() {
            HashNode<String> node1 = HashNode.of("node1", "data1");
            HashNode<String> node2 = HashNode.of("node1", "data2");

            assertThat(node1).isNotEqualTo(node2);
        }

        @Test
        @DisplayName("不同权重的节点不相等")
        void testNotEqualsDifferentWeight() {
            HashNode<String> node1 = HashNode.of("node1", "data1", 1);
            HashNode<String> node2 = HashNode.of("node1", "data1", 2);

            assertThat(node1).isNotEqualTo(node2);
        }

        @Test
        @DisplayName("hashCode一致性")
        void testHashCodeConsistency() {
            HashNode<String> node1 = HashNode.of("node1", "data1", 2);
            HashNode<String> node2 = HashNode.of("node1", "data1", 2);

            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含所有字段")
        void testToString() {
            HashNode<String> node = HashNode.of("node1", "data1", 3);

            String str = node.toString();

            assertThat(str).contains("node1");
            assertThat(str).contains("data1");
            assertThat(str).contains("3");
        }
    }

    @Nested
    @DisplayName("null值测试")
    class NullTests {

        @Test
        @DisplayName("null ID抛出异常")
        void testNullId() {
            assertThatThrownBy(() -> HashNode.of(null, "data"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null数据可以接受")
        void testNullData() {
            HashNode<String> node = HashNode.of("node1", null);

            assertThat(node.data()).isNull();
        }
    }

    @Nested
    @DisplayName("作为Map键测试")
    class MapKeyTests {

        @Test
        @DisplayName("可以作为Map的键")
        void testAsMapKey() {
            java.util.Map<HashNode<String>, String> map = new java.util.HashMap<>();
            HashNode<String> node = HashNode.of("node1", "data1");

            map.put(node, "value");

            assertThat(map.get(node)).isEqualTo("value");
        }

        @Test
        @DisplayName("相等的节点作为键可以检索")
        void testEqualNodesAsKey() {
            java.util.Map<HashNode<String>, String> map = new java.util.HashMap<>();
            HashNode<String> node1 = HashNode.of("node1", "data1");
            HashNode<String> node2 = HashNode.of("node1", "data1");

            map.put(node1, "value");

            assertThat(map.get(node2)).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("作为Set元素测试")
    class SetElementTests {

        @Test
        @DisplayName("可以作为Set的元素")
        void testAsSetElement() {
            java.util.Set<HashNode<String>> set = new java.util.HashSet<>();
            HashNode<String> node1 = HashNode.of("node1", "data1");
            HashNode<String> node2 = HashNode.of("node1", "data1");

            set.add(node1);
            set.add(node2);

            assertThat(set).hasSize(1);
        }
    }
}
