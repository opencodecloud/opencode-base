package cloud.opencode.base.hash.consistent;

import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.exception.OpenHashException;
import cloud.opencode.base.hash.function.Murmur3HashFunction;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Consistent hash ring implementation
 * 一致性哈希环实现
 *
 * <p>Implements consistent hashing with virtual nodes for improved load distribution.
 * Supports dynamic node addition/removal with minimal key migration.</p>
 * <p>实现带虚拟节点的一致性哈希以改善负载分布。
 * 支持动态节点添加/删除，最小化键迁移。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Virtual nodes for better distribution - 虚拟节点以获得更好的分布</li>
 *   <li>Weight-based virtual node count - 基于权重的虚拟节点数量</li>
 *   <li>Optional concurrent access support - 可选的并发访问支持</li>
 *   <li>Distribution statistics - 分布统计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ConsistentHash<String> ring = ConsistentHash.<String>builder()
 *     .virtualNodeCount(150)
 *     .addNode("server1", "192.168.1.1")
 *     .addNode("server2", "192.168.1.2", 2)
 *     .concurrent(true)
 *     .build();
 *
 * String server = ring.get("user_123");
 * List<String> replicas = ring.get("data_key", 3);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Optional (use concurrent(true)) - 线程安全: 可选（使用concurrent(true)）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(log n) for lookup where n = virtual nodes - 查找 O(log n), n为虚拟节点数</li>
 *   <li>Space complexity: O(n * v) where v = virtual replicas - O(n * v), v为虚拟副本数</li>
 * </ul>
 *
 * @param <T> node data type | 节点数据类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class ConsistentHash<T> implements NodeLocator<T> {

    private final TreeMap<Long, VirtualNode<T>> ring = new TreeMap<>();
    private final Map<String, HashNode<T>> nodes = new HashMap<>();
    private final HashFunction hashFunction;
    private final int virtualNodeCount;
    private final boolean concurrent;
    private final ReadWriteLock lock;

    ConsistentHash(HashFunction hashFunction, int virtualNodeCount, boolean concurrent) {
        this.hashFunction = hashFunction != null ? hashFunction : Murmur3HashFunction.murmur3_128();
        this.virtualNodeCount = virtualNodeCount;
        this.concurrent = concurrent;
        this.lock = concurrent ? new ReentrantReadWriteLock() : null;
    }

    // ==================== Node Location | 节点定位 ====================

    /**
     * Gets the node for a key
     * 获取键对应的节点
     *
     * @param key the key | 键
     * @return node data, or null if ring is empty | 节点数据，如果环为空则返回null
     */
    public T get(Object key) {
        long hash = hashKey(key);
        if (concurrent) {
            lock.readLock().lock();
            try {
                return locate(hash);
            } finally {
                lock.readLock().unlock();
            }
        }
        return locate(hash);
    }

    /**
     * Gets multiple nodes for a key (for replicas)
     * 获取键对应的多个节点（用于副本）
     *
     * @param key      the key | 键
     * @param replicas number of replicas | 副本数
     * @return list of node data (distinct physical nodes) | 节点数据列表（不同的物理节点）
     */
    public List<T> get(Object key, int replicas) {
        long hash = hashKey(key);
        if (concurrent) {
            lock.readLock().lock();
            try {
                return locateAll(hash, replicas);
            } finally {
                lock.readLock().unlock();
            }
        }
        return locateAll(hash, replicas);
    }

    @Override
    public T locate(long hashValue) {
        if (ring.isEmpty()) {
            return null;
        }
        Map.Entry<Long, VirtualNode<T>> entry = ring.ceilingEntry(hashValue);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        return entry.getValue().data();
    }

    @Override
    public List<T> locateAll(long hashValue, int count) {
        if (ring.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(count);
        Set<String> seenNodes = new HashSet<>();

        Long current = hashValue;
        int iterations = 0;
        int maxIterations = ring.size();

        while (result.size() < count && iterations < maxIterations) {
            Map.Entry<Long, VirtualNode<T>> entry = ring.ceilingEntry(current);
            if (entry == null) {
                entry = ring.firstEntry();
            }

            VirtualNode<T> vnode = entry.getValue();
            if (!seenNodes.contains(vnode.physicalNodeId())) {
                seenNodes.add(vnode.physicalNodeId());
                result.add(vnode.data());
            }

            Long higher = ring.higherKey(entry.getKey());
            current = (higher != null) ? higher : ring.firstKey();
            iterations++;
        }

        return result;
    }

    @Override
    public VirtualNode<T> getVirtualNode(long hashValue) {
        if (ring.isEmpty()) {
            return null;
        }
        Map.Entry<Long, VirtualNode<T>> entry = ring.ceilingEntry(hashValue);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    @Override
    public boolean isEmpty() {
        return ring.isEmpty();
    }

    @Override
    public int getVirtualNodeCount() {
        return ring.size();
    }

    // ==================== Node Management | 节点管理 ====================

    /**
     * Adds a node with default weight
     * 添加默认权重的节点
     *
     * @param nodeId   node id | 节点ID
     * @param nodeData node data | 节点数据
     */
    public void addNode(String nodeId, T nodeData) {
        addNode(nodeId, nodeData, 1);
    }

    /**
     * Adds a weighted node
     * 添加带权重的节点
     *
     * @param nodeId   node id | 节点ID
     * @param nodeData node data | 节点数据
     * @param weight   node weight | 节点权重
     */
    public void addNode(String nodeId, T nodeData, int weight) {
        HashNode<T> node = HashNode.of(nodeId, nodeData, weight);
        if (concurrent) {
            lock.writeLock().lock();
            try {
                addNodeInternal(node);
            } finally {
                lock.writeLock().unlock();
            }
        } else {
            addNodeInternal(node);
        }
    }

    private void addNodeInternal(HashNode<T> node) {
        if (nodes.containsKey(node.id())) {
            removeNodeInternal(node.id());
        }
        nodes.put(node.id(), node);

        int vnodeCount = virtualNodeCount * node.weight();
        for (int i = 0; i < vnodeCount; i++) {
            String vnodeKey = node.id() + "#" + i;
            long hash = hashKey(vnodeKey);
            VirtualNode<T> vnode = VirtualNode.of(node, i, hash);
            ring.put(hash, vnode);
        }
    }

    /**
     * Removes a node
     * 移除节点
     *
     * @param nodeId node id | 节点ID
     */
    public void removeNode(String nodeId) {
        if (concurrent) {
            lock.writeLock().lock();
            try {
                removeNodeInternal(nodeId);
            } finally {
                lock.writeLock().unlock();
            }
        } else {
            removeNodeInternal(nodeId);
        }
    }

    private void removeNodeInternal(String nodeId) {
        HashNode<T> node = nodes.remove(nodeId);
        if (node == null) {
            return;
        }

        int vnodeCount = virtualNodeCount * node.weight();
        for (int i = 0; i < vnodeCount; i++) {
            String vnodeKey = node.id() + "#" + i;
            long hash = hashKey(vnodeKey);
            // Only remove if the entry at this hash belongs to the node being removed,
            // to avoid removing another node's virtual node in case of hash collision
            VirtualNode<T> existing = ring.get(hash);
            if (existing != null && existing.physicalNodeId().equals(nodeId)) {
                ring.remove(hash);
            }
        }
    }

    /**
     * Gets all physical nodes
     * 获取所有物理节点
     *
     * @return set of nodes | 节点集合
     */
    public Set<HashNode<T>> getNodes() {
        if (concurrent) {
            lock.readLock().lock();
            try {
                return new HashSet<>(nodes.values());
            } finally {
                lock.readLock().unlock();
            }
        }
        return new HashSet<>(nodes.values());
    }

    /**
     * Gets the physical node count
     * 获取物理节点数量
     *
     * @return node count | 节点数量
     */
    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * Clears all nodes
     * 清空所有节点
     */
    public void clear() {
        if (concurrent) {
            lock.writeLock().lock();
            try {
                ring.clear();
                nodes.clear();
            } finally {
                lock.writeLock().unlock();
            }
        } else {
            ring.clear();
            nodes.clear();
        }
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Gets key distribution across nodes
     * 获取键在节点间的分布
     *
     * @param keys collection of keys | 键集合
     * @return map of node id to key count | 节点ID到键数量的映射
     */
    public Map<String, Integer> getDistribution(Collection<?> keys) {
        Map<String, Integer> distribution = new HashMap<>();
        if (concurrent) {
            lock.readLock().lock();
            try {
                for (Object key : keys) {
                    VirtualNode<T> vnode = getVirtualNode(hashKey(key));
                    if (vnode != null) {
                        distribution.merge(vnode.physicalNodeId(), 1, Integer::sum);
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
        } else {
            for (Object key : keys) {
                VirtualNode<T> vnode = getVirtualNode(hashKey(key));
                if (vnode != null) {
                    distribution.merge(vnode.physicalNodeId(), 1, Integer::sum);
                }
            }
        }
        return distribution;
    }

    /**
     * Gets the migration count if a node is removed
     * 获取如果移除节点需要迁移的键数量
     *
     * @param nodeId node id | 节点ID
     * @param keys   collection of keys | 键集合
     * @return number of keys that would be migrated | 需要迁移的键数量
     */
    public int getMigrationCount(String nodeId, Collection<?> keys) {
        int count = 0;
        if (concurrent) {
            lock.readLock().lock();
            try {
                if (!nodes.containsKey(nodeId)) {
                    throw OpenHashException.nodeNotFound(nodeId);
                }
                for (Object key : keys) {
                    VirtualNode<T> vnode = getVirtualNode(hashKey(key));
                    if (vnode != null && vnode.physicalNodeId().equals(nodeId)) {
                        count++;
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
        } else {
            if (!nodes.containsKey(nodeId)) {
                throw OpenHashException.nodeNotFound(nodeId);
            }
            for (Object key : keys) {
                VirtualNode<T> vnode = getVirtualNode(hashKey(key));
                if (vnode != null && vnode.physicalNodeId().equals(nodeId)) {
                    count++;
                }
            }
        }
        return count;
    }

    // ==================== Internal Methods | 内部方法 ====================

    private long hashKey(Object key) {
        String keyStr = key.toString();
        return hashFunction.hashString(keyStr, StandardCharsets.UTF_8).padToLong();
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Creates a builder
     * 创建构建器
     *
     * @param <T> node data type | 节点数据类型
     * @return builder | 构建器
     */
    public static <T> ConsistentHashBuilder<T> builder() {
        return new ConsistentHashBuilder<>();
    }
}
