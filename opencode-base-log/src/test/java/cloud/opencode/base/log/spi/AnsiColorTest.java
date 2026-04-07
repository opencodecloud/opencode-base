package cloud.opencode.base.log.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AnsiColor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
@DisplayName("AnsiColor 测试")
class AnsiColorTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("所有枚举值存在且数量正确")
        void allEnumValuesExist() {
            AnsiColor[] values = AnsiColor.values();
            assertThat(values).hasSize(21);
        }

        @Test
        @DisplayName("可通过名称获取枚举值")
        void canResolveByName() {
            assertThat(AnsiColor.valueOf("RESET")).isEqualTo(AnsiColor.RESET);
            assertThat(AnsiColor.valueOf("RED")).isEqualTo(AnsiColor.RED);
            assertThat(AnsiColor.valueOf("BOLD_RED")).isEqualTo(AnsiColor.BOLD_RED);
        }
    }

    @Nested
    @DisplayName("getCode 测试")
    class GetCodeTests {

        @Test
        @DisplayName("RESET 返回正确的 ANSI 代码")
        void resetCode() {
            assertThat(AnsiColor.RESET.getCode()).isEqualTo("\033[0m");
        }

        @Test
        @DisplayName("标准颜色返回正确的 ANSI 代码")
        void standardColorCodes() {
            assertThat(AnsiColor.BLACK.getCode()).isEqualTo("\033[30m");
            assertThat(AnsiColor.RED.getCode()).isEqualTo("\033[31m");
            assertThat(AnsiColor.GREEN.getCode()).isEqualTo("\033[32m");
            assertThat(AnsiColor.YELLOW.getCode()).isEqualTo("\033[33m");
            assertThat(AnsiColor.BLUE.getCode()).isEqualTo("\033[34m");
            assertThat(AnsiColor.MAGENTA.getCode()).isEqualTo("\033[35m");
            assertThat(AnsiColor.CYAN.getCode()).isEqualTo("\033[36m");
            assertThat(AnsiColor.WHITE.getCode()).isEqualTo("\033[37m");
        }

        @Test
        @DisplayName("亮色返回正确的 ANSI 代码")
        void brightColorCodes() {
            assertThat(AnsiColor.BRIGHT_BLACK.getCode()).isEqualTo("\033[90m");
            assertThat(AnsiColor.BRIGHT_RED.getCode()).isEqualTo("\033[91m");
            assertThat(AnsiColor.BRIGHT_GREEN.getCode()).isEqualTo("\033[92m");
            assertThat(AnsiColor.BRIGHT_YELLOW.getCode()).isEqualTo("\033[93m");
            assertThat(AnsiColor.BRIGHT_BLUE.getCode()).isEqualTo("\033[94m");
            assertThat(AnsiColor.BRIGHT_MAGENTA.getCode()).isEqualTo("\033[95m");
            assertThat(AnsiColor.BRIGHT_CYAN.getCode()).isEqualTo("\033[96m");
            assertThat(AnsiColor.BRIGHT_WHITE.getCode()).isEqualTo("\033[97m");
        }

        @Test
        @DisplayName("粗体变体返回正确的 ANSI 代码")
        void boldCodes() {
            assertThat(AnsiColor.BOLD.getCode()).isEqualTo("\033[1m");
            assertThat(AnsiColor.BOLD_RED.getCode()).isEqualTo("\033[1;31m");
            assertThat(AnsiColor.BOLD_YELLOW.getCode()).isEqualTo("\033[1;33m");
            assertThat(AnsiColor.BOLD_GREEN.getCode()).isEqualTo("\033[1;32m");
        }

        @ParameterizedTest
        @EnumSource(AnsiColor.class)
        @DisplayName("所有颜色代码以 ESC[ 开头")
        void allCodesStartWithEsc(AnsiColor color) {
            assertThat(color.getCode()).startsWith("\033[");
        }
    }

    @Nested
    @DisplayName("wrap 测试")
    class WrapTests {

        @Test
        @DisplayName("wrap 使用颜色代码包装文本并添加 RESET")
        void wrapAddsColorAndReset() {
            String result = AnsiColor.RED.wrap("hello");
            assertThat(result).isEqualTo("\033[31mhello\033[0m");
        }

        @Test
        @DisplayName("wrap 粗体颜色正确包装")
        void wrapBoldColor() {
            String result = AnsiColor.BOLD_RED.wrap("error");
            assertThat(result).isEqualTo("\033[1;31merror\033[0m");
        }

        @Test
        @DisplayName("wrap 空字符串返回颜色代码加 RESET")
        void wrapEmptyString() {
            String result = AnsiColor.GREEN.wrap("");
            assertThat(result).isEqualTo("\033[32m\033[0m");
        }

        @Test
        @DisplayName("wrap null 文本抛出 NullPointerException")
        void wrapNullThrows() {
            assertThatThrownBy(() -> AnsiColor.RED.wrap(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("text must not be null");
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString 返回与 getCode 相同的值")
        void toStringReturnsCode() {
            for (AnsiColor color : AnsiColor.values()) {
                assertThat(color.toString()).isEqualTo(color.getCode());
            }
        }
    }
}
