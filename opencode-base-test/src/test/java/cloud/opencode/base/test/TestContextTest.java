package cloud.opencode.base.test;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * TestContextTest Tests
 * TestContextTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("TestContext Tests")
class TestContextTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create(name) should create context with name")
        void createWithNameShouldCreateContextWithName() {
            TestContext context = TestContext.create("myTest");

            assertThat(context.testName()).isEqualTo("myTest");
            assertThat(context.startTime()).isNotNull();
        }

        @Test
        @DisplayName("create() should create context with default name")
        void createShouldCreateContextWithDefaultName() {
            TestContext context = TestContext.create();

            assertThat(context.testName()).startsWith("test-");
        }
    }

    @Nested
    @DisplayName("Variable Tests")
    class VariableTests {

        @Test
        @DisplayName("Should set and get variable")
        void shouldSetAndGetVariable() {
            TestContext context = TestContext.create("test");

            context.setVariable("key", "value");

            assertThat(context.getVariable("key")).isPresent().contains("value");
        }

        @Test
        @DisplayName("Should return empty for missing variable")
        void shouldReturnEmptyForMissingVariable() {
            TestContext context = TestContext.create("test");
            assertThat(context.getVariable("missing")).isEmpty();
        }

        @Test
        @DisplayName("Should return default value for missing variable")
        void shouldReturnDefaultValueForMissingVariable() {
            TestContext context = TestContext.create("test");
            assertThat(context.getVariable("missing", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("hasVariable should return true when exists")
        void hasVariableShouldReturnTrueWhenExists() {
            TestContext context = TestContext.create("test");
            context.setVariable("key", "value");

            assertThat(context.hasVariable("key")).isTrue();
            assertThat(context.hasVariable("missing")).isFalse();
        }

        @Test
        @DisplayName("removeVariable should remove variable")
        void removeVariableShouldRemoveVariable() {
            TestContext context = TestContext.create("test");
            context.setVariable("key", "value");
            context.removeVariable("key");

            assertThat(context.hasVariable("key")).isFalse();
        }

        @Test
        @DisplayName("variables() should return all variables")
        void variablesShouldReturnAllVariables() {
            TestContext context = TestContext.create("test");
            context.setVariable("key1", "value1");
            context.setVariable("key2", "value2");

            assertThat(context.variables()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Attribute Tests")
    class AttributeTests {

        @Test
        @DisplayName("Should set and get attribute")
        void shouldSetAndGetAttribute() {
            TestContext context = TestContext.create("test");

            context.setAttribute("attr", "value");

            assertThat(context.getAttribute("attr")).isPresent().contains("value");
        }

        @Test
        @DisplayName("Should return default value for missing attribute")
        void shouldReturnDefaultValueForMissingAttribute() {
            TestContext context = TestContext.create("test");
            assertThat(context.getAttribute("missing", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("hasAttribute should return true when exists")
        void hasAttributeShouldReturnTrueWhenExists() {
            TestContext context = TestContext.create("test");
            context.setAttribute("attr", "value");

            assertThat(context.hasAttribute("attr")).isTrue();
            assertThat(context.hasAttribute("missing")).isFalse();
        }

        @Test
        @DisplayName("attributes() should return all attributes")
        void attributesShouldReturnAllAttributes() {
            TestContext context = TestContext.create("test");
            context.setAttribute("attr1", "value1");

            assertThat(context.attributes()).containsKey("attr1");
        }
    }

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should have pending status initially")
        void shouldHavePendingStatusInitially() {
            TestContext context = TestContext.create("test");
            assertThat(context.status()).isEqualTo("pending");
        }

        @Test
        @DisplayName("isPassed should return false initially")
        void isPassedShouldReturnFalseInitially() {
            TestContext context = TestContext.create("test");
            assertThat(context.isPassed()).isFalse();
        }

        @Test
        @DisplayName("isFailed should return false initially")
        void isFailedShouldReturnFalseInitially() {
            TestContext context = TestContext.create("test");
            assertThat(context.isFailed()).isFalse();
        }
    }

    @Nested
    @DisplayName("Duration Tests")
    class DurationTests {

        @Test
        @DisplayName("duration() should return positive duration")
        void durationShouldReturnPositiveDuration() {
            TestContext context = TestContext.create("test");
            Duration duration = context.duration();
            assertThat(duration.isNegative()).isFalse();
        }

        @Test
        @DisplayName("endTime() should be empty initially")
        void endTimeShouldBeEmptyInitially() {
            TestContext context = TestContext.create("test");
            assertThat(context.endTime()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Callback Tests")
    class CallbackTests {

        @Test
        @DisplayName("Should support fluent chaining")
        void shouldSupportFluentChaining() {
            TestContext context = TestContext.create("test")
                .setVariable("var", "value")
                .setAttribute("attr", "value")
                .onSuccess(ctx -> {})
                .onFailure((ctx, ex) -> {});

            assertThat(context).isNotNull();
        }
    }

    @Nested
    @DisplayName("Run Tests")
    class RunTests {

        @Test
        @DisplayName("run with runnable should execute in context")
        void runWithRunnableShouldExecuteInContext() {
            TestContext context = TestContext.create("test");
            AtomicBoolean executed = new AtomicBoolean(false);

            TestContext.run(context, () -> {
                executed.set(true);
                assertThat(TestContext.current()).isPresent();
            });

            assertThat(executed.get()).isTrue();
            assertThat(context.isPassed()).isTrue();
        }

        @Test
        @DisplayName("run should set status to failure on exception")
        void runShouldSetStatusToFailureOnException() {
            TestContext context = TestContext.create("test");

            assertThatThrownBy(() -> TestContext.run(context, () -> {
                throw new RuntimeException("test error");
            })).isInstanceOf(RuntimeException.class);

            assertThat(context.isFailed()).isTrue();
            assertThat(context.exception()).isPresent();
        }
    }

    @Nested
    @DisplayName("Current Context Tests")
    class CurrentContextTests {

        @Test
        @DisplayName("current() should return empty outside context")
        void currentShouldReturnEmptyOutsideContext() {
            assertThat(TestContext.current()).isEmpty();
        }

        @Test
        @DisplayName("currentOrCreate() should create new context")
        void currentOrCreateShouldCreateNewContext() {
            TestContext context = TestContext.currentOrCreate();
            assertThat(context).isNotNull();
        }

        @Test
        @DisplayName("currentOrCreate(name) should create with name")
        void currentOrCreateWithNameShouldCreateWithName() {
            TestContext context = TestContext.currentOrCreate("myTest");
            assertThat(context.testName()).isEqualTo("myTest");
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string")
        void shouldReturnFormattedString() {
            TestContext context = TestContext.create("myTest");
            String str = context.toString();

            assertThat(str).contains("TestContext");
            assertThat(str).contains("myTest");
            assertThat(str).contains("status=");
            assertThat(str).contains("duration=");
        }
    }
}
