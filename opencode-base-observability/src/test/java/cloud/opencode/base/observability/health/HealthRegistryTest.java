package cloud.opencode.base.observability.health;

import cloud.opencode.base.observability.exception.ObservabilityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link HealthRegistry}.
 */
class HealthRegistryTest {

    private HealthRegistry registry;

    @BeforeEach
    void setUp() {
        registry = HealthRegistry.create();
    }

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("register and check returns result")
        void registerAndCheck() {
            registry.register("db", () -> HealthResult.up("db", Duration.ofMillis(1)));

            Map<String, HealthResult> results = registry.check();
            assertThat(results).containsKey("db");
            assertThat(results.get("db").status()).isEqualTo(HealthStatus.UP);
        }

        @Test
        @DisplayName("register replaces existing check")
        void replaceExisting() {
            registry.register("db", () -> HealthResult.up("db", Duration.ofMillis(1)));
            registry.register("db", () -> HealthResult.down("db", "replaced", Duration.ofMillis(2)));

            Map<String, HealthResult> results = registry.check();
            assertThat(results.get("db").status()).isEqualTo(HealthStatus.DOWN);
            assertThat(registry.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("unregister returns true when check exists")
        void unregisterExisting() {
            registry.register("db", () -> HealthResult.up("db", Duration.ofMillis(1)));
            assertThat(registry.unregister("db")).isTrue();
            assertThat(registry.size()).isZero();
        }

        @Test
        @DisplayName("unregister returns false when check does not exist")
        void unregisterNonexistent() {
            assertThat(registry.unregister("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("register with null name throws")
        void registerNullName() {
            assertThatThrownBy(() -> registry.register(null, () -> HealthResult.up("x", Duration.ZERO)))
                    .isInstanceOf(ObservabilityException.class);
        }

        @Test
        @DisplayName("register with null check throws")
        void registerNullCheck() {
            assertThatThrownBy(() -> registry.register("db", null))
                    .isInstanceOf(ObservabilityException.class);
        }
    }

    @Nested
    @DisplayName("Checking")
    class Checking {

        @Test
        @DisplayName("executes all registered checks")
        void executesAllChecks() {
            registry.register("db", () -> HealthResult.up("db", Duration.ofMillis(1)));
            registry.register("cache", () -> HealthResult.up("cache", Duration.ofMillis(2)));

            Map<String, HealthResult> results = registry.check();
            assertThat(results).hasSize(2);
            assertThat(results).containsKeys("db", "cache");
        }

        @Test
        @DisplayName("catches exception and returns DOWN")
        void catchesException() {
            registry.register("failing", () -> {
                throw new RuntimeException("connection timeout");
            });

            Map<String, HealthResult> results = registry.check();
            assertThat(results.get("failing").status()).isEqualTo(HealthStatus.DOWN);
            assertThat(results.get("failing").detail()).isEqualTo("java.lang.RuntimeException");
        }

        @Test
        @DisplayName("status aggregates all check results")
        void statusAggregation() {
            registry.register("db", () -> HealthResult.up("db", Duration.ofMillis(1)));
            registry.register("cache", () -> HealthResult.degraded("cache", "slow", Duration.ofMillis(2)));

            assertThat(registry.status()).isEqualTo(HealthStatus.DEGRADED);
        }
    }

    @Nested
    @DisplayName("Management")
    class Management {

        @Test
        @DisplayName("names() returns registered check names")
        void names() {
            registry.register("db", () -> HealthResult.up("db", Duration.ofMillis(1)));
            registry.register("cache", () -> HealthResult.up("cache", Duration.ofMillis(1)));

            assertThat(registry.names()).containsExactlyInAnyOrder("db", "cache");
        }

        @Test
        @DisplayName("size() returns correct count")
        void size() {
            assertThat(registry.size()).isZero();
            registry.register("db", () -> HealthResult.up("db", Duration.ofMillis(1)));
            assertThat(registry.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("clear() removes all checks")
        void clear() {
            registry.register("db", () -> HealthResult.up("db", Duration.ofMillis(1)));
            registry.register("cache", () -> HealthResult.up("cache", Duration.ofMillis(1)));
            registry.clear();

            assertThat(registry.size()).isZero();
            assertThat(registry.names()).isEmpty();
        }
    }

    @Nested
    @DisplayName("MaxChecksLimit")
    class MaxChecksLimit {

        @Test
        @DisplayName("create(0) throws ObservabilityException")
        void createWithZeroMaxChecks() {
            assertThatThrownBy(() -> HealthRegistry.create(0))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("maxChecks must be positive");
        }

        @Test
        @DisplayName("create(-1) throws ObservabilityException")
        void createWithNegativeMaxChecks() {
            assertThatThrownBy(() -> HealthRegistry.create(-1))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("maxChecks must be positive");
        }

        @Test
        @DisplayName("register beyond maxChecks throws REGISTRY_FULL")
        void registerBeyondMaxChecksThrows() {
            HealthRegistry limited = HealthRegistry.create(2);
            limited.register("check1", () -> HealthResult.up("check1", Duration.ofMillis(1)));
            limited.register("check2", () -> HealthResult.up("check2", Duration.ofMillis(1)));

            assertThatThrownBy(() -> limited.register("check3",
                    () -> HealthResult.up("check3", Duration.ofMillis(1))))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("REGISTRY_FULL");
        }

        @Test
        @DisplayName("replacing existing check at capacity succeeds")
        void replaceExistingAtCapacitySucceeds() {
            HealthRegistry limited = HealthRegistry.create(2);
            limited.register("check1", () -> HealthResult.up("check1", Duration.ofMillis(1)));
            limited.register("check2", () -> HealthResult.up("check2", Duration.ofMillis(1)));

            // Should not throw — replacing an existing entry does not exceed capacity
            limited.register("check1", () -> HealthResult.down("check1", "replaced", Duration.ofMillis(2)));

            Map<String, HealthResult> results = limited.check();
            assertThat(results.get("check1").status()).isEqualTo(HealthStatus.DOWN);
            assertThat(limited.size()).isEqualTo(2);
        }
    }
}
