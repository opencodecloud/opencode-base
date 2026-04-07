package cloud.opencode.base.deepclone;

import cloud.opencode.base.deepclone.cloner.*;
import cloud.opencode.base.deepclone.handler.TypeHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Builder for creating configured Cloner instances
 * 用于创建配置的Cloner实例的构建器
 *
 * <p>Provides a fluent API for configuring and building cloner instances
 * with custom settings, handlers, and immutable type registrations.</p>
 * <p>提供流畅的API来配置和构建具有自定义设置、处理器和不可变类型注册的克隆器实例。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Cloner cloner = OpenClone.builder()
 *     .reflective()
 *     .registerImmutable(Money.class, CustomValue.class)
 *     .registerHandler(MyType.class, new MyTypeHandler())
 *     .maxDepth(50)
 *     .cloneTransient(true)
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API - 流式构建器API</li>
 *   <li>Strategy selection (reflective, serializing, unsafe) - 策略选择</li>
 *   <li>Custom type handler registration - 自定义类型处理器注册</li>
 *   <li>Immutable type registration - 不可变类型注册</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder is not thread-safe, built Cloner is) - 线程安全: 否（构建器非线程安全，构建的克隆器是）</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(t) to build where t is the number of registered types/handlers - 时间复杂度: O(t)，t 为注册的类型/处理器数量</li>
 *   <li>Space complexity: O(t) for registered type and handler maps - 空间复杂度: O(t) 注册类型和处理器映射</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public final class ClonerBuilder {

    /**
     * Strategy type
     */
    private enum Strategy {
        REFLECTIVE, SERIALIZING, UNSAFE
    }

    private Strategy strategy = Strategy.REFLECTIVE;
    private final Set<Class<?>> immutableTypes = new HashSet<>();
    private final java.util.Map<Class<?>, TypeHandler<?>> handlers = new java.util.HashMap<>();
    private final java.util.Map<Class<?>, UnaryOperator<?>> customCloners = new java.util.HashMap<>();
    private boolean cloneTransient = false;
    private boolean useCache = true;
    private int maxDepth = 100;
    private ClonePolicy policy = ClonePolicy.STANDARD;
    private FieldFilter fieldFilter;
    private CloneListener listener;

    ClonerBuilder() {
    }

    // ==================== Strategy Selection | 策略选择 ====================

    /**
     * Uses the reflective cloning strategy (default)
     * 使用反射克隆策略（默认）
     *
     * @return this builder | 此构建器
     */
    public ClonerBuilder reflective() {
        this.strategy = Strategy.REFLECTIVE;
        return this;
    }

    /**
     * Uses the serialization cloning strategy
     * 使用序列化克隆策略
     *
     * @return this builder | 此构建器
     */
    public ClonerBuilder serializing() {
        this.strategy = Strategy.SERIALIZING;
        return this;
    }

    /**
     * Uses the Unsafe cloning strategy
     * 使用Unsafe克隆策略
     *
     * @return this builder | 此构建器
     */
    public ClonerBuilder unsafe() {
        this.strategy = Strategy.UNSAFE;
        return this;
    }

    // ==================== Configuration | 配置 ====================

    /**
     * Registers types as immutable (will not be cloned)
     * 注册类型为不可变（不会被克隆）
     *
     * @param types the types to register | 要注册的类型
     * @return this builder | 此构建器
     */
    public ClonerBuilder registerImmutable(Class<?>... types) {
        for (Class<?> type : types) {
            immutableTypes.add(type);
        }
        return this;
    }

    /**
     * Registers a custom type handler
     * 注册自定义类型处理器
     *
     * @param type    the type | 类型
     * @param handler the handler | 处理器
     * @param <T>     the type parameter | 类型参数
     * @return this builder | 此构建器
     */
    public <T> ClonerBuilder registerHandler(Class<T> type, TypeHandler<T> handler) {
        handlers.put(type, handler);
        return this;
    }

    /**
     * Registers a custom cloning function for a type
     * 为类型注册自定义克隆函数
     *
     * @param type   the type | 类型
     * @param cloner the cloning function | 克隆函数
     * @param <T>    the type parameter | 类型参数
     * @return this builder | 此构建器
     */
    public <T> ClonerBuilder registerCloner(Class<T> type, UnaryOperator<T> cloner) {
        customCloners.put(type, cloner);
        return this;
    }

    /**
     * Sets whether to clone transient fields
     * 设置是否克隆transient字段
     *
     * @param cloneTransient whether to clone | 是否克隆
     * @return this builder | 此构建器
     */
    public ClonerBuilder cloneTransient(boolean cloneTransient) {
        this.cloneTransient = cloneTransient;
        return this;
    }

    /**
     * Sets whether to use caching
     * 设置是否使用缓存
     *
     * @param useCache whether to cache | 是否缓存
     * @return this builder | 此构建器
     */
    public ClonerBuilder useCache(boolean useCache) {
        this.useCache = useCache;
        return this;
    }

    /**
     * Sets the maximum clone depth
     * 设置最大克隆深度
     *
     * @param maxDepth the max depth | 最大深度
     * @return this builder | 此构建器
     */
    public ClonerBuilder maxDepth(int maxDepth) {
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("maxDepth must be positive");
        }
        this.maxDepth = maxDepth;
        return this;
    }

    // ==================== Policy, Filter & Listener | 策略、过滤器与监听器 ====================

    /**
     * Sets the clone policy
     * 设置克隆策略
     *
     * @param policy the clone policy | 克隆策略
     * @return this builder | 此构建器
     */
    public ClonerBuilder policy(ClonePolicy policy) {
        this.policy = policy != null ? policy : ClonePolicy.STANDARD;
        return this;
    }

    /**
     * Sets the field filter for programmatic field exclusion
     * 设置编程式字段排除的字段过滤器
     *
     * @param filter the field filter | 字段过滤器
     * @return this builder | 此构建器
     */
    public ClonerBuilder filter(FieldFilter filter) {
        this.fieldFilter = filter;
        return this;
    }

    /**
     * Sets the clone listener for lifecycle events
     * 设置克隆生命周期事件的监听器
     *
     * @param listener the clone listener | 克隆监听器
     * @return this builder | 此构建器
     */
    public ClonerBuilder listener(CloneListener listener) {
        this.listener = listener;
        return this;
    }

    // ==================== Build | 构建 ====================

    /**
     * Builds the configured Cloner instance
     * 构建配置的Cloner实例
     *
     * @return the cloner | 克隆器
     */
    public Cloner build() {
        AbstractCloner cloner = switch (strategy) {
            case REFLECTIVE -> ReflectiveCloner.create(
                    new ReflectiveCloner.ReflectiveConfig(cloneTransient, useCache, true)
            );
            case SERIALIZING -> SerializingCloner.create();
            case UNSAFE -> UnsafeCloner.create();
        };

        // Configure cloner
        cloner.setMaxDepth(maxDepth);
        cloner.setCloneTransient(cloneTransient);
        cloner.setPolicy(policy);
        cloner.setFieldFilter(fieldFilter);
        cloner.setListener(listener);

        // Register immutable types
        for (Class<?> type : immutableTypes) {
            cloner.registerImmutable(type);
        }

        // Register handlers
        for (var entry : handlers.entrySet()) {
            @SuppressWarnings("unchecked")
            TypeHandler<Object> handler = (TypeHandler<Object>) entry.getValue();
            cloner.registerHandler((Class<Object>) entry.getKey(), handler);
        }

        return cloner;
    }
}
