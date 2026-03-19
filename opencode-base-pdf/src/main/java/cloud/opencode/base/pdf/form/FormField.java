package cloud.opencode.base.pdf.form;

/**
 * Form Field Base - Common interface for all form field types
 * 表单字段基类 - 所有表单字段类型的公共接口
 *
 * <p>Sealed interface permitting known form field types.</p>
 * <p>密封接口，仅允许已知的表单字段类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Common field properties (name, type, value, tooltip) - 通用字段属性（名称、类型、值、提示）</li>
 *   <li>Field state queries (required, read-only) - 字段状态查询（必填、只读）</li>
 *   <li>Position and size via rectangle - 通过矩形获取位置和大小</li>
 *   <li>Sealed hierarchy for type-safe processing - 密封层次结构用于类型安全处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FormField field = form.getField("name");
 * String value = field.getValue();
 * field.setValue("New Value");
 * boolean required = field.isRequired();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public sealed interface FormField
    permits TextField, CheckBox, RadioButton, ComboBox, ListBox, SignatureField {

    /**
     * Gets field name
     * 获取字段名
     *
     * @return field name | 字段名
     */
    String getName();

    /**
     * Gets field type
     * 获取字段类型
     *
     * @return field type | 字段类型
     */
    FieldType getType();

    /**
     * Gets field value
     * 获取字段值
     *
     * @return field value | 字段值
     */
    String getValue();

    /**
     * Sets field value
     * 设置字段值
     *
     * @param value new value | 新值
     */
    void setValue(String value);

    /**
     * Gets field tooltip
     * 获取字段工具提示
     *
     * @return tooltip text | 工具提示文本
     */
    String getTooltip();

    /**
     * Checks if field is required
     * 检查字段是否必填
     *
     * @return true if required | 如果必填返回 true
     */
    boolean isRequired();

    /**
     * Checks if field is read-only
     * 检查字段是否只读
     *
     * @return true if read-only | 如果只读返回 true
     */
    boolean isReadOnly();

    /**
     * Gets field rectangle (position and size)
     * 获取字段矩形（位置和大小）
     *
     * @return rectangle as [x, y, width, height] | 矩形 [x, y, 宽, 高]
     */
    float[] getRectangle();

    /**
     * Field types
     * 字段类型
     */
    enum FieldType {
        /** Text field | 文本字段 */
        TEXT,
        /** Checkbox field | 复选框字段 */
        CHECKBOX,
        /** Radio button field | 单选按钮字段 */
        RADIO,
        /** Combo box (dropdown) field | 下拉框字段 */
        COMBO,
        /** List box field | 列表框字段 */
        LIST,
        /** Signature field | 签名字段 */
        SIGNATURE
    }
}
