package cloud.opencode.base.lunar.calendar;

import cloud.opencode.base.lunar.LunarDate;
import cloud.opencode.base.lunar.SolarDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * DateConverter (日期转换器) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("DateConverter (日期转换器) 测试")
class DateConverterTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(DateConverter.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = DateConverter.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("solarToLunar方法测试")
    class SolarToLunarTests {

        @Test
        @DisplayName("2024年春节转换")
        void testSpringFestival2024() {
            LunarDate lunar = DateConverter.solarToLunar(2024, 2, 10);

            assertThat(lunar.year()).isEqualTo(2024);
            assertThat(lunar.month()).isEqualTo(1);
            assertThat(lunar.day()).isEqualTo(1);
            assertThat(lunar.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("SolarDate转换")
        void testFromSolarDate() {
            SolarDate solar = new SolarDate(2024, 2, 10);
            LunarDate lunar = DateConverter.solarToLunar(solar);

            assertThat(lunar.year()).isEqualTo(2024);
            assertThat(lunar.month()).isEqualTo(1);
            assertThat(lunar.day()).isEqualTo(1);
        }

        @ParameterizedTest
        @CsvSource({
            "2020, 1, 25, 2020, 1, 1",
            "2021, 2, 12, 2021, 1, 1",
            "2022, 2, 1, 2022, 1, 1",
            "2023, 1, 22, 2023, 1, 1"
        })
        @DisplayName("多年春节转换")
        void testMultipleSpringFestivals(int sy, int sm, int sd, int ly, int lm, int ld) {
            LunarDate lunar = DateConverter.solarToLunar(sy, sm, sd);

            assertThat(lunar.year()).isEqualTo(ly);
            assertThat(lunar.month()).isEqualTo(lm);
            assertThat(lunar.day()).isEqualTo(ld);
        }
    }

    @Nested
    @DisplayName("lunarToSolar方法测试")
    class LunarToSolarTests {

        @Test
        @DisplayName("2024年春节转换")
        void testSpringFestival2024() {
            SolarDate solar = DateConverter.lunarToSolar(2024, 1, 1, false);

            assertThat(solar.year()).isEqualTo(2024);
            assertThat(solar.month()).isEqualTo(2);
            assertThat(solar.day()).isEqualTo(10);
        }

        @Test
        @DisplayName("LunarDate转换")
        void testFromLunarDate() {
            LunarDate lunar = new LunarDate(2024, 1, 1, false);
            SolarDate solar = DateConverter.lunarToSolar(lunar);

            assertThat(solar.year()).isEqualTo(2024);
            assertThat(solar.month()).isEqualTo(2);
            assertThat(solar.day()).isEqualTo(10);
        }

        @Test
        @DisplayName("闰月日期转换")
        void testLeapMonthConversion() {
            // 2020年闰四月
            SolarDate solar = DateConverter.lunarToSolar(2020, 4, 1, true);

            assertThat(solar.year()).isEqualTo(2020);
            assertThat(solar.month()).isEqualTo(5);
        }

        @ParameterizedTest
        @CsvSource({
            "2020, 1, 1, 2020, 1, 25",
            "2021, 1, 1, 2021, 2, 12",
            "2022, 1, 1, 2022, 2, 1",
            "2023, 1, 1, 2023, 1, 22"
        })
        @DisplayName("多年春节转换")
        void testMultipleSpringFestivals(int ly, int lm, int ld, int sy, int sm, int sd) {
            SolarDate solar = DateConverter.lunarToSolar(ly, lm, ld, false);

            assertThat(solar.year()).isEqualTo(sy);
            assertThat(solar.month()).isEqualTo(sm);
            assertThat(solar.day()).isEqualTo(sd);
        }
    }

    @Nested
    @DisplayName("toLunar方法测试")
    class ToLunarTests {

        @Test
        @DisplayName("LocalDate转LunarDate")
        void testLocalDateToLunar() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LunarDate lunar = DateConverter.toLunar(date);

            assertThat(lunar).isNotNull();
            assertThat(lunar.year()).isEqualTo(2024);
        }
    }

    @Nested
    @DisplayName("toLocalDate方法测试")
    class ToLocalDateTests {

        @Test
        @DisplayName("LunarDate转LocalDate")
        void testLunarToLocalDate() {
            LunarDate lunar = new LunarDate(2024, 1, 1, false);
            LocalDate date = DateConverter.toLocalDate(lunar);

            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(2);
            assertThat(date.getDayOfMonth()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("today方法测试")
    class TodayTests {

        @Test
        @DisplayName("获取今日农历")
        void testToday() {
            LunarDate today = DateConverter.today();

            assertThat(today).isNotNull();
            assertThat(today.year()).isGreaterThanOrEqualTo(2024);
        }
    }

    @Nested
    @DisplayName("往返转换测试")
    class RoundTripTests {

        @Test
        @DisplayName("公历转农历再转公历")
        void testSolarToLunarAndBack() {
            LocalDate original = LocalDate.of(2024, 6, 15);
            LunarDate lunar = DateConverter.toLunar(original);
            SolarDate solar = DateConverter.lunarToSolar(lunar);

            assertThat(solar.year()).isEqualTo(original.getYear());
            assertThat(solar.month()).isEqualTo(original.getMonthValue());
            assertThat(solar.day()).isEqualTo(original.getDayOfMonth());
        }

        @Test
        @DisplayName("农历转公历再转农历")
        void testLunarToSolarAndBack() {
            LunarDate original = new LunarDate(2024, 5, 10, false);
            SolarDate solar = DateConverter.lunarToSolar(original);
            LocalDate localDate = LocalDate.of(solar.year(), solar.month(), solar.day());
            LunarDate lunar = DateConverter.toLunar(localDate);

            assertThat(lunar.year()).isEqualTo(original.year());
            assertThat(lunar.month()).isEqualTo(original.month());
            assertThat(lunar.day()).isEqualTo(original.day());
            assertThat(lunar.isLeapMonth()).isEqualTo(original.isLeapMonth());
        }
    }
}
