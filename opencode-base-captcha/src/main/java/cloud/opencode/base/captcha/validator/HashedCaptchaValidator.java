package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.store.HashedCaptchaStore;

import java.util.Objects;

/**
 * Hashed Captcha Validator - Validator for HashedCaptchaStore
 * 哈希验证码验证器 - 用于 HashedCaptchaStore 的验证器
 *
 * <p>This validator works with {@link HashedCaptchaStore} to verify plaintext answers
 * against stored hashes. It must be used instead of {@link SimpleCaptchaValidator}
 * when the store hashes answers.</p>
 * <p>此验证器与 {@link HashedCaptchaStore} 配合使用，验证明文答案与存储的哈希。
 * 当存储对答案进行哈希时，必须使用此验证器替代 {@link SimpleCaptchaValidator}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Transparent hash verification - 透明哈希验证</li>
 *   <li>Atomic verify-and-remove (one-time validation) - 原子验证并删除（一次性验证）</li>
 *   <li>Timing-safe comparison via SHA-256 - 通过 SHA-256 的时间安全比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HashedCaptchaStore store = HashedCaptchaStore.wrap(CaptchaStore.memory());
 * CaptchaValidator validator = new HashedCaptchaValidator(store);
 * ValidationResult result = validator.validate(id, answer);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe store) - 线程安全: 是（委托给线程安全的存储）</li>
 *   <li>Null-safe: No (store, id, answer must not be null) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class HashedCaptchaValidator implements CaptchaValidator {

    private final HashedCaptchaStore store;

    /**
     * Creates a new validator with the specified hashed store.
     * 使用指定的哈希存储创建新验证器。
     *
     * @param store the hashed CAPTCHA store | 哈希验证码存储
     */
    public HashedCaptchaValidator(HashedCaptchaStore store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    /**
     * Validates a CAPTCHA answer against the stored hash.
     * 验证验证码答案与存储的哈希。
     *
     * @param id     the CAPTCHA ID | 验证码 ID
     * @param answer the plaintext answer | 明文答案
     * @return the validation result | 验证结果
     */
    @Override
    public ValidationResult validate(String id, String answer) {
        return validate(id, answer, false);
    }

    /**
     * Validates a CAPTCHA answer against the stored hash.
     * 验证验证码答案与存储的哈希。
     *
     * <p>The {@code caseSensitive} parameter is ignored because case sensitivity
     * is configured at the {@link HashedCaptchaStore} level during construction.</p>
     * <p>{@code caseSensitive} 参数被忽略，因为大小写敏感性在
     * {@link HashedCaptchaStore} 构造时配置。</p>
     *
     * @param id            the CAPTCHA ID | 验证码 ID
     * @param answer        the plaintext answer | 明文答案
     * @param caseSensitive ignored (configured on store) | 忽略（在存储上配置）
     * @return the validation result | 验证结果
     */
    @Override
    public ValidationResult validate(String id, String answer, boolean caseSensitive) {
        if (id == null || id.isBlank()) {
            return ValidationResult.invalidInput();
        }
        if (answer == null || answer.isBlank()) {
            return ValidationResult.invalidInput();
        }

        return store.verifyAndRemoveResult(id, answer);
    }
}
