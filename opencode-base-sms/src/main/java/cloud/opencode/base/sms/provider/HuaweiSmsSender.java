package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.exception.SmsSendException;
import cloud.opencode.base.sms.exception.SmsErrorCode;
import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * Huawei Cloud SMS Sender
 * 华为云短信发送器
 *
 * <p>SMS sender implementation for Huawei Cloud using WSSE authentication.
 * Supports batch SMS sending via Huawei Cloud SMS API.</p>
 *
 * <p>使用WSSE认证的华为云短信发送器实现。
 * 支持通过华为云短信API批量发送短信。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>WSSE authentication - WSSE认证</li>
 *   <li>Single and batch SMS sending - 单条和批量短信发送</li>
 *   <li>Template-based messaging - 基于模板的消息</li>
 *   <li>Virtual thread HTTP client - 虚拟线程HTTP客户端</li>
 * </ul>
 *
 * <h2>Usage Example | 使用示例</h2>
 * <pre>{@code
 * var config = new HuaweiSmsConfig(
 *     "your-app-key",
 *     "your-app-secret",
 *     "sender-channel",
 *     "channel-id",
 *     "cn-north-4"
 * );
 * var sender = HuaweiSmsSender.create(config);
 * var result = sender.send(SmsMessage.of("+8613800138000", "Your code is 123456"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable config, shared HttpClient) - 线程安全: 是（不可变配置，共享HttpClient）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 * @see SmsProvider
 * @see HuaweiSmsConfig
 */
public class HuaweiSmsSender implements SmsProvider {

    private static final String DEFAULT_REGION = "cn-north-4";
    private static final String API_PATH = "/sms/batchSendSms/v1";
    private static final DateTimeFormatter WSSE_DATE_FORMAT = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .withZone(ZoneOffset.UTC);

    private static final HttpClient SHARED_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .executor(Executors.newVirtualThreadPerTaskExecutor())
        .build();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final HuaweiSmsConfig config;
    private final String endpoint;

    /**
     * Huawei SMS Configuration
     * 华为短信配置
     *
     * <p>Configuration record for Huawei Cloud SMS service.</p>
     * <p>华为云短信服务配置记录。</p>
     *
     * @param appKey the application key | 应用Key
     * @param appSecret the application secret | 应用密钥
     * @param sender the sender channel (signature channel number) | 发送通道（签名通道号）
     * @param channelId the channel ID (template ID) | 通道ID（模板ID）
     * @param region the region code (e.g., cn-north-4) | 区域代码（如cn-north-4）
     */
    public record HuaweiSmsConfig(
        String appKey,
        String appSecret,
        String sender,
        String channelId,
        String region
    ) {
        /**
         * Create config with validation
         * 创建带验证的配置
         */
        public HuaweiSmsConfig {
            Objects.requireNonNull(appKey, "appKey cannot be null | appKey不能为空");
            Objects.requireNonNull(appSecret, "appSecret cannot be null | appSecret不能为空");
            Objects.requireNonNull(sender, "sender cannot be null | sender不能为空");
            if (region == null || region.isBlank()) {
                region = DEFAULT_REGION;
            }
        }

        /**
         * Create config with default region
         * 使用默认区域创建配置
         *
         * @param appKey the application key | 应用Key
         * @param appSecret the application secret | 应用密钥
         * @param sender the sender channel | 发送通道
         * @param channelId the channel ID | 通道ID
         * @return the config | 配置
         */
        public static HuaweiSmsConfig of(String appKey, String appSecret, String sender, String channelId) {
            return new HuaweiSmsConfig(appKey, appSecret, sender, channelId, DEFAULT_REGION);
        }

        /**
         * Check if config is properly configured
         * 检查配置是否正确配置
         *
         * @return true if configured | 如果配置正确返回true
         */
        public boolean isConfigured() {
            return appKey != null && !appKey.isBlank()
                && appSecret != null && !appSecret.isBlank()
                && sender != null && !sender.isBlank();
        }
    }

    /**
     * Create Huawei SMS sender
     * 创建华为短信发送器
     *
     * @param config the configuration | 配置
     */
    private HuaweiSmsSender(HuaweiSmsConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null | 配置不能为空");
        this.endpoint = buildEndpoint(config.region());
    }

    /**
     * Create Huawei SMS sender with config
     * 使用配置创建华为短信发送器
     *
     * @param config the configuration | 配置
     * @return the sender instance | 发送器实例
     */
    public static HuaweiSmsSender create(HuaweiSmsConfig config) {
        return new HuaweiSmsSender(config);
    }

    @Override
    public SmsResult send(SmsMessage message) {
        validateMessage(message);

        try {
            String requestBody = buildRequestBody(message);
            String wsseHeader = buildWsseHeader();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "WSSE realm=\"SDP\",profile=\"UsernameToken\",type=\"Appkey\"")
                .header("X-WSSE", wsseHeader)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();

            HttpResponse<String> response = SHARED_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            return parseResponse(response.body(), response.statusCode(), message.phoneNumber());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return SmsResult.failure(message.phoneNumber(), "INTERRUPTED", "Request interrupted | 请求被中断");
        } catch (SmsSendException e) {
            throw e;
        } catch (Exception e) {
            return SmsResult.failure(message.phoneNumber(), "SEND_ERROR", e.getMessage());
        }
    }

    @Override
    public List<SmsResult> sendBatch(List<SmsMessage> messages) {
        return messages.parallelStream()
            .map(this::send)
            .toList();
    }

    @Override
    public String getName() {
        return "HUAWEI";
    }

    @Override
    public boolean isAvailable() {
        return config.isConfigured();
    }

    /**
     * Build WSSE authentication header
     * 构建WSSE认证头
     *
     * <p>WSSE header format:</p>
     * <pre>
     * UsernameToken Username="appKey",PasswordDigest="Base64(SHA256(Nonce+Created+appSecret))",
     * Nonce="random",Created="timestamp"
     * </pre>
     *
     * @return the X-WSSE header value | X-WSSE头值
     */
    private String buildWsseHeader() {
        String nonce = generateNonce();
        String created = WSSE_DATE_FORMAT.format(Instant.now());
        String passwordDigest = calculatePasswordDigest(nonce, created);

        return String.format(
            "UsernameToken Username=\"%s\",PasswordDigest=\"%s\",Nonce=\"%s\",Created=\"%s\"",
            config.appKey(),
            passwordDigest,
            nonce,
            created
        );
    }

    /**
     * Calculate WSSE password digest
     * 计算WSSE密码摘要
     *
     * @param nonce the nonce value | 随机数
     * @param created the creation timestamp | 创建时间戳
     * @return Base64 encoded SHA-256 digest | Base64编码的SHA-256摘要
     */
    private String calculatePasswordDigest(String nonce, String created) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = nonce + created + config.appSecret();
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available | SHA-256算法不可用", e);
        }
    }

    /**
     * Generate random nonce for WSSE
     * 为WSSE生成随机数
     *
     * @return hex-encoded random nonce | 十六进制编码的随机数
     */
    private String generateNonce() {
        byte[] nonceBytes = new byte[16];
        SECURE_RANDOM.nextBytes(nonceBytes);
        return HexFormat.of().formatHex(nonceBytes);
    }

    /**
     * Build request body for Huawei SMS API
     * 构建华为短信API请求体
     *
     * @param message the SMS message | 短信消息
     * @return URL-encoded request body | URL编码的请求体
     */
    private String buildRequestBody(SmsMessage message) {
        StringBuilder body = new StringBuilder();

        // Sender channel
        body.append("from=").append(urlEncode(config.sender()));

        // Receiver phone number(s)
        body.append("&to=").append(urlEncode(message.phoneNumber()));

        // Template ID if specified
        if (config.channelId() != null && !config.channelId().isBlank()) {
            body.append("&templateId=").append(urlEncode(config.channelId()));
        } else if (message.templateId() != null && !message.templateId().isBlank()) {
            body.append("&templateId=").append(urlEncode(message.templateId()));
        }

        // Template parameters
        if (!message.variables().isEmpty()) {
            String templateParas = buildTemplateParams(message.variables());
            body.append("&templateParas=").append(urlEncode(templateParas));
        } else if (message.content() != null && !message.content().isBlank()) {
            // For direct content, wrap as single parameter
            body.append("&templateParas=").append(urlEncode("[\"" + escapeJson(message.content()) + "\"]"));
        }

        return body.toString();
    }

    /**
     * Build template parameters JSON array
     * 构建模板参数JSON数组
     *
     * @param variables the template variables | 模板变量
     * @return JSON array string | JSON数组字符串
     */
    private String buildTemplateParams(Map<String, String> variables) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String value : variables.values()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(value)).append("\"");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Parse Huawei SMS API response
     * 解析华为短信API响应
     *
     * @param body the response body | 响应体
     * @param statusCode the HTTP status code | HTTP状态码
     * @param phoneNumber the phone number | 手机号码
     * @return the SMS result | 短信结果
     */
    private SmsResult parseResponse(String body, int statusCode, String phoneNumber) {
        if (statusCode >= 200 && statusCode < 300) {
            String resultCode = extractField(body, "code");
            if ("000000".equals(resultCode)) {
                String smsMsgId = extractField(body, "smsMsgId");
                return SmsResult.success(smsMsgId != null ? smsMsgId : "unknown", phoneNumber);
            } else {
                String description = extractField(body, "description");
                return SmsResult.failure(phoneNumber, resultCode, description);
            }
        } else {
            String code = extractField(body, "code");
            String message = extractField(body, "description");
            return SmsResult.failure(
                phoneNumber,
                code != null ? code : "HTTP_" + statusCode,
                message != null ? message : body
            );
        }
    }

    /**
     * Validate SMS message before sending
     * 发送前验证短信消息
     *
     * @param message the message to validate | 要验证的消息
     */
    private void validateMessage(SmsMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null | 消息不能为空");
        }
        if (message.phoneNumber() == null || message.phoneNumber().isBlank()) {
            throw new SmsSendException(SmsErrorCode.INVALID_PHONE_NUMBER, null);
        }
        if ((message.content() == null || message.content().isBlank())
            && (message.templateId() == null || message.templateId().isBlank())
            && (config.channelId() == null || config.channelId().isBlank())) {
            throw new SmsSendException(SmsErrorCode.MESSAGE_EMPTY, message.phoneNumber());
        }
    }

    /**
     * Build endpoint URL for region
     * 为区域构建端点URL
     *
     * @param region the region code | 区域代码
     * @return the endpoint URL | 端点URL
     */
    private String buildEndpoint(String region) {
        return "https://smsapi." + region + ".myhuaweicloud.com:443" + API_PATH;
    }

    /**
     * URL encode string
     * URL编码字符串
     */
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Escape JSON string
     * 转义JSON字符串
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    /**
     * Extract field from JSON response
     * 从JSON响应中提取字段
     */
    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\"";
        int index = json.indexOf(pattern);
        if (index < 0) return null;

        int colonIndex = json.indexOf(':', index);
        if (colonIndex < 0) return null;

        // Skip whitespace
        int start = colonIndex + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start >= json.length()) return null;

        // Check if value is a string
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            if (end < 0) return null;
            return json.substring(start + 1, end);
        }

        // Non-string value (number, boolean, null)
        int end = start;
        while (end < json.length() && !",}]".contains(String.valueOf(json.charAt(end)))) {
            end++;
        }
        return json.substring(start, end).trim();
    }

    /**
     * Get the configuration
     * 获取配置
     *
     * @return the config | 配置
     */
    public HuaweiSmsConfig getConfig() {
        return config;
    }

    /**
     * Get the endpoint URL
     * 获取端点URL
     *
     * @return the endpoint | 端点
     */
    public String getEndpoint() {
        return endpoint;
    }
}
