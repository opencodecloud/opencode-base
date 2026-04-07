package cloud.opencode.base.id.snowflake;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FixedWorkerIdAssigner")
class FixedWorkerIdAssignerTest {

    @Nested
    @DisplayName("of()")
    class Construction {

        @Test
        void returnsConfiguredIds() {
            FixedWorkerIdAssigner assigner = FixedWorkerIdAssigner.of(7, 3);
            assertThat(assigner.assignWorkerId()).isEqualTo(7);
            assertThat(assigner.assignDatacenterId()).isEqualTo(3);
        }

        @Test
        void zeroIsValid() {
            FixedWorkerIdAssigner assigner = FixedWorkerIdAssigner.of(0, 0);
            assertThat(assigner.assignWorkerId()).isEqualTo(0);
            assertThat(assigner.assignDatacenterId()).isEqualTo(0);
        }

        @Test
        void negativeWorkerIdThrows() {
            assertThatThrownBy(() -> FixedWorkerIdAssigner.of(-1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void negativeDatacenterIdThrows() {
            assertThatThrownBy(() -> FixedWorkerIdAssigner.of(0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getStrategyName()")
    class StrategyName {

        @Test
        void includesBothIds() {
            FixedWorkerIdAssigner assigner = FixedWorkerIdAssigner.of(5, 2);
            String name = assigner.getStrategyName();
            assertThat(name).contains("workerId=5").contains("datacenterId=2");
        }
    }

    @Nested
    @DisplayName("integration with SnowflakeBuilder")
    class Integration {

        @Test
        void buildsSnowflakeGeneratorWithFixedIds() {
            SnowflakeGenerator gen = SnowflakeGenerator.builder()
                    .workerIdAssigner(FixedWorkerIdAssigner.of(3, 1))
                    .build();
            assertThat(gen.getWorkerId()).isEqualTo(3);
            assertThat(gen.getDatacenterId()).isEqualTo(1);
            assertThat(gen.generate()).isPositive();
        }
    }
}
