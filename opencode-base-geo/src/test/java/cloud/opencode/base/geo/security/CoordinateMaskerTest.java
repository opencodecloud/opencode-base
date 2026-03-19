package cloud.opencode.base.geo.security;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.CoordinateSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CoordinateMasker 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("CoordinateMasker 测试")
class CoordinateMaskerTest {

    private final Coordinate beijing = Coordinate.wgs84(116.4074, 39.9042);

    @Nested
    @DisplayName("mask()测试")
    class MaskTests {

        @Test
        @DisplayName("在指定半径内模糊化")
        void testMaskWithinRadius() {
            Coordinate masked = CoordinateMasker.mask(beijing, 500);

            // 模糊化后的坐标应该在500米范围内
            double distance = calculateDistance(beijing, masked);
            assertThat(distance).isLessThanOrEqualTo(500.0);
        }

        @Test
        @DisplayName("偏移为0返回原坐标")
        void testMaskZeroOffset() {
            Coordinate masked = CoordinateMasker.mask(beijing, 0);

            assertThat(masked).isEqualTo(beijing);
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testMaskNullCoordinate() {
            assertThatThrownBy(() -> CoordinateMasker.mask(null, 500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("负偏移抛出异常")
        void testMaskNegativeOffset() {
            assertThatThrownBy(() -> CoordinateMasker.mask(beijing, -100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
        }

        @Test
        @DisplayName("保持坐标系统不变")
        void testMaskPreservesSystem() {
            Coordinate gcj02 = Coordinate.gcj02(116.4074, 39.9042);
            Coordinate masked = CoordinateMasker.mask(gcj02, 500);

            assertThat(masked.system()).isEqualTo(CoordinateSystem.GCJ02);
        }
    }

    @Nested
    @DisplayName("reducePrecision()测试")
    class ReducePrecisionTests {

        @Test
        @DisplayName("降低到3位小数")
        void testReduceTo3Decimals() {
            Coordinate coord = Coordinate.wgs84(116.40746789, 39.90426789);
            Coordinate reduced = CoordinateMasker.reducePrecision(coord, 3);

            assertThat(reduced.longitude()).isEqualTo(116.407);
            assertThat(reduced.latitude()).isEqualTo(39.904);
        }

        @Test
        @DisplayName("降低到0位小数")
        void testReduceTo0Decimals() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);
            Coordinate reduced = CoordinateMasker.reducePrecision(coord, 0);

            assertThat(reduced.longitude()).isEqualTo(116.0);
            assertThat(reduced.latitude()).isEqualTo(40.0);
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testReduceNullCoordinate() {
            assertThatThrownBy(() -> CoordinateMasker.reducePrecision(null, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("负小数位数抛出异常")
        void testReduceNegativeDecimals() {
            assertThatThrownBy(() -> CoordinateMasker.reducePrecision(beijing, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
        }

        @Test
        @DisplayName("保持坐标系统不变")
        void testReducePreservesSystem() {
            Coordinate bd09 = Coordinate.bd09(116.4074, 39.9042);
            Coordinate reduced = CoordinateMasker.reducePrecision(bd09, 3);

            assertThat(reduced.system()).isEqualTo(CoordinateSystem.BD09);
        }
    }

    @Nested
    @DisplayName("maskByGeoHash()测试")
    class MaskByGeoHashTests {

        @Test
        @DisplayName("使用GeoHash精度6模糊化")
        void testMaskByGeoHashPrecision6() {
            Coordinate masked = CoordinateMasker.maskByGeoHash(beijing, 6);

            // 应该返回GeoHash网格中心
            assertThat(masked).isNotNull();
            assertThat(masked.system()).isEqualTo(beijing.system());
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testMaskByGeoHashNullCoordinate() {
            assertThatThrownBy(() -> CoordinateMasker.maskByGeoHash(null, 6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("精度小于1抛出异常")
        void testMaskByGeoHashPrecisionTooLow() {
            assertThatThrownBy(() -> CoordinateMasker.maskByGeoHash(beijing, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 12");
        }

        @Test
        @DisplayName("精度大于12抛出异常")
        void testMaskByGeoHashPrecisionTooHigh() {
            assertThatThrownBy(() -> CoordinateMasker.maskByGeoHash(beijing, 13))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 12");
        }
    }

    @Nested
    @DisplayName("maskEnhanced()测试")
    class MaskEnhancedTests {

        @Test
        @DisplayName("组合模糊化")
        void testMaskEnhanced() {
            Coordinate masked = CoordinateMasker.maskEnhanced(beijing, 3, 100);

            assertThat(masked).isNotNull();
            assertThat(masked.system()).isEqualTo(beijing.system());
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testMaskEnhancedNullCoordinate() {
            assertThatThrownBy(() -> CoordinateMasker.maskEnhanced(null, 3, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
        }
    }

    @Nested
    @DisplayName("maskToCity()测试")
    class MaskToCityTests {

        @Test
        @DisplayName("城市级模糊化")
        void testMaskToCity() {
            Coordinate masked = CoordinateMasker.maskToCity(beijing);

            assertThat(masked).isNotNull();
            // 城市级精度约5km，坐标应该有变化但不会太远
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testMaskToCityNull() {
            assertThatThrownBy(() -> CoordinateMasker.maskToCity(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("maskToNeighborhood()测试")
    class MaskToNeighborhoodTests {

        @Test
        @DisplayName("街区级模糊化")
        void testMaskToNeighborhood() {
            Coordinate masked = CoordinateMasker.maskToNeighborhood(beijing);

            assertThat(masked).isNotNull();
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testMaskToNeighborhoodNull() {
            assertThatThrownBy(() -> CoordinateMasker.maskToNeighborhood(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("maskToBlock()测试")
    class MaskToBlockTests {

        @Test
        @DisplayName("街区块级模糊化")
        void testMaskToBlock() {
            Coordinate masked = CoordinateMasker.maskToBlock(beijing);

            assertThat(masked).isNotNull();
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testMaskToBlockNull() {
            assertThatThrownBy(() -> CoordinateMasker.maskToBlock(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    /**
     * 简单的Haversine距离计算（米）
     */
    private double calculateDistance(Coordinate c1, Coordinate c2) {
        double lat1 = Math.toRadians(c1.latitude());
        double lat2 = Math.toRadians(c2.latitude());
        double dLat = lat2 - lat1;
        double dLng = Math.toRadians(c2.longitude() - c1.longitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1) * Math.cos(lat2)
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371000.0 * c;
    }
}
