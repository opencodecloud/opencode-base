package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.exception.OpenIdGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SnowflakeFriendlyId")
class SnowflakeFriendlyIdTest {

    private final SnowflakeGenerator snowflake = SnowflakeGenerator.create();
    private final SnowflakeFriendlyId friendly = SnowflakeFriendlyId.ofDefault();

    @Nested
    @DisplayName("toFriendly()")
    class ToFriendly {

        @Test
        void containsHashSeparator() {
            long id = snowflake.generate();
            String result = friendly.toFriendly(id);
            assertThat(result).contains("#");
        }

        @Test
        void formatMatchesPattern() {
            long id = snowflake.generate();
            String result = friendly.toFriendly(id);
            // Format: {ISO timestamp}#{dc}-{worker}-{seq}
            assertThat(result).matches(".+#\\d+-\\d+-\\d+");
        }

        @Test
        void containsIsoTimestamp() {
            long id = snowflake.generate();
            String result = friendly.toFriendly(id);
            String tsPart = result.substring(0, result.indexOf('#'));
            // ISO-8601 instant ends with 'Z'
            assertThat(tsPart).endsWith("Z");
        }
    }

    @Nested
    @DisplayName("fromFriendly()")
    class FromFriendly {

        @Test
        void roundTripPreservesId() {
            long id = snowflake.generate();
            String friendlyStr = friendly.toFriendly(id);
            long recovered = friendly.fromFriendly(friendlyStr);
            assertThat(recovered).isEqualTo(id);
        }

        @Test
        void multipleRoundTrips() {
            for (int i = 0; i < 20; i++) {
                long id = snowflake.generate();
                assertThat(friendly.fromFriendly(friendly.toFriendly(id))).isEqualTo(id);
            }
        }

        @Test
        void invalidFormatThrows() {
            assertThatThrownBy(() -> friendly.fromFriendly("notafriendlyformat"))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void nullThrows() {
            assertThatThrownBy(() -> friendly.fromFriendly(null))
                    .isInstanceOf(OpenIdGenerationException.class);
        }
    }

    @Nested
    @DisplayName("isFriendlyFormat()")
    class IsFriendlyFormat {

        @Test
        void validFormat() {
            long id = snowflake.generate();
            assertThat(friendly.isFriendlyFormat(friendly.toFriendly(id))).isTrue();
        }

        @Test
        void invalidFormat() {
            assertThat(friendly.isFriendlyFormat("not-a-friendly-id")).isFalse();
        }

        @Test
        void nullReturnsFalse() {
            assertThat(friendly.isFriendlyFormat(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("of(config)")
    class WithConfig {

        @Test
        void customConfigPreservesRoundTrip() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();
            SnowflakeFriendlyId customFriendly = SnowflakeFriendlyId.of(config);
            long id = SnowflakeGenerator.create().generate();
            assertThat(customFriendly.fromFriendly(customFriendly.toFriendly(id))).isEqualTo(id);
        }

        @Test
        void nullConfigThrows() {
            assertThatThrownBy(() -> SnowflakeFriendlyId.of(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
