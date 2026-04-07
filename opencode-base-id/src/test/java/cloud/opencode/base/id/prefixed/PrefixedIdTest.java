package cloud.opencode.base.id.prefixed;

import cloud.opencode.base.id.exception.OpenIdGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PrefixedId")
class PrefixedIdTest {

    @Nested
    @DisplayName("of() / construction")
    class Construction {

        @Test
        void validPrefixAndId() {
            PrefixedId id = PrefixedId.of("usr", "01ARZ3NDEK");
            assertThat(id.prefix()).isEqualTo("usr");
            assertThat(id.rawId()).isEqualTo("01ARZ3NDEK");
        }

        @Test
        void prefixWithUnderscoreAndDigits() {
            PrefixedId id = PrefixedId.of("order_item", "abc123");
            assertThat(id.prefix()).isEqualTo("order_item");
        }

        @Test
        void nullPrefixThrows() {
            assertThatThrownBy(() -> PrefixedId.of(null, "abc"))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void emptyPrefixThrows() {
            assertThatThrownBy(() -> PrefixedId.of("", "abc"))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void uppercasePrefixThrows() {
            assertThatThrownBy(() -> PrefixedId.of("Usr", "abc"))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void prefixStartingWithDigitThrows() {
            assertThatThrownBy(() -> PrefixedId.of("1usr", "abc"))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void prefixTooLongThrows() {
            // 32 characters — over limit of 31
            String longPrefix = "a" + "b".repeat(31);
            assertThatThrownBy(() -> PrefixedId.of(longPrefix, "abc"))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void nullRawIdThrows() {
            assertThatThrownBy(() -> PrefixedId.of("usr", null))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void emptyRawIdThrows() {
            assertThatThrownBy(() -> PrefixedId.of("usr", ""))
                    .isInstanceOf(OpenIdGenerationException.class);
        }
    }

    @Nested
    @DisplayName("toString() / fullId()")
    class StringRepresentation {

        @Test
        void toStringReturnsPrefixUnderscore() {
            PrefixedId id = PrefixedId.of("order", "XYZ999");
            assertThat(id.toString()).isEqualTo("order_XYZ999");
        }

        @Test
        void fullIdMatchesToString() {
            PrefixedId id = PrefixedId.of("inv", "ABC123");
            assertThat(id.fullId()).isEqualTo(id.toString());
        }
    }

    @Nested
    @DisplayName("fromString()")
    class FromString {

        @Test
        void parsesNormalPrefixedId() {
            PrefixedId id = PrefixedId.fromString("usr_01ARZ3NDEK");
            assertThat(id.prefix()).isEqualTo("usr");
            assertThat(id.rawId()).isEqualTo("01ARZ3NDEK");
        }

        @Test
        void rawIdMayContainUnderscores() {
            // Only the FIRST underscore splits prefix from rawId
            PrefixedId id = PrefixedId.fromString("order_abc_123_xyz");
            assertThat(id.prefix()).isEqualTo("order");
            assertThat(id.rawId()).isEqualTo("abc_123_xyz");
        }

        @Test
        void noUnderscoreThrows() {
            assertThatThrownBy(() -> PrefixedId.fromString("nounderscore"))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void nullThrows() {
            assertThatThrownBy(() -> PrefixedId.fromString(null))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void roundTripPreservesValue() {
            String original = "inv_7ZYQP4T89A";
            assertThat(PrefixedId.fromString(original).toString()).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("isValid()")
    class Validation {

        @Test
        void validPrefixedId() {
            assertThat(PrefixedId.isValid("usr_01ARZ3NDEK")).isTrue();
        }

        @Test
        void uppercasePrefixIsInvalid() {
            assertThat(PrefixedId.isValid("Usr_01ARZ3NDEK")).isFalse();
        }

        @Test
        void missingUnderscoreIsInvalid() {
            assertThat(PrefixedId.isValid("nounderscore")).isFalse();
        }

        @Test
        void nullIsInvalid() {
            assertThat(PrefixedId.isValid(null)).isFalse();
        }

        @Test
        void emptyIsInvalid() {
            assertThat(PrefixedId.isValid("")).isFalse();
        }

        @Test
        void underscoreOnlyIsInvalid() {
            assertThat(PrefixedId.isValid("_abc")).isFalse();
        }
    }
}
