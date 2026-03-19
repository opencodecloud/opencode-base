package cloud.opencode.base.parallel.executor;

import java.time.Duration;

/**
 * Executor Config - Executor Configuration
 * 执行器配置 - 执行器配置
 *
 * <p>Configuration options for virtual thread executors.</p>
 * <p>虚拟线程执行器的配置选项。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * ExecutorConfig config = ExecutorConfig.builder()
 *     .namePrefix("worker-")
 *     .maxConcurrency(100)
 *     .taskTimeout(Duration.ofSeconds(30))
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Concurrency limit configuration - 并发限制配置</li>
 *   <li>Task timeout configuration - 任务超时配置</li>
 *   <li>Thread naming configuration - 线程命名配置</li>
 *   <li>Builder pattern - 构建器模式</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class ExecutorConfig {

    private final String namePrefix;
    private final int maxConcurrency;
    private final Duration taskTimeout;
    private final boolean inheritThreadLocals;
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    private ExecutorConfig(Builder builder) {
        this.namePrefix = builder.namePrefix;
        this.maxConcurrency = builder.maxConcurrency;
        this.taskTimeout = builder.taskTimeout;
        this.inheritThreadLocals = builder.inheritThreadLocals;
        this.uncaughtExceptionHandler = builder.uncaughtExceptionHandler;
    }

    /**
     * Creates a default configuration.
     * 创建默认配置。
     *
     * @return the default config - 默认配置
     */
    public static ExecutorConfig defaults() {
        return new Builder().build();
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Getters ====================

    /**
     * Gets the thread name prefix.
     * 获取线程名称前缀。
     *
     * @return the name prefix - 名称前缀
     */
    public String getNamePrefix() {
        return namePrefix;
    }

    /**
     * Gets the maximum concurrency.
     * 获取最大并发数。
     *
     * @return the max concurrency - 最大并发数
     */
    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    /**
     * Gets the task timeout.
     * 获取任务超时。
     *
     * @return the timeout or null - 超时或 null
     */
    public Duration getTaskTimeout() {
        return taskTimeout;
    }

    /**
     * Checks if thread locals should be inherited.
     * 检查是否应继承线程本地变量。
     *
     * @return true if inherit - 如果继承返回 true
     */
    public boolean isInheritThreadLocals() {
        return inheritThreadLocals;
    }

    /**
     * Gets the uncaught exception handler.
     * 获取未捕获异常处理器。
     *
     * @return the handler or null - 处理器或 null
     */
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    // ==================== Builder ====================

    /**
     * Builder for ExecutorConfig.
     * ExecutorConfig 的构建器。
     */
    public static final class Builder {
        private String namePrefix = "virtual-";
        private int maxConcurrency = Integer.MAX_VALUE;
        private Duration taskTimeout;
        private boolean inheritThreadLocals = true;
        private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

        private Builder() {
        }

        /**
         * Sets the thread name prefix.
         * 设置线程名称前缀。
         *
         * @param namePrefix the name prefix - 名称前缀
         * @return this builder - 此构建器
         */
        public Builder namePrefix(String namePrefix) {
            this.namePrefix = namePrefix;
            return this;
        }

        /**
         * Sets the maximum concurrency.
         * 设置最大并发数。
         *
         * @param maxConcurrency the max concurrency - 最大并发数
         * @return this builder - 此构建器
         */
        public Builder maxConcurrency(int maxConcurrency) {
            if (maxConcurrency <= 0) {
                throw new IllegalArgumentException("Max concurrency must be positive: " + maxConcurrency);
            }
            this.maxConcurrency = maxConcurrency;
            return this;
        }

        /**
         * Sets the task timeout.
         * 设置任务超时。
         *
         * @param taskTimeout the timeout - 超时
         * @return this builder - 此构建器
         */
        public Builder taskTimeout(Duration taskTimeout) {
            this.taskTimeout = taskTimeout;
            return this;
        }

        /**
         * Sets whether to inherit thread locals.
         * 设置是否继承线程本地变量。
         *
         * @param inheritThreadLocals whether to inherit - 是否继承
         * @return this builder - 此构建器
         */
        public Builder inheritThreadLocals(boolean inheritThreadLocals) {
            this.inheritThreadLocals = inheritThreadLocals;
            return this;
        }

        /**
         * Sets the uncaught exception handler.
         * 设置未捕获异常处理器。
         *
         * @param handler the handler - 处理器
         * @return this builder - 此构建器
         */
        public Builder uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
            this.uncaughtExceptionHandler = handler;
            return this;
        }

        /**
         * Builds the config.
         * 构建配置。
         *
         * @return the config - 配置
         */
        public ExecutorConfig build() {
            return new ExecutorConfig(this);
        }
    }
}
