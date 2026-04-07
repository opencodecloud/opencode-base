package cloud.opencode.base.observability.health;

import cloud.opencode.base.observability.exception.ObservabilityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link HealthResult}.
 */
class HealthResultTest {

    private static final Duration ONE_MS = Duration.ofMillis(1);

    @Nested
    @DisplayName("ConstructorValidation")
    class ConstructorValidation {

        @Test
        @DisplayName("nullName throws ObservabilityException")
        void nullName() {
            assertThatThrownBy(() -> new HealthResult(null, HealthStatus.UP, null, ONE_MS))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("blankName throws ObservabilityException")
        void blankName() {
            assertThatThrownBy(() -> new HealthResult("  ", HealthStatus.UP, null, ONE_MS))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("nullStatus throws ObservabilityException")
        void nullStatus() {
            assertThatThrownBy(() -> new HealthResult("db", null, null, ONE_MS))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("status");
        }

        @Test
        @DisplayName("nullDuration throws ObservabilityException")
        void nullDuration() {
            assertThatThrownBy(() -> new HealthResult("db", HealthStatus.UP, null, null))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Duration");
        }

        @Test
        @DisplayName("nullDetail is allowed")
        void nullDetail() {
            HealthResult result = new HealthResult("db", HealthStatus.UP, null, ONE_MS);
            assertThat(result.detail()).isNull();
            assertThat(result.name()).isEqualTo("db");
            assertThat(result.status()).isEqualTo(HealthStatus.UP);
        }
    }

    @Nested
    @DisplayName("FactoryMethods")
    class FactoryMethods {

        @Test
        @DisplayName("up() creates correct result")
        void up() {
            HealthResult result = HealthResult.up("db", ONE_MS);
            assertThat(result.name()).isEqualTo("db");
            assertThat(result.status()).isEqualTo(HealthStatus.UP);
            assertThat(result.detail()).isNull();
            assertThat(result.duration()).isEqualTo(ONE_MS);
        }

        @Test
        @DisplayName("down() creates correct result")
        void down() {
            HealthResult result = HealthResult.down("db", "connection refused", ONE_MS);
            assertThat(result.name()).isEqualTo("db");
            assertThat(result.status()).isEqualTo(HealthStatus.DOWN);
            assertThat(result.detail()).isEqualTo("connection refused");
            assertThat(result.duration()).isEqualTo(ONE_MS);
        }

        @Test
        @DisplayName("degraded() creates correct result")
        void degraded() {
            HealthResult result = HealthResult.degraded("db", "slow response", ONE_MS);
            assertThat(result.name()).isEqualTo("db");
            assertThat(result.status()).isEqualTo(HealthStatus.DEGRADED);
            assertThat(result.detail()).isEqualTo("slow response");
            assertThat(result.duration()).isEqualTo(ONE_MS);
        }
    }
}
