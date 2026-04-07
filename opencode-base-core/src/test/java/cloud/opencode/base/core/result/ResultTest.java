package cloud.opencode.base.core.result;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

class ResultTest {

    // ==================== Factory Methods ====================

    @Nested
    class FactoryMethods {

        @Test
        void ofReturnsSuccessOnNormalExecution() {
            Result<String> result = Result.of(() -> "hello");
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrElse("")).isEqualTo("hello");
        }

        @Test
        void ofReturnsFailureOnException() {
            Result<String> result = Result.of(() -> {
                throw new IOException("file not found");
            });
            assertThat(result.isFailure()).isTrue();
        }

        @Test
        void ofReturnsSuccessWithNull() {
            Result<String> result = Result.of(() -> null);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrElse("default")).isNull();
        }

        @Test
        void successCreatesSuccessResult() {
            Result<Integer> result = Result.success(42);
            assertThat(result).isInstanceOf(Result.Success.class);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
        }

        @Test
        void successAllowsNullValue() {
            Result<Void> result = Result.success(null);
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        void failureCreatesFailureResult() {
            Result<Integer> result = Result.failure(new RuntimeException("boom"));
            assertThat(result).isInstanceOf(Result.Failure.class);
            assertThat(result.isFailure()).isTrue();
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        void failureRejectsNullCause() {
            assertThatThrownBy(() -> Result.failure(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void successVoidReturnsSuccessWithNullValue() {
            Result<Void> result = Result.successVoid();
            assertThat(result.isSuccess()).isTrue();
            assertThat(((Result.Success<Void>) result).value()).isNull();
        }
    }

    // ==================== Map ====================

    @Nested
    class Map {

        @Test
        void mapTransformsSuccessValue() {
            Result<Integer> result = Result.success(5);
            Result<String> mapped = result.map(i -> "val:" + i);
            assertThat(mapped.isSuccess()).isTrue();
            assertThat(mapped.getOrElse("")).isEqualTo("val:5");
        }

        @Test
        void mapCatchesExceptionInMapper() {
            Result<Integer> result = Result.success(5);
            Result<String> mapped = result.map(i -> {
                throw new RuntimeException("mapper failed");
            });
            assertThat(mapped.isFailure()).isTrue();
        }

        @Test
        void mapSkipsFailure() {
            Result<Integer> result = Result.failure(new RuntimeException("original"));
            Result<String> mapped = result.map(i -> "val:" + i);
            assertThat(mapped.isFailure()).isTrue();
            assertThat(((Result.Failure<String>) mapped).cause().getMessage()).isEqualTo("original");
        }
    }

    // ==================== FlatMap ====================

    @Nested
    class FlatMap {

        @Test
        void flatMapTransformsToSuccess() {
            Result<Integer> result = Result.success(5);
            Result<String> flat = result.flatMap(i -> Result.success("val:" + i));
            assertThat(flat.getOrElse("")).isEqualTo("val:5");
        }

        @Test
        void flatMapTransformsToFailure() {
            Result<Integer> result = Result.success(5);
            Result<String> flat = result.flatMap(i -> Result.failure(new RuntimeException("nope")));
            assertThat(flat.isFailure()).isTrue();
        }

        @Test
        void flatMapCatchesExceptionInMapper() {
            Result<Integer> result = Result.success(5);
            Result<String> flat = result.flatMap(i -> {
                throw new RuntimeException("mapper boom");
            });
            assertThat(flat.isFailure()).isTrue();
        }

        @Test
        void flatMapSkipsFailure() {
            Result<Integer> result = Result.failure(new RuntimeException("original"));
            Result<String> flat = result.flatMap(i -> Result.success("val:" + i));
            assertThat(flat.isFailure()).isTrue();
        }
    }

    // ==================== Recover ====================

    @Nested
    class Recover {

        @Test
        void recoverReturnsOriginalForSuccess() {
            Result<Integer> result = Result.success(42);
            Result<Integer> recovered = result.recover(ex -> 0);
            assertThat(recovered.getOrElse(0)).isEqualTo(42);
        }

        @Test
        void recoverAppliesFunctionForFailure() {
            Result<Integer> result = Result.failure(new RuntimeException("boom"));
            Result<Integer> recovered = result.recover(ex -> 99);
            assertThat(recovered.isSuccess()).isTrue();
            assertThat(recovered.getOrElse(0)).isEqualTo(99);
        }

        @Test
        void recoverCatchesExceptionInRecoverer() {
            Result<Integer> result = Result.failure(new RuntimeException("original"));
            Result<Integer> recovered = result.recover(ex -> {
                throw new RuntimeException("recover failed");
            });
            assertThat(recovered.isFailure()).isTrue();
        }

        @Test
        void recoverWithReturnsOriginalForSuccess() {
            Result<Integer> result = Result.success(42);
            Result<Integer> recovered = result.recoverWith(ex -> Result.success(0));
            assertThat(recovered.getOrElse(0)).isEqualTo(42);
        }

        @Test
        void recoverWithAppliesFunctionForFailure() {
            Result<Integer> result = Result.failure(new RuntimeException("boom"));
            Result<Integer> recovered = result.recoverWith(ex -> Result.success(99));
            assertThat(recovered.isSuccess()).isTrue();
            assertThat(recovered.getOrElse(0)).isEqualTo(99);
        }

        @Test
        void recoverWithCatchesExceptionInRecoverer() {
            Result<Integer> result = Result.failure(new RuntimeException("original"));
            Result<Integer> recovered = result.recoverWith(ex -> {
                throw new RuntimeException("recoverWith failed");
            });
            assertThat(recovered.isFailure()).isTrue();
        }
    }

    // ==================== Peek ====================

    @Nested
    class Peek {

        @Test
        void peekExecutesOnSuccess() {
            AtomicReference<Integer> ref = new AtomicReference<>();
            Result<Integer> result = Result.success(42);
            Result<Integer> same = result.peek(ref::set);
            assertThat(ref.get()).isEqualTo(42);
            assertThat(same).isSameAs(result);
        }

        @Test
        void peekSkipsFailure() {
            AtomicReference<Integer> ref = new AtomicReference<>();
            Result<Integer> result = Result.failure(new RuntimeException("boom"));
            result.peek(ref::set);
            assertThat(ref.get()).isNull();
        }

        @Test
        void peekFailureExecutesOnFailure() {
            AtomicReference<Throwable> ref = new AtomicReference<>();
            RuntimeException ex = new RuntimeException("boom");
            Result<Integer> result = Result.failure(ex);
            Result<Integer> same = result.peekFailure(ref::set);
            assertThat(ref.get()).isSameAs(ex);
            assertThat(same).isSameAs(result);
        }

        @Test
        void peekFailureSkipsSuccess() {
            AtomicReference<Throwable> ref = new AtomicReference<>();
            Result<Integer> result = Result.success(42);
            result.peekFailure(ref::set);
            assertThat(ref.get()).isNull();
        }
    }

    // ==================== Terminal Operations ====================

    @Nested
    class TerminalOperations {

        @Test
        void getOrElseReturnsValueForSuccess() {
            assertThat(Result.success(42).getOrElse(0)).isEqualTo(42);
        }

        @Test
        void getOrElseReturnsDefaultForFailure() {
            assertThat(Result.<Integer>failure(new RuntimeException()).getOrElse(0)).isEqualTo(0);
        }

        @Test
        void getOrElseGetReturnsValueForSuccess() {
            assertThat(Result.success(42).getOrElseGet(() -> 0)).isEqualTo(42);
        }

        @Test
        void getOrElseGetComputesDefaultForFailure() {
            assertThat(Result.<Integer>failure(new RuntimeException()).getOrElseGet(() -> 99)).isEqualTo(99);
        }

        @Test
        void getOrElseThrowReturnsValueForSuccess() throws Exception {
            Integer value = Result.success(42).getOrElseThrow(ex -> new Exception("wrapped"));
            assertThat(value).isEqualTo(42);
        }

        @Test
        void getOrElseThrowThrowsForFailure() {
            Result<Integer> result = Result.failure(new RuntimeException("original"));
            assertThatThrownBy(() -> result.getOrElseThrow(ex -> new NoSuchElementException("wrapped: " + ex.getMessage())))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("wrapped: original");
        }

        @Test
        void toOptionalReturnsPresentForSuccess() {
            assertThat(Result.success(42).toOptional()).contains(42);
        }

        @Test
        void toOptionalReturnsEmptyForFailure() {
            assertThat(Result.<Integer>failure(new RuntimeException()).toOptional()).isEmpty();
        }

        @Test
        void toOptionalReturnsEmptyForNullSuccess() {
            assertThat(Result.success(null).toOptional()).isEmpty();
        }

        @Test
        void streamReturnsSingleElementForSuccess() {
            assertThat(Result.success(42).stream().toList()).containsExactly(42);
        }

        @Test
        void streamReturnsEmptyForFailure() {
            assertThat(Result.<Integer>failure(new RuntimeException()).stream().toList()).isEmpty();
        }

        @Test
        void streamReturnsEmptyForNullSuccess() {
            assertThat(Result.success(null).stream().toList()).isEmpty();
        }
    }

    // ==================== Equality and ToString ====================

    @Nested
    class EqualityAndToString {

        @Test
        void successEquality() {
            Result<Integer> a = Result.success(42);
            Result<Integer> b = Result.success(42);
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        void failureEquality() {
            RuntimeException ex = new RuntimeException("boom");
            Result<Integer> a = Result.failure(ex);
            Result<Integer> b = Result.failure(ex);
            assertThat(a).isEqualTo(b);
        }

        @Test
        void successToString() {
            assertThat(Result.success(42).toString()).isEqualTo("Success[42]");
        }

        @Test
        void failureToStringHidesStackTrace() {
            Result<Integer> result = Result.failure(new RuntimeException("boom"));
            String str = result.toString();
            assertThat(str).isEqualTo("Failure[java.lang.RuntimeException: boom]");
            assertThat(str).doesNotContain("at ");
        }

        @Test
        void failureToStringWithNullMessage() {
            Result<Integer> result = Result.failure(new RuntimeException());
            String str = result.toString();
            assertThat(str).isEqualTo("Failure[java.lang.RuntimeException: null]");
        }
    }

    // ==================== Pattern Matching ====================

    @Nested
    class PatternMatching {

        @Test
        void switchOnSuccess() {
            Result<Integer> result = Result.success(42);
            String output = switch (result) {
                case Result.Success<Integer>(var v) -> "ok:" + v;
                case Result.Failure<Integer>(var e) -> "err:" + e.getMessage();
            };
            assertThat(output).isEqualTo("ok:42");
        }

        @Test
        void switchOnFailure() {
            Result<Integer> result = Result.failure(new RuntimeException("boom"));
            String output = switch (result) {
                case Result.Success<Integer>(var v) -> "ok:" + v;
                case Result.Failure<Integer>(var e) -> "err:" + e.getMessage();
            };
            assertThat(output).isEqualTo("err:boom");
        }
    }

    // ==================== Chaining ====================

    @Nested
    class Chaining {

        @Test
        void fullChainSuccess() {
            String result = Result.of(() -> "  hello  ")
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(s -> s + "!")
                    .getOrElse("default");
            assertThat(result).isEqualTo("HELLO!");
        }

        @Test
        void fullChainWithRecovery() {
            String result = Result.<String>of(() -> {
                        throw new IOException("file missing");
                    })
                    .map(String::trim)
                    .recover(ex -> "recovered")
                    .getOrElse("default");
            assertThat(result).isEqualTo("recovered");
        }
    }
}
