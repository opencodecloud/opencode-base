package cloud.opencode.base.expression.eval;

import cloud.opencode.base.expression.OpenExpressionException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

/**
 * Type Coercion Utility
 * 类型转换工具
 *
 * <p>Provides type conversion and coercion for expression evaluation.</p>
 * <p>为表达式求值提供类型转换和强制转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Convert to primitives: boolean, int, long, double - 转换为基本类型</li>
 *   <li>Convert to BigDecimal, BigInteger - 转换为BigDecimal、BigInteger</li>
 *   <li>Convert to date types: LocalDate, LocalDateTime - 转换为日期类型</li>
 *   <li>Truthiness evaluation for any type - 任何类型的真值求值</li>
 *   <li>Convertibility check - 可转换性检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean b = TypeCoercion.toBoolean("true");  // true
 * int i = TypeCoercion.toInt("42");  // 42
 * Integer typed = TypeCoercion.convert("42", Integer.class);  // 42
 * boolean can = TypeCoercion.canConvert("42", Integer.class);  // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, stateless utility class - 线程安全: 是，无状态工具类</li>
 *   <li>Null-safe: Yes, null returns default values (0, false, "null") - 空值安全: 是，null返回默认值</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public final class TypeCoercion {

    private TypeCoercion() {
    }

    /**
     * Convert value to boolean
     * 将值转换为布尔值
     *
     * @param value the value | 值
     * @return the boolean value | 布尔值
     */
    public static boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.doubleValue() != 0;
        }
        if (value instanceof String s) {
            return !s.isEmpty() && !"false".equalsIgnoreCase(s);
        }
        if (value instanceof Collection<?> c) {
            return !c.isEmpty();
        }
        if (value instanceof Map<?, ?> m) {
            return !m.isEmpty();
        }
        return true; // Non-null objects are truthy
    }

    /**
     * Convert value to integer
     * 将值转换为整数
     *
     * @param value the value | 值
     * @return the integer value | 整数值
     */
    public static int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                throw OpenExpressionException.typeError("integer", value);
            }
        }
        if (value instanceof Boolean b) {
            return b ? 1 : 0;
        }
        throw OpenExpressionException.typeError("integer", value);
    }

    /**
     * Convert value to long
     * 将值转换为长整数
     *
     * @param value the value | 值
     * @return the long value | 长整数值
     */
    public static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException e) {
                throw OpenExpressionException.typeError("long", value);
            }
        }
        if (value instanceof Boolean b) {
            return b ? 1L : 0L;
        }
        throw OpenExpressionException.typeError("long", value);
    }

    /**
     * Convert value to double
     * 将值转换为双精度浮点数
     *
     * @param value the value | 值
     * @return the double value | 双精度值
     */
    public static double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException e) {
                throw OpenExpressionException.typeError("double", value);
            }
        }
        if (value instanceof Boolean b) {
            return b ? 1.0 : 0.0;
        }
        throw OpenExpressionException.typeError("double", value);
    }

    /**
     * Convert value to string
     * 将值转换为字符串
     *
     * @param value the value | 值
     * @return the string value | 字符串值
     */
    public static String toString(Object value) {
        if (value == null) {
            return "null";
        }
        return value.toString();
    }

    /**
     * Convert value to target type
     * 将值转换为目标类型
     *
     * @param value the value | 值
     * @param targetType the target type | 目标类型
     * @param <T> the target type | 目标类型
     * @return the converted value | 转换后的值
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isInstance(value)) {
            return (T) value;
        }

        // Boolean conversion
        if (targetType == Boolean.class || targetType == boolean.class) {
            return (T) Boolean.valueOf(toBoolean(value));
        }

        // Integer conversion
        if (targetType == Integer.class || targetType == int.class) {
            return (T) Integer.valueOf(toInt(value));
        }

        // Long conversion
        if (targetType == Long.class || targetType == long.class) {
            return (T) Long.valueOf(toLong(value));
        }

        // Double conversion
        if (targetType == Double.class || targetType == double.class) {
            return (T) Double.valueOf(toDouble(value));
        }

        // Float conversion
        if (targetType == Float.class || targetType == float.class) {
            return (T) Float.valueOf((float) toDouble(value));
        }

        // String conversion
        if (targetType == String.class) {
            return (T) toString(value);
        }

        // BigDecimal conversion
        if (targetType == BigDecimal.class) {
            if (value instanceof Number n) {
                return (T) BigDecimal.valueOf(n.doubleValue());
            }
            if (value instanceof String s) {
                return (T) new BigDecimal(s.trim());
            }
        }

        // BigInteger conversion
        if (targetType == BigInteger.class) {
            if (value instanceof Number n) {
                return (T) BigInteger.valueOf(n.longValue());
            }
            if (value instanceof String s) {
                return (T) new BigInteger(s.trim());
            }
        }

        // LocalDate conversion
        if (targetType == LocalDate.class) {
            if (value instanceof String s) {
                return (T) LocalDate.parse(s.trim());
            }
            if (value instanceof LocalDateTime ldt) {
                return (T) ldt.toLocalDate();
            }
        }

        // LocalDateTime conversion
        if (targetType == LocalDateTime.class) {
            if (value instanceof String s) {
                return (T) LocalDateTime.parse(s.trim());
            }
            if (value instanceof LocalDate ld) {
                return (T) ld.atStartOfDay();
            }
        }

        throw OpenExpressionException.typeError(targetType.getSimpleName(), value);
    }

    /**
     * Check if value can be converted to target type
     * 检查值是否可以转换为目标类型
     *
     * @param value the value | 值
     * @param targetType the target type | 目标类型
     * @return true if convertible | 如果可转换返回true
     */
    public static boolean canConvert(Object value, Class<?> targetType) {
        if (value == null) {
            return !targetType.isPrimitive();
        }
        if (targetType.isInstance(value)) {
            return true;
        }

        // Primitive and wrapper types
        if (targetType == Boolean.class || targetType == boolean.class ||
            targetType == Integer.class || targetType == int.class ||
            targetType == Long.class || targetType == long.class ||
            targetType == Double.class || targetType == double.class ||
            targetType == Float.class || targetType == float.class ||
            targetType == String.class) {
            return true;
        }

        // Number to BigDecimal/BigInteger
        if ((targetType == BigDecimal.class || targetType == BigInteger.class) &&
            (value instanceof Number || value instanceof String)) {
            return true;
        }

        return false;
    }
}
