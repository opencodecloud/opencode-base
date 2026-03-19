package cloud.opencode.base.cache.spi;

import cloud.opencode.base.cache.internal.expiry.*;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * ExpiryPolicyTest Tests
 * ExpiryPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("ExpiryPolicy SPI Tests")
class ExpiryPolicyTest {

    @Nested
    @DisplayName("Constants Tests")
    class ConstantsTests {

        @Test
        @DisplayName("INFINITE should be Duration of Long.MAX_VALUE nanos")
        void infiniteShouldBeMaxNanos() {
            assertThat(ExpiryPolicy.INFINITE).isEqualTo(Duration.ofNanos(Long.MAX_VALUE));
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("ttl() should return TtlExpiryPolicy")
        void ttlShouldReturnCorrectType() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.ttl(Duration.ofHours(1));
            assertThat(policy).isInstanceOf(TtlExpiryPolicy.class);
        }

        @Test
        @DisplayName("tti() should return TtiExpiryPolicy")
        void ttiShouldReturnCorrectType() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.tti(Duration.ofMinutes(30));
            assertThat(policy).isInstanceOf(TtiExpiryPolicy.class);
        }

        @Test
        @DisplayName("combined() should return CombinedExpiryPolicy")
        void combinedShouldReturnCorrectType() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.combined(
                Duration.ofHours(1), Duration.ofMinutes(30));
            assertThat(policy).isInstanceOf(CombinedExpiryPolicy.class);
        }
    }

    @Nested
    @DisplayName("Eternal Policy Tests")
    class EternalPolicyTests {

        @Test
        @DisplayName("eternal() should return INFINITE on create")
        void shouldReturnInfiniteOnCreate() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.eternal();
            assertThat(policy.expireAfterCreate("key", "value")).isEqualTo(ExpiryPolicy.INFINITE);
        }

        @Test
        @DisplayName("eternal() should return INFINITE on update")
        void shouldReturnInfiniteOnUpdate() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.eternal();
            assertThat(policy.expireAfterUpdate("key", "value", Duration.ofMinutes(5)))
                .isEqualTo(ExpiryPolicy.INFINITE);
        }

        @Test
        @DisplayName("eternal() should return INFINITE on read")
        void shouldReturnInfiniteOnRead() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.eternal();
            assertThat(policy.expireAfterRead("key", "value", Duration.ofMinutes(5)))
                .isEqualTo(ExpiryPolicy.INFINITE);
        }

        @Test
        @DisplayName("eternal() should never expire regardless of operations")
        void shouldNeverExpire() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.eternal();

            Duration create = policy.expireAfterCreate("k", "v");
            Duration update = policy.expireAfterUpdate("k", "v", Duration.ZERO);
            Duration read = policy.expireAfterRead("k", "v", Duration.ZERO);

            assertThat(create).isEqualTo(ExpiryPolicy.INFINITE);
            assertThat(update).isEqualTo(ExpiryPolicy.INFINITE);
            assertThat(read).isEqualTo(ExpiryPolicy.INFINITE);
        }
    }
}
