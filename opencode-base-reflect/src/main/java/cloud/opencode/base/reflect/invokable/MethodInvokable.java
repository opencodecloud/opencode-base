package cloud.opencode.base.reflect.invokable;

import cloud.opencode.base.reflect.type.TypeToken;

import java.lang.reflect.*;
import java.util.*;

/**
 * Method Invokable Implementation
 * 方法Invokable实现
 *
 * <p>Implementation of Invokable for Method objects.</p>
 * <p>Method对象的Invokable实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Method wrapping as Invokable - 将方法包装为Invokable</li>
 *   <li>Type-safe method invocation - 类型安全的方法调用</li>
 *   <li>Parameter and annotation access - 参数和注解访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Invokable<Service, Object> invokable = Invokable.from(method);
 * Object result = invokable.invoke(service, args);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (method must be non-null) - 空值安全: 否（方法须非空）</li>
 * </ul>
 *
 * @param <T> the declaring class type | 声明类类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class MethodInvokable<T> extends Invokable<T, Object> {

    private final Method method;
    private final TypeToken<T> declaringClass;

    @SuppressWarnings("unchecked")
    MethodInvokable(Method method) {
        this.method = Objects.requireNonNull(method, "method must not be null");
        this.declaringClass = (TypeToken<T>) TypeToken.of(method.getDeclaringClass());
    }

    /**
     * Gets the underlying Method
     * 获取底层Method
     *
     * @return the Method | 方法
     */
    public Method getMethod() {
        return method;
    }

    @Override
    protected AccessibleObject getAccessibleObject() {
        return method;
    }

    @Override
    public TypeToken<T> getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public Class<?> getDeclaringClassRaw() {
        return method.getDeclaringClass();
    }

    @Override
    @SuppressWarnings("unchecked")
    public TypeToken<Object> getReturnType() {
        return (TypeToken<Object>) TypeToken.of(method.getGenericReturnType());
    }

    @Override
    public List<Parameter> getParameters() {
        java.lang.reflect.Parameter[] params = method.getParameters();
        List<Parameter> result = new ArrayList<>(params.length);
        for (int i = 0; i < params.length; i++) {
            result.add(new Parameter(params[i], i));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<TypeToken<?>> getParameterTypes() {
        Type[] types = method.getGenericParameterTypes();
        List<TypeToken<?>> result = new ArrayList<>(types.length);
        for (Type type : types) {
            result.add(TypeToken.of(type));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TypeToken<? extends Throwable>> getExceptionTypes() {
        Type[] types = method.getGenericExceptionTypes();
        List<TypeToken<? extends Throwable>> result = new ArrayList<>(types.length);
        for (Type type : types) {
            result.add((TypeToken<? extends Throwable>) TypeToken.of(type));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public TypeVariable<?>[] getTypeParameters() {
        return method.getTypeParameters();
    }

    @Override
    public Object invoke(T receiver, Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(receiver, args);
    }

    @Override
    public boolean isVarArgs() {
        return method.isVarArgs();
    }

    @Override
    public boolean isSynthetic() {
        return method.isSynthetic();
    }

    /**
     * Checks if this is a bridge method
     * 检查是否为桥接方法
     *
     * @return true if bridge | 如果是桥接方法返回true
     */
    public boolean isBridge() {
        return method.isBridge();
    }

    /**
     * Checks if this is a default method
     * 检查是否为默认方法
     *
     * @return true if default | 如果是默认方法返回true
     */
    public boolean isDefault() {
        return method.isDefault();
    }

    // ==================== Member Implementation | Member实现 ====================

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public int getModifiers() {
        return method.getModifiers();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MethodInvokable<?> other)) return false;
        return method.equals(other.method);
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public String toString() {
        return "MethodInvokable[" + method + "]";
    }
}
