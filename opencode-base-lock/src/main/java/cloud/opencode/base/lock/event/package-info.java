/**
 * Lock Event Package - Lock Lifecycle Event Notification (JDK 25)
 * 锁事件包 - 锁生命周期事件通知 (JDK 25)
 *
 * <p>This package provides an event-driven observation mechanism for lock
 * lifecycle transitions. It enables monitoring, auditing, and metrics
 * collection without modifying existing lock implementations.</p>
 * <p>此包提供锁生命周期转换的事件驱动观察机制。它支持在不修改现有锁实现的
 * 情况下进行监控、审计和指标收集。</p>
 *
 * <h2>Core Types | 核心类型</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.lock.event.LockEvent} - Immutable event record</li>
 *   <li>{@link cloud.opencode.base.lock.event.LockListener} - Event listener interface</li>
 *   <li>{@link cloud.opencode.base.lock.event.ObservableLock} - Lock decorator with events</li>
 * </ul>
 *
 * <h2>Quick Start | 快速开始</h2>
 * <pre>{@code
 * // Wrap any lock with event observation | 用事件观察包装任意锁
 * Lock<Long> baseLock = new LocalLock();
 * ObservableLock<Long> lock = new ObservableLock<>(baseLock, "order-lock");
 *
 * // Register listeners | 注册监听器
 * lock.addListener(event ->
 *     System.out.println(event.type() + ": " + event.lockName()));
 *
 * // Use normally - events fire automatically | 正常使用 - 事件自动触发
 * try (var guard = lock.lock()) {
 *     // Critical section
 * }
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.3
 */
package cloud.opencode.base.lock.event;
