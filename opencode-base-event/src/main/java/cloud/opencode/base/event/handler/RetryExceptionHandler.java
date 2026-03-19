package cloud.opencode.base.event.handler;

import cloud.opencode.base.event.Event;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Retry Exception Handler
 * 重试异常处理器
 *
 * <p>Exception handler that retries failed listeners with backoff.</p>
 * <p>使用退避策略重试失败监听器的异常处理器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable retry count - 可配置的重试次数</li>
 *   <li>Delay between retries - 重试之间的延迟</li>
 *   <li>Dead letter queue - 死信队列</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RetryExceptionHandler handler = new RetryExceptionHandler(3, Duration.ofSeconds(1));
 *
 * // With retry action
 * handler.setRetryAction(event -> eventBus.publish(event));
 *
 * // Process dead letters
 * handler.processDeadLetters(failedEvent -> {
 *     log.error("Permanently failed: {}", failedEvent);
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public class RetryExceptionHandler implements EventExceptionHandler {

    private static final System.Logger logger = System.getLogger(RetryExceptionHandler.class.getName());

    private final int maxRetries;
    private final Duration delay;
    private final Queue<FailedEvent> deadLetterQueue;
    private Consumer<Event> retryAction;

    /**
     * Create retry handler with default settings
     * 使用默认设置创建重试处理器
     */
    public RetryExceptionHandler() {
        this(3, Duration.ofSeconds(1));
    }

    /**
     * Create retry handler with custom settings
     * 使用自定义设置创建重试处理器
     *
     * @param maxRetries max retry attempts | 最大重试次数
     * @param delay      delay between retries | 重试之间的延迟
     */
    public RetryExceptionHandler(int maxRetries, Duration delay) {
        this.maxRetries = maxRetries;
        this.delay = delay;
        this.deadLetterQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Set the retry action
     * 设置重试动作
     *
     * @param retryAction the action to execute on retry | 重试时执行的动作
     */
    public void setRetryAction(Consumer<Event> retryAction) {
        this.retryAction = retryAction;
    }

    /**
     * Handle exception with retry logic
     * 使用重试逻辑处理异常
     *
     * @param event        the event being processed | 正在处理的事件
     * @param exception    the exception that occurred | 发生的异常
     * @param listenerName the name of the listener that threw | 抛出异常的监听器名称
     */
    @Override
    public void handleException(Event event, Throwable exception, String listenerName) {
        if (retryAction == null) {
            // No retry action configured, just log and add to dead letter queue
            logger.log(System.Logger.Level.ERROR,
                "Event processing failed (no retry action): event=" + event.getId(),
                exception);
            deadLetterQueue.add(new FailedEvent(event, exception, listenerName, 0));
            return;
        }

        boolean success = false;
        int attempt = 0;

        for (int i = 0; i < maxRetries && !success; i++) {
            attempt = i + 1;
            try {
                Thread.sleep(delay.toMillis());
                retryAction.accept(event);
                success = true;
                logger.log(System.Logger.Level.INFO,
                    "Event retry succeeded: event={0}, attempt={1}",
                    event.getId(), attempt);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING,
                    "Event retry failed: event=" + event.getId() + ", attempt=" + attempt,
                    e);
            }
        }

        if (!success) {
            // All retries failed, add to dead letter queue
            deadLetterQueue.add(new FailedEvent(event, exception, listenerName, attempt));
            logger.log(System.Logger.Level.ERROR,
                "Event permanently failed after {0} retries: event={1}",
                attempt, event.getId());
        }
    }

    /**
     * Process dead letter queue
     * 处理死信队列
     *
     * @param handler the handler for failed events | 失败事件的处理器
     */
    public void processDeadLetters(Consumer<FailedEvent> handler) {
        FailedEvent failed;
        while ((failed = deadLetterQueue.poll()) != null) {
            handler.accept(failed);
        }
    }

    /**
     * Get dead letter queue size
     * 获取死信队列大小
     *
     * @return queue size | 队列大小
     */
    public int getDeadLetterQueueSize() {
        return deadLetterQueue.size();
    }

    /**
     * Clear dead letter queue
     * 清除死信队列
     */
    public void clearDeadLetterQueue() {
        deadLetterQueue.clear();
    }

    /**
     * Get max retries
     * 获取最大重试次数
     *
     * @return max retries | 最大重试次数
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Get retry delay
     * 获取重试延迟
     *
     * @return retry delay | 重试延迟
     */
    public Duration getDelay() {
        return delay;
    }

    /**
     * Failed Event Record
     * 失败事件记录
     *
     * @param event        the failed event | 失败的事件
     * @param exception    the exception that caused failure | 导致失败的异常
     * @param listenerName the listener that failed | 失败的监听器
     * @param attempts     number of retry attempts | 重试次数
     */
    public record FailedEvent(
        Event event,
        Throwable exception,
        String listenerName,
        int attempts
    ) {}
}
