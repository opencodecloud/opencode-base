package cloud.opencode.base.expression.context;

import cloud.opencode.base.expression.function.FunctionRegistry;
import cloud.opencode.base.expression.sandbox.Sandbox;
import cloud.opencode.base.expression.spi.PropertyAccessor;
import cloud.opencode.base.expression.spi.TypeConverter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chained Evaluation Context
 * 链式求值上下文
 *
 * <p>A context that chains multiple contexts together, allowing hierarchical variable lookup.
 * Variables are searched in order from this context to parent contexts.</p>
 * <p>一个将多个上下文链接在一起的上下文，允许分层变量查找。变量按从当前上下文到父上下文的顺序搜索。</p>
 *
 * <h2>Usage | 用法</h2>
 * <pre>{@code
 * EvaluationContext parent = new StandardContext();
 * parent.setVariable("x", 10);
 *
 * ChainedContext child = ChainedContext.of(parent);
 * child.setVariable("y", 20);
 *
 * // child has access to both x (from parent) and y (local)
 * Object result = OpenExpression.eval("x + y", child);  // 30
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Hierarchical variable lookup (local then parent) - 层次化变量查找（本地然后父级）</li>
 *   <li>Inherits function registry and sandbox from parent - 继承父级的函数注册表和沙箱</li>
 *   <li>Chain depth tracking - 链深度跟踪</li>
 *   <li>Builder pattern for construction - 构建器模式用于构造</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, ConcurrentHashMap for local variables - 线程安全: 是，本地变量使用ConcurrentHashMap</li>
 *   <li>Null-safe: Yes, null name returns null/false - 空值安全: 是，null名称返回null/false</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EvaluationContext parent = new StandardContext();
 * parent.setVariable("x", 10);
 * ChainedContext child = ChainedContext.of(parent);
 * child.setVariable("y", 20);
 * Object result = OpenExpression.eval("x + y", child);  // 30
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class ChainedContext implements EvaluationContext {

    private Object rootObject;
    private final Map<String, Object> localVariables;
    private final EvaluationContext parent;
    private final FunctionRegistry functionRegistry;
    private final List<PropertyAccessor> propertyAccessors;
    private final Sandbox sandbox;

    /**
     * Create chained context with parent
     * 使用父上下文创建链式上下文
     *
     * @param parent the parent context | 父上下文
     */
    public ChainedContext(EvaluationContext parent) {
        this(parent, null);
    }

    /**
     * Create chained context with parent and root object
     * 使用父上下文和根对象创建链式上下文
     *
     * @param parent the parent context | 父上下文
     * @param rootObject the root object | 根对象
     */
    public ChainedContext(EvaluationContext parent, Object rootObject) {
        this.parent = Objects.requireNonNull(parent, "Parent context must not be null");
        this.rootObject = rootObject != null ? rootObject : parent.getRootObject();
        this.localVariables = new ConcurrentHashMap<>();
        this.functionRegistry = parent.getFunctionRegistry();
        this.propertyAccessors = new ArrayList<>(parent.getPropertyAccessors());
        this.sandbox = parent.getSandbox();
    }

    @Override
    public Object getRootObject() {
        return rootObject != null ? rootObject : (parent != null ? parent.getRootObject() : null);
    }

    @Override
    public void setRootObject(Object root) {
        this.rootObject = root;
    }

    @Override
    public Object getVariable(String name) {
        if (name == null) {
            return null;
        }
        // First check local variables
        if (localVariables.containsKey(name)) {
            return localVariables.get(name);
        }
        // Then check parent
        if (parent != null) {
            return parent.getVariable(name);
        }
        return null;
    }

    @Override
    public void setVariable(String name, Object value) {
        if (name != null) {
            localVariables.put(name, value);
        }
    }

    @Override
    public boolean hasVariable(String name) {
        if (name == null) {
            return false;
        }
        if (localVariables.containsKey(name)) {
            return true;
        }
        return parent != null && parent.hasVariable(name);
    }

    @Override
    public Map<String, Object> getVariables() {
        Map<String, Object> allVars = new HashMap<>();
        if (parent != null) {
            allVars.putAll(parent.getVariables());
        }
        allVars.putAll(localVariables);
        return Collections.unmodifiableMap(allVars);
    }

    /**
     * Get local variables only (not including parent)
     * 仅获取本地变量（不包括父上下文）
     *
     * @return the local variable map | 本地变量映射
     */
    public Map<String, Object> getLocalVariables() {
        return Collections.unmodifiableMap(localVariables);
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
        return parent != null ? parent.getTypeConverter() : null;
    }

    @Override
    public Sandbox getSandbox() {
        return sandbox;
    }

    @Override
    public EvaluationContext createChild() {
        return new ChainedContext(this, rootObject);
    }

    /**
     * Get the parent context
     * 获取父上下文
     *
     * @return the parent context | 父上下文
     */
    public EvaluationContext getParent() {
        return parent;
    }

    /**
     * Get the chain depth (number of parent contexts + 1)
     * 获取链深度（父上下文数量 + 1）
     *
     * @return the chain depth | 链深度
     */
    public int getDepth() {
        int depth = 1;
        EvaluationContext current = parent;
        while (current != null) {
            depth++;
            if (current instanceof ChainedContext chained) {
                current = chained.parent;
            } else {
                break;
            }
        }
        return depth;
    }

    /**
     * Create chained context from parent
     * 从父上下文创建链式上下文
     *
     * @param parent the parent context | 父上下文
     * @return the chained context | 链式上下文
     */
    public static ChainedContext of(EvaluationContext parent) {
        return new ChainedContext(parent);
    }

    /**
     * Create chained context from parent with root object
     * 从父上下文和根对象创建链式上下文
     *
     * @param parent the parent context | 父上下文
     * @param rootObject the root object | 根对象
     * @return the chained context | 链式上下文
     */
    public static ChainedContext of(EvaluationContext parent, Object rootObject) {
        return new ChainedContext(parent, rootObject);
    }

    /**
     * Create a builder for ChainedContext
     * 创建 ChainedContext 的构建器
     *
     * @param parent the parent context | 父上下文
     * @return the builder | 构建器
     */
    public static Builder builder(EvaluationContext parent) {
        return new Builder(parent);
    }

    /**
     * Builder for ChainedContext
     * ChainedContext 构建器
     */
    public static class Builder {
        private final EvaluationContext parent;
        private Object rootObject;
        private final Map<String, Object> variables = new HashMap<>();

        /**
         * Create builder with parent context
         * 使用父上下文创建构建器
         *
         * @param parent the parent context | 父上下文
         */
        public Builder(EvaluationContext parent) {
            this.parent = Objects.requireNonNull(parent);
        }

        /**
         * Set root object
         * 设置根对象
         *
         * @param root the root object | 根对象
         * @return this builder | 此构建器
         */
        public Builder rootObject(Object root) {
            this.rootObject = root;
            return this;
        }

        /**
         * Set a variable
         * 设置变量
         *
         * @param name the variable name | 变量名
         * @param value the variable value | 变量值
         * @return this builder | 此构建器
         */
        public Builder variable(String name, Object value) {
            this.variables.put(name, value);
            return this;
        }

        /**
         * Build the context
         * 构建上下文
         *
         * @return the context | 上下文
         */
        public ChainedContext build() {
            ChainedContext ctx = new ChainedContext(parent, rootObject);
            variables.forEach(ctx::setVariable);
            return ctx;
        }
    }
}
