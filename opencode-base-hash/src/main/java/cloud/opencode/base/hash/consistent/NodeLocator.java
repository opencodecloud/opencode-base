package cloud.opencode.base.hash.consistent;

import java.util.List;

/**
 * Node locator interface for consistent hash ring
 * 一致性哈希环的节点定位器接口
 *
 * <p>Defines the contract for locating nodes on a consistent hash ring
 * based on a key's hash value.</p>
 * <p>定义根据键的哈希值在一致性哈希环上定位节点的契约。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single node location - 单节点定位</li>
 *   <li>Multiple replica location - 多副本定位</li>
 *   <li>Ring traversal - 环遍历</li>
 * </ul>
 *
 * @param <T> node data type | 节点数据类型
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Locate a node for a given key
 * // 为给定键定位节点
 * NodeLocator<String> locator = consistentHash;
 * String node = locator.locate("my-key");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public interface NodeLocator<T> {

    /**
     * Locates the node for a given hash value
     * 为给定的哈希值定位节点
     *
     * @param hashValue hash value | 哈希值
     * @return the node data, or null if ring is empty | 节点数据，如果环为空则返回null
     */
    T locate(long hashValue);

    /**
     * Locates multiple nodes for replica placement
     * 为副本放置定位多个节点
     *
     * <p>Returns nodes in clockwise order from the hash position.
     * Duplicate physical nodes are excluded.</p>
     * <p>按哈希位置的顺时针顺序返回节点。排除重复的物理节点。</p>
     *
     * @param hashValue hash value | 哈希值
     * @param count     number of nodes to return | 要返回的节点数
     * @return list of node data | 节点数据列表
     */
    List<T> locateAll(long hashValue, int count);

    /**
     * Gets the virtual node at the specified position
     * 获取指定位置的虚拟节点
     *
     * @param hashValue hash value | 哈希值
     * @return virtual node, or null if ring is empty | 虚拟节点，如果环为空则返回null
     */
    VirtualNode<T> getVirtualNode(long hashValue);

    /**
     * Checks if the ring is empty
     * 检查环是否为空
     *
     * @return true if empty | 如果为空返回true
     */
    boolean isEmpty();

    /**
     * Gets the total number of virtual nodes
     * 获取虚拟节点总数
     *
     * @return virtual node count | 虚拟节点数量
     */
    int getVirtualNodeCount();
}
