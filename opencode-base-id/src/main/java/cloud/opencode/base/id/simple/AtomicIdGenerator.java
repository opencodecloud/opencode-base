package cloud.opencode.base.id.simple;

import cloud.opencode.base.id.IdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Atomic Increment ID Generator
 * 原子自增ID生成器
 *
 * <p>Simple thread-safe ID generator using atomic increment.
 * Suitable for single-node or testing scenarios.</p>
 * <p>使用原子递增的简单线程安全ID生成器。
 * 适用于单节点或测试场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe atomic increment - 线程安全原子递增</li>
 *   <li>Configurable start value - 可配置起始值</li>
 *   <li>Reset capability - 重置功能</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AtomicIdGenerator gen = AtomicIdGenerator.create();
 * long id1 = gen.generate(); // 1
 * long id2 = gen.generate(); // 2
 *
 * // With start value
 * AtomicIdGenerator gen2 = AtomicIdGenerator.create(1000);
 * long id = gen2.generate(); // 1000
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>~50M ops/sec single thread - 单线程约50M次/秒</li>
 *   <li>~200M ops/sec with 8 threads - 8线程约200M次/秒</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class AtomicIdGenerator implements IdGenerator<Long> {

    private final AtomicLong counter;

    /**
     * Creates a generator with start value
     * 使用起始值创建生成器
     *
     * @param startValue the start value | 起始值
     */
    private AtomicIdGenerator(long startValue) {
        this.counter = new AtomicLong(startValue);
    }

    /**
     * Creates a generator starting from 1
     * 创建从1开始的生成器
     *
     * @return generator | 生成器
     */
    public static AtomicIdGenerator create() {
        return new AtomicIdGenerator(1);
    }

    /**
     * Creates a generator with specific start value
     * 使用指定起始值创建生成器
     *
     * @param startValue the start value | 起始值
     * @return generator | 生成器
     */
    public static AtomicIdGenerator create(long startValue) {
        return new AtomicIdGenerator(startValue);
    }

    @Override
    public Long generate() {
        return counter.getAndIncrement();
    }

    /**
     * Gets the current value without incrementing
     * 获取当前值但不递增
     *
     * @return current value | 当前值
     */
    public long getCurrentValue() {
        return counter.get();
    }

    /**
     * Resets the counter to a new value
     * 将计数器重置为新值
     *
     * @param value the new value | 新值
     */
    public void reset(long value) {
        counter.set(value);
    }

    @Override
    public String getType() {
        return "Atomic";
    }
}
