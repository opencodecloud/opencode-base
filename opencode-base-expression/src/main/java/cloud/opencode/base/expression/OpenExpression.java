package cloud.opencode.base.expression;

import cloud.opencode.base.expression.ast.Node;
import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.context.StandardContext;
import cloud.opencode.base.expression.eval.TypeCoercion;
import cloud.opencode.base.expression.function.FunctionRegistry;
import cloud.opencode.base.expression.parser.Parser;
import cloud.opencode.base.expression.sandbox.DefaultSandbox;
import cloud.opencode.base.expression.sandbox.Sandbox;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OpenExpression - Main Entry Point
 * OpenExpression - 主入口点
 *
 * <p>Provides a simple and powerful expression evaluation API.</p>
 * <p>提供简单而强大的表达式求值API。</p>
 *
 * <h2>Basic Usage | 基本用法</h2>
 * <pre>{@code
 * // Simple evaluation
 * Object result = OpenExpression.eval("1 + 2 * 3");  // 7
 *
 * // With variables
 * Object result = OpenExpression.eval("name + ' World'", Map.of("name", "Hello"));  // "Hello World"
 *
 * // With root object
 * Object result = OpenExpression.eval("user.name", myUser);  // user's name
 *
 * // Type conversion
 * int result = OpenExpression.eval("100 / 3", Integer.class);  // 33
 * }</pre>
 *
 * <h2>Advanced Usage | 高级用法</h2>
 * <pre>{@code
 * // Create parser for reuse
 * ExpressionParser parser = OpenExpression.parser();
 * Expression expr = parser.parseExpression("price * quantity");
 *
 * // Evaluate with different contexts
 * StandardContext ctx = StandardContext.builder()
 *     .rootObject(order1)
 *     .sandbox(DefaultSandbox.standard())
 *     .build();
 * Object result1 = expr.getValue(ctx);
 *
 * ctx.setRootObject(order2);
 * Object result2 = expr.getValue(ctx);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Simple one-line expression evaluation - 简单的一行表达式求值</li>
 *   <li>Variable binding via Map or EvaluationContext - 通过Map或EvaluationContext绑定变量</li>
 *   <li>Type-safe evaluation with automatic conversion - 类型安全求值与自动转换</li>
 *   <li>Built-in expression caching (LRU, max 1000) - 内置表达式缓存（LRU，最大1000）</li>
 *   <li>Sandbox support for security constraints - 沙箱支持安全约束</li>
 *   <li>Expression validation - 表达式验证</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, uses synchronized LRU cache - 线程安全: 是，使用同步的LRU缓存</li>
 *   <li>Null-safe: Yes, null context/variables handled gracefully - 空值安全: 是，null上下文/变量优雅处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple evaluation
 * Object result = OpenExpression.eval("1 + 2 * 3");  // 7
 *
 * // With variables
 * Object result = OpenExpression.eval("name + ' World'", Map.of("name", "Hello"));
 *
 * // Type-safe evaluation
 * int result = OpenExpression.eval("100 / 3", Integer.class);  // 33
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public final class OpenExpression {

    private static final ExpressionParser DEFAULT_PARSER = new StandardExpressionParser();
    private static final int MAX_CACHE_SIZE = 1000;
    private static final Map<String, ParsedExpression> EXPRESSION_CACHE =
            Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, ParsedExpression> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            });

    private OpenExpression() {
    }

    // ==================== Simple Evaluation | 简单求值 ====================

    /**
     * Evaluate expression
     * 求值表达式
     *
     * @param expression the expression string | 表达式字符串
     * @return the result | 结果
     */
    public static Object eval(String expression) {
        return eval(expression, (EvaluationContext) null);
    }

    /**
     * Evaluate expression with variables
     * 使用变量求值表达式
     *
     * @param expression the expression string | 表达式字符串
     * @param variables the variable map | 变量映射
     * @return the result | 结果
     */
    public static Object eval(String expression, Map<String, Object> variables) {
        StandardContext ctx = new StandardContext();
        if (variables != null) {
            variables.forEach(ctx::setVariable);
        }
        return eval(expression, ctx);
    }

    /**
     * Evaluate expression with root object
     * 使用根对象求值表达式
     *
     * @param expression the expression string | 表达式字符串
     * @param rootObject the root object | 根对象
     * @return the result | 结果
     */
    public static Object eval(String expression, Object rootObject) {
        if (rootObject instanceof EvaluationContext ctx) {
            return eval(expression, ctx);
        }
        if (rootObject instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> vars = (Map<String, Object>) map;
            return eval(expression, vars);
        }
        return eval(expression, new StandardContext(rootObject));
    }

    /**
     * Evaluate expression with context
     * 使用上下文求值表达式
     *
     * @param expression the expression string | 表达式字符串
     * @param context the evaluation context | 求值上下文
     * @return the result | 结果
     */
    public static Object eval(String expression, EvaluationContext context) {
        ParsedExpression parsed = getCachedExpression(expression);
        EvaluationContext ctx = context != null ? context : new StandardContext();
        return parsed.node().evaluate(ctx);
    }

    // ==================== Typed Evaluation | 类型化求值 ====================

    /**
     * Evaluate expression and convert to type
     * 求值表达式并转换为指定类型
     *
     * @param expression the expression string | 表达式字符串
     * @param targetType the target type | 目标类型
     * @param <T> the target type | 目标类型
     * @return the typed result | 类型化结果
     */
    public static <T> T eval(String expression, Class<T> targetType) {
        return eval(expression, (EvaluationContext) null, targetType);
    }

    /**
     * Evaluate expression with variables and convert to type
     * 使用变量求值表达式并转换为指定类型
     *
     * @param expression the expression string | 表达式字符串
     * @param variables the variable map | 变量映射
     * @param targetType the target type | 目标类型
     * @param <T> the target type | 目标类型
     * @return the typed result | 类型化结果
     */
    public static <T> T eval(String expression, Map<String, Object> variables, Class<T> targetType) {
        StandardContext ctx = new StandardContext();
        if (variables != null) {
            variables.forEach(ctx::setVariable);
        }
        return eval(expression, ctx, targetType);
    }

    /**
     * Evaluate expression with context and convert to type
     * 使用上下文求值表达式并转换为指定类型
     *
     * @param expression the expression string | 表达式字符串
     * @param context the evaluation context | 求值上下文
     * @param targetType the target type | 目标类型
     * @param <T> the target type | 目标类型
     * @return the typed result | 类型化结果
     */
    public static <T> T eval(String expression, EvaluationContext context, Class<T> targetType) {
        Object result = eval(expression, context);
        return TypeCoercion.convert(result, targetType);
    }

    // ==================== Parser Access | 解析器访问 ====================

    /**
     * Get the default expression parser
     * 获取默认表达式解析器
     *
     * @return the default parser | 默认解析器
     */
    public static ExpressionParser parser() {
        return DEFAULT_PARSER;
    }

    /**
     * Create a new parser
     * 创建新解析器
     *
     * @return new parser | 新解析器
     */
    public static ExpressionParser newParser() {
        return new StandardExpressionParser();
    }

    /**
     * Parse expression
     * 解析表达式
     *
     * @param expression the expression string | 表达式字符串
     * @return the parsed expression | 解析后的表达式
     */
    public static Expression parse(String expression) {
        return DEFAULT_PARSER.parseExpression(expression);
    }

    // ==================== Function Registry | 函数注册表 ====================

    /**
     * Get the global function registry
     * 获取全局函数注册表
     *
     * @return the global registry | 全局注册表
     */
    public static FunctionRegistry functions() {
        return FunctionRegistry.getGlobal();
    }

    // ==================== Sandbox | 沙箱 ====================

    /**
     * Create a standard sandbox
     * 创建标准沙箱
     *
     * @return the standard sandbox | 标准沙箱
     */
    public static Sandbox standardSandbox() {
        return DefaultSandbox.standard();
    }

    /**
     * Create a restrictive sandbox
     * 创建限制性沙箱
     *
     * @return the restrictive sandbox | 限制性沙箱
     */
    public static Sandbox restrictiveSandbox() {
        return DefaultSandbox.restrictive();
    }

    /**
     * Create a permissive sandbox
     * 创建宽松沙箱
     *
     * @return the permissive sandbox | 宽松沙箱
     */
    public static Sandbox permissiveSandbox() {
        return DefaultSandbox.permissive();
    }

    // ==================== Context | 上下文 ====================

    /**
     * Create a new standard context
     * 创建新的标准上下文
     *
     * @return new context | 新上下文
     */
    public static StandardContext context() {
        return new StandardContext();
    }

    /**
     * Create a new standard context with root object
     * 使用根对象创建新的标准上下文
     *
     * @param rootObject the root object | 根对象
     * @return new context | 新上下文
     */
    public static StandardContext context(Object rootObject) {
        return new StandardContext(rootObject);
    }

    /**
     * Create a context builder
     * 创建上下文构建器
     *
     * @return the builder | 构建器
     */
    public static StandardContext.Builder contextBuilder() {
        return StandardContext.builder();
    }

    // ==================== Variable Discovery | 变量发现 ====================

    /**
     * Extract variable names from expression
     * 从表达式中提取变量名
     *
     * @param expression the expression string | 表达式字符串
     * @return set of variable names | 变量名集合
     */
    public static java.util.Set<String> extractVariables(String expression) {
        return VariableExtractor.extract(expression);
    }

    // ==================== Template | 模板 ====================

    /**
     * Render expression template with variables
     * 使用变量渲染表达式模板
     *
     * <p>Supports ${expression} placeholders.</p>
     * <p>支持 ${expression} 占位符。</p>
     *
     * @param template the template string | 模板字符串
     * @param variables the variable map | 变量映射
     * @return the rendered string | 渲染后的字符串
     */
    public static String render(String template, Map<String, Object> variables) {
        return ExpressionTemplate.render(template, variables);
    }

    /**
     * Render expression template with context
     * 使用上下文渲染表达式模板
     *
     * @param template the template string | 模板字符串
     * @param context the evaluation context | 求值上下文
     * @return the rendered string | 渲染后的字符串
     */
    public static String render(String template, EvaluationContext context) {
        return ExpressionTemplate.render(template, context);
    }

    // ==================== Utilities | 工具方法 ====================

    /**
     * Check if expression is valid
     * 检查表达式是否有效
     *
     * @param expression the expression string | 表达式字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String expression) {
        if (expression == null || expression.isBlank()) {
            return false;
        }
        try {
            Parser.parse(expression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clear the expression cache
     * 清除表达式缓存
     */
    public static void clearCache() {
        EXPRESSION_CACHE.clear();
    }

    /**
     * Get cache size
     * 获取缓存大小
     *
     * @return the cache size | 缓存大小
     */
    public static int cacheSize() {
        return EXPRESSION_CACHE.size();
    }

    // ==================== Internal | 内部方法 ====================

    private static ParsedExpression getCachedExpression(String expression) {
        ParsedExpression cached = EXPRESSION_CACHE.get(expression);
        if (cached != null) {
            return cached;
        }
        // Parse and cache; LRU eviction is handled automatically by removeEldestEntry
        Node node = Parser.parse(expression);
        ParsedExpression parsed = new ParsedExpression(expression, node);
        EXPRESSION_CACHE.putIfAbsent(expression, parsed);
        ParsedExpression result = EXPRESSION_CACHE.get(expression);
        return result != null ? result : parsed;
    }

    private record ParsedExpression(String expression, Node node) {}

    /**
     * Standard Expression Parser Implementation
     * 标准表达式解析器实现
     */
    private static class StandardExpressionParser implements ExpressionParser {
        @Override
        public Expression parseExpression(String expressionString) {
            return new StandardExpression(expressionString);
        }
    }

    /**
     * Standard Expression Implementation
     * 标准表达式实现
     */
    private static class StandardExpression implements Expression {
        private final String expressionString;
        private final Node node;

        StandardExpression(String expressionString) {
            this.expressionString = Objects.requireNonNull(expressionString);
            this.node = Parser.parse(expressionString);
        }

        @Override
        public String getExpressionString() {
            return expressionString;
        }

        @Override
        public Object getValue() {
            return getValue(new StandardContext());
        }

        @Override
        public Object getValue(EvaluationContext context) {
            return node.evaluate(context != null ? context : new StandardContext());
        }

        @Override
        public <T> T getValue(Class<T> targetType) {
            return getValue(new StandardContext(), targetType);
        }

        @Override
        public <T> T getValue(EvaluationContext context, Class<T> targetType) {
            Object result = getValue(context);
            return TypeCoercion.convert(result, targetType);
        }

        @Override
        public Object getValue(Object rootObject) {
            return getValue(new StandardContext(rootObject));
        }

        @Override
        public <T> T getValue(Object rootObject, Class<T> targetType) {
            return getValue(new StandardContext(rootObject), targetType);
        }

        @Override
        public Class<?> getValueType() {
            Object value = getValue();
            return value != null ? value.getClass() : Object.class;
        }

        @Override
        public Class<?> getValueType(EvaluationContext context) {
            Object value = getValue(context);
            return value != null ? value.getClass() : Object.class;
        }
    }
}
