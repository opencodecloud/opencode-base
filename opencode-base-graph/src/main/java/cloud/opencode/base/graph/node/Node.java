package cloud.opencode.base.graph.node;

/**
 * Node Interface
 * 节点接口
 *
 * <p>Interface for graph nodes with value accessor.</p>
 * <p>带值访问器的图节点接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Simple value-holding node abstraction - 简单的值持有节点抽象</li>
 *   <li>Factory method for easy creation - 便捷创建的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node<String> node = new SimpleNode<>("A");
 * String value = node.getValue();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable implementations) - 线程安全: 是（不可变实现）</li>
 *   <li>Null-safe: No (accepts null values) - 空值安全: 否（接受null值）</li>
 * </ul>
 *
 * @param <V> the vertex value type | 顶点值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public interface Node<V> {

    /**
     * Get the node value
     * 获取节点值
     *
     * @return the value | 值
     */
    V getValue();

    /**
     * Create a simple node with the given value
     * 使用给定值创建简单节点
     *
     * @param <V> the value type | 值类型
     * @param value the value | 值
     * @return the node | 节点
     */
    static <V> Node<V> of(V value) {
        return new SimpleNode<>(value);
    }
}

/**
 * Simple Node implementation
 * 简单节点实现
 *
 * @param value the node value | 节点值
 * @param <V> the value type | 值类型
 */
record SimpleNode<V>(V value) implements Node<V> {
    @Override
    public V getValue() {
        return value;
    }
}
