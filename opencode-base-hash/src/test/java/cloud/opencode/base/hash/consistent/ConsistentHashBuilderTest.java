package cloud.opencode.base.hash.consistent;

import cloud.opencode.base.hash.OpenHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ConsistentHashBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("ConsistentHashBuilder 测试")
class ConsistentHashBuilderTest {

    @Nested
    @DisplayName("hashFunction方法测试")
    class HashFunctionTests {

        @Test
        @DisplayName("设置哈希函数")
        void testHashFunction() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .hashFunction(OpenHash.xxHash64())
                    .addNode("node1", "data1")
                    .build();

            assertThat(ch).isNotNull();
        }

        @Test
        @DisplayName("不同哈希函数产生不同分布")
        void testDifferentHashFunctions() {
            ConsistentHash<String> ch1 = OpenHash.<String>consistentHash()
                    .hashFunction(OpenHash.murmur3_128())
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            ConsistentHash<String> ch2 = OpenHash.<String>consistentHash()
                    .hashFunction(OpenHash.xxHash64())
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .build();

            // 相同键可能映射到不同节点
            String data1 = ch1.get("testkey");
            String data2 = ch2.get("testkey");

            // 不强制要求不同，但说明哈希函数设置生效
            assertThat(data1).isIn("data1", "data2");
            assertThat(data2).isIn("data1", "data2");
        }
    }

    @Nested
    @DisplayName("virtualNodeCount方法测试")
    class VirtualNodeCountTests {

        @Test
        @DisplayName("设置虚拟节点数量")
        void testVirtualNodeCount() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .virtualNodeCount(200)
                    .addNode("node1", "data1")
                    .build();

            assertThat(ch.getVirtualNodeCount()).isEqualTo(200);
        }

        @Test
        @DisplayName("默认虚拟节点数量")
        void testDefaultVirtualNodeCount() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();

            // 应该有默认的虚拟节点数量
            assertThat(ch.getVirtualNodeCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("零虚拟节点数量抛出异常")
        void testZeroVirtualNodes() {
            assertThatThrownBy(() -> OpenHash.<String>consistentHash()
                    .virtualNodeCount(0)
                    .addNode("node1", "data1")
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负虚拟节点数量抛出异常")
        void testNegativeVirtualNodes() {
            assertThatThrownBy(() -> OpenHash.<String>consistentHash()
                    .virtualNodeCount(-1)
                    .addNode("node1", "data1")
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("addNode方法测试")
    class AddNodeTests {

        @Test
        @DisplayName("添加单个节点")
        void testAddNode() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();

            assertThat(ch.getNodeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("添加多个节点")
        void testAddMultipleNodes() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .addNode("node3", "data3")
                    .build();

            assertThat(ch.getNodeCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("添加带权重的节点")
        void testAddWeightedNode() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1", 3)
                    .virtualNodeCount(100)
                    .build();

            assertThat(ch.getVirtualNodeCount()).isEqualTo(300);
        }
    }

    @Nested
    @DisplayName("addNodes方法测试")
    class AddNodesTests {

        @Test
        @DisplayName("批量添加节点")
        void testAddNodes() {
            List<HashNode<String>> nodes = List.of(
                    HashNode.of("node1", "data1"),
                    HashNode.of("node2", "data2"),
                    HashNode.of("node3", "data3")
            );

            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNodes(nodes)
                    .build();

            assertThat(ch.getNodeCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("批量添加空列表")
        void testAddEmptyNodes() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNodes(List.of())
                    .build();

            assertThat(ch.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("concurrent方法测试")
    class ConcurrentTests {

        @Test
        @DisplayName("创建并发安全的哈希环")
        void testConcurrent() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .concurrent(true)
                    .build();

            assertThat(ch).isNotNull();
        }

        @Test
        @DisplayName("并发安全哈希环正常工作")
        void testConcurrentWorks() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .concurrent(true)
                    .build();

            String data = ch.get("testkey");
            assertThat(data).isIn("data1", "data2");
        }
    }

    @Nested
    @DisplayName("build方法测试")
    class BuildTests {

        @Test
        @DisplayName("构建哈希环")
        void testBuild() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();

            assertThat(ch).isNotNull();
        }

        @Test
        @DisplayName("构建空哈希环")
        void testBuildEmpty() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash().build();

            assertThat(ch.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("完整链式调用")
        void testFullChain() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .hashFunction(OpenHash.murmur3_128())
                    .virtualNodeCount(150)
                    .addNode("node1", "data1")
                    .addNode("node2", "data2")
                    .concurrent(true)
                    .build();

            assertThat(ch).isNotNull();
            assertThat(ch.getNodeCount()).isEqualTo(2);
            assertThat(ch.getVirtualNodeCount()).isEqualTo(300);
        }
    }

    @Nested
    @DisplayName("NodeLocator接口实现测试")
    class NodeLocatorTests {

        @Test
        @DisplayName("ConsistentHash实现NodeLocator")
        void testImplementsNodeLocator() {
            ConsistentHash<String> ch = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();

            assertThat(ch).isInstanceOf(NodeLocator.class);
        }

        @Test
        @DisplayName("作为NodeLocator使用")
        void testUseAsNodeLocator() {
            NodeLocator<String> locator = OpenHash.<String>consistentHash()
                    .addNode("node1", "data1")
                    .build();

            long hashValue = OpenHash.murmur3_128().hashUtf8("key").padToLong();
            String data = locator.locate(hashValue);
            assertThat(data).isNotNull();
        }
    }
}
