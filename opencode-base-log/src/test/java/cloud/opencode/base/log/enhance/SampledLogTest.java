package cloud.opencode.base.log.enhance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * SampledLog 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("SampledLog 测试")
class SampledLogTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(SampledLog.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = SampledLog.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("sample(double)方法测试")
    class SampleDoubleTests {

        @Test
        @DisplayName("创建概率采样日志记录器")
        void testSample() {
            SampledLog.SampledLogger logger = SampledLog.sample(0.5);
            assertThat(logger).isNotNull();
        }

        @Test
        @DisplayName("采样率为0")
        void testSampleRateZero() {
            SampledLog.SampledLogger logger = SampledLog.sample(0.0);
            assertThat(logger).isNotNull();
            assertThat(logger.shouldLog()).isFalse();
        }

        @Test
        @DisplayName("采样率为1")
        void testSampleRateOne() {
            SampledLog.SampledLogger logger = SampledLog.sample(1.0);
            assertThat(logger).isNotNull();
            assertThat(logger.shouldLog()).isTrue();
        }

        @Test
        @DisplayName("采样率小于0抛出异常")
        void testSampleRateNegative() {
            assertThatThrownBy(() -> SampledLog.sample(-0.1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("采样率大于1抛出异常")
        void testSampleRateGreaterThanOne() {
            assertThatThrownBy(() -> SampledLog.sample(1.1))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("sampleByTime方法测试")
    class SampleByTimeTests {

        @Test
        @DisplayName("创建时间采样日志记录器")
        void testSampleByTime() {
            SampledLog.SampledLogger logger = SampledLog.sampleByTime(Duration.ofSeconds(5));
            assertThat(logger).isNotNull();
        }

        @Test
        @DisplayName("第一次调用shouldLog返回true")
        void testSampleByTimeFirstCall() {
            SampledLog.SampledLogger logger = SampledLog.sampleByTime(Duration.ofMinutes(1));
            assertThat(logger.shouldLog()).isTrue();
        }

        @Test
        @DisplayName("间隔内调用shouldLog返回false")
        void testSampleByTimeWithinInterval() {
            SampledLog.SampledLogger logger = SampledLog.sampleByTime(Duration.ofMinutes(1));
            logger.shouldLog(); // First call
            assertThat(logger.shouldLog()).isFalse();
        }
    }

    @Nested
    @DisplayName("sampleByCount方法测试")
    class SampleByCountTests {

        @Test
        @DisplayName("创建计数采样日志记录器")
        void testSampleByCount() {
            SampledLog.SampledLogger logger = SampledLog.sampleByCount(100);
            assertThat(logger).isNotNull();
        }

        @Test
        @DisplayName("计数小于1抛出异常")
        void testSampleByCountInvalid() {
            assertThatThrownBy(() -> SampledLog.sampleByCount(0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("每N次调用shouldLog返回true")
        void testSampleByCountEveryN() {
            SampledLog.SampledLogger logger = SampledLog.sampleByCount(3);

            // 1st, 2nd should be false, 3rd should be true
            assertThat(logger.shouldLog()).isFalse();
            assertThat(logger.shouldLog()).isFalse();
            assertThat(logger.shouldLog()).isTrue();
            assertThat(logger.shouldLog()).isFalse();
            assertThat(logger.shouldLog()).isFalse();
            assertThat(logger.shouldLog()).isTrue();
        }
    }

    @Nested
    @DisplayName("SampledLogger接口测试")
    class SampledLoggerInterfaceTests {

        @Test
        @DisplayName("接口方法存在")
        void testInterfaceMethods() {
            SampledLog.SampledLogger logger = SampledLog.sample(1.0);

            assertThatCode(() -> {
                logger.trace("trace {}", "arg");
                logger.debug("debug {}", "arg");
                logger.info("info {}", "arg");
                logger.warn("warn {}", "arg");
                logger.error("error {}", "arg");
                logger.shouldLog();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("日志方法测试")
    class LogMethodTests {

        @Test
        @DisplayName("trace方法可调用")
        void testTrace() {
            SampledLog.SampledLogger logger = SampledLog.sample(1.0);
            assertThatCode(() -> logger.trace("message {}", "arg")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("debug方法可调用")
        void testDebug() {
            SampledLog.SampledLogger logger = SampledLog.sample(1.0);
            assertThatCode(() -> logger.debug("message {}", "arg")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("info方法可调用")
        void testInfo() {
            SampledLog.SampledLogger logger = SampledLog.sample(1.0);
            assertThatCode(() -> logger.info("message {}", "arg")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("warn方法可调用")
        void testWarn() {
            SampledLog.SampledLogger logger = SampledLog.sample(1.0);
            assertThatCode(() -> logger.warn("message {}", "arg")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("error方法可调用")
        void testError() {
            SampledLog.SampledLogger logger = SampledLog.sample(1.0);
            assertThatCode(() -> logger.error("message {}", "arg")).doesNotThrowAnyException();
        }
    }
}
