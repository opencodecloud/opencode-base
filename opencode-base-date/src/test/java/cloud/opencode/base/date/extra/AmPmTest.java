package cloud.opencode.base.date.extra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.temporal.ChronoField;

import static org.assertj.core.api.Assertions.*;

/**
 * AmPm 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("AmPm 测试")
class AmPmTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("AM枚举值")
        void testAmValues() {
            assertThat(AmPm.AM.getValue()).isEqualTo(0);
            assertThat(AmPm.AM.getShortName()).isEqualTo("AM");
            assertThat(AmPm.AM.getChineseName()).isEqualTo("上午");
            assertThat(AmPm.AM.getDisplayName()).isEqualTo("AM");
        }

        @Test
        @DisplayName("PM枚举值")
        void testPmValues() {
            assertThat(AmPm.PM.getValue()).isEqualTo(1);
            assertThat(AmPm.PM.getShortName()).isEqualTo("PM");
            assertThat(AmPm.PM.getChineseName()).isEqualTo("下午");
            assertThat(AmPm.PM.getDisplayName()).isEqualTo("PM");
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of() 从值获取")
        void testOf() {
            assertThat(AmPm.of(0)).isEqualTo(AmPm.AM);
            assertThat(AmPm.of(1)).isEqualTo(AmPm.PM);
        }

        @Test
        @DisplayName("ofHour() 从小时获取")
        void testOfHour() {
            assertThat(AmPm.ofHour(0)).isEqualTo(AmPm.AM);
            assertThat(AmPm.ofHour(11)).isEqualTo(AmPm.AM);
            assertThat(AmPm.ofHour(12)).isEqualTo(AmPm.PM);
            assertThat(AmPm.ofHour(23)).isEqualTo(AmPm.PM);
        }

        @Test
        @DisplayName("from() 从LocalTime获取")
        void testFrom() {
            assertThat(AmPm.from(LocalTime.of(0, 0))).isEqualTo(AmPm.AM);
            assertThat(AmPm.from(LocalTime.of(11, 59))).isEqualTo(AmPm.AM);
            assertThat(AmPm.from(LocalTime.of(12, 0))).isEqualTo(AmPm.PM);
            assertThat(AmPm.from(LocalTime.of(23, 59))).isEqualTo(AmPm.PM);
        }

        @Test
        @DisplayName("from() 从AmPm返回自身")
        void testFromAmPm() {
            assertThat(AmPm.from(AmPm.AM)).isSameAs(AmPm.AM);
            assertThat(AmPm.from(AmPm.PM)).isSameAs(AmPm.PM);
        }

        @Test
        @DisplayName("now() 获取当前")
        void testNow() {
            AmPm current = AmPm.now();
            assertThat(current).isIn(AmPm.AM, AmPm.PM);
        }
    }

    @Nested
    @DisplayName("判断方法测试")
    class PredicateTests {

        @Test
        @DisplayName("isAm() 检查是否上午")
        void testIsAm() {
            assertThat(AmPm.AM.isAm()).isTrue();
            assertThat(AmPm.PM.isAm()).isFalse();
        }

        @Test
        @DisplayName("isPm() 检查是否下午")
        void testIsPm() {
            assertThat(AmPm.AM.isPm()).isFalse();
            assertThat(AmPm.PM.isPm()).isTrue();
        }
    }

    @Nested
    @DisplayName("小时范围测试")
    class HourRangeTests {

        @Test
        @DisplayName("firstHour() 获取第一个小时")
        void testFirstHour() {
            assertThat(AmPm.AM.firstHour()).isEqualTo(0);
            assertThat(AmPm.PM.firstHour()).isEqualTo(12);
        }

        @Test
        @DisplayName("lastHour() 获取最后一个小时")
        void testLastHour() {
            assertThat(AmPm.AM.lastHour()).isEqualTo(11);
            assertThat(AmPm.PM.lastHour()).isEqualTo(23);
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("opposite() 获取相反时段")
        void testOpposite() {
            assertThat(AmPm.AM.opposite()).isEqualTo(AmPm.PM);
            assertThat(AmPm.PM.opposite()).isEqualTo(AmPm.AM);
        }
    }

    @Nested
    @DisplayName("TemporalAccessor实现测试")
    class TemporalAccessorTests {

        @Test
        @DisplayName("isSupported() 支持的字段")
        void testIsSupported() {
            assertThat(AmPm.AM.isSupported(ChronoField.AMPM_OF_DAY)).isTrue();
            assertThat(AmPm.AM.isSupported(ChronoField.HOUR_OF_DAY)).isFalse();
        }

        @Test
        @DisplayName("getLong() 获取字段值")
        void testGetLong() {
            assertThat(AmPm.AM.getLong(ChronoField.AMPM_OF_DAY)).isEqualTo(0);
            assertThat(AmPm.PM.getLong(ChronoField.AMPM_OF_DAY)).isEqualTo(1);
        }

        @Test
        @DisplayName("getLong() 不支持字段抛出异常")
        void testGetLongUnsupported() {
            assertThatThrownBy(() -> AmPm.AM.getLong(ChronoField.HOUR_OF_DAY))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("range() 获取字段范围")
        void testRange() {
            assertThat(AmPm.AM.range(ChronoField.AMPM_OF_DAY).getMinimum()).isEqualTo(0);
            assertThat(AmPm.AM.range(ChronoField.AMPM_OF_DAY).getMaximum()).isEqualTo(1);
        }

        @Test
        @DisplayName("range() 不支持字段抛出异常")
        void testRangeUnsupported() {
            assertThatThrownBy(() -> AmPm.AM.range(ChronoField.HOUR_OF_DAY))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("TemporalQuery实现测试")
    class TemporalQueryTests {

        @Test
        @DisplayName("queryFrom() 查询")
        void testQueryFrom() {
            LocalTime morning = LocalTime.of(9, 0);
            LocalTime afternoon = LocalTime.of(15, 0);

            assertThat(AmPm.AM.queryFrom(morning)).isEqualTo(AmPm.AM);
            assertThat(AmPm.AM.queryFrom(afternoon)).isEqualTo(AmPm.PM);
        }

        @Test
        @DisplayName("query() 静态查询方法")
        void testQuery() {
            LocalTime morning = LocalTime.of(9, 0);
            assertThat(morning.query(AmPm.query())).isEqualTo(AmPm.AM);
        }
    }
}
