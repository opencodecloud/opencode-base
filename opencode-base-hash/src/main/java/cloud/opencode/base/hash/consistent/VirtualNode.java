package cloud.opencode.base.hash.consistent;

/**
 * Virtual node in consistent hash ring
 * 一致性哈希环中的虚拟节点
 *
 * <p>Represents a virtual node that maps to a physical node. Virtual nodes
 * help achieve more uniform distribution of keys across physical nodes.</p>
 * <p>表示映射到物理节点的虚拟节点。虚拟节点有助于在物理节点间实现更均匀的键分布。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Maps to physical node - 映射到物理节点</li>
 *   <li>Has position on hash ring - 在哈希环上有位置</li>
 *   <li>Identified by replica number - 通过副本号标识</li>
 * </ul>
 *
 * @param physicalNode the physical node this virtual node belongs to | 此虚拟节点所属的物理节点
 * @param replicaIndex the replica index (0-based) | 副本索引（从0开始）
 * @param hashValue    the hash value (position on ring) | 哈希值（环上的位置）
 * @param <T>          data type | 数据类型
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Virtual nodes map to physical nodes
 * // 虚拟节点映射到物理节点
 * HashNode physical = new HashNode("server-1");
 * VirtualNode<HashNode> vnode = new VirtualNode<>(physical, 0);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public record VirtualNode<T>(
        HashNode<T> physicalNode,
        int replicaIndex,
        long hashValue
) {

    /**
     * Gets the physical node's id
     * 获取物理节点的ID
     *
     * @return physical node id | 物理节点ID
     */
    public String physicalNodeId() {
        return physicalNode.id();
    }

    /**
     * Gets the physical node's data
     * 获取物理节点的数据
     *
     * @return node data | 节点数据
     */
    public T data() {
        return physicalNode.data();
    }

    /**
     * Gets the key used for hashing this virtual node
     * 获取用于哈希此虚拟节点的键
     *
     * @return virtual node key | 虚拟节点键
     */
    public String getKey() {
        return physicalNode.id() + "#" + replicaIndex;
    }

    /**
     * Creates a virtual node
     * 创建虚拟节点
     *
     * @param physicalNode physical node | 物理节点
     * @param replicaIndex replica index | 副本索引
     * @param hashValue    hash value | 哈希值
     * @param <T>          data type | 数据类型
     * @return virtual node | 虚拟节点
     */
    public static <T> VirtualNode<T> of(HashNode<T> physicalNode, int replicaIndex, long hashValue) {
        return new VirtualNode<>(physicalNode, replicaIndex, hashValue);
    }

    @Override
    public String toString() {
        return "VirtualNode{" +
                "nodeId='" + physicalNodeId() + '\'' +
                ", replica=" + replicaIndex +
                ", hash=" + hashValue +
                '}';
    }
}
