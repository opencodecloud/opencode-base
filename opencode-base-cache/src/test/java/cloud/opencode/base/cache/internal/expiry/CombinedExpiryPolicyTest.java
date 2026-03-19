package cloud.opencode.base.cache.internal.expiry;

import cloud.opencode.base.cache.spi.ExpiryPolicy;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * CombinedExpiryPolicyTest Tests
 * CombinedExpiryPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CombinedExpiryPolicy Tests")
class CombinedExpiryPolicyTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create combined policy via SPI factory method")
        void shouldCreateViaFactoryMethod() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.combined(
                Duration.ofHours(1), Duration.ofMinutes(30));
            assertThat(policy).isInstanceOf(CombinedExpiryPolicy.class);
        }
    }

    @Nested
    @DisplayName("Expire After Create Tests")
    class ExpireAfterCreateTests {

        @Test
        @DisplayName("should return shorter of TTL and TTI on create")
        void shouldReturnShorterDuration() {
            CombinedExpiryPolicy<String, String> policy = new CombinedExpiryPolicy<>(
                Duration.ofHours(1), Duration.ofMinutes(30));

            Duration result = policy.expireAfterCreate("key", "value");
            assertThat(result).isEqualTo(Duration.ofMinutes(30));
        }

        @Test
        @DisplayName("should return TTL when TTL is shorter")
        void shouldReturnTtlWhenShorter() {
            CombinedExpiryPolicy<String, String> policy = new CombinedExpiryPolicy<>(
                Duration.ofMinutes(10), Duration.ofHours(1));

            Duration result = policy.expireAfterCreate("key", "value");
            assertThat(result).isEqualTo(Duration.ofMinutes(10));
        }

        @Test
        @DisplayName("should handle equal TTL and TTI")
        void shouldHandleEqualDurations() {
            Duration same = Duration.ofMinutes(30);
            CombinedExpiryPolicy<String, String> policy = new CombinedExpiryPolicy<>(same, same);

            Duration result = policy.expireAfterCreate("key", "value");
            assertThat(result).isEqualTo(same);
        }
    }

    @Nested
    @DisplayName("Expire After Update Tests")
    class ExpireAfterUpdateTests {

        @Test
        @DisplayName("should return shorter of TTL and TTI on update")
        void shouldReturnShorterOnUpdate() {
            CombinedExpiryPolicy<String, String> policy = new CombinedExpiryPolicy<>(
                Duration.ofHours(1), Duration.ofMinutes(30));

            Duration result = policy.expireAfterUpdate("key", "value", Duration.ofMinutes(45));
            assertThat(result).isEqualTo(Duration.ofMinutes(30));
        }
    }

    @Nested
    @DisplayName("Expire After Read Tests")
    class ExpireAfterReadTests {

        @Test
        @DisplayName("should return shorter of currentDuration and TTI on read")
        void shouldReturnShorterOfCurrentAndTti() {
            CombinedExpiryPolicy<String, String> policy = new CombinedExpiryPolicy<>(
                Duration.ofHours(1), Duration.ofMinutes(30));

            // Remaining TTL is 20 minutes, TTI is 30 minutes -> should return 20 min
            Duration result = policy.expireAfterRead("key", "value", Duration.ofMinutes(20));
            assertThat(result).isEqualTo(Duration.ofMinutes(20));
        }

        @Test
        @DisplayName("should return TTI when it is shorter than currentDuration")
        void shouldReturnTtiWhenShorter() {
            CombinedExpiryPolicy<String, String> policy = new CombinedExpiryPolicy<>(
                Duration.ofHours(1), Duration.ofMinutes(30));

            // Remaining TTL is 50 minutes, TTI is 30 minutes -> should return 30 min
            Duration result = policy.expireAfterRead("key", "value", Duration.ofMinutes(50));
            assertThat(result).isEqualTo(Duration.ofMinutes(30));
        }
    }
}
