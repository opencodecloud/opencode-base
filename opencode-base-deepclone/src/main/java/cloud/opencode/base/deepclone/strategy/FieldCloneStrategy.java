package cloud.opencode.base.deepclone.strategy;

import cloud.opencode.base.deepclone.annotation.CloneDeep;
import cloud.opencode.base.deepclone.annotation.CloneIgnore;
import cloud.opencode.base.deepclone.annotation.CloneReference;

import java.lang.reflect.Field;

/**
 * Enumeration of field cloning strategies
 * 字段克隆策略枚举
 *
 * <p>Defines how individual fields should be handled during object cloning.
 * Can be determined from field annotations or explicitly specified.</p>
 * <p>定义对象克隆期间如何处理各个字段。可以从字段注解确定或显式指定。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Field field = MyClass.class.getDeclaredField("data");
 * FieldCloneStrategy strategy = FieldCloneStrategy.fromAnnotations(field);
 *
 * switch (strategy) {
 *     case DEEP -> deepCloneField(field, source, target);
 *     case SHALLOW -> shallowCopyField(field, source, target);
 *     case IGNORE -> {} // Skip the field
 *     case NULL -> setFieldNull(field, target);
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Annotation-based strategy detection - 基于注解的策略检测</li>
 *   <li>Four clone modes: DEEP, SHALLOW, IGNORE, NULL - 四种克隆模式</li>
 *   <li>Priority-based annotation resolution - 基于优先级的注解解析</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is immutable) - 线程安全: 是（枚举不可变）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public enum FieldCloneStrategy {

    /**
     * Deep clone the field (default behavior)
     * 深度克隆字段（默认行为）
     */
    DEEP,

    /**
     * Shallow copy (reference only)
     * 浅拷贝（仅复制引用）
     */
    SHALLOW,

    /**
     * Ignore the field (keep default value)
     * 忽略字段（保持默认值）
     */
    IGNORE,

    /**
     * Set field to null explicitly
     * 显式设置字段为null
     */
    NULL;

    /**
     * Determines the clone strategy from field annotations
     * 从字段注解确定克隆策略
     *
     * <p>Priority order: @CloneIgnore > @CloneReference > @CloneDeep > DEEP</p>
     * <p>优先级顺序：@CloneIgnore > @CloneReference > @CloneDeep > DEEP</p>
     *
     * @param field the field to analyze | 要分析的字段
     * @return the determined strategy | 确定的策略
     */
    public static FieldCloneStrategy fromAnnotations(Field field) {
        if (field.isAnnotationPresent(CloneIgnore.class)) {
            return IGNORE;
        }
        if (field.isAnnotationPresent(CloneReference.class)) {
            return SHALLOW;
        }
        if (field.isAnnotationPresent(CloneDeep.class)) {
            return DEEP;
        }
        return DEEP;
    }
}
