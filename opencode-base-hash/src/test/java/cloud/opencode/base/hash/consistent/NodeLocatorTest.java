package cloud.opencode.base.hash.consistent;

import cloud.opencode.base.hash.OpenHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * NodeLocator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("NodeLocator 测试")
class NodeLocatorTest {

    @Nested
    @DisplayName("locate方法测试")
    class LocateTests {

        @Test
        @DisplayName("定位单个节点")
        void testLocate() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            long hashValue = OpenHash.murmur3_128().hashUtf8("key1").padToLong();
            String data = locator.locate(hashValue);

            assertThat(data).isIn("data1", "data2");
        }

        @Test
        @DisplayName("空定位器返回null")
        void testLocateEmpty() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash().build();

            long hashValue = OpenHash.murmur3_128().hashUtf8("key1").padToLong();
            String data = locator.locate(hashValue);

            assertThat(data).isNull();
        }

        @Test
        @DisplayName("一致性定位")
        void testConsistentLocate() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            long hashValue = OpenHash.murmur3_128().hashUtf8("mykey").padToLong();
            String data1 = locator.locate(hashValue);
            String data2 = locator.locate(hashValue);

            assertThat(data1).isEqualTo(data2);
        }
    }

    @Nested
    @DisplayName("locateAll方法测试")
    class LocateAllTests {

        @Test
        @DisplayName("定位多个节点")
        void testLocateAll() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .addNode("node3", "data3")
                    .build();

            long hashValue = OpenHash.murmur3_128().hashUtf8("key1").padToLong();
            List<String> nodes = locator.locateAll(hashValue, 2);

            assertThat(nodes).hasSize(2);
            assertThat(nodes.get(0)).isNotEqualTo(nodes.get(1));
        }

        @Test
        @DisplayName("请求超过可用节点数")
        void testLocateAllMoreThanAvailable() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            long hashValue = OpenHash.murmur3_128().hashUtf8("key1").padToLong();
            List<String> nodes = locator.locateAll(hashValue, 5);

            assertThat(nodes).hasSize(2);
        }

        @Test
        @DisplayName("空定位器返回空列表")
        void testLocateAllEmpty() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash().build();

            long hashValue = OpenHash.murmur3_128().hashUtf8("key1").padToLong();
            List<String> nodes = locator.locateAll(hashValue, 3);

            assertThat(nodes).isEmpty();
        }
    }

    @Nested
    @DisplayName("getVirtualNode方法测试")
    class GetVirtualNodeTests {

        @Test
        @DisplayName("获取虚拟节点")
        void testGetVirtualNode() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .virtualNodeCount(100)
                    .build();

            long hashValue = OpenHash.murmur3_128().hashUtf8("key1").padToLong();
            VirtualNode<String> vn = locator.getVirtualNode(hashValue);

            assertThat(vn).isNotNull();
            assertThat(vn.physicalNodeId()).isEqualTo("node1");
        }

        @Test
        @DisplayName("空定位器返回null")
        void testGetVirtualNodeEmpty() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash().build();

            long hashValue = OpenHash.murmur3_128().hashUtf8("key1").padToLong();
            VirtualNode<String> vn = locator.getVirtualNode(hashValue);

            assertThat(vn).isNull();
        }
    }

    @Nested
    @DisplayName("isEmpty方法测试")
    class IsEmptyTests {

        @Test
        @DisplayName("空定位器")
        void testIsEmpty() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash().build();

            assertThat(locator.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("非空定位器")
        void testIsNotEmpty() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();

            assertThat(locator.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("getVirtualNodeCount方法测试")
    class GetVirtualNodeCountTests {

        @Test
        @DisplayName("获取虚拟节点数量")
        void testGetVirtualNodeCount() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .virtualNodeCount(150)
                    .build();

            assertThat(locator.getVirtualNodeCount()).isEqualTo(150);
        }

        @Test
        @DisplayName("空定位器虚拟节点数量为0")
        void testEmptyVirtualNodeCount() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash().build();

            assertThat(locator.getVirtualNodeCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("多节点虚拟节点数量")
        void testMultipleNodesVirtualCount() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .virtualNodeCount(50)
                    .build();

            assertThat(locator.getVirtualNodeCount()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class ImplementationTests {

        @Test
        @DisplayName("ConsistentHash实现NodeLocator")
        void testConsistentHashImplements() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();

            assertThat(locator).isInstanceOf(ConsistentHash.class);
        }

        @Test
        @DisplayName("可以多态使用")
        void testPolymorphicUse() {
            NodeLocator<String> locator = createLocator();

            long hashValue = OpenHash.murmur3_128().hashUtf8("key").padToLong();
            String data = locator.locate(hashValue);

            assertThat(data).isNotNull();
        }

        private NodeLocator<String> createLocator() {
            return OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();
        }
    }

    @Nested
    @DisplayName("键分布测试")
    class KeyDistributionTests {

        @Test
        @DisplayName("键均匀分布到节点")
        void testKeyDistribution() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .addNode("node3", "data3")
                    .virtualNodeCount(100)
                    .build();

            java.util.Map<String, Integer> distribution = new java.util.HashMap<>();
            for (int i = 0; i < 3000; i++) {
                String data = ch.get("key" + i);
                distribution.merge(data, 1, Integer::sum);
            }

            // 每个节点应该分配到约1000个键（允许一定偏差）
            for (int count : distribution.values()) {
                assertThat(count).isBetween(500, 1500);
            }
        }
    }
}
