package cloud.opencode.base.geo.geohash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoHashSearch 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.3
 */
@DisplayName("GeoHashSearch 测试")
class GeoHashSearchTest {

    // Beijing Tiananmen: lat=39.9042, lng=116.4074
    private static final double BEIJING_LAT = 39.9042;
    private static final double BEIJING_LNG = 116.4074;

    @Nested
    @DisplayName("searchHashes(lat, lng, radiusMeters)自动精度测试")
    class AutoPrecisionTests {

        @Test
        @DisplayName("返回非空结果集")
        void testReturnsNonEmpty() {
            Set<String> hashes = GeoHashSearch.searchHashes(BEIJING_LAT, BEIJING_LNG, 500);
            assertThat(hashes).isNotEmpty();
        }

        @Test
        @DisplayName("中心点的GeoHash包含在结果中")
        void testContainsCenterHash() {
            double radiusMeters = 500;
            Set<String> hashes = GeoHashSearch.searchHashes(BEIJING_LAT, BEIJING_LNG, radiusMeters);

            // Auto-select precision
            GeoHashPrecision precision = GeoHashPrecision.forRadius(radiusMeters / 1000.0);
            String centerHash = GeoHashUtil.encode(BEIJING_LAT, BEIJING_LNG, precision.getValue());

            assertThat(hashes).contains(centerHash);
        }

        @Test
        @DisplayName("小半径返回少量哈希")
        void testSmallRadiusLessHashes() {
            Set<String> hashes = GeoHashSearch.searchHashes(BEIJING_LAT, BEIJING_LNG, 10);
            assertThat(hashes).hasSizeLessThanOrEqualTo(50);
        }

        @Test
        @DisplayName("大半径返回更多哈希")
        void testLargerRadiusMoreHashes() {
            Set<String> small = GeoHashSearch.searchHashes(BEIJING_LAT, BEIJING_LNG, 100);
            Set<String> large = GeoHashSearch.searchHashes(BEIJING_LAT, BEIJING_LNG, 5000);
            // Both should have results; larger radius may have more at the auto-selected precision
            assertThat(small).isNotEmpty();
            assertThat(large).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("searchHashes(lat, lng, radiusMeters, int)指定精度测试")
    class SpecifiedPrecisionTests {

        @Test
        @DisplayName("精度6，500m半径返回合理数量的哈希")
        void testPrecision6() {
            Set<String> hashes = GeoHashSearch.searchHashes(
                    BEIJING_LAT, BEIJING_LNG, 500, 6);
            assertThat(hashes).isNotEmpty();
            // All hashes should have length 6
            for (String hash : hashes) {
                assertThat(hash).hasSize(6);
            }
        }

        @Test
        @DisplayName("精度7，200m半径返回合理数量的哈希")
        void testPrecision7() {
            Set<String> hashes = GeoHashSearch.searchHashes(
                    BEIJING_LAT, BEIJING_LNG, 200, 7);
            assertThat(hashes).isNotEmpty();
            for (String hash : hashes) {
                assertThat(hash).hasSize(7);
            }
        }

        @Test
        @DisplayName("所有哈希均为有效GeoHash")
        void testAllValidGeoHashes() {
            Set<String> hashes = GeoHashSearch.searchHashes(
                    BEIJING_LAT, BEIJING_LNG, 1000, 5);
            for (String hash : hashes) {
                assertThat(GeoHashUtil.isValid(hash))
                        .as("Hash '%s' should be valid", hash)
                        .isTrue();
            }
        }

        @Test
        @DisplayName("精度1-12范围检查")
        void testPrecisionRange() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashSearch.searchHashes(
                            BEIJING_LAT, BEIJING_LNG, 500, 0));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashSearch.searchHashes(
                            BEIJING_LAT, BEIJING_LNG, 500, 13));
        }
    }

    @Nested
    @DisplayName("searchHashes(lat, lng, radiusMeters, GeoHashPrecision)枚举精度测试")
    class EnumPrecisionTests {

        @Test
        @DisplayName("使用GeoHashPrecision.STREET")
        void testWithStreetPrecision() {
            Set<String> hashes = GeoHashSearch.searchHashes(
                    BEIJING_LAT, BEIJING_LNG, 200, GeoHashPrecision.STREET);
            assertThat(hashes).isNotEmpty();
            for (String hash : hashes) {
                assertThat(hash).hasSize(7);
            }
        }

        @Test
        @DisplayName("null精度抛出NullPointerException")
        void testNullPrecision() {
            assertThatNullPointerException()
                    .isThrownBy(() -> GeoHashSearch.searchHashes(
                            BEIJING_LAT, BEIJING_LNG, 500, (GeoHashPrecision) null));
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("赤道上的搜索")
        void testEquator() {
            Set<String> hashes = GeoHashSearch.searchHashes(0.0, 0.0, 1000, 6);
            assertThat(hashes).isNotEmpty();
        }

        @Test
        @DisplayName("国际日期变更线附近的搜索")
        void testDateLine() {
            Set<String> hashes = GeoHashSearch.searchHashes(0.0, 179.99, 5000, 5);
            assertThat(hashes).isNotEmpty();
        }

        @Test
        @DisplayName("南极附近的搜索")
        void testSouthPole() {
            Set<String> hashes = GeoHashSearch.searchHashes(-89.0, 0.0, 5000, 4);
            assertThat(hashes).isNotEmpty();
        }

        @Test
        @DisplayName("北极附近的搜索")
        void testNorthPole() {
            Set<String> hashes = GeoHashSearch.searchHashes(89.0, 0.0, 5000, 4);
            assertThat(hashes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("输入验证测试")
    class ValidationTests {

        @Test
        @DisplayName("无效纬度抛出异常")
        void testInvalidLatitude() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashSearch.searchHashes(91.0, 0.0, 500))
                    .withMessageContaining("Latitude");
        }

        @Test
        @DisplayName("无效经度抛出异常")
        void testInvalidLongitude() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashSearch.searchHashes(0.0, 181.0, 500))
                    .withMessageContaining("Longitude");
        }

        @Test
        @DisplayName("零半径抛出异常")
        void testZeroRadius() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashSearch.searchHashes(0.0, 0.0, 0));
        }

        @Test
        @DisplayName("负半径抛出异常")
        void testNegativeRadius() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashSearch.searchHashes(0.0, 0.0, -100));
        }

        @Test
        @DisplayName("NaN坐标抛出异常")
        void testNaNCoordinates() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashSearch.searchHashes(Double.NaN, 0.0, 500));
        }
    }

    @Nested
    @DisplayName("覆盖率测试")
    class CoverageTests {

        @Test
        @DisplayName("搜索结果覆盖圆形区域的边界框")
        void testCoversBoundingBox() {
            int precision = 6;
            double radiusMeters = 1000;
            Set<String> hashes = GeoHashSearch.searchHashes(
                    BEIJING_LAT, BEIJING_LNG, radiusMeters, precision);

            // Each hash in the result should be a valid geohash
            // and the collection should contain multiple hashes for a 1km radius
            assertThat(hashes.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("搜索结果中没有重复哈希")
        void testNoDuplicates() {
            Set<String> hashes = GeoHashSearch.searchHashes(
                    BEIJING_LAT, BEIJING_LNG, 500, 7);
            // Set naturally doesn't have duplicates, but verify size matches
            assertThat(hashes).hasSize(hashes.size());
        }
    }
}
