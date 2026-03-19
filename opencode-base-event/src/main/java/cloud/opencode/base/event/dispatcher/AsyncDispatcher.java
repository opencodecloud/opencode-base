package cloud.opencode.base.event.dispatcher;

import cloud.opencode.base.event.Event;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Asynchronous Event Dispatcher
 * 异步事件分发器
 *
 * <p>Dispatches events asynchronously using virtual threads.</p>
 * <p>使用虚拟线程异步分发事件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Virtual thread execution - 虚拟线程执行</li>
 *   <li>Non-blocking processing - 非阻塞处理</li>
 *   <li>Parallel listener execution - 并行监听器执行</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AsyncDispatcher dispatcher = new AsyncDispatcher();
 * dispatcher.dispatch(event, listeners);
 *
 * // With custom executor
 * AsyncDispatcher customDispatcher = new AsyncDispatcher(myExecutor);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public class AsyncDispatcher implements EventDispatcher {

    private static final System.Logger LOGGER = System.getLogger(AsyncDispatcher.class.getName());

    private final ExecutorService executor;
    private final boolean ownsExecutor;

    /**
     * Create async dispatcher with default virtual thread executor
     * 使用默认虚拟线程执行器创建异步分发器
     */
    public AsyncDispatcher() {
        this.executor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("event-async-", 0).factory()
        );
        this.ownsExecutor = true;
    }

    /**
     * Create async dispatcher with custom executor
     * 使用自定义执行器创建异步分发器
     *
     * @param executor the executor service to use | 要使用的执行器服务
     */
    public AsyncDispatcher(ExecutorService executor) {
        this.executor = executor;
        this.ownsExecutor = false;
    }

    /**
     * Dispatch event to listeners asynchronously
     * 异步将事件分发到监听器
     *
     * <p>Each listener is executed in a separate virtual thread.
     * Event cancellation is checked before each listener invocation.</p>
     * <p>每个监听器在单独的虚拟线程中执行。
     * 在每次监听器调用前检查事件是否被取消。</p>
     *
     * @param event     the event to dispatch | 要分发的事件
     * @param listeners the list of listener handlers | 监听器处理器列表
     */
    @Override
    public void dispatch(Event event, List<Consumer<Event>> listeners) {
        if (event == null || listeners == null || listeners.isEmpty()) {
            return;
        }

        for (Consumer<Event> listener : listeners) {
            // Check if event has been cancelled before scheduling
            if (event.isCancelled()) {
                break;
            }

            executor.submit(() -> {
                // Double-check cancellation before execution
                if (!event.isCancelled()) {
                    try {
                        listener.accept(event);
                    } catch (Exception e) {
                        // Log error but don't propagate in async context
                        LOGGER.log(System.Logger.Level.ERROR,
                                "Event listener failed for event type {0}: {1}",
                                event.getClass().getSimpleName(), e.getMessage(), e);
                    }
                }
            });
        }
    }

    /**
     * Dispatch event and return a future for completion
     * 分发事件并返回完成的Future
     *
     * @param event     the event to dispatch | 要分发的事件
     * @param listeners the list of listener handlers | 监听器处理器列表
     * @return CompletableFuture that completes when all listeners finish | 当所有监听器完成时完成的CompletableFuture
     */
    public CompletableFuture<Void> dispatchAsync(Event event, List<Consumer<Event>> listeners) {
        if (event == null || listeners == null || listeners.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<?>[] futures = listeners.stream()
            .takeWhile(_ -> !event.isCancelled())
            .map(listener -> CompletableFuture.runAsync(() -> {
                if (!event.isCancelled()) {
                    listener.accept(event);
                }
            }, executor).exceptionally(throwable -> {
                LOGGER.log(System.Logger.Level.ERROR,
                        "Event listener failed asynchronously for event type {0}: {1}",
                        event.getClass().getSimpleName(), throwable.getMessage(), throwable);
                return null;
            }))
            .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    /**
     * Shutdown the dispatcher and release resources
     * 关闭分发器并释放资源
     */
    @Override
    public void shutdown() {
        if (ownsExecutor) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }
    }

    /**
     * Get the underlying executor service
     * 获取底层执行器服务
     *
     * @return the executor service | 执行器服务
     */
    public ExecutorService getExecutor() {
        return executor;
    }
}
