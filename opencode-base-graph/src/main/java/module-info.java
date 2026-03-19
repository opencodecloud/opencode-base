/**
 * OpenCode Base Graph Module
 * OpenCode 基础图论模块
 *
 * <p>Provides graph data structures and algorithms based on JDK 25,
 * including directed/undirected graphs, shortest path, spanning tree, and layout algorithms.</p>
 * <p>提供基于 JDK 25 的图数据结构与算法，包括有向/无向图、最短路径、生成树和布局算法。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Directed &amp; Undirected Graphs - 有向图与无向图</li>
 *   <li>Shortest Path (Dijkstra, BFS, DFS) - 最短路径算法</li>
 *   <li>Minimum Spanning Tree - 最小生成树</li>
 *   <li>Graph Layout Algorithms - 图布局算法</li>
 *   <li>Graph Serialization - 图序列化</li>
 *   <li>Cycle Detection &amp; Topological Sort - 环检测与拓扑排序</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
module cloud.opencode.base.graph {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.graph;
    exports cloud.opencode.base.graph.algorithm;
    exports cloud.opencode.base.graph.builder;
    exports cloud.opencode.base.graph.exception;
    exports cloud.opencode.base.graph.layout;
    exports cloud.opencode.base.graph.node;
    exports cloud.opencode.base.graph.security;
    exports cloud.opencode.base.graph.serializer;
    exports cloud.opencode.base.graph.validation;
}
