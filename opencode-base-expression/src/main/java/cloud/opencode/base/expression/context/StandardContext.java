package cloud.opencode.base.expression.context;

import cloud.opencode.base.expression.function.FunctionRegistry;
import cloud.opencode.base.expression.sandbox.Sandbox;
import cloud.opencode.base.expression.spi.PropertyAccessor;
import cloud.opencode.base.expression.spi.TypeConverter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Standard Evaluation Context
 * 标准求值上下文
 *
 * <p>The default implementation of EvaluationContext with full feature support.</p>
 * <p>具有完整功能支持的EvaluationContext默认实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Variable management with ConcurrentHashMap - 使用ConcurrentHashMap的变量管理</li>
 *   <li>Built-in Map and Bean property accessors - 内置Map和Bean属性访问器</li>
 *   <li>Parent context chaining for scoped evaluation - 父上下文链接用于作用域求值</li>
 *   <li>Configurable sandbox, type converter, and function registry - 可配置沙箱、类型转换器和函数注册表</li>
 *   <li>Builder pattern for construction - 构建器模式用于构造</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * StandardContext ctx = StandardContext.builder()
 *     .rootObject(myBean)
 *     .sandbox(DefaultSandbox.standard())
 *     .build();
 * ctx.setVariable("discount", 0.1);
 * Object result = OpenExpression.eval("price * (1 - discount)", ctx);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, ConcurrentHashMap for variables - 线程安全: 是，变量使用ConcurrentHashMap</li>
 *   <li>Null-safe: Yes, null name/variable handled gracefully - 空值安全: 是，null名称/变量优雅处理</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class StandardContext implements EvaluationContext {

    private Object rootObject;
    private final Map<String, Object> variables;
    private final FunctionRegistry functionRegistry;
    private final List<PropertyAccessor> propertyAccessors;
    private final TypeConverter typeConverter;
    private final Sandbox sandbox;
    private final EvaluationContext parent;

    /**
     * Create standard context with default settings
     * 使用默认设置创建标准上下文
     */
    public StandardContext() {
        this(null, null, null, null, null, null);
    }

    /**
     * Create standard context with root object
     * 使用根对象创建标准上下文
     *
     * @param rootObject the root object | 根对象
     */
    public StandardContext(Object rootObject) {
        this(rootObject, null, null, null, null, null);
    }

    /**
     * Create standard context with full customization
     * 使用完整自定义创建标准上下文
     *
     * @param rootObject the root object | 根对象
     * @param functionRegistry the function registry | 函数注册表
     * @param propertyAccessors the property accessors | 属性访问器
     * @param typeConverter the type converter | 类型转换器
     * @param sandbox the security sandbox | 安全沙箱
     * @param parent the parent context | 父上下文
     */
    public StandardContext(Object rootObject, FunctionRegistry functionRegistry,
                           List<PropertyAccessor> propertyAccessors, TypeConverter typeConverter,
                           Sandbox sandbox, EvaluationContext parent) {
        this.rootObject = rootObject;
        this.variables = new ConcurrentHashMap<>();
        this.functionRegistry = functionRegistry != null ? functionRegistry : FunctionRegistry.create();
        this.propertyAccessors = propertyAccessors != null ? new ArrayList<>(propertyAccessors) : new ArrayList<>();
        this.typeConverter = typeConverter;
        this.sandbox = sandbox;
        this.parent = parent;

        // Add default property accessors
        if (this.propertyAccessors.isEmpty()) {
            this.propertyAccessors.add(new MapPropertyAccessor());
            this.propertyAccessors.add(new BeanPropertyAccessor());
        }
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
        if (name == null) {
            return null;
        }
        Object value = variables.get(name);
        if (value == null && parent != null && !variables.containsKey(name)) {
            return parent.getVariable(name);
        }
        return value;
    }

    @Override
    public void setVariable(String name, Object value) {
        if (name != null) {
            variables.put(name, value);
        }
    }

    @Override
    public boolean hasVariable(String name) {
        if (name == null) {
            return false;
        }
        if (variables.containsKey(name)) {
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
        allVars.putAll(variables);
        return Collections.unmodifiableMap(allVars);
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
        return typeConverter;
    }

    @Override
    public Sandbox getSandbox() {
        return sandbox;
    }

    @Override
    public EvaluationContext createChild() {
        return new StandardContext(rootObject, functionRegistry, propertyAccessors, typeConverter, sandbox, this);
    }

    /**
     * Add a property accessor
     * 添加属性访问器
     *
     * @param accessor the accessor | 访问器
     * @return this context for chaining | 用于链式调用的上下文
     */
    public StandardContext addPropertyAccessor(PropertyAccessor accessor) {
        if (accessor != null) {
            propertyAccessors.addFirst(accessor);
        }
        return this;
    }

    /**
     * Create a builder for StandardContext
     * 创建StandardContext的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for StandardContext
     * StandardContext构建器
     */
    public static class Builder {
        private Object rootObject;
        private FunctionRegistry functionRegistry;
        private final List<PropertyAccessor> propertyAccessors = new ArrayList<>();
        private TypeConverter typeConverter;
        private Sandbox sandbox;

        /**
         * Set root object
         * 设置根对象
         *
         * @param rootObject the root object | 根对象
         * @return this builder | 此构建器
         */
        public Builder rootObject(Object rootObject) {
            this.rootObject = rootObject;
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
         * Add property accessor
         * 添加属性访问器
         *
         * @param accessor the accessor | 访问器
         * @return this builder | 此构建器
         */
        public Builder addPropertyAccessor(PropertyAccessor accessor) {
            if (accessor != null) {
                this.propertyAccessors.add(accessor);
            }
            return this;
        }

        /**
         * Set type converter
         * 设置类型转换器
         *
         * @param converter the converter | 转换器
         * @return this builder | 此构建器
         */
        public Builder typeConverter(TypeConverter converter) {
            this.typeConverter = converter;
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
        public StandardContext build() {
            return new StandardContext(rootObject, functionRegistry,
                    propertyAccessors.isEmpty() ? null : propertyAccessors,
                    typeConverter, sandbox, null);
        }
    }

    /**
     * Map Property Accessor
     * Map属性访问器
     */
    private static class MapPropertyAccessor implements PropertyAccessor {
        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return new Class<?>[]{Map.class};
        }

        @Override
        public boolean canRead(Object target, String name) {
            return target instanceof Map<?, ?> m && m.containsKey(name);
        }

        @Override
        public Object read(Object target, String name) {
            if (target instanceof Map<?, ?> m) {
                return m.get(name);
            }
            return null;
        }

        @Override
        public boolean canWrite(Object target, String name) {
            return target instanceof Map;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void write(Object target, String name, Object value) {
            if (target instanceof Map m) {
                m.put(name, value);
            }
        }
    }

    /**
     * Bean Property Accessor
     * Bean属性访问器
     */
    private static class BeanPropertyAccessor implements PropertyAccessor {

        private static final System.Logger LOG = System.getLogger(BeanPropertyAccessor.class.getName());

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return null; // All types
        }

        @Override
        public boolean canRead(Object target, String name) {
            if (target == null || name == null || name.isEmpty()) {
                return false;
            }
            try {
                return findGetter(target.getClass(), name) != null;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public Object read(Object target, String name) {
            if (target == null || name == null || name.isEmpty()) {
                return null;
            }
            try {
                java.lang.reflect.Method getter = findGetter(target.getClass(), name);
                if (getter != null) {
                    if (java.lang.reflect.Modifier.isPublic(getter.getModifiers())) {
                        // setAccessible for public methods is needed for cross-module
                        // scenarios (e.g., public methods on package-private classes),
                        // not for bypassing access control
                        getter.setAccessible(true);
                        return getter.invoke(target);
                    }
                    LOG.log(System.Logger.Level.WARNING,
                        "Skipping non-public getter ''{0}'' on {1}",
                        getter.getName(), target.getClass().getName());
                    return null;
                }
                // Try direct field access - only allow public fields
                java.lang.reflect.Field field = findField(target.getClass(), name);
                if (field != null) {
                    if (java.lang.reflect.Modifier.isPublic(field.getModifiers())) {
                        return field.get(target);
                    }
                    LOG.log(System.Logger.Level.WARNING,
                        "Skipping non-public field ''{0}'' on {1}",
                        field.getName(), target.getClass().getName());
                    return null;
                }
            } catch (Exception e) {
                LOG.log(System.Logger.Level.WARNING,
                    "Failed to read property ''{0}'' from {1}: {2}",
                    name, target.getClass().getName(), e.getMessage());
            }
            return null;
        }

        private java.lang.reflect.Method findGetter(Class<?> clazz, String name) {
            if (name == null || name.isEmpty()) {
                return null;
            }
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
