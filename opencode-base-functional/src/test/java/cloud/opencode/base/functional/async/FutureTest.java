package cloud.opencode.base.functional.async;

import cloud.opencode.base.functional.monad.Option;
import cloud.opencode.base.functional.monad.Try;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Future 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Future 测试")
class FutureTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() 使用 ForkJoinPool 创建 Future")
        void testOf() {
            Future<Integer> future = Future.of(() -> 42);

            assertThat(future.await().get()).isEqualTo(42);
        }

        @Test
        @DisplayName("of() 带自定义执行器")
        void testOfWithExecutor() {
            Future<Integer> future = Future.of(() -> 42, Runnable::run);

            assertThat(future.await().get()).isEqualTo(42);
        }

        @Test
        @DisplayName("ofVirtual() 使用虚拟线程")
        void testOfVirtual() {
            Future<Integer> future = Future.ofVirtual(() -> 42);

            assertThat(future.await().get()).isEqualTo(42);
        }

        @Test
        @DisplayName("fromCallable() 从 Callable 创建")
        void testFromCallable() {
            Future<Integer> future = Future.fromCallable(() -> 42);

            assertThat(future.await().get()).isEqualTo(42);
        }

        @Test
        @DisplayName("successful() 创建成功的 Future")
        void testSuccessful() {
            Future<Integer> future = Future.successful(42);

            assertThat(future.isCompleted()).isTrue();
            assertThat(future.isSuccess()).isTrue();
            assertThat(future.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("failed() 创建失败的 Future")
        void testFailed() {
            Future<Integer> future = Future.failed(new RuntimeException("error"));

            assertThat(future.isCompleted()).isTrue();
            assertThat(future.isFailure()).isTrue();
        }

        @Test
        @DisplayName("fromCompletableFuture() 包装 CompletableFuture")
        void testFromCompletableFuture() {
            CompletableFuture<Integer> cf = CompletableFuture.completedFuture(42);
            Future<Integer> future = Future.fromCompletableFuture(cf);

            assertThat(future.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("never() 创建永不完成的 Future")
        void testNever() {
            Future<Integer> future = Future.never();

            assertThat(future.isCompleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("map() 测试")
    class MapTests {

        @Test
        @DisplayName("map() 转换值")
        void testMap() {
            Future<Integer> future = Future.successful(5)
                    .map(n -> n * 2);

            assertThat(future.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("map() 失败时保持失败")
        void testMapOnFailure() {
            Future<Integer> future = Future.<Integer>failed(new RuntimeException("error"))
                    .map(n -> n * 2);

            assertThat(future.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("flatMap() 测试")
    class FlatMapTests {

        @Test
        @DisplayName("flatMap() 成功转换")
        void testFlatMap() {
            Future<Integer> future = Future.successful(5)
                    .flatMap(n -> Future.successful(n * 2));

            assertThat(future.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("flatMap() 转换为失败的 Future")
        void testFlatMapToFailure() {
            Future<Integer> future = Future.successful(5)
                    .flatMap(n -> Future.failed(new RuntimeException("error")));

            assertThat(future.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("filter() 测试")
    class FilterTests {

        @Test
        @DisplayName("filter() 条件满足")
        void testFilterMatches() {
            Future<Integer> future = Future.successful(10)
                    .filter(n -> n > 5);

            assertThat(future.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("filter() 条件不满足时失败")
        void testFilterNotMatches() {
            Future<Integer> future = Future.successful(3)
                    .filter(n -> n > 5);

            assertThat(future.isFailure()).isTrue();
            assertThat(future.await().getCause().get()).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("onSuccess/onFailure/onComplete 测试")
    class CallbackTests {

        @Test
        @DisplayName("onSuccess() 成功时执行")
        void testOnSuccess() throws InterruptedException {
            AtomicReference<Integer> captured = new AtomicReference<>();
            Future<Integer> future = Future.successful(42)
                    .onSuccess(captured::set);

            Thread.sleep(50);
            assertThat(captured.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("onFailure() 失败时执行")
        void testOnFailure() throws InterruptedException {
            AtomicBoolean called = new AtomicBoolean(false);
            Future<Integer> future = Future.<Integer>failed(new RuntimeException("error"))
                    .onFailure(e -> called.set(true));

            Thread.sleep(50);
            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("onComplete() 完成时执行")
        void testOnComplete() throws InterruptedException {
            AtomicReference<Integer> value = new AtomicReference<>();
            AtomicReference<Throwable> error = new AtomicReference<>();
            Future<Integer> future = Future.successful(42)
                    .onComplete((v, e) -> {
                        value.set(v);
                        error.set(e);
                    });

            Thread.sleep(50);
            assertThat(value.get()).isEqualTo(42);
            assertThat(error.get()).isNull();
        }
    }

    @Nested
    @DisplayName("recover 测试")
    class RecoverTests {

        @Test
        @DisplayName("recover() 从失败恢复")
        void testRecover() {
            Future<Integer> future = Future.<Integer>failed(new RuntimeException("error"))
                    .recover(e -> 0);

            assertThat(future.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("recover() 成功时不执行")
        void testRecoverOnSuccess() {
            Future<Integer> future = Future.successful(42)
                    .recover(e -> 0);

            assertThat(future.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("recover(Class, Function) 从特定异常恢复")
        void testRecoverSpecificException() {
            Future<Integer> future = Future.<Integer>failed(new IllegalArgumentException("bad arg"))
                    .recover(IllegalArgumentException.class, e -> 0);

            assertThat(future.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("recoverWith() 使用 Future 恢复")
        void testRecoverWith() {
            Future<Integer> future = Future.<Integer>failed(new RuntimeException("error"))
                    .recoverWith(e -> Future.successful(0));

            assertThat(future.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("orElse(T) 提供备用值")
        void testOrElseValue() {
            Future<Integer> future = Future.<Integer>failed(new RuntimeException("error"))
                    .orElse(0);

            assertThat(future.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("orElse(Future) 提供备用 Future")
        void testOrElseFuture() {
            Future<Integer> future = Future.<Integer>failed(new RuntimeException("error"))
                    .orElse(Future.successful(0));

            assertThat(future.get()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("timeout 测试")
    class TimeoutTests {

        @Test
        @DisplayName("timeout() 应用超时")
        void testTimeout() {
            Future<Integer> future = Future.of(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 42;
            }).timeout(Duration.ofMillis(50));

            Try<Integer> result = future.await();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause().get()).isInstanceOf(TimeoutException.class);
        }

        @Test
        @DisplayName("timeout() 带默认值")
        void testTimeoutWithDefault() {
            Future<Integer> future = Future.of(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 42;
            }).timeout(Duration.ofMillis(50), 0);

            assertThat(future.get()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("await 测试")
    class AwaitTests {

        @Test
        @DisplayName("await() 成功返回 Try.Success")
        void testAwaitSuccess() {
            Future<Integer> future = Future.successful(42);

            Try<Integer> result = future.await();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("await() 失败返回 Try.Failure")
        void testAwaitFailure() {
            Future<Integer> future = Future.failed(new RuntimeException("error"));

            Try<Integer> result = future.await();

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("await(Duration) 带超时")
        void testAwaitWithTimeout() {
            Future<Integer> future = Future.of(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 42;
            });

            Try<Integer> result = future.await(Duration.ofMillis(50));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause().get()).isInstanceOf(TimeoutException.class);
        }
    }

    @Nested
    @DisplayName("get 测试")
    class GetTests {

        @Test
        @DisplayName("get() 返回值")
        void testGet() {
            Future<Integer> future = Future.successful(42);

            assertThat(future.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("get() 失败时抛出异常")
        void testGetFailure() {
            Future<Integer> future = Future.failed(new RuntimeException("error"));

            assertThatThrownBy(future::get).isInstanceOf(CompletionException.class);
        }

        @Test
        @DisplayName("getOrElse() 失败时返回默认值")
        void testGetOrElse() {
            Future<Integer> future = Future.failed(new RuntimeException("error"));

            assertThat(future.getOrElse(0)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("状态查询测试")
    class StatusTests {

        @Test
        @DisplayName("isCompleted() 测试")
        void testIsCompleted() {
            Future<Integer> completed = Future.successful(42);
            Future<Integer> notCompleted = Future.never();

            assertThat(completed.isCompleted()).isTrue();
            assertThat(notCompleted.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("isSuccess() 测试")
        void testIsSuccess() {
            Future<Integer> success = Future.successful(42);
            Future<Integer> failure = Future.failed(new RuntimeException("error"));

            assertThat(success.isSuccess()).isTrue();
            assertThat(failure.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("isFailure() 测试")
        void testIsFailure() {
            Future<Integer> success = Future.successful(42);
            Future<Integer> failure = Future.failed(new RuntimeException("error"));

            assertThat(success.isFailure()).isFalse();
            assertThat(failure.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toOption() 成功返回 Some")
        void testToOptionSuccess() {
            Future<Integer> future = Future.successful(42);

            Option<Integer> option = future.toOption();

            assertThat(option.isSome()).isTrue();
            assertThat(option.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("toOption() 失败返回 None")
        void testToOptionFailure() {
            Future<Integer> future = Future.failed(new RuntimeException("error"));

            Option<Integer> option = future.toOption();

            assertThat(option.isNone()).isTrue();
        }

        @Test
        @DisplayName("toTry() 返回 Try")
        void testToTry() {
            Future<Integer> future = Future.successful(42);

            Try<Integer> result = future.toTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("toCompletableFuture() 返回底层 CompletableFuture")
        void testToCompletableFuture() {
            Future<Integer> future = Future.successful(42);

            CompletableFuture<Integer> cf = future.toCompletableFuture();

            assertThat(cf.join()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("组合测试")
    class CombiningTests {

        @Test
        @DisplayName("zip() 合并两个 Future")
        void testZip() {
            Future<Integer> future = Future.zip(
                    Future.successful(1),
                    Future.successful(2),
                    Integer::sum
            );

            assertThat(future.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("zipWith() 与另一个 Future 合并")
        void testZipWith() {
            Future<Integer> future = Future.successful(1)
                    .zipWith(Future.successful(2), Integer::sum);

            assertThat(future.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("sequence() 将 Future 列表转为列表 Future")
        void testSequence() {
            Future<List<Integer>> future = Future.sequence(
                    Future.successful(1),
                    Future.successful(2),
                    Future.successful(3)
            );

            assertThat(future.get()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("sequence(List) 空列表")
        void testSequenceEmptyList() {
            Future<List<Integer>> future = Future.sequence(List.of());

            assertThat(future.get()).isEmpty();
        }

        @Test
        @DisplayName("traverse() 映射并序列化")
        void testTraverse() {
            List<Integer> items = List.of(1, 2, 3);

            Future<List<Integer>> future = Future.traverse(items, n -> Future.successful(n * 2));

            assertThat(future.get()).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("firstCompleted() 返回第一个完成的")
        void testFirstCompleted() {
            Future<Integer> future = Future.firstCompleted(
                    Future.successful(1),
                    Future.successful(2)
            );

            assertThat(future.get()).isIn(1, 2);
        }

        @Test
        @DisplayName("firstCompleted() 空数组返回 never")
        void testFirstCompletedEmpty() {
            Future<Integer> future = Future.firstCompleted();

            assertThat(future.isCompleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("虚拟线程操作测试")
    class VirtualThreadTests {

        @Test
        @DisplayName("andThenVirtual() 在虚拟线程上执行")
        void testAndThenVirtual() {
            Future<Integer> future = Future.successful(5)
                    .andThenVirtual(n -> n * 2);

            assertThat(future.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("flatMapVirtual() 在虚拟线程上扁平映射")
        void testFlatMapVirtual() {
            Future<Integer> future = Future.successful(5)
                    .flatMapVirtual(n -> Future.successful(n * 2));

            assertThat(future.get()).isEqualTo(10);
        }
    }
}
