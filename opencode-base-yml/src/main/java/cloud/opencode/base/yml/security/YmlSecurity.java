/*
 * Copyright 2025 OpenCode Cloud Group.
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

package cloud.opencode.base.yml.security;

import cloud.opencode.base.yml.YmlConfig;
import cloud.opencode.base.yml.exception.YmlSecurityException;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * YML Security - Security utilities for YAML processing
 * YML 安全 - YAML 处理的安全工具
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Prevention of YAML bomb attacks (billion laughs) - 防止 YAML 炸弹攻击（十亿笑声）</li>
 *   <li>Alias expansion limits - 别名展开限制</li>
 *   <li>Document size limits - 文档大小限制</li>
 *   <li>Safe type restrictions - 安全类型限制</li>
 *   <li>Dangerous pattern detection and sanitization - 危险模式检测和净化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Validate YAML before parsing
 * YmlSecurity.validate(yamlContent);
 *
 * // Check for dangerous patterns
 * if (YmlSecurity.containsDangerousPatterns(yamlContent)) {
 *     throw new YmlSecurityException("Unsafe YAML content");
 * }
 *
 * // Get safe config
 * YmlConfig safeConfig = YmlSecurity.createSafeConfig();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (null input is handled gracefully) - 空值安全: 是（空输入被优雅处理）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see YmlConfig
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class YmlSecurity {

    /**
     * Default maximum document size (10 MB)
     * 默认最大文档大小（10 MB）
     */
    public static final long DEFAULT_MAX_DOCUMENT_SIZE = 10 * 1024 * 1024;

    /**
     * Default maximum alias count
     * 默认最大别名数量
     */
    public static final int DEFAULT_MAX_ALIASES = 50;

    /**
     * Default maximum nesting depth
     * 默认最大嵌套深度
     */
    public static final int DEFAULT_MAX_DEPTH = 50;

    /**
     * Dangerous YAML tag patterns
     * 危险的 YAML 标签模式
     */
    private static final Set<Pattern> DANGEROUS_PATTERNS = Set.of(
            Pattern.compile("!!python/"),
            Pattern.compile("!!java/"),
            Pattern.compile("!!ruby/"),
            Pattern.compile("!!perl/"),
            Pattern.compile("!<tag:yaml\\.org,2002:"),
            Pattern.compile("!!javax\\.script"),
            Pattern.compile("!!com\\.sun\\."),
            Pattern.compile("!!java\\.lang\\.Runtime"),
            Pattern.compile("!!java\\.lang\\.ProcessBuilder")
    );

    private YmlSecurity() {
    }

    // ==================== Validation | 验证 ====================

    /**
     * Validates YAML content for security issues
     * 验证 YAML 内容的安全问题
     *
     * @param yamlContent YAML content | YAML 内容
     * @throws YmlSecurityException if security issue found | 如果发现安全问题
     */
    public static void validate(String yamlContent) {
        validate(yamlContent, DEFAULT_MAX_DOCUMENT_SIZE, DEFAULT_MAX_ALIASES);
    }

    /**
     * Validates YAML content with custom limits
     * 使用自定义限制验证 YAML 内容
     *
     * @param yamlContent YAML content | YAML 内容
     * @param maxSize     maximum document size | 最大文档大小
     * @param maxAliases  maximum alias count | 最大别名数量
     * @throws YmlSecurityException if security issue found | 如果发现安全问题
     */
    public static void validate(String yamlContent, long maxSize, int maxAliases) {
        if (yamlContent == null) {
            return;
        }

        // Check document size
        if (yamlContent.length() > maxSize) {
            throw YmlSecurityException.documentSizeExceeded(yamlContent.length(), maxSize);
        }

        // Check for dangerous patterns
        if (containsDangerousPatterns(yamlContent)) {
            throw YmlSecurityException.forbiddenType("dangerous YAML type tags detected");
        }

        // Count aliases
        int aliasCount = countAliases(yamlContent);
        if (aliasCount > maxAliases) {
            throw YmlSecurityException.aliasLimitExceeded(aliasCount, maxAliases);
        }
    }

    /**
     * Checks if YAML content contains dangerous patterns
     * 检查 YAML 内容是否包含危险模式
     *
     * @param yamlContent YAML content | YAML 内容
     * @return true if dangerous | 如果危险返回 true
     */
    public static boolean containsDangerousPatterns(String yamlContent) {
        if (yamlContent == null || yamlContent.isEmpty()) {
            return false;
        }

        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(yamlContent).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts aliases in YAML content
     * 计算 YAML 内容中的别名数量
     *
     * @param yamlContent YAML content | YAML 内容
     * @return alias count | 别名数量
     */
    public static int countAliases(String yamlContent) {
        if (yamlContent == null || yamlContent.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = yamlContent.indexOf('*', index)) != -1) {
            // Check if it's an alias reference (not in a string)
            if (index == 0 || yamlContent.charAt(index - 1) != '\\') {
                count++;
            }
            index++;
        }
        return count;
    }

    /**
     * Counts anchors in YAML content
     * 计算 YAML 内容中的锚点数量
     *
     * @param yamlContent YAML content | YAML 内容
     * @return anchor count | 锚点数量
     */
    public static int countAnchors(String yamlContent) {
        if (yamlContent == null || yamlContent.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = yamlContent.indexOf('&', index)) != -1) {
            if (index == 0 || yamlContent.charAt(index - 1) != '\\') {
                count++;
            }
            index++;
        }
        return count;
    }

    // ==================== Safe Configuration | 安全配置 ====================

    /**
     * Creates a safe YAML configuration
     * 创建安全的 YAML 配置
     *
     * @return safe configuration | 安全配置
     */
    public static YmlConfig createSafeConfig() {
        return YmlConfig.builder()
                .safeMode(true)
                .maxAliasesForCollections(DEFAULT_MAX_ALIASES)
                .maxDocumentSize(DEFAULT_MAX_DOCUMENT_SIZE)
                .allowDuplicateKeys(false)
                .build();
    }

    /**
     * Creates a safe YAML configuration with custom limits
     * 使用自定义限制创建安全的 YAML 配置
     *
     * @param maxSize    maximum document size | 最大文档大小
     * @param maxAliases maximum alias count | 最大别名数量
     * @return safe configuration | 安全配置
     */
    public static YmlConfig createSafeConfig(long maxSize, int maxAliases) {
        return YmlConfig.builder()
                .safeMode(true)
                .maxAliasesForCollections(maxAliases)
                .maxDocumentSize(maxSize)
                .allowDuplicateKeys(false)
                .build();
    }

    // ==================== Sanitization | 净化 ====================

    /**
     * Sanitizes YAML content by removing dangerous patterns
     * 通过移除危险模式净化 YAML 内容
     *
     * @param yamlContent YAML content | YAML 内容
     * @return sanitized content | 净化后的内容
     */
    public static String sanitize(String yamlContent) {
        if (yamlContent == null || yamlContent.isEmpty()) {
            return yamlContent;
        }

        String result = yamlContent;
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }
        return result;
    }

    /**
     * Checks if a type is safe for deserialization
     * 检查类型是否可以安全反序列化
     *
     * @param typeName the type name | 类型名称
     * @return true if safe | 如果安全返回 true
     */
    public static boolean isSafeType(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return false;
        }

        // Allow basic types
        Set<String> safeTypes = Set.of(
                "java.lang.String",
                "java.lang.Integer",
                "java.lang.Long",
                "java.lang.Double",
                "java.lang.Float",
                "java.lang.Boolean",
                "java.lang.Short",
                "java.lang.Byte",
                "java.lang.Character",
                "java.math.BigDecimal",
                "java.math.BigInteger",
                "java.util.Date",
                "java.time.LocalDate",
                "java.time.LocalDateTime",
                "java.time.LocalTime",
                "java.time.Instant",
                "java.util.UUID"
        );

        // Check if it's a safe type or a collection/map of safe types
        if (safeTypes.contains(typeName)) {
            return true;
        }

        // Allow collections and maps
        if (typeName.startsWith("java.util.List") ||
            typeName.startsWith("java.util.Set") ||
            typeName.startsWith("java.util.Map") ||
            typeName.startsWith("java.util.ArrayList") ||
            typeName.startsWith("java.util.LinkedList") ||
            typeName.startsWith("java.util.HashSet") ||
            typeName.startsWith("java.util.TreeSet") ||
            typeName.startsWith("java.util.HashMap") ||
            typeName.startsWith("java.util.TreeMap") ||
            typeName.startsWith("java.util.LinkedHashMap")) {
            return true;
        }

        return false;
    }
}
