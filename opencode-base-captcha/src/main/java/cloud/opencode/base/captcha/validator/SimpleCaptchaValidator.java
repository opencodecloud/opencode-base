package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.store.CaptchaStore;

import java.util.Optional;

/**
 * Simple Captcha Validator - Basic CAPTCHA validation
 * 简单验证码验证器 - 基础验证码验证
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Case-insensitive answer comparison - 大小写不敏感的答案比较</li>
 *   <li>Single-use validation (answer removed after check) - 一次性验证（检查后删除答案）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaValidator validator = new SimpleCaptchaValidator(store);
 * ValidationResult result = validator.validate(captchaId, userAnswer);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe store) - 线程安全: 是（委托给线程安全的存储）</li>
 *   <li>Null-safe: No (store, id, and answer must not be null) - 空值安全: 否（存储、ID和答案不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class SimpleCaptchaValidator implements CaptchaValidator {

    private final CaptchaStore store;

    /**
     * Creates a new validator with the specified store.
     * 使用指定存储创建新验证器。
     *
     * @param store the CAPTCHA store | 验证码存储
     */
    public SimpleCaptchaValidator(CaptchaStore store) {
        this.store = store;
    }

    @Override
    public ValidationResult validate(String id, String answer) {
        return validate(id, answer, false);
    }

    @Override
    public ValidationResult validate(String id, String answer, boolean caseSensitive) {
        if (id == null || id.isBlank()) {
            return ValidationResult.invalidInput();
        }
        if (answer == null || answer.isBlank()) {
            return ValidationResult.invalidInput();
        }

        Optional<String> storedAnswer = store.getAndRemove(id);

        if (storedAnswer.isEmpty()) {
            return ValidationResult.notFound();
        }

        String stored = storedAnswer.get();
        boolean matches = caseSensitive
            ? stored.equals(answer)
            : stored.equalsIgnoreCase(answer);

        return matches ? ValidationResult.ok() : ValidationResult.mismatch();
    }
}
