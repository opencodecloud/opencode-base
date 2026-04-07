package cloud.opencode.base.functional.function;

import cloud.opencode.base.functional.monad.Option;
import cloud.opencode.base.functional.monad.Try;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for enhanced FunctionUtil methods: lift, liftTry, liftBi, liftBiTry.
 */
@DisplayName("FunctionUtil Enhanced Methods")
class FunctionUtilEnhancedTest {

    // ==================== lift ====================

    @Nested
    @DisplayName("lift")
    class LiftTest {

        @Test
        @DisplayName("lift returns Option.some on success")
        void liftReturnsSomeOnSuccess() {
            Function<String, Option<Integer>> safeParse = FunctionUtil.lift(Integer::parseInt);

            Option<Integer> result = safeParse.apply("123");

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(123);
        }

        @Test
        @DisplayName("lift returns Option.none on exception")
        void liftReturnsNoneOnException() {
            Function<String, Option<Integer>> safeParse = FunctionUtil.lift(Integer::parseInt);

            Option<Integer> result = safeParse.apply("not-a-number");

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("lift returns Option.none on null result")
        void liftReturnsNoneOnNullResult() {
            Function<String, Option<String>> fn = FunctionUtil.lift(s -> null);

            Option<String> result = fn.apply("input");

            // Option.of(null) returns none
            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("lift handles checked exceptions")
        void liftHandlesCheckedException() {
            CheckedFunction<String, String> throwing = s -> {
                throw new Exception("checked");
            };
            Function<String, Option<String>> safe = FunctionUtil.lift(throwing);

            Option<String> result = safe.apply("input");

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("lift handles RuntimeException")
        void liftHandlesRuntimeException() {
            Function<String, Option<Integer>> safeParse = FunctionUtil.lift(s -> {
                throw new ArithmeticException("boom");
            });

            Option<Integer> result = safeParse.apply("input");

            assertThat(result.isNone()).isTrue();
        }
    }

    // ==================== liftTry ====================

    @Nested
    @DisplayName("liftTry")
    class LiftTryTest {

        @Test
        @DisplayName("liftTry returns Try.success on success")
        void liftTryReturnsSuccessOnSuccess() {
            Function<String, Try<Integer>> safeParse = FunctionUtil.liftTry(Integer::parseInt);

            Try<Integer> result = safeParse.apply("42");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("liftTry returns Try.failure on exception")
        void liftTryReturnsFailureOnException() {
            Function<String, Try<Integer>> safeParse = FunctionUtil.liftTry(Integer::parseInt);

            Try<Integer> result = safeParse.apply("bad");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).isPresent();
            assertThat(result.getCause().get()).isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("liftTry preserves exception type")
        void liftTryPreservesExceptionType() {
            CheckedFunction<String, String> throwing = s -> {
                throw new IllegalStateException("test error");
            };
            Function<String, Try<String>> safe = FunctionUtil.liftTry(throwing);

            Try<String> result = safe.apply("input");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).isPresent();
            assertThat(result.getCause().get())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("test error");
        }

        @Test
        @DisplayName("liftTry handles checked exceptions")
        void liftTryHandlesCheckedException() {
            CheckedFunction<String, String> throwing = s -> {
                throw new Exception("checked ex");
            };
            Function<String, Try<String>> safe = FunctionUtil.liftTry(throwing);

            Try<String> result = safe.apply("input");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).isPresent();
            assertThat(result.getCause().get())
                    .isInstanceOf(Exception.class)
                    .hasMessage("checked ex");
        }

        @Test
        @DisplayName("liftTry success with null result")
        void liftTrySuccessWithNullResult() {
            Function<String, Try<String>> fn = FunctionUtil.liftTry(s -> null);

            Try<String> result = fn.apply("input");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isNull();
        }
    }

    // ==================== liftBi ====================

    @Nested
    @DisplayName("liftBi")
    class LiftBiTest {

        @Test
        @DisplayName("liftBi returns Option.some on success")
        void liftBiReturnsSomeOnSuccess() {
            BiFunction<String, Integer, Option<String>> safeSub =
                    FunctionUtil.liftBi((s, len) -> s.substring(0, len));

            Option<String> result = safeSub.apply("hello", 3);

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo("hel");
        }

        @Test
        @DisplayName("liftBi returns Option.none on exception")
        void liftBiReturnsNoneOnException() {
            BiFunction<String, Integer, Option<String>> safeSub =
                    FunctionUtil.liftBi((s, len) -> s.substring(0, len));

            Option<String> result = safeSub.apply("hi", 100);

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("liftBi handles checked exception")
        void liftBiHandlesCheckedException() {
            CheckedBiFunction<String, String, String> throwing =
                    (a, b) -> { throw new Exception("checked"); };
            BiFunction<String, String, Option<String>> safe = FunctionUtil.liftBi(throwing);

            Option<String> result = safe.apply("a", "b");

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("liftBi returns Option.none on null result")
        void liftBiReturnsNoneOnNull() {
            BiFunction<String, String, Option<String>> fn =
                    FunctionUtil.liftBi((a, b) -> null);

            Option<String> result = fn.apply("a", "b");

            assertThat(result.isNone()).isTrue();
        }
    }

    // ==================== liftBiTry ====================

    @Nested
    @DisplayName("liftBiTry")
    class LiftBiTryTest {

        @Test
        @DisplayName("liftBiTry returns Try.success on success")
        void liftBiTryReturnsSuccessOnSuccess() {
            BiFunction<Integer, Integer, Try<Integer>> safeDivide =
                    FunctionUtil.liftBiTry((a, b) -> a / b);

            Try<Integer> result = safeDivide.apply(10, 2);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("liftBiTry returns Try.failure on exception")
        void liftBiTryReturnsFailureOnException() {
            BiFunction<Integer, Integer, Try<Integer>> safeDivide =
                    FunctionUtil.liftBiTry((a, b) -> a / b);

            Try<Integer> result = safeDivide.apply(10, 0);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).isPresent();
            assertThat(result.getCause().get()).isInstanceOf(ArithmeticException.class);
        }

        @Test
        @DisplayName("liftBiTry preserves checked exception")
        void liftBiTryPreservesCheckedException() {
            CheckedBiFunction<String, String, String> throwing =
                    (a, b) -> { throw new Exception("bi-checked"); };
            BiFunction<String, String, Try<String>> safe = FunctionUtil.liftBiTry(throwing);

            Try<String> result = safe.apply("a", "b");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).isPresent();
            assertThat(result.getCause().get())
                    .isInstanceOf(Exception.class)
                    .hasMessage("bi-checked");
        }

        @Test
        @DisplayName("liftBiTry success with null result")
        void liftBiTrySuccessWithNullResult() {
            BiFunction<String, String, Try<String>> fn =
                    FunctionUtil.liftBiTry((a, b) -> null);

            Try<String> result = fn.apply("a", "b");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isNull();
        }
    }

    // ==================== Integration ====================

    @Nested
    @DisplayName("Integration")
    class IntegrationTest {

        @Test
        @DisplayName("lift + map on Option")
        void liftThenMapOption() {
            Function<String, Option<Integer>> safeParse = FunctionUtil.lift(Integer::parseInt);

            Option<String> result = safeParse.apply("42")
                    .map(n -> "Value: " + n);

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo("Value: 42");
        }

        @Test
        @DisplayName("liftTry + recover")
        void liftTryThenRecover() {
            Function<String, Try<Integer>> safeParse = FunctionUtil.liftTry(Integer::parseInt);

            int result = safeParse.apply("bad")
                    .recover(e -> -1)
                    .get();

            assertThat(result).isEqualTo(-1);
        }

        @Test
        @DisplayName("liftBi used with stream mapping")
        void liftBiWithStream() {
            BiFunction<String, Integer, Option<String>> safeSub =
                    FunctionUtil.liftBi((s, len) -> s.substring(0, len));

            var results = java.util.List.of("hello", "hi", "hey").stream()
                    .map(s -> safeSub.apply(s, 3))
                    .filter(Option::isSome)
                    .map(Option::get)
                    .toList();

            // "hi" has length 2, substring(0,3) throws -> none
            assertThat(results).containsExactly("hel", "hey");
        }
    }
}
