package cloud.opencode.base.core.convert;

/**
 * AttributeConverter - SPI for bidirectional type conversion between two types.
 * 属性转换器 - 用于两种类型之间双向转换的 SPI。
 *
 * <p>Implement this interface to define custom conversion logic between an attribute type
 * and a storage type. Commonly used for entity field to database column type mapping.</p>
 * <p>实现此接口以定义属性类型和存储类型之间的自定义转换逻辑。
 * 常用于实体字段到数据库列类型的映射。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class JsonConverter implements AttributeConverter<MyObject, String> {
 *     public String convertTo(MyObject attribute) {
 *         return JsonUtils.toJson(attribute);
 *     }
 *     public MyObject convertFrom(String stored) {
 *         return JsonUtils.fromJson(stored, MyObject.class);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bidirectional type conversion SPI - 双向类型转换SPI</li>
 *   <li>Generic type parameters for type safety - 泛型参数保证类型安全</li>
 *   <li>Suitable for entity-to-storage mapping - 适用于实体到存储的映射</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AttributeConverter<LocalDate, String> conv = new AttributeConverter<>() {
 *     public String convertTo(LocalDate date) { return date.toString(); }
 *     public LocalDate convertFrom(String s) { return LocalDate.parse(s); }
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation, null inputs possible - 空值安全: 取决于实现，可能接收null输入</li>
 * </ul>
 *
 * @param <X> the attribute type | 属性类型
 * @param <Y> the storage type | 存储类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public interface AttributeConverter<X, Y> {

    /**
     * Converts the attribute value into the storage representation.
     * 将属性值转换为存储表示形式。
     *
     * @param attribute the attribute value to be converted; may be null | 要转换的属性值，可以为 null
     * @return the converted storage value | 转换后的存储值
     */
    Y convertTo(X attribute);

    /**
     * Converts the storage value back into the attribute value.
     * 将存储值转换回属性值。
     *
     * @param stored the storage value to be converted; may be null | 要转换的存储值，可以为 null
     * @return the converted attribute value | 转换后的属性值
     */
    X convertFrom(Y stored);
}
