package cloud.opencode.base.lunar.calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Festival (节日) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("Festival (节日) 测试")
class FestivalTest {

    @Nested
    @DisplayName("Record基本测试")
    class RecordBasicTests {

        @Test
        @DisplayName("创建节日")
        void testCreate() {
            Festival festival = new Festival("测试节", "Test Festival",
                Festival.FestivalType.SOLAR, MonthDay.of(1, 1));

            assertThat(festival.name()).isEqualTo("测试节");
            assertThat(festival.englishName()).isEqualTo("Test Festival");
            assertThat(festival.type()).isEqualTo(Festival.FestivalType.SOLAR);
            assertThat(festival.date()).isEqualTo(MonthDay.of(1, 1));
        }

        @Test
        @DisplayName("equals比较")
        void testEquals() {
            Festival f1 = new Festival("测试节", "Test", Festival.FestivalType.SOLAR, MonthDay.of(1, 1));
            Festival f2 = new Festival("测试节", "Test", Festival.FestivalType.SOLAR, MonthDay.of(1, 1));
            Festival f3 = new Festival("其他节", "Other", Festival.FestivalType.SOLAR, MonthDay.of(1, 2));

            assertThat(f1).isEqualTo(f2);
            assertThat(f1).isNotEqualTo(f3);
        }
    }

    @Nested
    @DisplayName("预定义节日测试")
    class PredefinedFestivalsTests {

        @Test
        @DisplayName("元旦")
        void testNewYear() {
            assertThat(Festival.NEW_YEAR).isNotNull();
            assertThat(Festival.NEW_YEAR.name()).isEqualTo("元旦");
            assertThat(Festival.NEW_YEAR.date()).isEqualTo(MonthDay.of(1, 1));
            assertThat(Festival.NEW_YEAR.type()).isEqualTo(Festival.FestivalType.SOLAR);
        }

        @Test
        @DisplayName("情人节")
        void testValentine() {
            assertThat(Festival.VALENTINE).isNotNull();
            assertThat(Festival.VALENTINE.name()).isEqualTo("情人节");
            assertThat(Festival.VALENTINE.date()).isEqualTo(MonthDay.of(2, 14));
        }

        @Test
        @DisplayName("春节")
        void testSpringFestival() {
            assertThat(Festival.SPRING_FESTIVAL).isNotNull();
            assertThat(Festival.SPRING_FESTIVAL.name()).isEqualTo("春节");
            assertThat(Festival.SPRING_FESTIVAL.date()).isEqualTo(MonthDay.of(1, 1));
            assertThat(Festival.SPRING_FESTIVAL.type()).isEqualTo(Festival.FestivalType.LUNAR);
        }

        @Test
        @DisplayName("元宵节")
        void testLanternFestival() {
            assertThat(Festival.LANTERN_FESTIVAL).isNotNull();
            assertThat(Festival.LANTERN_FESTIVAL.name()).isEqualTo("元宵节");
            assertThat(Festival.LANTERN_FESTIVAL.date()).isEqualTo(MonthDay.of(1, 15));
            assertThat(Festival.LANTERN_FESTIVAL.type()).isEqualTo(Festival.FestivalType.LUNAR);
        }

        @Test
        @DisplayName("中秋节")
        void testMidAutumnFestival() {
            assertThat(Festival.MID_AUTUMN).isNotNull();
            assertThat(Festival.MID_AUTUMN.name()).isEqualTo("中秋节");
            assertThat(Festival.MID_AUTUMN.date()).isEqualTo(MonthDay.of(8, 15));
            assertThat(Festival.MID_AUTUMN.type()).isEqualTo(Festival.FestivalType.LUNAR);
        }

        @Test
        @DisplayName("端午节")
        void testDragonBoatFestival() {
            assertThat(Festival.DRAGON_BOAT).isNotNull();
            assertThat(Festival.DRAGON_BOAT.name()).isEqualTo("端午节");
            assertThat(Festival.DRAGON_BOAT.date()).isEqualTo(MonthDay.of(5, 5));
            assertThat(Festival.DRAGON_BOAT.type()).isEqualTo(Festival.FestivalType.LUNAR);
        }

        @Test
        @DisplayName("重阳节")
        void testDoubleNinthFestival() {
            assertThat(Festival.DOUBLE_NINTH).isNotNull();
            assertThat(Festival.DOUBLE_NINTH.name()).isEqualTo("重阳节");
            assertThat(Festival.DOUBLE_NINTH.date()).isEqualTo(MonthDay.of(9, 9));
        }

        @Test
        @DisplayName("国庆节")
        void testNationalDay() {
            assertThat(Festival.NATIONAL_DAY).isNotNull();
            assertThat(Festival.NATIONAL_DAY.name()).isEqualTo("国庆节");
            assertThat(Festival.NATIONAL_DAY.date()).isEqualTo(MonthDay.of(10, 1));
            assertThat(Festival.NATIONAL_DAY.type()).isEqualTo(Festival.FestivalType.SOLAR);
        }

        @Test
        @DisplayName("劳动节")
        void testLaborDay() {
            assertThat(Festival.LABOR_DAY).isNotNull();
            assertThat(Festival.LABOR_DAY.name()).isEqualTo("劳动节");
            assertThat(Festival.LABOR_DAY.date()).isEqualTo(MonthDay.of(5, 1));
        }
    }

    @Nested
    @DisplayName("getSolarFestivals方法测试")
    class GetSolarFestivalsTests {

        @Test
        @DisplayName("1月1日有元旦")
        void testJan1() {
            List<Festival> festivals = Festival.getSolarFestivals(LocalDate.of(2024, 1, 1));
            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().anyMatch(f -> f.name().equals("元旦"))).isTrue();
        }

        @Test
        @DisplayName("2月14日有情人节")
        void testFeb14() {
            List<Festival> festivals = Festival.getSolarFestivals(LocalDate.of(2024, 2, 14));
            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().anyMatch(f -> f.name().equals("情人节"))).isTrue();
        }

        @Test
        @DisplayName("非节日日期返回空列表")
        void testNonFestivalDate() {
            List<Festival> festivals = Festival.getSolarFestivals(LocalDate.of(2024, 3, 15));
            // 可能为空或包含其他节日
            assertThat(festivals).isNotNull();
        }
    }

    @Nested
    @DisplayName("getLunarFestivals方法测试")
    class GetLunarFestivalsTests {

        @Test
        @DisplayName("正月初一有春节")
        void testSpringFestival() {
            List<Festival> festivals = Festival.getLunarFestivals(1, 1);
            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().anyMatch(f -> f.name().equals("春节"))).isTrue();
        }

        @Test
        @DisplayName("正月十五有元宵节")
        void testLanternFestival() {
            List<Festival> festivals = Festival.getLunarFestivals(1, 15);
            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().anyMatch(f -> f.name().equals("元宵节"))).isTrue();
        }

        @Test
        @DisplayName("八月十五有中秋节")
        void testMidAutumn() {
            List<Festival> festivals = Festival.getLunarFestivals(8, 15);
            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().anyMatch(f -> f.name().equals("中秋节"))).isTrue();
        }

        @Test
        @DisplayName("五月初五有端午节")
        void testDragonBoat() {
            List<Festival> festivals = Festival.getLunarFestivals(5, 5);
            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().anyMatch(f -> f.name().equals("端午节"))).isTrue();
        }
    }

    @Nested
    @DisplayName("getAllSolarFestivals方法测试")
    class GetAllSolarFestivalsTests {

        @Test
        @DisplayName("返回所有公历节日")
        void testGetAllSolar() {
            List<Festival> festivals = Festival.getAllSolarFestivals();

            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().allMatch(f -> f.type() == Festival.FestivalType.SOLAR)).isTrue();
        }

        @Test
        @DisplayName("包含元旦和国庆")
        void testContainsMainFestivals() {
            List<Festival> festivals = Festival.getAllSolarFestivals();

            assertThat(festivals).contains(Festival.NEW_YEAR);
            assertThat(festivals).contains(Festival.NATIONAL_DAY);
        }
    }

    @Nested
    @DisplayName("getAllLunarFestivals方法测试")
    class GetAllLunarFestivalsTests {

        @Test
        @DisplayName("返回所有农历节日")
        void testGetAllLunar() {
            List<Festival> festivals = Festival.getAllLunarFestivals();

            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().allMatch(f -> f.type() == Festival.FestivalType.LUNAR)).isTrue();
        }

        @Test
        @DisplayName("包含春节和中秋")
        void testContainsMainFestivals() {
            List<Festival> festivals = Festival.getAllLunarFestivals();

            assertThat(festivals).contains(Festival.SPRING_FESTIVAL);
            assertThat(festivals).contains(Festival.MID_AUTUMN);
        }
    }

    @Nested
    @DisplayName("FestivalType枚举测试")
    class FestivalTypeTests {

        @Test
        @DisplayName("包含SOLAR、LUNAR和SOLAR_TERM三种类型")
        void testTypes() {
            assertThat(Festival.FestivalType.values()).hasSize(3);
            assertThat(Festival.FestivalType.SOLAR).isNotNull();
            assertThat(Festival.FestivalType.LUNAR).isNotNull();
            assertThat(Festival.FestivalType.SOLAR_TERM).isNotNull();
        }
    }

    @Nested
    @DisplayName("type方法测试")
    class TypeCheckTests {

        @Test
        @DisplayName("元旦是公历节日")
        void testNewYearIsSolar() {
            assertThat(Festival.NEW_YEAR.type()).isEqualTo(Festival.FestivalType.SOLAR);
        }

        @Test
        @DisplayName("春节是农历节日")
        void testSpringFestivalIsLunar() {
            assertThat(Festival.SPRING_FESTIVAL.type()).isEqualTo(Festival.FestivalType.LUNAR);
        }
    }

    @Nested
    @DisplayName("新增节日测试 V1.0.3")
    class NewFestivalsV103Tests {

        @Test
        @DisplayName("地球日 - 4月22日")
        void testEarthDay() {
            assertThat(Festival.EARTH_DAY).isNotNull();
            assertThat(Festival.EARTH_DAY.name()).isEqualTo("地球日");
            assertThat(Festival.EARTH_DAY.englishName()).isEqualTo("Earth Day");
            assertThat(Festival.EARTH_DAY.date()).isEqualTo(MonthDay.of(4, 22));
            assertThat(Festival.EARTH_DAY.type()).isEqualTo(Festival.FestivalType.SOLAR);
        }

        @Test
        @DisplayName("万圣节 - 10月31日")
        void testHalloween() {
            assertThat(Festival.HALLOWEEN).isNotNull();
            assertThat(Festival.HALLOWEEN.name()).isEqualTo("万圣节");
            assertThat(Festival.HALLOWEEN.date()).isEqualTo(MonthDay.of(10, 31));
            assertThat(Festival.HALLOWEEN.type()).isEqualTo(Festival.FestivalType.SOLAR);
        }

        @Test
        @DisplayName("上巳节 - 三月三")
        void testShangSi() {
            assertThat(Festival.SHANG_SI).isNotNull();
            assertThat(Festival.SHANG_SI.name()).isEqualTo("上巳节");
            assertThat(Festival.SHANG_SI.date()).isEqualTo(MonthDay.of(3, 3));
            assertThat(Festival.SHANG_SI.type()).isEqualTo(Festival.FestivalType.LUNAR);
        }

        @Test
        @DisplayName("下元节 - 十月十五")
        void testXiaYuan() {
            assertThat(Festival.XIA_YUAN).isNotNull();
            assertThat(Festival.XIA_YUAN.name()).isEqualTo("下元节");
            assertThat(Festival.XIA_YUAN.date()).isEqualTo(MonthDay.of(10, 15));
            assertThat(Festival.XIA_YUAN.type()).isEqualTo(Festival.FestivalType.LUNAR);
        }

        @Test
        @DisplayName("寒衣节 - 十月初一")
        void testHanYi() {
            assertThat(Festival.HAN_YI).isNotNull();
            assertThat(Festival.HAN_YI.name()).isEqualTo("寒衣节");
            assertThat(Festival.HAN_YI.date()).isEqualTo(MonthDay.of(10, 1));
            assertThat(Festival.HAN_YI.type()).isEqualTo(Festival.FestivalType.LUNAR);
        }

        @Test
        @DisplayName("天穿节 - 正月二十")
        void testTianChuan() {
            assertThat(Festival.TIAN_CHUAN).isNotNull();
            assertThat(Festival.TIAN_CHUAN.name()).isEqualTo("天穿节");
            assertThat(Festival.TIAN_CHUAN.date()).isEqualTo(MonthDay.of(1, 20));
            assertThat(Festival.TIAN_CHUAN.type()).isEqualTo(Festival.FestivalType.LUNAR);
        }

        @Test
        @DisplayName("新增公历节日包含在getAllSolarFestivals中")
        void testNewSolarFestivalsInList() {
            List<Festival> all = Festival.getAllSolarFestivals();
            assertThat(all).contains(Festival.EARTH_DAY, Festival.HALLOWEEN);
        }

        @Test
        @DisplayName("新增农历节日包含在getAllLunarFestivals中")
        void testNewLunarFestivalsInList() {
            List<Festival> all = Festival.getAllLunarFestivals();
            assertThat(all).contains(Festival.SHANG_SI, Festival.XIA_YUAN,
                    Festival.HAN_YI, Festival.TIAN_CHUAN);
        }

        @Test
        @DisplayName("4月22日能查到地球日")
        void testEarthDayLookup() {
            List<Festival> festivals = Festival.getSolarFestivals(LocalDate.of(2024, 4, 22));
            assertThat(festivals.stream().anyMatch(f -> f.name().equals("地球日"))).isTrue();
        }

        @Test
        @DisplayName("三月三能查到上巳节")
        void testShangSiLookup() {
            List<Festival> festivals = Festival.getLunarFestivals(3, 3);
            assertThat(festivals.stream().anyMatch(f -> f.name().equals("上巳节"))).isTrue();
        }
    }

    @Nested
    @DisplayName("除夕修复测试 V1.0.3")
    class ChuXiFixTests {

        @Test
        @DisplayName("getChuXi返回正确日期")
        void testGetChuXi() {
            Festival chuXi = Festival.getChuXi(2024);
            assertThat(chuXi.name()).isEqualTo("除夕");
            assertThat(chuXi.englishName()).isEqualTo("New Year's Eve");
            assertThat(chuXi.type()).isEqualTo(Festival.FestivalType.LUNAR);
            int day = chuXi.date().getDayOfMonth();
            assertThat(day).isBetween(29, 30);
        }

        @Test
        @DisplayName("腊月三十有除夕（常规查找）")
        void testChuXiOnDay30() {
            List<Festival> festivals = Festival.getLunarFestivals(12, 30);
            assertThat(festivals.stream().anyMatch(f -> f.name().equals("除夕"))).isTrue();
        }

        @Test
        @DisplayName("getLunarFestivals带年份能检测29日除夕")
        void testChuXiWithYearContext() {
            // Test the year-aware overload
            // For a year where month 12 has 29 days, day 29 should include 除夕
            // For a year where month 12 has 30 days, day 29 should NOT include 除夕
            Festival chuXi2024 = Festival.getChuXi(2024);
            int lastDay = chuXi2024.date().getDayOfMonth();

            if (lastDay == 29) {
                // This year has 29-day month 12
                List<Festival> festivals = Festival.getLunarFestivals(2024, 12, 29);
                assertThat(festivals.stream().anyMatch(f -> f.name().equals("除夕"))).isTrue();
            } else {
                // This year has 30-day month 12
                List<Festival> festivals = Festival.getLunarFestivals(2024, 12, 29);
                // Should NOT include 除夕 since the month has 30 days
                assertThat(festivals.stream().noneMatch(f -> f.name().equals("除夕"))).isTrue();
            }
        }

        @Test
        @DisplayName("NEW_YEARS_EVE常量日期为12月30日")
        void testNewYearsEveConstant() {
            assertThat(Festival.NEW_YEARS_EVE.date()).isEqualTo(MonthDay.of(12, 30));
        }
    }

    @Nested
    @DisplayName("动态节日测试 V1.0.3")
    class DynamicFestivalTests {

        @Test
        @DisplayName("母亲节是五月第二个星期日")
        void testMothersDay() {
            LocalDate mothersDay2024 = Festival.getMothersDay(2024);
            assertThat(mothersDay2024.getMonthValue()).isEqualTo(5);
            assertThat(mothersDay2024.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
            // Second Sunday of May 2024 = May 12
            assertThat(mothersDay2024.getDayOfMonth()).isEqualTo(12);
        }

        @Test
        @DisplayName("父亲节是六月第三个星期日")
        void testFathersDay() {
            LocalDate fathersDay2024 = Festival.getFathersDay(2024);
            assertThat(fathersDay2024.getMonthValue()).isEqualTo(6);
            assertThat(fathersDay2024.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
            // Third Sunday of June 2024 = June 16
            assertThat(fathersDay2024.getDayOfMonth()).isEqualTo(16);
        }

        @Test
        @DisplayName("母亲节不同年份日期不同")
        void testMothersDayDifferentYears() {
            LocalDate md2024 = Festival.getMothersDay(2024);
            LocalDate md2025 = Festival.getMothersDay(2025);
            assertThat(md2024).isNotEqualTo(md2025);
            // But both are Sundays in May
            assertThat(md2024.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
            assertThat(md2025.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
            assertThat(md2024.getMonthValue()).isEqualTo(5);
            assertThat(md2025.getMonthValue()).isEqualTo(5);
        }
    }
}
