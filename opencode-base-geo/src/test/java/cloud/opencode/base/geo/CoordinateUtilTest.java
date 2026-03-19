package cloud.opencode.base.geo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CoordinateUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("CoordinateUtil 测试")
class CoordinateUtilTest {

    @Nested
    @DisplayName("transform()测试")
    class TransformTests {

        @Test
        @DisplayName("相同坐标系返回原坐标")
        void testTransformSameSystem() {
            Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);

            Coordinate result = CoordinateUtil.transform(wgs84, CoordinateSystem.WGS84);

            assertThat(result).isEqualTo(wgs84);
        }

        @Test
        @DisplayName("WGS84转GCJ02")
        void testWgs84ToGcj02() {
            Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);

            Coordinate gcj02 = CoordinateUtil.transform(wgs84, CoordinateSystem.GCJ02);

            assertThat(gcj02.system()).isEqualTo(CoordinateSystem.GCJ02);
            // 在中国境内会有偏移
            assertThat(gcj02.longitude()).isNotEqualTo(wgs84.longitude());
        }

        @Test
        @DisplayName("WGS84转BD09")
        void testWgs84ToBd09() {
            Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);

            Coordinate bd09 = CoordinateUtil.transform(wgs84, CoordinateSystem.BD09);

            assertThat(bd09.system()).isEqualTo(CoordinateSystem.BD09);
        }

        @Test
        @DisplayName("GCJ02转WGS84")
        void testGcj02ToWgs84() {
            Coordinate gcj02 = Coordinate.gcj02(116.4074, 39.9042);

            Coordinate wgs84 = CoordinateUtil.transform(gcj02, CoordinateSystem.WGS84);

            assertThat(wgs84.system()).isEqualTo(CoordinateSystem.WGS84);
        }

        @Test
        @DisplayName("GCJ02转BD09")
        void testGcj02ToBd09() {
            Coordinate gcj02 = Coordinate.gcj02(116.4074, 39.9042);

            Coordinate bd09 = CoordinateUtil.transform(gcj02, CoordinateSystem.BD09);

            assertThat(bd09.system()).isEqualTo(CoordinateSystem.BD09);
        }

        @Test
        @DisplayName("BD09转GCJ02")
        void testBd09ToGcj02() {
            Coordinate bd09 = Coordinate.bd09(116.4074, 39.9042);

            Coordinate gcj02 = CoordinateUtil.transform(bd09, CoordinateSystem.GCJ02);

            assertThat(gcj02.system()).isEqualTo(CoordinateSystem.GCJ02);
        }

        @Test
        @DisplayName("BD09转WGS84")
        void testBd09ToWgs84() {
            Coordinate bd09 = Coordinate.bd09(116.4074, 39.9042);

            Coordinate wgs84 = CoordinateUtil.transform(bd09, CoordinateSystem.WGS84);

            assertThat(wgs84.system()).isEqualTo(CoordinateSystem.WGS84);
        }
    }

    @Nested
    @DisplayName("wgs84ToGcj02()测试")
    class Wgs84ToGcj02Tests {

        @Test
        @DisplayName("中国境内坐标转换有偏移")
        void testInChinaTransform() {
            Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);

            Coordinate gcj02 = CoordinateUtil.wgs84ToGcj02(wgs84);

            assertThat(gcj02.system()).isEqualTo(CoordinateSystem.GCJ02);
            assertThat(gcj02.longitude()).isNotEqualTo(wgs84.longitude());
            assertThat(gcj02.latitude()).isNotEqualTo(wgs84.latitude());
        }

        @Test
        @DisplayName("中国境外坐标转换无偏移")
        void testOutsideChinaTransform() {
            Coordinate wgs84 = Coordinate.wgs84(-74.006, 40.7128); // 纽约

            Coordinate gcj02 = CoordinateUtil.wgs84ToGcj02(wgs84);

            assertThat(gcj02.longitude()).isEqualTo(wgs84.longitude());
            assertThat(gcj02.latitude()).isEqualTo(wgs84.latitude());
        }
    }

    @Nested
    @DisplayName("gcj02ToBd09()测试")
    class Gcj02ToBd09Tests {

        @Test
        @DisplayName("GCJ02转BD09有偏移")
        void testGcj02ToBd09HasOffset() {
            Coordinate gcj02 = Coordinate.gcj02(116.4074, 39.9042);

            Coordinate bd09 = CoordinateUtil.gcj02ToBd09(gcj02);

            assertThat(bd09.system()).isEqualTo(CoordinateSystem.BD09);
            assertThat(bd09.longitude()).isNotEqualTo(gcj02.longitude());
            assertThat(bd09.latitude()).isNotEqualTo(gcj02.latitude());
        }
    }

    @Nested
    @DisplayName("bd09ToGcj02()测试")
    class Bd09ToGcj02Tests {

        @Test
        @DisplayName("BD09转GCJ02")
        void testBd09ToGcj02() {
            Coordinate bd09 = Coordinate.bd09(116.4074, 39.9042);

            Coordinate gcj02 = CoordinateUtil.bd09ToGcj02(bd09);

            assertThat(gcj02.system()).isEqualTo(CoordinateSystem.GCJ02);
        }
    }

    @Nested
    @DisplayName("gcj02ToWgs84()测试")
    class Gcj02ToWgs84Tests {

        @Test
        @DisplayName("GCJ02转WGS84")
        void testGcj02ToWgs84() {
            Coordinate gcj02 = Coordinate.gcj02(116.4074, 39.9042);

            Coordinate wgs84 = CoordinateUtil.gcj02ToWgs84(gcj02);

            assertThat(wgs84.system()).isEqualTo(CoordinateSystem.WGS84);
        }
    }

    @Nested
    @DisplayName("往返转换测试")
    class RoundTripTests {

        @Test
        @DisplayName("WGS84->GCJ02->WGS84近似还原")
        void testWgs84Gcj02RoundTrip() {
            Coordinate original = Coordinate.wgs84(116.4074, 39.9042);

            Coordinate gcj02 = CoordinateUtil.wgs84ToGcj02(original);
            Coordinate restored = CoordinateUtil.gcj02ToWgs84(gcj02);

            // 往返转换会有微小误差
            assertThat(restored.longitude()).isCloseTo(original.longitude(), within(0.001));
            assertThat(restored.latitude()).isCloseTo(original.latitude(), within(0.001));
        }
    }
}
