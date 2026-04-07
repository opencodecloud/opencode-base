package cloud.opencode.base.geo.geohash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoHashPrecision 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.3
 */
@DisplayName("GeoHashPrecision 测试")
class GeoHashPrecisionTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("所有精度级别的值正确")
        void testAllValues() {
            assertThat(GeoHashPrecision.CONTINENT.getValue()).isEqualTo(1);
            assertThat(GeoHashPrecision.COUNTRY.getValue()).isEqualTo(2);
            assertThat(GeoHashPrecision.LARGE_REGION.getValue()).isEqualTo(3);
            assertThat(GeoHashPrecision.REGION.getValue()).isEqualTo(4);
            assertThat(GeoHashPrecision.CITY.getValue()).isEqualTo(5);
            assertThat(GeoHashPrecision.NEIGHBORHOOD.getValue()).isEqualTo(6);
            assertThat(GeoHashPrecision.STREET.getValue()).isEqualTo(7);
            assertThat(GeoHashPrecision.BUILDING.getValue()).isEqualTo(8);
            assertThat(GeoHashPrecision.DOOR.getValue()).isEqualTo(9);
        }

        @Test
        @DisplayName("共有9个精度级别")
        void testEnumCount() {
            assertThat(GeoHashPrecision.values()).hasSize(9);
        }

        @Test
        @DisplayName("宽度随精度递增而递减")
        void testWidthDecreasing() {
            GeoHashPrecision[] levels = GeoHashPrecision.values();
            for (int i = 1; i < levels.length; i++) {
                assertThat(levels[i].getWidthKm())
                        .as("Width at precision %d should be less than at %d",
                                levels[i].getValue(), levels[i - 1].getValue())
                        .isLessThan(levels[i - 1].getWidthKm());
            }
        }

        @Test
        @DisplayName("高度随精度递增而递减")
        void testHeightDecreasing() {
            GeoHashPrecision[] levels = GeoHashPrecision.values();
            for (int i = 1; i < levels.length; i++) {
                assertThat(levels[i].getHeightKm())
                        .as("Height at precision %d should be less than at %d",
                                levels[i].getValue(), levels[i - 1].getValue())
                        .isLessThanOrEqualTo(levels[i - 1].getHeightKm());
            }
        }
    }

    @Nested
    @DisplayName("描述测试")
    class DescriptionTests {

        @Test
        @DisplayName("英文描述不为空")
        void testEnglishDescriptions() {
            for (GeoHashPrecision p : GeoHashPrecision.values()) {
                assertThat(p.getDescription()).isNotNull().isNotEmpty();
            }
        }

        @Test
        @DisplayName("中文描述不为空")
        void testChineseDescriptions() {
            for (GeoHashPrecision p : GeoHashPrecision.values()) {
                assertThat(p.getDescriptionZh()).isNotNull().isNotEmpty();
            }
        }

        @Test
        @DisplayName("CITY级别描述正确")
        void testCityDescription() {
            assertThat(GeoHashPrecision.CITY.getDescription()).isEqualTo("City/district");
            assertThat(GeoHashPrecision.CITY.getDescriptionZh()).isEqualTo("城市/区级");
        }
    }

    @Nested
    @DisplayName("fromValue()测试")
    class FromValueTests {

        @Test
        @DisplayName("有效值返回正确枚举")
        void testValidValues() {
            for (int i = 1; i <= 9; i++) {
                GeoHashPrecision p = GeoHashPrecision.fromValue(i);
                assertThat(p.getValue()).isEqualTo(i);
            }
        }

        @Test
        @DisplayName("值0抛出异常")
        void testZeroValue() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashPrecision.fromValue(0))
                    .withMessageContaining("1 and 9");
        }

        @Test
        @DisplayName("值10抛出异常")
        void testTenValue() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashPrecision.fromValue(10))
                    .withMessageContaining("1 and 9");
        }

        @Test
        @DisplayName("负值抛出异常")
        void testNegativeValue() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashPrecision.fromValue(-1));
        }
    }

    @Nested
    @DisplayName("forRadius()测试")
    class ForRadiusTests {

        @Test
        @DisplayName("大半径返回粗精度")
        void testLargeRadius() {
            GeoHashPrecision p = GeoHashPrecision.forRadius(5000.0);
            assertThat(p).isEqualTo(GeoHashPrecision.CONTINENT);
        }

        @Test
        @DisplayName("中等半径返回合适精度")
        void testMediumRadius() {
            GeoHashPrecision p = GeoHashPrecision.forRadius(3.0);
            assertThat(p.getValue()).isBetween(5, 6);
        }

        @Test
        @DisplayName("小半径返回细精度")
        void testSmallRadius() {
            GeoHashPrecision p = GeoHashPrecision.forRadius(0.01);
            assertThat(p.getValue()).isGreaterThanOrEqualTo(8);
        }

        @Test
        @DisplayName("非常小的半径返回DOOR")
        void testVerySmallRadius() {
            GeoHashPrecision p = GeoHashPrecision.forRadius(0.001);
            assertThat(p).isEqualTo(GeoHashPrecision.DOOR);
        }

        @Test
        @DisplayName("零半径抛出异常")
        void testZeroRadius() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashPrecision.forRadius(0.0));
        }

        @Test
        @DisplayName("负半径抛出异常")
        void testNegativeRadius() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashPrecision.forRadius(-1.0));
        }

        @Test
        @DisplayName("NaN半径抛出异常")
        void testNaNRadius() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashPrecision.forRadius(Double.NaN));
        }

        @Test
        @DisplayName("无穷大半径抛出异常")
        void testInfiniteRadius() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GeoHashPrecision.forRadius(Double.POSITIVE_INFINITY));
        }

        @Test
        @DisplayName("返回的精度单元格宽度不小于搜索半径")
        void testForRadiusCellWidthConstraint() {
            double[] radii = {0.005, 0.05, 0.5, 2.0, 10.0, 100.0, 1000.0};
            for (double r : radii) {
                GeoHashPrecision p = GeoHashPrecision.forRadius(r);
                assertThat(p.getWidthKm())
                        .as("Cell width for radius %.3f should be >= radius", r)
                        .isGreaterThanOrEqualTo(r);
            }
        }
    }

    @Nested
    @DisplayName("维度测试")
    class DimensionTests {

        @Test
        @DisplayName("所有宽度为正数")
        void testAllWidthsPositive() {
            for (GeoHashPrecision p : GeoHashPrecision.values()) {
                assertThat(p.getWidthKm()).isPositive();
            }
        }

        @Test
        @DisplayName("所有高度为正数")
        void testAllHeightsPositive() {
            for (GeoHashPrecision p : GeoHashPrecision.values()) {
                assertThat(p.getHeightKm()).isPositive();
            }
        }

        @Test
        @DisplayName("CONTINENT级别约5000km宽")
        void testContinentWidth() {
            assertThat(GeoHashPrecision.CONTINENT.getWidthKm()).isEqualTo(5000.0);
        }

        @Test
        @DisplayName("DOOR级别约4.8m宽")
        void testDoorWidth() {
            assertThat(GeoHashPrecision.DOOR.getWidthKm()).isEqualTo(0.0048);
        }
    }
}
