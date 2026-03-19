package cloud.opencode.base.cache.internal.eviction;

import cloud.opencode.base.cache.model.CacheEntry;
import cloud.opencode.base.cache.spi.EvictionPolicy;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * FifoEvictionPolicyTest Tests
 * FifoEvictionPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("FifoEvictionPolicy Tests")
class FifoEvictionPolicyTest {

    private FifoEvictionPolicy<String, String> policy;

    @BeforeEach
    void setUp() {
        policy = new FifoEvictionPolicy<>();
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create FIFO policy via SPI factory method")
        void shouldCreateViaFactoryMethod() {
            EvictionPolicy<String, String> created = EvictionPolicy.fifo();
            assertThat(created).isInstanceOf(FifoEvictionPolicy.class);
        }
    }

    @Nested
    @DisplayName("Record Write Tests")
    class RecordWriteTests {

        @Test
        @DisplayName("should track insertion order on recordWrite")
        void shouldTrackInsertionOrder() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");
            CacheEntry<String, String> entry3 = new CacheEntry<>("k3", "v3");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);
            policy.recordWrite(entry3);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);
            entries.put("k3", entry3);

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).hasValue("k1");
        }

        @Test
        @DisplayName("should not reorder existing key on repeated recordWrite")
        void shouldNotReorderExistingKeyOnRepeatedWrite() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);
            // Writing k1 again should NOT change insertion order
            policy.recordWrite(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).hasValue("k1");
        }
    }

    @Nested
    @DisplayName("Record Access Tests")
    class RecordAccessTests {

        @Test
        @DisplayName("recordAccess should not affect eviction order")
        void recordAccessShouldNotAffectOrder() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);

            // Access k1 multiple times - should not change FIFO order
            policy.recordAccess(entry1);
            policy.recordAccess(entry1);
            policy.recordAccess(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).hasValue("k1");
        }
    }

    @Nested
    @DisplayName("Select Victim Tests")
    class SelectVictimTests {

        @Test
        @DisplayName("should return empty for empty entries")
        void shouldReturnEmptyForEmptyEntries() {
            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).isEmpty();
        }

        @Test
        @DisplayName("should select first inserted entry")
        void shouldSelectFirstInserted() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");
            CacheEntry<String, String> entry3 = new CacheEntry<>("k3", "v3");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);
            policy.recordWrite(entry3);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);
            entries.put("k3", entry3);

            assertThat(policy.selectVictim(entries)).hasValue("k1");
        }

        @Test
        @DisplayName("should skip entries not in map")
        void shouldSkipEntriesNotInMap() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);

            // Only k2 is in the entries map
            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k2", entry2);

            assertThat(policy.selectVictim(entries)).hasValue("k2");
        }
    }

    @Nested
    @DisplayName("On Removal Tests")
    class OnRemovalTests {

        @Test
        @DisplayName("should remove key from tracking on removal")
        void shouldRemoveKeyFromTracking() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);
            policy.onRemoval("k1");

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k2", entry2);

            assertThat(policy.selectVictim(entries)).hasValue("k2");
        }
    }

    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {

        @Test
        @DisplayName("should clear all tracking state on reset")
        void shouldClearAllTrackingOnReset() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry1);
            policy.reset();

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);

            assertThat(policy.selectVictim(entries)).isEmpty();
        }
    }
}
