package cloud.opencode.base.geo.transform;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.CoordinateSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * WGS84ToGCJ02Transformer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("WGS84ToGCJ02Transformer 测试")
class WGS84ToGCJ02TransformerTest {

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("INSTANCE单例存在")
        void testInstanceExists() {
            assertThat(WGS84ToGCJ02Transformer.INSTANCE).isNotNull();
        }

        @Test
        @DisplayName("INSTANCE单例类型正确")
        void testInstanceType() {
            assertThat(WGS84ToGCJ02Transformer.INSTANCE)
                .isInstanceOf(WGS84ToGCJ02Transformer.class)
                .isInstanceOf(CoordinateTransformer.class);
        }
    }

    @Nested
    @DisplayName("transform()测试")
    class TransformTests {

        @Test
        @DisplayName("WGS84坐标转换为GCJ02")
        void testTransform() {
            Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);

            Coordinate gcj02 = WGS84ToGCJ02Transformer.INSTANCE.transform(wgs84);

            assertThat(gcj02).isNotNull();
            assertThat(gcj02.system()).isEqualTo(CoordinateSystem.GCJ02);
            // 转换后坐标应该有偏移
            assertThat(gcj02.longitude()).isNotEqualTo(wgs84.longitude());
            assertThat(gcj02.latitude()).isNotEqualTo(wgs84.latitude());
        }

        @Test
        @DisplayName("北京坐标转换")
        void testBeijingTransform() {
            Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);

            Coordinate result = WGS84ToGCJ02Transformer.INSTANCE.transform(beijing);

            // GCJ02偏移通常在几百米范围内
            assertThat(result.longitude()).isCloseTo(116.413, within(0.01));
            assertThat(result.latitude()).isCloseTo(39.907, within(0.01));
        }

        @Test
        @DisplayName("原点坐标转换")
        void testOriginTransform() {
            Coordinate origin = Coordinate.wgs84(0, 0);

            Coordinate result = WGS84ToGCJ02Transformer.INSTANCE.transform(origin);

            // 原点不在中国境内，可能不会有偏移
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("实现CoordinateTransformer接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现CoordinateTransformer接口")
        void testImplementsInterface() {
            WGS84ToGCJ02Transformer transformer = new WGS84ToGCJ02Transformer();

            assertThat(transformer).isInstanceOf(CoordinateTransformer.class);
        }

        @Test
        @DisplayName("可以作为函数式接口使用")
        void testAsFunctionalInterface() {
            CoordinateTransformer transformer = WGS84ToGCJ02Transformer.INSTANCE;
            Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);

            Coordinate result = transformer.transform(wgs84);

            assertThat(result).isNotNull();
        }
    }
}
