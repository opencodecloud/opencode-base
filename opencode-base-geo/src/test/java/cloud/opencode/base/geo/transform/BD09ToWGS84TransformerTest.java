package cloud.opencode.base.geo.transform;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.CoordinateSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * BD09ToWGS84Transformer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("BD09ToWGS84Transformer 测试")
class BD09ToWGS84TransformerTest {

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("INSTANCE单例存在")
        void testInstanceExists() {
            assertThat(BD09ToWGS84Transformer.INSTANCE).isNotNull();
        }

        @Test
        @DisplayName("INSTANCE单例类型正确")
        void testInstanceType() {
            assertThat(BD09ToWGS84Transformer.INSTANCE)
                .isInstanceOf(BD09ToWGS84Transformer.class)
                .isInstanceOf(CoordinateTransformer.class);
        }
    }

    @Nested
    @DisplayName("transform()测试")
    class TransformTests {

        @Test
        @DisplayName("BD09坐标转换为WGS84")
        void testTransform() {
            Coordinate bd09 = Coordinate.bd09(116.4135, 39.9108);

            Coordinate wgs84 = BD09ToWGS84Transformer.INSTANCE.transform(bd09);

            assertThat(wgs84).isNotNull();
            assertThat(wgs84.system()).isEqualTo(CoordinateSystem.WGS84);
        }

        @Test
        @DisplayName("北京坐标转换")
        void testBeijingTransform() {
            Coordinate beijing = Coordinate.bd09(116.4135, 39.9108);

            Coordinate result = BD09ToWGS84Transformer.INSTANCE.transform(beijing);

            // WGS84应该比BD09小
            assertThat(result.longitude()).isLessThan(beijing.longitude());
            assertThat(result.latitude()).isLessThan(beijing.latitude());
        }

        @Test
        @DisplayName("往返转换近似相等")
        void testRoundTrip() {
            Coordinate originalWgs84 = Coordinate.wgs84(116.4074, 39.9042);

            // WGS84 -> GCJ02 -> BD09
            Coordinate gcj02 = WGS84ToGCJ02Transformer.INSTANCE.transform(originalWgs84);
            Coordinate bd09 = GCJ02ToBD09Transformer.INSTANCE.transform(gcj02);

            // BD09 -> WGS84
            Coordinate resultWgs84 = BD09ToWGS84Transformer.INSTANCE.transform(bd09);

            // 往返转换应该近似相等
            assertThat(resultWgs84.longitude()).isCloseTo(originalWgs84.longitude(), within(0.0001));
            assertThat(resultWgs84.latitude()).isCloseTo(originalWgs84.latitude(), within(0.0001));
        }
    }

    @Nested
    @DisplayName("实现CoordinateTransformer接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现CoordinateTransformer接口")
        void testImplementsInterface() {
            BD09ToWGS84Transformer transformer = new BD09ToWGS84Transformer();

            assertThat(transformer).isInstanceOf(CoordinateTransformer.class);
        }

        @Test
        @DisplayName("可以作为函数式接口使用")
        void testAsFunctionalInterface() {
            CoordinateTransformer transformer = BD09ToWGS84Transformer.INSTANCE;
            Coordinate bd09 = Coordinate.bd09(116.4074, 39.9042);

            Coordinate result = transformer.transform(bd09);

            assertThat(result).isNotNull();
        }
    }
}
