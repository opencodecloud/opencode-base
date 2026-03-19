package cloud.opencode.base.reflect.bean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Property Type Converter
 * 属性类型转换器
 *
 * <p>Converts property values between different types.</p>
 * <p>在不同类型之间转换属性值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>String/Number/Boolean type conversions - String/Number/Boolean类型转换</li>
 *   <li>Date/Time type conversions - 日期/时间类型转换</li>
 *   <li>Enum and Collection conversions - 枚举和集合转换</li>
 *   <li>Custom converter registration - 自定义转换器注册</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * int num = PropertyConverter.convert("42", int.class);
 * String str = PropertyConverter.convert(42, String.class);
 * boolean canConvert = PropertyConverter.canConvert(String.class, Integer.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap for converter registry) - 线程安全: 是（使用ConcurrentHashMap存储转换器）</li>
 *   <li>Null-safe: Yes (null input returns type default) - 空值安全: 是（null输入返回类型默认值）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for convert (hash map lookup) and canConvert - 时间复杂度: convert（哈希映射查找）和 canConvert 均为 O(1)</li>
 *   <li>Space complexity: O(1) per conversion - 空间复杂度: 每次转换 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class PropertyConverter {

    private static final Map<ConversionKey, Function<Object, Object>> CONVERTERS = new ConcurrentHashMap<>();

    static {
        // String conversions
        registerConverter(String.class, Integer.class, s -> Integer.parseInt((String) s));
        registerConverter(String.class, int.class, s -> Integer.parseInt((String) s));
        registerConverter(String.class, Long.class, s -> Long.parseLong((String) s));
        registerConverter(String.class, long.class, s -> Long.parseLong((String) s));
        registerConverter(String.class, Double.class, s -> Double.parseDouble((String) s));
        registerConverter(String.class, double.class, s -> Double.parseDouble((String) s));
        registerConverter(String.class, Float.class, s -> Float.parseFloat((String) s));
        registerConverter(String.class, float.class, s -> Float.parseFloat((String) s));
        registerConverter(String.class, Boolean.class, s -> Boolean.parseBoolean((String) s));
        registerConverter(String.class, boolean.class, s -> Boolean.parseBoolean((String) s));
        registerConverter(String.class, Short.class, s -> Short.parseShort((String) s));
        registerConverter(String.class, short.class, s -> Short.parseShort((String) s));
        registerConverter(String.class, Byte.class, s -> Byte.parseByte((String) s));
        registerConverter(String.class, byte.class, s -> Byte.parseByte((String) s));
        registerConverter(String.class, BigDecimal.class, s -> new BigDecimal((String) s));
        registerConverter(String.class, BigInteger.class, s -> new BigInteger((String) s));

        // Number to String
        registerConverter(Number.class, String.class, n -> String.valueOf(n));

        // Number conversions
        registerConverter(Number.class, Integer.class, n -> ((Number) n).intValue());
        registerConverter(Number.class, int.class, n -> ((Number) n).intValue());
        registerConverter(Number.class, Long.class, n -> ((Number) n).longValue());
        registerConverter(Number.class, long.class, n -> ((Number) n).longValue());
        registerConverter(Number.class, Double.class, n -> ((Number) n).doubleValue());
        registerConverter(Number.class, double.class, n -> ((Number) n).doubleValue());
        registerConverter(Number.class, Float.class, n -> ((Number) n).floatValue());
        registerConverter(Number.class, float.class, n -> ((Number) n).floatValue());
        registerConverter(Number.class, Short.class, n -> ((Number) n).shortValue());
        registerConverter(Number.class, short.class, n -> ((Number) n).shortValue());
        registerConverter(Number.class, Byte.class, n -> ((Number) n).byteValue());
        registerConverter(Number.class, byte.class, n -> ((Number) n).byteValue());
        registerConverter(Number.class, BigDecimal.class, n -> BigDecimal.valueOf(((Number) n).doubleValue()));
        registerConverter(Number.class, BigInteger.class, n -> BigInteger.valueOf(((Number) n).longValue()));

        // Boolean conversions
        registerConverter(Boolean.class, String.class, b -> String.valueOf(b));
        registerConverter(String.class, Boolean.class, s -> {
            String str = ((String) s).toLowerCase();
            return "true".equals(str) || "yes".equals(str) || "1".equals(str) || "on".equals(str);
        });
        registerConverter(Number.class, Boolean.class, n -> ((Number) n).intValue() != 0);
        registerConverter(Boolean.class, Integer.class, b -> (Boolean) b ? 1 : 0);

        // Date/Time conversions
        registerConverter(String.class, LocalDate.class, s -> LocalDate.parse((String) s));
        registerConverter(String.class, LocalTime.class, s -> LocalTime.parse((String) s));
        registerConverter(String.class, LocalDateTime.class, s -> LocalDateTime.parse((String) s));
        registerConverter(String.class, Instant.class, s -> Instant.parse((String) s));
        registerConverter(LocalDate.class, String.class, d -> ((LocalDate) d).toString());
        registerConverter(LocalTime.class, String.class, t -> ((LocalTime) t).toString());
        registerConverter(LocalDateTime.class, String.class, dt -> ((LocalDateTime) dt).toString());
        registerConverter(Instant.class, String.class, i -> ((Instant) i).toString());
        registerConverter(Long.class, Instant.class, l -> Instant.ofEpochMilli((Long) l));
        registerConverter(Instant.class, Long.class, i -> ((Instant) i).toEpochMilli());

        // Enum conversions
        registerConverter(String.class, Enum.class, s -> null); // Handled specially
        registerConverter(Enum.class, String.class, e -> ((Enum<?>) e).name());
        registerConverter(Enum.class, Integer.class, e -> ((Enum<?>) e).ordinal());

        // Collection conversions
        registerConverter(Object[].class, List.class, a -> Arrays.asList((Object[]) a));
        registerConverter(Collection.class, List.class, c -> new ArrayList<>((Collection<?>) c));
        registerConverter(Collection.class, Set.class, c -> new LinkedHashSet<>((Collection<?>) c));
    }

    private PropertyConverter() {
    }

    /**
     * Converts a value to the target type
     * 将值转换为目标类型
     *
     * @param value      the value to convert | 要转换的值
     * @param targetType the target type | 目标类型
     * @param <T>        the target type | 目标类型
     * @return the converted value | 转换后的值
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object value, Class<T> targetType) {
        if (value == null) {
            return getDefaultValue(targetType);
        }

        Class<?> sourceType = value.getClass();

        // Already correct type
        if (targetType.isAssignableFrom(sourceType)) {
            return (T) value;
        }

        // Handle enum specially
        if (targetType.isEnum() && value instanceof String) {
            return (T) convertToEnum((String) value, (Class<Enum>) targetType);
        }

        // Look for exact converter
        Function<Object, Object> converter = findConverter(sourceType, targetType);
        if (converter != null) {
            return (T) converter.apply(value);
        }

        // Try to find converter for supertype
        converter = findConverterForSupertype(sourceType, targetType);
        if (converter != null) {
            return (T) converter.apply(value);
        }

        throw new IllegalArgumentException(
                "Cannot convert from " + sourceType.getName() + " to " + targetType.getName());
    }

    /**
     * Converts a value, returning null if conversion fails
     * 转换值，如果转换失败返回null
     *
     * @param value      the value to convert | 要转换的值
     * @param targetType the target type | 目标类型
     * @param <T>        the target type | 目标类型
     * @return the converted value or null | 转换后的值或null
     */
    public static <T> T convertSafe(Object value, Class<T> targetType) {
        try {
            return convert(value, targetType);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Converts a value with a default value if conversion fails
     * 转换值，如果转换失败返回默认值
     *
     * @param value        the value to convert | 要转换的值
     * @param targetType   the target type | 目标类型
     * @param defaultValue the default value | 默认值
     * @param <T>          the target type | 目标类型
     * @return the converted value or default | 转换后的值或默认值
     */
    public static <T> T convertOrDefault(Object value, Class<T> targetType, T defaultValue) {
        T result = convertSafe(value, targetType);
        return result != null ? result : defaultValue;
    }

    /**
     * Checks if conversion is possible
     * 检查是否可以转换
     *
     * @param sourceType the source type | 源类型
     * @param targetType the target type | 目标类型
     * @return true if conversion is possible | 如果可以转换返回true
     */
    public static boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        if (targetType.isAssignableFrom(sourceType)) {
            return true;
        }
        return findConverter(sourceType, targetType) != null ||
                findConverterForSupertype(sourceType, targetType) != null;
    }

    /**
     * Registers a custom converter
     * 注册自定义转换器
     *
     * @param sourceType the source type | 源类型
     * @param targetType the target type | 目标类型
     * @param converter  the converter function | 转换器函数
     * @param <S>        the source type | 源类型
     * @param <T>        the target type | 目标类型
     */
    public static <S, T> void registerConverter(Class<S> sourceType, Class<T> targetType,
                                                 Function<Object, Object> converter) {
        CONVERTERS.put(new ConversionKey(sourceType, targetType), converter);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Enum<T>> T convertToEnum(String value, Class<T> enumType) {
        return Enum.valueOf(enumType, value);
    }

    private static Function<Object, Object> findConverter(Class<?> sourceType, Class<?> targetType) {
        return CONVERTERS.get(new ConversionKey(sourceType, targetType));
    }

    private static Function<Object, Object> findConverterForSupertype(Class<?> sourceType, Class<?> targetType) {
        // Check source supertypes
        for (Map.Entry<ConversionKey, Function<Object, Object>> entry : CONVERTERS.entrySet()) {
            ConversionKey key = entry.getKey();
            if (key.targetType.equals(targetType) && key.sourceType.isAssignableFrom(sourceType)) {
                return entry.getValue();
            }
        }

        // Check target supertypes
        for (Map.Entry<ConversionKey, Function<Object, Object>> entry : CONVERTERS.entrySet()) {
            ConversionKey key = entry.getKey();
            if (key.sourceType.isAssignableFrom(sourceType) && targetType.isAssignableFrom(key.targetType)) {
                return entry.getValue();
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getDefaultValue(Class<T> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) return (T) Boolean.FALSE;
            if (type == byte.class) return (T) Byte.valueOf((byte) 0);
            if (type == short.class) return (T) Short.valueOf((short) 0);
            if (type == int.class) return (T) Integer.valueOf(0);
            if (type == long.class) return (T) Long.valueOf(0L);
            if (type == float.class) return (T) Float.valueOf(0f);
            if (type == double.class) return (T) Double.valueOf(0d);
            if (type == char.class) return (T) Character.valueOf('\0');
        }
        return null;
    }

    private record ConversionKey(Class<?> sourceType, Class<?> targetType) {
    }
}
