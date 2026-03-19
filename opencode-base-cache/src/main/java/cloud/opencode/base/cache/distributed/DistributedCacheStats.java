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

import java.time.Duration;
import java.time.Instant;

/**
 * Statistics for Distributed Cache
 * 分布式缓存统计信息
 *
 * @param hitCount the number of cache hits - 缓存命中次数
 * @param missCount the number of cache misses - 缓存未命中次数
 * @param loadCount the number of load operations - 加载操作次数
 * @param loadSuccessCount the number of successful loads - 成功加载次数
 * @param loadFailureCount the number of failed loads - 失败加载次数
 * @param totalLoadTime the total load time - 总加载时间
 * @param evictionCount the number of evictions - 淘汰次数
 * @param requestCount the total number of requests - 总请求次数
 * @param connectionCount the number of active connections - 活跃连接数
 * @param memoryUsed the memory used by the cache - 缓存使用的内存
 * @param keyCount the number of keys in the cache - 缓存中的键数量
 * @param avgLatency the average operation latency - 平均操作延迟
 * @param p99Latency the 99th percentile latency - 99分位延迟
 * @param lastResetTime the last statistics reset time - 上次统计重置时间
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Hit/miss statistics - 命中/未命中统计</li>
 *   <li>Latency tracking (avg, P99) - 延迟跟踪（平均、P99）</li>
 *   <li>Connection and memory metrics - 连接和内存指标</li>
 *   <li>Rate calculations - 比率计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DistributedCacheStats stats = distributedCache.stats();
 * double hitRate = stats.hitRate();
 * long keyCount = stats.keyCount();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public record DistributedCacheStats(
        long hitCount,
        long missCount,
        long loadCount,
        long loadSuccessCount,
        long loadFailureCount,
        Duration totalLoadTime,
        long evictionCount,
        long requestCount,
        int connectionCount,
        long memoryUsed,
        long keyCount,
        Duration avgLatency,
        Duration p99Latency,
        Instant lastResetTime
) {

    /**
     * Creates an empty stats instance.
     * 创建空的统计实例。
     *
     * @return empty stats - 空统计
     */
    public static DistributedCacheStats empty() {
        return new DistributedCacheStats(
                0, 0, 0, 0, 0,
                Duration.ZERO, 0, 0, 0, 0, 0,
                Duration.ZERO, Duration.ZERO,
                Instant.now()
        );
    }

    /**
     * Gets the hit rate.
     * 获取命中率。
     *
     * @return the hit rate (0.0 to 1.0) - 命中率
     */
    public double hitRate() {
        long total = hitCount + missCount;
        return total == 0 ? 0.0 : (double) hitCount / total;
    }

    /**
     * Gets the miss rate.
     * 获取未命中率。
     *
     * @return the miss rate (0.0 to 1.0) - 未命中率
     */
    public double missRate() {
        return 1.0 - hitRate();
    }

    /**
     * Gets the load success rate.
     * 获取加载成功率。
     *
     * @return the load success rate - 加载成功率
     */
    public double loadSuccessRate() {
        return loadCount == 0 ? 0.0 : (double) loadSuccessCount / loadCount;
    }

    /**
     * Gets the average load time.
     * 获取平均加载时间。
     *
     * @return the average load time - 平均加载时间
     */
    public Duration averageLoadTime() {
        return loadCount == 0 ? Duration.ZERO :
                Duration.ofNanos(totalLoadTime.toNanos() / loadCount);
    }

    /**
     * Creates a builder for DistributedCacheStats.
     * 创建 DistributedCacheStats 构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for DistributedCacheStats.
     * DistributedCacheStats 构建器。
     */
    public static final class Builder {
        private long hitCount = 0;
        private long missCount = 0;
        private long loadCount = 0;
        private long loadSuccessCount = 0;
        private long loadFailureCount = 0;
        private Duration totalLoadTime = Duration.ZERO;
        private long evictionCount = 0;
        private long requestCount = 0;
        private int connectionCount = 0;
        private long memoryUsed = 0;
        private long keyCount = 0;
        private Duration avgLatency = Duration.ZERO;
        private Duration p99Latency = Duration.ZERO;
        private Instant lastResetTime = Instant.now();

        private Builder() {}

        /**
         * hitCount | hitCount
         * @param hitCount the hitCount | hitCount
         * @return the result | 结果
         */
        public Builder hitCount(long hitCount) {
            this.hitCount = hitCount;
            return this;
        }

        /**
         * missCount | missCount
         * @param missCount the missCount | missCount
         * @return the result | 结果
         */
        public Builder missCount(long missCount) {
            this.missCount = missCount;
            return this;
        }

        /**
         * loadCount | loadCount
         * @param loadCount the loadCount | loadCount
         * @return the result | 结果
         */
        public Builder loadCount(long loadCount) {
            this.loadCount = loadCount;
            return this;
        }

        /**
         * loadSuccessCount | loadSuccessCount
         * @param loadSuccessCount the loadSuccessCount | loadSuccessCount
         * @return the result | 结果
         */
        public Builder loadSuccessCount(long loadSuccessCount) {
            this.loadSuccessCount = loadSuccessCount;
            return this;
        }

        /**
         * loadFailureCount | loadFailureCount
         * @param loadFailureCount the loadFailureCount | loadFailureCount
         * @return the result | 结果
         */
        public Builder loadFailureCount(long loadFailureCount) {
            this.loadFailureCount = loadFailureCount;
            return this;
        }

        /**
         * totalLoadTime | totalLoadTime
         * @param totalLoadTime the totalLoadTime | totalLoadTime
         * @return the result | 结果
         */
        public Builder totalLoadTime(Duration totalLoadTime) {
            this.totalLoadTime = totalLoadTime;
            return this;
        }

        /**
         * evictionCount | evictionCount
         * @param evictionCount the evictionCount | evictionCount
         * @return the result | 结果
         */
        public Builder evictionCount(long evictionCount) {
            this.evictionCount = evictionCount;
            return this;
        }

        /**
         * requestCount | requestCount
         * @param requestCount the requestCount | requestCount
         * @return the result | 结果
         */
        public Builder requestCount(long requestCount) {
            this.requestCount = requestCount;
            return this;
        }

        /**
         * connectionCount | connectionCount
         * @param connectionCount the connectionCount | connectionCount
         * @return the result | 结果
         */
        public Builder connectionCount(int connectionCount) {
            this.connectionCount = connectionCount;
            return this;
        }

        /**
         * memoryUsed | memoryUsed
         * @param memoryUsed the memoryUsed | memoryUsed
         * @return the result | 结果
         */
        public Builder memoryUsed(long memoryUsed) {
            this.memoryUsed = memoryUsed;
            return this;
        }

        /**
         * keyCount | keyCount
         * @param keyCount the keyCount | keyCount
         * @return the result | 结果
         */
        public Builder keyCount(long keyCount) {
            this.keyCount = keyCount;
            return this;
        }

        /**
         * avgLatency | avgLatency
         * @param avgLatency the avgLatency | avgLatency
         * @return the result | 结果
         */
        public Builder avgLatency(Duration avgLatency) {
            this.avgLatency = avgLatency;
            return this;
        }

        /**
         * p99Latency | p99Latency
         * @param p99Latency the p99Latency | p99Latency
         * @return the result | 结果
         */
        public Builder p99Latency(Duration p99Latency) {
            this.p99Latency = p99Latency;
            return this;
        }

        /**
         * lastResetTime | lastResetTime
         * @param lastResetTime the lastResetTime | lastResetTime
         * @return the result | 结果
         */
        public Builder lastResetTime(Instant lastResetTime) {
            this.lastResetTime = lastResetTime;
            return this;
        }

        /**
         * build | build
         * @return the result | 结果
         */
        public DistributedCacheStats build() {
            return new DistributedCacheStats(
                    hitCount, missCount, loadCount, loadSuccessCount, loadFailureCount,
                    totalLoadTime, evictionCount, requestCount, connectionCount,
                    memoryUsed, keyCount, avgLatency, p99Latency, lastResetTime
            );
        }
    }
}
