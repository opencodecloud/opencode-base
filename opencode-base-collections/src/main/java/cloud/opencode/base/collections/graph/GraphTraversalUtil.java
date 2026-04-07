package cloud.opencode.base.collections.graph;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * GraphTraversalUtil - Graph Traversal Utilities
 * GraphTraversalUtil - 图遍历工具类
 *
 * <p>Provides common graph traversal algorithms including BFS, DFS, topological sort, etc.</p>
 * <p>提供常见的图遍历算法，包括 BFS、DFS、拓扑排序等。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Breadth-first search (BFS) - 广度优先搜索</li>
 *   <li>Depth-first search (DFS) - 深度优先搜索</li>
 *   <li>Topological sort - 拓扑排序</li>
 *   <li>Cycle detection - 环检测</li>
 *   <li>Path finding - 路径查找</li>
 *   <li>Connected components - 连通分量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MutableGraph<String> graph = MutableGraph.directed();
 * graph.putEdge("A", "B");
 * graph.putEdge("B", "C");
 * graph.putEdge("A", "C");
 *
 * // BFS traversal
 * List<String> bfsOrder = GraphTraversalUtil.bfs(graph, "A");
 *
 * // DFS traversal
 * List<String> dfsOrder = GraphTraversalUtil.dfs(graph, "A");
 *
 * // Topological sort
 * List<String> topoOrder = GraphTraversalUtil.topologicalSort(graph);
 *
 * // Check for cycles
 * boolean hasCycle = GraphTraversalUtil.hasCycle(graph);
 *
 * // Find shortest path
 * List<String> path = GraphTraversalUtil.shortestPath(graph, "A", "C");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>BFS/DFS: O(V + E) - BFS/DFS: O(V + E)</li>
 *   <li>Topological sort: O(V + E) - 拓扑排序: O(V + E)</li>
 *   <li>Cycle detection: O(V + E) - 环检测: O(V + E)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (depends on graph) - 线程安全: 否（取决于图）</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class GraphTraversalUtil {

    /**
     * Maximum recursion depth for recursive graph traversal methods.
     * 递归图遍历方法的最大递归深度。
     */
    private static final int MAX_RECURSION_DEPTH = 10_000;

    private GraphTraversalUtil() {
        throw new AssertionError("No instances");
    }

    // ==================== BFS 遍历 | BFS Traversal ====================

    /**
     * Perform breadth-first search starting from the given node.
     * 从给定节点开始执行广度优先搜索。
     *
     * @param <N>       node type | 节点类型
     * @param graph     the graph | 图
     * @param startNode the starting node | 起始节点
     * @return nodes in BFS order | BFS 顺序的节点
     */
    public static <N> List<N> bfs(Graph<N> graph, N startNode) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(startNode, "Start node cannot be null");

        if (!graph.hasNode(startNode)) {
            return Collections.emptyList();
        }

        List<N> result = new ArrayList<>();
        Set<N> visited = new HashSet<>();
        Queue<N> queue = new LinkedList<>();

        queue.offer(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            N current = queue.poll();
            result.add(current);

            for (N neighbor : graph.successors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return result;
    }

    /**
     * Perform BFS with a visitor function.
     * 使用访问者函数执行 BFS。
     *
     * @param <N>       node type | 节点类型
     * @param graph     the graph | 图
     * @param startNode the starting node | 起始节点
     * @param visitor   the visitor function | 访问者函数
     */
    public static <N> void bfs(Graph<N> graph, N startNode, Consumer<N> visitor) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(startNode, "Start node cannot be null");
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        if (!graph.hasNode(startNode)) {
            return;
        }

        Set<N> visited = new HashSet<>();
        Queue<N> queue = new LinkedList<>();

        queue.offer(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            N current = queue.poll();
            visitor.accept(current);

            for (N neighbor : graph.successors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
    }

    /**
     * Perform BFS until a condition is met.
     * 执行 BFS 直到满足条件。
     *
     * @param <N>       node type | 节点类型
     * @param graph     the graph | 图
     * @param startNode the starting node | 起始节点
     * @param condition the stop condition | 停止条件
     * @return the first node matching condition, or empty | 第一个匹配条件的节点，或空
     */
    public static <N> Optional<N> bfsUntil(Graph<N> graph, N startNode, Predicate<N> condition) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(startNode, "Start node cannot be null");
        Objects.requireNonNull(condition, "Condition cannot be null");

        if (!graph.hasNode(startNode)) {
            return Optional.empty();
        }

        Set<N> visited = new HashSet<>();
        Queue<N> queue = new LinkedList<>();

        queue.offer(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            N current = queue.poll();
            if (condition.test(current)) {
                return Optional.of(current);
            }

            for (N neighbor : graph.successors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return Optional.empty();
    }

    // ==================== DFS 遍历 | DFS Traversal ====================

    /**
     * Perform depth-first search starting from the given node.
     * 从给定节点开始执行深度优先搜索。
     *
     * @param <N>       node type | 节点类型
     * @param graph     the graph | 图
     * @param startNode the starting node | 起始节点
     * @return nodes in DFS order | DFS 顺序的节点
     */
    public static <N> List<N> dfs(Graph<N> graph, N startNode) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(startNode, "Start node cannot be null");

        if (!graph.hasNode(startNode)) {
            return Collections.emptyList();
        }

        List<N> result = new ArrayList<>();
        Set<N> visited = new HashSet<>();
        dfsRecursive(graph, startNode, visited, result, 0);
        return result;
    }

    private static <N> void dfsRecursive(Graph<N> graph, N node, Set<N> visited, List<N> result, int depth) {
        if (depth >= MAX_RECURSION_DEPTH) {
            throw new OpenCollectionException("Graph recursion depth exceeded");
        }
        visited.add(node);
        result.add(node);

        for (N neighbor : graph.successors(node)) {
            if (!visited.contains(neighbor)) {
                dfsRecursive(graph, neighbor, visited, result, depth + 1);
            }
        }
    }

    /**
     * Perform iterative depth-first search.
     * 执行迭代式深度优先搜索。
     *
     * @param <N>       node type | 节点类型
     * @param graph     the graph | 图
     * @param startNode the starting node | 起始节点
     * @return nodes in DFS order | DFS 顺序的节点
     */
    public static <N> List<N> dfsIterative(Graph<N> graph, N startNode) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(startNode, "Start node cannot be null");

        if (!graph.hasNode(startNode)) {
            return Collections.emptyList();
        }

        List<N> result = new ArrayList<>();
        Set<N> visited = new HashSet<>();
        Deque<N> stack = new ArrayDeque<>();

        stack.push(startNode);

        while (!stack.isEmpty()) {
            N current = stack.pop();
            if (!visited.contains(current)) {
                visited.add(current);
                result.add(current);

                // Push neighbors in reverse order for consistent ordering
                List<N> neighbors = new ArrayList<>(graph.successors(current));
                Collections.reverse(neighbors);
                for (N neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        stack.push(neighbor);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Perform DFS with a visitor function.
     * 使用访问者函数执行 DFS。
     *
     * @param <N>       node type | 节点类型
     * @param graph     the graph | 图
     * @param startNode the starting node | 起始节点
     * @param visitor   the visitor function | 访问者函数
     */
    public static <N> void dfs(Graph<N> graph, N startNode, Consumer<N> visitor) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(startNode, "Start node cannot be null");
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        if (!graph.hasNode(startNode)) {
            return;
        }

        Set<N> visited = new HashSet<>();
        dfsWithVisitor(graph, startNode, visited, visitor, 0);
    }

    private static <N> void dfsWithVisitor(Graph<N> graph, N node, Set<N> visited, Consumer<N> visitor, int depth) {
        if (depth >= MAX_RECURSION_DEPTH) {
            throw new OpenCollectionException("Graph recursion depth exceeded");
        }
        visited.add(node);
        visitor.accept(node);

        for (N neighbor : graph.successors(node)) {
            if (!visited.contains(neighbor)) {
                dfsWithVisitor(graph, neighbor, visited, visitor, depth + 1);
            }
        }
    }

    // ==================== 拓扑排序 | Topological Sort ====================

    /**
     * Perform topological sort on a directed acyclic graph (DAG).
     * 在有向无环图 (DAG) 上执行拓扑排序。
     *
     * @param <N>   node type | 节点类型
     * @param graph the graph | 图
     * @return nodes in topological order | 拓扑顺序的节点
     * @throws IllegalArgumentException if graph has a cycle | 如果图有环则抛出异常
     */
    public static <N> List<N> topologicalSort(Graph<N> graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort requires a directed graph");
        }

        Set<N> visited = new HashSet<>();
        Set<N> inProgress = new HashSet<>();
        Deque<N> result = new ArrayDeque<>();

        for (N node : graph.nodes()) {
            if (!visited.contains(node)) {
                topologicalSortDfs(graph, node, visited, inProgress, result, 0);
            }
        }

        return new ArrayList<>(result);
    }

    private static <N> void topologicalSortDfs(Graph<N> graph, N node, Set<N> visited,
                                               Set<N> inProgress, Deque<N> result, int depth) {
        if (depth >= MAX_RECURSION_DEPTH) {
            throw new OpenCollectionException("Graph recursion depth exceeded");
        }
        if (inProgress.contains(node)) {
            throw new IllegalArgumentException("Graph has a cycle, topological sort not possible");
        }
        if (visited.contains(node)) {
            return;
        }

        inProgress.add(node);
        for (N neighbor : graph.successors(node)) {
            topologicalSortDfs(graph, neighbor, visited, inProgress, result, depth + 1);
        }
        inProgress.remove(node);
        visited.add(node);
        result.addFirst(node);
    }

    /**
     * Perform topological sort using Kahn's algorithm.
     * 使用 Kahn 算法执行拓扑排序。
     *
     * @param <N>   node type | 节点类型
     * @param graph the graph | 图
     * @return nodes in topological order, or empty if cycle exists | 拓扑顺序的节点，如果有环则返回空
     */
    public static <N> Optional<List<N>> topologicalSortKahn(Graph<N> graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort requires a directed graph");
        }

        // Calculate in-degrees
        Map<N, Integer> inDegree = new HashMap<>();
        for (N node : graph.nodes()) {
            inDegree.put(node, graph.inDegree(node));
        }

        // Find nodes with zero in-degree
        Queue<N> queue = new LinkedList<>();
        for (N node : graph.nodes()) {
            if (inDegree.get(node) == 0) {
                queue.offer(node);
            }
        }

        List<N> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            N current = queue.poll();
            result.add(current);

            for (N neighbor : graph.successors(current)) {
                int newDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDegree);
                if (newDegree == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        if (result.size() != graph.nodeCount()) {
            return Optional.empty(); // Cycle detected
        }

        return Optional.of(result);
    }

    // ==================== 环检测 | Cycle Detection ====================

    /**
     * Check if the graph has a cycle.
     * 检查图是否有环。
     *
     * @param <N>   node type | 节点类型
     * @param graph the graph | 图
     * @return true if has cycle | 如果有环则返回 true
     */
    public static <N> boolean hasCycle(Graph<N> graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        if (graph.isDirected()) {
            return hasCycleDirected(graph);
        } else {
            return hasCycleUndirected(graph);
        }
    }

    private static <N> boolean hasCycleDirected(Graph<N> graph) {
        Set<N> visited = new HashSet<>();
        Set<N> inProgress = new HashSet<>();

        for (N node : graph.nodes()) {
            if (!visited.contains(node)) {
                if (hasCycleDirectedDfs(graph, node, visited, inProgress, 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static <N> boolean hasCycleDirectedDfs(Graph<N> graph, N node,
                                                   Set<N> visited, Set<N> inProgress, int depth) {
        if (depth >= MAX_RECURSION_DEPTH) {
            throw new OpenCollectionException("Graph recursion depth exceeded");
        }
        inProgress.add(node);

        for (N neighbor : graph.successors(node)) {
            if (inProgress.contains(neighbor)) {
                return true;
            }
            if (!visited.contains(neighbor)) {
                if (hasCycleDirectedDfs(graph, neighbor, visited, inProgress, depth + 1)) {
                    return true;
                }
            }
        }

        inProgress.remove(node);
        visited.add(node);
        return false;
    }

    private static <N> boolean hasCycleUndirected(Graph<N> graph) {
        Set<N> visited = new HashSet<>();

        for (N node : graph.nodes()) {
            if (!visited.contains(node)) {
                if (hasCycleUndirectedDfs(graph, node, null, visited, 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static <N> boolean hasCycleUndirectedDfs(Graph<N> graph, N node, N parent, Set<N> visited, int depth) {
        if (depth >= MAX_RECURSION_DEPTH) {
            throw new OpenCollectionException("Graph recursion depth exceeded");
        }
        visited.add(node);

        for (N neighbor : graph.adjacentNodes(node)) {
            if (!visited.contains(neighbor)) {
                if (hasCycleUndirectedDfs(graph, neighbor, node, visited, depth + 1)) {
                    return true;
                }
            } else if (!neighbor.equals(parent)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 路径查找 | Path Finding ====================

    /**
     * Find the shortest path between two nodes using BFS.
     * 使用 BFS 找到两个节点之间的最短路径。
     *
     * @param <N>    node type | 节点类型
     * @param graph  the graph | 图
     * @param source the source node | 源节点
     * @param target the target node | 目标节点
     * @return the shortest path, or empty if no path exists | 最短路径，如果不存在则返回空
     */
    public static <N> Optional<List<N>> shortestPath(Graph<N> graph, N source, N target) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(source, "Source cannot be null");
        Objects.requireNonNull(target, "Target cannot be null");

        if (!graph.hasNode(source) || !graph.hasNode(target)) {
            return Optional.empty();
        }

        if (source.equals(target)) {
            return Optional.of(Collections.singletonList(source));
        }

        Map<N, N> parentMap = new HashMap<>();
        Set<N> visited = new HashSet<>();
        Queue<N> queue = new LinkedList<>();

        queue.offer(source);
        visited.add(source);
        parentMap.put(source, null);

        while (!queue.isEmpty()) {
            N current = queue.poll();

            for (N neighbor : graph.successors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.offer(neighbor);

                    if (neighbor.equals(target)) {
                        return Optional.of(reconstructPath(parentMap, target));
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static <N> List<N> reconstructPath(Map<N, N> parentMap, N target) {
        LinkedList<N> path = new LinkedList<>();
        N current = target;
        while (current != null) {
            path.addFirst(current);
            current = parentMap.get(current);
        }
        return path;
    }

    /**
     * Check if there is a path between two nodes.
     * 检查两个节点之间是否有路径。
     *
     * @param <N>    node type | 节点类型
     * @param graph  the graph | 图
     * @param source the source node | 源节点
     * @param target the target node | 目标节点
     * @return true if path exists | 如果路径存在则返回 true
     */
    public static <N> boolean hasPath(Graph<N> graph, N source, N target) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(source, "Source cannot be null");
        Objects.requireNonNull(target, "Target cannot be null");

        if (!graph.hasNode(source) || !graph.hasNode(target)) {
            return false;
        }

        if (source.equals(target)) {
            return true;
        }

        Set<N> visited = new HashSet<>();
        Queue<N> queue = new LinkedList<>();

        queue.offer(source);
        visited.add(source);

        while (!queue.isEmpty()) {
            N current = queue.poll();

            for (N neighbor : graph.successors(current)) {
                if (neighbor.equals(target)) {
                    return true;
                }
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return false;
    }

    /**
     * Find all paths between two nodes (limited by max depth).
     * 找到两个节点之间的所有路径（受最大深度限制）。
     *
     * @param <N>      node type | 节点类型
     * @param graph    the graph | 图
     * @param source   the source node | 源节点
     * @param target   the target node | 目标节点
     * @param maxDepth maximum path depth | 最大路径深度
     * @return all paths | 所有路径
     */
    public static <N> List<List<N>> allPaths(Graph<N> graph, N source, N target, int maxDepth) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(source, "Source cannot be null");
        Objects.requireNonNull(target, "Target cannot be null");

        if (!graph.hasNode(source) || !graph.hasNode(target) || maxDepth < 1) {
            return Collections.emptyList();
        }

        List<List<N>> result = new ArrayList<>();
        List<N> currentPath = new ArrayList<>();
        Set<N> visited = new HashSet<>();

        currentPath.add(source);
        visited.add(source);
        allPathsDfs(graph, source, target, currentPath, visited, result, maxDepth);

        return result;
    }

    private static <N> void allPathsDfs(Graph<N> graph, N current, N target,
                                        List<N> currentPath, Set<N> visited,
                                        List<List<N>> result, int maxDepth) {
        if (current.equals(target)) {
            result.add(new ArrayList<>(currentPath));
            return;
        }

        if (currentPath.size() >= maxDepth) {
            return;
        }

        for (N neighbor : graph.successors(current)) {
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                currentPath.add(neighbor);
                allPathsDfs(graph, neighbor, target, currentPath, visited, result, maxDepth);
                currentPath.removeLast();
                visited.remove(neighbor);
            }
        }
    }

    // ==================== 连通分量 | Connected Components ====================

    /**
     * Find all connected components in an undirected graph.
     * 找到无向图中的所有连通分量。
     *
     * @param <N>   node type | 节点类型
     * @param graph the graph | 图
     * @return list of connected components | 连通分量列表
     */
    public static <N> List<Set<N>> connectedComponents(Graph<N> graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        if (graph.isDirected()) {
            throw new IllegalArgumentException("Connected components requires an undirected graph");
        }

        List<Set<N>> components = new ArrayList<>();
        Set<N> visited = new HashSet<>();

        for (N node : graph.nodes()) {
            if (!visited.contains(node)) {
                Set<N> component = new HashSet<>();
                bfsCollect(graph, node, visited, component);
                components.add(component);
            }
        }

        return components;
    }

    private static <N> void bfsCollect(Graph<N> graph, N start, Set<N> visited, Set<N> component) {
        Queue<N> queue = new LinkedList<>();
        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            N current = queue.poll();
            component.add(current);

            for (N neighbor : graph.adjacentNodes(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
    }

    /**
     * Find all strongly connected components in a directed graph (Kosaraju's algorithm).
     * 使用 Kosaraju 算法找到有向图中的所有强连通分量。
     *
     * @param <N>   node type | 节点类型
     * @param graph the graph | 图
     * @return list of strongly connected components | 强连通分量列表
     */
    public static <N> List<Set<N>> stronglyConnectedComponents(Graph<N> graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Strongly connected components requires a directed graph");
        }

        // First pass: get finishing order
        Set<N> visited = new HashSet<>();
        Deque<N> finishOrder = new ArrayDeque<>();

        for (N node : graph.nodes()) {
            if (!visited.contains(node)) {
                sccFirstDfs(graph, node, visited, finishOrder, 0);
            }
        }

        // Second pass: find SCCs in reverse graph
        visited.clear();
        List<Set<N>> components = new ArrayList<>();

        while (!finishOrder.isEmpty()) {
            N node = finishOrder.pollFirst();
            if (!visited.contains(node)) {
                Set<N> component = new HashSet<>();
                sccSecondDfs(graph, node, visited, component, 0);
                components.add(component);
            }
        }

        return components;
    }

    private static <N> void sccFirstDfs(Graph<N> graph, N node, Set<N> visited, Deque<N> finishOrder, int depth) {
        if (depth >= MAX_RECURSION_DEPTH) {
            throw new OpenCollectionException("Graph recursion depth exceeded");
        }
        visited.add(node);
        for (N neighbor : graph.successors(node)) {
            if (!visited.contains(neighbor)) {
                sccFirstDfs(graph, neighbor, visited, finishOrder, depth + 1);
            }
        }
        finishOrder.addFirst(node);
    }

    private static <N> void sccSecondDfs(Graph<N> graph, N node, Set<N> visited, Set<N> component, int depth) {
        if (depth >= MAX_RECURSION_DEPTH) {
            throw new OpenCollectionException("Graph recursion depth exceeded");
        }
        visited.add(node);
        component.add(node);
        // Use predecessors (reverse edges)
        for (N neighbor : graph.predecessors(node)) {
            if (!visited.contains(neighbor)) {
                sccSecondDfs(graph, neighbor, visited, component, depth + 1);
            }
        }
    }

    // ==================== 工具方法 | Utility Methods ====================

    /**
     * Get all reachable nodes from a starting node.
     * 获取从起始节点可达的所有节点。
     *
     * @param <N>       node type | 节点类型
     * @param graph     the graph | 图
     * @param startNode the starting node | 起始节点
     * @return set of reachable nodes | 可达节点集合
     */
    public static <N> Set<N> reachableFrom(Graph<N> graph, N startNode) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(startNode, "Start node cannot be null");

        if (!graph.hasNode(startNode)) {
            return Collections.emptySet();
        }

        Set<N> reachable = new HashSet<>();
        Queue<N> queue = new LinkedList<>();

        queue.offer(startNode);
        reachable.add(startNode);

        while (!queue.isEmpty()) {
            N current = queue.poll();
            for (N neighbor : graph.successors(current)) {
                if (!reachable.contains(neighbor)) {
                    reachable.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return reachable;
    }

    /**
     * Check if the graph is connected (for undirected graphs).
     * 检查图是否连通（针对无向图）。
     *
     * @param <N>   node type | 节点类型
     * @param graph the graph | 图
     * @return true if connected | 如果连通则返回 true
     */
    public static <N> boolean isConnected(Graph<N> graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        if (graph.isDirected()) {
            throw new IllegalArgumentException("isConnected requires an undirected graph");
        }

        if (graph.nodeCount() == 0) {
            return true;
        }

        N start = graph.nodes().iterator().next();
        Set<N> reachable = reachableFrom(graph, start);
        return reachable.size() == graph.nodeCount();
    }

    /**
     * Check if the graph is a DAG (Directed Acyclic Graph).
     * 检查图是否是 DAG（有向无环图）。
     *
     * @param <N>   node type | 节点类型
     * @param graph the graph | 图
     * @return true if DAG | 如果是 DAG 则返回 true
     */
    public static <N> boolean isDag(Graph<N> graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        if (!graph.isDirected()) {
            return false;
        }

        return !hasCycle(graph);
    }

    // ==================== Dijkstra 最短路径 | Dijkstra Shortest Path ====================

    /**
     * Computes shortest distances from source to all reachable nodes using Dijkstra's algorithm.
     * 使用 Dijkstra 算法计算从源节点到所有可达节点的最短距离。
     *
     * <p>The graph edge values must be non-negative numbers. If a negative weight edge
     * is encountered, an {@link IllegalArgumentException} is thrown.</p>
     * <p>图的边值必须为非负数。如果遇到负权边，将抛出 {@link IllegalArgumentException}。</p>
     *
     * @param <N>    node type | 节点类型
     * @param graph  the value graph | 值图
     * @param source the source node | 源节点
     * @return map of node to shortest distance from source | 节点到源节点最短距离的映射
     * @throws IllegalArgumentException if a negative weight edge is found | 如果发现负权边则抛出异常
     * @since JDK 25, opencode-base-collections V1.0.3
     */
    public static <N> Map<N, Double> dijkstra(ValueGraph<N, ? extends Number> graph, N source) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(source, "Source cannot be null");

        if (!graph.nodes().contains(source)) {
            return Collections.emptyMap();
        }

        Map<N, Double> dist = new HashMap<>();
        Set<N> visited = new HashSet<>();
        PriorityQueue<Map.Entry<N, Double>> pq = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue)
        );

        // Initialize
        for (N node : graph.nodes()) {
            dist.put(node, Double.MAX_VALUE);
        }
        dist.put(source, 0.0);
        pq.offer(Map.entry(source, 0.0));

        while (!pq.isEmpty()) {
            Map.Entry<N, Double> current = pq.poll();
            N u = current.getKey();
            double distU = current.getValue();

            if (visited.contains(u)) {
                continue;
            }
            visited.add(u);

            for (N v : graph.successors(u)) {
                final N edgeU = u;
                final N edgeV = v;
                double weight = graph.edgeValue(u, v)
                        .map(Number::doubleValue)
                        .orElseThrow(() -> new IllegalStateException(
                                "Edge value missing for edge " + edgeU + " -> " + edgeV));

                if (weight < 0) {
                    throw new IllegalArgumentException(
                            "Negative edge weight not allowed in Dijkstra's algorithm: "
                                    + u + " -> " + v + " = " + weight);
                }

                double newDist = distU + weight;
                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    pq.offer(Map.entry(v, newDist));
                }
            }
        }

        // Remove unreachable nodes (those still at MAX_VALUE)
        dist.entrySet().removeIf(e -> e.getValue() >= Double.MAX_VALUE);

        return dist;
    }

    /**
     * Finds the shortest path from source to target using Dijkstra's algorithm.
     * 使用 Dijkstra 算法查找从源到目标的最短路径。
     *
     * <p>Returns the path as a list of nodes from source to target (inclusive).
     * Returns an empty list if the target is unreachable.</p>
     * <p>返回从源到目标的节点列表（包含两端）。如果目标不可达则返回空列表。</p>
     *
     * @param <N>    node type | 节点类型
     * @param graph  the value graph | 值图
     * @param source the source node | 源节点
     * @param target the target node | 目标节点
     * @return path as list of nodes, or empty list if unreachable | 节点路径列表，不可达则返回空列表
     * @throws IllegalArgumentException if a negative weight edge is found | 如果发现负权边则抛出异常
     * @since JDK 25, opencode-base-collections V1.0.3
     */
    public static <N> List<N> shortestWeightedPath(ValueGraph<N, ? extends Number> graph, N source, N target) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(source, "Source cannot be null");
        Objects.requireNonNull(target, "Target cannot be null");

        if (!graph.nodes().contains(source) || !graph.nodes().contains(target)) {
            return Collections.emptyList();
        }

        if (source.equals(target)) {
            return Collections.singletonList(source);
        }

        Map<N, Double> dist = new HashMap<>();
        Map<N, N> prev = new HashMap<>();
        Set<N> visited = new HashSet<>();
        PriorityQueue<Map.Entry<N, Double>> pq = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue)
        );

        // Initialize
        for (N node : graph.nodes()) {
            dist.put(node, Double.MAX_VALUE);
        }
        dist.put(source, 0.0);
        pq.offer(Map.entry(source, 0.0));

        while (!pq.isEmpty()) {
            Map.Entry<N, Double> current = pq.poll();
            N u = current.getKey();
            double distU = current.getValue();

            if (visited.contains(u)) {
                continue;
            }
            visited.add(u);

            if (u.equals(target)) {
                break;
            }

            for (N v : graph.successors(u)) {
                final N edgeU = u;
                final N edgeV = v;
                double weight = graph.edgeValue(u, v)
                        .map(Number::doubleValue)
                        .orElseThrow(() -> new IllegalStateException(
                                "Edge value missing for edge " + edgeU + " -> " + edgeV));

                if (weight < 0) {
                    throw new IllegalArgumentException(
                            "Negative edge weight not allowed in Dijkstra's algorithm: "
                                    + u + " -> " + v + " = " + weight);
                }

                double newDist = distU + weight;
                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.offer(Map.entry(v, newDist));
                }
            }
        }

        // Reconstruct path
        if (dist.get(target) >= Double.MAX_VALUE) {
            return Collections.emptyList();
        }

        LinkedList<N> path = new LinkedList<>();
        N current = target;
        while (current != null) {
            path.addFirst(current);
            current = prev.get(current);
        }

        return path;
    }
}
