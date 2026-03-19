package cloud.opencode.base.expression.context;

import cloud.opencode.base.expression.function.FunctionRegistry;
import cloud.opencode.base.expression.sandbox.Sandbox;
import cloud.opencode.base.expression.spi.PropertyAccessor;
import cloud.opencode.base.expression.spi.TypeConverter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean-based Evaluation Context
 * 基于 Bean 的求值上下文
 *
 * <p>A context that wraps a Java bean as the root object.
 * Properties of the bean can be accessed directly in expressions.</p>
 * <p>一个以 Java Bean 作为根对象的上下文。Bean 的属性可以在表达式中直接访问。</p>
 *
 * <h2>Usage | 用法</h2>
 * <pre>{@code
 * User user = new User("John", 30);
 * BeanContext ctx = new BeanContext(user);
 * Object name = OpenExpression.eval("name", ctx);  // "John"
 * Object age = OpenExpression.eval("age", ctx);    // 30
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Wraps a Java bean as root object for property access - 将Java Bean包装为根对象用于属性访问</li>
 *   <li>Built-in BeanPropertyAccessor for getter/field access - 内置BeanPropertyAccessor用于getter/字段访问</li>
 *   <li>Variable management with ConcurrentHashMap - 使用ConcurrentHashMap的变量管理</li>
 *   <li>Builder pattern for construction - 构建器模式用于构造</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, ConcurrentHashMap for variables - 线程安全: 是，变量使用ConcurrentHashMap</li>
 *   <li>Null-safe: Yes, null name ignored on setVariable - 空值安全: 是，setVariable中忽略null名称</li>
 *   <li>Only public methods and fields accessible - 仅可访问公共方法和字段</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * User user = new User("John", 30);
 * BeanContext ctx = new BeanContext(user);
 * Object name = OpenExpression.eval("name", ctx);  // "John"
 * Object age = OpenExpression.eval("age", ctx);     // 30
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class BeanContext implements EvaluationContext {

    private Object rootObject;
    private final Map<String, Object> variables;
    private final FunctionRegistry functionRegistry;
    private final List<PropertyAccessor> propertyAccessors;
    private final Sandbox sandbox;

    /**
     * Create bean context with root object
     * 使用根对象创建 Bean 上下文
     *
     * @param rootObject the root bean | 根 Bean
     */
    public BeanContext(Object rootObject) {
        this(rootObject, null, null);
    }

    /**
     * Create bean context with full customization
     * 使用完整自定义创建 Bean 上下文
     *
     * @param rootObject the root bean | 根 Bean
     * @param functionRegistry the function registry | 函数注册表
     * @param sandbox the security sandbox | 安全沙箱
     */
    public BeanContext(Object rootObject, FunctionRegistry functionRegistry, Sandbox sandbox) {
        this.rootObject = rootObject;
        this.variables = new ConcurrentHashMap<>();
        this.functionRegistry = functionRegistry != null ? functionRegistry : FunctionRegistry.create();
        this.propertyAccessors = new ArrayList<>();
        this.sandbox = sandbox;

        // Add default bean property accessor
        this.propertyAccessors.add(new BeanPropertyAccessor());
    }

    @Override
    public Object getRootObject() {
        return rootObject;
    }

    @Override
    public void setRootObject(Object root) {
        this.rootObject = root;
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
        BeanContext child = new BeanContext(rootObject, functionRegistry, sandbox);
        child.variables.putAll(this.variables);
        return child;
    }

    /**
     * Create from bean
     * 从 Bean 创建
     *
     * @param bean the bean object | Bean 对象
     * @return the context | 上下文
     */
    public static BeanContext of(Object bean) {
        return new BeanContext(bean);
    }

    /**
     * Create a builder for BeanContext
     * 创建 BeanContext 的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for BeanContext
     * BeanContext 构建器
     */
    public static class Builder {
        private Object rootObject;
        private FunctionRegistry functionRegistry;
        private Sandbox sandbox;
        private final Map<String, Object> variables = new HashMap<>();

        /**
         * Set root object
         * 设置根对象
         *
         * @param root the root bean | 根 Bean
         * @return this builder | 此构建器
         */
        public Builder rootObject(Object root) {
            this.rootObject = root;
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
        public BeanContext build() {
            BeanContext ctx = new BeanContext(rootObject, functionRegistry, sandbox);
            variables.forEach(ctx::setVariable);
            return ctx;
        }
    }

    /**
     * Bean Property Accessor
     * Bean 属性访问器
     */
    private static class BeanPropertyAccessor implements PropertyAccessor {
        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return null; // All types
        }

        @Override
        public boolean canRead(Object target, String name) {
            if (target == null || name == null) {
                return false;
            }
            try {
                return findGetter(target.getClass(), name) != null ||
                       findField(target.getClass(), name) != null;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public Object read(Object target, String name) {
            if (target == null || name == null) {
                return null;
            }
            try {
                java.lang.reflect.Method getter = findGetter(target.getClass(), name);
                if (getter != null) {
                    if (!java.lang.reflect.Modifier.isPublic(getter.getModifiers())) {
                        return null; // Only allow public methods
                    }
                    return getter.invoke(target);
                }
                java.lang.reflect.Field field = findField(target.getClass(), name);
                if (field != null) {
                    if (!java.lang.reflect.Modifier.isPublic(field.getModifiers())) {
                        return null; // Only allow public fields
                    }
                    return field.get(target);
                }
            } catch (Exception e) {
                // Ignore
            }
            return null;
        }

        private java.lang.reflect.Method findGetter(Class<?> clazz, String name) {
            String capitalized = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            try {
                return clazz.getMethod("get" + capitalized);
            } catch (NoSuchMethodException e) {
                try {
                    return clazz.getMethod("is" + capitalized);
                } catch (NoSuchMethodException e2) {
                    try {
                        // Record accessor style
                        return clazz.getMethod(name);
                    } catch (NoSuchMethodException e3) {
                        return null;
                    }
                }
            }
        }

        private java.lang.reflect.Field findField(Class<?> clazz, String name) {
            Class<?> current = clazz;
            while (current != null) {
                try {
                    return current.getDeclaredField(name);
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            return null;
        }
    }
}
