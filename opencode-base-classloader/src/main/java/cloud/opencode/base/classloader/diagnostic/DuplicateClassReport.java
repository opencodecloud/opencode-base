package cloud.opencode.base.classloader.diagnostic;

import java.util.List;
import java.util.Objects;

/**
 * Immutable report of a class found in multiple ClassLoaders
 * 在多个 ClassLoader 中发现的重复类的不可变报告
 *
 * <p>Represents a single class that was found to exist in two or more ClassLoaders,
 * which may lead to classpath conflicts, {@code ClassCastException}, or unexpected
 * behavior at runtime.</p>
 *
 * <p>表示在两个或多个 ClassLoader 中发现存在的单个类，
 * 这可能导致类路径冲突、{@code ClassCastException} 或运行时意外行为。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Records class name, loader names, and resource locations - 记录类名、加载器名称和资源位置</li>
 *   <li>Defensive copies for immutability - 防御性拷贝以保证不可变性</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DuplicateClassReport report = new DuplicateClassReport(
 *     "com.example.Foo",
 *     List.of("loader1", "loader2"),
 *     List.of("file:/path/a.jar", "file:/path/b.jar")
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是 (不可变记录)</li>
 * </ul>
 *
 * @param className        the fully qualified class name | 完全限定类名
 * @param classLoaderNames the names of ClassLoaders containing this class | 包含此类的 ClassLoader 名称列表
 * @param locations        the resource URL locations where the class was found | 发现此类的资源 URL 位置列表
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record DuplicateClassReport(
        String className,
        List<String> classLoaderNames,
        List<String> locations
) {

    /**
     * Compact constructor with validation and defensive copies
     * 带验证和防御性拷贝的紧凑构造器
     *
     * @throws NullPointerException     if any parameter is null | 当任何参数为 null 时
     * @throws IllegalArgumentException if classLoaderNames has fewer than 2 entries |
     *                                  当 classLoaderNames 少于 2 个条目时
     */
    public DuplicateClassReport {
        Objects.requireNonNull(className, "className must not be null");
        Objects.requireNonNull(classLoaderNames, "classLoaderNames must not be null");
        Objects.requireNonNull(locations, "locations must not be null");
        if (classLoaderNames.size() < 2) {
            throw new IllegalArgumentException(
                    "classLoaderNames must contain at least 2 entries, got: " + classLoaderNames.size());
        }
        classLoaderNames = List.copyOf(classLoaderNames);
        locations = List.copyOf(locations);
    }
}
