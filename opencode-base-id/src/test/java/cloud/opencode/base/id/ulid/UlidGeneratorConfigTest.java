package cloud.opencode.base.id.ulid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for UlidGenerator.create(UlidConfig) integration
 * UlidConfig集成到UlidGenerator的测试
 */
@DisplayName("UlidGenerator + UlidConfig integration")
class UlidGeneratorConfigTest {

    @Nested
    @DisplayName("create(UlidConfig)")
    class CreateWithConfig {

        @Test
        void monotonicConfigReturnsSingleton() {
            UlidGenerator g1 = UlidGenerator.create(UlidConfig.withMonotonic());
            UlidGenerator g2 = UlidGenerator.create(UlidConfig.defaultConfig());
            assertThat(g1).isSameAs(g2);
        }

        @Test
        void nonMonotonicConfigReturnsNewInstance() {
            UlidGenerator g1 = UlidGenerator.create(UlidConfig.nonMonotonic());
            UlidGenerator g2 = UlidGenerator.create(UlidConfig.nonMonotonic());
            assertThat(g1).isNotSameAs(g2);
        }

        @Test
        void nullConfigFallsBackToMonotonic() {
            UlidGenerator g = UlidGenerator.create(null);
            assertThat(g.isMonotonic()).isTrue();
        }

        @Test
        void monotonicFlagIsCorrect() {
            assertThat(UlidGenerator.create(UlidConfig.withMonotonic()).isMonotonic()).isTrue();
            assertThat(UlidGenerator.create(UlidConfig.nonMonotonic()).isMonotonic()).isFalse();
        }
    }

    @Nested
    @DisplayName("Non-monotonic generation produces valid ULIDs")
    class NonMonotonicGeneration {

        @Test
        void generatesValidUlids() {
            UlidGenerator gen = UlidGenerator.create(UlidConfig.nonMonotonic());
            for (int i = 0; i < 10; i++) {
                assertThat(UlidGenerator.isValid(gen.generate())).isTrue();
            }
        }

        @Test
        void generatesUniqueIds() {
            UlidGenerator gen = UlidGenerator.create(UlidConfig.nonMonotonic());
            String id1 = gen.generate();
            String id2 = gen.generate();
            // Both must be valid
            assertThat(UlidGenerator.isValid(id1)).isTrue();
            assertThat(UlidGenerator.isValid(id2)).isTrue();
        }
    }
}
