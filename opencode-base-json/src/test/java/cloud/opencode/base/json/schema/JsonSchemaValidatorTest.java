package cloud.opencode.base.json.schema;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.JsonSchemaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonSchemaValidator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonSchemaValidator 测试")
class JsonSchemaValidatorTest {

    @Nested
    @DisplayName("of方法测试")
    class OfMethodTests {

        @Test
        @DisplayName("创建验证器")
        void testOf() {
            JsonNode schema = JsonNode.object().put("type", "string");
            JsonSchemaValidator validator = JsonSchemaValidator.of(schema);

            assertThat(validator).isNotNull();
        }

        @Test
        @DisplayName("null schema抛出异常")
        void testOfNullSchema() {
            assertThatThrownBy(() -> JsonSchemaValidator.of(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("type验证测试")
    class TypeValidationTests {

        @Test
        @DisplayName("验证字符串类型")
        void testValidateStringType() {
            JsonNode schema = JsonNode.object().put("type", "string");
            JsonNode data = JsonNode.of("hello");

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("验证数字类型")
        void testValidateNumberType() {
            JsonNode schema = JsonNode.object().put("type", "number");
            JsonNode data = JsonNode.of(42.5);

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("验证整数类型")
        void testValidateIntegerType() {
            JsonNode schema = JsonNode.object().put("type", "integer");
            JsonNode data = JsonNode.of(42);

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("验证布尔类型")
        void testValidateBooleanType() {
            JsonNode schema = JsonNode.object().put("type", "boolean");
            JsonNode data = JsonNode.of(true);

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("验证null类型")
        void testValidateNullType() {
            JsonNode schema = JsonNode.object().put("type", "null");
            JsonNode data = JsonNode.nullNode();

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("验证数组类型")
        void testValidateArrayType() {
            JsonNode schema = JsonNode.object().put("type", "array");
            JsonNode data = JsonNode.array().add(1).add(2);

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("验证对象类型")
        void testValidateObjectType() {
            JsonNode schema = JsonNode.object().put("type", "object");
            JsonNode data = JsonNode.object().put("name", "test");

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("类型不匹配验证失败")
        void testValidateTypeFailure() {
            JsonNode schema = JsonNode.object().put("type", "string");
            JsonNode data = JsonNode.of(42);

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).isNotEmpty();
        }

        @Test
        @DisplayName("多类型验证")
        void testValidateMultipleTypes() {
            JsonNode schema = JsonNode.object()
                .put("type", JsonNode.array().add("string").add("number"));
            JsonNode stringData = JsonNode.of("hello");
            JsonNode numberData = JsonNode.of(42);

            assertThat(JsonSchemaValidator.validate(stringData, schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(numberData, schema).isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("enum验证测试")
    class EnumValidationTests {

        @Test
        @DisplayName("值在枚举中验证通过")
        void testValidateEnumValid() {
            JsonNode schema = JsonNode.object()
                .put("enum", JsonNode.array().add("a").add("b").add("c"));
            JsonNode data = JsonNode.of("b");

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("值不在枚举中验证失败")
        void testValidateEnumInvalid() {
            JsonNode schema = JsonNode.object()
                .put("enum", JsonNode.array().add("a").add("b"));
            JsonNode data = JsonNode.of("c");

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("const验证测试")
    class ConstValidationTests {

        @Test
        @DisplayName("值匹配const验证通过")
        void testValidateConstValid() {
            JsonNode schema = JsonNode.object().put("const", "expected");
            JsonNode data = JsonNode.of("expected");

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("值不匹配const验证失败")
        void testValidateConstInvalid() {
            JsonNode schema = JsonNode.object().put("const", "expected");
            JsonNode data = JsonNode.of("actual");

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("字符串验证测试")
    class StringValidationTests {

        @Test
        @DisplayName("验证minLength")
        void testValidateMinLength() {
            JsonNode schema = JsonNode.object()
                .put("type", "string")
                .put("minLength", 3);

            assertThat(JsonSchemaValidator.validate(JsonNode.of("abc"), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.of("ab"), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证maxLength")
        void testValidateMaxLength() {
            JsonNode schema = JsonNode.object()
                .put("type", "string")
                .put("maxLength", 5);

            assertThat(JsonSchemaValidator.validate(JsonNode.of("abc"), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.of("abcdef"), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证pattern")
        void testValidatePattern() {
            JsonNode schema = JsonNode.object()
                .put("type", "string")
                .put("pattern", "^[a-z]+$");

            assertThat(JsonSchemaValidator.validate(JsonNode.of("abc"), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.of("ABC"), schema).isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("数字验证测试")
    class NumberValidationTests {

        @Test
        @DisplayName("验证minimum")
        void testValidateMinimum() {
            JsonNode schema = JsonNode.object()
                .put("type", "number")
                .put("minimum", 10);

            assertThat(JsonSchemaValidator.validate(JsonNode.of(10), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.of(9), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证maximum")
        void testValidateMaximum() {
            JsonNode schema = JsonNode.object()
                .put("type", "number")
                .put("maximum", 100);

            assertThat(JsonSchemaValidator.validate(JsonNode.of(100), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.of(101), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证exclusiveMinimum")
        void testValidateExclusiveMinimum() {
            JsonNode schema = JsonNode.object()
                .put("type", "number")
                .put("exclusiveMinimum", 10);

            assertThat(JsonSchemaValidator.validate(JsonNode.of(11), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.of(10), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证exclusiveMaximum")
        void testValidateExclusiveMaximum() {
            JsonNode schema = JsonNode.object()
                .put("type", "number")
                .put("exclusiveMaximum", 100);

            assertThat(JsonSchemaValidator.validate(JsonNode.of(99), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.of(100), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证multipleOf")
        void testValidateMultipleOf() {
            JsonNode schema = JsonNode.object()
                .put("type", "number")
                .put("multipleOf", 5);

            assertThat(JsonSchemaValidator.validate(JsonNode.of(10), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.of(12), schema).isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("数组验证测试")
    class ArrayValidationTests {

        @Test
        @DisplayName("验证minItems")
        void testValidateMinItems() {
            JsonNode schema = JsonNode.object()
                .put("type", "array")
                .put("minItems", 2);

            assertThat(JsonSchemaValidator.validate(JsonNode.array().add(1).add(2), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.array().add(1), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证maxItems")
        void testValidateMaxItems() {
            JsonNode schema = JsonNode.object()
                .put("type", "array")
                .put("maxItems", 3);

            assertThat(JsonSchemaValidator.validate(JsonNode.array().add(1).add(2), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.array().add(1).add(2).add(3).add(4), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证uniqueItems")
        void testValidateUniqueItems() {
            JsonNode schema = JsonNode.object()
                .put("type", "array")
                .put("uniqueItems", true);

            assertThat(JsonSchemaValidator.validate(JsonNode.array().add(1).add(2), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.array().add(1).add(1), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证items")
        void testValidateItems() {
            JsonNode schema = JsonNode.object()
                .put("type", "array")
                .put("items", JsonNode.object().put("type", "string"));

            assertThat(JsonSchemaValidator.validate(JsonNode.array().add("a").add("b"), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.array().add(1).add(2), schema).isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("对象验证测试")
    class ObjectValidationTests {

        @Test
        @DisplayName("验证required")
        void testValidateRequired() {
            JsonNode schema = JsonNode.object()
                .put("type", "object")
                .put("required", JsonNode.array().add("name"));

            JsonNode validData = JsonNode.object().put("name", "test");
            JsonNode invalidData = JsonNode.object().put("other", "value");

            assertThat(JsonSchemaValidator.validate(validData, schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(invalidData, schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证properties")
        void testValidateProperties() {
            JsonNode schema = JsonNode.object()
                .put("type", "object")
                .put("properties", JsonNode.object()
                    .put("name", JsonNode.object().put("type", "string"))
                    .put("age", JsonNode.object().put("type", "integer")));

            JsonNode validData = JsonNode.object()
                .put("name", "John")
                .put("age", 30);

            assertThat(JsonSchemaValidator.validate(validData, schema).isValid()).isTrue();
        }

        @Test
        @DisplayName("验证additionalProperties为false")
        void testValidateNoAdditionalProperties() {
            JsonNode schema = JsonNode.object()
                .put("type", "object")
                .put("properties", JsonNode.object()
                    .put("name", JsonNode.object().put("type", "string")))
                .put("additionalProperties", false);

            JsonNode validData = JsonNode.object().put("name", "test");
            JsonNode invalidData = JsonNode.object().put("name", "test").put("extra", "field");

            assertThat(JsonSchemaValidator.validate(validData, schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(invalidData, schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证minProperties")
        void testValidateMinProperties() {
            JsonNode schema = JsonNode.object()
                .put("type", "object")
                .put("minProperties", 2);

            assertThat(JsonSchemaValidator.validate(JsonNode.object().put("a", 1).put("b", 2), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.object().put("a", 1), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证maxProperties")
        void testValidateMaxProperties() {
            JsonNode schema = JsonNode.object()
                .put("type", "object")
                .put("maxProperties", 2);

            assertThat(JsonSchemaValidator.validate(JsonNode.object().put("a", 1), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.object().put("a", 1).put("b", 2).put("c", 3), schema).isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("组合验证测试")
    class CompositionValidationTests {

        @Test
        @DisplayName("验证allOf")
        void testValidateAllOf() {
            JsonNode schema = JsonNode.object()
                .put("allOf", JsonNode.array()
                    .add(JsonNode.object().put("type", "object"))
                    .add(JsonNode.object().put("required", JsonNode.array().add("name"))));

            JsonNode validData = JsonNode.object().put("name", "test");
            JsonNode invalidData = JsonNode.object().put("other", "value");

            assertThat(JsonSchemaValidator.validate(validData, schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(invalidData, schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证anyOf")
        void testValidateAnyOf() {
            JsonNode schema = JsonNode.object()
                .put("anyOf", JsonNode.array()
                    .add(JsonNode.object().put("type", "string"))
                    .add(JsonNode.object().put("type", "number")));

            assertThat(JsonSchemaValidator.validate(JsonNode.of("text"), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.of(42), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.of(true), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证oneOf")
        void testValidateOneOf() {
            JsonNode schema = JsonNode.object()
                .put("oneOf", JsonNode.array()
                    .add(JsonNode.object().put("type", "integer"))
                    .add(JsonNode.object().put("minimum", 2)));

            // 42 matches both (integer AND >= 2), so fails oneOf
            assertThat(JsonSchemaValidator.validate(JsonNode.of(42), schema).isValid()).isFalse();

            // 1.5 matches only second (>= 2 is false), so fails anyOf
            // Actually 1.5 doesn't match integer, and doesn't satisfy minimum 2
            assertThat(JsonSchemaValidator.validate(JsonNode.of(1.5), schema).isValid()).isFalse();
        }

        @Test
        @DisplayName("验证not")
        void testValidateNot() {
            JsonNode schema = JsonNode.object()
                .put("not", JsonNode.object().put("type", "string"));

            assertThat(JsonSchemaValidator.validate(JsonNode.of(42), schema).isValid()).isTrue();
            assertThat(JsonSchemaValidator.validate(JsonNode.of("text"), schema).isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("ValidationResult测试")
    class ValidationResultTests {

        @Test
        @DisplayName("success返回有效结果")
        void testSuccess() {
            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.ValidationResult.success();

            assertThat(result.valid()).isTrue();
            assertThat(result.isValid()).isTrue();
            assertThat(result.errors()).isEmpty();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("failure返回无效结果")
        void testFailure() {
            var error = new JsonSchemaException.ValidationError("/path", "type", "message");
            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.ValidationResult.failure(error);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("validateOrThrow方法测试")
    class ValidateOrThrowTests {

        @Test
        @DisplayName("验证通过不抛出异常")
        void testValidateOrThrowValid() {
            JsonNode schema = JsonNode.object().put("type", "string");
            JsonNode data = JsonNode.of("test");

            assertThatCode(() -> JsonSchemaValidator.validateOrThrow(data, schema))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("验证失败抛出异常")
        void testValidateOrThrowInvalid() {
            JsonNode schema = JsonNode.object().put("type", "string");
            JsonNode data = JsonNode.of(42);

            assertThatThrownBy(() -> JsonSchemaValidator.validateOrThrow(data, schema))
                .isInstanceOf(JsonSchemaException.class);
        }

        @Test
        @DisplayName("实例方法validateOrThrow")
        void testInstanceValidateOrThrow() {
            JsonNode schema = JsonNode.object().put("type", "string");
            JsonSchemaValidator validator = JsonSchemaValidator.of(schema);

            assertThatCode(() -> validator.validateOrThrow(JsonNode.of("test")))
                .doesNotThrowAnyException();

            assertThatThrownBy(() -> validator.validateOrThrow(JsonNode.of(42)))
                .isInstanceOf(JsonSchemaException.class);
        }
    }

    @Nested
    @DisplayName("布尔schema测试")
    class BooleanSchemaTests {

        @Test
        @DisplayName("true schema始终通过")
        void testTrueSchema() {
            JsonNode schema = JsonNode.of(true);
            JsonNode data = JsonNode.of("anything");

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("false schema始终失败")
        void testFalseSchema() {
            JsonNode schema = JsonNode.of(false);
            JsonNode data = JsonNode.of("anything");

            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result.isValid()).isFalse();
        }
    }
}
