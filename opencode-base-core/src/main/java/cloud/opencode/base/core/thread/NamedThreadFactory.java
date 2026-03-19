package cloud.opencode.base.core.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Named Thread Factory - Custom thread factory with naming support
 * 命名线程工厂 - 支持自定义命名的线程工厂
 *
 * <p>ThreadFactory implementation with configurable naming, daemon, and priority settings.</p>
 * <p>线程工厂实现，支持配置命名、守护进程和优先级。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom thread name prefix with auto-numbering - 自定义线程名前缀和自动编号</li>
 *   <li>Daemon thread configuration - 守护线程配置</li>
 *   <li>Thread priority configuration - 线程优先级配置</li>
 *   <li>Builder pattern support - 构建器模式支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple factory - 简单工厂
 * ThreadFactory factory = new NamedThreadFactory("worker");
 *
 * // Daemon factory - 守护线程工厂
 * ThreadFactory daemon = NamedThreadFactory.daemon("background");
 *
 * // Builder pattern - 构建器模式
 * ThreadFactory custom = NamedThreadFactory.builder()
 *     .namePrefix("task")
 *     .daemon(true)
 *     .priority(Thread.MAX_PRIORITY)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (AtomicInteger counter) - 线程安全: 是</li>
 *   <li>Null-safe: No (requires name prefix) - 空值安全: 否</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemon;
    private final int priority;

    public NamedThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }

    public NamedThreadFactory(String namePrefix, boolean daemon) {
        this(namePrefix, daemon, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String namePrefix, boolean daemon, int priority) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = Thread.ofPlatform()
            .name(namePrefix + "-" + threadNumber.getAndIncrement())
            .daemon(daemon)
            .priority(priority)
            .unstarted(r);
        return t;
    }

    /**
     * Creates a daemon thread factory
     * 创建守护线程工厂
     */
    public static NamedThreadFactory daemon(String namePrefix) {
        return new NamedThreadFactory(namePrefix, true);
    }

    /**
     * Creates a non-daemon thread factory
     * 创建非守护线程工厂
     */
    public static NamedThreadFactory nonDaemon(String namePrefix) {
        return new NamedThreadFactory(namePrefix, false);
    }

    /**
     * Builder
     * 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String namePrefix = "thread";
        private boolean daemon = false;
        private int priority = Thread.NORM_PRIORITY;

        public Builder namePrefix(String namePrefix) {
            this.namePrefix = namePrefix;
            return this;
        }

        public Builder daemon(boolean daemon) {
            this.daemon = daemon;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public NamedThreadFactory build() {
            return new NamedThreadFactory(namePrefix, daemon, priority);
        }
    }
}
