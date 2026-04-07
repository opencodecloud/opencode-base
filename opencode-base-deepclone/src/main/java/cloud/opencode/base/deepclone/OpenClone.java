package cloud.opencode.base.deepclone;

import cloud.opencode.base.deepclone.cloner.ReflectiveCloner;
import cloud.opencode.base.deepclone.cloner.SerializingCloner;
import cloud.opencode.base.deepclone.cloner.UnsafeCloner;
import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Facade class for deep cloning operations
 * 深度克隆操作的门面类
 *
 * <p>Provides a simple, unified API for deep cloning objects using various strategies.
 * This is the main entry point for all cloning operations.</p>
 * <p>提供简单统一的API，使用各种策略进行对象深度克隆。这是所有克隆操作的主入口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single-object cloning - 单对象克隆</li>
 *   <li>Batch cloning - 批量克隆</li>
 *   <li>Parallel cloning with virtual threads - 虚拟线程并行克隆</li>
 *   <li>Async cloning - 异步克隆</li>
 *   <li>Multiple cloning strategies - 多种克隆策略</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple cloning
 * User cloned = OpenClone.clone(originalUser);
 *
 * // Batch cloning
 * List<User> clonedList = OpenClone.cloneBatch(userList);
 *
 * // Parallel cloning
 * List<User> parallel = OpenClone.cloneBatchParallel(userList, 4);
 *
 * // Async cloning
 * CompletableFuture<User> future = OpenClone.cloneAsync(user);
 *
 * // Custom cloner
 * Cloner custom = OpenClone.builder()
 *     .reflective()
 *     .maxDepth(50)
 *     .build();
 * User cloned = custom.clone(user);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public final class OpenClone {

    /**
     * Default cloner (reflective)
     */
    private static final Cloner DEFAULT_CLONER = ReflectiveCloner.create();

    /**
     * Immutable types registry
     */
    private static final java.util.Set<Class<?>> CUSTOM_IMMUTABLE_TYPES =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    private OpenClone() {
        // Utility class
    }

    // ==================== Single Object Cloning | 单对象克隆 ====================

    /**
     * Deep clones an object using the default reflective strategy
     * 使用默认反射策略深度克隆对象
     *
     * @param original the original object | 原始对象
     * @param <T>      the object type | 对象类型
     * @return the cloned object | 克隆的对象
     */
    public static <T> T clone(T original) {
        return DEFAULT_CLONER.clone(original);
    }

    /**
     * Deep clones using serialization strategy
     * 使用序列化策略深度克隆
     *
     * @param original the original object (must be Serializable) | 原始对象（必须是Serializable）
     * @param <T>      the object type | 对象类型
     * @return the cloned object | 克隆的对象
     */
    public static <T> T cloneBySerialization(T original) {
        return SerializingCloner.create().clone(original);
    }

    /**
     * Deep clones using Unsafe strategy (high performance)
     * 使用Unsafe策略深度克隆（高性能）
     *
     * @param original the original object | 原始对象
     * @param <T>      the object type | 对象类型
     * @return the cloned object | 克隆的对象
     */
    public static <T> T cloneByUnsafe(T original) {
        return UnsafeCloner.create().clone(original);
    }

    /**
     * Deep clones using a specific cloner
     * 使用指定克隆器深度克隆
     *
     * @param original the original object | 原始对象
     * @param cloner   the cloner to use | 要使用的克隆器
     * @param <T>      the object type | 对象类型
     * @return the cloned object | 克隆的对象
     */
    public static <T> T clone(T original, Cloner cloner) {
        return cloner.clone(original);
    }

    // ==================== Batch Cloning | 批量克隆 ====================

    /**
     * Clones a list of objects
     * 克隆对象列表
     *
     * @param originals the original objects | 原始对象列表
     * @param <T>       the object type | 对象类型
     * @return the cloned objects | 克隆的对象列表
     */
    public static <T> List<T> cloneBatch(List<T> originals) {
        if (originals == null || originals.isEmpty()) {
            return originals == null ? null : new ArrayList<>();
        }

        List<T> cloned = new ArrayList<>(originals.size());
        for (T original : originals) {
            cloned.add(clone(original));
        }
        return cloned;
    }

    /**
     * Clones a list of objects in parallel using virtual threads
     * 使用虚拟线程并行克隆对象列表
     *
     * @param originals   the original objects | 原始对象列表
     * @param parallelism the parallelism level (unused, virtual threads auto-scale) | 并行度（未使用，虚拟线程自动扩展）
     * @param <T>         the object type | 对象类型
     * @return the cloned objects | 克隆的对象列表
     */
    public static <T> List<T> cloneBatchParallel(List<T> originals, int parallelism) {
        if (originals == null || originals.isEmpty()) {
            return originals == null ? null : new ArrayList<>();
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<T>> futures = originals.stream()
                    .map(obj -> CompletableFuture.supplyAsync(
                            () -> clone(obj),
                            executor
                    ))
                    .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        }
    }

    // ==================== Async Cloning | 异步克隆 ====================

    /**
     * Asynchronously clones an object
     * 异步克隆对象
     *
     * @param original the original object | 原始对象
     * @param <T>      the object type | 对象类型
     * @return a future containing the cloned object | 包含克隆对象的Future
     */
    public static <T> CompletableFuture<T> cloneAsync(T original) {
        return CompletableFuture.supplyAsync(() -> clone(original));
    }

    /**
     * Asynchronously clones a batch of objects
     * 异步批量克隆对象
     *
     * @param originals the original objects | 原始对象列表
     * @param <T>       the object type | 对象类型
     * @return a future containing the cloned objects | 包含克隆对象的Future
     */
    public static <T> CompletableFuture<List<T>> cloneBatchAsync(List<T> originals) {
        return CompletableFuture.supplyAsync(() -> cloneBatch(originals));
    }

    // ==================== Shallow Clone | 浅拷贝 ====================

    /**
     * Shallow clones an object (copies field references without deep cloning)
     * 浅拷贝对象（复制字段引用，不进行深度克隆）
     *
     * @param original the original object | 原始对象
     * @param <T>      the object type | 对象类型
     * @return the shallow cloned object | 浅拷贝的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T shallowClone(T original) {
        if (original == null) {
            return null;
        }
        try {
            Class<?> type = original.getClass();
            T clone = (T) createNewInstance(type);
            Class<?> current = type;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isStatic(modifiers) || field.isSynthetic()) {
                        continue;
                    }
                    field.setAccessible(true);
                    field.set(clone, field.get(original));
                }
                current = current.getSuperclass();
            }
            return clone;
        } catch (Exception e) {
            throw new OpenDeepCloneException("Shallow clone failed for type: " + original.getClass().getName(), e);
        }
    }

    // ==================== Copy To | 合并复制 ====================

    /**
     * Copies all non-null fields from source to an existing target object
     * 将源对象的所有非 null 字段复制到已有的目标对象
     *
     * @param source the source object | 源对象
     * @param target the target object | 目标对象
     * @param <T>    the object type | 对象类型
     * @return the target object with copied fields | 复制了字段的目标对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T copyTo(T source, T target) {
        if (source == null || target == null) {
            return target;
        }
        try {
            Class<?> type = source.getClass();
            Class<?> current = type;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || field.isSynthetic()) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = field.get(source);
                    if (value != null) {
                        field.set(target, DEFAULT_CLONER.clone(value));
                    }
                }
                current = current.getSuperclass();
            }
            return target;
        } catch (Exception e) {
            throw new OpenDeepCloneException("CopyTo failed: " + e.getMessage(), e);
        }
    }

    // ==================== Policy-based Clone | 策略克隆 ====================

    /**
     * Deep clones with a specific clone policy
     * 使用指定策略进行深度克隆
     *
     * @param original the original object | 原始对象
     * @param policy   the clone policy | 克隆策略
     * @param <T>      the object type | 对象类型
     * @return the cloned object | 克隆的对象
     */
    public static <T> T cloneWith(T original, ClonePolicy policy) {
        if (original == null) {
            return null;
        }
        Cloner cloner = builder().policy(policy).build();
        return cloner.clone(original);
    }

    // ==================== Internal | 内部方法 ====================

    /**
     * Creates a new instance, trying default constructor first then Unsafe
     * 创建新实例，优先尝试默认构造器再回退到 Unsafe
     */
    private static Object createNewInstance(Class<?> type) throws Exception {
        try {
            var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException e) {
            if (UnsafeCloner.isAvailable()) {
                return UnsafeCloner.allocateInstanceStatic(type);
            }
            throw new OpenDeepCloneException("No default constructor and Unsafe not available for: " + type.getName());
        }
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Checks if a type is immutable
     * 检查类型是否不可变
     *
     * @param type the type | 类型
     * @return true if immutable | 如果不可变返回true
     */
    public static boolean isImmutable(Class<?> type) {
        if (type == null || type.isPrimitive() || type.isEnum()) {
            return true;
        }
        return CUSTOM_IMMUTABLE_TYPES.contains(type);
    }

    /**
     * Registers types as immutable
     * 注册类型为不可变
     *
     * @param types the types | 类型
     */
    public static void registerImmutable(Class<?>... types) {
        for (Class<?> type : types) {
            CUSTOM_IMMUTABLE_TYPES.add(type);
        }
    }

    /**
     * Gets the default cloner
     * 获取默认克隆器
     *
     * @return the default cloner | 默认克隆器
     */
    public static Cloner getDefaultCloner() {
        return DEFAULT_CLONER;
    }

    /**
     * Creates a cloner builder
     * 创建克隆器构建器
     *
     * @return the builder | 构建器
     */
    public static ClonerBuilder builder() {
        return new ClonerBuilder();
    }
}
