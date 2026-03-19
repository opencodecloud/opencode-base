package cloud.opencode.base.config.converter.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for DurationConverter.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("DurationConverter Tests")
class DurationConverterTest {

    private DurationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DurationConverter();
    }

    @Nested
    @DisplayName("Simple Format Tests")
    class SimpleFormatTests {

        @Test
        @DisplayName("converts seconds format")
        void testSeconds() {
            assertThat(converter.convert("30s")).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("converts minutes format")
        void testMinutes() {
            assertThat(converter.convert("5m")).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("converts hours format")
        void testHours() {
            assertThat(converter.convert("2h")).isEqualTo(Duration.ofHours(2));
        }

        @Test
        @DisplayName("converts days format")
        void testDays() {
            assertThat(converter.convert("1d")).isEqualTo(Duration.ofDays(1));
        }

        @Test
        @DisplayName("converts large values")
        void testLargeValue() {
            assertThat(converter.convert("3600s")).isEqualTo(Duration.ofSeconds(3600));
        }
    }

    @Nested
    @DisplayName("ISO-8601 Format Tests")
    class IsoFormatTests {

        @Test
        @DisplayName("converts PT format")
        void testPtFormat() {
            assertThat(converter.convert("PT1H30M")).isEqualTo(Duration.ofMinutes(90));
        }

        @Test
        @DisplayName("converts PT seconds format")
        void testPtSeconds() {
            assertThat(converter.convert("PT30S")).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("converts P format for days")
        void testPDays() {
            assertThat(converter.convert("P1D")).isEqualTo(Duration.ofDays(1));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("throws for too-short input")
        void testTooShort() {
            assertThatThrownBy(() -> converter.convert("5"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("converts zero seconds")
        void testZeroSeconds() {
            assertThat(converter.convert("0s")).isEqualTo(Duration.ZERO);
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns Duration.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(Duration.class);
        }
    }
}
