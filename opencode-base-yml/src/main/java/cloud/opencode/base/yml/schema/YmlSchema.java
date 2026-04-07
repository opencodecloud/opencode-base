package cloud.opencode.base.yml.schema;

import cloud.opencode.base.yml.YmlDocument;
import cloud.opencode.base.yml.path.PathResolver;
import cloud.opencode.base.yml.path.YmlPath;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * YmlSchema - Lightweight structural validator for YAML documents
 * YmlSchema - 轻量级 YAML 文档结构验证器
 *
 * <p>Provides a builder-based API for defining validation rules on YAML data,
 * including required keys, type constraints, value ranges, regex patterns,
 * nested schemas, and custom predicate rules.</p>
 * <p>提供基于构建器的 API 来定义 YAML 数据的验证规则，
 * 包括必需键、类型约束、值范围、正则模式、嵌套模式和自定义谓词规则。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Required key validation at root level - 根级别必需键验证</li>
 *   <li>Type constraint validation via path - 通过路径的类型约束验证</li>
 *   <li>Value range validation for Comparable types - Comparable 类型的值范围验证</li>
 *   <li>Regex pattern validation for string values - 字符串值的正则模式验证</li>
 *   <li>Nested schema validation for sub-documents - 子文档的嵌套模式验证</li>
 *   <li>Custom predicate-based validation rules - 基于自定义谓词的验证规则</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * YmlSchema schema = YmlSchema.builder()
 *     .required("name", "version")
 *     .type("name", String.class)
 *     .type("port", Integer.class)
 *     .range("port", 1, 65535)
 *     .pattern("email", "^[\\w@.]+$")
 *     .nested("database", YmlSchema.builder()
 *         .required("url")
 *         .build())
 *     .rule("name", v -> ((String) v).length() <= 100, "Name too long")
 *     .build();
 *
 * ValidationResult result = schema.validate(data);
 * if (!result.isValid()) {
 *     result.getErrors().forEach(System.err::println);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public final class YmlSchema {

    private final Set<String> requiredKeys;
    private final Map<String, Class<?>> typeConstraints;
    private final Map<String, RangeConstraint> rangeConstraints;
    private final Map<String, Pattern> patternConstraints;
    private final Map<String, YmlSchema> nestedSchemas;
    private final Map<String, List<CustomRule>> customRules;

    private YmlSchema(Builder builder) {
        this.requiredKeys = Collections.unmodifiableSet(new LinkedHashSet<>(builder.requiredKeys));
        this.typeConstraints = Collections.unmodifiableMap(new LinkedHashMap<>(builder.typeConstraints));
        this.rangeConstraints = Collections.unmodifiableMap(new LinkedHashMap<>(builder.rangeConstraints));
        this.patternConstraints = Collections.unmodifiableMap(new LinkedHashMap<>(builder.patternConstraints));
        this.nestedSchemas = Collections.unmodifiableMap(new LinkedHashMap<>(builder.nestedSchemas));
        // Deep-copy custom rules map to ensure immutability
        Map<String, List<CustomRule>> rulesCopy = new LinkedHashMap<>();
        for (Map.Entry<String, List<CustomRule>> entry : builder.customRules.entrySet()) {
            rulesCopy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        this.customRules = Collections.unmodifiableMap(rulesCopy);
    }

    /**
     * Creates a new schema builder.
     * 创建新的模式构建器。
     *
     * @return a new builder | 新的构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Validates a Map against this schema.
     * 根据此模式验证 Map。
     *
     * @param data the data to validate | 要验证的数据
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(Map<String, Object> data) {
        if (data == null) {
            List<ValidationError> errors = List.of(
                new ValidationError("", "Data must not be null", ValidationError.ErrorType.MISSING_REQUIRED)
            );
            return ValidationResult.failure(errors);
        }
        List<ValidationError> errors = new ArrayList<>();
        doValidate(data, "", errors);
        if (errors.isEmpty()) {
            return ValidationResult.success();
        }
        return ValidationResult.failure(errors);
    }

    /**
     * Validates a YmlDocument against this schema.
     * 根据此模式验证 YmlDocument。
     *
     * @param doc the document to validate | 要验证的文档
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(YmlDocument doc) {
        if (doc == null) {
            List<ValidationError> errors = List.of(
                new ValidationError("", "Document must not be null", ValidationError.ErrorType.MISSING_REQUIRED)
            );
            return ValidationResult.failure(errors);
        }
        return validate(doc.asMap());
    }

    /**
     * Performs validation, collecting all errors.
     */
    private void doValidate(Map<String, Object> data, String prefix, List<ValidationError> errors) {
        // Check required keys
        for (String key : requiredKeys) {
            if (!data.containsKey(key)) {
                String fullPath = prefix.isEmpty() ? key : prefix + "." + key;
                errors.add(new ValidationError(
                    fullPath,
                    "Required key '" + key + "' is missing",
                    ValidationError.ErrorType.MISSING_REQUIRED
                ));
            }
        }

        // Check type constraints
        for (Map.Entry<String, Class<?>> entry : typeConstraints.entrySet()) {
            String path = entry.getKey();
            Class<?> expectedType = entry.getValue();
            String fullPath = prefix.isEmpty() ? path : prefix + "." + path;

            Object value = resolvePath(data, path);
            if (value != null && !expectedType.isInstance(value)) {
                errors.add(new ValidationError(
                    fullPath,
                    "Expected " + expectedType.getSimpleName() + " but got " + value.getClass().getSimpleName(),
                    ValidationError.ErrorType.TYPE_MISMATCH
                ));
            }
        }

        // Check range constraints
        for (Map.Entry<String, RangeConstraint> entry : rangeConstraints.entrySet()) {
            String path = entry.getKey();
            RangeConstraint range = entry.getValue();
            String fullPath = prefix.isEmpty() ? path : prefix + "." + path;

            Object value = resolvePath(data, path);
            if (value instanceof Comparable<?>) {
                validateRange(fullPath, value, range, errors);
            }
        }

        // Check pattern constraints
        for (Map.Entry<String, Pattern> entry : patternConstraints.entrySet()) {
            String path = entry.getKey();
            Pattern pattern = entry.getValue();
            String fullPath = prefix.isEmpty() ? path : prefix + "." + path;

            Object value = resolvePath(data, path);
            if (value != null) {
                String str = value.toString();
                if (!pattern.matcher(str).matches()) {
                    errors.add(new ValidationError(
                        fullPath,
                        "Value '" + str + "' does not match pattern: " + pattern.pattern(),
                        ValidationError.ErrorType.PATTERN_MISMATCH
                    ));
                }
            }
        }

        // Check nested schemas
        for (Map.Entry<String, YmlSchema> entry : nestedSchemas.entrySet()) {
            String path = entry.getKey();
            YmlSchema nestedSchema = entry.getValue();
            String fullPath = prefix.isEmpty() ? path : prefix + "." + path;

            Object value = resolvePath(data, path);
            if (value instanceof Map<?, ?> nestedMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedData = (Map<String, Object>) nestedMap;
                nestedSchema.doValidate(nestedData, fullPath, errors);
            } else if (value != null) {
                errors.add(new ValidationError(
                    fullPath,
                    "Expected a map for nested schema but got " + value.getClass().getSimpleName(),
                    ValidationError.ErrorType.TYPE_MISMATCH
                ));
            }
        }

        // Check custom rules
        for (Map.Entry<String, List<CustomRule>> entry : customRules.entrySet()) {
            String path = entry.getKey();
            String fullPath = prefix.isEmpty() ? path : prefix + "." + path;

            Object value = resolvePath(data, path);
            if (value != null) {
                for (CustomRule rule : entry.getValue()) {
                    try {
                        if (!rule.predicate().test(value)) {
                            errors.add(new ValidationError(
                                fullPath,
                                rule.message(),
                                ValidationError.ErrorType.CUSTOM_RULE_FAILED
                            ));
                        }
                    } catch (RuntimeException e) {
                        errors.add(new ValidationError(
                            fullPath,
                            "Custom rule failed with exception: " + e.getMessage(),
                            ValidationError.ErrorType.CUSTOM_RULE_FAILED
                        ));
                    }
                }
            }
        }
    }

    /**
     * Resolves a path within a data map using PathResolver.
     */
    private static Object resolvePath(Map<String, Object> data, String path) {
        return PathResolver.resolve(data, YmlPath.of(path));
    }

    /**
     * Validates that a value is within the specified range.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void validateRange(String fullPath, Object value, RangeConstraint range,
                                      List<ValidationError> errors) {
        Comparable comp = (Comparable) value;
        Comparable min = range.min();
        Comparable max = range.max();

        try {
            if (min != null && comp.compareTo(min) < 0) {
                errors.add(new ValidationError(
                    fullPath,
                    "Value " + value + " is below minimum " + min,
                    ValidationError.ErrorType.OUT_OF_RANGE
                ));
            }
            if (max != null && comp.compareTo(max) > 0) {
                errors.add(new ValidationError(
                    fullPath,
                    "Value " + value + " is above maximum " + max,
                    ValidationError.ErrorType.OUT_OF_RANGE
                ));
            }
        } catch (ClassCastException e) {
            errors.add(new ValidationError(
                fullPath,
                "Cannot compare value of type " + value.getClass().getSimpleName()
                    + " with range bounds",
                ValidationError.ErrorType.TYPE_MISMATCH
            ));
        }
    }

    /**
     * Internal record for range constraints.
     */
    private record RangeConstraint(Comparable<?> min, Comparable<?> max) {}

    /**
     * Internal record for custom rules.
     */
    private record CustomRule(Predicate<Object> predicate, String message) {}

    /**
     * Builder for constructing YmlSchema instances.
     * 用于构建 YmlSchema 实例的构建器。
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-yml V1.0.3
     */
    public static final class Builder {
        private final Set<String> requiredKeys = new LinkedHashSet<>();
        private final Map<String, Class<?>> typeConstraints = new LinkedHashMap<>();
        private final Map<String, RangeConstraint> rangeConstraints = new LinkedHashMap<>();
        private final Map<String, Pattern> patternConstraints = new LinkedHashMap<>();
        private final Map<String, YmlSchema> nestedSchemas = new LinkedHashMap<>();
        private final Map<String, List<CustomRule>> customRules = new LinkedHashMap<>();

        private Builder() {}

        /**
         * Adds required keys at the root level.
         * 在根级别添加必需键。
         *
         * @param keys the required key names | 必需的键名
         * @return this builder | 此构建器
         */
        public Builder required(String... keys) {
            Objects.requireNonNull(keys, "Keys must not be null");
            for (String key : keys) {
                Objects.requireNonNull(key, "Key must not be null");
                requiredKeys.add(key);
            }
            return this;
        }

        /**
         * Adds a type constraint: the value at the given path must be assignable to the expected type.
         * 添加类型约束：给定路径处的值必须可赋值给预期类型。
         *
         * @param path         the dot-notation path | 点号路径
         * @param expectedType the expected type | 预期类型
         * @return this builder | 此构建器
         */
        public Builder type(String path, Class<?> expectedType) {
            Objects.requireNonNull(path, "Path must not be null");
            Objects.requireNonNull(expectedType, "Expected type must not be null");
            typeConstraints.put(path, expectedType);
            return this;
        }

        /**
         * Adds a range constraint for Comparable values at the given path.
         * 为给定路径的 Comparable 值添加范围约束。
         *
         * @param path the dot-notation path | 点号路径
         * @param min  the minimum value (inclusive), or null for no lower bound | 最小值（含），null 表示无下界
         * @param max  the maximum value (inclusive), or null for no upper bound | 最大值（含），null 表示无上界
         * @return this builder | 此构建器
         */
        public Builder range(String path, Comparable<?> min, Comparable<?> max) {
            Objects.requireNonNull(path, "Path must not be null");
            rangeConstraints.put(path, new RangeConstraint(min, max));
            return this;
        }

        /**
         * Adds a regex pattern constraint for string values at the given path.
         * 为给定路径的字符串值添加正则模式约束。
         *
         * @param path  the dot-notation path | 点号路径
         * @param regex the regex pattern | 正则模式
         * @return this builder | 此构建器
         * @throws PatternSyntaxException if the regex is invalid | 如果正则无效
         */
        public Builder pattern(String path, String regex) {
            Objects.requireNonNull(path, "Path must not be null");
            Objects.requireNonNull(regex, "Regex must not be null");
            patternConstraints.put(path, Pattern.compile(regex));
            return this;
        }

        /**
         * Adds a nested schema for validating a sub-document at the given path.
         * 添加嵌套模式以验证给定路径处的子文档。
         *
         * @param path   the dot-notation path | 点号路径
         * @param schema the nested schema | 嵌套模式
         * @return this builder | 此构建器
         */
        public Builder nested(String path, YmlSchema schema) {
            Objects.requireNonNull(path, "Path must not be null");
            Objects.requireNonNull(schema, "Schema must not be null");
            nestedSchemas.put(path, schema);
            return this;
        }

        /**
         * Adds a custom validation rule at the given path.
         * 在给定路径添加自定义验证规则。
         *
         * @param path      the dot-notation path | 点号路径
         * @param predicate the validation predicate | 验证谓词
         * @param message   the error message if the predicate returns false | 谓词返回 false 时的错误消息
         * @return this builder | 此构建器
         */
        public Builder rule(String path, Predicate<Object> predicate, String message) {
            Objects.requireNonNull(path, "Path must not be null");
            Objects.requireNonNull(predicate, "Predicate must not be null");
            Objects.requireNonNull(message, "Message must not be null");
            customRules.computeIfAbsent(path, k -> new ArrayList<>())
                .add(new CustomRule(predicate, message));
            return this;
        }

        /**
         * Builds the immutable YmlSchema.
         * 构建不可变的 YmlSchema。
         *
         * @return the schema | 模式
         */
        public YmlSchema build() {
            return new YmlSchema(this);
        }
    }
}
