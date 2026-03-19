package cloud.opencode.base.parallel.pipeline;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * AsyncPipelineTest Tests
 * AsyncPipelineTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("AsyncPipeline 测试")
class AsyncPipelineTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of从CompletableFuture创建")
        void testOf() {
            CompletableFuture<String> future = CompletableFuture.completedFuture("test");
            AsyncPipeline<String> pipeline = AsyncPipeline.of(future);

            assertThat(pipeline.get()).isEqualTo("test");
        }

        @Test
        @DisplayName("completed创建已完成的流水线")
        void testCompleted() {
            AsyncPipeline<String> pipeline = AsyncPipeline.completed("value");

            assertThat(pipeline.get()).isEqualTo("value");
            assertThat(pipeline.isDone()).isTrue();
        }

        @Test
        @DisplayName("failed创建失败的流水线")
        void testFailed() {
            RuntimeException error = new RuntimeException("test error");
            AsyncPipeline<String> pipeline = AsyncPipeline.failed(error);

            assertThat(pipeline.isCompletedExceptionally()).isTrue();
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用CompletableFuture构造")
        void testConstructor() {
            CompletableFuture<String> future = CompletableFuture.completedFuture("test");
            AsyncPipeline<String> pipeline = new AsyncPipeline<>(future);

            assertThat(pipeline.get()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class TransformationTests {

        @Test
        @DisplayName("then应用转换函数")
        void testThen() {
            AsyncPipeline<Integer> pipeline = AsyncPipeline.completed(5)
                    .then(x -> x * 2);

            assertThat(pipeline.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("thenAsync应用异步转换函数")
        void testThenAsync() {
            AsyncPipeline<Integer> pipeline = AsyncPipeline.completed(5)
                    .thenAsync(x -> CompletableFuture.completedFuture(x * 2));

            assertThat(pipeline.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("peek查看值但不转换")
        void testPeek() {
            AtomicBoolean called = new AtomicBoolean(false);
            AsyncPipeline<String> pipeline = AsyncPipeline.completed("test")
                    .peek(s -> called.set(true));

            assertThat(pipeline.get()).isEqualTo("test");
            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("filter过滤值")
        void testFilter() {
            AsyncPipeline<Integer> pipeline = AsyncPipeline.completed(10)
                    .filter(x -> x > 5);

            assertThat(pipeline.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("filter不匹配时抛出异常")
        void testFilterNotMatch() {
            AsyncPipeline<Integer> pipeline = AsyncPipeline.completed(3)
                    .filter(x -> x > 5);

            assertThatThrownBy(pipeline::get)
                    .hasCauseInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("onError处理错误并恢复")
        void testOnError() {
            AsyncPipeline<String> pipeline = AsyncPipeline.<String>failed(new RuntimeException("error"))
                    .onError(e -> "fallback");

            assertThat(pipeline.get()).isEqualTo("fallback");
        }

        @Test
        @DisplayName("onErrorAsync使用异步恢复函数")
        void testOnErrorAsync() {
            AsyncPipeline<String> pipeline = AsyncPipeline.<String>failed(new RuntimeException("error"))
                    .onErrorAsync(e -> CompletableFuture.completedFuture("async fallback"));

            assertThat(pipeline.get()).isEqualTo("async fallback");
        }

        @Test
        @DisplayName("handle同时处理成功和错误")
        void testHandle() {
            AsyncPipeline<String> success = AsyncPipeline.completed("value")
                    .handle((v, e) -> e == null ? v.toUpperCase() : "error");

            AsyncPipeline<String> failure = AsyncPipeline.<String>failed(new RuntimeException())
                    .handle((v, e) -> e == null ? v : "error");

            assertThat(success.get()).isEqualTo("VALUE");
            assertThat(failure.get()).isEqualTo("error");
        }
    }

    @Nested
    @DisplayName("组合方法测试")
    class CombiningTests {

        @Test
        @DisplayName("combine组合两个流水线")
        void testCombine() {
            AsyncPipeline<String> pipeline = AsyncPipeline.completed("Hello")
                    .combine(AsyncPipeline.completed("World"), (a, b) -> a + " " + b);

            assertThat(pipeline.get()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("runAfter在另一个流水线完成后运行")
        void testRunAfter() {
            AsyncPipeline<String> first = AsyncPipeline.completed("first");
            AsyncPipeline<String> second = AsyncPipeline.completed("second")
                    .runAfter(first);

            assertThat(second.get()).isEqualTo("second");
        }
    }

    @Nested
    @DisplayName("终端操作测试")
    class TerminalOperationsTests {

        @Test
        @DisplayName("get阻塞获取结果")
        void testGet() {
            AsyncPipeline<String> pipeline = AsyncPipeline.completed("result");

            assertThat(pipeline.get()).isEqualTo("result");
        }

        @Test
        @DisplayName("get带超时获取结果")
        void testGetWithTimeout() throws TimeoutException {
            AsyncPipeline<String> pipeline = AsyncPipeline.completed("result");

            assertThat(pipeline.get(Duration.ofSeconds(1))).isEqualTo("result");
        }

        @Test
        @DisplayName("get超时时抛出TimeoutException")
        void testGetTimeout() {
            AsyncPipeline<String> pipeline = AsyncPipeline.of(
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "slow";
                    }));

            assertThatThrownBy(() -> pipeline.get(Duration.ofMillis(50)))
                    .isInstanceOf(TimeoutException.class);
        }

        @Test
        @DisplayName("getOrDefault错误时返回默认值")
        void testGetOrDefault() {
            AsyncPipeline<String> pipeline = AsyncPipeline.failed(new RuntimeException());

            assertThat(pipeline.getOrDefault("default")).isEqualTo("default");
        }

        @Test
        @DisplayName("getOptional返回Optional")
        void testGetOptional() {
            AsyncPipeline<String> success = AsyncPipeline.completed("value");
            AsyncPipeline<String> failure = AsyncPipeline.failed(new RuntimeException());

            assertThat(success.getOptional()).isEqualTo(Optional.of("value"));
            assertThat(failure.getOptional()).isEmpty();
        }

        @Test
        @DisplayName("toFuture返回底层Future")
        void testToFuture() {
            CompletableFuture<String> future = CompletableFuture.completedFuture("test");
            AsyncPipeline<String> pipeline = AsyncPipeline.of(future);

            assertThat(pipeline.toFuture()).isEqualTo(future);
        }
    }

    @Nested
    @DisplayName("状态检查测试")
    class StateCheckTests {

        @Test
        @DisplayName("isDone检查是否完成")
        void testIsDone() {
            AsyncPipeline<String> completed = AsyncPipeline.completed("test");
            AsyncPipeline<String> notCompleted = AsyncPipeline.of(new CompletableFuture<>());

            assertThat(completed.isDone()).isTrue();
            assertThat(notCompleted.isDone()).isFalse();
        }

        @Test
        @DisplayName("isCompletedExceptionally检查是否异常完成")
        void testIsCompletedExceptionally() {
            AsyncPipeline<String> success = AsyncPipeline.completed("test");
            AsyncPipeline<String> failure = AsyncPipeline.failed(new RuntimeException());

            assertThat(success.isCompletedExceptionally()).isFalse();
            assertThat(failure.isCompletedExceptionally()).isTrue();
        }

        @Test
        @DisplayName("cancel取消流水线")
        void testCancel() {
            CompletableFuture<String> future = new CompletableFuture<>();
            AsyncPipeline<String> pipeline = AsyncPipeline.of(future);

            boolean cancelled = pipeline.cancel(true);

            assertThat(cancelled).isTrue();
            assertThat(future.isCancelled()).isTrue();
        }
    }
}
