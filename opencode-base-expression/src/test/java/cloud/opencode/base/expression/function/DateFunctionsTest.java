package cloud.opencode.base.expression.function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * DateFunctions Tests
 * DateFunctions 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("DateFunctions Tests | DateFunctions 测试")
class DateFunctionsTest {

    private static Map<String, Function> functions;

    @BeforeAll
    static void setup() {
        functions = DateFunctions.getFunctions();
    }

    @Nested
    @DisplayName("Current Date/Time Tests | 当前日期/时间测试")
    class CurrentDateTimeTests {

        @Test
        @DisplayName("now function | now 函数")
        void testNow() {
            Function now = functions.get("now");
            Object result = now.apply();
            assertThat(result).isInstanceOf(LocalDateTime.class);
        }

        @Test
        @DisplayName("today function | today 函数")
        void testToday() {
            Function today = functions.get("today");
            Object result = today.apply();
            assertThat(result).isInstanceOf(LocalDate.class);
            assertThat(result).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("currenttime function | currenttime 函数")
        void testCurrentTime() {
            Function currenttime = functions.get("currenttime");
            Object result = currenttime.apply();
            assertThat(result).isInstanceOf(LocalTime.class);
        }

        @Test
        @DisplayName("timestamp function | timestamp 函数")
        void testTimestamp() {
            Function timestamp = functions.get("timestamp");
            Object result = timestamp.apply();
            assertThat(result).isInstanceOf(Long.class);
            assertThat((Long) result).isPositive();
        }
    }

    @Nested
    @DisplayName("Date Creation Tests | 日期创建测试")
    class DateCreationTests {

        @Test
        @DisplayName("date function with year, month, day | date 函数使用年、月、日")
        void testDateWithYearMonthDay() {
            Function date = functions.get("date");
            Object result = date.apply(2024, 1, 15);
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("date function with string | date 函数使用字符串")
        void testDateWithString() {
            Function date = functions.get("date");
            Object result = date.apply("2024-01-15");
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("date function no args | date 函数无参数")
        void testDateNoArgs() {
            Function date = functions.get("date");
            Object result = date.apply();
            assertThat(result).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("datetime function with all components | datetime 函数使用所有组件")
        void testDateTimeWithAllComponents() {
            Function datetime = functions.get("datetime");
            Object result = datetime.apply(2024, 1, 15, 10, 30, 45);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 45));
        }

        @Test
        @DisplayName("datetime function with date only | datetime 函数仅使用日期")
        void testDateTimeWithDateOnly() {
            Function datetime = functions.get("datetime");
            Object result = datetime.apply(2024, 1, 15);
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15).atStartOfDay());
        }

        @Test
        @DisplayName("time function with components | time 函数使用组件")
        void testTimeWithComponents() {
            Function time = functions.get("time");
            assertThat(time.apply(10, 30, 45)).isEqualTo(LocalTime.of(10, 30, 45));
            assertThat(time.apply(10, 30)).isEqualTo(LocalTime.of(10, 30));
        }
    }

    @Nested
    @DisplayName("Date Extraction Tests | 日期提取测试")
    class DateExtractionTests {

        private final LocalDate testDate = LocalDate.of(2024, 3, 15);

        @Test
        @DisplayName("year function | year 函数")
        void testYear() {
            Function year = functions.get("year");
            assertThat(year.apply(testDate)).isEqualTo(2024);
            assertThat(year.apply()).isEqualTo(LocalDate.now().getYear());
        }

        @Test
        @DisplayName("month function | month 函数")
        void testMonth() {
            Function month = functions.get("month");
            assertThat(month.apply(testDate)).isEqualTo(3);
        }

        @Test
        @DisplayName("day function | day 函数")
        void testDay() {
            Function day = functions.get("day");
            assertThat(day.apply(testDate)).isEqualTo(15);
        }

        @Test
        @DisplayName("dayofweek function | dayofweek 函数")
        void testDayOfWeek() {
            Function dayofweek = functions.get("dayofweek");
            assertThat(dayofweek.apply(testDate)).isEqualTo(testDate.getDayOfWeek().getValue());
        }

        @Test
        @DisplayName("dayofyear function | dayofyear 函数")
        void testDayOfYear() {
            Function dayofyear = functions.get("dayofyear");
            assertThat(dayofyear.apply(testDate)).isEqualTo(testDate.getDayOfYear());
        }

        @Test
        @DisplayName("hour function | hour 函数")
        void testHour() {
            Function hour = functions.get("hour");
            assertThat(hour.apply(LocalTime.of(14, 30))).isEqualTo(14);
        }

        @Test
        @DisplayName("minute function | minute 函数")
        void testMinute() {
            Function minute = functions.get("minute");
            assertThat(minute.apply(LocalTime.of(14, 30))).isEqualTo(30);
        }

        @Test
        @DisplayName("second function | second 函数")
        void testSecond() {
            Function second = functions.get("second");
            assertThat(second.apply(LocalTime.of(14, 30, 45))).isEqualTo(45);
        }
    }

    @Nested
    @DisplayName("Date Arithmetic Tests | 日期算术测试")
    class DateArithmeticTests {

        private final LocalDate testDate = LocalDate.of(2024, 1, 15);
        private final LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        @Test
        @DisplayName("adddays function | adddays 函数")
        void testAddDays() {
            Function adddays = functions.get("adddays");
            assertThat(adddays.apply(testDate, 5)).isEqualTo(LocalDate.of(2024, 1, 20));
            assertThat(adddays.apply(testDate, -5)).isEqualTo(LocalDate.of(2024, 1, 10));
            assertThat(adddays.apply((Object) null, 5)).isNull();
        }

        @Test
        @DisplayName("addmonths function | addmonths 函数")
        void testAddMonths() {
            Function addmonths = functions.get("addmonths");
            assertThat(addmonths.apply(testDate, 2)).isEqualTo(LocalDate.of(2024, 3, 15));
        }

        @Test
        @DisplayName("addyears function | addyears 函数")
        void testAddYears() {
            Function addyears = functions.get("addyears");
            assertThat(addyears.apply(testDate, 1)).isEqualTo(LocalDate.of(2025, 1, 15));
        }

        @Test
        @DisplayName("addhours function | addhours 函数")
        void testAddHours() {
            Function addhours = functions.get("addhours");
            assertThat(addhours.apply(testDateTime, 5)).isEqualTo(LocalDateTime.of(2024, 1, 15, 15, 30, 0));
        }

        @Test
        @DisplayName("addminutes function | addminutes 函数")
        void testAddMinutes() {
            Function addminutes = functions.get("addminutes");
            assertThat(addminutes.apply(testDateTime, 30)).isEqualTo(LocalDateTime.of(2024, 1, 15, 11, 0, 0));
        }

        @Test
        @DisplayName("addseconds function | addseconds 函数")
        void testAddSeconds() {
            Function addseconds = functions.get("addseconds");
            assertThat(addseconds.apply(testDateTime, 30)).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 30));
        }
    }

    @Nested
    @DisplayName("Date Difference Tests | 日期差异测试")
    class DateDifferenceTests {

        private final LocalDate date1 = LocalDate.of(2024, 1, 1);
        private final LocalDate date2 = LocalDate.of(2024, 1, 15);

        @Test
        @DisplayName("daysbetween function | daysbetween 函数")
        void testDaysBetween() {
            Function daysbetween = functions.get("daysbetween");
            assertThat(daysbetween.apply(date1, date2)).isEqualTo(14L);
            assertThat(daysbetween.apply((Object) null, date2)).isEqualTo(0L);
        }

        @Test
        @DisplayName("monthsbetween function | monthsbetween 函数")
        void testMonthsBetween() {
            Function monthsbetween = functions.get("monthsbetween");
            assertThat(monthsbetween.apply(date1, LocalDate.of(2024, 3, 1))).isEqualTo(2L);
        }

        @Test
        @DisplayName("yearsbetween function | yearsbetween 函数")
        void testYearsBetween() {
            Function yearsbetween = functions.get("yearsbetween");
            assertThat(yearsbetween.apply(date1, LocalDate.of(2026, 1, 1))).isEqualTo(2L);
        }

        @Test
        @DisplayName("hoursbetween function | hoursbetween 函数")
        void testHoursBetween() {
            Function hoursbetween = functions.get("hoursbetween");
            LocalDateTime dt1 = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime dt2 = LocalDateTime.of(2024, 1, 1, 5, 0);
            assertThat(hoursbetween.apply(dt1, dt2)).isEqualTo(5L);
        }

        @Test
        @DisplayName("minutesbetween function | minutesbetween 函数")
        void testMinutesBetween() {
            Function minutesbetween = functions.get("minutesbetween");
            LocalDateTime dt1 = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime dt2 = LocalDateTime.of(2024, 1, 1, 0, 30);
            assertThat(minutesbetween.apply(dt1, dt2)).isEqualTo(30L);
        }

        @Test
        @DisplayName("secondsbetween function | secondsbetween 函数")
        void testSecondsBetween() {
            Function secondsbetween = functions.get("secondsbetween");
            LocalDateTime dt1 = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
            LocalDateTime dt2 = LocalDateTime.of(2024, 1, 1, 0, 0, 45);
            assertThat(secondsbetween.apply(dt1, dt2)).isEqualTo(45L);
        }
    }

    @Nested
    @DisplayName("Date Formatting Tests | 日期格式化测试")
    class DateFormattingTests {

        @Test
        @DisplayName("formatdate function | formatdate 函数")
        void testFormatDate() {
            Function formatdate = functions.get("formatdate");
            LocalDate date = LocalDate.of(2024, 1, 15);
            assertThat(formatdate.apply(date)).isEqualTo("2024-01-15");
            assertThat(formatdate.apply(date, "dd/MM/yyyy")).isEqualTo("15/01/2024");
            assertThat(formatdate.apply((Object) null)).isEqualTo("");
        }

        @Test
        @DisplayName("parsedate function | parsedate 函数")
        void testParseDate() {
            Function parsedate = functions.get("parsedate");
            assertThat(parsedate.apply("2024-01-15")).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(parsedate.apply("15/01/2024", "dd/MM/yyyy")).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(parsedate.apply((Object) null)).isNull();
        }

        @Test
        @DisplayName("parsedatetime function | parsedatetime 函数")
        void testParseDatetime() {
            Function parsedatetime = functions.get("parsedatetime");
            assertThat(parsedatetime.apply("2024-01-15T10:30:00")).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
            assertThat(parsedatetime.apply((Object) null)).isNull();
        }
    }

    @Nested
    @DisplayName("Special Dates Tests | 特殊日期测试")
    class SpecialDatesTests {

        private final LocalDate testDate = LocalDate.of(2024, 3, 15);

        @Test
        @DisplayName("startofday function | startofday 函数")
        void testStartOfDay() {
            Function startofday = functions.get("startofday");
            assertThat(startofday.apply(testDate)).isEqualTo(testDate.atStartOfDay());
        }

        @Test
        @DisplayName("endofday function | endofday 函数")
        void testEndOfDay() {
            Function endofday = functions.get("endofday");
            assertThat(endofday.apply(testDate)).isEqualTo(testDate.atTime(LocalTime.MAX));
        }

        @Test
        @DisplayName("startofmonth function | startofmonth 函数")
        void testStartOfMonth() {
            Function startofmonth = functions.get("startofmonth");
            assertThat(startofmonth.apply(testDate)).isEqualTo(LocalDate.of(2024, 3, 1));
        }

        @Test
        @DisplayName("endofmonth function | endofmonth 函数")
        void testEndOfMonth() {
            Function endofmonth = functions.get("endofmonth");
            assertThat(endofmonth.apply(testDate)).isEqualTo(LocalDate.of(2024, 3, 31));
        }

        @Test
        @DisplayName("startofyear function | startofyear 函数")
        void testStartOfYear() {
            Function startofyear = functions.get("startofyear");
            assertThat(startofyear.apply(testDate)).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("endofyear function | endofyear 函数")
        void testEndOfYear() {
            Function endofyear = functions.get("endofyear");
            assertThat(endofyear.apply(testDate)).isEqualTo(LocalDate.of(2024, 12, 31));
        }
    }

    @Nested
    @DisplayName("Date Comparison Tests | 日期比较测试")
    class DateComparisonTests {

        private final LocalDate date1 = LocalDate.of(2024, 1, 1);
        private final LocalDate date2 = LocalDate.of(2024, 1, 15);

        @Test
        @DisplayName("isbefore function | isbefore 函数")
        void testIsBefore() {
            Function isbefore = functions.get("isbefore");
            assertThat(isbefore.apply(date1, date2)).isEqualTo(true);
            assertThat(isbefore.apply(date2, date1)).isEqualTo(false);
            assertThat(isbefore.apply((Object) null, date2)).isEqualTo(false);
        }

        @Test
        @DisplayName("isafter function | isafter 函数")
        void testIsAfter() {
            Function isafter = functions.get("isafter");
            assertThat(isafter.apply(date2, date1)).isEqualTo(true);
            assertThat(isafter.apply(date1, date2)).isEqualTo(false);
        }

        @Test
        @DisplayName("isweekend function | isweekend 函数")
        void testIsWeekend() {
            Function isweekend = functions.get("isweekend");
            assertThat(isweekend.apply(LocalDate.of(2024, 1, 13))).isEqualTo(true); // Saturday
            assertThat(isweekend.apply(LocalDate.of(2024, 1, 14))).isEqualTo(true); // Sunday
            assertThat(isweekend.apply(LocalDate.of(2024, 1, 15))).isEqualTo(false); // Monday
            assertThat(isweekend.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("isleapyear function | isleapyear 函数")
        void testIsLeapYear() {
            Function isleapyear = functions.get("isleapyear");
            assertThat(isleapyear.apply(LocalDate.of(2024, 1, 1))).isEqualTo(true);
            assertThat(isleapyear.apply(LocalDate.of(2023, 1, 1))).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("GetFunctions Tests | getFunctions 测试")
    class GetFunctionsTests {

        @Test
        @DisplayName("getFunctions returns all functions | getFunctions 返回所有函数")
        void testGetFunctionsReturnsAll() {
            Map<String, Function> funcs = DateFunctions.getFunctions();
            assertThat(funcs).isNotEmpty();
            assertThat(funcs).containsKey("now");
            assertThat(funcs).containsKey("today");
            assertThat(funcs).containsKey("year");
        }
    }
}
