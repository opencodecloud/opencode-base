package cloud.opencode.base.log.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * LogContext 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("LogContext 测试")
class LogContextTest {

    @BeforeEach
    void setUp() {
        LogContext.clear();
    }

    @AfterEach
    void tearDown() {
        LogContext.clear();
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(LogContext.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = LogContext.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("KEY_TRACE_ID常量")
        void testKeyTraceId() {
            assertThat(LogContext.KEY_TRACE_ID).isEqualTo("traceId");
        }

        @Test
        @DisplayName("KEY_REQUEST_ID常量")
        void testKeyRequestId() {
            assertThat(LogContext.KEY_REQUEST_ID).isEqualTo("requestId");
        }

        @Test
        @DisplayName("KEY_USER_ID常量")
        void testKeyUserId() {
            assertThat(LogContext.KEY_USER_ID).isEqualTo("userId");
        }

        @Test
        @DisplayName("KEY_TENANT_ID常量")
        void testKeyTenantId() {
            assertThat(LogContext.KEY_TENANT_ID).isEqualTo("tenantId");
        }

        @Test
        @DisplayName("KEY_SPAN_ID常量")
        void testKeySpanId() {
            assertThat(LogContext.KEY_SPAN_ID).isEqualTo("spanId");
        }

        @Test
        @DisplayName("KEY_PARENT_SPAN_ID常量")
        void testKeyParentSpanId() {
            assertThat(LogContext.KEY_PARENT_SPAN_ID).isEqualTo("parentSpanId");
        }
    }

    @Nested
    @DisplayName("TraceId操作测试")
    class TraceIdTests {

        @Test
        @DisplayName("设置和获取traceId")
        void testSetAndGetTraceId() {
            LogContext.setTraceId("trace-123");
            assertThat(LogContext.getTraceId()).isEqualTo("trace-123");
        }

        @Test
        @DisplayName("未设置时返回null")
        void testGetTraceIdWhenNotSet() {
            assertThat(LogContext.getTraceId()).isNull();
        }
    }

    @Nested
    @DisplayName("RequestId操作测试")
    class RequestIdTests {

        @Test
        @DisplayName("设置和获取requestId")
        void testSetAndGetRequestId() {
            LogContext.setRequestId("req-456");
            assertThat(LogContext.getRequestId()).isEqualTo("req-456");
        }
    }

    @Nested
    @DisplayName("UserId操作测试")
    class UserIdTests {

        @Test
        @DisplayName("设置和获取userId")
        void testSetAndGetUserId() {
            LogContext.setUserId("user-789");
            assertThat(LogContext.getUserId()).isEqualTo("user-789");
        }
    }

    @Nested
    @DisplayName("TenantId操作测试")
    class TenantIdTests {

        @Test
        @DisplayName("设置和获取tenantId")
        void testSetAndGetTenantId() {
            LogContext.setTenantId("tenant-001");
            assertThat(LogContext.getTenantId()).isEqualTo("tenant-001");
        }
    }

    @Nested
    @DisplayName("自定义键值操作测试")
    class CustomKeyValueTests {

        @Test
        @DisplayName("设置和获取自定义值")
        void testSetAndGet() {
            LogContext.set("customKey", "customValue");
            assertThat(LogContext.get("customKey")).isEqualTo("customValue");
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清空所有上下文")
        void testClear() {
            LogContext.setTraceId("trace-1");
            LogContext.setUserId("user-1");
            LogContext.clear();

            assertThat(LogContext.getTraceId()).isNull();
            assertThat(LogContext.getUserId()).isNull();
        }
    }

    @Nested
    @DisplayName("getAll方法测试")
    class GetAllTests {

        @Test
        @DisplayName("获取所有上下文值")
        void testGetAll() {
            LogContext.setTraceId("trace-1");
            LogContext.setUserId("user-1");

            Map<String, String> all = LogContext.getAll();

            assertThat(all).containsEntry("traceId", "trace-1");
            assertThat(all).containsEntry("userId", "user-1");
        }
    }

    @Nested
    @DisplayName("snapshot方法测试")
    class SnapshotTests {

        @Test
        @DisplayName("创建上下文快照")
        void testSnapshot() {
            LogContext.setTraceId("trace-snapshot");
            LogContext.ContextSnapshot snapshot = LogContext.snapshot();

            assertThat(snapshot).isNotNull();
            assertThat(snapshot.mdcContext()).containsEntry("traceId", "trace-snapshot");
        }

        @Test
        @DisplayName("快照是独立的")
        void testSnapshotIsIndependent() {
            LogContext.setTraceId("trace-1");
            LogContext.ContextSnapshot snapshot = LogContext.snapshot();

            LogContext.setTraceId("trace-2");

            assertThat(snapshot.mdcContext().get("traceId")).isEqualTo("trace-1");
        }
    }

    @Nested
    @DisplayName("apply方法测试")
    class ApplyTests {

        @Test
        @DisplayName("应用快照")
        void testApply() {
            LogContext.setTraceId("trace-original");
            LogContext.ContextSnapshot snapshot = LogContext.snapshot();

            LogContext.clear();
            LogContext.apply(snapshot);

            assertThat(LogContext.getTraceId()).isEqualTo("trace-original");
        }

        @Test
        @DisplayName("应用null快照不报错")
        void testApplyNull() {
            assertThatCode(() -> LogContext.apply(null)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("ContextSnapshot内部类测试")
    class ContextSnapshotTests {

        @Test
        @DisplayName("ContextSnapshot是final类")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(LogContext.ContextSnapshot.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("runWith执行任务并恢复上下文")
        void testRunWith() {
            LogContext.setTraceId("original");
            LogContext.ContextSnapshot snapshot = LogContext.snapshot();

            LogContext.clear();
            LogContext.setTraceId("new");

            AtomicReference<String> captured = new AtomicReference<>();
            snapshot.runWith(() -> {
                captured.set(LogContext.getTraceId());
            });

            assertThat(captured.get()).isEqualTo("original");
            assertThat(LogContext.getTraceId()).isEqualTo("new");
        }

        @Test
        @DisplayName("callWith执行任务并返回结果")
        void testCallWith() {
            LogContext.setTraceId("trace-call");
            LogContext.ContextSnapshot snapshot = LogContext.snapshot();

            LogContext.clear();

            String result = snapshot.callWith(() -> LogContext.getTraceId());

            assertThat(result).isEqualTo("trace-call");
        }

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            LogContext.setTraceId("trace-eq");
            LogContext.ContextSnapshot snapshot1 = LogContext.snapshot();
            LogContext.ContextSnapshot snapshot2 = LogContext.snapshot();

            assertThat(snapshot1).isEqualTo(snapshot2);
            assertThat(snapshot1.hashCode()).isEqualTo(snapshot2.hashCode());
        }

        @Test
        @DisplayName("toString包含上下文信息")
        void testToString() {
            LogContext.setTraceId("trace-str");
            LogContext.ContextSnapshot snapshot = LogContext.snapshot();

            assertThat(snapshot.toString()).contains("ContextSnapshot");
        }

        @Test
        @DisplayName("null mdcContext变为空Map")
        void testNullMdcContext() {
            LogContext.ContextSnapshot snapshot = new LogContext.ContextSnapshot(null);
            assertThat(snapshot.mdcContext()).isEmpty();
        }
    }
}
