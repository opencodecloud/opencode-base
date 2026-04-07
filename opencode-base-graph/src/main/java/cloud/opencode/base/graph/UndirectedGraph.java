package cloud.opencode.base.graph;

import cloud.opencode.base.graph.exception.InvalidVertexException;
import cloud.opencode.base.graph.node.Edge;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Undirected Graph
 * 无向图
 *
 * <p>Implementation of an undirected graph using adjacency list.</p>
 * <p>使用邻接表实现的无向图。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Thread-safe (uses ConcurrentHashMap) | 线程安全（使用ConcurrentHashMap）</li>
 *   <li>Supports weighted edges | 支持加权边</li>
 *   <li>Bidirectional edge storage | 双向边存储</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> graph = new UndirectedGraph<>();
 * graph.addEdge("A", "B");
 * // Both A->B and B->A are created
 * Set<String> aNeighbors = graph.neighbors("A");  // ["B"]
 * Set<String> bNeighbors = graph.neighbors("B");  // ["A"]
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
public class UndirectedGraph<V> implements Graph<V> {

    private final Map<V, Set<Edge<V>>> adjacencyList = new ConcurrentHashMap<>();

    /**
     * Create an empty undirected graph
     * 创建空的无向图
     */
    public UndirectedGraph() {
    }

    @Override
    public void addVertex(V vertex) {
        if (vertex == null) {
            throw new InvalidVertexException("Vertex cannot be null");
        }
        adjacencyList.putIfAbsent(vertex, ConcurrentHashMap.newKeySet());
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

        // Add edges in both directions
        adjacencyList.get(from).add(new Edge<>(from, to, weight));
        adjacencyList.get(to).add(new Edge<>(to, from, weight));
    }

    @Override
    public void removeVertex(V vertex) {
        if (vertex == null || !containsVertex(vertex)) {
            return;
        }

        // Remove all edges connected to this vertex
        Set<Edge<V>> edges = adjacencyList.remove(vertex);
        if (edges != null) {
            for (Edge<V> edge : edges) {
                Set<Edge<V>> neighborEdges = adjacencyList.get(edge.to());
                if (neighborEdges != null) {
                    neighborEdges.removeIf(e -> e.to().equals(vertex));
                }
            }
        }
    }

    @Override
    public void removeEdge(V from, V to) {
        if (from == null || to == null) {
            return;
        }

        Set<Edge<V>> fromEdges = adjacencyList.get(from);
        if (fromEdges != null) {
            fromEdges.removeIf(e -> e.to().equals(to));
        }

        Set<Edge<V>> toEdges = adjacencyList.get(to);
        if (toEdges != null) {
            toEdges.removeIf(e -> e.to().equals(from));
        }
    }

    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(adjacencyList.keySet());
    }

    @Override
    public Set<Edge<V>> edges() {
        Set<Edge<V>> allEdges = new HashSet<>();
        Set<V> visited = new HashSet<>();

        for (Map.Entry<V, Set<Edge<V>>> entry : adjacencyList.entrySet()) {
            V vertex = entry.getKey();
            for (Edge<V> edge : entry.getValue()) {
                // Only add edge (u,v) when processing u and v hasn't been visited yet,
                // or it's a self-loop (from == to). This avoids the (v,u) duplicate.
                if (!visited.contains(edge.to()) || edge.from().equals(edge.to())) {
                    allEdges.add(edge);
                }
            }
            visited.add(vertex);
        }

        return Collections.unmodifiableSet(allEdges);
    }

    @Override
    public Set<V> neighbors(V vertex) {
        Set<Edge<V>> edges = adjacencyList.get(vertex);
        if (edges == null) {
            return Set.of();
        }
        Set<V> result = HashSet.newHashSet(edges.size());
        for (Edge<V> edge : edges) {
            result.add(edge.to());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Set<Edge<V>> outEdges(V vertex) {
        Set<Edge<V>> edges = adjacencyList.get(vertex);
        return edges != null ? Collections.unmodifiableSet(edges) : Set.of();
    }

    @Override
    public Set<Edge<V>> inEdges(V vertex) {
        // In undirected graph, in-edges = out-edges
        return outEdges(vertex);
    }

    @Override
    public int vertexCount() {
        return adjacencyList.size();
    }

    @Override
    public int edgeCount() {
        // Each non-self-loop edge is stored twice, so divide by 2.
        // Self-loops are stored once (same edge object in one set), so count them separately.
        int totalEntries = 0;
        int selfLoops = 0;
        for (Map.Entry<V, Set<Edge<V>>> entry : adjacencyList.entrySet()) {
            for (Edge<V> edge : entry.getValue()) {
                totalEntries++;
                if (edge.from().equals(edge.to())) {
                    selfLoops++;
                }
            }
        }
        return (totalEntries - selfLoops) / 2 + selfLoops;
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
        return false;
    }

    @Override
    public void clear() {
        adjacencyList.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("UndirectedGraph {\n");
        for (V vertex : adjacencyList.keySet()) {
            sb.append("  ").append(vertex).append(" -- ");
            sb.append(neighbors(vertex));
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
