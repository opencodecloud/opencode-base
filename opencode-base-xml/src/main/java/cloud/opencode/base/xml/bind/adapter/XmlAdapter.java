package cloud.opencode.base.xml.bind.adapter;

/**
 * XML Adapter Interface - Converts between XML representation and Java types
 * XML 适配器接口 - 在 XML 表示和 Java 类型之间转换
 *
 * <p>This interface defines methods for marshalling and unmarshalling
 * custom types that cannot be directly represented in XML.</p>
 * <p>此接口定义用于编组和解组无法直接在 XML 中表示的自定义类型的方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bidirectional type conversion (marshal/unmarshal) - 双向类型转换（编组/解组）</li>
 *   <li>Pluggable adapter registration with XmlBinder - 可插拔适配器注册到 XmlBinder</li>
 *   <li>Optional type introspection via getBoundType/getValueType - 通过 getBoundType/getValueType 进行可选类型内省</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class LocalDateAdapter implements XmlAdapter<String, LocalDate> {
 *     @Override
 *     public LocalDate unmarshal(String value) {
 *         return LocalDate.parse(value);
 *     }
 *
 *     @Override
 *     public String marshal(LocalDate value) {
 *         return value.toString();
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
 * @param <V> the XML value type (usually String) | XML 值类型（通常是 String）
 * @param <B> the bound Java type | 绑定的 Java 类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public interface XmlAdapter<V, B> {

    /**
     * Converts an XML value to a Java object.
     * 将 XML 值转换为 Java 对象。
     *
     * @param value the XML value | XML 值
     * @return the Java object | Java 对象
     * @throws Exception if conversion fails | 如果转换失败则抛出异常
     */
    B unmarshal(V value) throws Exception;

    /**
     * Converts a Java object to XML value.
     * 将 Java 对象转换为 XML 值。
     *
     * @param value the Java object | Java 对象
     * @return the XML value | XML 值
     * @throws Exception if conversion fails | 如果转换失败则抛出异常
     */
    V marshal(B value) throws Exception;

    /**
     * Returns the bound Java type.
     * 返回绑定的 Java 类型。
     *
     * @return the bound type | 绑定类型
     */
    default Class<B> getBoundType() {
        return null;
    }

    /**
     * Returns the XML value type.
     * 返回 XML 值类型。
     *
     * @return the value type | 值类型
     */
    default Class<V> getValueType() {
        return null;
    }
}
