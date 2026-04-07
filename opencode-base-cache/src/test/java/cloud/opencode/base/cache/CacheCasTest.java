package cloud.opencode.base.cache;

import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * CAS (Compare-And-Swap) operations tests for Cache interface
 * Cache 接口 CAS（比较并交换）操作测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.3
 */
class CacheCasTest {

    @BeforeEach
    void setup() {
        CacheManager.getInstance().reset();
    }

    // ==================== replaceIf ====================

    @Nested
    @DisplayName("replaceIf")
    class ReplaceIfTests {

        @Test
        @DisplayName("should replace when condition is met")
        void shouldReplaceWhenConditionMet() {
            Cache<String, String> cache = OpenCache.getOrCreate("cas-test");
            cache.put("key", "old-value");

            boolean replaced = cache.replaceIf("key", v -> v.equals("old-value"), "new-value");

            assertThat(replaced).isTrue();
            assertThat(cache.get("key")).isEqualTo("new-value");
        }

        @Test
        @DisplayName("should not replace when condition is not met")
        void shouldNotReplaceWhenConditionNotMet() {
            Cache<String, String> cache = OpenCache.getOrCreate("cas-test");
            cache.put("key", "old-value");

            boolean replaced = cache.replaceIf("key", v -> v.equals("other-value"), "new-value");

            assertThat(replaced).isFalse();
            assertThat(cache.get("key")).isEqualTo("old-value");
        }

        @Test
        @DisplayName("should not replace when key does not exist")
        void shouldNotReplaceWhenKeyMissing() {
            Cache<String, String> cache = OpenCache.getOrCreate("cas-test");

            boolean replaced = cache.replaceIf("missing", v -> true, "new-value");

            assertThat(replaced).isFalse();
            assertThat(cache.get("missing")).isNull();
        }

        @Test
        @DisplayName("should replace with predicate on value property")
        void shouldReplaceWithPredicateOnValueProperty() {
            Cache<String, Integer> cache = OpenCache.getOrCreate("cas-int");
            cache.put("counter", 10);

            boolean replaced = cache.replaceIf("counter", v -> v < 100, 20);

            assertThat(replaced).isTrue();
            assertThat(cache.get("counter")).isEqualTo(20);
        }
    }

    // ==================== computeIfMatch ====================

    @Nested
    @DisplayName("computeIfMatch")
    class ComputeIfMatchTests {

        @Test
        @DisplayName("should compute when condition is met")
        void shouldComputeWhenConditionMet() {
            Cache<String, Integer> cache = OpenCache.getOrCreate("cas-compute");
            cache.put("counter", 5);

            Optional<Integer> result = cache.computeIfMatch("counter", v -> v < 10, v -> v * 2);

            assertThat(result).hasValue(10);
            assertThat(cache.get("counter")).isEqualTo(10);
        }

        @Test
        @DisplayName("should return empty when condition is not met")
        void shouldReturnEmptyWhenConditionNotMet() {
            Cache<String, Integer> cache = OpenCache.getOrCreate("cas-compute");
            cache.put("counter", 50);

            Optional<Integer> result = cache.computeIfMatch("counter", v -> v < 10, v -> v * 2);

            assertThat(result).isEmpty();
            assertThat(cache.get("counter")).isEqualTo(50);
        }

        @Test
        @DisplayName("should return empty when key does not exist")
        void shouldReturnEmptyWhenKeyMissing() {
            Cache<String, Integer> cache = OpenCache.getOrCreate("cas-compute");

            Optional<Integer> result = cache.computeIfMatch("missing", v -> true, v -> v * 2);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when remapper returns null")
        void shouldReturnEmptyWhenRemapperReturnsNull() {
            Cache<String, String> cache = OpenCache.getOrCreate("cas-compute-null");
            cache.put("key", "value");

            Optional<String> result = cache.computeIfMatch("key", v -> true, v -> null);

            assertThat(result).isEmpty();
            // Original value should be retained (remapper returning null means no-op)
            assertThat(cache.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("should apply remapper function correctly")
        void shouldApplyRemapperCorrectly() {
            Cache<String, String> cache = OpenCache.getOrCreate("cas-remap");
            cache.put("greeting", "hello");

            Optional<String> result = cache.computeIfMatch(
                    "greeting",
                    v -> v.startsWith("h"),
                    v -> v.toUpperCase()
            );

            assertThat(result).hasValue("HELLO");
            assertThat(cache.get("greeting")).isEqualTo("HELLO");
        }
    }
}
