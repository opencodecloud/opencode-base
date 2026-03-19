package cloud.opencode.base.geo.distance;

import cloud.opencode.base.geo.distance.DistanceCalculatorFactory.DistanceAccuracy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * DistanceCalculatorFactory 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("DistanceCalculatorFactory 测试")
class DistanceCalculatorFactoryTest {

    @Nested
    @DisplayName("DistanceAccuracy枚举测试")
    class DistanceAccuracyTests {

        @Test
        @DisplayName("FAST枚举存在")
        void testFastExists() {
            assertThat(DistanceAccuracy.FAST).isNotNull();
        }

        @Test
        @DisplayName("PRECISE枚举存在")
        void testPreciseExists() {
            assertThat(DistanceAccuracy.PRECISE).isNotNull();
        }

        @Test
        @DisplayName("values()返回所有枚举值")
        void testValues() {
            DistanceAccuracy[] values = DistanceAccuracy.values();
            assertThat(values).hasSize(2);
            assertThat(values).containsExactly(DistanceAccuracy.FAST, DistanceAccuracy.PRECISE);
        }

        @Test
        @DisplayName("valueOf()返回正确枚举")
        void testValueOf() {
            assertThat(DistanceAccuracy.valueOf("FAST")).isEqualTo(DistanceAccuracy.FAST);
            assertThat(DistanceAccuracy.valueOf("PRECISE")).isEqualTo(DistanceAccuracy.PRECISE);
        }
    }

    @Nested
    @DisplayName("create()测试")
    class CreateTests {

        @Test
        @DisplayName("create(FAST)返回HaversineCalculator")
        void testCreateFast() {
            DistanceCalculator calculator = DistanceCalculatorFactory.create(DistanceAccuracy.FAST);

            assertThat(calculator).isInstanceOf(HaversineCalculator.class);
            assertThat(calculator).isSameAs(HaversineCalculator.INSTANCE);
        }

        @Test
        @DisplayName("create(PRECISE)返回VincentyCalculator")
        void testCreatePrecise() {
            DistanceCalculator calculator = DistanceCalculatorFactory.create(DistanceAccuracy.PRECISE);

            assertThat(calculator).isInstanceOf(VincentyCalculator.class);
            assertThat(calculator).isSameAs(VincentyCalculator.INSTANCE);
        }
    }

    @Nested
    @DisplayName("getDefault()测试")
    class GetDefaultTests {

        @Test
        @DisplayName("getDefault()返回HaversineCalculator")
        void testGetDefault() {
            DistanceCalculator calculator = DistanceCalculatorFactory.getDefault();

            assertThat(calculator).isInstanceOf(HaversineCalculator.class);
            assertThat(calculator).isSameAs(HaversineCalculator.INSTANCE);
        }
    }

    @Nested
    @DisplayName("getPrecise()测试")
    class GetPreciseTests {

        @Test
        @DisplayName("getPrecise()返回VincentyCalculator")
        void testGetPrecise() {
            DistanceCalculator calculator = DistanceCalculatorFactory.getPrecise();

            assertThat(calculator).isInstanceOf(VincentyCalculator.class);
            assertThat(calculator).isSameAs(VincentyCalculator.INSTANCE);
        }
    }
}
