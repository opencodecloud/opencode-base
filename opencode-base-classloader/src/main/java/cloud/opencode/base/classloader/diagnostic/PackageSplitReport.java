package cloud.opencode.base.classloader.diagnostic;

import java.util.List;
import java.util.Objects;

/**
 * Immutable report of a package split across multiple ClassLoaders
 * 跨多个 ClassLoader 拆分的包的不可变报告
 *
 * <p>Represents a Java package whose classes are spread across two or more ClassLoaders,
 * which violates the Java module system's package uniqueness guarantee and can cause
 * runtime errors or security issues.</p>
 *
 * <p>表示类分布在两个或多个 ClassLoader 中的 Java 包，
 * 这违反了 Java 模块系统的包唯一性保证，可能导致运行时错误或安全问题。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Records split package name and involved loaders - 记录拆分包名和涉及的加载器</li>
 *   <li>Defensive copies for immutability - 防御性拷贝以保证不可变性</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PackageSplitReport report = new PackageSplitReport(
 *     "com.example.service",
 *     List.of("appLoader", "pluginLoader")
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是 (不可变记录)</li>
 * </ul>
 *
 * @param packageName      the fully qualified package name | 完全限定包名
 * @param classLoaderNames the names of ClassLoaders containing classes from this package |
 *                         包含此包中类的 ClassLoader 名称列表
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record PackageSplitReport(
        String packageName,
        List<String> classLoaderNames
) {

    /**
     * Compact constructor with validation and defensive copies
     * 带验证和防御性拷贝的紧凑构造器
     *
     * @throws NullPointerException     if any parameter is null | 当任何参数为 null 时
     * @throws IllegalArgumentException if classLoaderNames has fewer than 2 entries |
     *                                  当 classLoaderNames 少于 2 个条目时
     */
    public PackageSplitReport {
        Objects.requireNonNull(packageName, "packageName must not be null");
        Objects.requireNonNull(classLoaderNames, "classLoaderNames must not be null");
        if (classLoaderNames.size() < 2) {
            throw new IllegalArgumentException(
                    "classLoaderNames must contain at least 2 entries, got: " + classLoaderNames.size());
        }
        classLoaderNames = List.copyOf(classLoaderNames);
    }
}
