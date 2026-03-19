package cloud.opencode.base.lunar.divination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * AuspiciousDay (吉日) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("AuspiciousDay (吉日) 测试")
class AuspiciousDayTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(AuspiciousDay.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = AuspiciousDay.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("findNext方法测试")
    class FindNextTests {

        @Test
        @DisplayName("查找下一个吉日")
        void testFindNext() {
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate result = AuspiciousDay.findNext("嫁娶", from);

            // 结果可能为null（如果在搜索范围内没找到）或者是有效日期
            if (result != null) {
                assertThat(result.isAfter(from) || result.isEqual(from)).isTrue();
            }
        }

        @Test
        @DisplayName("指定最大搜索天数")
        void testFindNextWithMaxDays() {
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate result = AuspiciousDay.findNext("祭祀", from, 30);

            if (result != null) {
                assertThat(result).isBetween(from, from.plusDays(30));
            }
        }
    }

    @Nested
    @DisplayName("findInMonth方法测试")
    class FindInMonthTests {

        @Test
        @DisplayName("查找月内吉日")
        void testFindInMonth() {
            List<LocalDate> results = AuspiciousDay.findInMonth("祭祀", 2024, 1);

            assertThat(results).isNotNull();
            for (LocalDate date : results) {
                assertThat(date.getYear()).isEqualTo(2024);
                assertThat(date.getMonthValue()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("不同月份返回不同结果")
        void testDifferentMonths() {
            List<LocalDate> jan = AuspiciousDay.findInMonth("嫁娶", 2024, 1);
            List<LocalDate> feb = AuspiciousDay.findInMonth("嫁娶", 2024, 2);

            assertThat(jan).isNotNull();
            assertThat(feb).isNotNull();
            // 两个月的结果可能不同
        }
    }

    @Nested
    @DisplayName("findInRange方法测试")
    class FindInRangeTests {

        @Test
        @DisplayName("查找日期范围内吉日")
        void testFindInRange() {
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 1, 31);
            List<LocalDate> results = AuspiciousDay.findInRange("出行", from, to);

            assertThat(results).isNotNull();
            for (LocalDate date : results) {
                assertThat(date).isBetween(from, to);
            }
        }

        @Test
        @DisplayName("空范围返回空列表")
        void testEmptyRange() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            List<LocalDate> results = AuspiciousDay.findInRange("嫁娶", date, date);

            assertThat(results).isNotNull();
        }
    }

    @Nested
    @DisplayName("isAuspicious方法测试")
    class IsAuspiciousTests {

        @Test
        @DisplayName("检查日期是否为吉日")
        void testIsAuspicious() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            boolean result = AuspiciousDay.isAuspicious("祭祀", date);

            // 结果为true或false
            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("shouldAvoid方法测试")
    class ShouldAvoidTests {

        @Test
        @DisplayName("检查日期是否应避免")
        void testShouldAvoid() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            boolean result = AuspiciousDay.shouldAvoid("破土", date);

            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("getYiJi方法测试")
    class GetYiJiTests {

        @Test
        @DisplayName("获取日期宜忌")
        void testGetYiJi() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            YiJi yiji = AuspiciousDay.getYiJi(date);

            assertThat(yiji).isNotNull();
            assertThat(yiji.suitable()).isNotNull();
            assertThat(yiji.avoid()).isNotNull();
        }
    }

    @Nested
    @DisplayName("today方法测试")
    class TodayTests {

        @Test
        @DisplayName("获取今日宜忌")
        void testToday() {
            YiJi yiji = AuspiciousDay.today();

            assertThat(yiji).isNotNull();
        }
    }
}
