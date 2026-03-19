package cloud.opencode.base.lunar;

import cloud.opencode.base.lunar.calendar.Festival;
import cloud.opencode.base.lunar.calendar.SolarTerm;
import cloud.opencode.base.lunar.ganzhi.GanZhi;
import cloud.opencode.base.lunar.zodiac.Constellation;
import cloud.opencode.base.lunar.zodiac.Zodiac;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenLunar (主门面类) 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("OpenLunar (主门面类) 测试")
class OpenLunarTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(OpenLunar.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = OpenLunar.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("solarToLunar方法测试")
    class SolarToLunarTests {

        @Test
        @DisplayName("转换2024年春节")
        void testSpringFestival2024() {
            LunarDate lunar = OpenLunar.solarToLunar(2024, 2, 10);

            assertThat(lunar.year()).isEqualTo(2024);
            assertThat(lunar.month()).isEqualTo(1);
            assertThat(lunar.day()).isEqualTo(1);
        }

        @Test
        @DisplayName("LocalDate参数转换")
        void testFromLocalDate() {
            LocalDate date = LocalDate.of(2024, 2, 10);
            LunarDate lunar = OpenLunar.solarToLunar(date);

            assertThat(lunar.year()).isEqualTo(2024);
            assertThat(lunar.month()).isEqualTo(1);
            assertThat(lunar.day()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("lunarToSolar方法测试")
    class LunarToSolarTests {

        @Test
        @DisplayName("转换2024年春节")
        void testSpringFestival2024() {
            SolarDate solar = OpenLunar.lunarToSolar(2024, 1, 1, false);

            assertThat(solar.year()).isEqualTo(2024);
            assertThat(solar.month()).isEqualTo(2);
            assertThat(solar.day()).isEqualTo(10);
        }

        @Test
        @DisplayName("LunarDate参数转换")
        void testFromLunarDate() {
            LunarDate lunar = new LunarDate(2024, 1, 1, false);
            SolarDate solar = OpenLunar.lunarToSolar(lunar);

            assertThat(solar.year()).isEqualTo(2024);
            assertThat(solar.month()).isEqualTo(2);
            assertThat(solar.day()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("getSolarTerm方法测试")
    class GetSolarTermTests {

        @Test
        @DisplayName("获取节气日期的节气")
        void testOnTermDay() {
            LocalDate date = SolarTerm.CHUN_FEN.getDate(2024);
            SolarTerm term = OpenLunar.getSolarTerm(date);

            assertThat(term).isEqualTo(SolarTerm.CHUN_FEN);
        }
    }

    @Nested
    @DisplayName("getFestivals方法测试")
    class GetFestivalsTests {

        @Test
        @DisplayName("获取春节节日")
        void testSpringFestival() {
            LunarDate lunar = new LunarDate(2024, 1, 1, false);
            LocalDate solarDate = lunar.toSolar().toLocalDate();
            List<Festival> festivals = OpenLunar.getFestivals(solarDate);

            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().anyMatch(f -> f.name().contains("春节"))).isTrue();
        }

        @Test
        @DisplayName("获取元旦节日")
        void testNewYear() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            List<Festival> festivals = OpenLunar.getFestivals(date);

            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().anyMatch(f -> f.name().equals("元旦"))).isTrue();
        }
    }

    @Nested
    @DisplayName("getZodiac方法测试")
    class GetZodiacTests {

        @Test
        @DisplayName("2024年是龙年")
        void test2024Dragon() {
            Zodiac zodiac = OpenLunar.getZodiac(2024);

            assertThat(zodiac).isEqualTo(Zodiac.DRAGON);
        }

        @Test
        @DisplayName("2023年是兔年")
        void test2023Rabbit() {
            Zodiac zodiac = OpenLunar.getZodiac(2023);

            assertThat(zodiac).isEqualTo(Zodiac.RABBIT);
        }
    }

    @Nested
    @DisplayName("getConstellation方法测试")
    class GetConstellationTests {

        @Test
        @DisplayName("4月1日是白羊座")
        void testAries() {
            Constellation constellation = OpenLunar.getConstellation(4, 1);

            assertThat(constellation).isEqualTo(Constellation.ARIES);
        }

        @Test
        @DisplayName("LocalDate参数")
        void testFromLocalDate() {
            LocalDate date = LocalDate.of(2024, 4, 1);
            Constellation constellation = OpenLunar.getConstellation(date);

            assertThat(constellation).isEqualTo(Constellation.ARIES);
        }
    }

    @Nested
    @DisplayName("getYearGanZhi方法测试")
    class GetYearGanZhiTests {

        @Test
        @DisplayName("2024年是甲辰年")
        void test2024() {
            GanZhi ganZhi = OpenLunar.getYearGanZhi(2024);

            assertThat(ganZhi.getName()).isEqualTo("甲辰");
        }

        @Test
        @DisplayName("2023年是癸卯年")
        void test2023() {
            GanZhi ganZhi = OpenLunar.getYearGanZhi(2023);

            assertThat(ganZhi.getName()).isEqualTo("癸卯");
        }
    }

    @Nested
    @DisplayName("getMonthGanZhi方法测试")
    class GetMonthGanZhiTests {

        @Test
        @DisplayName("获取月干支")
        void testGetMonthGanZhi() {
            GanZhi ganZhi = OpenLunar.getMonthGanZhi(2024, 1);

            assertThat(ganZhi).isNotNull();
            assertThat(ganZhi.getName()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getDayGanZhi方法测试")
    class GetDayGanZhiTests {

        @Test
        @DisplayName("获取日干支")
        void testGetDayGanZhi() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            GanZhi ganZhi = OpenLunar.getDayGanZhi(date);

            assertThat(ganZhi).isNotNull();
            assertThat(ganZhi.getName()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("today方法测试")
    class TodayTests {

        @Test
        @DisplayName("获取今天农历")
        void testToday() {
            LunarDate today = OpenLunar.today();

            assertThat(today).isNotNull();
            assertThat(today.year()).isGreaterThanOrEqualTo(2024);
        }
    }

    @Nested
    @DisplayName("getLeapMonth方法测试")
    class GetLeapMonthTests {

        @Test
        @DisplayName("2020年闰四月")
        void test2020() {
            int leapMonth = OpenLunar.getLeapMonth(2020);
            assertThat(leapMonth).isEqualTo(4);
        }

        @Test
        @DisplayName("2024年无闰月")
        void test2024() {
            int leapMonth = OpenLunar.getLeapMonth(2024);
            assertThat(leapMonth).isEqualTo(0);
        }
    }
}
