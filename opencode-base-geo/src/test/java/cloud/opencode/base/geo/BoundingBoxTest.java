package cloud.opencode.base.geo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * BoundingBox 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.3
 */
@DisplayName("BoundingBox 测试")
class BoundingBoxTest {

    @Nested
    @DisplayName("构造函数验证测试")
    class ConstructorValidationTests {

        @Test
        @DisplayName("有效参数创建成功")
        void testValidConstruction() {
            BoundingBox box = new BoundingBox(116.0, 39.0, 117.0, 40.0);

            assertThat(box.minLng()).isEqualTo(116.0);
            assertThat(box.minLat()).isEqualTo(39.0);
            assertThat(box.maxLng()).isEqualTo(117.0);
            assertThat(box.maxLat()).isEqualTo(40.0);
        }

        @Test
        @DisplayName("NaN经度抛出异常")
        void testNanLongitude() {
            assertThatThrownBy(() -> new BoundingBox(Double.NaN, 39.0, 117.0, 40.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minLng");
        }

        @Test
        @DisplayName("NaN纬度抛出异常")
        void testNanLatitude() {
            assertThatThrownBy(() -> new BoundingBox(116.0, Double.NaN, 117.0, 40.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minLat");
        }

        @Test
        @DisplayName("Infinity经度抛出异常")
        void testInfinityLongitude() {
            assertThatThrownBy(() -> new BoundingBox(116.0, 39.0, Double.POSITIVE_INFINITY, 40.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxLng");
        }

        @Test
        @DisplayName("Infinity纬度抛出异常")
        void testInfinityLatitude() {
            assertThatThrownBy(() -> new BoundingBox(116.0, 39.0, 117.0, Double.NEGATIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxLat");
        }

        @Test
        @DisplayName("minLat大于maxLat抛出异常")
        void testMinLatGreaterThanMaxLat() {
            assertThatThrownBy(() -> new BoundingBox(116.0, 40.0, 117.0, 39.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minLat");
        }

        @Test
        @DisplayName("允许minLng大于maxLng（日期变更线）")
        void testDateLineCrossing() {
            BoundingBox box = new BoundingBox(170.0, 39.0, -170.0, 40.0);

            assertThat(box.minLng()).isEqualTo(170.0);
            assertThat(box.maxLng()).isEqualTo(-170.0);
        }
    }

    @Nested
    @DisplayName("of() 工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("创建正确的边界框")
        void testOf() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThat(box.minLng()).isEqualTo(116.0);
            assertThat(box.minLat()).isEqualTo(39.0);
            assertThat(box.maxLng()).isEqualTo(117.0);
            assertThat(box.maxLat()).isEqualTo(40.0);
        }
    }

    @Nested
    @DisplayName("fromCenter() 工厂方法测试")
    class FromCenterTests {

        @Test
        @DisplayName("从中心点和半径创建边界框")
        void testFromCenter() {
            Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
            BoundingBox box = BoundingBox.fromCenter(center, 1000);

            assertThat(box.minLng()).isLessThan(center.longitude());
            assertThat(box.maxLng()).isGreaterThan(center.longitude());
            assertThat(box.minLat()).isLessThan(center.latitude());
            assertThat(box.maxLat()).isGreaterThan(center.latitude());
            assertThat(box.contains(center)).isTrue();
        }

        @Test
        @DisplayName("半径为0时退化为点")
        void testFromCenterZeroRadius() {
            Coordinate center = Coordinate.wgs84(116.0, 39.0);
            BoundingBox box = BoundingBox.fromCenter(center, 0);

            assertThat(box.minLng()).isEqualTo(center.longitude());
            assertThat(box.maxLng()).isEqualTo(center.longitude());
            assertThat(box.minLat()).isEqualTo(center.latitude());
            assertThat(box.maxLat()).isEqualTo(center.latitude());
        }

        @Test
        @DisplayName("null中心点抛出异常")
        void testNullCenter() {
            assertThatThrownBy(() -> BoundingBox.fromCenter(null, 1000))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("负半径抛出异常")
        void testNegativeRadius() {
            Coordinate center = Coordinate.wgs84(116.0, 39.0);
            assertThatThrownBy(() -> BoundingBox.fromCenter(center, -1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NaN半径抛出异常")
        void testNanRadius() {
            Coordinate center = Coordinate.wgs84(116.0, 39.0);
            assertThatThrownBy(() -> BoundingBox.fromCenter(center, Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("fromCoordinates() 工厂方法测试")
    class FromCoordinatesTests {

        @Test
        @DisplayName("从坐标集合创建边界框")
        void testFromCoordinates() {
            List<Coordinate> coords = List.of(
                Coordinate.wgs84(116.0, 39.0),
                Coordinate.wgs84(117.0, 40.0),
                Coordinate.wgs84(116.5, 39.5)
            );
            BoundingBox box = BoundingBox.fromCoordinates(coords);

            assertThat(box.minLng()).isEqualTo(116.0);
            assertThat(box.minLat()).isEqualTo(39.0);
            assertThat(box.maxLng()).isEqualTo(117.0);
            assertThat(box.maxLat()).isEqualTo(40.0);
        }

        @Test
        @DisplayName("单个坐标创建退化边界框")
        void testFromSingleCoordinate() {
            List<Coordinate> coords = List.of(Coordinate.wgs84(116.0, 39.0));
            BoundingBox box = BoundingBox.fromCoordinates(coords);

            assertThat(box.minLng()).isEqualTo(116.0);
            assertThat(box.maxLng()).isEqualTo(116.0);
            assertThat(box.minLat()).isEqualTo(39.0);
            assertThat(box.maxLat()).isEqualTo(39.0);
        }

        @Test
        @DisplayName("null集合抛出异常")
        void testNullCollection() {
            assertThatThrownBy(() -> BoundingBox.fromCoordinates(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空集合抛出异常")
        void testEmptyCollection() {
            assertThatThrownBy(() -> BoundingBox.fromCoordinates(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("contains(Coordinate) 测试")
    class ContainsCoordinateTests {

        @Test
        @DisplayName("内部点返回true")
        void testContainsInside() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThat(box.contains(Coordinate.wgs84(116.5, 39.5))).isTrue();
        }

        @Test
        @DisplayName("边界点返回true")
        void testContainsBoundary() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThat(box.contains(Coordinate.wgs84(116.0, 39.0))).isTrue();
            assertThat(box.contains(Coordinate.wgs84(117.0, 40.0))).isTrue();
        }

        @Test
        @DisplayName("外部点返回false")
        void testContainsOutside() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThat(box.contains(Coordinate.wgs84(115.0, 39.5))).isFalse();
            assertThat(box.contains(Coordinate.wgs84(116.5, 41.0))).isFalse();
        }

        @Test
        @DisplayName("日期变更线穿越包含测试")
        void testContainsDateLineCrossing() {
            BoundingBox box = BoundingBox.of(170.0, -10.0, -170.0, 10.0);

            // Should contain points near 180/-180
            assertThat(box.contains(Coordinate.wgs84(175.0, 0.0))).isTrue();
            assertThat(box.contains(Coordinate.wgs84(-175.0, 0.0))).isTrue();

            // Should not contain points far from date line
            assertThat(box.contains(Coordinate.wgs84(0.0, 0.0))).isFalse();
        }

        @Test
        @DisplayName("null坐标抛出异常")
        void testContainsNull() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThatThrownBy(() -> box.contains((Coordinate) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("contains(BoundingBox) 测试")
    class ContainsBoundingBoxTests {

        @Test
        @DisplayName("完全包含返回true")
        void testContainsInner() {
            BoundingBox outer = BoundingBox.of(116.0, 39.0, 118.0, 41.0);
            BoundingBox inner = BoundingBox.of(116.5, 39.5, 117.5, 40.5);

            assertThat(outer.contains(inner)).isTrue();
        }

        @Test
        @DisplayName("部分重叠返回false")
        void testContainsPartialOverlap() {
            BoundingBox outer = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            BoundingBox partial = BoundingBox.of(116.5, 39.5, 118.0, 40.5);

            assertThat(outer.contains(partial)).isFalse();
        }

        @Test
        @DisplayName("自身包含返回true")
        void testContainsSelf() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThat(box.contains(box)).isTrue();
        }
    }

    @Nested
    @DisplayName("intersects() 测试")
    class IntersectsTests {

        @Test
        @DisplayName("重叠框返回true")
        void testIntersectsOverlapping() {
            BoundingBox box1 = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            BoundingBox box2 = BoundingBox.of(116.5, 39.5, 118.0, 41.0);

            assertThat(box1.intersects(box2)).isTrue();
            assertThat(box2.intersects(box1)).isTrue();
        }

        @Test
        @DisplayName("不相交框返回false")
        void testIntersectsDisjoint() {
            BoundingBox box1 = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            BoundingBox box2 = BoundingBox.of(120.0, 39.0, 121.0, 40.0);

            assertThat(box1.intersects(box2)).isFalse();
            assertThat(box2.intersects(box1)).isFalse();
        }

        @Test
        @DisplayName("纬度不相交返回false")
        void testIntersectsLatDisjoint() {
            BoundingBox box1 = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            BoundingBox box2 = BoundingBox.of(116.0, 41.0, 117.0, 42.0);

            assertThat(box1.intersects(box2)).isFalse();
        }

        @Test
        @DisplayName("边界接触返回true")
        void testIntersectsTouching() {
            BoundingBox box1 = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            BoundingBox box2 = BoundingBox.of(117.0, 39.0, 118.0, 40.0);

            assertThat(box1.intersects(box2)).isTrue();
        }

        @Test
        @DisplayName("日期变更线穿越相交测试")
        void testIntersectsDateLineCrossing() {
            BoundingBox wrap1 = BoundingBox.of(170.0, -10.0, -170.0, 10.0);
            BoundingBox wrap2 = BoundingBox.of(175.0, -5.0, -175.0, 5.0);

            assertThat(wrap1.intersects(wrap2)).isTrue();
        }

        @Test
        @DisplayName("null参数抛出异常")
        void testIntersectsNull() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThatThrownBy(() -> box.intersects(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("union() 测试")
    class UnionTests {

        @Test
        @DisplayName("合并两个框")
        void testUnion() {
            BoundingBox box1 = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            BoundingBox box2 = BoundingBox.of(117.0, 40.0, 118.0, 41.0);
            BoundingBox union = box1.union(box2);

            assertThat(union.minLng()).isEqualTo(116.0);
            assertThat(union.minLat()).isEqualTo(39.0);
            assertThat(union.maxLng()).isEqualTo(118.0);
            assertThat(union.maxLat()).isEqualTo(41.0);
        }

        @Test
        @DisplayName("合并包含关系的框")
        void testUnionContaining() {
            BoundingBox outer = BoundingBox.of(116.0, 39.0, 118.0, 41.0);
            BoundingBox inner = BoundingBox.of(116.5, 39.5, 117.5, 40.5);
            BoundingBox union = outer.union(inner);

            assertThat(union).isEqualTo(outer);
        }

        @Test
        @DisplayName("null参数抛出异常")
        void testUnionNull() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThatThrownBy(() -> box.union(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("expand() 测试")
    class ExpandTests {

        @Test
        @DisplayName("扩展后框变大")
        void testExpand() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            BoundingBox expanded = box.expand(1000);

            assertThat(expanded.minLng()).isLessThan(box.minLng());
            assertThat(expanded.maxLng()).isGreaterThan(box.maxLng());
            assertThat(expanded.minLat()).isLessThan(box.minLat());
            assertThat(expanded.maxLat()).isGreaterThan(box.maxLat());
        }

        @Test
        @DisplayName("扩展0米不变")
        void testExpandZero() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            BoundingBox expanded = box.expand(0);

            assertThat(expanded).isEqualTo(box);
        }

        @Test
        @DisplayName("负距离抛出异常")
        void testExpandNegative() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThatThrownBy(() -> box.expand(-1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("纬度不超过90度")
        void testExpandClampsLatitude() {
            BoundingBox box = BoundingBox.of(-180.0, 85.0, 180.0, 89.0);
            BoundingBox expanded = box.expand(1_000_000);

            assertThat(expanded.maxLat()).isLessThanOrEqualTo(90.0);
            assertThat(expanded.minLat()).isGreaterThanOrEqualTo(-90.0);
        }
    }

    @Nested
    @DisplayName("center() 测试")
    class CenterTests {

        @Test
        @DisplayName("正常框的中心")
        void testCenter() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 118.0, 41.0);
            Coordinate center = box.center();

            assertThat(center.longitude()).isCloseTo(117.0, within(0.001));
            assertThat(center.latitude()).isCloseTo(40.0, within(0.001));
            assertThat(center.system()).isEqualTo(CoordinateSystem.WGS84);
        }

        @Test
        @DisplayName("日期变更线穿越框的中心")
        void testCenterDateLineCrossing() {
            BoundingBox box = BoundingBox.of(170.0, -10.0, -170.0, 10.0);
            Coordinate center = box.center();

            assertThat(center.latitude()).isCloseTo(0.0, within(0.001));
            // Center should be at 180/-180
            assertThat(Math.abs(center.longitude())).isCloseTo(180.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("width()和height()测试")
    class DimensionTests {

        @Test
        @DisplayName("正常框宽度")
        void testWidth() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 118.0, 41.0);

            assertThat(box.width()).isCloseTo(2.0, within(0.001));
        }

        @Test
        @DisplayName("正常框高度")
        void testHeight() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 118.0, 41.0);

            assertThat(box.height()).isCloseTo(2.0, within(0.001));
        }

        @Test
        @DisplayName("日期变更线穿越框宽度")
        void testWidthDateLineCrossing() {
            BoundingBox box = BoundingBox.of(170.0, -10.0, -170.0, 10.0);

            assertThat(box.width()).isCloseTo(20.0, within(0.001));
        }

        @Test
        @DisplayName("退化框宽高为0")
        void testZeroDimensions() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 116.0, 39.0);

            assertThat(box.width()).isCloseTo(0.0, within(0.001));
            assertThat(box.height()).isCloseTo(0.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("crossesDateLine() 测试")
    class CrossesDateLineTests {

        @Test
        @DisplayName("正常框不穿越")
        void testNotCrossing() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThat(box.crossesDateLine()).isFalse();
        }

        @Test
        @DisplayName("穿越框minLng大于maxLng")
        void testCrossing() {
            BoundingBox box = BoundingBox.of(170.0, 39.0, -170.0, 40.0);

            assertThat(box.crossesDateLine()).isTrue();
        }
    }

    @Nested
    @DisplayName("toGeoHashes() 测试")
    class ToGeoHashesTests {

        @Test
        @DisplayName("生成非空GeoHash集合")
        void testToGeoHashes() {
            BoundingBox box = BoundingBox.of(116.3, 39.9, 116.5, 40.0);
            Set<String> hashes = box.toGeoHashes(5);

            assertThat(hashes).isNotEmpty();
            for (String hash : hashes) {
                assertThat(hash).hasSize(5);
            }
        }

        @Test
        @DisplayName("精度越高GeoHash数量越多")
        void testPrecisionAffectsCount() {
            BoundingBox box = BoundingBox.of(116.3, 39.9, 116.5, 40.0);
            Set<String> lowPrec = box.toGeoHashes(3);
            Set<String> highPrec = box.toGeoHashes(5);

            assertThat(highPrec.size()).isGreaterThanOrEqualTo(lowPrec.size());
        }

        @Test
        @DisplayName("无效精度抛出异常")
        void testInvalidPrecision() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThatThrownBy(() -> box.toGeoHashes(0))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> box.toGeoHashes(13))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("小框低精度只有少量GeoHash")
        void testSmallBoxLowPrecision() {
            BoundingBox box = BoundingBox.of(116.4, 39.9, 116.5, 40.0);
            Set<String> hashes = box.toGeoHashes(1);

            assertThat(hashes).isNotEmpty();
            // At precision 1, a small box should map to very few cells
            assertThat(hashes.size()).isLessThanOrEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Record特性测试")
    class RecordTests {

        @Test
        @DisplayName("equals基于值")
        void testEquals() {
            BoundingBox box1 = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            BoundingBox box2 = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThat(box1).isEqualTo(box2);
        }

        @Test
        @DisplayName("不同值不相等")
        void testNotEquals() {
            BoundingBox box1 = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            BoundingBox box2 = BoundingBox.of(116.0, 39.0, 118.0, 40.0);

            assertThat(box1).isNotEqualTo(box2);
        }

        @Test
        @DisplayName("hashCode一致")
        void testHashCode() {
            BoundingBox box1 = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            BoundingBox box2 = BoundingBox.of(116.0, 39.0, 117.0, 40.0);

            assertThat(box1.hashCode()).isEqualTo(box2.hashCode());
        }

        @Test
        @DisplayName("toString包含值")
        void testToString() {
            BoundingBox box = BoundingBox.of(116.0, 39.0, 117.0, 40.0);
            String str = box.toString();

            assertThat(str).contains("116.0").contains("39.0").contains("117.0").contains("40.0");
        }
    }
}
