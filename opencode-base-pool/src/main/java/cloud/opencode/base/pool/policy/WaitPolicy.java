package cloud.opencode.base.pool.policy;

/**
 * WaitPolicy - Wait Policy Enumeration
 * WaitPolicy - 等待策略枚举
 *
 * <p>Defines how the pool behaves when exhausted and a borrow is requested.</p>
 * <p>定义池耗尽时请求借用的行为。</p>
 *
 * <p><strong>Policies | 策略:</strong></p>
 * <ul>
 *   <li>BLOCK - Wait for available object - 等待可用对象</li>
 *   <li>FAIL - Throw exception immediately - 立即抛出异常</li>
 *   <li>GROW - Create new object beyond max - 创建超出最大值的新对象</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Three exhaustion strategies: BLOCK, FAIL, GROW - 三种耗尽策略: 阻塞、失败、增长</li>
 *   <li>Immutable enum constants for thread-safe configuration - 不可变枚举常量，线程安全配置</li>
 *   <li>Integrates with PoolConfig for flexible pool behavior - 与PoolConfig集成实现灵活的池行为</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PoolConfig config = PoolConfig.builder()
 *     .waitPolicy(WaitPolicy.BLOCK)
 *     .maxWait(Duration.ofSeconds(5))
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is immutable) - 线程安全: 是（枚举不可变）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public enum WaitPolicy {

    /**
     * Block and wait for an object to become available.
     * 阻塞并等待对象可用。
     */
    BLOCK,

    /**
     * Fail immediately with an exception.
     * 立即抛出异常失败。
     */
    FAIL,

    /**
     * Grow the pool beyond maxTotal (use with caution).
     * 增长池超过maxTotal（谨慎使用）。
     */
    GROW
}
