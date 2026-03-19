package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.store.CaptchaStore;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Time-Based Captcha Validator - Validation with timing checks
 * 基于时间的验证码验证器 - 带时间检查的验证
 *
 * <p>This validator checks for suspiciously fast responses that might indicate
 * automated attacks.</p>
 * <p>此验证器检查可能表示自动攻击的可疑快速响应。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Minimum response time enforcement - 最小响应时间强制</li>
 *   <li>Expiration time check - 过期时间检查</li>
 *   <li>Suspicious timing detection - 可疑时间检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeBasedCaptchaValidator validator = new TimeBasedCaptchaValidator(store);
 * validator.recordCreation(captchaId);
 * ValidationResult result = validator.validate(captchaId, answer);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap) - 线程安全: 是（使用ConcurrentHashMap）</li>
 *   <li>Null-safe: No (store, id, and answer must not be null) - 空值安全: 否（存储、ID和答案不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class TimeBasedCaptchaValidator implements CaptchaValidator {

    private static final Duration MIN_RESPONSE_TIME = Duration.ofMillis(500);

    private final CaptchaStore store;
    private final Map<String, Instant> creationTimes = new ConcurrentHashMap<>();

    /**
     * Creates a new validator with the specified store.
     * 使用指定存储创建新验证器。
     *
     * @param store the CAPTCHA store | 验证码存储
     */
    public TimeBasedCaptchaValidator(CaptchaStore store) {
        this.store = store;
    }

    /**
     * Records CAPTCHA creation time.
     * 记录验证码创建时间。
     *
     * @param id the CAPTCHA ID | 验证码 ID
     */
    public void recordCreation(String id) {
        creationTimes.put(id, Instant.now());
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

        // Check response time
        Instant creationTime = creationTimes.remove(id);
        if (creationTime != null) {
            Duration responseTime = Duration.between(creationTime, Instant.now());
            if (responseTime.compareTo(MIN_RESPONSE_TIME) < 0) {
                store.remove(id);
                return ValidationResult.suspiciousBehavior();
            }
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

    /**
     * Clears old creation time records.
     * 清除旧的创建时间记录。
     */
    public void clearOldRecords() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(10));
        creationTimes.entrySet().removeIf(e -> e.getValue().isBefore(cutoff));
    }
}
