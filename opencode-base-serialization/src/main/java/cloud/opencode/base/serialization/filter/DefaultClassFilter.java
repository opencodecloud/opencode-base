
package cloud.opencode.base.serialization.filter;

/**
 * DefaultClassFilter - Pre-built Secure Class Filters
 * 预置安全类过滤器
 *
 * <p>Provides factory methods for commonly used deserialization class filters.
 * These filters protect against known deserialization gadget chains and
 * restrict classes to safe subsets of the JDK and common libraries.</p>
 * <p>提供常用反序列化类过滤器的工厂方法。
 * 这些过滤器可防御已知的反序列化利用链，
 * 并将类限制在 JDK 和常见库的安全子集内。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>{@link #secure()} - Blocks known gadget chain classes - 阻止已知利用链类</li>
 *   <li>{@link #strict()} - Allowlist-only filter for JDK standard types - 仅允许 JDK 标准类型的白名单过滤器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use the secure filter to block known dangerous classes
 * ClassFilter filter = DefaultClassFilter.secure();
 * boolean allowed = filter.isAllowed("java.lang.String"); // true
 * boolean blocked = filter.isAllowed("javax.naming.InitialContext"); // false
 *
 * // Use the strict filter for maximum security
 * ClassFilter strict = DefaultClassFilter.strict();
 * boolean ok = strict.isAllowed("java.util.ArrayList"); // true
 * boolean denied = strict.isAllowed("com.example.MyClass"); // false
 *
 * // Combine secure filter with custom allowlist
 * ClassFilter custom = DefaultClassFilter.secure()
 *     .and(new ClassFilterBuilder()
 *         .allowPackage("com.myapp.model")
 *         .defaultDeny()
 *         .build());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (all returned filters are immutable) - 线程安全: 是（所有返回的过滤器均不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see ClassFilter
 * @see ClassFilterBuilder
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
public final class DefaultClassFilter {

    /**
     * Cached secure filter instance (immutable, thread-safe, safe to share).
     * 缓存的安全过滤器实例（不可变、线程安全、可安全共享）。
     */
    private static final ClassFilter SECURE = buildSecure();

    /**
     * Cached strict filter instance (immutable, thread-safe, safe to share).
     * 缓存的严格过滤器实例（不可变、线程安全、可安全共享）。
     */
    private static final ClassFilter STRICT = buildStrict();

    private DefaultClassFilter() {
        // Utility class, no instantiation
    }

    /**
     * Returns a filter that blocks known dangerous deserialization gadget classes.
     * 返回阻止已知危险反序列化利用链类的过滤器。
     *
     * <p>This filter denies classes commonly exploited in deserialization attacks,
     * including JNDI injection, RMI exploitation, and known gadget chain classes
     * from Apache Commons Collections, Xalan, Spring, and other libraries.
     * All other classes are allowed by default.</p>
     * <p>此过滤器拒绝反序列化攻击中常被利用的类，
     * 包括 JNDI 注入、RMI 利用以及来自 Apache Commons Collections、
     * Xalan、Spring 等库的已知利用链类。
     * 默认允许其他所有类。</p>
     *
     * <p><strong>Blocked categories | 阻止的类别:</strong></p>
     * <ul>
     *   <li>JNDI: javax.naming.*, com.sun.jndi.* - JNDI 相关</li>
     *   <li>RMI: java.rmi.*, sun.rmi.* - RMI 相关</li>
     *   <li>Commons Collections gadgets: org.apache.commons.collections.functors.*,
     *       org.apache.commons.collections4.functors.* - Commons 集合利用链</li>
     *   <li>Xalan/XSLT: org.apache.xalan.*, com.sun.org.apache.xalan.* - XSLT 处理器</li>
     *   <li>BCEL: com.sun.org.apache.bcel.*, org.apache.bcel.* - 字节码工程库</li>
     *   <li>Spring: org.springframework.beans.factory.*, org.springframework.aop.* - Spring 框架</li>
     *   <li>Script engines: javax.script.*, jdk.nashorn.* - 脚本引擎</li>
     *   <li>Process execution: java.lang.ProcessBuilder, java.lang.Runtime - 进程执行</li>
     * </ul>
     *
     * @return a secure class filter that blocks known gadget chains | 阻止已知利用链的安全类过滤器
     */
    public static ClassFilter secure() {
        return SECURE;
    }

    private static ClassFilter buildSecure() {
        return new ClassFilterBuilder()
                // JNDI injection vectors
                .denyPackage("javax.naming")
                .denyPackage("com.sun.jndi")

                // RMI exploitation
                .denyPackage("java.rmi")
                .denyPackage("sun.rmi")
                .denyPackage("com.sun.rmi")

                // Apache Commons Collections gadget chains
                .denyPackage("org.apache.commons.collections.functors")
                .denyPackage("org.apache.commons.collections.transformers")
                .denyPackage("org.apache.commons.collections4.functors")
                .denyPackage("org.apache.commons.collections4.transformers")

                // Xalan / XSLT
                .denyPackage("org.apache.xalan")
                .denyPackage("com.sun.org.apache.xalan")

                // BCEL bytecode manipulation
                .denyPackage("com.sun.org.apache.bcel")
                .denyPackage("org.apache.bcel")

                // Spring framework gadgets
                .denyPackage("org.springframework.beans.factory")
                .denyPackage("org.springframework.aop")
                .denyPackage("org.springframework.transaction")

                // Script engines
                .denyPackage("javax.script")
                .denyPackage("jdk.nashorn")
                .denyPackage("org.mozilla.javascript")

                // Process execution
                .deny("java.lang.ProcessBuilder")
                .deny("java.lang.Runtime")

                // ClassLoader manipulation
                .deny("java.lang.ClassLoader")
                .deny("java.net.URLClassLoader")

                // URLDNS gadget: URL.hashCode() triggers DNS resolution (SSRF/recon)
                .deny("java.net.URL")

                // Sun internal classes (AnnotationInvocationHandler gadget, etc.)
                .denyPackage("sun")

                // JDK internal classes
                .denyPackage("jdk.internal")

                // JMX
                .denyPackage("javax.management")

                // Thread manipulation
                .deny("java.lang.Thread")
                .deny("java.lang.ThreadGroup")

                // Reflection-based attacks
                .denyPackage("java.lang.reflect")
                .denyPackage("java.lang.invoke")

                // C3P0 JNDI gadgets
                .denyPackage("com.mchange")

                // Groovy RCE gadgets
                .denyPackage("org.codehaus.groovy")
                .denyPackage("groovy")

                // Hibernate gadgets
                .denyPackage("org.hibernate")

                // JBoss gadgets
                .denyPackage("org.jboss")

                // Wicket gadgets
                .denyPackage("org.apache.wicket")

                // MyFaces gadgets
                .denyPackage("org.apache.myfaces")

                // JDBC RowSet JNDI attacks (all RowSet implementations can trigger JNDI via setDataSourceName)
                .denyPackage("com.sun.rowset")

                // Commons BeanUtils
                .denyPackage("org.apache.commons.beanutils")

                // Default: allow everything else
                .defaultAllow()
                .build();
    }

    /**
     * Returns a filter that only allows JDK standard types and common value types.
     * 返回仅允许 JDK 标准类型和常见值类型的过滤器。
     *
     * <p>This is the most restrictive built-in filter. Only the following categories
     * of classes are allowed:</p>
     * <p>这是最严格的内置过滤器。仅允许以下类别的类：</p>
     * <ul>
     *   <li>Primitives and wrappers: int, Integer, etc. - 基本类型和包装类</li>
     *   <li>String and CharSequence - 字符串</li>
     *   <li>java.math: BigDecimal, BigInteger - 数学类</li>
     *   <li>java.time: all temporal types - 时间类型</li>
     *   <li>java.util: collections, Optional, UUID, etc. - 集合、Optional、UUID 等</li>
     *   <li>java.net: URI - 网络地址（URL 因 DNS gadget 风险已排除）</li>
     *   <li>java.io.Serializable (marker interface only) - 序列化标记接口</li>
     *   <li>Arrays of allowed types - 允许类型的数组</li>
     * </ul>
     *
     * @return a strict allowlist-only class filter | 严格的白名单类过滤器
     */
    public static ClassFilter strict() {
        return STRICT;
    }

    private static ClassFilter buildStrict() {
        return new ClassFilterBuilder()
                // Primitive wrappers and String
                .allowPackage("java.lang")

                // Math types
                .allow("java.math.BigDecimal")
                .allow("java.math.BigInteger")
                .allow("java.math.MathContext")
                .allow("java.math.RoundingMode")

                // Date/Time types
                .allowPackage("java.time")

                // Collections and utilities
                .allowPackage("java.util")

                // Network value types (URL excluded: URL.hashCode() triggers DNS resolution — SSRF gadget)
                .allow("java.net.URI")

                // IO marker
                .allow("java.io.Serializable")

                // Primitive arrays (e.g. [I, [[B) — not stripped by array descriptor normalization
                .allowPattern("\\[+[ZBCSIFJD]")

                // Re-deny dangerous java.lang classes that were allowed by allowPackage("java.lang")
                .deny("java.lang.ProcessBuilder")
                .deny("java.lang.Runtime")
                .deny("java.lang.ClassLoader")
                .deny("java.lang.Thread")
                .deny("java.lang.ThreadGroup")
                .deny("java.lang.System")
                .denyPackage("java.lang.reflect")
                .denyPackage("java.lang.invoke")

                // Re-deny dangerous java.util sub-packages
                .denyPackage("java.util.logging")
                .denyPackage("java.util.prefs")

                // Default: deny everything else
                .defaultDeny()
                .build();
    }
}
