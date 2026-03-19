/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.cache.distributed;

import cloud.opencode.base.cache.spi.CacheSerializer;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for Distributed Cache
 * 分布式缓存配置
 *
 * <p>Provides configuration options for distributed cache implementations.</p>
 * <p>为分布式缓存实现提供配置选项。</p>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * DistributedCacheConfig config = DistributedCacheConfig.builder()
 *     .name("user-cache")
 *     .keyPrefix("user:")
 *     .defaultTtl(Duration.ofHours(1))
 *     .connectionTimeout(Duration.ofSeconds(5))
 *     .operationTimeout(Duration.ofSeconds(1))
 *     .maxRetries(3)
 *     .enableCompression(true)
 *     .compressionThreshold(1024)
 *     .build();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Connection and operation timeout configuration - 连接和操作超时配置</li>
 *   <li>Key prefix support - 键前缀支持</li>
 *   <li>Compression configuration - 压缩配置</li>
 *   <li>Retry configuration - 重试配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DistributedCacheConfig config = DistributedCacheConfig.builder()
 *     .name("users")
 *     .keyPrefix("user:")
 *     .defaultTtl(Duration.ofHours(1))
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param name the cache name | 缓存名称
 * @param keyPrefix the key prefix for namespacing | 键前缀（用于命名空间）
 * @param defaultTtl the default time-to-live | 默认过期时间
 * @param connectionTimeout the connection timeout | 连接超时时间
 * @param operationTimeout the operation timeout | 操作超时时间
 * @param maxRetries the maximum number of retries | 最大重试次数
 * @param retryBackoff the backoff duration between retries | 重试间隔
 * @param enableCompression whether to enable value compression | 是否启用值压缩
 * @param compressionThreshold the compression threshold in bytes | 压缩阈值（字节）
 * @param enableStats whether to enable statistics | 是否启用统计
 * @param enableLocalCache whether to enable local cache | 是否启用本地缓存
 * @param localCacheSize the local cache size | 本地缓存大小
 * @param localCacheTtl the local cache TTL | 本地缓存过期时间
 * @param keySerializer the key serializer | 键序列化器
 * @param valueSerializer the value serializer | 值序列化器
 * @param invalidationChannel the cache invalidation channel name | 缓存失效通道名称
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public record DistributedCacheConfig(
        String name,
        String keyPrefix,
        Duration defaultTtl,
        Duration connectionTimeout,
        Duration operationTimeout,
        int maxRetries,
        Duration retryBackoff,
        boolean enableCompression,
        int compressionThreshold,
        boolean enableStats,
        boolean enableLocalCache,
        int localCacheSize,
        Duration localCacheTtl,
        CacheSerializer<?> keySerializer,
        CacheSerializer<?> valueSerializer,
        String invalidationChannel
) {

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a default configuration with the given name.
     * 使用给定名称创建默认配置。
     *
     * @param name the cache name - 缓存名称
     * @return the config - 配置
     */
    public static DistributedCacheConfig withDefaults(String name) {
        return builder().name(name).build();
    }

    /**
     * Builder for DistributedCacheConfig.
     * DistributedCacheConfig 构建器。
     */
    public static final class Builder {
        private String name = "default";
        private String keyPrefix = "";
        private Duration defaultTtl = Duration.ofHours(1);
        private Duration connectionTimeout = Duration.ofSeconds(10);
        private Duration operationTimeout = Duration.ofSeconds(3);
        private int maxRetries = 3;
        private Duration retryBackoff = Duration.ofMillis(100);
        private boolean enableCompression = false;
        private int compressionThreshold = 1024;
        private boolean enableStats = true;
        private boolean enableLocalCache = false;
        private int localCacheSize = 1000;
        private Duration localCacheTtl = Duration.ofMinutes(5);
        private CacheSerializer<?> keySerializer;
        private CacheSerializer<?> valueSerializer;
        private String invalidationChannel;

        private Builder() {}

        /**
         * Sets the cache name.
         * 设置缓存名称。
          * @param name the name | name
          * @return the result | 结果
         */
        public Builder name(String name) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            return this;
        }

        /**
         * Sets the key prefix for all keys.
         * 设置所有键的前缀。
          * @param keyPrefix the keyPrefix | keyPrefix
          * @return the result | 结果
         */
        public Builder keyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix != null ? keyPrefix : "";
            return this;
        }

        /**
         * Sets the default TTL for entries.
         * 设置条目的默认 TTL。
          * @param defaultTtl the defaultTtl | defaultTtl
          * @return the result | 结果
         */
        public Builder defaultTtl(Duration defaultTtl) {
            this.defaultTtl = Objects.requireNonNull(defaultTtl, "defaultTtl must not be null");
            return this;
        }

        /**
         * Sets the connection timeout.
         * 设置连接超时。
          * @param connectionTimeout the connectionTimeout | connectionTimeout
          * @return the result | 结果
         */
        public Builder connectionTimeout(Duration connectionTimeout) {
            this.connectionTimeout = Objects.requireNonNull(connectionTimeout);
            return this;
        }

        /**
         * Sets the operation timeout.
         * 设置操作超时。
          * @param operationTimeout the operationTimeout | operationTimeout
          * @return the result | 结果
         */
        public Builder operationTimeout(Duration operationTimeout) {
            this.operationTimeout = Objects.requireNonNull(operationTimeout);
            return this;
        }

        /**
         * Sets the maximum retry count.
         * 设置最大重试次数。
          * @param maxRetries the maxRetries | maxRetries
          * @return the result | 结果
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = Math.max(0, maxRetries);
            return this;
        }

        /**
         * Sets the retry backoff duration.
         * 设置重试退避时间。
          * @param retryBackoff the retryBackoff | retryBackoff
          * @return the result | 结果
         */
        public Builder retryBackoff(Duration retryBackoff) {
            this.retryBackoff = Objects.requireNonNull(retryBackoff);
            return this;
        }

        /**
         * Enables value compression.
         * 启用值压缩。
          * @param enable the enable | enable
          * @return the result | 结果
         */
        public Builder enableCompression(boolean enable) {
            this.enableCompression = enable;
            return this;
        }

        /**
         * Sets the compression threshold (bytes).
         * 设置压缩阈值（字节）。
          * @param threshold the threshold | threshold
          * @return the result | 结果
         */
        public Builder compressionThreshold(int threshold) {
            this.compressionThreshold = Math.max(0, threshold);
            return this;
        }

        /**
         * Enables statistics collection.
         * 启用统计收集。
          * @param enable the enable | enable
          * @return the result | 结果
         */
        public Builder enableStats(boolean enable) {
            this.enableStats = enable;
            return this;
        }

        /**
         * Enables local (L1) caching.
         * 启用本地（L1）缓存。
          * @param enable the enable | enable
          * @return the result | 结果
         */
        public Builder enableLocalCache(boolean enable) {
            this.enableLocalCache = enable;
            return this;
        }

        /**
         * Sets the local cache size.
         * 设置本地缓存大小。
          * @param size the size | size
          * @return the result | 结果
         */
        public Builder localCacheSize(int size) {
            this.localCacheSize = Math.max(0, size);
            return this;
        }

        /**
         * Sets the local cache TTL.
         * 设置本地缓存 TTL。
          * @param ttl the ttl | ttl
          * @return the result | 结果
         */
        public Builder localCacheTtl(Duration ttl) {
            this.localCacheTtl = Objects.requireNonNull(ttl);
            return this;
        }

        /**
         * Sets the key serializer.
         * 设置键序列化器。
          * @param serializer the serializer | serializer
          * @return the result | 结果
         */
        public Builder keySerializer(CacheSerializer<?> serializer) {
            this.keySerializer = serializer;
            return this;
        }

        /**
         * Sets the value serializer.
         * 设置值序列化器。
          * @param serializer the serializer | serializer
          * @return the result | 结果
         */
        public Builder valueSerializer(CacheSerializer<?> serializer) {
            this.valueSerializer = serializer;
            return this;
        }

        /**
         * Sets the invalidation channel for pub/sub.
         * 设置发布/订阅的失效频道。
          * @param channel the channel | channel
          * @return the result | 结果
         */
        public Builder invalidationChannel(String channel) {
            this.invalidationChannel = channel;
            return this;
        }

        /**
         * Builds the config.
         * 构建配置。
          * @return the result | 结果
         */
        public DistributedCacheConfig build() {
            return new DistributedCacheConfig(
                    name, keyPrefix, defaultTtl, connectionTimeout, operationTimeout,
                    maxRetries, retryBackoff, enableCompression, compressionThreshold,
                    enableStats, enableLocalCache, localCacheSize, localCacheTtl,
                    keySerializer, valueSerializer, invalidationChannel
            );
        }
    }
}
