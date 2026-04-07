package cloud.opencode.base.event.security;

import cloud.opencode.base.event.Event;
import cloud.opencode.base.event.EventListener;
import cloud.opencode.base.event.OpenEvent;
import cloud.opencode.base.event.exception.EventSecurityException;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Secure Event Bus
 * 安全事件总线
 *
 * <p>A secure wrapper around OpenEvent that enforces security policies.</p>
 * <p>在OpenEvent周围的安全包装器，强制执行安全策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Listener whitelist - 监听器白名单</li>
 *   <li>Package restrictions - 包限制</li>
 *   <li>Event verification - 事件验证</li>
 *   <li>Rate limiting integration - 频率限制集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SecureEventBus bus = new SecureEventBus();
 * bus.addAllowedPackage("com.myapp.handlers");
 * bus.addToWhitelist(MyHandler.class);
 *
 * bus.register(new MyHandler()); // OK
 * bus.register(new UntrustedHandler()); // throws SecurityException
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
public class SecureEventBus implements AutoCloseable {

    private final OpenEvent eventBus;
    private final Set<Class<?>> allowedListeners;
    private final Set<String> allowedPackages;
    private final EventRateLimiter rateLimiter;
    private final String verificationSecret;

    /**
     * Create secure event bus with default settings
     * 使用默认设置创建安全事件总线
     */
    public SecureEventBus() {
        this(null, null);
    }

    /**
     * Create secure event bus with rate limiter
     * 使用频率限制器创建安全事件总线
     *
     * @param rateLimiter the rate limiter | 频率限制器
     */
    public SecureEventBus(EventRateLimiter rateLimiter) {
        this(rateLimiter, null);
    }

    /**
     * Create secure event bus with rate limiter and verification secret
     * 使用频率限制器和验证密钥创建安全事件总线
     *
     * @param rateLimiter        the rate limiter | 频率限制器
     * @param verificationSecret the secret for verifying signed events | 用于验证签名事件的密钥
     */
    public SecureEventBus(EventRateLimiter rateLimiter, String verificationSecret) {
        this.eventBus = OpenEvent.create();
        this.allowedListeners = ConcurrentHashMap.newKeySet();
        this.allowedPackages = ConcurrentHashMap.newKeySet();
        this.rateLimiter = rateLimiter;
        this.verificationSecret = verificationSecret;
    }

    /**
     * Add a class to the listener whitelist
     * 将类添加到监听器白名单
     *
     * @param listenerClass the listener class to allow | 要允许的监听器类
     */
    public void addToWhitelist(Class<?> listenerClass) {
        allowedListeners.add(listenerClass);
    }

    /**
     * Add an allowed package prefix
     * 添加允许的包前缀
     *
     * @param packageName the package prefix to allow | 要允许的包前缀
     */
    public void addAllowedPackage(String packageName) {
        allowedPackages.add(packageName);
    }

    /**
     * Register a subscriber with security checks
     * 使用安全检查注册订阅者
     *
     * @param subscriber the subscriber to register | 要注册的订阅者
     * @throws EventSecurityException if subscriber is not allowed | 如果订阅者不被允许
     */
    public void register(Object subscriber) {
        Class<?> clazz = subscriber.getClass();

        // Check if class is in allowed packages
        if (!isAllowedPackage(clazz)) {
            throw new EventSecurityException("Listener not in allowed package: " + clazz.getName());
        }

        // Check if class is in whitelist (if whitelist is not empty)
        if (!allowedListeners.isEmpty() && !allowedListeners.contains(clazz)) {
            throw new EventSecurityException("Listener not in whitelist: " + clazz.getName());
        }

        eventBus.register(subscriber);
    }

    /**
     * Register a lambda listener with security checks
     * 使用安全检查注册Lambda监听器
     *
     * @param eventType the event type | 事件类型
     * @param listener  the listener | 监听器
     * @param <E>       the event type parameter | 事件类型参数
     */
    public <E extends Event> void on(Class<E> eventType, EventListener<E> listener) {
        // Lambda listeners are anonymous - validate the declaring class if identifiable
        Class<?> listenerClass = listener.getClass();
        if (!isAllowedPackage(listenerClass)) {
            throw new EventSecurityException(
                    "Lambda listener not in allowed package: " + listenerClass.getName());
        }
        eventBus.on(eventType, listener);
    }

    /**
     * Unregister a subscriber
     * 注销订阅者
     *
     * @param subscriber the subscriber to unregister | 要注销的订阅者
     */
    public void unregister(Object subscriber) {
        eventBus.unregister(subscriber);
    }

    /**
     * Publish an event with security checks
     * 使用安全检查发布事件
     *
     * @param event the event to publish | 要发布的事件
     * @throws EventSecurityException if rate limit exceeded or verification failed | 如果频率限制超出或验证失败
     */
    public void publish(Event event) {
        // Check rate limit
        if (rateLimiter != null && !rateLimiter.allowPublish(event)) {
            throw new EventSecurityException("Rate limit exceeded for event type: " + event.getClass().getName());
        }

        // Verify signed events
        if (verificationSecret != null && event instanceof VerifiableEvent ve) {
            if (!ve.verify(verificationSecret)) {
                throw new EventSecurityException("Event signature verification failed");
            }
        }

        eventBus.publish(event);
    }

    /**
     * Check if a class is in an allowed package
     * 检查类是否在允许的包中
     *
     * @param clazz the class to check | 要检查的类
     * @return true if allowed | 如果允许返回true
     */
    private boolean isAllowedPackage(Class<?> clazz) {
        // If no packages configured, allow all
        if (allowedPackages.isEmpty()) {
            return true;
        }

        String packageName = clazz.getPackageName();
        return allowedPackages.stream().anyMatch(pkg ->
                packageName.equals(pkg) || packageName.startsWith(pkg + "."));
    }


    /**
     * Close the secure event bus, releasing underlying resources.
     * 关闭安全事件总线，释放底层资源。
     */
    @Override
    public void close() {
        eventBus.close();
    }

    /**
     * Get the underlying event bus (CAUTION: bypasses ALL security checks)
     * 获取底层事件总线（注意：绕过所有安全检查）
     *
     * <p><strong>WARNING:</strong> Direct access to the underlying event bus bypasses
     * all security policies (whitelist, rate limiting, signature verification).
     * Use only for advanced integration scenarios where the caller manages its own security.
     * Consider using {@link SecureEventBus} methods instead.</p>
     * <p><strong>警告：</strong>直接访问底层事件总线将绕过所有安全策略（白名单、频率限制、签名验证）。
     * 仅在调用者自行管理安全性的高级集成场景中使用。请优先使用 {@link SecureEventBus} 方法。</p>
     *
     * @apiNote This method exists for advanced integration scenarios only.
     *          The caller is responsible for enforcing security on the returned instance.
     *          此方法仅用于高级集成场景。调用者需自行对返回实例强制执行安全策略。
     * @return the underlying OpenEvent instance | 底层OpenEvent实例
     */
    public OpenEvent getEventBus() {
        return eventBus;
    }
}
