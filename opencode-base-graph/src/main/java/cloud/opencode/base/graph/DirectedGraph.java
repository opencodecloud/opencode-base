package cloud.opencode.base.graph;

import cloud.opencode.base.graph.exception.InvalidVertexException;
import cloud.opencode.base.graph.node.Edge;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Directed Graph
 * 有向图
 *
 * <p>Implementation of a directed graph using adjacency list.</p>
 * <p>使用邻接表实现的有向图。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Thread-safe (uses ConcurrentHashMap) | 线程安全（使用ConcurrentHashMap）</li>
 *   <li>Supports weighted edges | 支持加权边</li>
 *   <li>O(1) vertex/edge lookup | O(1)顶点/边查找</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> graph = new DirectedGraph<>();
 * graph.addEdge("A", "B", 1.0);
 * graph.addEdge("B", "C", 2.0);
 * Set<String> neighbors = graph.neighbors("A");  // ["B"]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap) - 线程安全: 是（使用ConcurrentHashMap）</li>
 *   <li>Null-safe: Yes (rejects null vertices) - 空值安全: 是（拒绝null顶点）</li>
 * </ul>
 *
 * @param <V> the vertex type | 顶点类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public class DirectedGraph<V> implements Graph<V> {

    private final Map<V, Set<Edge<V>>> adjacencyList = new ConcurrentHashMap<>();
    private final Map<V, Set<Edge<V>>> reverseAdjacencyList = new ConcurrentHashMap<>();

    /**
     * Create an empty directed graph
     * 创建空的有向图
     */
    public DirectedGraph() {
    }

    @Override
    public void addVertex(V vertex) {
        if (vertex == null) {
            throw new InvalidVertexException("Vertex cannot be null");
        }
        adjacencyList.putIfAbsent(vertex, ConcurrentHashMap.newKeySet());
        reverseAdjacencyList.putIfAbsent(vertex, ConcurrentHashMap.newKeySet());
    }

    @Override
    public void addEdge(V from, V to) {
        addEdge(from, to, Edge.DEFAULT_WEIGHT);
    }

    @Override
    public void addEdge(V from, V to, double weight) {
        if (from == null || to == null) {
            throw new InvalidVertexException("Vertex cannot be null");
        }
        addVertex(from);
        addVertex(to);

        Edge<V> edge = new Edge<>(from, to, weight);
        adjacencyList.get(from).add(edge);
        reverseAdjacencyList.get(to).add(edge);
    }

    @Override
    public void removeVertex(V vertex) {
        if (vertex == null || !containsVertex(vertex)) {
            return;
        }

        // Remove all outgoing edges
        Set<Edge<V>> outEdges = adjacencyList.remove(vertex);
        if (outEdges != null) {
            for (Edge<V> edge : outEdges) {
                Set<Edge<V>> inEdges = reverseAdjacencyList.get(edge.to());
                if (inEdges != null) {
                    inEdges.removeIf(e -> e.from().equals(vertex));
                }
            }
        }

        // Remove all incoming edges
        Set<Edge<V>> inEdges = reverseAdjacencyList.remove(vertex);
        if (inEdges != null) {
            for (Edge<V> edge : inEdges) {
                Set<Edge<V>> outgoing = adjacencyList.get(edge.from());
                if (outgoing != null) {
                    outgoing.removeIf(e -> e.to().equals(vertex));
                }
            }
        }
    }

    @Override
    public void removeEdge(V from, V to) {
        if (from == null || to == null) {
            return;
        }

        Set<Edge<V>> outEdges = adjacencyList.get(from);
        if (outEdges != null) {
            outEdges.removeIf(e -> e.to().equals(to));
        }

        Set<Edge<V>> inEdges = reverseAdjacencyList.get(to);
        if (inEdges != null) {
            inEdges.removeIf(e -> e.from().equals(from));
        }
    }

    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(adjacencyList.keySet());
    }

    @Override
    public Set<Edge<V>> edges() {
        return adjacencyList.values().stream()
            .flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<V> neighbors(V vertex) {
        Set<Edge<V>> edges = adjacencyList.get(vertex);
        if (edges == null) {
            return Set.of();
        }
        return edges.stream()
            .map(Edge::to)
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<Edge<V>> outEdges(V vertex) {
        Set<Edge<V>> edges = adjacencyList.get(vertex);
        return edges != null ? Collections.unmodifiableSet(edges) : Set.of();
    }

    @Override
    public Set<Edge<V>> inEdges(V vertex) {
        Set<Edge<V>> edges = reverseAdjacencyList.get(vertex);
        return edges != null ? Collections.unmodifiableSet(edges) : Set.of();
    }

    @Override
    public int vertexCount() {
        return adjacencyList.size();
    }

    @Override
    public int edgeCount() {
        return adjacencyList.values().stream()
            .mapToInt(Set::size)
            .sum();
    }

    @Override
    public boolean containsVertex(V vertex) {
        return vertex != null && adjacencyList.containsKey(vertex);
    }

    @Override
    public boolean containsEdge(V from, V to) {
        if (from == null || to == null) {
            return false;
        }
        Set<Edge<V>> edges = adjacencyList.get(from);
        if (edges == null) {
            return false;
        }
        return edges.stream().anyMatch(e -> e.to().equals(to));
    }

    @Override
    public double getWeight(V from, V to) {
        if (from == null || to == null) {
            return Double.MAX_VALUE;
        }
        Set<Edge<V>> edges = adjacencyList.get(from);
        if (edges == null) {
            return Double.MAX_VALUE;
        }
        return edges.stream()
            .filter(e -> e.to().equals(to))
            .findFirst()
            .map(Edge::weight)
            .orElse(Double.MAX_VALUE);
    }

    @Override
    public boolean isDirected() {
        return true;
    }

    @Override
    public void clear() {
        adjacencyList.clear();
        reverseAdjacencyList.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DirectedGraph {\n");
        for (V vertex : adjacencyList.keySet()) {
            sb.append("  ").append(vertex).append(" -> ");
            sb.append(neighbors(vertex));
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
