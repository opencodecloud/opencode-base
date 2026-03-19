package cloud.opencode.base.geo.region;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.fence.GeoFence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Region 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("Region 测试")
class RegionTest {

    private List<Coordinate> createBoundary() {
        return List.of(
            Coordinate.wgs84(116.0, 39.0),
            Coordinate.wgs84(117.0, 39.0),
            Coordinate.wgs84(117.0, 40.0),
            Coordinate.wgs84(116.0, 40.0)
        );
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("正常创建Region")
        void testConstructor() {
            Region region = new Region(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                createBoundary(),
                "100000"
            );

            assertThat(region.code()).isEqualTo("110000");
            assertThat(region.name()).isEqualTo("北京市");
            assertThat(region.level()).isEqualTo(RegionLevel.PROVINCE);
        }

        @Test
        @DisplayName("code为null抛出异常")
        void testNullCode() {
            assertThatThrownBy(() -> new Region(
                null,
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                null,
                null
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("code cannot be null");
        }

        @Test
        @DisplayName("code为空白抛出异常")
        void testBlankCode() {
            assertThatThrownBy(() -> new Region(
                "  ",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                null,
                null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("name为null抛出异常")
        void testNullName() {
            assertThatThrownBy(() -> new Region(
                "110000",
                null,
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                null,
                null
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("name cannot be null");
        }

        @Test
        @DisplayName("level为null抛出异常")
        void testNullLevel() {
            assertThatThrownBy(() -> new Region(
                "110000",
                "北京市",
                null,
                Coordinate.wgs84(116.4074, 39.9042),
                null,
                null
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("level cannot be null");
        }

        @Test
        @DisplayName("boundary不可变")
        void testBoundaryImmutable() {
            List<Coordinate> boundary = new ArrayList<>(createBoundary());
            Region region = new Region(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                boundary,
                null
            );

            boundary.clear();

            assertThat(region.boundary()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of()创建无边界Region")
        void testOfWithoutBoundary() {
            Region region = Region.of(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                "100000"
            );

            assertThat(region.code()).isEqualTo("110000");
            assertThat(region.boundary()).isNull();
        }

        @Test
        @DisplayName("of()创建有边界Region")
        void testOfWithBoundary() {
            Region region = Region.of(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                createBoundary(),
                "100000"
            );

            assertThat(region.boundary()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("contains()测试")
    class ContainsTests {

        @Test
        @DisplayName("边界内的点返回true")
        void testContainsInside() {
            Region region = new Region(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                createBoundary(),
                null
            );

            Coordinate inside = Coordinate.wgs84(116.5, 39.5);

            assertThat(region.contains(inside)).isTrue();
        }

        @Test
        @DisplayName("边界外的点返回false")
        void testContainsOutside() {
            Region region = new Region(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                createBoundary(),
                null
            );

            Coordinate outside = Coordinate.wgs84(118.0, 39.5);

            assertThat(region.contains(outside)).isFalse();
        }

        @Test
        @DisplayName("null点返回false")
        void testContainsNull() {
            Region region = new Region(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                createBoundary(),
                null
            );

            assertThat(region.contains(null)).isFalse();
        }

        @Test
        @DisplayName("无边界返回false")
        void testContainsNoBoundary() {
            Region region = Region.of(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                null
            );

            assertThat(region.contains(Coordinate.wgs84(116.5, 39.5))).isFalse();
        }
    }

    @Nested
    @DisplayName("getBoundaryFence()测试")
    class GetBoundaryFenceTests {

        @Test
        @DisplayName("有边界返回GeoFence")
        void testHasBoundary() {
            Region region = new Region(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                createBoundary(),
                null
            );

            Optional<GeoFence> fence = region.getBoundaryFence();

            assertThat(fence).isPresent();
        }

        @Test
        @DisplayName("无边界返回empty")
        void testNoBoundary() {
            Region region = Region.of(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                null
            );

            Optional<GeoFence> fence = region.getBoundaryFence();

            assertThat(fence).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasBoundary()测试")
    class HasBoundaryTests {

        @Test
        @DisplayName("有边界返回true")
        void testHasBoundary() {
            Region region = new Region(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                createBoundary(),
                null
            );

            assertThat(region.hasBoundary()).isTrue();
        }

        @Test
        @DisplayName("无边界返回false")
        void testNoBoundary() {
            Region region = Region.of(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                null
            );

            assertThat(region.hasBoundary()).isFalse();
        }

        @Test
        @DisplayName("边界不足3点返回false")
        void testInsufficientBoundary() {
            Region region = new Region(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                List.of(Coordinate.wgs84(116, 39), Coordinate.wgs84(117, 39)),
                null
            );

            assertThat(region.hasBoundary()).isFalse();
        }
    }

    @Nested
    @DisplayName("isTopLevel()测试")
    class IsTopLevelTests {

        @Test
        @DisplayName("无父级是顶级")
        void testNoParent() {
            Region region = Region.of(
                "100000",
                "中国",
                RegionLevel.COUNTRY,
                Coordinate.wgs84(116.4074, 39.9042),
                null
            );

            assertThat(region.isTopLevel()).isTrue();
        }

        @Test
        @DisplayName("有父级不是顶级")
        void testHasParent() {
            Region region = Region.of(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                "100000"
            );

            assertThat(region.isTopLevel()).isFalse();
        }

        @Test
        @DisplayName("空白父级是顶级")
        void testBlankParent() {
            Region region = new Region(
                "100000",
                "中国",
                RegionLevel.COUNTRY,
                Coordinate.wgs84(116.4074, 39.9042),
                null,
                "  "
            );

            assertThat(region.isTopLevel()).isTrue();
        }
    }

    @Nested
    @DisplayName("getShortId()测试")
    class GetShortIdTests {

        @Test
        @DisplayName("返回code:name格式")
        void testShortId() {
            Region region = Region.of(
                "110000",
                "北京市",
                RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042),
                null
            );

            assertThat(region.getShortId()).isEqualTo("110000:北京市");
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("equals()正确比较")
        void testEquals() {
            Region region1 = Region.of("110000", "北京市", RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042), null);
            Region region2 = Region.of("110000", "北京市", RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042), null);

            assertThat(region1).isEqualTo(region2);
        }

        @Test
        @DisplayName("hashCode()一致性")
        void testHashCode() {
            Region region1 = Region.of("110000", "北京市", RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042), null);
            Region region2 = Region.of("110000", "北京市", RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042), null);

            assertThat(region1.hashCode()).isEqualTo(region2.hashCode());
        }
    }
}
