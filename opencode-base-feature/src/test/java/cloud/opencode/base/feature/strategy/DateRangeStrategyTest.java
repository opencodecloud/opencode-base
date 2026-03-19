package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * DateRangeStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("DateRangeStrategy 测试")
class DateRangeStrategyTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用Instant创建")
        void testInstantConstructor() {
            Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
            Instant end = Instant.now().plus(1, ChronoUnit.HOURS);

            DateRangeStrategy strategy = new DateRangeStrategy(start, end);

            assertThat(strategy.getStartTime()).isEqualTo(start);
            assertThat(strategy.getEndTime()).isEqualTo(end);
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() 使用系统时区")
        void testOfWithSystemTimezone() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now().plusHours(1);

            DateRangeStrategy strategy = DateRangeStrategy.of(start, end);

            assertThat(strategy.getStartTime()).isNotNull();
            assertThat(strategy.getEndTime()).isNotNull();
        }

        @Test
        @DisplayName("of() 使用指定时区")
        void testOfWithTimezone() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now().plusHours(1);
            ZoneId zoneId = ZoneId.of("UTC");

            DateRangeStrategy strategy = DateRangeStrategy.of(start, end, zoneId);

            assertThat(strategy.getStartTime()).isNotNull();
            assertThat(strategy.getEndTime()).isNotNull();
        }

        @Test
        @DisplayName("until() 从现在到指定时间")
        void testUntil() {
            Instant end = Instant.now().plus(1, ChronoUnit.HOURS);

            DateRangeStrategy strategy = DateRangeStrategy.until(end);

            assertThat(strategy.getStartTime()).isBeforeOrEqualTo(Instant.now());
            assertThat(strategy.getEndTime()).isEqualTo(end);
        }

        @Test
        @DisplayName("from() 从指定时间到无穷大")
        void testFrom() {
            Instant start = Instant.now().minus(1, ChronoUnit.HOURS);

            DateRangeStrategy strategy = DateRangeStrategy.from(start);

            assertThat(strategy.getStartTime()).isEqualTo(start);
            assertThat(strategy.getEndTime()).isEqualTo(Instant.MAX);
        }
    }

    @Nested
    @DisplayName("isEnabled() 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("在时间范围内启用")
        void testWithinRangeEnabled() {
            Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
            Instant end = Instant.now().plus(1, ChronoUnit.HOURS);
            DateRangeStrategy strategy = new DateRangeStrategy(start, end);
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isTrue();
        }

        @Test
        @DisplayName("在开始时间之前禁用")
        void testBeforeStartDisabled() {
            Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
            Instant end = Instant.now().plus(2, ChronoUnit.HOURS);
            DateRangeStrategy strategy = new DateRangeStrategy(start, end);
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isFalse();
        }

        @Test
        @DisplayName("在结束时间之后禁用")
        void testAfterEndDisabled() {
            Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
            Instant end = Instant.now().minus(1, ChronoUnit.HOURS);
            DateRangeStrategy strategy = new DateRangeStrategy(start, end);
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isFalse();
        }

        @Test
        @DisplayName("上下文不影响结果")
        void testContextDoesNotAffect() {
            Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
            Instant end = Instant.now().plus(1, ChronoUnit.HOURS);
            DateRangeStrategy strategy = new DateRangeStrategy(start, end);
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isTrue();
            assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("user1"))).isTrue();
            assertThat(strategy.isEnabled(feature, 
                    FeatureContext.builder().tenantId("tenant1").build())).isTrue();
        }
    }

    @Nested
    @DisplayName("getStartTime() 测试")
    class GetStartTimeTests {

        @Test
        @DisplayName("获取开始时间")
        void testGetStartTime() {
            Instant start = Instant.parse("2024-01-01T00:00:00Z");
            Instant end = Instant.parse("2024-12-31T23:59:59Z");
            DateRangeStrategy strategy = new DateRangeStrategy(start, end);

            assertThat(strategy.getStartTime()).isEqualTo(start);
        }
    }

    @Nested
    @DisplayName("getEndTime() 测试")
    class GetEndTimeTests {

        @Test
        @DisplayName("获取结束时间")
        void testGetEndTime() {
            Instant start = Instant.parse("2024-01-01T00:00:00Z");
            Instant end = Instant.parse("2024-12-31T23:59:59Z");
            DateRangeStrategy strategy = new DateRangeStrategy(start, end);

            assertThat(strategy.getEndTime()).isEqualTo(end);
        }
    }

    @Nested
    @DisplayName("hasStarted() 测试")
    class HasStartedTests {

        @Test
        @DisplayName("已开始")
        void testHasStarted() {
            Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
            Instant end = Instant.now().plus(1, ChronoUnit.HOURS);
            DateRangeStrategy strategy = new DateRangeStrategy(start, end);

            assertThat(strategy.hasStarted()).isTrue();
        }

        @Test
        @DisplayName("未开始")
        void testHasNotStarted() {
            Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
            Instant end = Instant.now().plus(2, ChronoUnit.HOURS);
            DateRangeStrategy strategy = new DateRangeStrategy(start, end);

            assertThat(strategy.hasStarted()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasEnded() 测试")
    class HasEndedTests {

        @Test
        @DisplayName("已结束")
        void testHasEnded() {
            Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
            Instant end = Instant.now().minus(1, ChronoUnit.HOURS);
            DateRangeStrategy strategy = new DateRangeStrategy(start, end);

            assertThat(strategy.hasEnded()).isTrue();
        }

        @Test
        @DisplayName("未结束")
        void testHasNotEnded() {
            Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
            Instant end = Instant.now().plus(1, ChronoUnit.HOURS);
            DateRangeStrategy strategy = new DateRangeStrategy(start, end);

            assertThat(strategy.hasEnded()).isFalse();
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("包含时间范围")
        void testToString() {
            Instant start = Instant.parse("2024-01-01T00:00:00Z");
            Instant end = Instant.parse("2024-12-31T23:59:59Z");
            DateRangeStrategy strategy = new DateRangeStrategy(start, end);

            assertThat(strategy.toString())
                    .contains("DateRangeStrategy")
                    .contains("startTime")
                    .contains("endTime");
        }
    }

    @Nested
    @DisplayName("实现EnableStrategy测试")
    class ImplementsEnableStrategyTests {

        @Test
        @DisplayName("实现EnableStrategy接口")
        void testImplementsEnableStrategy() {
            DateRangeStrategy strategy = new DateRangeStrategy(Instant.now(), Instant.now());

            assertThat(strategy).isInstanceOf(EnableStrategy.class);
        }
    }
}
