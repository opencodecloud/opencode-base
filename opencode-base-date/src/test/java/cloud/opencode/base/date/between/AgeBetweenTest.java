package cloud.opencode.base.date.between;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import static org.assertj.core.api.Assertions.*;

/**
 * AgeBetween 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("AgeBetween 测试")
class AgeBetweenTest {

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("fromBirth(LocalDate) 从出生日期创建")
        void testFromBirthLocalDate() {
            LocalDate birthDate = LocalDate.of(2000, 1, 1);
            AgeBetween age = AgeBetween.fromBirth(birthDate);

            assertThat(age.getBirthDate()).isEqualTo(birthDate);
            assertThat(age.getYears()).isGreaterThanOrEqualTo(24);
        }

        @Test
        @DisplayName("fromBirth(LocalDateTime) 从出生日期时间创建")
        void testFromBirthLocalDateTime() {
            LocalDateTime birthDateTime = LocalDateTime.of(2000, 1, 1, 10, 30);
            AgeBetween age = AgeBetween.fromBirth(birthDateTime);

            assertThat(age.getBirthDate()).isEqualTo(birthDateTime.toLocalDate());
        }

        @Test
        @DisplayName("at() 在指定日期计算年龄")
        void testAt() {
            LocalDate birthDate = LocalDate.of(1990, 5, 15);
            LocalDate referenceDate = LocalDate.of(2024, 5, 15);
            AgeBetween age = AgeBetween.at(birthDate, referenceDate);

            assertThat(age.getYears()).isEqualTo(34);
        }

        @Test
        @DisplayName("at() 出生日期在参考日期之后抛出异常")
        void testAtBirthAfterReferenceThrows() {
            LocalDate birthDate = LocalDate.of(2024, 1, 1);
            LocalDate referenceDate = LocalDate.of(2020, 1, 1);

            assertThatThrownBy(() -> AgeBetween.at(birthDate, referenceDate))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("ageInYears() 快速计算年龄")
        void testAgeInYears() {
            LocalDate birthDate = LocalDate.of(2000, 1, 1);
            int age = AgeBetween.ageInYears(birthDate);

            assertThat(age).isGreaterThanOrEqualTo(24);
        }
    }

    @Nested
    @DisplayName("年龄获取测试")
    class AgeGetterTests {

        @Test
        @DisplayName("getYears() 获取完整年数")
        void testGetYears() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 14)
            );

            assertThat(age.getYears()).isEqualTo(33);
        }

        @Test
        @DisplayName("getMonths() 获取月份组件")
        void testGetMonths() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 8, 20)
            );

            assertThat(age.getMonths()).isEqualTo(3);
        }

        @Test
        @DisplayName("getDays() 获取天数组件")
        void testGetDays() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 20)
            );

            assertThat(age.getDays()).isEqualTo(5);
        }

        @Test
        @DisplayName("getTotalMonths() 获取总月数")
        void testGetTotalMonths() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(2022, 1, 1),
                    LocalDate.of(2024, 1, 1)
            );

            assertThat(age.getTotalMonths()).isEqualTo(24);
        }

        @Test
        @DisplayName("getTotalDays() 获取总天数")
        void testGetTotalDays() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 11)
            );

            assertThat(age.getTotalDays()).isEqualTo(10);
        }

        @Test
        @DisplayName("getTotalWeeks() 获取总周数")
        void testGetTotalWeeks() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 22)
            );

            assertThat(age.getTotalWeeks()).isEqualTo(3);
        }

        @Test
        @DisplayName("getPeriod() 获取Period")
        void testGetPeriod() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(2022, 1, 15),
                    LocalDate.of(2024, 3, 20)
            );

            Period period = age.getPeriod();

            assertThat(period.getYears()).isEqualTo(2);
            assertThat(period.getMonths()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("生日方法测试")
    class BirthdayMethodTests {

        @Test
        @DisplayName("isBirthdayToday() 今天是生日")
        void testIsBirthdayTodayTrue() {
            LocalDate birthDate = LocalDate.of(1990, 5, 15);
            LocalDate referenceDate = LocalDate.of(2024, 5, 15);
            AgeBetween age = AgeBetween.at(birthDate, referenceDate);

            assertThat(age.isBirthdayToday()).isTrue();
        }

        @Test
        @DisplayName("isBirthdayToday() 今天不是生日")
        void testIsBirthdayTodayFalse() {
            LocalDate birthDate = LocalDate.of(1990, 5, 15);
            LocalDate referenceDate = LocalDate.of(2024, 5, 16);
            AgeBetween age = AgeBetween.at(birthDate, referenceDate);

            assertThat(age.isBirthdayToday()).isFalse();
        }

        @Test
        @DisplayName("getNextBirthday() 获取下一个生日")
        void testGetNextBirthday() {
            LocalDate birthDate = LocalDate.of(1990, 5, 15);
            LocalDate referenceDate = LocalDate.of(2024, 3, 1);
            AgeBetween age = AgeBetween.at(birthDate, referenceDate);

            LocalDate nextBirthday = age.getNextBirthday();

            assertThat(nextBirthday).isEqualTo(LocalDate.of(2024, 5, 15));
        }

        @Test
        @DisplayName("getNextBirthday() 生日已过获取明年生日")
        void testGetNextBirthdayNextYear() {
            LocalDate birthDate = LocalDate.of(1990, 5, 15);
            LocalDate referenceDate = LocalDate.of(2024, 6, 1);
            AgeBetween age = AgeBetween.at(birthDate, referenceDate);

            LocalDate nextBirthday = age.getNextBirthday();

            assertThat(nextBirthday).isEqualTo(LocalDate.of(2025, 5, 15));
        }

        @Test
        @DisplayName("getDaysUntilNextBirthday() 获取距离下一个生日的天数")
        void testGetDaysUntilNextBirthday() {
            LocalDate birthDate = LocalDate.of(1990, 5, 15);
            LocalDate referenceDate = LocalDate.of(2024, 5, 10);
            AgeBetween age = AgeBetween.at(birthDate, referenceDate);

            long days = age.getDaysUntilNextBirthday();

            assertThat(days).isEqualTo(5);
        }

        @Test
        @DisplayName("getLastBirthday() 获取上一个生日")
        void testGetLastBirthday() {
            LocalDate birthDate = LocalDate.of(1990, 5, 15);
            LocalDate referenceDate = LocalDate.of(2024, 8, 1);
            AgeBetween age = AgeBetween.at(birthDate, referenceDate);

            LocalDate lastBirthday = age.getLastBirthday();

            assertThat(lastBirthday).isEqualTo(LocalDate.of(2024, 5, 15));
        }

        @Test
        @DisplayName("getLastBirthday() 生日未到获取去年生日")
        void testGetLastBirthdayPreviousYear() {
            LocalDate birthDate = LocalDate.of(1990, 5, 15);
            LocalDate referenceDate = LocalDate.of(2024, 3, 1);
            AgeBetween age = AgeBetween.at(birthDate, referenceDate);

            LocalDate lastBirthday = age.getLastBirthday();

            assertThat(lastBirthday).isEqualTo(LocalDate.of(2023, 5, 15));
        }
    }

    @Nested
    @DisplayName("星座方法测试")
    class ZodiacMethodTests {

        @Test
        @DisplayName("getZodiacSign() 各月份星座")
        void testGetZodiacSign() {
            assertThat(AgeBetween.at(LocalDate.of(1990, 1, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Capricorn");
            assertThat(AgeBetween.at(LocalDate.of(1990, 2, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Aquarius");
            assertThat(AgeBetween.at(LocalDate.of(1990, 3, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Pisces");
            assertThat(AgeBetween.at(LocalDate.of(1990, 4, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Aries");
            assertThat(AgeBetween.at(LocalDate.of(1990, 5, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Taurus");
            assertThat(AgeBetween.at(LocalDate.of(1990, 6, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Gemini");
            assertThat(AgeBetween.at(LocalDate.of(1990, 7, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Cancer");
            assertThat(AgeBetween.at(LocalDate.of(1990, 8, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Leo");
            assertThat(AgeBetween.at(LocalDate.of(1990, 9, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Virgo");
            assertThat(AgeBetween.at(LocalDate.of(1990, 10, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Libra");
            assertThat(AgeBetween.at(LocalDate.of(1990, 11, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Scorpio");
            assertThat(AgeBetween.at(LocalDate.of(1990, 12, 15), LocalDate.now()).getZodiacSign()).isEqualTo("Sagittarius");
        }

        @Test
        @DisplayName("getZodiacSignChinese() 中文星座")
        void testGetZodiacSignChinese() {
            AgeBetween age = AgeBetween.at(LocalDate.of(1990, 5, 15), LocalDate.now());

            assertThat(age.getZodiacSignChinese()).isEqualTo("金牛座");
        }

        @Test
        @DisplayName("getChineseZodiac() 中国生肖")
        void testGetChineseZodiac() {
            // 1990 is Year of the Horse
            AgeBetween age = AgeBetween.at(LocalDate.of(1990, 5, 15), LocalDate.now());

            assertThat(age.getChineseZodiac()).isEqualTo("Horse");
        }

        @Test
        @DisplayName("getChineseZodiacChinese() 中文生肖")
        void testGetChineseZodiacChinese() {
            // 1990 is Year of the Horse
            AgeBetween age = AgeBetween.at(LocalDate.of(1990, 5, 15), LocalDate.now());

            assertThat(age.getChineseZodiacChinese()).isEqualTo("马");
        }
    }

    @Nested
    @DisplayName("格式化测试")
    class FormattingTests {

        @Test
        @DisplayName("format() 英文格式化")
        void testFormat() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 8, 20)
            );

            String formatted = age.format();

            assertThat(formatted).contains("34 years");
            assertThat(formatted).contains("3 months");
        }

        @Test
        @DisplayName("format() 零年龄")
        void testFormatZeroAge() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 1)
            );

            String formatted = age.format();

            assertThat(formatted).isEqualTo("0 days");
        }

        @Test
        @DisplayName("formatChinese() 中文格式化")
        void testFormatChinese() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 8, 20)
            );

            String formatted = age.formatChinese();

            assertThat(formatted).contains("34岁");
            assertThat(formatted).contains("3个月");
        }

        @Test
        @DisplayName("formatChinese() 零年龄")
        void testFormatChineseZeroAge() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 1)
            );

            String formatted = age.formatChinese();

            assertThat(formatted).isEqualTo("0天");
        }
    }

    @Nested
    @DisplayName("toDetail测试")
    class DetailTests {

        @Test
        @DisplayName("toDetail() 获取详细年龄信息")
        void testToDetail() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            AgeDetail detail = age.toDetail();

            assertThat(detail).isNotNull();
            assertThat(detail.getAgeBetween()).isEqualTo(age);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            AgeBetween age1 = AgeBetween.at(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );
            AgeBetween age2 = AgeBetween.at(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            assertThat(age1).isEqualTo(age2);
            assertThat(age1.hashCode()).isEqualTo(age2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            AgeBetween age = AgeBetween.at(
                    LocalDate.of(1990, 5, 15),
                    LocalDate.of(2024, 5, 15)
            );

            String str = age.toString();

            assertThat(str).contains("34 years");
            assertThat(str).contains("1990-05-15");
        }
    }

    @Nested
    @DisplayName("null安全性测试")
    class NullSafetyTests {

        @Test
        @DisplayName("fromBirth() null抛出异常")
        void testFromBirthNull() {
            assertThatThrownBy(() -> AgeBetween.fromBirth((LocalDate) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("at() null birth抛出异常")
        void testAtNullBirth() {
            assertThatThrownBy(() -> AgeBetween.at(null, LocalDate.now()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("at() null reference抛出异常")
        void testAtNullReference() {
            assertThatThrownBy(() -> AgeBetween.at(LocalDate.now(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
