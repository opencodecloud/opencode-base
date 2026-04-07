package cloud.opencode.base.config;

import java.util.*;

/**
 * Relaxed Key Resolver for configuration property matching
 * 宽松键解析器，用于配置属性匹配
 *
 * <p>Provides relaxed binding support, allowing configuration keys to be matched
 * regardless of naming convention (kebab-case, camelCase, snake_case, UPPER_SNAKE).</p>
 * <p>提供宽松绑定支持，允许配置键在不同命名约定（kebab-case、camelCase、snake_case、UPPER_SNAKE）之间匹配。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key normalization to canonical form - 键归一化为规范形式</li>
 *   <li>Variant generation for all common naming styles - 生成所有常见命名风格变体</li>
 *   <li>Relaxed resolution against available keys - 宽松解析匹配可用键</li>
 * </ul>
 *
 * <p><strong>Normalization Rules | 归一化规则:</strong></p>
 * <ul>
 *   <li>UPPER_SNAKE keys: replace {@code _} with {@code .}, lowercase - UPPER_SNAKE键: 将{@code _}替换为{@code .}，转小写</li>
 *   <li>Dot-separated keys: remove {@code -} and {@code _}, lowercase - 点分隔键: 移除{@code -}和{@code _}，转小写</li>
 *   <li>All variants produce the same canonical form - 所有变体产生相同的规范形式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Normalization
 * RelaxedKeyResolver.normalize("database.max-pool-size");  // "databasemaxpoolsize"
 * RelaxedKeyResolver.normalize("DATABASE_MAX_POOL_SIZE");  // "databasemaxpoolsize"
 * RelaxedKeyResolver.normalize("database.maxPoolSize");    // "databasemaxpoolsize"
 *
 * // Variants
 * Set<String> variants = RelaxedKeyResolver.variants("database.max-pool-size");
 * // Contains: "database.max-pool-size", "database.maxPoolSize",
 * //           "database.max_pool_size", "DATABASE_MAX_POOL_SIZE",
 * //           "database.maxpoolsize"
 *
 * // Resolution
 * Set<String> available = Set.of("DATABASE_MAX_POOL_SIZE");
 * Optional<String> key = RelaxedKeyResolver.resolve("database.max-pool-size", available);
 * // Returns: Optional["DATABASE_MAX_POOL_SIZE"]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for resolve where n is availableKeys size - 时间复杂度: resolve为O(n)，n为可用键数量</li>
 *   <li>Space complexity: O(1) for normalize, O(k) for variants where k is segments count - 空间复杂度: normalize为O(1)，variants为O(k)，k为段数</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (all methods are stateless) - 线程安全: 是（所有方法无状态）</li>
 *   <li>Immutable results - 不可变结果</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.3
 */
public final class RelaxedKeyResolver {

    private RelaxedKeyResolver() {
        // Utility class - no instantiation
    }

    /**
     * Normalize a key to a canonical form for comparison
     * 将键归一化为规范形式以便比较
     *
     * <p>The normalization algorithm strips all hyphens ({@code -}), underscores ({@code _}),
     * and dots ({@code .}), then lowercases the result. This ensures all naming conventions
     * converge to the same canonical form.</p>
     *
     * <p>归一化算法移除所有连字符（{@code -}）、下划线（{@code _}）和点（{@code .}），
     * 然后转为小写。这确保所有命名约定收敛到相同的规范形式。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <ul>
     *   <li>{@code "database.max-pool-size"} → {@code "databasemaxpoolsize"}</li>
     *   <li>{@code "DATABASE_MAX_POOL_SIZE"} → {@code "databasemaxpoolsize"}</li>
     *   <li>{@code "database.maxPoolSize"} → {@code "databasemaxpoolsize"}</li>
     * </ul>
     *
     * @param key the configuration key to normalize | 要归一化的配置键
     * @return the canonical form of the key | 键的规范形式
     * @throws NullPointerException if key is null | 如果键为null
     */
    public static String normalize(String key) {
        Objects.requireNonNull(key, "key must not be null");
        if (key.isEmpty()) {
            return key;
        }

        // Canonical normalization: strip all '-', '_', '.', lowercase everything.
        // This ensures all naming conventions converge to the same canonical form:
        //   "database.max-pool-size"  → "databasemaxpoolsize"
        //   "DATABASE_MAX_POOL_SIZE"  → "databasemaxpoolsize"
        //   "database.maxPoolSize"    → "databasemaxpoolsize"
        //   "database.max_pool_size"  → "databasemaxpoolsize"
        StringBuilder sb = new StringBuilder(key.length());
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '-' || c == '_' || c == '.') {
                continue;
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /**
     * Generate all common variants of a key
     * 生成键的所有常见变体
     *
     * <p>Given a key in any supported format, generates variants in:</p>
     * <ul>
     *   <li>kebab-case: {@code database.max-pool-size}</li>
     *   <li>camelCase: {@code database.maxPoolSize}</li>
     *   <li>snake_case: {@code database.max_pool_size}</li>
     *   <li>UPPER_SNAKE: {@code DATABASE_MAX_POOL_SIZE}</li>
     *   <li>flat lowercase: {@code database.maxpoolsize}</li>
     * </ul>
     *
     * <p>给定任何支持格式的键，生成以下格式变体：</p>
     * <ul>
     *   <li>kebab-case: {@code database.max-pool-size}</li>
     *   <li>camelCase: {@code database.maxPoolSize}</li>
     *   <li>snake_case: {@code database.max_pool_size}</li>
     *   <li>UPPER_SNAKE: {@code DATABASE_MAX_POOL_SIZE}</li>
     *   <li>扁平小写: {@code database.maxpoolsize}</li>
     * </ul>
     *
     * @param key the configuration key | 配置键
     * @return an unmodifiable set of all variant forms | 所有变体形式的不可修改集合
     * @throws NullPointerException if key is null | 如果键为null
     */
    public static Set<String> variants(String key) {
        Objects.requireNonNull(key, "key must not be null");
        if (key.isEmpty()) {
            return Set.of(key);
        }

        // First, parse the key into word segments grouped by dot-separated parts
        List<List<String>> parts = parseIntoParts(key);

        Set<String> result = new LinkedHashSet<>();

        // kebab-case: database.max-pool-size
        result.add(buildWithSeparator(parts, ".", "-"));
        // camelCase: database.maxPoolSize
        result.add(buildCamelCase(parts));
        // snake_case: database.max_pool_size
        result.add(buildWithSeparator(parts, ".", "_"));
        // UPPER_SNAKE: DATABASE_MAX_POOL_SIZE
        result.add(buildUpperSnake(parts));
        // flat lowercase: database.maxpoolsize
        result.add(buildFlat(parts));

        return Collections.unmodifiableSet(result);
    }

    /**
     * Resolve a key against available keys using relaxed matching
     * 使用宽松匹配将键解析为可用键
     *
     * <p>Resolution strategy:</p>
     * <ol>
     *   <li>Exact match first</li>
     *   <li>Normalize both sides and compare</li>
     * </ol>
     *
     * <p>解析策略：</p>
     * <ol>
     *   <li>首先精确匹配</li>
     *   <li>归一化两端后比较</li>
     * </ol>
     *
     * @param key           the key to resolve | 要解析的键
     * @param availableKeys the set of available keys to match against | 要匹配的可用键集合
     * @return an Optional containing the matching key from availableKeys, or empty if no match |
     *         包含来自availableKeys的匹配键的Optional，如果没有匹配则为空
     * @throws NullPointerException if key or availableKeys is null | 如果键或可用键为null
     */
    public static Optional<String> resolve(String key, Set<String> availableKeys) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(availableKeys, "availableKeys must not be null");

        // 1. Exact match
        if (availableKeys.contains(key)) {
            return Optional.of(key);
        }

        // 2. Normalize and compare
        String normalizedKey = normalize(key);
        for (String candidate : availableKeys) {
            if (normalizedKey.equals(normalize(candidate))) {
                return Optional.of(candidate);
            }
        }

        return Optional.empty();
    }

    // ============ Internal Helpers | 内部辅助方法 ============

    /**
     * Check if a key is in UPPER_SNAKE format
     */
    private static boolean isUpperSnake(String key) {
        boolean hasUpper = false;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (c != '_' && !Character.isDigit(c)) {
                return false;
            }
        }
        return hasUpper;
    }

    /**
     * Parse a key (in any format) into a list of parts, where each part is a list of words.
     * E.g., "database.max-pool-size" → [["database"], ["max", "pool", "size"]]
     *        "DATABASE_MAX_POOL_SIZE" → [["database"], ["max", "pool", "size"]]
     *        "database.maxPoolSize"   → [["database"], ["max", "pool", "size"]]
     */
    private static List<List<String>> parseIntoParts(String key) {
        String[] dotSegments;

        if (isUpperSnake(key)) {
            // UPPER_SNAKE: replace _ with . then split
            dotSegments = key.toLowerCase(Locale.ROOT).replace('_', '.').split("\\.");
        } else {
            // Split by dots first
            dotSegments = key.split("\\.");
        }

        List<List<String>> parts = new ArrayList<>();
        for (String segment : dotSegments) {
            if (segment.isEmpty()) {
                continue;
            }
            parts.add(splitIntoWords(segment));
        }
        return parts;
    }

    /**
     * Split a segment into individual words.
     * Handles kebab-case, snake_case, and camelCase.
     * E.g., "max-pool-size" → ["max", "pool", "size"]
     *        "maxPoolSize"  → ["max", "pool", "size"]
     *        "max_pool_size" → ["max", "pool", "size"]
     */
    private static List<String> splitIntoWords(String segment) {
        // First split by hyphens and underscores
        String[] tokens = segment.split("[-_]");
        List<String> words = new ArrayList<>();

        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            // Split camelCase
            splitCamelCase(token, words);
        }

        return words;
    }

    /**
     * Split a camelCase token into words.
     * E.g., "maxPoolSize" → ["max", "pool", "size"]
     */
    private static void splitCamelCase(String token, List<String> words) {
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (Character.isUpperCase(c) && current.length() > 0) {
                words.add(current.toString().toLowerCase(Locale.ROOT));
                current.setLength(0);
            }
            current.append(c);
        }
        if (current.length() > 0) {
            words.add(current.toString().toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Build a key with the given part separator (dot) and word separator within parts.
     * E.g., parts=[["database"], ["max","pool","size"]], partSep=".", wordSep="-"
     *        → "database.max-pool-size"
     */
    private static String buildWithSeparator(List<List<String>> parts, String partSep, String wordSep) {
        StringJoiner partJoiner = new StringJoiner(partSep);
        for (List<String> words : parts) {
            partJoiner.add(String.join(wordSep, words));
        }
        return partJoiner.toString();
    }

    /**
     * Build camelCase form.
     * E.g., parts=[["database"], ["max","pool","size"]] → "database.maxPoolSize"
     */
    private static String buildCamelCase(List<List<String>> parts) {
        StringJoiner partJoiner = new StringJoiner(".");
        for (List<String> words : parts) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i);
                if (i == 0) {
                    sb.append(word);
                } else {
                    sb.append(Character.toUpperCase(word.charAt(0)));
                    sb.append(word.substring(1));
                }
            }
            partJoiner.add(sb.toString());
        }
        return partJoiner.toString();
    }

    /**
     * Build UPPER_SNAKE form.
     * E.g., parts=[["database"], ["max","pool","size"]] → "DATABASE_MAX_POOL_SIZE"
     */
    private static String buildUpperSnake(List<List<String>> parts) {
        List<String> allWords = new ArrayList<>();
        for (List<String> words : parts) {
            allWords.addAll(words);
        }
        return String.join("_", allWords).toUpperCase(Locale.ROOT);
    }

    /**
     * Build flat lowercase form.
     * E.g., parts=[["database"], ["max","pool","size"]] → "database.maxpoolsize"
     */
    private static String buildFlat(List<List<String>> parts) {
        StringJoiner partJoiner = new StringJoiner(".");
        for (List<String> words : parts) {
            partJoiner.add(String.join("", words));
        }
        return partJoiner.toString();
    }
}
