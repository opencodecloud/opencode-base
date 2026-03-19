package cloud.opencode.base.classloader;

import cloud.opencode.base.classloader.metadata.*;
import cloud.opencode.base.classloader.resource.Resource;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * OpenMetadata - Metadata Reading Facade
 * OpenMetadata - 元数据读取门面
 *
 * <p>Unified entry point for class metadata reading without loading classes.</p>
 * <p>不加载类读取类元数据的统一入口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Read class metadata by name - 按名称读取类元数据</li>
 *   <li>Read class metadata from Class - 从 Class 读取类元数据</li>
 *   <li>Read class metadata from resource - 从资源读取类元数据</li>
 *   <li>Batch reading from package - 从包批量读取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Read by class name
 * ClassMetadata metadata = OpenMetadata.read("com.example.MyClass");
 *
 * // Read from Class object
 * ClassMetadata metadata = OpenMetadata.read(MyClass.class);
 *
 * // Check annotations without loading
 * if (metadata.hasAnnotation("org.springframework.stereotype.Service")) {
 *     // Handle service class
 * }
 *
 * // Batch read
 * List<ClassMetadata> all = OpenMetadata.readPackage("com.example");
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
public final class OpenMetadata {

    private OpenMetadata() {
        // Utility class
    }

    // ==================== Read Methods | 读取方法 ====================

    /**
     * Read class metadata by class name
     * 按类名读取类元数据
     *
     * @param className fully qualified class name | 完全限定类名
     * @return class metadata | 类元数据
     */
    public static ClassMetadata read(String className) {
        Objects.requireNonNull(className, "Class name must not be null");
        return MetadataReader.read(className);
    }

    /**
     * Read class metadata from Class object
     * 从 Class 对象读取类元数据
     *
     * @param clazz class object | 类对象
     * @return class metadata | 类元数据
     */
    public static ClassMetadata read(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        return MetadataReader.read(clazz);
    }

    /**
     * Read class metadata from resource
     * 从资源读取类元数据
     *
     * @param resource class file resource | 类文件资源
     * @return class metadata | 类元数据
     */
    public static ClassMetadata read(Resource resource) {
        Objects.requireNonNull(resource, "Resource must not be null");
        return MetadataReader.read(resource);
    }

    /**
     * Read class metadata from bytecode
     * 从字节码读取类元数据
     *
     * @param bytecode class bytecode | 类字节码
     * @return class metadata | 类元数据
     */
    public static ClassMetadata read(byte[] bytecode) {
        Objects.requireNonNull(bytecode, "Bytecode must not be null");
        return MetadataReader.read(bytecode);
    }

    // ==================== Batch Read | 批量读取 ====================

    /**
     * Read all class metadata from package
     * 从包读取所有类元数据
     *
     * @param packageName package name | 包名
     * @return list of class metadata | 类元数据列表
     */
    public static List<ClassMetadata> readPackage(String packageName) {
        Objects.requireNonNull(packageName, "Package name must not be null");
        return MetadataReader.readAll(packageName);
    }

    /**
     * Read class metadata from package with filter
     * 从包读取类元数据（带过滤）
     *
     * @param packageName     package name | 包名
     * @param classNameFilter class name filter | 类名过滤器
     * @return list of class metadata | 类元数据列表
     */
    public static List<ClassMetadata> readPackage(String packageName, Predicate<String> classNameFilter) {
        Objects.requireNonNull(packageName, "Package name must not be null");
        Objects.requireNonNull(classNameFilter, "Filter must not be null");
        return MetadataReader.readAll(packageName, classNameFilter);
    }

    // ==================== Reader | 读取器 ====================

    /**
     * Get metadata reader for advanced usage
     * 获取元数据读取器用于高级用法
     *
     * @return metadata reader class | 元数据读取器类
     */
    public static Class<MetadataReader> reader() {
        return MetadataReader.class;
    }
}
