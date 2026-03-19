package cloud.opencode.base.timeseries.aggregation;

import cloud.opencode.base.timeseries.DataPoint;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * AvgAggregatorTest Tests
 * AvgAggregatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("AvgAggregator 测试")
class AvgAggregatorTest {

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("getInstance返回相同实例")
        void testGetInstanceReturnsSameInstance() {
            AvgAggregator a = AvgAggregator.getInstance();
            AvgAggregator b = AvgAggregator.getInstance();

            assertThat(a).isSameAs(b);
        }
    }

    @Nested
    @DisplayName("aggregate方法测试")
    class AggregateTests {

        @Test
        @DisplayName("计算平均值")
        void testAverageCalculation() {
            List<DataPoint> points = List.of(
                    DataPoint.of(Instant.now(), 10.0),
                    DataPoint.of(Instant.now(), 20.0),
                    DataPoint.of(Instant.now(), 30.0)
            );

            double result = AvgAggregator.getInstance().aggregate(points);

            assertThat(result).isEqualTo(20.0);
        }

        @Test
        @DisplayName("单个数据点返回其值")
        void testSinglePoint() {
            List<DataPoint> points = List.of(DataPoint.of(Instant.now(), 42.0));

            double result = AvgAggregator.getInstance().aggregate(points);

            assertThat(result).isEqualTo(42.0);
        }

        @Test
        @DisplayName("空列表返回0")
        void testEmptyList() {
            List<DataPoint> points = List.of();

            double result = AvgAggregator.getInstance().aggregate(points);

            assertThat(result).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("name方法测试")
    class NameTests {

        @Test
        @DisplayName("name返回AVG")
        void testName() {
            assertThat(AvgAggregator.getInstance().name()).isEqualTo("AVG");
        }
    }
}
