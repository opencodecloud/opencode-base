package cloud.opencode.base.email;

import java.time.Duration;

/**
 * SMTP Connection Test Result
 * SMTP连接测试结果
 *
 * <p>Result of testing SMTP server connectivity and authentication.</p>
 * <p>测试SMTP服务器连接和认证的结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Connection status - 连接状态</li>
 *   <li>Latency measurement - 延迟测量</li>
 *   <li>Error details - 错误详情</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ConnectionTestResult result = sender.testConnection();
 * if (result.success()) {
 *     System.out.println("Connected in " + result.latency().toMillis() + "ms");
 * } else {
 *     System.err.println("Failed: " + result.errorMessage());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public record ConnectionTestResult(
        boolean success,
        String serverGreeting,
        Duration latency,
        String errorMessage,
        Throwable cause
) {

    /**
     * Create a successful test result
     * 创建成功的测试结果
     *
     * @param serverGreeting the SMTP server greeting | SMTP服务器问候语
     * @param latency        the connection latency | 连接延迟
     * @return the result | 结果
     */
    public static ConnectionTestResult success(String serverGreeting, Duration latency) {
        return new ConnectionTestResult(true, serverGreeting, latency, null, null);
    }

    /**
     * Create a failed test result
     * 创建失败的测试结果
     *
     * @param errorMessage the error message | 错误消息
     * @param cause        the exception cause | 异常原因
     * @param latency      the time spent before failure | 失败前的耗时
     * @return the result | 结果
     */
    public static ConnectionTestResult failure(String errorMessage, Throwable cause, Duration latency) {
        return new ConnectionTestResult(false, null, latency, errorMessage, cause);
    }
}
