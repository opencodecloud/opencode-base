package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaConfig;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaDifficultyAdapter Test - Unit tests for adaptive difficulty adjustment
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaDifficultyAdapterTest {

    private CaptchaDifficultyAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CaptchaDifficultyAdapter();
    }

    @Nested
    @DisplayName("GetStrength Tests")
    class GetStrengthTests {

        @Test
        @DisplayName("should return EASY for unknown client")
        void shouldReturnEasyForUnknownClient() {
            CaptchaStrength strength = adapter.getStrength("unknown");

            assertThat(strength).isEqualTo(CaptchaStrength.EASY);
        }

        @Test
        @DisplayName("should return EASY after 0 failures")
        void shouldReturnEasyAfter0Failures() {
            adapter.recordAttempt("client-1", true);

            CaptchaStrength strength = adapter.getStrength("client-1");

            assertThat(strength).isEqualTo(CaptchaStrength.EASY);
        }

        @Test
        @DisplayName("should return MEDIUM after 2 consecutive failures")
        void shouldReturnMediumAfter2ConsecutiveFailures() {
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", false);

            CaptchaStrength strength = adapter.getStrength("client-1");

            assertThat(strength).isEqualTo(CaptchaStrength.MEDIUM);
        }

        @Test
        @DisplayName("should return HARD after 4 consecutive failures")
        void shouldReturnHardAfter4ConsecutiveFailures() {
            for (int i = 0; i < 4; i++) {
                adapter.recordAttempt("client-1", false);
            }

            CaptchaStrength strength = adapter.getStrength("client-1");

            assertThat(strength).isEqualTo(CaptchaStrength.HARD);
        }

        @Test
        @DisplayName("should return EXTREME after 6 consecutive failures")
        void shouldReturnExtremeAfter6ConsecutiveFailures() {
            for (int i = 0; i < 6; i++) {
                adapter.recordAttempt("client-1", false);
            }

            CaptchaStrength strength = adapter.getStrength("client-1");

            assertThat(strength).isEqualTo(CaptchaStrength.EXTREME);
        }

        @Test
        @DisplayName("should reset to EASY after success")
        void shouldResetToEasyAfterSuccess() {
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", true);

            CaptchaStrength strength = adapter.getStrength("client-1");

            assertThat(strength).isEqualTo(CaptchaStrength.EASY);
        }
    }

    @Nested
    @DisplayName("GetConfig Tests")
    class GetConfigTests {

        @Test
        @DisplayName("should return config for client strength")
        void shouldReturnConfigForClientStrength() {
            CaptchaConfig config = adapter.getConfig("unknown");

            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("should return config with base config overrides")
        void shouldReturnConfigWithBaseConfigOverrides() {
            CaptchaConfig baseConfig = CaptchaConfig.builder()
                .width(200)
                .height(80)
                .build();

            CaptchaConfig config = adapter.getConfig("unknown", baseConfig);

            assertThat(config).isNotNull();
            assertThat(config.getWidth()).isEqualTo(200);
            assertThat(config.getHeight()).isEqualTo(80);
        }

        @Test
        @DisplayName("should adjust noise based on strength")
        void shouldAdjustNoiseBasedOnStrength() {
            // Build up failures to get EXTREME
            for (int i = 0; i < 6; i++) {
                adapter.recordAttempt("client-1", false);
            }

            CaptchaConfig extremeConfig = adapter.getConfig("client-1");
            CaptchaConfig easyConfig = adapter.getConfig("new-client");

            assertThat(extremeConfig.getNoiseLines()).isGreaterThan(easyConfig.getNoiseLines());
            assertThat(extremeConfig.getNoiseDots()).isGreaterThan(easyConfig.getNoiseDots());
        }
    }

    @Nested
    @DisplayName("RecordAttempt Tests")
    class RecordAttemptTests {

        @Test
        @DisplayName("should increment failure count on failure")
        void shouldIncrementFailureCountOnFailure() {
            adapter.recordAttempt("client-1", false);

            assertThat(adapter.getFailureCount("client-1")).isEqualTo(1);
        }

        @Test
        @DisplayName("should reset failure count on success")
        void shouldResetFailureCountOnSuccess() {
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", true);

            assertThat(adapter.getFailureCount("client-1")).isEqualTo(0);
        }

        @Test
        @DisplayName("should track multiple failures")
        void shouldTrackMultipleFailures() {
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", false);

            assertThat(adapter.getFailureCount("client-1")).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {

        @Test
        @DisplayName("should reset client difficulty")
        void shouldResetClientDifficulty() {
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", false);

            adapter.reset("client-1");

            assertThat(adapter.getStrength("client-1")).isEqualTo(CaptchaStrength.EASY);
            assertThat(adapter.getFailureCount("client-1")).isEqualTo(0);
        }

        @Test
        @DisplayName("should not throw when resetting unknown client")
        void shouldNotThrowWhenResettingUnknownClient() {
            assertThatCode(() -> adapter.reset("unknown"))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Global Failure Rate Tests")
    class GlobalFailureRateTests {

        @Test
        @DisplayName("should return 0 when no attempts")
        void shouldReturnZeroWhenNoAttempts() {
            assertThat(adapter.getGlobalFailureRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should return 1.0 when all failures")
        void shouldReturn1WhenAllFailures() {
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-2", false);

            assertThat(adapter.getGlobalFailureRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should return 0.0 when all successes")
        void shouldReturn0WhenAllSuccesses() {
            adapter.recordAttempt("client-1", true);
            adapter.recordAttempt("client-2", true);

            assertThat(adapter.getGlobalFailureRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should return correct rate for mixed results")
        void shouldReturnCorrectRateForMixedResults() {
            adapter.recordAttempt("client-1", true);
            adapter.recordAttempt("client-1", false);

            assertThat(adapter.getGlobalFailureRate()).isEqualTo(0.5);
        }
    }

    @Nested
    @DisplayName("Tracked Client Count Tests")
    class TrackedClientCountTests {

        @Test
        @DisplayName("should return 0 when no clients tracked")
        void shouldReturnZeroWhenNoClientsTracked() {
            assertThat(adapter.getTrackedClientCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("should count tracked clients")
        void shouldCountTrackedClients() {
            adapter.recordAttempt("client-1", true);
            adapter.recordAttempt("client-2", false);
            adapter.recordAttempt("client-3", true);

            assertThat(adapter.getTrackedClientCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("should not double-count same client")
        void shouldNotDoubleCountSameClient() {
            adapter.recordAttempt("client-1", true);
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", true);

            assertThat(adapter.getTrackedClientCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("ClearAll Tests")
    class ClearAllTests {

        @Test
        @DisplayName("should clear all client records")
        void shouldClearAllClientRecords() {
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-2", false);

            adapter.clearAll();

            assertThat(adapter.getTrackedClientCount()).isEqualTo(0);
            assertThat(adapter.getFailureCount("client-1")).isEqualTo(0);
        }

        @Test
        @DisplayName("should not throw on empty adapter")
        void shouldNotThrowOnEmptyAdapter() {
            assertThatCode(() -> adapter.clearAll())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Failure Count Tests")
    class FailureCountTests {

        @Test
        @DisplayName("should return 0 for unknown client")
        void shouldReturnZeroForUnknownClient() {
            assertThat(adapter.getFailureCount("unknown")).isEqualTo(0);
        }

        @Test
        @DisplayName("should return correct failure count")
        void shouldReturnCorrectFailureCount() {
            adapter.recordAttempt("client-1", false);
            adapter.recordAttempt("client-1", false);

            assertThat(adapter.getFailureCount("client-1")).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Per-Client Independence Tests")
    class PerClientIndependenceTests {

        @Test
        @DisplayName("should track clients independently")
        void shouldTrackClientsIndependently() {
            adapter.recordAttempt("client-A", false);
            adapter.recordAttempt("client-A", false);
            adapter.recordAttempt("client-A", false);

            adapter.recordAttempt("client-B", true);

            assertThat(adapter.getStrength("client-A")).isNotEqualTo(CaptchaStrength.EASY);
            assertThat(adapter.getStrength("client-B")).isEqualTo(CaptchaStrength.EASY);
        }
    }
}
