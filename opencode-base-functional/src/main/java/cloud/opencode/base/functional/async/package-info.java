/**
 * Async Utilities - Virtual Thread functional utilities
 * 异步工具 - 虚拟线程函数式工具
 *
 * <p>Provides asynchronous functional utilities leveraging JDK 25's
 * Virtual Threads and Structured Concurrency for efficient parallel execution.</p>
 * <p>利用 JDK 25 的虚拟线程和结构化并发提供异步函数式工具，实现高效并行执行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.functional.async.AsyncFunctionUtil} - Async execution utilities</li>
 *   <li>{@link cloud.opencode.base.functional.async.LazyAsync} - Async lazy evaluation</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Async Try execution
 * CompletableFuture<Try<String>> future = AsyncFunctionUtil.tryAsync(
 *     () -> httpClient.get(url));
 *
 * // Parallel execution
 * List<Try<User>> results = AsyncFunctionUtil.parallel(
 *     List.of(
 *         () -> fetchUser(1),
 *         () -> fetchUser(2),
 *         () -> fetchUser(3)));
 *
 * // Structured concurrency - all must succeed
 * Try<List<User>> users = AsyncFunctionUtil.sequence(suppliers);
 *
 * // LazyAsync - preheating
 * LazyAsync<ExpensiveData> lazy = LazyAsync.of(this::computeData);
 * lazy.preheat();  // Start computing in virtual thread
 * // ... do other work ...
 * ExpensiveData data = lazy.get();  // Get result when ready
 * }</pre>
 *
 * <p><strong>JDK 25 Features Used | 使用的 JDK 25 特性:</strong></p>
 * <ul>
 *   <li>Virtual Threads - 虚拟线程</li>
 *   <li>Structured Concurrency - 结构化并发</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
package cloud.opencode.base.functional.async;
