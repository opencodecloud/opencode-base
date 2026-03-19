package cloud.opencode.base.core.convert;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Unified Type Conversion Entry - Static methods for type conversion
 * 统一类型转换入口 - 类型转换的静态方法
 *
 * <p>Provides convenient static methods for common type conversions using registered converters.</p>
 * <p>提供使用注册转换器的常用类型转换便捷静态方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Primitive conversions (toInt, toLong, toDouble, toBoolean) - 基本类型转换</li>
 *   <li>String conversion (toStr) - 字符串转换</li>
 *   <li>Date/Time conversion (toDate, toLocalDate, toLocalDateTime) - 日期时间转换</li>
 *   <li>Array conversion (toArray) - 数组转换</li>
 *   <li>Generic conversion (convert with TypeReference) - 泛型转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic type conversion - 基本类型转换
 * Integer num = Convert.toInt("123", 0);
 * Boolean b = Convert.toBool("true");
 *
 * // Date conversion - 日期转换
 * LocalDate date = Convert.toLocalDate("2024-01-15");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses thread-safe registry) - 线程安全: 是 (使用线程安全注册表)</li>
 *   <li>Null-safe: Yes (returns default on null) - 空值安全: 是 (null 返回默认值)</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class Convert {

    private Convert() {
    }

    // ==================== 基本类型转换 ====================

    /**
     * Converts
     * 转换为 Integer
     */
    public static Integer toInt(Object value) {
        return toInt(value, null);
    }

    /**
     * Converts
     * 转换为 Integer，带默认值
     */
    public static Integer toInt(Object value, Integer defaultValue) {
        Converter<Integer> converter = ConverterRegistry.getConverter(Integer.class);
        return converter != null ? converter.convert(value, defaultValue) : defaultValue;
    }

    /**
     * Converts
     * 转换为 Long
     */
    public static Long toLong(Object value) {
        return toLong(value, null);
    }

    /**
     * Converts
     * 转换为 Long，带默认值
     */
    public static Long toLong(Object value, Long defaultValue) {
        Converter<Long> converter = ConverterRegistry.getConverter(Long.class);
        return converter != null ? converter.convert(value, defaultValue) : defaultValue;
    }

    /**
     * Converts
     * 转换为 Double
     */
    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }

    /**
     * Converts
     * 转换为 Double，带默认值
     */
    public static Double toDouble(Object value, Double defaultValue) {
        Converter<Double> converter = ConverterRegistry.getConverter(Double.class);
        return converter != null ? converter.convert(value, defaultValue) : defaultValue;
    }

    /**
     * Converts
     * 转换为 Float
     */
    public static Float toFloat(Object value) {
        return toFloat(value, null);
    }

    /**
     * Converts
     * 转换为 Float，带默认值
     */
    public static Float toFloat(Object value, Float defaultValue) {
        Converter<Float> converter = ConverterRegistry.getConverter(Float.class);
        return converter != null ? converter.convert(value, defaultValue) : defaultValue;
    }

    /**
     * Converts
     * 转换为 Short
     */
    public static Short toShort(Object value) {
        return toShort(value, null);
    }

    /**
     * Converts
     * 转换为 Short，带默认值
     */
    public static Short toShort(Object value, Short defaultValue) {
        Converter<Short> converter = ConverterRegistry.getConverter(Short.class);
        return converter != null ? converter.convert(value, defaultValue) : defaultValue;
    }

    /**
     * Converts
     * 转换为 Byte
     */
    public static Byte toByte(Object value) {
        return toByte(value, null);
    }

    /**
     * Converts
     * 转换为 Byte，带默认值
     */
    public static Byte toByte(Object value, Byte defaultValue) {
        Converter<Byte> converter = ConverterRegistry.getConverter(Byte.class);
        return converter != null ? converter.convert(value, defaultValue) : defaultValue;
    }

    /**
     * Converts
     * 转换为 Boolean
     */
    public static Boolean toBool(Object value) {
        return toBool(value, null);
    }

    /**
     * Converts
     * 转换为 Boolean，带默认值
     */
    public static Boolean toBool(Object value, Boolean defaultValue) {
        Converter<Boolean> converter = ConverterRegistry.getConverter(Boolean.class);
        return converter != null ? converter.convert(value, defaultValue) : defaultValue;
    }

    /**
     * Converts
     * 转换为 Character
     */
    public static Character toChar(Object value) {
        return toChar(value, null);
    }

    /**
     * Converts
     * 转换为 Character，带默认值
     */
    public static Character toChar(Object value, Character defaultValue) {
        Converter<Character> converter = ConverterRegistry.getConverter(Character.class);
        return converter != null ? converter.convert(value, defaultValue) : defaultValue;
    }

    /**
     * Converts
     * 转换为 String
     */
    public static String toStr(Object value) {
        return toStr(value, null);
    }

    /**
     * Converts
     * 转换为 String，带默认值
     */
    public static String toStr(Object value, String defaultValue) {
        if (value == null) return defaultValue;
        return value.toString();
    }

    // ==================== 数组转换 ====================

    /**
     * Converts
     * 转换为 int 数组
     */
    public static int[] toIntArray(Object value) {
        if (value == null) return new int[0];
        if (value instanceof int[] arr) return arr;
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            int[] result = new int[length];
            for (int i = 0; i < length; i++) {
                Integer v = toInt(Array.get(value, i));
                result[i] = v != null ? v : 0;
            }
            return result;
        }
        if (value instanceof Collection<?> coll) {
            int[] result = new int[coll.size()];
            int i = 0;
            for (Object o : coll) {
                Integer v = toInt(o);
                result[i++] = v != null ? v : 0;
            }
            return result;
        }
        if (value instanceof String str) {
            return parseIntArray(str);
        }
        Integer v = toInt(value);
        return v != null ? new int[]{v} : new int[0];
    }

    /**
     * Converts
     * 转换为 long 数组
     */
    public static long[] toLongArray(Object value) {
        if (value == null) return new long[0];
        if (value instanceof long[] arr) return arr;
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            long[] result = new long[length];
            for (int i = 0; i < length; i++) {
                Long v = toLong(Array.get(value, i));
                result[i] = v != null ? v : 0L;
            }
            return result;
        }
        if (value instanceof Collection<?> coll) {
            long[] result = new long[coll.size()];
            int i = 0;
            for (Object o : coll) {
                Long v = toLong(o);
                result[i++] = v != null ? v : 0L;
            }
            return result;
        }
        if (value instanceof String str) {
            return parseLongArray(str);
        }
        Long v = toLong(value);
        return v != null ? new long[]{v} : new long[0];
    }

    /**
     * Converts
     * 转换为 String 数组
     */
    public static String[] toStrArray(Object value) {
        if (value == null) return new String[0];
        if (value instanceof String[] arr) return arr;
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            String[] result = new String[length];
            for (int i = 0; i < length; i++) {
                result[i] = toStr(Array.get(value, i));
            }
            return result;
        }
        if (value instanceof Collection<?> coll) {
            String[] result = new String[coll.size()];
            int i = 0;
            for (Object o : coll) {
                result[i++] = toStr(o);
            }
            return result;
        }
        if (value instanceof String str) {
            return str.split(",");
        }
        return new String[]{toStr(value)};
    }

    // ==================== 集合转换 ====================

    /**
     * Converts
     * 转换为 List
     */
    public static <T> List<T> toList(Object value, Class<T> elementType) {
        if (value == null) return new ArrayList<>();
        if (value instanceof List<?> list) {
            List<T> result = new ArrayList<>(list.size());
            for (Object o : list) {
                result.add(TypeUtil.convert(o, elementType));
            }
            return result;
        }
        if (value instanceof Collection<?> coll) {
            List<T> result = new ArrayList<>(coll.size());
            for (Object o : coll) {
                result.add(TypeUtil.convert(o, elementType));
            }
            return result;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<T> result = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                result.add(TypeUtil.convert(Array.get(value, i), elementType));
            }
            return result;
        }
        if (value instanceof String str && !str.isEmpty()) {
            String[] parts = str.split(",");
            List<T> result = new ArrayList<>(parts.length);
            for (String part : parts) {
                result.add(TypeUtil.convert(part.trim(), elementType));
            }
            return result;
        }
        List<T> result = new ArrayList<>(1);
        result.add(TypeUtil.convert(value, elementType));
        return result;
    }

    /**
     * Converts
     * 转换为 Set
     */
    public static <T> Set<T> toSet(Object value, Class<T> elementType) {
        List<T> list = toList(value, elementType);
        return new LinkedHashSet<>(list);
    }

    // ==================== 泛型转换 ====================

    /**
     * Generic type conversion
     * 泛型转换
     */
    public static <T> T convert(Object value, Class<T> clazz) {
        return TypeUtil.convert(value, clazz);
    }

    /**
     * Generic type conversion (using TypeReference)
     * 泛型转换（使用 TypeReference）
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object value, TypeReference<T> typeRef) {
        if (value == null) return null;
        Class<T> rawType = typeRef.getRawType();
        if (rawType != null) {
            return convert(value, rawType);
        }
        return (T) value;
    }

    // ==================== 私有辅助方法 ====================

    private static int[] parseIntArray(String str) {
        if (str == null || str.isEmpty()) return new int[0];
        String[] parts = str.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            Integer v = toInt(parts[i].trim());
            result[i] = v != null ? v : 0;
        }
        return result;
    }

    private static long[] parseLongArray(String str) {
        if (str == null || str.isEmpty()) return new long[0];
        String[] parts = str.split(",");
        long[] result = new long[parts.length];
        for (int i = 0; i < parts.length; i++) {
            Long v = toLong(parts[i].trim());
            result[i] = v != null ? v : 0L;
        }
        return result;
    }
}
