package cloud.opencode.base.event;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Waitable Event Wrapper
 * 可等待事件包装器
 *
 * <p>Event wrapper that supports waiting for processing completion.</p>
 * <p>支持等待处理完成的事件包装器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Wait for event processing - 等待事件处理完成</li>
 *   <li>Timeout support - 超时支持</li>
 *   <li>Wraps any event type - 包装任意事件类型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Publish and wait
 * Event event = new UserRegisteredEvent(1L, "user@example.com");
 * OpenEvent.getDefault().publishAndWait(event, Duration.ofSeconds(5));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public class WaitableEvent extends Event {

    private final Event wrappedEvent;
    private final CountDownLatch latch;

    /**
     * Create waitable event wrapper
     * 创建可等待事件包装器
     *
     * @param event the event to wrap | 要包装的事件
     * @param latch the countdown latch | 倒计时锁存器
     */
    public WaitableEvent(Event event, CountDownLatch latch) {
        super(event.getSource());
        this.wrappedEvent = event;
        this.latch = latch;
    }

    /**
     * Create waitable event with new latch
     * 创建带新锁存器的可等待事件
     *
     * @param event the event to wrap | 要包装的事件
     */
    public WaitableEvent(Event event) {
        this(event, new CountDownLatch(1));
    }

    /**
     * Get the wrapped event
     * 获取被包装的事件
     *
     * @return the wrapped event | 被包装的事件
     */
    public Event getWrappedEvent() {
        return wrappedEvent;
    }

    /**
     * Get the countdown latch
     * 获取倒计时锁存器
     *
     * @return the latch | 锁存器
     */
    public CountDownLatch getLatch() {
        return latch;
    }

    /**
     * Signal that event processing is complete
     * 通知事件处理完成
     */
    public void complete() {
        latch.countDown();
    }

    /**
     * Wait for event processing to complete
     * 等待事件处理完成
     *
     * @param timeout timeout in milliseconds | 超时时间（毫秒）
     * @return true if completed before timeout | 超时前完成返回true
     * @throws InterruptedException if interrupted | 被中断时抛出
     */
    public boolean await(long timeout) throws InterruptedException {
        return latch.await(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Wait for event processing to complete indefinitely
     * 无限期等待事件处理完成
     *
     * @throws InterruptedException if interrupted | 被中断时抛出
     */
    public void await() throws InterruptedException {
        latch.await();
    }

    @Override
    public boolean isCancelled() {
        return wrappedEvent.isCancelled();
    }

    @Override
    public void cancel() {
        wrappedEvent.cancel();
    }

    @Override
    public String toString() {
        return "WaitableEvent{" +
                "wrappedEvent=" + wrappedEvent +
                '}';
    }
}
