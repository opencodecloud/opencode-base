package cloud.opencode.base.date.between;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * AgeDetail 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("AgeDetail 测试")
class AgeDetailTest {

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("of(AgeBetween) 从AgeBetween创建")
        void testOfAgeBetween() {
            AgeBetween ageBetween = AgeBetween.at(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );
            AgeDetail detail = AgeDetail.of(ageBetween);

            assertThat(detail.getAgeBetween()).isEqualTo(ageBetween);
        }

        @Test
        @DisplayName("of(LocalDate) 从出生日期创建")
        void testOfBirthDate() {
            LocalDate birthDate = LocalDate.of(1990, 5, 15);
            AgeDetail detail = AgeDetail.of(birthDate);

            assertThat(detail.getAgeBetween().getBirthDate()).isEqualTo(birthDate);
        }

        @Test
        @DisplayName("of(LocalDate, LocalDate) 从出生日期和参考日期创建")
        void testOfBirthAndReference() {
            LocalDate birthDate = LocalDate.of(1990, 5, 15);
            LocalDate referenceDate = LocalDate.of(2024, 5, 15);
            AgeDetail detail = AgeDetail.of(birthDate, referenceDate);

            assertThat(detail.getAgeBetween().getBirthDate()).isEqualTo(birthDate);
            assertThat(detail.getAgeBetween().getReferenceDate()).isEqualTo(referenceDate);
        }
    }

    @Nested
    @DisplayName("总时间获取测试")
    class TotalTimeTests {

        private final AgeDetail detail = AgeDetail.of(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 11)
        );

        @Test
        @DisplayName("getTotalDays() 获取总天数")
        void testGetTotalDays() {
            assertThat(detail.getTotalDays()).isEqualTo(10);
        }

        @Test
        @DisplayName("getTotalWeeks() 获取总周数")
        void testGetTotalWeeks() {
            assertThat(detail.getTotalWeeks()).isEqualTo(1);
        }

        @Test
        @DisplayName("getTotalMonths() 获取总月数")
        void testGetTotalMonths() {
            assertThat(detail.getTotalMonths()).isEqualTo(0);
        }

        @Test
        @DisplayName("getTotalHours() 获取总小时数")
        void testGetTotalHours() {
            assertThat(detail.getTotalHours()).isEqualTo(10 * 24);
        }

        @Test
        @DisplayName("getTotalMinutes() 获取总分钟数")
        void testGetTotalMinutes() {
            assertThat(detail.getTotalMinutes()).isEqualTo(10 * 24 * 60);
        }

        @Test
        @DisplayName("getTotalSeconds() 获取总秒数")
        void testGetTotalSeconds() {
            assertThat(detail.getTotalSeconds()).isEqualTo(10L * 24 * 60 * 60);
        }
    }

    @Nested
    @DisplayName("统计数据测试")
    class StatisticsTests {

        @Test
        @DisplayName("getTotalWeekends() 获取周末天数")
        void testGetTotalWeekends() {
            // 2024-01-01 to 2024-01-14 includes 2 weekends (4 weekend days)
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 14)
            );

            long weekends = detail.getTotalWeekends();

            assertThat(weekends).isGreaterThanOrEqualTo(4);
        }

        @Test
        @DisplayName("getEstimatedWeekends() 获取估计周末天数")
        void testGetEstimatedWeekends() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            long estimatedWeekends = detail.getEstimatedWeekends();

            // Should be approximately 30 * 2/7 = ~8
            assertThat(estimatedWeekends).isGreaterThanOrEqualTo(6);
        }

        @Test
        @DisplayName("getLeapYearsLived() 获取闰年数")
        void testGetLeapYearsLived() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );

            int leapYears = detail.getLeapYearsLived();

            // 2020 and 2024 are leap years
            assertThat(leapYears).isEqualTo(2);
        }

        @Test
        @DisplayName("getBirthdaysCelebrated() 获取庆祝的生日数")
        void testGetBirthdaysCelebrated() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            assertThat(detail.getBirthdaysCelebrated()).isEqualTo(34);
        }
    }

    @Nested
    @DisplayName("里程碑测试")
    class MilestoneTests {

        @Test
        @DisplayName("getNextMilestone() 获取下一个里程碑")
        void testGetNextMilestone() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(2010, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            // Age 14, next milestone is 16
            assertThat(detail.getNextMilestone()).isEqualTo(16);
        }

        @Test
        @DisplayName("getNextMilestone() 超过所有里程碑返回-1")
        void testGetNextMilestoneNone() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1920, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            // Age 104, no more milestones
            assertThat(detail.getNextMilestone()).isEqualTo(-1);
        }

        @Test
        @DisplayName("getNextMilestoneDate() 获取下一个里程碑日期")
        void testGetNextMilestoneDate() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(2010, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            LocalDate nextMilestoneDate = detail.getNextMilestoneDate();

            assertThat(nextMilestoneDate).isEqualTo(LocalDate.of(2026, 5, 15));
        }

        @Test
        @DisplayName("getNextMilestoneDate() 无里程碑返回null")
        void testGetNextMilestoneDateNone() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1920, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            assertThat(detail.getNextMilestoneDate()).isNull();
        }

        @Test
        @DisplayName("getDaysUntilNextMilestone() 获取距离下一个里程碑的天数")
        void testGetDaysUntilNextMilestone() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(2010, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            long days = detail.getDaysUntilNextMilestone();

            assertThat(days).isGreaterThan(0);
        }

        @Test
        @DisplayName("getDaysUntilNextMilestone() 无里程碑返回-1")
        void testGetDaysUntilNextMilestoneNone() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1920, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            assertThat(detail.getDaysUntilNextMilestone()).isEqualTo(-1);
        }

        @Test
        @DisplayName("getLastMilestone() 获取上一个里程碑")
        void testGetLastMilestone() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            // Age 34, last milestone is 30
            assertThat(detail.getLastMilestone()).isEqualTo(30);
        }

        @Test
        @DisplayName("getLastMilestone() 无通过里程碑返回-1")
        void testGetLastMilestoneNone() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(2024, 5, 15),
                    LocalDate.of(2024, 8, 15)
            );

            // Age 0, no milestone passed
            assertThat(detail.getLastMilestone()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("季节测试")
    class SeasonTests {

        @Test
        @DisplayName("getBirthSeason() 春季")
        void testGetBirthSeasonSpring() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 4, 15),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(detail.getBirthSeason()).isEqualTo("Spring");
        }

        @Test
        @DisplayName("getBirthSeason() 夏季")
        void testGetBirthSeasonSummer() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 7, 15),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(detail.getBirthSeason()).isEqualTo("Summer");
        }

        @Test
        @DisplayName("getBirthSeason() 秋季")
        void testGetBirthSeasonAutumn() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 10, 15),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(detail.getBirthSeason()).isEqualTo("Autumn");
        }

        @Test
        @DisplayName("getBirthSeason() 冬季")
        void testGetBirthSeasonWinter() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 1, 15),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(detail.getBirthSeason()).isEqualTo("Winter");
        }

        @Test
        @DisplayName("getBirthSeasonChinese() 中文季节")
        void testGetBirthSeasonChinese() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 4, 15),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(detail.getBirthSeasonChinese()).isEqualTo("春季");
        }
    }

    @Nested
    @DisplayName("预期寿命测试")
    class LifeExpectancyTests {

        @Test
        @DisplayName("getLifePercentage() 获取生命百分比")
        void testGetLifePercentage() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            // Age 34, life expectancy 80
            double percentage = detail.getLifePercentage(80);

            assertThat(percentage).isCloseTo(42.5, within(0.5));
        }

        @Test
        @DisplayName("getLifePercentage() 非正预期寿命抛出异常")
        void testGetLifePercentageInvalidThrows() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            assertThatThrownBy(() -> detail.getLifePercentage(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("getEstimatedRemainingYears() 获取估计剩余年数")
        void testGetEstimatedRemainingYears() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            // Age 34, life expectancy 80, remaining 46
            int remaining = detail.getEstimatedRemainingYears(80);

            assertThat(remaining).isEqualTo(46);
        }

        @Test
        @DisplayName("getEstimatedRemainingYears() 超过预期寿命返回0")
        void testGetEstimatedRemainingYearsZero() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1930, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            // Age 94, life expectancy 80, remaining 0
            int remaining = detail.getEstimatedRemainingYears(80);

            assertThat(remaining).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("格式化测试")
    class FormattingTests {

        @Test
        @DisplayName("toSummary() 获取摘要")
        void testToSummary() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            String summary = detail.toSummary();

            assertThat(summary).contains("Age Detail Summary");
            assertThat(summary).contains("34 years");
            assertThat(summary).contains("Total Days");
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            AgeDetail detail1 = AgeDetail.of(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );
            AgeDetail detail2 = AgeDetail.of(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            assertThat(detail1).isEqualTo(detail2);
            assertThat(detail1.hashCode()).isEqualTo(detail2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            AgeDetail detail = AgeDetail.of(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            String str = detail.toString();

            assertThat(str).contains("AgeDetail");
            assertThat(str).contains("34 years");
        }
    }

    @Nested
    @DisplayName("null安全性测试")
    class NullSafetyTests {

        @Test
        @DisplayName("of(AgeBetween) null抛出异常")
        void testOfAgeBetweenNull() {
            assertThatThrownBy(() -> AgeDetail.of((AgeBetween) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
