package cloud.opencode.base.parallel.structured;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * ScopedContextTest Tests
 * ScopedContextTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("ScopedContext 测试")
class ScopedContextTest {

    @Nested
    @DisplayName("TraceId方法测试")
    class TraceIdTests {

        @Test
        @DisplayName("runWithTraceId绑定traceId运行任务")
        void testRunWithTraceId() {
            AtomicBoolean executed = new AtomicBoolean(false);

            ScopedContext.runWithTraceId("trace-123", () -> {
                executed.set(true);
                // Verify binding via isBound
                assertThat(ScopedContext.TRACE_ID.isBound()).isTrue();
                assertThat(ScopedContext.TRACE_ID.get()).isEqualTo("trace-123");
            });

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("callWithTraceId绑定traceId调用任务")
        void testCallWithTraceId() {
            String result = ScopedContext.callWithTraceId("trace-456", () -> {
                assertThat(ScopedContext.TRACE_ID.isBound()).isTrue();
                return "result-" + ScopedContext.TRACE_ID.get();
            });

            assertThat(result).isEqualTo("result-trace-456");
        }

        @Test
        @DisplayName("getTraceIdOrDefault绑定时返回绑定值")
        void testGetTraceIdOrDefaultBound() {
            ScopedContext.runWithTraceId("my-trace", () -> {
                String value = ScopedContext.getTraceIdOrDefault("default");
                assertThat(value).isEqualTo("my-trace");
            });
        }
    }

    @Nested
    @DisplayName("UserId方法测试")
    class UserIdTests {

        @Test
        @DisplayName("runWithUserId绑定userId运行任务")
        void testRunWithUserId() {
            AtomicBoolean executed = new AtomicBoolean(false);

            ScopedContext.runWithUserId("user-123", () -> {
                executed.set(true);
                assertThat(ScopedContext.USER_ID.isBound()).isTrue();
                assertThat(ScopedContext.USER_ID.get()).isEqualTo("user-123");
            });

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("callWithUserId绑定userId调用任务")
        void testCallWithUserId() {
            String result = ScopedContext.callWithUserId("user-456", () -> {
                return "result-" + ScopedContext.USER_ID.get();
            });

            assertThat(result).isEqualTo("result-user-456");
        }
    }

    @Nested
    @DisplayName("TenantId方法测试")
    class TenantIdTests {

        @Test
        @DisplayName("runWithTenantId绑定tenantId运行任务")
        void testRunWithTenantId() {
            AtomicBoolean executed = new AtomicBoolean(false);

            ScopedContext.runWithTenantId("tenant-123", () -> {
                executed.set(true);
                assertThat(ScopedContext.TENANT_ID.isBound()).isTrue();
                assertThat(ScopedContext.TENANT_ID.get()).isEqualTo("tenant-123");
            });

            assertThat(executed.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("RequestId方法测试")
    class RequestIdTests {

        @Test
        @DisplayName("runWithRequestId绑定requestId运行任务")
        void testRunWithRequestId() {
            AtomicBoolean executed = new AtomicBoolean(false);

            ScopedContext.runWithRequestId("req-123", () -> {
                executed.set(true);
                assertThat(ScopedContext.REQUEST_ID.isBound()).isTrue();
                assertThat(ScopedContext.REQUEST_ID.get()).isEqualTo("req-123");
            });

            assertThat(executed.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("多绑定方法测试")
    class MultipleBindingsTests {

        @Test
        @DisplayName("run绑定traceId和userId")
        void testRunWithTraceIdAndUserId() {
            AtomicBoolean executed = new AtomicBoolean(false);

            ScopedContext.run("trace-123", "user-456", () -> {
                executed.set(true);
                assertThat(ScopedContext.TRACE_ID.isBound()).isTrue();
                assertThat(ScopedContext.USER_ID.isBound()).isTrue();
                assertThat(ScopedContext.TRACE_ID.get()).isEqualTo("trace-123");
                assertThat(ScopedContext.USER_ID.get()).isEqualTo("user-456");
            });

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("call绑定traceId和userId")
        void testCallWithTraceIdAndUserId() {
            String result = ScopedContext.call("trace-123", "user-456", () -> {
                return ScopedContext.TRACE_ID.get() + "-" + ScopedContext.USER_ID.get();
            });

            assertThat(result).isEqualTo("trace-123-user-456");
        }

        @Test
        @DisplayName("run绑定traceId、userId和tenantId")
        void testRunWithThreeBindings() {
            AtomicBoolean executed = new AtomicBoolean(false);

            ScopedContext.run("trace-123", "user-456", "tenant-789", () -> {
                executed.set(true);
                assertThat(ScopedContext.TRACE_ID.get()).isEqualTo("trace-123");
                assertThat(ScopedContext.USER_ID.get()).isEqualTo("user-456");
                assertThat(ScopedContext.TENANT_ID.get()).isEqualTo("tenant-789");
            });

            assertThat(executed.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("自定义ScopedValue测试")
    class CustomScopedValueTests {

        @Test
        @DisplayName("newScopedValue创建新的作用域值")
        void testNewScopedValue() {
            ScopedValue<String> customValue = ScopedContext.newScopedValue();

            assertThat(customValue).isNotNull();
            assertThat(customValue.isBound()).isFalse();
        }

        @Test
        @DisplayName("runWith绑定自定义作用域值")
        void testRunWith() {
            ScopedValue<Integer> customValue = ScopedContext.newScopedValue();
            AtomicReference<Integer> captured = new AtomicReference<>();

            ScopedContext.runWith(customValue, 42, () -> {
                captured.set(customValue.get());
            });

            assertThat(captured.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("callWith绑定自定义作用域值调用任务")
        void testCallWith() {
            ScopedValue<String> customValue = ScopedContext.newScopedValue();

            String result = ScopedContext.callWith(customValue, "custom", () -> {
                return "result-" + customValue.get();
            });

            assertThat(result).isEqualTo("result-custom");
        }

        @Test
        @DisplayName("isBound检查是否绑定")
        void testIsBound() {
            ScopedValue<String> customValue = ScopedContext.newScopedValue();

            assertThat(ScopedContext.isBound(customValue)).isFalse();

            ScopedContext.runWith(customValue, "test", () -> {
                assertThat(ScopedContext.isBound(customValue)).isTrue();
            });
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("callWithTraceId任务抛出异常时包装为OpenParallelException")
        void testCallWithTraceIdException() {
            assertThatThrownBy(() -> ScopedContext.callWithTraceId("trace-123", () -> {
                throw new RuntimeException("test error");
            })).isInstanceOf(OpenParallelException.class)
               .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("callWithUserId任务抛出异常时包装为OpenParallelException")
        void testCallWithUserIdException() {
            assertThatThrownBy(() -> ScopedContext.callWithUserId("user-123", () -> {
                throw new RuntimeException("test error");
            })).isInstanceOf(OpenParallelException.class);
        }

        @Test
        @DisplayName("call任务抛出异常时包装为OpenParallelException")
        void testCallException() {
            assertThatThrownBy(() -> ScopedContext.call("trace", "user", () -> {
                throw new RuntimeException("test error");
            })).isInstanceOf(OpenParallelException.class);
        }

        @Test
        @DisplayName("callWith任务抛出异常时包装为OpenParallelException")
        void testCallWithException() {
            ScopedValue<String> customValue = ScopedContext.newScopedValue();

            assertThatThrownBy(() -> ScopedContext.callWith(customValue, "test", () -> {
                throw new RuntimeException("test error");
            })).isInstanceOf(OpenParallelException.class);
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("TRACE_ID是有效的ScopedValue")
        void testTraceIdConstant() {
            assertThat(ScopedContext.TRACE_ID).isNotNull();
        }

        @Test
        @DisplayName("USER_ID是有效的ScopedValue")
        void testUserIdConstant() {
            assertThat(ScopedContext.USER_ID).isNotNull();
        }

        @Test
        @DisplayName("TENANT_ID是有效的ScopedValue")
        void testTenantIdConstant() {
            assertThat(ScopedContext.TENANT_ID).isNotNull();
        }

        @Test
        @DisplayName("REQUEST_ID是有效的ScopedValue")
        void testRequestIdConstant() {
            assertThat(ScopedContext.REQUEST_ID).isNotNull();
        }
    }
}
