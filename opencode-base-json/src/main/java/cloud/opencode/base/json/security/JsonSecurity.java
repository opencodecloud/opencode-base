
package cloud.opencode.base.json.security;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.annotation.JsonMask;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON Security - Security Utilities for JSON Processing
 * JSON 安全 - JSON 处理的安全工具
 *
 * <p>This class provides security features including data masking,
 * depth/size validation, and secure parsing options.</p>
 * <p>此类提供安全特性，包括数据脱敏、深度/大小验证和安全解析选项。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Data masking for sensitive fields - 敏感字段的数据脱敏</li>
 *   <li>Depth and size limits to prevent DoS - 深度和大小限制以防止 DoS</li>
 *   <li>Dangerous key detection - 危险键检测</li>
 *   <li>XSS prevention - XSS 防护</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Mask sensitive data
 * String masked = JsonSecurity.mask("13812345678", JsonMask.MaskType.PHONE);
 * // Result: "138****5678"
 *
 * // Validate JSON depth
 * JsonSecurity.validateDepth(jsonNode, 50);
 *
 * // Sanitize for XSS
 * String safe = JsonSecurity.sanitizeForHtml(jsonString);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Data masking for sensitive fields (phone, email, ID card, etc.) - 敏感字段数据脱敏</li>
 *   <li>JSON depth and size validation against DoS attacks - JSON深度和大小验证防止DoS攻击</li>
 *   <li>XSS prevention via HTML sanitization - 通过HTML净化防止XSS</li>
 *   <li>Dangerous key detection for injection prevention - 危险键检测防止注入</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class JsonSecurity {

    /**
     * Default maximum depth
     * 默认最大深度
     */
    public static final int DEFAULT_MAX_DEPTH = 1000;

    /**
     * Default maximum string length
     * 默认最大字符串长度
     */
    public static final int DEFAULT_MAX_STRING_LENGTH = 20_000_000;

    /**
     * Default maximum entries
     * 默认最大条目数
     */
    public static final int DEFAULT_MAX_ENTRIES = 100_000;

    /**
     * Dangerous property names that may indicate injection attempts
     * 可能表示注入尝试的危险属性名
     */
    private static final int MAX_MASK_PATTERN_CACHE_SIZE = 256;
    private static final Map<String, Pattern> PATTERN_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private static final Set<String> DANGEROUS_KEYS = Set.of(
            "__proto__", "constructor", "prototype",
            "$where", "$regex", "$gt", "$lt", "$ne",
            "eval", "Function", "setTimeout", "setInterval"
    );

    private JsonSecurity() {
        // Utility class
    }

    // ==================== Data Masking ====================

    /**
     * Masks a string value based on mask type.
     * 根据脱敏类型对字符串值进行脱敏。
     *
     * @param value the value to mask - 要脱敏的值
     * @param type  the mask type - 脱敏类型
     * @return the masked value - 脱敏后的值
     */
    public static String mask(String value, JsonMask.MaskType type) {
        return mask(value, type, '*');
    }

    /**
     * Masks a string value with custom mask character.
     * 使用自定义脱敏字符对字符串值进行脱敏。
     *
     * @param value    the value to mask - 要脱敏的值
     * @param type     the mask type - 脱敏类型
     * @param maskChar the mask character - 脱敏字符
     * @return the masked value - 脱敏后的值
     */
    public static String mask(String value, JsonMask.MaskType type, char maskChar) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return switch (type) {
            case PASSWORD -> repeat(maskChar, 6);
            case PHONE -> maskPhone(value, maskChar);
            case ID_CARD -> maskIdCard(value, maskChar);
            case EMAIL -> maskEmail(value, maskChar);
            case BANK_CARD -> maskBankCard(value, maskChar);
            case NAME -> maskName(value, maskChar);
            case ADDRESS -> maskAddress(value, maskChar);
            case FULL -> repeat(maskChar, 6);
            case CUSTOM -> repeat(maskChar, value.length());
        };
    }

    /**
     * Masks a value with custom prefix/suffix lengths.
     * 使用自定义前缀/后缀长度对值进行脱敏。
     *
     * @param value        the value to mask - 要脱敏的值
     * @param prefixLength visible prefix length - 可见前缀长度
     * @param suffixLength visible suffix length - 可见后缀长度
     * @param maskChar     the mask character - 脱敏字符
     * @return the masked value - 脱敏后的值
     */
    public static String mask(String value, int prefixLength, int suffixLength, char maskChar) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        int len = value.length();
        if (prefixLength + suffixLength >= len) {
            return repeat(maskChar, len);
        }

        String prefix = value.substring(0, prefixLength);
        String suffix = value.substring(len - suffixLength);
        int maskLen = len - prefixLength - suffixLength;

        return prefix + repeat(maskChar, maskLen) + suffix;
    }

    /**
     * Masks a value using regex pattern.
     * 使用正则表达式模式对值进行脱敏。
     *
     * <p>Note: This method validates the pattern and escapes special characters
     * in the replacement to prevent ReDoS and injection attacks.</p>
     * <p>注意：此方法验证模式并转义替换字符串中的特殊字符以防止ReDoS和注入攻击。</p>
     *
     * @param value       the value to mask - 要脱敏的值
     * @param pattern     the regex pattern - 正则表达式模式
     * @param replacement the replacement - 替换内容
     * @return the masked value - 脱敏后的值
     * @throws IllegalArgumentException if pattern is invalid | 如果模式无效则抛出异常
     */
    public static String maskWithPattern(String value, String pattern, String replacement) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (pattern == null || pattern.isEmpty()) {
            return value;
        }
        if (pattern.length() > 1000) {
            throw new IllegalArgumentException("Pattern length exceeds maximum of 1000 characters");
        }
        try {
            // Compile pattern to validate it before use
            Pattern compiledPattern = PATTERN_CACHE.get(pattern);
            if (compiledPattern == null) {
                compiledPattern = Pattern.compile(pattern);
                if (PATTERN_CACHE.size() < MAX_MASK_PATTERN_CACHE_SIZE) {
                    PATTERN_CACHE.putIfAbsent(pattern, compiledPattern);
                    Pattern existing = PATTERN_CACHE.get(pattern);
                    if (existing != null) compiledPattern = existing;
                }
            }
            // Use Matcher.quoteReplacement to escape $ and \ in replacement
            String safeReplacement = replacement != null ? Matcher.quoteReplacement(replacement) : "";
            return compiledPattern.matcher(value).replaceAll(safeReplacement);
        } catch (java.util.regex.PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regex pattern: " + pattern, e);
        }
    }

    private static String maskPhone(String value, char maskChar) {
        // Format: 138****5678
        if (value.length() < 7) {
            return repeat(maskChar, value.length());
        }
        return value.substring(0, 3) + repeat(maskChar, 4) + value.substring(value.length() - 4);
    }

    private static String maskIdCard(String value, char maskChar) {
        // Format: 110***********1234
        if (value.length() < 8) {
            return repeat(maskChar, value.length());
        }
        return value.substring(0, 3) + repeat(maskChar, value.length() - 7) + value.substring(value.length() - 4);
    }

    private static String maskEmail(String value, char maskChar) {
        // Format: t***@example.com
        int atIndex = value.indexOf('@');
        if (atIndex <= 1) {
            return repeat(maskChar, value.length());
        }
        return value.charAt(0) + repeat(maskChar, 3) + value.substring(atIndex);
    }

    private static String maskBankCard(String value, char maskChar) {
        // Format: 6222****0123
        if (value.length() < 8) {
            return repeat(maskChar, value.length());
        }
        return value.substring(0, 4) + repeat(maskChar, 4) + value.substring(value.length() - 4);
    }

    private static String maskName(String value, char maskChar) {
        // Keep first character, mask rest
        if (value.length() <= 1) {
            return value;
        }
        return value.charAt(0) + repeat(maskChar, value.length() - 1);
    }

    private static String maskAddress(String value, char maskChar) {
        // Keep first few characters of address
        if (value.length() <= 6) {
            return repeat(maskChar, value.length());
        }
        return value.substring(0, 6) + repeat(maskChar, 4);
    }

    private static String repeat(char c, int count) {
        if (count <= 0) return "";
        char[] chars = new char[count];
        java.util.Arrays.fill(chars, c);
        return new String(chars);
    }

    // ==================== Validation ====================

    /**
     * Validates JSON depth.
     * 验证 JSON 深度。
     *
     * @param node     the JSON node - JSON 节点
     * @param maxDepth maximum allowed depth - 最大允许深度
     * @throws OpenJsonProcessingException if depth exceeds limit - 如果深度超过限制
     */
    public static void validateDepth(JsonNode node, int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be non-negative");
        }
        int depth = calculateDepth(node, 0);
        if (depth > maxDepth) {
            throw OpenJsonProcessingException.configError(
                    "JSON depth " + depth + " exceeds maximum " + maxDepth);
        }
    }

    /**
     * Validates JSON size (total entries).
     * 验证 JSON 大小（总条目数）。
     *
     * @param node    the JSON node - JSON 节点
     * @param maxSize maximum allowed entries - 最大允许条目数
     * @throws OpenJsonProcessingException if size exceeds limit - 如果大小超过限制
     */
    public static void validateSize(JsonNode node, int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize must be non-negative");
        }
        int size = calculateSize(node);
        if (size > maxSize) {
            throw OpenJsonProcessingException.configError(
                    "JSON size " + size + " exceeds maximum " + maxSize);
        }
    }

    /**
     * Calculates the depth of a JSON tree.
     * 计算 JSON 树的深度。
     *
     * @param node the JSON node - JSON 节点
     * @return the depth - 深度
     */
    public static int calculateDepth(JsonNode node) {
        return calculateDepth(node, 0);
    }

    private static int calculateDepth(JsonNode node, int current) {
        if (node == null || node.isValue()) {
            return current;
        }
        // Guard against stack overflow on extremely deep structures
        if (current > DEFAULT_MAX_DEPTH) {
            return current;
        }

        int maxChildDepth = current;
        if (node.isObject()) {
            for (String key : node.keys()) {
                maxChildDepth = Math.max(maxChildDepth, calculateDepth(node.get(key), current + 1));
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                maxChildDepth = Math.max(maxChildDepth, calculateDepth(node.get(i), current + 1));
            }
        }
        return maxChildDepth;
    }

    /**
     * Calculates the total size (entries) of a JSON tree.
     * 计算 JSON 树的总大小（条目数）。
     *
     * @param node the JSON node - JSON 节点
     * @return the size - 大小
     */
    public static int calculateSize(JsonNode node) {
        return calculateSize(node, 0);
    }

    private static int calculateSize(JsonNode node, int depth) {
        if (node == null || node.isValue()) {
            return 1;
        }
        // Guard against stack overflow on extremely deep structures
        if (depth > DEFAULT_MAX_DEPTH) {
            return 1;
        }

        int size = 1;
        if (node.isObject()) {
            for (String key : node.keys()) {
                size += calculateSize(node.get(key), depth + 1);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                size += calculateSize(node.get(i), depth + 1);
            }
        }
        return size;
    }

    // ==================== Security Checks ====================

    /**
     * Checks for dangerous property keys.
     * 检查危险的属性键。
     *
     * @param node the JSON node - JSON 节点
     * @return list of dangerous keys found - 找到的危险键列表
     */
    public static List<String> findDangerousKeys(JsonNode node) {
        List<String> found = new ArrayList<>();
        findDangerousKeysRecursive(node, "", found, 0);
        return found;
    }

    private static void findDangerousKeysRecursive(JsonNode node, String path, List<String> found, int depth) {
        if (node == null || node.isValue()) {
            return;
        }
        // Guard against stack overflow on extremely deep structures
        if (depth > DEFAULT_MAX_DEPTH) {
            found.add(path.isEmpty() ? "<depth exceeded>" : path + "/<depth exceeded>");
            return;
        }

        if (node.isObject()) {
            for (String key : node.keys()) {
                if (DANGEROUS_KEYS.contains(key)) {
                    found.add(path.isEmpty() ? key : path + "." + key);
                }
                findDangerousKeysRecursive(node.get(key), path.isEmpty() ? key : path + "." + key, found, depth + 1);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                findDangerousKeysRecursive(node.get(i), path + "[" + i + "]", found, depth + 1);
            }
        }
    }

    /**
     * Checks if JSON contains dangerous keys.
     * 检查 JSON 是否包含危险键。
     *
     * @param node the JSON node - JSON 节点
     * @return true if dangerous keys found - 如果发现危险键则返回 true
     */
    public static boolean hasDangerousKeys(JsonNode node) {
        return !findDangerousKeys(node).isEmpty();
    }

    // ==================== XSS Prevention ====================

    /**
     * Sanitizes a string for safe HTML output.
     * 净化字符串以安全输出 HTML。
     *
     * @param value the value to sanitize - 要净化的值
     * @return the sanitized value - 净化后的值
     */
    public static String sanitizeForHtml(String value) {
        if (value == null) {
            return null;
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;")
                .replace("\uFF1C", "&lt;")   // fullwidth <
                .replace("\uFF1E", "&gt;")   // fullwidth >
                .replace("\uFF06", "&amp;");  // fullwidth &
    }

    /**
     * Sanitizes all string values in a JSON tree for HTML output.
     * 净化 JSON 树中所有字符串值以安全输出 HTML。
     *
     * @param node the JSON node - JSON 节点
     * @return sanitized copy of the node - 净化后的节点副本
     */
    public static JsonNode sanitizeForHtml(JsonNode node) {
        if (node == null || node.isNull()) {
            return node;
        }
        if (node.isString()) {
            return JsonNode.of(sanitizeForHtml(node.asString()));
        }
        if (node.isObject()) {
            JsonNode.ObjectNode result = JsonNode.object();
            for (String key : node.keys()) {
                result.put(sanitizeForHtml(key), sanitizeForHtml(node.get(key)));
            }
            return result;
        }
        if (node.isArray()) {
            JsonNode.ArrayNode result = JsonNode.array();
            for (int i = 0; i < node.size(); i++) {
                result.add(sanitizeForHtml(node.get(i)));
            }
            return result;
        }
        return node;
    }

    // ==================== Secure Parsing Options ====================

    /**
     * Security options for JSON parsing.
     * JSON 解析的安全选项。
     */
    public record SecurityOptions(
            int maxDepth,
            int maxStringLength,
            int maxEntries,
            boolean rejectDangerousKeys,
            boolean sanitizeStrings
    ) {
        public static SecurityOptions defaults() {
            return new SecurityOptions(
                    DEFAULT_MAX_DEPTH,
                    DEFAULT_MAX_STRING_LENGTH,
                    DEFAULT_MAX_ENTRIES,
                    true,
                    false
            );
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int maxDepth = DEFAULT_MAX_DEPTH;
            private int maxStringLength = DEFAULT_MAX_STRING_LENGTH;
            private int maxEntries = DEFAULT_MAX_ENTRIES;
            private boolean rejectDangerousKeys = true;
            private boolean sanitizeStrings = false;

            public Builder maxDepth(int maxDepth) {
                this.maxDepth = maxDepth;
                return this;
            }

            public Builder maxStringLength(int maxStringLength) {
                this.maxStringLength = maxStringLength;
                return this;
            }

            public Builder maxEntries(int maxEntries) {
                this.maxEntries = maxEntries;
                return this;
            }

            public Builder rejectDangerousKeys(boolean reject) {
                this.rejectDangerousKeys = reject;
                return this;
            }

            public Builder sanitizeStrings(boolean sanitize) {
                this.sanitizeStrings = sanitize;
                return this;
            }

            public SecurityOptions build() {
                return new SecurityOptions(maxDepth, maxStringLength, maxEntries,
                        rejectDangerousKeys, sanitizeStrings);
            }
        }
    }

    /**
     * Validates a JSON node against security options.
     * 根据安全选项验证 JSON 节点。
     *
     * @param node    the JSON node - JSON 节点
     * @param options the security options - 安全选项
     * @throws OpenJsonProcessingException if validation fails - 如果验证失败
     */
    public static void validate(JsonNode node, SecurityOptions options) {
        validateDepth(node, options.maxDepth());
        validateSize(node, options.maxEntries());

        if (options.rejectDangerousKeys() && hasDangerousKeys(node)) {
            List<String> dangerous = findDangerousKeys(node);
            throw OpenJsonProcessingException.configError(
                    "JSON contains dangerous keys: " + dangerous);
        }
    }
}
