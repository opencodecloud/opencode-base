package cloud.opencode.base.core.convert.impl;

import cloud.opencode.base.core.convert.Converter;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * String Type Converter - Converts any type to String representation
 * 字符串类型转换器 - 将任意类型转换为字符串
 *
 * <p>Converts various types to String with charset and separator support.</p>
 * <p>将各种类型转换为字符串，支持字符集和分隔符配置。</p>
 *
 * <p><strong>Supported Types | 支持的类型:</strong></p>
 * <ul>
 *   <li>Primitives and wrappers - 基本类型和包装类型</li>
 *   <li>Arrays and collections (comma-separated) - 数组和集合</li>
 *   <li>Date/Time types (ISO format) - 日期时间类型</li>
 *   <li>Byte arrays (with charset) - 字节数组</li>
 *   <li>Enums (using name()) - 枚举类型</li>
 *   <li>Any object (using toString()) - 任意对象</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic usage - 基本用法
 * String s = StringConverter.instance().convert(123);  // "123"
 *
 * // Array to string - 数组转字符串
 * String arr = StringConverter.instance().convert(new int[]{1,2,3});  // "1,2,3"
 *
 * // Custom charset - 自定义字符集
 * StringConverter conv = new StringConverter(StandardCharsets.GBK, ";");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Convert any type to String representation - 将任意类型转换为字符串</li>
 *   <li>Configurable charset and separator - 可配置字符集和分隔符</li>
 *   <li>Special handling for arrays, collections, dates, enums - 数组、集合、日期、枚举的特殊处理</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = input length - O(n), n为输入长度</li>
 *   <li>Space complexity: O(n) for output string - 输出字符串 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class StringConverter implements Converter<String> {

    private static final StringConverter INSTANCE = new StringConverter();

    private final Charset charset;
    private final String arraySeparator;

    public StringConverter() {
        this(StandardCharsets.UTF_8, ",");
    }

    public StringConverter(Charset charset, String arraySeparator) {
        this.charset = charset;
        this.arraySeparator = arraySeparator;
    }

    @Override
    public String convert(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof CharSequence cs) {
            return cs.toString();
        }

        try {
            return doConvert(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Performs the actual conversion
     * 执行实际转换
     */
    private String doConvert(Object value) {
        // 基本类型
        if (value instanceof Number || value instanceof Boolean || value instanceof Character) {
            return value.toString();
        }

        // 枚举
        if (value instanceof Enum<?> e) {
            return e.name();
        }

        // 字节数组
        if (value instanceof byte[] bytes) {
            return new String(bytes, charset);
        }

        // char 数组
        if (value instanceof char[] chars) {
            return new String(chars);
        }

        // 日期时间
        if (value instanceof TemporalAccessor ta) {
            return formatTemporal(ta);
        }
        if (value instanceof Date d) {
            return d.toInstant().toString();
        }
        if (value instanceof Calendar c) {
            return c.toInstant().toString();
        }

        // 数组
        if (value.getClass().isArray()) {
            return arrayToString(value);
        }

        // 集合
        if (value instanceof Collection<?> coll) {
            return collectionToString(coll);
        }

        // Map
        if (value instanceof Map<?, ?> map) {
            return mapToString(map);
        }

        // Optional
        if (value instanceof Optional<?> opt) {
            return opt.map(this::convert).orElse(null);
        }

        // Reader
        if (value instanceof Reader reader) {
            return readerToString(reader);
        }

        // 其他类型使用 toString
        return value.toString();
    }

    /**
     * Formats a temporal type
     * 格式化时间类型
     */
    private String formatTemporal(TemporalAccessor ta) {
        try {
            return DateTimeFormatter.ISO_DATE_TIME.format(ta);
        } catch (Exception e1) {
            try {
                return DateTimeFormatter.ISO_DATE.format(ta);
            } catch (Exception e2) {
                try {
                    return DateTimeFormatter.ISO_TIME.format(ta);
                } catch (Exception e3) {
                    return ta.toString();
                }
            }
        }
    }

    /**
     * Converts an array to string
     * 数组转字符串
     */
    private String arrayToString(Object array) {
        int length = Array.getLength(array);
        if (length == 0) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(arraySeparator);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            joiner.add(element == null ? "" : element.toString());
        }
        return joiner.toString();
    }

    /**
     * Converts a collection to string
     * 集合转字符串
     */
    private String collectionToString(Collection<?> coll) {
        if (coll.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(arraySeparator);
        for (Object element : coll) {
            joiner.add(element == null ? "" : element.toString());
        }
        return joiner.toString();
    }

    /**
     * Map 转字符串
     */
    private String mapToString(Map<?, ?> map) {
        if (map.isEmpty()) {
            return "{}";
        }
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey() == null ? "null" : entry.getKey().toString();
            String val = entry.getValue() == null ? "null" : entry.getValue().toString();
            joiner.add(key + "=" + val);
        }
        return joiner.toString();
    }

    /**
     * Reader 转字符串
     */
    private String readerToString(Reader reader) {
        try (reader) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[8192];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 静态方法 ====================

    /**
     * Gets the default instance
     * 获取默认实例
     */
    public static StringConverter getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a converter with a custom charset
     * 创建带自定义字符集的转换器
     */
    public static StringConverter of(Charset charset) {
        return new StringConverter(charset, ",");
    }

    /**
     * Creates a converter with a custom separator
     * 创建带自定义分隔符的转换器
     */
    public static StringConverter of(String separator) {
        return new StringConverter(StandardCharsets.UTF_8, separator);
    }

    /**
     * Creates a fully customized converter
     * 创建完全自定义的转换器
     */
    public static StringConverter of(Charset charset, String separator) {
        return new StringConverter(charset, separator);
    }
}
