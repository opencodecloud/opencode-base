package cloud.opencode.base.hash.consistent;

import cloud.opencode.base.hash.HashFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builder for consistent hash ring
 * 一致性哈希环构建器
 *
 * <p>Provides a fluent API for configuring and building a consistent hash ring.</p>
 * <p>提供流畅的API来配置和构建一致性哈希环。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ConsistentHash<String> ring = ConsistentHash.<String>builder()
 *     .hashFunction(OpenHash.murmur3_128())
 *     .virtualNodeCount(150)
 *     .addNode("server1", "192.168.1.1")
 *     .addNode("server2", "192.168.1.2", 2)
 *     .concurrent(true)
 *     .build();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API for ConsistentHash ring construction - 流畅的一致性哈希环构建器API</li>
 *   <li>Configurable virtual node count - 可配置虚拟节点数量</li>
 *   <li>Custom hash function support - 自定义哈希函数支持</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n×v) for build() where n=number of nodes and v=virtual nodes per node (ring insertion with TreeMap O(log(n×v)) per entry) - 时间复杂度: build() 为 O(n×v)，n 为节点数，v 为每节点虚拟节点数（TreeMap 每次插入 O(log(n×v))）</li>
 *   <li>Space complexity: O(n×v) for the virtual node ring - 空间复杂度: 虚拟节点环为 O(n×v)</li>
 * </ul>
 *
 * @param <T> node data type | 节点数据类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class ConsistentHashBuilder<T> {

    private HashFunction hashFunction;
    private int virtualNodeCount = 150;
    private boolean concurrent = false;
    private final List<HashNode<T>> initialNodes = new ArrayList<>();

    ConsistentHashBuilder() {
    }

    /**
     * Sets the hash function
     * 设置哈希函数
     *
     * @param hashFunction hash function | 哈希函数
     * @return this builder | 此构建器
     */
    public ConsistentHashBuilder<T> hashFunction(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
        return this;
    }

    /**
     * Sets the virtual node count per physical node
     * 设置每个物理节点的虚拟节点数
     *
     * @param count virtual node count | 虚拟节点数
     * @return this builder | 此构建器
     */
    public ConsistentHashBuilder<T> virtualNodeCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Virtual node count must be positive");
        }
        this.virtualNodeCount = count;
        return this;
    }

    /**
     * Adds a node with default weight
     * 添加默认权重的节点
     *
     * @param nodeId   node id | 节点ID
     * @param nodeData node data | 节点数据
     * @return this builder | 此构建器
     */
    public ConsistentHashBuilder<T> addNode(String nodeId, T nodeData) {
        initialNodes.add(HashNode.of(nodeId, nodeData));
        return this;
    }

    /**
     * Adds a weighted node
     * 添加带权重的节点
     *
     * @param nodeId   node id | 节点ID
     * @param nodeData node data | 节点数据
     * @param weight   node weight | 节点权重
     * @return this builder | 此构建器
     */
    public ConsistentHashBuilder<T> addNode(String nodeId, T nodeData, int weight) {
        initialNodes.add(HashNode.of(nodeId, nodeData, weight));
        return this;
    }

    /**
     * Adds multiple nodes
     * 添加多个节点
     *
     * @param nodes collection of nodes | 节点集合
     * @return this builder | 此构建器
     */
    public ConsistentHashBuilder<T> addNodes(Collection<HashNode<T>> nodes) {
        initialNodes.addAll(nodes);
        return this;
    }

    /**
     * Sets whether to use concurrent mode
     * 设置是否使用并发模式
     *
     * @param concurrent whether to be thread-safe | 是否线程安全
     * @return this builder | 此构建器
     */
    public ConsistentHashBuilder<T> concurrent(boolean concurrent) {
        this.concurrent = concurrent;
        return this;
    }

    /**
     * Builds the consistent hash ring
     * 构建一致性哈希环
     *
     * @return consistent hash ring | 一致性哈希环
     */
    public ConsistentHash<T> build() {
        ConsistentHash<T> ring = new ConsistentHash<>(hashFunction, virtualNodeCount, concurrent);
        for (HashNode<T> node : initialNodes) {
            ring.addNode(node.id(), node.data(), node.weight());
        }
        return ring;
    }
}
