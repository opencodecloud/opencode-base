package cloud.opencode.base.string.diff;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DiffLineTest Tests
 * DiffLineTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("DiffLine Tests")
class DiffLineTest {

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("Should create DiffLine with all properties")
        void shouldCreateDiffLineWithAllProperties() {
            DiffLine diffLine = new DiffLine(DiffLine.Type.INSERT, 1, 2, "content");

            assertThat(diffLine.type()).isEqualTo(DiffLine.Type.INSERT);
            assertThat(diffLine.originalLine()).isEqualTo(1);
            assertThat(diffLine.revisedLine()).isEqualTo(2);
            assertThat(diffLine.content()).isEqualTo("content");
        }

        @Test
        @DisplayName("Should implement equals and hashCode")
        void shouldImplementEqualsAndHashCode() {
            DiffLine line1 = new DiffLine(DiffLine.Type.EQUAL, 1, 1, "test");
            DiffLine line2 = new DiffLine(DiffLine.Type.EQUAL, 1, 1, "test");

            assertThat(line1).isEqualTo(line2);
            assertThat(line1.hashCode()).isEqualTo(line2.hashCode());
        }

        @Test
        @DisplayName("Should implement toString")
        void shouldImplementToString() {
            DiffLine diffLine = new DiffLine(DiffLine.Type.DELETE, 5, -1, "deleted");

            assertThat(diffLine.toString()).contains("DELETE", "5", "-1", "deleted");
        }
    }

    @Nested
    @DisplayName("Type Enum Tests")
    class TypeEnumTests {

        @Test
        @DisplayName("Should have all expected types")
        void shouldHaveAllExpectedTypes() {
            assertThat(DiffLine.Type.values()).hasSize(4);
            assertThat(DiffLine.Type.values()).contains(
                DiffLine.Type.EQUAL,
                DiffLine.Type.INSERT,
                DiffLine.Type.DELETE,
                DiffLine.Type.MODIFY
            );
        }

        @Test
        @DisplayName("valueOf should work for all types")
        void valueOfShouldWorkForAllTypes() {
            assertThat(DiffLine.Type.valueOf("EQUAL")).isEqualTo(DiffLine.Type.EQUAL);
            assertThat(DiffLine.Type.valueOf("INSERT")).isEqualTo(DiffLine.Type.INSERT);
            assertThat(DiffLine.Type.valueOf("DELETE")).isEqualTo(DiffLine.Type.DELETE);
            assertThat(DiffLine.Type.valueOf("MODIFY")).isEqualTo(DiffLine.Type.MODIFY);
        }
    }
}
