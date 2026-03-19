package cloud.opencode.base.id.ulid;

/**
 * ULID Configuration
 * ULID配置
 *
 * <p>Configuration for ULID generation including entropy source settings.</p>
 * <p>ULID生成配置，包括熵源设置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Monotonic mode configuration - 单调模式配置</li>
 *   <li>Entropy source selection - 熵源选择</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * UlidConfig config = UlidConfig.defaultConfig();
 * UlidConfig monotonic = UlidConfig.monotonic();
 * }</pre>
 *
 * @param monotonic whether to use monotonic mode | 是否使用单调模式
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public record UlidConfig(boolean monotonic) {

    /**
     * Default configuration (monotonic)
     * 默认配置（单调）
     */
    public static final UlidConfig DEFAULT = new UlidConfig(true);

    /**
     * Creates default configuration
     * 创建默认配置
     *
     * @return default configuration | 默认配置
     */
    public static UlidConfig defaultConfig() {
        return DEFAULT;
    }

    /**
     * Creates monotonic configuration
     * 创建单调配置
     *
     * @return monotonic configuration | 单调配置
     */
    public static UlidConfig withMonotonic() {
        return new UlidConfig(true);
    }

    /**
     * Creates non-monotonic configuration
     * 创建非单调配置
     *
     * @return non-monotonic configuration | 非单调配置
     */
    public static UlidConfig nonMonotonic() {
        return new UlidConfig(false);
    }
}
