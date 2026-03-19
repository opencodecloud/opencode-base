package cloud.opencode.base.web;

/**
 * Result Code Interface
 * 响应码接口
 *
 * <p>Defines the contract for result codes used in API responses.</p>
 * <p>定义API响应中使用的响应码契约。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified code format - 统一的响应码格式</li>
 *   <li>Extensible via enum - 可通过枚举扩展</li>
 *   <li>HTTP status mapping - HTTP状态码映射</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ResultCode custom = ResultCode.of("E001", "Not found", 404);
 * String code = custom.getCode();
 * boolean ok = custom.isSuccess();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (interface with stateless defaults) - 是（带无状态默认方法的接口）</li>
 *   <li>Null-safe: No (code and message should not be null) - 否（响应码和消息不应为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public interface ResultCode {

    /**
     * Get the code
     * 获取响应码
     *
     * @return the code | 响应码
     */
    String getCode();

    /**
     * Get the message
     * 获取响应消息
     *
     * @return the message | 响应消息
     */
    String getMessage();

    /**
     * Get the HTTP status code
     * 获取HTTP状态码
     *
     * @return the HTTP status code | HTTP状态码
     */
    default int getHttpStatus() {
        return 200;
    }

    /**
     * Check if success
     * 检查是否成功
     *
     * @return true if success | 如果成功返回true
     */
    default boolean isSuccess() {
        return getHttpStatus() >= 200 && getHttpStatus() < 300;
    }

    /**
     * Create custom result code
     * 创建自定义响应码
     *
     * @param code the code | 响应码
     * @param message the message | 响应消息
     * @return result code instance | 响应码实例
     */
    static ResultCode of(String code, String message) {
        return of(code, message, 200);
    }

    /**
     * Create custom result code with HTTP status
     * 创建带HTTP状态的自定义响应码
     *
     * @param code the code | 响应码
     * @param message the message | 响应消息
     * @param httpStatus the HTTP status | HTTP状态码
     * @return result code instance | 响应码实例
     */
    static ResultCode of(String code, String message, int httpStatus) {
        return new ResultCode() {
            @Override
            public String getCode() {
                return code;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public int getHttpStatus() {
                return httpStatus;
            }
        };
    }
}
