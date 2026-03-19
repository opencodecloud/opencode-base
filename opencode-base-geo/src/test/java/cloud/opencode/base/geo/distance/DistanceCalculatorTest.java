package cloud.opencode.base.geo.distance;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * DistanceCalculator 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("DistanceCalculator 测试")
class DistanceCalculatorTest {

    private final Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);
    private final Coordinate shanghai = Coordinate.wgs84(121.4737, 31.2304);

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("可以用lambda实现")
        void testLambdaImplementation() {
            // 简单的直线距离（不考虑地球曲率）
            DistanceCalculator simpleCalculator = (c1, c2) -> {
                double dLng = c2.longitude() - c1.longitude();
                double dLat = c2.latitude() - c1.latitude();
                return Math.sqrt(dLng * dLng + dLat * dLat) * 111000; // 近似转换为米
            };

            double distance = simpleCalculator.calculate(beijing, shanghai);

            assertThat(distance).isPositive();
        }

        @Test
        @DisplayName("可以用方法引用实现")
        void testMethodReference() {
            DistanceCalculator calculator = HaversineCalculator.INSTANCE::calculate;

            double distance = calculator.calculate(beijing, shanghai);

            assertThat(distance).isGreaterThan(1000000); // 约1000公里
        }
    }

    @Nested
    @DisplayName("实现类测试")
    class ImplementationsTests {

        @Test
        @DisplayName("HaversineCalculator实现接口")
        void testHaversineImplements() {
            DistanceCalculator calculator = HaversineCalculator.INSTANCE;

            assertThat(calculator).isInstanceOf(DistanceCalculator.class);
        }

        @Test
        @DisplayName("VincentyCalculator实现接口")
        void testVincentyImplements() {
            DistanceCalculator calculator = VincentyCalculator.INSTANCE;

            assertThat(calculator).isInstanceOf(DistanceCalculator.class);
        }

        @Test
        @DisplayName("不同实现计算相同距离")
        void testDifferentImplementationsSameResult() {
            DistanceCalculator haversine = HaversineCalculator.INSTANCE;
            DistanceCalculator vincenty = VincentyCalculator.INSTANCE;

            double d1 = haversine.calculate(beijing, shanghai);
            double d2 = vincenty.calculate(beijing, shanghai);

            // 两种算法结果应该接近
            assertThat(d1).isCloseTo(d2, within(d1 * 0.01)); // 1%误差范围内
        }
    }

    @Nested
    @DisplayName("通过接口调用测试")
    class InterfaceCallTests {

        @Test
        @DisplayName("通过接口引用调用calculate")
        void testCalculateViaInterface() {
            DistanceCalculator calculator = HaversineCalculator.INSTANCE;

            double distance = calculator.calculate(beijing, shanghai);

            assertThat(distance).isGreaterThan(1000000);
            assertThat(distance).isLessThan(2000000);
        }

        @Test
        @DisplayName("相同点距离为0")
        void testSamePointDistance() {
            DistanceCalculator calculator = HaversineCalculator.INSTANCE;

            double distance = calculator.calculate(beijing, beijing);

            assertThat(distance).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("可以在方法参数中使用接口")
        void testAsMethodParameter() {
            double distance = measureDistance(HaversineCalculator.INSTANCE, beijing, shanghai);

            assertThat(distance).isGreaterThan(0);
        }

        private double measureDistance(DistanceCalculator calculator, Coordinate c1, Coordinate c2) {
            return calculator.calculate(c1, c2);
        }
    }

    @Nested
    @DisplayName("DistanceCalculatorFactory创建测试")
    class FactoryCreationTests {

        @Test
        @DisplayName("Factory创建的计算器实现接口")
        void testFactoryCreatesInterface() {
            DistanceCalculator fast = DistanceCalculatorFactory.create(DistanceCalculatorFactory.DistanceAccuracy.FAST);
            DistanceCalculator precise = DistanceCalculatorFactory.create(DistanceCalculatorFactory.DistanceAccuracy.PRECISE);

            assertThat(fast).isInstanceOf(DistanceCalculator.class);
            assertThat(precise).isInstanceOf(DistanceCalculator.class);
        }
    }
}
