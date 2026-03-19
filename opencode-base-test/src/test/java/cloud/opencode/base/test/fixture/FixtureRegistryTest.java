package cloud.opencode.base.test.fixture;

import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * FixtureRegistryTest Tests
 * FixtureRegistryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("FixtureRegistry Tests")
class FixtureRegistryTest {

    @BeforeEach
    void setUp() {
        FixtureRegistry.clear();
    }

    @AfterEach
    void tearDown() {
        FixtureRegistry.clear();
    }

    @Nested
    @DisplayName("register(TestFixture) Tests")
    class RegisterFixtureTests {

        @Test
        @DisplayName("Should register a fixture")
        void shouldRegisterAFixture() {
            TestFixture<String> fixture = new TestFixture<>("test", () -> "data");
            FixtureRegistry.register(fixture);

            assertThat(FixtureRegistry.exists("test")).isTrue();
        }

        @Test
        @DisplayName("Should overwrite existing fixture with same name")
        void shouldOverwriteExistingFixtureWithSameName() {
            TestFixture<String> fixture1 = new TestFixture<>("test", () -> "data1");
            TestFixture<String> fixture2 = new TestFixture<>("test", () -> "data2");

            FixtureRegistry.register(fixture1);
            FixtureRegistry.register(fixture2);

            assertThat(FixtureRegistry.<String>getData("test")).isEqualTo("data2");
        }
    }

    @Nested
    @DisplayName("register(name, supplier) Tests")
    class RegisterSimpleTests {

        @Test
        @DisplayName("Should register a simple fixture")
        void shouldRegisterASimpleFixture() {
            FixtureRegistry.register("test", () -> "data");

            assertThat(FixtureRegistry.exists("test")).isTrue();
            assertThat(FixtureRegistry.<String>getData("test")).isEqualTo("data");
        }
    }

    @Nested
    @DisplayName("get Tests")
    class GetTests {

        @Test
        @DisplayName("Should return fixture by name")
        void shouldReturnFixtureByName() {
            TestFixture<String> fixture = new TestFixture<>("test", () -> "data");
            FixtureRegistry.register(fixture);

            TestFixture<String> retrieved = FixtureRegistry.get("test");
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo("test");
        }

        @Test
        @DisplayName("Should return null for non-existent fixture")
        void shouldReturnNullForNonExistentFixture() {
            TestFixture<String> retrieved = FixtureRegistry.get("non-existent");
            assertThat(retrieved).isNull();
        }
    }

    @Nested
    @DisplayName("getData Tests")
    class GetDataTests {

        @Test
        @DisplayName("Should return fixture data")
        void shouldReturnFixtureData() {
            FixtureRegistry.register("test", () -> "data");

            String data = FixtureRegistry.getData("test");
            assertThat(data).isEqualTo("data");
        }

        @Test
        @DisplayName("Should return null for non-existent fixture")
        void shouldReturnNullForNonExistentFixture() {
            String data = FixtureRegistry.getData("non-existent");
            assertThat(data).isNull();
        }

        @Test
        @DisplayName("Should initialize fixture on getData")
        void shouldInitializeFixtureOnGetData() {
            AtomicBoolean initialized = new AtomicBoolean(false);
            FixtureRegistry.register("test", () -> {
                initialized.set(true);
                return "data";
            });

            FixtureRegistry.getData("test");
            assertThat(initialized.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("exists Tests")
    class ExistsTests {

        @Test
        @DisplayName("Should return true for existing fixture")
        void shouldReturnTrueForExistingFixture() {
            FixtureRegistry.register("test", () -> "data");
            assertThat(FixtureRegistry.exists("test")).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-existent fixture")
        void shouldReturnFalseForNonExistentFixture() {
            assertThat(FixtureRegistry.exists("non-existent")).isFalse();
        }
    }

    @Nested
    @DisplayName("unregister Tests")
    class UnregisterTests {

        @Test
        @DisplayName("Should unregister fixture")
        void shouldUnregisterFixture() {
            FixtureRegistry.register("test", () -> "data");
            FixtureRegistry.unregister("test");

            assertThat(FixtureRegistry.exists("test")).isFalse();
        }

        @Test
        @DisplayName("Should call tearDown on unregister")
        void shouldCallTearDownOnUnregister() {
            AtomicBoolean tornDown = new AtomicBoolean(false);
            TestFixture<String> fixture = new TestFixture<>("test",
                () -> "data",
                data -> tornDown.set(true));

            FixtureRegistry.register(fixture);
            fixture.setUp(); // Initialize first
            FixtureRegistry.unregister("test");

            assertThat(tornDown.get()).isTrue();
        }

        @Test
        @DisplayName("Should handle unregistering non-existent fixture")
        void shouldHandleUnregisteringNonExistentFixture() {
            assertThatCode(() -> FixtureRegistry.unregister("non-existent"))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("resetAll Tests")
    class ResetAllTests {

        @Test
        @DisplayName("Should reset all fixtures")
        void shouldResetAllFixtures() {
            AtomicInteger counter1 = new AtomicInteger(0);
            AtomicInteger counter2 = new AtomicInteger(0);

            FixtureRegistry.register("test1", counter1::incrementAndGet);
            FixtureRegistry.register("test2", counter2::incrementAndGet);

            FixtureRegistry.getData("test1");
            FixtureRegistry.getData("test2");
            FixtureRegistry.resetAll();
            FixtureRegistry.getData("test1");
            FixtureRegistry.getData("test2");

            assertThat(counter1.get()).isEqualTo(2);
            assertThat(counter2.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("tearDownAll Tests")
    class TearDownAllTests {

        @Test
        @DisplayName("Should tear down all fixtures")
        void shouldTearDownAllFixtures() {
            AtomicBoolean tornDown1 = new AtomicBoolean(false);
            AtomicBoolean tornDown2 = new AtomicBoolean(false);

            TestFixture<String> fixture1 = new TestFixture<>("test1",
                () -> "data1",
                data -> tornDown1.set(true));
            TestFixture<String> fixture2 = new TestFixture<>("test2",
                () -> "data2",
                data -> tornDown2.set(true));

            FixtureRegistry.register(fixture1);
            FixtureRegistry.register(fixture2);
            fixture1.setUp();
            fixture2.setUp();

            FixtureRegistry.tearDownAll();

            assertThat(tornDown1.get()).isTrue();
            assertThat(tornDown2.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("clear Tests")
    class ClearTests {

        @Test
        @DisplayName("Should clear all fixtures")
        void shouldClearAllFixtures() {
            FixtureRegistry.register("test1", () -> "data1");
            FixtureRegistry.register("test2", () -> "data2");

            FixtureRegistry.clear();

            assertThat(FixtureRegistry.size()).isZero();
            assertThat(FixtureRegistry.exists("test1")).isFalse();
            assertThat(FixtureRegistry.exists("test2")).isFalse();
        }

        @Test
        @DisplayName("Should call tearDown on clear")
        void shouldCallTearDownOnClear() {
            AtomicBoolean tornDown = new AtomicBoolean(false);
            TestFixture<String> fixture = new TestFixture<>("test",
                () -> "data",
                data -> tornDown.set(true));

            FixtureRegistry.register(fixture);
            fixture.setUp();
            FixtureRegistry.clear();

            assertThat(tornDown.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("size Tests")
    class SizeTests {

        @Test
        @DisplayName("Should return correct size")
        void shouldReturnCorrectSize() {
            assertThat(FixtureRegistry.size()).isZero();

            FixtureRegistry.register("test1", () -> "data1");
            assertThat(FixtureRegistry.size()).isEqualTo(1);

            FixtureRegistry.register("test2", () -> "data2");
            assertThat(FixtureRegistry.size()).isEqualTo(2);

            FixtureRegistry.unregister("test1");
            assertThat(FixtureRegistry.size()).isEqualTo(1);
        }
    }
}
