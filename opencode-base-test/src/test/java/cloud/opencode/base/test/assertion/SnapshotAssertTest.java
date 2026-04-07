package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SnapshotAssertTest Tests
 * SnapshotAssertTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.3
 */
@DisplayName("SnapshotAssert Tests")
class SnapshotAssertTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("First Run (Snapshot Creation) Tests")
    class FirstRunTests {

        @Test
        @DisplayName("should create snapshot file on first run")
        void shouldCreateSnapshotOnFirstRun() {
            String json = """
                {"name": "Alice", "age": 30}""";

            SnapshotAssert.assertMatchesSnapshot(tempDir, "first-run-test", json);

            Path snapshotFile = tempDir.resolve("first-run-test.json");
            assertThat(snapshotFile).exists();
            assertThat(snapshotFile).hasContent(json);
        }

        @Test
        @DisplayName("should create nested directories if needed")
        void shouldCreateNestedDirectories() {
            Path nestedDir = tempDir.resolve("sub/dir");
            String json = """
                {"key": "value"}""";

            SnapshotAssert.assertMatchesSnapshot(nestedDir, "nested-test", json);

            Path snapshotFile = nestedDir.resolve("nested-test.json");
            assertThat(snapshotFile).exists();
        }
    }

    @Nested
    @DisplayName("Subsequent Run (Comparison) Tests")
    class SubsequentRunTests {

        @Test
        @DisplayName("should pass when actual matches snapshot")
        void shouldPassWhenActualMatchesSnapshot() {
            String json = """
                {"name": "Alice"}""";

            // First run - creates snapshot
            SnapshotAssert.assertMatchesSnapshot(tempDir, "match-test", json);

            // Second run - compares against snapshot
            assertThatNoException().isThrownBy(() ->
                SnapshotAssert.assertMatchesSnapshot(tempDir, "match-test", json));
        }

        @Test
        @DisplayName("should pass when only whitespace differs")
        void shouldPassWhenWhitespaceDiffers() {
            String compact = """
                {"name":"Alice","age":30}""";
            String formatted = """
                { "name" : "Alice" , "age" : 30 }""";

            // Create snapshot with compact version
            SnapshotAssert.assertMatchesSnapshot(tempDir, "whitespace-test", compact);

            // Compare with formatted version - whitespace is normalized
            assertThatNoException().isThrownBy(() ->
                SnapshotAssert.assertMatchesSnapshot(tempDir, "whitespace-test", formatted));
        }

        @Test
        @DisplayName("should fail when actual differs from snapshot")
        void shouldFailWhenActualDiffers() {
            String original = """
                {"name": "Alice"}""";
            String different = """
                {"name": "Bob"}""";

            // Create snapshot
            SnapshotAssert.assertMatchesSnapshot(tempDir, "mismatch-test", original);

            // Compare with different value
            assertThatThrownBy(() ->
                SnapshotAssert.assertMatchesSnapshot(tempDir, "mismatch-test", different))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Snapshot mismatch")
                .hasMessageContaining("mismatch-test")
                .hasMessageContaining("update-snapshots");
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("should throw for null snapshot name")
        void shouldThrowForNullSnapshotName() {
            assertThatThrownBy(() ->
                SnapshotAssert.assertMatchesSnapshot(tempDir, null, "{}"))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("snapshotName must not be null or blank");
        }

        @Test
        @DisplayName("should throw for blank snapshot name")
        void shouldThrowForBlankSnapshotName() {
            assertThatThrownBy(() ->
                SnapshotAssert.assertMatchesSnapshot(tempDir, "  ", "{}"))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("snapshotName must not be null or blank");
        }

        @Test
        @DisplayName("should throw for null actual json")
        void shouldThrowForNullActualJson() {
            assertThatThrownBy(() ->
                SnapshotAssert.assertMatchesSnapshot(tempDir, "test", null))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("actualJson must not be null");
        }
    }

    @Nested
    @DisplayName("DeleteSnapshot Tests")
    class DeleteSnapshotTests {

        @Test
        @DisplayName("deleteSnapshot should return true when file exists")
        void deleteSnapshotShouldReturnTrueWhenExists() {
            String json = """
                {"delete": true}""";
            SnapshotAssert.assertMatchesSnapshot(tempDir, "to-delete", json);

            boolean deleted = SnapshotAssert.deleteSnapshot(tempDir, "to-delete");
            assertThat(deleted).isTrue();
            assertThat(tempDir.resolve("to-delete.json")).doesNotExist();
        }

        @Test
        @DisplayName("deleteSnapshot should return false when file does not exist")
        void deleteSnapshotShouldReturnFalseWhenNotExists() {
            boolean deleted = SnapshotAssert.deleteSnapshot(tempDir, "nonexistent");
            assertThat(deleted).isFalse();
        }
    }

    @Nested
    @DisplayName("Snapshot Overwrite Tests")
    class OverwriteTests {

        @Test
        @DisplayName("first-run should overwrite if snapshot does not exist")
        void firstRunShouldCreateNewSnapshot() throws IOException {
            String json1 = """
                {"version": 1}""";
            String json2 = """
                {"version": 2}""";

            // Create snapshot
            SnapshotAssert.assertMatchesSnapshot(tempDir, "overwrite-test", json1);

            // Delete it
            Files.delete(tempDir.resolve("overwrite-test.json"));

            // Run again with different value - should create new snapshot
            SnapshotAssert.assertMatchesSnapshot(tempDir, "overwrite-test", json2);

            String content = Files.readString(tempDir.resolve("overwrite-test.json"), StandardCharsets.UTF_8);
            assertThat(content).isEqualTo(json2);
        }
    }

    @Nested
    @DisplayName("UTF-8 Encoding Tests")
    class EncodingTests {

        @Test
        @DisplayName("should handle UTF-8 content correctly")
        void shouldHandleUtf8Content() {
            String json = """
                {"message": "Hello, \u4e16\u754c!"}""";

            SnapshotAssert.assertMatchesSnapshot(tempDir, "utf8-test", json);

            // Second run should still match
            assertThatNoException().isThrownBy(() ->
                SnapshotAssert.assertMatchesSnapshot(tempDir, "utf8-test", json));
        }
    }

    @Nested
    @DisplayName("Empty JSON Tests")
    class EmptyJsonTests {

        @Test
        @DisplayName("should handle empty JSON object")
        void shouldHandleEmptyJson() {
            SnapshotAssert.assertMatchesSnapshot(tempDir, "empty-test", "{}");

            assertThatNoException().isThrownBy(() ->
                SnapshotAssert.assertMatchesSnapshot(tempDir, "empty-test", "{}"));
        }

        @Test
        @DisplayName("should handle empty string")
        void shouldHandleEmptyString() {
            SnapshotAssert.assertMatchesSnapshot(tempDir, "empty-string-test", "");

            assertThatNoException().isThrownBy(() ->
                SnapshotAssert.assertMatchesSnapshot(tempDir, "empty-string-test", ""));
        }
    }
}
