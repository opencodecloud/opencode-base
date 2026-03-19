/**
 * OpenCode Base Lock Module
 * OpenCode 基础锁模块
 *
 * <p>Provides unified lock abstraction based on JDK 25, supporting both local and
 * distributed locks with fencing tokens, lock metrics, and SPI-based provider extension.</p>
 * <p>提供基于 JDK 25 的统一锁抽象，支持本地锁和分布式锁，具有围栏令牌、
 * 锁指标和基于 SPI 的提供者扩展。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Local Locks (ReentrantLock, ReadWriteLock) - 本地锁</li>
 *   <li>Distributed Lock Abstraction - 分布式锁抽象</li>
 *   <li>Fencing Token Support - 围栏令牌支持</li>
 *   <li>Lock Manager - 锁管理器</li>
 *   <li>Lock Metrics - 锁指标</li>
 *   <li>SPI Provider Extension - SPI 提供者扩展</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
module cloud.opencode.base.lock {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Optional: ID module for fencing token generation
    requires static cloud.opencode.base.id;

    // Export public API packages
    exports cloud.opencode.base.lock;
    exports cloud.opencode.base.lock.distributed;
    exports cloud.opencode.base.lock.exception;
    exports cloud.opencode.base.lock.local;
    exports cloud.opencode.base.lock.manager;
    exports cloud.opencode.base.lock.metrics;
    exports cloud.opencode.base.lock.spi;
    exports cloud.opencode.base.lock.token;

    // SPI: Distributed lock provider extension point
    uses cloud.opencode.base.lock.spi.DistributedLockProvider;
}
