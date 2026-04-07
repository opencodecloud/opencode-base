package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.store.CaptchaStore;
import cloud.opencode.base.captcha.store.HashedCaptchaStore;

/**
 * Captcha Validator - Interface for CAPTCHA validation
 * 验证码验证器 - 验证码验证接口
 *
 * <p>This interface defines the contract for validating CAPTCHA answers.</p>
 * <p>此接口定义了验证验证码答案的契约。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Answer validation contract - 答案验证契约</li>
 *   <li>Multiple implementation support - 多种实现支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaValidator validator = new SimpleCaptchaValidator(store);
 * ValidationResult result = validator.validate(id, answer);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (id and answer must not be null) - 空值安全: 否（ID和答案不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public interface CaptchaValidator {

    /**
     * Validates a CAPTCHA answer.
     * 验证验证码答案。
     *
     * @param id     the CAPTCHA ID | 验证码 ID
     * @param answer the provided answer | 提供的答案
     * @return the validation result | 验证结果
     */
    ValidationResult validate(String id, String answer);

    /**
     * Validates a CAPTCHA answer with case sensitivity option.
     * 验证验证码答案（带大小写敏感选项）。
     *
     * @param id            the CAPTCHA ID | 验证码 ID
     * @param answer        the provided answer | 提供的答案
     * @param caseSensitive whether case sensitive | 是否区分大小写
     * @return the validation result | 验证结果
     */
    ValidationResult validate(String id, String answer, boolean caseSensitive);

    /**
     * Creates a simple validator.
     * 创建简单验证器。
     *
     * @param store the CAPTCHA store | 验证码存储
     * @return the validator | 验证器
     */
    static CaptchaValidator simple(CaptchaStore store) {
        return new SimpleCaptchaValidator(store);
    }

    /**
     * Creates a time-based validator with behavior checking.
     * 创建带行为检查的基于时间的验证器。
     *
     * @param store the CAPTCHA store | 验证码存储
     * @return the validator | 验证器
     */
    static CaptchaValidator timeBased(CaptchaStore store) {
        return new TimeBasedCaptchaValidator(store);
    }

    /**
     * Creates a Proof-of-Work validator.
     * 创建工作量证明验证器。
     *
     * @param store the CAPTCHA store | 验证码存储
     * @return the validator | 验证器
     */
    static CaptchaValidator pow(CaptchaStore store) {
        return new PowCaptchaValidator(store);
    }

    /**
     * Creates a composite validator that chains multiple validators.
     * 创建串联多个验证器的组合验证器。
     *
     * @param first the first (primary) validator | 第一个（主要）验证器
     * @param rest  additional validators | 附加验证器
     * @return the composite validator | 组合验证器
     */
    static CaptchaValidator composite(CaptchaValidator first, CaptchaValidator... rest) {
        return CompositeValidator.of(first, rest);
    }

    /**
     * Creates a validator for hashed stores.
     * 创建用于哈希存储的验证器。
     *
     * @param store the hashed CAPTCHA store | 哈希验证码存储
     * @return the validator | 验证器
     */
    static CaptchaValidator hashed(HashedCaptchaStore store) {
        return new HashedCaptchaValidator(store);
    }
}
