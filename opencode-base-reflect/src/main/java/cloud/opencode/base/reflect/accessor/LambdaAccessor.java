package cloud.opencode.base.reflect.accessor;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Lambda-based Property Accessor (via LambdaMetafactory)
 * 基于Lambda的属性访问器（通过LambdaMetafactory）
 *
 * <p>Uses {@link LambdaMetafactory} to generate {@link Function} (getter) and
 * {@link BiConsumer} (setter) for near-zero-cost property access after warmup.
 * This is the fastest reflective access strategy available.</p>
 * <p>使用 {@link LambdaMetafactory} 生成 {@link Function}（getter）和
 * {@link BiConsumer}（setter），预热后实现接近零成本的属性访问。
 * 这是可用的最快反射访问策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>LambdaMetafactory-based near-native access - 基于LambdaMetafactory的接近原生访问</li>
 *   <li>Getter/setter method discovery with field fallback - getter/setter方法发现及字段回退</li>
 *   <li>Immutable and thread-safe after construction - 构造后不可变且线程安全</li>
 *   <li>Cached lambda for repeated invocations - 缓存lambda用于重复调用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LambdaAccessor<User> accessor = LambdaAccessor.of(User.class, "name");
 * String name = (String) accessor.get(user);
 * accessor.set(user, "Alice");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (caller must ensure non-null target) - 空值安全: 否（调用方须确保非空目标）</li>
 * </ul>
 *
 * @param <T> the target type | 目标类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
public class LambdaAccessor<T> implements PropertyAccessor<T> {

    private final String name;
    private final Class<T> declaringClass;
    private final Class<?> type;
    private final Type genericType;
    private final Function<Object, Object> getter;
    private final BiConsumer<Object, Object> setter;

    private LambdaAccessor(String name, Class<T> declaringClass, Class<?> type,
                           Type genericType, Function<Object, Object> getter,
                           BiConsumer<Object, Object> setter) {
        this.name = name;
        this.declaringClass = declaringClass;
        this.type = type;
        this.genericType = genericType;
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * Creates a LambdaAccessor for a property by name
     * 按名称为属性创建LambdaAccessor
     *
     * <p>Tries getter/setter methods first (getXxx/isXxx/setXxx), then falls back
     * to direct field access via MethodHandle.</p>
     * <p>优先尝试getter/setter方法（getXxx/isXxx/setXxx），然后回退到
     * 通过MethodHandle的直接字段访问。</p>
     *
     * @param clazz        the class | 类
     * @param propertyName the property name | 属性名
     * @param <T>          the target type | 目标类型
     * @return the accessor | 访问器
     */
    public static <T> LambdaAccessor<T> of(Class<T> clazz, String propertyName) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        Objects.requireNonNull(propertyName, "propertyName must not be null");
        if (propertyName.isBlank()) {
            throw new OpenReflectException("propertyName must not be blank");
        }

        // Resolve the field for type info (may be null if only methods exist)
        Field field = findField(clazz, propertyName);
        Method getterMethod = findGetter(clazz, propertyName);
        Method setterMethod = findSetter(clazz, propertyName,
                getterMethod != null ? getterMethod.getReturnType()
                        : (field != null ? field.getType() : null));

        if (getterMethod == null && setterMethod == null && field == null) {
            throw OpenReflectException.fieldNotFound(clazz, propertyName);
        }

        // Determine type and genericType
        Class<?> type;
        Type genericType;
        if (getterMethod != null) {
            type = getterMethod.getReturnType();
            genericType = getterMethod.getGenericReturnType();
        } else if (field != null) {
            type = field.getType();
            genericType = field.getGenericType();
        } else {
            type = setterMethod.getParameterTypes()[0];
            genericType = setterMethod.getGenericParameterTypes()[0];
        }

        Function<Object, Object> getterFn = buildGetter(clazz, getterMethod, field);
        BiConsumer<Object, Object> setterFn = buildSetter(clazz, setterMethod, field);

        return new LambdaAccessor<>(propertyName, clazz, type, genericType, getterFn, setterFn);
    }

    // ==================== PropertyAccessor Implementation | 接口实现 ====================

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Type getGenericType() {
        return genericType;
    }

    @Override
    public Class<T> getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public boolean isReadable() {
        return getter != null;
    }

    @Override
    public boolean isWritable() {
        return setter != null;
    }

    @Override
    public Object get(T target) {
        if (getter == null) {
            throw new IllegalStateException("Property is not readable: " + name);
        }
        Objects.requireNonNull(target, "target must not be null");
        return getter.apply(target);
    }

    @Override
    public void set(T target, Object value) {
        if (setter == null) {
            throw new IllegalStateException("Property is not writable: " + name);
        }
        Objects.requireNonNull(target, "target must not be null");
        setter.accept(target, value);
    }

    // ==================== Lambda Construction | Lambda构建 ====================

    /**
     * Builds a getter Function using LambdaMetafactory, falling back to MethodHandle
     * 使用LambdaMetafactory构建getter Function，回退到MethodHandle
     */
    @SuppressWarnings("unchecked")
    private static <T> Function<Object, Object> buildGetter(Class<T> clazz, Method getterMethod, Field field) {
        // Strategy 1: use getter method with LambdaMetafactory
        if (getterMethod != null) {
            try {
                MethodHandles.Lookup lookup = acquireLookup(clazz);
                MethodHandle mh = lookup.unreflect(getterMethod);
                return createGetterLambda(lookup, mh, clazz, getterMethod.getReturnType());
            } catch (Exception e) {
                // LambdaMetafactory failed, fall back to MethodHandle wrapping
                try {
                    getterMethod.setAccessible(true);
                    MethodHandles.Lookup lookup = acquireLookup(clazz);
                    MethodHandle mh = lookup.unreflect(getterMethod);
                    return wrapGetterHandle(mh);
                } catch (Exception ex) {
                    throw new OpenReflectException(clazz, getterMethod.getName(), "buildGetter",
                            "Failed to create getter for method: " + getterMethod.getName(), ex);
                }
            }
        }

        // Strategy 2: use field directly via MethodHandle
        if (field != null) {
            try {
                field.setAccessible(true);
                MethodHandles.Lookup lookup = acquireLookup(clazz);
                MethodHandle mh = lookup.unreflectGetter(field);
                return wrapGetterHandle(mh);
            } catch (Exception e) {
                throw new OpenReflectException("Failed to create getter for field: " + field.getName(), e);
            }
        }

        return null;
    }

    /**
     * Builds a setter BiConsumer using LambdaMetafactory, falling back to MethodHandle
     * 使用LambdaMetafactory构建setter BiConsumer，回退到MethodHandle
     */
    @SuppressWarnings("unchecked")
    private static <T> BiConsumer<Object, Object> buildSetter(Class<T> clazz, Method setterMethod, Field field) {
        // Strategy 1: use setter method with LambdaMetafactory
        if (setterMethod != null) {
            try {
                MethodHandles.Lookup lookup = acquireLookup(clazz);
                MethodHandle mh = lookup.unreflect(setterMethod);
                return createSetterLambda(lookup, mh, clazz, setterMethod.getParameterTypes()[0]);
            } catch (Exception e) {
                // LambdaMetafactory failed, fall back to MethodHandle wrapping
                try {
                    setterMethod.setAccessible(true);
                    MethodHandles.Lookup lookup = acquireLookup(clazz);
                    MethodHandle mh = lookup.unreflect(setterMethod);
                    return wrapSetterHandle(mh);
                } catch (Exception ex) {
                    throw new OpenReflectException(clazz, setterMethod.getName(), "buildSetter",
                            "Failed to create setter for method: " + setterMethod.getName(), ex);
                }
            }
        }

        // Strategy 2: use field directly via MethodHandle (skip final fields)
        if (field != null && !Modifier.isFinal(field.getModifiers())) {
            try {
                field.setAccessible(true);
                MethodHandles.Lookup lookup = acquireLookup(clazz);
                MethodHandle mh = lookup.unreflectSetter(field);
                return wrapSetterHandle(mh);
            } catch (Exception e) {
                throw new OpenReflectException("Failed to create setter for field: " + field.getName(), e);
            }
        }

        return null;
    }

    /**
     * Creates a getter lambda via LambdaMetafactory
     * 通过LambdaMetafactory创建getter lambda
     */
    @SuppressWarnings("unchecked")
    private static Function<Object, Object> createGetterLambda(MethodHandles.Lookup lookup,
                                                                MethodHandle mh,
                                                                Class<?> ownerClass,
                                                                Class<?> returnType) throws Exception {
        MethodType invokedType = MethodType.methodType(Function.class);
        MethodType samMethodType = MethodType.methodType(Object.class, Object.class);
        MethodType instantiatedMethodType = MethodType.methodType(box(returnType), ownerClass);

        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "apply",
                invokedType,
                samMethodType,
                mh,
                instantiatedMethodType
        );

        try {
            return (Function<Object, Object>) callSite.getTarget().invoke();
        } catch (Throwable t) {
            throw new OpenReflectException("Failed to create getter lambda", t);
        }
    }

    /**
     * Creates a setter lambda via LambdaMetafactory
     * 通过LambdaMetafactory创建setter lambda
     */
    @SuppressWarnings("unchecked")
    private static BiConsumer<Object, Object> createSetterLambda(MethodHandles.Lookup lookup,
                                                                  MethodHandle mh,
                                                                  Class<?> ownerClass,
                                                                  Class<?> paramType) throws Exception {
        MethodType invokedType = MethodType.methodType(BiConsumer.class);
        MethodType samMethodType = MethodType.methodType(void.class, Object.class, Object.class);
        MethodType instantiatedMethodType = MethodType.methodType(void.class, ownerClass, box(paramType));

        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "accept",
                invokedType,
                samMethodType,
                mh,
                instantiatedMethodType
        );

        try {
            return (BiConsumer<Object, Object>) callSite.getTarget().invoke();
        } catch (Throwable t) {
            throw new OpenReflectException("Failed to create setter lambda", t);
        }
    }

    /**
     * Wraps a MethodHandle as a getter Function (fallback)
     * 将MethodHandle包装为getter Function（回退方案）
     */
    private static Function<Object, Object> wrapGetterHandle(MethodHandle mh) {
        return target -> {
            try {
                return mh.invoke(target);
            } catch (Throwable e) {
                throw new OpenReflectException("Failed to invoke getter MethodHandle", e);
            }
        };
    }

    /**
     * Wraps a MethodHandle as a setter BiConsumer (fallback)
     * 将MethodHandle包装为setter BiConsumer（回退方案）
     */
    private static BiConsumer<Object, Object> wrapSetterHandle(MethodHandle mh) {
        return (target, value) -> {
            try {
                mh.invoke(target, value);
            } catch (Throwable e) {
                throw new OpenReflectException("Failed to invoke setter MethodHandle", e);
            }
        };
    }

    // ==================== Lookup Acquisition | Lookup获取 ====================

    /**
     * Acquires a Lookup for the target class, trying privateLookupIn first
     * 获取目标类的Lookup，优先尝试privateLookupIn
     */
    private static MethodHandles.Lookup acquireLookup(Class<?> clazz) throws IllegalAccessException {
        try {
            return MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            // Fall back to the caller's own lookup
            return MethodHandles.lookup();
        }
    }

    // ==================== Reflection Helpers | 反射辅助 ====================

    private static Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static Method findGetter(Class<?> clazz, String propertyName) {
        String capitalized = capitalize(propertyName);

        // Try getXxx
        try {
            Method method = clazz.getMethod("get" + capitalized);
            if (method.getReturnType() != void.class) {
                return method;
            }
        } catch (NoSuchMethodException ignored) {
        }

        // Try isXxx for boolean
        try {
            Method method = clazz.getMethod("is" + capitalized);
            if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
                return method;
            }
        } catch (NoSuchMethodException ignored) {
        }

        return null;
    }

    private static Method findSetter(Class<?> clazz, String propertyName, Class<?> type) {
        String setterName = "set" + capitalize(propertyName);

        // If we know the type, try exact match
        if (type != null) {
            try {
                return clazz.getMethod(setterName, type);
            } catch (NoSuchMethodException ignored) {
            }
        }

        // Search for any setter with that name
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                return method;
            }
        }

        return null;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Boxes a primitive type to its wrapper; returns the type unchanged if not primitive
     * 将基本类型装箱为包装类型；如果不是基本类型则原样返回
     */
    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        if (type == void.class) return Void.class;
        return type;
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LambdaAccessor<?> that)) return false;
        return name.equals(that.name) && declaringClass.equals(that.declaringClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, declaringClass);
    }

    @Override
    public String toString() {
        return "LambdaAccessor[" + declaringClass.getSimpleName() + "." + name + "]";
    }
}
