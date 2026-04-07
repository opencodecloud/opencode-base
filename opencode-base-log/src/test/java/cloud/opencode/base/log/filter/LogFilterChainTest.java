package cloud.opencode.base.log.filter;

import cloud.opencode.base.log.CallerInfo;
import cloud.opencode.base.log.LogEvent;
import cloud.opencode.base.log.LogLevel;
import cloud.opencode.base.log.marker.Marker;
import cloud.opencode.base.log.marker.Markers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * LogFilterChain 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
@DisplayName("LogFilterChain 测试")
class LogFilterChainTest {

    private LogFilterChain chain;

    @BeforeEach
    void setUp() {
        chain = new LogFilterChain();
    }

    private static LogEvent createEvent(LogLevel level, String message) {
        return LogEvent.builder(level, message)
                .loggerName("test.Logger")
                .threadName("test-thread")
                .build();
    }

    private static LogEvent createEventWithMarker(LogLevel level, String message, Marker marker) {
        return LogEvent.builder(level, message)
                .loggerName("test.Logger")
                .marker(marker)
                .threadName("test-thread")
                .build();
    }

    @Nested
    @DisplayName("空链测试")
    class EmptyChainTests {

        @Test
        @DisplayName("空链返回 NEUTRAL")
        void testEmptyChainReturnsNeutral() {
            FilterAction result = chain.apply(createEvent(LogLevel.INFO, "msg"));
            assertThat(result).isEqualTo(FilterAction.NEUTRAL);
        }
    }

    @Nested
    @DisplayName("短路测试")
    class ShortCircuitTests {

        @Test
        @DisplayName("DENY 立即短路")
        void testDenyShortCircuits() {
            chain.addFilter(event -> FilterAction.DENY);
            chain.addFilter(event -> FilterAction.ACCEPT);

            FilterAction result = chain.apply(createEvent(LogLevel.INFO, "msg"));
            assertThat(result).isEqualTo(FilterAction.DENY);
        }

        @Test
        @DisplayName("ACCEPT 立即短路")
        void testAcceptShortCircuits() {
            chain.addFilter(event -> FilterAction.ACCEPT);
            chain.addFilter(event -> FilterAction.DENY);

            FilterAction result = chain.apply(createEvent(LogLevel.INFO, "msg"));
            assertThat(result).isEqualTo(FilterAction.ACCEPT);
        }

        @Test
        @DisplayName("NEUTRAL 继续到下一个过滤器")
        void testNeutralContinuesToNext() {
            chain.addFilter(event -> FilterAction.NEUTRAL);
            chain.addFilter(event -> FilterAction.ACCEPT);

            FilterAction result = chain.apply(createEvent(LogLevel.INFO, "msg"));
            assertThat(result).isEqualTo(FilterAction.ACCEPT);
        }

        @Test
        @DisplayName("全部 NEUTRAL 返回 NEUTRAL")
        void testAllNeutralReturnsNeutral() {
            chain.addFilter(event -> FilterAction.NEUTRAL);
            chain.addFilter(event -> FilterAction.NEUTRAL);

            FilterAction result = chain.apply(createEvent(LogLevel.INFO, "msg"));
            assertThat(result).isEqualTo(FilterAction.NEUTRAL);
        }
    }

    @Nested
    @DisplayName("排序测试")
    class OrderingTests {

        @Test
        @DisplayName("过滤器按 getOrder 排序")
        void testFiltersOrderedByGetOrder() {
            // Add higher-order first, then lower-order
            chain.addFilter(new LogFilter() {
                @Override
                public FilterAction filter(LogEvent event) {
                    return FilterAction.ACCEPT;
                }

                @Override
                public int getOrder() {
                    return 10;
                }
            });
            chain.addFilter(new LogFilter() {
                @Override
                public FilterAction filter(LogEvent event) {
                    return FilterAction.DENY;
                }

                @Override
                public int getOrder() {
                    return -5;
                }
            });

            // Lower order (-5) should execute first → DENY
            FilterAction result = chain.apply(createEvent(LogLevel.INFO, "msg"));
            assertThat(result).isEqualTo(FilterAction.DENY);
        }
    }

    @Nested
    @DisplayName("过滤器管理测试")
    class ManagementTests {

        @Test
        @DisplayName("removeFilter 移除过滤器")
        void testRemoveFilter() {
            LogFilter denyFilter = event -> FilterAction.DENY;
            chain.addFilter(denyFilter);
            chain.removeFilter(denyFilter);

            FilterAction result = chain.apply(createEvent(LogLevel.INFO, "msg"));
            assertThat(result).isEqualTo(FilterAction.NEUTRAL);
        }

        @Test
        @DisplayName("clear 清除所有过滤器")
        void testClear() {
            chain.addFilter(event -> FilterAction.DENY);
            chain.addFilter(event -> FilterAction.ACCEPT);
            chain.clear();

            assertThat(chain.getFilters()).isEmpty();
            FilterAction result = chain.apply(createEvent(LogLevel.INFO, "msg"));
            assertThat(result).isEqualTo(FilterAction.NEUTRAL);
        }

        @Test
        @DisplayName("getFilters 返回不可修改列表")
        void testGetFiltersUnmodifiable() {
            chain.addFilter(event -> FilterAction.NEUTRAL);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> chain.getFilters().add(event -> FilterAction.DENY));
        }
    }

    @Nested
    @DisplayName("LevelFilter 测试")
    class LevelFilterTests {

        @Test
        @DisplayName("低于阈值返回 DENY")
        void testBelowThresholdDenied() {
            LevelFilter filter = new LevelFilter(LogLevel.WARN);

            assertThat(filter.filter(createEvent(LogLevel.DEBUG, "msg")))
                    .isEqualTo(FilterAction.DENY);
            assertThat(filter.filter(createEvent(LogLevel.INFO, "msg")))
                    .isEqualTo(FilterAction.DENY);
            assertThat(filter.filter(createEvent(LogLevel.TRACE, "msg")))
                    .isEqualTo(FilterAction.DENY);
        }

        @Test
        @DisplayName("达到或超过阈值返回 NEUTRAL")
        void testAtOrAboveThresholdNeutral() {
            LevelFilter filter = new LevelFilter(LogLevel.WARN);

            assertThat(filter.filter(createEvent(LogLevel.WARN, "msg")))
                    .isEqualTo(FilterAction.NEUTRAL);
            assertThat(filter.filter(createEvent(LogLevel.ERROR, "msg")))
                    .isEqualTo(FilterAction.NEUTRAL);
        }

        @Test
        @DisplayName("null 阈值抛出异常")
        void testNullThresholdThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new LevelFilter(null));
        }
    }

    @Nested
    @DisplayName("MarkerFilter 测试")
    class MarkerFilterTests {

        @Test
        @DisplayName("标记匹配时返回 onMatch")
        void testMarkerMatchReturnsOnMatch() {
            MarkerFilter filter = new MarkerFilter("SECURITY",
                    FilterAction.ACCEPT, FilterAction.DENY);
            Marker secMarker = Markers.getMarker("SECURITY");

            LogEvent event = createEventWithMarker(LogLevel.INFO, "msg", secMarker);
            assertThat(filter.filter(event)).isEqualTo(FilterAction.ACCEPT);
        }

        @Test
        @DisplayName("标记不匹配时返回 onMismatch")
        void testMarkerMismatchReturnsOnMismatch() {
            MarkerFilter filter = new MarkerFilter("SECURITY",
                    FilterAction.ACCEPT, FilterAction.DENY);

            LogEvent event = createEvent(LogLevel.INFO, "msg");
            assertThat(filter.filter(event)).isEqualTo(FilterAction.DENY);
        }

        @Test
        @DisplayName("不同标记名不匹配")
        void testDifferentMarkerNameMismatch() {
            MarkerFilter filter = new MarkerFilter("SECURITY",
                    FilterAction.ACCEPT, FilterAction.NEUTRAL);
            Marker other = Markers.getMarker("AUDIT");

            LogEvent event = createEventWithMarker(LogLevel.INFO, "msg", other);
            assertThat(filter.filter(event)).isEqualTo(FilterAction.NEUTRAL);
        }

        @Test
        @DisplayName("null 参数抛出异常")
        void testNullArgumentsThrow() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new MarkerFilter(null, FilterAction.ACCEPT, FilterAction.DENY));
            assertThatNullPointerException()
                    .isThrownBy(() -> new MarkerFilter("X", null, FilterAction.DENY));
            assertThatNullPointerException()
                    .isThrownBy(() -> new MarkerFilter("X", FilterAction.ACCEPT, null));
        }
    }

    @Nested
    @DisplayName("ThrottleFilter 测试")
    class ThrottleFilterTests {

        @Test
        @DisplayName("首次消息通过")
        void testFirstMessageAllowed() {
            ThrottleFilter filter = new ThrottleFilter(Duration.ofSeconds(5));

            FilterAction result = filter.filter(createEvent(LogLevel.INFO, "first"));
            assertThat(result).isEqualTo(FilterAction.NEUTRAL);
        }

        @Test
        @DisplayName("间隔内重复消息被拒绝")
        void testDuplicateWithinIntervalDenied() {
            ThrottleFilter filter = new ThrottleFilter(Duration.ofSeconds(5));

            filter.filter(createEvent(LogLevel.INFO, "repeated"));
            FilterAction result = filter.filter(createEvent(LogLevel.INFO, "repeated"));
            assertThat(result).isEqualTo(FilterAction.DENY);
        }

        @Test
        @DisplayName("不同消息不受影响")
        void testDifferentMessageNotThrottled() {
            ThrottleFilter filter = new ThrottleFilter(Duration.ofSeconds(5));

            filter.filter(createEvent(LogLevel.INFO, "msg-a"));
            FilterAction result = filter.filter(createEvent(LogLevel.INFO, "msg-b"));
            assertThat(result).isEqualTo(FilterAction.NEUTRAL);
        }

        @Test
        @DisplayName("间隔后消息允许通过")
        void testAllowedAfterInterval() throws InterruptedException {
            ThrottleFilter filter = new ThrottleFilter(Duration.ofMillis(50));

            filter.filter(createEvent(LogLevel.INFO, "interval-test"));
            Thread.sleep(80);

            FilterAction result = filter.filter(createEvent(LogLevel.INFO, "interval-test"));
            assertThat(result).isEqualTo(FilterAction.NEUTRAL);
        }

        @Test
        @DisplayName("clearCache 清除节流缓存")
        void testClearCache() {
            ThrottleFilter filter = new ThrottleFilter(Duration.ofSeconds(60));

            filter.filter(createEvent(LogLevel.INFO, "cached"));
            filter.clearCache();

            FilterAction result = filter.filter(createEvent(LogLevel.INFO, "cached"));
            assertThat(result).isEqualTo(FilterAction.NEUTRAL);
        }

        @Test
        @DisplayName("null 间隔抛出异常")
        void testNullIntervalThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ThrottleFilter(null));
        }

        @Test
        @DisplayName("零或负间隔抛出异常")
        void testZeroOrNegativeIntervalThrows() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ThrottleFilter(Duration.ZERO));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ThrottleFilter(Duration.ofSeconds(-1)));
        }
    }
}
