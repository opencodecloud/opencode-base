package cloud.opencode.base.reflect.invokable;

import cloud.opencode.base.reflect.type.TypeToken;

import java.lang.reflect.*;
import java.util.*;

/**
 * Constructor Invokable Implementation
 * 构造器Invokable实现
 *
 * <p>Implementation of Invokable for Constructor objects.</p>
 * <p>Constructor对象的Invokable实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Constructor wrapping as Invokable - 将构造器包装为Invokable</li>
 *   <li>Type-safe constructor invocation - 类型安全的构造器调用</li>
 *   <li>Parameter and annotation access - 参数和注解访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Invokable<User, User> invokable = Invokable.from(constructor);
 * User user = invokable.invoke(null, "Alice", 25);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (constructor must be non-null) - 空值安全: 否（构造器须非空）</li>
 * </ul>
 *
 * @param <T> the type | 类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class ConstructorInvokable<T> extends Invokable<T, T> {

    private final Constructor<T> constructor;
    private final TypeToken<T> declaringClass;

    ConstructorInvokable(Constructor<T> constructor) {
        this.constructor = Objects.requireNonNull(constructor, "constructor must not be null");
        this.declaringClass = TypeToken.of(constructor.getDeclaringClass());
    }

    /**
     * Gets the underlying Constructor
     * 获取底层Constructor
     *
     * @return the Constructor | 构造器
     */
    public Constructor<T> getConstructor() {
        return constructor;
    }

    @Override
    protected AccessibleObject getAccessibleObject() {
        return constructor;
    }

    @Override
    public TypeToken<T> getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public Class<?> getDeclaringClassRaw() {
        return constructor.getDeclaringClass();
    }

    @Override
    public TypeToken<T> getReturnType() {
        return declaringClass;
    }

    @Override
    public List<Parameter> getParameters() {
        java.lang.reflect.Parameter[] params = constructor.getParameters();
        List<Parameter> result = new ArrayList<>(params.length);
        for (int i = 0; i < params.length; i++) {
            result.add(new Parameter(params[i], i));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<TypeToken<?>> getParameterTypes() {
        Type[] types = constructor.getGenericParameterTypes();
        List<TypeToken<?>> result = new ArrayList<>(types.length);
        for (Type type : types) {
            result.add(TypeToken.of(type));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TypeToken<? extends Throwable>> getExceptionTypes() {
        Type[] types = constructor.getGenericExceptionTypes();
        List<TypeToken<? extends Throwable>> result = new ArrayList<>(types.length);
        for (Type type : types) {
            result.add((TypeToken<? extends Throwable>) TypeToken.of(type));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public TypeVariable<?>[] getTypeParameters() {
        return constructor.getTypeParameters();
    }

    @Override
    public T invoke(T receiver, Object... args) throws InvocationTargetException, IllegalAccessException {
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException e) {
            throw new IllegalAccessException("Cannot instantiate: " + e.getMessage());
        }
    }

    @Override
    public boolean isVarArgs() {
        return constructor.isVarArgs();
    }

    @Override
    public boolean isSynthetic() {
        return constructor.isSynthetic();
    }

    // ==================== Member Implementation | Member实现 ====================

    @Override
    public String getName() {
        return constructor.getName();
    }

    @Override
    public int getModifiers() {
        return constructor.getModifiers();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ConstructorInvokable<?> other)) return false;
        return constructor.equals(other.constructor);
    }

    @Override
    public int hashCode() {
        return constructor.hashCode();
    }

    @Override
    public String toString() {
        return "ConstructorInvokable[" + constructor + "]";
    }
}
