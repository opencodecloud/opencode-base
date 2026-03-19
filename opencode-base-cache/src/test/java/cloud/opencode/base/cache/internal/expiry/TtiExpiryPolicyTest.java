package cloud.opencode.base.cache.internal.expiry;

import cloud.opencode.base.cache.spi.ExpiryPolicy;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * TtiExpiryPolicyTest Tests
 * TtiExpiryPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("TtiExpiryPolicy Tests")
class TtiExpiryPolicyTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create TTI policy via SPI factory method")
        void shouldCreateViaFactoryMethod() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.tti(Duration.ofMinutes(30));
            assertThat(policy).isInstanceOf(TtiExpiryPolicy.class);
        }
    }

    @Nested
    @DisplayName("Expire After Create Tests")
    class ExpireAfterCreateTests {

        @Test
        @DisplayName("should return TTI duration on create")
        void shouldReturnTtiOnCreate() {
            Duration tti = Duration.ofMinutes(30);
            TtiExpiryPolicy<String, String> policy = new TtiExpiryPolicy<>(tti);

            Duration result = policy.expireAfterCreate("key", "value");
            assertThat(result).isEqualTo(tti);
        }
    }

    @Nested
    @DisplayName("Expire After Update Tests")
    class ExpireAfterUpdateTests {

        @Test
        @DisplayName("should reset to TTI duration on update")
        void shouldResetToTtiOnUpdate() {
            Duration tti = Duration.ofMinutes(30);
            TtiExpiryPolicy<String, String> policy = new TtiExpiryPolicy<>(tti);

            Duration result = policy.expireAfterUpdate("key", "value", Duration.ofMinutes(10));
            assertThat(result).isEqualTo(tti);
        }
    }

    @Nested
    @DisplayName("Expire After Read Tests")
    class ExpireAfterReadTests {

        @Test
        @DisplayName("should reset to TTI duration on read")
        void shouldResetToTtiOnRead() {
            Duration tti = Duration.ofMinutes(30);
            TtiExpiryPolicy<String, String> policy = new TtiExpiryPolicy<>(tti);

            Duration result = policy.expireAfterRead("key", "value", Duration.ofMinutes(5));
            assertThat(result).isEqualTo(tti);
        }

        @Test
        @DisplayName("should always return TTI regardless of currentDuration")
        void shouldAlwaysReturnTti() {
            Duration tti = Duration.ofHours(1);
            TtiExpiryPolicy<String, String> policy = new TtiExpiryPolicy<>(tti);

            assertThat(policy.expireAfterRead("k", "v", Duration.ofMinutes(1))).isEqualTo(tti);
            assertThat(policy.expireAfterRead("k", "v", Duration.ofHours(2))).isEqualTo(tti);
            assertThat(policy.expireAfterRead("k", "v", Duration.ZERO)).isEqualTo(tti);
        }
    }
}
