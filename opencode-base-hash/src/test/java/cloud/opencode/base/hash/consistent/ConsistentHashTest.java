package cloud.opencode.base.hash.consistent;

import cloud.opencode.base.hash.OpenHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * ConsistentHash 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("ConsistentHash 测试")
class ConsistentHashTest {

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("获取键对应的节点")
        void testGet() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            String data = ch.get("key1");

            assertThat(data).isIn("data1", "data2");
        }

        @Test
        @DisplayName("相同键返回相同节点")
        void testConsistency() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            String data1 = ch.get("mykey");
            String data2 = ch.get("mykey");

            assertThat(data1).isEqualTo(data2);
        }

        @Test
        @DisplayName("不同键可能返回不同节点")
        void testDistribution() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .addNode("node3", "data3")
                    .build();

            Set<String> results = new java.util.HashSet<>();
            for (int i = 0; i < 100; i++) {
                results.add(ch.get("key" + i));
            }

            // 应该分布到多个节点
            assertThat(results.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("获取多个副本")
        void testGetMultipleReplicas() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .addNode("node3", "data3")
                    .build();

            List<String> replicas = ch.get("key", 2);

            assertThat(replicas).hasSize(2);
            assertThat(replicas.get(0)).isNotEqualTo(replicas.get(1));
        }
    }

    @Nested
    @DisplayName("locate方法测试")
    class LocateTests {

        @Test
        @DisplayName("定位节点")
        void testLocate() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            // locate takes a hash value, not a key
            long hashValue = OpenHash.murmur3_128().hashUtf8("key1").padToLong();
            String data = ch.locate(hashValue);

            assertThat(data).isIn("data1", "data2");
        }

        @Test
        @DisplayName("定位所有副本节点")
        void testLocateAll() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .addNode("node3", "data3")
                    .build();

            long hashValue = OpenHash.murmur3_128().hashUtf8("key1").padToLong();
            List<String> nodes = ch.locateAll(hashValue, 2);

            assertThat(nodes).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getVirtualNode方法测试")
    class GetVirtualNodeTests {

        @Test
        @DisplayName("获取虚拟节点")
        void testGetVirtualNode() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .virtualNodeCount(100)
                    .build();

            long hashValue = OpenHash.murmur3_128().hashUtf8("key1").padToLong();
            VirtualNode<String> vn = ch.getVirtualNode(hashValue);

            assertThat(vn).isNotNull();
            assertThat(vn.physicalNodeId()).isEqualTo("node1");
        }
    }

    @Nested
    @DisplayName("isEmpty方法测试")
    class IsEmptyTests {

        @Test
        @DisplayName("空哈希环")
        void testIsEmpty() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash().build();

            assertThat(ch.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("非空哈希环")
        void testIsNotEmpty() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();

            assertThat(ch.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("getVirtualNodeCount方法测试")
    class GetVirtualNodeCountTests {

        @Test
        @DisplayName("获取虚拟节点数量")
        void testGetVirtualNodeCount() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .virtualNodeCount(100)
                    .build();

            assertThat(ch.getVirtualNodeCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("多节点虚拟节点数量")
        void testMultipleNodesVirtualCount() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .virtualNodeCount(50)
                    .build();

            assertThat(ch.getVirtualNodeCount()).isEqualTo(100); // 2 nodes * 50
        }
    }

    @Nested
    @DisplayName("addNode方法测试")
    class AddNodeTests {

        @Test
        @DisplayName("动态添加节点")
        void testAddNode() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();

            ch.addNode("node2", "data2");

            assertThat(ch.getNodeCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("添加节点后仍可定位")
        void testLocateAfterAdd() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();

            ch.addNode("node2", "data2");

            String data = ch.get("testkey");
            assertThat(data).isNotNull();
        }
    }

    @Nested
    @DisplayName("removeNode方法测试")
    class RemoveNodeTests {

        @Test
        @DisplayName("动态删除节点")
        void testRemoveNode() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            ch.removeNode("node1");

            assertThat(ch.getNodeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("删除节点后仍可定位")
        void testLocateAfterRemove() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            ch.removeNode("node1");

            String data = ch.get("testkey");
            assertThat(data).isNotNull();
            assertThat(data).isEqualTo("data2");
        }

        @Test
        @DisplayName("删除不存在的节点")
        void testRemoveNonExistent() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();

            ch.removeNode("nonexistent");

            assertThat(ch.getNodeCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getNodes方法测试")
    class GetNodesTests {

        @Test
        @DisplayName("获取所有节点")
        void testGetNodes() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            Set<HashNode<String>> nodes = ch.getNodes();

            assertThat(nodes).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getNodeCount方法测试")
    class GetNodeCountTests {

        @Test
        @DisplayName("获取节点数量")
        void testGetNodeCount() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .addNode("node3", "data3")
                    .build();

            assertThat(ch.getNodeCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("空哈希环节点数量为0")
        void testEmptyNodeCount() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash().build();

            assertThat(ch.getNodeCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清空哈希环")
        void testClear() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            ch.clear();

            assertThat(ch.isEmpty()).isTrue();
            assertThat(ch.getNodeCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getDistribution方法测试")
    class GetDistributionTests {

        @Test
        @DisplayName("获取负载分布")
        void testGetDistribution() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .virtualNodeCount(100)
                    .build();

            List<String> keys = new java.util.ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                keys.add("key" + i);
            }

            Map<String, Integer> distribution = ch.getDistribution(keys);

            assertThat(distribution).hasSize(2);
            assertThat(distribution.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("getMigrationCount方法测试")
    class GetMigrationCountTests {

        @Test
        @DisplayName("计算迁移数量")
        void testGetMigrationCount() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .virtualNodeCount(100)
                    .build();

            List<String> keys = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                keys.add("key" + i);
            }

            // getMigrationCount calculates how many keys would be migrated if a node is removed
            int migration = ch.getMigrationCount("node1", keys);

            // Removing node1 would affect some keys
            assertThat(migration).isGreaterThanOrEqualTo(0);
            assertThat(migration).isLessThanOrEqualTo(100);
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("并发创建")
        void testConcurrentBuild() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .concurrent(true)
                    .build();

            assertThat(ch).isNotNull();
        }

        @Test
        @DisplayName("并发访问")
        void testConcurrentAccess() throws InterruptedException {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .concurrent(true)
                    .build();

            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                final int idx = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        ch.get("key" + idx + "_" + j);
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
        }
    }

    @Nested
    @DisplayName("权重节点测试")
    class WeightedNodeTests {

        @Test
        @DisplayName("带权重的节点")
        void testWeightedNodes() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1", 2)
                    .addNode("node2", "data2", 1)
                    .virtualNodeCount(100)
                    .build();

            // 权重为2的节点应有更多虚拟节点
            assertThat(ch.getVirtualNodeCount()).isEqualTo(300); // 2*100 + 1*100
        }
    }
}
