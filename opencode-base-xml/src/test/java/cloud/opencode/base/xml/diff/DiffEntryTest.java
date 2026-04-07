package cloud.opencode.base.xml.diff;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DiffEntry Tests
 * DiffEntry 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
@DisplayName("DiffEntry Tests")
class DiffEntryTest {

    @Nested
    @DisplayName("Record Accessor Tests")
    class RecordAccessorTests {

        @Test
        @DisplayName("should return correct path")
        void shouldReturnCorrectPath() {
            DiffEntry entry = new DiffEntry("/root/item[0]", DiffType.ADDED, null, "value");

            assertThat(entry.path()).isEqualTo("/root/item[0]");
        }

        @Test
        @DisplayName("should return correct type")
        void shouldReturnCorrectType() {
            DiffEntry entry = new DiffEntry("/root", DiffType.MODIFIED, "old", "new");

            assertThat(entry.type()).isEqualTo(DiffType.MODIFIED);
        }

        @Test
        @DisplayName("should return correct oldValue")
        void shouldReturnCorrectOldValue() {
            DiffEntry entry = new DiffEntry("/root", DiffType.REMOVED, "old", null);

            assertThat(entry.oldValue()).isEqualTo("old");
        }

        @Test
        @DisplayName("should return correct newValue")
        void shouldReturnCorrectNewValue() {
            DiffEntry entry = new DiffEntry("/root", DiffType.ADDED, null, "new");

            assertThat(entry.newValue()).isEqualTo("new");
        }

        @Test
        @DisplayName("should allow null oldValue for ADDED type")
        void shouldAllowNullOldValueForAddedType() {
            DiffEntry entry = new DiffEntry("/root/item[0]", DiffType.ADDED, null, "item");

            assertThat(entry.oldValue()).isNull();
            assertThat(entry.newValue()).isEqualTo("item");
        }

        @Test
        @DisplayName("should allow null newValue for REMOVED type")
        void shouldAllowNullNewValueForRemovedType() {
            DiffEntry entry = new DiffEntry("/root/item[0]", DiffType.REMOVED, "item", null);

            assertThat(entry.oldValue()).isEqualTo("item");
            assertThat(entry.newValue()).isNull();
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("equal entries should be equal")
        void equalEntriesShouldBeEqual() {
            DiffEntry entry1 = new DiffEntry("/root", DiffType.ADDED, null, "v");
            DiffEntry entry2 = new DiffEntry("/root", DiffType.ADDED, null, "v");

            assertThat(entry1).isEqualTo(entry2);
            assertThat(entry1.hashCode()).isEqualTo(entry2.hashCode());
        }

        @Test
        @DisplayName("different entries should not be equal")
        void differentEntriesShouldNotBeEqual() {
            DiffEntry entry1 = new DiffEntry("/root", DiffType.ADDED, null, "v");
            DiffEntry entry2 = new DiffEntry("/root", DiffType.REMOVED, "v", null);

            assertThat(entry1).isNotEqualTo(entry2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain all fields")
        void toStringShouldContainAllFields() {
            DiffEntry entry = new DiffEntry("/root/item[0]", DiffType.TEXT_MODIFIED, "old", "new");
            String str = entry.toString();

            assertThat(str).contains("/root/item[0]");
            assertThat(str).contains("TEXT_MODIFIED");
            assertThat(str).contains("old");
            assertThat(str).contains("new");
        }
    }
}
