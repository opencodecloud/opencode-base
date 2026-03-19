package cloud.opencode.base.cache.internal.eviction;

import cloud.opencode.base.cache.model.CacheEntry;
import cloud.opencode.base.cache.spi.EvictionPolicy;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Eviction Policy Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class EvictionPolicyTest {

    // ==================== LRU Tests ====================

    @Nested
    class LruEvictionPolicyTest {

        @Test
        void shouldCreateLruPolicy() {
            EvictionPolicy<String, String> policy = EvictionPolicy.lru();
            assertThat(policy).isInstanceOf(LruEvictionPolicy.class);
        }

        @Test
        void shouldEvictLeastRecentlyUsed() {
            LruEvictionPolicy<String, String> policy = new LruEvictionPolicy<>();

            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");
            CacheEntry<String, String> entry3 = new CacheEntry<>("k3", "v3");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);
            policy.recordWrite(entry3);

            // Access k1 to make it more recent
            policy.recordAccess(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);
            entries.put("k3", entry3);

            Optional<String> victim = policy.selectVictim(entries);

            assertThat(victim).isPresent();
            assertThat(victim.get()).isEqualTo("k2"); // Least recently used
        }

        @Test
        void shouldHandleRemoval() {
            LruEvictionPolicy<String, String> policy = new LruEvictionPolicy<>();

            CacheEntry<String, String> entry = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry);
            policy.onRemoval("k1");

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).isEmpty();
        }

        @Test
        void shouldReset() {
            LruEvictionPolicy<String, String> policy = new LruEvictionPolicy<>();

            CacheEntry<String, String> entry = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry);
            policy.reset();

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry);

            Optional<String> victim = policy.selectVictim(entries);
            // After reset, it should still find a victim from entries
            assertThat(victim).isPresent();
        }
    }

    // ==================== LFU Tests ====================

    @Nested
    class LfuEvictionPolicyTest {

        @Test
        void shouldCreateLfuPolicy() {
            EvictionPolicy<String, String> policy = EvictionPolicy.lfu();
            assertThat(policy).isInstanceOf(LfuEvictionPolicy.class);
        }

        @Test
        void shouldEvictLeastFrequentlyUsed() {
            LfuEvictionPolicy<String, String> policy = new LfuEvictionPolicy<>();

            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");
            CacheEntry<String, String> entry3 = new CacheEntry<>("k3", "v3");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);
            policy.recordWrite(entry3);

            // Access k1 and k3 multiple times
            policy.recordAccess(entry1);
            policy.recordAccess(entry1);
            policy.recordAccess(entry3);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);
            entries.put("k3", entry3);

            Optional<String> victim = policy.selectVictim(entries);

            assertThat(victim).isPresent();
            assertThat(victim.get()).isEqualTo("k2"); // Least frequently used
        }

        @Test
        void shouldHandleRemoval() {
            LfuEvictionPolicy<String, String> policy = new LfuEvictionPolicy<>();

            CacheEntry<String, String> entry = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry);
            policy.onRemoval("k1");

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).isEmpty();
        }

        @Test
        void shouldReset() {
            LfuEvictionPolicy<String, String> policy = new LfuEvictionPolicy<>();

            CacheEntry<String, String> entry = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry);
            policy.reset();

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry);

            // After reset, internal frequency tracking is cleared
            // Policy may return empty or find entry by fallback
            Optional<String> victim = policy.selectVictim(entries);
            // selectVictim returns empty when no tracked entries exist after reset
            assertThat(victim).isEmpty();
        }
    }

    // ==================== FIFO Tests ====================

    @Nested
    class FifoEvictionPolicyTest {

        @Test
        void shouldCreateFifoPolicy() {
            EvictionPolicy<String, String> policy = EvictionPolicy.fifo();
            assertThat(policy).isInstanceOf(FifoEvictionPolicy.class);
        }

        @Test
        void shouldEvictFirstIn() {
            FifoEvictionPolicy<String, String> policy = new FifoEvictionPolicy<>();

            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");
            CacheEntry<String, String> entry3 = new CacheEntry<>("k3", "v3");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);
            policy.recordWrite(entry3);

            // Access shouldn't matter for FIFO
            policy.recordAccess(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);
            entries.put("k3", entry3);

            Optional<String> victim = policy.selectVictim(entries);

            assertThat(victim).isPresent();
            assertThat(victim.get()).isEqualTo("k1"); // First in
        }

        @Test
        void shouldHandleRemoval() {
            FifoEvictionPolicy<String, String> policy = new FifoEvictionPolicy<>();

            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);
            policy.onRemoval("k1");

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k2", entry2);

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).hasValue("k2");
        }

        @Test
        void shouldReset() {
            FifoEvictionPolicy<String, String> policy = new FifoEvictionPolicy<>();

            CacheEntry<String, String> entry = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry);
            policy.reset();

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry);

            // After reset, internal insertion order is cleared
            // Policy may return empty or find entry by fallback
            Optional<String> victim = policy.selectVictim(entries);
            // selectVictim returns empty when no tracked entries exist after reset
            assertThat(victim).isEmpty();
        }
    }

    // ==================== W-TinyLFU Tests ====================

    @Nested
    class WTinyLfuEvictionPolicyTest {

        @Test
        void shouldCreateWTinyLfuPolicy() {
            EvictionPolicy<String, String> policy = EvictionPolicy.wTinyLfu();
            assertThat(policy).isInstanceOf(WTinyLfuEvictionPolicy.class);
        }

        @Test
        void shouldTrackFrequency() {
            WTinyLfuEvictionPolicy<String, String> policy = new WTinyLfuEvictionPolicy<>();

            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);

            // Access k1 multiple times
            for (int i = 0; i < 10; i++) {
                policy.recordAccess(entry1);
            }

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);

            Optional<String> victim = policy.selectVictim(entries);

            assertThat(victim).isPresent();
            assertThat(victim.get()).isEqualTo("k2"); // Less frequently used
        }

        @Test
        void shouldHandleRemoval() {
            WTinyLfuEvictionPolicy<String, String> policy = new WTinyLfuEvictionPolicy<>();

            CacheEntry<String, String> entry = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry);
            policy.onRemoval("k1");

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).isEmpty();
        }

        @Test
        void shouldReset() {
            WTinyLfuEvictionPolicy<String, String> policy = new WTinyLfuEvictionPolicy<>();

            CacheEntry<String, String> entry = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry);
            policy.reset();

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry);

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).isPresent();
        }

        @Test
        void shouldSelectFromEmptyEntries() {
            WTinyLfuEvictionPolicy<String, String> policy = new WTinyLfuEvictionPolicy<>();

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).isEmpty();
        }
    }

    // ==================== Factory Method Tests ====================

    @Test
    void shouldCreateAllPoliciesViaFactoryMethods() {
        assertThat(EvictionPolicy.lru()).isNotNull();
        assertThat(EvictionPolicy.lfu()).isNotNull();
        assertThat(EvictionPolicy.fifo()).isNotNull();
        assertThat(EvictionPolicy.wTinyLfu()).isNotNull();
    }
}
