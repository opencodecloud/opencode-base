package cloud.opencode.base.expression.context;

import cloud.opencode.base.expression.function.FunctionRegistry;
import cloud.opencode.base.expression.sandbox.Sandbox;
import cloud.opencode.base.expression.spi.PropertyAccessor;
import cloud.opencode.base.expression.spi.TypeConverter;

import java.util.*;

/**
 * Map-based Evaluation Context
 * 基于 Map 的求值上下文
 *
 * <p>A lightweight context that wraps a Map for simple variable-based expressions.
 * The map values are treated as variables.</p>
 * <p>一个轻量级上下文，包装 Map 用于简单的基于变量的表达式。Map 值被视为变量。</p>
 *
 * <h2>Usage | 用法</h2>
 * <pre>{@code
 * Map<String, Object> vars = Map.of("name", "John", "age", 30);
 * MapContext ctx = new MapContext(vars);
 * Object result = OpenExpression.eval("name + ' is ' + age", ctx);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lightweight context backed by HashMap - 基于HashMap的轻量级上下文</li>
 *   <li>Map values treated as variables - Map值被视为变量</li>
 *   <li>Builder pattern for construction - 构建器模式用于构造</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No, uses HashMap internally - 线程安全: 否，内部使用HashMap</li>
 *   <li>Null-safe: Yes, null name ignored on setVariable - 空值安全: 是，setVariable中忽略null名称</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MapContext ctx = new MapContext(Map.of("name", "John", "age", 30));
 * Object result = OpenExpression.eval("name + ' is ' + age", ctx);  // "John is 30"
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class MapContext implements EvaluationContext {

    private final Map<String, Object> variables;
    private final FunctionRegistry functionRegistry;
    private final List<PropertyAccessor> propertyAccessors;
    private final Sandbox sandbox;

    /**
     * Create empty map context
     * 创建空的 Map 上下文
     */
    public MapContext() {
        this(new HashMap<>());
    }

    /**
     * Create map context with initial variables
     * 使用初始变量创建 Map 上下文
     *
     * @param variables the variable map | 变量映射
     */
    public MapContext(Map<String, Object> variables) {
        this(variables, null, null);
    }

    /**
     * Create map context with full customization
     * 使用完整自定义创建 Map 上下文
     *
     * @param variables the variable map | 变量映射
     * @param functionRegistry the function registry | 函数注册表
     * @param sandbox the security sandbox | 安全沙箱
     */
    public MapContext(Map<String, Object> variables, FunctionRegistry functionRegistry, Sandbox sandbox) {
        this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
        this.functionRegistry = functionRegistry != null ? functionRegistry : FunctionRegistry.create();
        this.propertyAccessors = new ArrayList<>();
        this.sandbox = sandbox;
    }

    @Override
    public Object getRootObject() {
        return variables;
    }

    @Override
    public void setRootObject(Object root) {
        if (root instanceof Map<?, ?> map) {
            variables.clear();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    variables.put(key, entry.getValue());
                }
            }
        }
    }

    @Override
    public Object getVariable(String name) {
        return variables.get(name);
    }

    @Override
    public void setVariable(String name, Object value) {
        if (name != null) {
            variables.put(name, value);
        }
    }

    @Override
    public boolean hasVariable(String name) {
        return name != null && variables.containsKey(name);
    }

    @Override
    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    @Override
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    @Override
    public List<PropertyAccessor> getPropertyAccessors() {
        return Collections.unmodifiableList(propertyAccessors);
    }

    @Override
    public TypeConverter getTypeConverter() {
        return null;
    }

    @Override
    public Sandbox getSandbox() {
        return sandbox;
    }

    @Override
    public EvaluationContext createChild() {
        MapContext child = new MapContext(new HashMap<>(variables), functionRegistry, sandbox);
        return child;
    }

    /**
     * Create a builder for MapContext
     * 创建 MapContext 的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create from map
     * 从 Map 创建
     *
     * @param map the variable map | 变量映射
     * @return the context | 上下文
     */
    public static MapContext of(Map<String, Object> map) {
        return new MapContext(map);
    }

    /**
     * Builder for MapContext
     * MapContext 构建器
     */
    public static class Builder {
        private final Map<String, Object> variables = new HashMap<>();
        private FunctionRegistry functionRegistry;
        private Sandbox sandbox;

        /**
         * Set a variable
         * 设置变量
         *
         * @param name the variable name | 变量名
         * @param value the variable value | 变量值
         * @return this builder | 此构建器
         */
        public Builder variable(String name, Object value) {
            variables.put(name, value);
            return this;
        }

        /**
         * Set all variables
         * 设置所有变量
         *
         * @param vars the variable map | 变量映射
         * @return this builder | 此构建器
         */
        public Builder variables(Map<String, Object> vars) {
            if (vars != null) {
                variables.putAll(vars);
            }
            return this;
        }

        /**
         * Set function registry
         * 设置函数注册表
         *
         * @param registry the registry | 注册表
         * @return this builder | 此构建器
         */
        public Builder functionRegistry(FunctionRegistry registry) {
            this.functionRegistry = registry;
            return this;
        }

        /**
         * Set sandbox
         * 设置沙箱
         *
         * @param sandbox the sandbox | 沙箱
         * @return this builder | 此构建器
         */
        public Builder sandbox(Sandbox sandbox) {
            this.sandbox = sandbox;
            return this;
        }

        /**
         * Build the context
         * 构建上下文
         *
         * @return the context | 上下文
         */
        public MapContext build() {
            return new MapContext(variables, functionRegistry, sandbox);
        }
    }
}
