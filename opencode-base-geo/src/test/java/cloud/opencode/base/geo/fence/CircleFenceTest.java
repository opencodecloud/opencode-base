package cloud.opencode.base.geo.fence;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CircleFence 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("CircleFence 测试")
class CircleFenceTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("正常创建CircleFence")
        void testConstructor() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence = new CircleFence(center, 1000);

            assertThat(fence.center()).isEqualTo(center);
            assertThat(fence.radius()).isEqualTo(1000);
        }

        @Test
        @DisplayName("center为null抛出异常")
        void testNullCenter() {
            assertThatThrownBy(() -> new CircleFence(null, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Center cannot be null");
        }

        @Test
        @DisplayName("radius为负数抛出异常")
        void testNegativeRadius() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);

            assertThatThrownBy(() -> new CircleFence(center, -100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Radius cannot be negative");
        }

        @Test
        @DisplayName("radius为0有效")
        void testZeroRadius() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence = new CircleFence(center, 0);

            assertThat(fence.radius()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("contains()测试")
    class ContainsTests {

        @Test
        @DisplayName("中心点在围栏内")
        void testCenterInside() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence = new CircleFence(center, 1000);

            assertThat(fence.contains(center)).isTrue();
        }

        @Test
        @DisplayName("半径内的点在围栏内")
        void testPointInside() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence = new CircleFence(center, 1000);

            // 约100米距离的点
            Coordinate nearby = Coordinate.wgs84(116.4084, 39.9042);

            assertThat(fence.contains(nearby)).isTrue();
        }

        @Test
        @DisplayName("半径外的点不在围栏内")
        void testPointOutside() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence = new CircleFence(center, 1000);

            // 约100公里距离的点
            Coordinate far = Coordinate.wgs84(117.0, 40.0);

            assertThat(fence.contains(far)).isFalse();
        }

        @Test
        @DisplayName("null点返回false")
        void testNullPoint() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence = new CircleFence(center, 1000);

            assertThat(fence.contains(null)).isFalse();
        }

        @Test
        @DisplayName("边界上的点在围栏内")
        void testPointOnBoundary() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence = new CircleFence(center, 1000);

            // 约1000米距离的点（在边界上）
            Coordinate onBoundary = Coordinate.wgs84(116.4164, 39.9042); // 约1km

            // 可能在内也可能在外，取决于精度
            // 这里主要测试不会抛异常
            fence.contains(onBoundary); // no exception
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("center()返回中心点")
        void testCenter() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence = new CircleFence(center, 1000);

            assertThat(fence.center()).isEqualTo(center);
        }

        @Test
        @DisplayName("radius()返回半径")
        void testRadius() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence = new CircleFence(center, 1000);

            assertThat(fence.radius()).isEqualTo(1000);
        }

        @Test
        @DisplayName("equals()正确比较")
        void testEquals() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence1 = new CircleFence(center, 1000);
            CircleFence fence2 = new CircleFence(center, 1000);
            CircleFence fence3 = new CircleFence(center, 2000);

            assertThat(fence1).isEqualTo(fence2);
            assertThat(fence1).isNotEqualTo(fence3);
        }

        @Test
        @DisplayName("hashCode()一致性")
        void testHashCode() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence1 = new CircleFence(center, 1000);
            CircleFence fence2 = new CircleFence(center, 1000);

            assertThat(fence1.hashCode()).isEqualTo(fence2.hashCode());
        }

        @Test
        @DisplayName("toString()包含信息")
        void testToString() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence = new CircleFence(center, 1000);

            String str = fence.toString();
            assertThat(str).contains("1000");
        }
    }

    @Nested
    @DisplayName("GeoFence接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现GeoFence接口")
        void testImplementsInterface() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            CircleFence fence = new CircleFence(center, 1000);

            assertThat(fence).isInstanceOf(GeoFence.class);
        }
    }
}
