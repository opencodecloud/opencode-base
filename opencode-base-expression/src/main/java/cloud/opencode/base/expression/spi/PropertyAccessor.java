package cloud.opencode.base.expression.spi;

/**
 * Property Accessor SPI
 * 属性访问器SPI
 *
 * <p>Provides a service provider interface for property access.</p>
 * <p>为属性访问提供服务提供者接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI for custom property access on any object type - 用于任意对象类型自定义属性访问的SPI</li>
 *   <li>Read and optional write support - 读取和可选写入支持</li>
 *   <li>Target type filtering via getSpecificTargetClasses - 通过getSpecificTargetClasses进行目标类型过滤</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class JsonPropertyAccessor implements PropertyAccessor {
 *     @Override
 *     public Class<?>[] getSpecificTargetClasses() {
 *         return new Class<?>[]{ JsonNode.class };
 *     }
 *
 *     @Override
 *     public boolean canRead(Object target, String name) {
 *         return target instanceof JsonNode jn && jn.has(name);
 *     }
 *
 *     @Override
 *     public Object read(Object target, String name) {
 *         return ((JsonNode) target).get(name);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public interface PropertyAccessor {

    /**
     * Get the target types this accessor supports
     * 获取此访问器支持的目标类型
     *
     * @return the supported types, or null for all types | 支持的类型，null表示所有类型
     */
    Class<?>[] getSpecificTargetClasses();

    /**
     * Check if this accessor can read the property
     * 检查此访问器是否可以读取属性
     *
     * @param target the target object | 目标对象
     * @param name the property name | 属性名
     * @return true if readable | 如果可读返回true
     */
    boolean canRead(Object target, String name);

    /**
     * Read the property value
     * 读取属性值
     *
     * @param target the target object | 目标对象
     * @param name the property name | 属性名
     * @return the property value | 属性值
     */
    Object read(Object target, String name);

    /**
     * Check if this accessor can write the property
     * 检查此访问器是否可以写入属性
     *
     * @param target the target object | 目标对象
     * @param name the property name | 属性名
     * @return true if writable | 如果可写返回true
     */
    default boolean canWrite(Object target, String name) {
        return false;
    }

    /**
     * Write the property value
     * 写入属性值
     *
     * @param target the target object | 目标对象
     * @param name the property name | 属性名
     * @param value the value to write | 要写入的值
     */
    default void write(Object target, String name, Object value) {
        throw new UnsupportedOperationException("Write not supported");
    }
}
