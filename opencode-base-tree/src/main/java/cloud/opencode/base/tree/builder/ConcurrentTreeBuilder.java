package cloud.opencode.base.tree.builder;

import cloud.opencode.base.tree.Treeable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Concurrent Tree Builder
 * 并发树构建器
 *
 * <p>Thread-safe tree builder for concurrent scenarios.</p>
 * <p>用于并发场景的线程安全树构建器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe tree construction using ConcurrentHashMap and synchronized blocks - 使用ConcurrentHashMap和synchronized块的线程安全树构建</li>
 *   <li>Parallel stream processing for large node lists - 使用并行流处理大量节点列表</li>
 *   <li>Automatic root detection (null/zero/empty parentId) - 自动根节点检测（null/零/空parentId）</li>
 *   <li>Threshold-based sequential fallback for small lists - 小列表的基于阈值的顺序降级处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build tree concurrently - 并发构建树
 * List<MyNode> roots = ConcurrentTreeBuilder.build(flatList);
 *
 * // Build with explicit root ID - 使用显式根ID构建
 * List<MyNode> roots = ConcurrentTreeBuilder.build(flatList, 0L);
 *
 * // Build large tree with parallel threshold - 带并行阈值的大型树构建
 * List<MyNode> roots = ConcurrentTreeBuilder.buildLarge(flatList, 0L, 1000);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap and per-parent synchronization) - 线程安全: 是（使用ConcurrentHashMap和每父节点同步）</li>
 *   <li>Null-safe: Partial (null input list returns empty list) - 空值安全: 部分（null输入列表返回空列表）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - each node is processed once via parallel stream - 时间复杂度: O(n) - 通过并行流每个节点仅处理一次</li>
 *   <li>Space complexity: O(n) - ConcurrentHashMap and roots list proportional to node count - 空间复杂度: O(n) - ConcurrentHashMap 和根节点列表与节点数成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class ConcurrentTreeBuilder {

    private ConcurrentTreeBuilder() {
        // Utility class
    }

    /**
     * Build tree concurrently
     * 并发构建树
     *
     * @param nodes the node list | 节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the root nodes | 根节点列表
     */
    public static <T extends Treeable<T, ID>, ID> List<T> build(List<T> nodes) {
        return build(nodes, null);
    }

    /**
     * Build tree concurrently with root ID
     * 使用根ID并发构建树
     *
     * @param nodes the node list | 节点列表
     * @param rootId the root ID | 根ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the root nodes | 根节点列表
     */
    public static <T extends Treeable<T, ID>, ID> List<T> build(List<T> nodes, ID rootId) {
        if (nodes == null || nodes.isEmpty()) {
            return new CopyOnWriteArrayList<>();
        }

        // Thread-safe map
        Map<ID, T> nodeMap = new ConcurrentHashMap<>();
        nodes.forEach(n -> nodeMap.put(n.getId(), n));

        // Thread-safe roots
        List<T> roots = new CopyOnWriteArrayList<>();

        // Process in parallel
        nodes.parallelStream().forEach(node -> {
            ID parentId = node.getParentId();

            if (isRoot(parentId, rootId)) {
                roots.add(node);
            } else {
                T parent = nodeMap.get(parentId);
                if (parent != null) {
                    synchronized (parent) {
                        List<T> children = parent.getChildren();
                        if (children == null) {
                            children = new CopyOnWriteArrayList<>();
                            parent.setChildren(children);
                        }
                        children.add(node);
                    }
                }
            }
        });

        return new ArrayList<>(roots);
    }

    /**
     * Build large tree with parallel processing
     * 使用并行处理构建大型树
     *
     * @param nodes the node list | 节点列表
     * @param rootId the root ID | 根ID
     * @param parallelThreshold the threshold for parallel processing | 并行处理阈值
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the root nodes | 根节点列表
     */
    public static <T extends Treeable<T, ID>, ID> List<T> buildLarge(
            List<T> nodes, ID rootId, int parallelThreshold) {

        if (nodes == null || nodes.isEmpty()) {
            return new ArrayList<>();
        }

        // Use sequential for small lists
        if (nodes.size() < parallelThreshold) {
            return ListToTreeConverter.convert(nodes, rootId);
        }

        return build(nodes, rootId);
    }

    private static <ID> boolean isRoot(ID parentId, ID rootId) {
        if (rootId != null) {
            return Objects.equals(parentId, rootId);
        }
        return parentId == null ||
            (parentId instanceof Number n && n.longValue() == 0) ||
            "".equals(parentId) ||
            "0".equals(parentId);
    }
}
