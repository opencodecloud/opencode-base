package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Tag}.
 */
@DisplayName("Tag")
class TagTest {

    @Nested
    @DisplayName("of() factory method")
    class OfFactory {

        @Test
        @DisplayName("should create a tag with key and value")
        void shouldCreateTag() {
            Tag tag = Tag.of("env", "prod");
            assertThat(tag.key()).isEqualTo("env");
            assertThat(tag.value()).isEqualTo("prod");
        }

        @Test
        @DisplayName("should allow empty string as value")
        void shouldAllowEmptyValue() {
            Tag tag = Tag.of("key", "");
            assertThat(tag.value()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should throw on null key")
        void shouldThrowOnNullKey() {
            assertThatThrownBy(() -> Tag.of(null, "val"))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Tag key must not be null or blank");
        }

        @Test
        @DisplayName("should throw on blank key")
        void shouldThrowOnBlankKey() {
            assertThatThrownBy(() -> Tag.of("  ", "val"))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Tag key must not be null or blank");
        }

        @Test
        @DisplayName("should throw on empty key")
        void shouldThrowOnEmptyKey() {
            assertThatThrownBy(() -> Tag.of("", "val"))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Tag key must not be null or blank");
        }

        @Test
        @DisplayName("should throw on null value")
        void shouldThrowOnNullValue() {
            assertThatThrownBy(() -> Tag.of("key", null))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Tag value must not be null");
        }

        @Test
        @DisplayName("key 含换行符应抛出异常（防注入）")
        void shouldThrowOnLineBreakInKey() {
            assertThatThrownBy(() -> Tag.of("env\nfake", "prod"))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("line-break");
            assertThatThrownBy(() -> Tag.of("env\rfake", "prod"))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("line-break");
        }

        @Test
        @DisplayName("value 含换行符应抛出异常（防注入）")
        void shouldThrowOnLineBreakInValue() {
            assertThatThrownBy(() -> Tag.of("env", "prod\n# FORGED"))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("line-break");
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("should be equal for same key and value")
        void shouldBeEqualForSameKeyValue() {
            Tag tag1 = Tag.of("env", "prod");
            Tag tag2 = Tag.of("env", "prod");
            assertThat(tag1).isEqualTo(tag2);
            assertThat(tag1.hashCode()).isEqualTo(tag2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different keys")
        void shouldNotBeEqualForDifferentKeys() {
            Tag tag1 = Tag.of("env", "prod");
            Tag tag2 = Tag.of("region", "prod");
            assertThat(tag1).isNotEqualTo(tag2);
        }

        @Test
        @DisplayName("should not be equal for different values")
        void shouldNotBeEqualForDifferentValues() {
            Tag tag1 = Tag.of("env", "prod");
            Tag tag2 = Tag.of("env", "staging");
            assertThat(tag1).isNotEqualTo(tag2);
        }
    }
}
