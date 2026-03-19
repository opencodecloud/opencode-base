package cloud.opencode.base.cache.internal.expiry;

import cloud.opencode.base.cache.spi.ExpiryPolicy;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Expiry Policy Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class ExpiryPolicyTest {

    // ==================== TTL Policy Tests ====================

    @Nested
    class TtlExpiryPolicyTest {

        @Test
        void shouldCreateTtlPolicy() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.ttl(Duration.ofMinutes(30));
            assertThat(policy).isInstanceOf(TtlExpiryPolicy.class);
        }

        @Test
        void shouldReturnTtlOnCreate() {
            Duration ttl = Duration.ofMinutes(30);
            TtlExpiryPolicy<String, String> policy = new TtlExpiryPolicy<>(ttl);

            Duration expiry = policy.expireAfterCreate("key", "value");

            assertThat(expiry).isEqualTo(ttl);
        }

        @Test
        void shouldReturnTtlOnUpdate() {
            Duration ttl = Duration.ofMinutes(30);
            TtlExpiryPolicy<String, String> policy = new TtlExpiryPolicy<>(ttl);

            Duration expiry = policy.expireAfterUpdate("key", "value", Duration.ofMinutes(10));

            assertThat(expiry).isEqualTo(ttl);
        }

        @Test
        void shouldReturnCurrentDurationOnRead() {
            Duration ttl = Duration.ofMinutes(30);
            TtlExpiryPolicy<String, String> policy = new TtlExpiryPolicy<>(ttl);

            Duration current = Duration.ofMinutes(10);
            Duration expiry = policy.expireAfterRead("key", "value", current);

            assertThat(expiry).isEqualTo(current); // TTL doesn't extend on read
        }
    }

    // ==================== TTI Policy Tests ====================

    @Nested
    class TtiExpiryPolicyTest {

        @Test
        void shouldCreateTtiPolicy() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.tti(Duration.ofMinutes(10));
            assertThat(policy).isInstanceOf(TtiExpiryPolicy.class);
        }

        @Test
        void shouldReturnTtiOnCreate() {
            Duration tti = Duration.ofMinutes(10);
            TtiExpiryPolicy<String, String> policy = new TtiExpiryPolicy<>(tti);

            Duration expiry = policy.expireAfterCreate("key", "value");

            assertThat(expiry).isEqualTo(tti);
        }

        @Test
        void shouldReturnTtiOnUpdate() {
            Duration tti = Duration.ofMinutes(10);
            TtiExpiryPolicy<String, String> policy = new TtiExpiryPolicy<>(tti);

            Duration expiry = policy.expireAfterUpdate("key", "value", Duration.ofMinutes(5));

            assertThat(expiry).isEqualTo(tti);
        }

        @Test
        void shouldReturnTtiOnRead() {
            Duration tti = Duration.ofMinutes(10);
            TtiExpiryPolicy<String, String> policy = new TtiExpiryPolicy<>(tti);

            Duration expiry = policy.expireAfterRead("key", "value", Duration.ofMinutes(5));

            assertThat(expiry).isEqualTo(tti); // TTI extends on read
        }
    }

    // ==================== Combined Policy Tests ====================

    @Nested
    class CombinedExpiryPolicyTest {

        @Test
        void shouldCreateCombinedPolicy() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.combined(
                    Duration.ofHours(1), Duration.ofMinutes(10));
            assertThat(policy).isInstanceOf(CombinedExpiryPolicy.class);
        }

        @Test
        void shouldReturnMinOfTtlAndTtiOnCreate() {
            Duration ttl = Duration.ofHours(1);
            Duration tti = Duration.ofMinutes(10);
            CombinedExpiryPolicy<String, String> policy = new CombinedExpiryPolicy<>(ttl, tti);

            Duration expiry = policy.expireAfterCreate("key", "value");

            assertThat(expiry).isEqualTo(tti); // Min of TTL and TTI
        }

        @Test
        void shouldReturnMinOfTtlAndTtiOnUpdate() {
            Duration ttl = Duration.ofMinutes(5);
            Duration tti = Duration.ofMinutes(10);
            CombinedExpiryPolicy<String, String> policy = new CombinedExpiryPolicy<>(ttl, tti);

            Duration expiry = policy.expireAfterUpdate("key", "value", Duration.ofMinutes(1));

            assertThat(expiry).isEqualTo(ttl); // Min of TTL and TTI
        }

        @Test
        void shouldReturnMinOfCurrentAndTtiOnRead() {
            Duration ttl = Duration.ofHours(1);
            Duration tti = Duration.ofMinutes(10);
            CombinedExpiryPolicy<String, String> policy = new CombinedExpiryPolicy<>(ttl, tti);

            Duration current = Duration.ofMinutes(5);
            Duration expiry = policy.expireAfterRead("key", "value", current);

            assertThat(expiry).isEqualTo(current); // Min of current and TTI
        }

        @Test
        void shouldReturnTtiWhenCurrentIsLarger() {
            Duration ttl = Duration.ofHours(1);
            Duration tti = Duration.ofMinutes(10);
            CombinedExpiryPolicy<String, String> policy = new CombinedExpiryPolicy<>(ttl, tti);

            Duration current = Duration.ofMinutes(30);
            Duration expiry = policy.expireAfterRead("key", "value", current);

            assertThat(expiry).isEqualTo(tti);
        }
    }

    // ==================== Eternal Policy Tests ====================

    @Nested
    class EternalPolicyTest {

        @Test
        void shouldCreateEternalPolicy() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.eternal();
            assertThat(policy).isNotNull();
        }

        @Test
        void shouldReturnInfiniteOnCreate() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.eternal();

            Duration expiry = policy.expireAfterCreate("key", "value");

            assertThat(expiry).isEqualTo(ExpiryPolicy.INFINITE);
        }

        @Test
        void shouldReturnInfiniteOnUpdate() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.eternal();

            Duration expiry = policy.expireAfterUpdate("key", "value", Duration.ofMinutes(1));

            assertThat(expiry).isEqualTo(ExpiryPolicy.INFINITE);
        }

        @Test
        void shouldReturnInfiniteOnRead() {
            ExpiryPolicy<String, String> policy = ExpiryPolicy.eternal();

            Duration expiry = policy.expireAfterRead("key", "value", Duration.ofMinutes(1));

            assertThat(expiry).isEqualTo(ExpiryPolicy.INFINITE);
        }
    }

    // ==================== Factory Method Tests ====================

    @Test
    void shouldCreateAllPoliciesViaFactoryMethods() {
        assertThat(ExpiryPolicy.ttl(Duration.ofMinutes(1))).isNotNull();
        assertThat(ExpiryPolicy.tti(Duration.ofMinutes(1))).isNotNull();
        assertThat(ExpiryPolicy.combined(Duration.ofHours(1), Duration.ofMinutes(10))).isNotNull();
        assertThat(ExpiryPolicy.eternal()).isNotNull();
    }

    @Test
    void shouldDefineInfiniteConstant() {
        assertThat(ExpiryPolicy.INFINITE).isEqualTo(Duration.ofNanos(Long.MAX_VALUE));
    }
}
