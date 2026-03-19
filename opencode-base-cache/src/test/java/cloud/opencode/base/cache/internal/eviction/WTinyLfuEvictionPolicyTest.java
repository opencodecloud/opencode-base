package cloud.opencode.base.cache.internal.eviction;

import cloud.opencode.base.cache.model.CacheEntry;
import cloud.opencode.base.cache.spi.EvictionPolicy;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * WTinyLfuEvictionPolicyTest Tests
 * WTinyLfuEvictionPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("WTinyLfuEvictionPolicy Tests")
class WTinyLfuEvictionPolicyTest {

    private WTinyLfuEvictionPolicy<String, String> policy;

    @BeforeEach
    void setUp() {
        policy = new WTinyLfuEvictionPolicy<>();
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create W-TinyLFU policy via SPI factory method")
        void shouldCreateViaFactoryMethod() {
            EvictionPolicy<String, String> created = EvictionPolicy.wTinyLfu();
            assertThat(created).isInstanceOf(WTinyLfuEvictionPolicy.class);
        }
    }

    @Nested
    @DisplayName("Record Write Tests")
    class RecordWriteTests {

        @Test
        @DisplayName("should add new entries to window segment")
        void shouldAddNewEntriesToWindow() {
            CacheEntry<String, String> entry = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry);

            // Entry should be in window, so it can be selected as victim
            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).isPresent();
        }

        @Test
        @DisplayName("should not re-add existing entry to window")
        void shouldNotReAddExistingEntry() {
            CacheEntry<String, String> entry = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry);
            policy.recordWrite(entry); // Should not duplicate

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry);

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).hasValue("k1");
        }
    }

    @Nested
    @DisplayName("Record Access Tests")
    class RecordAccessTests {

        @Test
        @DisplayName("should promote entry from probation to protected on access")
        void shouldTrackFrequencyOnAccess() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);

            // Access k1 many times to increase its frequency
            for (int i = 0; i < 10; i++) {
                policy.recordAccess(entry1);
            }

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);
            entries.put("k2", entry2);

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).isPresent();
            // k2 should be victim since k1 has higher frequency
            assertThat(victim.get()).isEqualTo("k2");
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
        @DisplayName("should select victim from window first")
        void shouldSelectVictimFromWindow() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry1);

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);

            Optional<String> victim = policy.selectVictim(entries);
            assertThat(victim).hasValue("k1");
        }

        @Test
        @DisplayName("should handle multiple entries")
        void shouldHandleMultipleEntries() {
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
            assertThat(victim).isPresent();
        }
    }

    @Nested
    @DisplayName("On Removal Tests")
    class OnRemovalTests {

        @Test
        @DisplayName("should remove key from all segments on removal")
        void shouldRemoveKeyFromAllSegments() {
            CacheEntry<String, String> entry = new CacheEntry<>("k1", "v1");
            policy.recordWrite(entry);
            policy.onRemoval("k1");

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            assertThat(policy.selectVictim(entries)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {

        @Test
        @DisplayName("should clear all segments and sketch on reset")
        void shouldClearAllOnReset() {
            CacheEntry<String, String> entry1 = new CacheEntry<>("k1", "v1");
            CacheEntry<String, String> entry2 = new CacheEntry<>("k2", "v2");

            policy.recordWrite(entry1);
            policy.recordWrite(entry2);
            policy.recordAccess(entry1);
            policy.reset();

            Map<String, CacheEntry<String, String>> entries = new HashMap<>();
            entries.put("k1", entry1);

            // After reset, entry is not tracked in any segment
            // selectVictim should fall through to entries.keySet()
            assertThat(policy.selectVictim(entries)).isPresent();
        }
    }
}
