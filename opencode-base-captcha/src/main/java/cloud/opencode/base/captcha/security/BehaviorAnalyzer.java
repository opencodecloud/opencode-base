package cloud.opencode.base.captcha.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Behavior Analyzer - Analyzes user behavior for bot detection
 * 行为分析器 - 分析用户行为以检测机器人
 *
 * <p>This class analyzes patterns in CAPTCHA interactions to detect potential bots.</p>
 * <p>此类分析验证码交互中的模式以检测潜在的机器人。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Response time analysis - 响应时间分析</li>
 *   <li>Failure pattern detection - 失败模式检测</li>
 *   <li>Client risk scoring - 客户端风险评分</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BehaviorAnalyzer analyzer = new BehaviorAnalyzer();
 * analyzer.recordAttempt(clientId, success, responseTime);
 * double riskScore = analyzer.getRiskScore(clientId);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap) - 线程安全: 是（使用ConcurrentHashMap）</li>
 *   <li>Null-safe: No (clientId must not be null) - 空值安全: 否（客户端ID不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class BehaviorAnalyzer {

    private static final Duration MIN_HUMAN_RESPONSE = Duration.ofMillis(500);
    private static final int MAX_FAILURES_PER_WINDOW = 5;
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(5);

    private final Map<String, ClientBehavior> behaviors = new ConcurrentHashMap<>();

    /**
     * Analyzes behavior for a client.
     * 分析客户端的行为。
     *
     * @param clientId     the client identifier | 客户端标识符
     * @param responseTime the response time | 响应时间
     * @param success      whether the attempt was successful | 尝试是否成功
     * @return the analysis result | 分析结果
     */
    public AnalysisResult analyze(String clientId, Duration responseTime, boolean success) {
        ClientBehavior behavior = behaviors.computeIfAbsent(clientId, k -> new ClientBehavior());

        behavior.recordAttempt(responseTime, success);

        // Check for suspicious patterns
        if (responseTime.compareTo(MIN_HUMAN_RESPONSE) < 0) {
            return AnalysisResult.SUSPICIOUS_TIMING;
        }

        if (behavior.getRecentFailures() > MAX_FAILURES_PER_WINDOW) {
            return AnalysisResult.TOO_MANY_FAILURES;
        }

        if (behavior.hasConsistentTiming()) {
            return AnalysisResult.CONSISTENT_TIMING;
        }

        return AnalysisResult.NORMAL;
    }

    /**
     * Gets the behavior for a client.
     * 获取客户端的行为。
     *
     * @param clientId the client identifier | 客户端标识符
     * @return the behavior or null | 行为或 null
     */
    public ClientBehavior getBehavior(String clientId) {
        return behaviors.get(clientId);
    }

    /**
     * Clears behavior data for a client.
     * 清除客户端的行为数据。
     *
     * @param clientId the client identifier | 客户端标识符
     */
    public void clear(String clientId) {
        behaviors.remove(clientId);
    }

    /**
     * Clears all old behavior data.
     * 清除所有旧的行为数据。
     */
    public void clearOld() {
        Instant cutoff = Instant.now().minus(Duration.ofHours(1));
        behaviors.entrySet().removeIf(e -> e.getValue().getLastActivity().isBefore(cutoff));
    }

    /**
     * Analysis result enumeration.
     * 分析结果枚举。
     */
    public enum AnalysisResult {
        /** Normal behavior | 正常行为 */
        NORMAL,
        /** Suspiciously fast response | 可疑的快速响应 */
        SUSPICIOUS_TIMING,
        /** Too many failures | 失败次数过多 */
        TOO_MANY_FAILURES,
        /** Consistent timing suggests automation | 一致的时间表明自动化 */
        CONSISTENT_TIMING
    }

    /**
     * Client behavior tracking.
     * 客户端行为跟踪。
     */
    public static final class ClientBehavior {
        private volatile Instant lastActivity = Instant.now();
        private final AtomicInteger totalAttempts = new AtomicInteger(0);
        private final AtomicInteger recentFailures = new AtomicInteger(0);
        private volatile Instant failureWindowStart = Instant.now();
        private volatile long lastResponseTimeMs = 0;
        private final AtomicInteger consistentTimingCount = new AtomicInteger(0);

        void recordAttempt(Duration responseTime, boolean success) {
            lastActivity = Instant.now();
            totalAttempts.incrementAndGet();

            // Check failure window
            if (Instant.now().isAfter(failureWindowStart.plus(FAILURE_WINDOW))) {
                failureWindowStart = Instant.now();
                recentFailures.set(0);
            }

            if (!success) {
                recentFailures.incrementAndGet();
            }

            // Check timing consistency
            long currentMs = responseTime.toMillis();
            if (lastResponseTimeMs > 0 && Math.abs(currentMs - lastResponseTimeMs) < 100) {
                consistentTimingCount.incrementAndGet();
            } else {
                consistentTimingCount.set(0);
            }
            lastResponseTimeMs = currentMs;
        }

        public Instant getLastActivity() {
            return lastActivity;
        }

        public int getTotalAttempts() {
            return totalAttempts.get();
        }

        public int getRecentFailures() {
            return recentFailures.get();
        }

        boolean hasConsistentTiming() {
            return consistentTimingCount.get() >= 3;
        }
    }
}
