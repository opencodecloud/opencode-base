package cloud.opencode.base.cache;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.within;

/**
 * CacheStats Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class CacheStatsTest {

    @Test
    void shouldCreateEmptyStats() {
        CacheStats stats = CacheStats.empty();

        assertThat(stats.hitCount()).isEqualTo(0);
        assertThat(stats.missCount()).isEqualTo(0);
        assertThat(stats.loadSuccessCount()).isEqualTo(0);
        assertThat(stats.loadFailureCount()).isEqualTo(0);
        assertThat(stats.totalLoadTime()).isEqualTo(0);
        assertThat(stats.evictionCount()).isEqualTo(0);
        assertThat(stats.evictionWeight()).isEqualTo(0);
    }

    @Test
    void shouldCreateStatsWithValues() {
        CacheStats stats = CacheStats.of(100, 20, 15, 5, 1000000, 10, 10);

        assertThat(stats.hitCount()).isEqualTo(100);
        assertThat(stats.missCount()).isEqualTo(20);
        assertThat(stats.loadSuccessCount()).isEqualTo(15);
        assertThat(stats.loadFailureCount()).isEqualTo(5);
        assertThat(stats.totalLoadTime()).isEqualTo(1000000);
        assertThat(stats.evictionCount()).isEqualTo(10);
        assertThat(stats.evictionWeight()).isEqualTo(10);
    }

    @Test
    void shouldCalculateRequestCount() {
        CacheStats stats = CacheStats.of(100, 20, 0, 0, 0, 0, 0);
        assertThat(stats.requestCount()).isEqualTo(120);
    }

    @Test
    void shouldCalculateHitRate() {
        CacheStats stats = CacheStats.of(80, 20, 0, 0, 0, 0, 0);
        assertThat(stats.hitRate()).isEqualTo(0.8);
    }

    @Test
    void shouldReturnOneForHitRateWhenNoRequests() {
        CacheStats stats = CacheStats.empty();
        assertThat(stats.hitRate()).isEqualTo(1.0);
    }

    @Test
    void shouldCalculateMissRate() {
        CacheStats stats = CacheStats.of(80, 20, 0, 0, 0, 0, 0);
        assertThat(stats.missRate()).isCloseTo(0.2, within(0.001));
    }

    @Test
    void shouldCalculateLoadCount() {
        CacheStats stats = CacheStats.of(0, 0, 15, 5, 0, 0, 0);
        assertThat(stats.loadCount()).isEqualTo(20);
    }

    @Test
    void shouldCalculateLoadFailureRate() {
        CacheStats stats = CacheStats.of(0, 0, 15, 5, 0, 0, 0);
        assertThat(stats.loadFailureRate()).isEqualTo(0.25);
    }

    @Test
    void shouldReturnZeroForLoadFailureRateWhenNoLoads() {
        CacheStats stats = CacheStats.empty();
        assertThat(stats.loadFailureRate()).isEqualTo(0.0);
    }

    @Test
    void shouldCalculateAverageLoadPenalty() {
        CacheStats stats = CacheStats.of(0, 0, 10, 10, 2000000, 0, 0);
        assertThat(stats.averageLoadPenalty()).isEqualTo(100000.0);
    }

    @Test
    void shouldReturnZeroForAverageLoadPenaltyWhenNoLoads() {
        CacheStats stats = CacheStats.empty();
        assertThat(stats.averageLoadPenalty()).isEqualTo(0.0);
    }

    @Test
    void shouldSubtractStats() {
        CacheStats before = CacheStats.of(100, 20, 10, 2, 1000000, 5, 5);
        CacheStats after = CacheStats.of(150, 30, 15, 3, 1500000, 8, 8);

        CacheStats delta = after.minus(before);

        assertThat(delta.hitCount()).isEqualTo(50);
        assertThat(delta.missCount()).isEqualTo(10);
        assertThat(delta.loadSuccessCount()).isEqualTo(5);
        assertThat(delta.loadFailureCount()).isEqualTo(1);
        assertThat(delta.totalLoadTime()).isEqualTo(500000);
        assertThat(delta.evictionCount()).isEqualTo(3);
        assertThat(delta.evictionWeight()).isEqualTo(3);
    }

    @Test
    void shouldNotGoNegativeOnMinus() {
        CacheStats large = CacheStats.of(100, 100, 100, 100, 100, 100, 100);
        CacheStats small = CacheStats.of(10, 10, 10, 10, 10, 10, 10);

        CacheStats delta = small.minus(large);

        assertThat(delta.hitCount()).isEqualTo(0);
        assertThat(delta.missCount()).isEqualTo(0);
        assertThat(delta.loadSuccessCount()).isEqualTo(0);
        assertThat(delta.loadFailureCount()).isEqualTo(0);
        assertThat(delta.totalLoadTime()).isEqualTo(0);
        assertThat(delta.evictionCount()).isEqualTo(0);
        assertThat(delta.evictionWeight()).isEqualTo(0);
    }

    @Test
    void shouldAddStats() {
        CacheStats stats1 = CacheStats.of(100, 20, 10, 2, 1000000, 5, 5);
        CacheStats stats2 = CacheStats.of(50, 10, 5, 1, 500000, 3, 3);

        CacheStats combined = stats1.plus(stats2);

        assertThat(combined.hitCount()).isEqualTo(150);
        assertThat(combined.missCount()).isEqualTo(30);
        assertThat(combined.loadSuccessCount()).isEqualTo(15);
        assertThat(combined.loadFailureCount()).isEqualTo(3);
        assertThat(combined.totalLoadTime()).isEqualTo(1500000);
        assertThat(combined.evictionCount()).isEqualTo(8);
        assertThat(combined.evictionWeight()).isEqualTo(8);
    }
}
