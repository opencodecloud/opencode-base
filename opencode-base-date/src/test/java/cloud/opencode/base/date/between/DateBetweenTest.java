package cloud.opencode.base.date.between;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import static org.assertj.core.api.Assertions.*;

/**
 * DateBetween 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("DateBetween 测试")
class DateBetweenTest {

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("of(LocalDate, LocalDate) 创建日期差异")
        void testOfLocalDate() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 12, 31);
            DateBetween between = DateBetween.of(start, end);

            assertThat(between.getStart()).isEqualTo(start);
            assertThat(between.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("of(LocalDateTime, LocalDateTime) 创建日期时间差异")
        void testOfLocalDateTime() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 9, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 1, 17, 0);
            DateBetween between = DateBetween.of(start, end);

            assertThat(between.getStart()).isEqualTo(start);
            assertThat(between.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("between() 通用创建")
        void testBetween() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 12, 31);
            DateBetween between = DateBetween.between(start, end);

            assertThat(between.days()).isEqualTo(365);
        }
    }

    @Nested
    @DisplayName("天数计算测试")
    class DayCalculationTests {

        @Test
        @DisplayName("days() 计算天数")
        void testDays() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 11)
            );

            assertThat(between.days()).isEqualTo(10);
        }

        @Test
        @DisplayName("days() 负数差异")
        void testDaysNegative() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 1, 11),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(between.days()).isEqualTo(-10);
        }

        @Test
        @DisplayName("absDays() 绝对天数")
        void testAbsDays() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 1, 11),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(between.absDays()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("周数计算测试")
    class WeekCalculationTests {

        @Test
        @DisplayName("weeks() 计算周数")
        void testWeeks() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 22)
            );

            assertThat(between.weeks()).isEqualTo(3);
        }

        @Test
        @DisplayName("absWeeks() 绝对周数")
        void testAbsWeeks() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 1, 22),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(between.absWeeks()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("月数计算测试")
    class MonthCalculationTests {

        @Test
        @DisplayName("months() 计算月数")
        void testMonths() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 4, 1)
            );

            assertThat(between.months()).isEqualTo(3);
        }

        @Test
        @DisplayName("absMonths() 绝对月数")
        void testAbsMonths() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 4, 1),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(between.absMonths()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("年数计算测试")
    class YearCalculationTests {

        @Test
        @DisplayName("years() 计算年数")
        void testYears() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(between.years()).isEqualTo(4);
        }

        @Test
        @DisplayName("absYears() 绝对年数")
        void testAbsYears() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2020, 1, 1)
            );

            assertThat(between.absYears()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("时间计算测试")
    class TimeCalculationTests {

        @Test
        @DisplayName("hours() 计算小时数")
        void testHours() {
            DateBetween between = DateBetween.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(between.hours()).isEqualTo(8);
        }

        @Test
        @DisplayName("minutes() 计算分钟数")
        void testMinutes() {
            DateBetween between = DateBetween.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 10, 30)
            );

            assertThat(between.minutes()).isEqualTo(90);
        }

        @Test
        @DisplayName("seconds() 计算秒数")
        void testSeconds() {
            DateBetween between = DateBetween.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 9, 1, 30)
            );

            assertThat(between.seconds()).isEqualTo(90);
        }

        @Test
        @DisplayName("millis() 计算毫秒数")
        void testMillis() {
            DateBetween between = DateBetween.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 9, 0, 1)
            );

            assertThat(between.millis()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("Period/Duration转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toPeriod() 从LocalDate转换")
        void testToPeriodFromLocalDate() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 3, 15)
            );

            Period period = between.toPeriod();

            assertThat(period.getYears()).isEqualTo(0);
            assertThat(period.getMonths()).isEqualTo(2);
            assertThat(period.getDays()).isEqualTo(14);
        }

        @Test
        @DisplayName("toPeriod() 从LocalDateTime转换")
        void testToPeriodFromLocalDateTime() {
            DateBetween between = DateBetween.of(
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    LocalDateTime.of(2024, 3, 15, 0, 0)
            );

            Period period = between.toPeriod();

            assertThat(period.getMonths()).isEqualTo(2);
        }

        @Test
        @DisplayName("toDuration() 从LocalDateTime转换")
        void testToDurationFromLocalDateTime() {
            DateBetween between = DateBetween.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            Duration duration = between.toDuration();

            assertThat(duration).isEqualTo(Duration.ofHours(8));
        }

        @Test
        @DisplayName("toDuration() 从LocalDate转换")
        void testToDurationFromLocalDate() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 2)
            );

            Duration duration = between.toDuration();

            assertThat(duration).isEqualTo(Duration.ofDays(1));
        }

        @Test
        @DisplayName("toDateDiff() 转换为详细差异")
        void testToDateDiff() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 3, 15)
            );

            DateDiff diff = between.toDateDiff();

            assertThat(diff).isNotNull();
            assertThat(diff.getMonths()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            DateBetween between1 = DateBetween.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );
            DateBetween between2 = DateBetween.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );

            assertThat(between1).isEqualTo(between2);
            assertThat(between1.hashCode()).isEqualTo(between2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            DateBetween between = DateBetween.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 11)
            );

            String str = between.toString();

            assertThat(str).contains("2024-01-01");
            assertThat(str).contains("2024-01-11");
            assertThat(str).contains("days=10");
        }
    }

    @Nested
    @DisplayName("null安全性测试")
    class NullSafetyTests {

        @Test
        @DisplayName("of() null start抛出异常")
        void testOfNullStart() {
            assertThatThrownBy(() -> DateBetween.of((LocalDate) null, LocalDate.now()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of() null end抛出异常")
        void testOfNullEnd() {
            assertThatThrownBy(() -> DateBetween.of(LocalDate.now(), (LocalDate) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
