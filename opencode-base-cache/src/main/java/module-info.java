/**
 * OpenCode Base Cache Module
 * OpenCode 基础缓存模块
 *
 * <p>Provides comprehensive caching capabilities based on JDK 25 features,
 * including local cache, distributed cache, multi-level cache, reactive cache,
 * and cache protection mechanisms.</p>
 * <p>提供基于 JDK 25 特性的全面缓存能力，包括本地缓存、分布式缓存、多级缓存、
 * 响应式缓存和缓存保护机制。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Local Cache (LRU/LFU/W-TinyLFU) - 本地缓存</li>
 *   <li>Distributed Cache - 分布式缓存</li>
 *   <li>Multi-Level Cache - 多级缓存</li>
 *   <li>Reactive Cache - 响应式缓存</li>
 *   <li>Cache Protection (Bloom Filter, Mutex) - 缓存保护</li>
 *   <li>Cache Warming &amp; Bulk Operations - 缓存预热与批量操作</li>
 *   <li>Cache Metrics &amp; Analysis - 缓存指标与分析</li>
 *   <li>Write-Behind / Write-Through - 写后/直写策略</li>
 *   <li>SPI Extension Point - SPI 扩展点</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
module cloud.opencode.base.cache {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Optional: JMX support for cache monitoring
    requires static java.management;

    // Export public API packages
    exports cloud.opencode.base.cache;
    exports cloud.opencode.base.cache.analysis;
    exports cloud.opencode.base.cache.bulk;
    exports cloud.opencode.base.cache.compression;
    exports cloud.opencode.base.cache.config;
    exports cloud.opencode.base.cache.distributed;
    exports cloud.opencode.base.cache.dlq;
    exports cloud.opencode.base.cache.event;
    exports cloud.opencode.base.cache.exception;
    exports cloud.opencode.base.cache.metrics;
    exports cloud.opencode.base.cache.model;
    exports cloud.opencode.base.cache.multilevel;
    exports cloud.opencode.base.cache.protection;
    exports cloud.opencode.base.cache.query;
    exports cloud.opencode.base.cache.reactive;
    exports cloud.opencode.base.cache.resilience;
    exports cloud.opencode.base.cache.spi;
    exports cloud.opencode.base.cache.spring;
    exports cloud.opencode.base.cache.testing;
    exports cloud.opencode.base.cache.ttl;
    exports cloud.opencode.base.cache.util;
    exports cloud.opencode.base.cache.warming;
    exports cloud.opencode.base.cache.write;

    // Internal packages - not exported
    // cloud.opencode.base.cache.internal
    // cloud.opencode.base.cache.internal.eviction
    // cloud.opencode.base.cache.internal.expiry
    // cloud.opencode.base.cache.internal.stats
    // cloud.opencode.base.cache.jmx

    // SPI: Cache extension points
    uses cloud.opencode.base.cache.spi.EvictionPolicy;
    uses cloud.opencode.base.cache.spi.ExpiryPolicy;
    uses cloud.opencode.base.cache.spi.CacheLoader;
}
