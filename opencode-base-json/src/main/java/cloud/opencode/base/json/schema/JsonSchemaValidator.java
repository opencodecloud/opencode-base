
package cloud.opencode.base.json.schema;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.JsonSchemaException;
import cloud.opencode.base.json.exception.JsonSchemaException.ValidationError;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * JSON Schema Validator - JSON Schema Draft 2020-12 Implementation
 * JSON Schema 验证器 - JSON Schema Draft 2020-12 实现
 *
 * <p>This class validates JSON documents against JSON Schema specifications.
 * It supports a subset of JSON Schema Draft 2020-12 keywords.</p>
 * <p>此类根据 JSON Schema 规范验证 JSON 文档。
 * 它支持 JSON Schema Draft 2020-12 关键字的子集。</p>
 *
 * <p><strong>Supported Keywords | 支持的关键字:</strong></p>
 * <ul>
 *   <li>Type: type, enum, const</li>
 *   <li>String: minLength, maxLength, pattern, format</li>
 *   <li>Number: minimum, maximum, exclusiveMinimum, exclusiveMaximum, multipleOf</li>
 *   <li>Array: minItems, maxItems, uniqueItems, items</li>
 *   <li>Object: properties, required, additionalProperties, minProperties, maxProperties</li>
 *   <li>Composition: allOf, anyOf, oneOf, not</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * JsonNode schema = OpenJson.parse("""
 *     {
 *         "type": "object",
 *         "properties": {
 *             "name": {"type": "string", "minLength": 1},
 *             "age": {"type": "integer", "minimum": 0}
 *         },
 *         "required": ["name"]
 *     }
 *     """);
 *
 * JsonNode data = OpenJson.parse("{\"name\":\"John\",\"age\":30}");
 *
 * ValidationResult result = JsonSchemaValidator.validate(data, schema);
 * if (!result.isValid()) {
 *     result.getErrors().forEach(System.out::println);
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JSON Schema Draft 2020-12 keyword validation - JSON Schema Draft 2020-12关键字验证</li>
 *   <li>Type, string, number, array, object, and composition validation - 类型、字符串、数字、数组、对象和组合验证</li>
 *   <li>Cached regex pattern compilation for performance - 缓存的正则模式编译提升性能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://json-schema.org/draft/2020-12/json-schema-core.html">JSON Schema Draft 2020-12</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class JsonSchemaValidator {

    /**
     * Validation result record.
     * 验证结果记录。
     */
    public record ValidationResult(
            boolean valid,
            List<ValidationError> errors
    ) {
        public static ValidationResult success() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult failure(List<ValidationError> errors) {
            return new ValidationResult(false, List.copyOf(errors));
        }

        public static ValidationResult failure(ValidationError error) {
            return new ValidationResult(false, List.of(error));
        }

        public boolean isValid() { return valid; }
        public List<ValidationError> getErrors() { return errors; }
    }

    private final JsonNode schema;
    /**
     * Maximum number of cached compiled patterns to prevent unbounded memory growth.
     * 缓存编译模式的最大数量以防止无限内存增长。
     */
    private static final int MAX_PATTERN_CACHE_SIZE = 1000;
    /**
     * Thread-safe pattern cache for regex validation with bounded size.
     * 用于正则验证的线程安全且大小有限的模式缓存。
     */
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    private JsonSchemaValidator(JsonNode schema) {
        this.schema = Objects.requireNonNull(schema, "Schema must not be null");
    }

    /**
     * Creates a validator for the given schema.
     * 为给定 schema 创建验证器。
     *
     * @param schema the JSON Schema - JSON Schema
     * @return the validator - 验证器
     */
    public static JsonSchemaValidator of(JsonNode schema) {
        return new JsonSchemaValidator(schema);
    }

    /**
     * Validates data against a schema.
     * 根据 schema 验证数据。
     *
     * @param data   the data to validate - 要验证的数据
     * @param schema the JSON Schema - JSON Schema
     * @return the validation result - 验证结果
     */
    public static ValidationResult validate(JsonNode data, JsonNode schema) {
        return of(schema).validate(data);
    }

    /**
     * Validates data and throws if invalid.
     * 验证数据，如果无效则抛出异常。
     *
     * @param data   the data to validate - 要验证的数据
     * @param schema the JSON Schema - JSON Schema
     * @throws JsonSchemaException if validation fails - 如果验证失败
     */
    public static void validateOrThrow(JsonNode data, JsonNode schema) {
        ValidationResult result = validate(data, schema);
        if (!result.isValid()) {
            throw new JsonSchemaException("Schema validation failed", result.errors());
        }
    }

    /**
     * Validates data against this validator's schema.
     * 根据此验证器的 schema 验证数据。
     *
     * @param data the data to validate - 要验证的数据
     * @return the validation result - 验证结果
     */
    public ValidationResult validate(JsonNode data) {
        List<ValidationError> errors = new ArrayList<>();
        validateNode(data, schema, "", errors);
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * Validates and throws if invalid.
     * 验证，如果无效则抛出异常。
     *
     * @param data the data to validate - 要验证的数据
     * @throws JsonSchemaException if validation fails - 如果验证失败
     */
    public void validateOrThrow(JsonNode data) {
        ValidationResult result = validate(data);
        if (!result.isValid()) {
            throw new JsonSchemaException("Schema validation failed", result.errors());
        }
    }

    private void validateNode(JsonNode data, JsonNode schema, String path, List<ValidationError> errors) {
        if (schema.isBoolean()) {
            if (!schema.asBoolean()) {
                errors.add(new ValidationError(path, "false", "Schema is false, validation always fails"));
            }
            return;
        }

        if (!schema.isObject()) {
            return;
        }

        // type validation
        validateType(data, schema, path, errors);

        // enum validation
        validateEnum(data, schema, path, errors);

        // const validation
        validateConst(data, schema, path, errors);

        // string validations
        if (data.isString()) {
            validateString(data, schema, path, errors);
        }

        // number validations
        if (data.isNumber()) {
            validateNumber(data, schema, path, errors);
        }

        // array validations
        if (data.isArray()) {
            validateArray(data, schema, path, errors);
        }

        // object validations
        if (data.isObject()) {
            validateObject(data, schema, path, errors);
        }

        // composition validations
        validateComposition(data, schema, path, errors);
    }

    private void validateType(JsonNode data, JsonNode schema, String path, List<ValidationError> errors) {
        JsonNode typeNode = schema.get("type");
        if (typeNode == null) return;

        List<String> allowedTypes = new ArrayList<>();
        if (typeNode.isString()) {
            allowedTypes.add(typeNode.asString());
        } else if (typeNode.isArray()) {
            for (int i = 0; i < typeNode.size(); i++) {
                allowedTypes.add(typeNode.get(i).asString());
            }
        }

        String actualType = getJsonType(data);
        boolean matches = allowedTypes.stream().anyMatch(t -> matchesType(data, t));

        if (!matches) {
            errors.add(new ValidationError(path, "type",
                    "Expected type " + allowedTypes + ", got " + actualType,
                    allowedTypes, actualType));
        }
    }

    private boolean matchesType(JsonNode data, String type) {
        return switch (type) {
            case "string" -> data.isString();
            case "number" -> data.isNumber();
            case "integer" -> data.isNumber() && isInteger(data);
            case "boolean" -> data.isBoolean();
            case "null" -> data.isNull();
            case "array" -> data.isArray();
            case "object" -> data.isObject();
            default -> false;
        };
    }

    private boolean isInteger(JsonNode data) {
        double value = data.asDouble();
        return value == Math.floor(value) && !Double.isInfinite(value);
    }

    private String getJsonType(JsonNode data) {
        return switch (data) {
            case JsonNode.StringNode _ -> "string";
            case JsonNode.NumberNode _ -> isInteger(data) ? "integer" : "number";
            case JsonNode.BooleanNode _ -> "boolean";
            case JsonNode.NullNode _ -> "null";
            case JsonNode.ArrayNode _ -> "array";
            case JsonNode.ObjectNode _ -> "object";
        };
    }

    private void validateEnum(JsonNode data, JsonNode schema, String path, List<ValidationError> errors) {
        JsonNode enumNode = schema.get("enum");
        if (enumNode == null || !enumNode.isArray()) return;

        boolean found = false;
        for (int i = 0; i < enumNode.size(); i++) {
            if (nodesEqual(data, enumNode.get(i))) {
                found = true;
                break;
            }
        }

        if (!found) {
            errors.add(new ValidationError(path, "enum",
                    "Value not in enum: " + data));
        }
    }

    private void validateConst(JsonNode data, JsonNode schema, String path, List<ValidationError> errors) {
        JsonNode constNode = schema.get("const");
        if (constNode == null) return;

        if (!nodesEqual(data, constNode)) {
            errors.add(new ValidationError(path, "const",
                    "Value does not match const: expected " + constNode + ", got " + data));
        }
    }

    private void validateString(JsonNode data, JsonNode schema, String path, List<ValidationError> errors) {
        String value = data.asString();

        // minLength
        JsonNode minLength = schema.get("minLength");
        if (minLength != null && value.length() < minLength.asInt()) {
            errors.add(new ValidationError(path, "minLength",
                    "String length " + value.length() + " is less than minLength " + minLength.asInt()));
        }

        // maxLength
        JsonNode maxLength = schema.get("maxLength");
        if (maxLength != null && value.length() > maxLength.asInt()) {
            errors.add(new ValidationError(path, "maxLength",
                    "String length " + value.length() + " exceeds maxLength " + maxLength.asInt()));
        }

        // pattern
        JsonNode patternNode = schema.get("pattern");
        if (patternNode != null) {
            String patternStr = patternNode.asString();
            try {
                Pattern pattern = patternCache.get(patternStr);
                if (pattern == null) {
                    pattern = Pattern.compile(patternStr);
                    // Only cache if under the size limit to prevent unbounded growth
                    if (patternCache.size() < MAX_PATTERN_CACHE_SIZE) {
                        patternCache.putIfAbsent(patternStr, pattern);
                        pattern = patternCache.get(patternStr);
                    }
                }
                if (!pattern.matcher(value).find()) {
                    errors.add(new ValidationError(path, "pattern",
                            "String does not match pattern: " + patternStr));
                }
            } catch (PatternSyntaxException e) {
                errors.add(new ValidationError(path, "pattern",
                        "Invalid regex pattern: " + patternStr));
            }
        }
    }

    private void validateNumber(JsonNode data, JsonNode schema, String path, List<ValidationError> errors) {
        double value = data.asDouble();

        // minimum
        JsonNode minimum = schema.get("minimum");
        if (minimum != null && value < minimum.asDouble()) {
            errors.add(new ValidationError(path, "minimum",
                    "Value " + value + " is less than minimum " + minimum.asDouble()));
        }

        // maximum
        JsonNode maximum = schema.get("maximum");
        if (maximum != null && value > maximum.asDouble()) {
            errors.add(new ValidationError(path, "maximum",
                    "Value " + value + " exceeds maximum " + maximum.asDouble()));
        }

        // exclusiveMinimum
        JsonNode exMin = schema.get("exclusiveMinimum");
        if (exMin != null && value <= exMin.asDouble()) {
            errors.add(new ValidationError(path, "exclusiveMinimum",
                    "Value " + value + " must be greater than " + exMin.asDouble()));
        }

        // exclusiveMaximum
        JsonNode exMax = schema.get("exclusiveMaximum");
        if (exMax != null && value >= exMax.asDouble()) {
            errors.add(new ValidationError(path, "exclusiveMaximum",
                    "Value " + value + " must be less than " + exMax.asDouble()));
        }

        // multipleOf
        JsonNode multipleOf = schema.get("multipleOf");
        if (multipleOf != null) {
            double divisor = multipleOf.asDouble();
            if (divisor != 0) {
                // Use BigDecimal for precise multipleOf check to avoid floating-point issues
                java.math.BigDecimal bdValue = java.math.BigDecimal.valueOf(value);
                java.math.BigDecimal bdDivisor = java.math.BigDecimal.valueOf(divisor);
                try {
                    bdValue.remainder(bdDivisor);
                    if (bdValue.remainder(bdDivisor).compareTo(java.math.BigDecimal.ZERO) != 0) {
                        errors.add(new ValidationError(path, "multipleOf",
                                "Value " + value + " is not a multiple of " + divisor));
                    }
                } catch (ArithmeticException e) {
                    // If remainder cannot be computed exactly, fall back to double
                    double remainder = value % divisor;
                    if (Math.abs(remainder) > 1e-10 && Math.abs(remainder - divisor) > 1e-10) {
                        errors.add(new ValidationError(path, "multipleOf",
                                "Value " + value + " is not a multiple of " + divisor));
                    }
                }
            }
        }
    }

    private void validateArray(JsonNode data, JsonNode schema, String path, List<ValidationError> errors) {
        int size = data.size();

        // minItems
        JsonNode minItems = schema.get("minItems");
        if (minItems != null && size < minItems.asInt()) {
            errors.add(new ValidationError(path, "minItems",
                    "Array has " + size + " items, minimum is " + minItems.asInt()));
        }

        // maxItems
        JsonNode maxItems = schema.get("maxItems");
        if (maxItems != null && size > maxItems.asInt()) {
            errors.add(new ValidationError(path, "maxItems",
                    "Array has " + size + " items, maximum is " + maxItems.asInt()));
        }

        // uniqueItems
        JsonNode uniqueItems = schema.get("uniqueItems");
        if (uniqueItems != null && uniqueItems.asBoolean()) {
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < size; i++) {
                String repr = data.get(i).toString();
                if (!seen.add(repr)) {
                    errors.add(new ValidationError(path, "uniqueItems",
                            "Array contains duplicate items"));
                    break;
                }
            }
        }

        // items
        JsonNode items = schema.get("items");
        if (items != null) {
            for (int i = 0; i < size; i++) {
                validateNode(data.get(i), items, path + "/" + i, errors);
            }
        }
    }

    private void validateObject(JsonNode data, JsonNode schema, String path, List<ValidationError> errors) {
        Set<String> keys = data.keys();

        // required
        JsonNode required = schema.get("required");
        if (required != null && required.isArray()) {
            for (int i = 0; i < required.size(); i++) {
                String reqKey = required.get(i).asString();
                if (!data.has(reqKey)) {
                    errors.add(new ValidationError(path, "required",
                            "Missing required property: " + reqKey));
                }
            }
        }

        // minProperties
        JsonNode minProps = schema.get("minProperties");
        if (minProps != null && keys.size() < minProps.asInt()) {
            errors.add(new ValidationError(path, "minProperties",
                    "Object has " + keys.size() + " properties, minimum is " + minProps.asInt()));
        }

        // maxProperties
        JsonNode maxProps = schema.get("maxProperties");
        if (maxProps != null && keys.size() > maxProps.asInt()) {
            errors.add(new ValidationError(path, "maxProperties",
                    "Object has " + keys.size() + " properties, maximum is " + maxProps.asInt()));
        }

        // properties
        JsonNode properties = schema.get("properties");
        Set<String> validatedProps = new HashSet<>();
        if (properties != null && properties.isObject()) {
            for (String key : properties.keys()) {
                validatedProps.add(key);
                if (data.has(key)) {
                    validateNode(data.get(key), properties.get(key), path + "/" + key, errors);
                }
            }
        }

        // additionalProperties
        JsonNode additionalProps = schema.get("additionalProperties");
        if (additionalProps != null) {
            for (String key : keys) {
                if (!validatedProps.contains(key)) {
                    if (additionalProps.isBoolean() && !additionalProps.asBoolean()) {
                        errors.add(new ValidationError(path, "additionalProperties",
                                "Additional property not allowed: " + key));
                    } else if (additionalProps.isObject()) {
                        validateNode(data.get(key), additionalProps, path + "/" + key, errors);
                    }
                }
            }
        }
    }

    private void validateComposition(JsonNode data, JsonNode schema, String path, List<ValidationError> errors) {
        // allOf
        JsonNode allOf = schema.get("allOf");
        if (allOf != null && allOf.isArray()) {
            for (int i = 0; i < allOf.size(); i++) {
                validateNode(data, allOf.get(i), path, errors);
            }
        }

        // anyOf
        JsonNode anyOf = schema.get("anyOf");
        if (anyOf != null && anyOf.isArray()) {
            boolean anyValid = false;
            for (int i = 0; i < anyOf.size(); i++) {
                List<ValidationError> subErrors = new ArrayList<>();
                validateNode(data, anyOf.get(i), path, subErrors);
                if (subErrors.isEmpty()) {
                    anyValid = true;
                    break;
                }
            }
            if (!anyValid) {
                errors.add(new ValidationError(path, "anyOf",
                        "Value does not match any schema in anyOf"));
            }
        }

        // oneOf
        JsonNode oneOf = schema.get("oneOf");
        if (oneOf != null && oneOf.isArray()) {
            int validCount = 0;
            for (int i = 0; i < oneOf.size(); i++) {
                List<ValidationError> subErrors = new ArrayList<>();
                validateNode(data, oneOf.get(i), path, subErrors);
                if (subErrors.isEmpty()) {
                    validCount++;
                }
            }
            if (validCount != 1) {
                errors.add(new ValidationError(path, "oneOf",
                        "Value must match exactly one schema in oneOf, matched " + validCount));
            }
        }

        // not
        JsonNode not = schema.get("not");
        if (not != null) {
            List<ValidationError> subErrors = new ArrayList<>();
            validateNode(data, not, path, subErrors);
            if (subErrors.isEmpty()) {
                errors.add(new ValidationError(path, "not",
                        "Value must not match the schema in 'not'"));
            }
        }
    }

    private boolean nodesEqual(JsonNode a, JsonNode b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
