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

    @Nested
    @DisplayName("全局超时测试")
    class GlobalTimeoutTests {

        @Test
        @DisplayName("全局超时触发补偿")
        void testGlobalTimeoutTriggersCompensation() {
            AtomicBoolean step2Executed = new AtomicBoolean(false);
            AtomicBoolean step1Compensated = new AtomicBoolean(false);

            Saga<String> saga = Saga.<String>builder()
                    .name("timeout-saga")
                    .globalTimeout(Duration.ofMillis(50))
                    .step("step1")
                        .action(ctx -> {
                            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        })
                        .compensation(ctx -> step1Compensated.set(true))
                        .build()
                    .step("step2")
                        .action(ctx -> step2Executed.set(true))
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isCompensated()).isTrue();
            assertThat(step2Executed.get()).isFalse();
        }

        @Test
        @DisplayName("全局超时在步骤执行前检测到已超时")
        void testGlobalTimeoutElapsedBeforeStep() {
            AtomicBoolean step2Executed = new AtomicBoolean(false);
            AtomicBoolean step1Compensated = new AtomicBoolean(false);

            Saga<String> saga = Saga.<String>builder()
                    .name("pre-check-timeout-saga")
                    .globalTimeout(Duration.ofMillis(50))
                    .step("step1")
                        .action(ctx -> {
                            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        })
                        .compensation(ctx -> step1Compensated.set(true))
                        .timeout(Duration.ofMillis(200))
                        .build()
                    .step("step2")
                        .action(ctx -> step2Executed.set(true))
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isCompensated()).isTrue();
            assertThat(result.error()).isNotNull();
            assertThat(step2Executed.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("步骤超时测试")
    class StepTimeoutTests {

        @Test
        @DisplayName("步骤超时触发补偿")
        void testStepTimeoutTriggersCompensation() {
            AtomicBoolean compensated = new AtomicBoolean(false);

            Saga<String> saga = Saga.<String>builder()
                    .step("slow-step")
                        .action(ctx -> {
                            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        })
                        .compensation(ctx -> compensated.set(true))
                        .timeout(Duration.ofMillis(50))
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isCompensated()).isTrue();
            assertThat(result.failedStep()).isEqualTo("slow-step");
            assertThat(result.error()).isInstanceOf(Saga.SagaTimeoutException.class);
        }
    }

    @Nested
    @DisplayName("全局超时与步骤超时交互测试")
    class GlobalStepTimeoutInteractionTests {

        @Test
        @DisplayName("全局剩余时间小于步骤超时时使用全局剩余时间")
        void testGlobalRemainingLessThanStepTimeout() {
            AtomicBoolean compensated = new AtomicBoolean(false);

            Saga<String> saga = Saga.<String>builder()
                    .name("interaction-saga")
                    .globalTimeout(Duration.ofMillis(80))
                    .step("step1")
                        .action(ctx -> {
                            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        })
                        .build()
                    .step("step2")
                        .action(ctx -> {
                            // After step1 takes ~50ms, only ~30ms global remains.
                            // Step timeout is 5000ms but effective should be ~30ms.
                            // This sleep of 200ms should exceed the effective timeout.
                            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        })
                        .compensation(ctx -> compensated.set(true))
                        .timeout(Duration.ofSeconds(5))
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isCompensated()).isTrue();
        }
    }

    @Nested
    @DisplayName("补偿失败处理测试")
    class CompensationFailureTests {

        @Test
        @DisplayName("补偿本身抛异常时继续补偿其他步骤")
        void testCompensationExceptionContinuesOtherCompensations() {
            AtomicBoolean step1Compensated = new AtomicBoolean(false);

            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> {})
                        .compensation(ctx -> step1Compensated.set(true))
                        .build()
                    .step("step2")
                        .action(ctx -> {})
                        .compensation(ctx -> { throw new RuntimeException("comp2 failed"); })
                        .build()
                    .step("step3")
                        .action(ctx -> { throw new RuntimeException("step3 failed"); })
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isCompensated()).isTrue();
            // step2's compensation threw, but step1's compensation should still have run
            assertThat(step1Compensated.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("回调异常处理测试")
    class CallbackExceptionTests {

        @Test
        @DisplayName("onSuccess回调异常不影响结果")
        void testOnSuccessCallbackExceptionDoesNotAffectResult() {
            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> {})
                        .build()
                    .onSuccess(ctx -> { throw new RuntimeException("callback exploded"); })
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(SagaStatus.COMPLETED);
        }

        @Test
        @DisplayName("onFailure回调异常不影响结果")
        void testOnFailureCallbackExceptionDoesNotAffectResult() {
            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> { throw new RuntimeException("fail"); })
                        .build()
                    .onFailure((ctx, error) -> { throw new RuntimeException("callback exploded"); })
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isCompensated()).isTrue();
            assertThat(result.status()).isEqualTo(SagaStatus.COMPENSATED);
        }
    }

    @Nested
    @DisplayName("无超时步骤测试")
    class NoTimeoutTests {

        @Test
        @DisplayName("步骤和全局都无超时时直接执行")
        void testStepWithNullTimeoutAndNullGlobalTimeout() {
            AtomicBoolean executed = new AtomicBoolean(false);

            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> executed.set(true))
                        // no timeout, no globalTimeout
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isSuccess()).isTrue();
            assertThat(executed.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("重试耗尽测试")
    class RetryExhaustionTests {

        @Test
        @DisplayName("重试耗尽后触发补偿")
        void testRetryExhaustionTriggersCompensation() {
            AtomicInteger attemptCount = new AtomicInteger(0);
            AtomicBoolean compensated = new AtomicBoolean(false);

            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> {})
                        .compensation(ctx -> compensated.set(true))
                        .build()
                    .step("retry-step")
                        .action(ctx -> {
                            attemptCount.incrementAndGet();
                            throw new RuntimeException("always fail");
                        })
                        .retries(2)
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isCompensated()).isTrue();
            assertThat(result.failedStep()).isEqualTo("retry-step");
            // initial attempt + 2 retries = 3 total
            assertThat(attemptCount.get()).isEqualTo(3);
            assertThat(compensated.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("异步失败测试")
    class ExecuteAsyncFailureTests {

        @Test
        @DisplayName("异步执行失败返回正确结果")
        void testAsyncExecutionFailure() throws Exception {
            Saga<String> saga = Saga.<String>builder()
                    .step("fail-step")
                        .action(ctx -> { throw new RuntimeException("async fail"); })
                        .build()
                    .build();

            CompletableFuture<SagaResult<String>> future = saga.executeAsync("ctx");
            SagaResult<String> result = future.get(5, TimeUnit.SECONDS);

            assertThat(result.isCompensated()).isTrue();
            assertThat(result.failedStep()).isEqualTo("fail-step");
        }
    }

    @Nested
    @DisplayName("负数重试测试")
    class NegativeRetriesTests {

        @Test
        @DisplayName("负数重试被钳制为0")
        void testNegativeRetriesClampedToZero() {
            AtomicInteger attemptCount = new AtomicInteger(0);

            Saga<String> saga = Saga.<String>builder()
                    .step("step1")
                        .action(ctx -> {
                            attemptCount.incrementAndGet();
                            throw new RuntimeException("fail");
                        })
                        .retries(-1)
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isCompensated()).isTrue();
            // Only 1 attempt, no retries
            assertThat(attemptCount.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("EventException解包测试")
    class EventExceptionUnwrapTests {

        @Test
        @DisplayName("步骤action抛出的异常通过CompletableFuture时被包装为ExecutionException")
        void testActionExceptionWrappedViaTimeout() {
            Saga<String> saga = Saga.<String>builder()
                    .step("wrapped-step")
                        .action(ctx -> { throw new RuntimeException("original cause"); })
                        .timeout(Duration.ofSeconds(5))
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isCompensated()).isTrue();
            assertThat(result.failedStep()).isEqualTo("wrapped-step");
            // ExecutionException is not a RuntimeException, so the unwrap logic
            // keeps it as-is; the original cause is nested inside
            assertThat(result.error()).isNotNull();
        }

        @Test
        @DisplayName("直接RuntimeException包装的cause被解包")
        void testRuntimeExceptionCauseIsUnwrapped() {
            // Without timeout, the action runs directly - no CompletableFuture wrapping
            // With no timeout, step.action().accept(context) is called directly
            AtomicInteger attempts = new AtomicInteger(0);
            RuntimeException originalCause = new RuntimeException("original");

            Saga<String> saga = Saga.<String>builder()
                    .step("direct-step")
                        .action(ctx -> {
                            attempts.incrementAndGet();
                            throw originalCause;
                        })
                        .build()
                    .build();

            SagaResult<String> result = saga.execute("ctx");

            assertThat(result.isCompensated()).isTrue();
            // Direct execution: exception is the RuntimeException itself
            assertThat(result.error()).isSameAs(originalCause);
        }
    }
}
