package cloud.opencode.base.classloader.index;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Index-Aware Scanner - Filters classes from a pre-built ClassIndex by package prefix
 * 索引感知扫描器 - 按包前缀从预构建的 ClassIndex 中过滤类
 *
 * <p>Provides fast, in-memory scanning of a {@link ClassIndex} without loading classes
 * or touching the filesystem. This is the primary consumer of the index at runtime.</p>
 * <p>提供对 {@link ClassIndex} 的快速内存扫描，无需加载类或访问文件系统。
 * 这是运行时索引的主要消费者。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public class IndexAwareScanner {

    private IndexAwareScanner() {
        // Utility class
    }

    /**
     * Scan the index for all class names under the given base package
     * 扫描索引中给定基础包下的所有类名
     *
     * @param index       the class index to scan | 要扫描的类索引
     * @param basePackage the base package prefix (e.g. "com.example") | 基础包前缀
     * @return set of fully qualified class names matching the package | 匹配包的完全限定类名集合
     */
    public static Set<String> scan(ClassIndex index, String basePackage) {
        return scan(index, basePackage, _ -> true);
    }

    /**
     * Scan the index for class names under the given base package that match the filter
     * 扫描索引中给定基础包下匹配过滤器的类名
     *
     * @param index       the class index to scan | 要扫描的类索引
     * @param basePackage the base package prefix (e.g. "com.example") | 基础包前缀
     * @param filter      predicate to filter entries | 过滤条目的谓词
     * @return set of fully qualified class names matching the package and filter | 匹配包和过滤器的完全限定类名集合
     */
    public static Set<String> scan(ClassIndex index, String basePackage, Predicate<ClassIndexEntry> filter) {
        Objects.requireNonNull(index, "ClassIndex must not be null");
        Objects.requireNonNull(basePackage, "Base package must not be null");
        Objects.requireNonNull(filter, "Filter must not be null");

        String prefix = basePackage.endsWith(".") ? basePackage : basePackage + ".";

        return index.entries().stream()
                .filter(entry -> entry.className().startsWith(prefix)
                        || entry.className().equals(basePackage))
                .filter(filter)
                .map(ClassIndexEntry::className)
                .collect(Collectors.toUnmodifiableSet());
    }
}
