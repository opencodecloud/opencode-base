/**
 * OpenCode Base Pool Module
 * OpenCode 基础对象池模块
 *
 * <p>Provides generic object pooling capabilities based on JDK 25 virtual threads,
 * including connection pools, thread pools, and custom resource pools with metrics and policies.</p>
 * <p>提供基于 JDK 25 虚拟线程的通用对象池能力，包括连接池、线程池和自定义资源池，
 * 支持指标监控和策略配置。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Generic Object Pool - 通用对象池</li>
 *   <li>Connection Pool - 连接池</li>
 *   <li>Pool Metrics - 池指标监控</li>
 *   <li>Eviction Policy - 驱逐策略</li>
 *   <li>Object Factory SPI - 对象工厂 SPI</li>
 *   <li>Pool Tracker - 池追踪器</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
module cloud.opencode.base.pool {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.pool;
    exports cloud.opencode.base.pool.exception;
    exports cloud.opencode.base.pool.factory;
    exports cloud.opencode.base.pool.impl;
    exports cloud.opencode.base.pool.metrics;
    exports cloud.opencode.base.pool.policy;
    exports cloud.opencode.base.pool.tracker;
}
