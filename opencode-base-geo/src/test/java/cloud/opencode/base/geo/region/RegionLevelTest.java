package cloud.opencode.base.geo.region;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * RegionLevel 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("RegionLevel 测试")
class RegionLevelTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("COUNTRY枚举存在")
        void testCountryExists() {
            assertThat(RegionLevel.COUNTRY).isNotNull();
            assertThat(RegionLevel.COUNTRY.getCode()).isEqualTo(0);
            assertThat(RegionLevel.COUNTRY.getDescription()).isEqualTo("国家");
        }

        @Test
        @DisplayName("PROVINCE枚举存在")
        void testProvinceExists() {
            assertThat(RegionLevel.PROVINCE).isNotNull();
            assertThat(RegionLevel.PROVINCE.getCode()).isEqualTo(1);
        }

        @Test
        @DisplayName("CITY枚举存在")
        void testCityExists() {
            assertThat(RegionLevel.CITY).isNotNull();
            assertThat(RegionLevel.CITY.getCode()).isEqualTo(2);
        }

        @Test
        @DisplayName("DISTRICT枚举存在")
        void testDistrictExists() {
            assertThat(RegionLevel.DISTRICT).isNotNull();
            assertThat(RegionLevel.DISTRICT.getCode()).isEqualTo(3);
        }

        @Test
        @DisplayName("STREET枚举存在")
        void testStreetExists() {
            assertThat(RegionLevel.STREET).isNotNull();
            assertThat(RegionLevel.STREET.getCode()).isEqualTo(4);
        }

        @Test
        @DisplayName("COMMUNITY枚举存在")
        void testCommunityExists() {
            assertThat(RegionLevel.COMMUNITY).isNotNull();
            assertThat(RegionLevel.COMMUNITY.getCode()).isEqualTo(5);
        }

        @Test
        @DisplayName("values()返回所有枚举值")
        void testValues() {
            RegionLevel[] values = RegionLevel.values();
            assertThat(values).hasSize(6);
        }
    }

    @Nested
    @DisplayName("fromCode()测试")
    class FromCodeTests {

        @Test
        @DisplayName("有效code返回对应枚举")
        void testValidCode() {
            assertThat(RegionLevel.fromCode(0)).isEqualTo(RegionLevel.COUNTRY);
            assertThat(RegionLevel.fromCode(1)).isEqualTo(RegionLevel.PROVINCE);
            assertThat(RegionLevel.fromCode(2)).isEqualTo(RegionLevel.CITY);
            assertThat(RegionLevel.fromCode(3)).isEqualTo(RegionLevel.DISTRICT);
            assertThat(RegionLevel.fromCode(4)).isEqualTo(RegionLevel.STREET);
            assertThat(RegionLevel.fromCode(5)).isEqualTo(RegionLevel.COMMUNITY);
        }

        @Test
        @DisplayName("无效code返回null")
        void testInvalidCode() {
            assertThat(RegionLevel.fromCode(-1)).isNull();
            assertThat(RegionLevel.fromCode(100)).isNull();
        }
    }

    @Nested
    @DisplayName("isHigherThan()测试")
    class IsHigherThanTests {

        @Test
        @DisplayName("COUNTRY高于PROVINCE")
        void testCountryHigherThanProvince() {
            assertThat(RegionLevel.COUNTRY.isHigherThan(RegionLevel.PROVINCE)).isTrue();
        }

        @Test
        @DisplayName("PROVINCE不高于COUNTRY")
        void testProvinceNotHigherThanCountry() {
            assertThat(RegionLevel.PROVINCE.isHigherThan(RegionLevel.COUNTRY)).isFalse();
        }

        @Test
        @DisplayName("相同级别不是更高")
        void testSameLevelNotHigher() {
            assertThat(RegionLevel.CITY.isHigherThan(RegionLevel.CITY)).isFalse();
        }
    }

    @Nested
    @DisplayName("isLowerThan()测试")
    class IsLowerThanTests {

        @Test
        @DisplayName("PROVINCE低于COUNTRY")
        void testProvinceLowerThanCountry() {
            assertThat(RegionLevel.PROVINCE.isLowerThan(RegionLevel.COUNTRY)).isTrue();
        }

        @Test
        @DisplayName("COUNTRY不低于PROVINCE")
        void testCountryNotLowerThanProvince() {
            assertThat(RegionLevel.COUNTRY.isLowerThan(RegionLevel.PROVINCE)).isFalse();
        }

        @Test
        @DisplayName("相同级别不是更低")
        void testSameLevelNotLower() {
            assertThat(RegionLevel.CITY.isLowerThan(RegionLevel.CITY)).isFalse();
        }

        @Test
        @DisplayName("COMMUNITY低于所有其他级别")
        void testCommunityLowest() {
            assertThat(RegionLevel.COMMUNITY.isLowerThan(RegionLevel.COUNTRY)).isTrue();
            assertThat(RegionLevel.COMMUNITY.isLowerThan(RegionLevel.PROVINCE)).isTrue();
            assertThat(RegionLevel.COMMUNITY.isLowerThan(RegionLevel.CITY)).isTrue();
            assertThat(RegionLevel.COMMUNITY.isLowerThan(RegionLevel.DISTRICT)).isTrue();
            assertThat(RegionLevel.COMMUNITY.isLowerThan(RegionLevel.STREET)).isTrue();
        }
    }

    @Nested
    @DisplayName("getCode()测试")
    class GetCodeTests {

        @Test
        @DisplayName("code按层级递增")
        void testCodeIncreases() {
            assertThat(RegionLevel.COUNTRY.getCode()).isLessThan(RegionLevel.PROVINCE.getCode());
            assertThat(RegionLevel.PROVINCE.getCode()).isLessThan(RegionLevel.CITY.getCode());
            assertThat(RegionLevel.CITY.getCode()).isLessThan(RegionLevel.DISTRICT.getCode());
            assertThat(RegionLevel.DISTRICT.getCode()).isLessThan(RegionLevel.STREET.getCode());
            assertThat(RegionLevel.STREET.getCode()).isLessThan(RegionLevel.COMMUNITY.getCode());
        }
    }

    @Nested
    @DisplayName("getDescription()测试")
    class GetDescriptionTests {

        @Test
        @DisplayName("描述非空")
        void testDescriptionNotEmpty() {
            for (RegionLevel level : RegionLevel.values()) {
                assertThat(level.getDescription()).isNotNull().isNotEmpty();
            }
        }
    }
}
