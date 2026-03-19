package cloud.opencode.base.cache.internal.expiry;

import cloud.opencode.base.cache.spi.ExpiryPolicy;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * TtlExpiryPolicyTest Tests
 * TtlExpiryPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("TtlExpiryPolicy Tests")
class TtlExpiryPolicyTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create TTL policy via SPI factory method")
        void shouldCreateViaFactoryMethod() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.ttl(Duration.ofHours(1));
            assertThat(policy).isInstanceOf(TtlExpiryPolicy.class);
        }
    }

    @Nested
    @DisplayName("Expire After Create Tests")
    class ExpireAfterCreateTests {

        @Test
        @DisplayName("should return TTL duration on create")
        void shouldReturnTtlOnCreate() {
            Duration ttl = Duration.ofHours(1);
            TtlExpiryPolicy<String, String> policy = new TtlExpiryPolicy<>(ttl);

            Duration result = policy.expireAfterCreate("key", "value");
            assertThat(result).isEqualTo(ttl);
        }
    }

    @Nested
    @DisplayName("Expire After Update Tests")
    class ExpireAfterUpdateTests {

        @Test
        @DisplayName("should reset to TTL duration on update")
        void shouldResetToTtlOnUpdate() {
            Duration ttl = Duration.ofHours(1);
            TtlExpiryPolicy<String, String> policy = new TtlExpiryPolicy<>(ttl);

            Duration result = policy.expireAfterUpdate("key", "value", Duration.ofMinutes(30));
            assertThat(result).isEqualTo(ttl);
        }
    }

    @Nested
    @DisplayName("Expire After Read Tests")
    class ExpireAfterReadTests {

        @Test
        @DisplayName("should not change duration on read")
        void shouldNotChangeDurationOnRead() {
            Duration ttl = Duration.ofHours(1);
            TtlExpiryPolicy<String, String> policy = new TtlExpiryPolicy<>(ttl);

            Duration currentDuration = Duration.ofMinutes(30);
            Duration result = policy.expireAfterRead("key", "value", currentDuration);
            assertThat(result).isEqualTo(currentDuration);
        }

        @Test
        @DisplayName("should preserve currentDuration regardless of TTL value")
        void shouldPreserveCurrentDuration() {
            Duration ttl = Duration.ofHours(1);
            TtlExpiryPolicy<String, String> policy = new TtlExpiryPolicy<>(ttl);

            Duration shortDuration = Duration.ofSeconds(5);
            assertThat(policy.expireAfterRead("k", "v", shortDuration)).isEqualTo(shortDuration);

            Duration longDuration = Duration.ofDays(1);
            assertThat(policy.expireAfterRead("k", "v", longDuration)).isEqualTo(longDuration);
        }
    }
}
