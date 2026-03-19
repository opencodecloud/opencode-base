package cloud.opencode.base.config.validation;

import cloud.opencode.base.config.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Range Validator - Validates numeric configuration values are within specified range
 * 范围验证器 - 验证数字配置值在指定范围内
 *
 * <p>Validates that configuration values fall within the specified minimum and maximum bounds.
 * Supports all numeric types including Integer, Long, Double, and Float.</p>
 * <p>验证配置值在指定的最小和最大界限内。支持所有数字类型包括Integer、Long、Double和Float。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Min/max validation - 最小/最大值验证</li>
 *   <li>Multiple numeric type support - 支持多种数字类型</li>
 *   <li>Inclusive/exclusive bounds - 包含/排除边界</li>
 *   <li>Optional bounds (min-only or max-only) - 可选边界（仅最小值或最大值）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Integer range for port
 * RangeValidator portValidator = new RangeValidator("server.port", 1, 65535);
 * ValidationResult result = portValidator.validate(config);
 *
 * // Double range for percentage
 * RangeValidator percentValidator = new RangeValidator("completion", 0.0, 100.0);
 * result = percentValidator.validate(config);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable: Yes (after construction) - 不可变: 是（构造后）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ConfigValidator
 * @see RequiredValidator
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class RangeValidator implements ConfigValidator {

    private final String key;
    private final Number min;
    private final Number max;
    private final boolean minInclusive;
    private final boolean maxInclusive;

    /**
     * Create range validator with inclusive bounds
     * 创建包含边界的范围验证器
     *
     * @param key configuration key | 配置键
     * @param min minimum value (null for no min limit) | 最小值（null表示无最小限制）
     * @param max maximum value (null for no max limit) | 最大值（null表示无最大限制）
     */
    public RangeValidator(String key, Number min, Number max) {
        this(key, min, max, true, true);
    }

    /**
     * Create range validator with configurable bounds
     * 创建可配置边界的范围验证器
     *
     * @param key          configuration key | 配置键
     * @param min          minimum value | 最小值
     * @param max          maximum value | 最大值
     * @param minInclusive include minimum value | 包含最小值
     * @param maxInclusive include maximum value | 包含最大值
     */
    public RangeValidator(String key, Number min, Number max, boolean minInclusive, boolean maxInclusive) {
        this.key = key;
        this.min = min;
        this.max = max;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    @Override
    public ValidationResult validate(Config config) {
        List<String> errors = new ArrayList<>();

        if (!config.hasKey(key)) {
            return ValidationResult.valid();
        }

        String value = config.getString(key);
        if (value == null || value.isEmpty()) {
            return ValidationResult.valid();
        }

        try {
            double numericValue = Double.parseDouble(value);

            if (min != null) {
                double minVal = min.doubleValue();
                if (minInclusive ? numericValue < minVal : numericValue <= minVal) {
                    errors.add("Value for key '" + key + "' must be " +
                            (minInclusive ? ">=" : ">") + " " + min + ", but was: " + numericValue);
                }
            }

            if (max != null) {
                double maxVal = max.doubleValue();
                if (maxInclusive ? numericValue > maxVal : numericValue >= maxVal) {
                    errors.add("Value for key '" + key + "' must be " +
                            (maxInclusive ? "<=" : "<") + " " + max + ", but was: " + numericValue);
                }
            }

        } catch (NumberFormatException e) {
            errors.add("Value for key '" + key + "' is not a valid number: " + value);
        }

        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }
}
