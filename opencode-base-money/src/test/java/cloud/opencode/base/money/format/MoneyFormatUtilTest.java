package cloud.opencode.base.money.format;

import cloud.opencode.base.money.Currency;
import cloud.opencode.base.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * MoneyFormatUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("MoneyFormatUtil 测试")
class MoneyFormatUtilTest {

    @Nested
    @DisplayName("format方法测试")
    class FormatTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(MoneyFormatUtil.format(null)).isEmpty();
        }

        @Test
        @DisplayName("格式化金额")
        void testFormat() {
            Money money = Money.of("1234.50");
            String result = MoneyFormatUtil.format(money);

            assertThat(result).startsWith("¥");
        }
    }

    @Nested
    @DisplayName("formatNumber方法测试")
    class FormatNumberTests {

        @Test
        @DisplayName("null Money返回空字符串")
        void testNullMoney() {
            assertThat(MoneyFormatUtil.formatNumber((Money) null)).isEmpty();
        }

        @Test
        @DisplayName("null BigDecimal返回空字符串")
        void testNullBigDecimal() {
            assertThat(MoneyFormatUtil.formatNumber(null, 2)).isEmpty();
        }

        @Test
        @DisplayName("格式化数字")
        void testFormatNumber() {
            Money money = Money.of("1234.50");
            String result = MoneyFormatUtil.formatNumber(money);

            assertThat(result).doesNotStartWith("¥");
        }

        @Test
        @DisplayName("按精度格式化")
        void testFormatWithScale() {
            String result = MoneyFormatUtil.formatNumber(new BigDecimal("1234.5"), 2);
            assertThat(result.contains(".50") || result.contains(",50")).isTrue();
        }
    }

    @Nested
    @DisplayName("formatWithCode方法测试")
    class FormatWithCodeTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(MoneyFormatUtil.formatWithCode(null)).isEmpty();
        }

        @Test
        @DisplayName("带货币代码格式化")
        void testFormatWithCode() {
            Money money = Money.of("100", Currency.CNY);
            String result = MoneyFormatUtil.formatWithCode(money);

            assertThat(result).startsWith("CNY");
        }
    }

    @Nested
    @DisplayName("formatWithNameZh方法测试")
    class FormatWithNameZhTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(MoneyFormatUtil.formatWithNameZh(null)).isEmpty();
        }

        @Test
        @DisplayName("带中文名称格式化")
        void testFormatWithNameZh() {
            Money money = Money.of("100", Currency.CNY);
            String result = MoneyFormatUtil.formatWithNameZh(money);

            assertThat(result).startsWith("人民币");
        }
    }

    @Nested
    @DisplayName("formatAccounting方法测试")
    class FormatAccountingTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(MoneyFormatUtil.formatAccounting(null)).isEmpty();
        }

        @Test
        @DisplayName("正数正常格式化")
        void testPositive() {
            Money money = Money.of("100");
            String result = MoneyFormatUtil.formatAccounting(money);

            assertThat(result).doesNotContain("(");
        }

        @Test
        @DisplayName("负数用括号")
        void testNegative() {
            Money money = Money.of("-100");
            String result = MoneyFormatUtil.formatAccounting(money);

            assertThat(result).contains("(");
            assertThat(result).contains(")");
        }
    }

    @Nested
    @DisplayName("formatWithSign方法测试")
    class FormatWithSignTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(MoneyFormatUtil.formatWithSign(null)).isEmpty();
        }

        @Test
        @DisplayName("正数带+号")
        void testPositive() {
            Money money = Money.of("100");
            String result = MoneyFormatUtil.formatWithSign(money);

            assertThat(result).startsWith("+");
        }

        @Test
        @DisplayName("负数带-号")
        void testNegative() {
            Money money = Money.of("-100");
            String result = MoneyFormatUtil.formatWithSign(money);

            assertThat(result).startsWith("-");
        }

        @Test
        @DisplayName("零没有符号")
        void testZero() {
            Money money = Money.of("0");
            String result = MoneyFormatUtil.formatWithSign(money);

            assertThat(result).doesNotStartWith("+").doesNotStartWith("-");
        }
    }

    @Nested
    @DisplayName("formatNoGrouping方法测试")
    class FormatNoGroupingTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(MoneyFormatUtil.formatNoGrouping(null)).isEmpty();
        }

        @Test
        @DisplayName("不带千位分隔符")
        void testNoGrouping() {
            Money money = Money.of("1234567.89");
            String result = MoneyFormatUtil.formatNoGrouping(money);

            assertThat(result).doesNotContain(",");
        }
    }

    @Nested
    @DisplayName("format with locale方法测试")
    class FormatWithLocaleTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(MoneyFormatUtil.format(null, Locale.CHINA)).isEmpty();
        }

        @Test
        @DisplayName("按地区格式化")
        void testWithLocale() {
            Money money = Money.of("1234.50");
            String result = MoneyFormatUtil.format(money, Locale.CHINA);

            assertThat(result).isNotBlank();
        }
    }

    @Nested
    @DisplayName("format with pattern方法测试")
    class FormatWithPatternTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(MoneyFormatUtil.format(null, "#,##0.00")).isEmpty();
        }

        @Test
        @DisplayName("使用自定义模式")
        void testWithPattern() {
            Money money = Money.of("1234.56");
            String result = MoneyFormatUtil.format(money, "#,##0.00");

            assertThat(result).isNotBlank();
        }
    }

    @Nested
    @DisplayName("formatCompact方法测试")
    class FormatCompactTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(MoneyFormatUtil.formatCompact(null)).isEmpty();
        }

        @Test
        @DisplayName("小金额正常显示")
        void testSmallAmount() {
            Money money = Money.of("1234.56");
            String result = MoneyFormatUtil.formatCompact(money);

            assertThat(result).doesNotContain("万");
        }

        @Test
        @DisplayName("万级金额")
        void testWan() {
            Money money = Money.of("12345");
            String result = MoneyFormatUtil.formatCompact(money);

            assertThat(result).contains("万");
        }

        @Test
        @DisplayName("亿级金额")
        void testYi() {
            Money money = Money.of("123456789");
            String result = MoneyFormatUtil.formatCompact(money);

            assertThat(result).contains("亿");
        }

        @Test
        @DisplayName("负数紧凑格式")
        void testNegative() {
            Money money = Money.of("-12345");
            String result = MoneyFormatUtil.formatCompact(money);

            assertThat(result).startsWith("-");
        }
    }

    @Nested
    @DisplayName("formatPercent方法测试")
    class FormatPercentTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(MoneyFormatUtil.formatPercent(null, 2)).isEmpty();
        }

        @Test
        @DisplayName("格式化百分比")
        void testFormatPercent() {
            String result = MoneyFormatUtil.formatPercent(new BigDecimal("0.05"), 1);
            assertThat(result).contains("5");
            assertThat(result).contains("%");
        }
    }
}
