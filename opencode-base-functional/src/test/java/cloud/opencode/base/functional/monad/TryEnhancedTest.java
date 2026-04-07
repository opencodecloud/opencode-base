package cloud.opencode.base.functional.monad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for enhanced Try methods
 * Try 增强方法测试
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Try Enhanced Methods | Try 增强方法测试")
class TryEnhancedTest {

    @Nested
    @DisplayName("andFinally")
    class AndFinallyTests {

        @Test
        @DisplayName("executes action on Success and returns this")
        void successExecutesAction() {
            AtomicBoolean executed = new AtomicBoolean(false);
            Try<Integer> result = Try.success(42).andFinally(() -> executed.set(true));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("executes action on Failure and returns this")
        void failureExecutesAction() {
            AtomicBoolean executed = new AtomicBoolean(false);
            Try<Integer> original = Try.failure(new IOException("fail"));
            Try<Integer> result = original.andFinally(() -> executed.set(true));

            assertThat(result.isFailure()).isTrue();
            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("returns Failure if action throws")
        void actionThrowsReturnsFailure() {
            RuntimeException ex = new RuntimeException("finally failed");
            Try<Integer> result = Try.success(42).andFinally(() -> { throw ex; });

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).hasValue(ex);
        }

        @Test
        @DisplayName("preserves original Failure and adds action exception as suppressed")
        void failureInputActionThrows() {
            IOException originalEx = new IOException("original");
            RuntimeException actionEx = new RuntimeException("action error");
            Try<Integer> result = Try.<Integer>failure(originalEx)
                    .andFinally(() -> { throw actionEx; });

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).hasValue(originalEx);
            assertThat(originalEx.getSuppressed()).containsExactly(actionEx);
        }

        @Test
        @DisplayName("handles self-suppression safely when action throws the same exception")
        void selfSuppressionGuard() {
            RuntimeException sameEx = new RuntimeException("same");
            Try<Integer> result = Try.<Integer>failure(sameEx)
                    .andFinally(() -> { throw sameEx; });

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).hasValue(sameEx);
            assertThat(sameEx.getSuppressed()).isEmpty();
        }
    }

    @Nested
    @DisplayName("mapFailure")
    class MapFailureTests {

        @Test
        @DisplayName("Success returns itself unchanged")
        void successUnchanged() {
            Try<Integer> success = Try.success(42);
            Try<Integer> result = success.mapFailure(t -> new RuntimeException("mapped"));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("Failure transforms exception")
        void failureTransforms() {
            Try<Integer> failure = Try.failure(new IOException("original"));
            Try<Integer> result = failure.mapFailure(t -> new RuntimeException("mapped: " + t.getMessage()));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause().get()).isInstanceOf(RuntimeException.class);
            assertThat(result.getCause().get().getMessage()).isEqualTo("mapped: original");
        }

        @Test
        @DisplayName("Failure returns Failure if mapper throws, preserving original cause as suppressed")
        void mapperThrowsReturnsFailure() {
            IOException originalEx = new IOException("original");
            RuntimeException mapperEx = new RuntimeException("mapper error");
            Try<Integer> result = Try.<Integer>failure(originalEx)
                    .mapFailure(t -> { throw mapperEx; });

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).hasValue(mapperEx);
            assertThat(mapperEx.getSuppressed()).containsExactly(originalEx);
        }
    }

    @Nested
    @DisplayName("fold")
    class FoldTests {

        @Test
        @DisplayName("Success applies success mapper")
        void successFold() {
            String result = Try.success(42).fold(
                    Throwable::getMessage,
                    v -> "value=" + v
            );
            assertThat(result).isEqualTo("value=42");
        }

        @Test
        @DisplayName("Failure applies failure mapper")
        void failureFold() {
            String result = Try.<Integer>failure(new IOException("boom")).fold(
                    t -> "error=" + t.getMessage(),
                    v -> "value=" + v
            );
            assertThat(result).isEqualTo("error=boom");
        }
    }

    @Nested
    @DisplayName("toOption")
    class ToOptionTests {

        @Test
        @DisplayName("Success with non-null value returns Some")
        void successNonNull() {
            Option<Integer> opt = Try.success(42).toOption();
            assertThat(opt.isSome()).isTrue();
            assertThat(opt.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("Success with null value returns None")
        void successNull() {
            Option<String> opt = Try.success((String) null).toOption();
            assertThat(opt.isNone()).isTrue();
        }

        @Test
        @DisplayName("Failure returns None")
        void failureReturnsNone() {
            Option<Integer> opt = Try.<Integer>failure(new IOException("fail")).toOption();
            assertThat(opt.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("toValidation")
    class ToValidationTests {

        @Test
        @DisplayName("Success returns Valid")
        void successReturnsValid() {
            Validation<Throwable, Integer> v = Try.success(42).toValidation();
            assertThat(v.isValid()).isTrue();
            assertThat(v.getValue()).hasValue(42);
        }

        @Test
        @DisplayName("Failure returns Invalid with cause")
        void failureReturnsInvalid() {
            IOException cause = new IOException("fail");
            Validation<Throwable, Integer> v = Try.<Integer>failure(cause).toValidation();
            assertThat(v.isInvalid()).isTrue();
            assertThat(v.getErrors()).containsExactly(cause);
        }
    }

    @Nested
    @DisplayName("stream")
    class StreamTests {

        @Test
        @DisplayName("Success returns single-element stream")
        void successStream() {
            List<Integer> list = Try.success(42).stream().toList();
            assertThat(list).containsExactly(42);
        }

        @Test
        @DisplayName("Failure returns empty stream")
        void failureStream() {
            List<Integer> list = Try.<Integer>failure(new IOException("fail")).stream().toList();
            assertThat(list).isEmpty();
        }
    }

    @Nested
    @DisplayName("contains")
    class ContainsTests {

        @Test
        @DisplayName("Success with matching value returns true")
        void successMatching() {
            assertThat(Try.success(42).contains(42)).isTrue();
        }

        @Test
        @DisplayName("Success with different value returns false")
        void successDifferent() {
            assertThat(Try.success(42).contains(99)).isFalse();
        }

        @Test
        @DisplayName("Success with null value and null argument returns true")
        void successNullContainsNull() {
            assertThat(Try.success((String) null).contains(null)).isTrue();
        }

        @Test
        @DisplayName("Failure returns false")
        void failureReturnsFalse() {
            assertThat(Try.<Integer>failure(new IOException("fail")).contains(42)).isFalse();
        }
    }

    @Nested
    @DisplayName("exists")
    class ExistsTests {

        @Test
        @DisplayName("Success with matching predicate returns true")
        void successMatching() {
            assertThat(Try.success(42).exists(v -> v > 40)).isTrue();
        }

        @Test
        @DisplayName("Success with non-matching predicate returns false")
        void successNonMatching() {
            assertThat(Try.success(42).exists(v -> v > 100)).isFalse();
        }

        @Test
        @DisplayName("Failure returns false")
        void failureReturnsFalse() {
            assertThat(Try.<Integer>failure(new IOException("fail")).exists(v -> true)).isFalse();
        }
    }
}
