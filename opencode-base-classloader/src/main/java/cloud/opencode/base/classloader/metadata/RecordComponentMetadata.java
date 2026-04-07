package cloud.opencode.base.classloader.metadata;

import java.util.List;
import java.util.Objects;

/**
 * Record Component Metadata - Immutable record component information
 * Record 组件元数据 - 不可变的 Record 组件信息
 *
 * <p>Represents metadata of a record component, including name, type, generic type and annotations.</p>
 * <p>表示 Record 组件的元数据，包括名称、类型、泛型类型和注解。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Component name and type - 组件名称和类型</li>
 *   <li>Generic type information - 泛型类型信息</li>
 *   <li>Annotation information - 注解信息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RecordComponentMetadata component = classMetadata.getRecordComponents().get(0);
 * String name = component.name();
 * String type = component.type();
 * String genericType = component.genericType();
 * List<AnnotationMetadata> annotations = component.annotations();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public record RecordComponentMetadata(
        String name,
        String type,
        String genericType,
        List<AnnotationMetadata> annotations
) {

    /**
     * Compact constructor with defensive copying and null checks
     * 紧凑构造器，进行防御性拷贝和空值校验
     *
     * @param name        component name | 组件名称
     * @param type        component type | 组件类型
     * @param genericType generic type | 泛型类型
     * @param annotations component annotations | 组件注解
     */
    public RecordComponentMetadata {
        Objects.requireNonNull(name, "Record component name must not be null");
        Objects.requireNonNull(type, "Record component type must not be null");
        annotations = annotations != null ? List.copyOf(annotations) : List.of();
    }
}
