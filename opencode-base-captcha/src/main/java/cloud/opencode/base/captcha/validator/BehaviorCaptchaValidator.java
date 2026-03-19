package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.security.BehaviorAnalyzer;
import cloud.opencode.base.captcha.store.CaptchaStore;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Behavior Captcha Validator - Validation with behavior analysis
 * 行为验证码验证器 - 带行为分析的验证
 *
 * <p>This validator combines answer validation with behavior analysis to detect
 * automated attacks and suspicious patterns.</p>
 * <p>此验证器结合答案验证和行为分析来检测自动攻击和可疑模式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Response time analysis - 响应时间分析</li>
 *   <li>Failure rate tracking - 失败率跟踪</li>
 *   <li>Consistent timing detection - 一致时间检测</li>
 *   <li>Bot behavior identification - 机器人行为识别</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaStore store = CaptchaStore.memory();
 * BehaviorCaptchaValidator validator = new BehaviorCaptchaValidator(store);
 *
 * // Record creation time
 * validator.recordCreation(captchaId, clientId);
 * // (validation continues...)
 *
 * // Later, validate with behavior check
 * ValidationResult result = validator.validate(captchaId, answer, clientId);
 * if (result.code() == ResultCode.SUSPICIOUS_BEHAVIOR) {
 *     // Handle bot detection
 * }
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
public final class BehaviorCaptchaValidator implements CaptchaValidator {

    private static final Duration MIN_RESPONSE_TIME = Duration.ofMillis(500);
    private static final Duration MAX_RESPONSE_TIME = Duration.ofMinutes(5);

    private final CaptchaStore store;
    private final BehaviorAnalyzer analyzer;
    private final Map<String, CreationRecord> creationRecords = new ConcurrentHashMap<>();

    /**
     * Creates a new behavior validator with the specified store.
     * 使用指定存储创建新行为验证器。
     *
     * @param store the CAPTCHA store | 验证码存储
     */
    public BehaviorCaptchaValidator(CaptchaStore store) {
        this(store, new BehaviorAnalyzer());
    }

    /**
     * Creates a new behavior validator with custom analyzer.
     * 使用自定义分析器创建新行为验证器。
     *
     * @param store    the CAPTCHA store | 验证码存储
     * @param analyzer the behavior analyzer | 行为分析器
     */
    public BehaviorCaptchaValidator(CaptchaStore store, BehaviorAnalyzer analyzer) {
        this.store = store;
        this.analyzer = analyzer;
    }

    /**
     * Records CAPTCHA creation for a client.
     * 为客户端记录验证码创建。
     *
     * @param captchaId the CAPTCHA ID | 验证码 ID
     * @param clientId  the client identifier | 客户端标识符
     */
    public void recordCreation(String captchaId, String clientId) {
        creationRecords.put(captchaId, new CreationRecord(clientId, Instant.now()));
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

        // Get and remove creation record
        CreationRecord record = creationRecords.remove(id);
        String clientId = record != null ? record.clientId() : "unknown";
        Instant creationTime = record != null ? record.creationTime() : null;

        // Check response time
        if (creationTime != null) {
            Duration responseTime = Duration.between(creationTime, Instant.now());

            // Too fast - likely automated
            if (responseTime.compareTo(MIN_RESPONSE_TIME) < 0) {
                store.remove(id);
                analyzer.analyze(clientId, responseTime, false);
                return ValidationResult.suspiciousBehavior();
            }

            // Too slow - expired
            if (responseTime.compareTo(MAX_RESPONSE_TIME) > 0) {
                store.remove(id);
                return ValidationResult.expired();
            }

            // Analyze behavior
            BehaviorAnalyzer.AnalysisResult behaviorResult = analyzer.analyze(clientId, responseTime, true);
            if (behaviorResult != BehaviorAnalyzer.AnalysisResult.NORMAL) {
                if (behaviorResult == BehaviorAnalyzer.AnalysisResult.TOO_MANY_FAILURES ||
                    behaviorResult == BehaviorAnalyzer.AnalysisResult.CONSISTENT_TIMING) {
                    store.remove(id);
                    return ValidationResult.suspiciousBehavior();
                }
            }
        }

        // Validate answer
        Optional<String> storedAnswer = store.getAndRemove(id);

        if (storedAnswer.isEmpty()) {
            if (record != null) {
                analyzer.analyze(clientId, Duration.ZERO, false);
            }
            return ValidationResult.notFound();
        }

        String stored = storedAnswer.get();
        boolean matches = caseSensitive
            ? stored.equals(answer)
            : stored.equalsIgnoreCase(answer);

        // Record result in analyzer
        if (record != null) {
            Duration responseTime = Duration.between(record.creationTime(), Instant.now());
            analyzer.analyze(clientId, responseTime, matches);
        }

        return matches ? ValidationResult.ok() : ValidationResult.mismatch();
    }

    /**
     * Validates with explicit client ID.
     * 使用明确的客户端 ID 进行验证。
     *
     * @param id        the CAPTCHA ID | 验证码 ID
     * @param answer    the provided answer | 提供的答案
     * @param clientId  the client identifier | 客户端标识符
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(String id, String answer, String clientId) {
        return validate(id, answer, clientId, false);
    }

    /**
     * Validates with explicit client ID and case sensitivity.
     * 使用明确的客户端 ID 和大小写敏感选项进行验证。
     *
     * @param id            the CAPTCHA ID | 验证码 ID
     * @param answer        the provided answer | 提供的答案
     * @param clientId      the client identifier | 客户端标识符
     * @param caseSensitive whether case sensitive | 是否区分大小写
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(String id, String answer, String clientId, boolean caseSensitive) {
        // Temporarily store client ID for validation
        CreationRecord existing = creationRecords.get(id);
        if (existing != null) {
            // Update with provided client ID if different
            if (!clientId.equals(existing.clientId())) {
                creationRecords.put(id, new CreationRecord(clientId, existing.creationTime()));
            }
        } else {
            // No creation record, create one with current time
            creationRecords.put(id, new CreationRecord(clientId, Instant.now().minus(Duration.ofSeconds(1))));
        }

        return validate(id, answer, caseSensitive);
    }

    /**
     * Gets the behavior analyzer.
     * 获取行为分析器。
     *
     * @return the behavior analyzer | 行为分析器
     */
    public BehaviorAnalyzer getAnalyzer() {
        return analyzer;
    }

    /**
     * Clears old creation records.
     * 清除旧的创建记录。
     */
    public void clearOldRecords() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(10));
        creationRecords.entrySet().removeIf(e -> e.getValue().creationTime().isBefore(cutoff));
        analyzer.clearOld();
    }

    /**
     * Creation record for tracking.
     */
    private record CreationRecord(String clientId, Instant creationTime) {}
}
