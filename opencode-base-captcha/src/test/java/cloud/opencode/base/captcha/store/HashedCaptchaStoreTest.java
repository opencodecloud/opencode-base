package cloud.opencode.base.captcha.store;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * HashedCaptchaStore Test - Unit tests for hashed CAPTCHA storage decorator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
class HashedCaptchaStoreTest {

    private MemoryCaptchaStore delegate;
    private HashedCaptchaStore store;

    @BeforeEach
    void setUp() {
        delegate = new MemoryCaptchaStore();
        store = HashedCaptchaStore.wrap(delegate);
    }

    @AfterEach
    void tearDown() {
        delegate.shutdown();
    }

    @Nested
    @DisplayName("Factory Tests | 工厂方法测试")
    class FactoryTests {

        @Test
        @DisplayName("should reject null delegate")
        void shouldRejectNullDelegate() {
            assertThatNullPointerException()
                .isThrownBy(() -> HashedCaptchaStore.wrap(null));
        }

        @Test
        @DisplayName("should create case-sensitive store")
        void shouldCreateCaseSensitiveStore() {
            HashedCaptchaStore sensitive = HashedCaptchaStore.wrap(delegate, true);
            assertThat(sensitive).isNotNull();
        }
    }

    @Nested
    @DisplayName("Store and Hash Tests | 存储和哈希测试")
    class StoreAndHashTests {

        @Test
        @DisplayName("should hash the answer before storing")
        void shouldHashAnswerBeforeStoring() {
            store.store("id-1", "answer123", Duration.ofMinutes(5));

            // The stored value in the delegate should NOT be the plaintext answer
            Optional<String> storedValue = delegate.get("id-1");
            assertThat(storedValue).isPresent();
            assertThat(storedValue.get()).isNotEqualTo("answer123");
            // Should contain the salt:hash separator
            assertThat(storedValue.get()).contains(":");
        }

        @Test
        @DisplayName("should produce different hashes for same answer (random salt)")
        void shouldProduceDifferentHashesForSameAnswer() {
            store.store("id-1", "same-answer", Duration.ofMinutes(5));
            String hash1 = delegate.get("id-1").orElseThrow();

            store.store("id-2", "same-answer", Duration.ofMinutes(5));
            String hash2 = delegate.get("id-2").orElseThrow();

            // Random salt means different stored values even for same plaintext
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("should reject null parameters on store")
        void shouldRejectNullParametersOnStore() {
            assertThatNullPointerException()
                .isThrownBy(() -> store.store(null, "answer", Duration.ofMinutes(5)));
            assertThatNullPointerException()
                .isThrownBy(() -> store.store("id", null, Duration.ofMinutes(5)));
            assertThatNullPointerException()
                .isThrownBy(() -> store.store("id", "answer", null));
        }
    }

    @Nested
    @DisplayName("Verify Answer Tests | 验证答案测试")
    class VerifyAnswerTests {

        @Test
        @DisplayName("should verify correct answer")
        void shouldVerifyCorrectAnswer() {
            store.store("id-1", "abc123", Duration.ofMinutes(5));

            assertThat(store.verifyAnswer("id-1", "abc123")).isTrue();
        }

        @Test
        @DisplayName("should reject wrong answer")
        void shouldRejectWrongAnswer() {
            store.store("id-1", "abc123", Duration.ofMinutes(5));

            assertThat(store.verifyAnswer("id-1", "wrong")).isFalse();
        }

        @Test
        @DisplayName("should return false for non-existent id")
        void shouldReturnFalseForNonExistentId() {
            assertThat(store.verifyAnswer("no-such-id", "answer")).isFalse();
        }

        @Test
        @DisplayName("should not remove entry after verifyAnswer")
        void shouldNotRemoveEntryAfterVerifyAnswer() {
            store.store("id-1", "abc123", Duration.ofMinutes(5));

            store.verifyAnswer("id-1", "abc123");

            // Entry should still exist
            assertThat(store.exists("id-1")).isTrue();
        }

        @Test
        @DisplayName("should be case-insensitive by default")
        void shouldBeCaseInsensitiveByDefault() {
            store.store("id-1", "AbCdEf", Duration.ofMinutes(5));

            assertThat(store.verifyAnswer("id-1", "abcdef")).isTrue();
            assertThat(store.verifyAnswer("id-1", "ABCDEF")).isTrue();
        }

        @Test
        @DisplayName("should respect case sensitivity when configured")
        void shouldRespectCaseSensitivityWhenConfigured() {
            HashedCaptchaStore sensitive = HashedCaptchaStore.wrap(delegate, true);
            sensitive.store("id-1", "AbCdEf", Duration.ofMinutes(5));

            assertThat(sensitive.verifyAnswer("id-1", "AbCdEf")).isTrue();
            assertThat(sensitive.verifyAnswer("id-1", "abcdef")).isFalse();
        }

        @Test
        @DisplayName("should reject null parameters on verifyAnswer")
        void shouldRejectNullParametersOnVerifyAnswer() {
            assertThatNullPointerException()
                .isThrownBy(() -> store.verifyAnswer(null, "answer"));
            assertThatNullPointerException()
                .isThrownBy(() -> store.verifyAnswer("id", null));
        }
    }

    @Nested
    @DisplayName("Verify And Remove Tests | 验证并删除测试")
    class VerifyAndRemoveTests {

        @Test
        @DisplayName("should verify and remove on correct answer")
        void shouldVerifyAndRemoveOnCorrectAnswer() {
            store.store("id-1", "abc123", Duration.ofMinutes(5));

            boolean result = store.verifyAndRemove("id-1", "abc123");

            assertThat(result).isTrue();
            assertThat(store.exists("id-1")).isFalse();
        }

        @Test
        @DisplayName("should remove entry even on wrong answer")
        void shouldRemoveEntryEvenOnWrongAnswer() {
            store.store("id-1", "abc123", Duration.ofMinutes(5));

            boolean result = store.verifyAndRemove("id-1", "wrong");

            assertThat(result).isFalse();
            // Entry consumed by getAndRemove regardless
            assertThat(store.exists("id-1")).isFalse();
        }

        @Test
        @DisplayName("should return false for non-existent id")
        void shouldReturnFalseForNonExistentId() {
            assertThat(store.verifyAndRemove("no-such-id", "answer")).isFalse();
        }

        @Test
        @DisplayName("should reject null parameters on verifyAndRemove")
        void shouldRejectNullParametersOnVerifyAndRemove() {
            assertThatNullPointerException()
                .isThrownBy(() -> store.verifyAndRemove(null, "answer"));
            assertThatNullPointerException()
                .isThrownBy(() -> store.verifyAndRemove("id", null));
        }
    }

    @Nested
    @DisplayName("Delegate Pass-Through Tests | 委托透传测试")
    class DelegatePassThroughTests {

        @Test
        @DisplayName("should delegate exists")
        void shouldDelegateExists() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            assertThat(store.exists("id-1")).isTrue();
            assertThat(store.exists("no-such-id")).isFalse();
        }

        @Test
        @DisplayName("should delegate remove")
        void shouldDelegateRemove() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            store.remove("id-1");

            assertThat(store.exists("id-1")).isFalse();
        }

        @Test
        @DisplayName("should delegate size")
        void shouldDelegateSize() {
            assertThat(store.size()).isZero();

            store.store("id-1", "a", Duration.ofMinutes(5));
            store.store("id-2", "b", Duration.ofMinutes(5));

            assertThat(store.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("should delegate clearAll")
        void shouldDelegateClearAll() {
            store.store("id-1", "a", Duration.ofMinutes(5));
            store.store("id-2", "b", Duration.ofMinutes(5));

            store.clearAll();

            assertThat(store.size()).isZero();
        }

        @Test
        @DisplayName("should delegate get returning hash")
        void shouldDelegateGetReturningHash() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            Optional<String> result = store.get("id-1");

            assertThat(result).isPresent();
            // The returned value is a hash, not the plaintext
            assertThat(result.get()).isNotEqualTo("answer");
        }

        @Test
        @DisplayName("should delegate getAndRemove")
        void shouldDelegateGetAndRemove() {
            store.store("id-1", "answer", Duration.ofMinutes(5));

            Optional<String> result = store.getAndRemove("id-1");

            assertThat(result).isPresent();
            assertThat(result.get()).isNotEqualTo("answer");
            assertThat(store.exists("id-1")).isFalse();
        }
    }
}
