package cloud.opencode.base.expression.sandbox;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Allow List
 * 白名单
 *
 * <p>A flexible whitelist for controlling access to classes, methods, and properties.
 * Supports exact matches and wildcard patterns.</p>
 * <p>用于控制对类、方法和属性访问的灵活白名单。支持精确匹配和通配符模式。</p>
 *
 * <h2>Usage | 用法</h2>
 * <pre>{@code
 * AllowList allowList = AllowList.builder()
 *     .allowClass("java.lang.String")
 *     .allowClass("java.util.*")  // All classes in java.util
 *     .allowMethod("get*")        // All methods starting with "get"
 *     .allowProperty("name")
 *     .build();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exact match and wildcard pattern support for classes, methods, properties - 类、方法、属性的精确匹配和通配符模式支持</li>
 *   <li>Configurable default allow/deny policy - 可配置的默认允许/拒绝策略</li>
 *   <li>Immutable after construction - 构造后不可变</li>
 *   <li>Factory methods for empty and allow-all lists - 空列表和允许所有列表的工厂方法</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable after construction with defensive copies - 线程安全: 是，构造后不可变，使用防御性拷贝</li>
 *   <li>Null-safe: Yes, null names return false - 空值安全: 是，null名称返回false</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AllowList allowList = AllowList.builder()
 *     .allowClass("java.lang.String")
 *     .allowClass("java.util.*")
 *     .allowMethod("get*")
 *     .allowProperty("name")
 *     .build();
 * boolean allowed = allowList.isClassAllowed("java.lang.String");  // true
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class AllowList {

    private final Set<String> allowedClasses;
    private final Set<String> allowedMethods;
    private final Set<String> allowedProperties;
    private final List<Pattern> classPatterns;
    private final List<Pattern> methodPatterns;
    private final List<Pattern> propertyPatterns;
    private final boolean allowAllByDefault;

    private AllowList(Builder builder) {
        this.allowedClasses = Set.copyOf(builder.allowedClasses);
        this.allowedMethods = Set.copyOf(builder.allowedMethods);
        this.allowedProperties = Set.copyOf(builder.allowedProperties);
        this.classPatterns = List.copyOf(builder.classPatterns);
        this.methodPatterns = List.copyOf(builder.methodPatterns);
        this.propertyPatterns = List.copyOf(builder.propertyPatterns);
        this.allowAllByDefault = builder.allowAllByDefault;
    }

    /**
     * Check if class is allowed
     * 检查类是否被允许
     *
     * @param className the class name | 类名
     * @return true if allowed | 如果允许返回 true
     */
    public boolean isClassAllowed(String className) {
        if (className == null) {
            return false;
        }

        // Check exact match
        if (allowedClasses.contains(className)) {
            return true;
        }

        // Check patterns
        for (Pattern pattern : classPatterns) {
            if (pattern.matcher(className).matches()) {
                return true;
            }
        }

        return allowAllByDefault && allowedClasses.isEmpty() && classPatterns.isEmpty();
    }

    /**
     * Check if class is allowed
     * 检查类是否被允许
     *
     * @param clazz the class | 类
     * @return true if allowed | 如果允许返回 true
     */
    public boolean isClassAllowed(Class<?> clazz) {
        return clazz != null && isClassAllowed(clazz.getName());
    }

    /**
     * Check if method is allowed
     * 检查方法是否被允许
     *
     * @param methodName the method name | 方法名
     * @return true if allowed | 如果允许返回 true
     */
    public boolean isMethodAllowed(String methodName) {
        if (methodName == null) {
            return false;
        }

        // Check exact match
        if (allowedMethods.contains(methodName)) {
            return true;
        }

        // Check patterns
        for (Pattern pattern : methodPatterns) {
            if (pattern.matcher(methodName).matches()) {
                return true;
            }
        }

        return allowAllByDefault && allowedMethods.isEmpty() && methodPatterns.isEmpty();
    }

    /**
     * Check if property is allowed
     * 检查属性是否被允许
     *
     * @param propertyName the property name | 属性名
     * @return true if allowed | 如果允许返回 true
     */
    public boolean isPropertyAllowed(String propertyName) {
        if (propertyName == null) {
            return false;
        }

        // Check exact match
        if (allowedProperties.contains(propertyName)) {
            return true;
        }

        // Check patterns
        for (Pattern pattern : propertyPatterns) {
            if (pattern.matcher(propertyName).matches()) {
                return true;
            }
        }

        return allowAllByDefault && allowedProperties.isEmpty() && propertyPatterns.isEmpty();
    }

    /**
     * Get all allowed classes
     * 获取所有允许的类
     *
     * @return the allowed classes | 允许的类
     */
    public Set<String> getAllowedClasses() {
        return allowedClasses;
    }

    /**
     * Get all allowed methods
     * 获取所有允许的方法
     *
     * @return the allowed methods | 允许的方法
     */
    public Set<String> getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * Get all allowed properties
     * 获取所有允许的属性
     *
     * @return the allowed properties | 允许的属性
     */
    public Set<String> getAllowedProperties() {
        return allowedProperties;
    }

    /**
     * Create a builder
     * 创建构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create an empty allow list (denies all)
     * 创建空白名单（拒绝所有）
     *
     * @return the empty allow list | 空白名单
     */
    public static AllowList empty() {
        return new Builder().allowAllByDefault(false).build();
    }

    /**
     * Create an allow-all list (allows everything)
     * 创建允许所有的白名单
     *
     * @return the allow-all list | 允许所有的白名单
     */
    public static AllowList allowAll() {
        return new Builder().allowAllByDefault(true).build();
    }

    /**
     * Builder for AllowList
     * AllowList 构建器
     */
    public static class Builder {
        private final Set<String> allowedClasses = new HashSet<>();
        private final Set<String> allowedMethods = new HashSet<>();
        private final Set<String> allowedProperties = new HashSet<>();
        private final List<Pattern> classPatterns = new ArrayList<>();
        private final List<Pattern> methodPatterns = new ArrayList<>();
        private final List<Pattern> propertyPatterns = new ArrayList<>();
        private boolean allowAllByDefault = false;

        /**
         * Allow a class
         * 允许一个类
         *
         * @param className the class name (supports * wildcard) | 类名（支持 * 通配符）
         * @return this builder | 此构建器
         */
        public Builder allowClass(String className) {
            if (className != null) {
                if (className.contains("*")) {
                    classPatterns.add(wildcardToPattern(className));
                } else {
                    allowedClasses.add(className);
                }
            }
            return this;
        }

        /**
         * Allow a class
         * 允许一个类
         *
         * @param clazz the class | 类
         * @return this builder | 此构建器
         */
        public Builder allowClass(Class<?> clazz) {
            if (clazz != null) {
                allowedClasses.add(clazz.getName());
            }
            return this;
        }

        /**
         * Allow multiple classes
         * 允许多个类
         *
         * @param classNames the class names | 类名
         * @return this builder | 此构建器
         */
        public Builder allowClasses(String... classNames) {
            for (String className : classNames) {
                allowClass(className);
            }
            return this;
        }

        /**
         * Allow a method
         * 允许一个方法
         *
         * @param methodName the method name (supports * wildcard) | 方法名（支持 * 通配符）
         * @return this builder | 此构建器
         */
        public Builder allowMethod(String methodName) {
            if (methodName != null) {
                if (methodName.contains("*")) {
                    methodPatterns.add(wildcardToPattern(methodName));
                } else {
                    allowedMethods.add(methodName);
                }
            }
            return this;
        }

        /**
         * Allow multiple methods
         * 允许多个方法
         *
         * @param methodNames the method names | 方法名
         * @return this builder | 此构建器
         */
        public Builder allowMethods(String... methodNames) {
            for (String methodName : methodNames) {
                allowMethod(methodName);
            }
            return this;
        }

        /**
         * Allow a property
         * 允许一个属性
         *
         * @param propertyName the property name (supports * wildcard) | 属性名（支持 * 通配符）
         * @return this builder | 此构建器
         */
        public Builder allowProperty(String propertyName) {
            if (propertyName != null) {
                if (propertyName.contains("*")) {
                    propertyPatterns.add(wildcardToPattern(propertyName));
                } else {
                    allowedProperties.add(propertyName);
                }
            }
            return this;
        }

        /**
         * Allow multiple properties
         * 允许多个属性
         *
         * @param propertyNames the property names | 属性名
         * @return this builder | 此构建器
         */
        public Builder allowProperties(String... propertyNames) {
            for (String propertyName : propertyNames) {
                allowProperty(propertyName);
            }
            return this;
        }

        /**
         * Set allow all by default
         * 设置默认允许所有
         *
         * @param allow true to allow all by default | true 表示默认允许所有
         * @return this builder | 此构建器
         */
        public Builder allowAllByDefault(boolean allow) {
            this.allowAllByDefault = allow;
            return this;
        }

        /**
         * Build the allow list
         * 构建白名单
         *
         * @return the allow list | 白名单
         */
        public AllowList build() {
            return new AllowList(this);
        }

        private Pattern wildcardToPattern(String wildcard) {
            StringBuilder regex = new StringBuilder();
            for (int i = 0; i < wildcard.length(); i++) {
                char c = wildcard.charAt(i);
                switch (c) {
                    case '*' -> regex.append(".*");
                    case '?' -> regex.append(".");
                    case '.' -> regex.append("\\.");
                    case '\\' -> regex.append("\\\\");
                    case '[', ']', '(', ')', '{', '}', '^', '$', '|', '+' -> regex.append("\\").append(c);
                    default -> regex.append(c);
                }
            }
            return Pattern.compile(regex.toString());
        }
    }
}
