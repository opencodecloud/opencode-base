package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.DirectedGraph;
import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.exception.InvalidVertexException;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Strongly Connected Components Util - Tarjan's Algorithm
 * 强连通分量工具类 - Tarjan算法
 *
 * <p>Utility class for finding strongly connected components (SCCs) in directed graphs
 * using Tarjan's algorithm. For undirected graphs, returns connected components.</p>
 * <p>使用Tarjan算法在有向图中查找强连通分量(SCC)的工具类。对于无向图，返回连通分量。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Find all SCCs - 查找所有强连通分量</li>
 *   <li>Count SCCs - 统计强连通分量数量</li>
 *   <li>Find SCC containing a specific vertex - 查找包含特定顶点的强连通分量</li>
 *   <li>Check if graph is strongly connected - 检查图是否强连通</li>
 *   <li>Build condensation graph (DAG of SCCs) - 构建缩合图（强连通分量的有向无环图）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> graph = new DirectedGraph<>();
 * graph.addEdge("A", "B");
 * graph.addEdge("B", "C");
 * graph.addEdge("C", "A");
 *
 * List<Set<String>> sccs = StronglyConnectedComponentsUtil.find(graph);
 * boolean strong = StronglyConnectedComponentsUtil.isStronglyConnected(graph);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns empty results for null graph, throws for null vertex) - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(V + E) - 时间复杂度: O(V + E)</li>
 *   <li>Space complexity: O(V) - 空间复杂度: O(V)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
public final class StronglyConnectedComponentsUtil {

    private StronglyConnectedComponentsUtil() {
        // Utility class
    }

    /**
     * Find all strongly connected components using Tarjan's algorithm.
     * For undirected graphs, returns connected components.
     * 使用Tarjan算法查找所有强连通分量。对于无向图，返回连通分量。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return list of SCCs (each SCC is a set of vertices) | 强连通分量列表（每个分量是顶点集合）
     */
    public static <V> List<Set<V>> find(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyList();
        }

        if (!graph.isDirected()) {
            return ConnectedComponentsUtil.find(graph);
        }

        TarjanState<V> state = new TarjanState<>();

        for (V vertex : graph.vertices()) {
            if (!state.indexMap.containsKey(vertex)) {
                tarjanIterative(graph, vertex, state);
            }
        }

        return state.result;
    }

    /**
     * Count the number of strongly connected components.
     * 统计强连通分量的数量。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return number of SCCs | 强连通分量数量
     */
    public static <V> int count(Graph<V> graph) {
        return find(graph).size();
    }

    /**
     * Find the SCC containing the given vertex.
     * 查找包含给定顶点的强连通分量。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param vertex the vertex to find | 要查找的顶点
     * @return the SCC containing the vertex | 包含该顶点的强连通分量
     * @throws InvalidVertexException if vertex is null | 当顶点为null时抛出
     */
    public static <V> Set<V> componentOf(Graph<V> graph, V vertex) {
        if (vertex == null) {
            throw new InvalidVertexException("Vertex cannot be null");
        }
        if (graph == null || graph.isEmpty() || !graph.containsVertex(vertex)) {
            return Collections.emptySet();
        }

        List<Set<V>> sccs = find(graph);
        for (Set<V> scc : sccs) {
            if (scc.contains(vertex)) {
                return scc;
            }
        }
        return Collections.emptySet();
    }

    /**
     * Check if the entire graph is strongly connected.
     * 检查整个图是否为强连通。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if the graph is strongly connected | 如果图是强连通的返回true
     */
    public static <V> boolean isStronglyConnected(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return true;
        }
        List<Set<V>> sccs = find(graph);
        return sccs.size() == 1;
    }

    /**
     * Build the condensation graph (DAG of SCCs).
     * Each vertex in the result is a Set representing an SCC.
     * 构建缩合图（强连通分量的有向无环图）。结果中每个顶点是一个表示SCC的集合。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return condensation graph (DAG) | 缩合图（有向无环图）
     */
    public static <V> Graph<Set<V>> condensation(Graph<V> graph) {
        DirectedGraph<Set<V>> dag = new DirectedGraph<>();

        if (graph == null || graph.isEmpty()) {
            return dag;
        }

        List<Set<V>> sccs = find(graph);

        // Map each vertex to its SCC
        Map<V, Set<V>> vertexToScc = new HashMap<>();
        for (Set<V> scc : sccs) {
            dag.addVertex(scc);
            for (V v : scc) {
                vertexToScc.put(v, scc);
            }
        }

        // Add edges between SCCs
        for (Edge<V> edge : graph.edges()) {
            Set<V> fromScc = vertexToScc.get(edge.from());
            Set<V> toScc = vertexToScc.get(edge.to());
            if (fromScc != toScc && !dag.containsEdge(fromScc, toScc)) {
                dag.addEdge(fromScc, toScc);
            }
        }

        return dag;
    }

    /**
     * Iterative Tarjan's algorithm to avoid stack overflow on large graphs.
     * 迭代式Tarjan算法，避免大图上的栈溢出。
     */
    private static <V> void tarjanIterative(Graph<V> graph, V start, TarjanState<V> state) {
        Deque<TarjanFrame<V>> callStack = new ArrayDeque<>();
        callStack.push(new TarjanFrame<>(start, graph.outEdges(start).iterator()));
        state.indexMap.put(start, state.index);
        state.lowLinkMap.put(start, state.index);
        state.index++;
        state.onStack.add(start);
        state.stack.push(start);

        while (!callStack.isEmpty()) {
            TarjanFrame<V> frame = callStack.peek();
            V v = frame.vertex;

            if (frame.neighborIterator.hasNext()) {
                Edge<V> edge = frame.neighborIterator.next();
                V w = edge.to();

                if (!state.indexMap.containsKey(w)) {
                    // Not yet visited — push onto call stack
                    callStack.push(new TarjanFrame<>(w, graph.outEdges(w).iterator()));
                    state.indexMap.put(w, state.index);
                    state.lowLinkMap.put(w, state.index);
                    state.index++;
                    state.onStack.add(w);
                    state.stack.push(w);
                } else if (state.onStack.contains(w)) {
                    // w is on the stack → update lowlink
                    state.lowLinkMap.put(v,
                            Math.min(state.lowLinkMap.get(v), state.indexMap.get(w)));
                }
            } else {
                // All neighbors processed — check if v is root of SCC
                if (state.lowLinkMap.get(v).equals(state.indexMap.get(v))) {
                    Set<V> scc = new LinkedHashSet<>();
                    V w;
                    do {
                        w = state.stack.pop();
                        state.onStack.remove(w);
                        scc.add(w);
                    } while (!w.equals(v));
                    state.result.add(scc);
                }

                callStack.pop();

                // Update parent's lowlink
                if (!callStack.isEmpty()) {
                    V parent = callStack.peek().vertex;
                    state.lowLinkMap.put(parent,
                            Math.min(state.lowLinkMap.get(parent), state.lowLinkMap.get(v)));
                }
            }
        }
    }

    /**
     * Mutable state for Tarjan's algorithm.
     */
    private static final class TarjanState<V> {
        int index = 0;
        final Map<V, Integer> indexMap = new HashMap<>();
        final Map<V, Integer> lowLinkMap = new HashMap<>();
        final Deque<V> stack = new ArrayDeque<>();
        final Set<V> onStack = new HashSet<>();
        final List<Set<V>> result = new ArrayList<>();
    }

    /**
     * Frame for iterative DFS in Tarjan's algorithm.
     */
    private static final class TarjanFrame<V> {
        final V vertex;
        final Iterator<Edge<V>> neighborIterator;

        TarjanFrame(V vertex, Iterator<Edge<V>> neighborIterator) {
            this.vertex = vertex;
            this.neighborIterator = neighborIterator;
        }
    }
}
