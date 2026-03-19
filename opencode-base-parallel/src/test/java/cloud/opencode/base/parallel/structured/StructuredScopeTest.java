package cloud.opencode.base.parallel.structured;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.*;

/**
 * StructuredScopeTest Tests
 * StructuredScopeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("StructuredScope 测试")
class StructuredScopeTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("shutdownOnFailure创建失败关闭策略的作用域")
        void testShutdownOnFailure() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                assertThat(scope.getPolicy()).isEqualTo(StructuredScope.Policy.SHUTDOWN_ON_FAILURE);
                // Must join before close even if no tasks forked
                scope.joinAll();
            }
        }

        @Test
        @DisplayName("shutdownOnSuccess创建成功关闭策略的作用域")
        void testShutdownOnSuccess() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnSuccess()) {
                assertThat(scope.getPolicy()).isEqualTo(StructuredScope.Policy.SHUTDOWN_ON_SUCCESS);
                scope.joinAll();
            }
        }
    }

    @Nested
    @DisplayName("fork方法测试")
    class ForkTests {

        @Test
        @DisplayName("fork添加单个任务并joinAll")
        void testFork() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                scope.fork(() -> "task1");

                assertThat(scope.getTaskCount()).isEqualTo(1);
                List<String> results = scope.joinAll();
                assertThat(results).containsExactly("task1");
            }
        }

        @Test
        @DisplayName("fork返回当前作用域以支持链式调用")
        void testForkChaining() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                StructuredScope<String> result = scope.fork(() -> "task1");

                assertThat(result).isSameAs(scope);
                scope.joinAll();
            }
        }

        @Test
        @DisplayName("forkAll(varargs)添加多个任务")
        void testForkAllVarargs() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                scope.forkAll(
                        () -> "task1",
                        () -> "task2",
                        () -> "task3"
                );

                assertThat(scope.getTaskCount()).isEqualTo(3);
                List<String> results = scope.joinAll();
                assertThat(results).containsExactly("task1", "task2", "task3");
            }
        }

        @Test
        @DisplayName("forkAll(Iterable)添加多个任务")
        void testForkAllIterable() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                List<Callable<String>> tasks = List.of(
                        () -> "task1",
                        () -> "task2"
                );
                scope.forkAll(tasks);

                assertThat(scope.getTaskCount()).isEqualTo(2);
                List<String> results = scope.joinAll();
                assertThat(results).containsExactly("task1", "task2");
            }
        }
    }

    @Nested
    @DisplayName("joinAll方法测试")
    class JoinAllTests {

        @Test
        @DisplayName("joinAll等待并返回所有结果")
        void testJoinAll() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                scope.fork(() -> "a")
                     .fork(() -> "b")
                     .fork(() -> "c");

                List<String> results = scope.joinAll();

                assertThat(results).containsExactly("a", "b", "c");
            }
        }

        @Test
        @DisplayName("joinAll带超时")
        void testJoinAllWithTimeout() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                scope.fork(() -> "fast");

                List<String> results = scope.joinAll(Duration.ofSeconds(5));

                assertThat(results).containsExactly("fast");
            }
        }

        @Test
        @DisplayName("任务失败时抛出异常")
        void testJoinAllWithFailure() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                scope.fork(() -> "success")
                     .fork(() -> { throw new RuntimeException("failure"); });

                assertThatThrownBy(scope::joinAll)
                        .isInstanceOf(OpenParallelException.class);
            }
        }

        @Test
        @DisplayName("空任务列表joinAll返回空列表")
        void testJoinAllEmpty() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                List<String> results = scope.joinAll();

                assertThat(results).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("joinAny方法测试")
    class JoinAnyTests {

        @Test
        @DisplayName("joinAny返回首个成功结果")
        void testJoinAny() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnSuccess()) {
                scope.fork(() -> {
                    Thread.sleep(100);
                    return "slow";
                }).fork(() -> "fast");

                String result = scope.joinAny();

                assertThat(result).isIn("fast", "slow");
            }
        }

        @Test
        @DisplayName("joinAny非SHUTDOWN_ON_SUCCESS策略时抛出异常")
        void testJoinAnyWrongPolicy() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                scope.fork(() -> "task");

                assertThatThrownBy(scope::joinAny)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("SHUTDOWN_ON_SUCCESS");

                // Still need to join to clean up
                try { scope.joinAll(); } catch (Exception ignored) {}
            }
        }
    }

    @Nested
    @DisplayName("joinAndReduce方法测试")
    class JoinAndReduceTests {

        @Test
        @DisplayName("joinAndReduce归约所有结果")
        void testJoinAndReduce() {
            try (StructuredScope<Integer> scope = StructuredScope.shutdownOnFailure()) {
                scope.fork(() -> 1)
                     .fork(() -> 2)
                     .fork(() -> 3);

                Integer sum = scope.joinAndReduce(0, Integer::sum);

                assertThat(sum).isEqualTo(6);
            }
        }

        @Test
        @DisplayName("joinAndReduce空任务列表返回identity")
        void testJoinAndReduceEmpty() {
            try (StructuredScope<Integer> scope = StructuredScope.shutdownOnFailure()) {
                Integer result = scope.joinAndReduce(0, Integer::sum);

                assertThat(result).isZero();
            }
        }
    }

    @Nested
    @DisplayName("joinAsResults方法测试")
    class JoinAsResultsTests {

        @Test
        @DisplayName("joinAsResults返回TaskResult列表")
        void testJoinAsResults() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                scope.fork(() -> "success");

                List<TaskResult<String>> results = scope.joinAsResults();

                assertThat(results).hasSize(1);
                assertThat(results.getFirst().isSuccess()).isTrue();
                assertThat(results.getFirst().get()).isEqualTo("success");
            }
        }
    }

    @Nested
    @DisplayName("Policy枚举测试")
    class PolicyEnumTests {

        @Test
        @DisplayName("SHUTDOWN_ON_FAILURE值存在")
        void testShutdownOnFailureValue() {
            assertThat(StructuredScope.Policy.SHUTDOWN_ON_FAILURE).isNotNull();
        }

        @Test
        @DisplayName("SHUTDOWN_ON_SUCCESS值存在")
        void testShutdownOnSuccessValue() {
            assertThat(StructuredScope.Policy.SHUTDOWN_ON_SUCCESS).isNotNull();
        }

        @Test
        @DisplayName("枚举包含所有值")
        void testEnumValues() {
            assertThat(StructuredScope.Policy.values()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("生命周期测试")
    class LifecycleTests {

        @Test
        @DisplayName("close关闭作用域")
        void testClose() {
            StructuredScope<String> scope = StructuredScope.shutdownOnFailure();
            scope.fork(() -> "task");
            scope.joinAll();

            scope.close();
            // Should not throw
        }

        @Test
        @DisplayName("getTaskCount返回任务数")
        void testGetTaskCount() {
            try (StructuredScope<String> scope = StructuredScope.shutdownOnFailure()) {
                assertThat(scope.getTaskCount()).isZero();

                scope.fork(() -> "a");
                assertThat(scope.getTaskCount()).isEqualTo(1);

                scope.fork(() -> "b");
                assertThat(scope.getTaskCount()).isEqualTo(2);

                scope.joinAll();
            }
        }

        @Test
        @DisplayName("getPolicy返回策略")
        void testGetPolicy() {
            try (StructuredScope<String> failure = StructuredScope.shutdownOnFailure();
                 StructuredScope<String> success = StructuredScope.shutdownOnSuccess()) {

                assertThat(failure.getPolicy()).isEqualTo(StructuredScope.Policy.SHUTDOWN_ON_FAILURE);
                assertThat(success.getPolicy()).isEqualTo(StructuredScope.Policy.SHUTDOWN_ON_SUCCESS);

                failure.joinAll();
                success.joinAll();
            }
        }
    }
}
