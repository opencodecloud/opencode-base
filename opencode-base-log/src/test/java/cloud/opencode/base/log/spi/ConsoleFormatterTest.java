package cloud.opencode.base.log.spi;

import cloud.opencode.base.log.LogLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ConsoleFormatter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
@DisplayName("ConsoleFormatter 测试")
class ConsoleFormatterTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("显式启用颜色")
        void explicitColorEnabled() {
            ConsoleFormatter formatter = new ConsoleFormatter(true);
            assertThat(formatter.isColorEnabled()).isTrue();
        }

        @Test
        @DisplayName("显式禁用颜色")
        void explicitColorDisabled() {
            ConsoleFormatter formatter = new ConsoleFormatter(false);
            assertThat(formatter.isColorEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("无颜色格式化测试")
    class PlainFormatTests {

        private final ConsoleFormatter formatter = new ConsoleFormatter(false);

        @Test
        @DisplayName("INFO 级别格式化为正确的纯文本")
        void formatInfoPlain() {
            String result = formatter.format(LogLevel.INFO, "2026-04-03 10:00:00",
                    "main", "com.example.App", "Started");
            assertThat(result).isEqualTo("2026-04-03 10:00:00 [main] INFO  com.example.App - Started");
        }

        @Test
        @DisplayName("ERROR 级别格式化为正确的纯文本")
        void formatErrorPlain() {
            String result = formatter.format(LogLevel.ERROR, "2026-04-03 10:00:00",
                    "worker-1", "com.example.Service", "Something failed");
            assertThat(result).isEqualTo("2026-04-03 10:00:00 [worker-1] ERROR com.example.Service - Something failed");
        }

        @Test
        @DisplayName("WARN 级别名称填充至 5 字符")
        void formatWarnPadded() {
            String result = formatter.format(LogLevel.WARN, "ts", "t", "logger", "msg");
            assertThat(result).isEqualTo("ts [t] WARN  logger - msg");
        }

        @Test
        @DisplayName("TRACE 级别格式化正确")
        void formatTrace() {
            String result = formatter.format(LogLevel.TRACE, "ts", "t", "logger", "msg");
            assertThat(result).isEqualTo("ts [t] TRACE logger - msg");
        }

        @Test
        @DisplayName("DEBUG 级别格式化正确")
        void formatDebug() {
            String result = formatter.format(LogLevel.DEBUG, "ts", "t", "logger", "msg");
            assertThat(result).isEqualTo("ts [t] DEBUG logger - msg");
        }

        @Test
        @DisplayName("不包含 ANSI 转义码")
        void noAnsiCodes() {
            String result = formatter.format(LogLevel.ERROR, "ts", "t", "logger", "msg");
            assertThat(result).doesNotContain("\033[");
        }
    }

    @Nested
    @DisplayName("颜色格式化测试")
    class ColorFormatTests {

        private final ConsoleFormatter formatter = new ConsoleFormatter(true);

        @Test
        @DisplayName("ERROR 级别包含粗体红色 ANSI 代码")
        void errorUseBoldRed() {
            String result = formatter.format(LogLevel.ERROR, "ts", "t", "logger", "msg");
            assertThat(result).contains("\033[1;31m");
            assertThat(result).contains("\033[0m");
            assertThat(result).contains("ERROR");
        }

        @Test
        @DisplayName("WARN 级别包含粗体黄色 ANSI 代码")
        void warnUseBoldYellow() {
            String result = formatter.format(LogLevel.WARN, "ts", "t", "logger", "msg");
            assertThat(result).contains("\033[1;33m");
            assertThat(result).contains("WARN");
        }

        @Test
        @DisplayName("INFO 级别包含粗体绿色 ANSI 代码")
        void infoUseBoldGreen() {
            String result = formatter.format(LogLevel.INFO, "ts", "t", "logger", "msg");
            assertThat(result).contains("\033[1;32m");
            assertThat(result).contains("INFO");
        }

        @Test
        @DisplayName("DEBUG 级别包含亮蓝色 ANSI 代码")
        void debugUseBrightBlue() {
            String result = formatter.format(LogLevel.DEBUG, "ts", "t", "logger", "msg");
            assertThat(result).contains("\033[94m");
            assertThat(result).contains("DEBUG");
        }

        @Test
        @DisplayName("TRACE 级别包含亮黑色 ANSI 代码")
        void traceUseBrightBlack() {
            String result = formatter.format(LogLevel.TRACE, "ts", "t", "logger", "msg");
            assertThat(result).contains("\033[90m");
            assertThat(result).contains("TRACE");
        }

        @Test
        @DisplayName("颜色仅应用于级别部分")
        void colorOnlyOnLevel() {
            String result = formatter.format(LogLevel.INFO, "2026-04-03", "main", "com.App", "hello");
            // timestamp should not be colored
            assertThat(result).startsWith("2026-04-03 [main] ");
            // message should not be colored (appears after RESET)
            assertThat(result).endsWith("com.App - hello");
        }
    }

    @Nested
    @DisplayName("formatWithColor 测试")
    class FormatWithColorTests {

        @Test
        @DisplayName("启用颜色时包装文本")
        void wrapsWhenEnabled() {
            ConsoleFormatter formatter = new ConsoleFormatter(true);
            String result = formatter.formatWithColor("hello", AnsiColor.RED);
            assertThat(result).isEqualTo("\033[31mhello\033[0m");
        }

        @Test
        @DisplayName("禁用颜色时返回纯文本")
        void plainWhenDisabled() {
            ConsoleFormatter formatter = new ConsoleFormatter(false);
            String result = formatter.formatWithColor("hello", AnsiColor.RED);
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("null 文本抛出 NullPointerException")
        void nullTextThrows() {
            ConsoleFormatter formatter = new ConsoleFormatter(true);
            assertThatThrownBy(() -> formatter.formatWithColor(null, AnsiColor.RED))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 颜色抛出 NullPointerException")
        void nullColorThrows() {
            ConsoleFormatter formatter = new ConsoleFormatter(true);
            assertThatThrownBy(() -> formatter.formatWithColor("text", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("format 参数校验测试")
    class FormatValidationTests {

        private final ConsoleFormatter formatter = new ConsoleFormatter(false);

        @Test
        @DisplayName("null level 抛出 NullPointerException")
        void nullLevel() {
            assertThatThrownBy(() -> formatter.format(null, "ts", "t", "l", "m"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("level");
        }

        @Test
        @DisplayName("null timestamp 抛出 NullPointerException")
        void nullTimestamp() {
            assertThatThrownBy(() -> formatter.format(LogLevel.INFO, null, "t", "l", "m"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("timestamp");
        }

        @Test
        @DisplayName("null threadName 抛出 NullPointerException")
        void nullThreadName() {
            assertThatThrownBy(() -> formatter.format(LogLevel.INFO, "ts", null, "l", "m"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("threadName");
        }

        @Test
        @DisplayName("null loggerName 抛出 NullPointerException")
        void nullLoggerName() {
            assertThatThrownBy(() -> formatter.format(LogLevel.INFO, "ts", "t", null, "m"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("loggerName");
        }

        @Test
        @DisplayName("null message 抛出 NullPointerException")
        void nullMessage() {
            assertThatThrownBy(() -> formatter.format(LogLevel.INFO, "ts", "t", "l", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("message");
        }
    }

    @Nested
    @DisplayName("isAnsiSupported 测试")
    class AnsiSupportTests {

        @Test
        @DisplayName("isAnsiSupported 返回布尔值不抛异常")
        void isAnsiSupportedDoesNotThrow() {
            // Just verify it runs without exceptions
            boolean supported = ConsoleFormatter.isAnsiSupported();
            assertThat(supported).isIn(true, false);
        }
    }
}
