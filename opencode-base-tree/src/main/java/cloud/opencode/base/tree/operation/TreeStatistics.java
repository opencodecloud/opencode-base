package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.Treeable;

import java.util.*;

/**
 * Tree Statistics - Comprehensive tree metrics collected in a single pass
 * 树统计 - 单次遍历收集的全面树指标
 *
 * <p>Collects comprehensive tree metrics including node count, leaf count,
 * max depth, max width, average branching factor, and width per level
 * using a single BFS traversal.</p>
 * <p>使用单次 BFS 遍历收集全面的树指标，包括节点数、叶子节点数、
 * 最大深度、最大宽度、平均分支因子和每层宽度。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single-pass BFS collection of all metrics - 单次 BFS 收集所有指标</li>
 *   <li>Immutable record-based result - 基于不可变 record 的结果</li>
 *   <li>Derived metrics (internal node count, leaf ratio) - 派生指标</li>
 *   <li>Human-readable summary - 人类可读的摘要</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TreeStatistics stats = TreeStatistics.of(roots);
 * int nodes = stats.nodeCount();
 * int leaves = stats.leafCount();
 * int maxDepth = stats.maxDepth();
 * int maxWidth = stats.maxWidth();
 * double avgBranching = stats.avgBranchingFactor();
 * Map<Integer, Integer> widths = stats.widthByLevel();
 * String summary = stats.summary();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) single BFS pass - 时间复杂度: O(n) 单次 BFS</li>
 *   <li>Space complexity: O(w) where w is max width - 空间复杂度: O(w)，w 为最大宽度</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变 record）</li>
 *   <li>Null-safe: No (roots must not be null) - 空值安全: 否（根节点不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.3
 */
public record TreeStatistics(
        int nodeCount,
        int leafCount,
        int maxDepth,
        int maxWidth,
        double avgBranchingFactor,
        Map<Integer, Integer> widthByLevel
) {

    /**
     * Compact constructor - defensive copy of widthByLevel
     * 紧凑构造器 - widthByLevel 防御性拷贝
     */
    public TreeStatistics {
        widthByLevel = Collections.unmodifiableMap(new LinkedHashMap<>(widthByLevel));
    }

    /**
     * Collect statistics from a tree forest in a single BFS pass
     * 通过单次 BFS 遍历从树森林收集统计信息
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the statistics | 统计信息
     */
    public static <T extends Treeable<T, ID>, ID> TreeStatistics of(List<T> roots) {
        Objects.requireNonNull(roots, "roots must not be null");

        if (roots.isEmpty()) {
            return new TreeStatistics(0, 0, 0, 0, 0.0, Map.of());
        }

        int nodeCount = 0;
        int leafCount = 0;
        int internalNodeCount = 0;
        long totalChildCount = 0;
        int maxWidth = 0;
        int maxDepth = 0;
        Map<Integer, Integer> widthByLevel = new LinkedHashMap<>();

        // BFS with depth tracking using typed record to avoid Object[]/boxing overhead
        record NodeEntry<N>(N node, int depth) {}
        Deque<NodeEntry<T>> queue = new ArrayDeque<>();
        for (T root : roots) {
            if (root != null) {
                queue.offer(new NodeEntry<>(root, 0));
            }
        }

        while (!queue.isEmpty()) {
            NodeEntry<T> entry = queue.poll();
            T node = entry.node();
            int depth = entry.depth();

            nodeCount++;
            maxDepth = Math.max(maxDepth, depth);
            widthByLevel.merge(depth, 1, Integer::sum);

            List<T> children = node.getChildren();
            boolean isLeaf = (children == null || children.isEmpty());

            if (isLeaf) {
                leafCount++;
            } else {
                internalNodeCount++;
                totalChildCount += children.size();
                for (T child : children) {
                    if (child != null) {
                        queue.offer(new NodeEntry<>(child, depth + 1));
                    }
                }
            }
        }

        for (int width : widthByLevel.values()) {
            maxWidth = Math.max(maxWidth, width);
        }

        double avgBranchingFactor = internalNodeCount > 0
                ? (double) totalChildCount / internalNodeCount
                : 0.0;

        return new TreeStatistics(nodeCount, leafCount, maxDepth, maxWidth,
                avgBranchingFactor, widthByLevel);
    }

    /**
     * Get internal (non-leaf) node count
     * 获取内部（非叶子）节点数
     *
     * @return the internal node count | 内部节点数
     */
    public int internalNodeCount() {
        return nodeCount - leafCount;
    }

    /**
     * Get the ratio of leaf nodes to total nodes
     * 获取叶子节点占总节点的比率
     *
     * @return the leaf ratio (0.0 for empty tree) | 叶子比率（空树为0.0）
     */
    public double leafRatio() {
        return nodeCount > 0 ? (double) leafCount / nodeCount : 0.0;
    }

    /**
     * Get a human-readable summary of the statistics
     * 获取人类可读的统计摘要
     *
     * @return the summary string | 摘要字符串
     */
    public String summary() {
        return "TreeStatistics{nodes=%d, leaves=%d, internal=%d, maxDepth=%d, maxWidth=%d, avgBranching=%.2f, leafRatio=%.2f}"
                .formatted(nodeCount, leafCount, internalNodeCount(), maxDepth, maxWidth,
                        avgBranchingFactor, leafRatio());
    }
}
