package cloud.opencode.base.reflect.accessor;

import cloud.opencode.base.reflect.OpenField;
import cloud.opencode.base.reflect.OpenMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Property Accessors Factory
 * 属性访问器工厂
 *
 * <p>Factory for creating property accessors with different strategies.</p>
 * <p>用不同策略创建属性访问器的工厂。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple access strategies (Field, Bean, MethodHandle, VarHandle, Lambda) - 多种访问策略</li>
 *   <li>Auto-strategy selection - 自动策略选择</li>
 *   <li>Bulk accessor creation - 批量访问器创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Auto-select best strategy
 * PropertyAccessor<User> accessor = PropertyAccessors.create(User.class, "name");
 *
 * // Create all accessors for a class
 * Map<String, PropertyAccessor<User>> accessors = PropertyAccessors.createAll(User.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless factory) - 线程安全: 是（无状态工厂）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class PropertyAccessors {

    private PropertyAccessors() {
    }

    /**
     * Access strategy enumeration
     * 访问策略枚举
     */
    public enum Strategy {
        /**
         * Use field reflection
         * 使用字段反射
         */
        FIELD,
        /**
         * Use getter/setter methods
         * 使用getter/setter方法
         */
        BEAN,
        /**
         * Use MethodHandle (fast after warmup)
         * 使用MethodHandle（预热后快）
         */
        METHOD_HANDLE,
        /**
         * Use VarHandle (for atomic ops)
         * 使用VarHandle（用于原子操作）
         */
        VAR_HANDLE,
        /**
         * Use LambdaMetafactory (fastest after warmup)
         * 使用LambdaMetafactory（预热后最快）
         */
        LAMBDA,
        /**
         * Auto-select best strategy
         * 自动选择最佳策略
         */
        AUTO
    }

    /**
     * Creates a property accessor with auto strategy
     * 以自动策略创建属性访问器
     *
     * @param clazz        the class | 类
     * @param propertyName the property name | 属性名
     * @param <T>          the target type | 目标类型
     * @return the accessor | 访问器
     */
    public static <T> PropertyAccessor<T> create(Class<T> clazz, String propertyName) {
        return create(clazz, propertyName, Strategy.AUTO);
    }

    /**
     * Creates a property accessor with specified strategy
     * 以指定策略创建属性访问器
     *
     * @param clazz        the class | 类
     * @param propertyName the property name | 属性名
     * @param strategy     the strategy | 策略
     * @param <T>          the target type | 目标类型
     * @return the accessor | 访问器
     */
    public static <T> PropertyAccessor<T> create(Class<T> clazz, String propertyName, Strategy strategy) {
        return switch (strategy) {
            case FIELD -> FieldAccessor.of(clazz, propertyName);
            case BEAN -> BeanAccessor.of(clazz, propertyName);
            case METHOD_HANDLE -> MethodHandleAccessor.of(clazz, propertyName);
            case VAR_HANDLE -> VarHandleAccessor.of(clazz, propertyName);
            case LAMBDA -> LambdaAccessor.of(clazz, propertyName);
            case AUTO -> createAuto(clazz, propertyName);
        };
    }

    /**
     * Creates all property accessors for a class
     * 为类创建所有属性访问器
     *
     * @param clazz the class | 类
     * @param <T>   the target type | 目标类型
     * @return map of property name to accessor | 属性名到访问器的映射
     */
    public static <T> Map<String, PropertyAccessor<T>> createAll(Class<T> clazz) {
        return createAll(clazz, Strategy.AUTO);
    }

    /**
     * Creates all property accessors for a class with strategy
     * 以策略为类创建所有属性访问器
     *
     * @param clazz    the class | 类
     * @param strategy the strategy | 策略
     * @param <T>      the target type | 目标类型
     * @return map of property name to accessor | 属性名到访问器的映射
     */
    public static <T> Map<String, PropertyAccessor<T>> createAll(Class<T> clazz, Strategy strategy) {
        Map<String, PropertyAccessor<T>> result = new LinkedHashMap<>();
        Set<String> propertyNames = new LinkedHashSet<>();

        // Collect from fields
        for (Field field : OpenField.getAllFields(clazz)) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                propertyNames.add(field.getName());
            }
        }

        // Collect from getters
        for (Method method : OpenMethod.getGetters(clazz)) {
            String name = extractPropertyName(method.getName());
            if (name != null) {
                propertyNames.add(name);
            }
        }

        // Create accessors
        for (String name : propertyNames) {
            try {
                PropertyAccessor<T> accessor = create(clazz, name, strategy);
                result.put(name, accessor);
            } catch (Exception ignored) {
                // Skip properties that can't be accessed
            }
        }

        return result;
    }

    /**
     * Creates field-based accessors for all fields
     * 为所有字段创建基于字段的访问器
     *
     * @param clazz the class | 类
     * @param <T>   the target type | 目标类型
     * @return map of field name to accessor | 字段名到访问器的映射
     */
    public static <T> Map<String, FieldAccessor<T>> createFieldAccessors(Class<T> clazz) {
        Map<String, FieldAccessor<T>> result = new LinkedHashMap<>();
        for (Field field : OpenField.getAllFields(clazz)) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                result.put(field.getName(), new FieldAccessor<>(field));
            }
        }
        return result;
    }

    /**
     * Creates bean-based accessors for all properties
     * 为所有属性创建基于bean的访问器
     *
     * @param clazz the class | 类
     * @param <T>   the target type | 目标类型
     * @return map of property name to accessor | 属性名到访问器的映射
     */
    public static <T> Map<String, BeanAccessor<T>> createBeanAccessors(Class<T> clazz) {
        Map<String, BeanAccessor<T>> result = new LinkedHashMap<>();

        for (Method method : OpenMethod.getGetters(clazz)) {
            String name = extractPropertyName(method.getName());
            if (name != null && !result.containsKey(name)) {
                try {
                    result.put(name, BeanAccessor.of(clazz, name));
                } catch (Exception ignored) {
                }
            }
        }

        return result;
    }

    /**
     * Creates VarHandle-based accessors for all fields
     * 为所有字段创建基于VarHandle的访问器
     *
     * @param clazz the class | 类
     * @param <T>   the target type | 目标类型
     * @return map of field name to accessor | 字段名到访问器的映射
     */
    public static <T> Map<String, VarHandleAccessor<T>> createVarHandleAccessors(Class<T> clazz) {
        Map<String, VarHandleAccessor<T>> result = new LinkedHashMap<>();
        for (Field field : OpenField.getAllFields(clazz)) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                try {
                    result.put(field.getName(), VarHandleAccessor.fromField(field));
                } catch (Exception ignored) {
                }
            }
        }
        return result;
    }

    private static <T> PropertyAccessor<T> createAuto(Class<T> clazz, String propertyName) {
        // Try LambdaAccessor first (fastest after warmup)
        try {
            LambdaAccessor<T> lambdaAccessor = LambdaAccessor.of(clazz, propertyName);
            if (lambdaAccessor.isReadable() || lambdaAccessor.isWritable()) {
                return lambdaAccessor;
            }
        } catch (Exception ignored) {
        }

        // Try bean accessor (preferred for public API)
        try {
            BeanAccessor<T> beanAccessor = BeanAccessor.of(clazz, propertyName);
            if (beanAccessor.isReadable() || beanAccessor.isWritable()) {
                return beanAccessor;
            }
        } catch (Exception ignored) {
        }

        // Fall back to field accessor
        try {
            return FieldAccessor.of(clazz, propertyName);
        } catch (Exception ignored) {
        }

        // Try MethodHandle as last resort
        return MethodHandleAccessor.of(clazz, propertyName);
    }

    private static String extractPropertyName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return decapitalize(methodName.substring(2));
        }
        return null;
    }

    private static String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.length() > 1 && Character.isUpperCase(str.charAt(1))) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
