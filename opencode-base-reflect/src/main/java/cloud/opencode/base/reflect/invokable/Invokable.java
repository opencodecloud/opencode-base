package cloud.opencode.base.reflect.invokable;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import cloud.opencode.base.reflect.type.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Invokable - Fluent Wrapper for Method/Constructor (Similar to Guava Invokable)
 * 可调用 - 方法/构造器的流畅包装（对标 Guava Invokable）
 *
 * <p>Provides a unified, fluent API for invoking methods and constructors
 * with type safety and rich metadata access.</p>
 * <p>为调用方法和构造器提供统一、流畅的API，具有类型安全和丰富的元数据访问。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified method/constructor API - 统一的方法/构造器API</li>
 *   <li>Type-safe invocation - 类型安全调用</li>
 *   <li>Modifier inspection - 修饰符检查</li>
 *   <li>Annotation access - 注解访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // From Method
 * Invokable<Service, String> invokable = Invokable.from(method);
 * String result = invokable.invoke(service, args);
 *
 * // From Constructor
 * Invokable<User, User> ctor = Invokable.from(constructor);
 * User user = ctor.invoke(null, "name", 25);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (wrapped method/constructor must be non-null) - 空值安全: 否（包装的方法/构造器须非空）</li>
 * </ul>
 *
 * @param <T> the declaring class type | 声明类类型
 * @param <R> the return type | 返回类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public abstract class Invokable<T, R> implements AnnotatedElement {

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates an Invokable from a Method
     * 从Method创建Invokable
     *
     * @param method the method | 方法
     * @param <T>    the declaring class type | 声明类类型
     * @return Invokable instance | Invokable实例
     */
    @SuppressWarnings("unchecked")
    public static <T> Invokable<T, Object> from(Method method) {
        return new MethodInvokable<>(method);
    }

    /**
     * Creates an Invokable from a Constructor
     * 从Constructor创建Invokable
     *
     * @param constructor the constructor | 构造器
     * @param <T>         the type | 类型
     * @return Invokable instance | Invokable实例
     */
    public static <T> Invokable<T, T> from(Constructor<T> constructor) {
        return new ConstructorInvokable<>(constructor);
    }

    // ==================== Abstract Methods | 抽象方法 ====================

    /**
     * Gets the underlying accessible object
     * 获取底层可访问对象
     *
     * @return the accessible object | 可访问对象
     */
    protected abstract AccessibleObject getAccessibleObject();

    /**
     * Gets the declaring class as TypeToken
     * 获取声明类的TypeToken
     *
     * @return TypeToken of declaring class | 声明类的TypeToken
     */
    public abstract TypeToken<T> getDeclaringClass();

    /**
     * Gets the return type as TypeToken
     * 获取返回类型的TypeToken
     *
     * @return TypeToken of return type | 返回类型的TypeToken
     */
    public abstract TypeToken<? extends R> getReturnType();

    /**
     * Gets the parameter list
     * 获取参数列表
     *
     * @return list of Parameters | 参数列表
     */
    public abstract List<Parameter> getParameters();

    /**
     * Gets the parameter types
     * 获取参数类型
     *
     * @return list of parameter TypeTokens | 参数TypeToken列表
     */
    public abstract List<TypeToken<?>> getParameterTypes();

    /**
     * Gets the exception types
     * 获取异常类型
     *
     * @return list of exception TypeTokens | 异常TypeToken列表
     */
    public abstract List<TypeToken<? extends Throwable>> getExceptionTypes();

    /**
     * Gets the type parameters
     * 获取类型参数
     *
     * @return array of type variables | 类型变量数组
     */
    public abstract TypeVariable<?>[] getTypeParameters();

    /**
     * Invokes the method/constructor
     * 调用方法/构造器
     *
     * @param receiver the receiver object | 接收对象
     * @param args     the arguments | 参数
     * @return the result | 结果
     * @throws InvocationTargetException if invocation fails | 如果调用失败
     * @throws IllegalAccessException    if access denied | 如果访问被拒绝
     */
    public abstract R invoke(T receiver, Object... args) throws InvocationTargetException, IllegalAccessException;

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Invokes ignoring access modifiers
     * 调用（忽略访问修饰符）
     *
     * @param receiver the receiver object | 接收对象
     * @param args     the arguments | 参数
     * @return the result | 结果
     */
    @SuppressWarnings("unchecked")
    public R invokeForced(T receiver, Object... args) {
        try {
            getAccessibleObject().setAccessible(true);
            return invoke(receiver, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            if (cause instanceof Error err) {
                throw err;
            }
            throw new OpenReflectException("Invocation failed", cause);
        } catch (IllegalAccessException e) {
            throw new OpenReflectException("Access denied", e);
        }
    }

    /**
     * Safely invokes returning Optional
     * 安全调用返回Optional
     *
     * @param receiver the receiver object | 接收对象
     * @param args     the arguments | 参数
     * @return Optional of result | 结果的Optional
     */
    public Optional<R> invokeSafe(T receiver, Object... args) {
        try {
            return Optional.ofNullable(invokeForced(receiver, args));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Sets accessible flag
     * 设置可访问标志
     *
     * @param flag the flag | 标志
     * @return this | 当前对象
     */
    public Invokable<T, R> setAccessible(boolean flag) {
        getAccessibleObject().setAccessible(flag);
        return this;
    }

    // ==================== Modifier Methods | 修饰符方法 ====================

    /**
     * Checks if public
     * 检查是否为public
     *
     * @return true if public | 如果是public返回true
     */
    public boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    /**
     * Checks if protected
     * 检查是否为protected
     *
     * @return true if protected | 如果是protected返回true
     */
    public boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }

    /**
     * Checks if private
     * 检查是否为private
     *
     * @return true if private | 如果是private返回true
     */
    public boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    /**
     * Checks if package-private
     * 检查是否为包私有
     *
     * @return true if package-private | 如果是包私有返回true
     */
    public boolean isPackagePrivate() {
        return !isPublic() && !isProtected() && !isPrivate();
    }

    /**
     * Checks if static
     * 检查是否为static
     *
     * @return true if static | 如果是static返回true
     */
    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    /**
     * Checks if final
     * 检查是否为final
     *
     * @return true if final | 如果是final返回true
     */
    public boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    /**
     * Checks if abstract
     * 检查是否为abstract
     *
     * @return true if abstract | 如果是abstract返回true
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    /**
     * Checks if native
     * 检查是否为native
     *
     * @return true if native | 如果是native返回true
     */
    public boolean isNative() {
        return Modifier.isNative(getModifiers());
    }

    /**
     * Checks if synchronized
     * 检查是否为synchronized
     *
     * @return true if synchronized | 如果是synchronized返回true
     */
    public boolean isSynchronized() {
        return Modifier.isSynchronized(getModifiers());
    }

    /**
     * Checks if varargs
     * 检查是否为可变参数
     *
     * @return true if varargs | 如果是可变参数返回true
     */
    public abstract boolean isVarArgs();

    /**
     * Checks if synthetic
     * 检查是否为合成
     *
     * @return true if synthetic | 如果是合成返回true
     */
    public abstract boolean isSynthetic();

    /**
     * Gets the name
     * 获取名称
     *
     * @return the name | 名称
     */
    public abstract String getName();

    /**
     * Gets the modifiers
     * 获取修饰符
     *
     * @return the modifiers | 修饰符
     */
    public abstract int getModifiers();

    /**
     * Gets the declaring class as raw Class
     * 获取声明类的原始Class
     *
     * @return the declaring class | 声明类
     */
    public abstract Class<?> getDeclaringClassRaw();

    /**
     * Checks if overridable (not final, not private, not static)
     * 检查是否可重写（非final、非private、非static）
     *
     * @return true if overridable | 如果可重写返回true
     */
    public boolean isOverridable() {
        return !isFinal() && !isPrivate() && !isStatic();
    }

    // ==================== AnnotatedElement Implementation | AnnotatedElement实现 ====================

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAccessibleObject().isAnnotationPresent(annotationClass);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return getAccessibleObject().getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return getAccessibleObject().getAnnotations();
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        return getAccessibleObject().getAnnotationsByType(annotationClass);
    }

    @Override
    public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        return getAccessibleObject().getDeclaredAnnotation(annotationClass);
    }

    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        return getAccessibleObject().getDeclaredAnnotationsByType(annotationClass);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getAccessibleObject().getDeclaredAnnotations();
    }
}
