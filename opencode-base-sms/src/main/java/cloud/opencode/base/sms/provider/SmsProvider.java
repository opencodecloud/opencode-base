package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;

import java.util.List;

/**
 * SMS Provider
 * 短信提供商
 *
 * <p>Interface for SMS providers.</p>
 * <p>短信提供商接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single and batch SMS sending - 单条和批量短信发送</li>
 *   <li>Provider availability check - 提供商可用性检查</li>
 *   <li>Default batch implementation via stream - 默认批量实现通过流</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SmsProvider provider = SmsProviderFactory.create(config);
 * SmsResult result = provider.send(SmsMessage.of("13800138000", "Hello"));
 * boolean available = provider.isAvailable();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public interface SmsProvider {

    /**
     * Send single SMS
     * 发送单条短信
     *
     * @param message the message | 消息
     * @return the result | 结果
     */
    SmsResult send(SmsMessage message);

    /**
     * Send batch SMS
     * 批量发送短信
     *
     * @param messages the messages | 消息列表
     * @return the results | 结果列表
     */
    default List<SmsResult> sendBatch(List<SmsMessage> messages) {
        return messages.stream().map(this::send).toList();
    }

    /**
     * Get provider name
     * 获取提供商名称
     *
     * @return the name | 名称
     */
    String getName();

    /**
     * Check if provider is available
     * 检查提供商是否可用
     *
     * @return true if available | 如果可用返回true
     */
    default boolean isAvailable() {
        return true;
    }

    /**
     * Close provider resources
     * 关闭提供商资源
     */
    default void close() {
        // No-op by default
    }
}
