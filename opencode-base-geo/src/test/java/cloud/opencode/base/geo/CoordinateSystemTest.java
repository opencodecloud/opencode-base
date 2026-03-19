package cloud.opencode.base.geo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CoordinateSystem 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("CoordinateSystem 测试")
class CoordinateSystemTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("WGS84枚举存在")
        void testWGS84Exists() {
            assertThat(CoordinateSystem.WGS84).isNotNull();
            assertThat(CoordinateSystem.WGS84.getCode()).isEqualTo("WGS84");
            assertThat(CoordinateSystem.WGS84.getDescription()).isEqualTo("World Geodetic System 1984");
        }

        @Test
        @DisplayName("GCJ02枚举存在")
        void testGCJ02Exists() {
            assertThat(CoordinateSystem.GCJ02).isNotNull();
            assertThat(CoordinateSystem.GCJ02.getCode()).isEqualTo("GCJ02");
            assertThat(CoordinateSystem.GCJ02.getDescription()).isEqualTo("China Geodetic Coordinate System 2002");
        }

        @Test
        @DisplayName("BD09枚举存在")
        void testBD09Exists() {
            assertThat(CoordinateSystem.BD09).isNotNull();
            assertThat(CoordinateSystem.BD09.getCode()).isEqualTo("BD09");
            assertThat(CoordinateSystem.BD09.getDescription()).isEqualTo("Baidu Coordinate System 09");
        }

        @Test
        @DisplayName("values()返回所有枚举值")
        void testValues() {
            CoordinateSystem[] values = CoordinateSystem.values();
            assertThat(values).hasSize(3);
            assertThat(values).containsExactly(
                CoordinateSystem.WGS84,
                CoordinateSystem.GCJ02,
                CoordinateSystem.BD09
            );
        }
    }

    @Nested
    @DisplayName("valueOf()测试")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf返回正确枚举")
        void testValueOf() {
            assertThat(CoordinateSystem.valueOf("WGS84")).isEqualTo(CoordinateSystem.WGS84);
            assertThat(CoordinateSystem.valueOf("GCJ02")).isEqualTo(CoordinateSystem.GCJ02);
            assertThat(CoordinateSystem.valueOf("BD09")).isEqualTo(CoordinateSystem.BD09);
        }

        @Test
        @DisplayName("无效名称抛出异常")
        void testValueOfInvalid() {
            assertThatThrownBy(() -> CoordinateSystem.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getCode()测试")
    class GetCodeTests {

        @Test
        @DisplayName("每个枚举都有唯一code")
        void testUniqueCode() {
            String wgs84Code = CoordinateSystem.WGS84.getCode();
            String gcj02Code = CoordinateSystem.GCJ02.getCode();
            String bd09Code = CoordinateSystem.BD09.getCode();

            assertThat(wgs84Code).isNotEqualTo(gcj02Code);
            assertThat(gcj02Code).isNotEqualTo(bd09Code);
            assertThat(wgs84Code).isNotEqualTo(bd09Code);
        }
    }

    @Nested
    @DisplayName("getDescription()测试")
    class GetDescriptionTests {

        @Test
        @DisplayName("描述非空")
        void testDescriptionNotEmpty() {
            for (CoordinateSystem system : CoordinateSystem.values()) {
                assertThat(system.getDescription()).isNotNull().isNotEmpty();
            }
        }
    }
}
