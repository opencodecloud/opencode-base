package cloud.opencode.base.log.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * AuditEvent 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("AuditEvent 测试")
class AuditEventTest {

    @Nested
    @DisplayName("记录创建测试")
    class RecordCreationTests {

        @Test
        @DisplayName("创建基本事件")
        void testCreateBasicEvent() {
            AuditEvent event = new AuditEvent(
                null, null, "user1", "LOGIN", "system", null, "SUCCESS", null, null, null
            );

            assertThat(event.action()).isEqualTo("LOGIN");
            assertThat(event.userId()).isEqualTo("user1");
            assertThat(event.target()).isEqualTo("system");
            assertThat(event.result()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("自动生成eventId")
        void testAutoGenerateEventId() {
            AuditEvent event = new AuditEvent(
                null, null, "user1", "LOGIN", "system", null, null, null, null, null
            );

            assertThat(event.eventId()).isNotNull();
            assertThat(event.eventId()).isNotEmpty();
        }

        @Test
        @DisplayName("自动生成timestamp")
        void testAutoGenerateTimestamp() {
            Instant before = Instant.now();
            AuditEvent event = new AuditEvent(
                null, null, "user1", "LOGIN", "system", null, null, null, null, null
            );
            Instant after = Instant.now();

            assertThat(event.timestamp()).isNotNull();
            assertThat(event.timestamp()).isBetween(before, after);
        }

        @Test
        @DisplayName("null details变为空Map")
        void testNullDetailsBecomesEmptyMap() {
            AuditEvent event = new AuditEvent(
                null, null, "user1", "LOGIN", "system", null, null, null, null, null
            );

            assertThat(event.details()).isNotNull();
            assertThat(event.details()).isEmpty();
        }

        @Test
        @DisplayName("details被复制为不可变")
        void testDetailsAreCopied() {
            Map<String, Object> details = new java.util.HashMap<>();
            details.put("key", "value");

            AuditEvent event = new AuditEvent(
                null, null, "user1", "LOGIN", "system", null, null, null, null, details
            );

            assertThat(event.details()).containsEntry("key", "value");
            assertThatThrownBy(() -> event.details().put("new", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("null action抛出异常")
        void testNullActionThrows() {
            assertThatThrownBy(() -> new AuditEvent(
                null, null, "user1", null, "system", null, null, null, null, null
            )).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("RESULT_SUCCESS常量")
        void testResultSuccess() {
            assertThat(AuditEvent.RESULT_SUCCESS).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("RESULT_FAILURE常量")
        void testResultFailure() {
            assertThat(AuditEvent.RESULT_FAILURE).isEqualTo("FAILURE");
        }
    }

    @Nested
    @DisplayName("isSuccess方法测试")
    class IsSuccessTests {

        @Test
        @DisplayName("SUCCESS结果返回true")
        void testSuccessReturnsTrue() {
            AuditEvent event = AuditEvent.builder("TEST").success().build();
            assertThat(event.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("FAILURE结果返回false")
        void testFailureReturnsFalse() {
            AuditEvent event = AuditEvent.builder("TEST").failure().build();
            assertThat(event.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("其他结果返回false")
        void testOtherResultReturnsFalse() {
            AuditEvent event = AuditEvent.builder("TEST").result("PENDING").build();
            assertThat(event.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("创建Builder")
        void testBuilderCreation() {
            AuditEvent.Builder builder = AuditEvent.builder("LOGIN");
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("设置所有字段")
        void testBuilderAllFields() {
            Instant now = Instant.now();
            AuditEvent event = AuditEvent.builder("LOGIN")
                .eventId("evt-001")
                .timestamp(now)
                .userId("user123")
                .target("system")
                .targetId("target001")
                .result("SUCCESS")
                .ip("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .detail("browser", "Chrome")
                .build();

            assertThat(event.eventId()).isEqualTo("evt-001");
            assertThat(event.timestamp()).isEqualTo(now);
            assertThat(event.userId()).isEqualTo("user123");
            assertThat(event.action()).isEqualTo("LOGIN");
            assertThat(event.target()).isEqualTo("system");
            assertThat(event.targetId()).isEqualTo("target001");
            assertThat(event.result()).isEqualTo("SUCCESS");
            assertThat(event.ip()).isEqualTo("192.168.1.1");
            assertThat(event.userAgent()).isEqualTo("Mozilla/5.0");
            assertThat(event.details()).containsEntry("browser", "Chrome");
        }

        @Test
        @DisplayName("success()设置SUCCESS结果")
        void testBuilderSuccess() {
            AuditEvent event = AuditEvent.builder("TEST").success().build();
            assertThat(event.result()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("failure()设置FAILURE结果")
        void testBuilderFailure() {
            AuditEvent event = AuditEvent.builder("TEST").failure().build();
            assertThat(event.result()).isEqualTo("FAILURE");
        }

        @Test
        @DisplayName("details()添加多个详情")
        void testBuilderDetails() {
            Map<String, Object> details = Map.of("key1", "value1", "key2", "value2");
            AuditEvent event = AuditEvent.builder("TEST").details(details).build();

            assertThat(event.details()).containsEntry("key1", "value1");
            assertThat(event.details()).containsEntry("key2", "value2");
        }

        @Test
        @DisplayName("链式调用")
        void testBuilderChaining() {
            AuditEvent event = AuditEvent.builder("UPDATE")
                .userId("admin")
                .target("User")
                .targetId("user-001")
                .success()
                .ip("10.0.0.1")
                .detail("field", "email")
                .detail("oldValue", "old@test.com")
                .detail("newValue", "new@test.com")
                .build();

            assertThat(event.userId()).isEqualTo("admin");
            assertThat(event.target()).isEqualTo("User");
            assertThat(event.details()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("记录方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("action()访问器")
        void testActionAccessor() {
            AuditEvent event = AuditEvent.builder("TEST_ACTION").build();
            assertThat(event.action()).isEqualTo("TEST_ACTION");
        }

        @Test
        @DisplayName("记录相等性")
        void testRecordEquality() {
            Instant now = Instant.now();
            AuditEvent event1 = new AuditEvent("id1", now, "user", "ACTION", "target", null, null, null, null, null);
            AuditEvent event2 = new AuditEvent("id1", now, "user", "ACTION", "target", null, null, null, null, null);

            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }
    }
}
