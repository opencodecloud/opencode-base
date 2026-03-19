package cloud.opencode.base.captcha.security;

import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.support.CaptchaStrength;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * AntiBotStrategy Test - Unit tests for adaptive anti-bot strategy
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class AntiBotStrategyTest {

    private BehaviorAnalyzer analyzer;
    private AntiBotStrategy strategy;

    @BeforeEach
    void setUp() {
        analyzer = new BehaviorAnalyzer();
        strategy = new AntiBotStrategy(analyzer);
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create() should return a new strategy with fresh analyzer")
        void createShouldReturnNewStrategy() {
            AntiBotStrategy s = AntiBotStrategy.create();

            assertThat(s).isNotNull();
            assertThat(s.getAnalyzer()).isNotNull();
        }

        @Test
        @DisplayName("create() should return different instances each time")
        void createShouldReturnDifferentInstances() {
            AntiBotStrategy s1 = AntiBotStrategy.create();
            AntiBotStrategy s2 = AntiBotStrategy.create();

            assertThat(s1).isNotSameAs(s2);
        }
    }

    @Nested
    @DisplayName("Base Strength Configuration Tests")
    class BaseStrengthConfigurationTests {

        @Test
        @DisplayName("should use MEDIUM as default base strength")
        void shouldUseMediumAsDefaultBaseStrength() {
            // Unknown client should get the base strength
            CaptchaStrength strength = strategy.recommendStrength("unknown-client");

            assertThat(strength).isEqualTo(CaptchaStrength.MEDIUM);
        }

        @Test
        @DisplayName("should use configured base strength")
        void shouldUseConfiguredBaseStrength() {
            strategy.withBaseStrength(CaptchaStrength.EASY);

            CaptchaStrength strength = strategy.recommendStrength("unknown-client");

            assertThat(strength).isEqualTo(CaptchaStrength.EASY);
        }

        @Test
        @DisplayName("withBaseStrength should return this for chaining")
        void withBaseStrengthShouldReturnThisForChaining() {
            AntiBotStrategy result = strategy.withBaseStrength(CaptchaStrength.HARD);

            assertThat(result).isSameAs(strategy);
        }
    }

    @Nested
    @DisplayName("Base Type Configuration Tests")
    class BaseTypeConfigurationTests {

        @Test
        @DisplayName("should use ALPHANUMERIC as default base type")
        void shouldUseAlphanumericAsDefaultBaseType() {
            CaptchaType type = strategy.recommendType("unknown-client");

            assertThat(type).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("should use configured base type")
        void shouldUseConfiguredBaseType() {
            strategy.withBaseType(CaptchaType.NUMERIC);

            CaptchaType type = strategy.recommendType("unknown-client");

            assertThat(type).isEqualTo(CaptchaType.NUMERIC);
        }

        @Test
        @DisplayName("withBaseType should return this for chaining")
        void withBaseTypeShouldReturnThisForChaining() {
            AntiBotStrategy result = strategy.withBaseType(CaptchaType.ARITHMETIC);

            assertThat(result).isSameAs(strategy);
        }
    }

    @Nested
    @DisplayName("Recommend Strength Tests")
    class RecommendStrengthTests {

        @Test
        @DisplayName("should return base strength for unknown client")
        void shouldReturnBaseStrengthForUnknownClient() {
            CaptchaStrength strength = strategy.recommendStrength("new-client");

            assertThat(strength).isEqualTo(CaptchaStrength.MEDIUM);
        }

        @Test
        @DisplayName("should return HARD for client with 2 failures")
        void shouldReturnHardForClientWith2Failures() {
            analyzer.analyze("client-1", Duration.ofSeconds(2), false);
            analyzer.analyze("client-1", Duration.ofSeconds(2), false);

            CaptchaStrength strength = strategy.recommendStrength("client-1");

            assertThat(strength).isEqualTo(CaptchaStrength.HARD);
        }

        @Test
        @DisplayName("should return EXTREME for client with 4+ failures")
        void shouldReturnExtremeForClientWith4Failures() {
            for (int i = 0; i < 4; i++) {
                analyzer.analyze("client-1", Duration.ofSeconds(2), false);
            }

            CaptchaStrength strength = strategy.recommendStrength("client-1");

            assertThat(strength).isEqualTo(CaptchaStrength.EXTREME);
        }

        @Test
        @DisplayName("should return EASY for good behavior client with many successes")
        void shouldReturnEasyForGoodBehaviorClient() {
            for (int i = 0; i < 11; i++) {
                analyzer.analyze("client-1", Duration.ofSeconds(2), true);
            }

            CaptchaStrength strength = strategy.recommendStrength("client-1");

            assertThat(strength).isEqualTo(CaptchaStrength.EASY);
        }

        @Test
        @DisplayName("should return EXTREME when failure rate exceeds 50%")
        void shouldReturnExtremeWhenFailureRateExceedsHalf() {
            // 6 attempts, 4 failures (> 50%)
            for (int i = 0; i < 4; i++) {
                analyzer.analyze("client-1", Duration.ofSeconds(2), false);
            }
            for (int i = 0; i < 2; i++) {
                analyzer.analyze("client-1", Duration.ofSeconds(2), true);
            }

            CaptchaStrength strength = strategy.recommendStrength("client-1");

            assertThat(strength).isEqualTo(CaptchaStrength.EXTREME);
        }
    }

    @Nested
    @DisplayName("Recommend Type Tests")
    class RecommendTypeTests {

        @Test
        @DisplayName("should return base type for unknown client")
        void shouldReturnBaseTypeForUnknownClient() {
            CaptchaType type = strategy.recommendType("unknown");

            assertThat(type).isEqualTo(CaptchaType.ALPHANUMERIC);
        }

        @Test
        @DisplayName("should escalate to SLIDER for client with 4+ failures")
        void shouldEscalateToSliderForClientWith4Failures() {
            for (int i = 0; i < 4; i++) {
                analyzer.analyze("client-1", Duration.ofSeconds(2), false);
            }

            CaptchaType type = strategy.recommendType("client-1");

            assertThat(type).isEqualTo(CaptchaType.SLIDER);
        }

        @Test
        @DisplayName("should return base type for client with few failures")
        void shouldReturnBaseTypeForClientWithFewFailures() {
            analyzer.analyze("client-1", Duration.ofSeconds(2), false);
            analyzer.analyze("client-1", Duration.ofSeconds(2), false);

            CaptchaType type = strategy.recommendType("client-1");

            assertThat(type).isEqualTo(CaptchaType.ALPHANUMERIC);
        }
    }

    @Nested
    @DisplayName("Should Block Tests")
    class ShouldBlockTests {

        @Test
        @DisplayName("should not block unknown client")
        void shouldNotBlockUnknownClient() {
            assertThat(strategy.shouldBlock("unknown")).isFalse();
        }

        @Test
        @DisplayName("should not block client with few failures")
        void shouldNotBlockClientWithFewFailures() {
            for (int i = 0; i < 5; i++) {
                analyzer.analyze("client-1", Duration.ofSeconds(2), false);
            }

            assertThat(strategy.shouldBlock("client-1")).isFalse();
        }

        @Test
        @DisplayName("should block client with over 10 failures")
        void shouldBlockClientWithOver10Failures() {
            for (int i = 0; i < 11; i++) {
                analyzer.analyze("client-1", Duration.ofSeconds(2), false);
            }

            assertThat(strategy.shouldBlock("client-1")).isTrue();
        }

        @Test
        @DisplayName("should not block client with exactly 10 failures")
        void shouldNotBlockClientWithExactly10Failures() {
            for (int i = 0; i < 10; i++) {
                analyzer.analyze("client-1", Duration.ofSeconds(2), false);
            }

            assertThat(strategy.shouldBlock("client-1")).isFalse();
        }
    }

    @Nested
    @DisplayName("Analyzer Access Tests")
    class AnalyzerAccessTests {

        @Test
        @DisplayName("should return the analyzer")
        void shouldReturnTheAnalyzer() {
            assertThat(strategy.getAnalyzer()).isSameAs(analyzer);
        }
    }

    @Nested
    @DisplayName("Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("should support fluent configuration")
        void shouldSupportFluentConfiguration() {
            AntiBotStrategy s = new AntiBotStrategy(analyzer)
                .withBaseStrength(CaptchaStrength.HARD)
                .withBaseType(CaptchaType.ARITHMETIC);

            assertThat(s.recommendStrength("unknown")).isEqualTo(CaptchaStrength.HARD);
            assertThat(s.recommendType("unknown")).isEqualTo(CaptchaType.ARITHMETIC);
        }
    }
}
