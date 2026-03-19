package cloud.opencode.base.geo.fence;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CrossDateLineFence 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("CrossDateLineFence 测试")
class CrossDateLineFenceTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("正常创建CrossDateLineFence")
        void testConstructor() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);

            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            assertThat(fence.southwest()).isEqualTo(sw);
            assertThat(fence.northeast()).isEqualTo(ne);
        }

        @Test
        @DisplayName("southwest为null抛出异常")
        void testNullSouthwest() {
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);

            assertThatThrownBy(() -> new CrossDateLineFence(null, ne))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Corners cannot be null");
        }

        @Test
        @DisplayName("northeast为null抛出异常")
        void testNullNortheast() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);

            assertThatThrownBy(() -> new CrossDateLineFence(sw, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Corners cannot be null");
        }
    }

    @Nested
    @DisplayName("contains()跨日期线测试")
    class ContainsCrossDateLineTests {

        @Test
        @DisplayName("日期线东边的点在围栏内")
        void testPointEastOfDateLine() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            Coordinate point = Coordinate.wgs84(175.0, 0);

            assertThat(fence.contains(point)).isTrue();
        }

        @Test
        @DisplayName("日期线西边的点在围栏内")
        void testPointWestOfDateLine() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            Coordinate point = Coordinate.wgs84(-175.0, 0);

            assertThat(fence.contains(point)).isTrue();
        }

        @Test
        @DisplayName("日期线上的点在围栏内")
        void testPointOnDateLine() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            Coordinate point = Coordinate.wgs84(180.0, 0);

            assertThat(fence.contains(point)).isTrue();
        }

        @Test
        @DisplayName("范围外的点不在围栏内")
        void testPointOutside() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            Coordinate point = Coordinate.wgs84(0, 0);

            assertThat(fence.contains(point)).isFalse();
        }

        @Test
        @DisplayName("纬度范围外的点不在围栏内")
        void testPointOutsideLatitude() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            Coordinate point = Coordinate.wgs84(175.0, 20.0);

            assertThat(fence.contains(point)).isFalse();
        }
    }

    @Nested
    @DisplayName("contains()不跨日期线测试")
    class ContainsNormalTests {

        @Test
        @DisplayName("正常矩形内的点")
        void testPointInsideNormal() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            Coordinate inside = Coordinate.wgs84(116.5, 39.5);

            assertThat(fence.contains(inside)).isTrue();
        }

        @Test
        @DisplayName("正常矩形外的点")
        void testPointOutsideNormal() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            Coordinate outside = Coordinate.wgs84(118.0, 39.5);

            assertThat(fence.contains(outside)).isFalse();
        }
    }

    @Nested
    @DisplayName("contains()边界测试")
    class ContainsBoundaryTests {

        @Test
        @DisplayName("null点返回false")
        void testNullPoint() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            assertThat(fence.contains(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("crossesDateLine()测试")
    class CrossesDateLineTests {

        @Test
        @DisplayName("跨越日期线返回true")
        void testCrosses() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            assertThat(fence.crossesDateLine()).isTrue();
        }

        @Test
        @DisplayName("不跨越日期线返回false")
        void testDoesNotCross() {
            Coordinate sw = Coordinate.wgs84(116.0, 39.0);
            Coordinate ne = Coordinate.wgs84(117.0, 40.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            assertThat(fence.crossesDateLine()).isFalse();
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("southwest()返回西南角")
        void testSouthwest() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            assertThat(fence.southwest()).isEqualTo(sw);
        }

        @Test
        @DisplayName("northeast()返回东北角")
        void testNortheast() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            assertThat(fence.northeast()).isEqualTo(ne);
        }

        @Test
        @DisplayName("equals()正确比较")
        void testEquals() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);

            CrossDateLineFence fence1 = new CrossDateLineFence(sw, ne);
            CrossDateLineFence fence2 = new CrossDateLineFence(sw, ne);

            assertThat(fence1).isEqualTo(fence2);
        }

        @Test
        @DisplayName("hashCode()一致性")
        void testHashCode() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);

            CrossDateLineFence fence1 = new CrossDateLineFence(sw, ne);
            CrossDateLineFence fence2 = new CrossDateLineFence(sw, ne);

            assertThat(fence1.hashCode()).isEqualTo(fence2.hashCode());
        }
    }

    @Nested
    @DisplayName("GeoFence接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现GeoFence接口")
        void testImplementsInterface() {
            Coordinate sw = Coordinate.wgs84(170.0, -10.0);
            Coordinate ne = Coordinate.wgs84(-170.0, 10.0);
            CrossDateLineFence fence = new CrossDateLineFence(sw, ne);

            assertThat(fence).isInstanceOf(GeoFence.class);
        }
    }
}
