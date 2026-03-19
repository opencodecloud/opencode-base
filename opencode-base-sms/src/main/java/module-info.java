/**
 * OpenCode Base SMS Module
 * OpenCode 基础短信模块
 *
 * <p>Provides SMS sending capabilities with support for multiple providers
 * (Aliyun, Tencent, Huawei) via SPI mechanism.</p>
 * <p>提供短信发送能力，通过 SPI 机制支持多个服务商（阿里云、腾讯云、华为云）。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multi-provider Support - Aliyun, Tencent, Huawei - 多服务商支持</li>
 *   <li>Template Management - Template registration and parsing - 模板管理</li>
 *   <li>Batch Sending - Virtual Thread optimized - 批量发送</li>
 *   <li>Phone Validation - China mobile, E.164 format - 手机号验证</li>
 *   <li>Rate Limiting - Minute/Hour/Day limits - 频率限制</li>
 *   <li>Log Sanitization - Sensitive data masking - 日志脱敏</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-sms V1.0.0
 */
module cloud.opencode.base.sms {
    // Required modules
    requires transitive cloud.opencode.base.core;
    requires java.net.http;

    // Export public API packages
    exports cloud.opencode.base.sms;
    exports cloud.opencode.base.sms.batch;
    exports cloud.opencode.base.sms.config;
    exports cloud.opencode.base.sms.exception;
    exports cloud.opencode.base.sms.message;
    exports cloud.opencode.base.sms.provider;
    exports cloud.opencode.base.sms.template;
    exports cloud.opencode.base.sms.util;
    exports cloud.opencode.base.sms.validation;

    // SPI: Allow ServiceLoader to find SmsProvider implementations
    uses cloud.opencode.base.sms.provider.SmsProvider;
}
