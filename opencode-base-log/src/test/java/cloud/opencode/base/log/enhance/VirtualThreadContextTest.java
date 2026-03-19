package cloud.opencode.base.log.enhance;

import cloud.opencode.base.log.context.LogContext;
import cloud.opencode.base.log.context.MDC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * VirtualThreadContext 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("VirtualThreadContext 测试")
class VirtualThreadContextTest {

    @BeforeEach
    void setUp() {
        MDC.clear();
        LogContext.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        LogContext.clear();
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(VirtualThreadContext.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = VirtualThreadContext.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("startVirtualThread方法测试")
    class StartVirtualThreadTests {

        @Test
        @DisplayName("启动虚拟线程并传播上下文")
        void testStartVirtualThread() throws Exception {
            LogContext.setTraceId("trace-123");
            AtomicReference<String> captured = new AtomicReference<>();

            Thread thread = VirtualThreadContext.startVirtualThread(() -> {
                captured.set(LogContext.getTraceId());
            });

            thread.join();
            assertThat(captured.get()).isEqualTo("trace-123");
        }

        @Test
        @DisplayName("返回的线程是虚拟线程")
        void testIsVirtualThread() {
            Thread thread = VirtualThreadContext.startVirtualThread(() -> {});
            assertThat(thread.isVirtual()).isTrue();
        }
    }

    @Nested
    @DisplayName("virtualThreadBuilder方法测试")
    class VirtualThreadBuilderTests {

        @Test
        @DisplayName("创建虚拟线程构建器")
        void testVirtualThreadBuilder() {
            Thread.Builder.OfVirtual builder = VirtualThreadContext.virtualThreadBuilder("test-");
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("newVirtualThreadExecutor方法测试")
    class NewVirtualThreadExecutorTests {

        @Test
        @DisplayName("创建虚拟线程执行器")
        void testNewVirtualThreadExecutor() {
            ExecutorService executor = VirtualThreadContext.newVirtualThreadExecutor();
            assertThat(executor).isNotNull();
            executor.shutdown();
        }

        @Test
        @DisplayName("创建命名虚拟线程执行器")
        void testNewVirtualThreadExecutorWithName() {
            ExecutorService executor = VirtualThreadContext.newVirtualThreadExecutor("test-");
            assertThat(executor).isNotNull();
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("wrap(Runnable)方法测试")
    class WrapRunnableTests {

        @Test
        @DisplayName("包装Runnable传播上下文")
        void testWrapRunnable() throws Exception {
            LogContext.setTraceId("trace-wrap");
            AtomicReference<String> captured = new AtomicReference<>();

            Runnable wrapped = VirtualThreadContext.wrap(() -> {
                captured.set(LogContext.getTraceId());
            });

            LogContext.clear();
            Thread thread = Thread.startVirtualThread(wrapped);
            thread.join();

            assertThat(captured.get()).isEqualTo("trace-wrap");
        }

        @Test
        @DisplayName("包装后恢复之前的上下文")
        void testWrapRunnableRestoresPrevious() throws Exception {
            LogContext.setTraceId("trace-original");

            Runnable wrapped = VirtualThreadContext.wrap(() -> {
                LogContext.setTraceId("trace-inner");
            });

            LogContext.clear();
            wrapped.run();

            assertThat(LogContext.getTraceId()).isNull();
        }
    }

    @Nested
    @DisplayName("wrap(Callable)方法测试")
    class WrapCallableTests {

        @Test
        @DisplayName("包装Callable传播上下文")
        void testWrapCallable() throws Exception {
            LogContext.setTraceId("trace-callable");

            var wrapped = VirtualThreadContext.wrap(() -> LogContext.getTraceId());

            LogContext.clear();
            String result = wrapped.call();

            assertThat(result).isEqualTo("trace-callable");
        }
    }

    @Nested
    @DisplayName("wrap(Function)方法测试")
    class WrapFunctionTests {

        @Test
        @DisplayName("包装Function传播上下文")
        void testWrapFunction() {
            LogContext.setTraceId("trace-func");

            Function<String, String> wrapped = VirtualThreadContext.wrap(input ->
                LogContext.getTraceId() + "-" + input
            );

            LogContext.clear();
            String result = wrapped.apply("suffix");

            assertThat(result).isEqualTo("trace-func-suffix");
        }
    }

    @Nested
    @DisplayName("wrap(CompletableFuture)方法测试")
    class WrapCompletableFutureTests {

        @Test
        @DisplayName("包装CompletableFuture传播上下文")
        void testWrapCompletableFuture() throws Exception {
            LogContext.setTraceId("trace-future");

            CompletableFuture<String> original = CompletableFuture.supplyAsync(() -> "result");
            CompletableFuture<String> wrapped = VirtualThreadContext.wrap(original);

            LogContext.clear();
            String result = wrapped.get();

            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("包装带异常的CompletableFuture")
        void testWrapCompletableFutureWithException() {
            LogContext.setTraceId("trace-future-error");

            CompletableFuture<String> original = CompletableFuture.failedFuture(new RuntimeException("test error"));
            CompletableFuture<String> wrapped = VirtualThreadContext.wrap(original);

            assertThatThrownBy(wrapped::get)
                .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("runAsync方法测试")
    class RunAsyncTests {

        @Test
        @DisplayName("异步运行任务并传播上下文")
        void testRunAsync() throws Exception {
            LogContext.setTraceId("trace-async");
            AtomicReference<String> captured = new AtomicReference<>();

            CompletableFuture<Void> future = VirtualThreadContext.runAsync(() -> {
                captured.set(LogContext.getTraceId());
            });

            future.get();
            assertThat(captured.get()).isEqualTo("trace-async");
        }
    }

    @Nested
    @DisplayName("supplyAsync方法测试")
    class SupplyAsyncTests {

        @Test
        @DisplayName("异步供应值并返回结果")
        void testSupplyAsync() throws Exception {
            CompletableFuture<String> future = VirtualThreadContext.supplyAsync(() -> "test-result");

            String result = future.get();
            assertThat(result).isEqualTo("test-result");
        }

        @Test
        @DisplayName("供应者抛出异常时包装为RuntimeException")
        void testSupplyAsyncWithException() {
            CompletableFuture<String> future = VirtualThreadContext.supplyAsync(() -> {
                throw new Exception("test error");
            });

            assertThatThrownBy(future::get)
                .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("runWithContext方法测试")
    class RunWithContextTests {

        @Test
        @DisplayName("使用指定上下文运行任务")
        void testRunWithContext() {
            AtomicReference<String> capturedTrace = new AtomicReference<>();
            AtomicReference<String> capturedUser = new AtomicReference<>();

            VirtualThreadContext.runWithContext("trace-ctx", "user-ctx", () -> {
                capturedTrace.set(LogContext.getTraceId());
                capturedUser.set(LogContext.getUserId());
            });

            assertThat(capturedTrace.get()).isEqualTo("trace-ctx");
            assertThat(capturedUser.get()).isEqualTo("user-ctx");
            assertThat(LogContext.getTraceId()).isNull();
        }
    }

    @Nested
    @DisplayName("callWithContext方法测试")
    class CallWithContextTests {

        @Test
        @DisplayName("使用指定上下文调用任务")
        void testCallWithContext() throws Exception {
            String result = VirtualThreadContext.callWithContext("trace-ctx", "user-ctx", () ->
                LogContext.getTraceId() + "-" + LogContext.getUserId()
            );

            assertThat(result).isEqualTo("trace-ctx-user-ctx");
            assertThat(LogContext.getTraceId()).isNull();
        }
    }

    @Nested
    @DisplayName("上下文恢复测试")
    class ContextRestorationTests {

        @Test
        @DisplayName("wrap恢复null上下文")
        void testWrapRestoresNullContext() throws Exception {
            // No context set initially

            Runnable wrapped = VirtualThreadContext.wrap(() -> {
                LogContext.setTraceId("inner");
            });

            wrapped.run();

            assertThat(LogContext.getTraceId()).isNull();
        }

        @Test
        @DisplayName("wrap恢复非null上下文")
        void testWrapRestoresNonNullContext() throws Exception {
            MDC.put("key", "outer");

            Runnable wrapped = VirtualThreadContext.wrap(() -> {
                MDC.put("key", "inner");
            });

            MDC.clear();
            MDC.put("key", "current");

            wrapped.run();

            assertThat(MDC.get("key")).isEqualTo("current");
        }
    }
}
