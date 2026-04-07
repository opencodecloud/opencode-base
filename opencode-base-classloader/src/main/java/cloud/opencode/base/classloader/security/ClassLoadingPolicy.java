package cloud.opencode.base.classloader.security;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable policy governing which classes may be loaded
 * 控制哪些类可以被加载的不可变策略
 *
 * <p>Provides a declarative, builder-based approach to define allowed and denied packages,
 * class count limits, bytecode size limits, and optional custom bytecode verification.
 * Use {@link #builder()} to create instances.</p>
 *
 * <p>提供声明式的、基于构建器的方式来定义允许和拒绝的包、类数量限制、
 * 字节码大小限制和可选的自定义字节码验证。使用 {@link #builder()} 创建实例。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public final class ClassLoadingPolicy {

    private final Set<String> allowedPackages;
    private final Set<String> deniedPackages;
    private final int maxLoadedClasses;
    private final int maxBytecodeSize;
    private final BytecodeVerifier bytecodeVerifier;

    private ClassLoadingPolicy(Builder builder) {
        this.allowedPackages = Collections.unmodifiableSet(new LinkedHashSet<>(builder.allowedPackages));
        this.deniedPackages = Collections.unmodifiableSet(new LinkedHashSet<>(builder.deniedPackages));
        this.maxLoadedClasses = builder.maxLoadedClasses;
        this.maxBytecodeSize = builder.maxBytecodeSize;
        this.bytecodeVerifier = builder.bytecodeVerifier;
    }

    /**
     * Create a new Builder for ClassLoadingPolicy
     * 创建 ClassLoadingPolicy 的新 Builder
     *
     * @return a new builder instance | 新的构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get the set of allowed packages (whitelist)
     * 获取允许的包集合（白名单）
     *
     * @return unmodifiable set of allowed package prefixes | 不可修改的允许包前缀集合
     */
    public Set<String> allowedPackages() {
        return allowedPackages;
    }

    /**
     * Get the set of denied packages (blacklist)
     * 获取拒绝的包集合（黑名单）
     *
     * @return unmodifiable set of denied package prefixes | 不可修改的拒绝包前缀集合
     */
    public Set<String> deniedPackages() {
        return deniedPackages;
    }

    /**
     * Get the maximum number of loaded classes allowed
     * 获取允许的最大已加载类数量
     *
     * @return max loaded classes, or 0 if unlimited | 最大已加载类数，0 表示无限制
     */
    public int maxLoadedClasses() {
        return maxLoadedClasses;
    }

    /**
     * Get the maximum bytecode size per class in bytes
     * 获取每个类的最大字节码大小（字节）
     *
     * @return max bytecode size, or 0 if unlimited | 最大字节码大小，0 表示无限制
     */
    public int maxBytecodeSize() {
        return maxBytecodeSize;
    }

    /**
     * Get the custom bytecode verifier
     * 获取自定义字节码验证器
     *
     * @return the bytecode verifier, or null if none configured |
     *         字节码验证器，如果未配置则为 null
     */
    public BytecodeVerifier bytecodeVerifier() {
        return bytecodeVerifier;
    }

    /**
     * Check whether a class is allowed to load by name and count
     * 按类名和数量检查是否允许加载
     *
     * <p>Evaluation order (deny wins over allow): denied packages are checked first;
     * if the class matches any denied prefix, it is rejected immediately regardless
     * of the allowed list. Only if the class passes the deny check is the allow list
     * consulted (when non-empty, the class must match at least one allowed prefix).
     * Finally, the loaded-class count limit is enforced.</p>
     * <p>评估顺序（拒绝优先于允许）：首先检查拒绝的包；如果类匹配任何拒绝前缀，
     * 则立即拒绝，不论允许列表如何。仅当类通过拒绝检查后才查询允许列表
     * （当允许列表非空时，类必须匹配至少一个允许前缀）。最后执行类数量限制检查。</p>
     *
     * @param className    the fully qualified class name | 完全限定类名
     * @param currentCount the current number of loaded classes | 当前已加载的类数量
     * @throws OpenClassLoaderException if the class is not allowed | 当类不被允许时
     * @throws NullPointerException     if className is null | 当 className 为 null 时
     */
    public void checkNameAllowed(String className, int currentCount) {
        Objects.requireNonNull(className, "className must not be null");

        // Check denied packages (blacklist)
        for (String denied : deniedPackages) {
            if (matchesPackagePrefix(className, denied)) {
                throw new OpenClassLoaderException(
                        "Class denied by policy — package '" + denied + "' is blacklisted: " + className);
            }
        }

        // Check allowed packages (whitelist) — if whitelist is non-empty, class must match
        if (!allowedPackages.isEmpty()) {
            boolean matched = false;
            for (String allowed : allowedPackages) {
                if (matchesPackagePrefix(className, allowed)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                throw new OpenClassLoaderException(
                        "Class denied by policy — not in any allowed package: " + className);
            }
        }

        // Check max loaded classes
        if (maxLoadedClasses > 0 && currentCount >= maxLoadedClasses) {
            throw new OpenClassLoaderException(
                    "Class denied by policy — max loaded classes reached (" + maxLoadedClasses + "): " + className);
        }
    }

    /**
     * Check whether bytecode is allowed under this policy
     * 检查字节码在此策略下是否允许
     *
     * <p>Checks bytecode size limit and custom bytecode verifier.
     * Call this when actual bytecode is available (in findClass/defineClass).</p>
     * <p>检查字节码大小限制和自定义字节码验证器。在字节码可用时调用（在 findClass/defineClass 中）。</p>
     *
     * @param className the fully qualified class name | 完全限定类名
     * @param bytecode  the raw class bytecode | 原始类字节码
     * @throws OpenClassLoaderException if the bytecode is not allowed | 当字节码不被允许时
     * @throws NullPointerException     if className or bytecode is null | 当参数为 null 时
     */
    public void checkBytecodeAllowed(String className, byte[] bytecode) {
        Objects.requireNonNull(className, "className must not be null");
        Objects.requireNonNull(bytecode, "bytecode must not be null");

        // Check max bytecode size
        if (maxBytecodeSize > 0 && bytecode.length > maxBytecodeSize) {
            throw new OpenClassLoaderException(
                    "Class denied by policy — bytecode size " + bytecode.length
                            + " exceeds max " + maxBytecodeSize + ": " + className);
        }

        // Custom bytecode verifier
        if (bytecodeVerifier != null && !bytecodeVerifier.verify(bytecode)) {
            throw new OpenClassLoaderException(
                    "Class denied by policy — bytecode verification failed: " + className);
        }
    }

    /**
     * Check whether a class is allowed to load under this policy (full check)
     * 检查一个类在此策略下是否允许加载（完整检查）
     *
     * <p>Combines name check and bytecode check. Use when bytecode is available.</p>
     * <p>合并名称检查和字节码检查。在字节码可用时使用。</p>
     *
     * @param className    the fully qualified class name | 完全限定类名
     * @param bytecode     the raw class bytecode (may be null to skip bytecode checks) |
     *                     原始类字节码（可为 null 以跳过字节码检查）
     * @param currentCount the current number of loaded classes | 当前已加载的类数量
     * @throws OpenClassLoaderException if the class is not allowed | 当类不被允许时
     * @throws NullPointerException     if className is null | 当 className 为 null 时
     */
    public void checkAllowed(String className, byte[] bytecode, int currentCount) {
        checkNameAllowed(className, currentCount);
        if (bytecode != null) {
            checkBytecodeAllowed(className, bytecode);
        }
    }

    /**
     * Check if a class name matches a package prefix at a proper package boundary.
     * Ensures "com.example" matches "com.example.Foo" but NOT "com.exampleEvil.Foo".
     * Handles both forms: prefix with trailing dot ("com.example.") and without ("com.example").
     * 检查类名是否在正确的包边界处匹配包前缀。
     * 确保 "com.example" 匹配 "com.example.Foo" 但不匹配 "com.exampleEvil.Foo"。
     * 同时处理带尾部点号 ("com.example.") 和不带 ("com.example") 两种形式。
     */
    private static boolean matchesPackagePrefix(String className, String prefix) {
        if (!className.startsWith(prefix)) {
            return false;
        }
        // If prefix already ends with '.', startsWith is sufficient for boundary safety
        if (prefix.endsWith(".")) {
            return true;
        }
        // Otherwise, ensure next char is '.' (package boundary) or exact match
        return className.length() == prefix.length()
                || className.charAt(prefix.length()) == '.';
    }

    /**
     * Builder for ClassLoadingPolicy
     * ClassLoadingPolicy 的构建器
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-classloader V1.0.3
     */
    public static final class Builder {

        private final Set<String> allowedPackages = new LinkedHashSet<>();
        private final Set<String> deniedPackages = new LinkedHashSet<>();
        private int maxLoadedClasses;
        private int maxBytecodeSize;
        private BytecodeVerifier bytecodeVerifier;

        private Builder() {
        }

        /**
         * Set the allowed packages (whitelist)
         * 设置允许的包（白名单）
         *
         * @param packages set of allowed package prefixes | 允许的包前缀集合
         * @return this builder | 此构建器
         * @throws NullPointerException if packages is null | 当 packages 为 null 时
         */
        public Builder allowedPackages(Set<String> packages) {
            Objects.requireNonNull(packages, "packages must not be null");
            this.allowedPackages.clear();
            this.allowedPackages.addAll(packages);
            return this;
        }

        /**
         * Add a single allowed package prefix
         * 添加单个允许的包前缀
         *
         * @param packagePrefix the package prefix to allow | 要允许的包前缀
         * @return this builder | 此构建器
         * @throws NullPointerException if packagePrefix is null | 当 packagePrefix 为 null 时
         */
        public Builder addAllowedPackage(String packagePrefix) {
            Objects.requireNonNull(packagePrefix, "packagePrefix must not be null");
            this.allowedPackages.add(packagePrefix);
            return this;
        }

        /**
         * Set the denied packages (blacklist)
         * 设置拒绝的包（黑名单）
         *
         * @param packages set of denied package prefixes | 拒绝的包前缀集合
         * @return this builder | 此构建器
         * @throws NullPointerException if packages is null | 当 packages 为 null 时
         */
        public Builder deniedPackages(Set<String> packages) {
            Objects.requireNonNull(packages, "packages must not be null");
            this.deniedPackages.clear();
            this.deniedPackages.addAll(packages);
            return this;
        }

        /**
         * Add a single denied package prefix
         * 添加单个拒绝的包前缀
         *
         * @param packagePrefix the package prefix to deny | 要拒绝的包前缀
         * @return this builder | 此构建器
         * @throws NullPointerException if packagePrefix is null | 当 packagePrefix 为 null 时
         */
        public Builder addDeniedPackage(String packagePrefix) {
            Objects.requireNonNull(packagePrefix, "packagePrefix must not be null");
            this.deniedPackages.add(packagePrefix);
            return this;
        }

        /**
         * Set the maximum number of loaded classes (0 for unlimited)
         * 设置最大已加载类数量（0 表示无限制）
         *
         * @param maxLoadedClasses maximum class count | 最大类数量
         * @return this builder | 此构建器
         * @throws IllegalArgumentException if maxLoadedClasses is negative |
         *                                  当 maxLoadedClasses 为负数时
         */
        public Builder maxLoadedClasses(int maxLoadedClasses) {
            if (maxLoadedClasses < 0) {
                throw new IllegalArgumentException("maxLoadedClasses must not be negative: " + maxLoadedClasses);
            }
            this.maxLoadedClasses = maxLoadedClasses;
            return this;
        }

        /**
         * Set the maximum bytecode size per class in bytes (0 for unlimited)
         * 设置每个类的最大字节码大小（字节，0 表示无限制）
         *
         * @param maxBytecodeSize maximum bytecode size in bytes | 最大字节码大小（字节）
         * @return this builder | 此构建器
         * @throws IllegalArgumentException if maxBytecodeSize is negative |
         *                                  当 maxBytecodeSize 为负数时
         */
        public Builder maxBytecodeSize(int maxBytecodeSize) {
            if (maxBytecodeSize < 0) {
                throw new IllegalArgumentException("maxBytecodeSize must not be negative: " + maxBytecodeSize);
            }
            this.maxBytecodeSize = maxBytecodeSize;
            return this;
        }

        /**
         * Set a custom bytecode verifier
         * 设置自定义字节码验证器
         *
         * @param verifier the bytecode verifier | 字节码验证器
         * @return this builder | 此构建器
         */
        public Builder bytecodeVerifier(BytecodeVerifier verifier) {
            this.bytecodeVerifier = verifier;
            return this;
        }

        /**
         * Build the immutable ClassLoadingPolicy
         * 构建不可变的 ClassLoadingPolicy
         *
         * @return a new ClassLoadingPolicy instance | 新的 ClassLoadingPolicy 实例
         */
        public ClassLoadingPolicy build() {
            return new ClassLoadingPolicy(this);
        }
    }
}
