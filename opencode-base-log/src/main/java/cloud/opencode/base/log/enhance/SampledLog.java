package cloud.opencode.base.log.enhance;

import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sampled Log - Rate-limited and Sampled Logging
 * 采样日志 - 限流和采样的日志
 *
 * <p>SampledLog provides mechanisms to control high-frequency logging
 * through probability sampling, time-based sampling, or count-based sampling.</p>
 * <p>SampledLog 提供通过概率采样、基于时间的采样或基于计数的采样来控制高频日志的机制。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // 1% probability sampling
 * SampledLogger sampledLog = SampledLog.sample(0.01);
 * sampledLog.info("High frequency event: {}", eventId);
 *
 * // Time-based: at most once per 5 seconds
 * SampledLogger rateLimited = SampledLog.sampleByTime(Duration.ofSeconds(5));
 * rateLimited.warn("Rate limited warning");
 *
 * // Count-based: every 100th log
 * SampledLogger countBased = SampledLog.sampleByCount(100);
 * countBased.debug("Processing item {}", itemId);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Probability-based sampling (0.0 to 1.0) - 基于概率的采样（0.0 到 1.0）</li>
 *   <li>Time-based sampling (minimum interval) - 基于时间的采样（最小间隔）</li>
 *   <li>Count-based sampling (every Nth message) - 基于计数的采样（每 N 条消息）</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (AtomicLong + ThreadLocalRandom) - 线程安全: 是（AtomicLong + ThreadLocalRandom）</li>
 *   <li>Null-safe: No (format must not be null) - 空值安全: 否（格式不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class SampledLog {

    private SampledLog() {
        // Utility class
    }

    /**
     * Creates a probability-based sampled logger.
     * 创建基于概率的采样日志记录器。
     *
     * @param sampleRate the sample rate (0.0 to 1.0) - 采样率
     * @return the sampled logger - 采样日志记录器
     */
    public static SampledLogger sample(double sampleRate) {
        if (sampleRate < 0 || sampleRate > 1) {
            throw new IllegalArgumentException("Sample rate must be between 0.0 and 1.0");
        }
        return new ProbabilitySampledLogger(sampleRate);
    }

    /**
     * Creates a time-based sampled logger.
     * 创建基于时间的采样日志记录器。
     *
     * @param interval the minimum interval between logs - 日志之间的最小间隔
     * @return the sampled logger - 采样日志记录器
     */
    public static SampledLogger sampleByTime(Duration interval) {
        return new TimeSampledLogger(interval.toMillis());
    }

    /**
     * Creates a count-based sampled logger.
     * 创建基于计数的采样日志记录器。
     *
     * @param every log every Nth message - 每 N 条消息记录一次
     * @return the sampled logger - 采样日志记录器
     */
    public static SampledLogger sampleByCount(int every) {
        if (every < 1) {
            throw new IllegalArgumentException("Count must be at least 1");
        }
        return new CountSampledLogger(every);
    }

    /**
     * Sampled Logger Interface.
     * 采样日志记录器接口。
     */
    public interface SampledLogger {
        void trace(String format, Object... args);
        void debug(String format, Object... args);
        void info(String format, Object... args);
        void warn(String format, Object... args);
        void error(String format, Object... args);

        /**
         * Checks if the next log should be sampled.
         * 检查下一条日志是否应该被采样。
         *
         * @return true if should log - 如果应该记录返回 true
         */
        boolean shouldLog();
    }

    // ==================== Implementations ====================

    private static abstract class AbstractSampledLogger implements SampledLogger {
        private final Logger logger;

        protected AbstractSampledLogger() {
            String callerClass = StackWalker.getInstance()
                    .walk(frames -> frames
                            .skip(3)
                            .map(StackWalker.StackFrame::getClassName)
                            .findFirst()
                            .orElse("UNKNOWN"));
            this.logger = LoggerFactory.getLogger(callerClass);
        }

        @Override
        public void trace(String format, Object... args) {
            if (shouldLog() && logger.isTraceEnabled()) {
                logger.trace(format, args);
            }
        }

        @Override
        public void debug(String format, Object... args) {
            if (shouldLog() && logger.isDebugEnabled()) {
                logger.debug(format, args);
            }
        }

        @Override
        public void info(String format, Object... args) {
            if (shouldLog() && logger.isInfoEnabled()) {
                logger.info(format, args);
            }
        }

        @Override
        public void warn(String format, Object... args) {
            if (shouldLog() && logger.isWarnEnabled()) {
                logger.warn(format, args);
            }
        }

        @Override
        public void error(String format, Object... args) {
            if (shouldLog() && logger.isErrorEnabled()) {
                logger.error(format, args);
            }
        }
    }

    private static final class ProbabilitySampledLogger extends AbstractSampledLogger {
        private final double sampleRate;

        ProbabilitySampledLogger(double sampleRate) {
            this.sampleRate = sampleRate;
        }

        @Override
        public boolean shouldLog() {
            return ThreadLocalRandom.current().nextDouble() < sampleRate;
        }
    }

    private static final class TimeSampledLogger extends AbstractSampledLogger {
        private final long intervalMs;
        private final AtomicLong lastLogTime = new AtomicLong(0);

        TimeSampledLogger(long intervalMs) {
            this.intervalMs = intervalMs;
        }

        @Override
        public boolean shouldLog() {
            long now = System.currentTimeMillis();
            long last = lastLogTime.get();
            if (now - last >= intervalMs) {
                return lastLogTime.compareAndSet(last, now);
            }
            return false;
        }
    }

    private static final class CountSampledLogger extends AbstractSampledLogger {
        private final int every;
        private final AtomicLong counter = new AtomicLong(0);

        CountSampledLogger(int every) {
            this.every = every;
        }

        @Override
        public boolean shouldLog() {
            return counter.incrementAndGet() % every == 0;
        }
    }
}
