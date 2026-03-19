package cloud.opencode.base.pdf.form;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PDF Interactive Form (AcroForm)
 * PDF 交互表单
 *
 * <p>Represents the interactive form in a PDF document.</p>
 * <p>表示 PDF 文档中的交互表单。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Field access by name and type - 按名称和类型访问字段</li>
 *   <li>Batch field value setting - 批量设置字段值</li>
 *   <li>Form flattening (making non-editable) - 表单扁平化（使其不可编辑）</li>
 *   <li>Appearance generation - 外观生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfForm form = document.getForm();
 * form.setFieldValues(Map.of("name", "John", "age", "30"));
 * form.flatten();
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
public interface PdfForm {

    // ==================== Field Access | 字段访问 ====================

    /**
     * Gets all field names
     * 获取所有字段名
     *
     * @return set of field names | 字段名集合
     */
    Set<String> getFieldNames();

    /**
     * Gets a field by name
     * 根据名称获取字段
     *
     * @param name field name | 字段名
     * @return form field, or null if not found | 表单字段，如果未找到则返回 null
     */
    FormField getField(String name);

    /**
     * Gets all fields
     * 获取所有字段
     *
     * @return list of fields | 字段列表
     */
    List<FormField> getFields();

    /**
     * Gets fields by type
     * 根据类型获取字段
     *
     * @param fieldType field type class | 字段类型类
     * @param <T>       field type | 字段类型
     * @return list of matching fields | 匹配的字段列表
     */
    <T extends FormField> List<T> getFields(Class<T> fieldType);

    // ==================== Field Filling | 字段填充 ====================

    /**
     * Sets field value
     * 设置字段值
     *
     * @param name  field name | 字段名
     * @param value field value | 字段值
     */
    void setFieldValue(String name, String value);

    /**
     * Sets multiple field values
     * 设置多个字段值
     *
     * @param values field name to value mapping | 字段名到值的映射
     */
    void setFieldValues(Map<String, String> values);

    /**
     * Gets field value
     * 获取字段值
     *
     * @param name field name | 字段名
     * @return field value | 字段值
     */
    String getFieldValue(String name);

    /**
     * Gets all field values
     * 获取所有字段值
     *
     * @return field name to value mapping | 字段名到值的映射
     */
    Map<String, String> getFieldValues();

    // ==================== Form Operations | 表单操作 ====================

    /**
     * Flattens the form (makes fields non-editable)
     * 扁平化表单（使字段不可编辑）
     */
    void flatten();

    /**
     * Flattens specific fields
     * 扁平化指定字段
     *
     * @param fieldNames fields to flatten | 要扁平化的字段
     */
    void flatten(String... fieldNames);

    /**
     * Clears all field values
     * 清除所有字段值
     */
    void clearValues();

    /**
     * Checks if form needs appearances
     * 检查表单是否需要外观
     *
     * @return true if needs appearances | 如果需要外观返回 true
     */
    boolean needsAppearances();

    /**
     * Generates field appearances
     * 生成字段外观
     */
    void generateAppearances();
}
