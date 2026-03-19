package cloud.opencode.base.geo.transform;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.CoordinateSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GCJ02ToBD09Transformer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("GCJ02ToBD09Transformer 测试")
class GCJ02ToBD09TransformerTest {

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("INSTANCE单例存在")
        void testInstanceExists() {
            assertThat(GCJ02ToBD09Transformer.INSTANCE).isNotNull();
        }

        @Test
        @DisplayName("INSTANCE单例类型正确")
        void testInstanceType() {
            assertThat(GCJ02ToBD09Transformer.INSTANCE)
                .isInstanceOf(GCJ02ToBD09Transformer.class)
                .isInstanceOf(CoordinateTransformer.class);
        }
    }

    @Nested
    @DisplayName("transform()测试")
    class TransformTests {

        @Test
        @DisplayName("GCJ02坐标转换为BD09")
        void testTransform() {
            Coordinate gcj02 = Coordinate.gcj02(116.4074, 39.9042);

            Coordinate bd09 = GCJ02ToBD09Transformer.INSTANCE.transform(gcj02);

            assertThat(bd09).isNotNull();
            assertThat(bd09.system()).isEqualTo(CoordinateSystem.BD09);
            // BD09坐标通常比GCJ02大
            assertThat(bd09.longitude()).isGreaterThan(gcj02.longitude());
            assertThat(bd09.latitude()).isGreaterThan(gcj02.latitude());
        }

        @Test
        @DisplayName("北京坐标转换")
        void testBeijingTransform() {
            Coordinate beijing = Coordinate.gcj02(116.4074, 39.9042);

            Coordinate result = GCJ02ToBD09Transformer.INSTANCE.transform(beijing);

            // BD09偏移量是固定算法
            assertThat(result.longitude()).isCloseTo(116.414, within(0.01));
            assertThat(result.latitude()).isCloseTo(39.910, within(0.01));
        }
    }

    @Nested
    @DisplayName("实现CoordinateTransformer接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现CoordinateTransformer接口")
        void testImplementsInterface() {
            GCJ02ToBD09Transformer transformer = new GCJ02ToBD09Transformer();

            assertThat(transformer).isInstanceOf(CoordinateTransformer.class);
        }

        @Test
        @DisplayName("可以作为函数式接口使用")
        void testAsFunctionalInterface() {
            CoordinateTransformer transformer = GCJ02ToBD09Transformer.INSTANCE;
            Coordinate gcj02 = Coordinate.gcj02(116.4074, 39.9042);

            Coordinate result = transformer.transform(gcj02);

            assertThat(result).isNotNull();
        }
    }
}
