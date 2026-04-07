package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaType;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Captcha Metrics - Lightweight metrics collection for CAPTCHA operations
 * 验证码指标 - 轻量级验证码操作指标收集器
 *
 * <p>This class collects operational metrics for CAPTCHA generation and validation,
 * including generation counts, validation success/failure rates, response times,
 * and type distribution. All counters use {@link LongAdder} for high-throughput
 * concurrent updates.</p>
 * <p>此类收集验证码生成和验证的操作指标，包括生成次数、验证成功/失败率、
 * 响应时间和类型分布。所有计数器使用 {@link LongAdder} 以支持高吞吐量并发更新。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Generation count tracking by type - 按类型跟踪生成次数</li>
 *   <li>Validation success/failure rate tracking - 验证成功/失败率跟踪</li>
 *   <li>Average response time calculation - 平均响应时间计算</li>
 *   <li>Immutable point-in-time snapshots - 不可变的时间点快照</li>
 *   <li>Reset capability for metric rotation - 用于指标轮换的重置功能</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaMetrics metrics = CaptchaMetrics.create();
 *
 * // Record generation
 * metrics.recordGeneration(CaptchaType.ALPHANUMERIC);
 *
 * // Record validation with response time
 * Instant start = Instant.now();
 * boolean success = validate(captchaId, answer);
 * metrics.recordValidation(success, Duration.between(start, Instant.now()));
 *
 * // Take a snapshot
 * CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();
 * System.out.println("Success rate: " + snapshot.successRate());
 * System.out.println("Average response time: " + snapshot.averageResponseTime());
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for all recording operations - 时间复杂度: 所有记录操作 O(1)</li>
 *   <li>Space complexity: O(T) where T is the number of CaptchaType values used - 空间复杂度: O(T)，T 为使用的 CaptchaType 值数量</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses LongAdder and ConcurrentHashMap) - 线程安全: 是（使用 LongAdder 和 ConcurrentHashMap）</li>
 *   <li>Null-safe: No (CaptchaType must not be null) - 空值安全: 否（CaptchaType 不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class CaptchaMetrics {

    /** Total number of CAPTCHAs generated. | 生成的验证码总数。 */
    private final LongAdder totalGenerated = new LongAdder();

    /** Total number of validation attempts. | 验证尝试总数。 */
    private final LongAdder totalValidations = new LongAdder();

    /** Number of successful validations. | 验证成功次数。 */
    private final LongAdder successfulValidations = new LongAdder();

    /** Number of failed validations. | 验证失败次数。 */
    private final LongAdder failedValidations = new LongAdder();

    /** Per-type generation counters. | 按类型的生成计数器。 */
    private final Map<CaptchaType, LongAdder> generationsByType = new ConcurrentHashMap<>();

    /** Cumulative response time in nanoseconds. | 累计响应时间（纳秒）。 */
    private final LongAdder totalResponseTimeNanos = new LongAdder();

    /** Metrics start time (reset on {@link #reset()}). | 指标起始时间（调用 {@link #reset()} 时重置）。 */
    private volatile Instant startTime;

    /**
     * Private constructor; use {@link #create()} factory method.
     * 私有构造器；使用 {@link #create()} 工厂方法。
     */
    private CaptchaMetrics() {
        this.startTime = Instant.now();
    }

    /**
     * Creates a new metrics instance.
     * 创建新的指标实例。
     *
     * @return a new CaptchaMetrics instance | 新的 CaptchaMetrics 实例
     */
    public static CaptchaMetrics create() {
        return new CaptchaMetrics();
    }

    /**
     * Records a CAPTCHA generation event.
     * 记录一次验证码生成事件。
     *
     * @param type the CAPTCHA type that was generated | 生成的验证码类型
     * @throws NullPointerException if type is null | 如果 type 为 null
     */
    public void recordGeneration(CaptchaType type) {
        Objects.requireNonNull(type, "type must not be null");
        totalGenerated.increment();
        generationsByType.computeIfAbsent(type, k -> new LongAdder()).increment();
    }

    /**
     * Records a CAPTCHA validation result with response time.
     * 记录一次验证码验证结果及响应时间。
     *
     * <p>The response time is added to the cumulative total for average calculation.
     * A {@code null} response time is silently ignored.</p>
     * <p>响应时间会被累加到总计中用于计算平均值。{@code null} 响应时间会被静默忽略。</p>
     *
     * @param success      whether the validation was successful | 验证是否成功
     * @param responseTime the response time, or null if not measured | 响应时间，如果未测量则为 null
     */
    public void recordValidation(boolean success, Duration responseTime) {
        totalValidations.increment();
        if (success) {
            successfulValidations.increment();
        } else {
            failedValidations.increment();
        }
        if (responseTime != null) {
            totalResponseTimeNanos.add(responseTime.toNanos());
        }
    }

    /**
     * Records a CAPTCHA validation result without response time.
     * 记录一次验证码验证结果（无响应时间）。
     *
     * @param success whether the validation was successful | 验证是否成功
     */
    public void recordValidation(boolean success) {
        recordValidation(success, null);
    }

    /**
     * Takes an immutable point-in-time snapshot of all metrics.
     * 获取所有指标的不可变时间点快照。
     *
     * <p>Because individual counters are read independently, the snapshot may
     * reflect a slightly inconsistent view under heavy concurrent updates.
     * This is acceptable for monitoring purposes.</p>
     * <p>由于各计数器独立读取，在高并发更新下快照可能反映略不一致的视图。
     * 这对于监控目的是可接受的。</p>
     *
     * @return an immutable metrics snapshot | 不可变的指标快照
     */
    public MetricsSnapshot snapshot() {
        long totalGen = totalGenerated.sum();
        long totalVal = totalValidations.sum();
        long successVal = successfulValidations.sum();
        long failedVal = failedValidations.sum();
        long totalNanos = totalResponseTimeNanos.sum();

        Duration avgResponseTime = totalVal > 0
                ? Duration.ofNanos(totalNanos / totalVal)
                : Duration.ZERO;
        double successRate = totalVal > 0
                ? (double) successVal / totalVal
                : 0.0;

        Map<CaptchaType, Long> byType = new EnumMap<>(CaptchaType.class);
        generationsByType.forEach((type, adder) -> byType.put(type, adder.sum()));

        Duration uptime = Duration.between(startTime, Instant.now());

        return new MetricsSnapshot(
                totalGen, totalVal, successVal, failedVal,
                successRate, avgResponseTime, Map.copyOf(byType), uptime
        );
    }

    /**
     * Resets all metrics counters and restarts the uptime clock.
     * 重置所有指标计数器并重启运行时间计时。
     *
     * <p><strong>Note:</strong> This method is <em>not</em> atomic — a concurrent
     * {@link #snapshot()} may observe partially-reset state (e.g., {@code successRate > 1.0}).
     * This is acceptable for monitoring; if exact atomicity is required, callers must
     * provide external synchronization.</p>
     * <p><strong>注意:</strong> 此方法<em>不是</em>原子的 — 并发的 {@link #snapshot()} 可能
     * 观察到部分重置状态。这对于监控是可接受的；如需精确原子性，调用者须自行同步。</p>
     */
    public void reset() {
        totalGenerated.reset();
        totalValidations.reset();
        successfulValidations.reset();
        failedValidations.reset();
        totalResponseTimeNanos.reset();
        generationsByType.clear();
        startTime = Instant.now();
    }

    /**
     * Immutable Metrics Snapshot - Point-in-time capture of all CAPTCHA metrics
     * 不可变指标快照 - 所有验证码指标的时间点捕获
     *
     * <p>This record holds an immutable snapshot of all metric values at the time
     * of creation. The {@code generationsByType} map is defensively copied.</p>
     * <p>此记录保存创建时所有指标值的不可变快照。{@code generationsByType} 映射会进行防御性复制。</p>
     *
     * @param totalGenerated       total CAPTCHAs generated | 生成的验证码总数
     * @param totalValidations     total validation attempts | 验证尝试总数
     * @param successfulValidations successful validations | 验证成功次数
     * @param failedValidations    failed validations | 验证失败次数
     * @param successRate          validation success rate (0.0 to 1.0) | 验证成功率（0.0 到 1.0）
     * @param averageResponseTime  average validation response time | 平均验证响应时间
     * @param generationsByType    generation counts per type | 按类型的生成次数
     * @param uptime               time since metrics creation or last reset | 自指标创建或上次重置以来的时间
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    public record MetricsSnapshot(
            long totalGenerated,
            long totalValidations,
            long successfulValidations,
            long failedValidations,
            double successRate,
            Duration averageResponseTime,
            Map<CaptchaType, Long> generationsByType,
            Duration uptime
    ) {

        /**
         * Compact constructor that performs defensive copy of the generationsByType map.
         * 紧凑构造器，对 generationsByType 映射执行防御性复制。
         *
         * @throws NullPointerException if averageResponseTime, generationsByType, or uptime is null |
         *         如果 averageResponseTime、generationsByType 或 uptime 为 null
         */
        public MetricsSnapshot {
            Objects.requireNonNull(averageResponseTime, "averageResponseTime must not be null");
            Objects.requireNonNull(generationsByType, "generationsByType must not be null");
            Objects.requireNonNull(uptime, "uptime must not be null");
            generationsByType = Map.copyOf(generationsByType);
        }
    }
}
