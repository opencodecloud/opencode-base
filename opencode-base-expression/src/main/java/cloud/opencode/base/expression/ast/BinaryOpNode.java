package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.eval.TypeCoercion;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Binary Operation Node
 * 二元运算节点
 *
 * <p>Represents binary operations: arithmetic, comparison, logical.</p>
 * <p>表示二元运算：算术、比较、逻辑。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Arithmetic: +, -, *, /, %, ** - 算术运算</li>
 *   <li>Comparison: ==, !=, &gt;, &gt;=, &lt;, &lt;= - 比较运算</li>
 *   <li>Logical: &amp;&amp;, || with short-circuit evaluation - 逻辑运算，支持短路求值</li>
 *   <li>Pattern matching: matches, instanceof - 模式匹配</li>
 *   <li>String concatenation via + operator - 通过+运算符进行字符串连接</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node left = LiteralNode.of(10);
 * Node right = LiteralNode.of(3);
 * Node add = BinaryOpNode.of(left, "+", right);
 * Object result = add.evaluate(ctx);  // 13
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: No, null operands require non-null - 空值安全: 否，操作数要求非空</li>
 *   <li>ReDoS protection: regex matching has timeout and length limits - ReDoS防护: 正则匹配有超时和长度限制</li>
 * </ul>
 *
 * @param operator the operator | 运算符
 * @param left the left operand | 左操作数
 * @param right the right operand | 右操作数
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record BinaryOpNode(String operator, Node left, Node right) implements Node {

    public BinaryOpNode {
        Objects.requireNonNull(operator, "operator cannot be null");
        Objects.requireNonNull(left, "left operand cannot be null");
        Objects.requireNonNull(right, "right operand cannot be null");
    }

    /**
     * Create binary operation node
     * 创建二元运算节点
     *
     * @param left the left operand | 左操作数
     * @param operator the operator string | 运算符字符串
     * @param right the right operand | 右操作数
     * @return the binary operation node | 二元运算节点
     */
    public static BinaryOpNode of(Node left, String operator, Node right) {
        return new BinaryOpNode(operator, left, right);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        // Short-circuit evaluation for logical operators
        if ("&&".equals(operator)) {
            Object leftVal = left.evaluate(context);
            if (!TypeCoercion.toBoolean(leftVal)) {
                return false;
            }
            return TypeCoercion.toBoolean(right.evaluate(context));
        }

        if ("||".equals(operator)) {
            Object leftVal = left.evaluate(context);
            if (TypeCoercion.toBoolean(leftVal)) {
                return true;
            }
            return TypeCoercion.toBoolean(right.evaluate(context));
        }

        Object leftVal = left.evaluate(context);
        Object rightVal = right.evaluate(context);

        return switch (operator) {
            // Arithmetic
            case "+" -> add(leftVal, rightVal);
            case "-" -> subtract(leftVal, rightVal);
            case "*" -> multiply(leftVal, rightVal);
            case "/" -> divide(leftVal, rightVal);
            case "%" -> modulo(leftVal, rightVal);
            case "**" -> power(leftVal, rightVal);

            // Comparison
            case "==" -> equals(leftVal, rightVal);
            case "!=" -> !equals(leftVal, rightVal);
            case ">" -> compare(leftVal, rightVal) > 0;
            case ">=" -> compare(leftVal, rightVal) >= 0;
            case "<" -> compare(leftVal, rightVal) < 0;
            case "<=" -> compare(leftVal, rightVal) <= 0;

            // Pattern matching
            case "matches" -> matches(leftVal, rightVal);
            case "instanceof" -> instanceOf(leftVal, rightVal);

            default -> throw OpenExpressionException.evaluationError("Unknown operator: " + operator);
        };
    }

    private Object add(Object left, Object right) {
        // String concatenation
        if (left instanceof String || right instanceof String) {
            return String.valueOf(left) + String.valueOf(right);
        }

        // Numeric addition
        if (left instanceof Number l && right instanceof Number r) {
            if (l instanceof Double || r instanceof Double) {
                return l.doubleValue() + r.doubleValue();
            }
            if (l instanceof Long || r instanceof Long) {
                return Math.addExact(l.longValue(), r.longValue());
            }
            return Math.addExact(l.intValue(), r.intValue());
        }

        throw OpenExpressionException.typeError("number or string", left);
    }

    private Object subtract(Object left, Object right) {
        if (left instanceof Number l && right instanceof Number r) {
            if (l instanceof Double || r instanceof Double) {
                return l.doubleValue() - r.doubleValue();
            }
            if (l instanceof Long || r instanceof Long) {
                return Math.subtractExact(l.longValue(), r.longValue());
            }
            return Math.subtractExact(l.intValue(), r.intValue());
        }
        throw OpenExpressionException.typeError("number", left);
    }

    private Object multiply(Object left, Object right) {
        if (left instanceof Number l && right instanceof Number r) {
            if (l instanceof Double || r instanceof Double) {
                return l.doubleValue() * r.doubleValue();
            }
            if (l instanceof Long || r instanceof Long) {
                return Math.multiplyExact(l.longValue(), r.longValue());
            }
            return Math.multiplyExact(l.intValue(), r.intValue());
        }
        throw OpenExpressionException.typeError("number", left);
    }

    private Object divide(Object left, Object right) {
        if (left instanceof Number l && right instanceof Number r) {
            if (r.doubleValue() == 0) {
                throw OpenExpressionException.divisionByZero();
            }
            if (l instanceof Double || r instanceof Double) {
                return l.doubleValue() / r.doubleValue();
            }
            if (l instanceof Long || r instanceof Long) {
                return l.longValue() / r.longValue();
            }
            return l.intValue() / r.intValue();
        }
        throw OpenExpressionException.typeError("number", left);
    }

    private Object modulo(Object left, Object right) {
        if (left instanceof Number l && right instanceof Number r) {
            if (r.doubleValue() == 0) {
                throw OpenExpressionException.divisionByZero();
            }
            if (l instanceof Double || r instanceof Double) {
                return l.doubleValue() % r.doubleValue();
            }
            if (l instanceof Long || r instanceof Long) {
                return l.longValue() % r.longValue();
            }
            return l.intValue() % r.intValue();
        }
        throw OpenExpressionException.typeError("number", left);
    }

    private Object power(Object left, Object right) {
        if (left instanceof Number l && right instanceof Number r) {
            return Math.pow(l.doubleValue(), r.doubleValue());
        }
        throw OpenExpressionException.typeError("number", left);
    }

    private boolean equals(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }

        // Numeric comparison
        if (left instanceof Number l && right instanceof Number r) {
            return l.doubleValue() == r.doubleValue();
        }

        return Objects.equals(left, right);
    }

    @SuppressWarnings("unchecked")
    private int compare(Object left, Object right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }

        // Numeric comparison
        if (left instanceof Number l && right instanceof Number r) {
            return Double.compare(l.doubleValue(), r.doubleValue());
        }

        // Comparable comparison
        if (left instanceof Comparable && left.getClass().isInstance(right)) {
            return ((Comparable<Object>) left).compareTo(right);
        }

        throw OpenExpressionException.typeError("comparable", left);
    }

    /**
     * Maximum allowed regex pattern length to prevent ReDoS.
     * 允许的最大正则表达式模式长度，以防止 ReDoS。
     */
    private static final int MAX_PATTERN_LENGTH = 1000;

    /**
     * Timeout for regex matching in seconds.
     * 正则匹配超时时间（秒）。
     */
    private static final long REGEX_TIMEOUT_SECONDS = 5;

    private boolean matches(Object left, Object right) {
        if (left == null) {
            return false;
        }
        String str = String.valueOf(left);
        String pattern = String.valueOf(right);
        if (pattern.length() > MAX_PATTERN_LENGTH) {
            throw OpenExpressionException.evaluationError(
                "Regex pattern exceeds maximum length of " + MAX_PATTERN_LENGTH + " characters");
        }
        try {
            return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                try {
                    return Pattern.matches(pattern, str);
                } catch (StackOverflowError e) {
                    throw OpenExpressionException.evaluationError(
                        "Regex pattern caused stack overflow (possible ReDoS)", e);
                }
            }).get(REGEX_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            throw OpenExpressionException.timeout(REGEX_TIMEOUT_SECONDS * 1000);
        } catch (java.util.concurrent.ExecutionException e) {
            if (e.getCause() instanceof OpenExpressionException oee) {
                throw oee;
            }
            throw OpenExpressionException.evaluationError(
                "Regex matching failed: " + e.getCause().getMessage(), e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenExpressionException.evaluationError("Regex matching was interrupted", e);
        }
    }

    private boolean instanceOf(Object left, Object right) {
        if (left == null) {
            return false;
        }
        if (right instanceof Class<?> clazz) {
            return clazz.isInstance(left);
        }
        if (right instanceof String typeName) {
            return switch (typeName.toLowerCase()) {
                case "string" -> left instanceof String;
                case "number" -> left instanceof Number;
                case "integer", "int" -> left instanceof Integer;
                case "long" -> left instanceof Long;
                case "double" -> left instanceof Double;
                case "boolean" -> left instanceof Boolean;
                case "list" -> left instanceof java.util.List;
                case "map" -> left instanceof java.util.Map;
                case "collection" -> left instanceof java.util.Collection;
                default -> false;
            };
        }
        return false;
    }

    @Override
    public String toExpressionString() {
        return "(" + left.toExpressionString() + " " + operator + " " + right.toExpressionString() + ")";
    }
}
