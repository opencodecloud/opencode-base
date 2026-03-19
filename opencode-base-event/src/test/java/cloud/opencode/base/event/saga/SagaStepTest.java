package cloud.opencode.base.event.saga;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * SagaStep 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("SagaStep 测试")
class SagaStepTest {

    @Nested
    @DisplayName("记录构造测试")
    class RecordConstructorTests {

        @Test
        @DisplayName("全参数构造")
        void testFullConstructor() {
            SagaStep<String> step = new SagaStep<>(
                    "step1",
                    ctx -> {},
                    ctx -> {},
                    Duration.ofSeconds(10),
                    3
            );

            assertThat(step.name()).isEqualTo("step1");
            assertThat(step.action()).isNotNull();
            assertThat(step.compensation()).isNotNull();
            assertThat(step.timeout()).isEqualTo(Duration.ofSeconds(10));
            assertThat(step.maxRetries()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("of() 工厂方法测试")
    class OfFactoryTests {

        @Test
        @DisplayName("仅action创建步骤")
        void testOfWithAction() {
            AtomicBoolean executed = new AtomicBoolean(false);

            SagaStep<String> step = SagaStep.of("simple", ctx -> executed.set(true));

            assertThat(step.name()).isEqualTo("simple");
            assertThat(step.action()).isNotNull();
            assertThat(step.compensation()).isNull();
            assertThat(step.timeout()).isNull();
            assertThat(step.maxRetries()).isZero();

            step.action().accept("context");
            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("action和compensation创建步骤")
        void testOfWithActionAndCompensation() {
            AtomicBoolean actionExecuted = new AtomicBoolean(false);
            AtomicBoolean compensationExecuted = new AtomicBoolean(false);

            SagaStep<String> step = SagaStep.of(
                    "withComp",
                    ctx -> actionExecuted.set(true),
                    ctx -> compensationExecuted.set(true)
            );

            assertThat(step.name()).isEqualTo("withComp");
            assertThat(step.action()).isNotNull();
            assertThat(step.compensation()).isNotNull();

            step.action().accept("context");
            assertThat(actionExecuted.get()).isTrue();

            step.compensation().accept("context");
            assertThat(compensationExecuted.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("记录访问器测试")
    class RecordAccessorTests {

        @Test
        @DisplayName("name()返回步骤名称")
        void testNameAccessor() {
            SagaStep<String> step = SagaStep.of("myStep", ctx -> {});

            assertThat(step.name()).isEqualTo("myStep");
        }

        @Test
        @DisplayName("action()返回动作")
        void testActionAccessor() {
            AtomicBoolean called = new AtomicBoolean(false);
            SagaStep<String> step = SagaStep.of("step", ctx -> called.set(true));

            step.action().accept("ctx");

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("timeout()返回超时时间")
        void testTimeoutAccessor() {
            SagaStep<String> step = new SagaStep<>(
                    "step", ctx -> {}, null, Duration.ofMinutes(5), 0);

            assertThat(step.timeout()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("maxRetries()返回最大重试次数")
        void testMaxRetriesAccessor() {
            SagaStep<String> step = new SagaStep<>(
                    "step", ctx -> {}, null, null, 5);

            assertThat(step.maxRetries()).isEqualTo(5);
        }
    }
}
