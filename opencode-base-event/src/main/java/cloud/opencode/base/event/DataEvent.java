package cloud.opencode.base.event;

/**
 * Generic Data Event
 * 泛型数据事件
 *
 * <p>Event that carries typed data payload.</p>
 * <p>携带类型化数据载荷的事件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe data payload - 类型安全的数据载荷</li>
 *   <li>Immutable data reference - 不可变数据引用</li>
 *   <li>Generic type support - 泛型类型支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create data event
 * Order order = new Order(1L, "Product");
 * DataEvent<Order> event = new DataEvent<>(order, "OrderService");
 *
 * // Publish
 * OpenEvent.getDefault().publish(event);
 *
 * // Listen
 * OpenEvent.getDefault().on(DataEvent.class, e -> {
 *     Order data = (Order) e.getData();
 *     System.out.println("Received: " + data);
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param <T> the type of data payload | 数据载荷类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public class DataEvent<T> extends Event {

    private final T data;

    /**
     * Create data event with payload
     * 创建带载荷的数据事件
     *
     * @param data the data payload | 数据载荷
     */
    public DataEvent(T data) {
        this.data = data;
    }

    /**
     * Create data event with payload and source
     * 创建带载荷和来源的数据事件
     *
     * @param data   the data payload | 数据载荷
     * @param source the event source | 事件来源
     */
    public DataEvent(T data, String source) {
        super(source);
        this.data = data;
    }

    /**
     * Get the data payload
     * 获取数据载荷
     *
     * @return the data | 数据
     */
    public T getData() {
        return data;
    }

    /**
     * Get the data type
     * 获取数据类型
     *
     * @return the data class or null | 数据类型或null
     */
    public Class<?> getDataType() {
        return data != null ? data.getClass() : null;
    }

    @Override
    public String toString() {
        return "DataEvent{" +
                "id='" + getId() + '\'' +
                ", data=" + data +
                ", source='" + getSource() + '\'' +
                '}';
    }
}
