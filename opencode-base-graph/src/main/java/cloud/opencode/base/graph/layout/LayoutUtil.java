/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.graph.layout;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Layout Util - Graph Layout Algorithms for Visualization
 * 布局工具类 - 图可视化布局算法
 *
 * <p>Utility class providing layout algorithms for graph visualization.
 * These algorithms compute 2D positions for graph vertices.</p>
 * <p>提供图可视化布局算法的工具类。这些算法为图顶点计算二维位置。</p>
 *
 * <p><strong>Layout Algorithms | 布局算法:</strong></p>
 * <ul>
 *   <li>Force-Directed (Fruchterman-Reingold) - 力导向布局</li>
 *   <li>Circular Layout - 环形布局</li>
 *   <li>Grid Layout - 网格布局</li>
 *   <li>Hierarchical Layout - 层次布局</li>
 *   <li>Random Layout - 随机布局</li>
 *   <li>Spring Layout - 弹簧布局</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Force-directed layout using Fruchterman-Reingold algorithm - 使用Fruchterman-Reingold算法的力导向布局</li>
 *   <li>Spring layout with configurable spring length and iterations - 弹簧布局，可配置弹簧长度和迭代次数</li>
 *   <li>Circular, grid, hierarchical, and random layout algorithms - 环形、网格、层次和随机布局算法</li>
 *   <li>Layout transformation utilities: center and scale - 布局变换工具：居中和缩放</li>
 *   <li>Immutable Point2D record for vertex positioning - 不可变Point2D记录用于顶点定位</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Force-directed layout
 * Map<String, Point2D> positions = LayoutUtil.forceDirected(graph, 800, 600);
 *
 * // Circular layout
 * Map<String, Point2D> positions = LayoutUtil.circular(graph, 400, 300, 250);
 *
 * // Hierarchical layout
 * Map<String, Point2D> positions = LayoutUtil.hierarchical(graph, 800, 600);
 *
 * // Apply layout to GEXF export
 * Map<String, VisualData> visuals = positions.entrySet().stream()
 *     .collect(Collectors.toMap(Map.Entry::getKey,
 *         e -> VisualData.position(e.getValue().x(), e.getValue().y())));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns empty map for null graph) - 空值安全: 是（null图返回空映射）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(V^2 * iterations) for force-directed - 时间复杂度: O(V^2 * iterations)（力导向布局）</li>
 *   <li>Space complexity: O(V) - 空间复杂度: O(V)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class LayoutUtil {

    /** Default number of iterations for force-directed algorithms | 力导向算法默认迭代次数 */
    public static final int DEFAULT_ITERATIONS = 100;

    /** Default cooling factor for simulated annealing | 模拟退火默认冷却因子 */
    public static final double DEFAULT_COOLING = 0.95;

    private LayoutUtil() {
        // Utility class
    }

    // ==================== Force-Directed Layout | 力导向布局 ====================

    /**
     * Compute force-directed layout using Fruchterman-Reingold algorithm.
     * 使用Fruchterman-Reingold算法计算力导向布局。
     *
     * <p>The algorithm simulates a physical system where vertices repel each other
     * and edges act as springs pulling connected vertices together.</p>
     * <p>该算法模拟一个物理系统，其中顶点相互排斥，边像弹簧一样将连接的顶点拉在一起。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param width layout width | 布局宽度
     * @param height layout height | 布局高度
     * @return map of vertex to position | 顶点到位置的映射
     */
    public static <V> Map<V, Point2D> forceDirected(Graph<V> graph, double width, double height) {
        return forceDirected(graph, width, height, DEFAULT_ITERATIONS);
    }

    /**
     * Compute force-directed layout with custom iterations.
     * 使用自定义迭代次数计算力导向布局。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param width layout width | 布局宽度
     * @param height layout height | 布局高度
     * @param iterations number of iterations | 迭代次数
     * @return map of vertex to position | 顶点到位置的映射
     */
    public static <V> Map<V, Point2D> forceDirected(Graph<V> graph, double width, double height,
                                                     int iterations) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        // Initialize with random positions
        Map<V, Point2D> positions = random(graph, width, height);
        Map<V, double[]> displacements = new HashMap<>();

        int n = graph.vertexCount();
        double area = width * height;
        double k = Math.sqrt(area / n); // Optimal distance
        double temperature = width / 10.0;
        double cooling = DEFAULT_COOLING;

        for (int iter = 0; iter < iterations; iter++) {
            // Initialize displacements
            for (V v : graph.vertices()) {
                displacements.put(v, new double[]{0, 0});
            }

            // Calculate repulsive forces (all pairs)
            List<V> vertices = new ArrayList<>(graph.vertices());
            for (int i = 0; i < vertices.size(); i++) {
                V v = vertices.get(i);
                Point2D pv = positions.get(v);
                double[] dv = displacements.get(v);

                for (int j = i + 1; j < vertices.size(); j++) {
                    V u = vertices.get(j);
                    Point2D pu = positions.get(u);
                    double[] du = displacements.get(u);

                    double dx = pv.x() - pu.x();
                    double dy = pv.y() - pu.y();
                    double dist = Math.max(0.01, Math.sqrt(dx * dx + dy * dy));

                    // Repulsive force
                    double force = k * k / dist;
                    double fx = (dx / dist) * force;
                    double fy = (dy / dist) * force;

                    dv[0] += fx;
                    dv[1] += fy;
                    du[0] -= fx;
                    du[1] -= fy;
                }
            }

            // Calculate attractive forces (edges)
            for (Edge<V> edge : graph.edges()) {
                V v = edge.from();
                V u = edge.to();
                Point2D pv = positions.get(v);
                Point2D pu = positions.get(u);
                double[] dv = displacements.get(v);
                double[] du = displacements.get(u);

                double dx = pv.x() - pu.x();
                double dy = pv.y() - pu.y();
                double dist = Math.max(0.01, Math.sqrt(dx * dx + dy * dy));

                // Attractive force
                double force = dist * dist / k;
                double fx = (dx / dist) * force;
                double fy = (dy / dist) * force;

                dv[0] -= fx;
                dv[1] -= fy;
                du[0] += fx;
                du[1] += fy;
            }

            // Apply displacements with temperature limiting
            for (V v : graph.vertices()) {
                double[] d = displacements.get(v);
                double dist = Math.max(0.01, Math.sqrt(d[0] * d[0] + d[1] * d[1]));
                double factor = Math.min(dist, temperature) / dist;

                Point2D p = positions.get(v);
                double newX = Math.max(0, Math.min(width, p.x() + d[0] * factor));
                double newY = Math.max(0, Math.min(height, p.y() + d[1] * factor));

                positions.put(v, new Point2D(newX, newY));
            }

            // Cool down
            temperature *= cooling;
        }

        return positions;
    }

    // ==================== Spring Layout | 弹簧布局 ====================

    /**
     * Compute spring layout (simplified force-directed).
     * 计算弹簧布局（简化的力导向）。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param width layout width | 布局宽度
     * @param height layout height | 布局高度
     * @param springLength natural spring length | 弹簧自然长度
     * @return map of vertex to position | 顶点到位置的映射
     */
    public static <V> Map<V, Point2D> spring(Graph<V> graph, double width, double height,
                                              double springLength) {
        return spring(graph, width, height, springLength, DEFAULT_ITERATIONS);
    }

    /**
     * Compute spring layout with custom parameters.
     * 使用自定义参数计算弹簧布局。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param width layout width | 布局宽度
     * @param height layout height | 布局高度
     * @param springLength natural spring length | 弹簧自然长度
     * @param iterations number of iterations | 迭代次数
     * @return map of vertex to position | 顶点到位置的映射
     */
    public static <V> Map<V, Point2D> spring(Graph<V> graph, double width, double height,
                                              double springLength, int iterations) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<V, Point2D> positions = random(graph, width, height);
        double springConstant = 0.1;
        double repulsionConstant = 10000.0;
        double damping = 0.85;

        Map<V, double[]> velocities = new HashMap<>();
        for (V v : graph.vertices()) {
            velocities.put(v, new double[]{0, 0});
        }

        for (int iter = 0; iter < iterations; iter++) {
            Map<V, double[]> forces = new HashMap<>();
            for (V v : graph.vertices()) {
                forces.put(v, new double[]{0, 0});
            }

            // Repulsion between all pairs
            List<V> vertices = new ArrayList<>(graph.vertices());
            for (int i = 0; i < vertices.size(); i++) {
                for (int j = i + 1; j < vertices.size(); j++) {
                    V v = vertices.get(i);
                    V u = vertices.get(j);
                    Point2D pv = positions.get(v);
                    Point2D pu = positions.get(u);

                    double dx = pv.x() - pu.x();
                    double dy = pv.y() - pu.y();
                    double dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));

                    double force = repulsionConstant / (dist * dist);
                    double fx = (dx / dist) * force;
                    double fy = (dy / dist) * force;

                    forces.get(v)[0] += fx;
                    forces.get(v)[1] += fy;
                    forces.get(u)[0] -= fx;
                    forces.get(u)[1] -= fy;
                }
            }

            // Spring forces for edges
            for (Edge<V> edge : graph.edges()) {
                V v = edge.from();
                V u = edge.to();
                Point2D pv = positions.get(v);
                Point2D pu = positions.get(u);

                double dx = pu.x() - pv.x();
                double dy = pu.y() - pv.y();
                double dist = Math.sqrt(dx * dx + dy * dy);
                double displacement = dist - springLength;

                double force = springConstant * displacement;
                double fx = (dx / Math.max(1, dist)) * force;
                double fy = (dy / Math.max(1, dist)) * force;

                forces.get(v)[0] += fx;
                forces.get(v)[1] += fy;
                forces.get(u)[0] -= fx;
                forces.get(u)[1] -= fy;
            }

            // Update velocities and positions
            for (V v : graph.vertices()) {
                double[] vel = velocities.get(v);
                double[] force = forces.get(v);

                vel[0] = (vel[0] + force[0]) * damping;
                vel[1] = (vel[1] + force[1]) * damping;

                Point2D p = positions.get(v);
                double newX = Math.max(0, Math.min(width, p.x() + vel[0]));
                double newY = Math.max(0, Math.min(height, p.y() + vel[1]));

                positions.put(v, new Point2D(newX, newY));
            }
        }

        return positions;
    }

    // ==================== Circular Layout | 环形布局 ====================

    /**
     * Compute circular layout.
     * 计算环形布局。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param centerX center x-coordinate | 中心x坐标
     * @param centerY center y-coordinate | 中心y坐标
     * @param radius circle radius | 圆半径
     * @return map of vertex to position | 顶点到位置的映射
     */
    public static <V> Map<V, Point2D> circular(Graph<V> graph, double centerX, double centerY,
                                                double radius) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<V, Point2D> positions = new HashMap<>();
        List<V> vertices = new ArrayList<>(graph.vertices());
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            positions.put(vertices.get(i), new Point2D(x, y));
        }

        return positions;
    }

    /**
     * Compute circular layout with default center.
     * 使用默认中心计算环形布局。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param width layout width | 布局宽度
     * @param height layout height | 布局高度
     * @return map of vertex to position | 顶点到位置的映射
     */
    public static <V> Map<V, Point2D> circular(Graph<V> graph, double width, double height) {
        double radius = Math.min(width, height) * 0.4;
        return circular(graph, width / 2, height / 2, radius);
    }

    // ==================== Grid Layout | 网格布局 ====================

    /**
     * Compute grid layout.
     * 计算网格布局。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param width layout width | 布局宽度
     * @param height layout height | 布局高度
     * @return map of vertex to position | 顶点到位置的映射
     */
    public static <V> Map<V, Point2D> grid(Graph<V> graph, double width, double height) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<V, Point2D> positions = new HashMap<>();
        List<V> vertices = new ArrayList<>(graph.vertices());
        int n = vertices.size();

        int cols = (int) Math.ceil(Math.sqrt(n));
        int rows = (int) Math.ceil((double) n / cols);

        double cellWidth = width / (cols + 1);
        double cellHeight = height / (rows + 1);

        for (int i = 0; i < n; i++) {
            int row = i / cols;
            int col = i % cols;
            double x = cellWidth * (col + 1);
            double y = cellHeight * (row + 1);
            positions.put(vertices.get(i), new Point2D(x, y));
        }

        return positions;
    }

    // ==================== Hierarchical Layout | 层次布局 ====================

    /**
     * Compute hierarchical layout (top-down).
     * 计算层次布局（自上而下）。
     *
     * <p>This algorithm places vertices in layers based on their distance from root vertices
     * (vertices with no incoming edges). Best suited for DAGs.</p>
     * <p>该算法根据顶点到根顶点（无入边的顶点）的距离将顶点放置在不同层。最适合DAG。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param width layout width | 布局宽度
     * @param height layout height | 布局高度
     * @return map of vertex to position | 顶点到位置的映射
     */
    public static <V> Map<V, Point2D> hierarchical(Graph<V> graph, double width, double height) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        // Find root vertices (no incoming edges)
        Set<V> roots = new LinkedHashSet<>();
        for (V v : graph.vertices()) {
            if (graph.inDegree(v) == 0) {
                roots.add(v);
            }
        }

        // If no roots (cyclic graph), pick vertex with minimum in-degree
        if (roots.isEmpty()) {
            V minVertex = null;
            int minDegree = Integer.MAX_VALUE;
            for (V v : graph.vertices()) {
                if (graph.inDegree(v) < minDegree) {
                    minDegree = graph.inDegree(v);
                    minVertex = v;
                }
            }
            if (minVertex != null) {
                roots.add(minVertex);
            }
        }

        // BFS to assign layers
        Map<V, Integer> layers = new HashMap<>();
        Queue<V> queue = new LinkedList<>(roots);
        for (V root : roots) {
            layers.put(root, 0);
        }

        while (!queue.isEmpty()) {
            V v = queue.poll();
            int layer = layers.get(v);

            for (V neighbor : graph.neighbors(v)) {
                if (!layers.containsKey(neighbor) || layers.get(neighbor) < layer + 1) {
                    layers.put(neighbor, layer + 1);
                    queue.add(neighbor);
                }
            }
        }

        // Handle unvisited vertices
        int maxLayer = layers.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        for (V v : graph.vertices()) {
            if (!layers.containsKey(v)) {
                layers.put(v, maxLayer + 1);
            }
        }

        // Group vertices by layer
        Map<Integer, List<V>> layerGroups = new TreeMap<>();
        for (Map.Entry<V, Integer> entry : layers.entrySet()) {
            layerGroups.computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                    .add(entry.getKey());
        }

        // Calculate positions
        Map<V, Point2D> positions = new HashMap<>();
        int numLayers = layerGroups.size();
        double layerHeight = height / (numLayers + 1);

        for (Map.Entry<Integer, List<V>> entry : layerGroups.entrySet()) {
            int layer = entry.getKey();
            List<V> vertices = entry.getValue();
            double y = layerHeight * (layer + 1);
            double spacing = width / (vertices.size() + 1);

            for (int i = 0; i < vertices.size(); i++) {
                double x = spacing * (i + 1);
                positions.put(vertices.get(i), new Point2D(x, y));
            }
        }

        return positions;
    }

    // ==================== Random Layout | 随机布局 ====================

    /**
     * Compute random layout.
     * 计算随机布局。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param width layout width | 布局宽度
     * @param height layout height | 布局高度
     * @return map of vertex to position | 顶点到位置的映射
     */
    public static <V> Map<V, Point2D> random(Graph<V> graph, double width, double height) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<V, Point2D> positions = new HashMap<>();
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        double margin = Math.min(width, height) * 0.1;

        for (V vertex : graph.vertices()) {
            double x = margin + rand.nextDouble() * (width - 2 * margin);
            double y = margin + rand.nextDouble() * (height - 2 * margin);
            positions.put(vertex, new Point2D(x, y));
        }

        return positions;
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Center layout in the given bounds.
     * 在给定边界内居中布局。
     *
     * @param <V> the vertex type | 顶点类型
     * @param positions the positions to center | 要居中的位置
     * @param width target width | 目标宽度
     * @param height target height | 目标高度
     * @return centered positions | 居中后的位置
     */
    public static <V> Map<V, Point2D> center(Map<V, Point2D> positions, double width, double height) {
        if (positions == null || positions.isEmpty()) {
            return positions;
        }

        // Find bounding box
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (Point2D p : positions.values()) {
            minX = Math.min(minX, p.x());
            minY = Math.min(minY, p.y());
            maxX = Math.max(maxX, p.x());
            maxY = Math.max(maxY, p.y());
        }

        // Calculate offset
        double currentWidth = maxX - minX;
        double currentHeight = maxY - minY;
        double offsetX = (width - currentWidth) / 2 - minX;
        double offsetY = (height - currentHeight) / 2 - minY;

        // Apply offset
        Map<V, Point2D> centered = new HashMap<>();
        for (Map.Entry<V, Point2D> entry : positions.entrySet()) {
            Point2D p = entry.getValue();
            centered.put(entry.getKey(), new Point2D(p.x() + offsetX, p.y() + offsetY));
        }

        return centered;
    }

    /**
     * Scale layout to fit in the given bounds.
     * 缩放布局以适应给定边界。
     *
     * @param <V> the vertex type | 顶点类型
     * @param positions the positions to scale | 要缩放的位置
     * @param width target width | 目标宽度
     * @param height target height | 目标高度
     * @param margin margin from edges | 边缘距离
     * @return scaled positions | 缩放后的位置
     */
    public static <V> Map<V, Point2D> scale(Map<V, Point2D> positions, double width, double height,
                                             double margin) {
        if (positions == null || positions.isEmpty()) {
            return positions;
        }

        // Find bounding box
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (Point2D p : positions.values()) {
            minX = Math.min(minX, p.x());
            minY = Math.min(minY, p.y());
            maxX = Math.max(maxX, p.x());
            maxY = Math.max(maxY, p.y());
        }

        double currentWidth = maxX - minX;
        double currentHeight = maxY - minY;

        if (currentWidth == 0) currentWidth = 1;
        if (currentHeight == 0) currentHeight = 1;

        // Calculate scale factor
        double targetWidth = width - 2 * margin;
        double targetHeight = height - 2 * margin;
        double scale = Math.min(targetWidth / currentWidth, targetHeight / currentHeight);

        // Apply scale and translate
        Map<V, Point2D> scaled = new HashMap<>();
        for (Map.Entry<V, Point2D> entry : positions.entrySet()) {
            Point2D p = entry.getValue();
            double x = margin + (p.x() - minX) * scale;
            double y = margin + (p.y() - minY) * scale;
            scaled.put(entry.getKey(), new Point2D(x, y));
        }

        return scaled;
    }

    // ==================== Point2D Record | 二维点记录 ====================

    /**
     * Immutable 2D point for graph layout.
     * 用于图布局的不可变二维点。
     *
     * @param x x-coordinate | x坐标
     * @param y y-coordinate | y坐标
     */
    public record Point2D(double x, double y) {

        /**
         * Calculate distance to another point.
         * 计算到另一点的距离。
         *
         * @param other the other point | 另一点
         * @return distance | 距离
         */
        public double distanceTo(Point2D other) {
            double dx = x - other.x;
            double dy = y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        /**
         * Add another point (vector addition).
         * 添加另一点（向量加法）。
         *
         * @param other the other point | 另一点
         * @return new point | 新点
         */
        public Point2D add(Point2D other) {
            return new Point2D(x + other.x, y + other.y);
        }

        /**
         * Subtract another point (vector subtraction).
         * 减去另一点（向量减法）。
         *
         * @param other the other point | 另一点
         * @return new point | 新点
         */
        public Point2D subtract(Point2D other) {
            return new Point2D(x - other.x, y - other.y);
        }

        /**
         * Multiply by scalar.
         * 标量乘法。
         *
         * @param scalar the scalar | 标量
         * @return new point | 新点
         */
        public Point2D multiply(double scalar) {
            return new Point2D(x * scalar, y * scalar);
        }

        @Override
        public String toString() {
            return String.format("(%.2f, %.2f)", x, y);
        }
    }
}
