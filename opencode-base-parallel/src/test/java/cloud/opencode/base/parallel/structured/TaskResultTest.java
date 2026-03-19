package cloud.opencode.base.parallel.structured;

import org.junit.jupiter.api.*;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * TaskResultTest Tests
 * TaskResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("TaskResult 测试")
class TaskResultTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("success创建成功结果")
        void testSuccess() {
            TaskResult<String> result = TaskResult.success("value");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.isCancelled()).isFalse();
            assertThat(result.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("failure创建失败结果")
        void testFailure() {
            RuntimeException ex = new RuntimeException("error");
            TaskResult<String> result = TaskResult.failure(ex);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.isCancelled()).isFalse();
            assertThat(result.getException()).isEqualTo(ex);
        }

        @Test
        @DisplayName("failure不接受null异常")
        void testFailureNullException() {
            assertThatThrownBy(() -> TaskResult.failure(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("cancelled创建取消结果")
        void testCancelled() {
            TaskResult<String> result = TaskResult.cancelled();

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("of从Callable创建结果")
        void testOf() {
            TaskResult<String> success = TaskResult.of(() -> "value");
            TaskResult<String> failure = TaskResult.of(() -> { throw new RuntimeException("error"); });

            assertThat(success.isSuccess()).isTrue();
            assertThat(success.get()).isEqualTo("value");
            assertThat(failure.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("状态检查测试")
    class StateCheckTests {

        @Test
        @DisplayName("getState返回正确状态")
        void testGetState() {
            assertThat(TaskResult.success("v").getState()).isEqualTo(TaskResult.State.SUCCESS);
            assertThat(TaskResult.failure(new RuntimeException()).getState()).isEqualTo(TaskResult.State.FAILURE);
            assertThat(TaskResult.cancelled().getState()).isEqualTo(TaskResult.State.CANCELLED);
        }
    }

    @Nested
    @DisplayName("值访问测试")
    class ValueAccessTests {

        @Test
        @DisplayName("get返回成功值")
        void testGet() {
            TaskResult<String> result = TaskResult.success("value");

            assertThat(result.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("get非成功状态时抛出异常")
        void testGetNotSuccess() {
            TaskResult<String> failure = TaskResult.failure(new RuntimeException());
            TaskResult<String> cancelled = TaskResult.cancelled();

            assertThatThrownBy(failure::get)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("FAILURE");

            assertThatThrownBy(cancelled::get)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("CANCELLED");
        }

        @Test
        @DisplayName("getOrNull返回值或null")
        void testGetOrNull() {
            assertThat(TaskResult.success("value").getOrNull()).isEqualTo("value");
            assertThat(TaskResult.failure(new RuntimeException()).getOrNull()).isNull();
            assertThat(TaskResult.cancelled().getOrNull()).isNull();
        }

        @Test
        @DisplayName("getOrDefault返回值或默认值")
        void testGetOrDefault() {
            assertThat(TaskResult.success("value").getOrDefault("default")).isEqualTo("value");
            assertThat(TaskResult.failure(new RuntimeException()).getOrDefault("default")).isEqualTo("default");
            assertThat(TaskResult.cancelled().getOrDefault("default")).isEqualTo("default");
        }

        @Test
        @DisplayName("getOrElse失败时从异常计算值")
        void testGetOrElse() {
            TaskResult<String> failure = TaskResult.failure(new RuntimeException("error"));

            String result = failure.getOrElse(ex -> "recovered: " + ex.getMessage());

            assertThat(result).isEqualTo("recovered: error");
        }

        @Test
        @DisplayName("toOptional返回Optional")
        void testToOptional() {
            assertThat(TaskResult.success("value").toOptional()).isEqualTo(Optional.of("value"));
            assertThat(TaskResult.failure(new RuntimeException()).toOptional()).isEmpty();
            assertThat(TaskResult.cancelled().toOptional()).isEmpty();
        }

        @Test
        @DisplayName("getException返回异常")
        void testGetException() {
            RuntimeException ex = new RuntimeException("error");

            assertThat(TaskResult.success("v").getException()).isNull();
            assertThat(TaskResult.failure(ex).getException()).isEqualTo(ex);
            assertThat(TaskResult.cancelled().getException()).isNull();
        }

        @Test
        @DisplayName("getExceptionOptional返回Optional异常")
        void testGetExceptionOptional() {
            RuntimeException ex = new RuntimeException();

            assertThat(TaskResult.success("v").getExceptionOptional()).isEmpty();
            assertThat(TaskResult.failure(ex).getExceptionOptional()).contains(ex);
            assertThat(TaskResult.cancelled().getExceptionOptional()).isEmpty();
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class TransformationTests {

        @Test
        @DisplayName("map转换成功值")
        void testMap() {
            TaskResult<Integer> result = TaskResult.success("hello")
                    .map(String::length);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("map失败时保持失败状态")
        void testMapFailure() {
            RuntimeException ex = new RuntimeException();
            TaskResult<Integer> result = TaskResult.<String>failure(ex)
                    .map(String::length);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getException()).isEqualTo(ex);
        }

        @Test
        @DisplayName("map转换函数抛出异常时转为失败")
        void testMapThrows() {
            TaskResult<Integer> result = TaskResult.success("hello")
                    .map(s -> { throw new RuntimeException("map error"); });

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("flatMap扁平映射成功值")
        void testFlatMap() {
            TaskResult<Integer> result = TaskResult.success("hello")
                    .flatMap(s -> TaskResult.success(s.length()));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("flatMap失败时保持失败状态")
        void testFlatMapFailure() {
            RuntimeException ex = new RuntimeException();
            TaskResult<Integer> result = TaskResult.<String>failure(ex)
                    .flatMap(s -> TaskResult.success(s.length()));

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("recover从失败恢复")
        void testRecover() {
            TaskResult<String> result = TaskResult.<String>failure(new RuntimeException("error"))
                    .recover(ex -> "recovered");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo("recovered");
        }

        @Test
        @DisplayName("recover成功时不变")
        void testRecoverSuccess() {
            TaskResult<String> result = TaskResult.success("original")
                    .recover(ex -> "recovered");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo("original");
        }

        @Test
        @DisplayName("recover恢复函数抛出异常时转为失败")
        void testRecoverThrows() {
            TaskResult<String> result = TaskResult.<String>failure(new RuntimeException())
                    .recover(ex -> { throw new RuntimeException("recover error"); });

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("副作用方法测试")
    class SideEffectTests {

        @Test
        @DisplayName("ifSuccess成功时执行动作")
        void testIfSuccess() {
            AtomicBoolean called = new AtomicBoolean(false);

            TaskResult.success("value").ifSuccess(v -> called.set(true));

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("ifSuccess失败时不执行")
        void testIfSuccessNotCalled() {
            AtomicBoolean called = new AtomicBoolean(false);

            TaskResult.failure(new RuntimeException()).ifSuccess(v -> called.set(true));

            assertThat(called.get()).isFalse();
        }

        @Test
        @DisplayName("ifFailure失败时执行动作")
        void testIfFailure() {
            AtomicBoolean called = new AtomicBoolean(false);

            TaskResult.failure(new RuntimeException()).ifFailure(ex -> called.set(true));

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("ifFailure成功时不执行")
        void testIfFailureNotCalled() {
            AtomicBoolean called = new AtomicBoolean(false);

            TaskResult.success("value").ifFailure(ex -> called.set(true));

            assertThat(called.get()).isFalse();
        }

        @Test
        @DisplayName("ifCancelled取消时执行动作")
        void testIfCancelled() {
            AtomicBoolean called = new AtomicBoolean(false);

            TaskResult.cancelled().ifCancelled(() -> called.set(true));

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("ifCancelled非取消时不执行")
        void testIfCancelledNotCalled() {
            AtomicBoolean called = new AtomicBoolean(false);

            TaskResult.success("value").ifCancelled(() -> called.set(true));

            assertThat(called.get()).isFalse();
        }

        @Test
        @DisplayName("副作用方法返回当前实例支持链式调用")
        void testChaining() {
            AtomicBoolean successCalled = new AtomicBoolean(false);
            AtomicBoolean failureCalled = new AtomicBoolean(false);

            TaskResult<String> result = TaskResult.success("value")
                    .ifSuccess(v -> successCalled.set(true))
                    .ifFailure(ex -> failureCalled.set(true));

            assertThat(result.get()).isEqualTo("value");
            assertThat(successCalled.get()).isTrue();
            assertThat(failureCalled.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Object方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals比较相同成功结果")
        void testEqualsSuccess() {
            TaskResult<String> r1 = TaskResult.success("value");
            TaskResult<String> r2 = TaskResult.success("value");

            assertThat(r1).isEqualTo(r2);
        }

        @Test
        @DisplayName("equals比较不同状态")
        void testEqualsDifferentState() {
            TaskResult<String> success = TaskResult.success("value");
            TaskResult<String> cancelled = TaskResult.cancelled();

            assertThat(success).isNotEqualTo(cancelled);
        }

        @Test
        @DisplayName("hashCode相同结果返回相同值")
        void testHashCode() {
            TaskResult<String> r1 = TaskResult.success("value");
            TaskResult<String> r2 = TaskResult.success("value");

            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("toString返回有意义的字符串")
        void testToString() {
            assertThat(TaskResult.success("value").toString()).contains("Success").contains("value");
            assertThat(TaskResult.failure(new RuntimeException("error")).toString()).contains("Failure").contains("error");
            assertThat(TaskResult.cancelled().toString()).contains("Cancelled");
        }
    }

    @Nested
    @DisplayName("State枚举测试")
    class StateEnumTests {

        @Test
        @DisplayName("枚举包含所有值")
        void testEnumValues() {
            assertThat(TaskResult.State.values()).containsExactly(
                    TaskResult.State.SUCCESS,
                    TaskResult.State.FAILURE,
                    TaskResult.State.CANCELLED
            );
        }
    }
}
