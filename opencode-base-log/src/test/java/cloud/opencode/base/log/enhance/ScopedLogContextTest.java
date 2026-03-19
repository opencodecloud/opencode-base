package cloud.opencode.base.log.enhance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * ScopedLogContext 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("ScopedLogContext 测试")
class ScopedLogContextTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(ScopedLogContext.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = ScopedLogContext.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("ScopedValue常量测试")
    class ScopedValueConstantTests {

        @Test
        @DisplayName("TRACE_ID常量存在")
        void testTraceId() {
            assertThat(ScopedLogContext.TRACE_ID).isNotNull();
        }

        @Test
        @DisplayName("USER_ID常量存在")
        void testUserId() {
            assertThat(ScopedLogContext.USER_ID).isNotNull();
        }

        @Test
        @DisplayName("REQUEST_ID常量存在")
        void testRequestId() {
            assertThat(ScopedLogContext.REQUEST_ID).isNotNull();
        }

        @Test
        @DisplayName("TENANT_ID常量存在")
        void testTenantId() {
            assertThat(ScopedLogContext.TENANT_ID).isNotNull();
        }

        @Test
        @DisplayName("SESSION_ID常量存在")
        void testSessionId() {
            assertThat(ScopedLogContext.SESSION_ID).isNotNull();
        }

        @Test
        @DisplayName("SPAN_ID常量存在")
        void testSpanId() {
            assertThat(ScopedLogContext.SPAN_ID).isNotNull();
        }

        @Test
        @DisplayName("OPERATION常量存在")
        void testOperation() {
            assertThat(ScopedLogContext.OPERATION).isNotNull();
        }
    }

    @Nested
    @DisplayName("where(ScopedValue, String)方法测试")
    class WhereSingleTests {

        @Test
        @DisplayName("创建带单个绑定的Carrier")
        void testWhereSingle() {
            ScopedLogContext.Carrier carrier = ScopedLogContext.where(ScopedLogContext.TRACE_ID, "trace-123");
            assertThat(carrier).isNotNull();
        }

        @Test
        @DisplayName("在作用域内可以访问值")
        void testWhereSingleAccess() {
            AtomicReference<String> captured = new AtomicReference<>();

            ScopedLogContext.where(ScopedLogContext.TRACE_ID, "trace-123")
                .run(() -> {
                    captured.set(ScopedLogContext.getTraceId());
                });

            assertThat(captured.get()).isEqualTo("trace-123");
        }
    }

    @Nested
    @DisplayName("where(Map)方法测试")
    class WhereMapTests {

        @Test
        @DisplayName("创建带多个绑定的Carrier")
        void testWhereMap() {
            Map<ScopedValue<String>, String> bindings = Map.of(
                ScopedLogContext.TRACE_ID, "trace-123",
                ScopedLogContext.USER_ID, "user-456"
            );

            ScopedLogContext.Carrier carrier = ScopedLogContext.where(bindings);
            assertThat(carrier).isNotNull();
        }

        @Test
        @DisplayName("null绑定抛出异常")
        void testWhereMapNull() {
            assertThatThrownBy(() -> ScopedLogContext.where((Map<ScopedValue<String>, String>) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空绑定抛出异常")
        void testWhereMapEmpty() {
            assertThatThrownBy(() -> ScopedLogContext.where(Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("run方法测试")
    class RunTests {

        @Test
        @DisplayName("使用traceId和userId运行任务")
        void testRun() {
            AtomicReference<String> capturedTrace = new AtomicReference<>();
            AtomicReference<String> capturedUser = new AtomicReference<>();

            ScopedLogContext.run("trace-123", "user-456", () -> {
                capturedTrace.set(ScopedLogContext.getTraceId());
                capturedUser.set(ScopedLogContext.getUserId());
            });

            assertThat(capturedTrace.get()).isEqualTo("trace-123");
            assertThat(capturedUser.get()).isEqualTo("user-456");
        }
    }

    @Nested
    @DisplayName("call方法测试")
    class CallTests {

        @Test
        @DisplayName("使用traceId和userId调用任务")
        void testCall() throws Exception {
            String result = ScopedLogContext.call("trace-123", "user-456", () -> {
                return ScopedLogContext.getTraceId() + "-" + ScopedLogContext.getUserId();
            });

            assertThat(result).isEqualTo("trace-123-user-456");
        }
    }

    @Nested
    @DisplayName("上下文访问方法测试")
    class ContextAccessTests {

        @Test
        @DisplayName("getTraceId未绑定返回null")
        void testGetTraceIdUnbound() {
            assertThat(ScopedLogContext.getTraceId()).isNull();
        }

        @Test
        @DisplayName("getUserId未绑定返回null")
        void testGetUserIdUnbound() {
            assertThat(ScopedLogContext.getUserId()).isNull();
        }

        @Test
        @DisplayName("getRequestId未绑定返回null")
        void testGetRequestIdUnbound() {
            assertThat(ScopedLogContext.getRequestId()).isNull();
        }

        @Test
        @DisplayName("getTenantId未绑定返回null")
        void testGetTenantIdUnbound() {
            assertThat(ScopedLogContext.getTenantId()).isNull();
        }

        @Test
        @DisplayName("getSessionId未绑定返回null")
        void testGetSessionIdUnbound() {
            assertThat(ScopedLogContext.getSessionId()).isNull();
        }

        @Test
        @DisplayName("getSpanId未绑定返回null")
        void testGetSpanIdUnbound() {
            assertThat(ScopedLogContext.getSpanId()).isNull();
        }

        @Test
        @DisplayName("getOperation未绑定返回null")
        void testGetOperationUnbound() {
            assertThat(ScopedLogContext.getOperation()).isNull();
        }

        @Test
        @DisplayName("get返回Optional")
        void testGetOptional() {
            Optional<String> value = ScopedLogContext.get(ScopedLogContext.TRACE_ID);
            assertThat(value).isEmpty();
        }

        @Test
        @DisplayName("绑定后get返回值")
        void testGetOptionalBound() {
            ScopedLogContext.where(ScopedLogContext.TRACE_ID, "trace-123")
                .run(() -> {
                    Optional<String> value = ScopedLogContext.get(ScopedLogContext.TRACE_ID);
                    assertThat(value).isPresent().hasValue("trace-123");
                });
        }
    }

    @Nested
    @DisplayName("Carrier内部类测试")
    class CarrierTests {

        @Test
        @DisplayName("Carrier是final类")
        void testCarrierIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(ScopedLogContext.Carrier.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("and方法链式添加绑定")
        void testAnd() {
            AtomicReference<String> capturedRequest = new AtomicReference<>();

            ScopedLogContext.where(ScopedLogContext.TRACE_ID, "trace-123")
                .and(ScopedLogContext.REQUEST_ID, "req-456")
                .run(() -> {
                    capturedRequest.set(ScopedLogContext.getRequestId());
                });

            assertThat(capturedRequest.get()).isEqualTo("req-456");
        }

        @Test
        @DisplayName("run方法在上下文中运行任务")
        void testCarrierRun() {
            AtomicReference<String> captured = new AtomicReference<>();

            ScopedLogContext.Carrier carrier = ScopedLogContext.where(ScopedLogContext.USER_ID, "user-123");
            carrier.run(() -> {
                captured.set(ScopedLogContext.getUserId());
            });

            assertThat(captured.get()).isEqualTo("user-123");
        }

        @Test
        @DisplayName("call方法在上下文中调用任务")
        void testCarrierCall() throws Exception {
            ScopedLogContext.Carrier carrier = ScopedLogContext.where(ScopedLogContext.USER_ID, "user-123");
            String result = carrier.call(() -> ScopedLogContext.getUserId());

            assertThat(result).isEqualTo("user-123");
        }

        @Test
        @DisplayName("underlying返回底层Carrier")
        void testUnderlying() {
            ScopedLogContext.Carrier carrier = ScopedLogContext.where(ScopedLogContext.TRACE_ID, "trace");
            assertThat(carrier.underlying()).isNotNull();
        }
    }

    @Nested
    @DisplayName("嵌套作用域测试")
    class NestedScopeTests {

        @Test
        @DisplayName("嵌套作用域可以访问外层值")
        void testNestedScope() {
            AtomicReference<String> outerTraceId = new AtomicReference<>();
            AtomicReference<String> innerTraceId = new AtomicReference<>();
            AtomicReference<String> innerRequestId = new AtomicReference<>();

            ScopedLogContext.where(ScopedLogContext.TRACE_ID, "trace-outer")
                .run(() -> {
                    outerTraceId.set(ScopedLogContext.getTraceId());

                    ScopedLogContext.where(ScopedLogContext.REQUEST_ID, "req-inner")
                        .run(() -> {
                            innerTraceId.set(ScopedLogContext.getTraceId());
                            innerRequestId.set(ScopedLogContext.getRequestId());
                        });
                });

            assertThat(outerTraceId.get()).isEqualTo("trace-outer");
            assertThat(innerTraceId.get()).isEqualTo("trace-outer");
            assertThat(innerRequestId.get()).isEqualTo("req-inner");
        }
    }
}
