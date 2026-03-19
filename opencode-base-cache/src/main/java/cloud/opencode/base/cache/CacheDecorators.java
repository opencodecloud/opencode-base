package cloud.opencode.base.cache;

import cloud.opencode.base.cache.spi.RefreshAheadPolicy;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Cache Decorators - Fluent API for chaining cache decorators
 * 缓存装饰器 - 用于链式组合缓存装饰器的流式 API
 *
 * <p>Provides a fluent builder pattern for composing multiple cache decorators
 * in a clean, readable way.</p>
 * <p>提供流式构建器模式，以清晰易读的方式组合多个缓存装饰器。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Chain multiple decorators
 * Cache<String, User> decorated = CacheDecorators.chain(cache)
 *     .withProtection()                    // Add BloomFilter + SingleFlight
 *     .withRefreshAhead(policy, loader)    // Add proactive refresh
 *     .withTimeout(Duration.ofSeconds(5))  // Add operation timeouts
 *     .withCopyOnRead(User::clone)         // Add copy-on-read
 *     .build();
 *
 * // With custom protection configuration
 * Cache<String, User> decorated = CacheDecorators.chain(cache)
 *     .withProtection(p -> p
 *         .bloomFilter(1_000_000, 0.01)
 *         .negativeCache(Duration.ofMinutes(5)))
 *     .build();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent decorator chaining API - 流式装饰器链式 API</li>
 *   <li>Protection (BloomFilter + SingleFlight) - 保护（布隆过滤器 + SingleFlight）</li>
 *   <li>Refresh-ahead support - 提前刷新支持</li>
 *   <li>Timeout wrapping - 超时包装</li>
 *   <li>Copy-on-read isolation - 读时复制隔离</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.2
 */
public final class CacheDecorators {

    private CacheDecorators() {
    }

    /**
     * Start a decorator chain for the given cache
     * 为给定的缓存开始装饰器链
     *
     * @param cache the cache to decorate | 要装饰的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return decorator chain builder | 装饰器链构建器
     */
    public static <K, V> ChainBuilder<K, V> chain(Cache<K, V> cache) {
        return new ChainBuilder<>(cache);
    }

    /**
     * Builder for chaining cache decorators
     * 用于链式组合缓存装饰器的构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static class ChainBuilder<K, V> {
        private Cache<K, V> current;

        ChainBuilder(Cache<K, V> cache) {
            this.current = Objects.requireNonNull(cache, "cache cannot be null");
        }

        /**
         * Add protection (BloomFilter + SingleFlight) with default settings
         * 添加保护（布隆过滤器 + SingleFlight）使用默认设置
         *
         * @return this builder | 此构建器
         */
        public ChainBuilder<K, V> withProtection() {
            this.current = ProtectedCache.wrap(current).build();
            return this;
        }

        /**
         * Add protection with custom configuration
         * 添加保护并自定义配置
         *
         * @param configurer configuration function | 配置函数
         * @return this builder | 此构建器
         */
        public ChainBuilder<K, V> withProtection(
                UnaryOperator<ProtectedCache.Builder<K, V>> configurer) {
            ProtectedCache.Builder<K, V> builder = ProtectedCache.wrap(current);
            this.current = configurer.apply(builder).build();
            return this;
        }

        /**
         * Add refresh-ahead behavior
         * 添加提前刷新行为
         *
         * @param policy the refresh policy | 刷新策略
         * @param loader the value loader | 值加载器
         * @return this builder | 此构建器
         */
        public ChainBuilder<K, V> withRefreshAhead(
                RefreshAheadPolicy<K, V> policy,
                Function<? super K, ? extends V> loader) {
            this.current = RefreshAheadCache.wrap(current)
                    .refreshPolicy(policy)
                    .loader(loader)
                    .build();
            return this;
        }

        /**
         * Add refresh-ahead with custom configuration
         * 添加提前刷新并自定义配置
         *
         * @param configurer configuration function | 配置函数
         * @return this builder | 此构建器
         */
        public ChainBuilder<K, V> withRefreshAhead(
                UnaryOperator<RefreshAheadCache.Builder<K, V>> configurer) {
            RefreshAheadCache.Builder<K, V> builder = RefreshAheadCache.wrap(current);
            this.current = configurer.apply(builder).build();
            return this;
        }

        /**
         * Add operation timeout
         * 添加操作超时
         *
         * @param timeout the timeout duration | 超时时长
         * @return this builder | 此构建器
         */
        public ChainBuilder<K, V> withTimeout(Duration timeout) {
            this.current = TimeoutCache.wrap(current)
                    .defaultTimeout(timeout)
                    .build();
            return this;
        }

        /**
         * Add timeout with custom configuration
         * 添加超时并自定义配置
         *
         * @param configurer configuration function | 配置函数
         * @return this builder | 此构建器
         */
        public ChainBuilder<K, V> withTimeout(
                UnaryOperator<TimeoutCache.Builder<K, V>> configurer) {
            TimeoutCache.Builder<K, V> builder = TimeoutCache.wrap(current);
            this.current = configurer.apply(builder).build();
            return this;
        }

        /**
         * Add copy-on-read behavior with default copier (serialization)
         * 添加读时复制行为，使用默认复制器（序列化）
         *
         * @return this builder | 此构建器
         */
        public ChainBuilder<K, V> withCopyOnRead() {
            this.current = CopyOnReadCache.wrap(current).build();
            return this;
        }

        /**
         * Add copy-on-read with custom copier
         * 添加读时复制并自定义复制器
         *
         * @param copier the value copier function | 值复制函数
         * @return this builder | 此构建器
         */
        public ChainBuilder<K, V> withCopyOnRead(UnaryOperator<V> copier) {
            this.current = CopyOnReadCache.wrap(current)
                    .copier(copier)
                    .build();
            return this;
        }

        /**
         * Add copy-on-read with custom configuration
         * 添加读时复制并自定义配置
         *
         * @param configurer configuration function | 配置函数
         * @return this builder | 此构建器
         */
        public ChainBuilder<K, V> withCopyOnReadConfigured(
                UnaryOperator<CopyOnReadCache.Builder<K, V>> configurer) {
            CopyOnReadCache.Builder<K, V> builder = CopyOnReadCache.wrap(current);
            this.current = configurer.apply(builder).build();
            return this;
        }

        /**
         * Apply a custom decorator
         * 应用自定义装饰器
         *
         * @param decorator the decorator function | 装饰器函数
         * @return this builder | 此构建器
         */
        public ChainBuilder<K, V> with(UnaryOperator<Cache<K, V>> decorator) {
            this.current = decorator.apply(current);
            return this;
        }

        /**
         * Build the decorated cache
         * 构建装饰后的缓存
         *
         * @return the decorated cache | 装饰后的缓存
         */
        public Cache<K, V> build() {
            return current;
        }
    }
}
