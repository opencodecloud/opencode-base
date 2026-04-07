package cloud.opencode.base.csv.validator;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * CSV Validator - Declarative validation framework for CSV data
 * CSV验证器 - CSV数据的声明式验证框架
 *
 * <p>Provides a fluent builder API for defining validation rules on CSV columns.
 * Rules are evaluated against all rows in a {@link CsvDocument}, collecting every
 * error (not fail-fast). Supports common constraints like notBlank, numeric range,
 * regex pattern, length bounds, allowed values, and custom predicates.</p>
 * <p>提供用于定义CSV列验证规则的流式构建器API。
 * 规则针对 {@link CsvDocument} 中的所有行进行评估，收集所有错误（非快速失败）。
 * 支持常见约束，如非空、数字范围、正则模式、长度限制、允许值和自定义谓词。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Builder pattern for rule definition - Builder模式定义规则</li>
 *   <li>Collects all errors (not fail-fast) - 收集所有错误（非快速失败）</li>
 *   <li>Built-in rules: notBlank, range, pattern, minLength, maxLength, oneOf - 内置规则</li>
 *   <li>Custom predicate rules - 自定义谓词规则</li>
 *   <li>Column existence validation at validate time - 验证时检查列存在性</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CsvValidator validator = CsvValidator.builder()
 *     .notBlank("name")
 *     .range("age", 0, 150)
 *     .pattern("email", "^[\\w@.]+$")
 *     .oneOf("status", "active", "inactive")
 *     .build();
 *
 * CsvValidationResult result = validator.validate(doc);
 * if (!result.valid()) {
 *     result.errors().forEach(System.out::println);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after build) - 线程安全: 是（构建后不可变）</li>
 *   <li>Regex compiled once at build time - 正则在构建时编译一次</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvValidator {

    private final List<Rule> rules;

    private CsvValidator(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    /**
     * Creates a new validator builder
     * 创建新的验证器构建器
     *
     * @return a new Builder | 新的Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Validates the given CSV document against all configured rules
     * 使用所有已配置规则验证给定的CSV文档
     *
     * <p>Iterates all rows and all rules, collecting every error.
     * Column existence is validated at this point.</p>
     * <p>遍历所有行和所有规则，收集所有错误。此时验证列的存在性。</p>
     *
     * @param doc the CSV document to validate | 要验证的CSV文档
     * @return the validation result | 验证结果
     * @throws NullPointerException if doc is null | 如果doc为null
     */
    public CsvValidationResult validate(CsvDocument doc) {
        Objects.requireNonNull(doc, "doc must not be null");

        if (doc.isEmpty()) {
            return CsvValidationResult.success();
        }

        List<String> headers = doc.headers();
        List<CsvValidationError> errors = new ArrayList<>();

        // Pre-compute column indices to avoid repeated O(n) headers.indexOf() per rule
        java.util.Map<String, Integer> columnIndexCache = new java.util.HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            columnIndexCache.put(headers.get(i), i);
        }

        List<CsvRow> rows = doc.rows();
        for (Rule rule : rules) {
            Integer colIndexObj = columnIndexCache.get(rule.column);
            if (colIndexObj == null) {
                // Column not found — report single error (not per-row)
                errors.add(new CsvValidationError(
                        -1, rule.column, null, rule.ruleName,
                        "Column '" + rule.column + "' not found in headers"
                ));
                continue;
            }
            int colIndex = colIndexObj;

            for (int i = 0; i < rows.size(); i++) {
                CsvRow row = rows.get(i);
                String value = colIndex < row.size() ? row.get(colIndex) : null;
                if (!rule.predicate.test(value)) {
                    errors.add(new CsvValidationError(
                            i, rule.column, value, rule.ruleName, rule.errorMessage(value)
                    ));
                }
            }
        }

        if (errors.isEmpty()) {
            return CsvValidationResult.success();
        }
        return CsvValidationResult.failure(errors);
    }

    // ==================== Internal Rule | 内部规则 ====================

    private record Rule(
            String column,
            Predicate<String> predicate,
            String ruleName,
            String errorMessageTemplate
    ) {
        String errorMessage(String value) {
            return errorMessageTemplate.replace("{value}", String.valueOf(value));
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for CsvValidator
     * CsvValidator构建器
     *
     * <p>Provides a fluent API for adding validation rules.</p>
     * <p>提供用于添加验证规则的流式API。</p>
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-csv V1.0.3
     */
    public static final class Builder {

        private final List<Rule> rules = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds a not-blank rule: value must not be null, empty, or whitespace-only
         * 添加非空规则：值不能为null、空或仅包含空白字符
         *
         * @param column the column name | 列名
         * @return this builder | 此构建器
         * @throws NullPointerException if column is null | 如果column为null
         */
        public Builder notBlank(String column) {
            Objects.requireNonNull(column, "column must not be null");
            rules.add(new Rule(
                    column,
                    v -> v != null && !v.isBlank(),
                    "notBlank",
                    "Column '" + column + "' must not be blank, but was '{value}'"
            ));
            return this;
        }

        /**
         * Adds a numeric range rule: value must be a valid double within [min, max]
         * 添加数字范围规则：值必须是在[min, max]范围内的有效双精度数
         *
         * @param column the column name | 列名
         * @param min    the minimum value (inclusive) | 最小值（包含）
         * @param max    the maximum value (inclusive) | 最大值（包含）
         * @return this builder | 此构建器
         * @throws NullPointerException     if column is null | 如果column为null
         * @throws IllegalArgumentException if min > max | 如果min大于max
         */
        public Builder range(String column, double min, double max) {
            Objects.requireNonNull(column, "column must not be null");
            if (min > max) {
                throw new IllegalArgumentException("min (" + min + ") must not be greater than max (" + max + ")");
            }
            rules.add(new Rule(
                    column,
                    v -> {
                        if (v == null || v.isBlank()) {
                            return false;
                        }
                        try {
                            double d = Double.parseDouble(v);
                            return d >= min && d <= max;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    },
                    "range[" + min + "," + max + "]",
                    "Column '" + column + "' value '{value}' is not in range [" + min + ", " + max + "]"
            ));
            return this;
        }

        /**
         * Adds a regex pattern rule: value must match the given pattern
         * 添加正则模式规则：值必须匹配给定的模式
         *
         * <p>The regex is compiled once at build time for efficiency.</p>
         * <p>正则在构建时编译一次以提高效率。</p>
         *
         * @param column the column name | 列名
         * @param regex  the regular expression | 正则表达式
         * @return this builder | 此构建器
         * @throws NullPointerException if column or regex is null | 如果column或regex为null
         */
        public Builder pattern(String column, String regex) {
            Objects.requireNonNull(column, "column must not be null");
            Objects.requireNonNull(regex, "regex must not be null");
            if (regex.length() > 1024) {
                throw new IllegalArgumentException("Regex pattern length must not exceed 1024 characters");
            }
            Pattern compiled;
            try {
                compiled = Pattern.compile(regex);
            } catch (java.util.regex.PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid regex pattern: " + e.getMessage(), e);
            }
            // Use matches with an input length guard to mitigate ReDoS
            final int maxMatchLength = 10_000;
            rules.add(new Rule(
                    column,
                    v -> {
                        if (v == null) return false;
                        if (v.length() > maxMatchLength) return false;
                        return compiled.matcher(v).matches();
                    },
                    "pattern[" + regex + "]",
                    "Column '" + column + "' value '{value}' does not match pattern '" + regex + "'"
            ));
            return this;
        }

        /**
         * Adds a minimum length rule: value must have at least min characters
         * 添加最小长度规则：值必须至少有min个字符
         *
         * @param column the column name | 列名
         * @param min    the minimum length (inclusive) | 最小长度（包含）
         * @return this builder | 此构建器
         * @throws NullPointerException     if column is null | 如果column为null
         * @throws IllegalArgumentException if min < 0 | 如果min小于0
         */
        public Builder minLength(String column, int min) {
            Objects.requireNonNull(column, "column must not be null");
            if (min < 0) {
                throw new IllegalArgumentException("min must not be negative: " + min);
            }
            rules.add(new Rule(
                    column,
                    v -> v != null && v.length() >= min,
                    "minLength[" + min + "]",
                    "Column '" + column + "' value '{value}' is shorter than minimum length " + min
            ));
            return this;
        }

        /**
         * Adds a maximum length rule: value must have at most max characters
         * 添加最大长度规则：值最多有max个字符
         *
         * @param column the column name | 列名
         * @param max    the maximum length (inclusive) | 最大长度（包含）
         * @return this builder | 此构建器
         * @throws NullPointerException     if column is null | 如果column为null
         * @throws IllegalArgumentException if max < 0 | 如果max小于0
         */
        public Builder maxLength(String column, int max) {
            Objects.requireNonNull(column, "column must not be null");
            if (max < 0) {
                throw new IllegalArgumentException("max must not be negative: " + max);
            }
            rules.add(new Rule(
                    column,
                    v -> v == null || v.length() <= max,
                    "maxLength[" + max + "]",
                    "Column '" + column + "' value '{value}' exceeds maximum length " + max
            ));
            return this;
        }

        /**
         * Adds an allowed-values rule: value must be one of the specified values (case-sensitive)
         * 添加允许值规则：值必须是指定值之一（区分大小写）
         *
         * @param column        the column name | 列名
         * @param allowedValues the allowed values | 允许的值
         * @return this builder | 此构建器
         * @throws NullPointerException     if column or allowedValues is null | 如果column或allowedValues为null
         * @throws IllegalArgumentException if allowedValues is empty | 如果allowedValues为空
         */
        public Builder oneOf(String column, String... allowedValues) {
            Objects.requireNonNull(column, "column must not be null");
            Objects.requireNonNull(allowedValues, "allowedValues must not be null");
            if (allowedValues.length == 0) {
                throw new IllegalArgumentException("allowedValues must not be empty");
            }
            Set<String> allowed = Set.of(allowedValues);
            rules.add(new Rule(
                    column,
                    v -> v != null && allowed.contains(v),
                    "oneOf" + allowed,
                    "Column '" + column + "' value '{value}' is not one of " + allowed
            ));
            return this;
        }

        /**
         * Adds a custom validation rule with a predicate and error message
         * 添加带有谓词和错误消息的自定义验证规则
         *
         * @param column       the column name | 列名
         * @param rule         the validation predicate (value may be null) | 验证谓词（值可能为null）
         * @param errorMessage the error message template (may contain {value}) | 错误消息模板（可包含{value}）
         * @return this builder | 此构建器
         * @throws NullPointerException if any argument is null | 如果任何参数为null
         */
        public Builder custom(String column, Predicate<String> rule, String errorMessage) {
            Objects.requireNonNull(column, "column must not be null");
            Objects.requireNonNull(rule, "rule must not be null");
            Objects.requireNonNull(errorMessage, "errorMessage must not be null");
            rules.add(new Rule(column, rule, "custom", errorMessage));
            return this;
        }

        /**
         * Builds the CsvValidator
         * 构建CsvValidator
         *
         * @return the CsvValidator instance | CsvValidator实例
         */
        public CsvValidator build() {
            return new CsvValidator(rules);
        }
    }
}
