package cloud.opencode.base.core.convert.impl;

import cloud.opencode.base.core.convert.Converter;
import cloud.opencode.base.core.convert.ConverterRegistry;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Array Type Converter - Converts various sources to arrays
 * 数组类型转换器 - 将各种来源转换为数组
 *
 * <p>Supports converting arrays, collections, iterables, strings and single objects to arrays.</p>
 * <p>支持将数组、集合、Iterable、字符串和单个对象转换为数组。</p>
 *
 * <p><strong>Supported Conversions | 支持的转换:</strong></p>
 * <ul>
 *   <li>Array → Array (different element types) - 数组转数组</li>
 *   <li>Collection → Array - 集合转数组</li>
 *   <li>Iterable/Iterator → Array - 可迭代对象转数组</li>
 *   <li>String (comma-separated) → Array - 字符串转数组</li>
 *   <li>Single object → Single-element array - 单对象转单元素数组</li>
 * </ul>
 *
 * <p><strong>Primitive Array Converters | 原始类型数组转换器:</strong></p>
 * <ul>
 *   <li>intArrayConverter(), longArrayConverter(), doubleArrayConverter()</li>
 *   <li>floatArrayConverter(), booleanArrayConverter(), byteArrayConverter()</li>
 *   <li>shortArrayConverter(), charArrayConverter()</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // String to String array - 字符串转数组
 * String[] arr = ArrayConverter.stringArrayConverter().convert("a,b,c");
 *
 * // List to array - 列表转数组
 * Integer[] nums = ArrayConverter.of(Integer.class).convert(list);
 *
 * // Primitive array - 原始类型数组
 * int[] ints = ArrayConverter.intArrayConverter().convert("1,2,3");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Array, Collection, Iterable, String to array conversion - 数组、集合、可迭代对象、字符串转数组</li>
 *   <li>Primitive array converters (int[], long[], etc.) - 原始类型数组转换器</li>
 *   <li>Factory methods for common array types - 常见数组类型的工厂方法</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = array length - O(n), n为数组长度</li>
 *   <li>Space complexity: O(n) for output array - 输出数组 O(n)</li>
 * </ul>
 *
 * @param <T> target array element type - 目标数组的元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class ArrayConverter<T> implements Converter<T[]> {

    private final Class<T> componentType;
    private final String separator;

    public ArrayConverter(Class<T> componentType) {
        this(componentType, ",");
    }

    public ArrayConverter(Class<T> componentType, String separator) {
        this.componentType = componentType;
        this.separator = separator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] convert(Object value, T[] defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        try {
            // 已经是目标类型数组
            if (value.getClass().isArray() && componentType.isAssignableFrom(value.getClass().getComponentType())) {
                return (T[]) value;
            }

            List<Object> list = toList(value);
            if (list == null) {
                return defaultValue;
            }

            return listToArray(list);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Converts the input value to a List
     * 将输入值转换为 List
     */
    private List<Object> toList(Object value) {
        // 数组
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                list.add(Array.get(value, i));
            }
            return list;
        }

        // 集合
        if (value instanceof Collection<?> coll) {
            return new ArrayList<>(coll);
        }

        // Iterable
        if (value instanceof Iterable<?> iter) {
            List<Object> list = new ArrayList<>();
            for (Object item : iter) {
                list.add(item);
            }
            return list;
        }

        // Iterator
        if (value instanceof Iterator<?> iter) {
            List<Object> list = new ArrayList<>();
            while (iter.hasNext()) {
                list.add(iter.next());
            }
            return list;
        }

        // 字符串 - 按分隔符拆分
        if (value instanceof CharSequence cs) {
            String str = cs.toString().trim();
            if (str.isEmpty()) {
                return Collections.emptyList();
            }
            String[] parts = str.split(Pattern.quote(separator));
            List<Object> list = new ArrayList<>(parts.length);
            for (String part : parts) {
                list.add(part.trim());
            }
            return list;
        }

        // 单个对象 - 转为单元素列表
        return Collections.singletonList(value);
    }

    /**
     * Converts a List to the target type array
     * 将 List 转换为目标类型数组
     */
    @SuppressWarnings("unchecked")
    private T[] listToArray(List<Object> list) {
        T[] array = (T[]) Array.newInstance(componentType, list.size());

        Converter<T> elementConverter = ConverterRegistry.getConverter(componentType);

        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            if (element == null) {
                array[i] = null;
            } else if (componentType.isInstance(element)) {
                array[i] = (T) element;
            } else if (elementConverter != null) {
                array[i] = elementConverter.convert(element);
            } else {
                // 尝试直接转换
                array[i] = convertElement(element);
            }
        }
        return array;
    }

    /**
     * Converts a single element
     * 转换单个元素
     */
    @SuppressWarnings("unchecked")
    private T convertElement(Object element) {
        if (componentType == String.class) {
            return (T) element.toString();
        }
        if (componentType.isInstance(element)) {
            return (T) element;
        }
        return null;
    }

    // ==================== 原始类型数组转换器 ====================

    /**
     * Returns an int[] converter
     * int[] 转换器
     */
    public static Converter<int[]> intArrayConverter() {
        return new PrimitiveArrayConverter<>(int.class, Integer::parseInt);
    }

    /**
     * Returns a long[] converter
     * long[] 转换器
     */
    public static Converter<long[]> longArrayConverter() {
        return new PrimitiveArrayConverter<>(long.class, Long::parseLong);
    }

    /**
     * Returns a double[] converter
     * double[] 转换器
     */
    public static Converter<double[]> doubleArrayConverter() {
        return new PrimitiveArrayConverter<>(double.class, Double::parseDouble);
    }

    /**
     * Returns a float[] converter
     * float[] 转换器
     */
    public static Converter<float[]> floatArrayConverter() {
        return new PrimitiveArrayConverter<>(float.class, Float::parseFloat);
    }

    /**
     * Returns a boolean[] converter
     * boolean[] 转换器
     */
    public static Converter<boolean[]> booleanArrayConverter() {
        return new PrimitiveArrayConverter<>(boolean.class, Boolean::parseBoolean);
    }

    /**
     * Returns a byte[] converter
     * byte[] 转换器
     */
    public static Converter<byte[]> byteArrayConverter() {
        return new PrimitiveArrayConverter<>(byte.class, Byte::parseByte);
    }

    /**
     * Returns a short[] converter
     * short[] 转换器
     */
    public static Converter<short[]> shortArrayConverter() {
        return new PrimitiveArrayConverter<>(short.class, Short::parseShort);
    }

    /**
     * Returns a char[] converter
     * char[] 转换器
     */
    public static Converter<char[]> charArrayConverter() {
        return (value, defaultValue) -> {
            if (value == null) {
                return defaultValue;
            }
            if (value instanceof char[] chars) {
                return chars;
            }
            if (value instanceof CharSequence cs) {
                return cs.toString().toCharArray();
            }
            return value.toString().toCharArray();
        };
    }

    // ==================== 对象数组工厂方法 ====================

    public static Converter<String[]> stringArrayConverter() {
        return new ArrayConverter<>(String.class);
    }

    public static Converter<Integer[]> integerArrayConverter() {
        return new ArrayConverter<>(Integer.class);
    }

    public static Converter<Long[]> longObjArrayConverter() {
        return new ArrayConverter<>(Long.class);
    }

    public static Converter<Double[]> doubleObjArrayConverter() {
        return new ArrayConverter<>(Double.class);
    }

    public static <E> Converter<E[]> of(Class<E> componentType) {
        return new ArrayConverter<>(componentType);
    }

    public static <E> Converter<E[]> of(Class<E> componentType, String separator) {
        return new ArrayConverter<>(componentType, separator);
    }

    // ==================== 原始类型数组转换器内部类 ====================

    /**
     * Primitive type array converter
     * 原始类型数组转换器
     */
    private static class PrimitiveArrayConverter<T> implements Converter<T> {

        private final Class<?> componentType;
        private final java.util.function.Function<String, ?> parser;
        private final String separator;

        PrimitiveArrayConverter(Class<?> componentType, java.util.function.Function<String, ?> parser) {
            this(componentType, parser, ",");
        }

        PrimitiveArrayConverter(Class<?> componentType, java.util.function.Function<String, ?> parser, String separator) {
            this.componentType = componentType;
            this.parser = parser;
            this.separator = separator;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T convert(Object value, T defaultValue) {
            if (value == null) {
                return defaultValue;
            }

            try {
                List<Object> list = toList(value);
                if (list == null) {
                    return defaultValue;
                }
                if (list.isEmpty()) {
                    return (T) Array.newInstance(componentType, 0);
                }
                return (T) toPrimitiveArray(list);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        private List<Object> toList(Object value) {
            if (value.getClass().isArray()) {
                int length = Array.getLength(value);
                List<Object> list = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    list.add(Array.get(value, i));
                }
                return list;
            }
            if (value instanceof Collection<?> coll) {
                return new ArrayList<>(coll);
            }
            if (value instanceof CharSequence cs) {
                String str = cs.toString().trim();
                if (str.isEmpty()) {
                    return Collections.emptyList();
                }
                String[] parts = str.split(Pattern.quote(separator));
                List<Object> list = new ArrayList<>(parts.length);
                for (String part : parts) {
                    list.add(part.trim());
                }
                return list;
            }
            return Collections.singletonList(value);
        }

        private Object toPrimitiveArray(List<Object> list) {
            Object array = Array.newInstance(componentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                Object element = list.get(i);
                if (element instanceof Number n) {
                    setPrimitiveValue(array, i, n);
                } else if (element instanceof Boolean b && componentType == boolean.class) {
                    Array.setBoolean(array, i, b);
                } else {
                    String str = element.toString().trim();
                    Object parsed = parser.apply(str);
                    setParsedValue(array, i, parsed);
                }
            }
            return array;
        }

        private void setPrimitiveValue(Object array, int index, Number n) {
            if (componentType == int.class) {
                Array.setInt(array, index, n.intValue());
            } else if (componentType == long.class) {
                Array.setLong(array, index, n.longValue());
            } else if (componentType == double.class) {
                Array.setDouble(array, index, n.doubleValue());
            } else if (componentType == float.class) {
                Array.setFloat(array, index, n.floatValue());
            } else if (componentType == byte.class) {
                Array.setByte(array, index, n.byteValue());
            } else if (componentType == short.class) {
                Array.setShort(array, index, n.shortValue());
            }
        }

        private void setParsedValue(Object array, int index, Object parsed) {
            if (componentType == int.class) {
                Array.setInt(array, index, ((Number) parsed).intValue());
            } else if (componentType == long.class) {
                Array.setLong(array, index, ((Number) parsed).longValue());
            } else if (componentType == double.class) {
                Array.setDouble(array, index, ((Number) parsed).doubleValue());
            } else if (componentType == float.class) {
                Array.setFloat(array, index, ((Number) parsed).floatValue());
            } else if (componentType == boolean.class) {
                Array.setBoolean(array, index, (Boolean) parsed);
            } else if (componentType == byte.class) {
                Array.setByte(array, index, ((Number) parsed).byteValue());
            } else if (componentType == short.class) {
                Array.setShort(array, index, ((Number) parsed).shortValue());
            }
        }
    }
}
