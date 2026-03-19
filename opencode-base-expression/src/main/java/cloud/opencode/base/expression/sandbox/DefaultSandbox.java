package cloud.opencode.base.expression.sandbox;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Default Security Sandbox
 * 默认安全沙箱
 *
 * <p>Provides configurable security constraints for expression evaluation.</p>
 * <p>为表达式求值提供可配置的安全约束。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Class-level and package-level allow/deny lists - 类级别和包级别的允许/拒绝列表</li>
 *   <li>Method-level allow/deny lists - 方法级别的允许/拒绝列表</li>
 *   <li>Configurable expression length, evaluation depth, and time limits - 可配置表达式长度、求值深度和时间限制</li>
 *   <li>Preset configurations: permissive, restrictive, standard - 预设配置: 宽松、限制、标准</li>
 *   <li>Builder pattern for custom configurations - 构建器模式用于自定义配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use standard sandbox
 * Sandbox sandbox = DefaultSandbox.standard();
 *
 * // Custom sandbox
 * Sandbox custom = DefaultSandbox.builder()
 *     .allowAllByDefault(true)
 *     .addDeniedClass("java.lang.Runtime")
 *     .addDeniedMethod("exec")
 *     .maxEvaluationTime(3000)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable after construction with defensive copies - 线程安全: 是，构造后不可变，使用防御性拷贝</li>
 *   <li>Null-safe: Yes, null class/method/property returns false - 空值安全: 是，null类/方法/属性返回false</li>
 *   <li>Deny takes priority over allow - 拒绝优先于允许</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class DefaultSandbox implements Sandbox {

    private final Set<String> allowedClasses;
    private final Set<String> deniedClasses;
    private final Set<String> allowedPackages;
    private final Set<String> deniedPackages;
    private final Set<String> allowedMethods;
    private final Set<String> deniedMethods;
    private final int maxExpressionLength;
    private final int maxEvaluationDepth;
    private final long maxEvaluationTime;
    private final boolean allowAllByDefault;

    private DefaultSandbox(Builder builder) {
        this.allowedClasses = Set.copyOf(builder.allowedClasses);
        this.deniedClasses = Set.copyOf(builder.deniedClasses);
        this.allowedPackages = Set.copyOf(builder.allowedPackages);
        this.deniedPackages = Set.copyOf(builder.deniedPackages);
        this.allowedMethods = Set.copyOf(builder.allowedMethods);
        this.deniedMethods = Set.copyOf(builder.deniedMethods);
        this.maxExpressionLength = builder.maxExpressionLength;
        this.maxEvaluationDepth = builder.maxEvaluationDepth;
        this.maxEvaluationTime = builder.maxEvaluationTime;
        this.allowAllByDefault = builder.allowAllByDefault;
    }

    /**
     * Create a permissive sandbox that allows everything
     * 创建允许所有操作的宽松沙箱
     *
     * @return the permissive sandbox | 宽松沙箱
     */
    public static DefaultSandbox permissive() {
        return builder().allowAllByDefault(true).build();
    }

    /**
     * Create a restrictive sandbox that denies by default
     * 创建默认拒绝的限制性沙箱
     *
     * @return the restrictive sandbox | 限制性沙箱
     */
    public static DefaultSandbox restrictive() {
        return builder().allowAllByDefault(false)
                .addAllowedPackage("java.lang")
                .addAllowedPackage("java.util")
                .addAllowedPackage("java.time")
                .addAllowedPackage("java.math")
                .addDeniedClass("java.lang.Runtime")
                .addDeniedClass("java.lang.ProcessBuilder")
                .addDeniedClass("java.lang.System")
                .addDeniedClass("java.lang.Class")
                .addDeniedClass("java.lang.ClassLoader")
                .addDeniedClass("java.lang.reflect.Method")
                .addDeniedClass("java.lang.reflect.Field")
                .addDeniedClass("java.lang.reflect.Constructor")
                .addDeniedMethod("getClass")
                .addDeniedMethod("forName")
                .addDeniedMethod("newInstance")
                .addDeniedMethod("invoke")
                .addDeniedMethod("exit")
                .addDeniedMethod("exec")
                .build();
    }

    /**
     * Create a standard sandbox with sensible defaults
     * 创建具有合理默认值的标准沙箱
     *
     * @return the standard sandbox | 标准沙箱
     */
    public static DefaultSandbox standard() {
        return builder().allowAllByDefault(true)
                .addDeniedClass("java.lang.Runtime")
                .addDeniedClass("java.lang.ProcessBuilder")
                .addDeniedClass("java.lang.System")
                .addDeniedClass("java.lang.Thread")
                .addDeniedClass("java.lang.ClassLoader")
                .addDeniedClass("java.lang.reflect.Method")
                .addDeniedClass("java.lang.reflect.Field")
                .addDeniedClass("java.lang.reflect.Constructor")
                .addDeniedMethod("getClass")
                .addDeniedMethod("forName")
                .addDeniedMethod("newInstance")
                .addDeniedMethod("invoke")
                .addDeniedMethod("exit")
                .addDeniedMethod("exec")
                .maxExpressionLength(10000)
                .maxEvaluationDepth(100)
                .maxEvaluationTime(5000)
                .build();
    }

    @Override
    public boolean isClassAllowed(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        String className = clazz.getName();

        // Check denied classes first (explicit deny takes priority)
        if (deniedClasses.contains(className)) {
            return false;
        }

        // Check denied packages
        for (String pkg : deniedPackages) {
            if (className.startsWith(pkg)) {
                return false;
            }
        }

        // Check allowed classes (explicit allow)
        if (allowedClasses.contains(className)) {
            return true;
        }

        // Check allowed packages
        for (String pkg : allowedPackages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }

        // Fall back to default
        return allowAllByDefault;
    }

    @Override
    public boolean isMethodAllowed(Object target, Method method) {
        if (target == null || method == null) {
            return false;
        }

        // Check if the declaring class is allowed
        if (!isClassAllowed(method.getDeclaringClass())) {
            return false;
        }

        String methodName = method.getName();
        String fullName = method.getDeclaringClass().getName() + "." + methodName;

        // Check denied methods
        if (deniedMethods.contains(methodName) || deniedMethods.contains(fullName)) {
            return false;
        }

        // Check allowed methods
        if (!allowedMethods.isEmpty()) {
            return allowedMethods.contains(methodName) || allowedMethods.contains(fullName);
        }

        return allowAllByDefault;
    }

    @Override
    public boolean isPropertyAllowed(Object target, String property) {
        if (target == null || property == null) {
            return false;
        }

        // Check if the class is allowed
        return isClassAllowed(target.getClass());
    }

    @Override
    public int getMaxExpressionLength() {
        return maxExpressionLength;
    }

    @Override
    public int getMaxEvaluationDepth() {
        return maxEvaluationDepth;
    }

    @Override
    public long getMaxEvaluationTime() {
        return maxEvaluationTime;
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
     * Builder for DefaultSandbox
     * DefaultSandbox构建器
     */
    public static class Builder {
        private final Set<String> allowedClasses = new HashSet<>();
        private final Set<String> deniedClasses = new HashSet<>();
        private final Set<String> allowedPackages = new HashSet<>();
        private final Set<String> deniedPackages = new HashSet<>();
        private final Set<String> allowedMethods = new HashSet<>();
        private final Set<String> deniedMethods = new HashSet<>();
        private int maxExpressionLength = -1;
        private int maxEvaluationDepth = 100;
        private long maxEvaluationTime = -1;
        private boolean allowAllByDefault = true;

        /**
         * Add allowed class
         * 添加允许的类
         *
         * @param className the class name | 类名
         * @return this builder | 此构建器
         */
        public Builder addAllowedClass(String className) {
            allowedClasses.add(className);
            return this;
        }

        /**
         * Add allowed class
         * 添加允许的类
         *
         * @param clazz the class | 类
         * @return this builder | 此构建器
         */
        public Builder addAllowedClass(Class<?> clazz) {
            allowedClasses.add(clazz.getName());
            return this;
        }

        /**
         * Add denied class
         * 添加拒绝的类
         *
         * @param className the class name | 类名
         * @return this builder | 此构建器
         */
        public Builder addDeniedClass(String className) {
            deniedClasses.add(className);
            return this;
        }

        /**
         * Add denied class
         * 添加拒绝的类
         *
         * @param clazz the class | 类
         * @return this builder | 此构建器
         */
        public Builder addDeniedClass(Class<?> clazz) {
            deniedClasses.add(clazz.getName());
            return this;
        }

        /**
         * Add allowed package
         * 添加允许的包
         *
         * @param packageName the package name | 包名
         * @return this builder | 此构建器
         */
        public Builder addAllowedPackage(String packageName) {
            allowedPackages.add(packageName);
            return this;
        }

        /**
         * Add denied package
         * 添加拒绝的包
         *
         * @param packageName the package name | 包名
         * @return this builder | 此构建器
         */
        public Builder addDeniedPackage(String packageName) {
            deniedPackages.add(packageName);
            return this;
        }

        /**
         * Add allowed method
         * 添加允许的方法
         *
         * @param methodName the method name | 方法名
         * @return this builder | 此构建器
         */
        public Builder addAllowedMethod(String methodName) {
            allowedMethods.add(methodName);
            return this;
        }

        /**
         * Add denied method
         * 添加拒绝的方法
         *
         * @param methodName the method name | 方法名
         * @return this builder | 此构建器
         */
        public Builder addDeniedMethod(String methodName) {
            deniedMethods.add(methodName);
            return this;
        }

        /**
         * Set max expression length
         * 设置最大表达式长度
         *
         * @param length the max length | 最大长度
         * @return this builder | 此构建器
         */
        public Builder maxExpressionLength(int length) {
            this.maxExpressionLength = length;
            return this;
        }

        /**
         * Set max evaluation depth
         * 设置最大求值深度
         *
         * @param depth the max depth | 最大深度
         * @return this builder | 此构建器
         */
        public Builder maxEvaluationDepth(int depth) {
            this.maxEvaluationDepth = depth;
            return this;
        }

        /**
         * Set max evaluation time
         * 设置最大求值时间
         *
         * @param timeMs the max time in milliseconds | 最大时间（毫秒）
         * @return this builder | 此构建器
         */
        public Builder maxEvaluationTime(long timeMs) {
            this.maxEvaluationTime = timeMs;
            return this;
        }

        /**
         * Set allow all by default
         * 设置默认允许所有
         *
         * @param allow true to allow by default | true表示默认允许
         * @return this builder | 此构建器
         */
        public Builder allowAllByDefault(boolean allow) {
            this.allowAllByDefault = allow;
            return this;
        }

        /**
         * Build the sandbox
         * 构建沙箱
         *
         * @return the sandbox | 沙箱
         */
        public DefaultSandbox build() {
            return new DefaultSandbox(this);
        }
    }
}
