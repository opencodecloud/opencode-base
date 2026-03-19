package cloud.opencode.base.cache.internal.stats;

import cloud.opencode.base.cache.CacheStats;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * LongAdderStatsCounter Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class LongAdderStatsCounterTest {

    @Test
    void shouldRecordHits() {
        LongAdderStatsCounter counter = new LongAdderStatsCounter();

        counter.recordHits(1);
        counter.recordHits(5);
        counter.recordHits(10);

        CacheStats stats = counter.snapshot();
        assertThat(stats.hitCount()).isEqualTo(16);
    }

    @Test
    void shouldRecordMisses() {
        LongAdderStatsCounter counter = new LongAdderStatsCounter();

        counter.recordMisses(1);
        counter.recordMisses(2);
        counter.recordMisses(3);

        CacheStats stats = counter.snapshot();
        assertThat(stats.missCount()).isEqualTo(6);
    }

    @Test
    void shouldRecordLoadSuccess() {
        LongAdderStatsCounter counter = new LongAdderStatsCounter();

        counter.recordLoadSuccess(1000000);
        counter.recordLoadSuccess(2000000);

        CacheStats stats = counter.snapshot();
        assertThat(stats.loadSuccessCount()).isEqualTo(2);
        assertThat(stats.totalLoadTime()).isEqualTo(3000000);
    }

    @Test
    void shouldRecordLoadFailure() {
        LongAdderStatsCounter counter = new LongAdderStatsCounter();

        counter.recordLoadFailure(500000);
        counter.recordLoadFailure(600000);

        CacheStats stats = counter.snapshot();
        assertThat(stats.loadFailureCount()).isEqualTo(2);
        assertThat(stats.totalLoadTime()).isEqualTo(1100000);
    }

    @Test
    void shouldRecordEviction() {
        LongAdderStatsCounter counter = new LongAdderStatsCounter();

        counter.recordEviction(1);
        counter.recordEviction(5);
        counter.recordEviction(10);

        CacheStats stats = counter.snapshot();
        assertThat(stats.evictionCount()).isEqualTo(3);
        assertThat(stats.evictionWeight()).isEqualTo(16);
    }

    @Test
    void shouldRecordAllStats() {
        LongAdderStatsCounter counter = new LongAdderStatsCounter();

        counter.recordHits(100);
        counter.recordMisses(20);
        counter.recordLoadSuccess(1000000);
        counter.recordLoadFailure(500000);
        counter.recordEviction(5);

        CacheStats stats = counter.snapshot();

        assertThat(stats.hitCount()).isEqualTo(100);
        assertThat(stats.missCount()).isEqualTo(20);
        assertThat(stats.loadSuccessCount()).isEqualTo(1);
        assertThat(stats.loadFailureCount()).isEqualTo(1);
        assertThat(stats.totalLoadTime()).isEqualTo(1500000);
        assertThat(stats.evictionCount()).isEqualTo(1);
        assertThat(stats.evictionWeight()).isEqualTo(5);
    }

    @Test
    void shouldReturnConsistentSnapshot() {
        LongAdderStatsCounter counter = new LongAdderStatsCounter();

        counter.recordHits(100);
        CacheStats snapshot1 = counter.snapshot();

        counter.recordHits(50);
        CacheStats snapshot2 = counter.snapshot();

        assertThat(snapshot1.hitCount()).isEqualTo(100);
        assertThat(snapshot2.hitCount()).isEqualTo(150);
    }

    @Test
    void shouldBeThreadSafe() throws Exception {
        LongAdderStatsCounter counter = new LongAdderStatsCounter();
        int threadCount = 10;
        int operationsPerThread = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        counter.recordHits(1);
                        counter.recordMisses(1);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        CacheStats stats = counter.snapshot();

        assertThat(stats.hitCount()).isEqualTo(threadCount * operationsPerThread);
        assertThat(stats.missCount()).isEqualTo(threadCount * operationsPerThread);
    }

    @Test
    void shouldHandleZeroWeight() {
        LongAdderStatsCounter counter = new LongAdderStatsCounter();

        counter.recordEviction(0);

        CacheStats stats = counter.snapshot();
        assertThat(stats.evictionCount()).isEqualTo(1);
        assertThat(stats.evictionWeight()).isEqualTo(0);
    }

    @Test
    void shouldHandleLargeValues() {
        LongAdderStatsCounter counter = new LongAdderStatsCounter();

        counter.recordHits(Integer.MAX_VALUE);
        counter.recordHits(Integer.MAX_VALUE);

        CacheStats stats = counter.snapshot();
        assertThat(stats.hitCount()).isEqualTo(2L * Integer.MAX_VALUE);
    }
}
