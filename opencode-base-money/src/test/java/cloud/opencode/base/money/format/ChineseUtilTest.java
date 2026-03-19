package cloud.opencode.base.money.format;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * ChineseUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("ChineseUtil 测试")
class ChineseUtilTest {

    @Nested
    @DisplayName("toUpperCase方法测试")
    class ToUpperCaseTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(ChineseUtil.toUpperCase(null)).isEmpty();
        }

        @Test
        @DisplayName("零")
        void testZero() {
            String result = ChineseUtil.toUpperCase(BigDecimal.ZERO);
            assertThat(result).isEqualTo("零元整");
        }

        @Test
        @DisplayName("整数金额")
        void testIntegerAmount() {
            String result = ChineseUtil.toUpperCase(new BigDecimal("100"));
            assertThat(result).contains("壹佰");
            assertThat(result).contains("元整");
        }

        @Test
        @DisplayName("带角分的金额")
        void testWithJiaoFen() {
            String result = ChineseUtil.toUpperCase(new BigDecimal("1234.56"));
            assertThat(result).contains("壹仟");
            assertThat(result).contains("贰佰");
            assertThat(result).contains("叁拾");
            assertThat(result).contains("肆元");
            assertThat(result).contains("伍角");
            assertThat(result).contains("陆分");
        }

        @Test
        @DisplayName("只有角")
        void testOnlyJiao() {
            String result = ChineseUtil.toUpperCase(new BigDecimal("100.50"));
            assertThat(result).contains("伍角");
            assertThat(result).doesNotContain("分");
        }

        @Test
        @DisplayName("只有分")
        void testOnlyFen() {
            String result = ChineseUtil.toUpperCase(new BigDecimal("100.05"));
            assertThat(result).contains("零");
            assertThat(result).contains("伍分");
        }

        @Test
        @DisplayName("负数")
        void testNegative() {
            String result = ChineseUtil.toUpperCase(new BigDecimal("-100"));
            assertThat(result).startsWith("负");
        }

        @ParameterizedTest
        @CsvSource({
            "1, 壹元整",
            "10, 壹拾元整",
            "100, 壹佰元整",
            "1000, 壹仟元整"
        })
        @DisplayName("基本数值转换")
        void testBasicValues(String amount, String expected) {
            String result = ChineseUtil.toUpperCase(new BigDecimal(amount));
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("万")
        void testWan() {
            String result = ChineseUtil.toUpperCase(new BigDecimal("10000"));
            assertThat(result).contains("万");
        }

        @Test
        @DisplayName("亿")
        void testYi() {
            String result = ChineseUtil.toUpperCase(new BigDecimal("100000000"));
            assertThat(result).contains("亿");
        }
    }

    @Nested
    @DisplayName("toSimplified方法测试")
    class ToSimplifiedTests {

        @Test
        @DisplayName("null返回空字符串")
        void testNull() {
            assertThat(ChineseUtil.toSimplified(null)).isEmpty();
        }

        @Test
        @DisplayName("整数")
        void testInteger() {
            String result = ChineseUtil.toSimplified(new BigDecimal("123"));
            assertThat(result).contains("一");
            assertThat(result).contains("二");
            assertThat(result).contains("三");
        }

        @Test
        @DisplayName("带小数")
        void testWithDecimal() {
            String result = ChineseUtil.toSimplified(new BigDecimal("123.45"));
            assertThat(result).contains("点");
            assertThat(result).contains("四");
            assertThat(result).contains("五");
        }

        @Test
        @DisplayName("负数")
        void testNegative() {
            String result = ChineseUtil.toSimplified(new BigDecimal("-100"));
            assertThat(result).startsWith("负");
        }
    }
}
