package cloud.opencode.base.core.convert;

import cloud.opencode.base.core.convert.impl.ArrayConverter;
import cloud.opencode.base.core.convert.impl.DateConverter;
import cloud.opencode.base.core.convert.impl.NumberConverter;
import cloud.opencode.base.core.convert.impl.StringConverter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Converter Registry - Manages registration and retrieval of type converters
 * 转换器注册表 - 管理类型转换器的注册和获取
 *
 * <p>Central registry for all type converters supporting built-in, array and custom converters.</p>
 * <p>管理所有类型转换器的注册和获取，支持内置、数组和自定义转换器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Built-in converters: number, string, date, boolean, char - 内置转换器</li>
 *   <li>Array converters: primitive and object arrays - 数组转换器</li>
 *   <li>Custom converters: via SPI or manual registration - 自定义转换器</li>
 *   <li>Type hierarchy lookup - 类型层级查找</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get converter - 获取转换器
 * Converter<Integer> conv = ConverterRegistry.getConverter(Integer.class);
 *
 * // Register custom converter - 注册自定义转换器
 * ConverterRegistry.register(MyType.class, new MyTypeConverter());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap) - 线程安全: 是 (并发哈希表)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for lookup/register - 查找/注册 O(1)</li>
 *   <li>Space complexity: O(n) where n = registered converters - O(n), n为已注册转换器数</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class ConverterRegistry {

    private static final Map<Type, Converter<?>> CONVERTERS = new ConcurrentHashMap<>();

    static {
        registerDefaultConverters();
    }

    private ConverterRegistry() {
    }

    /**
     * Registers a converter
     * 注册转换器
     *
     * @param type the target type | 目标类型
     * @param converter the value | 转换器
     */
    public static void register(Type type, Converter<?> converter) {
        CONVERTERS.put(type, converter);
    }

    /**
     * Gets
     * 获取转换器
     *
     * @param type the target type | 目标类型
     * @param <T> the target type | 目标类型
     * @return the result | 转换器，不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> Converter<T> getConverter(Type type) {
        Converter<?> converter = CONVERTERS.get(type);
        if (converter != null) {
            return (Converter<T>) converter;
        }

        // Walk type hierarchy for Class types (interfaces first, then superclasses)
        if (type instanceof Class<?> clazz) {
            // Check interfaces
            for (Class<?> iface : clazz.getInterfaces()) {
                converter = CONVERTERS.get(iface);
                if (converter != null) {
                    return (Converter<T>) converter;
                }
            }
            // Walk superclass chain
            Class<?> superclass = clazz.getSuperclass();
            while (superclass != null && superclass != Object.class) {
                converter = CONVERTERS.get(superclass);
                if (converter != null) {
                    return (Converter<T>) converter;
                }
                // Check interfaces of the superclass
                for (Class<?> iface : superclass.getInterfaces()) {
                    converter = CONVERTERS.get(iface);
                    if (converter != null) {
                        return (Converter<T>) converter;
                    }
                }
                superclass = superclass.getSuperclass();
            }
        }

        return null;
    }

    /**
     * Checks
     * 检查是否存在转换器
     *
     * @param type the target type | 目标类型
     * @return the result | 是否存在
     */
    public static boolean hasConverter(Type type) {
        return getConverter(type) != null;
    }

    /**
     * Removes a converter
     * 移除转换器
     *
     * @param type the target type | 目标类型
     */
    public static void unregister(Type type) {
        CONVERTERS.remove(type);
    }

    /**
     * Gets
     * 获取所有已注册类型数量
     *
     * @return the result | 数量
     */
    public static int size() {
        return CONVERTERS.size();
    }

    /**
     * Registers default converters
     * 注册默认转换器
     */
    private static void registerDefaultConverters() {
        registerStringConverter();
        registerNumberConverters();
        registerBooleanConverter();
        registerCharacterConverter();
        registerDateConverters();
        registerArrayConverters();
    }

    // ==================== String 转换器 ====================

    private static void registerStringConverter() {
        register(String.class, StringConverter.getInstance());
    }

    // ==================== 数字转换器 ====================

    private static void registerNumberConverters() {
        // 整数类型
        Converter<Byte> byteConverter = NumberConverter.byteConverter();
        register(Byte.class, byteConverter);
        register(byte.class, byteConverter);

        Converter<Short> shortConverter = NumberConverter.shortConverter();
        register(Short.class, shortConverter);
        register(short.class, shortConverter);

        Converter<Integer> intConverter = NumberConverter.integerConverter();
        register(Integer.class, intConverter);
        register(int.class, intConverter);

        Converter<Long> longConverter = NumberConverter.longConverter();
        register(Long.class, longConverter);
        register(long.class, longConverter);

        // 浮点类型
        Converter<Float> floatConverter = NumberConverter.floatConverter();
        register(Float.class, floatConverter);
        register(float.class, floatConverter);

        Converter<Double> doubleConverter = NumberConverter.doubleConverter();
        register(Double.class, doubleConverter);
        register(double.class, doubleConverter);

        // 大数类型
        register(BigDecimal.class, NumberConverter.bigDecimalConverter());
        register(BigInteger.class, NumberConverter.bigIntegerConverter());

        // 原子类型
        register(AtomicInteger.class, NumberConverter.atomicIntegerConverter());
        register(AtomicLong.class, NumberConverter.atomicLongConverter());
    }

    // ==================== 布尔转换器 ====================

    private static void registerBooleanConverter() {
        Converter<Boolean> boolConverter = (value, defaultValue) -> {
            if (value == null) return defaultValue;
            if (value instanceof Boolean b) return b;
            if (value instanceof Number n) return n.intValue() != 0;
            String str = value.toString().trim().toLowerCase();
            if ("true".equals(str) || "1".equals(str) || "yes".equals(str) || "on".equals(str) || "y".equals(str)) {
                return true;
            }
            if ("false".equals(str) || "0".equals(str) || "no".equals(str) || "off".equals(str) || "n".equals(str)) {
                return false;
            }
            return defaultValue;
        };
        register(Boolean.class, boolConverter);
        register(boolean.class, boolConverter);
    }

    // ==================== 字符转换器 ====================

    private static void registerCharacterConverter() {
        Converter<Character> charConverter = (value, defaultValue) -> {
            if (value == null) return defaultValue;
            if (value instanceof Character c) return c;
            if (value instanceof Number n) return (char) n.intValue();
            String str = value.toString();
            return str.isEmpty() ? defaultValue : str.charAt(0);
        };
        register(Character.class, charConverter);
        register(char.class, charConverter);
    }

    // ==================== 日期转换器 ====================

    private static void registerDateConverters() {
        // Java 8+ 时间类型
        register(LocalDate.class, DateConverter.localDateConverter());
        register(LocalDateTime.class, DateConverter.localDateTimeConverter());
        register(LocalTime.class, DateConverter.localTimeConverter());
        register(Instant.class, DateConverter.instantConverter());
        register(ZonedDateTime.class, DateConverter.zonedDateTimeConverter());
        register(OffsetDateTime.class, DateConverter.offsetDateTimeConverter());

        // 旧版日期类型
        register(Date.class, DateConverter.dateConverter());
        register(Calendar.class, DateConverter.calendarConverter());

        // java.sql 日期类型（运行时可选，requires static java.sql）
        registerSqlDateConverters();
    }

    /**
     * Registers java.sql date/time converters if java.sql module is available at runtime.
     * 如果运行时 java.sql 模块可用，则注册 java.sql 日期/时间转换器。
     */
    private static void registerSqlDateConverters() {
        try {
            Class.forName("java.sql.Date");
            register(java.sql.Date.class, DateConverter.sqlDateConverter());
            register(java.sql.Time.class, DateConverter.sqlTimeConverter());
            register(java.sql.Timestamp.class, DateConverter.timestampConverter());
        } catch (ClassNotFoundException | NoClassDefFoundError _) {
            // java.sql module not available at runtime — skip registration
        }
    }

    // ==================== 数组转换器 ====================

    private static void registerArrayConverters() {
        // 原始类型数组
        register(int[].class, ArrayConverter.intArrayConverter());
        register(long[].class, ArrayConverter.longArrayConverter());
        register(double[].class, ArrayConverter.doubleArrayConverter());
        register(float[].class, ArrayConverter.floatArrayConverter());
        register(boolean[].class, ArrayConverter.booleanArrayConverter());
        register(byte[].class, ArrayConverter.byteArrayConverter());
        register(short[].class, ArrayConverter.shortArrayConverter());
        register(char[].class, ArrayConverter.charArrayConverter());

        // 常用对象数组
        register(String[].class, ArrayConverter.stringArrayConverter());
        register(Integer[].class, ArrayConverter.integerArrayConverter());
        register(Long[].class, ArrayConverter.longObjArrayConverter());
        register(Double[].class, ArrayConverter.doubleObjArrayConverter());
    }
}
