package cloud.opencode.base.pdf.form;

import java.util.List;

/**
 * Radio Button Form Field
 * 单选按钮表单字段
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Option list access - 选项列表访问</li>
 *   <li>Single selection management - 单选管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RadioButton radio = (RadioButton) form.getField("gender");
 * List<String> options = radio.getOptions();
 * radio.setSelectedOption("Male");
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
public non-sealed interface RadioButton extends FormField {

    @Override
    default FieldType getType() {
        return FieldType.RADIO;
    }

    /**
     * Gets available options
     * 获取可用选项
     *
     * @return list of option values | 选项值列表
     */
    List<String> getOptions();

    /**
     * Gets selected option
     * 获取选中的选项
     *
     * @return selected option value | 选中的选项值
     */
    String getSelectedOption();

    /**
     * Sets selected option
     * 设置选中的选项
     *
     * @param option option to select | 要选中的选项
     */
    void setSelectedOption(String option);
}
