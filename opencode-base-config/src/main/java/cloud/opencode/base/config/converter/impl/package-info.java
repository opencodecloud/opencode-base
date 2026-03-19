/**
 * Built-in Type Converter Implementations
 * 内置类型转换器实现
 *
 * <p>This package contains all built-in type converter implementations for
 * common Java types.</p>
 * <p>此包包含所有常见Java类型的内置转换器实现。</p>
 *
 * <p><strong>Converter Classes | 转换器类:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.config.converter.impl.StringConverter} - String passthrough - 字符串直通转换</li>
 *   <li>{@link cloud.opencode.base.config.converter.impl.NumberConverters} - Integer, Long, Double, Float, BigDecimal, BigInteger</li>
 *   <li>{@link cloud.opencode.base.config.converter.impl.BooleanConverter} - Boolean with multiple formats (true/false, yes/no, on/off, 1/0)</li>
 *   <li>{@link cloud.opencode.base.config.converter.impl.DurationConverter} - Duration with simplified format (1s, 5m, 2h, 1d)</li>
 *   <li>{@link cloud.opencode.base.config.converter.impl.DateTimeConverters} - LocalDate, LocalTime, LocalDateTime, Instant, ZonedDateTime</li>
 *   <li>{@link cloud.opencode.base.config.converter.impl.EnumConverter} - All enum types (case-insensitive)</li>
 *   <li>{@link cloud.opencode.base.config.converter.impl.CollectionConverters} - List, Set, Map (comma-separated)</li>
 * </ul>
 *
 * <p><strong>Boolean Formats | 布尔格式:</strong></p>
 * <pre>
 * true:  "true", "yes", "on", "1"
 * false: "false", "no", "off", "0"
 * </pre>
 *
 * <p><strong>Collection Formats | 集合格式:</strong></p>
 * <pre>
 * List: "a,b,c" → List.of("a", "b", "c")
 * Set:  "a,b,c" → Set.of("a", "b", "c")
 * Map:  "k1=v1,k2=v2" → Map.of("k1", "v1", "k2", "v2")
 * </pre>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
package cloud.opencode.base.config.converter.impl;
