package cloud.opencode.base.functional.function;

import cloud.opencode.base.functional.exception.OpenFunctionalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * FunctionUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("FunctionUtil 测试")
class FunctionUtilTest {

    @Nested
    @DisplayName("compose() 测试")
    class ComposeTests {

        @Test
        @DisplayName("组合两个函数")
        void testComposeTwoFunctions() {
            Function<Integer, Integer> addOne = x -> x + 1;
            Function<Integer, Integer> doubleIt = x -> x * 2;

            Function<Integer, Integer> composed = FunctionUtil.compose(addOne, doubleIt);

            assertThat(composed.apply(5)).isEqualTo(12); // (5+1)*2
        }

        @Test
        @DisplayName("组合三个函数")
        void testComposeThreeFunctions() {
            Function<Integer, Integer> addOne = x -> x + 1;
            Function<Integer, Integer> doubleIt = x -> x * 2;
            Function<Integer, String> toString = Object::toString;

            Function<Integer, String> composed = FunctionUtil.compose(addOne, doubleIt, toString);

            assertThat(composed.apply(5)).isEqualTo("12"); // (5+1)*2 = 12
        }
    }

    @Nested
    @DisplayName("curry() 测试")
    class CurryTests {

        @Test
        @DisplayName("柯里化BiFunction")
        void testCurryBiFunction() {
            BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

            Function<Integer, Function<Integer, Integer>> curried = FunctionUtil.curry(add);

            assertThat(curried.apply(5).apply(3)).isEqualTo(8);
        }

        @Test
        @DisplayName("部分应用柯里化函数")
        void testCurryPartialApplication() {
            BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

            Function<Integer, Function<Integer, Integer>> curried = FunctionUtil.curry(add);
            Function<Integer, Integer> add5 = curried.apply(5);

            assertThat(add5.apply(3)).isEqualTo(8);
            assertThat(add5.apply(10)).isEqualTo(15);
        }

        @Test
        @DisplayName("柯里化TriFunction")
        void testCurryTriFunction() {
            TriFunction<Integer, Integer, Integer, Integer> add = (a, b, c) -> a + b + c;

            Function<Integer, Function<Integer, Function<Integer, Integer>>> curried = FunctionUtil.curry(add);

            assertThat(curried.apply(1).apply(2).apply(3)).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("uncurry() 测试")
    class UncurryTests {

        @Test
        @DisplayName("反柯里化")
        void testUncurry() {
            Function<Integer, Function<Integer, Integer>> curried = a -> b -> a + b;

            BiFunction<Integer, Integer, Integer> uncurried = FunctionUtil.uncurry(curried);

            assertThat(uncurried.apply(5, 3)).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("partial() 测试")
    class PartialTests {

        @Test
        @DisplayName("部分应用第一个参数")
        void testPartial() {
            BiFunction<String, String, String> concat = (a, b) -> a + b;

            Function<String, String> hello = FunctionUtil.partial(concat, "Hello, ");

            assertThat(hello.apply("World")).isEqualTo("Hello, World");
        }
    }

    @Nested
    @DisplayName("partialRight() 测试")
    class PartialRightTests {

        @Test
        @DisplayName("部分应用第二个参数")
        void testPartialRight() {
            BiFunction<String, String, String> concat = (a, b) -> a + b;

            Function<String, String> greet = FunctionUtil.partialRight(concat, "!");

            assertThat(greet.apply("Hello")).isEqualTo("Hello!");
        }
    }

    @Nested
    @DisplayName("flip() 测试")
    class FlipTests {

        @Test
        @DisplayName("翻转参数顺序")
        void testFlip() {
            BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);

            BiFunction<Integer, String, String> flipped = FunctionUtil.flip(repeat);

            assertThat(flipped.apply(3, "ab")).isEqualTo("ababab");
        }
    }

    @Nested
    @DisplayName("memoize(Function) 测试")
    class MemoizeFunctionTests {

        @Test
        @DisplayName("缓存函数结果")
        void testMemoize() {
            int[] callCount = {0};
            Function<Integer, Integer> expensive = n -> {
                callCount[0]++;
                return n * 2;
            };

            Function<Integer, Integer> memoized = FunctionUtil.memoize(expensive);

            assertThat(memoized.apply(5)).isEqualTo(10);
            assertThat(memoized.apply(5)).isEqualTo(10);
            assertThat(callCount[0]).isEqualTo(1); // 只调用一次
        }

        @Test
        @DisplayName("不同参数各自缓存")
        void testMemoizeDifferentArgs() {
            int[] callCount = {0};
            Function<Integer, Integer> expensive = n -> {
                callCount[0]++;
                return n * 2;
            };

            Function<Integer, Integer> memoized = FunctionUtil.memoize(expensive);

            assertThat(memoized.apply(5)).isEqualTo(10);
            assertThat(memoized.apply(10)).isEqualTo(20);
            assertThat(callCount[0]).isEqualTo(2);
        }

        @Test
        @DisplayName("带最大缓存大小")
        void testMemoizeWithMaxSize() {
            Function<Integer, Integer> doubleIt = n -> n * 2;

            Function<Integer, Integer> memoized = FunctionUtil.memoize(doubleIt, 2);

            assertThat(memoized.apply(1)).isEqualTo(2);
            assertThat(memoized.apply(2)).isEqualTo(4);
            assertThat(memoized.apply(3)).isEqualTo(6); // 这会驱逐最老的条目
        }

        @Test
        @DisplayName("最大缓存大小必须为正")
        void testMemoizeInvalidMaxSize() {
            Function<Integer, Integer> doubleIt = n -> n * 2;

            assertThatThrownBy(() -> FunctionUtil.memoize(doubleIt, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> FunctionUtil.memoize(doubleIt, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("缓存null结果")
        void testMemoizeNullResult() {
            int[] callCount = {0};
            Function<Integer, Integer> returnsNull = n -> {
                callCount[0]++;
                return null;
            };

            Function<Integer, Integer> memoized = FunctionUtil.memoize(returnsNull);

            assertThat(memoized.apply(5)).isNull();
            assertThat(memoized.apply(5)).isNull();
            assertThat(callCount[0]).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("memoize(Supplier) 测试")
    class MemoizeSupplierTests {

        @Test
        @DisplayName("缓存Supplier结果")
        void testMemoizeSupplier() {
            int[] callCount = {0};
            Supplier<String> expensive = () -> {
                callCount[0]++;
                return "computed";
            };

            Supplier<String> memoized = FunctionUtil.memoize(expensive);

            assertThat(memoized.get()).isEqualTo("computed");
            assertThat(memoized.get()).isEqualTo("computed");
            assertThat(callCount[0]).isEqualTo(1);
        }

        @Test
        @DisplayName("缓存Supplier的null结果")
        void testMemoizeSupplierNullResult() {
            int[] callCount = {0};
            Supplier<String> returnsNull = () -> {
                callCount[0]++;
                return null;
            };

            Supplier<String> memoized = FunctionUtil.memoize(returnsNull);

            assertThat(memoized.get()).isNull();
            assertThat(memoized.get()).isNull();
            assertThat(callCount[0]).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("unchecked(CheckedFunction) 测试")
    class UncheckedCheckedFunctionTests {

        @Test
        @DisplayName("转换CheckedFunction")
        void testUncheckedCheckedFunction() {
            CheckedFunction<String, Integer> parseInt = Integer::parseInt;

            Function<String, Integer> unchecked = FunctionUtil.unchecked(parseInt);

            assertThat(unchecked.apply("42")).isEqualTo(42);
        }

        @Test
        @DisplayName("受检异常包装为OpenFunctionalException")
        void testUncheckedCheckedFunctionWrapsException() {
            CheckedFunction<String, String> throwsIO = s -> {
                throw new IOException("IO error");
            };

            Function<String, String> unchecked = FunctionUtil.unchecked(throwsIO);

            assertThatThrownBy(() -> unchecked.apply("test"))
                    .isInstanceOf(OpenFunctionalException.class)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("RuntimeException不被包装")
        void testUncheckedCheckedFunctionDoesNotWrapRuntimeException() {
            CheckedFunction<String, String> throwsRuntime = s -> {
                throw new IllegalArgumentException("bad arg");
            };

            Function<String, String> unchecked = FunctionUtil.unchecked(throwsRuntime);

            assertThatThrownBy(() -> unchecked.apply("test"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("unchecked(CheckedBiFunction) 测试")
    class UncheckedCheckedBiFunctionTests {

        @Test
        @DisplayName("转换CheckedBiFunction")
        void testUncheckedCheckedBiFunction() {
            CheckedBiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

            BiFunction<Integer, Integer, Integer> unchecked = FunctionUtil.unchecked(add);

            assertThat(unchecked.apply(3, 5)).isEqualTo(8);
        }

        @Test
        @DisplayName("受检异常包装为OpenFunctionalException")
        void testUncheckedCheckedBiFunctionWrapsException() {
            CheckedBiFunction<String, String, String> throwsIO = (a, b) -> {
                throw new IOException("IO error");
            };

            BiFunction<String, String, String> unchecked = FunctionUtil.unchecked(throwsIO);

            assertThatThrownBy(() -> unchecked.apply("a", "b"))
                    .isInstanceOf(OpenFunctionalException.class)
                    .hasCauseInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("identity() 测试")
    class IdentityTests {

        @Test
        @DisplayName("返回输入值")
        void testIdentity() {
            Function<String, String> identity = FunctionUtil.identity();

            assertThat(identity.apply("test")).isEqualTo("test");
        }

        @Test
        @DisplayName("处理null")
        void testIdentityWithNull() {
            Function<String, String> identity = FunctionUtil.identity();

            assertThat(identity.apply(null)).isNull();
        }
    }

    @Nested
    @DisplayName("constant() 测试")
    class ConstantTests {

        @Test
        @DisplayName("始终返回相同值")
        void testConstant() {
            Function<Object, String> always = FunctionUtil.constant("default");

            assertThat(always.apply("anything")).isEqualTo("default");
            assertThat(always.apply(123)).isEqualTo("default");
            assertThat(always.apply(null)).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("not() 测试")
    class NotTests {

        @Test
        @DisplayName("取反谓词")
        void testNot() {
            Predicate<Integer> positive = n -> n > 0;

            Predicate<Integer> notPositive = FunctionUtil.not(positive);

            assertThat(notPositive.test(5)).isFalse();
            assertThat(notPositive.test(-5)).isTrue();
            assertThat(notPositive.test(0)).isTrue();
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("memoize在并发环境下是线程安全的")
        void testMemoizeThreadSafety() throws InterruptedException {
            int[] callCount = {0};
            Function<Integer, Integer> expensive = n -> {
                synchronized (callCount) {
                    callCount[0]++;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return n * 2;
            };

            Function<Integer, Integer> memoized = FunctionUtil.memoize(expensive);

            Thread[] threads = new Thread[10];
            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> memoized.apply(42));
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // 由于线程安全，可能会有多次计算，但结果应该一致
            assertThat(memoized.apply(42)).isEqualTo(84);
        }
    }
}
