package cloud.opencode.base.captcha.store;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Redis Captcha Store - Redis-based CAPTCHA storage
 * Redis验证码存储 - 基于Redis的验证码存储
 *
 * <p>Thread-safe Redis implementation of CaptchaStore using functional interfaces
 * for Redis operations, allowing integration with any Redis client.</p>
 * <p>使用函数式接口进行Redis操作的线程安全Redis验证码存储实现，
 * 允许与任何Redis客户端集成。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pluggable Redis client integration via functional interfaces - 通过函数式接口的可插拔 Redis 客户端集成</li>
 *   <li>Configurable key prefix for namespace isolation - 可配置键前缀用于命名空间隔离</li>
 *   <li>Automatic TTL-based expiration via Redis - 通过 Redis 自动基于 TTL 过期</li>
 *   <li>Support for Spring Data Redis, Jedis, Lettuce, etc. - 支持 Spring Data Redis、Jedis、Lettuce 等</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * // With Spring Data Redis
 * StringRedisTemplate redisTemplate = ...;
 * RedisCaptchaStore store = RedisCaptchaStore.builder()
 *     .keyPrefix("captcha:")
 *     .setter((key, value, ttl) ->
 *         redisTemplate.opsForValue().set(key, value, ttl))
 *     .getter(key ->
 *         redisTemplate.opsForValue().get(key))
 *     .deleter(key ->
 *         redisTemplate.delete(key))
 *     .build();
 *
 * // With Jedis
 * JedisPool pool = ...;
 * RedisCaptchaStore store = RedisCaptchaStore.builder()
 *     .keyPrefix("captcha:")
 *     .setter((key, value, ttl) -> {
 *         try (Jedis jedis = pool.getResource()) {
 *             jedis.setex(key, ttl.toSeconds(), value);
 *         }
 *     })
 *     .getter(key -> {
 *         try (Jedis jedis = pool.getResource()) {
 *             return jedis.get(key);
 *         }
 *     })
 *     .deleter(key -> {
 *         try (Jedis jedis = pool.getResource()) {
 *             jedis.del(key);
 *         }
 *     })
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe Redis operations) - 线程安全: 是（委托给线程安全的 Redis 操作）</li>
 *   <li>Null-safe: No (setter, getter, deleter must be non-null) - 空值安全: 否（setter、getter、deleter 不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class RedisCaptchaStore implements CaptchaStore {

    private static final String DEFAULT_KEY_PREFIX = "captcha:";

    private final String keyPrefix;
    private final RedisSetter setter;
    private final Function<String, String> getter;
    private final Consumer<String> deleter;
    private final Function<String, Boolean> existsChecker;

    /**
     * Functional interface for Redis SET with TTL operation.
     * 用于带TTL的Redis SET操作的函数式接口。
     */
    @FunctionalInterface
    public interface RedisSetter {
        /**
         * Sets a value with TTL.
         * 设置带TTL的值。
         *
         * @param key   the key | 键
         * @param value the value | 值
         * @param ttl   the time to live | 存活时间
         */
        void set(String key, String value, Duration ttl);
    }

    private RedisCaptchaStore(Builder builder) {
        this.keyPrefix = builder.keyPrefix;
        this.setter = Objects.requireNonNull(builder.setter, "setter is required");
        this.getter = Objects.requireNonNull(builder.getter, "getter is required");
        this.deleter = Objects.requireNonNull(builder.deleter, "deleter is required");
        this.existsChecker = builder.existsChecker != null ? builder.existsChecker : key -> getter.apply(key) != null;
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void store(String id, String answer, Duration ttl) {
        String key = buildKey(id);
        setter.set(key, answer, ttl);
    }

    @Override
    public Optional<String> get(String id) {
        String key = buildKey(id);
        String value = getter.apply(key);
        return Optional.ofNullable(value);
    }

    @Override
    public Optional<String> getAndRemove(String id) {
        String key = buildKey(id);
        String value = getter.apply(key);
        if (value != null) {
            deleter.accept(key);
        }
        return Optional.ofNullable(value);
    }

    @Override
    public void remove(String id) {
        String key = buildKey(id);
        deleter.accept(key);
    }

    @Override
    public boolean exists(String id) {
        String key = buildKey(id);
        return existsChecker.apply(key);
    }

    @Override
    public void clearExpired() {
        // Redis handles TTL-based expiration automatically
        // Redis自动处理基于TTL的过期
    }

    @Override
    public void clearAll() {
        // This operation requires SCAN with pattern matching
        // For safety, this is a no-op in the base implementation
        // Users should implement via Redis SCAN if needed
        // 此操作需要带模式匹配的SCAN命令
        // 为安全起见，基础实现中这是空操作
        // 用户如需要应通过Redis SCAN实现
    }

    @Override
    public int size() {
        // Cannot efficiently determine size without SCAN
        // Return -1 to indicate unknown
        // 没有SCAN无法高效确定大小
        // 返回-1表示未知
        return -1;
    }

    /**
     * Gets the key prefix.
     * 获取键前缀。
     *
     * @return the key prefix | 键前缀
     */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    private String buildKey(String id) {
        return keyPrefix + id;
    }

    /**
     * Builder for RedisCaptchaStore.
     * RedisCaptchaStore的构建器。
     */
    public static class Builder {
        private String keyPrefix = DEFAULT_KEY_PREFIX;
        private RedisSetter setter;
        private Function<String, String> getter;
        private Consumer<String> deleter;
        private Function<String, Boolean> existsChecker;

        /**
         * Sets the key prefix.
         * 设置键前缀。
         *
         * @param keyPrefix the key prefix | 键前缀
         * @return this builder | 此构建器
         */
        public Builder keyPrefix(String keyPrefix) {
            this.keyPrefix = Objects.requireNonNull(keyPrefix, "keyPrefix cannot be null");
            return this;
        }

        /**
         * Sets the Redis setter function.
         * 设置Redis setter函数。
         *
         * @param setter the setter | setter
         * @return this builder | 此构建器
         */
        public Builder setter(RedisSetter setter) {
            this.setter = setter;
            return this;
        }

        /**
         * Sets the Redis getter function.
         * 设置Redis getter函数。
         *
         * @param getter the getter | getter
         * @return this builder | 此构建器
         */
        public Builder getter(Function<String, String> getter) {
            this.getter = getter;
            return this;
        }

        /**
         * Sets the Redis deleter function.
         * 设置Redis deleter函数。
         *
         * @param deleter the deleter | deleter
         * @return this builder | 此构建器
         */
        public Builder deleter(Consumer<String> deleter) {
            this.deleter = deleter;
            return this;
        }

        /**
         * Sets the Redis exists checker function.
         * 设置Redis exists检查函数。
         *
         * @param existsChecker the exists checker | exists检查器
         * @return this builder | 此构建器
         */
        public Builder existsChecker(Function<String, Boolean> existsChecker) {
            this.existsChecker = existsChecker;
            return this;
        }

        /**
         * Builds the RedisCaptchaStore.
         * 构建RedisCaptchaStore。
         *
         * @return the store | 存储
         */
        public RedisCaptchaStore build() {
            return new RedisCaptchaStore(this);
        }
    }
}
