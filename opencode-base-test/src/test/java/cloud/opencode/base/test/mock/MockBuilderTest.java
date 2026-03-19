package cloud.opencode.base.test.mock;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MockBuilderTest Tests
 * MockBuilderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("MockBuilder Tests")
class MockBuilderTest {

    interface TestService {
        String getName();
        int getValue();
        String greet(String name);
    }

    @Nested
    @DisplayName("of() Factory Method Tests")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("Should create builder for interface")
        void shouldCreateBuilderForInterface() {
            MockBuilder<TestService> builder = MockBuilder.of(TestService.class);
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("Should throw for non-interface type")
        void shouldThrowForNonInterfaceType() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> MockBuilder.of(String.class))
                .withMessageContaining("only supports interfaces");
        }
    }

    @Nested
    @DisplayName("when() with Return Value Tests")
    class WhenWithReturnValueTests {

        @Test
        @DisplayName("Should return stubbed value")
        void shouldReturnStubbedValue() {
            TestService mock = MockBuilder.of(TestService.class)
                .when("getName", "John")
                .build();

            assertThat(mock.getName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should support multiple stubs")
        void shouldSupportMultipleStubs() {
            TestService mock = MockBuilder.of(TestService.class)
                .when("getName", "John")
                .when("getValue", 42)
                .build();

            assertThat(mock.getName()).isEqualTo("John");
            assertThat(mock.getValue()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("when() with Handler Tests")
    class WhenWithHandlerTests {

        @Test
        @DisplayName("Should execute handler function")
        void shouldExecuteHandlerFunction() {
            TestService mock = MockBuilder.of(TestService.class)
                .when("greet", args -> "Hello, " + args[0] + "!")
                .build();

            assertThat(mock.greet("World")).isEqualTo("Hello, World!");
        }
    }

    @Nested
    @DisplayName("defaultReturn() Tests")
    class DefaultReturnTests {

        @Test
        @DisplayName("Should return default value for non-stubbed methods")
        void shouldReturnDefaultValueForNonStubbedMethods() {
            TestService mock = MockBuilder.of(TestService.class)
                .defaultReturn("default")
                .build();

            assertThat(mock.getName()).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("defaultHandler() Tests")
    class DefaultHandlerTests {

        @Test
        @DisplayName("Should use default handler for non-stubbed methods")
        void shouldUseDefaultHandlerForNonStubbedMethods() {
            TestService mock = MockBuilder.of(TestService.class)
                .defaultHandler(method -> method.getName().toUpperCase())
                .build();

            assertThat(mock.getName()).isEqualTo("GETNAME");
        }
    }

    @Nested
    @DisplayName("build() Tests")
    class BuildTests {

        @Test
        @DisplayName("Should build mock instance")
        void shouldBuildMockInstance() {
            TestService mock = MockBuilder.of(TestService.class).build();
            assertThat(mock).isNotNull();
        }

        @Test
        @DisplayName("Mock toString should return mock identifier")
        void mockToStringShouldReturnMockIdentifier() {
            TestService mock = MockBuilder.of(TestService.class).build();
            assertThat(mock.toString()).isEqualTo("Mock[TestService]");
        }

        @Test
        @DisplayName("Mock hashCode should return identity hash code")
        void mockHashCodeShouldReturnIdentityHashCode() {
            TestService mock = MockBuilder.of(TestService.class).build();
            assertThat(mock.hashCode()).isEqualTo(System.identityHashCode(mock));
        }

        @Test
        @DisplayName("Mock equals should use identity comparison")
        void mockEqualsShouldUseIdentityComparison() {
            TestService mock1 = MockBuilder.of(TestService.class).build();
            TestService mock2 = MockBuilder.of(TestService.class).build();

            assertThat(mock1.equals(mock1)).isTrue();
            assertThat(mock1.equals(mock2)).isFalse();
        }
    }

    @Nested
    @DisplayName("mock() Static Method Tests")
    class MockStaticMethodTests {

        @Test
        @DisplayName("Should create mock with default values")
        void shouldCreateMockWithDefaultValues() {
            TestService mock = MockBuilder.mock(TestService.class);

            assertThat(mock).isNotNull();
            assertThat(mock.getName()).isNull();
        }
    }
}
