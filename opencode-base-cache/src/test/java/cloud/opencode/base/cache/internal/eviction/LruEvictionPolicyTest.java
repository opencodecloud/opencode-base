package cloud.opencode.base.cache.internal.eviction;

import cloud.opencode.base.cache.model.CacheEntry;
import cloud.opencode.base.cache.spi.EvictionPolicy;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * LruEvictionPolicyTest Tests
 * LruEvictionPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("LruEvictionPolicy Tests")
class LruEvictionPolicyTest {

    private LruEvictionPolicy<String, String> policy;

    @BeforeEach
    void setUp() {
        policy = new LruEvictionPolicy<>();
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create LRU policy via SPI factory method")
        void shouldCreateViaFactoryMethod() {
            EvictionPolicy<String, String> created = EvictionPolicy.lru();
            assertThat(created).isInstanceOf(LruEvictionPolicy.class);
        }
    }

    @Nested
    @DisplayName("Record Write Tests")
    class RecordWriteTests {

        @Test
        @DisplayName("should track entries on recordWrite")
        void shouldTrackEntriesOnRecordWrite() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).isPresent();
            assertThat(victim.get()).isEqualTo("k1");
        }
    }

    @Nested
    @DisplayName("Record Access Tests")
    class RecordAccessTests {

        @Test
        @DisplayName("should promote entry to most recently used on access")
        void shouldPromoteEntryOnAccess() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");
            CacheEntry<String, String> entry3 = new CacheEntry<>("k3", "v3");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);
            policy.recordWrite(entry3);

            // Access k1 to make it recent
            policy.recordAccess(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);
            entries.put("k3", entry3);

            // k2 is now least recently used
            assertThat(policy.selectVictim(entries)).hasValue("k2");
        }
    }

    @Nested
    @DisplayName("Select Victim Tests")
    class SelectVictimTests {

        @Test
        @DisplayName("should select from entries map when accessOrder is empty")
        void shouldSelectFromEntriesMapWhenEmpty() {
            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", new CacheEntry<>("k1", "v1"));

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).isPresent();
        }

        @Test
        @DisplayName("should return empty for empty entries and empty accessOrder")
        void shouldReturnEmptyForEmptyAll() {
            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            assertThat(policy.selectVictim(entries)).isEmpty();
        }

        @Test
        @DisplayName("should return least recently used entry")
        void shouldReturnLeastRecentlyUsed() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);

            // Access k1, making k2 the LRU
            policy.recordAccess(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);

            assertThat(policy.selectVictim(entries)).hasValue("k2");
        }
    }

    @Nested
    @DisplayName("On Removal Tests")
    class OnRemovalTests {

        @Test
        @DisplayName("should remove key from access tracking on removal")
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

            // After reset, accessOrder is empty so it falls through to entries.keySet()
            assertThat(policy.selectVictim(entries)).isPresent();
        }
    }
}
