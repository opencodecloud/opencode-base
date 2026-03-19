package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Captcha Difficulty Adapter - Adaptive difficulty adjustment
 * 验证码难度适配器 - 自适应难度调整
 *
 * <p>This class dynamically adjusts CAPTCHA difficulty based on client behavior,
 * increasing difficulty for suspicious patterns and reducing for legitimate users.</p>
 * <p>此类根据客户端行为动态调整验证码难度，对可疑模式增加难度，对合法用户降低难度。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Failure rate based adaptation - 基于失败率的适应</li>
 *   <li>Time based adjustment - 基于时间的调整</li>
 *   <li>Risk level integration - 风险级别集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaDifficultyAdapter adapter = new CaptchaDifficultyAdapter();
 *
 * // Get adapted strength for client
 * CaptchaStrength strength = adapter.getStrength(clientId);
 *
 * // Generate CAPTCHA with adapted difficulty
 * Captcha captcha = OpenCaptcha.generate(strength.toConfig());
 *
 * // Record validation result
 * adapter.recordAttempt(clientId, success);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap and ReentrantLock) - 线程安全: 是（使用ConcurrentHashMap和ReentrantLock）</li>
 *   <li>Null-safe: No (clientId must not be null) - 空值安全: 否（客户端ID不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class CaptchaDifficultyAdapter {

    private static final int FAILURE_THRESHOLD_MEDIUM = 2;
    private static final int FAILURE_THRESHOLD_HARD = 4;
    private static final int FAILURE_THRESHOLD_EXTREME = 6;
    private static final Duration RESET_WINDOW = Duration.ofMinutes(15);
    private static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(30);

    private final Map<String, ClientRecord> clientRecords = new ConcurrentHashMap<>();
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder totalFailures = new LongAdder();
    private final ReentrantLock cleanupLock = new ReentrantLock();
    private volatile Instant lastCleanup = Instant.now();

    /**
     * Gets the recommended strength for a client.
     * 获取客户端的推荐强度。
     *
     * @param clientId the client identifier | 客户端标识符
     * @return the recommended strength | 推荐强度
     */
    public CaptchaStrength getStrength(String clientId) {
        cleanupIfNeeded();

        ClientRecord record = clientRecords.get(clientId);
        if (record == null) {
            return CaptchaStrength.EASY;
        }

        // Reset if window expired
        if (record.isExpired()) {
            clientRecords.remove(clientId);
            return CaptchaStrength.EASY;
        }

        int failures = record.consecutiveFailures.get();
        if (failures >= FAILURE_THRESHOLD_EXTREME) {
            return CaptchaStrength.EXTREME;
        } else if (failures >= FAILURE_THRESHOLD_HARD) {
            return CaptchaStrength.HARD;
        } else if (failures >= FAILURE_THRESHOLD_MEDIUM) {
            return CaptchaStrength.MEDIUM;
        }
        return CaptchaStrength.EASY;
    }

    /**
     * Gets the adapted configuration for a client.
     * 获取客户端的适应配置。
     *
     * @param clientId the client identifier | 客户端标识符
     * @return the adapted configuration | 适应配置
     */
    public CaptchaConfig getConfig(String clientId) {
        return getStrength(clientId).toConfig();
    }

    /**
     * Gets the adapted configuration with custom base config.
     * 使用自定义基础配置获取适应配置。
     *
     * @param clientId   the client identifier | 客户端标识符
     * @param baseConfig the base configuration | 基础配置
     * @return the adapted configuration | 适应配置
     */
    public CaptchaConfig getConfig(String clientId, CaptchaConfig baseConfig) {
        CaptchaStrength strength = getStrength(clientId);
        return CaptchaConfig.builder()
            .type(baseConfig.getType())
            .width(baseConfig.getWidth())
            .height(baseConfig.getHeight())
            .length(baseConfig.getLength())
            .expireTime(adjustExpiration(baseConfig.getExpireTime(), strength))
            .noiseLines(strength.getNoiseLines())
            .noiseDots(strength.getNoiseDots())
            .fontSize(strength.getFontSize())
            .build();
    }

    /**
     * Records a validation attempt.
     * 记录一次验证尝试。
     *
     * @param clientId the client identifier | 客户端标识符
     * @param success  whether the attempt was successful | 尝试是否成功
     */
    public void recordAttempt(String clientId, boolean success) {
        totalRequests.increment();
        if (!success) {
            totalFailures.increment();
        }

        ClientRecord record = clientRecords.computeIfAbsent(clientId,
            k -> new ClientRecord());

        if (success) {
            record.recordSuccess();
        } else {
            record.recordFailure();
        }
    }

    /**
     * Resets the difficulty for a client.
     * 重置客户端的难度。
     *
     * @param clientId the client identifier | 客户端标识符
     */
    public void reset(String clientId) {
        clientRecords.remove(clientId);
    }

    /**
     * Gets the global failure rate.
     * 获取全局失败率。
     *
     * @return the failure rate (0.0 - 1.0) | 失败率
     */
    public double getGlobalFailureRate() {
        long total = totalRequests.sum();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalFailures.sum() / total;
    }

    /**
     * Gets the failure count for a client.
     * 获取客户端的失败次数。
     *
     * @param clientId the client identifier | 客户端标识符
     * @return the failure count | 失败次数
     */
    public int getFailureCount(String clientId) {
        ClientRecord record = clientRecords.get(clientId);
        return record != null ? record.consecutiveFailures.get() : 0;
    }

    /**
     * Gets the number of tracked clients.
     * 获取跟踪的客户端数量。
     *
     * @return the number of clients | 客户端数量
     */
    public int getTrackedClientCount() {
        return clientRecords.size();
    }

    /**
     * Clears all client records.
     * 清除所有客户端记录。
     */
    public void clearAll() {
        clientRecords.clear();
    }

    private Duration adjustExpiration(Duration base, CaptchaStrength strength) {
        return switch (strength) {
            case EASY -> base;
            case MEDIUM -> base.multipliedBy(3).dividedBy(4);  // 75%
            case HARD -> base.dividedBy(2);  // 50%
            case EXTREME -> base.dividedBy(3);  // ~33%
        };
    }

    private void cleanupIfNeeded() {
        Instant now = Instant.now();
        if (Duration.between(lastCleanup, now).compareTo(CLEANUP_INTERVAL) > 0) {
            if (cleanupLock.tryLock()) {
                try {
                    if (Duration.between(lastCleanup, now).compareTo(CLEANUP_INTERVAL) > 0) {
                        cleanup();
                        lastCleanup = now;
                    }
                } finally {
                    cleanupLock.unlock();
                }
            }
        }
    }

    private void cleanup() {
        clientRecords.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    /**
     * Client behavior record.
     */
    private static final class ClientRecord {
        private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
        private volatile Instant lastAttempt = Instant.now();

        void recordSuccess() {
            consecutiveFailures.set(0);
            lastAttempt = Instant.now();
        }

        void recordFailure() {
            consecutiveFailures.incrementAndGet();
            lastAttempt = Instant.now();
        }

        boolean isExpired() {
            return Duration.between(lastAttempt, Instant.now()).compareTo(RESET_WINDOW) > 0;
        }
    }
}
