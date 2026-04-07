package cloud.opencode.base.event.testing;

import cloud.opencode.base.event.Event;
import cloud.opencode.base.event.EventListener;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Event Captor - Test utility for capturing and asserting events
 * 事件捕获器 - 用于捕获和断言事件的测试工具
 *
 * <p>Provides a convenient way to capture events in unit tests, wait for async events,
 * and make assertions on captured events.</p>
 * <p>提供在单元测试中捕获事件、等待异步事件和对捕获的事件进行断言的便捷方式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Event capture list - 事件捕获列表</li>
 *   <li>Await support for async events - 支持等待异步事件</li>
 *   <li>First/last event accessors - 首个/最后事件访问器</li>
 *   <li>Reset for test isolation - 重置以隔离测试</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EventCaptor<MyEvent> captor = new EventCaptor<>();
 * eventBus.subscribe(MyEvent.class, captor);
 *
 * eventBus.publish(new MyEvent("test"));
 *
 * assertThat(captor.count()).isEqualTo(1);
 * assertThat(captor.getFirst()).isNotNull();
 * assertThat(captor.getLast().getData()).isEqualTo("test");
 *
 * // For async events
 * captor.reset();
 * eventBus.publishAsync(new MyEvent("async"));
 * assertThat(captor.awaitEvent(Duration.ofSeconds(5))).isTrue();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @param <E> the type of event to capture | 要捕获的事件类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
public class EventCaptor<E extends Event> implements EventListener<E> {

    private final CopyOnWriteArrayList<E> captured = new CopyOnWriteArrayList<>();
    private final AtomicReference<CountDownLatch> latchRef = new AtomicReference<>();

    /**
     * Create a new event captor
     * 创建新的事件捕获器
     */
    public EventCaptor() {
    }

    @Override
    public void onEvent(E event) {
        captured.add(event);
        CountDownLatch latch = latchRef.get();
        if (latch != null) {
            latch.countDown();
        }
    }

    /**
     * Get all captured events
     * 获取所有捕获的事件
     *
     * @return unmodifiable list of captured events | 捕获事件的不可修改列表
     */
    public List<E> getCapturedEvents() {
        return List.copyOf(captured);
    }

    /**
     * Get the first captured event
     * 获取第一个捕获的事件
     *
     * @return the first event, or null if none captured | 第一个事件，如果没有捕获则为 null
     */
    public E getFirst() {
        return captured.isEmpty() ? null : captured.getFirst();
    }

    /**
     * Get the last captured event
     * 获取最后一个捕获的事件
     *
     * @return the last event, or null if none captured | 最后一个事件，如果没有捕获则为 null
     */
    public E getLast() {
        return captured.isEmpty() ? null : captured.getLast();
    }

    /**
     * Get the number of captured events
     * 获取捕获的事件数量
     *
     * @return the count of captured events | 捕获的事件数量
     */
    public int count() {
        return captured.size();
    }

    /**
     * Check if any events were captured
     * 检查是否捕获了任何事件
     *
     * @return true if at least one event was captured | 如果至少捕获了一个事件返回 true
     */
    public boolean hasCaptured() {
        return !captured.isEmpty();
    }

    /**
     * Wait for at least one event to be captured
     * 等待至少一个事件被捕获
     *
     * @param timeout maximum time to wait | 最大等待时间
     * @return true if an event was captured within timeout | 如果在超时内捕获了事件返回 true
     * @throws NullPointerException if timeout is null | 如果 timeout 为 null
     */
    public boolean awaitEvent(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout cannot be null");
        if (!captured.isEmpty()) {
            return true;
        }
        CountDownLatch latch = new CountDownLatch(1);
        latchRef.set(latch);
        // Double-check after setting latch
        if (!captured.isEmpty()) {
            return true;
        }
        try {
            return latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Wait for a specific number of events to be captured
     * 等待捕获特定数量的事件
     *
     * @param count   the number of events to wait for | 等待的事件数量
     * @param timeout maximum time to wait | 最大等待时间
     * @return true if the required count was reached within timeout | 如果在超时内达到所需数量返回 true
     * @throws IllegalArgumentException if count is less than 1 | 如果 count 小于 1
     * @throws NullPointerException     if timeout is null | 如果 timeout 为 null
     */
    public boolean awaitEvents(int count, Duration timeout) {
        if (count < 1) {
            throw new IllegalArgumentException("count must be >= 1");
        }
        Objects.requireNonNull(timeout, "timeout cannot be null");
        if (captured.size() >= count) {
            return true;
        }
        int remaining = count - captured.size();
        CountDownLatch latch = new CountDownLatch(remaining);
        latchRef.set(latch);
        // Compensate for events that arrived between size check and latch set
        int arrived = captured.size() - (count - remaining);
        for (int i = 0; i < arrived && latch.getCount() > 0; i++) {
            latch.countDown();
        }
        if (captured.size() >= count) {
            return true;
        }
        try {
            return latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Reset the captor, clearing all captured events
     * 重置捕获器，清除所有捕获的事件
     */
    public void reset() {
        captured.clear();
        latchRef.set(null);
    }
}
