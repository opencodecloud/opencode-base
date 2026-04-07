package cloud.opencode.base.classloader.scanner;

import java.util.*;

/**
 * Cached Scan Result - Immutable record representing a cached class scan result
 * 缓存扫描结果 - 表示缓存的类扫描结果的不可变记录
 *
 * <p>Stores the scan result along with a classpath hash for cache validation.
 * If the classpath hash changes between runs, the cache is considered stale.</p>
 * <p>存储扫描结果及类路径哈希用于缓存验证。
 * 如果类路径哈希在两次运行之间发生变化，则缓存视为过期。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record with defensive copy - 防御性拷贝的不可变记录</li>
 *   <li>JSON serialization/deserialization - JSON 序列化/反序列化</li>
 *   <li>Classpath hash validation - 类路径哈希验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CachedScanResult result = new CachedScanResult(hash, classNames, timestamp);
 * String json = result.toJson();
 * CachedScanResult restored = CachedScanResult.fromJson(json);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record CachedScanResult(
        String classpathHash,
        Set<String> classNames,
        String timestamp
) {

    /**
     * Compact constructor with null checks, defensive copy, and sorted storage
     * 紧凑构造器：null 校验、防御性拷贝和排序存储
     */
    public CachedScanResult {
        Objects.requireNonNull(classpathHash, "classpathHash must not be null");
        Objects.requireNonNull(classNames, "classNames must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        // Store as a sorted unmodifiable set to avoid re-sorting during toJson
        classNames = Collections.unmodifiableSet(new java.util.TreeSet<>(classNames));
    }

    /**
     * Serialize to JSON string
     * 序列化为 JSON 字符串
     *
     * @return JSON representation | JSON 表示
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"classpathHash\": \"").append(escapeJson(classpathHash)).append("\",\n");
        sb.append("  \"timestamp\": \"").append(escapeJson(timestamp)).append("\",\n");
        sb.append("  \"classNames\": [");

        // classNames is already sorted (TreeSet) — no need to re-sort
        boolean first = true;
        for (String name : classNames) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\n    \"").append(escapeJson(name)).append("\"");
        }

        if (!classNames.isEmpty()) {
            sb.append("\n  ");
        }
        sb.append("]\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Deserialize from JSON string
     * 从 JSON 字符串反序列化
     *
     * @param json JSON string | JSON 字符串
     * @return parsed result | 解析的结果
     * @throws IllegalArgumentException if JSON is invalid | JSON 无效时抛出
     */
    public static CachedScanResult fromJson(String json) {
        Objects.requireNonNull(json, "JSON string must not be null");

        String classpathHash = extractStringValue(json, "classpathHash");
        String timestamp = extractStringValue(json, "timestamp");
        Set<String> classNames = extractStringArray(json, "classNames");

        return new CachedScanResult(classpathHash, classNames, timestamp);
    }

    // ==================== Private Methods | 私有方法 ====================

    /**
     * Extract a string value for a given key from JSON
     * 从 JSON 中提取给定键的字符串值
     */
    private static String extractStringValue(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex < 0) {
            throw new IllegalArgumentException("Missing key: " + key);
        }
        int colonIndex = json.indexOf(':', keyIndex + search.length());
        if (colonIndex < 0) {
            throw new IllegalArgumentException("Invalid JSON: no colon after key: " + key);
        }
        // Find opening quote
        int start = json.indexOf('"', colonIndex + 1);
        if (start < 0) {
            throw new IllegalArgumentException("Invalid JSON: no value for key: " + key);
        }
        // Find closing quote (handle escaped quotes)
        int end = findClosingQuote(json, start + 1);
        return unescapeJson(json.substring(start + 1, end));
    }

    /**
     * Extract a string array for a given key from JSON
     * 从 JSON 中提取给定键的字符串数组
     */
    private static Set<String> extractStringArray(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex < 0) {
            throw new IllegalArgumentException("Missing key: " + key);
        }
        int bracketStart = json.indexOf('[', keyIndex + search.length());
        if (bracketStart < 0) {
            throw new IllegalArgumentException("Invalid JSON: no array for key: " + key);
        }
        int bracketEnd = json.indexOf(']', bracketStart);
        if (bracketEnd < 0) {
            throw new IllegalArgumentException("Invalid JSON: unclosed array for key: " + key);
        }

        String arrayContent = json.substring(bracketStart + 1, bracketEnd).trim();
        if (arrayContent.isEmpty()) {
            return Set.of();
        }

        Set<String> result = new LinkedHashSet<>();
        int pos = 0;
        while (pos < arrayContent.length()) {
            int quoteStart = arrayContent.indexOf('"', pos);
            if (quoteStart < 0) {
                break;
            }
            int quoteEnd = findClosingQuote(arrayContent, quoteStart + 1);
            result.add(unescapeJson(arrayContent.substring(quoteStart + 1, quoteEnd)));
            pos = quoteEnd + 1;
        }
        return result;
    }

    /**
     * Find the closing (unescaped) quote
     * 查找未转义的闭合引号
     */
    private static int findClosingQuote(String s, int from) {
        for (int i = from; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++; // skip escaped char
            } else if (c == '"') {
                return i;
            }
        }
        throw new IllegalArgumentException("Invalid JSON: unclosed string");
    }

    /**
     * Escape a string for JSON output (delegates to shared utility)
     * 为 JSON 输出转义字符串（委托给共享工具方法）
     */
    static String escapeJson(String value) {
        return cloud.opencode.base.classloader.index.ClassIndexWriter.escapeJson(value);
    }

    /**
     * Unescape a JSON string value
     * 反转义 JSON 字符串值
     */
    static String unescapeJson(String value) {
        if (value == null || value.indexOf('\\') < 0) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(i + 1);
                switch (next) {
                    case '"' -> { sb.append('"'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case 'b' -> { sb.append('\b'); i++; }
                    case 'f' -> { sb.append('\f'); i++; }
                    case 'u' -> {
                        if (i + 5 < value.length()) {
                            String hex = value.substring(i + 2, i + 6);
                            sb.append((char) Integer.parseInt(hex, 16));
                            i += 5;
                        } else {
                            sb.append(c);
                        }
                    }
                    default -> sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
