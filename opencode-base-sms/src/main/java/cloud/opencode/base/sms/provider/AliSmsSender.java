/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Aliyun SMS Sender
 * 阿里云短信发送器
 *
 * <p>SMS provider implementation for Aliyun (Alibaba Cloud) SMS service.
 * Uses HMAC-SHA1 signature algorithm for API authentication.</p>
 * <p>阿里云短信服务的短信提供商实现。
 * 使用HMAC-SHA1签名算法进行API认证。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HMAC-SHA1 API authentication - HMAC-SHA1 API认证</li>
 *   <li>Template-based SMS sending - 基于模板的短信发送</li>
 *   <li>Batch SMS sending - 批量短信发送</li>
 *   <li>Virtual thread HTTP client - 虚拟线程HTTP客户端</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * var config = new AliSmsSender.AliSmsConfig(
 *     "your-access-key-id",
 *     "your-access-key-secret",
 *     "YourSignName",
 *     "cn-hangzhou"
 * );
 * var sender = AliSmsSender.create(config);
 * var result = sender.send(SmsMessage.ofTemplate(
 *     "13800138000",
 *     "SMS_123456789",
 *     Map.of("code", "123456")
 * ));
 * }</pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * var config = new AliSmsSender.AliSmsConfig(
 *     "your-access-key-id",
 *     "your-access-key-secret",
 *     "YourSignName",
 *     "cn-hangzhou"
 * );
 * var sender = AliSmsSender.create(config);
 * var result = sender.send(SmsMessage.ofTemplate(
 *     "13800138000",
 *     "SMS_123456789",
 *     Map.of("code", "123456")
 * ));
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
 */
public class AliSmsSender implements SmsProvider {

    private static final String NAME = "aliyun";
    private static final String API_VERSION = "2017-05-25";
    private static final String ACTION_SEND_SMS = "SendSms";
    private static final String SIGNATURE_METHOD = "HMAC-SHA1";
    private static final String SIGNATURE_VERSION = "1.0";
    private static final String FORMAT = "JSON";
    private static final String HMAC_SHA1 = "HmacSHA1";

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final HttpClient SHARED_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .executor(Executors.newVirtualThreadPerTaskExecutor())
        .build();

    private final AliSmsConfig config;

    /**
     * Aliyun SMS Configuration
     * 阿里云短信配置
     *
     * @param accessKeyId the AccessKey ID | AccessKey ID
     * @param accessKeySecret the AccessKey Secret | AccessKey Secret
     * @param signName the SMS signature name | 短信签名名称
     * @param regionId the region ID (e.g., cn-hangzhou) | 区域ID（例如：cn-hangzhou）
     */
    public record AliSmsConfig(
        String accessKeyId,
        String accessKeySecret,
        String signName,
        String regionId
    ) {
        /**
         * Compact constructor with validation
         * 带验证的紧凑构造函数
         */
        public AliSmsConfig {
            if (accessKeyId == null || accessKeyId.isBlank()) {
                throw new IllegalArgumentException("AccessKeyId cannot be null or blank");
            }
            if (accessKeySecret == null || accessKeySecret.isBlank()) {
                throw new IllegalArgumentException("AccessKeySecret cannot be null or blank");
            }
            if (signName == null || signName.isBlank()) {
                throw new IllegalArgumentException("SignName cannot be null or blank");
            }
            if (regionId == null || regionId.isBlank()) {
                regionId = "cn-hangzhou";
            }
        }

        /**
         * Create config with default region
         * 使用默认区域创建配置
         *
         * @param accessKeyId the AccessKey ID | AccessKey ID
         * @param accessKeySecret the AccessKey Secret | AccessKey Secret
         * @param signName the sign name | 签名名称
         * @return the config | 配置
         */
        public static AliSmsConfig of(String accessKeyId, String accessKeySecret, String signName) {
            return new AliSmsConfig(accessKeyId, accessKeySecret, signName, "cn-hangzhou");
        }

        /**
         * Check if config is valid
         * 检查配置是否有效
         *
         * @return true if configured | 如果已配置返回true
         */
        public boolean isConfigured() {
            return accessKeyId != null && !accessKeyId.isBlank()
                && accessKeySecret != null && !accessKeySecret.isBlank()
                && signName != null && !signName.isBlank();
        }
    }

    /**
     * Private constructor
     * 私有构造函数
     *
     * @param config the configuration | 配置
     */
    private AliSmsSender(AliSmsConfig config) {
        this.config = config;
    }

    /**
     * Create AliSmsSender instance
     * 创建阿里云短信发送器实例
     *
     * @param config the configuration | 配置
     * @return the sender instance | 发送器实例
     */
    public static AliSmsSender create(AliSmsConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        return new AliSmsSender(config);
    }

    @Override
    public SmsResult send(SmsMessage message) {
        if (message == null) {
            return SmsResult.failure("INVALID_PARAM", "Message cannot be null");
        }
        if (message.phoneNumber() == null || message.phoneNumber().isBlank()) {
            return SmsResult.failure("INVALID_PARAM", "Phone number cannot be null or blank");
        }
        if (message.templateId() == null || message.templateId().isBlank()) {
            return SmsResult.failure("INVALID_PARAM", "Template ID is required for Aliyun SMS");
        }

        try {
            String url = buildSignedUrl(message);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();

            HttpResponse<String> response = SHARED_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            return parseResponse(response.body(), message.phoneNumber());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return SmsResult.failure(message.phoneNumber(), "INTERRUPTED", "Request interrupted");
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

    /**
     * Build signed URL for Aliyun SMS API
     * 构建阿里云短信API的签名URL
     *
     * @param message the SMS message | 短信消息
     * @return the signed URL | 签名URL
     */
    private String buildSignedUrl(SmsMessage message) {
        String timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(TIMESTAMP_FORMATTER);
        String nonce = UUID.randomUUID().toString();

        // Build sorted parameters
        Map<String, String> params = new TreeMap<>();

        // System parameters
        params.put("AccessKeyId", config.accessKeyId());
        params.put("Action", ACTION_SEND_SMS);
        params.put("Format", FORMAT);
        params.put("RegionId", config.regionId());
        params.put("SignatureMethod", SIGNATURE_METHOD);
        params.put("SignatureNonce", nonce);
        params.put("SignatureVersion", SIGNATURE_VERSION);
        params.put("Timestamp", timestamp);
        params.put("Version", API_VERSION);

        // Business parameters
        params.put("PhoneNumbers", message.phoneNumber());
        params.put("SignName", config.signName());
        params.put("TemplateCode", message.templateId());

        // Template parameters as JSON
        if (message.variables() != null && !message.variables().isEmpty()) {
            params.put("TemplateParam", toJson(message.variables()));
        }

        // Build canonical query string
        String canonicalQueryString = buildCanonicalQueryString(params);

        // Build string to sign
        String stringToSign = "GET&" + percentEncode("/") + "&" + percentEncode(canonicalQueryString);

        // Calculate signature
        String signature = calculateSignature(stringToSign);
        params.put("Signature", signature);

        // Build final URL
        return buildRequestUrl(params);
    }

    /**
     * Build canonical query string
     * 构建规范查询字符串
     *
     * @param params the parameters | 参数
     * @return the canonical query string | 规范查询字符串
     */
    private String buildCanonicalQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            sb.append(percentEncode(entry.getKey()))
                .append("=")
                .append(percentEncode(entry.getValue()));
            first = false;
        }
        return sb.toString();
    }

    /**
     * Calculate HMAC-SHA1 signature
     * 计算HMAC-SHA1签名
     *
     * @param stringToSign the string to sign | 待签名字符串
     * @return the signature | 签名
     */
    private String calculateSignature(String stringToSign) {
        try {
            String key = config.accessKeySecret() + "&";
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA1));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signData);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to calculate signature", e);
        }
    }

    /**
     * Build request URL
     * 构建请求URL
     *
     * @param params the parameters | 参数
     * @return the URL | URL
     */
    private String buildRequestUrl(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://dysmsapi.aliyuncs.com/?");
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            sb.append(urlEncode(entry.getKey()))
                .append("=")
                .append(urlEncode(entry.getValue()));
            first = false;
        }
        return sb.toString();
    }

    /**
     * Parse API response
     * 解析API响应
     *
     * @param responseBody the response body | 响应体
     * @param phoneNumber the phone number | 手机号码
     * @return the SMS result | 短信结果
     */
    private SmsResult parseResponse(String responseBody, String phoneNumber) {
        String code = extractJsonField(responseBody, "Code");
        String message = extractJsonField(responseBody, "Message");
        String bizId = extractJsonField(responseBody, "BizId");
        String requestId = extractJsonField(responseBody, "RequestId");

        if ("OK".equals(code)) {
            String messageId = bizId != null ? bizId : requestId;
            return SmsResult.success(messageId, phoneNumber);
        } else {
            return SmsResult.failure(
                phoneNumber,
                code != null ? code : "UNKNOWN",
                message != null ? message : "Unknown error"
            );
        }
    }

    /**
     * Extract field from JSON response (simple implementation)
     * 从JSON响应中提取字段（简单实现）
     *
     * @param json the JSON string | JSON字符串
     * @param field the field name | 字段名
     * @return the field value | 字段值
     */
    private String extractJsonField(String json, String field) {
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

        // Check if value is quoted
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            if (end < 0) return null;
            return json.substring(start + 1, end);
        } else {
            // Unquoted value (number, boolean, null)
            int end = start;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                    break;
                }
                end++;
            }
            String value = json.substring(start, end);
            return "null".equals(value) ? null : value;
        }
    }

    /**
     * Convert map to JSON string
     * 将Map转换为JSON字符串
     *
     * @param map the map | 映射
     * @return the JSON string | JSON字符串
     */
    private String toJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append("\"").append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Escape JSON special characters
     * 转义JSON特殊字符
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
     * Percent encode for Aliyun signature (RFC 3986)
     * 阿里云签名的百分号编码（RFC 3986）
     *
     * @param value the value to encode | 要编码的值
     * @return the encoded value | 编码后的值
     */
    private String percentEncode(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~");
    }

    /**
     * URL encode
     * URL编码
     *
     * @param value the value to encode | 要编码的值
     * @return the encoded value | 编码后的值
     */
    private String urlEncode(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isAvailable() {
        return config != null && config.isConfigured();
    }

    /**
     * Get configuration
     * 获取配置
     *
     * @return the config | 配置
     */
    public AliSmsConfig getConfig() {
        return config;
    }

    /**
     * Get shared HTTP client
     * 获取共享HTTP客户端
     *
     * @return the HTTP client | HTTP客户端
     */
    protected HttpClient getHttpClient() {
        return SHARED_CLIENT;
    }
}
