package cloud.opencode.base.geo.transform;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.CoordinateSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CoordinateTransformer 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("CoordinateTransformer 测试")
class CoordinateTransformerTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("可以用lambda实现")
        void testLambdaImplementation() {
            // 简单的identity transformer
            CoordinateTransformer identity = c -> c;

            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate result = identity.transform(coord);

            assertThat(result).isEqualTo(coord);
        }

        @Test
        @DisplayName("可以用方法引用实现")
        void testMethodReference() {
            CoordinateTransformer transformer = coord ->
                new Coordinate(coord.longitude(), coord.latitude(), CoordinateSystem.GCJ02);

            Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate result = transformer.transform(wgs84);

            assertThat(result.system()).isEqualTo(CoordinateSystem.GCJ02);
        }

        @Test
        @DisplayName("自定义转换逻辑")
        void testCustomTransformer() {
            // 自定义转换器：将坐标加上偏移
            CoordinateTransformer offsetTransformer = coord ->
                new Coordinate(
                    coord.longitude() + 0.001,
                    coord.latitude() + 0.001,
                    coord.system()
                );

            Coordinate original = Coordinate.wgs84(116.0, 39.0);
            Coordinate result = offsetTransformer.transform(original);

            assertThat(result.longitude()).isEqualTo(116.001);
            assertThat(result.latitude()).isEqualTo(39.001);
        }
    }

    @Nested
    @DisplayName("链式转换测试")
    class ChainedTransformTests {

        @Test
        @DisplayName("链式使用多个转换器")
        void testChainedTransformers() {
            CoordinateTransformer wgs84ToGcj02 = WGS84ToGCJ02Transformer.INSTANCE;
            CoordinateTransformer gcj02ToBd09 = GCJ02ToBD09Transformer.INSTANCE;

            Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate gcj02 = wgs84ToGcj02.transform(wgs84);
            Coordinate bd09 = gcj02ToBd09.transform(gcj02);

            assertThat(wgs84.system()).isEqualTo(CoordinateSystem.WGS84);
            assertThat(gcj02.system()).isEqualTo(CoordinateSystem.GCJ02);
            assertThat(bd09.system()).isEqualTo(CoordinateSystem.BD09);
        }
    }

    @Nested
    @DisplayName("实现类测试")
    class ImplementationsTests {

        @Test
        @DisplayName("WGS84ToGCJ02Transformer实现接口")
        void testWGS84ToGCJ02() {
            CoordinateTransformer transformer = WGS84ToGCJ02Transformer.INSTANCE;
            assertThat(transformer).isInstanceOf(CoordinateTransformer.class);
        }

        @Test
        @DisplayName("GCJ02ToBD09Transformer实现接口")
        void testGCJ02ToBD09() {
            CoordinateTransformer transformer = GCJ02ToBD09Transformer.INSTANCE;
            assertThat(transformer).isInstanceOf(CoordinateTransformer.class);
        }

        @Test
        @DisplayName("BD09ToWGS84Transformer实现接口")
        void testBD09ToWGS84() {
            CoordinateTransformer transformer = BD09ToWGS84Transformer.INSTANCE;
            assertThat(transformer).isInstanceOf(CoordinateTransformer.class);
        }
    }
}
