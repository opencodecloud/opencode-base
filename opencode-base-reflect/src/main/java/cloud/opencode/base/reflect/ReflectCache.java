package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.type.TypeToken;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Reflection Cache - Centralized Cache for Reflection Results
 * 反射缓存 - 反射结果的集中缓存
 *
 * <p>Provides high-performance caching for reflection operations to avoid
 * repeated reflection lookups. Uses weak-key caching for Class-keyed entries
 * to prevent ClassLoader memory leaks in dynamic classloading environments.</p>
 * <p>为反射操作提供高性能缓存，避免重复的反射查找。
 * 对 Class 键使用弱引用缓存，防止动态类加载环境中的 ClassLoader 内存泄漏。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Field caching - 字段缓存</li>
 *   <li>Method caching - 方法缓存</li>
 *   <li>Constructor caching - 构造器缓存</li>
 *   <li>TypeToken caching - 类型令牌缓存</li>
 *   <li>Thread-safe operations - 线程安全操作</li>
 *   <li>Cache statistics - 缓存统计</li>
 *   <li>GC-aware: Class entries are released when ClassLoader is collected - GC感知: 当ClassLoader回收时自动释放</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get cached fields
 * Field[] fields = ReflectCache.getFields(MyClass.class)
 *     .orElseGet(() -> {
 *         Field[] f = MyClass.class.getDeclaredFields();
 *         ReflectCache.cacheFields(MyClass.class, f);
 *         return f;
 *     });
 *
 * // Get cache statistics
 * CacheStats stats = ReflectCache.getCacheStats();
 * System.out.println("Hit rate: " + stats.hitRate());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses synchronized WeakHashMap and ConcurrentHashMap) - 线程安全: 是</li>
 *   <li>Null-safe: Yes (null inputs are ignored) - 空值安全: 是（null输入被忽略）</li>
 *   <li>GC-safe: Class-keyed caches use weak keys to prevent ClassLoader leaks - GC安全: Class键使用弱引用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class ReflectCache {

    /**
     * Field cache (weak keys for GC-awareness)
     * 字段缓存（弱键，GC感知）
     */
    private static final Map<Class<?>, Field[]> FIELD_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>(256));

    /**
     * Method cache (weak keys for GC-awareness)
     * 方法缓存（弱键，GC感知）
     */
    private static final Map<Class<?>, Method[]> METHOD_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>(256));

    /**
     * Constructor cache (weak keys for GC-awareness)
     * 构造器缓存（弱键，GC感知）
     */
    private static final Map<Class<?>, Constructor<?>[]> CONSTRUCTOR_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>(256));

    /**
     * TypeToken cache (Type keys are not Class-loader-sensitive)
     * 类型令牌缓存（Type键不受ClassLoader影响）
     */
    private static final Map<Type, TypeToken<?>> TYPE_TOKEN_CACHE = new ConcurrentHashMap<>(256);

    // Statistics
    private static final LongAdder hits = new LongAdder();
    private static final LongAdder misses = new LongAdder();

    private ReflectCache() {
    }

    // ==================== Field Cache | 字段缓存 ====================

    /**
     * Gets cached fields for a class
     * 获取类的缓存字段
     *
     * @param clazz the class | 类
     * @return cached fields or empty | 缓存的字段或空
     */
    public static Optional<Field[]> getFields(Class<?> clazz) {
        Field[] fields = FIELD_CACHE.get(clazz);
        if (fields != null) {
            hits.increment();
            return Optional.of(fields.clone());
        }
        misses.increment();
        return Optional.empty();
    }

    /**
     * Caches fields for a class
     * 缓存类的字段
     *
     * @param clazz  the class | 类
     * @param fields the fields to cache | 要缓存的字段
     */
    public static void cacheFields(Class<?> clazz, Field[] fields) {
        if (clazz != null && fields != null) {
            FIELD_CACHE.put(clazz, fields);
        }
    }

    // ==================== Method Cache | 方法缓存 ====================

    /**
     * Gets cached methods for a class
     * 获取类的缓存方法
     *
     * @param clazz the class | 类
     * @return cached methods or empty | 缓存的方法或空
     */
    public static Optional<Method[]> getMethods(Class<?> clazz) {
        Method[] methods = METHOD_CACHE.get(clazz);
        if (methods != null) {
            hits.increment();
            return Optional.of(methods.clone());
        }
        misses.increment();
        return Optional.empty();
    }

    /**
     * Caches methods for a class
     * 缓存类的方法
     *
     * @param clazz   the class | 类
     * @param methods the methods to cache | 要缓存的方法
     */
    public static void cacheMethods(Class<?> clazz, Method[] methods) {
        if (clazz != null && methods != null) {
            METHOD_CACHE.put(clazz, methods);
        }
    }

    // ==================== Constructor Cache | 构造器缓存 ====================

    /**
     * Gets cached constructors for a class
     * 获取类的缓存构造器
     *
     * @param clazz the class | 类
     * @return cached constructors or empty | 缓存的构造器或空
     */
    public static Optional<Constructor<?>[]> getConstructors(Class<?> clazz) {
        Constructor<?>[] constructors = CONSTRUCTOR_CACHE.get(clazz);
        if (constructors != null) {
            hits.increment();
            return Optional.of(constructors.clone());
        }
        misses.increment();
        return Optional.empty();
    }

    /**
     * Caches constructors for a class
     * 缓存类的构造器
     *
     * @param clazz        the class | 类
     * @param constructors the constructors to cache | 要缓存的构造器
     */
    public static void cacheConstructors(Class<?> clazz, Constructor<?>[] constructors) {
        if (clazz != null && constructors != null) {
            CONSTRUCTOR_CACHE.put(clazz, constructors);
        }
    }

    // ==================== TypeToken Cache | 类型令牌缓存 ====================

    /**
     * Gets cached TypeToken for a type
     * 获取类型的缓存类型令牌
     *
     * @param type the type | 类型
     * @param <T>  the type parameter | 类型参数
     * @return cached TypeToken or empty | 缓存的类型令牌或空
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<TypeToken<T>> getTypeToken(Type type) {
        TypeToken<?> token = TYPE_TOKEN_CACHE.get(type);
        if (token != null) {
            hits.increment();
            return Optional.of((TypeToken<T>) token);
        }
        misses.increment();
        return Optional.empty();
    }

    /**
     * Caches TypeToken for a type
     * 缓存类型的类型令牌
     *
     * @param type  the type | 类型
     * @param token the TypeToken to cache | 要缓存的类型令牌
     * @param <T>   the type parameter | 类型参数
     */
    public static <T> void cacheTypeToken(Type type, TypeToken<T> token) {
        if (type != null && token != null) {
            TYPE_TOKEN_CACHE.put(type, token);
        }
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Clears all caches
     * 清除所有缓存
     */
    public static void clearCache() {
        FIELD_CACHE.clear();
        METHOD_CACHE.clear();
        CONSTRUCTOR_CACHE.clear();
        TYPE_TOKEN_CACHE.clear();
        hits.reset();
        misses.reset();
    }

    /**
     * Clears cache for a specific class
     * 清除特定类的缓存
     *
     * @param clazz the class to clear cache for | 要清除缓存的类
     */
    public static void clearCache(Class<?> clazz) {
        if (clazz != null) {
            FIELD_CACHE.remove(clazz);
            METHOD_CACHE.remove(clazz);
            CONSTRUCTOR_CACHE.remove(clazz);
        }
    }

    /**
     * Gets cache statistics
     * 获取缓存统计
     *
     * @return cache statistics | 缓存统计
     */
    public static CacheStats getCacheStats() {
        long hitCount = hits.sum();
        long missCount = misses.sum();
        long total = hitCount + missCount;
        double hitRate = total > 0 ? (double) hitCount / total : 0.0;

        return new CacheStats(
                hitCount,
                missCount,
                hitRate,
                FIELD_CACHE.size(),
                METHOD_CACHE.size(),
                CONSTRUCTOR_CACHE.size(),
                TYPE_TOKEN_CACHE.size()
        );
    }

    /**
     * Cache Statistics Record
     * 缓存统计记录
     *
     * @param hitCount          hit count | 命中次数
     * @param missCount         miss count | 未命中次数
     * @param hitRate           hit rate (0.0 - 1.0) | 命中率
     * @param fieldCacheSize    field cache size | 字段缓存大小
     * @param methodCacheSize   method cache size | 方法缓存大小
     * @param constructorCacheSize constructor cache size | 构造器缓存大小
     * @param typeTokenCacheSize TypeToken cache size | 类型令牌缓存大小
     */
    public record CacheStats(
            long hitCount,
            long missCount,
            double hitRate,
            int fieldCacheSize,
            int methodCacheSize,
            int constructorCacheSize,
            int typeTokenCacheSize
    ) {
        /**
         * Gets total cache size
         * 获取缓存总大小
         *
         * @return total size | 总大小
         */
        public int totalSize() {
            return fieldCacheSize + methodCacheSize + constructorCacheSize + typeTokenCacheSize;
        }

        /**
         * Gets total request count
         * 获取总请求次数
         *
         * @return total requests | 总请求次数
         */
        public long totalRequests() {
            return hitCount + missCount;
        }
    }
}
