package cloud.opencode.base.classloader.leak;

import java.util.List;
import java.util.Objects;

/**
 * Immutable report of ClassLoader cleanup operations
 * ClassLoader 清理操作的不可变报告
 *
 * <p>Aggregates the results of all cleanup operations performed by {@link LeakCleaner},
 * including counts of cleaned resources and any errors encountered during cleanup.</p>
 *
 * <p>汇总 {@link LeakCleaner} 执行的所有清理操作的结果，
 * 包括已清理资源的计数以及清理过程中遇到的任何错误。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Counts of each type of resource cleaned - 每种已清理资源类型的计数</li>
 *   <li>Error messages for failed cleanup attempts - 清理失败尝试的错误消息</li>
 *   <li>Static factory for empty report - 空报告的静态工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CleanupReport report = LeakCleaner.cleanAll(myClassLoader);
 * System.out.println("JDBC drivers removed: " + report.jdbcDriversRemoved());
 * if (!report.errors().isEmpty()) {
 *     report.errors().forEach(System.err::println);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是 (不可变记录)</li>
 * </ul>
 *
 * @param threadLocalsCleared the number of ThreadLocal entries cleared | 已清除的 ThreadLocal 条目数
 * @param jdbcDriversRemoved  the number of JDBC drivers deregistered | 已注销的 JDBC 驱动数
 * @param shutdownHooksRemoved the number of shutdown hooks removed | 已移除的关闭钩子数
 * @param timersCancelled     the number of timers cancelled | 已取消的计时器数
 * @param errors              error messages from failed cleanup attempts | 清理失败尝试的错误消息列表
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record CleanupReport(
        int threadLocalsCleared,
        int jdbcDriversRemoved,
        int shutdownHooksRemoved,
        int timersCancelled,
        List<String> errors
) {

    /**
     * Compact constructor with validation and defensive copy
     * 带验证和防御性拷贝的紧凑构造器
     *
     * @throws NullPointerException     if errors is null | 当 errors 为 null 时
     * @throws IllegalArgumentException if any count is negative | 当任何计数为负数时
     */
    public CleanupReport {
        Objects.requireNonNull(errors, "errors must not be null");
        if (threadLocalsCleared < 0) {
            throw new IllegalArgumentException("threadLocalsCleared must not be negative: " + threadLocalsCleared);
        }
        if (jdbcDriversRemoved < 0) {
            throw new IllegalArgumentException("jdbcDriversRemoved must not be negative: " + jdbcDriversRemoved);
        }
        if (shutdownHooksRemoved < 0) {
            throw new IllegalArgumentException("shutdownHooksRemoved must not be negative: " + shutdownHooksRemoved);
        }
        if (timersCancelled < 0) {
            throw new IllegalArgumentException("timersCancelled must not be negative: " + timersCancelled);
        }
        errors = List.copyOf(errors);
    }

    /**
     * Create an empty cleanup report with all counts at zero and no errors
     * 创建所有计数为零且无错误的空清理报告
     *
     * @return an empty CleanupReport | 一个空的 CleanupReport
     */
    public static CleanupReport empty() {
        return new CleanupReport(0, 0, 0, 0, List.of());
    }
}
