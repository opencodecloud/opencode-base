package cloud.opencode.base.hash.consistent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * VirtualNode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("VirtualNode 测试")
class VirtualNodeTest {

    @Nested
    @DisplayName("of工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("创建虚拟节点")
        void testOf() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> virtual = VirtualNode.of(physical, 0, 12345L);

            assertThat(virtual).isNotNull();
            assertThat(virtual.physicalNode()).isEqualTo(physical);
            assertThat(virtual.replicaIndex()).isEqualTo(0);
            assertThat(virtual.hashValue()).isEqualTo(12345L);
        }

        @Test
        @DisplayName("创建多个副本")
        void testMultipleReplicas() {
            HashNode<String> physical = HashNode.of("node1", "data1");

            VirtualNode<String> v0 = VirtualNode.of(physical, 0, 100L);
            VirtualNode<String> v1 = VirtualNode.of(physical, 1, 200L);
            VirtualNode<String> v2 = VirtualNode.of(physical, 2, 300L);

            assertThat(v0.replicaIndex()).isEqualTo(0);
            assertThat(v1.replicaIndex()).isEqualTo(1);
            assertThat(v2.replicaIndex()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("physicalNode方法测试")
    class PhysicalNodeTests {

        @Test
        @DisplayName("获取物理节点")
        void testGetPhysicalNode() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> virtual = VirtualNode.of(physical, 0, 100L);

            assertThat(virtual.physicalNode()).isEqualTo(physical);
        }
    }

    @Nested
    @DisplayName("physicalNodeId方法测试")
    class PhysicalNodeIdTests {

        @Test
        @DisplayName("获取物理节点ID")
        void testGetPhysicalNodeId() {
            HashNode<String> physical = HashNode.of("server1", "data1");
            VirtualNode<String> virtual = VirtualNode.of(physical, 0, 100L);

            assertThat(virtual.physicalNodeId()).isEqualTo("server1");
        }
    }

    @Nested
    @DisplayName("data方法测试")
    class DataTests {

        @Test
        @DisplayName("获取数据")
        void testGetData() {
            HashNode<String> physical = HashNode.of("node1", "myData");
            VirtualNode<String> virtual = VirtualNode.of(physical, 0, 100L);

            assertThat(virtual.data()).isEqualTo("myData");
        }

        @Test
        @DisplayName("获取复杂数据类型")
        void testGetComplexData() {
            record ServerInfo(String host, int port) {}
            HashNode<ServerInfo> physical = HashNode.of("server1", new ServerInfo("localhost", 8080));
            VirtualNode<ServerInfo> virtual = VirtualNode.of(physical, 0, 100L);

            assertThat(virtual.data().host()).isEqualTo("localhost");
            assertThat(virtual.data().port()).isEqualTo(8080);
        }
    }

    @Nested
    @DisplayName("replicaIndex方法测试")
    class ReplicaIndexTests {

        @Test
        @DisplayName("获取副本索引")
        void testGetReplicaIndex() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> virtual = VirtualNode.of(physical, 5, 100L);

            assertThat(virtual.replicaIndex()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("hashValue方法测试")
    class HashValueTests {

        @Test
        @DisplayName("获取哈希值")
        void testGetHashValue() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> virtual = VirtualNode.of(physical, 0, 999999L);

            assertThat(virtual.hashValue()).isEqualTo(999999L);
        }

        @Test
        @DisplayName("负哈希值")
        void testNegativeHashValue() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> virtual = VirtualNode.of(physical, 0, -123456L);

            assertThat(virtual.hashValue()).isEqualTo(-123456L);
        }
    }

    @Nested
    @DisplayName("getKey方法测试")
    class GetKeyTests {

        @Test
        @DisplayName("获取虚拟节点键")
        void testGetKey() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> virtual = VirtualNode.of(physical, 3, 100L);

            String key = virtual.getKey();

            assertThat(key).contains("node1");
            assertThat(key).contains("3");
        }

        @Test
        @DisplayName("不同副本有不同键")
        void testDifferentKeysForReplicas() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> v0 = VirtualNode.of(physical, 0, 100L);
            VirtualNode<String> v1 = VirtualNode.of(physical, 1, 200L);

            assertThat(v0.getKey()).isNotEqualTo(v1.getKey());
        }
    }

    @Nested
    @DisplayName("Record equals和hashCode测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同字段的虚拟节点相等")
        void testEquals() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> v1 = VirtualNode.of(physical, 0, 100L);
            VirtualNode<String> v2 = VirtualNode.of(physical, 0, 100L);

            assertThat(v1).isEqualTo(v2);
        }

        @Test
        @DisplayName("不同副本索引不相等")
        void testNotEqualsDifferentIndex() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> v1 = VirtualNode.of(physical, 0, 100L);
            VirtualNode<String> v2 = VirtualNode.of(physical, 1, 100L);

            assertThat(v1).isNotEqualTo(v2);
        }

        @Test
        @DisplayName("不同哈希值不相等")
        void testNotEqualsDifferentHash() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> v1 = VirtualNode.of(physical, 0, 100L);
            VirtualNode<String> v2 = VirtualNode.of(physical, 0, 200L);

            assertThat(v1).isNotEqualTo(v2);
        }

        @Test
        @DisplayName("hashCode一致性")
        void testHashCodeConsistency() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> v1 = VirtualNode.of(physical, 0, 100L);
            VirtualNode<String> v2 = VirtualNode.of(physical, 0, 100L);

            assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含关键信息")
        void testToString() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> virtual = VirtualNode.of(physical, 2, 12345L);

            String str = virtual.toString();

            assertThat(str).contains("node1");
        }
    }

    @Nested
    @DisplayName("排序测试")
    class SortingTests {

        @Test
        @DisplayName("可以按哈希值排序")
        void testSortByHashValue() {
            HashNode<String> physical = HashNode.of("node1", "data1");
            VirtualNode<String> v1 = VirtualNode.of(physical, 0, 300L);
            VirtualNode<String> v2 = VirtualNode.of(physical, 1, 100L);
            VirtualNode<String> v3 = VirtualNode.of(physical, 2, 200L);

            java.util.List<VirtualNode<String>> list = new java.util.ArrayList<>();
            list.add(v1);
            list.add(v2);
            list.add(v3);

            list.sort(java.util.Comparator.comparingLong(VirtualNode::hashValue));

            assertThat(list.get(0).hashValue()).isEqualTo(100L);
            assertThat(list.get(1).hashValue()).isEqualTo(200L);
            assertThat(list.get(2).hashValue()).isEqualTo(300L);
        }
    }

    @Nested
    @DisplayName("多物理节点测试")
    class MultiplePhysicalNodesTests {

        @Test
        @DisplayName("不同物理节点的虚拟节点")
        void testDifferentPhysicalNodes() {
            HashNode<String> physical1 = HashNode.of("node1", "data1");
            HashNode<String> physical2 = HashNode.of("node2", "data2");

            VirtualNode<String> v1 = VirtualNode.of(physical1, 0, 100L);
            VirtualNode<String> v2 = VirtualNode.of(physical2, 0, 200L);

            assertThat(v1.physicalNodeId()).isEqualTo("node1");
            assertThat(v2.physicalNodeId()).isEqualTo("node2");
            assertThat(v1).isNotEqualTo(v2);
        }
    }
}
