package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * RecordAssertTest Tests
 * RecordAssertTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.3
 */
@DisplayName("RecordAssert Tests")
class RecordAssertTest {

    record TestUser(String name, int age, String email) {}

    record EmptyRecord() {}

    record NullableRecord(String value) {}

    private final TestUser alice = new TestUser("Alice", 30, "alice@example.com");

    @Nested
    @DisplayName("Null/NotNull Tests")
    class NullNotNullTests {

        @Test
        @DisplayName("isNull should pass for null record")
        void isNullShouldPassForNull() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat((TestUser) null).isNull());
        }

        @Test
        @DisplayName("isNull should fail for non-null record")
        void isNullShouldFailForNonNull() {
            assertThatThrownBy(() -> RecordAssert.assertThat(alice).isNull())
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected null record");
        }

        @Test
        @DisplayName("isNotNull should pass for non-null record")
        void isNotNullShouldPassForNonNull() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(alice).isNotNull());
        }

        @Test
        @DisplayName("isNotNull should fail for null record")
        void isNotNullShouldFailForNull() {
            assertThatThrownBy(() -> RecordAssert.assertThat((TestUser) null).isNotNull())
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected non-null record");
        }
    }

    @Nested
    @DisplayName("hasComponent Tests")
    class HasComponentTests {

        @Test
        @DisplayName("hasComponent should pass for matching string value")
        void hasComponentShouldPassForMatchingString() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(alice).hasComponent("name", "Alice"));
        }

        @Test
        @DisplayName("hasComponent should pass for matching int value")
        void hasComponentShouldPassForMatchingInt() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(alice).hasComponent("age", 30));
        }

        @Test
        @DisplayName("hasComponent should fail for mismatched value")
        void hasComponentShouldFailForMismatch() {
            assertThatThrownBy(() -> RecordAssert.assertThat(alice).hasComponent("name", "Bob"))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("component 'name'")
                .hasMessageContaining("expected <Bob>")
                .hasMessageContaining("but was <Alice>");
        }

        @Test
        @DisplayName("hasComponent should fail for nonexistent component")
        void hasComponentShouldFailForNonexistentComponent() {
            assertThatThrownBy(() -> RecordAssert.assertThat(alice).hasComponent("address", "x"))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("No component named 'address'");
        }

        @Test
        @DisplayName("hasComponent should fail for null record")
        void hasComponentShouldFailForNullRecord() {
            assertThatThrownBy(() -> RecordAssert.assertThat((TestUser) null).hasComponent("name", "Alice"))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("hasComponent should pass for null expected value matching null component")
        void hasComponentShouldPassForNullMatch() {
            var record = new NullableRecord(null);
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(record).hasComponent("value", null));
        }
    }

    @Nested
    @DisplayName("componentIsNull Tests")
    class ComponentIsNullTests {

        @Test
        @DisplayName("componentIsNull should pass for null component")
        void componentIsNullShouldPassForNull() {
            var record = new NullableRecord(null);
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(record).componentIsNull("value"));
        }

        @Test
        @DisplayName("componentIsNull should fail for non-null component")
        void componentIsNullShouldFailForNonNull() {
            assertThatThrownBy(() -> RecordAssert.assertThat(alice).componentIsNull("name"))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("expected null");
        }
    }

    @Nested
    @DisplayName("componentIsNotNull Tests")
    class ComponentIsNotNullTests {

        @Test
        @DisplayName("componentIsNotNull should pass for non-null component")
        void componentIsNotNullShouldPassForNonNull() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(alice).componentIsNotNull("name"));
        }

        @Test
        @DisplayName("componentIsNotNull should fail for null component")
        void componentIsNotNullShouldFailForNull() {
            var record = new NullableRecord(null);
            assertThatThrownBy(() -> RecordAssert.assertThat(record).componentIsNotNull("value"))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("expected non-null");
        }
    }

    @Nested
    @DisplayName("componentIsInstanceOf Tests")
    class ComponentIsInstanceOfTests {

        @Test
        @DisplayName("componentIsInstanceOf should pass for correct type")
        void componentIsInstanceOfShouldPassForCorrectType() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(alice).componentIsInstanceOf("name", String.class));
        }

        @Test
        @DisplayName("componentIsInstanceOf should fail for wrong type")
        void componentIsInstanceOfShouldFailForWrongType() {
            assertThatThrownBy(() ->
                RecordAssert.assertThat(alice).componentIsInstanceOf("name", Integer.class))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("expected instance of java.lang.Integer")
                .hasMessageContaining("but was java.lang.String");
        }

        @Test
        @DisplayName("componentIsInstanceOf should fail for null component")
        void componentIsInstanceOfShouldFailForNullComponent() {
            var record = new NullableRecord(null);
            assertThatThrownBy(() ->
                RecordAssert.assertThat(record).componentIsInstanceOf("value", String.class))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("but was null");
        }

        @Test
        @DisplayName("componentIsInstanceOf should pass for supertype")
        void componentIsInstanceOfShouldPassForSupertype() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(alice).componentIsInstanceOf("name", CharSequence.class));
        }
    }

    @Nested
    @DisplayName("hasComponentCount Tests")
    class HasComponentCountTests {

        @Test
        @DisplayName("hasComponentCount should pass for correct count")
        void hasComponentCountShouldPassForCorrectCount() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(alice).hasComponentCount(3));
        }

        @Test
        @DisplayName("hasComponentCount should fail for wrong count")
        void hasComponentCountShouldFailForWrongCount() {
            assertThatThrownBy(() -> RecordAssert.assertThat(alice).hasComponentCount(2))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected 2 components but had 3");
        }

        @Test
        @DisplayName("hasComponentCount should pass for empty record")
        void hasComponentCountShouldPassForEmptyRecord() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(new EmptyRecord()).hasComponentCount(0));
        }
    }

    @Nested
    @DisplayName("hasComponentNamed Tests")
    class HasComponentNamedTests {

        @Test
        @DisplayName("hasComponentNamed should pass for existing component")
        void hasComponentNamedShouldPassForExisting() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(alice).hasComponentNamed("name"));
        }

        @Test
        @DisplayName("hasComponentNamed should fail for nonexistent component")
        void hasComponentNamedShouldFailForNonexistent() {
            assertThatThrownBy(() -> RecordAssert.assertThat(alice).hasComponentNamed("address"))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected component named 'address'");
        }
    }

    @Nested
    @DisplayName("isEqualTo Tests")
    class IsEqualToTests {

        @Test
        @DisplayName("isEqualTo should pass for equal records")
        void isEqualToShouldPassForEqualRecords() {
            var other = new TestUser("Alice", 30, "alice@example.com");
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(alice).isEqualTo(other));
        }

        @Test
        @DisplayName("isEqualTo should fail for different records")
        void isEqualToShouldFailForDifferentRecords() {
            var other = new TestUser("Bob", 25, "bob@example.com");
            assertThatThrownBy(() -> RecordAssert.assertThat(alice).isEqualTo(other))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isEqualTo should pass for both null")
        void isEqualToShouldPassForBothNull() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat((TestUser) null).isEqualTo(null));
        }
    }

    @Nested
    @DisplayName("Fluent Chaining Tests")
    class FluentChainingTests {

        @Test
        @DisplayName("should support fluent chaining of multiple assertions")
        void shouldSupportFluentChaining() {
            assertThatNoException().isThrownBy(() ->
                RecordAssert.assertThat(alice)
                    .isNotNull()
                    .hasComponentCount(3)
                    .hasComponentNamed("name")
                    .hasComponentNamed("age")
                    .hasComponent("name", "Alice")
                    .hasComponent("age", 30)
                    .hasComponent("email", "alice@example.com")
                    .componentIsNotNull("name")
                    .componentIsInstanceOf("name", String.class)
                    .isEqualTo(new TestUser("Alice", 30, "alice@example.com")));
        }
    }
}
