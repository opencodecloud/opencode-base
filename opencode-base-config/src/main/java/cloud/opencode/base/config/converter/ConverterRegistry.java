package cloud.opencode.base.config.converter;

import cloud.opencode.base.config.OpenConfigException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Type Converter Registry
 * 类型转换器注册表
 *
 * <p>Manages type converters for configuration value conversion with built-in support
 * for 30+ common types.</p>
 * <p>管理配置值转换的类型转换器,内置支持30多种常用类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>30+ built-in type converters - 30多个内置类型转换器</li>
 *   <li>Custom converter registration - 自定义转换器注册</li>
 *   <li>Automatic enum conversion - 自动枚举转换</li>
 *   <li>Thread-safe concurrent access - 线程安全的并发访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use defaults
 * ConverterRegistry registry = ConverterRegistry.defaults();
 *
 * // Convert values
 * Integer port = registry.convert("8080", Integer.class);
 * Duration timeout = registry.convert("30s", Duration.class);
 * LocalDate date = registry.convert("2025-01-01", LocalDate.class);
 *
 * // Register custom converter
 * registry.register(InetAddress.class, InetAddress::getByName);
 * InetAddress addr = registry.convert("127.0.0.1", InetAddress.class);
 * }</pre>
 *
 * <p><strong>Supported Types | 支持的类型:</strong></p>
 * <ul>
 *   <li>Primitives: int, long, double, float, boolean, etc.</li>
 *   <li>Numbers: Integer, Long, BigDecimal, BigInteger</li>
 *   <li>Time: Duration, LocalDate, LocalTime, LocalDateTime, Instant, ZonedDateTime</li>
 *   <li>Path: Path, URI, URL</li>
 *   <li>Collections: List, Set, Map (via separate converters)</li>
 *   <li>Enums: Automatic conversion</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for conversion - 时间复杂度: 转换为O(1)</li>
 *   <li>Thread-safe concurrent map - 线程安全的并发映射</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe conversions - 空值安全转换</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class ConverterRegistry {

    private final Map<Class<?>, ConfigConverter<?>> converters = new ConcurrentHashMap<>();

    /**
     * Create default converter registry
     * 创建默认转换器注册表
     *
     * @return registry with built-in converters | 带内置转换器的注册表
     */
    public static ConverterRegistry defaults() {
        ConverterRegistry registry = new ConverterRegistry();

        // String
        registry.register(String.class, s -> s);

        // Primitives and wrappers
        registry.register(Integer.class, Integer::parseInt);
        registry.register(int.class, Integer::parseInt);
        registry.register(Long.class, Long::parseLong);
        registry.register(long.class, Long::parseLong);
        registry.register(Double.class, Double::parseDouble);
        registry.register(double.class, Double::parseDouble);
        registry.register(Float.class, Float::parseFloat);
        registry.register(float.class, Float::parseFloat);
        registry.register(Boolean.class, ConverterRegistry::parseBoolean);
        registry.register(boolean.class, ConverterRegistry::parseBoolean);
        registry.register(Byte.class, Byte::parseByte);
        registry.register(byte.class, Byte::parseByte);
        registry.register(Short.class, Short::parseShort);
        registry.register(short.class, Short::parseShort);

        // Big numbers
        registry.register(BigDecimal.class, BigDecimal::new);
        registry.register(BigInteger.class, BigInteger::new);

        // Time types
        registry.register(Duration.class, ConverterRegistry::parseDuration);
        registry.register(LocalDate.class, LocalDate::parse);
        registry.register(LocalTime.class, LocalTime::parse);
        registry.register(LocalDateTime.class, LocalDateTime::parse);
        registry.register(Instant.class, Instant::parse);
        registry.register(ZonedDateTime.class, ZonedDateTime::parse);
        registry.register(OffsetDateTime.class, OffsetDateTime::parse);

        // Path and URI
        registry.register(Path.class, Path::of);
        registry.register(URI.class, URI::create);
        registry.register(URL.class, s -> {
            try {
                return URI.create(s).toURL();
            } catch (Exception e) {
                throw OpenConfigException.invalidUrl(s, e);
            }
        });

        return registry;
    }

    /**
     * Register type converter
     * 注册类型转换器
     *
     * @param <T> target type | 目标类型
     * @param type target class | 目标类
     * @param converter converter function | 转换器函数
     */
    public <T> void register(Class<T> type, ConfigConverter<T> converter) {
        converters.put(type, converter);
    }

    /**
     * Convert string value to target type
     * 将字符串值转换为目标类型
     *
     * @param <T> target type | 目标类型
     * @param value string value | 字符串值
     * @param type target class | 目标类
     * @return converted value | 转换后的值
     * @throws OpenConfigException if conversion fails | 如果转换失败
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(String value, Class<T> type) {
        if (value == null) {
            return null;
        }

        // Handle enum types
        if (type.isEnum()) {
            return convertEnum(value, (Class<Enum>) type);
        }

        // Get converter
        ConfigConverter<?> converter = converters.get(type);
        if (converter == null) {
            throw OpenConfigException.converterNotFound(type);
        }

        try {
            return (T) converter.convert(value);
        } catch (Exception e) {
            throw OpenConfigException.conversionFailed(null, value, type, e);
        }
    }

    /**
     * Convert to enum
     * 转换为枚举
     */
    @SuppressWarnings("unchecked")
    private <T> T convertEnum(String value, Class<Enum> enumType) {
        try {
            return (T) Enum.valueOf(enumType, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw OpenConfigException.conversionFailed(null, value, enumType, e);
        }
    }

    /**
     * Parse boolean value
     * 解析布尔值
     *
     * <p><strong>Supported Values | 支持的值:</strong></p>
     * <pre>
     * true:  true, yes, on, 1, enabled
     * false: false, no, off, 0, disabled
     * </pre>
     */
    private static Boolean parseBoolean(String value) {
        return switch (value.toLowerCase()) {
            case "true", "yes", "on", "1", "enabled" -> true;
            case "false", "no", "off", "0", "disabled" -> false;
            default -> throw OpenConfigException.invalidBoolean(value);
        };
    }

    /**
     * Parse Duration
     * 解析Duration
     *
     * <p><strong>Supported Formats | 支持的格式:</strong></p>
     * <pre>
     * Simple: 1s, 30s, 5m, 2h, 1d
     * ISO-8601: PT1H30M, PT30S, P1D
     * </pre>
     */
    private static Duration parseDuration(String value) {
        // ISO-8601 format
        if (value.startsWith("PT") || value.startsWith("P")) {
            return Duration.parse(value);
        }

        // Simple format: 30s, 5m, 2h, 1d
        if (value.length() < 2) {
            throw new IllegalArgumentException("Invalid duration: " + value);
        }

        char unit = value.charAt(value.length() - 1);
        long amount = Long.parseLong(value.substring(0, value.length() - 1));

        return switch (Character.toLowerCase(unit)) {
            case 's' -> Duration.ofSeconds(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 'h' -> Duration.ofHours(amount);
            case 'd' -> Duration.ofDays(amount);
            default -> Duration.parse("PT" + value.toUpperCase());
        };
    }

    /**
     * Check if converter exists for type
     * 检查类型是否存在转换器
     *
     * @param type target class | 目标类
     * @return true if converter exists | 如果转换器存在返回true
     */
    public boolean hasConverter(Class<?> type) {
        return type.isEnum() || converters.containsKey(type);
    }
}
