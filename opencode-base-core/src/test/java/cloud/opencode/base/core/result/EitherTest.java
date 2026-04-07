package cloud.opencode.base.core.result;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class EitherTest {

    // ==================== Factory Methods ====================

    @Nested
    class FactoryMethods {

        @Test
        void leftCreatesLeftInstance() {
            Either<String, Integer> either = Either.left("error");
            assertThat(either).isInstanceOf(Either.Left.class);
            assertThat(either.isLeft()).isTrue();
            assertThat(either.isRight()).isFalse();
        }

        @Test
        void rightCreatesRightInstance() {
            Either<String, Integer> either = Either.right(42);
            assertThat(either).isInstanceOf(Either.Right.class);
            assertThat(either.isRight()).isTrue();
            assertThat(either.isLeft()).isFalse();
        }

        @Test
        void leftAllowsNullValue() {
            Either<String, Integer> either = Either.left(null);
            assertThat(either.isLeft()).isTrue();
            assertThat(either.getLeft()).isEmpty(); // Optional.ofNullable(null) = empty
        }

        @Test
        void rightAllowsNullValue() {
            Either<String, Integer> either = Either.right(null);
            assertThat(either.isRight()).isTrue();
            assertThat(either.getRight()).isEmpty();
        }
    }

    // ==================== Value Access ====================

    @Nested
    class ValueAccess {

        @Test
        void getLeftReturnsValueForLeft() {
            Either<String, Integer> either = Either.left("error");
            assertThat(either.getLeft()).contains("error");
        }

        @Test
        void getLeftReturnsEmptyForRight() {
            Either<String, Integer> either = Either.right(42);
            assertThat(either.getLeft()).isEmpty();
        }

        @Test
        void getRightReturnsValueForRight() {
            Either<String, Integer> either = Either.right(42);
            assertThat(either.getRight()).contains(42);
        }

        @Test
        void getRightReturnsEmptyForLeft() {
            Either<String, Integer> either = Either.left("error");
            assertThat(either.getRight()).isEmpty();
        }

        @Test
        void leftRecordAccessor() {
            var left = new Either.Left<String, Integer>("error");
            assertThat(left.value()).isEqualTo("error");
        }

        @Test
        void rightRecordAccessor() {
            var right = new Either.Right<String, Integer>(42);
            assertThat(right.value()).isEqualTo(42);
        }
    }

    // ==================== Map ====================

    @Nested
    class Map {

        @Test
        void mapTransformsRightValue() {
            Either<String, Integer> either = Either.right(5);
            Either<String, String> result = either.map(i -> "val:" + i);
            assertThat(result.getRight()).contains("val:5");
        }

        @Test
        void mapSkipsLeftValue() {
            Either<String, Integer> either = Either.left("error");
            Either<String, String> result = either.map(i -> "val:" + i);
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).contains("error");
        }
    }

    // ==================== FlatMap ====================

    @Nested
    class FlatMap {

        @Test
        void flatMapTransformsRightToRight() {
            Either<String, Integer> either = Either.right(5);
            Either<String, String> result = either.flatMap(i -> Either.right("val:" + i));
            assertThat(result.getRight()).contains("val:5");
        }

        @Test
        void flatMapTransformsRightToLeft() {
            Either<String, Integer> either = Either.right(5);
            Either<String, String> result = either.flatMap(i -> Either.left("failed"));
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).contains("failed");
        }

        @Test
        void flatMapSkipsLeft() {
            Either<String, Integer> either = Either.left("error");
            Either<String, String> result = either.flatMap(i -> Either.right("val:" + i));
            assertThat(result.isLeft()).isTrue();
        }
    }

    // ==================== MapLeft ====================

    @Nested
    class MapLeft {

        @Test
        void mapLeftTransformsLeftValue() {
            Either<String, Integer> either = Either.left("error");
            Either<Integer, Integer> result = either.mapLeft(String::length);
            assertThat(result.getLeft()).contains(5);
        }

        @Test
        void mapLeftSkipsRightValue() {
            Either<String, Integer> either = Either.right(42);
            Either<Integer, Integer> result = either.mapLeft(String::length);
            assertThat(result.isRight()).isTrue();
            assertThat(result.getRight()).contains(42);
        }
    }

    // ==================== Bimap ====================

    @Nested
    class Bimap {

        @Test
        void bimapTransformsLeft() {
            Either<String, Integer> either = Either.left("err");
            Either<Integer, String> result = either.bimap(String::length, Object::toString);
            assertThat(result.getLeft()).contains(3);
        }

        @Test
        void bimapTransformsRight() {
            Either<String, Integer> either = Either.right(42);
            Either<Integer, String> result = either.bimap(String::length, Object::toString);
            assertThat(result.getRight()).contains("42");
        }
    }

    // ==================== GetOrElse / OrElse ====================

    @Nested
    class Recovery {

        @Test
        void getOrElseReturnsRightValue() {
            Either<String, Integer> either = Either.right(42);
            assertThat(either.getOrElse(0)).isEqualTo(42);
        }

        @Test
        void getOrElseReturnsDefaultForLeft() {
            Either<String, Integer> either = Either.left("error");
            assertThat(either.getOrElse(0)).isEqualTo(0);
        }

        @Test
        void orElseReturnsThisForRight() {
            Either<String, Integer> right = Either.right(42);
            Either<String, Integer> other = Either.right(99);
            assertThat(right.orElse(other)).isSameAs(right);
        }

        @Test
        void orElseReturnsOtherForLeft() {
            Either<String, Integer> left = Either.left("error");
            Either<String, Integer> other = Either.right(99);
            assertThat(left.orElse(other)).isSameAs(other);
        }
    }

    // ==================== Fold ====================

    @Nested
    class Fold {

        @Test
        void foldAppliesLeftMapper() {
            Either<String, Integer> either = Either.left("error");
            String result = either.fold(l -> "L:" + l, r -> "R:" + r);
            assertThat(result).isEqualTo("L:error");
        }

        @Test
        void foldAppliesRightMapper() {
            Either<String, Integer> either = Either.right(42);
            String result = either.fold(l -> "L:" + l, r -> "R:" + r);
            assertThat(result).isEqualTo("R:42");
        }
    }

    // ==================== Swap ====================

    @Nested
    class Swap {

        @Test
        void swapLeftBecomesRight() {
            Either<String, Integer> either = Either.left("error");
            Either<Integer, String> swapped = either.swap();
            assertThat(swapped.isRight()).isTrue();
            assertThat(swapped.getRight()).contains("error");
        }

        @Test
        void swapRightBecomesLeft() {
            Either<String, Integer> either = Either.right(42);
            Either<Integer, String> swapped = either.swap();
            assertThat(swapped.isLeft()).isTrue();
            assertThat(swapped.getLeft()).contains(42);
        }
    }

    // ==================== Peek ====================

    @Nested
    class Peek {

        @Test
        void peekExecutesOnRight() {
            AtomicReference<Integer> ref = new AtomicReference<>();
            Either<String, Integer> either = Either.right(42);
            Either<String, Integer> result = either.peek(ref::set);
            assertThat(ref.get()).isEqualTo(42);
            assertThat(result).isSameAs(either);
        }

        @Test
        void peekSkipsLeft() {
            AtomicReference<Integer> ref = new AtomicReference<>();
            Either<String, Integer> either = Either.left("error");
            either.peek(ref::set);
            assertThat(ref.get()).isNull();
        }

        @Test
        void peekLeftExecutesOnLeft() {
            AtomicReference<String> ref = new AtomicReference<>();
            Either<String, Integer> either = Either.left("error");
            Either<String, Integer> result = either.peekLeft(ref::set);
            assertThat(ref.get()).isEqualTo("error");
            assertThat(result).isSameAs(either);
        }

        @Test
        void peekLeftSkipsRight() {
            AtomicReference<String> ref = new AtomicReference<>();
            Either<String, Integer> either = Either.right(42);
            either.peekLeft(ref::set);
            assertThat(ref.get()).isNull();
        }
    }

    // ==================== toOptional ====================

    @Nested
    class ToOptional {

        @Test
        void rightToOptionalReturnsPresent() {
            Either<String, Integer> either = Either.right(42);
            Optional<Integer> opt = either.toOptional();
            assertThat(opt).contains(42);
        }

        @Test
        void leftToOptionalReturnsEmpty() {
            Either<String, Integer> either = Either.left("error");
            Optional<Integer> opt = either.toOptional();
            assertThat(opt).isEmpty();
        }

        @Test
        void rightNullToOptionalReturnsEmpty() {
            Either<String, Integer> either = Either.right(null);
            assertThat(either.toOptional()).isEmpty();
        }
    }

    // ==================== stream ====================

    @Nested
    class StreamTest {

        @Test
        void rightStreamReturnsSingleElement() {
            Either<String, Integer> either = Either.right(42);
            Stream<Integer> stream = either.stream();
            assertThat(stream.toList()).containsExactly(42);
        }

        @Test
        void leftStreamReturnsEmpty() {
            Either<String, Integer> either = Either.left("error");
            Stream<Integer> stream = either.stream();
            assertThat(stream.toList()).isEmpty();
        }

        @Test
        void rightNullStreamReturnsEmpty() {
            Either<String, Integer> either = Either.right(null);
            assertThat(either.stream().toList()).isEmpty();
        }
    }

    // ==================== toResult ====================

    @Nested
    class ToResult {

        @Test
        void rightToResultReturnsSuccess() {
            Either<String, Integer> either = Either.right(42);
            Result<Integer> result = either.toResult();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrElse(0)).isEqualTo(42);
        }

        @Test
        void leftThrowableToResultReturnsFailure() {
            RuntimeException ex = new RuntimeException("boom");
            Either<Throwable, Integer> either = Either.left(ex);
            Result<Integer> result = either.toResult();
            assertThat(result.isFailure()).isTrue();
            assertThat(((Result.Failure<Integer>) result).cause()).isSameAs(ex);
        }

        @Test
        void leftNonThrowableToResultWrapsInOpenException() {
            Either<String, Integer> either = Either.left("error msg");
            Result<Integer> result = either.toResult();
            assertThat(result.isFailure()).isTrue();
            Result.Failure<Integer> failure = (Result.Failure<Integer>) result;
            assertThat(failure.cause()).isInstanceOf(OpenException.class);
            assertThat(failure.cause().getMessage()).contains("error msg");
        }

        @Test
        void toResultWithMapper() {
            Either<String, Integer> either = Either.left("bad input");
            Result<Integer> result = either.toResult(msg -> new IllegalArgumentException(msg));
            assertThat(result.isFailure()).isTrue();
            Result.Failure<Integer> failure = (Result.Failure<Integer>) result;
            assertThat(failure.cause()).isInstanceOf(IllegalArgumentException.class);
            assertThat(failure.cause().getMessage()).isEqualTo("bad input");
        }

        @Test
        void toResultWithMapperOnRightReturnsSuccess() {
            Either<String, Integer> either = Either.right(42);
            Result<Integer> result = either.toResult(msg -> new IllegalArgumentException(msg));
            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== Equality and ToString ====================

    @Nested
    class EqualityAndToString {

        @Test
        void leftEquality() {
            Either<String, Integer> a = Either.left("error");
            Either<String, Integer> b = Either.left("error");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        void rightEquality() {
            Either<String, Integer> a = Either.right(42);
            Either<String, Integer> b = Either.right(42);
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        void leftNotEqualToRight() {
            Either<Integer, Integer> left = Either.left(42);
            Either<Integer, Integer> right = Either.right(42);
            assertThat(left).isNotEqualTo(right);
        }

        @Test
        void leftToString() {
            assertThat(Either.left("error").toString()).isEqualTo("Left[error]");
        }

        @Test
        void rightToString() {
            assertThat(Either.right(42).toString()).isEqualTo("Right[42]");
        }
    }

    // ==================== Pattern Matching ====================

    @Nested
    class PatternMatching {

        @Test
        void switchOnLeft() {
            Either<String, Integer> either = Either.left("error");
            String result = switch (either) {
                case Either.Left<String, Integer>(var v) -> "left:" + v;
                case Either.Right<String, Integer>(var v) -> "right:" + v;
            };
            assertThat(result).isEqualTo("left:error");
        }

        @Test
        void switchOnRight() {
            Either<String, Integer> either = Either.right(42);
            String result = switch (either) {
                case Either.Left<String, Integer>(var v) -> "left:" + v;
                case Either.Right<String, Integer>(var v) -> "right:" + v;
            };
            assertThat(result).isEqualTo("right:42");
        }
    }
}
