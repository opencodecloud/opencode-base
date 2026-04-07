package cloud.opencode.base.classloader.leak;

/**
 * Leak detection level for ClassLoader lifecycle tracking
 * ClassLoader 生命周期追踪的泄漏检测级别
 *
 * <p>Defines the level of detail captured when tracking ClassLoader instances
 * for potential memory leaks. Higher levels provide more diagnostic information
 * at the cost of additional overhead.</p>
 *
 * <p>定义追踪 ClassLoader 实例潜在内存泄漏时捕获的详细程度。
 * 更高级别在额外开销的代价下提供更多诊断信息。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是 (不可变枚举)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public enum LeakDetection {

    /**
     * No leak detection — zero overhead
     * 不检测泄漏 — 零开销
     */
    DISABLED,

    /**
     * Simple detection using PhantomReference to track unclosed ClassLoaders
     * 简单检测，使用 PhantomReference 追踪未关闭的 ClassLoader
     */
    SIMPLE,

    /**
     * Paranoid detection: SIMPLE + records creation stack trace for diagnostics
     * 偏执检测: SIMPLE + 记录创建栈轨迹用于诊断
     */
    PARANOID
}
