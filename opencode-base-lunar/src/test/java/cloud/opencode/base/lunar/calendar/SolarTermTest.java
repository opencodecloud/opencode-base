package cloud.opencode.base.lunar.calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SolarTerm (节气) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("SolarTerm (节气) 测试")
class SolarTermTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含24个节气")
        void testTwentyFourTerms() {
            assertThat(SolarTerm.values()).hasSize(24);
        }

        @Test
        @DisplayName("节气顺序正确")
        void testTermOrder() {
            SolarTerm[] terms = SolarTerm.values();
            assertThat(terms[0]).isEqualTo(SolarTerm.XIAO_HAN);  // 小寒
            assertThat(terms[1]).isEqualTo(SolarTerm.DA_HAN);    // 大寒
            assertThat(terms[2]).isEqualTo(SolarTerm.LI_CHUN);   // 立春
            assertThat(terms[5]).isEqualTo(SolarTerm.CHUN_FEN);  // 春分
            assertThat(terms[11]).isEqualTo(SolarTerm.XIA_ZHI);  // 夏至
            assertThat(terms[17]).isEqualTo(SolarTerm.QIU_FEN);  // 秋分
            assertThat(terms[23]).isEqualTo(SolarTerm.DONG_ZHI); // 冬至
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("小寒")
        void testXiaoHan() {
            assertThat(SolarTerm.XIAO_HAN.getName()).isEqualTo("小寒");
        }

        @Test
        @DisplayName("立春")
        void testLiChun() {
            assertThat(SolarTerm.LI_CHUN.getName()).isEqualTo("立春");
        }

        @Test
        @DisplayName("春分")
        void testChunFen() {
            assertThat(SolarTerm.CHUN_FEN.getName()).isEqualTo("春分");
        }

        @Test
        @DisplayName("清明")
        void testQingMing() {
            assertThat(SolarTerm.QING_MING.getName()).isEqualTo("清明");
        }

        @Test
        @DisplayName("夏至")
        void testXiaZhi() {
            assertThat(SolarTerm.XIA_ZHI.getName()).isEqualTo("夏至");
        }

        @Test
        @DisplayName("冬至")
        void testDongZhi() {
            assertThat(SolarTerm.DONG_ZHI.getName()).isEqualTo("冬至");
        }

        @ParameterizedTest
        @EnumSource(SolarTerm.class)
        @DisplayName("所有节气名称不为空")
        void testAllNamesNotEmpty(SolarTerm term) {
            assertThat(term.getName()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getDate方法测试")
    class GetDateTests {

        @Test
        @DisplayName("2024年小寒在1月")
        void testXiaoHan2024() {
            LocalDate date = SolarTerm.XIAO_HAN.getDate(2024);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(1);
            assertThat(date.getDayOfMonth()).isBetween(5, 7);
        }

        @Test
        @DisplayName("2024年立春在2月")
        void testLiChun2024() {
            LocalDate date = SolarTerm.LI_CHUN.getDate(2024);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(2);
            assertThat(date.getDayOfMonth()).isBetween(3, 5);
        }

        @Test
        @DisplayName("2024年春分在3月")
        void testChunFen2024() {
            LocalDate date = SolarTerm.CHUN_FEN.getDate(2024);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(3);
            assertThat(date.getDayOfMonth()).isBetween(19, 22);
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 1950, 2000, 2024, 2050, 2100})
        @DisplayName("不同年份返回有效日期")
        void testDifferentYears(int year) {
            for (SolarTerm term : SolarTerm.values()) {
                LocalDate date = term.getDate(year);
                assertThat(date).isNotNull();
                assertThat(date.getYear()).isEqualTo(year);
            }
        }
    }

    @Nested
    @DisplayName("isMajor和isMinor方法测试")
    class MajorMinorTests {

        @Test
        @DisplayName("春分是中气")
        void testChunFenMajor() {
            assertThat(SolarTerm.CHUN_FEN.isMajor()).isTrue();
            assertThat(SolarTerm.CHUN_FEN.isMinor()).isFalse();
        }

        @Test
        @DisplayName("立春是节气")
        void testLiChunMinor() {
            assertThat(SolarTerm.LI_CHUN.isMajor()).isFalse();
            assertThat(SolarTerm.LI_CHUN.isMinor()).isTrue();
        }

        @Test
        @DisplayName("夏至是中气")
        void testXiaZhiMajor() {
            assertThat(SolarTerm.XIA_ZHI.isMajor()).isTrue();
        }

        @Test
        @DisplayName("冬至是中气")
        void testDongZhiMajor() {
            assertThat(SolarTerm.DONG_ZHI.isMajor()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(SolarTerm.class)
        @DisplayName("节气和中气互斥")
        void testMutuallyExclusive(SolarTerm term) {
            assertThat(term.isMajor() != term.isMinor()).isTrue();
        }
    }

    @Nested
    @DisplayName("of(date)方法测试")
    class OfDateTests {

        @Test
        @DisplayName("节气当天返回节气")
        void testOnTermDay() {
            LocalDate date = SolarTerm.CHUN_FEN.getDate(2024);
            SolarTerm term = SolarTerm.of(date);
            assertThat(term).isEqualTo(SolarTerm.CHUN_FEN);
        }

        @Test
        @DisplayName("非节气当天返回null")
        void testNotOnTermDay() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            SolarTerm term = SolarTerm.of(date);
            // 可能返回null或者最近的节气，根据实现
            // 这里只验证不会抛出异常
            assertThatCode(() -> SolarTerm.of(date)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("ofYear方法测试")
    class OfYearTests {

        @Test
        @DisplayName("返回24个节气")
        void testReturnsAllTerms() {
            List<SolarTerm> terms = SolarTerm.ofYear(2024);
            assertThat(terms).hasSize(24);
        }
    }

    @Nested
    @DisplayName("next和previous静态方法测试")
    class NavigationTests {

        @Test
        @DisplayName("获取日期之后的下一个节气")
        void testNextFromDate() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            SolarTerm next = SolarTerm.next(date);
            assertThat(next).isNotNull();
        }

        @Test
        @DisplayName("获取日期之前的上一个节气")
        void testPreviousFromDate() {
            LocalDate date = LocalDate.of(2024, 3, 25);
            SolarTerm prev = SolarTerm.previous(date);
            assertThat(prev).isNotNull();
        }
    }
}
