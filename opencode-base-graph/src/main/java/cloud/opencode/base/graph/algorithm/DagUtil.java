package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.DirectedGraph;
import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.exception.GraphErrorCode;
import cloud.opencode.base.graph.exception.GraphException;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * DAG (Directed Acyclic Graph) Utility
 * 有向无环图工具类
 *
 * <p>Provides operations specific to directed acyclic graphs (DAGs), including
 * longest path (critical path), transitive reduction/closure, and ancestor/descendant queries.
 * All methods verify the input is a DAG before processing.</p>
 * <p>提供有向无环图（DAG）专用操作，包括最长路径（关键路径）、传递归约/闭包
 * 和祖先/后代查询。所有方法在处理前验证输入是否为DAG。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Longest path (critical path) computation - 最长路径（关键路径）计算</li>
 *   <li>Longest path between specific source and target - 指定源和目标之间的最长路径</li>
 *   <li>Transitive reduction (remove redundant edges) - 传递归约（移除冗余边）</li>
 *   <li>Transitive closure (add implied edges) - 传递闭包（添加隐含边）</li>
 *   <li>Ancestor and descendant queries - 祖先和后代查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> dag = OpenGraph.directed();
 * dag.addEdge("A", "B", 2.0);
 * dag.addEdge("B", "C", 3.0);
 * dag.addEdge("A", "C", 1.0);
 *
 * List<String> longest = DagUtil.longestPath(dag);
 * double length = DagUtil.longestPathLength(dag);
 *
 * Graph<String> reduced = DagUtil.transitiveReduction(dag);
 * Graph<String> closure = DagUtil.transitiveClosure(dag);
 *
 * Set<String> anc = DagUtil.ancestors(dag, "C");   // {"A", "B"}
 * Set<String> desc = DagUtil.descendants(dag, "A"); // {"B", "C"}
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>longestPath: O(V + E) - 时间复杂度: O(V + E)</li>
 *   <li>transitiveReduction/Closure: O(V * (V + E)) - 时间复杂度: O(V * (V + E))</li>
 *   <li>ancestors/descendants: O(V + E) - 时间复杂度: O(V + E)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
public final class DagUtil {

    private DagUtil() {
        // Utility class
    }

    /**
     * Find the longest path in the DAG (critical path).
     * 查找DAG中的最长路径（关键路径）。
     *
     * <p>Uses topological sort + dynamic programming. Returns vertex sequence
     * of the longest path by edge weight sum. If multiple paths have the same length,
     * any one of them may be returned.</p>
     * <p>使用拓扑排序+动态规划。返回按边权重和计算的最长路径的顶点序列。
     * 如果多条路径长度相同，可能返回其中任意一条。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the DAG | 有向无环图
     * @return vertex sequence of the longest path | 最长路径的顶点序列
     * @throws GraphException if graph is not a DAG | 如果图不是DAG则抛出异常
     */
    public static <V> List<V> longestPath(Graph<V> graph) {
        requireDag(graph);
        if (graph.vertexCount() == 0) {
            return Collections.emptyList();
        }

        List<V> topoOrder = TopologicalSortUtil.sort(graph);
        Map<V, Double> dist = new HashMap<>();
        Map<V, V> predecessor = new HashMap<>();

        for (V v : topoOrder) {
            dist.put(v, 0.0);
        }

        for (V u : topoOrder) {
            for (Edge<V> edge : graph.outEdges(u)) {
                V v = edge.to();
                double newDist = dist.get(u) + edge.weight();
                if (newDist > dist.get(v)) {
                    dist.put(v, newDist);
                    predecessor.put(v, u);
                }
            }
        }

        // Find the vertex with maximum distance
        V endVertex = null;
        double maxDist = Double.NEGATIVE_INFINITY;
        for (Map.Entry<V, Double> entry : dist.entrySet()) {
            if (entry.getValue() > maxDist) {
                maxDist = entry.getValue();
                endVertex = entry.getKey();
            }
        }

        if (endVertex == null) {
            // No vertices: should not happen after topoOrder check
            return List.of(topoOrder.getFirst());
        }

        // Reconstruct path
        LinkedList<V> path = new LinkedList<>();
        V current = endVertex;
        while (current != null) {
            path.addFirst(current);
            current = predecessor.get(current);
        }

        return path;
    }

    /**
     * Find the longest path between two specific vertices in the DAG.
     * 查找DAG中两个指定顶点之间的最长路径。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the DAG | 有向无环图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return vertex sequence of the longest path, or empty list if no path | 最长路径的顶点序列，无路径时返回空列表
     * @throws GraphException if graph is not a DAG | 如果图不是DAG则抛出异常
     */
    public static <V> List<V> longestPath(Graph<V> graph, V source, V target) {
        requireDag(graph);
        if (graph.vertexCount() == 0) {
            return Collections.emptyList();
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            return Collections.emptyList();
        }
        if (source.equals(target)) {
            return List.of(source);
        }

        List<V> topoOrder = TopologicalSortUtil.sort(graph);
        Map<V, Double> dist = new HashMap<>();
        Map<V, V> predecessor = new HashMap<>();

        // Initialize all to -infinity except source
        for (V v : topoOrder) {
            dist.put(v, Double.NEGATIVE_INFINITY);
        }
        dist.put(source, 0.0);

        for (V u : topoOrder) {
            if (dist.get(u) == Double.NEGATIVE_INFINITY) {
                continue; // unreachable from source
            }
            for (Edge<V> edge : graph.outEdges(u)) {
                V v = edge.to();
                double newDist = dist.get(u) + edge.weight();
                if (newDist > dist.get(v)) {
                    dist.put(v, newDist);
                    predecessor.put(v, u);
                }
            }
        }

        if (dist.get(target) == Double.NEGATIVE_INFINITY) {
            return Collections.emptyList(); // no path
        }

        // Reconstruct path
        LinkedList<V> path = new LinkedList<>();
        V current = target;
        while (current != null) {
            path.addFirst(current);
            current = predecessor.get(current);
        }

        return path;
    }

    /**
     * Compute the length (sum of edge weights) of the longest path in the DAG.
     * 计算DAG中最长路径的长度（边权重之和）。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the DAG | 有向无环图
     * @return the length of the longest path | 最长路径的长度
     * @throws GraphException if graph is not a DAG | 如果图不是DAG则抛出异常
     */
    public static <V> double longestPathLength(Graph<V> graph) {
        requireDag(graph);
        if (graph.vertexCount() == 0) {
            return 0.0;
        }

        List<V> topoOrder = TopologicalSortUtil.sort(graph);
        Map<V, Double> dist = new HashMap<>();

        for (V v : topoOrder) {
            dist.put(v, 0.0);
        }

        for (V u : topoOrder) {
            for (Edge<V> edge : graph.outEdges(u)) {
                V v = edge.to();
                double newDist = dist.get(u) + edge.weight();
                if (newDist > dist.get(v)) {
                    dist.put(v, newDist);
                }
            }
        }

        double maxDist = 0.0;
        for (double d : dist.values()) {
            if (d > maxDist) {
                maxDist = d;
            }
        }
        return maxDist;
    }

    /**
     * Compute the transitive reduction of a DAG.
     * 计算DAG的传递归约。
     *
     * <p>Removes redundant edges: if A->B->C and A->C exist, removes A->C.
     * Returns a new graph instance. O(V*(V+E)).</p>
     * <p>移除冗余边：如果A->B->C和A->C同时存在，则移除A->C。
     * 返回新图实例。时间复杂度O(V*(V+E))。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the DAG | 有向无环图
     * @return new graph with redundant edges removed | 移除冗余边的新图
     * @throws GraphException if graph is not a DAG | 如果图不是DAG则抛出异常
     */
    public static <V> Graph<V> transitiveReduction(Graph<V> graph) {
        requireDag(graph);

        DirectedGraph<V> result = new DirectedGraph<>();
        for (V v : graph.vertices()) {
            result.addVertex(v);
        }

        // For each edge u->v, check if there's a path from u to v of length >= 2
        // If yes, the edge is redundant
        for (Edge<V> edge : graph.edges()) {
            V u = edge.from();
            V v = edge.to();

            if (!hasLongerPath(graph, u, v)) {
                result.addEdge(u, v, edge.weight());
            }
        }

        return result;
    }

    /**
     * Compute the transitive closure of a DAG.
     * 计算DAG的传递闭包。
     *
     * <p>Adds implied edges: if A->B->C exists but A->C does not, adds A->C.
     * Returns a new graph instance. O(V*(V+E)).</p>
     * <p>添加隐含边：如果A->B->C存在但A->C不存在，则添加A->C。
     * 返回新图实例。时间复杂度O(V*(V+E))。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the DAG | 有向无环图
     * @return new graph with implied edges added | 添加隐含边的新图
     * @throws GraphException if graph is not a DAG | 如果图不是DAG则抛出异常
     */
    public static <V> Graph<V> transitiveClosure(Graph<V> graph) {
        requireDag(graph);

        DirectedGraph<V> result = new DirectedGraph<>();
        for (V v : graph.vertices()) {
            result.addVertex(v);
        }

        // Copy all existing edges
        for (Edge<V> edge : graph.edges()) {
            result.addEdge(edge.from(), edge.to(), edge.weight());
        }

        // For each vertex, find all reachable vertices and add edges
        for (V u : graph.vertices()) {
            Set<V> reachable = descendants(graph, u);
            for (V v : reachable) {
                if (!graph.containsEdge(u, v)) {
                    result.addEdge(u, v);
                }
            }
        }

        return result;
    }

    /**
     * Find all ancestors of a vertex (vertices that can reach this vertex).
     * 查找顶点的所有祖先（能够到达此顶点的顶点）。
     *
     * <p>Does not include the vertex itself.</p>
     * <p>不包含顶点自身。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the DAG | 有向无环图
     * @param vertex the vertex | 顶点
     * @return set of ancestor vertices | 祖先顶点集合
     * @throws GraphException if graph is not a DAG | 如果图不是DAG则抛出异常
     */
    public static <V> Set<V> ancestors(Graph<V> graph, V vertex) {
        requireDag(graph);
        if (!graph.containsVertex(vertex)) {
            return Collections.emptySet();
        }

        // BFS/DFS backward through incoming edges
        Set<V> result = new LinkedHashSet<>();
        Deque<V> stack = new ArrayDeque<>();
        stack.push(vertex);

        while (!stack.isEmpty()) {
            V current = stack.pop();
            for (Edge<V> inEdge : graph.inEdges(current)) {
                V from = inEdge.from();
                if (result.add(from)) {
                    stack.push(from);
                }
            }
        }

        return Collections.unmodifiableSet(result);
    }

    /**
     * Find all descendants of a vertex (vertices reachable from this vertex).
     * 查找顶点的所有后代（从此顶点可达的顶点）。
     *
     * <p>Does not include the vertex itself.</p>
     * <p>不包含顶点自身。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the DAG | 有向无环图
     * @param vertex the vertex | 顶点
     * @return set of descendant vertices | 后代顶点集合
     * @throws GraphException if graph is not a DAG | 如果图不是DAG则抛出异常
     */
    public static <V> Set<V> descendants(Graph<V> graph, V vertex) {
        requireDag(graph);
        if (!graph.containsVertex(vertex)) {
            return Collections.emptySet();
        }

        // BFS/DFS forward through outgoing edges
        Set<V> result = new LinkedHashSet<>();
        Deque<V> stack = new ArrayDeque<>();
        stack.push(vertex);

        while (!stack.isEmpty()) {
            V current = stack.pop();
            for (Edge<V> outEdge : graph.outEdges(current)) {
                V to = outEdge.to();
                if (result.add(to)) {
                    stack.push(to);
                }
            }
        }

        return Collections.unmodifiableSet(result);
    }

    /**
     * Verify that the graph is a directed acyclic graph (DAG).
     * 验证图是否为有向无环图（DAG）。
     *
     * <p>Each public method calls this guard independently. While this means cycle detection
     * runs once per method call (O(V+E)), it avoids coupling callers to each other and keeps
     * the API composable. For repeated operations on the same graph, callers may pre-validate
     * with {@link cloud.opencode.base.graph.algorithm.CycleDetectionUtil#hasCycle(Graph)} to
     * avoid redundant checks.</p>
     * <p>每个公开方法独立调用此校验。虽然这意味着每次方法调用都会执行一次环检测（O(V+E)），
     * 但这避免了调用方之间的耦合，保持了API的可组合性。对于同一图的重复操作，
     * 调用方可提前使用 {@code CycleDetectionUtil.hasCycle()} 进行预校验以避免重复检测。</p>
     *
     * @param graph the graph to verify | 要验证的图
     * @throws GraphException if graph is null, not directed, or contains a cycle | 如果图为null、不是有向图或包含环则抛出异常
     */
    private static <V> void requireDag(Graph<V> graph) {
        if (graph == null) {
            throw new GraphException("Graph must not be null", GraphErrorCode.NOT_DAG);
        }
        if (!graph.isDirected()) {
            throw new GraphException("Graph must be directed to be a DAG", GraphErrorCode.NOT_DAG);
        }
        if (CycleDetectionUtil.hasCycle(graph)) {
            throw new GraphException("Graph contains a cycle and is not a DAG", GraphErrorCode.NOT_DAG);
        }
    }

    /**
     * Check if there's a path from u to v of length >= 2 (bypassing the direct edge).
     * 检查是否存在从u到v的长度>=2的路径（绕过直接边）。
     */
    private static <V> boolean hasLongerPath(Graph<V> graph, V u, V v) {
        // BFS from u, but skip the direct edge u->v
        Set<V> visited = new HashSet<>();
        Deque<V> queue = new ArrayDeque<>();

        // Start from u's neighbors except v (or including v but via other paths)
        for (Edge<V> edge : graph.outEdges(u)) {
            V neighbor = edge.to();
            if (!neighbor.equals(v) && visited.add(neighbor)) {
                queue.add(neighbor);
            }
        }

        while (!queue.isEmpty()) {
            V current = queue.poll();
            if (current.equals(v)) {
                return true;
            }
            for (Edge<V> edge : graph.outEdges(current)) {
                V next = edge.to();
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }

        return false;
    }
}
