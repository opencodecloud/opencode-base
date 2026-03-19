package cloud.opencode.base.cache.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * RemovalCause Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class RemovalCauseTest {

    @Test
    void shouldHaveAllCauses() {
        assertThat(RemovalCause.values()).hasSize(5);
        assertThat(RemovalCause.values()).containsExactly(
                RemovalCause.EXPLICIT,
                RemovalCause.REPLACED,
                RemovalCause.EXPIRED,
                RemovalCause.SIZE,
                RemovalCause.COLLECTED
        );
    }

    @Test
    void shouldIdentifyEvictedCauses() {
        assertThat(RemovalCause.EXPLICIT.wasEvicted()).isFalse();
        assertThat(RemovalCause.REPLACED.wasEvicted()).isFalse();
        assertThat(RemovalCause.EXPIRED.wasEvicted()).isTrue();
        assertThat(RemovalCause.SIZE.wasEvicted()).isTrue();
        assertThat(RemovalCause.COLLECTED.wasEvicted()).isTrue();
    }

    @Test
    void shouldIdentifyExplicitCauses() {
        assertThat(RemovalCause.EXPLICIT.wasExplicit()).isTrue();
        assertThat(RemovalCause.REPLACED.wasExplicit()).isTrue();
        assertThat(RemovalCause.EXPIRED.wasExplicit()).isFalse();
        assertThat(RemovalCause.SIZE.wasExplicit()).isFalse();
        assertThat(RemovalCause.COLLECTED.wasExplicit()).isFalse();
    }

    @Test
    void shouldHaveCorrectValueOf() {
        assertThat(RemovalCause.valueOf("EXPLICIT")).isEqualTo(RemovalCause.EXPLICIT);
        assertThat(RemovalCause.valueOf("REPLACED")).isEqualTo(RemovalCause.REPLACED);
        assertThat(RemovalCause.valueOf("EXPIRED")).isEqualTo(RemovalCause.EXPIRED);
        assertThat(RemovalCause.valueOf("SIZE")).isEqualTo(RemovalCause.SIZE);
        assertThat(RemovalCause.valueOf("COLLECTED")).isEqualTo(RemovalCause.COLLECTED);
    }
}
