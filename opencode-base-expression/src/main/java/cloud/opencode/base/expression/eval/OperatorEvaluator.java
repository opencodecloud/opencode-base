package cloud.opencode.base.expression.eval;

import cloud.opencode.base.expression.OpenExpressionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * Operator Evaluator
 * 运算符求值器
 *
 * <p>Evaluates binary and unary operators on operand values.</p>
 * <p>对操作数值求值二元和一元运算符。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Binary operators: +, -, *, /, %, **, ==, !=, &lt;, &lt;=, &gt;, &gt;=, &amp;&amp;, ||, matches - 二元运算符</li>
 *   <li>Unary operators: -, !, + - 一元运算符</li>
 *   <li>Overflow-safe integer arithmetic with Math.*Exact - 溢出安全的整数运算</li>
 *   <li>Automatic type widening (int to long to double) - 自动类型拓宽</li>
 *   <li>String-to-number coercion - 字符串到数字的强制转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Object sum = OperatorEvaluator.evaluateBinary("+", 10, 20);  // 30
 * Object neg = OperatorEvaluator.evaluateUnary("-", 5);  // -5
 * boolean eq = OperatorEvaluator.equals(1, 1.0);  // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, stateless utility class - 线程安全: 是，无状态工具类</li>
 *   <li>Null-safe: Partially, null treated as 0/false/empty string - 空值安全: 部分，null视为0/false/空字符串</li>
 *   <li>ReDoS protection: regex matching has timeout and length limits - ReDoS防护: 正则匹配有超时和长度限制</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per arithmetic, comparison, and logical operation; O(m*n) for regex matches where m is pattern complexity and n is input length - 时间复杂度: 算术、比较、逻辑操作均为 O(1)；正则匹配为 O(m*n)，m为模式复杂度，n为输入长度</li>
 *   <li>Space complexity: O(1) per operation - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public final class OperatorEvaluator {

    private OperatorEvaluator() {
    }

    // ==================== Binary Operators ====================

    /**
     * Evaluate binary operator
     * 求值二元运算符
     *
     * @param operator the operator | 运算符
     * @param left the left operand | 左操作数
     * @param right the right operand | 右操作数
     * @return the result | 结果
     */
    public static Object evaluateBinary(String operator, Object left, Object right) {
        return switch (operator) {
            case "+" -> add(left, right);
            case "-" -> subtract(left, right);
            case "*" -> multiply(left, right);
            case "/" -> divide(left, right);
            case "%" -> modulo(left, right);
            case "**" -> power(left, right);
            case "==" -> equals(left, right);
            case "!=" -> notEquals(left, right);
            case "<" -> lessThan(left, right);
            case "<=" -> lessThanOrEqual(left, right);
            case ">" -> greaterThan(left, right);
            case ">=" -> greaterThanOrEqual(left, right);
            case "&&", "and" -> and(left, right);
            case "||", "or" -> or(left, right);
            case "matches" -> matches(left, right);
            default -> throw OpenExpressionException.evaluationError("Unknown operator: " + operator);
        };
    }

    /**
     * Evaluate unary operator
     * 求值一元运算符
     *
     * @param operator the operator | 运算符
     * @param operand the operand | 操作数
     * @return the result | 结果
     */
    public static Object evaluateUnary(String operator, Object operand) {
        return switch (operator) {
            case "!" , "not" -> not(operand);
            case "-" -> negate(operand);
            case "+" -> operand instanceof Number ? operand : toNumber(operand);
            default -> throw OpenExpressionException.evaluationError("Unknown unary operator: " + operator);
        };
    }

    // ==================== Arithmetic Operations ====================

    /**
     * Add two values
     * 加法
     *
     * @param left left operand | 左操作数
     * @param right right operand | 右操作数
     * @return result | 结果
     */
    public static Object add(Object left, Object right) {
        // String concatenation
        if (left instanceof String || right instanceof String) {
            return toString(left) + toString(right);
        }

        // Numeric addition
        if (isNumber(left) && isNumber(right)) {
            return addNumbers(toNumber(left), toNumber(right));
        }

        // Default to string concatenation
        return toString(left) + toString(right);
    }

    /**
     * Subtract two values
     * 减法
     */
    public static Object subtract(Object left, Object right) {
        return subtractNumbers(toNumber(left), toNumber(right));
    }

    /**
     * Multiply two values
     * 乘法
     */
    public static Object multiply(Object left, Object right) {
        return multiplyNumbers(toNumber(left), toNumber(right));
    }

    /**
     * Divide two values
     * 除法
     */
    public static Object divide(Object left, Object right) {
        Number rightNum = toNumber(right);
        if (rightNum.doubleValue() == 0) {
            throw OpenExpressionException.divisionByZero();
        }
        return divideNumbers(toNumber(left), rightNum);
    }

    /**
     * Modulo two values
     * 取模
     */
    public static Object modulo(Object left, Object right) {
        Number rightNum = toNumber(right);
        if (rightNum.doubleValue() == 0) {
            throw OpenExpressionException.divisionByZero();
        }
        return moduloNumbers(toNumber(left), rightNum);
    }

    /**
     * Power operation
     * 幂运算
     */
    public static Object power(Object left, Object right) {
        return Math.pow(toNumber(left).doubleValue(), toNumber(right).doubleValue());
    }

    // ==================== Comparison Operations ====================

    /**
     * Equals comparison
     * 相等比较
     */
    public static boolean equals(Object left, Object right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (left.equals(right)) {
            return true;
        }
        // Numeric comparison
        if (isNumber(left) && isNumber(right)) {
            return toNumber(left).doubleValue() == toNumber(right).doubleValue();
        }
        return false;
    }

    /**
     * Not equals comparison
     * 不等比较
     */
    public static boolean notEquals(Object left, Object right) {
        return !equals(left, right);
    }

    /**
     * Less than comparison
     * 小于比较
     */
    @SuppressWarnings("unchecked")
    public static boolean lessThan(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        if (isNumber(left) && isNumber(right)) {
            return toNumber(left).doubleValue() < toNumber(right).doubleValue();
        }
        if (left instanceof Comparable c) {
            return c.compareTo(right) < 0;
        }
        return false;
    }

    /**
     * Less than or equal comparison
     * 小于等于比较
     */
    @SuppressWarnings("unchecked")
    public static boolean lessThanOrEqual(Object left, Object right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        if (isNumber(left) && isNumber(right)) {
            return toNumber(left).doubleValue() <= toNumber(right).doubleValue();
        }
        if (left instanceof Comparable c) {
            return c.compareTo(right) <= 0;
        }
        return false;
    }

    /**
     * Greater than comparison
     * 大于比较
     */
    @SuppressWarnings("unchecked")
    public static boolean greaterThan(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        if (isNumber(left) && isNumber(right)) {
            return toNumber(left).doubleValue() > toNumber(right).doubleValue();
        }
        if (left instanceof Comparable c) {
            return c.compareTo(right) > 0;
        }
        return false;
    }

    /**
     * Greater than or equal comparison
     * 大于等于比较
     */
    @SuppressWarnings("unchecked")
    public static boolean greaterThanOrEqual(Object left, Object right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        if (isNumber(left) && isNumber(right)) {
            return toNumber(left).doubleValue() >= toNumber(right).doubleValue();
        }
        if (left instanceof Comparable c) {
            return c.compareTo(right) >= 0;
        }
        return false;
    }

    // ==================== Logical Operations ====================

    /**
     * Logical AND
     * 逻辑与
     */
    public static boolean and(Object left, Object right) {
        return toBoolean(left) && toBoolean(right);
    }

    /**
     * Logical OR
     * 逻辑或
     */
    public static boolean or(Object left, Object right) {
        return toBoolean(left) || toBoolean(right);
    }

    /**
     * Logical NOT
     * 逻辑非
     */
    public static boolean not(Object operand) {
        return !toBoolean(operand);
    }

    // ==================== Other Operations ====================

    /**
     * Negate number
     * 取负
     */
    public static Object negate(Object operand) {
        Number n = toNumber(operand);
        if (n instanceof Integer i) {
            return Math.negateExact(i);
        }
        if (n instanceof Long l) {
            return Math.negateExact(l);
        }
        if (n instanceof Float f) {
            return -f;
        }
        return -n.doubleValue();
    }

    /**
     * Maximum allowed regex pattern length to prevent ReDoS.
     * 允许的最大正则表达式模式长度，以防止 ReDoS。
     */
    private static final int MAX_PATTERN_LENGTH = 1000;
    private static final int MAX_PATTERN_CACHE_SIZE = 256;
    private static final java.util.concurrent.ConcurrentHashMap<String, Pattern> PATTERN_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Timeout for regex matching in seconds.
     * 正则匹配超时时间（秒）。
     */
    private static final long REGEX_TIMEOUT_SECONDS = 5;

    /**
     * Regex matches
     * 正则匹配
     */
    public static boolean matches(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        String pattern = right.toString();
        if (pattern.length() > MAX_PATTERN_LENGTH) {
            throw OpenExpressionException.evaluationError(
                "Regex pattern exceeds maximum length of " + MAX_PATTERN_LENGTH + " characters");
        }
        String input = left.toString();
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Evict cache outside computeIfAbsent to avoid ConcurrentHashMap contract violation
                    if (PATTERN_CACHE.size() >= MAX_PATTERN_CACHE_SIZE) {
                        PATTERN_CACHE.clear();
                    }
                    Pattern compiled = PATTERN_CACHE.computeIfAbsent(pattern, Pattern::compile);
                    return compiled.matcher(input).matches();
                } catch (StackOverflowError e) {
                    throw OpenExpressionException.evaluationError("Regex pattern caused stack overflow (possible ReDoS)", e);
                }
            }).get(REGEX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw OpenExpressionException.timeout(REGEX_TIMEOUT_SECONDS * 1000);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof OpenExpressionException oee) {
                throw oee;
            }
            throw OpenExpressionException.evaluationError("Regex matching failed: " + e.getCause().getMessage(), e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenExpressionException.evaluationError("Regex matching was interrupted", e);
        }
    }

    // ==================== Helper Methods ====================

    private static Number addNumbers(Number left, Number right) {
        if (isInteger(left) && isInteger(right)) {
            return Math.addExact(left.longValue(), right.longValue());
        }
        return left.doubleValue() + right.doubleValue();
    }

    private static Number subtractNumbers(Number left, Number right) {
        if (isInteger(left) && isInteger(right)) {
            return Math.subtractExact(left.longValue(), right.longValue());
        }
        return left.doubleValue() - right.doubleValue();
    }

    private static Number multiplyNumbers(Number left, Number right) {
        if (isInteger(left) && isInteger(right)) {
            return Math.multiplyExact(left.longValue(), right.longValue());
        }
        return left.doubleValue() * right.doubleValue();
    }

    private static Number divideNumbers(Number left, Number right) {
        if (isInteger(left) && isInteger(right) && left.longValue() % right.longValue() == 0) {
            return left.longValue() / right.longValue();
        }
        return left.doubleValue() / right.doubleValue();
    }

    private static Number moduloNumbers(Number left, Number right) {
        if (isInteger(left) && isInteger(right)) {
            return left.longValue() % right.longValue();
        }
        return left.doubleValue() % right.doubleValue();
    }

    private static boolean isNumber(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Number) {
            return true;
        }
        if (value instanceof String s) {
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private static boolean isInteger(Number n) {
        return n instanceof Integer || n instanceof Long ||
               n instanceof Short || n instanceof Byte;
    }

    private static Number toNumber(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number n) {
            return n;
        }
        if (value instanceof String s) {
            try {
                if (s.contains(".")) {
                    return Double.parseDouble(s);
                }
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw OpenExpressionException.typeError("number", value);
            }
        }
        if (value instanceof Boolean b) {
            return b ? 1 : 0;
        }
        throw OpenExpressionException.typeError("number", value);
    }

    private static boolean toBoolean(Object value) {
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
            return !s.isEmpty() && !s.equalsIgnoreCase("false");
        }
        return true;
    }

    private static String toString(Object value) {
        return value == null ? "" : value.toString();
    }
}
