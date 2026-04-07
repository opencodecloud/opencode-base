package cloud.opencode.base.csv.validator;

/**
 * CSV Validation Error - Describes a single validation failure
 * CSV验证错误 - 描述单个验证失败
 *
 * <p>Contains all diagnostic information for a validation error including
 * the row index, column name, actual value, rule name, and a human-readable
 * error message.</p>
 * <p>包含验证错误的所有诊断信息，包括行索引、列名、实际值、规则名称和
 * 人类可读的错误消息。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CsvValidationError error = new CsvValidationError(0, "age", "abc", "range[0.0,120.0]",
 *     "Value 'abc' in column 'age' is not a valid number");
 * }</pre>
 *
 * @param rowIndex 0-based data row index | 0起始的数据行索引
 * @param column   column name | 列名
 * @param value    actual value | 实际值
 * @param rule     rule name (e.g. "notBlank", "range[1.0,100.0]") | 规则名称
 * @param message  human-readable error message | 人类可读的错误消息
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public record CsvValidationError(
        int rowIndex,
        String column,
        String value,
        String rule,
        String message
) {}
