package cloud.opencode.base.event.saga;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Saga 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("Saga 测试")
class SagaTest {

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("构建基本Saga")
        void testBuildBasicSaga() {
            Saga<String> saga = Saga.<String>builder()
                    .name("test-saga")
                    .step("step1")
                        .action(ctx -> {})
                        .build()
                    .build();

            assertThat(saga.getName()).isEqualTo("test-saga");
            assertThat(saga.getStepCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("没有步骤抛出异常")
        void testBuildWithoutStepsThrows() {
            assertThatThrownBy(() -> Saga.<String>builder()
                    .name("empty-saga")
                    .build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("at least one step");
        }

        @Test
        @DisplayName("步骤没有action抛出异常")
        void testStepWithoutActionThrows() {
            assertThatThrownBy(() -> Saga.<String>builder()
                    .step("step1")
                    .build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("action is required");
        }

        @Test
        @DisplayName("配置多个步骤")
        void testMultipleSteps() {
            Saga<String> saga = Saga.<String>builder()
                    .name("multi-step")
                    .step("step1")
                        .action(ctx -> {})
                        .build()
                    .step("step2")
                        .action(ctx -> {})
                        .build()
                    .step("step3")
                        .action(ctx -> {})
                        .build()
                    .build();

            assertThat(saga.getStepCount()).isEqualTo(3);
            assertThat(saga.getStepNames()).containsExactly("step1", "step2", "step3");
        }

        @Test
        @DisplayName("配置补偿操作")
        void testStepWithCompensation() {
            AtomicBoolean compensated = new AtomicBoolean(false);

            // Note: Compensation is only called for COMPLETED steps
            // So step1 must succeed, and step2 must fail for step1's compensation to be called
            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> {})
                        .compensation(ctx -> compensated.set(true))
                        .build()
                    .step("step2")
                        .action(ctx -> { throw new RuntimeException("fail"); })
                        .build()
                    .build();

            saga.execute("ctx");

            assertThat(compensated.get()).isTrue();
        }

        @Test
        @DisplayName("配置超时")
        void testStepWithTimeout() {
            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> {})
                        .timeout(Duration.ofSeconds(30))
                        .build()
                    .build();

            assertThat(saga).isNotNull();
        }

        @Test
        @DisplayName("配置重试")
        void testStepWithRetries() {
            AtomicInteger attemptCount = new AtomicInteger(0);

            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> {
                            if (attemptCount.incrementAndGet() < 3) {
                                throw new RuntimeException("Retry please");
                            }
                        })
                        .retries(3)
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isSuccess()).isTrue();
            assertThat(attemptCount.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("使用addStep添加步骤")
        void testAddStep() {
            SagaStep<String> step = SagaStep.of("prebuilt", ctx -> {});

            Saga<String> saga = Saga.<String>builder()
                    .addStep(step)
                    .build();

            assertThat(saga.getStepCount()).isEqualTo(1);
            assertThat(saga.getStepNames()).containsExactly("prebuilt");
        }

        @Test
        @DisplayName("配置onSuccess回调")
        void testOnSuccessCallback() {
            AtomicBoolean successCalled = new AtomicBoolean(false);

            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> {})
                        .build()
                    .onSuccess(ctx -> successCalled.set(true))
                    .build();

            saga.execute("ctx");

            assertThat(successCalled.get()).isTrue();
        }

        @Test
        @DisplayName("配置onFailure回调")
        void testOnFailureCallback() {
            AtomicBoolean failureCalled = new AtomicBoolean(false);

            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> { throw new RuntimeException("fail"); })
                        .build()
                    .onFailure((ctx, error) -> failureCalled.set(true))
                    .build();

            saga.execute("ctx");

            assertThat(failureCalled.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("execute() 测试")
    class ExecuteTests {

        @Test
        @DisplayName("成功执行所有步骤")
        void testSuccessfulExecution() {
            List<String> executed = new ArrayList<>();

            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> executed.add("step1"))
                        .build()
                    .step("step2")
                        .action(ctx -> executed.add("step2"))
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("context");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(SagaStatus.COMPLETED);
            assertThat(executed).containsExactly("step1", "step2");
        }

        @Test
        @DisplayName("步骤失败触发补偿")
        void testFailureTriggersCompensation() {
            List<String> actions = new ArrayList<>();

            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> actions.add("action1"))
                        .compensation(ctx -> actions.add("comp1"))
                        .build()
                    .step("step2")
                        .action(ctx -> actions.add("action2"))
                        .compensation(ctx -> actions.add("comp2"))
                        .build()
                    .step("step3")
                        .action(ctx -> { throw new RuntimeException("fail"); })
                        .compensation(ctx -> actions.add("comp3"))
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isCompensated()).isTrue();
            assertThat(result.failedStep()).isEqualTo("step3");
            // 补偿按逆序执行
            assertThat(actions).containsExactly("action1", "action2", "comp2", "comp1");
        }

        @Test
        @DisplayName("返回正确的SagaResult")
        void testReturnsCorrectResult() {
            Saga<String> saga = Saga.<String>builder()
                    .name("test-saga")
                    .step("step1")
                        .action(ctx -> {})
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("myContext");

            assertThat(result.sagaId()).isNotNull();
            assertThat(result.sagaName()).isEqualTo("test-saga");
            assertThat(result.context()).isEqualTo("myContext");
            assertThat(result.startTime()).isNotNull();
            assertThat(result.endTime()).isNotNull();
            assertThat(result.getDuration()).isNotNull();
        }
    }

    @Nested
    @DisplayName("executeAsync() 测试")
    class ExecuteAsyncTests {

        @Test
        @DisplayName("异步执行返回Future")
        void testAsyncExecution() throws Exception {
            AtomicBoolean executed = new AtomicBoolean(false);

            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> executed.set(true))
                        .build()
                    .build();

            CompletableFuture<SagaResult<String>> future = saga.executeAsync("ctx");
            SagaResult<String> result = future.get(5, TimeUnit.SECONDS);

            assertThat(executed.get()).isTrue();
            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("信息方法测试")
    class InfoMethodTests {

        @Test
        @DisplayName("getName返回saga名称")
        void testGetName() {
            Saga<String> saga = Saga.<String>builder()
                    .name("my-saga")
                    .step("step1").action(ctx -> {}).build()
                    .build();

            assertThat(saga.getName()).isEqualTo("my-saga");
        }

        @Test
        @DisplayName("getStepCount返回步骤数量")
        void testGetStepCount() {
            Saga<String> saga = Saga.<String>builder()
                    .step("s1").action(ctx -> {}).build()
                    .step("s2").action(ctx -> {}).build()
                    .step("s3").action(ctx -> {}).build()
                    .build();

            assertThat(saga.getStepCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("getStepNames返回步骤名称列表")
        void testGetStepNames() {
            Saga<String> saga = Saga.<String>builder()
                    .step("alpha").action(ctx -> {}).build()
                    .step("beta").action(ctx -> {}).build()
                    .build();

            assertThat(saga.getStepNames()).containsExactly("alpha", "beta");
        }
    }

    @Nested
    @DisplayName("SagaTimeoutException测试")
    class SagaTimeoutExceptionTests {

        @Test
        @DisplayName("超时异常包含消息和原因")
        void testTimeoutException() {
            RuntimeException cause = new RuntimeException("Original");
            Saga.SagaTimeoutException ex = new Saga.SagaTimeoutException("Step timed out", cause);

            assertThat(ex.getMessage()).isEqualTo("Step timed out");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }
}
