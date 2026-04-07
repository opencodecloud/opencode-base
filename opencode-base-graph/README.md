# OpenCode Base Graph

**Graph data structures and algorithms for Java 25+**

`opencode-base-graph` provides a comprehensive graph library with directed/undirected/weighted/immutable graphs, classic graph algorithms (BFS, DFS, Dijkstra, A*, Bellman-Ford, Floyd-Warshall, topological sort), minimum spanning trees, network flow, cycle detection, strongly connected components, articulation points, bipartite detection, DAG operations, community detection, graph metrics, and graph serialization.

## Features

### Graph Types
- **DirectedGraph**: Directed graph with adjacency list representation
- **UndirectedGraph**: Undirected graph with symmetric edge storage
- **WeightedGraph**: Weighted graph supporting both directed and undirected modes
- **ImmutableGraph**: Thread-safe immutable graph snapshot

### Algorithms
- **Traversal**: BFS, DFS (recursive and iterative stack-safe)
- **Shortest Path**: Dijkstra, A* with custom heuristics, Bidirectional BFS, Bellman-Ford (negative weights), Floyd-Warshall (all-pairs)
- **Topological Sort**: Kahn's algorithm with cycle detection
- **Cycle Detection**: Detect and find cycles in directed/undirected graphs
- **Connectivity**: Connected components, reachability, full connectivity check
- **Strongly Connected Components**: Tarjan's algorithm with condensation graph
- **Articulation Points & Bridges**: Cut vertex and bridge detection for network reliability
- **Bipartite Detection**: 2-coloring with partition or odd cycle witness
- **DAG Operations**: Longest path (critical path), transitive reduction/closure, ancestors/descendants
- **Minimum Spanning Tree**: Prim's and Kruskal's algorithms
- **Network Flow**: Ford-Fulkerson (Edmonds-Karp), max flow, min cut
- **Centrality**: Degree, betweenness, closeness, and PageRank centrality
- **Community Detection**: Graph partitioning and cluster identification
- **Graph Metrics**: Density, diameter, radius, eccentricity, clustering coefficient
- **Subgraph**: Extract induced subgraphs

### Advanced Features
- **Graph Builder**: Fluent API for graph construction
- **Graph Diff**: Compare two graphs to find added/removed vertices and edges
- **Graph Transform**: Map vertices, filter, reverse graphs
- **Graph Validation**: Structure and constraint validation
- **Graph Serialization**: GraphML and GEXF format export
- **Layout**: Force-directed graph layout algorithms
- **Safety**: Stack-safe traversal, timeout protection, size limits
- **Union-Find**: Disjoint set data structure with path compression

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-graph</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.graph.*;

// Create a directed graph
Graph<String> graph = OpenGraph.directed();
graph.addEdge("A", "B", 1.0);
graph.addEdge("B", "C", 2.0);
graph.addEdge("A", "C", 4.0);

// BFS traversal
List<String> bfs = OpenGraph.bfs(graph, "A");  // [A, B, C]

// DFS traversal
List<String> dfs = OpenGraph.dfs(graph, "A");  // [A, B, C]

// Shortest path
List<String> path = OpenGraph.shortestPath(graph, "A", "C");  // [A, B, C]
```

### Graph Builder

```java
// Fluent graph construction
Graph<String> graph = OpenGraph.<String>directedBuilder()
    .addEdge("A", "B", 1.0)
    .addEdge("B", "C", 2.0)
    .addEdge("A", "C", 4.0)
    .build();
```

### Shortest Path Algorithms

```java
// Dijkstra - all shortest distances from source (non-negative weights)
Map<String, Double> distances = OpenGraph.dijkstra(graph, "A");

// A* with heuristic
List<String> path = OpenGraph.aStar(graph, "A", "Z",
    (a, b) -> estimateDistance(a, b));

// Bellman-Ford - supports negative edge weights
Map<String, Double> distances = OpenGraph.bellmanFord(graph, "A");
boolean hasNegCycle = OpenGraph.hasNegativeCycle(graph, "A");

// Floyd-Warshall - all-pairs shortest paths
var result = OpenGraph.allPairsShortestPaths(graph);
double dist = result.distance("A", "C");
List<String> path = result.path("A", "C");

// Bidirectional BFS (efficient for large unweighted graphs)
List<String> path = OpenGraph.bidirectionalBfs(graph, "A", "Z");
```

### Strongly Connected Components

```java
// Find all SCCs (Tarjan's algorithm)
List<Set<String>> sccs = OpenGraph.stronglyConnectedComponents(graph);

// Check if strongly connected
boolean strong = OpenGraph.isStronglyConnected(graph);

// Condensation graph (DAG of SCCs)
Graph<Set<String>> dag = OpenGraph.condensation(graph);
```

### Articulation Points & Bridges

```java
// Find cut vertices (whose removal disconnects the graph)
Set<String> cutVertices = OpenGraph.articulationPoints(graph);

// Find bridges (whose removal disconnects the graph)
Set<Edge<String>> bridges = OpenGraph.bridges(graph);

// Check biconnectedness
boolean biconnected = OpenGraph.isBiconnected(graph);
```

### Bipartite Detection

```java
boolean bipartite = OpenGraph.isBipartite(graph);
var result = OpenGraph.bipartitePartition(graph);
if (result.bipartite()) {
    Set<String> left = result.left();
    Set<String> right = result.right();
} else {
    List<String> oddCycle = result.oddCycle();
}
```

### DAG Operations

```java
// Longest path (critical path)
List<String> criticalPath = OpenGraph.longestPath(graph);

// Transitive reduction (remove redundant edges)
Graph<String> reduced = OpenGraph.transitiveReduction(graph);

// Transitive closure (add implied edges)
Graph<String> closure = OpenGraph.transitiveClosure(graph);
```

### Graph Metrics

```java
double density = OpenGraph.density(graph);
int diameter = OpenGraph.diameter(graph);
var summary = OpenGraph.summary(graph);
// summary.vertexCount(), summary.edgeCount(), summary.density(),
// summary.diameter(), summary.radius(), summary.averagePathLength()...
```

### Immutable Graph Snapshot

```java
// Create a thread-safe immutable snapshot
Graph<String> snapshot = graph.snapshot();
// or: Graph<String> snapshot = OpenGraph.snapshot(graph);

// Original modifications don't affect snapshot
graph.addVertex("D");
assert !snapshot.containsVertex("D");
```

### Graph Diff & Transform

```java
// Compare two graphs
var diff = OpenGraph.diff(before, after);
diff.addedVertices();   // vertices in after but not in before
diff.removedVertices(); // vertices in before but not in after

// Transform vertex types
Graph<String> stringGraph = OpenGraph.mapVertices(intGraph, String::valueOf);

// Filter vertices by predicate
Graph<String> filtered = OpenGraph.filterVertices(graph, v -> v.startsWith("A"));

// Filter edges by weight
Graph<String> lightweight = OpenGraph.filterEdges(graph, e -> e.weight() < 10.0);

// Reverse a directed graph
Graph<String> reversed = OpenGraph.reverse(graph);
```

### Topological Sort & Cycle Detection

```java
// Topological sort (DAG only)
List<String> order = OpenGraph.topologicalSort(graph);

// Check if sortable (is DAG)
boolean isDAG = OpenGraph.canTopologicalSort(graph);

// Cycle detection
boolean hasCycle = OpenGraph.hasCycle(graph);
List<String> cycle = OpenGraph.findCycle(graph);
```

### Minimum Spanning Tree

```java
Graph<String> graph = OpenGraph.undirected();
// ... add edges ...

// Prim's algorithm
Set<Edge<String>> mst = OpenGraph.prim(graph);

// Kruskal's algorithm
Set<Edge<String>> mst = OpenGraph.kruskal(graph);

// MST total weight
double weight = OpenGraph.mstWeight(graph);
```

### Network Flow

```java
// Maximum flow
double maxFlow = OpenGraph.maxFlow(graph, source, sink);

// Flow on each edge
Map<Edge<String>, Double> flows = OpenGraph.getFlows(graph, source, sink);

// Minimum cut
Set<Edge<String>> minCut = OpenGraph.minCut(graph, source, sink);

// Full flow result
NetworkFlowUtil.FlowResult<String> result = OpenGraph.computeFlow(graph, source, sink);
```

### Connectivity

```java
// Connected components
List<Set<String>> components = OpenGraph.connectedComponents(graph);

// Check connectivity
boolean connected = OpenGraph.isConnected(graph, "A", "B");
boolean fullyConnected = OpenGraph.isFullyConnected(graph);
int componentCount = OpenGraph.connectedComponentCount(graph);
```

## Class Reference

### Root Package (`cloud.opencode.base.graph`)
| Class | Description |
|-------|-------------|
| `OpenGraph` | Main facade with static methods for graph creation and algorithm access |
| `Graph` | Core interface for graph data structure operations |
| `DirectedGraph` | Directed graph implementation with adjacency list |
| `UndirectedGraph` | Undirected graph implementation with symmetric edges |
| `WeightedGraph` | Weighted graph supporting directed and undirected modes |
| `ImmutableGraph` | Thread-safe immutable graph snapshot via deep copy |
| `GraphDiff` | Compare two graphs and compute added/removed/common vertices and edges |
| `GraphTransform` | Graph transformation: vertex mapping, filtering, reversal |

### Algorithm Package (`cloud.opencode.base.graph.algorithm`)
| Class | Description |
|-------|-------------|
| `GraphTraversalUtil` | BFS and DFS traversal algorithms |
| `SafeGraphTraversalUtil` | Stack-safe iterative DFS to avoid stack overflow |
| `ShortestPathUtil` | Dijkstra's shortest path algorithm |
| `BellmanFordUtil` | Bellman-Ford shortest path with negative weight support |
| `FloydWarshallUtil` | Floyd-Warshall all-pairs shortest paths |
| `AStarUtil` | A* pathfinding with custom heuristic functions |
| `BidirectionalBfsUtil` | Bidirectional BFS for efficient path finding in large graphs |
| `TopologicalSortUtil` | Kahn's algorithm for topological ordering |
| `CycleDetectionUtil` | Cycle detection and cycle finding in graphs |
| `ConnectedComponentsUtil` | Connected component analysis and reachability |
| `StronglyConnectedComponentsUtil` | Tarjan's SCC algorithm with condensation graph |
| `ArticulationPointUtil` | Articulation points (cut vertices) and bridges (cut edges) |
| `BipartiteUtil` | Bipartite detection with partition or odd cycle witness |
| `DagUtil` | DAG operations: longest path, transitive reduction/closure, ancestors/descendants |
| `MinimumSpanningTreeUtil` | Prim's and Kruskal's MST algorithms |
| `NetworkFlowUtil` | Ford-Fulkerson max flow, min cut, and edge flow computation |
| `CentralityUtil` | Degree, betweenness, closeness, and PageRank centrality |
| `CommunityDetectionUtil` | Graph partitioning and community identification |
| `SubgraphUtil` | Induced subgraph extraction |
| `GraphMetrics` | Graph statistics: density, diameter, radius, clustering coefficient |
| `UnionFind` | Disjoint set with path compression and union by rank |

### Builder Package (`cloud.opencode.base.graph.builder`)
| Class | Description |
|-------|-------------|
| `GraphBuilder` | Fluent builder for directed and undirected graph construction |

### Node Package (`cloud.opencode.base.graph.node`)
| Class | Description |
|-------|-------------|
| `Node` | Graph vertex representation |
| `Edge` | Graph edge with source, target, and weight |

### Serializer Package (`cloud.opencode.base.graph.serializer`)
| Class | Description |
|-------|-------------|
| `GraphSerializer` | Interface for graph serialization/deserialization |
| `GraphMLUtil` | GraphML format serialization utility |
| `GexfUtil` | GEXF format serialization utility |

### Layout Package (`cloud.opencode.base.graph.layout`)
| Class | Description |
|-------|-------------|
| `LayoutUtil` | Force-directed and other graph layout algorithms |

### Validation Package (`cloud.opencode.base.graph.validation`)
| Class | Description |
|-------|-------------|
| `GraphValidator` | Graph structure and constraint validation |
| `ValidationResult` | Validation result with error details |

### Security Package (`cloud.opencode.base.graph.security`)
| Class | Description |
|-------|-------------|
| `SafeGraphOperations` | Size-limited and timeout-protected graph operations |

### Exception Package (`cloud.opencode.base.graph.exception`)
| Class | Description |
|-------|-------------|
| `GraphException` | Base exception for graph operations (extends OpenException) |
| `CycleDetectedException` | Cycle found where DAG was expected |
| `EdgeNotFoundException` | Referenced edge not found |
| `VertexNotFoundException` | Referenced vertex not found |
| `InvalidEdgeException` | Invalid edge definition |
| `InvalidVertexException` | Invalid vertex definition |
| `NoPathException` | No path exists between source and target |
| `GraphLimitExceededException` | Graph size or operation limit exceeded |
| `GraphTimeoutException` | Graph operation timed out |
| `GraphErrorCode` | Enumeration of graph error codes |

## Requirements

- Java 25+ (uses records, sealed interfaces, pattern matching)
- No external dependencies for core functionality

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
