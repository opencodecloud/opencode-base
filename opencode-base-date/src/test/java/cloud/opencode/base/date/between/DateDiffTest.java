package cloud.opencode.base.date.between;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import static org.assertj.core.api.Assertions.*;

/**
 * DateDiff 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("DateDiff 测试")
class DateDiffTest {

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("of(LocalDate, LocalDate) 从日期创建")
        void testOfLocalDate() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 3, 15);
            DateDiff diff = DateDiff.of(start, end);

            assertThat(diff.getStart()).isEqualTo(start);
            assertThat(diff.getEnd()).isEqualTo(end);
            assertThat(diff.getMonths()).isEqualTo(2);
            assertThat(diff.getDays()).isEqualTo(14);
        }

        @Test
        @DisplayName("of(LocalDate, LocalDate) 负差异")
        void testOfLocalDateNegative() {
            LocalDate start = LocalDate.of(2024, 3, 15);
            LocalDate end = LocalDate.of(2024, 1, 1);
            DateDiff diff = DateDiff.of(start, end);

            assertThat(diff.isNegative()).isTrue();
            assertThat(diff.getMonths()).isEqualTo(2);
            assertThat(diff.getDays()).isEqualTo(14);
        }

        @Test
        @DisplayName("of(LocalDateTime, LocalDateTime) 从日期时间创建")
        void testOfLocalDateTime() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 30, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 1, 14, 45, 30);
            DateDiff diff = DateDiff.of(start, end);

            assertThat(diff.getHours()).isEqualTo(4);
            assertThat(diff.getMinutes()).isEqualTo(15);
            assertThat(diff.getSeconds()).isEqualTo(30);
        }

        @Test
        @DisplayName("of(LocalDateTime, LocalDateTime) 跨天")
        void testOfLocalDateTimeCrossDay() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 3, 14, 30);
            DateDiff diff = DateDiff.of(start, end);

            assertThat(diff.getDays()).isEqualTo(2);
            assertThat(diff.getHours()).isEqualTo(4);
            assertThat(diff.getMinutes()).isEqualTo(30);
        }

        @Test
        @DisplayName("of(Temporal, Temporal) 混合类型")
        void testOfMixedTypes() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDateTime end = LocalDateTime.of(2024, 1, 3, 12, 0);
            DateDiff diff = DateDiff.of(start, end);

            assertThat(diff.getDays()).isEqualTo(2);
            assertThat(diff.getHours()).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("getYears() 获取年数")
        void testGetYears() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2024, 6, 15)
            );

            assertThat(diff.getYears()).isEqualTo(4);
        }

        @Test
        @DisplayName("getMonths() 获取月数")
        void testGetMonths() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 4, 15)
            );

            assertThat(diff.getMonths()).isEqualTo(3);
        }

        @Test
        @DisplayName("getDays() 获取天数")
        void testGetDays() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 15)
            );

            assertThat(diff.getDays()).isEqualTo(14);
        }

        @Test
        @DisplayName("getHours() 获取小时数")
        void testGetHours() {
            DateDiff diff = DateDiff.of(
                    LocalDateTime.of(2024, 1, 1, 10, 0),
                    LocalDateTime.of(2024, 1, 1, 15, 30)
            );

            assertThat(diff.getHours()).isEqualTo(5);
        }

        @Test
        @DisplayName("getMinutes() 获取分钟数")
        void testGetMinutes() {
            DateDiff diff = DateDiff.of(
                    LocalDateTime.of(2024, 1, 1, 10, 0),
                    LocalDateTime.of(2024, 1, 1, 10, 45)
            );

            assertThat(diff.getMinutes()).isEqualTo(45);
        }

        @Test
        @DisplayName("getSeconds() 获取秒数")
        void testGetSeconds() {
            DateDiff diff = DateDiff.of(
                    LocalDateTime.of(2024, 1, 1, 10, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 10, 0, 30)
            );

            assertThat(diff.getSeconds()).isEqualTo(30);
        }

        @Test
        @DisplayName("isNegative() 正差异")
        void testIsNegativeFalse() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 15)
            );

            assertThat(diff.isNegative()).isFalse();
        }

        @Test
        @DisplayName("isNegative() 负差异")
        void testIsNegativeTrue() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 15),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(diff.isNegative()).isTrue();
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toTotalDays() 转换为总天数")
        void testToTotalDays() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 11)
            );

            assertThat(diff.toTotalDays()).isEqualTo(10);
        }

        @Test
        @DisplayName("toPeriod() 转换为Period")
        void testToPeriod() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 3, 15)
            );

            Period period = diff.toPeriod();

            assertThat(period.getMonths()).isEqualTo(2);
            assertThat(period.getDays()).isEqualTo(14);
        }

        @Test
        @DisplayName("toPeriod() 负差异返回负Period")
        void testToPeriodNegative() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 3, 15),
                    LocalDate.of(2024, 1, 1)
            );

            Period period = diff.toPeriod();

            assertThat(period.isNegative()).isTrue();
        }
    }

    @Nested
    @DisplayName("格式化测试")
    class FormattingTests {

        @Test
        @DisplayName("format() 英文格式化")
        void testFormat() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2026, 3, 15)
            );

            String formatted = diff.format();

            assertThat(formatted).contains("2 years");
            assertThat(formatted).contains("2 months");
            assertThat(formatted).contains("14 days");
        }

        @Test
        @DisplayName("format() 包含时间部分")
        void testFormatWithTime() {
            DateDiff diff = DateDiff.of(
                    LocalDateTime.of(2024, 1, 1, 10, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 15, 30, 45)
            );

            String formatted = diff.format();

            assertThat(formatted).contains("5 hours");
            assertThat(formatted).contains("30 minutes");
            assertThat(formatted).contains("45 seconds");
        }

        @Test
        @DisplayName("format() 负差异")
        void testFormatNegative() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 3, 15),
                    LocalDate.of(2024, 1, 1)
            );

            String formatted = diff.format();

            assertThat(formatted).startsWith("-");
        }

        @Test
        @DisplayName("format() 零差异")
        void testFormatZero() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 1)
            );

            String formatted = diff.format();

            assertThat(formatted).isEqualTo("0 days");
        }

        @Test
        @DisplayName("format() 单数形式")
        void testFormatSingular() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2025, 2, 2)
            );

            String formatted = diff.format();

            assertThat(formatted).contains("1 year");
            assertThat(formatted).contains("1 month");
            assertThat(formatted).contains("1 day");
        }

        @Test
        @DisplayName("formatChinese() 中文格式化")
        void testFormatChinese() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2026, 3, 15)
            );

            String formatted = diff.formatChinese();

            assertThat(formatted).contains("2年");
            assertThat(formatted).contains("2个月");
            assertThat(formatted).contains("14天");
        }

        @Test
        @DisplayName("formatChinese() 包含时间部分")
        void testFormatChineseWithTime() {
            DateDiff diff = DateDiff.of(
                    LocalDateTime.of(2024, 1, 1, 10, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 15, 30, 45)
            );

            String formatted = diff.formatChinese();

            assertThat(formatted).contains("5小时");
            assertThat(formatted).contains("30分钟");
            assertThat(formatted).contains("45秒");
        }

        @Test
        @DisplayName("formatChinese() 负差异")
        void testFormatChineseNegative() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 3, 15),
                    LocalDate.of(2024, 1, 1)
            );

            String formatted = diff.formatChinese();

            assertThat(formatted).startsWith("负");
        }

        @Test
        @DisplayName("formatChinese() 零差异")
        void testFormatChineseZero() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 1)
            );

            String formatted = diff.formatChinese();

            assertThat(formatted).isEqualTo("0天");
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            DateDiff diff1 = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 3, 15)
            );
            DateDiff diff2 = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 3, 15)
            );

            assertThat(diff1).isEqualTo(diff2);
            assertThat(diff1.hashCode()).isEqualTo(diff2.hashCode());
        }

        @Test
        @DisplayName("toString() 等于format()")
        void testToString() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 3, 15)
            );

            assertThat(diff.toString()).isEqualTo(diff.format());
        }
    }

    @Nested
    @DisplayName("null安全性测试")
    class NullSafetyTests {

        @Test
        @DisplayName("of(LocalDate, LocalDate) null start抛出异常")
        void testOfNullStart() {
            assertThatThrownBy(() -> DateDiff.of((LocalDate) null, LocalDate.now()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of(LocalDate, LocalDate) null end抛出异常")
        void testOfNullEnd() {
            assertThatThrownBy(() -> DateDiff.of(LocalDate.now(), (LocalDate) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of(LocalDateTime, LocalDateTime) null start抛出异常")
        void testOfLocalDateTimeNullStart() {
            assertThatThrownBy(() -> DateDiff.of((LocalDateTime) null, LocalDateTime.now()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of(LocalDateTime, LocalDateTime) null end抛出异常")
        void testOfLocalDateTimeNullEnd() {
            assertThatThrownBy(() -> DateDiff.of(LocalDateTime.now(), (LocalDateTime) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("跨年差异")
        void testCrossYearDiff() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2023, 12, 31),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(diff.getYears()).isEqualTo(0);
            assertThat(diff.getMonths()).isEqualTo(0);
            assertThat(diff.getDays()).isEqualTo(1);
        }

        @Test
        @DisplayName("闰年2月29日差异")
        void testLeapYearDiff() {
            DateDiff diff = DateDiff.of(
                    LocalDate.of(2024, 2, 28),
                    LocalDate.of(2024, 3, 1)
            );

            assertThat(diff.getDays()).isEqualTo(2);
        }
    }
}
