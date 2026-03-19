package cloud.opencode.base.timeseries.aggregation;

import cloud.opencode.base.timeseries.DataPoint;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CountAggregatorTest Tests
 * CountAggregatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("CountAggregator 测试")
class CountAggregatorTest {

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("getInstance返回相同实例")
        void testGetInstanceReturnsSameInstance() {
            assertThat(CountAggregator.getInstance()).isSameAs(CountAggregator.getInstance());
        }
    }

    @Nested
    @DisplayName("aggregate方法测试")
    class AggregateTests {

        @Test
        @DisplayName("返回数据点数量")
        void testCountCalculation() {
            List<DataPoint> points = List.of(
                    DataPoint.of(Instant.now(), 10.0),
                    DataPoint.of(Instant.now(), 20.0),
                    DataPoint.of(Instant.now(), 30.0)
            );

            double result = CountAggregator.getInstance().aggregate(points);

            assertThat(result).isEqualTo(3.0);
        }

        @Test
        @DisplayName("空列表返回0")
        void testEmptyList() {
            assertThat(CountAggregator.getInstance().aggregate(List.of())).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("name方法测试")
    class NameTests {

        @Test
        @DisplayName("name返回COUNT")
        void testName() {
            assertThat(CountAggregator.getInstance().name()).isEqualTo("COUNT");
        }
    }
}
