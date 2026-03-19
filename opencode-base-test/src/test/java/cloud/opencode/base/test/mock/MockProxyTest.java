package cloud.opencode.base.test.mock;

import cloud.opencode.base.test.exception.MockException;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * MockProxyTest Tests
 * MockProxyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("MockProxy Tests")
class MockProxyTest {

    interface TestService {
        String getName();
        int getValue();
        void doSomething();
    }

    interface AnotherService {
        void process();
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create mock for interface")
        void shouldCreateMockForInterface() {
            TestService mock = MockProxy.create(TestService.class);
            assertThat(mock).isNotNull();
        }

        @Test
        @DisplayName("Should throw for null interface type")
        void shouldThrowForNullInterfaceType() {
            assertThatNullPointerException()
                .isThrownBy(() -> MockProxy.create((Class<?>) null));
        }

        @Test
        @DisplayName("Should throw for non-interface type")
        void shouldThrowForNonInterfaceType() {
            assertThatThrownBy(() -> MockProxy.create(String.class))
                .isInstanceOf(MockException.class);
        }

        @Test
        @DisplayName("Should create mock for multiple interfaces")
        void shouldCreateMockForMultipleInterfaces() {
            Object mock = MockProxy.create(TestService.class, AnotherService.class);
            assertThat(mock).isNotNull();
            assertThat(mock).isInstanceOf(TestService.class);
            assertThat(mock).isInstanceOf(AnotherService.class);
        }

        @Test
        @DisplayName("Should throw for empty interfaces array")
        void shouldThrowForEmptyInterfacesArray() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> MockProxy.create(new Class<?>[0]));
        }
    }

    @Nested
    @DisplayName("getHandler Tests")
    class GetHandlerTests {

        @Test
        @DisplayName("Should return handler for mock")
        void shouldReturnHandlerForMock() {
            TestService mock = MockProxy.create(TestService.class);
            MockInvocationHandler handler = MockProxy.getHandler(mock);

            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should throw for null mock")
        void shouldThrowForNullMock() {
            assertThatNullPointerException()
                .isThrownBy(() -> MockProxy.getHandler(null));
        }

        @Test
        @DisplayName("Should throw for non-mock object")
        void shouldThrowForNonMockObject() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> MockProxy.getHandler("not a mock"));
        }
    }

    @Nested
    @DisplayName("isMock Tests")
    class IsMockTests {

        @Test
        @DisplayName("Should return true for mock")
        void shouldReturnTrueForMock() {
            TestService mock = MockProxy.create(TestService.class);
            assertThat(MockProxy.isMock(mock)).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-mock")
        void shouldReturnFalseForNonMock() {
            assertThat(MockProxy.isMock("not a mock")).isFalse();
        }

        @Test
        @DisplayName("Should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(MockProxy.isMock(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Invocation Tests")
    class InvocationTests {

        @Test
        @DisplayName("Should record invocations")
        void shouldRecordInvocations() {
            TestService mock = MockProxy.create(TestService.class);

            mock.getName();
            mock.getName();

            List<Invocation> invocations = MockProxy.getInvocations(mock);
            assertThat(invocations).hasSize(2);
        }

        @Test
        @DisplayName("clearInvocations should clear all invocations")
        void clearInvocationsShouldClearAllInvocations() {
            TestService mock = MockProxy.create(TestService.class);
            mock.getName();

            MockProxy.clearInvocations(mock);

            assertThat(MockProxy.getInvocations(mock)).isEmpty();
        }

        @Test
        @DisplayName("reset should clear invocations and stubbing")
        void resetShouldClearInvocationsAndStubbing() {
            TestService mock = MockProxy.create(TestService.class);
            MockInvocationHandler handler = MockProxy.getHandler(mock);
            handler.when("getName").thenReturn("stubbed");
            mock.getName();

            MockProxy.reset(mock);

            assertThat(MockProxy.getInvocations(mock)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Verification Tests")
    class VerificationTests {

        @Test
        @DisplayName("verify should pass when method called expected times")
        void verifyShouldPassWhenMethodCalledExpectedTimes() {
            TestService mock = MockProxy.create(TestService.class);
            mock.getName();

            assertThatNoException().isThrownBy(() ->
                MockProxy.verify(mock).called("getName"));
        }

        @Test
        @DisplayName("verify should fail when method called different times")
        void verifyShouldFailWhenMethodCalledDifferentTimes() {
            TestService mock = MockProxy.create(TestService.class);

            assertThatThrownBy(() -> MockProxy.verify(mock).called("getName"))
                .isInstanceOf(MockException.class);
        }

        @Test
        @DisplayName("verify with times should check exact count")
        void verifyWithTimesShouldCheckExactCount() {
            TestService mock = MockProxy.create(TestService.class);
            mock.getName();
            mock.getName();

            assertThatNoException().isThrownBy(() ->
                MockProxy.verify(mock, 2).called("getName"));
        }

        @Test
        @DisplayName("neverCalled should pass when method not called")
        void neverCalledShouldPassWhenMethodNotCalled() {
            TestService mock = MockProxy.create(TestService.class);

            assertThatNoException().isThrownBy(() ->
                MockProxy.verify(mock).neverCalled("getName"));
        }

        @Test
        @DisplayName("neverCalled should fail when method was called")
        void neverCalledShouldFailWhenMethodWasCalled() {
            TestService mock = MockProxy.create(TestService.class);
            mock.getName();

            assertThatThrownBy(() -> MockProxy.verify(mock).neverCalled("getName"))
                .isInstanceOf(MockException.class);
        }
    }
}
