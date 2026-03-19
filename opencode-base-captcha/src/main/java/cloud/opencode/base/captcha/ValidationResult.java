package cloud.opencode.base.captcha;

/**
 * Validation Result - Result of CAPTCHA validation
 * 验证结果 - 验证码验证结果
 *
 * <p>This record holds the result of a CAPTCHA validation attempt.</p>
 * <p>此记录保存验证码验证尝试的结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable validation result record - 不可变验证结果记录</li>
 *   <li>Predefined result codes for common scenarios - 常见场景的预定义结果代码</li>
 *   <li>Static factory methods for standard results - 标准结果的静态工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ValidationResult result = OpenCaptcha.validate(id, answer);
 * if (result.success()) {
 *     // Proceed with action
 * } else {
 *     String error = result.message();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (message may be null) - 空值安全: 否（消息可能为空）</li>
 * </ul>
 *
 * @param success true if validation succeeded | 如果验证成功则为 true
 * @param message the result message | 结果消息
 * @param code    the result code | 结果代码
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public record ValidationResult(
    boolean success,
    String message,
    ResultCode code
) {

    /**
     * Result codes for validation.
     * 验证结果代码。
     */
    public enum ResultCode {
        /** Validation successful | 验证成功 */
        SUCCESS,
        /** CAPTCHA not found | 验证码未找到 */
        NOT_FOUND,
        /** CAPTCHA expired | 验证码已过期 */
        EXPIRED,
        /** Answer mismatch | 答案不匹配 */
        MISMATCH,
        /** Rate limit exceeded | 超过速率限制 */
        RATE_LIMITED,
        /** Invalid input | 输入无效 */
        INVALID_INPUT,
        /** Suspicious behavior detected | 检测到可疑行为 */
        SUSPICIOUS_BEHAVIOR
    }

    /**
     * Creates a successful result.
     * 创建成功结果。
     *
     * @return a success result | 成功结果
     */
    public static ValidationResult ok() {
        return new ValidationResult(true, "Validation successful", ResultCode.SUCCESS);
    }

    /**
     * Creates a failed result for not found.
     * 创建未找到的失败结果。
     *
     * @return a not found result | 未找到结果
     */
    public static ValidationResult notFound() {
        return new ValidationResult(false, "CAPTCHA not found", ResultCode.NOT_FOUND);
    }

    /**
     * Creates a failed result for expired.
     * 创建已过期的失败结果。
     *
     * @return an expired result | 过期结果
     */
    public static ValidationResult expired() {
        return new ValidationResult(false, "CAPTCHA has expired", ResultCode.EXPIRED);
    }

    /**
     * Creates a failed result for mismatch.
     * 创建不匹配的失败结果。
     *
     * @return a mismatch result | 不匹配结果
     */
    public static ValidationResult mismatch() {
        return new ValidationResult(false, "Answer does not match", ResultCode.MISMATCH);
    }

    /**
     * Creates a failed result for rate limiting.
     * 创建速率限制的失败结果。
     *
     * @return a rate limited result | 速率限制结果
     */
    public static ValidationResult rateLimited() {
        return new ValidationResult(false, "Too many attempts, please try again later", ResultCode.RATE_LIMITED);
    }

    /**
     * Creates a failed result for invalid input.
     * 创建无效输入的失败结果。
     *
     * @return an invalid input result | 无效输入结果
     */
    public static ValidationResult invalidInput() {
        return new ValidationResult(false, "Invalid input provided", ResultCode.INVALID_INPUT);
    }

    /**
     * Creates a failed result for suspicious behavior.
     * 创建可疑行为的失败结果。
     *
     * @return a suspicious behavior result | 可疑行为结果
     */
    public static ValidationResult suspiciousBehavior() {
        return new ValidationResult(false, "Suspicious behavior detected", ResultCode.SUSPICIOUS_BEHAVIOR);
    }

    /**
     * Checks if validation failed.
     * 检查验证是否失败。
     *
     * @return true if failed | 如果失败则返回 true
     */
    public boolean isFailed() {
        return !success;
    }
}
