package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Expression-based Enable Strategy with Optional Expression Module Delegation
 * 支持可选表达式模块委托的基于表达式的启用策略
 *
 * <p>Evaluates feature enablement using dynamic expressions.
 * If the Expression module (opencode-base-expression) is available, it delegates to OpenExpression
 * for powerful expression evaluation. Otherwise, falls back to simple constant evaluation.</p>
 * <p>使用动态表达式评估功能启用状态。
 * 如果表达式模块可用，则委托给 OpenExpression 进行强大的表达式求值。
 * 否则降级到简单的常量评估。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple comparison
 * EnableStrategy strategy = ExpressionStrategy.of("age >= 18");
 *
 * // Complex boolean expression
 * EnableStrategy strategy = ExpressionStrategy.of(
 *     "userType == 'premium' || (userType == 'standard' && age >= 21)"
 * );
 *
 * // With context attributes
 * FeatureContext ctx = FeatureContext.builder()
 *     .attribute("userType", "premium")
 *     .attribute("age", 25)
 *     .build();
 * boolean enabled = strategy.isEnabled(feature, ctx);
 * }</pre>
 *
 * <p><strong>Available Variables | 可用变量:</strong></p>
 * <ul>
 *   <li>{@code userId} - User ID from context | 来自上下文的用户ID</li>
 *   <li>{@code tenantId} - Tenant ID from context | 来自上下文的租户ID</li>
 *   <li>{@code featureKey} - Feature key | 功能键</li>
 *   <li>{@code defaultEnabled} - Default enabled state | 默认启用状态</li>
 *   <li>All context attributes | 所有上下文属性</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Expression-based feature evaluation with configurable operators - 基于表达式的功能评估和可配置运算符</li>
 *   <li>Context attribute matching for dynamic feature decisions - 上下文属性匹配实现动态功能决策</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public final class ExpressionStrategy implements EnableStrategy {

    /**
     * MethodHandle for OpenExpression.eval(String, Map) - null if Expression module not available
     */
    private static final MethodHandle EVAL_HANDLE;

    /**
     * MethodHandle for OpenExpression.isValid(String) - null if Expression module not available
     */
    private static final MethodHandle IS_VALID_HANDLE;

    static {
        EVAL_HANDLE = initEvalHandle();
        IS_VALID_HANDLE = initIsValidHandle();
    }

    private final String expression;
    private final boolean fallbackValue;

    /**
     * Creates an expression strategy with the given expression.
     * 使用给定的表达式创建表达式策略
     *
     * @param expression the expression to evaluate | 要求值的表达式
     */
    private ExpressionStrategy(String expression, boolean fallbackValue) {
        this.expression = Objects.requireNonNull(expression, "expression must not be null");
        this.fallbackValue = fallbackValue;
    }

    /**
     * Initializes MethodHandle for OpenExpression.eval(String, Map).
     */
    private static MethodHandle initEvalHandle() {
        try {
            Class<?> openExprClass = Class.forName("cloud.opencode.base.expression.OpenExpression");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findStatic(openExprClass, "eval",
                    MethodType.methodType(Object.class, String.class, Map.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Initializes MethodHandle for OpenExpression.isValid(String).
     */
    private static MethodHandle initIsValidHandle() {
        try {
            Class<?> openExprClass = Class.forName("cloud.opencode.base.expression.OpenExpression");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findStatic(openExprClass, "isValid",
                    MethodType.methodType(boolean.class, String.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Checks if the Expression module is available.
     * 检查表达式模块是否可用
     *
     * @return true if Expression module is available | 如果表达式模块可用返回 true
     */
    public static boolean isExpressionModuleAvailable() {
        return EVAL_HANDLE != null;
    }

    /**
     * Creates an expression strategy.
     * 创建表达式策略
     *
     * @param expression the expression to evaluate | 要求值的表达式
     * @return the strategy | 策略
     */
    public static ExpressionStrategy of(String expression) {
        return new ExpressionStrategy(expression, false);
    }

    /**
     * Creates an expression strategy with a fallback value.
     * 创建带有回退值的表达式策略
     *
     * @param expression the expression to evaluate | 要求值的表达式
     * @param fallbackValue the value to return if expression evaluation fails | 如果表达式求值失败时返回的值
     * @return the strategy | 策略
     */
    public static ExpressionStrategy of(String expression, boolean fallbackValue) {
        return new ExpressionStrategy(expression, fallbackValue);
    }

    /**
     * Checks if an expression is valid syntax.
     * 检查表达式语法是否有效
     *
     * @param expression the expression to check | 要检查的表达式
     * @return true if expression is valid syntax | 如果表达式语法有效返回 true
     */
    public static boolean isValidExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return false;
        }

        if (IS_VALID_HANDLE == null) {
            // Without expression module, treat all non-empty expressions as valid
            return true;
        }

        try {
            return (boolean) IS_VALID_HANDLE.invokeWithArguments(expression);
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public boolean isEnabled(Feature feature, FeatureContext context) {
        if (EVAL_HANDLE == null) {
            // Expression module not available, return fallback value
            return fallbackValue;
        }

        try {
            Map<String, Object> vars = buildVariables(feature, context);
            Object result = EVAL_HANDLE.invokeWithArguments(expression, vars);
            return toBoolean(result);
        } catch (Throwable e) {
            // On evaluation error, return fallback value
            return fallbackValue;
        }
    }

    /**
     * Builds the variable map from feature and context.
     * 从功能和上下文构建变量映射
     */
    private Map<String, Object> buildVariables(Feature feature, FeatureContext context) {
        Map<String, Object> vars = new HashMap<>();

        // Feature properties (filter out nulls)
        if (feature.key() != null) vars.put("featureKey", feature.key());
        if (feature.name() != null) vars.put("featureName", feature.name());
        vars.put("defaultEnabled", feature.defaultEnabled());

        // Context properties (filter out nulls)
        if (context != null) {
            if (context.userId() != null) vars.put("userId", context.userId());
            if (context.tenantId() != null) vars.put("tenantId", context.tenantId());

            // Add all context attributes (filter out nulls)
            Map<String, Object> attributes = context.attributes();
            if (attributes != null) {
                attributes.forEach((k, v) -> {
                    if (v != null) vars.put(k, v);
                });
            }
        }

        return vars;
    }

    /**
     * Converts the expression result to boolean.
     */
    private boolean toBoolean(Object result) {
        if (result instanceof Boolean b) {
            return b;
        }
        if (result == null) {
            return false;
        }
        // Try to convert common types
        if (result instanceof Number n) {
            return n.doubleValue() != 0;
        }
        if (result instanceof String s) {
            return !s.isEmpty() && !"false".equalsIgnoreCase(s);
        }
        return true; // Non-null objects are truthy
    }

    /**
     * Gets the expression string.
     * 获取表达式字符串
     *
     * @return the expression | 表达式
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Gets the fallback value.
     * 获取回退值
     *
     * @return the fallback value | 回退值
     */
    public boolean getFallbackValue() {
        return fallbackValue;
    }

    @Override
    public String toString() {
        return "ExpressionStrategy{expression='" + expression + "', fallback=" + fallbackValue + "}";
    }
}
