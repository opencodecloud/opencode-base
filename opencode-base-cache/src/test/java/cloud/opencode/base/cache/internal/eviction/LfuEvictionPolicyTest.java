package cloud.opencode.base.cache.internal.eviction;

import cloud.opencode.base.cache.model.CacheEntry;
import cloud.opencode.base.cache.spi.EvictionPolicy;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * LfuEvictionPolicyTest Tests
 * LfuEvictionPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("LfuEvictionPolicy Tests")
class LfuEvictionPolicyTest {

    private LfuEvictionPolicy<String, String> policy;

    @BeforeEach
    void setUp() {
        policy = new LfuEvictionPolicy<>();
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create LFU policy via SPI factory method")
        void shouldCreateViaFactoryMethod() {
            EvictionPolicy<String, String> created = EvictionPolicy.lfu();
            assertThat(created).isInstanceOf(LfuEvictionPolicy.class);
        }
    }

    @Nested
    @DisplayName("Record Write Tests")
    class RecordWriteTests {

        @Test
        @DisplayName("should initialize frequency to zero on recordWrite")
        void shouldInitializeFrequencyToZero() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);

            // Access k1 once
            policy.recordAccess(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);

            // k2 has frequency 0, k1 has frequency 1 - k2 should be evicted
            assertThat(policy.selectVictim(entries)).hasValue("k2");
        }
    }

    @Nested
    @DisplayName("Record Access Tests")
    class RecordAccessTests {

        @Test
        @DisplayName("should increment frequency on recordAccess")
        void shouldIncrementFrequency() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");
            CacheEntry<String, String> entry3 = new CacheEntry<>("k3", "v3");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);
            policy.recordWrite(entry3);

            policy.recordAccess(entry1);
            policy.recordAccess(entry1);
            policy.recordAccess(entry1);
            policy.recordAccess(entry3);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);
            entries.put("k3", entry3);

            // k2 freq=0, k3 freq=1, k1 freq=3 -> k2 should be evicted
            assertThat(policy.selectVictim(entries)).hasValue("k2");
        }
    }

    @Nested
    @DisplayName("Select Victim Tests")
    class SelectVictimTests {

        @Test
        @DisplayName("should return empty for empty entries")
        void shouldReturnEmptyForEmptyEntries() {
            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            assertThat(policy.selectVictim(entries)).isEmpty();
        }

        @Test
        @DisplayName("should select least frequently used entry")
        void shouldSelectLeastFrequentlyUsed() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);

            // Access k2 many times
            for (int i = 0; i < 5; i++) {
                policy.recordAccess(entry2);
            }
            // Access k1 once
            policy.recordAccess(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);

            // k1 has lower frequency
            assertThat(policy.selectVictim(entries)).hasValue("k1");
        }

        @Test
        @DisplayName("should filter by entries map")
        void shouldFilterByEntriesMap() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);

            // Only k2 in entries
            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k2", entry2);

            assertThat(policy.selectVictim(entries)).hasValue("k2");
        }
    }

    @Nested
    @DisplayName("On Removal Tests")
    class OnRemovalTests {

        @Test
        @DisplayName("should remove frequency tracking on removal")
        void shouldRemoveFrequencyTracking() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry1);
            policy.onRemoval("k1");

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            assertThat(policy.selectVictim(entries)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {

        @Test
        @DisplayName("should clear all frequency data on reset")
        void shouldClearAllFrequencyDataOnReset() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry1);
            policy.recordAccess(entry1);
            policy.reset();

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);

            assertThat(policy.selectVictim(entries)).isEmpty();
        }
    }
}
