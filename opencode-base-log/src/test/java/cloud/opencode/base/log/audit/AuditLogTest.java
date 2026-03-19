package cloud.opencode.base.log.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * AuditLog 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("AuditLog 测试")
class AuditLogTest {

    private TestAuditLogger testLogger;

    @BeforeEach
    void setUp() {
        testLogger = new TestAuditLogger();
        AuditLog.setLogger(testLogger);
    }

    @AfterEach
    void tearDown() {
        AuditLog.setLogger(null); // Reset to default
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(AuditLog.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = AuditLog.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("log方法测试")
    class LogMethodTests {

        @Test
        @DisplayName("记录简单事件")
        void testLogSimple() {
            AuditLog.log("user1", "LOGIN", "system", "SUCCESS");

            assertThat(testLogger.getEvents()).hasSize(1);
            AuditEvent event = testLogger.getEvents().get(0);
            assertThat(event.userId()).isEqualTo("user1");
            assertThat(event.action()).isEqualTo("LOGIN");
            assertThat(event.target()).isEqualTo("system");
            assertThat(event.result()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("记录带详情的事件")
        void testLogWithDetails() {
            Map<String, Object> details = Map.of("browser", "Chrome");
            AuditLog.log("user1", "LOGIN", "system", "SUCCESS", details);

            assertThat(testLogger.getEvents()).hasSize(1);
            assertThat(testLogger.getEvents().get(0).details()).containsEntry("browser", "Chrome");
        }

        @Test
        @DisplayName("记录AuditEvent对象")
        void testLogEvent() {
            AuditEvent event = AuditEvent.builder("TEST")
                .userId("user1")
                .success()
                .build();

            AuditLog.log(event);

            assertThat(testLogger.getEvents()).hasSize(1);
        }

        @Test
        @DisplayName("禁用时不记录")
        void testLogWhenDisabled() {
            testLogger.setEnabled(false);
            AuditLog.log("user1", "LOGIN", "system", "SUCCESS");

            assertThat(testLogger.getEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("event方法测试")
    class EventMethodTests {

        @Test
        @DisplayName("创建LoggingBuilder")
        void testEventBuilder() {
            AuditLog.LoggingBuilder builder = AuditLog.event("LOGIN");
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("builder自动记录")
        void testBuilderAutoLog() {
            AuditEvent event = AuditLog.event("LOGIN")
                .userId("user1")
                .target("system")
                .success()
                .build();

            assertThat(testLogger.getEvents()).hasSize(1);
            assertThat(event.action()).isEqualTo("LOGIN");
        }

        @Test
        @DisplayName("builder链式调用")
        void testBuilderChaining() {
            AuditEvent event = AuditLog.event("UPDATE")
                .userId("admin")
                .target("User")
                .targetId("user-001")
                .result("SUCCESS")
                .ip("192.168.1.1")
                .userAgent("Chrome")
                .detail("field", "email")
                .details(Map.of("extra", "info"))
                .build();

            assertThat(event.userId()).isEqualTo("admin");
            assertThat(event.targetId()).isEqualTo("user-001");
        }

        @Test
        @DisplayName("builder success()方法")
        void testBuilderSuccess() {
            AuditEvent event = AuditLog.event("TEST").success().build();
            assertThat(event.result()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("builder failure()方法")
        void testBuilderFailure() {
            AuditEvent event = AuditLog.event("TEST").failure().build();
            assertThat(event.result()).isEqualTo("FAILURE");
        }
    }

    @Nested
    @DisplayName("logLogin方法测试")
    class LogLoginTests {

        @Test
        @DisplayName("记录成功登录")
        void testLogLoginSuccess() {
            AuditLog.logLogin("user1", true, "192.168.1.1");

            assertThat(testLogger.getEvents()).hasSize(1);
            AuditEvent event = testLogger.getEvents().get(0);
            assertThat(event.action()).isEqualTo("LOGIN");
            assertThat(event.userId()).isEqualTo("user1");
            assertThat(event.result()).isEqualTo("SUCCESS");
            assertThat(event.ip()).isEqualTo("192.168.1.1");
        }

        @Test
        @DisplayName("记录失败登录")
        void testLogLoginFailure() {
            AuditLog.logLogin("user1", false, "10.0.0.1");

            assertThat(testLogger.getEvents()).hasSize(1);
            AuditEvent event = testLogger.getEvents().get(0);
            assertThat(event.result()).isEqualTo("FAILURE");
        }
    }

    @Nested
    @DisplayName("logLogout方法测试")
    class LogLogoutTests {

        @Test
        @DisplayName("记录注销")
        void testLogLogout() {
            AuditLog.logLogout("user1");

            assertThat(testLogger.getEvents()).hasSize(1);
            AuditEvent event = testLogger.getEvents().get(0);
            assertThat(event.action()).isEqualTo("LOGOUT");
            assertThat(event.userId()).isEqualTo("user1");
            assertThat(event.result()).isEqualTo("SUCCESS");
        }
    }

    @Nested
    @DisplayName("logDataChange方法测试")
    class LogDataChangeTests {

        @Test
        @DisplayName("记录数据更改")
        void testLogDataChange() {
            AuditLog.logDataChange("admin", "User", "user-001", "UPDATE", "oldValue", "newValue");

            assertThat(testLogger.getEvents()).hasSize(1);
            AuditEvent event = testLogger.getEvents().get(0);
            assertThat(event.action()).isEqualTo("UPDATE");
            assertThat(event.target()).isEqualTo("User");
            assertThat(event.targetId()).isEqualTo("user-001");
            assertThat(event.details()).containsEntry("before", "oldValue");
            assertThat(event.details()).containsEntry("after", "newValue");
        }

        @Test
        @DisplayName("before为null时不添加")
        void testLogDataChangeNullBefore() {
            AuditLog.logDataChange("admin", "User", "user-001", "CREATE", null, "newValue");

            AuditEvent event = testLogger.getEvents().get(0);
            assertThat(event.details()).doesNotContainKey("before");
            assertThat(event.details()).containsEntry("after", "newValue");
        }

        @Test
        @DisplayName("after为null时不添加")
        void testLogDataChangeNullAfter() {
            AuditLog.logDataChange("admin", "User", "user-001", "DELETE", "oldValue", null);

            AuditEvent event = testLogger.getEvents().get(0);
            assertThat(event.details()).containsEntry("before", "oldValue");
            assertThat(event.details()).doesNotContainKey("after");
        }
    }

    @Nested
    @DisplayName("getLogger/setLogger方法测试")
    class LoggerManagementTests {

        @Test
        @DisplayName("getLogger返回当前logger")
        void testGetLogger() {
            AuditLogger logger = AuditLog.getLogger();
            assertThat(logger).isSameAs(testLogger);
        }

        @Test
        @DisplayName("setLogger设置logger")
        void testSetLogger() {
            TestAuditLogger newLogger = new TestAuditLogger();
            AuditLog.setLogger(newLogger);

            assertThat(AuditLog.getLogger()).isSameAs(newLogger);
        }

        @Test
        @DisplayName("setLogger(null)重置为默认")
        void testSetLoggerNull() {
            AuditLog.setLogger(null);

            assertThat(AuditLog.getLogger()).isNotNull();
            assertThat(AuditLog.getLogger()).isNotSameAs(testLogger);
        }
    }

    /**
     * 测试用AuditLogger实现
     */
    private static class TestAuditLogger implements AuditLogger {
        private final List<AuditEvent> events = new ArrayList<>();
        private boolean enabled = true;

        @Override
        public void log(AuditEvent event) {
            events.add(event);
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<AuditEvent> getEvents() {
            return events;
        }
    }
}
