package cloud.opencode.base.geo.fence;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * RectangleFence 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("RectangleFence 测试")
class RectangleFenceTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("正常创建RectangleFence")
        void testConstructor() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);

            RectangleFence fence = new RectangleFence(sw, ne);

            assertThat(fence.southwest()).isEqualTo(sw);
            assertThat(fence.northeast()).isEqualTo(ne);
        }

        @Test
        @DisplayName("southwest为null抛出异常")
        void testNullSouthwest() {
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);

            assertThatThrownBy(() -> new RectangleFence(null, ne))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Corners cannot be null");
        }

        @Test
        @DisplayName("northeast为null抛出异常")
        void testNullNortheast() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);

            assertThatThrownBy(() -> new RectangleFence(sw, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Corners cannot be null");
        }
    }

    @Nested
    @DisplayName("contains()测试")
    class ContainsTests {

        @Test
        @DisplayName("内部点在围栏内")
        void testPointInside() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            Coordinate inside = Coordinate.wgs84(116.5, 39.5);

            assertThat(fence.contains(inside)).isTrue();
        }

        @Test
        @DisplayName("外部点不在围栏内")
        void testPointOutside() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            Coordinate outside = Coordinate.wgs84(118.0, 39.5);

            assertThat(fence.contains(outside)).isFalse();
        }

        @Test
        @DisplayName("边界上的点在围栏内")
        void testPointOnBoundary() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            Coordinate onBoundary = Coordinate.wgs84(116.0, 39.5);

            assertThat(fence.contains(onBoundary)).isTrue();
        }

        @Test
        @DisplayName("角点在围栏内")
        void testCornerInside() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            assertThat(fence.contains(sw)).isTrue();
            assertThat(fence.contains(ne)).isTrue();
        }

        @Test
        @DisplayName("null点返回false")
        void testNullPoint() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            assertThat(fence.contains(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("crossesDateLine()测试")
    class CrossesDateLineTests {

        @Test
        @DisplayName("不跨越日期线返回false")
        void testDoesNotCrossDateLine() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            assertThat(fence.crossesDateLine()).isFalse();
        }

        @Test
        @DisplayName("跨越日期线返回true")
        void testCrossesDateLine() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            assertThat(fence.crossesDateLine()).isTrue();
        }
    }

    @Nested
    @DisplayName("跨日期线contains()测试")
    class CrossDateLineContainsTests {

        @Test
        @DisplayName("跨日期线矩形包含正确点")
        void testCrossDateLineContains() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            // 在日期线东边
            assertThat(fence.contains(Coordinate.wgs84(175.0, 0))).isTrue();
            // 在日期线西边
            assertThat(fence.contains(Coordinate.wgs84(-175.0, 0))).isTrue();
            // 不在范围内
            assertThat(fence.contains(Coordinate.wgs84(0, 0))).isFalse();
        }
    }

    @Nested
    @DisplayName("widthDegrees()测试")
    class WidthDegreesTests {

        @Test
        @DisplayName("正常矩形宽度")
        void testNormalWidth() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            assertThat(fence.widthDegrees()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("跨日期线矩形宽度")
        void testCrossDateLineWidth() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            assertThat(fence.widthDegrees()).isEqualTo(20.0);
        }
    }

    @Nested
    @DisplayName("heightDegrees()测试")
    class HeightDegreesTests {

        @Test
        @DisplayName("矩形高度")
        void testHeight() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            assertThat(fence.heightDegrees()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("center()测试")
    class CenterTests {

        @Test
        @DisplayName("正常矩形中心")
        void testNormalCenter() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(118.0, 41.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            Coordinate center = fence.center();

            assertThat(center.longitude()).isEqualTo(117.0);
            assertThat(center.latitude()).isEqualTo(40.0);
        }

        @Test
        @DisplayName("跨日期线矩形中心")
        void testCrossDateLineCenter() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            Coordinate center = fence.center();

            // 中心应该在180度附近
            assertThat(Math.abs(center.longitude())).isCloseTo(180.0, within(1.0));
            assertThat(center.latitude()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("equals()正确比较")
        void testEquals() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);

            RectangleFence fence1 = new RectangleFence(sw, ne);
            RectangleFence fence2 = new RectangleFence(sw, ne);

            assertThat(fence1).isEqualTo(fence2);
        }

        @Test
        @DisplayName("hashCode()一致性")
        void testHashCode() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);

            RectangleFence fence1 = new RectangleFence(sw, ne);
            RectangleFence fence2 = new RectangleFence(sw, ne);

            assertThat(fence1.hashCode()).isEqualTo(fence2.hashCode());
        }
    }

    @Nested
    @DisplayName("GeoFence接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现GeoFence接口")
        void testImplementsInterface() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            RectangleFence fence = new RectangleFence(sw, ne);

            assertThat(fence).isInstanceOf(GeoFence.class);
        }
    }
}
