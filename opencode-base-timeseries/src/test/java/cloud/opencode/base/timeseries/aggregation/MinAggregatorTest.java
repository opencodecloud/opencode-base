package cloud.opencode.base.timeseries.aggregation;

import cloud.opencode.base.timeseries.DataPoint;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * MinAggregatorTest Tests
 * MinAggregatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("MinAggregator 测试")
class MinAggregatorTest {

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("getInstance返回相同实例")
        void testGetInstanceReturnsSameInstance() {
            assertThat(MinAggregator.getInstance()).isSameAs(MinAggregator.getInstance());
        }
    }

    @Nested
    @DisplayName("aggregate方法测试")
    class AggregateTests {

        @Test
        @DisplayName("返回最小值")
        void testMinCalculation() {
            List<DataPoint> points = List.of(
                    DataPoint.of(Instant.now(), 10.0),
                    DataPoint.of(Instant.now(), 5.0),
                    DataPoint.of(Instant.now(), 20.0)
            );

            double result = MinAggregator.getInstance().aggregate(points);

            assertThat(result).isEqualTo(5.0);
        }

        @Test
        @DisplayName("空列表返回MAX_VALUE")
        void testEmptyList() {
            assertThat(MinAggregator.getInstance().aggregate(List.of())).isEqualTo(Double.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("name方法测试")
    class NameTests {

        @Test
        @DisplayName("name返回MIN")
        void testName() {
            assertThat(MinAggregator.getInstance().name()).isEqualTo("MIN");
        }
    }
}
