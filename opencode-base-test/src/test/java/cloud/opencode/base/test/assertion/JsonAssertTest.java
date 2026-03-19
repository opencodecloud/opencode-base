package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonAssertTest Tests
 * JsonAssertTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("JsonAssert Tests")
class JsonAssertTest {

    @Nested
    @DisplayName("Null/NotNull Tests")
    class NullNotNullTests {

        @Test
        @DisplayName("isNull should pass for null")
        void isNullShouldPassForNull() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat(null).isNull());
        }

        @Test
        @DisplayName("isNull should fail for non-null")
        void isNullShouldFailForNonNull() {
            assertThatThrownBy(() -> JsonAssert.assertThat("{}").isNull())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isNotNull should pass for non-null")
        void isNotNullShouldPassForNonNull() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{}").isNotNull());
        }

        @Test
        @DisplayName("isNotNull should fail for null")
        void isNotNullShouldFailForNull() {
            assertThatThrownBy(() -> JsonAssert.assertThat(null).isNotNull())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("isValidJson Tests")
    class IsValidJsonTests {

        @Test
        @DisplayName("Should pass for valid JSON object")
        void shouldPassForValidJsonObject() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{\"name\": \"value\"}").isValidJson());
        }

        @Test
        @DisplayName("Should pass for valid JSON array")
        void shouldPassForValidJsonArray() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("[1, 2, 3]").isValidJson());
        }

        @Test
        @DisplayName("Should pass for valid JSON string")
        void shouldPassForValidJsonString() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("\"hello\"").isValidJson());
        }

        @Test
        @DisplayName("Should pass for valid JSON number")
        void shouldPassForValidJsonNumber() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("123").isValidJson());
        }

        @Test
        @DisplayName("Should pass for valid JSON boolean")
        void shouldPassForValidJsonBoolean() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("true").isValidJson());
        }

        @Test
        @DisplayName("Should pass for valid JSON null")
        void shouldPassForValidJsonNull() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("null").isValidJson());
        }

        @Test
        @DisplayName("Should fail for invalid JSON")
        void shouldFailForInvalidJson() {
            assertThatThrownBy(() -> JsonAssert.assertThat("invalid").isValidJson())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("isJsonObject should pass for object")
        void isJsonObjectShouldPassForObject() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{\"key\": \"value\"}").isJsonObject());
        }

        @Test
        @DisplayName("isJsonObject should fail for array")
        void isJsonObjectShouldFailForArray() {
            assertThatThrownBy(() -> JsonAssert.assertThat("[1, 2, 3]").isJsonObject())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isJsonArray should pass for array")
        void isJsonArrayShouldPassForArray() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("[1, 2, 3]").isJsonArray());
        }

        @Test
        @DisplayName("isJsonArray should fail for object")
        void isJsonArrayShouldFailForObject() {
            assertThatThrownBy(() -> JsonAssert.assertThat("{\"key\": \"value\"}").isJsonArray())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("containsKey Tests")
    class ContainsKeyTests {

        @Test
        @DisplayName("Should pass when key exists")
        void shouldPassWhenKeyExists() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{\"name\": \"John\"}").containsKey("name"));
        }

        @Test
        @DisplayName("Should fail when key not found")
        void shouldFailWhenKeyNotFound() {
            assertThatThrownBy(() -> JsonAssert.assertThat("{\"name\": \"John\"}").containsKey("age"))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("doesNotContainKey should pass when key not found")
        void doesNotContainKeyShouldPassWhenKeyNotFound() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{\"name\": \"John\"}").doesNotContainKey("age"));
        }

        @Test
        @DisplayName("doesNotContainKey should fail when key exists")
        void doesNotContainKeyShouldFailWhenKeyExists() {
            assertThatThrownBy(() -> JsonAssert.assertThat("{\"name\": \"John\"}").doesNotContainKey("name"))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("containsValue Tests")
    class ContainsValueTests {

        @Test
        @DisplayName("Should pass when string value exists")
        void shouldPassWhenStringValueExists() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{\"name\": \"John\"}").containsValue("John"));
        }

        @Test
        @DisplayName("Should fail when string value not found")
        void shouldFailWhenStringValueNotFound() {
            assertThatThrownBy(() -> JsonAssert.assertThat("{\"name\": \"John\"}").containsValue("Jane"))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("hasKeyValue Tests")
    class HasKeyValueTests {

        @Test
        @DisplayName("Should pass for string key-value")
        void shouldPassForStringKeyValue() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{\"name\": \"John\"}").hasKeyValue("name", "John"));
        }

        @Test
        @DisplayName("Should fail for wrong value")
        void shouldFailForWrongValue() {
            assertThatThrownBy(() -> JsonAssert.assertThat("{\"name\": \"John\"}").hasKeyValue("name", "Jane"))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("Should pass for number key-value")
        void shouldPassForNumberKeyValue() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{\"age\": 30}").hasKeyValue("age", 30));
        }

        @Test
        @DisplayName("Should pass for boolean key-value")
        void shouldPassForBooleanKeyValue() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{\"active\": true}").hasKeyValue("active", true));
        }

        @Test
        @DisplayName("hasNullValue should pass for null value")
        void hasNullValueShouldPassForNullValue() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{\"value\": null}").hasNullValue("value"));
        }
    }

    @Nested
    @DisplayName("isEqualTo Tests")
    class IsEqualToTests {

        @Test
        @DisplayName("Should pass for equal JSON ignoring whitespace")
        void shouldPassForEqualJsonIgnoringWhitespace() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{ \"name\" : \"John\" }").isEqualTo("{\"name\":\"John\"}"));
        }

        @Test
        @DisplayName("Should fail for different JSON")
        void shouldFailForDifferentJson() {
            assertThatThrownBy(() ->
                JsonAssert.assertThat("{\"name\": \"John\"}").isEqualTo("{\"name\": \"Jane\"}")
            ).isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Empty Tests")
    class EmptyTests {

        @Test
        @DisplayName("isEmptyObject should pass for empty object")
        void isEmptyObjectShouldPassForEmptyObject() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{}").isEmptyObject());
        }

        @Test
        @DisplayName("isEmptyObject should fail for non-empty object")
        void isEmptyObjectShouldFailForNonEmptyObject() {
            assertThatThrownBy(() -> JsonAssert.assertThat("{\"key\": \"value\"}").isEmptyObject())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isEmptyArray should pass for empty array")
        void isEmptyArrayShouldPassForEmptyArray() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("[]").isEmptyArray());
        }

        @Test
        @DisplayName("isEmptyArray should fail for non-empty array")
        void isEmptyArrayShouldFailForNonEmptyArray() {
            assertThatThrownBy(() -> JsonAssert.assertThat("[1]").isEmptyArray())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("hasLength Tests")
    class HasLengthTests {

        @Test
        @DisplayName("Should pass for correct length")
        void shouldPassForCorrectLength() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{}").hasLength(2));
        }

        @Test
        @DisplayName("Should fail for wrong length")
        void shouldFailForWrongLength() {
            assertThatThrownBy(() -> JsonAssert.assertThat("{}").hasLength(10))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("Should support fluent chaining")
        void shouldSupportFluentChaining() {
            assertThatNoException().isThrownBy(() ->
                JsonAssert.assertThat("{\"name\": \"John\", \"age\": 30, \"active\": true}")
                    .isNotNull()
                    .isValidJson()
                    .isJsonObject()
                    .containsKey("name")
                    .containsKey("age")
                    .hasKeyValue("name", "John")
                    .hasKeyValue("age", 30)
                    .hasKeyValue("active", true)
                    .doesNotContainKey("missing"));
        }
    }
}
