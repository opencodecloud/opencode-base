package cloud.opencode.base.deepclone.cloner;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;
import cloud.opencode.base.deepclone.strategy.FieldCloneStrategy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unsafe-based high-performance deep cloner
 * 基于Unsafe的高性能深度克隆器
 *
 * <p>Uses sun.misc.Unsafe for direct memory operations. Creates instances
 * without calling constructors and copies fields directly.</p>
 * <p>使用sun.misc.Unsafe进行直接内存操作。创建实例时不调用构造函数，直接复制字段。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Highest performance - 最高性能</li>
 *   <li>No constructor invocation - 不调用构造函数</li>
 *   <li>Direct memory field copy - 直接内存字段复制</li>
 * </ul>
 *
 * <p><strong>Limitations | 限制:</strong></p>
 * <ul>
 *   <li>Uses internal API (sun.misc.Unsafe) - 使用内部API</li>
 *   <li>May not work in all JVM environments - 可能不在所有JVM环境工作</li>
 *   <li>Bypasses constructor logic - 绕过构造函数逻辑</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * if (UnsafeCloner.isAvailable()) {
 *     UnsafeCloner cloner = UnsafeCloner.create();
 *     User cloned = cloner.clone(originalUser);
 * }
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, field cache uses synchronizedMap) - 线程安全: 是（无状态，字段缓存使用synchronizedMap）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public final class UnsafeCloner extends AbstractCloner {

    /**
     * MethodHandle for Unsafe.allocateInstance(Class)
     */
    private static final MethodHandle ALLOCATE_INSTANCE;

    /**
     * Whether Unsafe is available
     */
    private static final boolean AVAILABLE;

    /**
     * Maximum cache size to prevent unbounded memory growth.
     * 最大缓存大小，防止无限制内存增长。
     */
    private static final int MAX_CACHE_SIZE = 10_000;

    /**
     * Field cache (bounded LRU)
     * 字段缓存（有界 LRU）
     */
    private static final Map<Class<?>, Field[]> FIELD_CACHE =
            Collections.synchronizedMap(new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Class<?>, Field[]> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            });

    static {
        MethodHandle mh = null;
        boolean available = false;
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);
            mh = MethodHandles.lookup()
                    .findVirtual(unsafeClass, "allocateInstance",
                            MethodType.methodType(Object.class, Class.class))
                    .bindTo(unsafe);
            available = true;
        } catch (Exception e) {
            // Unsafe not available
        }
        ALLOCATE_INSTANCE = mh;
        AVAILABLE = available;
    }

    private UnsafeCloner() {
        if (!AVAILABLE) {
            throw new OpenDeepCloneException("Unsafe is not available in this JVM");
        }
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates an UnsafeCloner
     * 创建UnsafeCloner
     *
     * @return the cloner | 克隆器
     * @throws OpenDeepCloneException if Unsafe is not available | 如果Unsafe不可用
     */
    public static UnsafeCloner create() {
        return new UnsafeCloner();
    }

    /**
     * Checks if Unsafe is available
     * 检查Unsafe是否可用
     *
     * @return true if available | 如果可用返回true
     */
    public static boolean isAvailable() {
        return AVAILABLE;
    }

    /**
     * Allocates an instance without calling constructor (static utility)
     * 不调用构造函数分配实例（静态工具方法）
     *
     * @param type the type | 类型
     * @param <T>  the type parameter | 类型参数
     * @return the new instance | 新实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T allocateInstanceStatic(Class<T> type) {
        if (!AVAILABLE) {
            throw new OpenDeepCloneException("Unsafe is not available");
        }
        try {
            return (T) ALLOCATE_INSTANCE.invoke(type);
        } catch (Throwable e) {
            throw OpenDeepCloneException.instantiationFailed(type, e);
        }
    }

    // ==================== Clone Implementation | 克隆实现 ====================

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doClone(T original, CloneContext context) {
        Class<?> type = original.getClass();

        // Handle arrays
        if (type.isArray()) {
            return (T) cloneArray(original, context);
        }

        // Handle collections
        if (original instanceof Collection<?> collection) {
            return (T) cloneCollection(collection, context);
        }

        // Handle maps
        if (original instanceof Map<?, ?> map) {
            return (T) cloneMap(map, context);
        }

        // Handle records
        if (type.isRecord()) {
            return (T) recordHandler.clone((java.lang.Record) original, this, context);
        }

        // Clone regular object
        return cloneObject(original, context);
    }

    /**
     * Clones a regular object using Unsafe
     * 使用Unsafe克隆普通对象
     */
    @SuppressWarnings("unchecked")
    private <T> T cloneObject(T original, CloneContext context) {
        Class<?> type = original.getClass();

        // Allocate instance without constructor
        T clone = allocateInstance((Class<T>) type);
        context.registerCloned(original, clone);

        // Copy all fields
        Field[] fields = getFields(type);
        for (Field field : fields) {
            copyField(original, clone, field, context);
        }

        return clone;
    }

    /**
     * Allocates an instance without calling constructor
     * 不调用构造函数分配实例
     *
     * @param type the type | 类型
     * @param <T>  the type parameter | 类型参数
     * @return the new instance | 新实例
     */
    protected <T> T allocateInstance(Class<T> type) {
        return allocateInstanceStatic(type);
    }

    /**
     * Gets all fields for a class
     * 获取类的所有字段
     */
    private Field[] getFields(Class<?> type) {
        return FIELD_CACHE.computeIfAbsent(type, this::getAllFields);
    }

    /**
     * Gets all fields including inherited fields
     * 获取所有字段包括继承的字段
     */
    private Field[] getAllFields(Class<?> type) {
        java.util.List<Field> fields = new java.util.ArrayList<>();
        Class<?> current = type;

        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                if (!cloneTransient && Modifier.isTransient(modifiers)) {
                    continue;
                }
                field.setAccessible(true);
                fields.add(field);
            }
            current = current.getSuperclass();
        }

        return fields.toArray(Field[]::new);
    }

    /**
     * Copies a field value from source to target
     * 从源对象复制字段值到目标对象
     *
     * @param source  the source object | 源对象
     * @param target  the target object | 目标对象
     * @param field   the field | 字段
     * @param context the context | 上下文
     */
    protected void copyField(Object source, Object target, Field field, CloneContext context) {
        try {
            // Apply FieldFilter if set
            if (fieldFilter != null && !fieldFilter.accept(field)) {
                return;
            }

            FieldCloneStrategy strategy = FieldCloneStrategy.fromAnnotations(field);

            Object value = field.get(source);

            Object clonedValue = switch (strategy) {
                case DEEP -> clone(value, context);
                case SHALLOW -> value;
                case IGNORE, NULL -> null;
            };

            field.set(target, clonedValue);
        } catch (IllegalAccessException e) {
            if (context.isLenient()) {
                context.addWarning("Field access failed: " + field.getName() + " in " + source.getClass().getName());
                return;
            }
            throw OpenDeepCloneException.fieldAccessFailed(field.getName(), source.getClass(), e);
        }
    }

    @Override
    public String getStrategyName() {
        return "unsafe";
    }

    @Override
    public boolean supports(Class<?> type) {
        return AVAILABLE;
    }
}
