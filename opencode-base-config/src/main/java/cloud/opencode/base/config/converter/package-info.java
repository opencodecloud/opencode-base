/**
 * Type Converters Package
 * 类型转换器包
 *
 * <p>This package provides type conversion capabilities for converting string configuration
 * values to various target types.</p>
 * <p>此包提供类型转换能力，用于将字符串配置值转换为各种目标类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>30+ built-in type converters - 30+内置类型转换器</li>
 *   <li>Custom converter registration - 自定义转换器注册</li>
 *   <li>SPI-based extension - 基于SPI的扩展</li>
 *   <li>Thread-safe converter registry - 线程安全的转换器注册表</li>
 * </ul>
 *
 * <p><strong>Supported Types | 支持的类型:</strong></p>
 * <ul>
 *   <li><strong>Primitives | 基本类型:</strong> String, int, long, double, float, boolean, BigDecimal, BigInteger</li>
 *   <li><strong>Date/Time | 日期时间:</strong> Duration, LocalDate, LocalTime, LocalDateTime, Instant, ZonedDateTime</li>
 *   <li><strong>Path/URI | 路径:</strong> Path, URI, URL</li>
 *   <li><strong>Enum | 枚举:</strong> All enum types (case-insensitive) - 所有枚举类型(不区分大小写)</li>
 *   <li><strong>Collections | 集合:</strong> List, Set, Map (comma-separated format) - List, Set, Map (逗号分隔格式)</li>
 * </ul>
 *
 * <p><strong>Duration Format | Duration格式:</strong></p>
 * <pre>
 * 30s  → Duration.ofSeconds(30)
 * 5m   → Duration.ofMinutes(5)
 * 2h   → Duration.ofHours(2)
 * 1d   → Duration.ofDays(1)
 * PT1H30M → ISO-8601 format
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get default registry with all built-in converters
 * ConverterRegistry registry = ConverterRegistry.defaults();
 *
 * // Convert values
 * Duration timeout = registry.convert("30s", Duration.class);
 * Path file = registry.convert("/tmp/config.txt", Path.class);
 * MyEnum status = registry.convert("active", MyEnum.class);
 *
 * // Register custom converter
 * registry.register(InetAddress.class, s -> InetAddress.getByName(s));
 * }</pre>
 *
 * <p><strong>SPI Extension | SPI扩展:</strong></p>
 * <pre>{@code
 * // META-INF/services/cloud.opencode.base.config.advanced.ConfigConverterProvider
 * com.example.MyConverterProvider
 *
 * public class MyConverterProvider implements ConfigConverterProvider {
 *     public Class<?> supportedType() { return MyType.class; }
 *     public ConfigConverter<?> create() { return s -> new MyType(s); }
 * }
 * }</pre>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
package cloud.opencode.base.config.converter;
