package cloud.opencode.base.cache.config;

import cloud.opencode.base.cache.exception.OpenCacheException;
import cloud.opencode.base.cache.spi.EvictionPolicy;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CacheSpec - Parse cache configuration from a specification string
 * CacheSpec - 从规范字符串解析缓存配置
 *
 * <p>Parses a comma-separated list of key=value pairs into a CacheConfig.
 * This is inspired by Caffeine's CacheSpec and provides a convenient way to
 * configure caches from configuration files or system properties.</p>
 * <p>解析逗号分隔的 key=value 对列表为 CacheConfig。
 * 灵感来自 Caffeine 的 CacheSpec，提供从配置文件或系统属性配置缓存的便捷方式。</p>
 *
 * <p><strong>Supported Options | 支持的选项:</strong></p>
 * <ul>
 *   <li>{@code maximumSize=<long>} - Maximum number of entries | 最大条目数</li>
 *   <li>{@code maximumWeight=<long>} - Maximum total weight | 最大总权重</li>
 *   <li>{@code initialCapacity=<int>} - Initial capacity | 初始容量</li>
 *   <li>{@code concurrencyLevel=<int>} - Concurrency level | 并发级别</li>
 *   <li>{@code expireAfterWrite=<duration>} - TTL expiration | 写入后过期</li>
 *   <li>{@code expireAfterAccess=<duration>} - TTI expiration | 访问后过期</li>
 *   <li>{@code refreshAfterWrite=<duration>} - Refresh interval | 刷新间隔</li>
 *   <li>{@code evictionPolicy=<lru|lfu|fifo|wtinylfu>} - Eviction policy | 淘汰策略</li>
 *   <li>{@code recordStats} - Enable statistics (no value needed) | 启用统计</li>
 *   <li>{@code useVirtualThreads} - Enable virtual threads | 启用虚拟线程</li>
 * </ul>
 *
 * <p><strong>Duration Format | 时间格式:</strong></p>
 * <ul>
 *   <li>{@code 100} or {@code 100ms} - milliseconds | 毫秒</li>
 *   <li>{@code 10s} - seconds | 秒</li>
 *   <li>{@code 5m} - minutes | 分钟</li>
 *   <li>{@code 2h} - hours | 小时</li>
 *   <li>{@code 1d} - days | 天</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse specification string | 解析规范字符串
 * CacheConfig<String, User> config = CacheSpec.parse(
 *     "maximumSize=10000,expireAfterWrite=30m,recordStats");
 *
 * // Use with OpenCache | 与 OpenCache 一起使用
 * Cache<String, User> cache = OpenCache.fromSpec("users",
 *     "maximumSize=10000,expireAfterWrite=30m,expireAfterAccess=10m");
 *
 * // From properties file | 从配置文件
 * String spec = properties.getProperty("cache.users.spec");
 * Cache<String, User> cache = OpenCache.fromSpec("users", spec);
 *
 * // Complex configuration | 复杂配置
 * String spec = "maximumSize=50000,expireAfterWrite=1h,expireAfterAccess=30m," +
 *               "initialCapacity=1000,concurrencyLevel=32,evictionPolicy=wtinylfu," +
 *               "recordStats,useVirtualThreads";
 * CacheConfig<K, V> config = CacheSpec.parse(spec);
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is immutable and thread-safe.</p>
 * <p>此类是不可变的且线程安全。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>String-based cache configuration - 字符串缓存配置</li>
 *   <li>Duration parsing (ms, s, m, h, d) - 时间解析</li>
 *   <li>Validation support - 验证支持</li>
 *   <li>Properties file compatibility - 属性文件兼容</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class CacheSpec {

    /**
     * Pattern for duration values: number followed by optional unit (ms, s, m, h, d)
     * 时间值的模式：数字后跟可选单位
     */
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)([a-zA-Z]*)$");

    /**
     * Known configuration keys for validation
     * 已知的配置键，用于验证
     */
    private static final java.util.Set<String> KNOWN_KEYS = java.util.Set.of(
            "maximumSize", "maximumWeight", "initialCapacity", "concurrencyLevel",
            "expireAfterWrite", "expireAfterAccess", "refreshAfterWrite",
            "evictionPolicy", "recordStats", "useVirtualThreads"
    );

    private CacheSpec() {
        // Utility class, not instantiable
    }

    // ==================== Parse Methods | 解析方法 ====================

    /**
     * Parse a specification string into a CacheConfig
     * 解析规范字符串为 CacheConfig
     *
     * @param spec the specification string | 规范字符串
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return parsed CacheConfig | 解析后的 CacheConfig
     * @throws OpenCacheException if the specification is invalid | 规范无效时抛出异常
     */
    public static <K, V> CacheConfig<K, V> parse(String spec) {
        Objects.requireNonNull(spec, "spec must not be null");

        CacheConfig.Builder<K, V> builder = CacheConfig.builder();

        if (spec.isBlank()) {
            return builder.build();
        }

        Map<String, String> options = parseOptions(spec);
        applyOptions(builder, options);

        return builder.build();
    }

    /**
     * Parse a specification string into a CacheConfig.Builder
     * 解析规范字符串为 CacheConfig.Builder
     *
     * <p>This allows further customization after parsing.</p>
     * <p>这允许解析后进一步自定义。</p>
     *
     * @param spec the specification string | 规范字符串
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return configured builder | 配置好的构建器
     * @throws OpenCacheException if the specification is invalid | 规范无效时抛出异常
     */
    public static <K, V> CacheConfig.Builder<K, V> parseToBuilder(String spec) {
        Objects.requireNonNull(spec, "spec must not be null");

        CacheConfig.Builder<K, V> builder = CacheConfig.builder();

        if (!spec.isBlank()) {
            Map<String, String> options = parseOptions(spec);
            applyOptions(builder, options);
        }

        return builder;
    }

    /**
     * Validate a specification string without parsing
     * 验证规范字符串而不解析
     *
     * @param spec the specification string | 规范字符串
     * @return true if valid, false otherwise | 有效返回 true
     */
    public static boolean isValid(String spec) {
        if (spec == null || spec.isBlank()) {
            return true;
        }
        try {
            parse(spec);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get validation errors for a specification string
     * 获取规范字符串的验证错误
     *
     * @param spec the specification string | 规范字符串
     * @return list of error messages, empty if valid | 错误消息列表，有效时为空
     */
    public static java.util.List<String> validate(String spec) {
        java.util.List<String> errors = new java.util.ArrayList<>();

        if (spec == null) {
            errors.add("Specification string is null");
            return errors;
        }

        if (spec.isBlank()) {
            return errors; // Empty spec is valid
        }

        try {
            Map<String, String> options = parseOptions(spec);

            // Check for unknown keys
            for (String key : options.keySet()) {
                if (!KNOWN_KEYS.contains(key)) {
                    errors.add("Unknown option: " + key);
                }
            }

            // Validate individual values
            validateOptionValues(options, errors);

        } catch (Exception e) {
            errors.add("Parse error: " + e.getMessage());
        }

        return errors;
    }

    // ==================== Build Spec String | 构建规范字符串 ====================

    /**
     * Build a specification string from a CacheConfig
     * 从 CacheConfig 构建规范字符串
     *
     * @param config the cache config | 缓存配置
     * @param <K>    key type | 键类型
     * @param <V>    value type | 值类型
     * @return specification string | 规范字符串
     */
    public static <K, V> String toSpec(CacheConfig<K, V> config) {
        Objects.requireNonNull(config, "config must not be null");

        StringBuilder sb = new StringBuilder();

        if (config.maximumSize() > 0) {
            appendOption(sb, "maximumSize", config.maximumSize());
        }
        if (config.maximumWeight() > 0) {
            appendOption(sb, "maximumWeight", config.maximumWeight());
        }
        if (config.initialCapacity() > 0) {
            appendOption(sb, "initialCapacity", config.initialCapacity());
        }
        if (config.concurrencyLevel() != 16) { // Only include if non-default
            appendOption(sb, "concurrencyLevel", config.concurrencyLevel());
        }
        if (config.expireAfterWrite() != null) {
            appendOption(sb, "expireAfterWrite", formatDuration(config.expireAfterWrite()));
        }
        if (config.expireAfterAccess() != null) {
            appendOption(sb, "expireAfterAccess", formatDuration(config.expireAfterAccess()));
        }
        if (config.refreshAfterWrite() != null) {
            appendOption(sb, "refreshAfterWrite", formatDuration(config.refreshAfterWrite()));
        }
        if (config.evictionPolicy() != null) {
            String policyName = getEvictionPolicyName(config.evictionPolicy());
            if (policyName != null) {
                appendOption(sb, "evictionPolicy", policyName);
            }
        }
        if (config.recordStats()) {
            appendFlag(sb, "recordStats");
        }
        if (config.useVirtualThreads()) {
            appendFlag(sb, "useVirtualThreads");
        }

        return sb.toString();
    }

    // ==================== Internal Methods | 内部方法 ====================

    /**
     * Parse options from specification string
     */
    private static Map<String, String> parseOptions(String spec) {
        Map<String, String> options = new LinkedHashMap<>();

        String[] parts = spec.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            int eqIndex = trimmed.indexOf('=');
            if (eqIndex > 0) {
                // key=value pair
                String key = trimmed.substring(0, eqIndex).trim();
                String value = trimmed.substring(eqIndex + 1).trim();
                if (key.isEmpty()) {
                    throw new OpenCacheException("Empty key in specification: " + trimmed);
                }
                options.put(key, value);
            } else {
                // Flag (no value)
                options.put(trimmed, "");
            }
        }

        return options;
    }

    /**
     * Apply parsed options to builder
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <K, V> void applyOptions(CacheConfig.Builder<K, V> builder, Map<String, String> options) {
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                switch (key) {
                    case "maximumSize" -> {
                        long size = parseLong(key, value);
                        if (size < 0) {
                            throw new OpenCacheException("maximumSize cannot be negative: " + value);
                        }
                        builder.maximumSize(size);
                    }
                    case "maximumWeight" -> {
                        long weight = parseLong(key, value);
                        if (weight < 0) {
                            throw new OpenCacheException("maximumWeight cannot be negative: " + value);
                        }
                        builder.maximumWeight(weight);
                    }
                    case "initialCapacity" -> {
                        int cap = parseInt(key, value);
                        if (cap < 0) {
                            throw new OpenCacheException("initialCapacity cannot be negative: " + value);
                        }
                        builder.initialCapacity(cap);
                    }
                    case "concurrencyLevel" -> {
                        int level = parseInt(key, value);
                        if (level < 1) {
                            throw new OpenCacheException("concurrencyLevel must be at least 1: " + value);
                        }
                        builder.concurrencyLevel(level);
                    }
                    case "expireAfterWrite" -> {
                        Duration d = parseDuration(key, value);
                        if (d.isNegative()) {
                            throw new OpenCacheException("expireAfterWrite cannot be negative: " + value);
                        }
                        builder.expireAfterWrite(d);
                    }
                    case "expireAfterAccess" -> {
                        Duration d = parseDuration(key, value);
                        if (d.isNegative()) {
                            throw new OpenCacheException("expireAfterAccess cannot be negative: " + value);
                        }
                        builder.expireAfterAccess(d);
                    }
                    case "refreshAfterWrite" -> {
                        Duration d = parseDuration(key, value);
                        if (d.isNegative()) {
                            throw new OpenCacheException("refreshAfterWrite cannot be negative: " + value);
                        }
                        builder.refreshAfterWrite(d);
                    }
                    case "evictionPolicy" -> {
                        // Extract maximumSize if present so W-TinyLFU can size its sketch
                        long maxSize = options.containsKey("maximumSize")
                                ? parseLong("maximumSize", options.get("maximumSize"))
                                : 10_000;
                        builder.evictionPolicy((EvictionPolicy) parseEvictionPolicy(value,
                                (int) Math.min(maxSize, Integer.MAX_VALUE)));
                    }
                    case "recordStats" -> builder.recordStats();
                    case "useVirtualThreads" -> builder.useVirtualThreads();
                    default -> throw new OpenCacheException("Unknown option: " + key);
                }
            } catch (OpenCacheException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenCacheException("Invalid value for '" + key + "': " + value, e);
            }
        }
    }

    /**
     * Validate option values
     */
    private static void validateOptionValues(Map<String, String> options, java.util.List<String> errors) {
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                switch (key) {
                    case "maximumSize", "maximumWeight" -> {
                        long l = parseLong(key, value);
                        if (l < 0) errors.add(key + " cannot be negative: " + value);
                    }
                    case "initialCapacity", "concurrencyLevel" -> {
                        int i = parseInt(key, value);
                        if (i < 0) errors.add(key + " cannot be negative: " + value);
                    }
                    case "expireAfterWrite", "expireAfterAccess", "refreshAfterWrite" -> {
                        Duration d = parseDuration(key, value);
                        if (d.isNegative()) errors.add(key + " cannot be negative: " + value);
                    }
                    case "evictionPolicy" -> parseEvictionPolicy(value, 10_000);
                    case "recordStats", "useVirtualThreads" -> {
                        // Flags, no value validation needed
                    }
                }
            } catch (Exception e) {
                errors.add("Invalid " + key + ": " + e.getMessage());
            }
        }
    }

    /**
     * Parse long value
     */
    private static long parseLong(String key, String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new OpenCacheException("Invalid long value for '" + key + "': " + value);
        }
    }

    /**
     * Parse int value
     */
    private static int parseInt(String key, String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new OpenCacheException("Invalid int value for '" + key + "': " + value);
        }
    }

    /**
     * Parse duration value with unit suffix
     * Supports: ms (milliseconds), s (seconds), m (minutes), h (hours), d (days)
     * Default unit is milliseconds
     */
    private static Duration parseDuration(String key, String value) {
        String trimmed = value.trim().toLowerCase();
        Matcher matcher = DURATION_PATTERN.matcher(trimmed);

        if (!matcher.matches()) {
            throw new OpenCacheException(
                    "Invalid duration format for '" + key + "': " + value +
                            ". Expected format: <number>[ms|s|m|h|d]");
        }

        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);

        return switch (unit) {
            case "", "ms" -> Duration.ofMillis(amount);
            case "s" -> Duration.ofSeconds(amount);
            case "m" -> Duration.ofMinutes(amount);
            case "h" -> Duration.ofHours(amount);
            case "d" -> Duration.ofDays(amount);
            default -> throw new OpenCacheException(
                    "Unknown duration unit for '" + key + "': " + unit +
                            ". Supported: ms, s, m, h, d");
        };
    }

    /**
     * Parse eviction policy name
     */
    private static <K, V> EvictionPolicy<K, V> parseEvictionPolicy(String value, int expectedSize) {
        String trimmed = value.trim().toLowerCase();

        return switch (trimmed) {
            case "lru" -> EvictionPolicy.lru();
            case "lfu" -> EvictionPolicy.lfu();
            case "fifo" -> EvictionPolicy.fifo();
            case "wtinylfu", "w-tinylfu", "tinylfu" -> EvictionPolicy.wTinyLfu(expectedSize);
            default -> throw new OpenCacheException(
                    "Unknown eviction policy: " + value +
                            ". Supported: lru, lfu, fifo, wtinylfu");
        };
    }

    /**
     * Get eviction policy name from policy instance
     */
    private static <K, V> String getEvictionPolicyName(EvictionPolicy<K, V> policy) {
        String className = policy.getClass().getSimpleName().toLowerCase();
        if (className.contains("lru")) return "lru";
        if (className.contains("lfu") && !className.contains("tiny")) return "lfu";
        if (className.contains("fifo")) return "fifo";
        if (className.contains("tiny")) return "wtinylfu";
        return null;
    }

    /**
     * Format duration to compact string
     */
    private static String formatDuration(Duration duration) {
        long millis = duration.toMillis();

        // Check for exact day multiple
        if (millis % (24 * 60 * 60 * 1000) == 0 && millis >= 24 * 60 * 60 * 1000) {
            return (millis / (24 * 60 * 60 * 1000)) + "d";
        }
        // Check for exact hour multiple
        if (millis % (60 * 60 * 1000) == 0 && millis >= 60 * 60 * 1000) {
            return (millis / (60 * 60 * 1000)) + "h";
        }
        // Check for exact minute multiple
        if (millis % (60 * 1000) == 0 && millis >= 60 * 1000) {
            return (millis / (60 * 1000)) + "m";
        }
        // Check for exact second multiple
        if (millis % 1000 == 0 && millis >= 1000) {
            return (millis / 1000) + "s";
        }
        // Use milliseconds
        return millis + "ms";
    }

    /**
     * Append key=value option to StringBuilder
     */
    private static void appendOption(StringBuilder sb, String key, Object value) {
        if (sb.length() > 0) {
            sb.append(",");
        }
        sb.append(key).append("=").append(value);
    }

    /**
     * Append flag (no value) to StringBuilder
     */
    private static void appendFlag(StringBuilder sb, String key) {
        if (sb.length() > 0) {
            sb.append(",");
        }
        sb.append(key);
    }
}
