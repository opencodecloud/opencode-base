package cloud.opencode.base.pdf.form;

import java.util.List;

/**
 * Combo Box (Dropdown) Form Field
 * 下拉框表单字段
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dropdown option list access - 下拉选项列表访问</li>
 *   <li>Editable (custom input) support - 可编辑（自定义输入）支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ComboBox combo = (ComboBox) form.getField("country");
 * List<String> options = combo.getOptions();
 * combo.setValue("USA");
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
public non-sealed interface ComboBox extends FormField {

    @Override
    default FieldType getType() {
        return FieldType.COMBO;
    }

    /**
     * Gets available options
     * 获取可用选项
     *
     * @return list of options | 选项列表
     */
    List<String> getOptions();

    /**
     * Checks if editable (allows custom input)
     * 检查是否可编辑（允许自定义输入）
     *
     * @return true if editable | 如果可编辑返回 true
     */
    boolean isEditable();
}
