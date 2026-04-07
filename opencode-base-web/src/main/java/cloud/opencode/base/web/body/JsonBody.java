package cloud.opencode.base.web.body;

import cloud.opencode.base.web.http.ContentType;

import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * JSON Body - JSON Request Body
 * JSON 请求体 - JSON 请求体
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JSON string request body - JSON 字符串请求体</li>
 *   <li>Map to JSON conversion - Map 到 JSON 转换</li>
 *   <li>Pluggable serializer support - 可插拔序列化器支持</li>
 *   <li>Automatic UTF-8 encoding - 自动 UTF-8 编码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // From JSON string
 * JsonBody body = JsonBody.of("{\"name\":\"John\"}");
 *
 * // From Map
 * JsonBody body = JsonBody.of(Map.of("name", "John", "age", 30));
 *
 * // From object with custom serializer
 * JsonBody.setSerializer(obj -> mySerializer.toJson(obj));
 * JsonBody body = JsonBody.of(myObject);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (json content must not be null) - 空值安全: 否（JSON 内容不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class JsonBody implements RequestBody {

    private static final String JSON_CONTENT_TYPE = ContentType.APPLICATION_JSON + "; charset=utf-8";
    private static volatile Function<Object, String> defaultSerializer;

    private final String json;
    private final byte[] bytes;

    private JsonBody(String json) {
        this.json = json;
        this.bytes = json.getBytes(StandardCharsets.UTF_8);
    }

    public static JsonBody of(String json) {
        return new JsonBody(json);
    }

    public static JsonBody of(Map<String, ?> map) {
        return new JsonBody(mapToJson(map));
    }

    public static JsonBody of(Object object) {
        if (object instanceof String str) return of(str);
        if (object instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> stringMap = (Map<String, ?>) map;
            return of(stringMap);
        }
        if (defaultSerializer == null) {
            throw new IllegalStateException("JSON serializer not configured. Call JsonBody.setSerializer() first.");
        }
        return new JsonBody(defaultSerializer.apply(object));
    }

    public static <T> JsonBody of(T object, Function<T, String> serializer) {
        return new JsonBody(serializer.apply(object));
    }

    public static void setSerializer(Function<Object, String> serializer) {
        defaultSerializer = serializer;
    }

    public static Function<Object, String> getSerializer() {
        return defaultSerializer;
    }

    @Override
    public String getContentType() { return JSON_CONTENT_TYPE; }

    @Override
    public BodyPublisher getBodyPublisher() { return BodyPublishers.ofByteArray(bytes); }

    @Override
    public long getContentLength() { return bytes.length; }

    public String getJson() { return json; }

    private static final int MAX_DEPTH = 32;

    private static String mapToJson(Map<String, ?> map) {
        return mapToJson(map, 0);
    }

    private static String mapToJson(Map<String, ?> map, int depth) {
        if (depth > MAX_DEPTH) {
            throw new IllegalArgumentException("JSON nesting depth exceeds maximum: " + MAX_DEPTH);
        }
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String key = escapeJson(entry.getKey());
            String valueStr = valueToJson(entry.getValue(), depth);
            joiner.add("\"" + key + "\":" + valueStr);
        }
        return joiner.toString();
    }

    private static String valueToJson(Object value) {
        return valueToJson(value, 0);
    }

    private static String valueToJson(Object value, int depth) {
        if (depth > MAX_DEPTH) {
            throw new IllegalArgumentException("JSON nesting depth exceeds maximum: " + MAX_DEPTH);
        }
        if (value == null) return "null";
        if (value instanceof String str) return "\"" + escapeJson(str) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> stringMap = (Map<String, ?>) map;
            return mapToJson(stringMap, depth + 1);
        }
        if (value instanceof Iterable<?> iterable) {
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            for (Object item : iterable) joiner.add(valueToJson(item, depth + 1));
            return joiner.toString();
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private static String escapeJson(String str) {
        StringBuilder sb = new StringBuilder(str.length() + 16);
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append("\\u00");
                        sb.append(Character.forDigit((c >> 4) & 0xF, 16));
                        sb.append(Character.forDigit(c & 0xF, 16));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "JsonBody{length=" + bytes.length + "}";
    }
}
