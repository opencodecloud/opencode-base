package cloud.opencode.base.graph.node;

/**
 * Edge
 * 边
 *
 * <p>Immutable record representing a weighted edge in a graph.</p>
 * <p>表示图中加权边的不可变记录。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Supports weighted edges - 支持加权边</li>
 *   <li>Default weight is 1.0 - 默认权重为1.0</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Unweighted edge
 * Edge<String> edge1 = new Edge<>("A", "B");
 *
 * // Weighted edge
 * Edge<String> edge2 = new Edge<>("A", "B", 5.0);
 *
 * // Access properties
 * String from = edge2.from();
 * String to = edge2.to();
 * double weight = edge2.weight();
 * }</pre>
 *
 * @param from the source vertex | 源顶点
 * @param to the target vertex | 目标顶点
 * @param weight the edge weight | 边权重
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (rejects null vertices, NaN and infinite weights) - 空值安全: 是（拒绝null顶点、NaN和无穷权重）</li>
 * </ul>
 *
 * @param <V> the vertex type | 顶点类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public record Edge<V>(V from, V to, double weight) {

    /**
     * Default edge weight
     * 默认边权重
     */
    public static final double DEFAULT_WEIGHT = 1.0;

    /**
     * Create an unweighted edge (weight = 1.0)
     * 创建无权重边（权重=1.0）
     *
     * @param from the source vertex | 源顶点
     * @param to the target vertex | 目标顶点
     */
    public Edge(V from, V to) {
        this(from, to, DEFAULT_WEIGHT);
    }

    /**
     * Create an edge with validation
     * 创建带验证的边
     *
     * @param from the source vertex | 源顶点
     * @param to the target vertex | 目标顶点
     * @param weight the edge weight | 边权重
     */
    public Edge {
        if (from == null) {
            throw new IllegalArgumentException("Source vertex cannot be null");
        }
        if (to == null) {
            throw new IllegalArgumentException("Target vertex cannot be null");
        }
        if (Double.isNaN(weight)) {
            throw new IllegalArgumentException("Edge weight cannot be NaN");
        }
        if (Double.isInfinite(weight)) {
            throw new IllegalArgumentException("Edge weight cannot be infinite");
        }
    }

    /**
     * Create a reversed edge
     * 创建反向边
     *
     * @return the reversed edge | 反向边
     */
    public Edge<V> reversed() {
        return new Edge<>(to, from, weight);
    }

    /**
     * Create an edge with a new weight
     * 创建具有新权重的边
     *
     * @param newWeight the new weight | 新权重
     * @return the new edge | 新边
     */
    public Edge<V> withWeight(double newWeight) {
        return new Edge<>(from, to, newWeight);
    }

    /**
     * Check if this is a self-loop
     * 检查是否为自环
     *
     * @return true if self-loop | 如果是自环返回true
     */
    public boolean isSelfLoop() {
        return from.equals(to);
    }

    @Override
    public String toString() {
        if (weight == DEFAULT_WEIGHT) {
            return from + " -> " + to;
        }
        return from + " -(" + weight + ")-> " + to;
    }
}
