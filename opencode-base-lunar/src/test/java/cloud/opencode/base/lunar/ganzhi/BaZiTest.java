package cloud.opencode.base.lunar.ganzhi;

import cloud.opencode.base.lunar.calendar.SolarTerm;
import cloud.opencode.base.lunar.element.WuXing;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * BaZi (八字/四柱命理) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.3
 */
@DisplayName("BaZi (八字/四柱命理) 测试")
class BaZiTest {

    @Nested
    @DisplayName("Record基本测试")
    class RecordBasicTests {

        @Test
        @DisplayName("创建BaZi记录")
        void testCreate() {
            GanZhi year = GanZhi.ofYear(2024);
            GanZhi month = GanZhi.ofMonth(2024, 1);
            GanZhi day = GanZhi.ofDay(LocalDate.of(2024, 2, 4));
            GanZhi hour = GanZhi.ofHour(day, 14);

            BaZi bazi = new BaZi(year, month, day, hour);
            assertThat(bazi.yearPillar()).isEqualTo(year);
            assertThat(bazi.monthPillar()).isEqualTo(month);
            assertThat(bazi.dayPillar()).isEqualTo(day);
            assertThat(bazi.hourPillar()).isEqualTo(hour);
        }

        @Test
        @DisplayName("null参数抛出NullPointerException")
        void testNullValidation() {
            GanZhi gz = GanZhi.ofYear(2024);
            assertThatThrownBy(() -> new BaZi(null, gz, gz, gz))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new BaZi(gz, null, gz, gz))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new BaZi(gz, gz, null, gz))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new BaZi(gz, gz, gz, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("of(LocalDate, int)方法测试")
    class OfDateHourTests {

        @Test
        @DisplayName("null日期抛出NullPointerException")
        void testNullDate() {
            assertThatThrownBy(() -> BaZi.of((LocalDate) null, 12))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("无效小时抛出IllegalArgumentException")
        void testInvalidHour() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            assertThatThrownBy(() -> BaZi.of(date, -1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> BaZi.of(date, 24))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("返回的BaZi四柱不为null")
        void testAllPillarsNotNull() {
            BaZi bazi = BaZi.of(LocalDate.of(2024, 6, 15), 12);
            assertThat(bazi.yearPillar()).isNotNull();
            assertThat(bazi.monthPillar()).isNotNull();
            assertThat(bazi.dayPillar()).isNotNull();
            assertThat(bazi.hourPillar()).isNotNull();
        }
    }

    @Nested
    @DisplayName("年柱立春边界测试")
    class YearPillarLiChunBoundaryTests {

        @Test
        @DisplayName("2024年立春当天年柱为甲辰")
        void testOnLiChun2024() {
            // 2024 is 甲辰 year, 立春 is approx Feb 4
            LocalDate liChun2024 = SolarTerm.LI_CHUN.getDate(2024);
            BaZi bazi = BaZi.of(liChun2024, 12);
            // After 立春, year should be 2024 -> 甲辰
            assertThat(bazi.yearPillar().gan()).isEqualTo(Gan.JIA);
            assertThat(bazi.yearPillar().zhi()).isEqualTo(Zhi.CHEN);
            assertThat(bazi.yearPillar().getName()).isEqualTo("甲辰");
        }

        @Test
        @DisplayName("2024年立春前一天年柱为癸卯")
        void testBeforeLiChun2024() {
            // Before 立春 of 2024, year should be 2023 -> 癸卯
            LocalDate liChun2024 = SolarTerm.LI_CHUN.getDate(2024);
            LocalDate beforeLiChun = liChun2024.minusDays(1);
            BaZi bazi = BaZi.of(beforeLiChun, 12);
            assertThat(bazi.yearPillar().gan()).isEqualTo(Gan.GUI);
            assertThat(bazi.yearPillar().zhi()).isEqualTo(Zhi.MAO);
            assertThat(bazi.yearPillar().getName()).isEqualTo("癸卯");
        }

        @Test
        @DisplayName("2025年1月属于甲辰年（立春前）")
        void testJan2025BeforeLiChun() {
            LocalDate jan2025 = LocalDate.of(2025, 1, 15);
            LocalDate liChun2025 = SolarTerm.LI_CHUN.getDate(2025);
            if (jan2025.isBefore(liChun2025)) {
                BaZi bazi = BaZi.of(jan2025, 12);
                // Before 立春 2025, should use 2024 year -> 甲辰
                assertThat(bazi.yearPillar().getName()).isEqualTo("甲辰");
            }
        }
    }

    @Nested
    @DisplayName("月柱测试")
    class MonthPillarTests {

        @Test
        @DisplayName("立春后月柱地支为寅")
        void testMonthAfterLiChun() {
            // After 立春, in month 1 (寅月)
            LocalDate liChun = SolarTerm.LI_CHUN.getDate(2024);
            BaZi bazi = BaZi.of(liChun, 12);
            // Month pillar branch should be 寅 (YIN)
            assertThat(bazi.monthPillar().zhi()).isEqualTo(Zhi.YIN);
        }

        @Test
        @DisplayName("惊蛰后月柱地支为卯")
        void testMonthAfterJingZhe() {
            LocalDate jingZhe = SolarTerm.JING_ZHE.getDate(2024);
            BaZi bazi = BaZi.of(jingZhe, 12);
            // After 惊蛰, in month 2 (卯月)
            assertThat(bazi.monthPillar().zhi()).isEqualTo(Zhi.MAO);
        }

        @Test
        @DisplayName("清明后月柱地支为辰")
        void testMonthAfterQingMing() {
            LocalDate qingMing = SolarTerm.QING_MING.getDate(2024);
            BaZi bazi = BaZi.of(qingMing, 12);
            // After 清明, in month 3 (辰月)
            assertThat(bazi.monthPillar().zhi()).isEqualTo(Zhi.CHEN);
        }

        @Test
        @DisplayName("立夏后月柱地支为巳")
        void testMonthAfterLiXia() {
            LocalDate liXia = SolarTerm.LI_XIA.getDate(2024);
            BaZi bazi = BaZi.of(liXia, 12);
            assertThat(bazi.monthPillar().zhi()).isEqualTo(Zhi.SI);
        }
    }

    @Nested
    @DisplayName("日柱测试")
    class DayPillarTests {

        @Test
        @DisplayName("日柱与GanZhi.ofDay一致")
        void testDayPillarMatchesGanZhi() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            BaZi bazi = BaZi.of(date, 12);
            GanZhi expected = GanZhi.ofDay(date);
            assertThat(bazi.dayPillar()).isEqualTo(expected);
        }

        @Test
        @DisplayName("不同日期有不同日柱")
        void testDifferentDays() {
            BaZi bazi1 = BaZi.of(LocalDate.of(2024, 6, 15), 12);
            BaZi bazi2 = BaZi.of(LocalDate.of(2024, 6, 16), 12);
            assertThat(bazi1.dayPillar()).isNotEqualTo(bazi2.dayPillar());
        }
    }

    @Nested
    @DisplayName("时柱测试")
    class HourPillarTests {

        @Test
        @DisplayName("子时（23-1点）地支为子")
        void testZiHour() {
            BaZi bazi = BaZi.of(LocalDate.of(2024, 6, 15), 0);
            assertThat(bazi.hourPillar().zhi()).isEqualTo(Zhi.ZI);
        }

        @Test
        @DisplayName("午时（11-13点）地支为午")
        void testWuHour() {
            BaZi bazi = BaZi.of(LocalDate.of(2024, 6, 15), 12);
            assertThat(bazi.hourPillar().zhi()).isEqualTo(Zhi.WU);
        }

        @Test
        @DisplayName("不同时辰有不同时柱")
        void testDifferentHours() {
            BaZi bazi1 = BaZi.of(LocalDate.of(2024, 6, 15), 0);
            BaZi bazi2 = BaZi.of(LocalDate.of(2024, 6, 15), 12);
            assertThat(bazi1.hourPillar()).isNotEqualTo(bazi2.hourPillar());
        }
    }

    @Nested
    @DisplayName("of(LocalDateTime)方法测试")
    class OfDateTimeTests {

        @Test
        @DisplayName("null参数抛出NullPointerException")
        void testNullDateTime() {
            assertThatThrownBy(() -> BaZi.of((LocalDateTime) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("与of(LocalDate, int)结果一致")
        void testConsistentWithDateHour() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 14, 30);
            BaZi bazi1 = BaZi.of(dateTime);
            BaZi bazi2 = BaZi.of(dateTime.toLocalDate(), dateTime.getHour());
            assertThat(bazi1).isEqualTo(bazi2);
        }
    }

    @Nested
    @DisplayName("getDayMaster方法测试")
    class GetDayMasterTests {

        @Test
        @DisplayName("日主五行不为null")
        void testDayMasterNotNull() {
            BaZi bazi = BaZi.of(LocalDate.of(2024, 6, 15), 12);
            WuXing dayMaster = bazi.getDayMaster();
            assertThat(dayMaster).isNotNull();
        }

        @Test
        @DisplayName("日主五行与日干一致")
        void testDayMasterMatchesDayStem() {
            BaZi bazi = BaZi.of(LocalDate.of(2024, 6, 15), 12);
            WuXing expected = WuXing.fromGan(bazi.dayPillar().gan().ordinal());
            assertThat(bazi.getDayMaster()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("format方法测试")
    class FormatTests {

        @Test
        @DisplayName("format返回四柱空格分隔")
        void testFormat() {
            BaZi bazi = BaZi.of(LocalDate.of(2024, 6, 15), 12);
            String formatted = bazi.format();
            String[] parts = formatted.split(" ");
            assertThat(parts).hasSize(4);
            // Each part should be 2 characters (干+支)
            for (String part : parts) {
                assertThat(part).hasSize(2);
            }
        }

        @Test
        @DisplayName("formatWithLabels包含年月日时柱标签")
        void testFormatWithLabels() {
            BaZi bazi = BaZi.of(LocalDate.of(2024, 6, 15), 12);
            String formatted = bazi.formatWithLabels();
            assertThat(formatted).contains("年柱:");
            assertThat(formatted).contains("月柱:");
            assertThat(formatted).contains("日柱:");
            assertThat(formatted).contains("时柱:");
        }

        @Test
        @DisplayName("toString与format一致")
        void testToString() {
            BaZi bazi = BaZi.of(LocalDate.of(2024, 6, 15), 12);
            assertThat(bazi.toString()).isEqualTo(bazi.format());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("0点和23点都能正常计算")
        void testHourBoundaries() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            assertThatCode(() -> BaZi.of(date, 0)).doesNotThrowAnyException();
            assertThatCode(() -> BaZi.of(date, 23)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("年初和年末都能正常计算")
        void testYearBoundaries() {
            assertThatCode(() -> BaZi.of(LocalDate.of(2024, 1, 1), 12)).doesNotThrowAnyException();
            assertThatCode(() -> BaZi.of(LocalDate.of(2024, 12, 31), 12)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("不同年份都能正常计算")
        void testVariousYears() {
            assertThatCode(() -> BaZi.of(LocalDate.of(1950, 6, 15), 12)).doesNotThrowAnyException();
            assertThatCode(() -> BaZi.of(LocalDate.of(2000, 6, 15), 12)).doesNotThrowAnyException();
            assertThatCode(() -> BaZi.of(LocalDate.of(2050, 6, 15), 12)).doesNotThrowAnyException();
        }
    }
}
