/**
 * Lock Package - Unified Lock Abstraction (JDK 25)
 * Lock包 - 统一锁抽象 (JDK 25)
 *
 * <p>This package provides a unified lock abstraction for both local and
 * distributed locks with Virtual Thread support and JDK 25 features.</p>
 * <p>此包提供本地锁和分布式锁的统一抽象，支持虚拟线程和JDK 25特性。</p>
 *
 * <h2>Core Interfaces | 核心接口</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.lock.Lock} - Unified lock interface</li>
 *   <li>{@link cloud.opencode.base.lock.ReadWriteLock} - Read-write lock interface</li>
 *   <li>{@link cloud.opencode.base.lock.LockGuard} - Auto-release lock guard</li>
 * </ul>
 *
 * <h2>Entry Point | 入口点</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.lock.OpenLock} - Facade entry class</li>
 *   <li>{@link cloud.opencode.base.lock.LockConfig} - Lock configuration</li>
 * </ul>
 *
 * <h2>Quick Start | 快速开始</h2>
 * <pre>{@code
 * // Create lock
 * Lock<Long> lock = OpenLock.lock();
 *
 * // Execute with lock (recommended)
 * lock.execute(() -> {
 *     // Critical section
 * });
 *
 * // Or use try-with-resources
 * try (var guard = lock.lock()) {
 *     // Critical section
 * }
 *
 * // Read-write lock
 * ReadWriteLock<Long> rwLock = OpenLock.readWriteLock();
 * rwLock.executeRead(() -> loadData());
 * rwLock.executeWrite(() -> saveData());
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
package cloud.opencode.base.lock;
