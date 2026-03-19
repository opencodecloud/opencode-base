package cloud.opencode.base.config.converter;

/**
 * Configuration Type Converter Interface
 * 配置类型转换器接口
 *
 * <p>Functional interface for converting string configuration values to target types.</p>
 * <p>用于将字符串配置值转换为目标类型的函数式接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe string to object conversion - 类型安全的字符串到对象转换</li>
 *   <li>Functional interface with lambda support - 支持Lambda的函数式接口</li>
 *   <li>Extensible via SPI - 可通过SPI扩展</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register custom converter
 * registry.register(LocalDate.class, LocalDate::parse);
 *
 * // Complex converter
 * registry.register(InetAddress.class, value -> {
 *     try {
 *         return InetAddress.getByName(value);
 *     } catch (UnknownHostException e) {
 *         throw new OpenConfigException("Invalid address: " + value, e);
 *     }
 * });
 *
 * // Enum converter (handled automatically)
 * registry.register(LogLevel.class, value ->
 *     LogLevel.valueOf(value.toUpperCase()));
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for conversion - 时间复杂度: 转换为O(1)</li>
 *   <li>Results can be cached - 结果可以被缓存</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Should handle null input - 空值安全: 应处理空输入</li>
 * </ul>
 *
 * @param <T> target type | 目标类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@FunctionalInterface
public interface ConfigConverter<T> {

    /**
     * Convert string value to target type
     * 将字符串值转换为目标类型
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * convert("123")     -> 123           // Integer converter
     * convert("true")    -> Boolean.TRUE  // Boolean converter
     * convert("30s")     -> Duration.ofSeconds(30)  // Duration converter
     * convert("localhost") -> InetAddress.getByName("localhost")  // InetAddress converter
     * </pre>
     *
     * @param value string value from configuration | 配置中的字符串值
     * @return converted object | 转换后的对象
     * @throws cloud.opencode.base.config.OpenConfigException if conversion fails | 如果转换失败
     */
    T convert(String value);

    /**
     * Get supported target type (optional)
     * 获取支持的目标类型(可选)
     *
     * @return target class or null | 目标类或null
     */
    default Class<T> getType() {
        return null;
    }
}
