package cloud.opencode.base.test.fixture;

import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * TestFixtureTest Tests
 * TestFixtureTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("TestFixture Tests")
class TestFixtureTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create fixture with name and setup")
        void shouldCreateFixtureWithNameAndSetup() {
            TestFixture<String> fixture = new TestFixture<>("test", () -> "data");
            assertThat(fixture.getName()).isEqualTo("test");
        }

        @Test
        @DisplayName("Should create fixture with name, setup, and teardown")
        void shouldCreateFixtureWithNameSetupAndTeardown() {
            AtomicBoolean tornDown = new AtomicBoolean(false);
            TestFixture<String> fixture = new TestFixture<>("test",
                () -> "data",
                data -> tornDown.set(true));

            assertThat(fixture.getName()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create fixture using builder")
        void shouldCreateFixtureUsingBuilder() {
            TestFixture<String> fixture = TestFixture.<String>builder("test")
                .setup(() -> "data")
                .build();

            assertThat(fixture.getName()).isEqualTo("test");
            assertThat(fixture.get()).isEqualTo("data");
        }

        @Test
        @DisplayName("Should create fixture with teardown using builder")
        void shouldCreateFixtureWithTeardownUsingBuilder() {
            AtomicBoolean tornDown = new AtomicBoolean(false);
            TestFixture<String> fixture = TestFixture.<String>builder("test")
                .setup(() -> "data")
                .teardown(data -> tornDown.set(true))
                .build();

            fixture.setUp();
            fixture.tearDown();
            assertThat(tornDown.get()).isTrue();
        }

        @Test
        @DisplayName("Should throw when setup is missing")
        void shouldThrowWhenSetupIsMissing() {
            assertThatIllegalStateException().isThrownBy(() ->
                TestFixture.<String>builder("test").build());
        }
    }

    @Nested
    @DisplayName("setUp Tests")
    class SetUpTests {

        @Test
        @DisplayName("Should call setup function")
        void shouldCallSetupFunction() {
            AtomicInteger counter = new AtomicInteger(0);
            TestFixture<Integer> fixture = new TestFixture<>("test",
                () -> counter.incrementAndGet());

            Integer result = fixture.setUp();
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("Should only call setup once")
        void shouldOnlyCallSetupOnce() {
            AtomicInteger counter = new AtomicInteger(0);
            TestFixture<Integer> fixture = new TestFixture<>("test",
                () -> counter.incrementAndGet());

            fixture.setUp();
            fixture.setUp();
            fixture.setUp();

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return cached data on subsequent calls")
        void shouldReturnCachedDataOnSubsequentCalls() {
            TestFixture<Object> fixture = new TestFixture<>("test", Object::new);

            Object first = fixture.setUp();
            Object second = fixture.setUp();

            assertThat(first).isSameAs(second);
        }
    }

    @Nested
    @DisplayName("get Tests")
    class GetTests {

        @Test
        @DisplayName("get should be alias for setUp")
        void getShouldBeAliasForSetUp() {
            TestFixture<String> fixture = new TestFixture<>("test", () -> "data");
            assertThat(fixture.get()).isEqualTo(fixture.setUp());
        }
    }

    @Nested
    @DisplayName("tearDown Tests")
    class TearDownTests {

        @Test
        @DisplayName("Should call teardown function")
        void shouldCallTeardownFunction() {
            AtomicBoolean tornDown = new AtomicBoolean(false);
            TestFixture<String> fixture = new TestFixture<>("test",
                () -> "data",
                data -> tornDown.set(true));

            fixture.setUp();
            fixture.tearDown();

            assertThat(tornDown.get()).isTrue();
        }

        @Test
        @DisplayName("Should receive data in teardown")
        void shouldReceiveDataInTeardown() {
            String[] capturedData = new String[1];
            TestFixture<String> fixture = new TestFixture<>("test",
                () -> "test-data",
                data -> capturedData[0] = data);

            fixture.setUp();
            fixture.tearDown();

            assertThat(capturedData[0]).isEqualTo("test-data");
        }

        @Test
        @DisplayName("Should not call teardown if not initialized")
        void shouldNotCallTeardownIfNotInitialized() {
            AtomicBoolean tornDown = new AtomicBoolean(false);
            TestFixture<String> fixture = new TestFixture<>("test",
                () -> "data",
                data -> tornDown.set(true));

            fixture.tearDown();

            assertThat(tornDown.get()).isFalse();
        }

        @Test
        @DisplayName("Should reset initialized state")
        void shouldResetInitializedState() {
            AtomicInteger counter = new AtomicInteger(0);
            TestFixture<Integer> fixture = new TestFixture<>("test",
                () -> counter.incrementAndGet());

            fixture.setUp();
            assertThat(fixture.isInitialized()).isTrue();

            fixture.tearDown();
            assertThat(fixture.isInitialized()).isFalse();
        }

        @Test
        @DisplayName("Should allow re-initialization after teardown")
        void shouldAllowReInitializationAfterTeardown() {
            AtomicInteger counter = new AtomicInteger(0);
            TestFixture<Integer> fixture = new TestFixture<>("test",
                () -> counter.incrementAndGet());

            fixture.setUp();
            fixture.tearDown();
            fixture.setUp();

            assertThat(counter.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("isInitialized Tests")
    class IsInitializedTests {

        @Test
        @DisplayName("Should return false before setUp")
        void shouldReturnFalseBeforeSetUp() {
            TestFixture<String> fixture = new TestFixture<>("test", () -> "data");
            assertThat(fixture.isInitialized()).isFalse();
        }

        @Test
        @DisplayName("Should return true after setUp")
        void shouldReturnTrueAfterSetUp() {
            TestFixture<String> fixture = new TestFixture<>("test", () -> "data");
            fixture.setUp();
            assertThat(fixture.isInitialized()).isTrue();
        }

        @Test
        @DisplayName("Should return false after tearDown")
        void shouldReturnFalseAfterTearDown() {
            TestFixture<String> fixture = new TestFixture<>("test", () -> "data");
            fixture.setUp();
            fixture.tearDown();
            assertThat(fixture.isInitialized()).isFalse();
        }
    }

    @Nested
    @DisplayName("reset Tests")
    class ResetTests {

        @Test
        @DisplayName("reset should be alias for tearDown")
        void resetShouldBeAliasForTearDown() {
            AtomicBoolean tornDown = new AtomicBoolean(false);
            TestFixture<String> fixture = new TestFixture<>("test",
                () -> "data",
                data -> tornDown.set(true));

            fixture.setUp();
            fixture.reset();

            assertThat(tornDown.get()).isTrue();
            assertThat(fixture.isInitialized()).isFalse();
        }
    }

    @Nested
    @DisplayName("getName Tests")
    class GetNameTests {

        @Test
        @DisplayName("Should return fixture name")
        void shouldReturnFixtureName() {
            TestFixture<String> fixture = new TestFixture<>("my-fixture", () -> "data");
            assertThat(fixture.getName()).isEqualTo("my-fixture");
        }
    }
}
