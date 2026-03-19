package cloud.opencode.base.expression.compiler;

import cloud.opencode.base.expression.ast.Node;
import cloud.opencode.base.expression.parser.Parser;

/**
 * Expression Compiler
 * 表达式编译器
 *
 * <p>Compiles expression strings into optimized AST nodes that can be efficiently evaluated.
 * Supports caching and optimization for frequently used expressions.</p>
 * <p>将表达式字符串编译为可高效求值的优化 AST 节点。支持对常用表达式进行缓存和优化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compile expressions with optional optimization - 编译表达式，可选优化</li>
 *   <li>Configurable caching strategy - 可配置的缓存策略</li>
 *   <li>Builder pattern for customization - 构建器模式用于自定义</li>
 *   <li>Factory methods for common configurations - 常见配置的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default compiler with cache and optimization
 * ExpressionCompiler compiler = ExpressionCompiler.getDefault();
 * CompiledExpression expr = compiler.compile("a + b * c");
 *
 * // Custom compiler via builder
 * ExpressionCompiler custom = ExpressionCompiler.builder()
 *     .cacheSize(500)
 *     .optimization(true)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, delegates to thread-safe cache - 线程安全: 是，委托给线程安全的缓存</li>
 *   <li>Null-safe: No, null expression not supported - 空值安全: 否，不支持null表达式</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: compile() O(1) for cached expressions; O(n) for first compilation where n is the expression length - 时间复杂度: 缓存命中时 compile() 为 O(1)；首次编译为 O(n)，n为表达式长度</li>
 *   <li>Space complexity: O(n) for the AST; O(c) for the cache where c is the cache size - 空间复杂度: AST 为 O(n)；缓存为 O(c)，c为缓存大小</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class ExpressionCompiler {

    private static final ExpressionCompiler DEFAULT = new ExpressionCompiler();

    private final ExpressionCache cache;
    private final Optimizer optimizer;
    private final boolean optimizationEnabled;

    /**
     * Create compiler with default settings
     * 使用默认设置创建编译器
     */
    public ExpressionCompiler() {
        this(ExpressionCache.global(), new Optimizer(), true);
    }

    /**
     * Create compiler with custom settings
     * 使用自定义设置创建编译器
     *
     * @param cache the expression cache | 表达式缓存
     * @param optimizer the optimizer | 优化器
     * @param optimizationEnabled whether optimization is enabled | 是否启用优化
     */
    public ExpressionCompiler(ExpressionCache cache, Optimizer optimizer, boolean optimizationEnabled) {
        this.cache = cache;
        this.optimizer = optimizer;
        this.optimizationEnabled = optimizationEnabled;
    }

    /**
     * Get the default compiler instance
     * 获取默认编译器实例
     *
     * @return the default compiler | 默认编译器
     */
    public static ExpressionCompiler getDefault() {
        return DEFAULT;
    }

    /**
     * Create a new compiler
     * 创建新编译器
     *
     * @return new compiler | 新编译器
     */
    public static ExpressionCompiler create() {
        return new ExpressionCompiler();
    }

    /**
     * Create a compiler without caching
     * 创建不带缓存的编译器
     *
     * @return the compiler | 编译器
     */
    public static ExpressionCompiler withoutCache() {
        return new ExpressionCompiler(null, new Optimizer(), true);
    }

    /**
     * Create a compiler without optimization
     * 创建不带优化的编译器
     *
     * @return the compiler | 编译器
     */
    public static ExpressionCompiler withoutOptimization() {
        return new ExpressionCompiler(ExpressionCache.global(), null, false);
    }

    /**
     * Compile expression string
     * 编译表达式字符串
     *
     * @param expression the expression string | 表达式字符串
     * @return the compiled expression | 编译后的表达式
     */
    public CompiledExpression compile(String expression) {
        // Check cache first
        if (cache != null) {
            CompiledExpression cached = cache.get(expression);
            if (cached != null) {
                return cached;
            }
        }

        // Parse and compile
        Node ast = Parser.parse(expression);

        // Optimize if enabled
        if (optimizationEnabled && optimizer != null) {
            ast = optimizer.optimize(ast);
        }

        // Create compiled expression
        CompiledExpression compiled = new CompiledExpression(expression, ast);

        // Cache if enabled
        if (cache != null) {
            cache.put(expression, compiled);
        }

        return compiled;
    }

    /**
     * Compile expression without caching
     * 编译表达式不使用缓存
     *
     * @param expression the expression string | 表达式字符串
     * @return the compiled expression | 编译后的表达式
     */
    public CompiledExpression compileWithoutCache(String expression) {
        Node ast = Parser.parse(expression);

        if (optimizationEnabled && optimizer != null) {
            ast = optimizer.optimize(ast);
        }

        return new CompiledExpression(expression, ast);
    }

    /**
     * Compile expression with explicit optimization setting
     * 使用显式优化设置编译表达式
     *
     * @param expression the expression string | 表达式字符串
     * @param optimize whether to optimize | 是否优化
     * @return the compiled expression | 编译后的表达式
     */
    public CompiledExpression compile(String expression, boolean optimize) {
        Node ast = Parser.parse(expression);

        if (optimize && optimizer != null) {
            ast = optimizer.optimize(ast);
        }

        CompiledExpression compiled = new CompiledExpression(expression, ast);

        if (cache != null) {
            cache.put(expression, compiled);
        }

        return compiled;
    }

    /**
     * Check if expression is cached
     * 检查表达式是否已缓存
     *
     * @param expression the expression string | 表达式字符串
     * @return true if cached | 如果已缓存返回 true
     */
    public boolean isCached(String expression) {
        return cache != null && cache.contains(expression);
    }

    /**
     * Get the cache
     * 获取缓存
     *
     * @return the cache | 缓存
     */
    public ExpressionCache getCache() {
        return cache;
    }

    /**
     * Get the optimizer
     * 获取优化器
     *
     * @return the optimizer | 优化器
     */
    public Optimizer getOptimizer() {
        return optimizer;
    }

    /**
     * Check if optimization is enabled
     * 检查是否启用优化
     *
     * @return true if enabled | 如果启用返回 true
     */
    public boolean isOptimizationEnabled() {
        return optimizationEnabled;
    }

    /**
     * Clear the cache
     * 清除缓存
     */
    public void clearCache() {
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Create a builder for ExpressionCompiler
     * 创建 ExpressionCompiler 的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ExpressionCompiler
     * ExpressionCompiler 构建器
     */
    public static class Builder {
        private ExpressionCache cache = ExpressionCache.global();
        private Optimizer optimizer = new Optimizer();
        private boolean optimizationEnabled = true;

        /**
         * Set the cache
         * 设置缓存
         *
         * @param cache the cache | 缓存
         * @return this builder | 此构建器
         */
        public Builder cache(ExpressionCache cache) {
            this.cache = cache;
            return this;
        }

        /**
         * Disable caching
         * 禁用缓存
         *
         * @return this builder | 此构建器
         */
        public Builder noCache() {
            this.cache = null;
            return this;
        }

        /**
         * Set custom cache size
         * 设置自定义缓存大小
         *
         * @param maxSize the max size | 最大大小
         * @return this builder | 此构建器
         */
        public Builder cacheSize(int maxSize) {
            this.cache = ExpressionCache.create(maxSize);
            return this;
        }

        /**
         * Set the optimizer
         * 设置优化器
         *
         * @param optimizer the optimizer | 优化器
         * @return this builder | 此构建器
         */
        public Builder optimizer(Optimizer optimizer) {
            this.optimizer = optimizer;
            return this;
        }

        /**
         * Enable or disable optimization
         * 启用或禁用优化
         *
         * @param enabled true to enable | true 表示启用
         * @return this builder | 此构建器
         */
        public Builder optimization(boolean enabled) {
            this.optimizationEnabled = enabled;
            return this;
        }

        /**
         * Disable optimization
         * 禁用优化
         *
         * @return this builder | 此构建器
         */
        public Builder noOptimization() {
            this.optimizationEnabled = false;
            return this;
        }

        /**
         * Build the compiler
         * 构建编译器
         *
         * @return the compiler | 编译器
         */
        public ExpressionCompiler build() {
            return new ExpressionCompiler(cache, optimizer, optimizationEnabled);
        }
    }
}
