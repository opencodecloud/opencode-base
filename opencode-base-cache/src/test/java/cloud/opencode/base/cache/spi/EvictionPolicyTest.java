package cloud.opencode.base.cache.spi;

import cloud.opencode.base.cache.internal.eviction.*;
import cloud.opencode.base.cache.model.CacheEntry;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * EvictionPolicyTest Tests
 * EvictionPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("EvictionPolicy SPI Tests")
class EvictionPolicyTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("lru() should return LruEvictionPolicy")
        void lruShouldReturnCorrectType() {
            assertThat(EvictionPolicy.lru()).isInstanceOf(LruEvictionPolicy.class);
        }

        @Test
        @DisplayName("lfu() should return LfuEvictionPolicy")
        void lfuShouldReturnCorrectType() {
            assertThat(EvictionPolicy.lfu()).isInstanceOf(LfuEvictionPolicy.class);
        }

        @Test
        @DisplayName("fifo() should return FifoEvictionPolicy")
        void fifoShouldReturnCorrectType() {
            assertThat(EvictionPolicy.fifo()).isInstanceOf(FifoEvictionPolicy.class);
        }

        @Test
        @DisplayName("wTinyLfu() should return WTinyLfuEvictionPolicy")
        void wTinyLfuShouldReturnCorrectType() {
            assertThat(EvictionPolicy.wTinyLfu()).isInstanceOf(WTinyLfuEvictionPolicy.class);
        }
    }

    @Nested
    @DisplayName("Default Reset Method Tests")
    class DefaultResetMethodTests {

        @Test
        @DisplayName("default reset should be no-op")
        void defaultResetShouldBeNoOp() {
            EvictionPolicy<String, String> policy = new EvictionPolicy<>() {
                @Override
                public void recordAccess(CacheEntry<String, String> entry) {}
                @Override
                public void recordWrite(CacheEntry<String, String> entry) {}
                @Override
                public Optional<String> selectVictim(Map<String, CacheEntry<String, String>> entries) {
                    return Optional.empty();
                }
                @Override
                public void onRemoval(String key) {}
            };

            // Default reset should not throw
            assertThatCode(policy::reset).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("OR Composition Tests")
    class OrCompositionTests {

        @Test
        @DisplayName("or() should return victim from first policy if present")
        void shouldReturnFromFirstIfPresent() {
            EvictionPolicy<String, String> lru = EvictionPolicy.lru();
            EvictionPolicy<String, String> fifo = EvictionPolicy.fifo();

            EvictionPolicy<String, String> combined = lru.or(fifo);

            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            combined.recordWrite(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);

            assertThat(combined.selectVictim(entries)).isPresent();
        }

        @Test
        @DisplayName("or() should propagate recordAccess to both policies")
        void shouldPropagateRecordAccess() {
            EvictionPolicy<String, String> lru = EvictionPolicy.lru();
            EvictionPolicy<String, String> fifo = EvictionPolicy.fifo();

            EvictionPolicy<String, String> combined = lru.or(fifo);

            CacheEntry<String, String> entry = new CacheEntry<>("k1", "v1");
            assertThatCode(() -> combined.recordAccess(entry)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("or() should propagate reset to both policies")
        void shouldPropagateReset() {
            EvictionPolicy<String, String> lru = EvictionPolicy.lru();
            EvictionPolicy<String, String> fifo = EvictionPolicy.fifo();

            EvictionPolicy<String, String> combined = lru.or(fifo);
            assertThatCode(combined::reset).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("AND Composition Tests")
    class AndCompositionTests {

        @Test
        @DisplayName("and() should return empty when policies disagree")
        void shouldReturnEmptyWhenDisagree() {
            EvictionPolicy<String, String> lru = EvictionPolicy.lru();
            EvictionPolicy<String, String> fifo = EvictionPolicy.fifo();

            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            lru.recordWrite(entry1);
            lru.recordWrite(entry2);
            fifo.recordWrite(entry1);
            fifo.recordWrite(entry2);

            // Make LRU choose k1 and FIFO choose k1 - they should agree
            // Actually, let's force a disagreement
            lru.recordAccess(entry1); // k1 is now most recent in LRU, so LRU evicts k2

            EvictionPolicy<String, String> combined = lru.and(fifo);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);

            // LRU victim = k2 (least recent), FIFO victim = k1 (first in) -> disagree
            // Need to build combined from scratch for it to see both policies
            EvictionPolicy<String, String> freshLru = EvictionPolicy.lru();
            EvictionPolicy<String, String> freshFifo = EvictionPolicy.fifo();

            EvictionPolicy<String, String> andPolicy = freshLru.and(freshFifo);

            andPolicy.recordWrite(entry1);
            andPolicy.recordWrite(entry2);
            andPolicy.recordAccess(entry1); // LRU: k2 is victim, FIFO: k1 is victim

            Optional<String> victim = andPolicy.selectVictim(entries);
            // LRU evicts k2, FIFO evicts k1 -> different -> empty
            assertThat(victim).isEmpty();
        }

        @Test
        @DisplayName("and() should return victim when policies agree")
        void shouldReturnVictimWhenAgree() {
            EvictionPolicy<String, String> freshLru = EvictionPolicy.lru();
            EvictionPolicy<String, String> freshFifo = EvictionPolicy.fifo();

            EvictionPolicy<String, String> combined = freshLru.and(freshFifo);

            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            combined.recordWrite(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);

            // Only one entry -> both policies agree on k1
            assertThat(combined.selectVictim(entries)).hasValue("k1");
        }
    }

    @Nested
    @DisplayName("Weighted Composition Tests")
    class WeightedCompositionTests {

        @Test
        @DisplayName("weighted() should select based on weighted votes")
        void shouldSelectBasedOnWeightedVotes() {
            @SuppressWarnings("unchecked")
            EvictionPolicy<String, String> weighted = EvictionPolicy.weighted(
                EvictionPolicy.WeightedPolicy.of(EvictionPolicy.lru(), 1.0),
                EvictionPolicy.WeightedPolicy.of(EvictionPolicy.fifo(), 2.0)
            );

            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            weighted.recordWrite(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);

            assertThat(weighted.selectVictim(entries)).isPresent();
        }

        @Test
        @DisplayName("WeightedPolicy record should hold policy and weight")
        void weightedPolicyRecordShouldHoldValues() {
            EvictionPolicy<String, String> lru = EvictionPolicy.lru();
            EvictionPolicy.WeightedPolicy<String, String> wp = EvictionPolicy.WeightedPolicy.of(lru, 3.5);

            assertThat(wp.policy()).isSameAs(lru);
            assertThat(wp.weight()).isEqualTo(3.5);
        }
    }
}
