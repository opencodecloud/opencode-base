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
import cloud.opencode.base.event.store.EventStore;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * OpenEvent - Event Bus
 * 事件总线
 *
 * <p>The main entry point for event-driven architecture support.</p>
 * <p>事件驱动架构支持的主入口点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Publish/Subscribe pattern - 发布/订阅模式</li>
 *   <li>Sync/Async event processing - 同步/异步事件处理</li>
 *   <li>Event priority - 事件优先级</li>
 *   <li>Event cancellation - 事件取消</li>
 *   <li>Event sourcing support - 事件溯源支持</li>
 *   <li>Virtual thread async processing - 虚拟线程异步处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get default instance
 * OpenEvent eventBus = OpenEvent.getDefault();
 *
 * // Register annotation-based listener
 * eventBus.register(new MyEventHandler());
 *
 * // Register lambda listener
 * eventBus.on(UserRegisteredEvent.class, event -> {
 *     System.out.println("User registered: " + event.getUserId());
 * });
 *
 * // Publish event
 * eventBus.publish(new UserRegisteredEvent(userId, email));
 *
 * // Publish async
 * eventBus.publishAsync(event).thenRun(() -> log.info("Done"));
 *
 * // Create custom instance
 * OpenEvent custom = OpenEvent.builder()
 *     .eventStore(new InMemoryEventStore(10000))
 *     .exceptionHandler(new LoggingExceptionHandler())
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

    private static final OpenEvent INSTANCE = new OpenEvent();

    private final Map<Class<?>, List<ListenerInfo>> listeners;
    private final ExecutorService asyncExecutor;
    private final EventDispatcher syncDispatcher;
    private final EventDispatcher asyncDispatcher;
    private EventStore eventStore;
    private EventExceptionHandler exceptionHandler;

    /**
     * Private constructor for singleton
     * 单例私有构造函数
     */
    private OpenEvent() {
        this.listeners = new ConcurrentHashMap<>();
        this.asyncExecutor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("event-", 0).factory()
        );
        this.syncDispatcher = new SyncDispatcher();
        this.asyncDispatcher = new AsyncDispatcher(asyncExecutor);
        this.exceptionHandler = new LoggingExceptionHandler();
    }

    /**
     * Private constructor for builder
     * Builder私有构造函数
     */
    private OpenEvent(Builder builder) {
        this.listeners = new ConcurrentHashMap<>();
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

            ListenerInfo info = new ListenerInfo(subscriber, method, null, eventType, async, priority);
            addListener(eventType, info);
        }
    }

    /**
     * Register a lambda listener for an event type
     * 为事件类型注册Lambda监听器
     *
     * @param eventType the event type class | 事件类型类
     * @param listener  the event listener | 事件监听器
     * @param <E>       the event type parameter | 事件类型参数
     */
    public <E extends Event> void on(Class<E> eventType, EventListener<E> listener) {
        on(eventType, listener, false, 0);
    }

    /**
     * Register a lambda listener with async option
     * 使用异步选项注册Lambda监听器
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
     * Register a lambda listener with async and priority options
     * 使用异步和优先级选项注册Lambda监听器
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

        ListenerInfo info = new ListenerInfo(listener, null, listener, eventType, async, priority);
        addListener(eventType, info);
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

        listeners.values().forEach(list ->
            list.removeIf(info -> info.subscriber() == subscriber));
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

        // Store event if store is configured
        if (eventStore != null) {
            eventStore.save(event);
        }

        dispatch(event, false);
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

        // Store event if store is configured
        if (eventStore != null) {
            eventStore.save(event);
        }

        // Get listeners and convert to consumers
        List<Consumer<Event>> listenerConsumers = getMatchedListenerConsumers(event);
        if (listenerConsumers.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        // Use asyncDispatcher.dispatchAsync() for proper Future handling
        if (asyncDispatcher instanceof AsyncDispatcher ad) {
            try {
                return ad.dispatchAsync(event, listenerConsumers)
                        .whenComplete((_, _) -> completeWaitableEvent(event));
            } catch (Exception e) {
                completeWaitableEvent(event);
                return CompletableFuture.failedFuture(e);
            }
        }

        // Fallback for custom dispatchers
        return CompletableFuture.runAsync(() -> {
            asyncDispatcher.dispatch(event, listenerConsumers);
            completeWaitableEvent(event);
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

        publish(waitableEvent);

        try {
            return latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
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
        syncDispatcher.shutdown();
        asyncDispatcher.shutdown();
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

    // ============ Internal Methods | 内部方法 ============

    private void addListener(Class<?> eventType, ListenerInfo info) {
        listeners.computeIfAbsent(eventType, _ -> new CopyOnWriteArrayList<>()).add(info);
        // Sort by priority (descending)
        listeners.get(eventType).sort(Comparator.comparingInt(ListenerInfo::priority).reversed());
    }

    private void dispatch(Event event, boolean forceAsync) {
        // Get all matching listeners and convert to consumers
        List<ListenerInfo> matchedListeners = getMatchedListeners(event);
        if (matchedListeners.isEmpty()) {
            completeWaitableEvent(event);
            return;
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
                    // For waitable events, wait for async listeners to complete before
                    // signaling the latch, otherwise publishAndWait() returns prematurely
                    try {
                        ad.dispatchAsync(event, asyncConsumers)
                            .whenComplete((_, _) -> completeWaitableEvent(event));
                        return;
                    } catch (Exception e) {
                        completeWaitableEvent(event);
                        throw e;
                    }
                }
                asyncDispatcher.dispatch(event, asyncConsumers);
            }

            completeWaitableEvent(event);
        }
    }

    private List<ListenerInfo> getMatchedListeners(Event event) {
        Class<?> eventType = event.getClass();

        List<ListenerInfo> matchedListeners = new ArrayList<>();
        for (Map.Entry<Class<?>, List<ListenerInfo>> entry : listeners.entrySet()) {
            if (entry.getKey().isAssignableFrom(eventType)) {
                matchedListeners.addAll(entry.getValue());
            }
        }

        // Sort by priority (descending)
        matchedListeners.sort(Comparator.comparingInt(ListenerInfo::priority).reversed());
        return matchedListeners;
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
        } catch (Exception e) {
            if (exceptionHandler != null) {
                String listenerName = info.subscriber() != null
                    ? info.subscriber().getClass().getSimpleName()
                    : "Lambda";
                exceptionHandler.handleException(event, e, listenerName);
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
        int priority
    ) {
        @SuppressWarnings("unchecked")
        void invoke(Event event) throws Exception {
            if (lambdaListener != null) {
                ((EventListener<Event>) lambdaListener).onEvent(event);
            } else if (method != null) {
                method.setAccessible(true);
                method.invoke(subscriber, event);
            }
        }
    }
}
