package cloud.opencode.base.geo.distance;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * HaversineCalculator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("HaversineCalculator 测试")
class HaversineCalculatorTest {

    private final HaversineCalculator calculator = HaversineCalculator.INSTANCE;

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("INSTANCE不为null")
        void testInstanceNotNull() {
            assertThat(HaversineCalculator.INSTANCE).isNotNull();
        }

        @Test
        @DisplayName("INSTANCE是同一实例")
        void testSameInstance() {
            assertThat(HaversineCalculator.INSTANCE).isSameAs(calculator);
        }
    }

    @Nested
    @DisplayName("calculate()测试")
    class CalculateTests {

        @Test
        @DisplayName("计算两点距离")
        void testCalculateDistance() {
            Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate shanghai = Coordinate.wgs84(121.4737, 31.2304);

            double distance = calculator.calculate(beijing, shanghai);

            // 北京到上海约1068公里
            assertThat(distance).isGreaterThan(1000000);
            assertThat(distance).isLessThan(1200000);
        }

        @Test
        @DisplayName("相同点距离为0")
        void testSamePointDistance() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);

            double distance = calculator.calculate(coord, coord);

            assertThat(distance).isEqualTo(0);
        }

        @Test
        @DisplayName("短距离计算准确")
        void testShortDistance() {
            Coordinate c1 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate c2 = Coordinate.wgs84(116.4084, 39.9042); // 约100米

            double distance = calculator.calculate(c1, c2);

            assertThat(distance).isGreaterThan(50);
            assertThat(distance).isLessThan(150);
        }

        @Test
        @DisplayName("经度差异计算")
        void testLongitudeDifference() {
            Coordinate c1 = Coordinate.wgs84(0, 0);
            Coordinate c2 = Coordinate.wgs84(1, 0);

            double distance = calculator.calculate(c1, c2);

            // 赤道上1度约111公里
            assertThat(distance).isGreaterThan(100000);
            assertThat(distance).isLessThan(120000);
        }

        @Test
        @DisplayName("纬度差异计算")
        void testLatitudeDifference() {
            Coordinate c1 = Coordinate.wgs84(0, 0);
            Coordinate c2 = Coordinate.wgs84(0, 1);

            double distance = calculator.calculate(c1, c2);

            // 1度纬度约111公里
            assertThat(distance).isGreaterThan(100000);
            assertThat(distance).isLessThan(120000);
        }

        @Test
        @DisplayName("对称性：a到b等于b到a")
        void testSymmetry() {
            Coordinate c1 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate c2 = Coordinate.wgs84(121.4737, 31.2304);

            double d1 = calculator.calculate(c1, c2);
            double d2 = calculator.calculate(c2, c1);

            assertThat(d1).isEqualTo(d2);
        }

        @Test
        @DisplayName("跨越日期线计算")
        void testCrossDateLine() {
            Coordinate c1 = Coordinate.wgs84(179, 0);
            Coordinate c2 = Coordinate.wgs84(-179, 0);

            double distance = calculator.calculate(c1, c2);

            // 跨越日期线应该是短距离
            assertThat(distance).isLessThan(500000);
        }
    }

    @Nested
    @DisplayName("DistanceCalculator接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现DistanceCalculator接口")
        void testImplementsInterface() {
            assertThat(calculator).isInstanceOf(DistanceCalculator.class);
        }
    }
}
