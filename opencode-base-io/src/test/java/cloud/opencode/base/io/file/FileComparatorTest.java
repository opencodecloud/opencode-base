package cloud.opencode.base.io.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.*;

/**
 * FileComparator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("FileComparator 测试")
class FileComparatorTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("contentEquals方法测试")
    class ContentEqualsTests {

        @Test
        @DisplayName("相同内容返回true")
        void testContentEqualsTrue() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "same content");
            Files.writeString(file2, "same content");

            assertThat(FileComparator.contentEquals(file1, file2)).isTrue();
        }

        @Test
        @DisplayName("不同内容返回false")
        void testContentEqualsFalse() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "content1");
            Files.writeString(file2, "content2");

            assertThat(FileComparator.contentEquals(file1, file2)).isFalse();
        }

        @Test
        @DisplayName("相同文件返回true")
        void testContentEqualsSameFile() throws Exception {
            Path file = tempDir.resolve("same.txt");
            Files.writeString(file, "content");

            assertThat(FileComparator.contentEquals(file, file)).isTrue();
        }

        @Test
        @DisplayName("不同大小快速返回false")
        void testContentEqualsDifferentSize() throws Exception {
            Path file1 = tempDir.resolve("small.txt");
            Path file2 = tempDir.resolve("large.txt");
            Files.writeString(file1, "small");
            Files.writeString(file2, "much larger content");

            assertThat(FileComparator.contentEquals(file1, file2)).isFalse();
        }

        @Test
        @DisplayName("空文件相等")
        void testContentEqualsEmptyFiles() throws Exception {
            Path file1 = tempDir.resolve("empty1.txt");
            Path file2 = tempDir.resolve("empty2.txt");
            Files.createFile(file1);
            Files.createFile(file2);

            assertThat(FileComparator.contentEquals(file1, file2)).isTrue();
        }

        @Test
        @DisplayName("两个不存在的文件相等")
        void testContentEqualsBothNotExist() {
            Path file1 = tempDir.resolve("notexist1.txt");
            Path file2 = tempDir.resolve("notexist2.txt");

            assertThat(FileComparator.contentEquals(file1, file2)).isTrue();
        }

        @Test
        @DisplayName("大文件比较(使用内存映射)")
        void testContentEqualsLargeFiles() throws Exception {
            Path file1 = tempDir.resolve("large1.bin");
            Path file2 = tempDir.resolve("large2.bin");
            byte[] data = new byte[100000];
            Files.write(file1, data);
            Files.write(file2, data);

            assertThat(FileComparator.contentEquals(file1, file2)).isTrue();
        }
    }

    @Nested
    @DisplayName("hashEquals方法测试")
    class HashEqualsTests {

        @Test
        @DisplayName("相同文件哈希相等")
        void testHashEqualsTrue() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "same content");
            Files.writeString(file2, "same content");

            assertThat(FileComparator.hashEquals(file1, file2, "SHA-256")).isTrue();
        }

        @Test
        @DisplayName("不同文件哈希不等")
        void testHashEqualsFalse() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "content1");
            Files.writeString(file2, "content2");

            assertThat(FileComparator.hashEquals(file1, file2, "SHA-256")).isFalse();
        }

        @Test
        @DisplayName("使用MD5算法")
        void testHashEqualsMD5() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "same");
            Files.writeString(file2, "same");

            assertThat(FileComparator.hashEquals(file1, file2, "MD5")).isTrue();
        }
    }

    @Nested
    @DisplayName("computeHash方法测试")
    class ComputeHashTests {

        @Test
        @DisplayName("计算SHA-256哈希")
        void testComputeHashSha256() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello");

            byte[] hash = FileComparator.computeHash(file, "SHA-256");

            assertThat(hash).hasSize(32);
        }

        @Test
        @DisplayName("计算MD5哈希")
        void testComputeHashMd5() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello");

            byte[] hash = FileComparator.computeHash(file, "MD5");

            assertThat(hash).hasSize(16);
        }

        @Test
        @DisplayName("不支持的算法抛出异常")
        void testComputeHashInvalidAlgorithm() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);

            assertThatThrownBy(() -> FileComparator.computeHash(file, "INVALID"))
                .hasMessageContaining("Hash algorithm not found");
        }
    }

    @Nested
    @DisplayName("computeHashHex方法测试")
    class ComputeHashHexTests {

        @Test
        @DisplayName("返回十六进制字符串")
        void testComputeHashHex() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello");

            String hex = FileComparator.computeHashHex(file, "SHA-256");

            assertThat(hex).hasSize(64);
            assertThat(hex).matches("[0-9a-f]+");
        }
    }

    @Nested
    @DisplayName("linesEqual方法测试")
    class LinesEqualTests {

        @Test
        @DisplayName("相同行返回true")
        void testLinesEqualTrue() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "line1\nline2\nline3");
            Files.writeString(file2, "line1\nline2\nline3");

            assertThat(FileComparator.linesEqual(file1, file2)).isTrue();
        }

        @Test
        @DisplayName("不同行返回false")
        void testLinesEqualFalse() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "line1\nline2");
            Files.writeString(file2, "line1\nline3");

            assertThat(FileComparator.linesEqual(file1, file2)).isFalse();
        }

        @Test
        @DisplayName("行数不同返回false")
        void testLinesEqualDifferentLineCount() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "line1\nline2");
            Files.writeString(file2, "line1\nline2\nline3");

            assertThat(FileComparator.linesEqual(file1, file2)).isFalse();
        }

        @Test
        @DisplayName("使用指定字符集")
        void testLinesEqualWithCharset() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "第一行\n第二行", StandardCharsets.UTF_8);
            Files.writeString(file2, "第一行\n第二行", StandardCharsets.UTF_8);

            assertThat(FileComparator.linesEqual(file1, file2, StandardCharsets.UTF_8)).isTrue();
        }
    }

    @Nested
    @DisplayName("diffLines方法测试")
    class DiffLinesTests {

        @Test
        @DisplayName("相同文件无差异")
        void testDiffLinesNoDiff() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "line1\nline2");
            Files.writeString(file2, "line1\nline2");

            FileComparator.LineDiff diff = FileComparator.diffLines(file1, file2);

            assertThat(diff.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("检测添加的行")
        void testDiffLinesAdded() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "line1");
            Files.writeString(file2, "line1\nline2");

            FileComparator.LineDiff diff = FileComparator.diffLines(file1, file2);

            assertThat(diff.added()).hasSize(1);
            assertThat(diff.added().get(0).content()).isEqualTo("line2");
        }

        @Test
        @DisplayName("检测删除的行")
        void testDiffLinesRemoved() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "line1\nline2");
            Files.writeString(file2, "line1");

            FileComparator.LineDiff diff = FileComparator.diffLines(file1, file2);

            assertThat(diff.removed()).hasSize(1);
            assertThat(diff.removed().get(0).content()).isEqualTo("line2");
        }

        @Test
        @DisplayName("检测修改的行")
        void testDiffLinesChanged() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "old content");
            Files.writeString(file2, "new content");

            FileComparator.LineDiff diff = FileComparator.diffLines(file1, file2);

            assertThat(diff.changed()).hasSize(1);
            assertThat(diff.changed().get(0).oldContent()).isEqualTo("old content");
            assertThat(diff.changed().get(0).newContent()).isEqualTo("new content");
        }

        @Test
        @DisplayName("toUnifiedDiff方法")
        void testDiffLinesToUnifiedDiff() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "old");
            Files.writeString(file2, "new");

            FileComparator.LineDiff diff = FileComparator.diffLines(file1, file2);
            String unified = diff.toUnifiedDiff();

            assertThat(unified).contains("old").contains("new");
        }
    }

    @Nested
    @DisplayName("compareDirectories方法测试")
    class CompareDirectoriesTests {

        @Test
        @DisplayName("相同目录返回isIdentical true")
        void testCompareDirectoriesIdentical() throws Exception {
            Path dir1 = tempDir.resolve("dir1");
            Path dir2 = tempDir.resolve("dir2");
            Files.createDirectories(dir1);
            Files.createDirectories(dir2);
            Files.writeString(dir1.resolve("file.txt"), "content");
            Files.writeString(dir2.resolve("file.txt"), "content");

            FileComparator.DirectoryDiff diff = FileComparator.compareDirectories(dir1, dir2);

            assertThat(diff.isIdentical()).isTrue();
        }

        @Test
        @DisplayName("检测仅在第一个目录中的文件")
        void testCompareDirectoriesOnlyInFirst() throws Exception {
            Path dir1 = tempDir.resolve("dir1");
            Path dir2 = tempDir.resolve("dir2");
            Files.createDirectories(dir1);
            Files.createDirectories(dir2);
            Files.createFile(dir1.resolve("only1.txt"));

            FileComparator.DirectoryDiff diff = FileComparator.compareDirectories(dir1, dir2);

            assertThat(diff.onlyInFirst()).hasSize(1);
        }

        @Test
        @DisplayName("检测仅在第二个目录中的文件")
        void testCompareDirectoriesOnlyInSecond() throws Exception {
            Path dir1 = tempDir.resolve("dir1");
            Path dir2 = tempDir.resolve("dir2");
            Files.createDirectories(dir1);
            Files.createDirectories(dir2);
            Files.createFile(dir2.resolve("only2.txt"));

            FileComparator.DirectoryDiff diff = FileComparator.compareDirectories(dir1, dir2);

            assertThat(diff.onlyInSecond()).hasSize(1);
        }

        @Test
        @DisplayName("检测内容不同的文件")
        void testCompareDirectoriesDifferent() throws Exception {
            Path dir1 = tempDir.resolve("dir1");
            Path dir2 = tempDir.resolve("dir2");
            Files.createDirectories(dir1);
            Files.createDirectories(dir2);
            Files.writeString(dir1.resolve("file.txt"), "content1");
            Files.writeString(dir2.resolve("file.txt"), "content2");

            FileComparator.DirectoryDiff diff = FileComparator.compareDirectories(dir1, dir2);

            assertThat(diff.different()).hasSize(1);
        }

        @Test
        @DisplayName("使用过滤器")
        void testCompareDirectoriesWithFilter() throws Exception {
            Path dir1 = tempDir.resolve("dir1");
            Path dir2 = tempDir.resolve("dir2");
            Files.createDirectories(dir1);
            Files.createDirectories(dir2);
            Files.createFile(dir1.resolve("keep.txt"));
            Files.createFile(dir1.resolve("ignore.tmp"));

            FileComparator.DirectoryDiff diff = FileComparator.compareDirectories(
                dir1, dir2, p -> p.toString().endsWith(".txt"));

            assertThat(diff.onlyInFirst()).hasSize(1);
        }

        @Test
        @DisplayName("differenceCount方法")
        void testDirectoryDiffCount() throws Exception {
            Path dir1 = tempDir.resolve("dir1");
            Path dir2 = tempDir.resolve("dir2");
            Files.createDirectories(dir1);
            Files.createDirectories(dir2);
            Files.createFile(dir1.resolve("a.txt"));
            Files.createFile(dir2.resolve("b.txt"));

            FileComparator.DirectoryDiff diff = FileComparator.compareDirectories(dir1, dir2);

            assertThat(diff.differenceCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("LineEntry记录测试")
    class LineEntryTests {

        @Test
        @DisplayName("记录字段访问")
        void testLineEntryFields() {
            FileComparator.LineEntry entry = new FileComparator.LineEntry(5, "content");

            assertThat(entry.lineNumber()).isEqualTo(5);
            assertThat(entry.content()).isEqualTo("content");
        }
    }

    @Nested
    @DisplayName("LineChange记录测试")
    class LineChangeTests {

        @Test
        @DisplayName("记录字段访问")
        void testLineChangeFields() {
            FileComparator.LineChange change = new FileComparator.LineChange(3, "old", "new");

            assertThat(change.lineNumber()).isEqualTo(3);
            assertThat(change.oldContent()).isEqualTo("old");
            assertThat(change.newContent()).isEqualTo("new");
        }
    }
}
