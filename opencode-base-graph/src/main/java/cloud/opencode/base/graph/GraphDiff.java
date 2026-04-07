package cloud.opencode.base.graph;

import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Graph Diff - Utility for comparing two graphs and computing their differences
 * 图差异 - 比较两个图并计算差异的工具类
 *
 * <p>Compares a "before" graph and an "after" graph, producing a {@link DiffResult}
 * that details added, removed, and common vertices and edges.</p>
 * <p>比较"前"图和"后"图，生成详细说明已添加、已删除和共同的顶点和边的 {@link DiffResult}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Vertex-level diff (added, removed, common) - 顶点级差异（已添加、已删除、共同）</li>
 *   <li>Edge-level diff (added, removed, common) - 边级差异（已添加、已删除、共同）</li>
 *   <li>Edge comparison uses record equals (from, to, weight) - 边比较使用记录equals（from, to, weight）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> before = OpenGraph.directed();
 * before.addEdge("A", "B");
 * Graph<String> after = OpenGraph.directed();
 * after.addEdge("A", "C");
 *
 * GraphDiff.DiffResult<String> diff = GraphDiff.compare(before, after);
 * Set<String> added = diff.addedVertices();
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
public final class GraphDiff {

    private GraphDiff() {
        // Utility class
    }

    /**
     * Result of comparing two graphs
     * 比较两个图的结果
     *
     * @param <V> the vertex type | 顶点类型
     * @param addedVertices vertices present in after but not in before | 在after中存在但在before中不存在的顶点
     * @param removedVertices vertices present in before but not in after | 在before中存在但在after中不存在的顶点
     * @param commonVertices vertices present in both graphs | 两个图中都存在的顶点
     * @param addedEdges edges present in after but not in before | 在after中存在但在before中不存在的边
     * @param removedEdges edges present in before but not in after | 在before中存在但在after中不存在的边
     * @param commonEdges edges present in both graphs | 两个图中都存在的边
     */
    public record DiffResult<V>(
            Set<V> addedVertices, Set<V> removedVertices, Set<V> commonVertices,
            Set<Edge<V>> addedEdges, Set<Edge<V>> removedEdges, Set<Edge<V>> commonEdges
    ) {

        /**
         * Check if there are no differences between the two graphs
         * 检查两个图之间是否没有差异
         *
         * @return true if no differences | 如果没有差异返回true
         */
        public boolean isEmpty() {
            return addedVertices.isEmpty() && removedVertices.isEmpty()
                    && addedEdges.isEmpty() && removedEdges.isEmpty();
        }

        /**
         * Check if there are any differences between the two graphs
         * 检查两个图之间是否有任何差异
         *
         * @return true if there are differences | 如果有差异返回true
         */
        public boolean hasChanges() {
            return !isEmpty();
        }
    }

    /**
     * Compare two graphs and compute their differences
     * 比较两个图并计算差异
     *
     * @param <V> the vertex type | 顶点类型
     * @param before the original graph | 原始图
     * @param after the modified graph | 修改后的图
     * @return the diff result | 差异结果
     * @throws NullPointerException if either graph is null | 当任一图为null时抛出
     */
    public static <V> DiffResult<V> compare(Graph<V> before, Graph<V> after) {
        Objects.requireNonNull(before, "Before graph must not be null");
        Objects.requireNonNull(after, "After graph must not be null");
        if (before.isDirected() != after.isDirected()) {
            throw new IllegalArgumentException(
                    "Cannot compare directed and undirected graphs; both must be the same type");
        }

        Set<V> beforeVertices = before.vertices();
        Set<V> afterVertices = after.vertices();

        Set<V> addedVertices = new HashSet<>(afterVertices);
        addedVertices.removeAll(beforeVertices);

        Set<V> removedVertices = new HashSet<>(beforeVertices);
        removedVertices.removeAll(afterVertices);

        Set<V> commonVertices = new HashSet<>(beforeVertices);
        commonVertices.retainAll(afterVertices);

        Set<Edge<V>> beforeEdges = before.edges();
        Set<Edge<V>> afterEdges = after.edges();

        Set<Edge<V>> addedEdges = new HashSet<>(afterEdges);
        addedEdges.removeAll(beforeEdges);

        Set<Edge<V>> removedEdges = new HashSet<>(beforeEdges);
        removedEdges.removeAll(afterEdges);

        Set<Edge<V>> commonEdges = new HashSet<>(beforeEdges);
        commonEdges.retainAll(afterEdges);

        return new DiffResult<>(
                Collections.unmodifiableSet(addedVertices),
                Collections.unmodifiableSet(removedVertices),
                Collections.unmodifiableSet(commonVertices),
                Collections.unmodifiableSet(addedEdges),
                Collections.unmodifiableSet(removedEdges),
                Collections.unmodifiableSet(commonEdges)
        );
    }
}
