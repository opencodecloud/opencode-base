package cloud.opencode.base.yml.placeholder;

import cloud.opencode.base.yml.exception.YmlPlaceholderException;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Placeholder Resolver - Resolves ${...} placeholders in text
 * 占位符解析器 - 解析文本中的 ${...} 占位符
 *
 * <p>This class resolves placeholders like ${key} and ${key:default} in strings.</p>
 * <p>此类解析字符串中的 ${key} 和 ${key:default} 形式的占位符。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Resolve ${key} and ${key:default} placeholders - 解析 ${key} 和 ${key:default} 占位符</li>
 *   <li>Multiple property sources (system, env, custom maps) - 多属性源（系统、环境变量、自定义映射）</li>
 *   <li>Circular reference detection - 循环引用检测</li>
 *   <li>Configurable prefix, suffix, and strict mode - 可配置前缀、后缀和严格模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create resolver with properties
 * PlaceholderResolver resolver = PlaceholderResolver.create()
 *     .withSystemProperties()
 *     .withEnvironmentVariables();
 *
 * // Resolve placeholders
 * String resolved = resolver.resolve("Server: ${SERVER_HOST:localhost}");
 *
 * // Resolve in YAML
 * String yaml = resolver.resolveYaml("""
 *     server:
 *       port: ${SERVER_PORT:8080}
 *     """);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction, resolver functions should be thread-safe) - 线程安全: 是（构建后不可变，解析函数应线程安全）</li>
 *   <li>Null-safe: Yes (null input returns null) - 空值安全: 是（空输入返回 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class PlaceholderResolver {

    private static final String DEFAULT_PREFIX = "${";
    private static final String DEFAULT_SUFFIX = "}";
    private static final String DEFAULT_VALUE_SEPARATOR = ":";

    private final String prefix;
    private final String suffix;
    private final String valueSeparator;
    private final boolean strict;
    private final List<Function<String, String>> resolvers;
    private final Pattern pattern;

    private PlaceholderResolver(Builder builder) {
        this.prefix = builder.prefix;
        this.suffix = builder.suffix;
        this.valueSeparator = builder.valueSeparator;
        this.strict = builder.strict;
        this.resolvers = List.copyOf(builder.resolvers);
        this.pattern = buildPattern(prefix, suffix);
    }

    /**
     * Creates a new placeholder resolver.
     * 创建新的占位符解析器。
     *
     * @return a new resolver | 新解析器
     */
    public static PlaceholderResolver create() {
        return new Builder().build();
    }

    /**
     * Creates a resolver with the given properties.
     * 使用给定属性创建解析器。
     *
     * @param properties the properties | 属性
     * @return a new resolver | 新解析器
     */
    public static PlaceholderResolver create(Map<String, String> properties) {
        return new Builder()
            .addPropertySource("custom", properties)
            .build();
    }

    /**
     * Creates a resolver builder.
     * 创建解析器构建器。
     *
     * @return a new builder | 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Resolves placeholders in text.
     * 解析文本中的占位符。
     *
     * @param text the text | 文本
     * @return the resolved text | 解析后的文本
     */
    public String resolve(String text) {
        if (text == null || !text.contains(prefix)) {
            return text;
        }

        Set<String> visiting = new HashSet<>();
        return resolveInternal(text, visiting);
    }

    /**
     * Resolves placeholders in YAML string.
     * 解析 YAML 字符串中的占位符。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return the resolved YAML | 解析后的 YAML
     */
    public String resolveYaml(String yaml) {
        return resolve(yaml);
    }

    private static final int MAX_RESOLVE_DEPTH = 100;

    /**
     * Resolves placeholders in Map values.
     * 解析 Map 值中的占位符。
     *
     * @param map the map | Map
     * @return the resolved map | 解析后的 Map
     */
    public Map<String, Object> resolveMap(Map<String, Object> map) {
        return resolveMap(map, 0);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveMap(Map<String, Object> map, int depth) {
        if (map == null) {
            return null;
        }
        if (depth > MAX_RESOLVE_DEPTH) {
            throw new YmlPlaceholderException(
                "Maximum placeholder resolution depth (" + MAX_RESOLVE_DEPTH
                + ") exceeded; possible circular structure");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), resolveValue(entry.getValue(), depth + 1));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object resolveValue(Object value, int depth) {
        if (value instanceof String s) {
            return resolve(s);
        } else if (value instanceof Map<?, ?> map) {
            return resolveMap((Map<String, Object>) map, depth);
        } else if (value instanceof List<?> list) {
            if (depth > MAX_RESOLVE_DEPTH) {
                throw new YmlPlaceholderException(
                    "Maximum placeholder resolution depth (" + MAX_RESOLVE_DEPTH
                    + ") exceeded; possible circular structure");
            }
            List<Object> result = new ArrayList<>();
            for (Object item : list) {
                result.add(resolveValue(item, depth + 1));
            }
            return result;
        }
        return value;
    }

    private String resolveInternal(String text, Set<String> visiting) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(text, lastEnd, matcher.start());

            String placeholder = matcher.group(1);
            String resolved = resolvePlaceholder(placeholder, visiting);
            result.append(resolved);

            lastEnd = matcher.end();
        }

        result.append(text, lastEnd, text.length());
        return result.toString();
    }

    private String resolvePlaceholder(String placeholder, Set<String> visiting) {
        // Check for default value
        String key = placeholder;
        String defaultValue = null;

        int separatorIndex = placeholder.indexOf(valueSeparator);
        if (separatorIndex > 0) {
            key = placeholder.substring(0, separatorIndex);
            defaultValue = placeholder.substring(separatorIndex + valueSeparator.length());
        }

        // Detect circular reference
        if (visiting.contains(key)) {
            throw YmlPlaceholderException.circularReference(key);
        }
        visiting.add(key);

        try {
            // Try to resolve
            String value = null;
            for (Function<String, String> resolver : resolvers) {
                value = resolver.apply(key);
                if (value != null) {
                    break;
                }
            }

            if (value != null) {
                // Recursively resolve
                return resolveInternal(value, visiting);
            } else if (defaultValue != null) {
                return resolveInternal(defaultValue, visiting);
            } else if (strict) {
                throw new YmlPlaceholderException(placeholder);
            } else {
                return prefix + placeholder + suffix;
            }
        } finally {
            visiting.remove(key);
        }
    }

    private static Pattern buildPattern(String prefix, String suffix) {
        String escapedPrefix = Pattern.quote(prefix);
        String escapedSuffix = Pattern.quote(suffix);
        // Use the actual suffix character for the negated character class, not the escaped version
        String suffixChar = String.valueOf(suffix.charAt(0));
        String escapedSuffixChar = suffixChar.equals("]") ? "\\]" : Pattern.quote(suffixChar).replace("\\Q", "").replace("\\E", "");
        return Pattern.compile(escapedPrefix + "([^" + escapedSuffixChar + "]+)" + escapedSuffix);
    }

    /**
     * Placeholder Resolver Builder
     * 占位符解析器构建器
     */
    public static final class Builder {

        private String prefix = DEFAULT_PREFIX;
        private String suffix = DEFAULT_SUFFIX;
        private String valueSeparator = DEFAULT_VALUE_SEPARATOR;
        private boolean strict = false;
        private final List<Function<String, String>> resolvers = new ArrayList<>();

        private Builder() {}

        /**
         * Sets the placeholder prefix.
         * 设置占位符前缀。
         *
         * @param prefix the prefix (default: "${") | 前缀（默认："${"）
         * @return this builder | 此构建器
         */
        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Sets the placeholder suffix.
         * 设置占位符后缀。
         *
         * @param suffix the suffix (default: "}") | 后缀（默认："}"）
         * @return this builder | 此构建器
         */
        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        /**
         * Sets the default value separator.
         * 设置默认值分隔符。
         *
         * @param separator the separator (default: ":") | 分隔符（默认：":"）
         * @return this builder | 此构建器
         */
        public Builder defaultValueSeparator(String separator) {
            this.valueSeparator = separator;
            return this;
        }

        /**
         * Sets strict mode (throws exception for unresolved placeholders).
         * 设置严格模式（对未解析的占位符抛出异常）。
         *
         * @param strict whether strict mode is enabled | 是否启用严格模式
         * @return this builder | 此构建器
         */
        public Builder strict(boolean strict) {
            this.strict = strict;
            return this;
        }

        /**
         * Adds a property source.
         * 添加属性源。
         *
         * @param name   the source name | 源名称
         * @param source the properties | 属性
         * @return this builder | 此构建器
         */
        public Builder addPropertySource(String name, Map<String, String> source) {
            this.resolvers.add(source::get);
            return this;
        }

        /**
         * Adds a resolver function.
         * 添加解析函数。
         *
         * @param resolver the resolver function | 解析函数
         * @return this builder | 此构建器
         */
        public Builder addResolver(Function<String, String> resolver) {
            this.resolvers.add(resolver);
            return this;
        }

        /**
         * Adds system properties as a property source.
         * 添加系统属性作为属性源。
         *
         * @return this builder | 此构建器
         */
        public Builder withSystemProperties() {
            this.resolvers.add(System::getProperty);
            return this;
        }

        /**
         * Adds environment variables as a property source.
         * 添加环境变量作为属性源。
         *
         * @return this builder | 此构建器
         */
        public Builder withEnvironmentVariables() {
            this.resolvers.add(System::getenv);
            return this;
        }

        /**
         * Builds the resolver.
         * 构建解析器。
         *
         * @return the resolver | 解析器
         */
        public PlaceholderResolver build() {
            return new PlaceholderResolver(this);
        }
    }
}
