package cloud.opencode.base.timeseries.aggregation;

import cloud.opencode.base.timeseries.DataPoint;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * MaxAggregatorTest Tests
 * MaxAggregatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("MaxAggregator 测试")
class MaxAggregatorTest {

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("getInstance返回相同实例")
        void testGetInstanceReturnsSameInstance() {
            assertThat(MaxAggregator.getInstance()).isSameAs(MaxAggregator.getInstance());
        }
    }

    @Nested
    @DisplayName("aggregate方法测试")
    class AggregateTests {

        @Test
        @DisplayName("返回最大值")
        void testMaxCalculation() {
            List<DataPoint> points = List.of(
                    DataPoint.of(Instant.now(), 10.0),
                    DataPoint.of(Instant.now(), 50.0),
                    DataPoint.of(Instant.now(), 20.0)
            );

            double result = MaxAggregator.getInstance().aggregate(points);

            assertThat(result).isEqualTo(50.0);
        }

        @Test
        @DisplayName("空列表返回MIN_VALUE")
        void testEmptyList() {
            double result = MaxAggregator.getInstance().aggregate(List.of());
            // Implementation returns Double.MIN_VALUE for empty
            assertThat(result).isLessThanOrEqualTo(Double.MIN_VALUE);
        }
    }

    @Nested
    @DisplayName("name方法测试")
    class NameTests {

        @Test
        @DisplayName("name返回MAX")
        void testName() {
            assertThat(MaxAggregator.getInstance().name()).isEqualTo("MAX");
        }
    }
}
