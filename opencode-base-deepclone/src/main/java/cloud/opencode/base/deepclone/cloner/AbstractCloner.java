package cloud.opencode.base.deepclone.cloner;

import cloud.opencode.base.deepclone.*;
import cloud.opencode.base.deepclone.contract.DeepCloneable;
import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;
import cloud.opencode.base.deepclone.handler.*;
import cloud.opencode.base.deepclone.internal.ImmutableDetector;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Abstract base class for cloners (sealed hierarchy)
 * 克隆器的抽象基类（密封层次结构）
 *
 * <p>Provides common functionality for all cloner implementations including
 * immutable type detection, type handlers, and circular reference handling.</p>
 * <p>为所有克隆器实现提供通用功能，包括不可变类型检测、类型处理器和循环引用处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable type registry - 不可变类型注册</li>
 *   <li>Type handler management - 类型处理器管理</li>
 *   <li>Circular reference detection - 循环引用检测</li>
 *   <li>DeepCloneable interface support - DeepCloneable接口支持</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extend to implement custom cloning logic
 * public final class MyCloner extends AbstractCloner {
 *     @Override
 *     protected Object cloneInternal(Object obj, CloneContext ctx) {
 *         // custom clone logic
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable type registry, ConcurrentHashMap cache) - 线程安全: 是（不可变类型注册，ConcurrentHashMap缓存）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public abstract sealed class AbstractCloner implements Cloner
        permits ReflectiveCloner, SerializingCloner, UnsafeCloner {

    /**
     * Built-in immutable types
     * 内置不可变类型
     */
    private static final Set<Class<?>> BUILTIN_IMMUTABLE_TYPES = Set.of(
            // Primitives wrappers
            Boolean.class, Byte.class, Short.class, Integer.class,
            Long.class, Float.class, Double.class, Character.class,

            // String
            String.class,

            // Numbers
            BigInteger.class, BigDecimal.class,

            // Date/Time (java.time)
            LocalDate.class, LocalTime.class, LocalDateTime.class,
            Instant.class, Duration.class, Period.class,
            ZonedDateTime.class, OffsetDateTime.class, OffsetTime.class,
            Year.class, YearMonth.class, MonthDay.class, ZoneId.class, ZoneOffset.class,

            // Other
            Class.class, UUID.class, URI.class, URL.class, Pattern.class,
            Currency.class, Locale.class, TimeZone.class
    );

    /**
     * Custom immutable types
     * 自定义不可变类型
     */
    protected final Set<Class<?>> customImmutableTypes = ConcurrentHashMap.newKeySet();

    /**
     * Type handlers
     * 类型处理器
     */
    protected final Map<Class<?>, TypeHandler<?>> typeHandlers = new ConcurrentHashMap<>();

    /**
     * Default handlers
     * 默认处理器
     */
    protected final ArrayHandler arrayHandler = new ArrayHandler();
    protected final CollectionHandler collectionHandler = new CollectionHandler();
    protected final MapHandler mapHandler = new MapHandler();
    protected final RecordHandler recordHandler = new RecordHandler();
    protected final EnumHandler enumHandler = new EnumHandler();
    protected final OptionalHandler optionalHandler = new OptionalHandler();

    /**
     * Max clone depth
     * 最大克隆深度
     */
    protected volatile int maxDepth = 100;

    /**
     * Whether to clone transient fields
     * 是否克隆transient字段
     */
    protected volatile boolean cloneTransient = false;

    /**
     * Clone policy
     * 克隆策略
     */
    protected volatile ClonePolicy policy = ClonePolicy.STANDARD;

    /**
     * Field filter
     * 字段过滤器
     */
    protected volatile FieldFilter fieldFilter;

    /**
     * Clone listener
     * 克隆监听器
     */
    protected volatile CloneListener listener;

    // ==================== Cloner Interface | 克隆器接口 ====================

    @Override
    public <T> T clone(T original) {
        if (original == null) {
            return null;
        }
        return clone(original, CloneContext.create(maxDepth, policy));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T clone(T original, CloneContext context) {
        if (original == null) {
            return null;
        }

        // Check max depth
        if (context.isMaxDepthExceeded()) {
            if (context.isLenient()) {
                context.addWarning("Max depth exceeded at: " + context.getPathString());
                return original;
            }
            throw OpenDeepCloneException.maxDepthExceeded(context.getDepth(), context.getPathString());
        }

        Class<?> type = original.getClass();

        // Check for immutable types (includes enums, primitives, built-in immutables)
        if (isImmutable(type)) {
            context.incrementSkipped();
            return original;
        }

        // Check for JDK immutable collections
        if (ImmutableDetector.isImmutableCollection(original)) {
            context.incrementSkipped();
            return original;
        }

        // Check if already cloned (circular reference)
        if (context.isCloned(original)) {
            return context.getCloned(original);
        }

        // Handle Optional
        if (original instanceof java.util.Optional<?>) {
            return (T) optionalHandler.clone((java.util.Optional) original, this, context);
        }

        // Check for DeepCloneable — call the no-arg deepClone() to avoid
        // infinite recursion if the default deepClone(Cloner) delegates back to clone()
        if (original instanceof DeepCloneable<?> cloneable) {
            T cloned = (T) cloneable.deepClone();
            context.registerCloned(original, cloned);
            return cloned;
        }

        // Notify listener before clone
        if (listener != null) {
            try {
                listener.beforeClone(original, context);
            } catch (Exception e) {
                // Listener exceptions must not affect clone flow
            }
        }

        context.incrementDepth();
        try {
            T cloned = doClone(original, context);

            // Notify listener after clone
            if (listener != null) {
                try {
                    listener.afterClone(original, cloned, context);
                } catch (Exception e) {
                    // Listener exceptions must not affect clone flow
                }
            }

            return cloned;
        } catch (Exception e) {
            // Notify listener on error
            if (listener != null) {
                try {
                    listener.onError(original, e, context);
                } catch (Exception listenerEx) {
                    // Listener exceptions must not affect clone flow
                }
            }
            if (context.isLenient()) {
                context.addWarning("Clone failed for " + type.getName() + ": " + e.getMessage());
                return original;
            }
            throw e;
        } finally {
            context.decrementDepth();
        }
    }

    /**
     * Performs the actual cloning logic
     * 执行实际的克隆逻辑
     *
     * @param original the original object | 原始对象
     * @param context  the clone context | 克隆上下文
     * @param <T>      the object type | 对象类型
     * @return the cloned object | 克隆的对象
     */
    protected abstract <T> T doClone(T original, CloneContext context);

    // ==================== Immutable Type Detection | 不可变类型检测 ====================

    /**
     * Gets registered immutable types
     * 获取注册的不可变类型
     *
     * @return the immutable types | 不可变类型集合
     */
    protected Set<Class<?>> getImmutableTypes() {
        Set<Class<?>> all = new HashSet<>(BUILTIN_IMMUTABLE_TYPES);
        all.addAll(customImmutableTypes);
        return all;
    }

    /**
     * Checks if a type is immutable
     * 检查类型是否不可变
     *
     * @param type the type | 类型
     * @return true if immutable | 如果不可变返回true
     */
    protected boolean isImmutable(Class<?> type) {
        if (type == null) {
            return true;
        }
        if (type.isPrimitive()) {
            return true;
        }
        if (type.isEnum()) {
            return true;
        }
        return isBuiltinImmutable(type) || customImmutableTypes.contains(type);
    }

    /**
     * Checks if a type is a built-in immutable type
     * 检查类型是否为内置不可变类型
     *
     * @param type the type | 类型
     * @return true if built-in immutable | 如果是内置不可变返回true
     */
    protected boolean isBuiltinImmutable(Class<?> type) {
        return BUILTIN_IMMUTABLE_TYPES.contains(type);
    }

    /**
     * Registers custom immutable types
     * 注册自定义不可变类型
     *
     * @param types the types to register | 要注册的类型
     */
    public void registerImmutable(Class<?>... types) {
        customImmutableTypes.addAll(Arrays.asList(types));
    }

    // ==================== Type Handler Management | 类型处理器管理 ====================

    /**
     * Gets the type handler for a type
     * 获取类型的处理器
     *
     * @param type the type | 类型
     * @param <T>  the type parameter | 类型参数
     * @return the handler, or null if none | 处理器，如果没有则为null
     */
    @SuppressWarnings("unchecked")
    protected <T> TypeHandler<T> getHandler(Class<T> type) {
        return (TypeHandler<T>) typeHandlers.get(type);
    }

    /**
     * Registers a type handler
     * 注册类型处理器
     *
     * @param type    the type | 类型
     * @param handler the handler | 处理器
     * @param <T>     the type parameter | 类型参数
     */
    public <T> void registerHandler(Class<T> type, TypeHandler<T> handler) {
        typeHandlers.put(type, handler);
    }

    // ==================== Convenience Clone Methods | 便捷克隆方法 ====================

    /**
     * Clones an array
     * 克隆数组
     *
     * @param array   the array | 数组
     * @param context the context | 上下文
     * @return the cloned array | 克隆的数组
     */
    protected Object cloneArray(Object array, CloneContext context) {
        return arrayHandler.clone(array, this, context);
    }

    /**
     * Clones a collection
     * 克隆集合
     *
     * @param collection the collection | 集合
     * @param context    the context | 上下文
     * @param <T>        the element type | 元素类型
     * @return the cloned collection | 克隆的集合
     */
    @SuppressWarnings("unchecked")
    protected <T> Collection<T> cloneCollection(Collection<T> collection, CloneContext context) {
        return (Collection<T>) collectionHandler.clone(collection, this, context);
    }

    /**
     * Clones a map
     * 克隆Map
     *
     * @param map     the map | Map
     * @param context the context | 上下文
     * @param <K>     the key type | 键类型
     * @param <V>     the value type | 值类型
     * @return the cloned map | 克隆的Map
     */
    @SuppressWarnings("unchecked")
    protected <K, V> Map<K, V> cloneMap(Map<K, V> map, CloneContext context) {
        return (Map<K, V>) mapHandler.clone(map, this, context);
    }

    // ==================== Configuration | 配置 ====================

    /**
     * Sets the max clone depth
     * 设置最大克隆深度
     *
     * @param maxDepth the max depth | 最大深度
     */
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * Sets whether to clone transient fields
     * 设置是否克隆transient字段
     *
     * @param cloneTransient whether to clone | 是否克隆
     */
    public void setCloneTransient(boolean cloneTransient) {
        this.cloneTransient = cloneTransient;
    }

    /**
     * Sets the clone policy
     * 设置克隆策略
     *
     * @param policy the policy | 策略
     */
    public void setPolicy(ClonePolicy policy) {
        this.policy = policy != null ? policy : ClonePolicy.STANDARD;
    }

    /**
     * Sets the field filter
     * 设置字段过滤器
     *
     * @param fieldFilter the filter | 过滤器
     */
    public void setFieldFilter(FieldFilter fieldFilter) {
        this.fieldFilter = fieldFilter;
    }

    /**
     * Gets the field filter
     * 获取字段过滤器
     *
     * @return the field filter, may be null | 字段过滤器，可能为null
     */
    public FieldFilter getFieldFilter() {
        return fieldFilter;
    }

    /**
     * Sets the clone listener
     * 设置克隆监听器
     *
     * @param listener the listener | 监听器
     */
    public void setListener(CloneListener listener) {
        this.listener = listener;
    }
}
