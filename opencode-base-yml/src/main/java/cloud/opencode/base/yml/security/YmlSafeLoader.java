package cloud.opencode.base.yml.security;

import cloud.opencode.base.yml.exception.YmlSecurityException;

import java.util.*;

/**
 * YAML Safe Loader - Provides secure YAML loading
 * YAML 安全加载器 - 提供安全的 YAML 加载
 *
 * <p>This class provides utilities for safely loading YAML without
 * arbitrary code execution risks.</p>
 * <p>此类提供安全加载 YAML 的工具，避免任意代码执行风险。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable allowed types and denied tags - 可配置的允许类型和拒绝标签</li>
 *   <li>Maximum depth, size, and alias limits - 最大深度、大小和别名限制</li>
 *   <li>Cyclic reference detection - 循环引用检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Configure allowed types
 * YmlSafeLoader loader = YmlSafeLoader.builder()
 *     .allowType(MyClass.class)
 *     .maxDepth(100)
 *     .maxSize(1_000_000)
 *     .build();
 *
 * // Validate data
 * loader.validate(data);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (null data skips validation) - 空值安全: 否（空数据跳过验证）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class YmlSafeLoader {

    private static final int DEFAULT_MAX_DEPTH = 100;
    private static final int DEFAULT_MAX_SIZE = 10_000_000;
    private static final int DEFAULT_MAX_ALIASES = 50;

    private final Set<Class<?>> allowedTypes;
    private final Set<String> deniedTags;
    private final int maxDepth;
    private final int maxSize;
    private final int maxAliases;

    private YmlSafeLoader(Builder builder) {
        this.allowedTypes = Set.copyOf(builder.allowedTypes);
        this.deniedTags = Set.copyOf(builder.deniedTags);
        this.maxDepth = builder.maxDepth;
        this.maxSize = builder.maxSize;
        this.maxAliases = builder.maxAliases;
    }

    /**
     * Creates a default safe loader.
     * 创建默认的安全加载器。
     *
     * @return a safe loader | 安全加载器
     */
    public static YmlSafeLoader create() {
        return new Builder().build();
    }

    /**
     * Creates a builder for safe loader.
     * 创建安全加载器的构建器。
     *
     * @return a new builder | 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Validates that data is safe.
     * 验证数据是否安全。
     *
     * @param data the data to validate | 要验证的数据
     * @throws YmlSecurityException if data is not safe | 如果数据不安全
     */
    public void validate(Object data) {
        validateDepth(data, 0);
    }

    /**
     * Checks if a type is allowed.
     * 检查类型是否被允许。
     *
     * @param type the type to check | 要检查的类型
     * @return true if allowed | 如果允许则返回 true
     */
    public boolean isAllowedType(Class<?> type) {
        if (isBasicType(type)) {
            return true;
        }
        return allowedTypes.contains(type);
    }

    /**
     * Checks if a tag is denied.
     * 检查标签是否被拒绝。
     *
     * @param tag the tag to check | 要检查的标签
     * @return true if denied | 如果被拒绝则返回 true
     */
    public boolean isDeniedTag(String tag) {
        return deniedTags.contains(tag);
    }

    /**
     * Gets the maximum depth.
     * 获取最大深度。
     *
     * @return the max depth | 最大深度
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Gets the maximum size.
     * 获取最大大小。
     *
     * @return the max size | 最大大小
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Gets the maximum aliases.
     * 获取最大别名数。
     *
     * @return the max aliases | 最大别名数
     */
    public int getMaxAliases() {
        return maxAliases;
    }

    private void validateDepth(Object data, int depth) {
        validateDepth(data, depth, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private void validateDepth(Object data, int depth, Set<Object> visited) {
        if (depth > maxDepth) {
            throw YmlSecurityException.nestingDepthExceeded(depth, maxDepth);
        }

        if (data instanceof Map<?, ?> map) {
            if (!visited.add(map)) {
                throw new YmlSecurityException(YmlSecurityException.SecurityViolationType.RECURSIVE_REFERENCE,
                        "Cyclic reference detected in YAML structure");
            }
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                validateDepth(entry.getValue(), depth + 1, visited);
            }
        } else if (data instanceof List<?> list) {
            if (!visited.add(list)) {
                throw new YmlSecurityException(YmlSecurityException.SecurityViolationType.RECURSIVE_REFERENCE,
                        "Cyclic reference detected in YAML structure");
            }
            for (Object item : list) {
                validateDepth(item, depth + 1, visited);
            }
        }
    }

    private boolean isBasicType(Class<?> type) {
        return type.isPrimitive() ||
               type == String.class ||
               type == Integer.class ||
               type == Long.class ||
               type == Double.class ||
               type == Float.class ||
               type == Boolean.class ||
               type == Short.class ||
               type == Byte.class ||
               type == Character.class ||
               Map.class.isAssignableFrom(type) ||
               List.class.isAssignableFrom(type) ||
               Set.class.isAssignableFrom(type);
    }

    /**
     * Safe Loader Builder
     * 安全加载器构建器
     */
    public static final class Builder {

        private final Set<Class<?>> allowedTypes = new HashSet<>();
        private final Set<String> deniedTags = new HashSet<>();
        private int maxDepth = DEFAULT_MAX_DEPTH;
        private int maxSize = DEFAULT_MAX_SIZE;
        private int maxAliases = DEFAULT_MAX_ALIASES;

        private Builder() {
            // Default denied tags (potentially dangerous)
            deniedTags.add("!!java/object");
            deniedTags.add("!!java/class");
            deniedTags.add("!!javax/script");
            deniedTags.add("tag:yaml.org,2002:java/object");
        }

        /**
         * Allows a type for deserialization.
         * 允许反序列化的类型。
         *
         * @param type the type to allow | 要允许的类型
         * @return this builder | 此构建器
         */
        public Builder allowType(Class<?> type) {
            this.allowedTypes.add(type);
            return this;
        }

        /**
         * Allows multiple types for deserialization.
         * 允许多个反序列化的类型。
         *
         * @param types the types to allow | 要允许的类型
         * @return this builder | 此构建器
         */
        public Builder allowTypes(Class<?>... types) {
            this.allowedTypes.addAll(Arrays.asList(types));
            return this;
        }

        /**
         * Denies a YAML tag.
         * 拒绝 YAML 标签。
         *
         * @param tag the tag to deny | 要拒绝的标签
         * @return this builder | 此构建器
         */
        public Builder denyTag(String tag) {
            this.deniedTags.add(tag);
            return this;
        }

        /**
         * Sets the maximum nesting depth.
         * 设置最大嵌套深度。
         *
         * @param maxDepth the max depth | 最大深度
         * @return this builder | 此构建器
         */
        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        /**
         * Sets the maximum document size.
         * 设置最大文档大小。
         *
         * @param maxSize the max size | 最大大小
         * @return this builder | 此构建器
         */
        public Builder maxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        /**
         * Sets the maximum number of aliases.
         * 设置最大别名数。
         *
         * @param maxAliases the max aliases | 最大别名数
         * @return this builder | 此构建器
         */
        public Builder maxAliases(int maxAliases) {
            this.maxAliases = maxAliases;
            return this;
        }

        /**
         * Builds the safe loader.
         * 构建安全加载器。
         *
         * @return the safe loader | 安全加载器
         */
        public YmlSafeLoader build() {
            return new YmlSafeLoader(this);
        }
    }
}
