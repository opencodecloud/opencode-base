package cloud.opencode.base.money.validation;

import cloud.opencode.base.money.Money;
import cloud.opencode.base.money.exception.InvalidAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * MoneyValidator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("MoneyValidator 测试")
class MoneyValidatorTest {

    @Nested
    @DisplayName("validateAndParse方法测试")
    class ValidateAndParseTests {

        @Test
        @DisplayName("解析有效金额")
        void testValidAmount() {
            BigDecimal result = MoneyValidator.validateAndParse("100.50");
            assertThat(result).isEqualTo(new BigDecimal("100.50"));
        }

        @Test
        @DisplayName("解析整数")
        void testInteger() {
            BigDecimal result = MoneyValidator.validateAndParse("100");
            assertThat(result).isEqualTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("解析负数")
        void testNegative() {
            BigDecimal result = MoneyValidator.validateAndParse("-100.50");
            assertThat(result).isEqualTo(new BigDecimal("-100.50"));
        }

        @Test
        @DisplayName("null抛出异常")
        void testNull() {
            assertThatThrownBy(() -> MoneyValidator.validateAndParse(null))
                .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("空字符串抛出异常")
        void testEmpty() {
            assertThatThrownBy(() -> MoneyValidator.validateAndParse(""))
                .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("空白字符串抛出异常")
        void testBlank() {
            assertThatThrownBy(() -> MoneyValidator.validateAndParse("   "))
                .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("无效格式抛出异常")
        void testInvalidFormat() {
            assertThatThrownBy(() -> MoneyValidator.validateAndParse("abc"))
                .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("精度超限抛出异常")
        void testPrecisionError() {
            assertThatThrownBy(() -> MoneyValidator.validateAndParse("100.123"))
                .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("带maxScale参数")
        void testWithMaxScale() {
            BigDecimal result = MoneyValidator.validateAndParse("100.123", 3);
            assertThat(result).isEqualTo(new BigDecimal("100.123"));
        }

        @Test
        @DisplayName("去除首尾空格")
        void testTrimWhitespace() {
            BigDecimal result = MoneyValidator.validateAndParse("  100.50  ");
            assertThat(result).isEqualTo(new BigDecimal("100.50"));
        }
    }

    @Nested
    @DisplayName("validateRange方法测试")
    class ValidateRangeTests {

        @Test
        @DisplayName("有效范围内")
        void testValidRange() {
            assertThatCode(() -> MoneyValidator.validateRange(new BigDecimal("1000")))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("超过最大值抛出异常")
        void testExceedMax() {
            BigDecimal overflow = MoneyValidator.getMaxAmount().add(BigDecimal.ONE);
            assertThatThrownBy(() -> MoneyValidator.validateRange(overflow))
                .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("低于最小值抛出异常")
        void testBelowMin() {
            BigDecimal underflow = MoneyValidator.getMinAmount().subtract(BigDecimal.ONE);
            assertThatThrownBy(() -> MoneyValidator.validateRange(underflow))
                .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("null抛出异常")
        void testNull() {
            assertThatThrownBy(() -> MoneyValidator.validateRange((BigDecimal) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("validatePositive方法测试")
    class ValidatePositiveTests {

        @Test
        @DisplayName("正数通过")
        void testPositive() {
            assertThatCode(() -> MoneyValidator.validatePositive(Money.of("100")))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("零抛出异常")
        void testZero() {
            assertThatThrownBy(() -> MoneyValidator.validatePositive(Money.of("0")))
                .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("负数抛出异常")
        void testNegative() {
            assertThatThrownBy(() -> MoneyValidator.validatePositive(Money.of("-100")))
                .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("null抛出异常")
        void testNull() {
            assertThatThrownBy(() -> MoneyValidator.validatePositive(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("validateNonNegative方法测试")
    class ValidateNonNegativeTests {

        @Test
        @DisplayName("正数通过")
        void testPositive() {
            assertThatCode(() -> MoneyValidator.validateNonNegative(Money.of("100")))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("零通过")
        void testZero() {
            assertThatCode(() -> MoneyValidator.validateNonNegative(Money.of("0")))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("负数抛出异常")
        void testNegative() {
            assertThatThrownBy(() -> MoneyValidator.validateNonNegative(Money.of("-100")))
                .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    @DisplayName("validateNotZero方法测试")
    class ValidateNotZeroTests {

        @Test
        @DisplayName("非零通过")
        void testNonZero() {
            assertThatCode(() -> MoneyValidator.validateNotZero(Money.of("100")))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("零抛出异常")
        void testZero() {
            assertThatThrownBy(() -> MoneyValidator.validateNotZero(Money.of("0")))
                .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    @DisplayName("validateRange Money方法测试")
    class ValidateRangeMoneyTests {

        @Test
        @DisplayName("范围内通过")
        void testInRange() {
            assertThatCode(() -> MoneyValidator.validateRange(
                Money.of("50"), Money.of("0"), Money.of("100")))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("等于最小值通过")
        void testEqualMin() {
            assertThatCode(() -> MoneyValidator.validateRange(
                Money.of("0"), Money.of("0"), Money.of("100")))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("等于最大值通过")
        void testEqualMax() {
            assertThatCode(() -> MoneyValidator.validateRange(
                Money.of("100"), Money.of("0"), Money.of("100")))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("小于最小值抛出异常")
        void testBelowMin() {
            assertThatThrownBy(() -> MoneyValidator.validateRange(
                Money.of("-1"), Money.of("0"), Money.of("100")))
                .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("大于最大值抛出异常")
        void testAboveMax() {
            assertThatThrownBy(() -> MoneyValidator.validateRange(
                Money.of("101"), Money.of("0"), Money.of("100")))
                .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    @DisplayName("isValid方法测试")
    class IsValidTests {

        @Test
        @DisplayName("有效金额返回true")
        void testValid() {
            assertThat(MoneyValidator.isValid("100.50")).isTrue();
        }

        @Test
        @DisplayName("无效金额返回false")
        void testInvalid() {
            assertThat(MoneyValidator.isValid("abc")).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testNull() {
            assertThat(MoneyValidator.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("带maxScale参数")
        void testWithMaxScale() {
            assertThat(MoneyValidator.isValid("100.123", 3)).isTrue();
            assertThat(MoneyValidator.isValid("100.123", 2)).isFalse();
        }
    }

    @Nested
    @DisplayName("getMaxAmount方法测试")
    class GetMaxAmountTests {

        @Test
        @DisplayName("返回最大金额")
        void testGetMax() {
            BigDecimal max = MoneyValidator.getMaxAmount();
            assertThat(max).isEqualTo(new BigDecimal("100000000000"));
        }
    }

    @Nested
    @DisplayName("getMinAmount方法测试")
    class GetMinAmountTests {

        @Test
        @DisplayName("返回最小金额")
        void testGetMin() {
            BigDecimal min = MoneyValidator.getMinAmount();
            assertThat(min).isEqualTo(new BigDecimal("-100000000000"));
        }
    }
}
