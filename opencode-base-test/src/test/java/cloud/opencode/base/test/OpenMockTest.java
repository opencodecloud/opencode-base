package cloud.opencode.base.test;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenMockTest Tests
 * OpenMockTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("OpenMock Tests")
class OpenMockTest {

    interface TestService {
        String getName();
        int getValue();
        void doSomething();
        String greet(String name);
    }

    @Nested
    @DisplayName("mock() Tests")
    class MockTests {

        @Test
        @DisplayName("Should create mock for interface")
        void shouldCreateMockForInterface() {
            TestService mock = OpenMock.mock(TestService.class);
            assertThat(mock).isNotNull();
        }

        @Test
        @DisplayName("Should throw for non-interface type")
        void shouldThrowForNonInterfaceType() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> OpenMock.mock(String.class))
                .withMessageContaining("only supports interfaces");
        }

        @Test
        @DisplayName("Mock should return default values")
        void mockShouldReturnDefaultValues() {
            TestService mock = OpenMock.mock(TestService.class);

            assertThat(mock.getName()).isNull();
            assertThat(mock.getValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("Mock toString should return mock identifier")
        void mockToStringShouldReturnMockIdentifier() {
            TestService mock = OpenMock.mock(TestService.class);
            assertThat(mock.toString()).startsWith("Mock@");
        }

        @Test
        @DisplayName("Mock hashCode should return identity hash code")
        void mockHashCodeShouldReturnIdentityHashCode() {
            TestService mock = OpenMock.mock(TestService.class);
            assertThat(mock.hashCode()).isEqualTo(System.identityHashCode(mock));
        }

        @Test
        @DisplayName("Mock equals should use identity comparison")
        void mockEqualsShouldUseIdentityComparison() {
            TestService mock1 = OpenMock.mock(TestService.class);
            TestService mock2 = OpenMock.mock(TestService.class);

            assertThat(mock1.equals(mock1)).isTrue();
            assertThat(mock1.equals(mock2)).isFalse();
        }
    }

    @Nested
    @DisplayName("when() MockBuilder Tests")
    class MockBuilderTests {

        @Test
        @DisplayName("Should build mock with stubbed return value")
        void shouldBuildMockWithStubbedReturnValue() {
            TestService mock = OpenMock.when(TestService.class)
                .thenReturn("getName", "John")
                .build();

            assertThat(mock.getName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should build mock with multiple stubs")
        void shouldBuildMockWithMultipleStubs() {
            TestService mock = OpenMock.when(TestService.class)
                .thenReturn("getName", "John")
                .thenReturn("getValue", 42)
                .build();

            assertThat(mock.getName()).isEqualTo("John");
            assertThat(mock.getValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("Should build mock with args-specific stub")
        void shouldBuildMockWithArgsSpecificStub() {
            TestService mock = OpenMock.when(TestService.class)
                .thenReturn("greet", new Object[]{"World"}, "Hello, World!")
                .build();

            assertThat(mock.greet("World")).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should throw for non-interface type")
        void shouldThrowForNonInterfaceType() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> OpenMock.when(String.class))
                .withMessageContaining("only supports interfaces");
        }
    }

    @Nested
    @DisplayName("verify() Tests")
    class VerifyTests {

        @Test
        @DisplayName("wasInvoked should pass when method was called")
        void wasInvokedShouldPassWhenMethodWasCalled() {
            TestService mock = OpenMock.mock(TestService.class);
            mock.getName();

            assertThatNoException().isThrownBy(() ->
                OpenMock.verify(mock).wasInvoked("getName"));
        }

        @Test
        @DisplayName("wasInvoked should fail when method was not called")
        void wasInvokedShouldFailWhenMethodWasNotCalled() {
            TestService mock = OpenMock.mock(TestService.class);

            assertThatThrownBy(() -> OpenMock.verify(mock).wasInvoked("getName"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("not invoked");
        }

        @Test
        @DisplayName("wasInvoked with times should check exact count")
        void wasInvokedWithTimesShouldCheckExactCount() {
            TestService mock = OpenMock.mock(TestService.class);
            mock.getName();
            mock.getName();

            assertThatNoException().isThrownBy(() ->
                OpenMock.verify(mock).wasInvoked("getName", 2));
        }

        @Test
        @DisplayName("wasInvoked with times should fail on wrong count")
        void wasInvokedWithTimesShouldFailOnWrongCount() {
            TestService mock = OpenMock.mock(TestService.class);
            mock.getName();

            assertThatThrownBy(() -> OpenMock.verify(mock).wasInvoked("getName", 2))
                .isInstanceOf(AssertionError.class);
        }

        @Test
        @DisplayName("wasNeverInvoked should pass when not called")
        void wasNeverInvokedShouldPassWhenNotCalled() {
            TestService mock = OpenMock.mock(TestService.class);

            assertThatNoException().isThrownBy(() ->
                OpenMock.verify(mock).wasNeverInvoked("getName"));
        }

        @Test
        @DisplayName("wasNeverInvoked should fail when was called")
        void wasNeverInvokedShouldFailWhenWasCalled() {
            TestService mock = OpenMock.mock(TestService.class);
            mock.getName();

            assertThatThrownBy(() -> OpenMock.verify(mock).wasNeverInvoked("getName"))
                .isInstanceOf(AssertionError.class);
        }

        @Test
        @DisplayName("invocationCount should return correct count")
        void invocationCountShouldReturnCorrectCount() {
            TestService mock = OpenMock.mock(TestService.class);
            mock.getName();
            mock.getName();
            mock.getValue();

            assertThat(OpenMock.verify(mock).invocationCount("getName")).isEqualTo(2);
            assertThat(OpenMock.verify(mock).invocationCount("getValue")).isEqualTo(1);
        }

        @Test
        @DisplayName("getAllInvocations should return all invocations")
        void getAllInvocationsShouldReturnAllInvocations() {
            TestService mock = OpenMock.mock(TestService.class);
            mock.getName();
            mock.getValue();

            List<OpenMock.Invocation> invocations = OpenMock.verify(mock).getAllInvocations();
            assertThat(invocations).hasSize(2);
        }

        @Test
        @DisplayName("verify should throw for non-mock object")
        void verifyShouldThrowForNonMockObject() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> OpenMock.verify("not a mock"))
                .withMessageContaining("Not a mock object");
        }
    }

    @Nested
    @DisplayName("reset() Tests")
    class ResetTests {

        @Test
        @DisplayName("reset should clear invocations")
        void resetShouldClearInvocations() {
            TestService mock = OpenMock.mock(TestService.class);
            mock.getName();

            OpenMock.reset(mock);

            assertThat(OpenMock.verify(mock).getAllInvocations()).isEmpty();
        }

        @Test
        @DisplayName("reset should not throw for non-mock object")
        void resetShouldNotThrowForNonMockObject() {
            assertThatNoException().isThrownBy(() -> OpenMock.reset("not a mock"));
        }
    }

    @Nested
    @DisplayName("Invocation Record Tests")
    class InvocationRecordTests {

        @Test
        @DisplayName("Invocation should record method details")
        void invocationShouldRecordMethodDetails() {
            TestService mock = OpenMock.mock(TestService.class);
            mock.greet("World");

            List<OpenMock.Invocation> invocations = OpenMock.verify(mock).getAllInvocations();
            assertThat(invocations).hasSize(1);

            OpenMock.Invocation invocation = invocations.getFirst();
            assertThat(invocation.methodName()).isEqualTo("greet");
            assertThat(invocation.parameterTypes()).containsExactly(String.class);
            assertThat(invocation.args()).containsExactly("World");
        }
    }
}
