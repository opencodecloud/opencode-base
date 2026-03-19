package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.config.HttpSmsConfig;
import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * HTTP SMS Provider
 * HTTP短信提供商
 *
 * <p>Generic HTTP-based SMS provider.</p>
 * <p>通用的基于HTTP的短信提供商。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Generic HTTP API integration - 通用HTTP API集成</li>
 *   <li>Configurable authentication headers - 可配置认证头</li>
 *   <li>Parallel batch sending - 并行批量发送</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HttpSmsConfig config = HttpSmsConfig.builder()
 *     .apiUrl("https://sms-api.example.com/send")
 *     .appId("id").appKey("key").build();
 * HttpSmsProvider provider = new HttpSmsProvider(config);
 * SmsResult result = provider.send(SmsMessage.of("13800138000", "Hello"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (shared HttpClient, immutable config) - 线程安全: 是（共享HttpClient，不可变配置）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public class HttpSmsProvider implements SmsProvider {

    private static final HttpClient SHARED_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private final HttpSmsConfig config;

    /**
     * Create HTTP SMS provider
     * 创建HTTP短信提供商
     *
     * @param config the config | 配置
     */
    public HttpSmsProvider(HttpSmsConfig config) {
        this.config = config;
    }

    @Override
    public SmsResult send(SmsMessage message) {
        try {
            String requestBody = buildRequestBody(message);
            String[] headers = buildAuthHeaders();

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(config.apiUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30));

            for (int i = 0; i < headers.length - 1; i += 2) {
                requestBuilder.header(
                    sanitizeHeaderValue(headers[i]),
                    sanitizeHeaderValue(headers[i + 1])
                );
            }

            HttpResponse<String> response = SHARED_CLIENT.send(
                requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString()
            );

            return parseResponse(response.body(), response.statusCode());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return SmsResult.failure("INTERRUPTED", "Request interrupted");
        } catch (Exception e) {
            return SmsResult.failure("SEND_ERROR", e.getMessage());
        }
    }

    @Override
    public List<SmsResult> sendBatch(List<SmsMessage> messages) {
        return messages.parallelStream()
            .map(this::send)
            .toList();
    }

    /**
     * Build request body
     * 构建请求体
     *
     * @param message the message | 消息
     * @return the request body | 请求体
     */
    protected String buildRequestBody(SmsMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"phone\":\"").append(escapeJson(message.phoneNumber())).append("\",");
        if (message.templateId() != null) {
            sb.append("\"templateId\":\"").append(escapeJson(message.templateId())).append("\",");
        }
        if (message.content() != null) {
            sb.append("\"content\":\"").append(escapeJson(message.content())).append("\",");
        }
        if (config.signName() != null) {
            sb.append("\"signName\":\"").append(escapeJson(config.signName())).append("\",");
        }
        sb.append("\"params\":").append(toJsonObject(message.variables()));
        sb.append("}");
        return sb.toString();
    }

    /**
     * Build auth headers
     * 构建认证头
     *
     * @return the headers | 头部
     */
    protected String[] buildAuthHeaders() {
        if (config.appId() != null && config.appKey() != null) {
            return new String[]{
                "X-App-Id", config.appId(),
                "X-App-Key", config.appKey()
            };
        }
        return new String[0];
    }

    /**
     * Parse response
     * 解析响应
     *
     * @param body the response body | 响应体
     * @param statusCode the status code | 状态码
     * @return the result | 结果
     */
    protected SmsResult parseResponse(String body, int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            // Try to extract message ID from response
            String messageId = extractField(body, "messageId");
            if (messageId == null) {
                messageId = extractField(body, "msgId");
            }
            if (messageId == null) {
                messageId = "unknown";
            }
            return SmsResult.success(messageId);
        } else {
            String code = extractField(body, "code");
            String message = extractField(body, "message");
            return SmsResult.failure(
                code != null ? code : "HTTP_" + statusCode,
                message != null ? message : body
            );
        }
    }

    @Override
    public String getName() {
        return config.name();
    }

    @Override
    public boolean isAvailable() {
        return config.isConfigured();
    }

    /**
     * Get config
     * 获取配置
     *
     * @return the config | 配置
     */
    protected HttpSmsConfig getConfig() {
        return config;
    }

    /**
     * Get shared HTTP client
     * 获取共享HTTP客户端
     *
     * @return the client | 客户端
     */
    protected HttpClient getHttpClient() {
        return SHARED_CLIENT;
    }

    /**
     * Convert map to JSON object
     * 将映射转换为JSON对象
     */
    private String toJsonObject(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append("\"").append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Sanitize header value to prevent HTTP header injection.
     * Strips CR and LF characters from header values.
     * 清理头部值以防止HTTP头部注入。从头部值中去除CR和LF字符。
     *
     * @param value the header value | 头部值
     * @return sanitized value | 清理后的值
     */
    private String sanitizeHeaderValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r", "").replace("\n", "");
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
     * Extract field from JSON
     * 从JSON中提取字段
     */
    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\"";
        int index = json.indexOf(pattern);
        if (index < 0) return null;

        int colonIndex = json.indexOf(':', index);
        if (colonIndex < 0) return null;

        int start = json.indexOf('"', colonIndex);
        if (start < 0) return null;

        int end = json.indexOf('"', start + 1);
        if (end < 0) return null;

        return json.substring(start + 1, end);
    }
}
