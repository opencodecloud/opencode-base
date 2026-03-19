package cloud.opencode.base.expression.sandbox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Security Policy
 * 安全策略
 *
 * <p>Defines security constraints for expression evaluation including allowed classes,
 * methods, timeout limits, and iteration limits.</p>
 * <p>定义表达式求值的安全约束，包括允许的类、方法、超时限制和迭代限制。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Class, method, and function allow/deny lists - 类、方法和函数的允许/拒绝列表</li>
 *   <li>Timeout, iteration, and expression length limits - 超时、迭代和表达式长度限制</li>
 *   <li>Preset policies: strict and lenient - 预设策略: 严格和宽松</li>
 *   <li>Builder pattern for custom policies - 构建器模式用于自定义策略</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use strict policy
 * SecurityPolicy policy = SecurityPolicy.strict();
 * boolean allowed = policy.isClassAllowed(String.class);  // true
 * boolean denied = policy.isMethodAllowed("getClass");  // false
 *
 * // Custom policy
 * SecurityPolicy custom = SecurityPolicy.builder()
 *     .allowClass(String.class, Integer.class)
 *     .denyMethod("getClass", "forName")
 *     .timeout(10000)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: Yes, null class returns false - 空值安全: 是，null类返回false</li>
 *   <li>Deny takes priority over allow - 拒绝优先于允许</li>
 * </ul>
 *
 * @param allowedClasses the set of allowed class names | 允许的类名集合
 * @param deniedClasses the set of denied class names | 拒绝的类名集合
 * @param allowedMethods the set of allowed method names | 允许的方法名集合
 * @param deniedMethods the set of denied method names | 拒绝的方法名集合
 * @param allowedFunctions the set of allowed function names | 允许的函数名集合
 * @param deniedFunctions the set of denied function names | 拒绝的函数名集合
 * @param timeoutMillis the maximum execution time in milliseconds | 最大执行时间（毫秒）
 * @param maxIterations the maximum number of iterations | 最大迭代次数
 * @param maxExpressionLength the maximum expression length | 最大表达式长度
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record SecurityPolicy(
        Set<Class<?>> allowedClasses,
        Set<String> deniedClasses,
        Set<String> allowedMethods,
        Set<String> deniedMethods,
        Set<String> allowedFunctions,
        Set<String> deniedFunctions,
        long timeoutMillis,
        int maxIterations,
        int maxExpressionLength
) {

    /**
     * Create a strict security policy
     * 创建严格安全策略
     *
     * <p>Only allows basic types and common operations.</p>
     * <p>仅允许基本类型和常见操作。</p>
     *
     * @return the strict policy | 严格策略
     */
    public static SecurityPolicy strict() {
        return new SecurityPolicy(
                Set.of(String.class, Integer.class, Long.class, Double.class, Float.class,
                        Boolean.class, Short.class, Byte.class, Character.class,
                        List.class, ArrayList.class, LinkedList.class,
                        Map.class, HashMap.class, LinkedHashMap.class, TreeMap.class,
                        Set.class, HashSet.class, LinkedHashSet.class, TreeSet.class,
                        LocalDate.class, LocalDateTime.class),
                Set.of(), // No denied classes by default
                Set.of(), // Empty means allow all registered methods
                Set.of("getClass", "wait", "notify", "notifyAll", "clone",
                        "finalize", "forName", "newInstance", "invoke"),
                Set.of(), // Empty means allow all registered functions
                Set.of(),
                5000,  // 5 second timeout
                10000, // Max 10,000 iterations
                10000  // Max 10,000 character expression
        );
    }

    /**
     * Create a lenient security policy
     * 创建宽松安全策略
     *
     * <p>Allows most operations with minimal restrictions.</p>
     * <p>允许大多数操作，限制最小。</p>
     *
     * @return the lenient policy | 宽松策略
     */
    public static SecurityPolicy lenient() {
        return new SecurityPolicy(
                Set.of(), // Empty means allow all classes
                Set.of("java.lang.Runtime", "java.lang.ProcessBuilder",
                        "java.lang.ClassLoader", "java.lang.reflect.Method"),
                Set.of(),
                Set.of("getClass", "forName", "newInstance", "invoke", "exit", "exec"),
                Set.of(),
                Set.of(),
                30000,  // 30 second timeout
                100000, // Max 100,000 iterations
                100000  // Max 100,000 character expression
        );
    }

    /**
     * Create a custom security policy using builder
     * 使用构建器创建自定义安全策略
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Check if a class is allowed
     * 检查类是否被允许
     *
     * @param clazz the class | 类
     * @return true if allowed | 如果允许返回 true
     */
    public boolean isClassAllowed(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        String className = clazz.getName();

        // Check denied first
        if (deniedClasses.contains(className)) {
            return false;
        }

        // If allowed list is empty, allow all
        if (allowedClasses.isEmpty()) {
            return true;
        }

        // Check allowed
        return allowedClasses.contains(clazz);
    }

    /**
     * Check if a method is allowed
     * 检查方法是否被允许
     *
     * @param methodName the method name | 方法名
     * @return true if allowed | 如果允许返回 true
     */
    public boolean isMethodAllowed(String methodName) {
        // Check denied first
        if (deniedMethods.contains(methodName)) {
            return false;
        }

        // If allowed list is empty, allow all
        if (allowedMethods.isEmpty()) {
            return true;
        }

        // Check allowed
        return allowedMethods.contains(methodName);
    }

    /**
     * Check if a function is allowed
     * 检查函数是否被允许
     *
     * @param functionName the function name | 函数名
     * @return true if allowed | 如果允许返回 true
     */
    public boolean isFunctionAllowed(String functionName) {
        // Check denied first
        if (deniedFunctions.contains(functionName)) {
            return false;
        }

        // If allowed list is empty, allow all
        if (allowedFunctions.isEmpty()) {
            return true;
        }

        // Check allowed
        return allowedFunctions.contains(functionName);
    }

    /**
     * Builder for SecurityPolicy
     * SecurityPolicy 构建器
     */
    public static class Builder {
        private final Set<Class<?>> allowedClasses = new HashSet<>();
        private final Set<String> deniedClasses = new HashSet<>();
        private final Set<String> allowedMethods = new HashSet<>();
        private final Set<String> deniedMethods = new HashSet<>();
        private final Set<String> allowedFunctions = new HashSet<>();
        private final Set<String> deniedFunctions = new HashSet<>();
        private long timeoutMillis = 5000;
        private int maxIterations = 10000;
        private int maxExpressionLength = 10000;

        /**
         * Allow a class
         * 允许一个类
         *
         * @param classes the classes | 类
         * @return this builder | 此构建器
         */
        public Builder allowClass(Class<?>... classes) {
            allowedClasses.addAll(Arrays.asList(classes));
            return this;
        }

        /**
         * Deny a class
         * 拒绝一个类
         *
         * @param classNames the class names | 类名
         * @return this builder | 此构建器
         */
        public Builder denyClass(String... classNames) {
            deniedClasses.addAll(Arrays.asList(classNames));
            return this;
        }

        /**
         * Allow a method
         * 允许一个方法
         *
         * @param methods the method names | 方法名
         * @return this builder | 此构建器
         */
        public Builder allowMethod(String... methods) {
            allowedMethods.addAll(Arrays.asList(methods));
            return this;
        }

        /**
         * Deny a method
         * 拒绝一个方法
         *
         * @param methods the method names | 方法名
         * @return this builder | 此构建器
         */
        public Builder denyMethod(String... methods) {
            deniedMethods.addAll(Arrays.asList(methods));
            return this;
        }

        /**
         * Allow a function
         * 允许一个函数
         *
         * @param functions the function names | 函数名
         * @return this builder | 此构建器
         */
        public Builder allowFunction(String... functions) {
            allowedFunctions.addAll(Arrays.asList(functions));
            return this;
        }

        /**
         * Deny a function
         * 拒绝一个函数
         *
         * @param functions the function names | 函数名
         * @return this builder | 此构建器
         */
        public Builder denyFunction(String... functions) {
            deniedFunctions.addAll(Arrays.asList(functions));
            return this;
        }

        /**
         * Set timeout
         * 设置超时
         *
         * @param millis the timeout in milliseconds | 超时毫秒数
         * @return this builder | 此构建器
         */
        public Builder timeout(long millis) {
            this.timeoutMillis = millis;
            return this;
        }

        /**
         * Set max iterations
         * 设置最大迭代次数
         *
         * @param max the max iterations | 最大迭代次数
         * @return this builder | 此构建器
         */
        public Builder maxIterations(int max) {
            this.maxIterations = max;
            return this;
        }

        /**
         * Set max expression length
         * 设置最大表达式长度
         *
         * @param max the max length | 最大长度
         * @return this builder | 此构建器
         */
        public Builder maxExpressionLength(int max) {
            this.maxExpressionLength = max;
            return this;
        }

        /**
         * Build the policy
         * 构建策略
         *
         * @return the policy | 策略
         */
        public SecurityPolicy build() {
            return new SecurityPolicy(
                    Set.copyOf(allowedClasses),
                    Set.copyOf(deniedClasses),
                    Set.copyOf(allowedMethods),
                    Set.copyOf(deniedMethods),
                    Set.copyOf(allowedFunctions),
                    Set.copyOf(deniedFunctions),
                    timeoutMillis,
                    maxIterations,
                    maxExpressionLength
            );
        }
    }
}
