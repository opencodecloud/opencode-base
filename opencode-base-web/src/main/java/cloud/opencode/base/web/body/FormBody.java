package cloud.opencode.base.web.body;

import cloud.opencode.base.web.http.ContentType;

import java.net.URLEncoder;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Form Body - URL-Encoded Form Request Body
 * 表单请求体 - URL 编码的表单请求体
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>URL-encoded form parameter encoding - URL 编码的表单参数编码</li>
 *   <li>Builder pattern for fluent construction - 构建器模式支持流式构建</li>
 *   <li>Multi-value parameter support - 多值参数支持</li>
 *   <li>Immutable once built - 构建后不可变</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // From key-value pairs
 * FormBody body = FormBody.of("username", "john", "password", "secret");
 *
 * // Using builder
 * FormBody body = FormBody.builder()
 *     .add("username", "john")
 *     .addIfNotNull("email", email)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (values must not be null) - 空值安全: 否（值不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class FormBody implements RequestBody {

    private static final String FORM_CONTENT_TYPE =
            ContentType.APPLICATION_FORM_URLENCODED + "; charset=utf-8";

    private final List<Map.Entry<String, String>> entries;
    private final String encoded;
    private final byte[] bytes;

    private FormBody(List<Map.Entry<String, String>> entries) {
        this.entries = Collections.unmodifiableList(entries);
        this.encoded = encode(entries);
        this.bytes = encoded.getBytes(StandardCharsets.UTF_8);
    }

    public static FormBody empty() {
        return new FormBody(Collections.emptyList());
    }

    public static FormBody of(Map<String, String> map) {
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        map.forEach((k, v) -> entries.add(Map.entry(k, v)));
        return new FormBody(entries);
    }

    public static FormBody of(String... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Form pairs must be even-length (key-value pairs)");
        }
        Builder builder = builder();
        for (int i = 0; i < pairs.length; i += 2) {
            builder.add(pairs[i], pairs[i + 1]);
        }
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getContentType() { return FORM_CONTENT_TYPE; }

    @Override
    public BodyPublisher getBodyPublisher() { return BodyPublishers.ofByteArray(bytes); }

    @Override
    public long getContentLength() { return bytes.length; }

    public int size() { return entries.size(); }
    public boolean isEmpty() { return entries.isEmpty(); }
    public List<Map.Entry<String, String>> getEntries() { return entries; }
    public String getEncoded() { return encoded; }

    public String get(String name) {
        for (Map.Entry<String, String> entry : entries) {
            if (entry.getKey().equals(name)) return entry.getValue();
        }
        return null;
    }

    public List<String> getAll(String name) {
        List<String> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : entries) {
            if (entry.getKey().equals(name)) values.add(entry.getValue());
        }
        return values;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private static String encode(List<Map.Entry<String, String>> entries) {
        if (entries.isEmpty()) return "";
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : entries) {
            String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            joiner.add(key + "=" + value);
        }
        return joiner.toString();
    }

    @Override
    public String toString() {
        return "FormBody{entries=" + entries.size() + "}";
    }

    public static final class Builder {
        private final List<Map.Entry<String, String>> entries = new ArrayList<>();
        private Builder() {}

        public Builder add(String name, String value) {
            entries.add(Map.entry(name, value != null ? value : ""));
            return this;
        }

        public Builder addIfNotNull(String name, String value) {
            if (value != null) entries.add(Map.entry(name, value));
            return this;
        }

        public Builder addAll(Map<String, String> map) {
            map.forEach(this::add);
            return this;
        }

        public FormBody build() {
            return new FormBody(new ArrayList<>(entries));
        }
    }
}
