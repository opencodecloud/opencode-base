package cloud.opencode.base.parallel.executor;

/**
 * CPU-Bound Marker Interface
 * CPU 密集型标记接口
 *
 * <p>A marker interface for {@link Runnable} tasks that are CPU-bound.
 * When submitted to a {@link HybridExecutor}, tasks implementing this interface
 * will be dispatched to the platform thread pool instead of the virtual thread pool.</p>
 * <p>用于标记 CPU 密集型 {@link Runnable} 任务的标记接口。
 * 当提交到 {@link HybridExecutor} 时，实现此接口的任务将被分派到平台线程池而非虚拟线程池。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * CpuBound heavyTask = () -> {
 *     // CPU-intensive computation
 *     computeHash(data);
 * };
 *
 * try (var executor = HybridExecutor.create()) {
 *     executor.execute(heavyTask); // dispatched to platform thread pool
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Marker interface for CPU-bound tasks - CPU密集型任务的标记接口</li>
 *   <li>Automatic dispatch to platform thread pool - 自动分派到平台线程池</li>
 *   <li>Functional interface support - 函数式接口支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see HybridExecutor
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@FunctionalInterface
public interface CpuBound extends Runnable {
}
