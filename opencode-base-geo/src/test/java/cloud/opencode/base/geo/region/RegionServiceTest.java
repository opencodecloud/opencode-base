package cloud.opencode.base.geo.region;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * RegionService 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("RegionService 测试")
class RegionServiceTest {

    private RegionService service;

    @BeforeEach
    void setUp() {
        service = new RegionService();
    }

    private Region createBeijing() {
        return Region.of(
            "110000",
            "北京市",
            RegionLevel.PROVINCE,
            Coordinate.wgs84(116.4074, 39.9042),
            List.of(
                Coordinate.wgs84(115.0, 39.0),
                Coordinate.wgs84(117.0, 39.0),
                Coordinate.wgs84(117.0, 41.0),
                Coordinate.wgs84(115.0, 41.0)
            ),
            "100000"
        );
    }

    private Region createHaidian() {
        return Region.of(
            "110108",
            "海淀区",
            RegionLevel.DISTRICT,
            Coordinate.wgs84(116.2982, 39.9593),
            null,
            "110000"
        );
    }

    @Nested
    @DisplayName("register()测试")
    class RegisterTests {

        @Test
        @DisplayName("注册区域成功")
        void testRegister() {
            Region beijing = createBeijing();

            service.register(beijing);

            assertThat(service.size()).isEqualTo(1);
            assertThat(service.exists("110000")).isTrue();
        }

        @Test
        @DisplayName("null区域抛出异常")
        void testRegisterNull() {
            assertThatThrownBy(() -> service.register(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Region cannot be null");
        }

        @Test
        @DisplayName("重复注册覆盖")
        void testRegisterOverwrite() {
            Region beijing1 = createBeijing();
            Region beijing2 = Region.of("110000", "北京市2", RegionLevel.PROVINCE,
                Coordinate.wgs84(116.4074, 39.9042), null);

            service.register(beijing1);
            service.register(beijing2);

            assertThat(service.size()).isEqualTo(1);
            assertThat(service.findByCode("110000").get().name()).isEqualTo("北京市2");
        }
    }

    @Nested
    @DisplayName("registerAll()测试")
    class RegisterAllTests {

        @Test
        @DisplayName("批量注册成功")
        void testRegisterAll() {
            service.registerAll(List.of(createBeijing(), createHaidian()));

            assertThat(service.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("null列表不抛异常")
        void testRegisterAllNull() {
            service.registerAll(null);

            assertThat(service.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("unregister()测试")
    class UnregisterTests {

        @Test
        @DisplayName("注销存在的区域")
        void testUnregister() {
            service.register(createBeijing());

            Region removed = service.unregister("110000");

            assertThat(removed).isNotNull();
            assertThat(service.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("注销不存在的区域返回null")
        void testUnregisterNotExists() {
            Region removed = service.unregister("999999");

            assertThat(removed).isNull();
        }

        @Test
        @DisplayName("注销null返回null")
        void testUnregisterNull() {
            assertThat(service.unregister(null)).isNull();
        }
    }

    @Nested
    @DisplayName("findByCode()测试")
    class FindByCodeTests {

        @Test
        @DisplayName("找到存在的区域")
        void testFindExists() {
            service.register(createBeijing());

            Optional<Region> result = service.findByCode("110000");

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("北京市");
        }

        @Test
        @DisplayName("找不到返回empty")
        void testFindNotExists() {
            Optional<Region> result = service.findByCode("999999");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null返回empty")
        void testFindNull() {
            assertThat(service.findByCode(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByName()测试")
    class FindByNameTests {

        @Test
        @DisplayName("按名称查找")
        void testFindByName() {
            service.register(createBeijing());
            service.register(createHaidian());

            List<Region> results = service.findByName("北京");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).code()).isEqualTo("110000");
        }

        @Test
        @DisplayName("部分匹配")
        void testPartialMatch() {
            service.register(createBeijing());

            List<Region> results = service.findByName("京");

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("不区分大小写")
        void testCaseInsensitive() {
            service.register(Region.of("test", "TestRegion", RegionLevel.CITY,
                Coordinate.wgs84(0, 0), null));

            List<Region> results = service.findByName("testregion");

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("空白名称返回空列表")
        void testBlankName() {
            assertThat(service.findByName(null)).isEmpty();
            assertThat(service.findByName("")).isEmpty();
            assertThat(service.findByName("  ")).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByLevel()测试")
    class FindByLevelTests {

        @Test
        @DisplayName("按级别查找")
        void testFindByLevel() {
            service.register(createBeijing());
            service.register(createHaidian());

            List<Region> provinces = service.findByLevel(RegionLevel.PROVINCE);
            List<Region> districts = service.findByLevel(RegionLevel.DISTRICT);

            assertThat(provinces).hasSize(1);
            assertThat(districts).hasSize(1);
        }

        @Test
        @DisplayName("null级别返回空列表")
        void testNullLevel() {
            assertThat(service.findByLevel(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCoordinate()测试")
    class FindByCoordinateTests {

        @Test
        @DisplayName("按坐标查找包含的区域")
        void testFindByCoordinate() {
            service.register(createBeijing());

            Coordinate inside = Coordinate.wgs84(116.0, 40.0);
            List<Region> results = service.findByCoordinate(inside);

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("坐标外无结果")
        void testCoordinateOutside() {
            service.register(createBeijing());

            Coordinate outside = Coordinate.wgs84(120.0, 31.0);
            List<Region> results = service.findByCoordinate(outside);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("null坐标返回空列表")
        void testNullCoordinate() {
            assertThat(service.findByCoordinate(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("getChildren()测试")
    class GetChildrenTests {

        @Test
        @DisplayName("获取子区域")
        void testGetChildren() {
            service.register(createBeijing());
            service.register(createHaidian());

            List<Region> children = service.getChildren("110000");

            assertThat(children).hasSize(1);
            assertThat(children.get(0).code()).isEqualTo("110108");
        }

        @Test
        @DisplayName("无子区域返回空列表")
        void testNoChildren() {
            service.register(createHaidian());

            List<Region> children = service.getChildren("110108");

            assertThat(children).isEmpty();
        }

        @Test
        @DisplayName("null返回空列表")
        void testNullParent() {
            assertThat(service.getChildren(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("getParent()测试")
    class GetParentTests {

        @Test
        @DisplayName("获取父区域")
        void testGetParent() {
            service.register(createBeijing());
            service.register(createHaidian());

            Optional<Region> parent = service.getParent("110108");

            assertThat(parent).isPresent();
            assertThat(parent.get().code()).isEqualTo("110000");
        }

        @Test
        @DisplayName("顶级区域无父级")
        void testTopLevelNoParent() {
            Region country = Region.of("100000", "中国", RegionLevel.COUNTRY,
                Coordinate.wgs84(116, 39), null);
            service.register(country);

            Optional<Region> parent = service.getParent("100000");

            assertThat(parent).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAncestors()测试")
    class GetAncestorsTests {

        @Test
        @DisplayName("获取祖先链")
        void testGetAncestors() {
            Region country = Region.of("100000", "中国", RegionLevel.COUNTRY,
                Coordinate.wgs84(116, 39), null);
            service.register(country);
            service.register(createBeijing());
            service.register(createHaidian());

            List<Region> ancestors = service.getAncestors("110108");

            assertThat(ancestors).hasSize(2);
            assertThat(ancestors.get(0).code()).isEqualTo("110000"); // 直接父级
            assertThat(ancestors.get(1).code()).isEqualTo("100000"); // 祖父级
        }
    }

    @Nested
    @DisplayName("size()和exists()测试")
    class SizeAndExistsTests {

        @Test
        @DisplayName("size()返回正确数量")
        void testSize() {
            assertThat(service.size()).isEqualTo(0);

            service.register(createBeijing());
            assertThat(service.size()).isEqualTo(1);

            service.register(createHaidian());
            assertThat(service.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("exists()检查存在性")
        void testExists() {
            assertThat(service.exists("110000")).isFalse();

            service.register(createBeijing());
            assertThat(service.exists("110000")).isTrue();
            assertThat(service.exists(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("clear()和getAll()测试")
    class ClearAndGetAllTests {

        @Test
        @DisplayName("clear()清空所有区域")
        void testClear() {
            service.register(createBeijing());
            service.register(createHaidian());

            service.clear();

            assertThat(service.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("getAll()返回所有区域")
        void testGetAll() {
            service.register(createBeijing());
            service.register(createHaidian());

            List<Region> all = service.getAll();

            assertThat(all).hasSize(2);
        }
    }
}
