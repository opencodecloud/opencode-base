package cloud.opencode.base.captcha.security;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * BehaviorAnalyzer Test - Unit tests for behavior analysis and bot detection
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class BehaviorAnalyzerTest {

    private BehaviorAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new BehaviorAnalyzer();
    }

    @Nested
    @DisplayName("Normal Behavior Tests")
    class NormalBehaviorTests {

        @Test
        @DisplayName("should return NORMAL for reasonable response time")
        void shouldReturnNormalForReasonableResponseTime() {
            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ofSeconds(2), true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.NORMAL);
        }

        @Test
        @DisplayName("should return NORMAL for first attempt with 500ms")
        void shouldReturnNormalForFirstAttemptWith500ms() {
            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ofMillis(500), true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.NORMAL);
        }

        @Test
        @DisplayName("should return NORMAL for varied timing")
        void shouldReturnNormalForVariedTiming() {
            analyzer.analyze("client-1", Duration.ofSeconds(2), true);
            analyzer.analyze("client-1", Duration.ofSeconds(5), true);
            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ofSeconds(3), true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.NORMAL);
        }
    }

    @Nested
    @DisplayName("Suspicious Timing Tests")
    class SuspiciousTimingTests {

        @Test
        @DisplayName("should detect suspiciously fast response under 500ms")
        void shouldDetectSuspiciouslyFastResponse() {
            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ofMillis(100), true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.SUSPICIOUS_TIMING);
        }

        @Test
        @DisplayName("should detect zero-time response")
        void shouldDetectZeroTimeResponse() {
            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ZERO, true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.SUSPICIOUS_TIMING);
        }

        @Test
        @DisplayName("should detect 1ms response as suspicious")
        void shouldDetect1msResponseAsSuspicious() {
            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ofMillis(1), true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.SUSPICIOUS_TIMING);
        }

        @Test
        @DisplayName("should detect 499ms response as suspicious")
        void shouldDetect499msResponseAsSuspicious() {
            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ofMillis(499), true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.SUSPICIOUS_TIMING);
        }
    }

    @Nested
    @DisplayName("Too Many Failures Tests")
    class TooManyFailuresTests {

        @Test
        @DisplayName("should detect too many failures exceeding threshold")
        void shouldDetectTooManyFailures() {
            // Record 6 failures (threshold is 5), use varied timings to avoid CONSISTENT_TIMING
            int[] times = {1, 3, 2, 5, 4, 6};
            for (int t : times) {
                analyzer.analyze("client-1", Duration.ofSeconds(t), false);
            }

            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ofSeconds(7), false);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.TOO_MANY_FAILURES);
        }

        @Test
        @DisplayName("should allow up to 5 failures without detection")
        void shouldAllowUpTo5FailuresWithoutDetection() {
            // Use varied timings to avoid triggering CONSISTENT_TIMING
            analyzer.analyze("client-1", Duration.ofSeconds(1), false);
            analyzer.analyze("client-1", Duration.ofSeconds(3), false);
            analyzer.analyze("client-1", Duration.ofSeconds(2), false);
            analyzer.analyze("client-1", Duration.ofSeconds(5), false);
            analyzer.analyze("client-1", Duration.ofSeconds(4), false);

            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ofSeconds(6), true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.NORMAL);
        }

        @Test
        @DisplayName("should track failures per client independently")
        void shouldTrackFailuresPerClientIndependently() {
            // Client A has many failures (use varied timings)
            int[] times = {1, 3, 2, 5, 4, 6, 7};
            for (int t : times) {
                analyzer.analyze("client-A", Duration.ofSeconds(t), false);
            }

            // Client B should still be normal
            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-B", Duration.ofSeconds(2), true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.NORMAL);
        }
    }

    @Nested
    @DisplayName("Consistent Timing Tests")
    class ConsistentTimingTests {

        @Test
        @DisplayName("should detect consistent timing after multiple similar responses")
        void shouldDetectConsistentTimingAfterMultipleSimilarResponses() {
            // Need 3+ consistent timings (within 100ms of each other)
            analyzer.analyze("client-1", Duration.ofMillis(1000), true);
            analyzer.analyze("client-1", Duration.ofMillis(1050), true);
            analyzer.analyze("client-1", Duration.ofMillis(1020), true);

            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ofMillis(1030), true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.CONSISTENT_TIMING);
        }

        @Test
        @DisplayName("should not detect consistent timing with varied responses")
        void shouldNotDetectConsistentTimingWithVariedResponses() {
            analyzer.analyze("client-1", Duration.ofMillis(1000), true);
            analyzer.analyze("client-1", Duration.ofMillis(2000), true);
            analyzer.analyze("client-1", Duration.ofMillis(1500), true);

            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ofMillis(3000), true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.NORMAL);
        }

        @Test
        @DisplayName("should reset consistent count on varied response")
        void shouldResetConsistentCountOnVariedResponse() {
            analyzer.analyze("client-1", Duration.ofMillis(1000), true);
            analyzer.analyze("client-1", Duration.ofMillis(1020), true);
            // Break the pattern
            analyzer.analyze("client-1", Duration.ofMillis(3000), true);
            // Restart
            analyzer.analyze("client-1", Duration.ofMillis(1000), true);

            BehaviorAnalyzer.AnalysisResult result = analyzer.analyze(
                "client-1", Duration.ofMillis(1020), true);

            assertThat(result).isEqualTo(BehaviorAnalyzer.AnalysisResult.NORMAL);
        }
    }

    @Nested
    @DisplayName("Client Behavior Access Tests")
    class ClientBehaviorAccessTests {

        @Test
        @DisplayName("should return null for unknown client")
        void shouldReturnNullForUnknownClient() {
            assertThat(analyzer.getBehavior("unknown")).isNull();
        }

        @Test
        @DisplayName("should return behavior for known client")
        void shouldReturnBehaviorForKnownClient() {
            analyzer.analyze("client-1", Duration.ofSeconds(2), true);

            BehaviorAnalyzer.ClientBehavior behavior = analyzer.getBehavior("client-1");

            assertThat(behavior).isNotNull();
            assertThat(behavior.getTotalAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("should track total attempts correctly")
        void shouldTrackTotalAttemptsCorrectly() {
            analyzer.analyze("client-1", Duration.ofSeconds(2), true);
            analyzer.analyze("client-1", Duration.ofSeconds(3), false);
            analyzer.analyze("client-1", Duration.ofSeconds(1), true);

            BehaviorAnalyzer.ClientBehavior behavior = analyzer.getBehavior("client-1");

            assertThat(behavior.getTotalAttempts()).isEqualTo(3);
        }

        @Test
        @DisplayName("should track failure count correctly")
        void shouldTrackFailureCountCorrectly() {
            analyzer.analyze("client-1", Duration.ofSeconds(2), true);
            analyzer.analyze("client-1", Duration.ofSeconds(3), false);
            analyzer.analyze("client-1", Duration.ofSeconds(1), false);

            BehaviorAnalyzer.ClientBehavior behavior = analyzer.getBehavior("client-1");

            assertThat(behavior.getRecentFailures()).isEqualTo(2);
        }

        @Test
        @DisplayName("should track last activity time")
        void shouldTrackLastActivityTime() {
            analyzer.analyze("client-1", Duration.ofSeconds(2), true);

            BehaviorAnalyzer.ClientBehavior behavior = analyzer.getBehavior("client-1");

            assertThat(behavior.getLastActivity()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Clear Tests")
    class ClearTests {

        @Test
        @DisplayName("should clear specific client behavior")
        void shouldClearSpecificClientBehavior() {
            analyzer.analyze("client-1", Duration.ofSeconds(2), true);

            analyzer.clear("client-1");

            assertThat(analyzer.getBehavior("client-1")).isNull();
        }

        @Test
        @DisplayName("should not affect other clients when clearing one")
        void shouldNotAffectOtherClientsWhenClearingOne() {
            analyzer.analyze("client-1", Duration.ofSeconds(2), true);
            analyzer.analyze("client-2", Duration.ofSeconds(2), true);

            analyzer.clear("client-1");

            assertThat(analyzer.getBehavior("client-1")).isNull();
            assertThat(analyzer.getBehavior("client-2")).isNotNull();
        }

        @Test
        @DisplayName("should not throw when clearing unknown client")
        void shouldNotThrowWhenClearingUnknownClient() {
            assertThatCode(() -> analyzer.clear("unknown"))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("ClearOld Tests")
    class ClearOldTests {

        @Test
        @DisplayName("should not throw on clearOld with no clients")
        void shouldNotThrowOnClearOldWithNoClients() {
            assertThatCode(() -> analyzer.clearOld())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should not remove recent clients")
        void shouldNotRemoveRecentClients() {
            analyzer.analyze("client-1", Duration.ofSeconds(2), true);

            analyzer.clearOld();

            assertThat(analyzer.getBehavior("client-1")).isNotNull();
        }
    }

    @Nested
    @DisplayName("AnalysisResult Enum Tests")
    class AnalysisResultEnumTests {

        @Test
        @DisplayName("should have all expected values")
        void shouldHaveAllExpectedValues() {
            BehaviorAnalyzer.AnalysisResult[] values = BehaviorAnalyzer.AnalysisResult.values();

            assertThat(values).containsExactly(
                BehaviorAnalyzer.AnalysisResult.NORMAL,
                BehaviorAnalyzer.AnalysisResult.SUSPICIOUS_TIMING,
                BehaviorAnalyzer.AnalysisResult.TOO_MANY_FAILURES,
                BehaviorAnalyzer.AnalysisResult.CONSISTENT_TIMING
            );
        }

        @Test
        @DisplayName("valueOf should work for all values")
        void valueOfShouldWorkForAllValues() {
            assertThat(BehaviorAnalyzer.AnalysisResult.valueOf("NORMAL"))
                .isEqualTo(BehaviorAnalyzer.AnalysisResult.NORMAL);
            assertThat(BehaviorAnalyzer.AnalysisResult.valueOf("SUSPICIOUS_TIMING"))
                .isEqualTo(BehaviorAnalyzer.AnalysisResult.SUSPICIOUS_TIMING);
            assertThat(BehaviorAnalyzer.AnalysisResult.valueOf("TOO_MANY_FAILURES"))
                .isEqualTo(BehaviorAnalyzer.AnalysisResult.TOO_MANY_FAILURES);
            assertThat(BehaviorAnalyzer.AnalysisResult.valueOf("CONSISTENT_TIMING"))
                .isEqualTo(BehaviorAnalyzer.AnalysisResult.CONSISTENT_TIMING);
        }
    }
}
