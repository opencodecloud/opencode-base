package cloud.opencode.base.money.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * MoneyErrorCode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
@DisplayName("MoneyErrorCode 测试")
class MoneyErrorCodeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含UNKNOWN")
        void testUnknown() {
            assertThat(MoneyErrorCode.UNKNOWN).isNotNull();
            assertThat(MoneyErrorCode.UNKNOWN.getCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("包含金额错误码")
        void testAmountCodes() {
            assertThat(MoneyErrorCode.INVALID_AMOUNT).isNotNull();
            assertThat(MoneyErrorCode.AMOUNT_FORMAT_ERROR).isNotNull();
            assertThat(MoneyErrorCode.AMOUNT_OVERFLOW).isNotNull();
            assertThat(MoneyErrorCode.AMOUNT_PRECISION_ERROR).isNotNull();
            assertThat(MoneyErrorCode.AMOUNT_NEGATIVE).isNotNull();
            assertThat(MoneyErrorCode.NULL_AMOUNT).isNotNull();
        }

        @Test
        @DisplayName("包含币种错误码")
        void testCurrencyCodes() {
            assertThat(MoneyErrorCode.CURRENCY_MISMATCH).isNotNull();
            assertThat(MoneyErrorCode.UNSUPPORTED_CURRENCY).isNotNull();
            assertThat(MoneyErrorCode.NULL_CURRENCY).isNotNull();
        }

        @Test
        @DisplayName("包含汇率错误码")
        void testRateCodes() {
            assertThat(MoneyErrorCode.RATE_NOT_FOUND).isNotNull();
            assertThat(MoneyErrorCode.RATE_EXPIRED).isNotNull();
            assertThat(MoneyErrorCode.RATE_INVALID).isNotNull();
        }

        @Test
        @DisplayName("包含计算错误码")
        void testCalcCodes() {
            assertThat(MoneyErrorCode.ALLOCATION_ERROR).isNotNull();
            assertThat(MoneyErrorCode.ZERO_DIVISOR).isNotNull();
            assertThat(MoneyErrorCode.ZERO_RATIO).isNotNull();
            assertThat(MoneyErrorCode.INVALID_RATIO).isNotNull();
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @ParameterizedTest
        @EnumSource(MoneyErrorCode.class)
        @DisplayName("所有错误码都有code")
        void testAllHaveCode(MoneyErrorCode code) {
            assertThat(code.getCode()).isGreaterThanOrEqualTo(0);
        }

        @ParameterizedTest
        @EnumSource(MoneyErrorCode.class)
        @DisplayName("所有错误码都有消息")
        void testAllHaveMessage(MoneyErrorCode code) {
            assertThat(code.getMessage()).isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(MoneyErrorCode.class)
        @DisplayName("所有错误码都有中文消息")
        void testAllHaveMessageZh(MoneyErrorCode code) {
            assertThat(code.getMessageZh()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("错误码范围测试")
    class CodeRangeTests {

        @Test
        @DisplayName("金额错误码在1xxx范围")
        void testAmountCodeRange() {
            assertThat(MoneyErrorCode.INVALID_AMOUNT.getCode()).isBetween(1001, 1999);
        }

        @Test
        @DisplayName("币种错误码在2xxx范围")
        void testCurrencyCodeRange() {
            assertThat(MoneyErrorCode.CURRENCY_MISMATCH.getCode()).isBetween(2001, 2999);
        }

        @Test
        @DisplayName("汇率错误码在3xxx范围")
        void testRateCodeRange() {
            assertThat(MoneyErrorCode.RATE_NOT_FOUND.getCode()).isBetween(3001, 3999);
        }

        @Test
        @DisplayName("计算错误码在4xxx范围")
        void testCalcCodeRange() {
            assertThat(MoneyErrorCode.ALLOCATION_ERROR.getCode()).isBetween(4001, 4999);
        }
    }
}
