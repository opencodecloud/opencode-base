package cloud.opencode.base.csv.exception;

import java.io.Serial;

/**
 * CSV Bind Exception - Exception for CSV-to-object binding errors
 * CSV绑定异常 - CSV到对象绑定错误异常
 *
 * <p>Thrown when a CSV row cannot be bound to a target object, for example
 * due to type mismatches, missing required fields, or reflection failures.</p>
 * <p>当CSV行无法绑定到目标对象时抛出，例如类型不匹配、缺少必需字段或反射失败。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw CsvBindException.of(MyRecord.class, "amount", cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public class CsvBindException extends OpenCsvException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The target type that binding failed for
     * 绑定失败的目标类型
     */
    private final Class<?> targetType;

    /**
     * The field name that caused the binding failure
     * 导致绑定失败的字段名
     */
    private final String fieldName;

    /**
     * Constructs a bind exception with target type and field name
     * 构造带目标类型和字段名的绑定异常
     *
     * @param message    the detail message | 详细消息
     * @param targetType the target type | 目标类型
     * @param fieldName  the field name | 字段名
     * @param cause      the cause | 原因
     */
    /**
     * Constructs a bind exception with target type and field name (no cause)
     * 构造带目标类型和字段名的绑定异常（无原因）
     *
     * @param message    the detail message | 详细消息
     * @param targetType the target type | 目标类型
     * @param fieldName  the field name | 字段名
     */
    public CsvBindException(String message, Class<?> targetType, String fieldName) {
        this(message, targetType, fieldName, null);
    }

    /**
     * Constructs a bind exception with target type, field name, and cause
     * 构造带目标类型、字段名和原因的绑定异常
     *
     * @param message    the detail message | 详细消息
     * @param targetType the target type | 目标类型
     * @param fieldName  the field name | 字段名
     * @param cause      the cause | 原因
     */
    public CsvBindException(String message, Class<?> targetType, String fieldName, Throwable cause) {
        super(message, cause);
        this.targetType = targetType;
        this.fieldName = fieldName;
    }

    /**
     * Gets the target type that binding failed for
     * 获取绑定失败的目标类型
     *
     * @return the target type, or null | 目标类型，或null
     */
    public Class<?> getTargetType() {
        return targetType;
    }

    /**
     * Gets the field name that caused the binding failure
     * 获取导致绑定失败的字段名
     *
     * @return the field name, or null | 字段名，或null
     */
    public String getFieldName() {
        return fieldName;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a bind exception for a specific type and field
     * 为特定类型和字段创建绑定异常
     *
     * @param type  the target type | 目标类型
     * @param field the field name | 字段名
     * @param cause the cause | 原因
     * @return the exception | 异常
     */
    public static CsvBindException of(Class<?> type, String field, Throwable cause) {
        String message = "Failed to bind field '" + field + "' on type " + type.getName();
        return new CsvBindException(message, type, field, cause);
    }
}
