package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Tencent Cloud SMS Sender
 * 腾讯云短信发送器
 *
 * <p>SMS sender implementation for Tencent Cloud using TC3-HMAC-SHA256 signature.</p>
 * <p>使用TC3-HMAC-SHA256签名的腾讯云短信发送器实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single SMS sending - 单条短信发送</li>
 *   <li>Batch SMS sending - 批量短信发送</li>
 *   <li>Template-based messaging - 基于模板的消息</li>
 *   <li>TC3-HMAC-SHA256 authentication - TC3-HMAC-SHA256认证</li>
 * </ul>
 *
 * <p>Example usage | 使用示例:</p>
 * <pre>{@code
 * var config = new TencentSmsConfig(
 *     "your-secret-id",
 *     "your-secret-key",
 *     "1400000000",
 *     "YourSign",
 *     "ap-guangzhou"
 * );
 * var sender = TencentSmsSender.create(config);
 * var message = SmsMessage.ofTemplate("+8613800138000", "123456", Map.of("code", "1234"));
 * var result = sender.send(message);
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
 * @see TencentSmsConfig
 */
public class TencentSmsSender implements SmsProvider {

    private static final String SERVICE = "sms";
    private static final String HOST = "sms.tencentcloudapi.com";
    private static final String ENDPOINT = "https://" + HOST;
    private static final String ACTION = "SendSms";
    private static final String VERSION = "2021-01-11";
    private static final String ALGORITHM = "TC3-HMAC-SHA256";
    private static final String CONTENT_TYPE = "application/json; charset=utf-8";
    private static final String SIGNED_HEADERS = "content-type;host;x-tc-action";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final HttpClient SHARED_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .executor(Executors.newVirtualThreadPerTaskExecutor())
        .build();

    private final TencentSmsConfig config;

    /**
     * Tencent SMS Configuration
     * 腾讯云短信配置
     *
     * <p>Configuration record for Tencent Cloud SMS service.</p>
     * <p>腾讯云短信服务的配置记录。</p>
     *
     * @param secretId the secret ID from Tencent Cloud console | 腾讯云控制台的密钥ID
     * @param secretKey the secret key from Tencent Cloud console | 腾讯云控制台的密钥Key
     * @param appId the SMS application ID | 短信应用ID
     * @param signName the SMS signature name | 短信签名名称
     * @param region the service region (e.g., ap-guangzhou) | 服务地域（如：ap-guangzhou）
     * @author Leon Soo
     * @since JDK 25, opencode-base-sms V1.0.0
     */
    public record TencentSmsConfig(
        String secretId,
        String secretKey,
        String appId,
        String signName,
        String region
    ) {
        /**
         * Create configuration with validation
         * 创建带验证的配置
         */
        public TencentSmsConfig {
            if (secretId == null || secretId.isBlank()) {
                throw new IllegalArgumentException("secretId must not be blank | secretId不能为空");
            }
            if (secretKey == null || secretKey.isBlank()) {
                throw new IllegalArgumentException("secretKey must not be blank | secretKey不能为空");
            }
            if (appId == null || appId.isBlank()) {
                throw new IllegalArgumentException("appId must not be blank | appId不能为空");
            }
            if (signName == null || signName.isBlank()) {
                throw new IllegalArgumentException("signName must not be blank | signName不能为空");
            }
            if (region == null || region.isBlank()) {
                throw new IllegalArgumentException("region must not be blank | region不能为空");
            }
        }

        /**
         * Check if configuration is valid
         * 检查配置是否有效
         *
         * @return true if valid | 如果有效返回true
         */
        public boolean isConfigured() {
            return secretId != null && !secretId.isBlank()
                && secretKey != null && !secretKey.isBlank()
                && appId != null && !appId.isBlank();
        }
    }

    /**
     * Create Tencent SMS sender with configuration
     * 使用配置创建腾讯云短信发送器
     *
     * @param config the configuration | 配置
     */
    private TencentSmsSender(TencentSmsConfig config) {
        this.config = config;
    }

    /**
     * Create Tencent SMS sender
     * 创建腾讯云短信发送器
     *
     * <p>Factory method to create a new sender instance.</p>
     * <p>用于创建新发送器实例的工厂方法。</p>
     *
     * @param config the configuration | 配置
     * @return new sender instance | 新的发送器实例
     * @throws IllegalArgumentException if config is null or invalid | 如果配置为空或无效
     */
    public static TencentSmsSender create(TencentSmsConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null | 配置不能为空");
        }
        return new TencentSmsSender(config);
    }

    /**
     * Send SMS message
     * 发送短信消息
     *
     * <p>Sends a single SMS message using Tencent Cloud SMS API.</p>
     * <p>使用腾讯云短信API发送单条短信消息。</p>
     *
     * @param message the message to send | 要发送的消息
     * @return the send result | 发送结果
     */
    @Override
    public SmsResult send(SmsMessage message) {
        try {
            String requestBody = buildRequestBody(message);
            long timestamp = Instant.now().getEpochSecond();
            String date = Instant.ofEpochSecond(timestamp)
                .atZone(ZoneOffset.UTC)
                .format(DATE_FORMATTER);

            String authorization = buildAuthorization(requestBody, timestamp, date);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Content-Type", CONTENT_TYPE)
                .header("Host", HOST)
                .header("X-TC-Action", ACTION)
                .header("X-TC-Version", VERSION)
                .header("X-TC-Timestamp", String.valueOf(timestamp))
                .header("X-TC-Region", config.region())
                .header("Authorization", authorization)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(30))
                .build();

            HttpResponse<String> response = SHARED_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            return parseResponse(response.body(), message.phoneNumber());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return SmsResult.failure(message.phoneNumber(), "INTERRUPTED", "Request interrupted | 请求被中断");
        } catch (Exception e) {
            return SmsResult.failure(message.phoneNumber(), "SEND_ERROR", e.getMessage());
        }
    }

    /**
     * Send batch SMS messages
     * 批量发送短信消息
     *
     * <p>Sends multiple SMS messages in parallel using virtual threads.</p>
     * <p>使用虚拟线程并行发送多条短信消息。</p>
     *
     * @param messages the messages to send | 要发送的消息列表
     * @return list of send results | 发送结果列表
     */
    @Override
    public List<SmsResult> sendBatch(List<SmsMessage> messages) {
        return messages.parallelStream()
            .map(this::send)
            .toList();
    }

    /**
     * Get provider name
     * 获取提供商名称
     *
     * @return the provider name | 提供商名称
     */
    @Override
    public String getName() {
        return "TencentCloud";
    }

    /**
     * Check if provider is available
     * 检查提供商是否可用
     *
     * @return true if available | 如果可用返回true
     */
    @Override
    public boolean isAvailable() {
        return config.isConfigured();
    }

    /**
     * Get the configuration
     * 获取配置
     *
     * @return the configuration | 配置
     */
    public TencentSmsConfig getConfig() {
        return config;
    }

    /**
     * Build request body for SendSms action
     * 构建SendSms操作的请求体
     *
     * @param message the message | 消息
     * @return the request body JSON | 请求体JSON
     */
    private String buildRequestBody(SmsMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"SmsSdkAppId\":\"").append(escapeJson(config.appId())).append("\",");
        sb.append("\"SignName\":\"").append(escapeJson(config.signName())).append("\",");
        sb.append("\"PhoneNumberSet\":[\"").append(escapeJson(message.phoneNumber())).append("\"],");

        if (message.templateId() != null) {
            sb.append("\"TemplateId\":\"").append(escapeJson(message.templateId())).append("\",");
        }

        // Build template parameters array
        sb.append("\"TemplateParamSet\":[");
        if (message.variables() != null && !message.variables().isEmpty()) {
            boolean first = true;
            for (Map.Entry<String, String> entry : message.variables().entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJson(entry.getValue())).append("\"");
                first = false;
            }
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Build TC3-HMAC-SHA256 authorization header
     * 构建TC3-HMAC-SHA256授权头
     *
     * <p>Implements Tencent Cloud API v3 signature algorithm.</p>
     * <p>实现腾讯云API v3签名算法。</p>
     *
     * @param payload the request payload | 请求负载
     * @param timestamp the Unix timestamp | Unix时间戳
     * @param date the date string (yyyy-MM-dd) | 日期字符串
     * @return the authorization header value | 授权头值
     */
    private String buildAuthorization(String payload, long timestamp, String date) {
        try {
            // Step 1: Build canonical request
            String hashedPayload = sha256Hex(payload);
            String canonicalRequest = "POST\n"
                + "/\n"
                + "\n"
                + "content-type:" + CONTENT_TYPE + "\n"
                + "host:" + HOST + "\n"
                + "x-tc-action:" + ACTION.toLowerCase() + "\n"
                + "\n"
                + SIGNED_HEADERS + "\n"
                + hashedPayload;

            // Step 2: Build string to sign
            String credentialScope = date + "/" + SERVICE + "/tc3_request";
            String hashedCanonicalRequest = sha256Hex(canonicalRequest);
            String stringToSign = ALGORITHM + "\n"
                + timestamp + "\n"
                + credentialScope + "\n"
                + hashedCanonicalRequest;

            // Step 3: Calculate signature
            byte[] secretDate = hmacSha256(("TC3" + config.secretKey()).getBytes(StandardCharsets.UTF_8), date);
            byte[] secretService = hmacSha256(secretDate, SERVICE);
            byte[] secretSigning = hmacSha256(secretService, "tc3_request");
            String signature = bytesToHex(hmacSha256(secretSigning, stringToSign));

            // Step 4: Build authorization header
            return ALGORITHM
                + " Credential=" + config.secretId() + "/" + credentialScope
                + ", SignedHeaders=" + SIGNED_HEADERS
                + ", Signature=" + signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build authorization | 构建授权失败", e);
        }
    }

    /**
     * Parse Tencent Cloud SMS API response
     * 解析腾讯云短信API响应
     *
     * @param body the response body | 响应体
     * @param phoneNumber the phone number | 手机号码
     * @return the SMS result | 短信结果
     */
    private SmsResult parseResponse(String body, String phoneNumber) {
        // Check for error in response
        String errorCode = extractField(body, "Code");
        if (errorCode != null && !"Ok".equalsIgnoreCase(errorCode)) {
            String errorMessage = extractField(body, "Message");
            return SmsResult.failure(phoneNumber, errorCode, errorMessage != null ? errorMessage : "Unknown error");
        }

        // Check for SendStatusSet in response
        String serialNo = extractField(body, "SerialNo");
        if (serialNo != null) {
            // Check individual send status
            String sendCode = extractNestedField(body, "SendStatusSet", "Code");
            if (sendCode != null && !"Ok".equalsIgnoreCase(sendCode)) {
                String sendMessage = extractNestedField(body, "SendStatusSet", "Message");
                return SmsResult.failure(phoneNumber, sendCode, sendMessage != null ? sendMessage : "Send failed");
            }
            return SmsResult.success(serialNo, phoneNumber);
        }

        // Fallback: check if response contains error
        if (body.contains("\"Error\"")) {
            String code = extractNestedField(body, "Error", "Code");
            String message = extractNestedField(body, "Error", "Message");
            return SmsResult.failure(phoneNumber, code != null ? code : "UNKNOWN", message != null ? message : body);
        }

        return SmsResult.success("unknown", phoneNumber);
    }

    /**
     * Calculate SHA-256 hash and return as hex string
     * 计算SHA-256哈希并返回十六进制字符串
     *
     * @param data the data to hash | 要哈希的数据
     * @return the hex string | 十六进制字符串
     */
    private String sha256Hex(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 calculation failed | SHA-256计算失败", e);
        }
    }

    /**
     * Calculate HMAC-SHA256
     * 计算HMAC-SHA256
     *
     * @param key the key | 密钥
     * @param data the data | 数据
     * @return the HMAC result | HMAC结果
     */
    private byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 calculation failed | HMAC-SHA256计算失败", e);
        }
    }

    /**
     * Convert bytes to hex string
     * 将字节转换为十六进制字符串
     *
     * @param bytes the bytes | 字节
     * @return the hex string | 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * Escape JSON string
     * 转义JSON字符串
     *
     * @param s the string | 字符串
     * @return the escaped string | 转义后的字符串
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
     * Extract field from JSON
     * 从JSON中提取字段
     *
     * @param json the JSON string | JSON字符串
     * @param field the field name | 字段名
     * @return the field value or null | 字段值或null
     */
    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\"";
        int index = json.indexOf(pattern);
        if (index < 0) return null;

        int colonIndex = json.indexOf(':', index);
        if (colonIndex < 0) return null;

        // Skip whitespace
        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length()) return null;

        char startChar = json.charAt(valueStart);
        if (startChar == '"') {
            int end = json.indexOf('"', valueStart + 1);
            if (end < 0) return null;
            return json.substring(valueStart + 1, end);
        } else if (Character.isDigit(startChar) || startChar == '-') {
            int end = valueStart;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-' || json.charAt(end) == '.')) {
                end++;
            }
            return json.substring(valueStart, end);
        }

        return null;
    }

    /**
     * Extract nested field from JSON
     * 从JSON中提取嵌套字段
     *
     * @param json the JSON string | JSON字符串
     * @param parentField the parent field name | 父字段名
     * @param field the field name | 字段名
     * @return the field value or null | 字段值或null
     */
    private String extractNestedField(String json, String parentField, String field) {
        String parentPattern = "\"" + parentField + "\"";
        int parentIndex = json.indexOf(parentPattern);
        if (parentIndex < 0) return null;

        // Find the opening brace or bracket after parent field
        int braceIndex = json.indexOf('{', parentIndex);
        int bracketIndex = json.indexOf('[', parentIndex);

        int startIndex;
        if (braceIndex < 0 && bracketIndex < 0) return null;
        if (braceIndex < 0) startIndex = bracketIndex;
        else if (bracketIndex < 0) startIndex = braceIndex;
        else startIndex = Math.min(braceIndex, bracketIndex);

        // Extract from the nested object/array
        String nested = json.substring(startIndex);
        return extractField(nested, field);
    }
}
