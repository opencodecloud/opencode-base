package cloud.opencode.base.lunar.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * SolarTermData 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("SolarTermData 测试")
class SolarTermDataTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(SolarTermData.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = SolarTermData.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getDate方法测试")
    class GetDateTests {

        @Test
        @DisplayName("获取2024年小寒日期")
        void testXiaoHan2024() {
            LocalDate date = SolarTermData.getDate(2024, 0);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(1);
            assertThat(date.getDayOfMonth()).isBetween(5, 7);
        }

        @Test
        @DisplayName("获取2024年大寒日期")
        void testDaHan2024() {
            LocalDate date = SolarTermData.getDate(2024, 1);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(1);
            assertThat(date.getDayOfMonth()).isBetween(19, 21);
        }

        @Test
        @DisplayName("获取2024年立春日期")
        void testLiChun2024() {
            LocalDate date = SolarTermData.getDate(2024, 2);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(2);
            assertThat(date.getDayOfMonth()).isBetween(3, 5);
        }

        @Test
        @DisplayName("获取2024年春分日期")
        void testChunFen2024() {
            LocalDate date = SolarTermData.getDate(2024, 5);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(3);
            assertThat(date.getDayOfMonth()).isBetween(19, 22);
        }

        @Test
        @DisplayName("获取2024年清明日期")
        void testQingMing2024() {
            LocalDate date = SolarTermData.getDate(2024, 6);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(4);
            assertThat(date.getDayOfMonth()).isBetween(4, 6);
        }

        @Test
        @DisplayName("获取2024年夏至日期")
        void testXiaZhi2024() {
            LocalDate date = SolarTermData.getDate(2024, 11);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(6);
            assertThat(date.getDayOfMonth()).isBetween(20, 22);
        }

        @Test
        @DisplayName("获取2024年秋分日期")
        void testQiuFen2024() {
            LocalDate date = SolarTermData.getDate(2024, 17);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(9);
            assertThat(date.getDayOfMonth()).isBetween(22, 24);
        }

        @Test
        @DisplayName("获取2024年冬至日期")
        void testDongZhi2024() {
            LocalDate date = SolarTermData.getDate(2024, 23);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(12);
            assertThat(date.getDayOfMonth()).isBetween(21, 23);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23})
        @DisplayName("所有节气索引返回有效日期")
        void testAllTermIndexes(int termIndex) {
            LocalDate date = SolarTermData.getDate(2024, termIndex);
            assertThat(date).isNotNull();
            assertThat(date.getYear()).isEqualTo(2024);
        }
    }

    @Nested
    @DisplayName("getYearDates方法测试")
    class GetYearDatesTests {

        @Test
        @DisplayName("返回24个节气日期")
        void testReturns24Dates() {
            LocalDate[] dates = SolarTermData.getYearDates(2024);
            assertThat(dates).hasSize(24);
        }

        @Test
        @DisplayName("所有日期在同一年内")
        void testAllDatesInYear() {
            LocalDate[] dates = SolarTermData.getYearDates(2024);
            for (LocalDate date : dates) {
                assertThat(date.getYear()).isEqualTo(2024);
            }
        }

        @Test
        @DisplayName("日期大致按时间顺序")
        void testDatesOrdered() {
            LocalDate[] dates = SolarTermData.getYearDates(2024);
            // 小寒在1月，冬至在12月
            assertThat(dates[0].getMonthValue()).isEqualTo(1);
            assertThat(dates[23].getMonthValue()).isEqualTo(12);
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 1950, 2000, 2024, 2050, 2100})
        @DisplayName("不同年份都返回24个日期")
        void testDifferentYears(int year) {
            LocalDate[] dates = SolarTermData.getYearDates(year);
            assertThat(dates).hasSize(24);
        }
    }

    @Nested
    @DisplayName("特殊年份修正测试")
    class SpecialYearAdjustmentsTests {

        @Test
        @DisplayName("2019年小寒修正")
        void testXiaoHan2019() {
            LocalDate date = SolarTermData.getDate(2019, 0);
            assertThat(date.getDayOfMonth()).isEqualTo(5);
        }

        @Test
        @DisplayName("2019年清明修正")
        void testQingMing2019() {
            LocalDate date = SolarTermData.getDate(2019, 6);
            assertThat(date.getDayOfMonth()).isEqualTo(5);
        }

        @Test
        @DisplayName("2026年立春修正")
        void testLiChun2026() {
            LocalDate date = SolarTermData.getDate(2026, 2);
            assertThat(date.getDayOfMonth()).isEqualTo(4);
        }

        @Test
        @DisplayName("2008年芒种修正")
        void testMangZhong2008() {
            LocalDate date = SolarTermData.getDate(2008, 10);
            assertThat(date.getDayOfMonth()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("世纪系数测试")
    class CenturyCoefficientTests {

        @Test
        @DisplayName("20世纪使用正确系数")
        void test20thCentury() {
            LocalDate date = SolarTermData.getDate(1990, 0);
            assertThat(date).isNotNull();
            assertThat(date.getYear()).isEqualTo(1990);
        }

        @Test
        @DisplayName("21世纪使用正确系数")
        void test21stCentury() {
            LocalDate date = SolarTermData.getDate(2020, 0);
            assertThat(date).isNotNull();
            assertThat(date.getYear()).isEqualTo(2020);
        }

        @Test
        @DisplayName("世纪交界处正确")
        void testCenturyBoundary() {
            LocalDate date1999 = SolarTermData.getDate(1999, 0);
            LocalDate date2000 = SolarTermData.getDate(2000, 0);

            assertThat(date1999.getYear()).isEqualTo(1999);
            assertThat(date2000.getYear()).isEqualTo(2000);
        }
    }
}
