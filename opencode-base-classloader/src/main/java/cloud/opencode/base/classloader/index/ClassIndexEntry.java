package cloud.opencode.base.classloader.index;

import java.util.List;
import java.util.Objects;

/**
 * Class Index Entry - Immutable record representing a single class in the index
 * 类索引条目 - 表示索引中单个类的不可变记录
 *
 * <p>Stores lightweight metadata for a class without loading it at runtime.
 * Each entry captures the class name, hierarchy, annotations, and type flags.</p>
 * <p>存储类的轻量级元数据，无需在运行时加载类。
 * 每个条目记录类名、层次结构、注解和类型标志。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是 (不可变记录)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record ClassIndexEntry(
        String className,
        String superClassName,
        List<String> interfaceNames,
        List<String> annotationNames,
        int modifiers,
        boolean isInterface,
        boolean isAbstract,
        boolean isEnum,
        boolean isRecord,
        boolean isSealed
) {

    /**
     * Create a class index entry with defensive copies of lists
     * 创建类索引条目，对列表进行防御性拷贝
     *
     * @param className      fully qualified class name | 完全限定类名
     * @param superClassName super class name, may be null | 父类名，可为 null
     * @param interfaceNames list of interface names | 接口名列表
     * @param annotationNames list of annotation names | 注解名列表
     * @param modifiers      access modifiers | 访问修饰符
     * @param isInterface    whether this is an interface | 是否为接口
     * @param isAbstract     whether this is abstract | 是否为抽象类
     * @param isEnum         whether this is an enum | 是否为枚举
     * @param isRecord       whether this is a record | 是否为 Record
     * @param isSealed       whether this is sealed | 是否为密封类
     */
    public ClassIndexEntry {
        Objects.requireNonNull(className, "className must not be null");
        interfaceNames = interfaceNames != null ? List.copyOf(interfaceNames) : List.of();
        annotationNames = annotationNames != null ? List.copyOf(annotationNames) : List.of();
    }
}
