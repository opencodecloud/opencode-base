package cloud.opencode.base.captcha;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Captcha Event Dispatcher - Manages and dispatches CAPTCHA lifecycle events
 * 验证码事件分发器 - 管理和分发验证码生命周期事件
 *
 * <p>This class implements {@link CaptchaEventListener} and acts as a composite
 * dispatcher, forwarding events to all registered listeners. Each listener invocation
 * is exception-isolated: a failure in one listener does not prevent others from
 * being notified.</p>
 * <p>此类实现 {@link CaptchaEventListener} 并充当复合分发器，将事件转发给所有注册的监听器。
 * 每个监听器调用都是异常隔离的：一个监听器的失败不会阻止其他监听器被通知。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe listener registration and removal - 线程安全的监听器注册和移除</li>
 *   <li>Exception isolation per listener - 每个监听器异常隔离</li>
 *   <li>CopyOnWriteArrayList for safe concurrent iteration - 使用 CopyOnWriteArrayList 安全并发迭代</li>
 *   <li>Implements CaptchaEventListener for easy composition - 实现 CaptchaEventListener 便于组合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaEventDispatcher dispatcher = new CaptchaEventDispatcher();
 * dispatcher.addListener(new LoggingListener());
 * dispatcher.addListener(new MetricsListener());
 * dispatcher.onGenerated(captcha);  // both listeners notified
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Event dispatch: O(n) where n is listener count - 事件分发: O(n)，n 为监听器数量</li>
 *   <li>Add/remove: O(n) copy overhead (CopyOnWriteArrayList) - 添加/移除: O(n) 复制开销</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (CopyOnWriteArrayList) - 线程安全: 是</li>
 *   <li>Null-safe: No (listener must not be null) - 空值安全: 否（监听器不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class CaptchaEventDispatcher implements CaptchaEventListener {

    private final List<CaptchaEventListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Registers an event listener.
     * 注册事件监听器。
     *
     * @param listener the listener to add | 要添加的监听器
     * @throws NullPointerException if listener is null | 如果监听器为 null
     */
    public void addListener(CaptchaEventListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener must not be null"));
    }

    /**
     * Removes an event listener.
     * 移除事件监听器。
     *
     * @param listener the listener to remove | 要移除的监听器
     * @return true if the listener was found and removed | 如果找到并移除了监听器返回 true
     */
    public boolean removeListener(CaptchaEventListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Returns the number of registered listeners.
     * 返回已注册的监听器数量。
     *
     * @return the listener count | 监听器数量
     */
    public int listenerCount() {
        return listeners.size();
    }

    /**
     * Dispatches a generation event to all registered listeners.
     * 向所有注册的监听器分发生成事件。
     *
     * <p>Each listener is invoked in registration order. Exceptions thrown by
     * individual listeners are caught and suppressed to ensure all listeners
     * are notified.</p>
     * <p>按注册顺序调用每个监听器。单个监听器抛出的异常会被捕获并抑制，
     * 以确保所有监听器都被通知。</p>
     *
     * @param captcha the generated CAPTCHA | 生成的验证码
     */
    @Override
    public void onGenerated(Captcha captcha) {
        for (CaptchaEventListener listener : listeners) {
            try {
                listener.onGenerated(captcha);
            } catch (Exception ignored) {
                // Exception isolation: one listener failure must not affect others
            }
        }
    }

    /**
     * Dispatches a validation success event to all registered listeners.
     * 向所有注册的监听器分发验证成功事件。
     *
     * @param captchaId the CAPTCHA ID | 验证码 ID
     */
    @Override
    public void onValidationSuccess(String captchaId) {
        for (CaptchaEventListener listener : listeners) {
            try {
                listener.onValidationSuccess(captchaId);
            } catch (Exception ignored) {
                // Exception isolation: one listener failure must not affect others
            }
        }
    }

    /**
     * Dispatches a validation failure event to all registered listeners.
     * 向所有注册的监听器分发验证失败事件。
     *
     * @param captchaId the CAPTCHA ID | 验证码 ID
     * @param reason    the failure reason code | 失败原因代码
     */
    @Override
    public void onValidationFailure(String captchaId, ValidationResult.ResultCode reason) {
        for (CaptchaEventListener listener : listeners) {
            try {
                listener.onValidationFailure(captchaId, reason);
            } catch (Exception ignored) {
                // Exception isolation: one listener failure must not affect others
            }
        }
    }
}
