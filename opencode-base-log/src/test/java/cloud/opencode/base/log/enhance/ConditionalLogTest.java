package cloud.opencode.base.log.enhance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * ConditionalLog 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("ConditionalLog 测试")
class ConditionalLogTest {

    @BeforeEach
    void setUp() {
        ConditionalLog.clearOnceTracking();
        ConditionalLog.clearRateLimitTracking();
    }

    @AfterEach
    void tearDown() {
        ConditionalLog.clearOnceTracking();
        ConditionalLog.clearRateLimitTracking();
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(ConditionalLog.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = ConditionalLog.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("环境检测测试")
    class EnvironmentDetectionTests {

        @Test
        @DisplayName("isDev方法可调用")
        void testIsDev() {
            assertThatCode(() -> ConditionalLog.isDev()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("isTest方法可调用")
        void testIsTest() {
            assertThatCode(() -> ConditionalLog.isTest()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("isProd方法可调用")
        void testIsProd() {
            assertThatCode(() -> ConditionalLog.isProd()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("devOnly方法测试")
    class DevOnlyTests {

        @Test
        @DisplayName("返回ConditionalLogger")
        void testDevOnlyReturnsLogger() {
            ConditionalLog.ConditionalLogger logger = ConditionalLog.devOnly();
            assertThat(logger).isNotNull();
        }

        @Test
        @DisplayName("可以调用所有日志方法")
        void testDevOnlyLogMethods() {
            ConditionalLog.ConditionalLogger logger = ConditionalLog.devOnly();
            assertThatCode(() -> {
                logger.trace("trace message");
                logger.trace("trace format {}", "arg");
                logger.debug("debug message");
                logger.debug("debug format {}", "arg");
                logger.info("info message");
                logger.info("info format {}", "arg");
                logger.warn("warn message");
                logger.warn("warn format {}", "arg");
                logger.warn("warn with exception", new RuntimeException());
                logger.error("error message");
                logger.error("error format {}", "arg");
                logger.error("error with exception", new RuntimeException());
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("testOnly方法测试")
    class TestOnlyTests {

        @Test
        @DisplayName("返回ConditionalLogger")
        void testTestOnlyReturnsLogger() {
            ConditionalLog.ConditionalLogger logger = ConditionalLog.testOnly();
            assertThat(logger).isNotNull();
        }
    }

    @Nested
    @DisplayName("prodOnly方法测试")
    class ProdOnlyTests {

        @Test
        @DisplayName("返回ConditionalLogger")
        void testProdOnlyReturnsLogger() {
            ConditionalLog.ConditionalLogger logger = ConditionalLog.prodOnly();
            assertThat(logger).isNotNull();
        }
    }

    @Nested
    @DisplayName("when(boolean)方法测试")
    class WhenBooleanTests {

        @Test
        @DisplayName("条件为true时返回启用的logger")
        void testWhenTrue() {
            ConditionalLog.ConditionalLogger logger = ConditionalLog.when(true);
            assertThat(logger).isNotNull();
        }

        @Test
        @DisplayName("条件为false时返回禁用的logger")
        void testWhenFalse() {
            ConditionalLog.ConditionalLogger logger = ConditionalLog.when(false);
            assertThat(logger).isNotNull();
        }
    }

    @Nested
    @DisplayName("when(Supplier)方法测试")
    class WhenSupplierTests {

        @Test
        @DisplayName("Supplier返回true时启用")
        void testWhenSupplierTrue() {
            ConditionalLog.ConditionalLogger logger = ConditionalLog.when(() -> true);
            assertThat(logger).isNotNull();
        }

        @Test
        @DisplayName("Supplier返回false时禁用")
        void testWhenSupplierFalse() {
            ConditionalLog.ConditionalLogger logger = ConditionalLog.when(() -> false);
            assertThat(logger).isNotNull();
        }

        @Test
        @DisplayName("Supplier返回null时禁用")
        void testWhenSupplierNull() {
            ConditionalLog.ConditionalLogger logger = ConditionalLog.when(() -> null);
            assertThat(logger).isNotNull();
        }
    }

    @Nested
    @DisplayName("once方法测试")
    class OnceTests {

        @Test
        @DisplayName("返回OnceLogger")
        void testOnceReturnsLogger() {
            ConditionalLog.OnceLogger logger = ConditionalLog.once();
            assertThat(logger).isNotNull();
        }

        @Test
        @DisplayName("可以调用所有日志方法")
        void testOnceLogMethods() {
            assertThatCode(() -> {
                ConditionalLog.once().trace("trace");
                ConditionalLog.once().trace("trace {}", "arg");
                ConditionalLog.once().debug("debug");
                ConditionalLog.once().debug("debug {}", "arg");
                ConditionalLog.once().info("info");
                ConditionalLog.once().info("info {}", "arg");
                ConditionalLog.once().warn("warn");
                ConditionalLog.once().warn("warn {}", "arg");
                ConditionalLog.once().warn("warn", new RuntimeException());
                ConditionalLog.once().error("error");
                ConditionalLog.once().error("error {}", "arg");
                ConditionalLog.once().error("error", new RuntimeException());
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("atMostEvery方法测试")
    class AtMostEveryTests {

        @Test
        @DisplayName("返回RateLimitedLogger")
        void testAtMostEveryReturnsLogger() {
            ConditionalLog.RateLimitedLogger logger = ConditionalLog.atMostEvery(Duration.ofSeconds(1));
            assertThat(logger).isNotNull();
        }

        @Test
        @DisplayName("可以调用所有日志方法")
        void testAtMostEveryLogMethods() {
            ConditionalLog.RateLimitedLogger logger = ConditionalLog.atMostEvery(Duration.ofMillis(100));
            assertThatCode(() -> {
                logger.trace("trace");
                logger.trace("trace {}", "arg");
                logger.debug("debug");
                logger.debug("debug {}", "arg");
                logger.info("info");
                logger.info("info {}", "arg");
                logger.warn("warn");
                logger.warn("warn {}", "arg");
                logger.warn("warn", new RuntimeException());
                logger.error("error");
                logger.error("error {}", "arg");
                logger.error("error", new RuntimeException());
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("ConditionalLogger内部类测试")
    class ConditionalLoggerTests {

        @Test
        @DisplayName("ConditionalLogger是静态类")
        void testIsStatic() {
            assertThat(java.lang.reflect.Modifier.isStatic(ConditionalLog.ConditionalLogger.class.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("OnceLogger内部类测试")
    class OnceLoggerTests {

        @Test
        @DisplayName("OnceLogger是静态类")
        void testIsStatic() {
            assertThat(java.lang.reflect.Modifier.isStatic(ConditionalLog.OnceLogger.class.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("RateLimitedLogger内部类测试")
    class RateLimitedLoggerTests {

        @Test
        @DisplayName("RateLimitedLogger是静态类")
        void testIsStatic() {
            assertThat(java.lang.reflect.Modifier.isStatic(ConditionalLog.RateLimitedLogger.class.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("清除追踪测试")
    class ClearTrackingTests {

        @Test
        @DisplayName("clearOnceTracking清除一次性追踪")
        void testClearOnceTracking() {
            assertThatCode(() -> ConditionalLog.clearOnceTracking()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("clearRateLimitTracking清除限速追踪")
        void testClearRateLimitTracking() {
            assertThatCode(() -> ConditionalLog.clearRateLimitTracking()).doesNotThrowAnyException();
        }
    }
}
