package cloud.opencode.base.functional.monad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Trampoline 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Trampoline 测试")
class TrampolineTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("done() 创建已完成的 Trampoline")
        void testDone() {
            Trampoline<Integer> trampoline = Trampoline.done(42);

            assertThat(trampoline.isDone()).isTrue();
            assertThat(trampoline.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("more() 创建挂起的 Trampoline")
        void testMore() {
            Trampoline<Integer> trampoline = Trampoline.more(() -> Trampoline.done(42));

            assertThat(trampoline.isDone()).isFalse();
            assertThat(trampoline.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("suspend() 是 more() 的别名")
        void testSuspend() {
            Trampoline<Integer> trampoline = Trampoline.suspend(() -> Trampoline.done(42));

            assertThat(trampoline.isDone()).isFalse();
            assertThat(trampoline.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("delay() 延迟计算值")
        void testDelay() {
            Trampoline<Integer> trampoline = Trampoline.delay(() -> 42);

            assertThat(trampoline.isDone()).isFalse();
            assertThat(trampoline.get()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("get()/run() 测试")
    class GetRunTests {

        @Test
        @DisplayName("get() 执行蹦床并返回结果")
        void testGet() {
            Trampoline<Integer> trampoline = Trampoline.done(42);

            assertThat(trampoline.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("run() 是 get() 的别名")
        void testRun() {
            Trampoline<Integer> trampoline = Trampoline.done(42);

            assertThat(trampoline.run()).isEqualTo(42);
        }

        @Test
        @DisplayName("get() 迭代执行延续")
        void testGetIteratesMore() {
            Trampoline<Integer> trampoline = Trampoline.more(() ->
                    Trampoline.more(() ->
                            Trampoline.more(() ->
                                    Trampoline.done(42))));

            assertThat(trampoline.get()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("isDone() 测试")
    class IsDoneTests {

        @Test
        @DisplayName("Done.isDone() 返回 true")
        void testDoneIsDone() {
            Trampoline<Integer> trampoline = Trampoline.done(42);

            assertThat(trampoline.isDone()).isTrue();
        }

        @Test
        @DisplayName("More.isDone() 返回 false")
        void testMoreIsDone() {
            Trampoline<Integer> trampoline = Trampoline.more(() -> Trampoline.done(42));

            assertThat(trampoline.isDone()).isFalse();
        }
    }

    @Nested
    @DisplayName("map() 测试")
    class MapTests {

        @Test
        @DisplayName("map() 转换结果")
        void testMap() {
            Trampoline<Integer> trampoline = Trampoline.done(5)
                    .map(n -> n * 2);

            assertThat(trampoline.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("map() 链式调用")
        void testMapChain() {
            Trampoline<Integer> trampoline = Trampoline.done(5)
                    .map(n -> n * 2)
                    .map(n -> n + 1);

            assertThat(trampoline.get()).isEqualTo(11);
        }
    }

    @Nested
    @DisplayName("flatMap() 测试")
    class FlatMapTests {

        @Test
        @DisplayName("flatMap() 链接 Trampoline 计算")
        void testFlatMap() {
            Trampoline<Integer> trampoline = Trampoline.done(5)
                    .flatMap(n -> Trampoline.done(n * 2));

            assertThat(trampoline.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("flatMap() 链式调用")
        void testFlatMapChain() {
            Trampoline<Integer> trampoline = Trampoline.done(5)
                    .flatMap(n -> Trampoline.done(n * 2))
                    .flatMap(n -> Trampoline.done(n + 1));

            assertThat(trampoline.get()).isEqualTo(11);
        }

        @Test
        @DisplayName("flatMap() 在 More 上工作")
        void testFlatMapOnMore() {
            Trampoline<Integer> trampoline = Trampoline.more(() -> Trampoline.done(5))
                    .flatMap(n -> Trampoline.done(n * 2));

            assertThat(trampoline.get()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("peek() 测试")
    class PeekTests {

        @Test
        @DisplayName("peek() 执行副作用")
        void testPeek() {
            AtomicReference<Integer> captured = new AtomicReference<>();
            Trampoline<Integer> trampoline = Trampoline.done(42)
                    .peek(captured::set);

            trampoline.get();

            assertThat(captured.get()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("toLazy() 测试")
    class ToLazyTests {

        @Test
        @DisplayName("toLazy() 转换为 Lazy")
        void testToLazy() {
            Lazy<Integer> lazy = Trampoline.done(42).toLazy();

            assertThat(lazy.isEvaluated()).isFalse();
            assertThat(lazy.get()).isEqualTo(42);
            assertThat(lazy.isEvaluated()).isTrue();
        }
    }

    @Nested
    @DisplayName("toTry() 测试")
    class ToTryTests {

        @Test
        @DisplayName("toTry() 成功时返回 Success")
        void testToTrySuccess() {
            Try<Integer> result = Trampoline.done(42).toTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("toTry() 异常时返回 Failure")
        void testToTryFailure() {
            Trampoline<Integer> trampoline = Trampoline.more(() -> {
                throw new RuntimeException("error");
            });

            Try<Integer> result = trampoline.toTry();

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("iterate() 测试")
    class IterateTests {

        @Test
        @DisplayName("iterate() 迭代直到满足条件")
        void testIterate() {
            Trampoline<Integer> trampoline = Trampoline.iterate(
                    1,
                    n -> n >= 10,
                    n -> n + 1
            );

            assertThat(trampoline.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("iterate() 初始值满足条件时直接返回")
        void testIterateInitialSatisfies() {
            Trampoline<Integer> trampoline = Trampoline.iterate(
                    10,
                    n -> n >= 5,
                    n -> n + 1
            );

            assertThat(trampoline.get()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("repeat() 测试")
    class RepeatTests {

        @Test
        @DisplayName("repeat() 重复计算 n 次")
        void testRepeat() {
            Trampoline<Integer> trampoline = Trampoline.repeat(5, 0, n -> n + 1);

            assertThat(trampoline.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("repeat(0) 返回初始值")
        void testRepeatZero() {
            Trampoline<Integer> trampoline = Trampoline.repeat(0, 42, n -> n + 1);

            assertThat(trampoline.get()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("sequence() 测试")
    class SequenceStaticTests {

        @Test
        @DisplayName("sequence() 顺序执行多个 Trampoline")
        void testSequence() {
            Trampoline<Integer> trampoline = Trampoline.sequence(
                    Trampoline.done(1),
                    Trampoline.done(2),
                    Trampoline.done(3)
            );

            assertThat(trampoline.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("sequence() 空数组抛出异常")
        void testSequenceEmpty() {
            assertThatThrownBy(() -> Trampoline.sequence())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("At least one Trampoline required");
        }

        @Test
        @DisplayName("sequence(null) 抛出异常")
        void testSequenceNull() {
            assertThatThrownBy(() -> Trampoline.sequence((Trampoline<Integer>[]) null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("实现类测试")
    class ImplementationClassTests {

        @Test
        @DisplayName("Done record 测试")
        void testDoneRecord() {
            Trampoline.Done<Integer> done = new Trampoline.Done<>(42);

            assertThat(done.result()).isEqualTo(42);
            assertThat(done.isDone()).isTrue();
            assertThat(done.toString()).isEqualTo("Done[42]");
        }

        @Test
        @DisplayName("More record 测试")
        void testMoreRecord() {
            Trampoline.More<Integer> more = new Trampoline.More<>(() -> Trampoline.done(42));

            assertThat(more.isDone()).isFalse();
            assertThat(more.continuation().get().get()).isEqualTo(42);
            assertThat(more.toString()).isEqualTo("More[...]");
        }

        @Test
        @DisplayName("FlatMap record 测试")
        void testFlatMapRecord() {
            Trampoline<Integer> previous = Trampoline.done(5);
            Trampoline.FlatMap<Integer, Integer> flatMap = new Trampoline.FlatMap<>(
                    previous,
                    n -> Trampoline.done(n * 2)
            );

            assertThat(flatMap.isDone()).isFalse();
            assertThat(flatMap.previous()).isSameAs(previous);
            assertThat(flatMap.get()).isEqualTo(10);
            assertThat(flatMap.toString()).startsWith("FlatMap[");
        }
    }

    @Nested
    @DisplayName("栈安全递归测试")
    class StackSafeRecursionTests {

        // 使用蹦床实现阶乘
        Trampoline<Long> factorial(long n, long acc) {
            if (n <= 1) {
                return Trampoline.done(acc);
            }
            return Trampoline.more(() -> factorial(n - 1, n * acc));
        }

        @Test
        @DisplayName("阶乘 - 深度递归不会栈溢出")
        void testFactorialDeepRecursion() {
            // 这个深度的普通递归会导致栈溢出
            Trampoline<Long> trampoline = factorial(1000, 1);

            // 使用蹦床不会栈溢出
            Long result = trampoline.get();
            assertThat(result).isNotNull();
        }

        // 使用蹦床实现斐波那契
        Trampoline<Long> fibonacci(int n, long a, long b) {
            if (n == 0) return Trampoline.done(a);
            if (n == 1) return Trampoline.done(b);
            return Trampoline.more(() -> fibonacci(n - 1, b, a + b));
        }

        @Test
        @DisplayName("斐波那契 - 深度递归不会栈溢出")
        void testFibonacciDeepRecursion() {
            Trampoline<Long> trampoline = fibonacci(10000, 0, 1);

            Long result = trampoline.get();
            assertThat(result).isNotNull();
        }

        // 相互递归：判断奇偶
        Trampoline<Boolean> isEven(int n) {
            if (n == 0) return Trampoline.done(true);
            return Trampoline.more(() -> isOdd(n - 1));
        }

        Trampoline<Boolean> isOdd(int n) {
            if (n == 0) return Trampoline.done(false);
            return Trampoline.more(() -> isEven(n - 1));
        }

        @Test
        @DisplayName("相互递归 - 深度递归不会栈溢出")
        void testMutualRecursionDeepRecursion() {
            Trampoline<Boolean> trampoline = isEven(10000);

            Boolean result = trampoline.get();
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("相互递归 - 奇数判断")
        void testMutualRecursionOdd() {
            Trampoline<Boolean> trampoline = isOdd(10001);

            Boolean result = trampoline.get();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("FlatMap step 测试")
    class FlatMapStepTests {

        @Test
        @DisplayName("step() 在 Done 上返回应用函数的结果")
        void testStepOnDone() {
            Trampoline.FlatMap<Integer, Integer> flatMap = new Trampoline.FlatMap<>(
                    Trampoline.done(5),
                    n -> Trampoline.done(n * 2)
            );

            Trampoline<Integer> stepped = flatMap.step();

            assertThat(stepped.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("step() 在 More 上返回新的 FlatMap")
        void testStepOnMore() {
            Trampoline.FlatMap<Integer, Integer> flatMap = new Trampoline.FlatMap<>(
                    Trampoline.more(() -> Trampoline.done(5)),
                    n -> Trampoline.done(n * 2)
            );

            Trampoline<Integer> stepped = flatMap.step();

            assertThat(stepped.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("嵌套 FlatMap 重新平衡")
        void testNestedFlatMapRebalance() {
            Trampoline<Integer> trampoline = Trampoline.done(1)
                    .flatMap(a -> Trampoline.done(a + 1))
                    .flatMap(b -> Trampoline.done(b + 1))
                    .flatMap(c -> Trampoline.done(c + 1));

            assertThat(trampoline.get()).isEqualTo(4);
        }
    }
}
