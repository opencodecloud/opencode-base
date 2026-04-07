package cloud.opencode.base.id;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IdConverter — Base58")
class IdConverterBase58Test {

    @Nested
    @DisplayName("toBase58()")
    class ToBase58 {

        @Test
        void zeroReturnsOne() {
            assertThat(IdConverter.toBase58(0L)).isEqualTo("1");
        }

        @Test
        void smallPositive() {
            String encoded = IdConverter.toBase58(1000000000L);
            assertThat(encoded).isNotEmpty();
            // Round-trip
            assertThat(IdConverter.fromBase58(encoded)).isEqualTo(1000000000L);
        }

        @Test
        void longMaxValue() {
            String encoded = IdConverter.toBase58(Long.MAX_VALUE);
            assertThat(encoded).isNotEmpty();
            assertThat(IdConverter.fromBase58(encoded)).isEqualTo(Long.MAX_VALUE);
        }

        @Test
        void noAmbiguousChars() {
            // Base58 must not contain 0, O, I, l
            String encoded = IdConverter.toBase58(Long.MAX_VALUE);
            assertThat(encoded).doesNotContain("0", "O", "I", "l");
        }

        @Test
        void snowflakeLikeValue() {
            long snowflakeId = 1705312200123049003L;
            String encoded = IdConverter.toBase58(snowflakeId);
            assertThat(IdConverter.fromBase58(encoded)).isEqualTo(snowflakeId);
        }
    }

    @Nested
    @DisplayName("fromBase58()")
    class FromBase58 {

        @Test
        void oneDecodesToZero() {
            assertThat(IdConverter.fromBase58("1")).isEqualTo(0L);
        }

        @Test
        void invalidCharThrows() {
            assertThatThrownBy(() -> IdConverter.fromBase58("0abc"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void nullThrows() {
            assertThatThrownBy(() -> IdConverter.fromBase58(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void emptyStringThrows() {
            assertThatThrownBy(() -> IdConverter.fromBase58(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("isValidBase58()")
    class IsValidBase58 {

        @Test
        void validString() {
            assertThat(IdConverter.isValidBase58("2QGPK")).isTrue();
        }

        @Test
        void containsAmbiguousCharIsInvalid() {
            assertThat(IdConverter.isValidBase58("0QGPK")).isFalse(); // '0' not in Base58
            assertThat(IdConverter.isValidBase58("OQGPK")).isFalse(); // 'O' not in Base58
        }

        @Test
        void nullIsInvalid() {
            assertThat(IdConverter.isValidBase58(null)).isFalse();
        }

        @Test
        void emptyIsInvalid() {
            assertThat(IdConverter.isValidBase58("")).isFalse();
        }

        @Test
        void encodedValueIsValid() {
            String encoded = IdConverter.toBase58(999_999_999_999L);
            assertThat(IdConverter.isValidBase58(encoded)).isTrue();
        }
    }

    @Nested
    @DisplayName("round-trip consistency")
    class RoundTrip {

        @Test
        void roundTripForVariousValues() {
            long[] values = {0L, 1L, 57L, 58L, 100L, 10_000L, Integer.MAX_VALUE,
                    Long.MAX_VALUE / 2, Long.MAX_VALUE};
            for (long v : values) {
                assertThat(IdConverter.fromBase58(IdConverter.toBase58(v)))
                        .as("round-trip for %d", v).isEqualTo(v);
            }
        }
    }
}
