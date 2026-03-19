package cloud.opencode.base.core.thread;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * StructuredTaskUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("StructuredTaskUtil 测试")
class StructuredTaskUtilTest {

    @Nested
    @DisplayName("invokeAll 测试")
    class InvokeAllTests {

        @Test
        @DisplayName("invokeAll 全部成功")
        void testInvokeAllSuccess() throws Exception {
            List<String> results = StructuredTaskUtil.invokeAll(List.of(
                    () -> "result1",
                    () -> "result2",
                    () -> "result3"
            ));

            assertThat(results).containsExactly("result1", "result2", "result3");
        }

        @Test
        @DisplayName("invokeAll 空列表")
        void testInvokeAllEmpty() throws Exception {
            List<String> results = StructuredTaskUtil.invokeAll(List.of());
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("invokeAll 单任务")
        void testInvokeAllSingle() throws Exception {
            List<Integer> results = StructuredTaskUtil.invokeAll(List.of(
                    () -> 42
            ));

            assertThat(results).containsExactly(42);
        }

        @Test
        @DisplayName("invokeAll 任务失败抛出异常")
        void testInvokeAllFailure() {
            assertThatThrownBy(() -> {
                StructuredTaskUtil.invokeAll(List.of(
                        () -> "success",
                        () -> { throw new IllegalStateException("Task failed"); },
                        () -> "success2"
                ));
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("invokeAll 并行执行")
        void testInvokeAllParallel() throws Exception {
            AtomicInteger counter = new AtomicInteger(0);

            List<Integer> results = StructuredTaskUtil.invokeAll(List.of(
                    () -> {
                        Thread.sleep(50);
                        return counter.incrementAndGet();
                    },
                    () -> {
                        Thread.sleep(50);
                        return counter.incrementAndGet();
                    },
                    () -> {
                        Thread.sleep(50);
                        return counter.incrementAndGet();
                    }
            ));

            assertThat(results).hasSize(3);
            assertThat(counter.get()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("invokeAll 带超时测试")
    class InvokeAllWithTimeoutTests {

        @Test
        @DisplayName("invokeAll 超时成功完成")
        void testInvokeAllWithTimeoutSuccess() throws Exception {
            List<String> results = StructuredTaskUtil.invokeAll(
                    List.of(
                            () -> "result1",
                            () -> "result2"
                    ),
                    Duration.ofSeconds(5)
            );

            assertThat(results).containsExactly("result1", "result2");
        }

        @Test
        @DisplayName("invokeAll 超时")
        void testInvokeAllTimeout() {
            assertThatThrownBy(() -> {
                StructuredTaskUtil.invokeAll(
                        List.of(
                                () -> {
                                    Thread.sleep(5000);
                                    return "slow";
                                }
                        ),
                        Duration.ofMillis(100)
                );
            }).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("invokeAny 测试")
    class InvokeAnyTests {

        @Test
        @DisplayName("invokeAny 第一个成功")
        void testInvokeAnyFirstSuccess() throws Exception {
            String result = StructuredTaskUtil.invokeAny(List.of(
                    () -> "fast",
                    () -> {
                        Thread.sleep(1000);
                        return "slow";
                    }
            ));

            assertThat(result).isEqualTo("fast");
        }

        @Test
        @DisplayName("invokeAny 单任务成功")
        void testInvokeAnySingle() throws Exception {
            String result = StructuredTaskUtil.invokeAny(List.of(
                    () -> "only"
            ));

            assertThat(result).isEqualTo("only");
        }

        @Test
        @DisplayName("invokeAny 某些失败但有成功")
        void testInvokeAnySomeFailure() throws Exception {
            String result = StructuredTaskUtil.invokeAny(List.of(
                    () -> { throw new RuntimeException("fail1"); },
                    () -> "success",
                    () -> { throw new RuntimeException("fail2"); }
            ));

            assertThat(result).isEqualTo("success");
        }
    }

    @Nested
    @DisplayName("invokeAny 带超时测试")
    class InvokeAnyWithTimeoutTests {

        @Test
        @DisplayName("invokeAny 超时成功完成")
        void testInvokeAnyWithTimeoutSuccess() throws Exception {
            String result = StructuredTaskUtil.invokeAny(
                    List.of(
                            () -> "fast"
                    ),
                    Duration.ofSeconds(5)
            );

            assertThat(result).isEqualTo("fast");
        }
    }

    @Nested
    @DisplayName("parallel 双任务测试")
    class ParallelTwoTasksTests {

        @Test
        @DisplayName("parallel 两个任务成功")
        void testParallelSuccess() throws Exception {
            String result = StructuredTaskUtil.parallel(
                    () -> "hello",
                    () -> 42,
                    (s, i) -> s + "-" + i
            );

            assertThat(result).isEqualTo("hello-42");
        }

        @Test
        @DisplayName("parallel 任务失败")
        void testParallelFailure() {
            assertThatThrownBy(() -> {
                StructuredTaskUtil.parallel(
                        () -> "success",
                        () -> { throw new IllegalStateException("fail"); },
                        (a, b) -> a + "-" + b
                );
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("parallel 并行执行")
        void testParallelActuallyParallel() throws Exception {
            long start = System.currentTimeMillis();

            String result = StructuredTaskUtil.parallel(
                    () -> {
                        Thread.sleep(100);
                        return "A";
                    },
                    () -> {
                        Thread.sleep(100);
                        return "B";
                    },
                    (a, b) -> a + b
            );

            long duration = System.currentTimeMillis() - start;

            assertThat(result).isEqualTo("AB");
            assertThat(duration).isLessThan(200);
        }
    }

    @Nested
    @DisplayName("parallel 双任务带超时测试")
    class ParallelTwoTasksWithTimeoutTests {

        @Test
        @DisplayName("parallel 带超时成功")
        void testParallelWithTimeoutSuccess() throws Exception {
            String result = StructuredTaskUtil.parallel(
                    () -> "a",
                    () -> "b",
                    (a, b) -> a + b,
                    Duration.ofSeconds(5)
            );

            assertThat(result).isEqualTo("ab");
        }

        @Test
        @DisplayName("parallel 超时")
        void testParallelTimeout() {
            assertThatThrownBy(() -> {
                StructuredTaskUtil.parallel(
                        () -> {
                            Thread.sleep(5000);
                            return "slow";
                        },
                        () -> "fast",
                        (a, b) -> a + b,
                        Duration.ofMillis(100)
                );
            }).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("parallel 三任务测试")
    class ParallelThreeTasksTests {

        @Test
        @DisplayName("parallel 三个任务成功")
        void testParallelThreeSuccess() throws Exception {
            String result = StructuredTaskUtil.parallel(
                    () -> "A",
                    () -> "B",
                    () -> "C",
                    (a, b, c) -> a + b + c
            );

            assertThat(result).isEqualTo("ABC");
        }

        @Test
        @DisplayName("parallel 三任务类型不同")
        void testParallelThreeDifferentTypes() throws Exception {
            record Combined(String s, Integer i, Boolean b) {}

            Combined result = StructuredTaskUtil.parallel(
                    () -> "text",
                    () -> 42,
                    () -> true,
                    Combined::new
            );

            assertThat(result.s()).isEqualTo("text");
            assertThat(result.i()).isEqualTo(42);
            assertThat(result.b()).isTrue();
        }

        @Test
        @DisplayName("parallel 三任务一个失败")
        void testParallelThreeOneFailure() {
            assertThatThrownBy(() -> {
                StructuredTaskUtil.parallel(
                        () -> "A",
                        () -> { throw new RuntimeException("fail"); },
                        () -> "C",
                        (a, b, c) -> a + b + c
                );
            }).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("run 单任务测试")
    class RunTests {

        @Test
        @DisplayName("run 成功")
        void testRunSuccess() throws Exception {
            String result = StructuredTaskUtil.run(() -> "result");
            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("run 失败")
        void testRunFailure() {
            assertThatThrownBy(() -> {
                StructuredTaskUtil.run(() -> {
                    throw new IllegalStateException("fail");
                });
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("run 返回 null")
        void testRunNull() throws Exception {
            String result = StructuredTaskUtil.run(() -> null);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("run 带超时测试")
    class RunWithTimeoutTests {

        @Test
        @DisplayName("run 带超时成功")
        void testRunWithTimeoutSuccess() throws Exception {
            String result = StructuredTaskUtil.run(
                    () -> "result",
                    Duration.ofSeconds(5)
            );
            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("run 超时")
        void testRunTimeout() {
            assertThatThrownBy(() -> {
                StructuredTaskUtil.run(
                        () -> {
                            Thread.sleep(5000);
                            return "slow";
                        },
                        Duration.ofMillis(100)
                );
            }).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("TriFunction 测试")
    class TriFunctionTests {

        @Test
        @DisplayName("TriFunction apply")
        void testTriFunctionApply() {
            StructuredTaskUtil.TriFunction<String, Integer, Boolean, String> func =
                    (s, i, b) -> s + "-" + i + "-" + b;

            String result = func.apply("test", 42, true);
            assertThat(result).isEqualTo("test-42-true");
        }
    }

    @Nested
    @DisplayName("复杂场景测试")
    class ComplexScenarioTests {

        @Test
        @DisplayName("模拟用户订单信息获取")
        void testUserOrderInfoScenario() throws Exception {
            record User(String name) {}
            record Order(String id) {}
            record UserOrderInfo(User user, List<Order> orders) {}

            UserOrderInfo info = StructuredTaskUtil.parallel(
                    () -> new User("Leon"),
                    () -> List.of(new Order("order1"), new Order("order2")),
                    UserOrderInfo::new
            );

            assertThat(info.user().name()).isEqualTo("Leon");
            assertThat(info.orders()).hasSize(2);
        }

        @Test
        @DisplayName("冗余请求模式")
        void testRedundancyPattern() throws Exception {
            String result = StructuredTaskUtil.invokeAny(List.of(
                    () -> {
                        Thread.sleep(50);
                        return "primary";
                    },
                    () -> {
                        Thread.sleep(100);
                        return "backup";
                    }
            ));

            assertThat(result).isEqualTo("primary");
        }

        @Test
        @DisplayName("批量数据获取")
        void testBatchFetch() throws Exception {
            List<String> results = StructuredTaskUtil.invokeAll(List.of(
                    () -> "service1-data",
                    () -> "service2-data",
                    () -> "service3-data"
            ));

            assertThat(results).hasSize(3);
            assertThat(results).allMatch(s -> s.endsWith("-data"));
        }
    }
}
