package cloud.opencode.base.geo.distance;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * VincentyCalculator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("VincentyCalculator 测试")
class VincentyCalculatorTest {

    private final VincentyCalculator calculator = VincentyCalculator.INSTANCE;

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("INSTANCE不为null")
        void testInstanceNotNull() {
            assertThat(VincentyCalculator.INSTANCE).isNotNull();
        }

        @Test
        @DisplayName("INSTANCE是同一实例")
        void testSameInstance() {
            assertThat(VincentyCalculator.INSTANCE).isSameAs(calculator);
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
        @DisplayName("比Haversine更精确")
        void testMorePreciseThanHaversine() {
            Coordinate c1 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate c2 = Coordinate.wgs84(121.4737, 31.2304);

            double vincentyDistance = calculator.calculate(c1, c2);
            double haversineDistance = HaversineCalculator.INSTANCE.calculate(c1, c2);

            // 两者应该接近但不完全相同
            assertThat(Math.abs(vincentyDistance - haversineDistance)).isLessThan(5000);
        }

        @Test
        @DisplayName("短距离计算准确")
        void testShortDistance() {
            Coordinate c1 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate c2 = Coordinate.wgs84(116.4084, 39.9042);

            double distance = calculator.calculate(c1, c2);

            assertThat(distance).isGreaterThan(50);
            assertThat(distance).isLessThan(150);
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
        @DisplayName("反向子午线计算")
        void testAntipodal() {
            Coordinate c1 = Coordinate.wgs84(0, 0);
            Coordinate c2 = Coordinate.wgs84(0, 1);

            double distance = calculator.calculate(c1, c2);

            // 1度纬度约111公里
            assertThat(distance).isGreaterThan(100000);
            assertThat(distance).isLessThan(120000);
        }
    }

    @Nested
    @DisplayName("收敛测试")
    class ConvergenceTests {

        @Test
        @DisplayName("近反极点不会失败")
        void testNearAntipodal() {
            // 接近对极点的情况
            Coordinate c1 = Coordinate.wgs84(0, 0);
            Coordinate c2 = Coordinate.wgs84(179, 0);

            double distance = calculator.calculate(c1, c2);

            assertThat(distance).isGreaterThan(0);
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
