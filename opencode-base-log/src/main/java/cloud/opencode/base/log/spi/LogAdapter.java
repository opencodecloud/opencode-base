package cloud.opencode.base.log.spi;

import cloud.opencode.base.log.marker.Marker;

/**
 * Log Adapter Interface - Logging Framework Adapter
 * 日志适配器接口 - 日志框架适配器
 *
 * <p>This interface provides additional adapter functionality for logging frameworks,
 * such as marker conversion and message formatting.</p>
 * <p>此接口为日志框架提供额外的适配器功能，如标记转换和消息格式化。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Marker conversion between OpenLog and framework-specific markers - OpenLog 与框架特定标记之间的标记转换</li>
 *   <li>Message formatting with arguments - 使用参数的消息格式化</li>
 *   <li>Feature capability detection (markers, MDC, NDC) - 功能能力检测（标记、MDC、NDC）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implement custom log adapter
 * public class MyLogAdapter implements LogAdapter {
 *     @Override
 *     public Object convertMarker(Marker marker) {
 *         return myFramework.getMarker(marker.getName());
 *     }
 *     @Override
 *     public String formatMessage(String format, Object... args) {
 *         return MessageFormat.format(format, args);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (marker/format must not be null) - 空值安全: 否（标记/格式不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public interface LogAdapter {

    /**
     * Converts an OpenLog marker to the framework-specific marker.
     * 将 OpenLog 标记转换为框架特定的标记。
     *
     * @param marker the OpenLog marker - OpenLog 标记
     * @return the framework-specific marker object - 框架特定的标记对象
     */
    Object convertMarker(Marker marker);

    /**
     * Formats a message with arguments.
     * 使用参数格式化消息。
     *
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     * @return the formatted message - 格式化的消息
     */
    String formatMessage(String format, Object... args);

    /**
     * Checks if the adapter supports markers.
     * 检查适配器是否支持标记。
     *
     * @return true if markers are supported - 如果支持标记返回 true
     */
    default boolean supportsMarkers() {
        return true;
    }

    /**
     * Checks if the adapter supports MDC.
     * 检查适配器是否支持 MDC。
     *
     * @return true if MDC is supported - 如果支持 MDC 返回 true
     */
    default boolean supportsMDC() {
        return true;
    }

    /**
     * Checks if the adapter supports NDC.
     * 检查适配器是否支持 NDC。
     *
     * @return true if NDC is supported - 如果支持 NDC 返回 true
     */
    default boolean supportsNDC() {
        return false;
    }
}
