package cloud.opencode.base.yml.diff;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DiffEntry")
class DiffEntryTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("added() creates entry with ADDED type and null oldValue")
        void addedCreatesCorrectEntry() {
            DiffEntry entry = DiffEntry.added("server.port", 8080);

            assertThat(entry.type()).isEqualTo(DiffType.ADDED);
            assertThat(entry.path()).isEqualTo("server.port");
            assertThat(entry.oldValue()).isNull();
            assertThat(entry.newValue()).isEqualTo(8080);
        }

        @Test
        @DisplayName("removed() creates entry with REMOVED type and null newValue")
        void removedCreatesCorrectEntry() {
            DiffEntry entry = DiffEntry.removed("logging.level", "DEBUG");

            assertThat(entry.type()).isEqualTo(DiffType.REMOVED);
            assertThat(entry.path()).isEqualTo("logging.level");
            assertThat(entry.oldValue()).isEqualTo("DEBUG");
            assertThat(entry.newValue()).isNull();
        }

        @Test
        @DisplayName("modified() creates entry with MODIFIED type and both values")
        void modifiedCreatesCorrectEntry() {
            DiffEntry entry = DiffEntry.modified("app.name", "old-app", "new-app");

            assertThat(entry.type()).isEqualTo(DiffType.MODIFIED);
            assertThat(entry.path()).isEqualTo("app.name");
            assertThat(entry.oldValue()).isEqualTo("old-app");
            assertThat(entry.newValue()).isEqualTo("new-app");
        }

        @Test
        @DisplayName("added() with null value")
        void addedWithNullValue() {
            DiffEntry entry = DiffEntry.added("key", null);

            assertThat(entry.type()).isEqualTo(DiffType.ADDED);
            assertThat(entry.newValue()).isNull();
        }

        @Test
        @DisplayName("removed() with null value")
        void removedWithNullValue() {
            DiffEntry entry = DiffEntry.removed("key", null);

            assertThat(entry.type()).isEqualTo(DiffType.REMOVED);
            assertThat(entry.oldValue()).isNull();
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("null type throws NullPointerException")
        void nullTypeThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DiffEntry(null, "path", null, null))
                    .withMessageContaining("type");
        }

        @Test
        @DisplayName("null path throws NullPointerException")
        void nullPathThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DiffEntry(DiffType.ADDED, null, null, null))
                    .withMessageContaining("path");
        }
    }

    @Nested
    @DisplayName("Record Behavior")
    class RecordBehavior {

        @Test
        @DisplayName("equals and hashCode work correctly")
        void equalsAndHashCode() {
            DiffEntry a = DiffEntry.added("key", "value");
            DiffEntry b = DiffEntry.added("key", "value");
            DiffEntry c = DiffEntry.removed("key", "value");

            assertThat(a).isEqualTo(b);
            assertThat(a).hasSameHashCodeAs(b);
            assertThat(a).isNotEqualTo(c);
        }

        @Test
        @DisplayName("toString contains all fields")
        void toStringContainsFields() {
            DiffEntry entry = DiffEntry.modified("a.b", 1, 2);
            String str = entry.toString();

            assertThat(str).contains("MODIFIED", "a.b", "1", "2");
        }
    }
}
