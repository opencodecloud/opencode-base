package cloud.opencode.base.core.convert.impl;

import cloud.opencode.base.core.convert.Converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Number Type Converter - Converts between all numeric types
 * 数字类型转换器 - 支持所有数字类型的相互转换
 *
 * <p>Supports conversion between primitive, wrapper, big number and atomic number types.</p>
 * <p>支持基本类型、包装类型、大数类型和原子类型的相互转换。</p>
 *
 * <p><strong>Supported Types | 支持的类型:</strong></p>
 * <ul>
 *   <li>Primitives: byte, short, int, long, float, double - 基本类型</li>
 *   <li>Wrappers: Byte, Short, Integer, Long, Float, Double - 包装类型</li>
 *   <li>Big numbers: BigDecimal, BigInteger - 大数类型</li>
 *   <li>Atomic: AtomicInteger, AtomicLong - 原子类型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use via registry - 通过注册表使用
 * Converter<Integer> conv = ConverterRegistry.getConverter(Integer.class);
 * Integer result = conv.convert("123", 0);
 *
 * // Use factory methods - 使用工厂方法
 * Converter<BigDecimal> bdConv = NumberConverter.bigDecimalConverter();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes (returns default on null) - 空值安全: 是 (null 返回默认值)</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>All numeric type conversion (primitives, wrappers, BigDecimal, BigInteger) - 所有数值类型转换</li>
 *   <li>Hex, binary, and octal string parsing - 十六进制、二进制和八进制字符串解析</li>
 *   <li>Atomic number type support (AtomicInteger, AtomicLong) - 原子数值类型支持</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per conversion - 每次转换 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @param <T> target number type - 目标数字类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class NumberConverter<T extends Number> implements Converter<T> {

    private final Class<T> targetType;

    public NumberConverter(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T convert(Object value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (targetType.isInstance(value)) {
            return (T) value;
        }

        Number number = toNumber(value);
        if (number == null) {
            return defaultValue;
        }

        return convertNumber(number, defaultValue);
    }

    /**
     * Converts any object to a Number
     * 将任意对象转换为 Number
     */
    private Number toNumber(Object value) {
        if (value instanceof Number n) {
            return n;
        }
        if (value instanceof Boolean b) {
            return b ? 1 : 0;
        }
        if (value instanceof Character c) {
            return (int) c;
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return null;
        }
        try {
            // 处理十六进制
            if (str.startsWith("0x") || str.startsWith("0X")) {
                return Long.parseLong(str.substring(2), 16);
            }
            // 处理二进制（必须在八进制之前检查）
            if (str.startsWith("0b") || str.startsWith("0B")) {
                return Long.parseLong(str.substring(2), 2);
            }
            // 处理八进制
            if (str.startsWith("0") && str.length() > 1 && !str.contains(".")) {
                return Long.parseLong(str.substring(1), 8);
            }
            // 普通数字解析 — guard against DoS via extremely long strings
            if (str.length() > 10_000) {
                return null;
            }
            return new BigDecimal(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts a Number to the target type
     * 将 Number 转换为目标类型
     */
    @SuppressWarnings("unchecked")
    private T convertNumber(Number number, T defaultValue) {
        try {
            if (targetType == Byte.class || targetType == byte.class) {
                return (T) Byte.valueOf(number.byteValue());
            }
            if (targetType == Short.class || targetType == short.class) {
                return (T) Short.valueOf(number.shortValue());
            }
            if (targetType == Integer.class || targetType == int.class) {
                return (T) Integer.valueOf(number.intValue());
            }
            if (targetType == Long.class || targetType == long.class) {
                return (T) Long.valueOf(number.longValue());
            }
            if (targetType == Float.class || targetType == float.class) {
                return (T) Float.valueOf(number.floatValue());
            }
            if (targetType == Double.class || targetType == double.class) {
                return (T) Double.valueOf(number.doubleValue());
            }
            if (targetType == BigDecimal.class) {
                if (number instanceof BigDecimal bd) {
                    return (T) bd;
                }
                if (number instanceof BigInteger bi) {
                    return (T) new BigDecimal(bi);
                }
                if (number instanceof Double || number instanceof Float) {
                    return (T) BigDecimal.valueOf(number.doubleValue());
                }
                return (T) BigDecimal.valueOf(number.longValue());
            }
            if (targetType == BigInteger.class) {
                if (number instanceof BigInteger bi) {
                    return (T) bi;
                }
                if (number instanceof BigDecimal bd) {
                    return (T) bd.toBigInteger();
                }
                return (T) BigInteger.valueOf(number.longValue());
            }
            if (targetType == AtomicInteger.class) {
                return (T) new AtomicInteger(number.intValue());
            }
            if (targetType == AtomicLong.class) {
                return (T) new AtomicLong(number.longValue());
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ==================== 静态工厂方法 ====================

    public static Converter<Byte> byteConverter() {
        return new NumberConverter<>(Byte.class);
    }

    public static Converter<Short> shortConverter() {
        return new NumberConverter<>(Short.class);
    }

    public static Converter<Integer> integerConverter() {
        return new NumberConverter<>(Integer.class);
    }

    public static Converter<Long> longConverter() {
        return new NumberConverter<>(Long.class);
    }

    public static Converter<Float> floatConverter() {
        return new NumberConverter<>(Float.class);
    }

    public static Converter<Double> doubleConverter() {
        return new NumberConverter<>(Double.class);
    }

    public static Converter<BigDecimal> bigDecimalConverter() {
        return new NumberConverter<>(BigDecimal.class);
    }

    public static Converter<BigInteger> bigIntegerConverter() {
        return new NumberConverter<>(BigInteger.class);
    }

    public static Converter<AtomicInteger> atomicIntegerConverter() {
        return new NumberConverter<>(AtomicInteger.class);
    }

    public static Converter<AtomicLong> atomicLongConverter() {
        return new NumberConverter<>(AtomicLong.class);
    }
}
