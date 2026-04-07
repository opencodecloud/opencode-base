package cloud.opencode.base.captcha;

/**
 * Captcha Event Listener - Interface for CAPTCHA lifecycle event callbacks
 * 验证码事件监听器 - 验证码生命周期事件回调接口
 *
 * <p>Defines callback methods for CAPTCHA lifecycle events including generation,
 * successful validation, and failed validation. All methods have default empty
 * implementations so listeners can override only the events they are interested in.</p>
 * <p>定义验证码生命周期事件的回调方法，包括生成、验证成功和验证失败。
 * 所有方法都有默认的空实现，监听器可以只覆盖感兴趣的事件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Generation event callback - 生成事件回调</li>
 *   <li>Validation success callback - 验证成功回调</li>
 *   <li>Validation failure callback with reason code - 带原因代码的验证失败回调</li>
 *   <li>Default empty implementations for selective override - 默认空实现支持选择性覆盖</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaEventListener listener = new CaptchaEventListener() {
 *     @Override
 *     public void onValidationFailure(String captchaId, ValidationResult.ResultCode reason) {
 *         logger.warn("Captcha validation failed: {} reason={}", captchaId, reason);
 *     }
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (parameters should not be null) - 空值安全: 否（参数不应为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public interface CaptchaEventListener {

    /**
     * Called when a CAPTCHA is generated.
     * 当验证码生成时调用。
     *
     * @param captcha the generated CAPTCHA | 生成的验证码
     */
    default void onGenerated(Captcha captcha) {
    }

    /**
     * Called when a CAPTCHA validation succeeds.
     * 当验证码验证成功时调用。
     *
     * @param captchaId the CAPTCHA ID that was validated | 被验证的验证码 ID
     */
    default void onValidationSuccess(String captchaId) {
    }

    /**
     * Called when a CAPTCHA validation fails.
     * 当验证码验证失败时调用。
     *
     * @param captchaId the CAPTCHA ID that failed validation | 验证失败的验证码 ID
     * @param reason    the failure reason code | 失败原因代码
     */
    default void onValidationFailure(String captchaId, ValidationResult.ResultCode reason) {
    }
}
