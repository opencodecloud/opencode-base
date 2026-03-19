package cloud.opencode.base.rules.decision;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * HitPolicy Enum Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("HitPolicy Tests")
class HitPolicyTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have all expected values")
        void shouldHaveAllExpectedValues() {
            HitPolicy[] values = HitPolicy.values();

            assertThat(values).hasSize(7);
            assertThat(values).containsExactly(
                    HitPolicy.UNIQUE,
                    HitPolicy.FIRST,
                    HitPolicy.PRIORITY,
                    HitPolicy.ANY,
                    HitPolicy.COLLECT,
                    HitPolicy.RULE_ORDER,
                    HitPolicy.OUTPUT_ORDER
            );
        }

        @Test
        @DisplayName("UNIQUE should be defined")
        void uniqueShouldBeDefined() {
            assertThat(HitPolicy.UNIQUE).isNotNull();
            assertThat(HitPolicy.UNIQUE.name()).isEqualTo("UNIQUE");
        }

        @Test
        @DisplayName("FIRST should be defined")
        void firstShouldBeDefined() {
            assertThat(HitPolicy.FIRST).isNotNull();
            assertThat(HitPolicy.FIRST.name()).isEqualTo("FIRST");
        }

        @Test
        @DisplayName("PRIORITY should be defined")
        void priorityShouldBeDefined() {
            assertThat(HitPolicy.PRIORITY).isNotNull();
            assertThat(HitPolicy.PRIORITY.name()).isEqualTo("PRIORITY");
        }

        @Test
        @DisplayName("ANY should be defined")
        void anyShouldBeDefined() {
            assertThat(HitPolicy.ANY).isNotNull();
            assertThat(HitPolicy.ANY.name()).isEqualTo("ANY");
        }

        @Test
        @DisplayName("COLLECT should be defined")
        void collectShouldBeDefined() {
            assertThat(HitPolicy.COLLECT).isNotNull();
            assertThat(HitPolicy.COLLECT.name()).isEqualTo("COLLECT");
        }

        @Test
        @DisplayName("RULE_ORDER should be defined")
        void ruleOrderShouldBeDefined() {
            assertThat(HitPolicy.RULE_ORDER).isNotNull();
            assertThat(HitPolicy.RULE_ORDER.name()).isEqualTo("RULE_ORDER");
        }

        @Test
        @DisplayName("OUTPUT_ORDER should be defined")
        void outputOrderShouldBeDefined() {
            assertThat(HitPolicy.OUTPUT_ORDER).isNotNull();
            assertThat(HitPolicy.OUTPUT_ORDER.name()).isEqualTo("OUTPUT_ORDER");
        }
    }

    @Nested
    @DisplayName("valueOf() Tests")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf() should return correct enum")
        void valueOfShouldReturnCorrectEnum() {
            assertThat(HitPolicy.valueOf("UNIQUE")).isEqualTo(HitPolicy.UNIQUE);
            assertThat(HitPolicy.valueOf("FIRST")).isEqualTo(HitPolicy.FIRST);
            assertThat(HitPolicy.valueOf("PRIORITY")).isEqualTo(HitPolicy.PRIORITY);
            assertThat(HitPolicy.valueOf("ANY")).isEqualTo(HitPolicy.ANY);
            assertThat(HitPolicy.valueOf("COLLECT")).isEqualTo(HitPolicy.COLLECT);
            assertThat(HitPolicy.valueOf("RULE_ORDER")).isEqualTo(HitPolicy.RULE_ORDER);
            assertThat(HitPolicy.valueOf("OUTPUT_ORDER")).isEqualTo(HitPolicy.OUTPUT_ORDER);
        }

        @Test
        @DisplayName("valueOf() should throw for invalid name")
        void valueOfShouldThrowForInvalidName() {
            assertThatThrownBy(() -> HitPolicy.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Ordinal Tests")
    class OrdinalTests {

        @Test
        @DisplayName("ordinal() should return correct position")
        void ordinalShouldReturnCorrectPosition() {
            assertThat(HitPolicy.UNIQUE.ordinal()).isEqualTo(0);
            assertThat(HitPolicy.FIRST.ordinal()).isEqualTo(1);
            assertThat(HitPolicy.PRIORITY.ordinal()).isEqualTo(2);
            assertThat(HitPolicy.ANY.ordinal()).isEqualTo(3);
            assertThat(HitPolicy.COLLECT.ordinal()).isEqualTo(4);
            assertThat(HitPolicy.RULE_ORDER.ordinal()).isEqualTo(5);
            assertThat(HitPolicy.OUTPUT_ORDER.ordinal()).isEqualTo(6);
        }
    }
}
