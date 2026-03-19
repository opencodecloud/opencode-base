package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.exception.OpenIdGenerationException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Extension Strategy for Clock Backward Handling
 * 时钟回拨扩展策略
 *
 * <p>Uses extension bits to handle clock backward by incrementing an
 * extension value while keeping the last timestamp. This allows
 * continued ID generation without waiting.</p>
 * <p>使用扩展位处理时钟回拨，通过递增扩展值并保持上次时间戳来实现。
 * 这允许在不等待的情况下继续生成ID。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Non-blocking backward handling - 非阻塞回拨处理</li>
 *   <li>Configurable extension bits - 可配置扩展位数</li>
 *   <li>Fail-fast when extension exhausted - 扩展耗尽时快速失败</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // 2 bits = up to 4 backward events
 * Extend extend = new Extend(2);
 *
 * SnowflakeGenerator gen = SnowflakeBuilder.create()
 *     .clockBackwardStrategy(new Extend(2))
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses AtomicInteger) - 线程安全: 是（使用AtomicInteger）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class Extend implements ClockBackwardStrategy {

    private final int extensionBits;
    private final int maxExtension;
    private final AtomicInteger extensionValue;

    /**
     * Creates an extension strategy with specified bits
     * 使用指定位数创建扩展策略
     *
     * @param extensionBits the number of extension bits | 扩展位数
     */
    public Extend(int extensionBits) {
        if (extensionBits <= 0 || extensionBits > 10) {
            throw new IllegalArgumentException("Extension bits must be between 1 and 10");
        }
        this.extensionBits = extensionBits;
        this.maxExtension = (1 << extensionBits) - 1;
        this.extensionValue = new AtomicInteger(0);
    }

    /**
     * Gets the number of extension bits
     * 获取扩展位数
     *
     * @return extension bits | 扩展位数
     */
    public int extensionBits() {
        return extensionBits;
    }

    /**
     * Gets the current extension value
     * 获取当前扩展值
     *
     * @return extension value | 扩展值
     */
    public int extensionValue() {
        return extensionValue.get();
    }

    /**
     * Gets the maximum extension value
     * 获取最大扩展值
     *
     * @return maximum extension value | 最大扩展值
     */
    public int maxExtension() {
        return maxExtension;
    }

    /**
     * Resets the extension value
     * 重置扩展值
     */
    public void reset() {
        extensionValue.set(0);
    }

    @Override
    public long handle(long lastTimestamp, long currentTimestamp) {
        int current = extensionValue.getAndIncrement();
        if (current >= maxExtension) {
            throw OpenIdGenerationException.clockBackward(lastTimestamp, currentTimestamp);
        }
        return lastTimestamp;
    }

    @Override
    public String toString() {
        return String.format("Extend{bits=%d, value=%d, max=%d}",
                extensionBits, extensionValue.get(), maxExtension);
    }
}
