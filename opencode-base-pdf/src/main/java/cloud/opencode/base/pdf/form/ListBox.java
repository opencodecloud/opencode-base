package cloud.opencode.base.pdf.form;

import java.util.List;

/**
 * List Box Form Field
 * 列表框表单字段
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Option list access - 选项列表访问</li>
 *   <li>Single and multi-select support - 单选和多选支持</li>
 *   <li>Selected options management - 选中选项管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ListBox list = (ListBox) form.getField("items");
 * List<String> options = list.getOptions();
 * list.setSelectedOptions(List.of("Option1", "Option3"));
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
public non-sealed interface ListBox extends FormField {

    @Override
    default FieldType getType() {
        return FieldType.LIST;
    }

    /**
     * Gets available options
     * 获取可用选项
     *
     * @return list of options | 选项列表
     */
    List<String> getOptions();

    /**
     * Gets selected options
     * 获取选中的选项
     *
     * @return list of selected options | 选中的选项列表
     */
    List<String> getSelectedOptions();

    /**
     * Checks if multiple selection is allowed
     * 检查是否允许多选
     *
     * @return true if multi-select | 如果允许多选返回 true
     */
    boolean isMultiSelect();

    /**
     * Sets selected options
     * 设置选中的选项
     *
     * @param options options to select | 要选中的选项
     */
    void setSelectedOptions(List<String> options);
}
