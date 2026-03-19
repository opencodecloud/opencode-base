package cloud.opencode.base.config.jdk25;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigListener;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Reactive Configuration Value
 * 响应式配置值
 *
 * <p>Provides automatic update notification when configuration values change.
 * Uses virtual threads for subscriber notification.</p>
 * <p>当配置值变化时提供自动更新通知。使用虚拟线程进行订阅者通知。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic value updates - 自动值更新</li>
 *   <li>Subscriber notification - 订阅者通知</li>
 *   <li>Virtual thread execution - 虚拟线程执行</li>
 *   <li>Type-safe access - 类型安全访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ReactiveConfigValue<String> logLevel = ReactiveConfigValue
 *     .of(config, "log.level", String.class, "INFO")
 *     .subscribe(level -> updateLogLevel(level));
 *
 * // Current value
 * String current = logLevel.get();
 *
 * // Unsubscribe
 * logLevel.unsubscribe(subscriber);
 * }</pre>
 *
 * @param <T> value type | 值类型
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public final class ReactiveConfigValue<T> {

    private static final System.Logger LOGGER = System.getLogger(ReactiveConfigValue.class.getName());

    private final String key;
    private final Class<T> type;
    private final Config config;
    private final T defaultValue;
    private volatile T currentValue;
    private final List<Consumer<T>> subscribers = new CopyOnWriteArrayList<>();

    public ReactiveConfigValue(Config config, String key, Class<T> type, T defaultValue) {
        this.config = config;
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.currentValue = config.get(key, type, defaultValue);

        config.addListener(key, event -> {
            T newValue = config.get(key, type, defaultValue);
            if (!Objects.equals(currentValue, newValue)) {
                currentValue = newValue;
                notifySubscribers(newValue);
            }
        });
    }

    public T get() {
        return currentValue;
    }

    public ReactiveConfigValue<T> subscribe(Consumer<T> subscriber) {
        subscribers.add(subscriber);
        subscriber.accept(currentValue);
        return this;
    }

    public void unsubscribe(Consumer<T> subscriber) {
        subscribers.remove(subscriber);
    }

    private void notifySubscribers(T newValue) {
        for (Consumer<T> subscriber : subscribers) {
            Thread.startVirtualThread(() -> {
                try {
                    subscriber.accept(newValue);
                } catch (Exception e) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "Config subscriber failed for key {0}: {1}", key, e.getMessage(), e);
                }
            });
        }
    }

    public static <T> ReactiveConfigValue<T> of(Config config, String key, Class<T> type, T defaultValue) {
        return new ReactiveConfigValue<>(config, key, type, defaultValue);
    }
}
