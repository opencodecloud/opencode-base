package cloud.opencode.base.pdf.form;

/**
 * Checkbox Form Field
 * 复选框表单字段
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Check and uncheck state management - 选中和取消选中状态管理</li>
 *   <li>Export value access - 导出值访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckBox cb = (CheckBox) form.getField("agree");
 * cb.setChecked(true);
 * String exportVal = cb.getExportValue();
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
public non-sealed interface CheckBox extends FormField {

    @Override
    default FieldType getType() {
        return FieldType.CHECKBOX;
    }

    /**
     * Checks if checkbox is checked
     * 检查复选框是否选中
     *
     * @return true if checked | 如果选中返回 true
     */
    boolean isChecked();

    /**
     * Sets checked state
     * 设置选中状态
     *
     * @param checked new state | 新状态
     */
    void setChecked(boolean checked);

    /**
     * Gets export value (value when checked)
     * 获取导出值（选中时的值）
     *
     * @return export value | 导出值
     */
    String getExportValue();
}
