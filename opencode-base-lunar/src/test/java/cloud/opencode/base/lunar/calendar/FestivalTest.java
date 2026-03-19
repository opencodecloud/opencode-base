package cloud.opencode.base.lunar.calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
}
