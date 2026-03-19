package cloud.opencode.base.log.perf;

import cloud.opencode.base.log.LogLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * SlowOperationConfig 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("SlowOperationConfig 测试")
class SlowOperationConfigTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(SlowOperationConfig.class.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("DEFAULT_THRESHOLD是1秒")
        void testDefaultThreshold() {
            assertThat(SlowOperationConfig.DEFAULT_THRESHOLD).isEqualTo(Duration.ofSeconds(1));
        }

        @Test
        @DisplayName("DEFAULT_LOG_LEVEL是WARN")
        void testDefaultLogLevel() {
            assertThat(SlowOperationConfig.DEFAULT_LOG_LEVEL).isEqualTo(LogLevel.WARN);
        }

        @Test
        @DisplayName("DEFAULT实例存在")
        void testDefaultInstance() {
            assertThat(SlowOperationConfig.DEFAULT).isNotNull();
            assertThat(SlowOperationConfig.DEFAULT.getThreshold()).isEqualTo(Duration.ofSeconds(1));
            assertThat(SlowOperationConfig.DEFAULT.getLogLevel()).isEqualTo(LogLevel.WARN);
            assertThat(SlowOperationConfig.DEFAULT.isIncludeStackTrace()).isFalse();
            assertThat(SlowOperationConfig.DEFAULT.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("builder创建默认配置")
        void testBuilderDefaults() {
            SlowOperationConfig config = SlowOperationConfig.builder().build();

            assertThat(config.getThreshold()).isEqualTo(Duration.ofSeconds(1));
            assertThat(config.getLogLevel()).isEqualTo(LogLevel.WARN);
            assertThat(config.isIncludeStackTrace()).isFalse();
            assertThat(config.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("threshold(Duration)设置阈值")
        void testThresholdDuration() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .threshold(Duration.ofMillis(500))
                .build();

            assertThat(config.getThreshold()).isEqualTo(Duration.ofMillis(500));
        }

        @Test
        @DisplayName("thresholdMillis设置毫秒阈值")
        void testThresholdMillis() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .thresholdMillis(250)
                .build();

            assertThat(config.getThresholdMillis()).isEqualTo(250);
        }

        @Test
        @DisplayName("logLevel设置日志级别")
        void testLogLevel() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .logLevel(LogLevel.ERROR)
                .build();

            assertThat(config.getLogLevel()).isEqualTo(LogLevel.ERROR);
        }

        @Test
        @DisplayName("includeStackTrace设置堆栈跟踪")
        void testIncludeStackTrace() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .includeStackTrace(true)
                .build();

            assertThat(config.isIncludeStackTrace()).isTrue();
        }

        @Test
        @DisplayName("enabled启用检测")
        void testEnabled() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .disabled()
                .enabled()
                .build();

            assertThat(config.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("disabled禁用检测")
        void testDisabled() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .disabled()
                .build();

            assertThat(config.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("threshold(null)抛出异常")
        void testThresholdNull() {
            assertThatThrownBy(() -> SlowOperationConfig.builder().threshold(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("logLevel(null)抛出异常")
        void testLogLevelNull() {
            assertThatThrownBy(() -> SlowOperationConfig.builder().logLevel(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("链式调用")
        void testChainedCalls() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .threshold(Duration.ofMillis(300))
                .logLevel(LogLevel.INFO)
                .includeStackTrace(true)
                .enabled()
                .build();

            assertThat(config.getThresholdMillis()).isEqualTo(300);
            assertThat(config.getLogLevel()).isEqualTo(LogLevel.INFO);
            assertThat(config.isIncludeStackTrace()).isTrue();
            assertThat(config.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getThreshold返回阈值")
        void testGetThreshold() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .threshold(Duration.ofMinutes(1))
                .build();

            assertThat(config.getThreshold()).isEqualTo(Duration.ofMinutes(1));
        }

        @Test
        @DisplayName("getThresholdMillis返回毫秒阈值")
        void testGetThresholdMillis() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .threshold(Duration.ofSeconds(5))
                .build();

            assertThat(config.getThresholdMillis()).isEqualTo(5000);
        }
    }

    @Nested
    @DisplayName("isSlow方法测试")
    class IsSlowTests {

        @Test
        @DisplayName("超过阈值返回true")
        void testIsSlowTrue() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .thresholdMillis(100)
                .build();

            assertThat(config.isSlow(150)).isTrue();
        }

        @Test
        @DisplayName("未超过阈值返回false")
        void testIsSlowFalse() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .thresholdMillis(100)
                .build();

            assertThat(config.isSlow(50)).isFalse();
        }

        @Test
        @DisplayName("等于阈值返回false")
        void testIsSlowEqual() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .thresholdMillis(100)
                .build();

            assertThat(config.isSlow(100)).isFalse();
        }

        @Test
        @DisplayName("禁用时总是返回false")
        void testIsSlowDisabled() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .thresholdMillis(100)
                .disabled()
                .build();

            assertThat(config.isSlow(1000)).isFalse();
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含所有字段")
        void testToString() {
            SlowOperationConfig config = SlowOperationConfig.builder()
                .thresholdMillis(500)
                .logLevel(LogLevel.ERROR)
                .includeStackTrace(true)
                .enabled()
                .build();

            String str = config.toString();
            assertThat(str).contains("SlowOperationConfig");
            assertThat(str).contains("PT0.5S"); // Duration format
            assertThat(str).contains("ERROR");
            assertThat(str).contains("true");
        }
    }
}
