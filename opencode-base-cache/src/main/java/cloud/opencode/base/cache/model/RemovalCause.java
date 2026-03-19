package cloud.opencode.base.cache.model;

/**
 * Cache Entry Removal Cause - Enum indicating why a cache entry was removed
 * 缓存条目移除原因 - 表示缓存条目被移除原因的枚举
 *
 * <p>Provides detailed information about cache entry removal for monitoring and debugging.</p>
 * <p>提供缓存条目移除的详细信息，用于监控和调试。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Explicit removal (manual invalidation) - 显式移除（手动失效）</li>
 *   <li>Replaced by new value - 被新值替换</li>
 *   <li>Expired (TTL/TTI) - 过期（TTL/TTI）</li>
 *   <li>Evicted due to size limit - 因容量限制淘汰</li>
 *   <li>Collected by GC (weak/soft reference) - 被 GC 回收（弱/软引用）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * cache.removalListener((key, value, cause) -> {
 *     if (cause.wasEvicted()) {
 *         log.info("Entry evicted: key={}, cause={}", key, cause);
 *     }
 * });
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public enum RemovalCause {

    /**
     * Entry was explicitly removed by user (invalidate)
     * 用户显式移除（invalidate 调用）
     */
    EXPLICIT,

    /**
     * Entry was replaced by a new value
     * 被新值替换
     */
    REPLACED,

    /**
     * Entry expired (TTL or TTI)
     * 过期移除（TTL 或 TTI）
     */
    EXPIRED,

    /**
     * Entry was evicted due to size/weight limit
     * 因容量/权重限制被淘汰
     */
    SIZE,

    /**
     * Entry was collected by garbage collector (weak/soft reference)
     * 被垃圾回收器回收（弱引用/软引用）
     */
    COLLECTED;

    /**
     * Check if entry was passively evicted (not manually removed)
     * 检查是否为被动淘汰（非手动移除）
     *
     * @return true if passively evicted | 被动淘汰返回 true
     */
    public boolean wasEvicted() {
        return this == EXPIRED || this == SIZE || this == COLLECTED;
    }

    /**
     * Check if entry was explicitly removed
     * 检查是否为显式移除
     *
     * @return true if explicitly removed | 显式移除返回 true
     */
    public boolean wasExplicit() {
        return this == EXPLICIT || this == REPLACED;
    }
}
