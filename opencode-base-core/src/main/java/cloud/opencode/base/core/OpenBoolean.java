package cloud.opencode.base.core;

/**
 * Boolean Utility Class - Conversion, logical operations and validation for boolean values
 * 布尔值工具类 - 布尔值的转换、逻辑运算和验证
 *
 * <p>Provides comprehensive boolean operations including conversion, logical operations and validation.</p>
 * <p>提供全面的布尔值操作，包括转换、逻辑运算、验证。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Convert to boolean (toBoolean from String/Integer/Boolean) - 转换为 boolean</li>
 *   <li>Convert to Boolean (toBooleanObject) - 转换为 Boolean</li>
 *   <li>Convert to String (toString, toStringYesNo, toStringOnOff) - 转换为字符串</li>
 *   <li>Convert to Integer (toInteger) - 转换为整数</li>
 *   <li>Validation (isTrue, isFalse, isNotTrue, isNotFalse) - 验证</li>
 *   <li>Logical operations (negate, and, or, xor) - 逻辑运算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // String to boolean - 字符串转 boolean
 * boolean b1 = OpenBoolean.toBoolean("yes");  // true
 * boolean b2 = OpenBoolean.toBoolean("1");    // true
 * boolean b3 = OpenBoolean.toBoolean("on");   // true
 *
 * // Boolean to string - Boolean 转字符串
 * String s = OpenBoolean.toStringYesNo(true); // "yes"
 *
 * // Logical operations - 逻辑运算
 * boolean result = OpenBoolean.and(true, true, false); // false
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenBoolean {

    private OpenBoolean() {
    }

    // ==================== 转换为 boolean ====================

    /**
     * Boolean 转 boolean（null 安全）
     */
    public static boolean toBoolean(Boolean value) {
        return value != null && value;
    }

    /**
     * Converts a string to boolean
     * 字符串转 boolean
     * <p>
     * 以下值视为 true（忽略大小写）：true, yes, y, on, 1
     */
    public static boolean toBoolean(String str) {
        if (str == null) {
            return false;
        }
        String s = str.trim().toLowerCase();
        return "true".equals(s) || "yes".equals(s) || "y".equals(s) ||
                "on".equals(s) || "1".equals(s);
    }

    /**
     * Converts an integer to boolean (0 is false, others are true)
     * 整数转 boolean（0 为 false，其他为 true）
     */
    public static boolean toBoolean(int value) {
        return value != 0;
    }

    // ==================== 转换为 Boolean ====================

    /**
     * boolean 转 Boolean
     */
    public static Boolean toBooleanObject(boolean value) {
        return value;
    }

    /**
     * Converts a string to Boolean (null-safe)
     * 字符串转 Boolean（null 安全）
     */
    public static Boolean toBooleanObject(String str) {
        if (str == null) {
            return null;
        }
        String s = str.trim().toLowerCase();
        if ("true".equals(s) || "yes".equals(s) || "y".equals(s) ||
                "on".equals(s) || "1".equals(s)) {
            return Boolean.TRUE;
        }
        if ("false".equals(s) || "no".equals(s) || "n".equals(s) ||
                "off".equals(s) || "0".equals(s)) {
            return Boolean.FALSE;
        }
        return null;
    }

    // ==================== 转换为字符串 ====================

    /**
     * Boolean 转字符串
     */
    public static String toString(Boolean value) {
        return value == null ? null : value.toString();
    }

    /**
     * Converts to
     * 转为 yes/no 字符串
     */
    public static String toStringYesNo(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? "yes" : "no";
    }

    /**
     * Converts to
     * 转为 on/off 字符串
     */
    public static String toStringOnOff(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? "on" : "off";
    }

    /**
     * Converts to
     * 转为 Y/N 字符串
     */
    public static String toStringYN(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? "Y" : "N";
    }

    // ==================== 转换为整数 ====================

    /**
     * boolean 转整数（true=1, false=0）
     */
    public static int toInteger(boolean value) {
        return value ? 1 : 0;
    }

    /**
     * Boolean 转整数（null=0）
     */
    public static int toInteger(Boolean value) {
        return toBoolean(value) ? 1 : 0;
    }

    // ==================== 判断 ====================

    /**
     * Checks
     * 判断是否为 true
     */
    public static boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    /**
     * Checks
     * 判断是否为 false
     */
    public static boolean isFalse(Boolean value) {
        return Boolean.FALSE.equals(value);
    }

    /**
     * Checks
     * 判断是否不为 true（null 或 false）
     */
    public static boolean isNotTrue(Boolean value) {
        return !isTrue(value);
    }

    /**
     * Checks
     * 判断是否不为 false（null 或 true）
     */
    public static boolean isNotFalse(Boolean value) {
        return !isFalse(value);
    }

    // ==================== 逻辑运算 ====================

    /**
     * Negation operation
     * 取反操作
     */
    public static boolean negate(boolean value) {
        return !value;
    }

    /**
     * Negation operation (Boolean, null returns null)
     * 取反操作（Boolean，null 返回 null）
     */
    public static Boolean negate(Boolean value) {
        if (value == null) {
            return null;
        }
        return !value;
    }

    /**
     * Logical AND operation
     * 逻辑与操作
     */
    public static boolean and(boolean... values) {
        if (values == null || values.length == 0) {
            return false;
        }
        for (boolean value : values) {
            if (!value) {
                return false;
            }
        }
        return true;
    }

    /**
     * Logical OR operation
     * 逻辑或操作
     */
    public static boolean or(boolean... values) {
        if (values == null || values.length == 0) {
            return false;
        }
        for (boolean value : values) {
            if (value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logical XOR operation
     * 逻辑异或操作
     */
    public static boolean xor(boolean... values) {
        if (values == null || values.length == 0) {
            return false;
        }
        int trueCount = 0;
        for (boolean value : values) {
            if (value) {
                trueCount++;
            }
        }
        return trueCount % 2 == 1;
    }

    /**
     * Compares
     * 比较两个 Boolean
     */
    public static int compare(boolean x, boolean y) {
        return Boolean.compare(x, y);
    }
}
