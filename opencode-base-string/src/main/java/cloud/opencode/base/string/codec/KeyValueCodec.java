package cloud.opencode.base.string.codec;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Codec for encoding/decoding {@code Map<String,String>} to/from key-value string format.
 * 用于将 {@code Map<String,String>} 编码/解码为键值字符串格式的编解码器。
 *
 * <p>Default format: {@code key1=value1;key2=value2}. Separators are configurable.</p>
 * <p>默认格式：{@code key1=value1;key2=value2}。分隔符可配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Encode Map to key=value;key=value format - 将Map编码为键值字符串格式</li>
 *   <li>Decode key=value string back to Map - 将键值字符串解码为Map</li>
 *   <li>Configurable entry and key-value separators - 可配置条目和键值分隔符</li>
 *   <li>Preserves insertion order - 保持插入顺序</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Map<String, String> map = Map.of("host", "localhost", "port", "8080");
 * String encoded = KeyValueCodec.encode(map);
 * // "host=localhost;port=8080"
 *
 * Map<String, String> decoded = KeyValueCodec.decode(encoded);
 * // {host=localhost, port=8080}
 *
 * // Custom separators / 自定义分隔符
 * String custom = KeyValueCodec.encode(map, "&", ":");
 * // "host:localhost&port:8080"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns null/empty for null input) - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class KeyValueCodec {

    private static final String DEFAULT_ENTRY_SEPARATOR = ";";
    private static final String DEFAULT_KEY_VALUE_SEPARATOR = "=";

    private KeyValueCodec() {}

    /**
     * Encode a map to the default {@code key=value;key=value} format.
     * 将映射编码为默认的 {@code key=value;key=value} 格式。
     *
     * @param map the map to encode / 要编码的映射
     * @return the encoded string, or {@code null} if the map is null or empty / 编码后的字符串，如果映射为 null 或空则返回 null
     */
    public static String encode(Map<String, String> map) {
        return encode(map, DEFAULT_ENTRY_SEPARATOR, DEFAULT_KEY_VALUE_SEPARATOR);
    }

    /**
     * Encode a map with configurable separators.
     * 使用可配置的分隔符将映射编码。
     *
     * @param map                the map to encode / 要编码的映射
     * @param entrySeparator     separator between entries (e.g. {@code ";"}) / 条目之间的分隔符
     * @param keyValueSeparator  separator between key and value (e.g. {@code "="}) / 键值之间的分隔符
     * @return the encoded string, or {@code null} if the map is null or empty / 编码后的字符串，如果映射为 null 或空则返回 null
     * @throws NullPointerException if entrySeparator or keyValueSeparator is null / 如果分隔符为 null
     */
    public static String encode(Map<String, String> map, String entrySeparator, String keyValueSeparator) {
        Objects.requireNonNull(entrySeparator, "entrySeparator must not be null / entrySeparator 不能为空");
        Objects.requireNonNull(keyValueSeparator, "keyValueSeparator must not be null / keyValueSeparator 不能为空");
        if (map == null || map.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append(entrySeparator);
            }
            sb.append(entry.getKey()).append(keyValueSeparator).append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * Decode a string in the default {@code key=value;key=value} format to a map.
     * 将默认 {@code key=value;key=value} 格式的字符串解码为映射。
     *
     * <p>Returns an unmodifiable map preserving insertion order.
     * Returns an empty map if the input is null or blank.</p>
     *
     * <p>返回保持插入顺序的不可修改映射。如果输入为 null 或空白则返回空映射。</p>
     *
     * @param encoded the encoded string / 编码后的字符串
     * @return decoded map (unmodifiable) / 解码后的映射（不可修改）
     */
    public static Map<String, String> decode(String encoded) {
        return decode(encoded, DEFAULT_ENTRY_SEPARATOR, DEFAULT_KEY_VALUE_SEPARATOR);
    }

    /**
     * Decode a string with configurable separators to a map.
     * 使用可配置的分隔符将字符串解码为映射。
     *
     * <p>Returns an unmodifiable map preserving insertion order.
     * Returns an empty map if the input is null or blank.
     * Entries without a key-value separator are silently skipped.</p>
     *
     * <p>返回保持插入顺序的不可修改映射。如果输入为 null 或空白则返回空映射。
     * 没有键值分隔符的条目将被静默跳过。</p>
     *
     * @param encoded            the encoded string / 编码后的字符串
     * @param entrySeparator     separator between entries / 条目之间的分隔符
     * @param keyValueSeparator  separator between key and value / 键值之间的分隔符
     * @return decoded map (unmodifiable) / 解码后的映射（不可修改）
     * @throws NullPointerException if entrySeparator or keyValueSeparator is null / 如果分隔符为 null
     */
    public static Map<String, String> decode(String encoded, String entrySeparator, String keyValueSeparator) {
        Objects.requireNonNull(entrySeparator, "entrySeparator must not be null / entrySeparator 不能为空");
        Objects.requireNonNull(keyValueSeparator, "keyValueSeparator must not be null / keyValueSeparator 不能为空");
        if (encoded == null || encoded.isBlank()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String pair : encoded.split(java.util.regex.Pattern.quote(entrySeparator))) {
            int idx = pair.indexOf(keyValueSeparator);
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + keyValueSeparator.length());
                result.put(key, value);
            }
        }
        return Collections.unmodifiableMap(result);
    }
}
