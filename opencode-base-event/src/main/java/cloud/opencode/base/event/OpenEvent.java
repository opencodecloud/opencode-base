package cloud.opencode.base.event;

import cloud.opencode.base.event.annotation.Async;
import cloud.opencode.base.event.annotation.Priority;
import cloud.opencode.base.event.annotation.Subscribe;
import cloud.opencode.base.event.dispatcher.AsyncDispatcher;
import cloud.opencode.base.event.dispatcher.EventDispatcher;
import cloud.opencode.base.event.dispatcher.SyncDispatcher;
import cloud.opencode.base.event.exception.EventException;
import cloud.opencode.base.event.handler.EventExceptionHandler;
import cloud.opencode.base.event.handler.LoggingExceptionHandler;
import cloud.opencode.base.event.interceptor.EventInterceptor;
import cloud.opencode.base.event.monitor.EventBusMetrics;
import cloud.opencode.base.event.store.EventStore;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * OpenEvent - Event Bus with dead event detection, interceptors, sticky events, and metrics
 * 事件总线 - 支持死事件检测、拦截器、粘性事件和指标
 *
 * <p>The main entry point for event-driven architecture support. Provides publish/subscribe
 * pattern with virtual thread async processing, event filtering, interceptor chains,
 * sticky events, subscription handles, dead event detection, and operational metrics.</p>
 * <p>事件驱动架构支持的主入口点。提供基于虚拟线程异步处理的发布/订阅模式，
 * 支持事件过滤、拦截器链、粘性事件、订阅句柄、死事件检测和运行指标。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Publish/Subscribe pattern - 发布/订阅模式</li>
 *   <li>Sync/Async event processing - 同步/异步事件处理</li>
 *   <li>Event priority and cancellation - 事件优先级和取消</li>
 *   <li>Dead event detection - 死事件检测</li>
 *   <li>Subscription handles (AutoCloseable) - 订阅句柄（自动关闭）</li>
 *   <li>Event filtering (Predicate) - 事件过滤（谓词）</li>
 *   <li>Interceptor chain - 拦截器链</li>
 *   <li>Sticky events - 粘性事件</li>
 *   <li>Operational metrics - 运行指标</li>
 *   <li>Virtual thread async processing - 虚拟线程异步处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get default instance
 * OpenEvent eventBus = OpenEvent.getDefault();
 *
 * // Subscribe with Subscription handle
 * Subscription sub = eventBus.subscribe(UserEvent.class, e -> handle(e));
 * sub.unsubscribe(); // precise unsubscribe
 *
 * // Subscribe with filter
 * eventBus.subscribe(OrderEvent.class, e -> handle(e), e -> e.getAmount() > 100);
 *
 * // Dead event monitoring
 * eventBus.subscribe(DeadEvent.class, dead ->
 *     log.warn("Unhandled: {}", dead.getOriginalEvent()));
 *
 * // Sticky events
 * eventBus.publishSticky(new ConfigEvent(config));
 * eventBus.subscribe(ConfigEvent.class, e -> applyConfig(e)); // receives immediately
 *
 * // Interceptors
 * OpenEvent custom = OpenEvent.builder()
 *     .interceptor(new LoggingInterceptor())
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (concurrent data structures) - 线程安全: 是（并发数据结构）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public final class OpenEvent implements AutoCloseable {

    private static final Logger LOGGER = System.getLogger(OpenEvent.class.getName());

    private static final OpenEvent INSTANCE = new OpenEvent();

    private final Map<Class<?>, List<ListenerInfo>> listeners;
    private final ExecutorService asyncExecutor;
    private final boolean ownsExecutor;
    private final EventDispatcher syncDispatcher;
    private final EventDispatcher asyncDispatcher;
    private final List<EventInterceptor> interceptors;
    private final Map<Class<?>, Event> stickyEvents;
    private volatile EventStore eventStore;
    private volatile EventExceptionHandler exceptionHandler;

    // Listener lookup cache: eventType -> sorted listener list (invalidated on registration changes)
    // 监听器查找缓存：事件类型 -> 已排序监听器列表（注册变更时失效）
    private final Map<Class<?>, List<ListenerInfo>> listenerCache = new ConcurrentHashMap<>();

    // Metrics counters - LongAdder for high-throughput contention-free increment
    private final LongAdder publishedCount = new LongAdder();
    private final LongAdder deliveredCount = new LongAdder();
    private final LongAdder errorCount = new LongAdder();
    private final LongAdder deadEventCount = new LongAdder();

    // Listener count tracker - avoids stream iteration in getMetrics()
    private final AtomicInteger listenerCount = new AtomicInteger();

    /**
     * Private constructor for singleton
     * 单例私有构造函数
     */
    private OpenEvent() {
        this.listeners = new ConcurrentHashMap<>();
        this.asyncExecutor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("event-", 0).factory()
        );
        this.ownsExecutor = true;
        this.syncDispatcher = new SyncDispatcher();
        this.asyncDispatcher = new AsyncDispatcher(asyncExecutor);
        this.exceptionHandler = new LoggingExceptionHandler();
        this.interceptors = new CopyOnWriteArrayList<>();
        this.stickyEvents = new ConcurrentHashMap<>();
    }

    /**
     * Private constructor for builder
     * Builder私有构造函数
     */
    private OpenEvent(Builder builder) {
        this.listeners = new ConcurrentHashMap<>();
        this.ownsExecutor = (builder.asyncExecutor == null);
        this.asyncExecutor = builder.asyncExecutor != null
            ? builder.asyncExecutor
            : Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("event-", 0).factory()
            );
        this.syncDispatcher = builder.syncDispatcher != null
            ? builder.syncDispatcher
            : new SyncDispatcher();
        this.asyncDispatcher = builder.asyncDispatcher != null
            ? builder.asyncDispatcher
            : new AsyncDispatcher(asyncExecutor);
        this.eventStore = builder.eventStore;
        this.exceptionHandler = builder.exceptionHandler != null
            ? builder.exceptionHandler
            : new LoggingExceptionHandler();
        this.interceptors = new CopyOnWriteArrayList<>(builder.interceptors);
        this.stickyEvents = new ConcurrentHashMap<>();
    }

    // ============ Factory Methods | 工厂方法 ============

    /**
     * Get the default singleton instance
     * 获取默认单例实例
     *
     * @return the default OpenEvent instance | 默认OpenEvent实例
     */
    public static OpenEvent getDefault() {
        return INSTANCE;
    }

    /**
     * Create a new OpenEvent instance
     * 创建新的OpenEvent实例
     *
     * @return new OpenEvent instance | 新的OpenEvent实例
     */
    public static OpenEvent create() {
        return new OpenEvent();
    }

    /**
     * Create a builder for custom configuration
     * 创建用于自定义配置的构建器
     *
     * @return new Builder | 新的Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    // ============ Register Listeners | 注册监听器 ============

    /**
     * Register an object's @Subscribe methods as event listeners
     * 将对象的@Subscribe方法注册为事件监听器
     *
     * @param subscriber the subscriber object | 订阅者对象
     * @throws EventException if method signature is invalid | 如果方法签名无效
     */
    public void register(Object subscriber) {
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber cannot be null");
        }

        for (Method method : subscriber.getClass().getDeclaredMethods()) {
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            if (subscribe == null) {
                continue;
            }

            if (method.getParameterCount() != 1) {
                throw new EventException(
                    "@Subscribe method must have exactly one parameter: " + method.getName());
            }

            Class<?> eventType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(eventType)) {
                throw new EventException(
                    "@Subscribe method parameter must extend Event: " + method.getName());
            }

            boolean async = method.isAnnotationPresent(Async.class);
            int priority = getPriority(method);

            // Set accessible once at registration, not on every invoke
            method.setAccessible(true);

            ListenerInfo info = new ListenerInfo(subscriber, method, null, eventType, async, priority, null);
            addListener(eventType, info);
        }
    }

    /**
     * Register a lambda listener for an event type (legacy void return)
     * 为事件类型注册Lambda监听器（兼容旧接口，无返回值）
     *
     * @param eventType the event type class | 事件类型类
     * @param listener  the event listener | 事件监听器
     * @param <E>       the event type parameter | 事件类型参数
     */
    public <E extends Event> void on(Class<E> eventType, EventListener<E> listener) {
        on(eventType, listener, false, 0);
    }

    /**
     * Register a lambda listener with async option (legacy void return)
     * 使用异步选项注册Lambda监听器（兼容旧接口，无返回值）
     *
     * @param eventType the event type class | 事件类型类
     * @param listener  the event listener | 事件监听器
     * @param async     true for async execution | 异步执行为true
     * @param <E>       the event type parameter | 事件类型参数
     */
    public <E extends Event> void on(Class<E> eventType, EventListener<E> listener, boolean async) {
        on(eventType, listener, async, 0);
    }

    /**
     * Register a lambda listener with async and priority options (legacy void return)
     * 使用异步和优先级选项注册Lambda监听器（兼容旧接口，无返回值）
     *
     * @param eventType the event type class | 事件类型类
     * @param listener  the event listener | 事件监听器
     * @param async     true for async execution | 异步执行为true
     * @param priority  listener priority (higher = earlier) | 监听器优先级（越高越早）
     * @param <E>       the event type parameter | 事件类型参数
     */
    public <E extends Event> void on(Class<E> eventType, EventListener<E> listener,
                                      boolean async, int priority) {
        if (eventType == null || listener == null) {
            throw new IllegalArgumentException("EventType and listener cannot be null");
        }

        ListenerInfo info = new ListenerInfo(listener, null, listener, eventType, async, priority, null);
        addListener(eventType, info);
    }

    /**
     * Subscribe a listener and return a Subscription handle
     * 订阅监听器并返回订阅句柄
     *
     * @param eventType the event type class | 事件类型类
     * @param listener  the event listener | 事件监听器
     * @param <E>       the event type parameter | 事件类型参数
     * @return subscription handle for lifecycle management | 用于生命周期管理的订阅句柄
     */
    public <E extends Event> Subscription subscribe(Class<E> eventType, EventListener<E> listener) {
        return subscribe(eventType, listener, null, false, 0);
    }

    /**
     * Subscribe a listener with a filter predicate
     * 使用过滤谓词订阅监听器
     *
     * @param eventType the event type class | 事件类型类
     * @param listener  the event listener | 事件监听器
     * @param filter    predicate to filter events, null means accept all | 过滤事件的谓词，null 表示接受全部
     * @param <E>       the event type parameter | 事件类型参数
     * @return subscription handle | 订阅句柄
     */
    public <E extends Event> Subscription subscribe(Class<E> eventType, EventListener<E> listener,
                                                     Predicate<E> filter) {
        return subscribe(eventType, listener, filter, false, 0);
    }

    /**
     * Subscribe a listener with full configuration
     * 使用完整配置订阅监听器
     *
     * @param eventType the event type class | 事件类型类
     * @param listener  the event listener | 事件监听器
     * @param filter    predicate to filter events, null means accept all | 过滤事件的谓词，null 表示接受全部
     * @param async     true for async execution | 异步执行为true
     * @param priority  listener priority (higher = earlier) | 监听器优先级（越高越早）
     * @param <E>       the event type parameter | 事件类型参数
     * @return subscription handle | 订阅句柄
     */
    @SuppressWarnings("unchecked")
    public <E extends Event> Subscription subscribe(Class<E> eventType, EventListener<E> listener,
                                                     Predicate<E> filter,
                                                     boolean async, int priority) {
        if (eventType == null || listener == null) {
            throw new IllegalArgumentException("EventType and listener cannot be null");
        }

        Predicate<Event> rawFilter = filter != null ? e -> filter.test((E) e) : null;
        ListenerInfo info = new ListenerInfo(listener, null, listener, eventType, async, priority, rawFilter);
        addListener(eventType, info);

        // Deliver sticky event if available (goes through interceptor chain)
        Event sticky = stickyEvents.get(eventType);
        if (sticky != null && !sticky.isCancelled()
                && (rawFilter == null || rawFilter.test(sticky))) {
            if (runBeforeInterceptors(sticky)) {
                try {
                    ((EventListener<Event>) listener).onEvent(sticky);
                    deliveredCount.increment();
                    runAfterInterceptors(sticky, true);
                } catch (Exception e) {
                    errorCount.increment();
                    if (exceptionHandler != null) {
                        exceptionHandler.handleException(sticky, e, "StickyDelivery");
                    }
                    runAfterInterceptors(sticky, false);
                }
            }
        }

        return new SubscriptionImpl(eventType, info);
    }

    /**
     * Unregister all listeners from a subscriber
     * 从订阅者注销所有监听器
     *
     * @param subscriber the subscriber to unregister | 要注销的订阅者
     */
    public void unregister(Object subscriber) {
        if (subscriber == null) {
            return;
        }

        listeners.values().forEach(list -> {
            // Count actual removals explicitly to avoid TOCTOU on list.size()
            int removed = 0;
            var it = list.iterator();
            while (it.hasNext()) {
                if (it.next().subscriber() == subscriber) {
                    removed++;
                }
            }
            if (removed > 0) {
                list.removeIf(info -> info.subscriber() == subscriber);
                listenerCount.addAndGet(-removed);
            }
        });
        invalidateListenerCache();
    }

    // ============ Publish Events | 发布事件 ============

    /**
     * Publish an event synchronously
     * 同步发布事件
     *
     * @param event the event to publish | 要发布的事件
     */
    public void publish(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        publishedCount.increment();

        // Run interceptors beforePublish
        if (!runBeforeInterceptors(event)) {
            completeWaitableEvent(event);
            return;
        }

        // Store event if store is configured (best-effort, does not block dispatch)
        if (eventStore != null) {
            try {
                eventStore.save(event);
            } catch (Exception e) {
                errorCount.increment();
                if (exceptionHandler != null) {
                    exceptionHandler.handleException(event, e, "EventStore");
                }
            }
        }

        boolean dispatched = dispatch(event, false);

        // Run interceptors afterPublish
        runAfterInterceptors(event, dispatched);
    }

    /**
     * Publish an event asynchronously
     * 异步发布事件
     *
     * @param event the event to publish | 要发布的事件
     * @return CompletableFuture that completes when processing is done | 处理完成时完成的CompletableFuture
     */
    public CompletableFuture<Void> publishAsync(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        publishedCount.increment();

        // Run interceptors beforePublish
        if (!runBeforeInterceptors(event)) {
            completeWaitableEvent(event);
            return CompletableFuture.completedFuture(null);
        }

        // Store event if store is configured (best-effort, does not block dispatch)
        if (eventStore != null) {
            try {
                eventStore.save(event);
            } catch (Exception e) {
                errorCount.increment();
                if (exceptionHandler != null) {
                    exceptionHandler.handleException(event, e, "EventStore");
                }
            }
        }

        // Get listeners and convert to consumers
        List<Consumer<Event>> listenerConsumers = getMatchedListenerConsumers(event);
        if (listenerConsumers.isEmpty()) {
            handleDeadEvent(event);
            completeWaitableEvent(event);
            runAfterInterceptors(event, false);
            return CompletableFuture.completedFuture(null);
        }

        // Use asyncDispatcher.dispatchAsync() for proper Future handling
        if (asyncDispatcher instanceof AsyncDispatcher ad) {
            try {
                return ad.dispatchAsync(event, listenerConsumers)
                        .whenComplete((_, _) -> {
                            completeWaitableEvent(event);
                            runAfterInterceptors(event, true);
                        });
            } catch (Exception e) {
                completeWaitableEvent(event);
                runAfterInterceptors(event, false);
                return CompletableFuture.failedFuture(e);
            }
        }

        // Fallback for custom dispatchers
        return CompletableFuture.runAsync(() -> {
            asyncDispatcher.dispatch(event, listenerConsumers);
            completeWaitableEvent(event);
            runAfterInterceptors(event, true);
        }, asyncExecutor);
    }

    /**
     * Publish data as a DataEvent
     * 将数据作为DataEvent发布
     *
     * @param data the data to publish | 要发布的数据
     * @param <T>  the data type | 数据类型
     */
    public <T> void publish(T data) {
        publish(new DataEvent<>(data));
    }

    /**
     * Publish data as a DataEvent with source
     * 将数据作为带来源的DataEvent发布
     *
     * @param data   the data to publish | 要发布的数据
     * @param source the event source | 事件来源
     * @param <T>    the data type | 数据类型
     */
    public <T> void publish(T data, String source) {
        publish(new DataEvent<>(data, source));
    }

    /**
     * Publish and wait for processing to complete
     * 发布并等待处理完成
     *
     * @param event   the event to publish | 要发布的事件
     * @param timeout max time to wait | 最大等待时间
     * @return true if completed within timeout | 如果在超时内完成返回true
     */
    public boolean publishAndWait(Event event, Duration timeout) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        CountDownLatch latch = new CountDownLatch(1);
        WaitableEvent waitableEvent = new WaitableEvent(event, latch);

        // Dispatch using the wrapped event's type so that listeners for the original event type match.
        // Use getMatchedListeners on the original event, but track completion via the WaitableEvent.
        publishedCount.increment();

        if (!runBeforeInterceptors(event)) {
            waitableEvent.complete();
            return true;
        }

        if (eventStore != null) {
            try {
                eventStore.save(event);
            } catch (Exception e) {
                errorCount.increment();
                if (exceptionHandler != null) {
                    exceptionHandler.handleException(event, e, "EventStore");
                }
            }
        }

        List<ListenerInfo> matchedListeners = getMatchedListeners(event);
        if (matchedListeners.isEmpty()) {
            handleDeadEvent(event);
            waitableEvent.complete();
            return true;
        }

        // Separate sync and async listeners
        List<Consumer<Event>> syncConsumers = new ArrayList<>();
        List<Consumer<Event>> asyncConsumers = new ArrayList<>();
        for (ListenerInfo info : matchedListeners) {
            Consumer<Event> consumer = toConsumer(info);
            if (info.async()) {
                asyncConsumers.add(consumer);
            } else {
                syncConsumers.add(consumer);
            }
        }

        // Dispatch sync listeners
        if (!syncConsumers.isEmpty()) {
            syncDispatcher.dispatch(event, syncConsumers);
        }

        // Dispatch async listeners and wait for completion
        if (!asyncConsumers.isEmpty() && asyncDispatcher instanceof AsyncDispatcher ad) {
            try {
                ad.dispatchAsync(event, asyncConsumers)
                    .whenComplete((_, _) -> {
                        runAfterInterceptors(event, true);
                        waitableEvent.complete();
                    });
            } catch (Exception e) {
                waitableEvent.complete();
                throw e;
            }
        } else {
            if (!asyncConsumers.isEmpty()) {
                asyncDispatcher.dispatch(event, asyncConsumers);
            }
            runAfterInterceptors(event, true);
            waitableEvent.complete();
        }

        try {
            return latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // ============ Sticky Events | 粘性事件 ============

    /**
     * Publish a sticky event (stored and replayed to future subscribers)
     * 发布粘性事件（存储并重放给未来的订阅者）
     *
     * <p>The event is stored per-type (last one wins) and also published to current listeners.
     * Future subscribers for this event type will immediately receive the stored sticky event.</p>
     * <p>事件按类型存储（最后一个生效），并同时发布给当前监听器。
     * 该事件类型的未来订阅者将立即收到存储的粘性事件。</p>
     *
     * @param event the event to publish as sticky | 要作为粘性事件发布的事件
     */
    public void publishSticky(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        stickyEvents.put(event.getClass(), event);
        publish(event);
    }

    /**
     * Get the last sticky event of the given type
     * 获取指定类型的最后一个粘性事件
     *
     * @param eventType the event type class | 事件类型类
     * @param <E>       the event type | 事件类型
     * @return the sticky event or null if none | 粘性事件，如果没有则为 null
     */
    @SuppressWarnings("unchecked")
    public <E extends Event> E getStickyEvent(Class<E> eventType) {
        return (E) stickyEvents.get(eventType);
    }

    /**
     * Remove and return the sticky event of the given type
     * 移除并返回指定类型的粘性事件
     *
     * @param eventType the event type class | 事件类型类
     * @param <E>       the event type | 事件类型
     * @return the removed sticky event or null | 移除的粘性事件，如果没有则为 null
     */
    @SuppressWarnings("unchecked")
    public <E extends Event> E removeStickyEvent(Class<E> eventType) {
        return (E) stickyEvents.remove(eventType);
    }

    /**
     * Remove all sticky events, releasing references for GC.
     * 移除所有粘性事件，释放引用以便 GC。
     *
     * <p>Sticky events are retained indefinitely until explicitly removed.
     * Call this method periodically or on shutdown to prevent memory leaks
     * in long-running applications.</p>
     * <p>粘性事件会一直保留直到被显式移除。
     * 在长时间运行的应用中定期调用或在关闭时调用此方法以防止内存泄漏。</p>
     *
     * @since JDK 25, opencode-base-event V1.0.3
     */
    public void clearAllStickyEvents() {
        stickyEvents.clear();
    }

    // ============ Interceptors | 拦截器 ============

    /**
     * Add an event interceptor
     * 添加事件拦截器
     *
     * @param interceptor the interceptor to add | 要添加的拦截器
     */
    public void addInterceptor(EventInterceptor interceptor) {
        if (interceptor == null) {
            throw new IllegalArgumentException("Interceptor cannot be null");
        }
        interceptors.add(interceptor);
    }

    /**
     * Remove an event interceptor
     * 移除事件拦截器
     *
     * @param interceptor the interceptor to remove | 要移除的拦截器
     */
    public void removeInterceptor(EventInterceptor interceptor) {
        interceptors.remove(interceptor);
    }

    // ============ Metrics | 指标 ============

    /**
     * Get current event bus metrics snapshot
     * 获取当前事件总线指标快照
     *
     * @return metrics snapshot | 指标快照
     */
    public EventBusMetrics getMetrics() {
        return new EventBusMetrics(
                publishedCount.sum(),
                deliveredCount.sum(),
                errorCount.sum(),
                deadEventCount.sum(),
                listenerCount.get()
        );
    }

    /**
     * Reset all metrics counters
     * 重置所有指标计数器
     */
    public void resetMetrics() {
        publishedCount.reset();
        deliveredCount.reset();
        errorCount.reset();
        deadEventCount.reset();
    }

    // ============ Configuration | 配置 ============

    /**
     * Set the event store
     * 设置事件存储
     *
     * @param store the event store | 事件存储
     */
    public void setEventStore(EventStore store) {
        this.eventStore = store;
    }

    /**
     * Get the event store
     * 获取事件存储
     *
     * @return the event store or null | 事件存储或null
     */
    public EventStore getEventStore() {
        return eventStore;
    }

    /**
     * Set the exception handler
     * 设置异常处理器
     *
     * @param handler the exception handler | 异常处理器
     */
    public void setExceptionHandler(EventExceptionHandler handler) {
        this.exceptionHandler = handler != null ? handler : new LoggingExceptionHandler();
    }

    // ============ Lifecycle | 生命周期 ============

    /**
     * Shuts down the async executor and dispatchers, releasing resources.
     * 关闭异步执行器和分发器，释放资源。
     */
    @Override
    public void close() {
        if (this == INSTANCE) {
            LOGGER.log(Level.WARNING,
                    "Cannot close the default singleton OpenEvent instance; ignoring close()");
            return;
        }
        syncDispatcher.shutdown();
        asyncDispatcher.shutdown();
        // Only shut down the executor if we own it (not user-provided via builder)
        if (ownsExecutor) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                asyncExecutor.shutdownNow();
            }
        }
        // Release cached data to help GC
        listenerCache.clear();
        stickyEvents.clear();
    }

    // ============ Internal Methods | 内部方法 ============

    private void addListener(Class<?> eventType, ListenerInfo info) {
        listeners.computeIfAbsent(eventType, _ -> new CopyOnWriteArrayList<>()).add(info);
        listenerCount.incrementAndGet();
        // Priority sorting is handled by computeTypeMatchedListeners (on cache miss).
        // Sorting here on CopyOnWriteArrayList is racy under concurrent registration.
        invalidateListenerCache();
    }

    private boolean dispatch(Event event, boolean forceAsync) {
        // Get all matching listeners and convert to consumers
        List<ListenerInfo> matchedListeners = getMatchedListeners(event);
        if (matchedListeners.isEmpty()) {
            handleDeadEvent(event);
            completeWaitableEvent(event);
            return false;
        }

        if (forceAsync) {
            // All listeners execute async
            List<Consumer<Event>> consumers = matchedListeners.stream()
                    .map(this::toConsumer)
                    .toList();
            asyncDispatcher.dispatch(event, consumers);
            completeWaitableEvent(event);
        } else {
            // Separate sync and async listeners
            List<Consumer<Event>> syncConsumers = new ArrayList<>();
            List<Consumer<Event>> asyncConsumers = new ArrayList<>();

            for (ListenerInfo info : matchedListeners) {
                if (info.async()) {
                    asyncConsumers.add(toConsumer(info));
                } else {
                    syncConsumers.add(toConsumer(info));
                }
            }

            // Dispatch sync listeners using syncDispatcher
            if (!syncConsumers.isEmpty()) {
                syncDispatcher.dispatch(event, syncConsumers);
            }

            // Dispatch async listeners using asyncDispatcher
            if (!asyncConsumers.isEmpty()) {
                if (event instanceof WaitableEvent && asyncDispatcher instanceof AsyncDispatcher ad) {
                    try {
                        ad.dispatchAsync(event, asyncConsumers)
                            .whenComplete((_, _) -> completeWaitableEvent(event));
                        return true;
                    } catch (Exception e) {
                        completeWaitableEvent(event);
                        throw e;
                    }
                }
                asyncDispatcher.dispatch(event, asyncConsumers);
            }

            completeWaitableEvent(event);
        }
        return true;
    }

    private void handleDeadEvent(Event event) {
        // Prevent recursive dead events: if this IS a DeadEvent, don't wrap again
        if (event instanceof DeadEvent || event instanceof WaitableEvent) {
            return;
        }
        deadEventCount.increment();
        // Only create DeadEvent if someone is listening for it
        List<ListenerInfo> deadListenerList = listeners.get(DeadEvent.class);
        if (deadListenerList != null && !deadListenerList.isEmpty()) {
            DeadEvent deadEvent = new DeadEvent(event);
            List<ListenerInfo> deadListeners = getMatchedListeners(deadEvent);
            for (ListenerInfo info : deadListeners) {
                try {
                    info.invoke(deadEvent);
                    deliveredCount.increment();
                } catch (Exception e) {
                    errorCount.increment();
                    if (exceptionHandler != null) {
                        exceptionHandler.handleException(deadEvent, e, "DeadEventListener");
                    }
                }
            }
        }
    }

    private List<ListenerInfo> getMatchedListeners(Event event) {
        Class<?> eventType = event.getClass();

        // Use cached type-matched listeners (sorted, invalidated on registration changes)
        List<ListenerInfo> typeMatched = listenerCache.computeIfAbsent(eventType, this::computeTypeMatchedListeners);

        // Fast path: if no listeners have filters, return cached list directly
        boolean hasFilters = false;
        for (ListenerInfo info : typeMatched) {
            if (info.filter() != null) {
                hasFilters = true;
                break;
            }
        }
        if (!hasFilters) {
            return typeMatched;
        }

        // Slow path: apply event-specific filter predicates
        List<ListenerInfo> filtered = new ArrayList<>(typeMatched.size());
        for (ListenerInfo info : typeMatched) {
            if (info.filter() == null || info.filter().test(event)) {
                filtered.add(info);
            }
        }
        return filtered;
    }

    private List<ListenerInfo> computeTypeMatchedListeners(Class<?> eventType) {
        List<ListenerInfo> matched = new ArrayList<>();
        for (Map.Entry<Class<?>, List<ListenerInfo>> entry : listeners.entrySet()) {
            if (entry.getKey().isAssignableFrom(eventType)) {
                matched.addAll(entry.getValue());
            }
        }
        matched.sort(Comparator.comparingInt(ListenerInfo::priority).reversed());
        return List.copyOf(matched);
    }

    private void invalidateListenerCache() {
        listenerCache.clear();
    }

    private List<Consumer<Event>> getMatchedListenerConsumers(Event event) {
        return getMatchedListeners(event).stream()
                .map(this::toConsumer)
                .toList();
    }

    private Consumer<Event> toConsumer(ListenerInfo info) {
        return e -> invokeListener(info, e);
    }

    private void completeWaitableEvent(Event event) {
        if (event instanceof WaitableEvent we) {
            we.complete();
        }
    }

    private void invokeListener(ListenerInfo info, Event event) {
        try {
            info.invoke(event);
            deliveredCount.increment();
        } catch (Exception e) {
            errorCount.increment();
            if (exceptionHandler != null) {
                String listenerName = info.subscriber() != null
                    ? info.subscriber().getClass().getSimpleName()
                    : "Lambda";
                try {
                    exceptionHandler.handleException(event, e, listenerName);
                } catch (Exception handlerEx) {
                    // Exception handler itself failed - log and swallow to protect the event bus
                    // 异常处理器自身异常 - 记录并吞掉以保护事件总线
                    LOGGER.log(Level.ERROR,
                            "Exception handler failed for listener ''{0}'': {1}",
                            listenerName, handlerEx.getMessage());
                }
            }
        }
    }

    private boolean runBeforeInterceptors(Event event) {
        for (EventInterceptor interceptor : interceptors) {
            try {
                if (!interceptor.beforePublish(event)) {
                    return false;
                }
            } catch (Exception e) {
                // Fail-closed: interceptor exception blocks publishing (safer default)
                // 安全默认：拦截器异常阻止发布
                LOGGER.log(Level.WARNING, "Interceptor beforePublish failed, blocking event", e);
                return false;
            }
        }
        return true;
    }

    private void runAfterInterceptors(Event event, boolean dispatched) {
        for (EventInterceptor interceptor : interceptors) {
            try {
                interceptor.afterPublish(event, dispatched);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Interceptor afterPublish failed", e);
            }
        }
    }

    private int getPriority(Method method) {
        Priority priority = method.getAnnotation(Priority.class);
        return priority != null ? priority.value() : 0;
    }

    // ============ Builder | 构建器 ============

    /**
     * Builder for OpenEvent
     * OpenEvent构建器
     */
    public static class Builder {
        private ExecutorService asyncExecutor;
        private EventDispatcher syncDispatcher;
        private EventDispatcher asyncDispatcher;
        private EventStore eventStore;
        private EventExceptionHandler exceptionHandler;
        private final List<EventInterceptor> interceptors = new ArrayList<>();

        /**
         * Set the async executor
         * 设置异步执行器
         *
         * @param executor the executor service | 执行器服务
         * @return this builder | 此构建器
         */
        public Builder asyncExecutor(ExecutorService executor) {
            this.asyncExecutor = executor;
            return this;
        }

        /**
         * Set the sync dispatcher
         * 设置同步分发器
         *
         * @param dispatcher the sync dispatcher | 同步分发器
         * @return this builder | 此构建器
         */
        public Builder syncDispatcher(EventDispatcher dispatcher) {
            this.syncDispatcher = dispatcher;
            return this;
        }

        /**
         * Set the async dispatcher
         * 设置异步分发器
         *
         * @param dispatcher the async dispatcher | 异步分发器
         * @return this builder | 此构建器
         */
        public Builder asyncDispatcher(EventDispatcher dispatcher) {
            this.asyncDispatcher = dispatcher;
            return this;
        }

        /**
         * Set the event store
         * 设置事件存储
         *
         * @param store the event store | 事件存储
         * @return this builder | 此构建器
         */
        public Builder eventStore(EventStore store) {
            this.eventStore = store;
            return this;
        }

        /**
         * Set the exception handler
         * 设置异常处理器
         *
         * @param handler the exception handler | 异常处理器
         * @return this builder | 此构建器
         */
        public Builder exceptionHandler(EventExceptionHandler handler) {
            this.exceptionHandler = handler;
            return this;
        }

        /**
         * Add an event interceptor
         * 添加事件拦截器
         *
         * @param interceptor the interceptor to add | 要添加的拦截器
         * @return this builder | 此构建器
         */
        public Builder interceptor(EventInterceptor interceptor) {
            if (interceptor != null) {
                this.interceptors.add(interceptor);
            }
            return this;
        }

        /**
         * Build the OpenEvent instance
         * 构建OpenEvent实例
         *
         * @return new OpenEvent instance | 新的OpenEvent实例
         */
        public OpenEvent build() {
            return new OpenEvent(this);
        }
    }

    // ============ ListenerInfo Record | 监听器信息记录 ============

    /**
     * Listener information record
     * 监听器信息记录
     */
    private record ListenerInfo(
        Object subscriber,
        Method method,
        EventListener<?> lambdaListener,
        Class<?> eventType,
        boolean async,
        int priority,
        Predicate<Event> filter
    ) {
        @SuppressWarnings("unchecked")
        void invoke(Event event) throws Exception {
            if (lambdaListener != null) {
                ((EventListener<Event>) lambdaListener).onEvent(event);
            } else if (method != null) {
                method.invoke(subscriber, event);
            }
        }
    }

    // ============ Subscription Implementation | 订阅实现 ============

    /**
     * Internal subscription implementation
     * 内部订阅实现
     */
    private final class SubscriptionImpl implements Subscription {
        private final Class<? extends Event> eventType;
        private final ListenerInfo listenerInfo;
        private final AtomicBoolean active = new AtomicBoolean(true);

        @SuppressWarnings("unchecked")
        SubscriptionImpl(Class<?> eventType, ListenerInfo listenerInfo) {
            this.eventType = (Class<? extends Event>) eventType;
            this.listenerInfo = listenerInfo;
        }

        @Override
        public void unsubscribe() {
            if (active.compareAndSet(true, false)) {
                List<ListenerInfo> list = listeners.get(eventType);
                if (list != null && list.remove(listenerInfo)) {
                    listenerCount.decrementAndGet();
                    invalidateListenerCache();
                }
            }
        }

        @Override
        public boolean isActive() {
            return active.get();
        }

        @Override
        public Class<? extends Event> getEventType() {
            return eventType;
        }
    }
}
