package cloud.opencode.base.captcha.store;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * MemoryCaptchaStore Test - Unit tests for in-memory CAPTCHA storage
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class MemoryCaptchaStoreTest {

    private MemoryCaptchaStore store;

    @BeforeEach
    void setUp() {
        store = new MemoryCaptchaStore();
    }

    @AfterEach
    void tearDown() {
        store.shutdown();
    }

    @Nested
    @DisplayName("Store Tests")
    class StoreTests {

        @Test
        @DisplayName("should store and retrieve captcha")
        void shouldStoreAndRetrieveCaptcha() {
            store.store("id-1", "answer-1", Duration.ofMinutes(5));

            Optional<String> result = store.get("id-1");

            assertThat(result).isPresent().contains("answer-1");
        }

        @Test
        @DisplayName("should overwrite existing entry with same id")
        void shouldOverwriteExistingEntryWithSameId() {
            store.store("id-1", "original", Duration.ofMinutes(5));
            store.store("id-1", "updated", Duration.ofMinutes(5));

            Optional<String> result = store.get("id-1");

            assertThat(result).isPresent().contains("updated");
        }

        @Test
        @DisplayName("should store multiple captchas")
        void shouldStoreMultipleCaptchas() {
            store.store("id-1", "a1", Duration.ofMinutes(5));
            store.store("id-2", "a2", Duration.ofMinutes(5));
            store.store("id-3", "a3", Duration.ofMinutes(5));

            assertThat(store.size()).isEqualTo(3);
            assertThat(store.get("id-1")).contains("a1");
            assertThat(store.get("id-2")).contains("a2");
            assertThat(store.get("id-3")).contains("a3");
        }
    }

    @Nested
    @DisplayName("Get Tests")
    class GetTests {

        @Test
        @DisplayName("should return empty for nonexistent id")
        void shouldReturnEmptyForNonexistentId() {
            Optional<String> result = store.get("nonexistent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should not remove entry on get")
        void shouldNotRemoveEntryOnGet() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            store.get("id-1");
            Optional<String> result = store.get("id-1");

            assertThat(result).isPresent().contains("answer");
        }

        @Test
        @DisplayName("should return empty for expired entry")
        void shouldReturnEmptyForExpiredEntry() throws InterruptedException {
            store.store("id-1", "answer", Duration.ofMillis(100));

            Thread.sleep(200);

            Optional<String> result = store.get("id-1");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("GetAndRemove Tests")
    class GetAndRemoveTests {

        @Test
        @DisplayName("should retrieve and remove entry")
        void shouldRetrieveAndRemoveEntry() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            Optional<String> result = store.getAndRemove("id-1");

            assertThat(result).isPresent().contains("answer");
            assertThat(store.exists("id-1")).isFalse();
        }

        @Test
        @DisplayName("should return empty for nonexistent id")
        void shouldReturnEmptyForNonexistentId() {
            Optional<String> result = store.getAndRemove("nonexistent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty for expired entry")
        void shouldReturnEmptyForExpiredEntry() throws InterruptedException {
            store.store("id-1", "answer", Duration.ofMillis(100));

            Thread.sleep(200);

            Optional<String> result = store.getAndRemove("id-1");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty on second call (single-use)")
        void shouldReturnEmptyOnSecondCall() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            store.getAndRemove("id-1");
            Optional<String> result = store.getAndRemove("id-1");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Exists Tests")
    class ExistsTests {

        @Test
        @DisplayName("should return true for existing entry")
        void shouldReturnTrueForExistingEntry() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            assertThat(store.exists("id-1")).isTrue();
        }

        @Test
        @DisplayName("should return false for nonexistent entry")
        void shouldReturnFalseForNonexistentEntry() {
            assertThat(store.exists("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("should return false for expired entry")
        void shouldReturnFalseForExpiredEntry() throws InterruptedException {
            store.store("id-1", "answer", Duration.ofMillis(100));

            Thread.sleep(200);

            assertThat(store.exists("id-1")).isFalse();
        }

        @Test
        @DisplayName("should return false after remove")
        void shouldReturnFalseAfterRemove() {
            store.store("id-1", "answer", Duration.ofMinutes(5));
            store.remove("id-1");

            assertThat(store.exists("id-1")).isFalse();
        }
    }

    @Nested
    @DisplayName("Remove Tests")
    class RemoveTests {

        @Test
        @DisplayName("should remove existing entry")
        void shouldRemoveExistingEntry() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            store.remove("id-1");

            assertThat(store.exists("id-1")).isFalse();
            assertThat(store.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("should not throw when removing nonexistent entry")
        void shouldNotThrowWhenRemovingNonexistentEntry() {
            assertThatCode(() -> store.remove("nonexistent"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should only remove specified entry")
        void shouldOnlyRemoveSpecifiedEntry() {
            store.store("id-1", "a1", Duration.ofMinutes(5));
            store.store("id-2", "a2", Duration.ofMinutes(5));

            store.remove("id-1");

            assertThat(store.exists("id-1")).isFalse();
            assertThat(store.exists("id-2")).isTrue();
        }
    }

    @Nested
    @DisplayName("ClearExpired Tests")
    class ClearExpiredTests {

        @Test
        @DisplayName("should clear expired entries")
        void shouldClearExpiredEntries() throws InterruptedException {
            store.store("expired-1", "a1", Duration.ofMillis(100));
            store.store("expired-2", "a2", Duration.ofMillis(100));
            store.store("valid-1", "a3", Duration.ofMinutes(5));

            Thread.sleep(200);

            store.clearExpired();

            assertThat(store.size()).isEqualTo(1);
            assertThat(store.exists("valid-1")).isTrue();
        }

        @Test
        @DisplayName("should not throw when no expired entries")
        void shouldNotThrowWhenNoExpiredEntries() {
            store.store("valid-1", "a1", Duration.ofMinutes(5));

            assertThatCode(() -> store.clearExpired())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should not throw on empty store")
        void shouldNotThrowOnEmptyStore() {
            assertThatCode(() -> store.clearExpired())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("ClearAll Tests")
    class ClearAllTests {

        @Test
        @DisplayName("should clear all entries")
        void shouldClearAllEntries() {
            store.store("id-1", "a1", Duration.ofMinutes(5));
            store.store("id-2", "a2", Duration.ofMinutes(5));
            store.store("id-3", "a3", Duration.ofMinutes(5));

            store.clearAll();

            assertThat(store.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("should not throw on empty store")
        void shouldNotThrowOnEmptyStore() {
            assertThatCode(() -> store.clearAll())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Size Tests")
    class SizeTests {

        @Test
        @DisplayName("should return 0 for empty store")
        void shouldReturnZeroForEmptyStore() {
            assertThat(store.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return correct size")
        void shouldReturnCorrectSize() {
            store.store("id-1", "a1", Duration.ofMinutes(5));
            store.store("id-2", "a2", Duration.ofMinutes(5));

            assertThat(store.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("should update after removal")
        void shouldUpdateAfterRemoval() {
            store.store("id-1", "a1", Duration.ofMinutes(5));
            store.store("id-2", "a2", Duration.ofMinutes(5));
            store.remove("id-1");

            assertThat(store.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Max Size Tests")
    class MaxSizeTests {

        @Test
        @DisplayName("should enforce max size by removing oldest")
        void shouldEnforceMaxSizeByRemovingOldest() throws InterruptedException {
            MemoryCaptchaStore smallStore = new MemoryCaptchaStore(2);
            try {
                smallStore.store("id-1", "a1", Duration.ofMinutes(1));
                Thread.sleep(10);
                smallStore.store("id-2", "a2", Duration.ofMinutes(5));
                Thread.sleep(10);
                // This should evict the oldest (id-1 has earlier expiry)
                smallStore.store("id-3", "a3", Duration.ofMinutes(5));

                assertThat(smallStore.size()).isLessThanOrEqualTo(2);
                assertThat(smallStore.exists("id-3")).isTrue();
            } finally {
                smallStore.shutdown();
            }
        }

        @Test
        @DisplayName("should use default max size of 10000")
        void shouldUseDefaultMaxSize() {
            // Default constructor uses 10000 - just verify it works
            MemoryCaptchaStore defaultStore = new MemoryCaptchaStore();
            try {
                for (int i = 0; i < 100; i++) {
                    defaultStore.store("id-" + i, "a" + i, Duration.ofMinutes(5));
                }
                assertThat(defaultStore.size()).isEqualTo(100);
            } finally {
                defaultStore.shutdown();
            }
        }
    }

    @Nested
    @DisplayName("TTL Tests")
    class TtlTests {

        @Test
        @DisplayName("should respect TTL for entry availability")
        void shouldRespectTtlForEntryAvailability() throws InterruptedException {
            store.store("ttl-1", "answer", Duration.ofMillis(200));

            assertThat(store.exists("ttl-1")).isTrue();

            Thread.sleep(300);

            assertThat(store.exists("ttl-1")).isFalse();
        }

        @Test
        @DisplayName("should handle very short TTL")
        void shouldHandleVeryShortTtl() throws InterruptedException {
            store.store("short-ttl", "answer", Duration.ofMillis(50));

            Thread.sleep(100);

            assertThat(store.get("short-ttl")).isEmpty();
        }

        @Test
        @DisplayName("should handle long TTL")
        void shouldHandleLongTtl() {
            store.store("long-ttl", "answer", Duration.ofHours(24));

            assertThat(store.get("long-ttl")).isPresent().contains("answer");
        }
    }

    @Nested
    @DisplayName("Shutdown Tests")
    class ShutdownTests {

        @Test
        @DisplayName("should not throw on shutdown")
        void shouldNotThrowOnShutdown() {
            MemoryCaptchaStore s = new MemoryCaptchaStore();

            assertThatCode(s::shutdown).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should not throw on double shutdown")
        void shouldNotThrowOnDoubleShutdown() {
            MemoryCaptchaStore s = new MemoryCaptchaStore();
            s.shutdown();

            assertThatCode(s::shutdown).doesNotThrowAnyException();
        }
    }
}
