
package cloud.opencode.base.serialization.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * ClassFilterBuilder - Builder for Composite Class Filters
 * 组合类过滤器构建器
 *
 * <p>Provides a fluent API for building composite {@link ClassFilter} instances
 * that support allow/deny rules by exact class name, package prefix, and regex pattern.
 * The built filter evaluates rules in a deterministic order: deny rules first, then
 * allow rules, and finally the default policy.</p>
 * <p>提供流式 API 来构建组合 {@link ClassFilter} 实例，
 * 支持按精确类名、包前缀和正则表达式的允许/拒绝规则。
 * 构建的过滤器按确定性顺序评估规则：先拒绝规则，再允许规则，最后默认策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exact class name allow/deny rules - 精确类名允许/拒绝规则</li>
 *   <li>Package prefix allow/deny rules - 包前缀允许/拒绝规则</li>
 *   <li>Regex pattern allow/deny rules - 正则模式允许/拒绝规则</li>
 *   <li>Configurable default policy (allow or deny) - 可配置默认策略（允许或拒绝）</li>
 * </ul>
 *
 * <p><strong>Evaluation Order | 评估顺序:</strong></p>
 * <ol>
 *   <li>Check denied classes (exact match) - 检查拒绝的类（精确匹配）</li>
 *   <li>Check denied packages (prefix match) - 检查拒绝的包（前缀匹配）</li>
 *   <li>Check denied patterns (regex match) - 检查拒绝的模式（正则匹配）</li>
 *   <li>Check allowed classes (exact match) - 检查允许的类（精确匹配）</li>
 *   <li>Check allowed packages (prefix match) - 检查允许的包（前缀匹配）</li>
 *   <li>Check allowed patterns (regex match) - 检查允许的模式（正则匹配）</li>
 *   <li>Fall back to default policy - 回退到默认策略</li>
 * </ol>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build a filter that denies dangerous classes but allows everything else
 * ClassFilter filter = new ClassFilterBuilder()
 *     .denyPackage("javax.naming", "java.rmi", "sun.rmi")
 *     .denyPattern("org\\.apache\\.commons\\.collections\\.functors\\..*")
 *     .defaultAllow()
 *     .build();
 *
 * // Build a strict allowlist filter
 * ClassFilter strict = new ClassFilterBuilder()
 *     .allowPackage("java.lang", "java.util", "java.time")
 *     .allow("java.math.BigDecimal", "java.math.BigInteger")
 *     .defaultDeny()
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder is mutable), but built ClassFilter is thread-safe -
 *       线程安全: 否（构建器可变），但构建的 ClassFilter 是线程安全的</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see ClassFilter
 * @see DefaultClassFilter
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
public final class ClassFilterBuilder {

    private final Set<String> allowedClasses = new LinkedHashSet<>();
    private final Set<String> deniedClasses = new LinkedHashSet<>();
    private final Set<String> allowedPackages = new LinkedHashSet<>();
    private final Set<String> deniedPackages = new LinkedHashSet<>();
    private final List<Pattern> allowedPatterns = new ArrayList<>();
    private final List<Pattern> deniedPatterns = new ArrayList<>();
    private boolean defaultAllow = true;

    /**
     * Creates a new ClassFilterBuilder with default-allow policy.
     * 创建一个默认允许策略的 ClassFilterBuilder。
     */
    public ClassFilterBuilder() {
        // default: allow unless denied
    }

    // ==================== Allow Rules | 允许规则 ====================

    /**
     * Adds exact class names to the allow list.
     * 将精确类名添加到允许列表。
     *
     * @param classNames the fully qualified class names to allow | 要允许的完全限定类名
     * @return this builder | 此构建器
     * @throws NullPointerException if classNames is null or contains null | 当 classNames 为 null 或包含 null 时抛出
     */
    public ClassFilterBuilder allow(String... classNames) {
        Objects.requireNonNull(classNames, "classNames must not be null");
        for (String name : classNames) {
            Objects.requireNonNull(name, "class name must not be null");
            allowedClasses.add(name);
        }
        return this;
    }

    /**
     * Adds package prefixes to the allow list.
     * 将包前缀添加到允许列表。
     *
     * <p>A class is matched if its fully qualified name starts with any of the given
     * package prefixes followed by a dot. For example, package "java.lang" matches
     * "java.lang.String" but not "java.language.Foo".</p>
     * <p>如果类的完全限定名以给定包前缀加点号开头，则匹配。
     * 例如包 "java.lang" 匹配 "java.lang.String" 但不匹配 "java.language.Foo"。</p>
     *
     * @param packages the package prefixes to allow | 要允许的包前缀
     * @return this builder | 此构建器
     * @throws NullPointerException if packages is null or contains null | 当 packages 为 null 或包含 null 时抛出
     */
    public ClassFilterBuilder allowPackage(String... packages) {
        Objects.requireNonNull(packages, "packages must not be null");
        for (String pkg : packages) {
            Objects.requireNonNull(pkg, "package name must not be null");
            allowedPackages.add(pkg);
        }
        return this;
    }

    /**
     * Adds a regex pattern to the allow list.
     * 将正则模式添加到允许列表。
     *
     * @param regex the regex pattern to match class names against | 用于匹配类名的正则模式
     * @return this builder | 此构建器
     * @throws NullPointerException if regex is null | 当 regex 为 null 时抛出
     * @throws java.util.regex.PatternSyntaxException if regex is invalid | 当正则表达式无效时抛出
     */
    public ClassFilterBuilder allowPattern(String regex) {
        Objects.requireNonNull(regex, "regex must not be null");
        allowedPatterns.add(Pattern.compile(regex));
        return this;
    }

    // ==================== Deny Rules | 拒绝规则 ====================

    /**
     * Adds exact class names to the deny list.
     * 将精确类名添加到拒绝列表。
     *
     * @param classNames the fully qualified class names to deny | 要拒绝的完全限定类名
     * @return this builder | 此构建器
     * @throws NullPointerException if classNames is null or contains null | 当 classNames 为 null 或包含 null 时抛出
     */
    public ClassFilterBuilder deny(String... classNames) {
        Objects.requireNonNull(classNames, "classNames must not be null");
        for (String name : classNames) {
            Objects.requireNonNull(name, "class name must not be null");
            deniedClasses.add(name);
        }
        return this;
    }

    /**
     * Adds package prefixes to the deny list.
     * 将包前缀添加到拒绝列表。
     *
     * <p>A class is matched if its fully qualified name starts with any of the given
     * package prefixes followed by a dot. For example, package "javax.naming" matches
     * "javax.naming.InitialContext" but not "javax.namingExtra".</p>
     * <p>如果类的完全限定名以给定包前缀加点号开头，则匹配。
     * 例如包 "javax.naming" 匹配 "javax.naming.InitialContext" 但不匹配 "javax.namingExtra"。</p>
     *
     * @param packages the package prefixes to deny | 要拒绝的包前缀
     * @return this builder | 此构建器
     * @throws NullPointerException if packages is null or contains null | 当 packages 为 null 或包含 null 时抛出
     */
    public ClassFilterBuilder denyPackage(String... packages) {
        Objects.requireNonNull(packages, "packages must not be null");
        for (String pkg : packages) {
            Objects.requireNonNull(pkg, "package name must not be null");
            deniedPackages.add(pkg);
        }
        return this;
    }

    /**
     * Adds a regex pattern to the deny list.
     * 将正则模式添加到拒绝列表。
     *
     * @param regex the regex pattern to match class names against | 用于匹配类名的正则模式
     * @return this builder | 此构建器
     * @throws NullPointerException if regex is null | 当 regex 为 null 时抛出
     * @throws java.util.regex.PatternSyntaxException if regex is invalid | 当正则表达式无效时抛出
     */
    public ClassFilterBuilder denyPattern(String regex) {
        Objects.requireNonNull(regex, "regex must not be null");
        deniedPatterns.add(Pattern.compile(regex));
        return this;
    }

    // ==================== Default Policy | 默认策略 ====================

    /**
     * Sets the default policy to allow classes not matched by any rule.
     * 设置默认策略为允许未被任何规则匹配的类。
     *
     * <p>This is the default behavior. Classes that do not match any deny or allow
     * rule will be allowed.</p>
     * <p>这是默认行为。不匹配任何拒绝或允许规则的类将被允许。</p>
     *
     * @return this builder | 此构建器
     */
    public ClassFilterBuilder defaultAllow() {
        this.defaultAllow = true;
        return this;
    }

    /**
     * Sets the default policy to deny classes not matched by any rule.
     * 设置默认策略为拒绝未被任何规则匹配的类。
     *
     * <p>This creates a strict allowlist: only classes explicitly allowed by
     * allow rules will pass the filter.</p>
     * <p>这将创建严格的白名单：只有被允许规则明确允许的类才能通过过滤器。</p>
     *
     * @return this builder | 此构建器
     */
    public ClassFilterBuilder defaultDeny() {
        this.defaultAllow = false;
        return this;
    }

    // ==================== Build | 构建 ====================

    /**
     * Builds an immutable, thread-safe {@link ClassFilter} from the current configuration.
     * 根据当前配置构建不可变的、线程安全的 {@link ClassFilter}。
     *
     * <p>The returned filter evaluates rules in the following order:</p>
     * <ol>
     *   <li>If the class is in the denied classes set, deny</li>
     *   <li>If the class matches a denied package prefix, deny</li>
     *   <li>If the class matches a denied pattern, deny</li>
     *   <li>If the class is in the allowed classes set, allow</li>
     *   <li>If the class matches an allowed package prefix, allow</li>
     *   <li>If the class matches an allowed pattern, allow</li>
     *   <li>Otherwise, apply the default policy</li>
     * </ol>
     *
     * @return the built class filter | 构建的类过滤器
     */
    public ClassFilter build() {
        // Snapshot all collections for immutability
        Set<String> snapshotDeniedClasses = Set.copyOf(deniedClasses);
        Set<String> snapshotAllowedClasses = Set.copyOf(allowedClasses);
        // Pre-compute package prefixes with dot to avoid string concatenation on every check
        List<String> snapshotDeniedPrefixes = deniedPackages.stream()
                .map(pkg -> pkg + ".").toList();
        List<String> snapshotAllowedPrefixes = allowedPackages.stream()
                .map(pkg -> pkg + ".").toList();
        List<Pattern> snapshotDeniedPatterns = List.copyOf(deniedPatterns);
        List<Pattern> snapshotAllowedPatterns = List.copyOf(allowedPatterns);
        boolean snapshotDefaultAllow = this.defaultAllow;

        return className -> {
            if (className == null) {
                return false;
            }

            // Strip JVM array descriptor to prevent bypass via array class names
            // e.g. "[Ljavax.naming.Reference;" -> "javax.naming.Reference"
            // 剥离 JVM 数组描述符以防止通过数组类名绕过过滤
            String effectiveName = stripArrayDescriptor(className);

            // 1. Check denied classes (exact match)
            if (snapshotDeniedClasses.contains(effectiveName)) {
                return false;
            }

            // 2. Check denied packages (prefix match with dot boundary)
            for (String prefix : snapshotDeniedPrefixes) {
                if (effectiveName.startsWith(prefix)) {
                    return false;
                }
            }

            // 3. Check denied patterns (regex match)
            for (Pattern pattern : snapshotDeniedPatterns) {
                if (pattern.matcher(effectiveName).matches()) {
                    return false;
                }
            }

            // 4. Check allowed classes (exact match)
            if (snapshotAllowedClasses.contains(effectiveName)) {
                return true;
            }

            // 5. Check allowed packages (prefix match with dot boundary)
            for (String prefix : snapshotAllowedPrefixes) {
                if (effectiveName.startsWith(prefix)) {
                    return true;
                }
            }

            // 6. Check allowed patterns (regex match)
            for (Pattern pattern : snapshotAllowedPatterns) {
                if (pattern.matcher(effectiveName).matches()) {
                    return true;
                }
            }

            // 7. Fall back to default policy
            return snapshotDefaultAllow;
        };
    }

    /**
     * Strips JVM array descriptor prefix from a class name to get the component type.
     * 剥离 JVM 数组描述符前缀以获取组件类型名。
     */
    private static String stripArrayDescriptor(String className) {
        int i = 0;
        while (i < className.length() && className.charAt(i) == '[') {
            i++;
        }
        if (i > 0 && i < className.length() && className.charAt(i) == 'L' && className.endsWith(";")) {
            return className.substring(i + 1, className.length() - 1);
        }
        return className;
    }
}
