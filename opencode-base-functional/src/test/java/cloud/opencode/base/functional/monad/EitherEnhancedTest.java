package cloud.opencode.base.functional.monad;

import cloud.opencode.base.functional.exception.OpenFunctionalException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class EitherEnhancedTest {

    @Nested
    class FilterOrElseTest {

        @Test
        void rightMatchingPredicateReturnsThis() {
            Either<String, Integer> either = Either.right(42);
            Either<String, Integer> result = either.filterOrElse(v -> v > 0, () -> "negative");
            assertThat(result.isRight()).isTrue();
            assertThat(result.getRight()).hasValue(42);
        }

        @Test
        void rightFailingPredicateReturnsLeft() {
            Either<String, Integer> either = Either.right(-1);
            Either<String, Integer> result = either.filterOrElse(v -> v > 0, () -> "negative");
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).hasValue("negative");
        }

        @Test
        void leftReturnsThisRegardlessOfPredicate() {
            Either<String, Integer> either = Either.left("error");
            Either<String, Integer> result = either.filterOrElse(v -> v > 0, () -> "other");
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).hasValue("error");
        }

        @Test
        void nullPredicateThrowsNpe() {
            Either<String, Integer> either = Either.right(1);
            assertThatNullPointerException()
                    .isThrownBy(() -> either.filterOrElse(null, () -> "err"));
        }

        @Test
        void nullOrElseThrowsNpe() {
            Either<String, Integer> either = Either.right(1);
            assertThatNullPointerException()
                    .isThrownBy(() -> either.filterOrElse(v -> true, null));
        }
    }

    @Nested
    class ToOptionTest {

        @Test
        void rightReturnsSome() {
            Either<String, Integer> either = Either.right(42);
            Option<Integer> option = either.toOption();
            assertThat(option).isInstanceOf(Option.Some.class);
            assertThat(option.getOrElse(0)).isEqualTo(42);
        }

        @Test
        void leftReturnsNone() {
            Either<String, Integer> either = Either.left("error");
            Option<Integer> option = either.toOption();
            assertThat(option).isInstanceOf(Option.None.class);
        }

        @Test
        void rightWithNullReturnsSomeNull() {
            Either<String, Integer> either = Either.right(null);
            Option<Integer> option = either.toOption();
            // Option.of(null) returns None
            assertThat(option).isInstanceOf(Option.None.class);
        }
    }

    @Nested
    class ToTryTest {

        @Test
        void rightReturnsSuccess() {
            Either<String, Integer> either = Either.right(42);
            Try<Integer> result = either.toTry();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrElse(0)).isEqualTo(42);
        }

        @Test
        void leftWithThrowableReturnsFailureWithSameThrowable() {
            RuntimeException ex = new RuntimeException("boom");
            Either<Throwable, Integer> either = Either.left(ex);
            Try<Integer> result = either.toTry();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.fold(Throwable::getMessage, Object::toString)).isEqualTo("boom");
        }

        @Test
        void leftWithNonThrowableReturnsFailureWithOpenFunctionalException() {
            Either<String, Integer> either = Either.left("error");
            Try<Integer> result = either.toTry();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause().get())
                    .isInstanceOf(OpenFunctionalException.class)
                    .hasMessageContaining("Either.Left: error");
        }

        @Test
        void leftOverrideWithThrowable() {
            IllegalArgumentException ex = new IllegalArgumentException("bad arg");
            Either<Throwable, String> either = Either.left(ex);
            Try<String> result = either.toTry();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause().get()).isSameAs(ex);
        }

        @Test
        void rightOverrideReturnsSuccess() {
            Either<String, String> either = Either.right("ok");
            Try<String> result = either.toTry();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrElse("fail")).isEqualTo("ok");
        }
    }

    @Nested
    class ToValidationTest {

        @Test
        void rightReturnsValid() {
            Either<String, Integer> either = Either.right(42);
            Validation<String, Integer> validation = either.toValidation();
            assertThat(validation.isValid()).isTrue();
        }

        @Test
        void leftReturnsInvalid() {
            Either<String, Integer> either = Either.left("error");
            Validation<String, Integer> validation = either.toValidation();
            assertThat(validation.isValid()).isFalse();
        }
    }

    @Nested
    class StreamTest {

        @Test
        void rightReturnsSingleElementStream() {
            Either<String, Integer> either = Either.right(42);
            Stream<Integer> stream = either.stream();
            assertThat(stream.toList()).containsExactly(42);
        }

        @Test
        void leftReturnsEmptyStream() {
            Either<String, Integer> either = Either.left("error");
            Stream<Integer> stream = either.stream();
            assertThat(stream.toList()).isEmpty();
        }
    }

    @Nested
    class ContainsTest {

        @Test
        void rightContainsMatchingValue() {
            Either<String, Integer> either = Either.right(42);
            assertThat(either.contains(42)).isTrue();
        }

        @Test
        void rightDoesNotContainDifferentValue() {
            Either<String, Integer> either = Either.right(42);
            assertThat(either.contains(99)).isFalse();
        }

        @Test
        void leftDoesNotContainAnyValue() {
            Either<String, Integer> either = Either.left("error");
            assertThat(either.contains(42)).isFalse();
        }

        @Test
        void rightContainsNull() {
            Either<String, Integer> either = Either.right(null);
            assertThat(either.contains(null)).isTrue();
        }
    }

    @Nested
    class ExistsTest {

        @Test
        void rightMatchingPredicateReturnsTrue() {
            Either<String, Integer> either = Either.right(42);
            assertThat(either.exists(v -> v > 0)).isTrue();
        }

        @Test
        void rightFailingPredicateReturnsFalse() {
            Either<String, Integer> either = Either.right(-1);
            assertThat(either.exists(v -> v > 0)).isFalse();
        }

        @Test
        void leftReturnsFalse() {
            Either<String, Integer> either = Either.left("error");
            assertThat(either.exists(v -> true)).isFalse();
        }

        @Test
        void nullPredicateThrowsNpe() {
            Either<String, Integer> either = Either.right(1);
            assertThatNullPointerException()
                    .isThrownBy(() -> either.exists(null));
        }
    }

    @Nested
    class ForAllTest {

        @Test
        void rightMatchingPredicateReturnsTrue() {
            Either<String, Integer> either = Either.right(42);
            assertThat(either.forAll(v -> v > 0)).isTrue();
        }

        @Test
        void rightFailingPredicateReturnsFalse() {
            Either<String, Integer> either = Either.right(-1);
            assertThat(either.forAll(v -> v > 0)).isFalse();
        }

        @Test
        void leftReturnsTrue() {
            Either<String, Integer> either = Either.left("error");
            assertThat(either.forAll(v -> false)).isTrue();
        }

        @Test
        void nullPredicateThrowsNpeForRight() {
            Either<String, Integer> either = Either.right(1);
            assertThatNullPointerException()
                    .isThrownBy(() -> either.forAll(null));
        }
    }
}
