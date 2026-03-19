package cloud.opencode.base.log.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * AuditLogger 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("AuditLogger 接口测试")
class AuditLoggerTest {

    @Nested
    @DisplayName("接口定义测试")
    class InterfaceDefinitionTests {

        @Test
        @DisplayName("AuditLogger是接口")
        void testIsInterface() {
            assertThat(AuditLogger.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("定义了log方法")
        void testLogMethod() throws NoSuchMethodException {
            assertThat(AuditLogger.class.getMethod("log", AuditEvent.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了isEnabled方法")
        void testIsEnabledMethod() throws NoSuchMethodException {
            assertThat(AuditLogger.class.getMethod("isEnabled")).isNotNull();
        }

        @Test
        @DisplayName("定义了initialize方法")
        void testInitializeMethod() throws NoSuchMethodException {
            assertThat(AuditLogger.class.getMethod("initialize")).isNotNull();
        }

        @Test
        @DisplayName("定义了shutdown方法")
        void testShutdownMethod() throws NoSuchMethodException {
            assertThat(AuditLogger.class.getMethod("shutdown")).isNotNull();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("isEnabled默认返回true")
        void testIsEnabledDefault() {
            AuditLogger logger = event -> {};
            assertThat(logger.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("initialize默认无操作")
        void testInitializeDefault() {
            AuditLogger logger = event -> {};
            assertThatCode(logger::initialize).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("shutdown默认无操作")
        void testShutdownDefault() {
            AuditLogger logger = event -> {};
            assertThatCode(logger::shutdown).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Lambda实现测试")
    class LambdaImplementationTests {

        @Test
        @DisplayName("可以使用Lambda实现")
        void testLambdaImplementation() {
            java.util.concurrent.atomic.AtomicReference<AuditEvent> captured =
                new java.util.concurrent.atomic.AtomicReference<>();

            AuditLogger logger = captured::set;

            AuditEvent event = AuditEvent.builder("TEST").build();
            logger.log(event);

            assertThat(captured.get()).isEqualTo(event);
        }
    }

    @Nested
    @DisplayName("自定义实现测试")
    class CustomImplementationTests {

        @Test
        @DisplayName("可以自定义实现")
        void testCustomImplementation() {
            class CustomLogger implements AuditLogger {
                private int logCount = 0;
                private boolean enabled = true;

                @Override
                public void log(AuditEvent event) {
                    logCount++;
                }

                @Override
                public boolean isEnabled() {
                    return enabled;
                }

                @Override
                public void initialize() {
                    enabled = true;
                }

                @Override
                public void shutdown() {
                    enabled = false;
                }

                public int getLogCount() {
                    return logCount;
                }
            }

            CustomLogger logger = new CustomLogger();

            assertThat(logger.isEnabled()).isTrue();

            logger.log(AuditEvent.builder("TEST").build());
            assertThat(logger.getLogCount()).isEqualTo(1);

            logger.shutdown();
            assertThat(logger.isEnabled()).isFalse();
        }
    }
}
