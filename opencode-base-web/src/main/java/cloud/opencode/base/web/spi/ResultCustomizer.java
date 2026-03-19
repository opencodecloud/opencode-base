package cloud.opencode.base.web.spi;

import cloud.opencode.base.web.Result;

/**
 * Result Customizer SPI
 * 响应定制器SPI
 *
 * <p>Service Provider Interface for customizing results before returning.</p>
 * <p>在返回之前定制响应的服务提供者接口。</p>
 *
 * <p><strong>Usage | 使用方式:</strong></p>
 * <pre>{@code
 * public class TraceIdCustomizer implements ResultCustomizer {
 *     @Override
 *     public <T> Result<T> customize(Result<T> result) {
 *         // Add trace ID to result
 *         return result;
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI for result customization - 响应定制SPI</li>
 *   <li>Conditional application support - 条件应用支持</li>
 *   <li>Priority ordering support - 优先级排序支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class AuditCustomizer implements ResultCustomizer {
 *     public <T> Result<T> customize(Result<T> result) {
 *         return result.withTraceId(generateTraceId());
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 取决于实现</li>
 *   <li>Null-safe: No (result should not be null) - 否（结果不应为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public interface ResultCustomizer {

    /**
     * Customize the result
     * 定制响应
     *
     * @param result the original result | 原始响应
     * @param <T> the data type | 数据类型
     * @return the customized result | 定制后的响应
     */
    <T> Result<T> customize(Result<T> result);

    /**
     * Get the customizer order
     * 获取定制器顺序
     *
     * <p>Lower values execute first.</p>
     * <p>值越小越先执行。</p>
     *
     * @return the order | 顺序
     */
    default int getOrder() {
        return 0;
    }

    /**
     * Check if should apply to this result
     * 检查是否应用于此响应
     *
     * @param result the result to check | 要检查的响应
     * @return true if should apply | 如果应该应用返回true
     */
    default boolean shouldApply(Result<?> result) {
        return true;
    }
}
