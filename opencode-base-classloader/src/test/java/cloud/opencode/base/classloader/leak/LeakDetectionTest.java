package cloud.opencode.base.classloader.leak;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for LeakDetection enum
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
@DisplayName("LeakDetection Tests")
class LeakDetectionTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have exactly three values")
        void shouldHaveThreeValues() {
            assertThat(LeakDetection.values()).hasSize(3);
        }

        @Test
        @DisplayName("Should contain DISABLED, SIMPLE, PARANOID in order")
        void shouldContainExpectedValuesInOrder() {
            assertThat(LeakDetection.values())
                    .containsExactly(LeakDetection.DISABLED, LeakDetection.SIMPLE, LeakDetection.PARANOID);
        }
    }

    @Nested
    @DisplayName("valueOf Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should resolve DISABLED from string")
        void shouldResolveDisabled() {
            assertThat(LeakDetection.valueOf("DISABLED")).isEqualTo(LeakDetection.DISABLED);
        }

        @Test
        @DisplayName("Should resolve SIMPLE from string")
        void shouldResolveSimple() {
            assertThat(LeakDetection.valueOf("SIMPLE")).isEqualTo(LeakDetection.SIMPLE);
        }

        @Test
        @DisplayName("Should resolve PARANOID from string")
        void shouldResolveParanoid() {
            assertThat(LeakDetection.valueOf("PARANOID")).isEqualTo(LeakDetection.PARANOID);
        }

        @Test
        @DisplayName("Should throw on invalid name")
        void shouldThrowOnInvalidName() {
            assertThatThrownBy(() -> LeakDetection.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Ordinal Tests")
    class OrdinalTests {

        @Test
        @DisplayName("DISABLED should have ordinal 0")
        void disabledOrdinal() {
            assertThat(LeakDetection.DISABLED.ordinal()).isZero();
        }

        @Test
        @DisplayName("SIMPLE should have ordinal 1")
        void simpleOrdinal() {
            assertThat(LeakDetection.SIMPLE.ordinal()).isEqualTo(1);
        }

        @Test
        @DisplayName("PARANOID should have ordinal 2")
        void paranoidOrdinal() {
            assertThat(LeakDetection.PARANOID.ordinal()).isEqualTo(2);
        }
    }
}
