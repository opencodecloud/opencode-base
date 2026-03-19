package cloud.opencode.base.functional.monad;

import cloud.opencode.base.functional.exception.OpenFunctionalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Try 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Try 测试")
class TryTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() 成功时返回 Success")
        void testOfSuccess() {
            Try<Integer> result = Try.of(() -> 42);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("of() 异常时返回 Failure")
        void testOfFailure() {
            Try<Integer> result = Try.of(() -> {
                throw new RuntimeException("error");
            });

            assertThat(result.isFailure()).isTrue();
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("of() 捕获受检异常")
        void testOfCatchesCheckedException() {
            Try<Integer> result = Try.of(() -> {
                throw new IOException("IO error");
            });

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).isPresent();
            assertThat(result.getCause().get()).isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("success() 创建 Success")
        void testSuccess() {
            Try<Integer> result = Try.success(42);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("success(null) 允许 null")
        void testSuccessWithNull() {
            Try<Integer> result = Try.success(null);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isNull();
        }

        @Test
        @DisplayName("failure() 创建 Failure")
        void testFailure() {
            Throwable cause = new RuntimeException("error");
            Try<Integer> result = Try.failure(cause);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).contains(cause);
        }
    }

    @Nested
    @DisplayName("get() 测试")
    class GetTests {

        @Test
        @DisplayName("Success.get() 返回值")
        void testSuccessGet() {
            Try<Integer> result = Try.success(42);

            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("Failure.get() 抛出 OpenFunctionalException")
        void testFailureGet() {
            Try<Integer> result = Try.failure(new RuntimeException("error"));

            assertThatThrownBy(result::get)
                    .isInstanceOf(OpenFunctionalException.class)
                    .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("getCause() 测试")
    class GetCauseTests {

        @Test
        @DisplayName("Success.getCause() 返回空 Optional")
        void testSuccessGetCause() {
            Try<Integer> result = Try.success(42);

            assertThat(result.getCause()).isEmpty();
        }

        @Test
        @DisplayName("Failure.getCause() 返回异常")
        void testFailureGetCause() {
            Throwable cause = new RuntimeException("error");
            Try<Integer> result = Try.failure(cause);

            assertThat(result.getCause()).contains(cause);
        }
    }

    @Nested
    @DisplayName("map() 测试")
    class MapTests {

        @Test
        @DisplayName("Success.map() 转换值")
        void testSuccessMap() {
            Try<Integer> result = Try.success(5)
                    .map(n -> n * 2);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("Success.map() 映射函数异常时返回 Failure")
        void testSuccessMapThrows() {
            Try<Integer> result = Try.success(5)
                    .map(n -> {
                        throw new RuntimeException("error");
                    });

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("Failure.map() 保持 Failure")
        void testFailureMap() {
            Try<Integer> result = Try.<Integer>failure(new RuntimeException("error"))
                    .map(n -> n * 2);

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("flatMap() 测试")
    class FlatMapTests {

        @Test
        @DisplayName("Success.flatMap() 成功转换")
        void testSuccessFlatMap() {
            Try<Integer> result = Try.success(5)
                    .flatMap(n -> Try.success(n * 2));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("Success.flatMap() 转换为 Failure")
        void testSuccessFlatMapToFailure() {
            Try<Integer> result = Try.success(5)
                    .flatMap(n -> Try.failure(new RuntimeException("error")));

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("Success.flatMap() 映射函数异常时返回 Failure")
        void testSuccessFlatMapThrows() {
            Try<Integer> result = Try.success(5)
                    .flatMap(n -> {
                        throw new RuntimeException("error");
                    });

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("Failure.flatMap() 保持 Failure")
        void testFailureFlatMap() {
            Try<Integer> result = Try.<Integer>failure(new RuntimeException("error"))
                    .flatMap(n -> Try.success(n * 2));

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("filter() 测试")
    class FilterTests {

        @Test
        @DisplayName("Success.filter() 条件满足时保留值")
        void testSuccessFilterMatches() {
            Try<Integer> result = Try.success(10)
                    .filter(n -> n > 5);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("Success.filter() 条件不满足时返回 Failure")
        void testSuccessFilterNoMatch() {
            Try<Integer> result = Try.success(3)
                    .filter(n -> n > 5);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause().get()).isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("Failure.filter() 保持 Failure")
        void testFailureFilter() {
            Try<Integer> result = Try.<Integer>failure(new RuntimeException("error"))
                    .filter(n -> n > 5);

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("getOrElse() 测试")
    class GetOrElseTests {

        @Test
        @DisplayName("Success.getOrElse() 返回值")
        void testSuccessGetOrElse() {
            Try<Integer> result = Try.success(42);

            assertThat(result.getOrElse(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("Failure.getOrElse() 返回默认值")
        void testFailureGetOrElse() {
            Try<Integer> result = Try.failure(new RuntimeException("error"));

            assertThat(result.getOrElse(0)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("orElse() 测试")
    class OrElseTests {

        @Test
        @DisplayName("Success.orElse() 返回自身")
        void testSuccessOrElse() {
            Try<Integer> result = Try.success(42);
            Try<Integer> other = Try.success(0);

            assertThat(result.orElse(other)).isSameAs(result);
        }

        @Test
        @DisplayName("Failure.orElse() 返回备选 Try")
        void testFailureOrElse() {
            Try<Integer> result = Try.failure(new RuntimeException("error"));
            Try<Integer> other = Try.success(0);

            assertThat(result.orElse(other)).isEqualTo(other);
        }
    }

    @Nested
    @DisplayName("recover() 测试")
    class RecoverTests {

        @Test
        @DisplayName("Success.recover() 返回自身")
        void testSuccessRecover() {
            Try<Integer> result = Try.success(42)
                    .recover(e -> 0);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("Failure.recover() 恢复为 Success")
        void testFailureRecover() {
            Try<Integer> result = Try.<Integer>failure(new RuntimeException("error"))
                    .recover(e -> 0);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("Failure.recover() 恢复函数异常时返回 Failure")
        void testFailureRecoverThrows() {
            Try<Integer> result = Try.<Integer>failure(new RuntimeException("error"))
                    .recover(e -> {
                        throw new RuntimeException("recovery error");
                    });

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("recoverWith() 测试")
    class RecoverWithTests {

        @Test
        @DisplayName("Success.recoverWith() 返回自身")
        void testSuccessRecoverWith() {
            Try<Integer> result = Try.success(42)
                    .recoverWith(e -> Try.success(0));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("Failure.recoverWith() 恢复为新 Try")
        void testFailureRecoverWith() {
            Try<Integer> result = Try.<Integer>failure(new RuntimeException("error"))
                    .recoverWith(e -> Try.success(0));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("Failure.recoverWith() 恢复函数异常时返回 Failure")
        void testFailureRecoverWithThrows() {
            Try<Integer> result = Try.<Integer>failure(new RuntimeException("error"))
                    .recoverWith(e -> {
                        throw new RuntimeException("recovery error");
                    });

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("toOptional() 测试")
    class ToOptionalTests {

        @Test
        @DisplayName("Success.toOptional() 返回非空 Optional")
        void testSuccessToOptional() {
            Try<Integer> result = Try.success(42);

            Optional<Integer> optional = result.toOptional();

            assertThat(optional).contains(42);
        }

        @Test
        @DisplayName("Success(null).toOptional() 返回空 Optional")
        void testSuccessNullToOptional() {
            Try<Integer> result = Try.success(null);

            Optional<Integer> optional = result.toOptional();

            assertThat(optional).isEmpty();
        }

        @Test
        @DisplayName("Failure.toOptional() 返回空 Optional")
        void testFailureToOptional() {
            Try<Integer> result = Try.failure(new RuntimeException("error"));

            Optional<Integer> optional = result.toOptional();

            assertThat(optional).isEmpty();
        }
    }

    @Nested
    @DisplayName("toEither() 测试")
    class ToEitherTests {

        @Test
        @DisplayName("Success.toEither() 返回 Right")
        void testSuccessToEither() {
            Try<Integer> result = Try.success(42);

            Either<Throwable, Integer> either = result.toEither();

            assertThat(either.isRight()).isTrue();
            assertThat(either.getRight()).contains(42);
        }

        @Test
        @DisplayName("Failure.toEither() 返回 Left")
        void testFailureToEither() {
            RuntimeException cause = new RuntimeException("error");
            Try<Integer> result = Try.failure(cause);

            Either<Throwable, Integer> either = result.toEither();

            assertThat(either.isLeft()).isTrue();
            assertThat(either.getLeft()).contains(cause);
        }
    }

    @Nested
    @DisplayName("peek() 测试")
    class PeekTests {

        @Test
        @DisplayName("Success.peek() 执行操作")
        void testSuccessPeek() {
            AtomicReference<Integer> captured = new AtomicReference<>();
            Try<Integer> result = Try.success(42);

            Try<Integer> peeked = result.peek(captured::set);

            assertThat(peeked).isSameAs(result);
            assertThat(captured.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("Failure.peek() 不执行操作")
        void testFailurePeek() {
            AtomicBoolean called = new AtomicBoolean(false);
            Try<Integer> result = Try.failure(new RuntimeException("error"));

            Try<Integer> peeked = result.peek(n -> called.set(true));

            assertThat(peeked).isSameAs(result);
            assertThat(called.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("onFailure() 测试")
    class OnFailureTests {

        @Test
        @DisplayName("Success.onFailure() 不执行操作")
        void testSuccessOnFailure() {
            AtomicBoolean called = new AtomicBoolean(false);
            Try<Integer> result = Try.success(42);

            Try<Integer> handled = result.onFailure(e -> called.set(true));

            assertThat(handled).isSameAs(result);
            assertThat(called.get()).isFalse();
        }

        @Test
        @DisplayName("Failure.onFailure() 执行操作")
        void testFailureOnFailure() {
            AtomicReference<Throwable> captured = new AtomicReference<>();
            RuntimeException cause = new RuntimeException("error");
            Try<Integer> result = Try.failure(cause);

            Try<Integer> handled = result.onFailure(captured::set);

            assertThat(handled).isSameAs(result);
            assertThat(captured.get()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("onSuccess() 测试")
    class OnSuccessTests {

        @Test
        @DisplayName("Success.onSuccess() 执行操作")
        void testSuccessOnSuccess() {
            AtomicReference<Integer> captured = new AtomicReference<>();
            Try<Integer> result = Try.success(42);

            Try<Integer> handled = result.onSuccess(captured::set);

            assertThat(handled).isSameAs(result);
            assertThat(captured.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("Failure.onSuccess() 不执行操作")
        void testFailureOnSuccess() {
            AtomicBoolean called = new AtomicBoolean(false);
            Try<Integer> result = Try.failure(new RuntimeException("error"));

            Try<Integer> handled = result.onSuccess(n -> called.set(true));

            assertThat(handled).isSameAs(result);
            assertThat(called.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString 测试")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("Success equals 测试")
        void testSuccessEquals() {
            Try<Integer> success1 = Try.success(42);
            Try<Integer> success2 = Try.success(42);
            Try<Integer> success3 = Try.success(0);

            assertThat(success1).isEqualTo(success2);
            assertThat(success1).isNotEqualTo(success3);
        }

        @Test
        @DisplayName("Failure equals 测试")
        void testFailureEquals() {
            Try<Integer> failure1 = Try.failure(new RuntimeException("error"));
            Try<Integer> failure2 = Try.failure(new RuntimeException("error"));
            Try<Integer> failure3 = Try.failure(new RuntimeException("other"));

            assertThat(failure1).isEqualTo(failure2);
            assertThat(failure1).isNotEqualTo(failure3);
        }

        @Test
        @DisplayName("Success 和 Failure 不相等")
        void testSuccessNotEqualsFailure() {
            Try<Integer> success = Try.success(42);
            Try<Integer> failure = Try.failure(new RuntimeException("error"));

            assertThat(success).isNotEqualTo(failure);
        }

        @Test
        @DisplayName("Success hashCode 测试")
        void testSuccessHashCode() {
            Try<Integer> success1 = Try.success(42);
            Try<Integer> success2 = Try.success(42);

            assertThat(success1.hashCode()).isEqualTo(success2.hashCode());
        }

        @Test
        @DisplayName("Success toString 测试")
        void testSuccessToString() {
            Try<Integer> result = Try.success(42);

            assertThat(result.toString()).isEqualTo("Success[42]");
        }

        @Test
        @DisplayName("Failure toString 测试")
        void testFailureToString() {
            Try<Integer> result = Try.failure(new RuntimeException("error"));

            assertThat(result.toString()).startsWith("Failure[");
        }
    }

    @Nested
    @DisplayName("Success.value() 测试")
    class SuccessValueTests {

        @Test
        @DisplayName("Success.value() 返回值")
        void testSuccessValue() {
            Try.Success<Integer> success = new Try.Success<>(42);

            assertThat(success.value()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("Failure.cause() 测试")
    class FailureCauseTests {

        @Test
        @DisplayName("Failure.cause() 返回异常")
        void testFailureCause() {
            RuntimeException cause = new RuntimeException("error");
            Try.Failure<Integer> failure = new Try.Failure<>(cause);

            assertThat(failure.cause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("链式操作测试")
    class ChainedOperationsTests {

        @Test
        @DisplayName("链式 map 操作")
        void testChainedMap() {
            Try<Integer> result = Try.success(5)
                    .map(n -> n * 2)
                    .map(n -> n + 1);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(11);
        }

        @Test
        @DisplayName("链式 flatMap 操作")
        void testChainedFlatMap() {
            Try<Integer> result = Try.success(5)
                    .flatMap(n -> Try.success(n * 2))
                    .flatMap(n -> Try.success(n + 1));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(11);
        }

        @Test
        @DisplayName("复杂链式操作")
        void testComplexChain() {
            int result = Try.of(() -> "5")
                    .map(Integer::parseInt)
                    .map(n -> n * 2)
                    .filter(n -> n > 5)
                    .recover(e -> 0)
                    .get();

            assertThat(result).isEqualTo(10);
        }
    }
}
