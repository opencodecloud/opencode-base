package cloud.opencode.base.collections;

/**
 * EntryTransformer - Function to transform map entries
 * EntryTransformer - 转换 Map 条目的函数
 *
 * <p>A functional interface for transforming a key-value pair into a new value.
 * Used in {@link MapUtil#transformEntries}.</p>
 * <p>将键值对转换为新值的函数式接口。
 * 用于 {@link MapUtil#transformEntries}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Transform map entries based on key and value - 根据键和值转换 Map 条目</li>
 *   <li>Functional interface for lambda usage - 函数式接口支持 Lambda</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Transform values using both key and value - 使用键和值转换值
 * EntryTransformer<String, Integer, String> transformer =
 *     (key, value) -> key + "=" + value;
 *
 * Map<String, String> transformed = MapUtil.transformEntries(map, transformer);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Transform: depends on implementation - 转换: 取决于实现</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @param <K>  key type | 键类型
 * @param <V1> input value type | 输入值类型
 * @param <V2> output value type | 输出值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@FunctionalInterface
public interface EntryTransformer<K, V1, V2> {

    /**
     * Transform a key-value entry into a new value.
     * 将键值条目转换为新值。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return the transformed value | 转换后的值
     */
    V2 transformEntry(K key, V1 value);
}
