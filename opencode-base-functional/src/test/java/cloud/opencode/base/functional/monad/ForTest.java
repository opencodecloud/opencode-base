package cloud.opencode.base.functional.monad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * For 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("For 测试")
class ForTest {

    @Nested
    @DisplayName("Option For 测试")
    class OptionForTests {

        @Test
        @DisplayName("单个 Option yield")
        void testOptionFor1Yield() {
            Option<Integer> result = For.of(Option.some(5))
                    .yield(n -> n * 2);

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("单个 Option filter")
        void testOptionFor1Filter() {
            Option<Integer> result = For.of(Option.some(5))
                    .filter(n -> n > 3);

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("单个 Option filter 不满足")
        void testOptionFor1FilterNotSatisfied() {
            Option<Integer> result = For.of(Option.some(2))
                    .filter(n -> n > 3);

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("两个 Option yield")
        void testOptionFor2Yield() {
            Option<Integer> result = For.of(Option.some(3))
                    .and(Option.some(5))
                    .yield(Integer::sum);

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(8);
        }

        @Test
        @DisplayName("两个 Option 其中一个为 None")
        void testOptionFor2WithNone() {
            Option<Integer> result = For.of(Option.some(3))
                    .and(Option.<Integer>none())
                    .yield(Integer::sum);

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("两个 Option filter")
        void testOptionFor2Filter() {
            For.OptionFor2<Integer, Integer> filtered = For.of(Option.some(3))
                    .and(Option.some(5))
                    .filter((a, b) -> a + b > 5);

            Option<Integer> result = filtered.yield(Integer::sum);

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(8);
        }

        @Test
        @DisplayName("三个 Option yield")
        void testOptionFor3Yield() {
            Option<Integer> result = For.of(Option.some(1))
                    .and(Option.some(2))
                    .and(Option.some(3))
                    .yield((a, b, c) -> a + b + c);

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(6);
        }

        @Test
        @DisplayName("四个 Option yield")
        void testOptionFor4Yield() {
            Option<Integer> result = For.of(Option.some(1))
                    .and(Option.some(2))
                    .and(Option.some(3))
                    .and(Option.some(4))
                    .yield((a, b, c, d) -> a + b + c + d);

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("Option For 使用 Supplier")
        void testOptionForWithSupplier() {
            Option<Integer> result = For.of(Option.some(5))
                    .and(() -> Option.some(3))
                    .yield(Integer::sum);

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("Try For 测试")
    class TryForTests {

        @Test
        @DisplayName("单个 Try yield")
        void testTryFor1Yield() {
            Try<Integer> result = For.of(Try.success(5))
                    .yield(n -> n * 2);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("单个 Try filter")
        void testTryFor1Filter() {
            Try<Integer> result = For.of(Try.success(5))
                    .filter(n -> n > 3);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("两个 Try yield")
        void testTryFor2Yield() {
            Try<Integer> result = For.of(Try.success(3))
                    .and(Try.success(5))
                    .yield(Integer::sum);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(8);
        }

        @Test
        @DisplayName("两个 Try 其中一个为 Failure")
        void testTryFor2WithFailure() {
            Try<Integer> result = For.of(Try.success(3))
                    .and(Try.<Integer>failure(new RuntimeException("error")))
                    .yield(Integer::sum);

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("三个 Try yield")
        void testTryFor3Yield() {
            Try<Integer> result = For.of(Try.success(1))
                    .and(Try.success(2))
                    .and(Try.success(3))
                    .yield((a, b, c) -> a + b + c);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(6);
        }

        @Test
        @DisplayName("四个 Try yield")
        void testTryFor4Yield() {
            Try<Integer> result = For.of(Try.success(1))
                    .and(Try.success(2))
                    .and(Try.success(3))
                    .and(Try.success(4))
                    .yield((a, b, c, d) -> a + b + c + d);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("Try For 使用 Supplier")
        void testTryForWithSupplier() {
            Try<Integer> result = For.of(Try.success(5))
                    .and(() -> Try.success(3))
                    .yield(Integer::sum);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("Iterable For 测试")
    class IterableForTests {

        @Test
        @DisplayName("单个 Iterable yield")
        void testIterableFor1Yield() {
            Sequence<Integer> result = For.of(List.of(1, 2, 3))
                    .yield(n -> n * 2);

            assertThat(result.toList()).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("单个 Iterable filter")
        void testIterableFor1Filter() {
            Sequence<Integer> result = For.of(List.of(1, 2, 3, 4, 5))
                    .filter(n -> n > 2)
                    .yield(n -> n);

            assertThat(result.toList()).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("两个 Iterable yield (笛卡尔积)")
        void testIterableFor2Yield() {
            Sequence<String> result = For.of(List.of("a", "b"))
                    .and(List.of(1, 2))
                    .yield((s, n) -> s + n);

            assertThat(result.toList()).containsExactly("a1", "a2", "b1", "b2");
        }

        @Test
        @DisplayName("三个 Iterable yield")
        void testIterableFor3Yield() {
            Sequence<Integer> result = For.of(List.of(1, 2))
                    .and(List.of(10, 20))
                    .and(List.of(100, 200))
                    .yield((a, b, c) -> a + b + c);

            assertThat(result.toList()).containsExactly(111, 211, 121, 221, 112, 212, 122, 222);
        }
    }

    @Nested
    @DisplayName("Sequence For 测试")
    class SequenceForTests {

        @Test
        @DisplayName("单个 Sequence yield")
        void testSequenceFor1Yield() {
            Sequence<Integer> result = For.of(Sequence.of(1, 2, 3))
                    .yield(n -> n * 2);

            assertThat(result.toList()).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("单个 Sequence filter")
        void testSequenceFor1Filter() {
            Sequence<Integer> result = For.of(Sequence.of(1, 2, 3, 4, 5))
                    .filter(n -> n > 2)
                    .yield(n -> n);

            assertThat(result.toList()).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("两个 Sequence yield (笛卡尔积)")
        void testSequenceFor2Yield() {
            Sequence<String> result = For.of(Sequence.of("a", "b"))
                    .and(Sequence.of(1, 2))
                    .yield((s, n) -> s + n);

            assertThat(result.toList()).containsExactly("a1", "a2", "b1", "b2");
        }

        @Test
        @DisplayName("三个 Sequence yield")
        void testSequenceFor3Yield() {
            Sequence<Integer> result = For.of(Sequence.of(1, 2))
                    .and(Sequence.of(10, 20))
                    .and(Sequence.of(100, 200))
                    .yield((a, b, c) -> a + b + c);

            assertThat(result.toList()).containsExactly(111, 211, 121, 221, 112, 212, 122, 222);
        }
    }

    @Nested
    @DisplayName("Function 接口测试")
    class FunctionInterfaceTests {

        @Test
        @DisplayName("Function3 测试")
        void testFunction3() {
            For.Function3<Integer, Integer, Integer, Integer> add3 = (a, b, c) -> a + b + c;

            assertThat(add3.apply(1, 2, 3)).isEqualTo(6);
        }

        @Test
        @DisplayName("Function4 测试")
        void testFunction4() {
            For.Function4<Integer, Integer, Integer, Integer, Integer> add4 = (a, b, c, d) -> a + b + c + d;

            assertThat(add4.apply(1, 2, 3, 4)).isEqualTo(10);
        }

        @Test
        @DisplayName("Function5 测试")
        void testFunction5() {
            For.Function5<Integer, Integer, Integer, Integer, Integer, Integer> add5 =
                    (a, b, c, d, e) -> a + b + c + d + e;

            assertThat(add5.apply(1, 2, 3, 4, 5)).isEqualTo(15);
        }
    }
}
