package cloud.opencode.base.cache.ttl;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * TTL Policy - Advanced TTL management strategies
 * TTL 策略 - 高级 TTL 管理策略
 *
 * <p>Provides flexible TTL calculation based on various factors.</p>
 * <p>根据各种因素提供灵活的 TTL 计算。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fixed TTL - 固定 TTL</li>
 *   <li>Variable TTL by key - 按键可变 TTL</li>
 *   <li>Value-based TTL - 基于值的 TTL</li>
 *   <li>Pattern-based TTL - 基于模式的 TTL</li>
 *   <li>TTL decay - TTL 衰减</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Fixed TTL
 * TtlPolicy<String, User> fixed = TtlPolicy.fixed(Duration.ofMinutes(30));
 *
 * // Variable by key pattern
 * TtlPolicy<String, User> pattern = TtlPolicy.<String, User>builder()
 *     .pattern("session:*", Duration.ofHours(1))
 *     .pattern("user:*", Duration.ofMinutes(30))
 *     .defaultTtl(Duration.ofMinutes(10))
 *     .build();
 *
 * // Value-based TTL
 * TtlPolicy<String, User> valueBased = TtlPolicy.byValue(
 *     (key, user) -> user.isPremium() ? Duration.ofHours(1) : Duration.ofMinutes(10)
 * );
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (functional interface, implementations should be stateless) - 线程安全: 是（函数式接口，实现应无状态）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
@FunctionalInterface
public interface TtlPolicy<K, V> {

    /**
     * Calculate TTL for a cache entry
     * 计算缓存条目的 TTL
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return TTL duration, or null for no expiration | TTL 持续时间，null 表示不过期
     */
    Duration calculateTtl(K key, V value);

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a fixed TTL policy
     * 创建固定 TTL 策略
     *
     * @param ttl the fixed TTL | 固定 TTL
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return TTL policy | TTL 策略
     */
    static <K, V> TtlPolicy<K, V> fixed(Duration ttl) {
        Objects.requireNonNull(ttl, "ttl cannot be null");
        return (key, value) -> ttl;
    }

    /**
     * Create a no-expiration policy
     * 创建不过期策略
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return TTL policy | TTL 策略
     */
    static <K, V> TtlPolicy<K, V> noExpiration() {
        return (key, value) -> null;
    }

    /**
     * Create a key-based TTL policy
     * 创建基于键的 TTL 策略
     *
     * @param calculator TTL calculator function | TTL 计算函数
     * @param <K>        key type | 键类型
     * @param <V>        value type | 值类型
     * @return TTL policy | TTL 策略
     */
    static <K, V> TtlPolicy<K, V> byKey(Function<K, Duration> calculator) {
        Objects.requireNonNull(calculator, "calculator cannot be null");
        return (key, value) -> calculator.apply(key);
    }

    /**
     * Create a value-based TTL policy
     * 创建基于值的 TTL 策略
     *
     * @param calculator TTL calculator function | TTL 计算函数
     * @param <K>        key type | 键类型
     * @param <V>        value type | 值类型
     * @return TTL policy | TTL 策略
     */
    static <K, V> TtlPolicy<K, V> byValue(BiFunction<K, V, Duration> calculator) {
        Objects.requireNonNull(calculator, "calculator cannot be null");
        return calculator::apply;
    }

    /**
     * Create a builder for pattern-based TTL
     * 创建基于模式的 TTL 构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return builder | 构建器
     */
    static <K, V> PatternBuilder<K, V> builder() {
        return new PatternBuilder<>();
    }

    // ==================== Composition | 组合 ====================

    /**
     * Create a policy with minimum TTL
     * 创建具有最小 TTL 的策略
     *
     * @param minTtl minimum TTL | 最小 TTL
     * @return new policy | 新策略
     */
    default TtlPolicy<K, V> withMinimum(Duration minTtl) {
        return (key, value) -> {
            Duration calculated = calculateTtl(key, value);
            if (calculated == null) return null;
            return calculated.compareTo(minTtl) < 0 ? minTtl : calculated;
        };
    }

    /**
     * Create a policy with maximum TTL
     * 创建具有最大 TTL 的策略
     *
     * @param maxTtl maximum TTL | 最大 TTL
     * @return new policy | 新策略
     */
    default TtlPolicy<K, V> withMaximum(Duration maxTtl) {
        return (key, value) -> {
            Duration calculated = calculateTtl(key, value);
            if (calculated == null) return maxTtl;
            return calculated.compareTo(maxTtl) > 0 ? maxTtl : calculated;
        };
    }

    /**
     * Create a policy that adds jitter to prevent thundering herd
     * 创建添加抖动以防止惊群效应的策略
     *
     * @param jitterPercent jitter percentage (0.0 to 1.0) | 抖动百分比（0.0 到 1.0）
     * @return new policy | 新策略
     */
    default TtlPolicy<K, V> withJitter(double jitterPercent) {
        return (key, value) -> {
            Duration calculated = calculateTtl(key, value);
            if (calculated == null) return null;
            double jitter = 1.0 + (Math.random() * 2 - 1) * jitterPercent;
            return Duration.ofMillis((long) (calculated.toMillis() * jitter));
        };
    }

    // ==================== Pattern Builder | 模式构建器 ====================

    /**
     * Builder for pattern-based TTL policies
     * 基于模式的 TTL 策略构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    class PatternBuilder<K, V> {

        /** Creates a new PatternBuilder instance | 创建新的 PatternBuilder 实例 */
        public PatternBuilder() {}
        private final java.util.List<PatternRule> rules = new java.util.ArrayList<>();
        private Duration defaultTtl = Duration.ofMinutes(10);

        /**
         * Add a pattern rule (glob-style)
         * 添加模式规则（glob 风格）
         *
         * @param pattern glob pattern (e.g., "user:*") | glob 模式
         * @param ttl     TTL for matching keys | 匹配键的 TTL
         * @return this builder | 此构建器
         */
        public PatternBuilder<K, V> pattern(String pattern, Duration ttl) {
            String regex = pattern
                    .replace(".", "\\.")
                    .replace("*", ".*")
                    .replace("?", ".");
            rules.add(new PatternRule(Pattern.compile(regex), ttl));
            return this;
        }

        /**
         * Add a regex pattern rule
         * 添加正则表达式模式规则
         *
         * @param regex regex pattern | 正则表达式模式
         * @param ttl   TTL for matching keys | 匹配键的 TTL
         * @return this builder | 此构建器
         */
        public PatternBuilder<K, V> regex(String regex, Duration ttl) {
            rules.add(new PatternRule(Pattern.compile(regex), ttl));
            return this;
        }

        /**
         * Set default TTL for non-matching keys
         * 设置不匹配键的默认 TTL
         *
         * @param ttl default TTL | 默认 TTL
         * @return this builder | 此构建器
         */
        public PatternBuilder<K, V> defaultTtl(Duration ttl) {
            this.defaultTtl = ttl;
            return this;
        }

        /**
         * Build the TTL policy
         * 构建 TTL 策略
         *
         * @return TTL policy | TTL 策略
         */
        public TtlPolicy<K, V> build() {
            var rulesCopy = new java.util.ArrayList<>(rules);
            Duration defTtl = defaultTtl;
            return (key, value) -> {
                String keyStr = String.valueOf(key);
                for (PatternRule rule : rulesCopy) {
                    if (rule.pattern.matcher(keyStr).matches()) {
                        return rule.ttl;
                    }
                }
                return defTtl;
            };
        }

        private record PatternRule(Pattern pattern, Duration ttl) {}
    }
}
