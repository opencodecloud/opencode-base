package cloud.opencode.base.reflect.accessor;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * VarHandle-based Property Accessor
 * 基于VarHandle的属性访问器
 *
 * <p>High-performance property access using VarHandle.
 * Provides atomic operations and memory fence semantics.</p>
 * <p>使用VarHandle的高性能属性访问。
 * 提供原子操作和内存屏障语义。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>VarHandle-based high-performance access - 基于VarHandle的高性能访问</li>
 *   <li>Atomic operations (CAS, getAndSet) - 原子操作（CAS, getAndSet）</li>
 *   <li>Volatile and acquire/release semantics - Volatile和获取/释放语义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * VarHandleAccessor<Counter> accessor = VarHandleAccessor.of(Counter.class, "count");
 * accessor.compareAndSet(counter, 0, 1);
 * Object value = accessor.getVolatile(counter);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (VarHandle operations are atomic) - 线程安全: 是（VarHandle操作是原子的）</li>
 *   <li>Null-safe: No (caller must ensure non-null target for instance fields) - 空值安全: 否（实例字段须确保非空目标）</li>
 * </ul>
 *
 * @param <T> the target type | 目标类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class VarHandleAccessor<T> implements PropertyAccessor<T> {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final String name;
    private final Class<T> declaringClass;
    private final Class<?> type;
    private final Type genericType;
    private final VarHandle varHandle;
    private final boolean isFinal;
    private final boolean isStatic;

    @SuppressWarnings("unchecked")
    private VarHandleAccessor(Field field, VarHandle varHandle) {
        this.name = field.getName();
        this.declaringClass = (Class<T>) field.getDeclaringClass();
        this.type = field.getType();
        this.genericType = field.getGenericType();
        this.varHandle = varHandle;
        this.isFinal = Modifier.isFinal(field.getModifiers());
        this.isStatic = Modifier.isStatic(field.getModifiers());
    }

    /**
     * Creates a VarHandleAccessor from a field
     * 从字段创建VarHandleAccessor
     *
     * @param field the field | 字段
     * @param <T>   the target type | 目标类型
     * @return the accessor | 访问器
     */
    public static <T> VarHandleAccessor<T> fromField(Field field) {
        Objects.requireNonNull(field, "field must not be null");

        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), LOOKUP);
            VarHandle varHandle = lookup.unreflectVarHandle(field);
            return new VarHandleAccessor<>(field, varHandle);
        } catch (IllegalAccessException e) {
            throw new OpenReflectException("Failed to create VarHandle for field: " + field.getName(), e);
        }
    }

    /**
     * Creates a VarHandleAccessor for a field by name
     * 按名称为字段创建VarHandleAccessor
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @param <T>       the target type | 目标类型
     * @return the accessor | 访问器
     */
    public static <T> VarHandleAccessor<T> of(Class<T> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return fromField(field);
        } catch (NoSuchFieldException e) {
            throw OpenReflectException.fieldNotFound(clazz, fieldName);
        }
    }

    /**
     * Gets the underlying VarHandle
     * 获取底层VarHandle
     *
     * @return the VarHandle | VarHandle
     */
    public VarHandle getVarHandle() {
        return varHandle;
    }

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
        return true;
    }

    @Override
    public boolean isWritable() {
        return !isFinal;
    }

    /**
     * Checks if this is a static field
     * 检查是否为静态字段
     *
     * @return true if static | 如果是静态返回true
     */
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public Object get(T target) {
        if (isStatic) {
            return varHandle.get();
        }
        return varHandle.get(target);
    }

    @Override
    public void set(T target, Object value) {
        if (isFinal) {
            throw new IllegalStateException("Field is final: " + name);
        }
        if (isStatic) {
            varHandle.set(value);
        } else {
            varHandle.set(target, value);
        }
    }

    // ==================== Volatile Access | Volatile访问 ====================

    /**
     * Gets value with volatile semantics
     * 以volatile语义获取值
     *
     * @param target the target object | 目标对象
     * @return the value | 值
     */
    public Object getVolatile(T target) {
        if (isStatic) {
            return varHandle.getVolatile();
        }
        return varHandle.getVolatile(target);
    }

    /**
     * Sets value with volatile semantics
     * 以volatile语义设置值
     *
     * @param target the target object | 目标对象
     * @param value  the value | 值
     */
    public void setVolatile(T target, Object value) {
        if (isStatic) {
            varHandle.setVolatile(value);
        } else {
            varHandle.setVolatile(target, value);
        }
    }

    // ==================== Atomic Operations | 原子操作 ====================

    /**
     * Atomically gets and sets value
     * 原子获取并设置值
     *
     * @param target   the target object | 目标对象
     * @param newValue the new value | 新值
     * @return the previous value | 之前的值
     */
    public Object getAndSet(T target, Object newValue) {
        if (isStatic) {
            return varHandle.getAndSet(newValue);
        }
        return varHandle.getAndSet(target, newValue);
    }

    /**
     * Atomically compares and sets value
     * 原子比较并设置值
     *
     * @param target        the target object | 目标对象
     * @param expectedValue the expected value | 期望值
     * @param newValue      the new value | 新值
     * @return true if successful | 如果成功返回true
     */
    public boolean compareAndSet(T target, Object expectedValue, Object newValue) {
        if (isStatic) {
            return varHandle.compareAndSet(expectedValue, newValue);
        }
        return varHandle.compareAndSet(target, expectedValue, newValue);
    }

    /**
     * Atomically compares and exchanges value
     * 原子比较并交换值
     *
     * @param target        the target object | 目标对象
     * @param expectedValue the expected value | 期望值
     * @param newValue      the new value | 新值
     * @return the witness value | 见证值
     */
    public Object compareAndExchange(T target, Object expectedValue, Object newValue) {
        if (isStatic) {
            return varHandle.compareAndExchange(expectedValue, newValue);
        }
        return varHandle.compareAndExchange(target, expectedValue, newValue);
    }

    // ==================== Numeric Operations | 数值操作 ====================

    /**
     * Atomically adds to value (for numeric types)
     * 原子加法（用于数值类型）
     *
     * @param target the target object | 目标对象
     * @param delta  the delta | 增量
     * @return the previous value | 之前的值
     */
    public Object getAndAdd(T target, Object delta) {
        if (isStatic) {
            return varHandle.getAndAdd(delta);
        }
        return varHandle.getAndAdd(target, delta);
    }

    // ==================== Acquire/Release | 获取/释放 ====================

    /**
     * Gets value with acquire semantics
     * 以获取语义获取值
     *
     * @param target the target object | 目标对象
     * @return the value | 值
     */
    public Object getAcquire(T target) {
        if (isStatic) {
            return varHandle.getAcquire();
        }
        return varHandle.getAcquire(target);
    }

    /**
     * Sets value with release semantics
     * 以释放语义设置值
     *
     * @param target the target object | 目标对象
     * @param value  the value | 值
     */
    public void setRelease(T target, Object value) {
        if (isStatic) {
            varHandle.setRelease(value);
        } else {
            varHandle.setRelease(target, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VarHandleAccessor<?> that)) return false;
        return name.equals(that.name) && declaringClass.equals(that.declaringClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, declaringClass);
    }

    @Override
    public String toString() {
        return "VarHandleAccessor[" + declaringClass.getSimpleName() + "." + name + "]";
    }
}
