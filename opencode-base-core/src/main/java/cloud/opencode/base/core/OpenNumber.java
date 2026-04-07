package cloud.opencode.base.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Number Utility Class - Validation, parsing, conversion, formatting and arithmetic operations
 * 数值工具类 - 验证、解析、转换、格式化和算术运算
 *
 * <p>Provides comprehensive number operations including validation, parsing, conversion, formatting and range control.</p>
 * <p>提供全面的数值操作，包括验证、解析、转换、格式化和范围控制。参考 Guava Ints/Longs、Commons NumberUtils。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Validation (isNumber, isInteger, isDouble, isParsable) - 验证</li>
 *   <li>Parsing with default value (toInt, toLong, toDouble) - 带默认值解析</li>
 *   <li>Safe parsing with Optional (tryParseInt, tryParseLong) - 安全解析</li>
 *   <li>Overflow-safe conversion (saturatedCast, checkedCast) - 溢出安全转换</li>
 *   <li>Range control (clamp, inRange) - 范围控制</li>
 *   <li>High-precision arithmetic (add, subtract, multiply, divide) - 高精度运算</li>
 *   <li>Formatting (format, formatPercent, formatMoney) - 格式化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Validation - 验证
 * boolean isNum = OpenNumber.isNumber("123.45");
 *
 * // Parsing - 解析
 * int value = OpenNumber.toInt("123", 0);
 * OptionalInt opt = OpenNumber.tryParseInt("123");
 *
 * // Range control - 范围控制
 * int clamped = OpenNumber.clamp(value, 0, 100);
 *
 * // High-precision arithmetic - 高精度运算
 * BigDecimal result = OpenNumber.add(a, b, c);
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
public final class OpenNumber {

    private OpenNumber() {
        // 工具类不可实例化
    }

    // ==================== 验证 ====================

    /**
     * Returns true if the string represents a valid number.
     * 检查字符串是否为数字
     *
     * @param str the string | 字符串
     * @return true if it is a number | 如果是数字返回 true
     */
    private static final int MAX_NUMBER_STRING_LENGTH = 10_000;

    public static boolean isNumber(String str) {
        if (str == null || str.isEmpty() || str.length() > MAX_NUMBER_STRING_LENGTH) {
            return false;
        }
        try {
            new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns true if the string represents a valid integer.
     * 检查字符串是否为整数
     *
     * @param str the string | 字符串
     * @return true if it is an integer | 如果是整数返回 true
     */
    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        int start = 0;
        if (str.charAt(0) == '-' || str.charAt(0) == '+') {
            if (str.length() == 1) {
                return false;
            }
            start = 1;
        }
        for (int i = start; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the string can be parsed as a long value.
     * 检查字符串是否为 long 类型整数
     *
     * @param str the string | 字符串
     * @return true if parsable as long | 如果可解析为 long 返回 true
     */
    public static boolean isLong(String str) {
        if (!isInteger(str)) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns true if the string can be parsed as a double value.
     * 检查字符串是否为 double 类型浮点数
     *
     * @param str the string | 字符串
     * @return true if parsable as double | 如果可解析为 double 返回 true
     */
    public static boolean isDouble(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return !str.contains("Infinity") && !str.contains("NaN");
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns true if the string can be created as a Number (including hex and octal).
     * 检查字符串是否可以创建为 Number
     *
     * @param str the string | 字符串
     * @return true if creatable as Number | 如果可以创建为 Number 返回 true
     */
    public static boolean isCreatable(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // 检查十六进制
        if (str.startsWith("0x") || str.startsWith("0X") ||
                str.startsWith("-0x") || str.startsWith("-0X")) {
            return isHexNumber(str);
        }
        // 检查八进制
        if (str.startsWith("0") && str.length() > 1 &&
                Character.isDigit(str.charAt(1))) {
            return isOctalNumber(str);
        }
        return isNumber(str);
    }

    /**
     * Returns true if the string can be parsed as a decimal number (no hex/octal).
     * 检查字符串是否可解析为数字
     *
     * @param str the string | 字符串
     * @return true if parsable | 如果可以解析返回 true
     */
    public static boolean isParsable(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        if (str.charAt(str.length() - 1) == '.') {
            return false;
        }
        if (str.charAt(0) == '-') {
            if (str.length() == 1) {
                return false;
            }
            return withDecimalsParsing(str, 1);
        }
        return withDecimalsParsing(str, 0);
    }

    private static boolean withDecimalsParsing(String str, int beginIdx) {
        int decimalPoints = 0;
        for (int i = beginIdx; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '.') {
                decimalPoints++;
                if (decimalPoints > 1) {
                    return false;
                }
            } else if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexNumber(String str) {
        int start = str.startsWith("-") ? 3 : 2;
        if (str.length() <= start) {
            return false;
        }
        for (int i = start; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (!((ch >= '0' && ch <= '9') ||
                    (ch >= 'a' && ch <= 'f') ||
                    (ch >= 'A' && ch <= 'F'))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isOctalNumber(String str) {
        int start = str.startsWith("-") ? 2 : 1;
        for (int i = start; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch < '0' || ch > '7') {
                return false;
            }
        }
        return true;
    }

    // ==================== 解析（带默认值） ====================

    /**
     * Parses the string as an int, returning the default value on failure.
     * 解析字符串为 int
     *
     * @param str          the string | 字符串
     * @param defaultValue default value | 默认值
     * @return parsed value or default | 解析结果或默认值
     */
    public static int toInt(String str, int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses the string as a long, returning the default value on failure.
     * 解析字符串为 long
     *
     * @param str          the string | 字符串
     * @param defaultValue default value | 默认值
     * @return parsed value or default | 解析结果或默认值
     */
    public static long toLong(String str, long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses the string as a float, returning the default value on failure.
     * 解析字符串为 float
     *
     * @param str          the string | 字符串
     * @param defaultValue default value | 默认值
     * @return parsed value or default | 解析结果或默认值
     */
    public static float toFloat(String str, float defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses the string as a double, returning the default value on failure.
     * 解析字符串为 double
     *
     * @param str          the string | 字符串
     * @param defaultValue default value | 默认值
     * @return parsed value or default | 解析结果或默认值
     */
    public static double toDouble(String str, double defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses the string as a BigDecimal, returning null on failure.
     * 解析字符串为 BigDecimal
     *
     * @param str the string | 字符串
     * @return BigDecimal, or null if parsing fails | BigDecimal，如果解析失败返回 null
     */
    public static BigDecimal toBigDecimal(String str) {
        return toBigDecimal(str, null);
    }

    /**
     * Parses the string as a BigDecimal, returning the default value on failure.
     * 解析字符串为 BigDecimal
     *
     * @param str          the string | 字符串
     * @param defaultValue default value | 默认值
     * @return BigDecimal or default | BigDecimal 或默认值
     */
    public static BigDecimal toBigDecimal(String str, BigDecimal defaultValue) {
        if (str == null || str.isEmpty()) {
            return defaultValue;
        }
        String trimmed = str.trim();
        if (trimmed.length() > MAX_NUMBER_STRING_LENGTH) {
            return defaultValue;
        }
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses the string as a BigInteger, returning null on failure.
     * 解析字符串为 BigInteger
     *
     * @param str the string | 字符串
     * @return BigInteger, or null if parsing fails | BigInteger，如果解析失败返回 null
     */
    public static BigInteger toBigInteger(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        String trimmed = str.trim();
        if (trimmed.length() > MAX_NUMBER_STRING_LENGTH) {
            return null;
        }
        try {
            return new BigInteger(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== 安全解析（返回 Optional） ====================

    /**
     * Tries to parse the string as an int, returning OptionalInt.empty() on failure.
     * 尝试解析为 int
     *
     * @param str the string | 字符串
     * @return OptionalInt | OptionalInt
     */
    public static OptionalInt tryParseInt(String str) {
        if (str == null) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(str.trim()));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    /**
     * Tries to parse the string as a long, returning OptionalLong.empty() on failure.
     * 尝试解析为 long
     *
     * @param str the string | 字符串
     * @return OptionalLong | OptionalLong
     */
    public static OptionalLong tryParseLong(String str) {
        if (str == null) {
            return OptionalLong.empty();
        }
        try {
            return OptionalLong.of(Long.parseLong(str.trim()));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }

    /**
     * Tries to parse the string as a double, returning OptionalDouble.empty() on failure.
     * 尝试解析为 double
     *
     * @param str the string | 字符串
     * @return OptionalDouble | OptionalDouble
     */
    public static OptionalDouble tryParseDouble(String str) {
        if (str == null) {
            return OptionalDouble.empty();
        }
        try {
            return OptionalDouble.of(Double.parseDouble(str.trim()));
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    // ==================== 溢出安全转换 ====================

    /**
     * Converts a long to an int, clamping to Integer.MAX_VALUE or Integer.MIN_VALUE on overflow.
     * 将 long 转为 int，溢出时截断到 int 范围
     *
     * @param value long value | long 值
     * @return int value, or Integer.MAX_VALUE / Integer.MIN_VALUE on overflow | int 值，溢出时返回 Integer.MAX_VALUE 或 Integer.MIN_VALUE
     */
    public static int saturatedCast(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    /**
     * Converts a long to an int, throwing ArithmeticException on overflow.
     * 将 long 转为 int，溢出时抛出异常
     *
     * @param value long value | long 值
     * @return int value | int 值
     * @throws ArithmeticException if overflow | 如果溢出
     */
    public static int checkedCast(long value) {
        int result = (int) value;
        if (result != value) {
            throw new ArithmeticException("Out of range: " + value);
        }
        return result;
    }

    /**
     * Converts a BigDecimal to an int, clamping on overflow.
     * 将 BigDecimal 转为 int，溢出时截断
     *
     * @param value BigDecimal value | BigDecimal 值
     * @return int value | int 值
     */
    public static int saturatedCast(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        if (value.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
            return Integer.MAX_VALUE;
        }
        if (value.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) < 0) {
            return Integer.MIN_VALUE;
        }
        return value.intValue();
    }

    // ==================== 比较 ====================

    /**
     * Compares two int values.
     * 比较两个 int 值
     *
     * @param x first value | 值1
     * @param y second value | 值2
     * @return comparison result | 比较结果
     */
    public static int compare(int x, int y) {
        return Integer.compare(x, y);
    }

    /**
     * Compares two long values.
     * 比较两个 long 值
     *
     * @param x first value | 值1
     * @param y second value | 值2
     * @return comparison result | 比较结果
     */
    public static int compare(long x, long y) {
        return Long.compare(x, y);
    }

    /**
     * Compares two double values.
     * 比较两个 double 值
     *
     * @param x first value | 值1
     * @param y second value | 值2
     * @return comparison result | 比较结果
     */
    public static int compare(double x, double y) {
        return Double.compare(x, y);
    }

    /**
     * Returns the larger of two Comparable values.
     * 返回两个值中的较大值
     *
     * @param a   first value | 值1
     * @param b   second value | 值2
     * @param <T> value type | 值类型
     * @return the larger value | 较大值
     */
    public static <T extends Comparable<T>> T max(T a, T b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) >= 0 ? a : b;
    }

    /**
     * Returns the smaller of two Comparable values.
     * 返回两个值中的较小值
     *
     * @param a   first value | 值1
     * @param b   second value | 值2
     * @param <T> value type | 值类型
     * @return the smaller value | 较小值
     */
    public static <T extends Comparable<T>> T min(T a, T b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) <= 0 ? a : b;
    }

    /**
     * Returns the maximum value in the int array.
     * 返回数组中的最大值
     *
     * @param array int array | int 数组
     * @return maximum value | 最大值
     * @throws IllegalArgumentException if array is empty | 如果数组为空
     */
    public static int max(int... array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty");
        }
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * Returns the minimum value in the int array.
     * 返回数组中的最小值
     *
     * @param array int array | int 数组
     * @return minimum value | 最小值
     * @throws IllegalArgumentException if array is empty | 如果数组为空
     */
    public static int min(int... array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty");
        }
        int min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    /**
     * Returns the maximum value in the long array.
     * 返回 long 数组中的最大值
     */
    public static long max(long... array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty");
        }
        long max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * Returns the minimum value in the long array.
     * 返回 long 数组中的最小值
     */
    public static long min(long... array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty");
        }
        long min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    // ==================== 范围控制 ====================

    /**
     * Clamps the value to the range [min, max].
     * 限制值在指定范围内
     *
     * @param value the value | 值
     * @param min   minimum value | 最小值
     * @param max   maximum value | 最大值
     * @return value clamped to range | 范围内的值
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps the long value to the range [min, max].
     * 限制值在指定范围内
     */
    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps the double value to the range [min, max].
     * 限制值在指定范围内
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Constrains the value to the range [min, max] (alias for clamp).
     * 限制值在指定范围内（Guava 风格别名）
     */
    public static int constrainToRange(int value, int min, int max) {
        return clamp(value, min, max);
    }

    /**
     * Returns true if the value is within the range [min, max].
     * 检查值是否在范围内 [min, max]
     *
     * @param value the value | 值
     * @param min   minimum value | 最小值
     * @param max   maximum value | 最大值
     * @return true if in range | 如果在范围内返回 true
     */
    public static boolean inRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Returns true if the long value is within the range [min, max].
     * 检查值是否在范围内 [min, max]
     */
    public static boolean inRange(long value, long min, long max) {
        return value >= min && value <= max;
    }

    // ==================== 高精度运算 ====================

    /**
     * High-precision addition of multiple Number values.
     * 高精度加法
     *
     * @param values number values | 数值数组
     * @return sum | 和
     */
    public static BigDecimal add(Number... values) {
        if (values == null || values.length == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = BigDecimal.ZERO;
        for (Number value : values) {
            if (value != null) {
                result = result.add(toBigDecimal(value));
            }
        }
        return result;
    }

    /**
     * High-precision subtraction.
     * 高精度减法
     */
    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null) b = BigDecimal.ZERO;
        return a.subtract(b);
    }

    /**
     * High-precision multiplication.
     * 高精度乘法
     */
    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return BigDecimal.ZERO;
        }
        return a.multiply(b);
    }

    /**
     * High-precision division with half-up rounding.
     * 高精度除法
     *
     * @param a     dividend | 被除数
     * @param b     divisor | 除数
     * @param scale number of decimal places | 小数位数
     * @return quotient | 商
     */
    public static BigDecimal divide(BigDecimal a, BigDecimal b, int scale) {
        return divide(a, b, scale, RoundingMode.HALF_UP);
    }

    /**
     * High-precision division with specified rounding mode.
     * 高精度除法
     *
     * @param a     dividend | 被除数
     * @param b     divisor | 除数
     * @param scale number of decimal places | 小数位数
     * @param mode  rounding mode | 舍入模式
     * @return quotient | 商
     */
    public static BigDecimal divide(BigDecimal a, BigDecimal b, int scale, RoundingMode mode) {
        if (a == null) {
            return BigDecimal.ZERO;
        }
        if (b == null || b.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a.divide(b, scale, mode);
    }

    private static BigDecimal toBigDecimal(Number number) {
        if (number == null) {
            return BigDecimal.ZERO;
        }
        if (number instanceof BigDecimal bd) {
            return bd;
        }
        if (number instanceof BigInteger bi) {
            return new BigDecimal(bi);
        }
        if (number instanceof Long || number instanceof Integer ||
                number instanceof Short || number instanceof Byte) {
            return BigDecimal.valueOf(number.longValue());
        }
        if (number instanceof Float) {
            return new BigDecimal(number.toString());
        }
        return BigDecimal.valueOf(number.doubleValue());
    }

    // ==================== 四舍五入 ====================

    /**
     * Rounds the BigDecimal value to the specified scale using half-up rounding.
     * 四舍五入
     *
     * @param value BigDecimal value | BigDecimal 值
     * @param scale number of decimal places | 小数位数
     * @return rounded value | 四舍五入后的值
     */
    public static BigDecimal round(BigDecimal value, int scale) {
        return round(value, scale, RoundingMode.HALF_UP);
    }

    /**
     * Rounds the BigDecimal value to the specified scale with the given rounding mode.
     * 四舍五入
     *
     * @param value BigDecimal value | BigDecimal 值
     * @param scale number of decimal places | 小数位数
     * @param mode  rounding mode | 舍入模式
     * @return rounded value | 舍入后的值
     */
    public static BigDecimal round(BigDecimal value, int scale, RoundingMode mode) {
        if (value == null) {
            return null;
        }
        return value.setScale(scale, mode);
    }

    /**
     * Rounds the double value to the specified number of decimal places.
     * 四舍五入
     *
     * @param value double value | double 值
     * @param scale number of decimal places | 小数位数
     * @return rounded value | 四舍五入后的值
     */
    public static double round(double value, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("Scale must not be negative");
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Rounds the BigDecimal value using banker's rounding (HALF_EVEN).
     * 银行家舍入（四舍六入五成双）
     *
     * @param value BigDecimal value | BigDecimal 值
     * @param scale number of decimal places | 小数位数
     * @return rounded value | 舍入后的值
     */
    public static BigDecimal roundHalfEven(BigDecimal value, int scale) {
        return round(value, scale, RoundingMode.HALF_EVEN);
    }

    // ==================== 格式化 ====================

    /**
     * Formats a double value using the given pattern.
     * 格式化数值
     *
     * @param value   double value | double 值
     * @param pattern format pattern | 格式模式
     * @return formatted string | 格式化后的字符串
     */
    public static String format(double value, String pattern) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * Formats a BigDecimal value using the given pattern.
     * 格式化数值
     *
     * @param value   BigDecimal value | BigDecimal 值
     * @param pattern format pattern | 格式模式
     * @return formatted string | 格式化后的字符串
     */
    public static String format(BigDecimal value, String pattern) {
        if (value == null) {
            return "";
        }
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * Formats a double value as a percentage string with the given number of decimal places.
     * 格式化为百分比
     *
     * @param value double value | double 值
     * @param scale number of decimal places | 小数位数
     * @return percentage string | 百分比字符串
     */
    public static String formatPercent(double value, int scale) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMinimumFractionDigits(scale);
        percentFormat.setMaximumFractionDigits(scale);
        return percentFormat.format(value);
    }

    /**
     * Formats a BigDecimal value as a currency string using the JVM's default Locale.
     * 使用 JVM 默认 Locale 将 BigDecimal 值格式化为货币字符串。
     *
     * <p><b>API Note:</b> The output depends on the JVM's default Locale ({@link java.util.Locale#getDefault()}).
     * Results may differ across servers with different Locale settings.
     * For explicit Locale control, use {@link java.text.NumberFormat#getCurrencyInstance(java.util.Locale)}.
     * 输出取决于 JVM 默认 Locale，不同服务器上的结果可能不同。
     * 如需显式指定 Locale，请使用 {@link java.text.NumberFormat#getCurrencyInstance(java.util.Locale)}。</p>
     *
     * @param value BigDecimal value | BigDecimal 值
     * @return currency string | 货币字符串
     */
    public static String formatMoney(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return NumberFormat.getCurrencyInstance().format(value);
    }
}
