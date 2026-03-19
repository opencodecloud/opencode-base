package cloud.opencode.base.pdf.form;

/**
 * Text Form Field
 * 文本表单字段
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Maximum length constraint - 最大长度约束</li>
 *   <li>Multiline and password field support - 多行和密码字段支持</li>
 *   <li>Text alignment configuration - 文本对齐配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TextField field = (TextField) form.getField("address");
 * field.setValue("123 Main Street");
 * boolean multiline = field.isMultiline();
 * int maxLen = field.getMaxLength();
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
public non-sealed interface TextField extends FormField {

    @Override
    default FieldType getType() {
        return FieldType.TEXT;
    }

    /**
     * Gets maximum length
     * 获取最大长度
     *
     * @return max length, or -1 if unlimited | 最大长度，无限制则返回 -1
     */
    int getMaxLength();

    /**
     * Checks if multiline
     * 检查是否多行
     *
     * @return true if multiline | 如果多行返回 true
     */
    boolean isMultiline();

    /**
     * Checks if password field
     * 检查是否密码字段
     *
     * @return true if password | 如果是密码字段返回 true
     */
    boolean isPassword();

    /**
     * Gets text alignment
     * 获取文本对齐
     *
     * @return alignment (0=left, 1=center, 2=right) | 对齐方式
     */
    int getAlignment();
}
