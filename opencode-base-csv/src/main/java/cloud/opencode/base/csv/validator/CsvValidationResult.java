package cloud.opencode.base.csv.validator;

import java.util.List;
import java.util.Objects;

/**
 * CSV Validation Result - Aggregated result of CSV document validation
 * CSV验证结果 - CSV文档验证的聚合结果
 *
 * <p>Contains the overall validity flag and a list of all validation errors found.
 * Use the static factory methods {@link #success()} and {@link #failure(List)} to
 * create instances.</p>
 * <p>包含整体有效性标志和发现的所有验证错误列表。
 * 使用静态工厂方法 {@link #success()} 和 {@link #failure(List)} 创建实例。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CsvValidationResult ok = CsvValidationResult.success();
 * assert ok.valid();
 * assert ok.errors().isEmpty();
 *
 * CsvValidationResult fail = CsvValidationResult.failure(List.of(error1, error2));
 * assert !fail.valid();
 * }</pre>
 *
 * @param valid  true if validation passed with no errors | 如果验证通过且无错误则为true
 * @param errors unmodifiable list of validation errors | 不可修改的验证错误列表
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public record CsvValidationResult(
        boolean valid,
        List<CsvValidationError> errors
) {

    /**
     * Canonical constructor - validates and defensively copies errors
     * 规范构造器 - 验证并防御性复制错误列表
     *
     * @param valid  validity flag | 有效性标志
     * @param errors the errors | 错误列表
     */
    public CsvValidationResult {
        Objects.requireNonNull(errors, "errors must not be null");
        errors = List.copyOf(errors);
    }

    /**
     * Creates a successful validation result with no errors
     * 创建无错误的成功验证结果
     *
     * @return a successful result | 成功结果
     */
    public static CsvValidationResult success() {
        return new CsvValidationResult(true, List.of());
    }

    /**
     * Creates a failed validation result with the given errors
     * 创建带有给定错误的失败验证结果
     *
     * @param errors the validation errors (must not be empty) | 验证错误（不能为空）
     * @return a failed result | 失败结果
     * @throws NullPointerException     if errors is null | 如果errors为null
     * @throws IllegalArgumentException if errors is empty | 如果errors为空
     */
    public static CsvValidationResult failure(List<CsvValidationError> errors) {
        Objects.requireNonNull(errors, "errors must not be null");
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("failure result must have at least one error");
        }
        return new CsvValidationResult(false, errors);
    }
}
