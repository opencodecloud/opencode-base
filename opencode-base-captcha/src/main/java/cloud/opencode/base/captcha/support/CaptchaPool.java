package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.generator.CaptchaGenerator;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Captcha Pool - Pre-generates CAPTCHAs in background for instant retrieval
 * 验证码池 - 在后台预生成验证码以实现即时获取
 *
 * <p>This class maintains a pool of pre-generated CAPTCHAs using a background
 * virtual thread that fills the pool asynchronously. When the pool drops below
 * a configurable refill threshold, the background thread wakes up and generates
 * more CAPTCHAs. If the pool is empty when {@link #take()} is called, a CAPTCHA
 * is generated in real-time as a fallback.</p>
 * <p>此类使用后台虚拟线程维护一个预生成的验证码池，异步填充池。当池中数量低于
 * 可配置的补充阈值时，后台线程将被唤醒并生成更多验证码。如果调用 {@link #take()}
 * 时池为空，将实时生成验证码作为降级方案。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Background pre-generation via virtual thread - 通过虚拟线程后台预生成</li>
 *   <li>Configurable pool size and refill threshold - 可配置的池大小和补充阈值</li>
 *   <li>Graceful fallback when pool is empty - 池为空时优雅降级</li>
 *   <li>AutoCloseable for proper resource cleanup - 实现 AutoCloseable 以正确清理资源</li>
 *   <li>Builder pattern for configuration - 构建器模式配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a pool with default settings
 * try (CaptchaPool pool = CaptchaPool.builder()
 *         .config(CaptchaConfig.defaults())
 *         .poolSize(200)
 *         .refillThreshold(0.3f)
 *         .build()) {
 *
 *     // Get a pre-generated CAPTCHA instantly
 *     Captcha captcha = pool.take();
 *
 *     // Check pool status
 *     int available = pool.size();
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for take() from pool, O(generation) for fallback - 时间复杂度: 从池获取 O(1)，降级生成 O(生成时间)</li>
 *   <li>Space complexity: O(poolSize) - 空间复杂度: O(poolSize)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses LinkedBlockingQueue and AtomicBoolean) - 线程安全: 是（使用 LinkedBlockingQueue 和 AtomicBoolean）</li>
 *   <li>Null-safe: No (config must not be null) - 空值安全: 否（config 不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class CaptchaPool implements AutoCloseable {

    /** Default pool size. | 默认池大小。 */
    private static final int DEFAULT_POOL_SIZE = 100;

    /** Maximum allowed pool size. | 允许的最大池大小。 */
    private static final int MAX_POOL_SIZE = 10_000;

    /** Default refill threshold (20%). | 默认补充阈值（20%）。 */
    private static final float DEFAULT_REFILL_THRESHOLD = 0.2f;

    /** Sleep interval in milliseconds while actively filling. | 主动填充时的休眠间隔（毫秒）。 */
    private static final long FILL_SLEEP_MS = 10;

    /** Sleep interval in milliseconds when pool is sufficiently full. | 池已充足时的休眠间隔（毫秒）。 */
    private static final long IDLE_SLEEP_MS = 500;

    /** The pre-generated CAPTCHA pool. | 预生成的验证码池。 */
    private final LinkedBlockingQueue<Captcha> pool;

    /** The CAPTCHA configuration used for generation. | 用于生成的验证码配置。 */
    private final CaptchaConfig config;

    /** The target pool size. | 目标池大小。 */
    private final int poolSize;

    /** The refill threshold ratio (0.0 to 1.0). | 补充阈值比例（0.0 到 1.0）。 */
    private final float refillThreshold;

    /** Whether the pool is running and accepting requests. | 池是否正在运行并接受请求。 */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /** The background daemon thread that fills the pool. | 填充池的后台守护线程。 */
    private final Thread fillThread;

    /** The generator used for CAPTCHA creation. | 用于创建验证码的生成器。 */
    private final CaptchaGenerator generator;

    /**
     * Private constructor; use {@link #builder()} to create instances.
     * 私有构造器；使用 {@link #builder()} 创建实例。
     *
     * @param builder the builder | 构建器
     */
    private CaptchaPool(Builder builder) {
        this.config = builder.config;
        this.poolSize = builder.poolSize;
        this.refillThreshold = builder.refillThreshold;
        this.pool = new LinkedBlockingQueue<>(poolSize);
        this.generator = CaptchaGenerator.forType(config.getType());

        // Create thread last, after all fields are assigned, to ensure
        // the fillLoop sees fully initialized state via the JMM final-field guarantee.
        Thread thread = Thread.ofVirtual()
                .name("captcha-pool-filler")
                .unstarted(this::fillLoop);
        this.fillThread = thread;
        thread.start();
    }

    /**
     * Creates a new builder for CaptchaPool.
     * 创建新的 CaptchaPool 构建器。
     *
     * @return a new builder | 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Takes a CAPTCHA from the pool.
     * 从池中获取一个验证码。
     *
     * <p>If the pool is empty, a CAPTCHA is generated in real-time as a fallback.
     * This ensures that callers always receive a CAPTCHA even under high load.</p>
     * <p>如果池为空，将实时生成验证码作为降级方案。
     * 这确保了即使在高负载下调用者也能始终获得验证码。</p>
     *
     * @return a CAPTCHA instance | 验证码实例
     * @throws IllegalStateException if the pool has been closed | 如果池已关闭
     */
    public Captcha take() {
        if (!running.get()) {
            throw new IllegalStateException("CaptchaPool has been closed");
        }
        Captcha captcha = pool.poll();
        if (captcha != null) {
            return captcha;
        }
        // Fallback: generate in real-time
        return generator.generate(config);
    }

    /**
     * Returns the number of CAPTCHAs currently available in the pool.
     * 返回池中当前可用的验证码数量。
     *
     * @return the current pool size | 当前池大小
     */
    public int size() {
        return pool.size();
    }

    /**
     * Returns whether the pool is running.
     * 返回池是否正在运行。
     *
     * @return true if running | 如果正在运行则返回 true
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Shuts down the pool and interrupts the background fill thread.
     * 关闭池并中断后台填充线程。
     *
     * <p>After closing, {@link #take()} will throw {@link IllegalStateException}.
     * Any remaining CAPTCHAs in the pool are discarded.</p>
     * <p>关闭后，{@link #take()} 将抛出 {@link IllegalStateException}。
     * 池中剩余的验证码将被丢弃。</p>
     */
    @Override
    public void close() {
        if (running.compareAndSet(true, false)) {
            fillThread.interrupt();
            pool.clear();
        }
    }

    /**
     * Background fill loop that runs in the virtual thread.
     * 在虚拟线程中运行的后台填充循环。
     *
     * <p>The loop generates CAPTCHAs when the pool size drops below the refill
     * threshold and sleeps when the pool is full.</p>
     * <p>当池大小低于补充阈值时循环生成验证码，池满时休眠。</p>
     */
    private void fillLoop() {
        int threshold = Math.max(1, (int) (poolSize * refillThreshold));
        while (running.get()) {
            try {
                if (pool.size() < threshold) {
                    // Fill up to pool capacity
                    while (running.get() && pool.size() < poolSize) {
                        Captcha captcha = generator.generate(config);
                        if (!pool.offer(captcha)) {
                            break; // Pool is full
                        }
                    }
                    // Brief pause between fill bursts to avoid CPU saturation
                    Thread.sleep(FILL_SLEEP_MS);
                } else {
                    // Pool is sufficiently full — sleep longer to reduce CPU overhead
                    Thread.sleep(IDLE_SLEEP_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Generation failure - sleep and retry to avoid tight loop
                if (!running.get()) {
                    break;
                }
                try {
                    Thread.sleep(IDLE_SLEEP_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Builder for CaptchaPool
     * CaptchaPool 构建器
     *
     * <p>Provides a fluent API for configuring and creating a CaptchaPool instance.</p>
     * <p>提供用于配置和创建 CaptchaPool 实例的流式 API。</p>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    public static final class Builder {

        private CaptchaConfig config;
        private int poolSize = DEFAULT_POOL_SIZE;
        private float refillThreshold = DEFAULT_REFILL_THRESHOLD;

        private Builder() {}

        /**
         * Sets the CAPTCHA configuration for generation.
         * 设置用于生成的验证码配置。
         *
         * @param config the configuration | 配置
         * @return this builder | 此构建器
         * @throws NullPointerException if config is null | 如果 config 为 null
         */
        public Builder config(CaptchaConfig config) {
            this.config = Objects.requireNonNull(config, "config must not be null");
            return this;
        }

        /**
         * Sets the pool size (maximum number of pre-generated CAPTCHAs).
         * 设置池大小（预生成验证码的最大数量）。
         *
         * <p>Must be between 1 and 10000 (inclusive).</p>
         * <p>必须在 1 到 10000 之间（含端点）。</p>
         *
         * @param poolSize the pool size | 池大小
         * @return this builder | 此构建器
         * @throws IllegalArgumentException if poolSize is out of range | 如果 poolSize 超出范围
         */
        public Builder poolSize(int poolSize) {
            if (poolSize < 1 || poolSize > MAX_POOL_SIZE) {
                throw new IllegalArgumentException(
                        "poolSize must be between 1 and " + MAX_POOL_SIZE + ", got: " + poolSize);
            }
            this.poolSize = poolSize;
            return this;
        }

        /**
         * Sets the refill threshold ratio.
         * 设置补充阈值比例。
         *
         * <p>When the pool drops below this fraction of the pool size, the background
         * thread will start generating more CAPTCHAs. Must be between 0.0 and 1.0
         * (exclusive of 0.0, inclusive of 1.0).</p>
         * <p>当池中数量低于池大小的此比例时，后台线程将开始生成更多验证码。
         * 必须在 0.0（不含）到 1.0（含）之间。</p>
         *
         * @param refillThreshold the refill threshold (0.0 to 1.0) | 补充阈值（0.0 到 1.0）
         * @return this builder | 此构建器
         * @throws IllegalArgumentException if refillThreshold is out of range | 如果 refillThreshold 超出范围
         */
        public Builder refillThreshold(float refillThreshold) {
            if (refillThreshold <= 0.0f || refillThreshold > 1.0f) {
                throw new IllegalArgumentException(
                        "refillThreshold must be between 0.0 (exclusive) and 1.0 (inclusive), got: " + refillThreshold);
            }
            this.refillThreshold = refillThreshold;
            return this;
        }

        /**
         * Builds and starts the CaptchaPool.
         * 构建并启动 CaptchaPool。
         *
         * <p>The background fill thread starts immediately upon construction.</p>
         * <p>后台填充线程在构建时立即启动。</p>
         *
         * @return a new CaptchaPool instance | 新的 CaptchaPool 实例
         * @throws NullPointerException if config has not been set | 如果未设置 config
         */
        public CaptchaPool build() {
            Objects.requireNonNull(config, "config must be set before building CaptchaPool");
            return new CaptchaPool(this);
        }
    }
}
