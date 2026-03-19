package cloud.opencode.base.test.mock;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SpyTest Tests
 * SpyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("Spy Tests")
class SpyTest {

    @Nested
    @DisplayName("record() Tests")
    class RecordTests {

        @Test
        @DisplayName("Should record invocation")
        void shouldRecordInvocation() {
            Spy spy = new Spy();

            spy.record("methodName", "arg1", "arg2");

            assertThat(spy.getInvocations()).hasSize(1);
        }

        @Test
        @DisplayName("Should record multiple invocations")
        void shouldRecordMultipleInvocations() {
            Spy spy = new Spy();

            spy.record("method1");
            spy.record("method2");
            spy.record("method1");

            assertThat(spy.getInvocations()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("wasCalled() Tests")
    class WasCalledTests {

        @Test
        @DisplayName("Should return true when method was called")
        void shouldReturnTrueWhenMethodWasCalled() {
            Spy spy = new Spy();
            spy.record("methodName");

            assertThat(spy.wasCalled("methodName")).isTrue();
        }

        @Test
        @DisplayName("Should return false when method was not called")
        void shouldReturnFalseWhenMethodWasNotCalled() {
            Spy spy = new Spy();

            assertThat(spy.wasCalled("methodName")).isFalse();
        }
    }

    @Nested
    @DisplayName("wasCalledTimes() Tests")
    class WasCalledTimesTests {

        @Test
        @DisplayName("Should return true when called exact times")
        void shouldReturnTrueWhenCalledExactTimes() {
            Spy spy = new Spy();
            spy.record("method");
            spy.record("method");

            assertThat(spy.wasCalledTimes("method", 2)).isTrue();
        }

        @Test
        @DisplayName("Should return false when called different times")
        void shouldReturnFalseWhenCalledDifferentTimes() {
            Spy spy = new Spy();
            spy.record("method");

            assertThat(spy.wasCalledTimes("method", 2)).isFalse();
        }
    }

    @Nested
    @DisplayName("getCallCount() Tests")
    class GetCallCountTests {

        @Test
        @DisplayName("Should return correct call count")
        void shouldReturnCorrectCallCount() {
            Spy spy = new Spy();
            spy.record("method");
            spy.record("method");
            spy.record("other");

            assertThat(spy.getCallCount("method")).isEqualTo(2);
            assertThat(spy.getCallCount("other")).isEqualTo(1);
            assertThat(spy.getCallCount("unknown")).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getInvocations() Tests")
    class GetInvocationsTests {

        @Test
        @DisplayName("Should return all invocations")
        void shouldReturnAllInvocations() {
            Spy spy = new Spy();
            spy.record("method1");
            spy.record("method2");

            List<Spy.Invocation> invocations = spy.getInvocations();
            assertThat(invocations).hasSize(2);
        }

        @Test
        @DisplayName("Should return invocations for specific method")
        void shouldReturnInvocationsForSpecificMethod() {
            Spy spy = new Spy();
            spy.record("method1", "arg1");
            spy.record("method2");
            spy.record("method1", "arg2");

            List<Spy.Invocation> invocations = spy.getInvocations("method1");
            assertThat(invocations).hasSize(2);
            assertThat(invocations).allMatch(i -> i.methodName().equals("method1"));
        }
    }

    @Nested
    @DisplayName("getLastInvocation() Tests")
    class GetLastInvocationTests {

        @Test
        @DisplayName("Should return last invocation")
        void shouldReturnLastInvocation() {
            Spy spy = new Spy();
            spy.record("first");
            spy.record("last", "arg");

            Spy.Invocation last = spy.getLastInvocation();
            assertThat(last.methodName()).isEqualTo("last");
        }

        @Test
        @DisplayName("Should return null when no invocations")
        void shouldReturnNullWhenNoInvocations() {
            Spy spy = new Spy();
            assertThat(spy.getLastInvocation()).isNull();
        }
    }

    @Nested
    @DisplayName("clear() Tests")
    class ClearTests {

        @Test
        @DisplayName("Should clear all invocations")
        void shouldClearAllInvocations() {
            Spy spy = new Spy();
            spy.record("method");
            spy.clear();

            assertThat(spy.getInvocations()).isEmpty();
        }
    }

    @Nested
    @DisplayName("noInteractions() Tests")
    class NoInteractionsTests {

        @Test
        @DisplayName("Should return true when no invocations")
        void shouldReturnTrueWhenNoInvocations() {
            Spy spy = new Spy();
            assertThat(spy.noInteractions()).isTrue();
        }

        @Test
        @DisplayName("Should return false when has invocations")
        void shouldReturnFalseWhenHasInvocations() {
            Spy spy = new Spy();
            spy.record("method");
            assertThat(spy.noInteractions()).isFalse();
        }
    }

    @Nested
    @DisplayName("Invocation Record Tests")
    class InvocationRecordTests {

        @Test
        @DisplayName("Should store method name and args")
        void shouldStoreMethodNameAndArgs() {
            Spy spy = new Spy();
            spy.record("method", "arg1", "arg2");

            Spy.Invocation invocation = spy.getLastInvocation();
            assertThat(invocation.methodName()).isEqualTo("method");
            assertThat(invocation.args()).containsExactly("arg1", "arg2");
        }

        @Test
        @DisplayName("getArg should return argument at index")
        void getArgShouldReturnArgumentAtIndex() {
            Spy spy = new Spy();
            spy.record("method", "arg0", "arg1", "arg2");

            Spy.Invocation invocation = spy.getLastInvocation();
            assertThat(invocation.getArg(0)).isEqualTo("arg0");
            assertThat(invocation.getArg(1)).isEqualTo("arg1");
            assertThat(invocation.getArg(2)).isEqualTo("arg2");
        }

        @Test
        @DisplayName("getArg should return null for out of bounds index")
        void getArgShouldReturnNullForOutOfBoundsIndex() {
            Spy spy = new Spy();
            spy.record("method", "arg0");

            Spy.Invocation invocation = spy.getLastInvocation();
            assertThat(invocation.getArg(10)).isNull();
        }

        @Test
        @DisplayName("getArgCount should return argument count")
        void getArgCountShouldReturnArgumentCount() {
            Spy spy = new Spy();
            spy.record("method", "a", "b", "c");

            Spy.Invocation invocation = spy.getLastInvocation();
            assertThat(invocation.getArgCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should handle null args")
        void shouldHandleNullArgs() {
            Spy spy = new Spy();
            spy.record("method", (Object[]) null);

            Spy.Invocation invocation = spy.getLastInvocation();
            assertThat(invocation.args()).isEmpty();
        }
    }
}
