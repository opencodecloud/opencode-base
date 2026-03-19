package cloud.opencode.base.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LogLevel 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("LogLevel 测试")
class LogLevelTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("所有级别存在")
        void testAllLevelsExist() {
            assertThat(LogLevel.values())
                .containsExactly(
                    LogLevel.TRACE,
                    LogLevel.DEBUG,
                    LogLevel.INFO,
                    LogLevel.WARN,
                    LogLevel.ERROR,
                    LogLevel.OFF
                );
        }

        @Test
        @DisplayName("级别值正确")
        void testLevelValues() {
            assertThat(LogLevel.TRACE.getLevel()).isEqualTo(1);
            assertThat(LogLevel.DEBUG.getLevel()).isEqualTo(2);
            assertThat(LogLevel.INFO.getLevel()).isEqualTo(3);
            assertThat(LogLevel.WARN.getLevel()).isEqualTo(4);
            assertThat(LogLevel.ERROR.getLevel()).isEqualTo(5);
            assertThat(LogLevel.OFF.getLevel()).isEqualTo(6);
        }

        @Test
        @DisplayName("级别名称正确")
        void testLevelNames() {
            assertThat(LogLevel.TRACE.getName()).isEqualTo("TRACE");
            assertThat(LogLevel.DEBUG.getName()).isEqualTo("DEBUG");
            assertThat(LogLevel.INFO.getName()).isEqualTo("INFO");
            assertThat(LogLevel.WARN.getName()).isEqualTo("WARN");
            assertThat(LogLevel.ERROR.getName()).isEqualTo("ERROR");
            assertThat(LogLevel.OFF.getName()).isEqualTo("OFF");
        }
    }

    @Nested
    @DisplayName("isGreaterOrEqual方法测试")
    class IsGreaterOrEqualTests {

        @Test
        @DisplayName("相同级别返回true")
        void testSameLevel() {
            assertThat(LogLevel.INFO.isGreaterOrEqual(LogLevel.INFO)).isTrue();
        }

        @Test
        @DisplayName("更高级别返回true")
        void testHigherLevel() {
            assertThat(LogLevel.ERROR.isGreaterOrEqual(LogLevel.INFO)).isTrue();
            assertThat(LogLevel.WARN.isGreaterOrEqual(LogLevel.DEBUG)).isTrue();
        }

        @Test
        @DisplayName("更低级别返回false")
        void testLowerLevel() {
            assertThat(LogLevel.DEBUG.isGreaterOrEqual(LogLevel.INFO)).isFalse();
            assertThat(LogLevel.TRACE.isGreaterOrEqual(LogLevel.ERROR)).isFalse();
        }
    }

    @Nested
    @DisplayName("isEnabledFor方法测试")
    class IsEnabledForTests {

        @Test
        @DisplayName("阈值以上级别启用")
        void testEnabledAboveThreshold() {
            assertThat(LogLevel.INFO.isEnabledFor(LogLevel.DEBUG)).isTrue();
            assertThat(LogLevel.ERROR.isEnabledFor(LogLevel.INFO)).isTrue();
        }

        @Test
        @DisplayName("阈值以下级别禁用")
        void testDisabledBelowThreshold() {
            assertThat(LogLevel.DEBUG.isEnabledFor(LogLevel.INFO)).isFalse();
            assertThat(LogLevel.TRACE.isEnabledFor(LogLevel.WARN)).isFalse();
        }
    }

    @Nested
    @DisplayName("fromName方法测试")
    class FromNameTests {

        @Test
        @DisplayName("解析有效级别名称")
        void testParseValidName() {
            assertThat(LogLevel.fromName("TRACE")).isEqualTo(LogLevel.TRACE);
            assertThat(LogLevel.fromName("DEBUG")).isEqualTo(LogLevel.DEBUG);
            assertThat(LogLevel.fromName("INFO")).isEqualTo(LogLevel.INFO);
            assertThat(LogLevel.fromName("WARN")).isEqualTo(LogLevel.WARN);
            assertThat(LogLevel.fromName("ERROR")).isEqualTo(LogLevel.ERROR);
            assertThat(LogLevel.fromName("OFF")).isEqualTo(LogLevel.OFF);
        }

        @Test
        @DisplayName("解析小写名称")
        void testParseLowerCase() {
            assertThat(LogLevel.fromName("info")).isEqualTo(LogLevel.INFO);
            assertThat(LogLevel.fromName("debug")).isEqualTo(LogLevel.DEBUG);
        }

        @Test
        @DisplayName("解析混合大小写名称")
        void testParseMixedCase() {
            assertThat(LogLevel.fromName("Info")).isEqualTo(LogLevel.INFO);
            assertThat(LogLevel.fromName("WaRn")).isEqualTo(LogLevel.WARN);
        }

        @Test
        @DisplayName("无效名称抛出异常")
        void testInvalidNameThrows() {
            assertThatThrownBy(() -> LogLevel.fromName("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown log level");
        }

        @Test
        @DisplayName("null名称抛出异常")
        void testNullNameThrows() {
            assertThatThrownBy(() -> LogLevel.fromName(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("fromNameOrDefault方法测试")
    class FromNameOrDefaultTests {

        @Test
        @DisplayName("解析有效名称")
        void testParseValidName() {
            assertThat(LogLevel.fromNameOrDefault("INFO", LogLevel.DEBUG))
                .isEqualTo(LogLevel.INFO);
        }

        @Test
        @DisplayName("无效名称返回默认值")
        void testInvalidNameReturnsDefault() {
            assertThat(LogLevel.fromNameOrDefault("INVALID", LogLevel.WARN))
                .isEqualTo(LogLevel.WARN);
        }

        @Test
        @DisplayName("null名称返回默认值")
        void testNullNameReturnsDefault() {
            assertThat(LogLevel.fromNameOrDefault(null, LogLevel.ERROR))
                .isEqualTo(LogLevel.ERROR);
        }

        @Test
        @DisplayName("空白名称返回默认值")
        void testBlankNameReturnsDefault() {
            assertThat(LogLevel.fromNameOrDefault("  ", LogLevel.INFO))
                .isEqualTo(LogLevel.INFO);
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("返回级别名称")
        void testToStringReturnsName() {
            assertThat(LogLevel.INFO.toString()).isEqualTo("INFO");
            assertThat(LogLevel.ERROR.toString()).isEqualTo("ERROR");
        }
    }
}
