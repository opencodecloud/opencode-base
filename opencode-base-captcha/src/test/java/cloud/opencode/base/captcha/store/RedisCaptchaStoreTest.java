package cloud.opencode.base.captcha.store;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * RedisCaptchaStore Test - Unit tests for Redis-based CAPTCHA storage with mock functions
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class RedisCaptchaStoreTest {

    private Map<String, String> mockRedis;
    private RedisCaptchaStore store;

    @BeforeEach
    void setUp() {
        mockRedis = new HashMap<>();
        store = RedisCaptchaStore.builder()
            .setter((key, value, ttl) -> mockRedis.put(key, value))
            .getter(mockRedis::get)
            .deleter(mockRedis::remove)
            .build();
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("should create store with all required functions")
        void shouldCreateStoreWithAllRequiredFunctions() {
            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .setter((key, value, ttl) -> {})
                .getter(key -> null)
                .deleter(key -> {})
                .build();

            assertThat(s).isNotNull();
        }

        @Test
        @DisplayName("should use default key prefix")
        void shouldUseDefaultKeyPrefix() {
            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .setter((key, value, ttl) -> {})
                .getter(key -> null)
                .deleter(key -> {})
                .build();

            assertThat(s.getKeyPrefix()).isEqualTo("captcha:");
        }

        @Test
        @DisplayName("should use custom key prefix")
        void shouldUseCustomKeyPrefix() {
            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .keyPrefix("myapp:captcha:")
                .setter((key, value, ttl) -> {})
                .getter(key -> null)
                .deleter(key -> {})
                .build();

            assertThat(s.getKeyPrefix()).isEqualTo("myapp:captcha:");
        }

        @Test
        @DisplayName("should throw when setter is null")
        void shouldThrowWhenSetterIsNull() {
            assertThatThrownBy(() ->
                RedisCaptchaStore.builder()
                    .getter(key -> null)
                    .deleter(key -> {})
                    .build()
            ).isInstanceOf(NullPointerException.class)
             .hasMessageContaining("setter");
        }

        @Test
        @DisplayName("should throw when getter is null")
        void shouldThrowWhenGetterIsNull() {
            assertThatThrownBy(() ->
                RedisCaptchaStore.builder()
                    .setter((key, value, ttl) -> {})
                    .deleter(key -> {})
                    .build()
            ).isInstanceOf(NullPointerException.class)
             .hasMessageContaining("getter");
        }

        @Test
        @DisplayName("should throw when deleter is null")
        void shouldThrowWhenDeleterIsNull() {
            assertThatThrownBy(() ->
                RedisCaptchaStore.builder()
                    .setter((key, value, ttl) -> {})
                    .getter(key -> null)
                    .build()
            ).isInstanceOf(NullPointerException.class)
             .hasMessageContaining("deleter");
        }

        @Test
        @DisplayName("should throw when keyPrefix is null")
        void shouldThrowWhenKeyPrefixIsNull() {
            assertThatThrownBy(() ->
                RedisCaptchaStore.builder()
                    .keyPrefix(null)
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should support custom exists checker")
        void shouldSupportCustomExistsChecker() {
            AtomicInteger existsCalls = new AtomicInteger(0);

            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .setter((key, value, ttl) -> mockRedis.put(key, value))
                .getter(mockRedis::get)
                .deleter(mockRedis::remove)
                .existsChecker(key -> {
                    existsCalls.incrementAndGet();
                    return mockRedis.containsKey(key);
                })
                .build();

            s.store("id-1", "answer", Duration.ofMinutes(5));
            s.exists("id-1");

            assertThat(existsCalls.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Store Tests")
    class StoreTests {

        @Test
        @DisplayName("should store value with key prefix")
        void shouldStoreValueWithKeyPrefix() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            assertThat(mockRedis.get("captcha:id-1")).isEqualTo("answer");
        }

        @Test
        @DisplayName("should pass TTL to setter")
        void shouldPassTtlToSetter() {
            Duration[] capturedTtl = new Duration[1];
            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .setter((key, value, ttl) -> capturedTtl[0] = ttl)
                .getter(key -> null)
                .deleter(key -> {})
                .build();

            s.store("id-1", "answer", Duration.ofMinutes(3));

            assertThat(capturedTtl[0]).isEqualTo(Duration.ofMinutes(3));
        }
    }

    @Nested
    @DisplayName("Get Tests")
    class GetTests {

        @Test
        @DisplayName("should retrieve stored value")
        void shouldRetrieveStoredValue() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            Optional<String> result = store.get("id-1");

            assertThat(result).isPresent().contains("answer");
        }

        @Test
        @DisplayName("should return empty for nonexistent key")
        void shouldReturnEmptyForNonexistentKey() {
            Optional<String> result = store.get("nonexistent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should not remove value on get")
        void shouldNotRemoveValueOnGet() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            store.get("id-1");
            Optional<String> result = store.get("id-1");

            assertThat(result).isPresent().contains("answer");
        }
    }

    @Nested
    @DisplayName("GetAndRemove Tests")
    class GetAndRemoveTests {

        @Test
        @DisplayName("should get and remove value")
        void shouldGetAndRemoveValue() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            Optional<String> result = store.getAndRemove("id-1");

            assertThat(result).isPresent().contains("answer");
            assertThat(mockRedis.containsKey("captcha:id-1")).isFalse();
        }

        @Test
        @DisplayName("should return empty for nonexistent key and not call deleter")
        void shouldReturnEmptyForNonexistentKeyAndNotCallDeleter() {
            AtomicInteger deleteCount = new AtomicInteger(0);
            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .setter((key, value, ttl) -> {})
                .getter(key -> null)
                .deleter(key -> deleteCount.incrementAndGet())
                .build();

            Optional<String> result = s.getAndRemove("nonexistent");

            assertThat(result).isEmpty();
            assertThat(deleteCount.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return empty on second call")
        void shouldReturnEmptyOnSecondCall() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            store.getAndRemove("id-1");
            Optional<String> result = store.getAndRemove("id-1");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should use atomic getAndRemoveCommand when provided")
        void shouldUseAtomicGetAndRemoveCommandWhenProvided() {
            Map<String, String> redis = new HashMap<>();
            AtomicInteger getterCalls = new AtomicInteger(0);
            AtomicInteger deleterCalls = new AtomicInteger(0);

            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .setter((key, value, ttl) -> redis.put(key, value))
                .getter(key -> {
                    getterCalls.incrementAndGet();
                    return redis.get(key);
                })
                .deleter(key -> {
                    deleterCalls.incrementAndGet();
                    redis.remove(key);
                })
                .getAndRemoveCommand(key -> redis.remove(key))
                .build();

            s.store("id-1", "answer", Duration.ofMinutes(5));
            Optional<String> result = s.getAndRemove("id-1");

            assertThat(result).isPresent().contains("answer");
            assertThat(redis.containsKey("captcha:id-1")).isFalse();
            // getter and deleter should NOT have been called by getAndRemove
            assertThat(getterCalls.get()).isZero();
            assertThat(deleterCalls.get()).isZero();
        }

        @Test
        @DisplayName("should return empty from atomic getAndRemoveCommand for missing key")
        void shouldReturnEmptyFromAtomicGetAndRemoveCommandForMissingKey() {
            Map<String, String> redis = new HashMap<>();

            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .setter((key, value, ttl) -> redis.put(key, value))
                .getter(redis::get)
                .deleter(redis::remove)
                .getAndRemoveCommand(key -> redis.remove(key))
                .build();

            Optional<String> result = s.getAndRemove("nonexistent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should fall back to GET+DEL when getAndRemoveCommand not provided")
        void shouldFallBackToGetDelWhenGetAndRemoveCommandNotProvided() {
            AtomicInteger getterCalls = new AtomicInteger(0);
            AtomicInteger deleterCalls = new AtomicInteger(0);

            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .setter((key, value, ttl) -> mockRedis.put(key, value))
                .getter(key -> {
                    getterCalls.incrementAndGet();
                    return mockRedis.get(key);
                })
                .deleter(key -> {
                    deleterCalls.incrementAndGet();
                    mockRedis.remove(key);
                })
                .build();

            s.store("id-1", "answer", Duration.ofMinutes(5));
            Optional<String> result = s.getAndRemove("id-1");

            assertThat(result).isPresent().contains("answer");
            // fallback uses getter + deleter
            assertThat(getterCalls.get()).isEqualTo(1);
            assertThat(deleterCalls.get()).isEqualTo(1);
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

            assertThat(mockRedis.containsKey("captcha:id-1")).isFalse();
        }

        @Test
        @DisplayName("should use key prefix when removing")
        void shouldUseKeyPrefixWhenRemoving() {
            String[] capturedKey = new String[1];
            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .keyPrefix("test:")
                .setter((key, value, ttl) -> {})
                .getter(key -> null)
                .deleter(key -> capturedKey[0] = key)
                .build();

            s.remove("id-1");

            assertThat(capturedKey[0]).isEqualTo("test:id-1");
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
        @DisplayName("should use custom exists checker when provided")
        void shouldUseCustomExistsCheckerWhenProvided() {
            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .setter((key, value, ttl) -> {})
                .getter(key -> "value")
                .deleter(key -> {})
                .existsChecker(key -> false)
                .build();

            // Even though getter returns a value, custom existsChecker says false
            assertThat(s.exists("any-id")).isFalse();
        }

        @Test
        @DisplayName("should use getter-based fallback when no exists checker")
        void shouldUseGetterBasedFallbackWhenNoExistsChecker() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            // Default exists checker uses getter
            assertThat(store.exists("id-1")).isTrue();
            assertThat(store.exists("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("ClearExpired Tests")
    class ClearExpiredTests {

        @Test
        @DisplayName("should not throw - Redis handles TTL automatically")
        void shouldNotThrow() {
            assertThatCode(() -> store.clearExpired())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("ClearAll Tests")
    class ClearAllTests {

        @Test
        @DisplayName("should be a no-op in base implementation")
        void shouldBeNoOpInBaseImplementation() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            store.clearAll();

            // Should still exist because clearAll is a no-op
            assertThat(mockRedis.containsKey("captcha:id-1")).isTrue();
        }
    }

    @Nested
    @DisplayName("Size Tests")
    class SizeTests {

        @Test
        @DisplayName("should return -1 indicating unknown size")
        void shouldReturnNegativeOneIndicatingUnknownSize() {
            assertThat(store.size()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("ID Validation Tests")
    class IdValidationTests {

        @Test
        @DisplayName("should reject null ID")
        void shouldRejectNullId() {
            assertThatThrownBy(() -> store.store(null, "answer", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("should reject blank ID")
        void shouldRejectBlankId() {
            assertThatThrownBy(() -> store.store("   ", "answer", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("should reject empty ID")
        void shouldRejectEmptyId() {
            assertThatThrownBy(() -> store.store("", "answer", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("should reject ID with control characters")
        void shouldRejectIdWithControlCharacters() {
            assertThatThrownBy(() -> store.store("id\u0000inject", "answer", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid characters");

            assertThatThrownBy(() -> store.store("id\nnewline", "answer", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid characters");

            assertThatThrownBy(() -> store.store("id\rtab", "answer", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid characters");
        }

        @Test
        @DisplayName("should reject ID with glob pattern characters")
        void shouldRejectIdWithGlobPatternCharacters() {
            assertThatThrownBy(() -> store.store("id*", "answer", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid characters");

            assertThatThrownBy(() -> store.store("id?", "answer", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid characters");

            assertThatThrownBy(() -> store.store("id[0]", "answer", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid characters");
        }

        @Test
        @DisplayName("should reject invalid IDs in all public methods")
        void shouldRejectInvalidIdsInAllPublicMethods() {
            assertThatThrownBy(() -> store.get(null))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> store.getAndRemove("id*"))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> store.remove("id\u0000"))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> store.exists(""))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should accept valid IDs")
        void shouldAcceptValidIds() {
            assertThatCode(() -> store.store("valid-id-123", "answer", Duration.ofMinutes(5)))
                .doesNotThrowAnyException();

            assertThatCode(() -> store.store("UPPER_case.with-dashes", "answer", Duration.ofMinutes(5)))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Key Prefix Tests")
    class KeyPrefixTests {

        @Test
        @DisplayName("should prepend prefix to all keys")
        void shouldPrependPrefixToAllKeys() {
            String[] storedKey = new String[1];
            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .keyPrefix("app:captcha:")
                .setter((key, value, ttl) -> storedKey[0] = key)
                .getter(key -> null)
                .deleter(key -> {})
                .build();

            s.store("my-id", "answer", Duration.ofMinutes(5));

            assertThat(storedKey[0]).isEqualTo("app:captcha:my-id");
        }

        @Test
        @DisplayName("getKeyPrefix should return configured prefix")
        void getKeyPrefixShouldReturnConfiguredPrefix() {
            RedisCaptchaStore s = RedisCaptchaStore.builder()
                .keyPrefix("custom:")
                .setter((key, value, ttl) -> {})
                .getter(key -> null)
                .deleter(key -> {})
                .build();

            assertThat(s.getKeyPrefix()).isEqualTo("custom:");
        }
    }
}
