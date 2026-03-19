package cloud.opencode.base.classloader;

import cloud.opencode.base.classloader.scanner.*;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

/**
 * OpenClassScanner - Class Scanning Facade
 * OpenClassScanner - 类扫描门面
 *
 * <p>Unified entry point for class scanning operations.</p>
 * <p>类扫描操作的统一入口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Quick annotation scanning - 快速注解扫描</li>
 *   <li>Subtype scanning - 子类型扫描</li>
 *   <li>Implementation scanning - 实现类扫描</li>
 *   <li>Create scanners - 创建扫描器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Quick scan with annotation
 * Set<Class<?>> services = OpenClassScanner.scanWithAnnotation("com.example", Service.class);
 *
 * // Scan subtypes
 * Set<Class<? extends Plugin>> plugins = OpenClassScanner.scanSubTypes("com.example", Plugin.class);
 *
 * // Use scanner with configuration
 * Set<Class<?>> classes = OpenClassScanner.of("com.example")
 *     .includeInnerClasses(true)
 *     .parallel(true)
 *     .scan(ScanFilter.isConcrete());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public final class OpenClassScanner {

    private OpenClassScanner() {
        // Utility class
    }

    // ==================== Scanner Creation | 扫描器创建 ====================

    /**
     * Create class scanner for single package
     * 为单个包创建类扫描器
     *
     * @param basePackage base package | 基础包
     * @return class scanner | 类扫描器
     */
    public static ClassScanner of(String basePackage) {
        return ClassScanner.of(basePackage);
    }

    /**
     * Create class scanner for multiple packages
     * 为多个包创建类扫描器
     *
     * @param basePackages base packages | 基础包数组
     * @return class scanner | 类扫描器
     */
    public static ClassScanner of(String... basePackages) {
        return ClassScanner.of(basePackages);
    }

    /**
     * Create class scanner with classloader
     * 使用类加载器创建类扫描器
     *
     * @param classLoader class loader | 类加载器
     * @param basePackage base package | 基础包
     * @return class scanner | 类扫描器
     */
    public static ClassScanner of(ClassLoader classLoader, String basePackage) {
        return ClassScanner.of(classLoader, basePackage);
    }

    // ==================== Quick Scan | 快速扫描 ====================

    /**
     * Quick scan for classes with annotation
     * 快速扫描带注解的类
     *
     * @param basePackage base package | 基础包
     * @param annotation  annotation class | 注解类
     * @return set of annotated classes | 带注解的类集合
     */
    public static Set<Class<?>> scanWithAnnotation(String basePackage, Class<? extends Annotation> annotation) {
        Objects.requireNonNull(basePackage, "Base package must not be null");
        Objects.requireNonNull(annotation, "Annotation must not be null");
        return ClassScanner.of(basePackage).scanWithAnnotation(annotation);
    }

    /**
     * Quick scan for subtypes
     * 快速扫描子类型
     *
     * @param basePackage base package | 基础包
     * @param superType   super type | 父类型
     * @param <T>         type parameter | 类型参数
     * @return set of subtypes | 子类型集合
     */
    public static <T> Set<Class<? extends T>> scanSubTypes(String basePackage, Class<T> superType) {
        Objects.requireNonNull(basePackage, "Base package must not be null");
        Objects.requireNonNull(superType, "Super type must not be null");
        return ClassScanner.of(basePackage).scanSubTypes(superType);
    }

    /**
     * Quick scan for interface implementations
     * 快速扫描接口实现
     *
     * @param basePackage   base package | 基础包
     * @param interfaceType interface type | 接口类型
     * @param <T>           type parameter | 类型参数
     * @return set of implementations | 实现类集合
     */
    public static <T> Set<Class<? extends T>> scanImplementations(String basePackage, Class<T> interfaceType) {
        Objects.requireNonNull(basePackage, "Base package must not be null");
        Objects.requireNonNull(interfaceType, "Interface type must not be null");
        return ClassScanner.of(basePackage).scanImplementations(interfaceType);
    }

    // ==================== Specialized Scanners | 专用扫描器 ====================

    /**
     * Create annotation scanner
     * 创建注解扫描器
     *
     * @param basePackage base package | 基础包
     * @return annotation scanner | 注解扫描器
     */
    public static AnnotationScanner annotationScanner(String basePackage) {
        return AnnotationScanner.of(basePackage);
    }

    /**
     * Create package scanner
     * 创建包扫描器
     *
     * @param basePackage base package | 基础包
     * @return package scanner | 包扫描器
     */
    public static PackageScanner packageScanner(String basePackage) {
        return PackageScanner.of(basePackage);
    }
}
