package cloud.opencode.base.deepclone.cloner;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;
import cloud.opencode.base.deepclone.strategy.FieldCloneStrategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflection-based deep cloner
 * 基于反射的深度克隆器
 *
 * <p>Uses Java reflection to access and copy all fields of an object.
 * Supports any object type without requiring Serializable interface.</p>
 * <p>使用Java反射访问和复制对象的所有字段。
 * 支持任何对象类型，无需实现Serializable接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>No Serializable requirement - 无需Serializable</li>
 *   <li>Annotation-based field control - 基于注解的字段控制</li>
 *   <li>Field caching for performance - 字段缓存提升性能</li>
 *   <li>Transient field handling - transient字段处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ReflectiveCloner cloner = ReflectiveCloner.create();
 * User cloned = cloner.clone(originalUser);
 *
 * // With configuration
 * ReflectiveCloner configured = ReflectiveCloner.create(
 *     new ReflectiveConfig(true, true, true)
 * );
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, field cache uses ConcurrentHashMap) - 线程安全: 是（无状态，字段缓存使用ConcurrentHashMap）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public final class ReflectiveCloner extends AbstractCloner {

    /**
     * Maximum cache size to prevent unbounded growth
     * 最大缓存大小，防止无限增长
     */
    private static final int MAX_CACHE_SIZE = 1024;

    /**
     * Field cache (bounded LRU via access-order LinkedHashMap wrapped in ConcurrentHashMap)
     * 字段缓存（通过访问顺序 LinkedHashMap 实现有界 LRU）
     */
    private static final Map<Class<?>, Field[]> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * Constructor cache (bounded)
     * 构造器缓存（有界）
     */
    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    /**
     * Configuration
     * 配置
     */
    private final ReflectiveConfig config;

    private ReflectiveCloner(ReflectiveConfig config) {
        this.config = config;
        this.cloneTransient = config.cloneTransient();
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a ReflectiveCloner with default configuration
     * 使用默认配置创建ReflectiveCloner
     *
     * @return the cloner | 克隆器
     */
    public static ReflectiveCloner create() {
        return new ReflectiveCloner(ReflectiveConfig.defaults());
    }

    /**
     * Creates a ReflectiveCloner with specific configuration
     * 使用指定配置创建ReflectiveCloner
     *
     * @param config the configuration | 配置
     * @return the cloner | 克隆器
     */
    public static ReflectiveCloner create(ReflectiveConfig config) {
        return new ReflectiveCloner(config);
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
     * Clones a regular object using reflection
     * 使用反射克隆普通对象
     */
    @SuppressWarnings("unchecked")
    private <T> T cloneObject(T original, CloneContext context) {
        Class<?> type = original.getClass();

        // Create new instance
        T clone = (T) createInstance(type);
        context.registerCloned(original, clone);

        // Copy all fields
        Field[] fields = getFields(type);
        for (Field field : fields) {
            copyField(field, original, clone, context);
        }

        return clone;
    }

    /**
     * Creates a new instance of a class
     * 创建类的新实例
     */
    private Object createInstance(Class<?> type) {
        try {
            // Check cache first (only stores non-null constructors)
            Constructor<?> constructor = CONSTRUCTOR_CACHE.get(type);
            if (constructor != null) {
                return constructor.newInstance();
            }

            // Try to find and cache default constructor
            try {
                Constructor<?> ctor = type.getDeclaredConstructor();
                ctor.setAccessible(true);
                // Evict an entry if cache is full before adding
                if (CONSTRUCTOR_CACHE.size() >= MAX_CACHE_SIZE) {
                    var it = CONSTRUCTOR_CACHE.keySet().iterator();
                    if (it.hasNext()) {
                        CONSTRUCTOR_CACHE.remove(it.next());
                    }
                }
                CONSTRUCTOR_CACHE.put(type, ctor);
                return ctor.newInstance();
            } catch (NoSuchMethodException e) {
                // No default constructor - try Unsafe
            }

            // Try Unsafe if no default constructor
            if (UnsafeCloner.isAvailable()) {
                return UnsafeCloner.allocateInstanceStatic(type);
            }

            throw new OpenDeepCloneException(type, null,
                    "No default constructor and Unsafe not available");
        } catch (OpenDeepCloneException e) {
            throw e;
        } catch (Exception e) {
            throw OpenDeepCloneException.instantiationFailed(type, e);
        }
    }

    /**
     * Gets all fields for a class (including inherited)
     * 获取类的所有字段（包括继承的）
     */
    private Field[] getFields(Class<?> type) {
        if (!config.useFieldCache()) {
            return getAllFields(type);
        }
        Field[] cached = FIELD_CACHE.computeIfAbsent(type, this::getAllFields);
        // Trim cache if over limit (best-effort, no lock needed)
        // 超过限制时修剪缓存（尽力而为，无需加锁）
        if (FIELD_CACHE.size() > MAX_CACHE_SIZE) {
            var it = FIELD_CACHE.keySet().iterator();
            if (it.hasNext()) {
                FIELD_CACHE.remove(it.next());
            }
        }
        return cached;
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
     */
    private void copyField(Field field, Object source, Object target, CloneContext context) {
        try {
            // Determine strategy from annotations
            FieldCloneStrategy strategy = config.respectAnnotations()
                    ? FieldCloneStrategy.fromAnnotations(field)
                    : FieldCloneStrategy.DEEP;

            Object value = field.get(source);

            Object clonedValue = switch (strategy) {
                case DEEP -> clone(value, context);
                case SHALLOW -> value;
                case IGNORE, NULL -> null;
            };

            field.set(target, clonedValue);
        } catch (IllegalAccessException e) {
            throw OpenDeepCloneException.fieldAccessFailed(field.getName(), source.getClass(), e);
        }
    }

    /**
     * Clears the static field and constructor caches.
     * Useful for reclaiming memory or after bulk cloning operations.
     * 清除静态字段和构造器缓存。
     * 适用于回收内存或批量克隆操作后。
     */
    public static void clearCaches() {
        FIELD_CACHE.clear();
        CONSTRUCTOR_CACHE.clear();
    }

    @Override
    public String getStrategyName() {
        return "reflective";
    }

    @Override
    public boolean supports(Class<?> type) {
        return true;
    }

    /**
     * Configuration for ReflectiveCloner
     * ReflectiveCloner的配置
     *
     * @param cloneTransient     whether to clone transient fields | 是否克隆transient字段
     * @param useFieldCache      whether to cache field metadata | 是否缓存字段元数据
     * @param respectAnnotations whether to respect clone annotations | 是否遵守克隆注解
     */
    public record ReflectiveConfig(
            boolean cloneTransient,
            boolean useFieldCache,
            boolean respectAnnotations
    ) {
        /**
         * Creates default configuration
         * 创建默认配置
         *
         * @return the default config | 默认配置
         */
        public static ReflectiveConfig defaults() {
            return new ReflectiveConfig(false, true, true);
        }
    }
}
