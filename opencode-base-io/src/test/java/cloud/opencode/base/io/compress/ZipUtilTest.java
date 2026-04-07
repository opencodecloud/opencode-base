package cloud.opencode.base.io.compress;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * ZipUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
@DisplayName("ZipUtil 测试")
class ZipUtilTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("zip/unzip 单文件测试")
    class SingleFileTests {

        @Test
        @DisplayName("压缩和解压缩单个文件")
        void testZipUnzipSingleFile() throws Exception {
            Path source = tempDir.resolve("hello.txt");
            Files.writeString(source, "Hello, Zip!");

            Path zipFile = tempDir.resolve("output.zip");
            ZipUtil.zip(source, zipFile);
            assertThat(zipFile).exists();

            Path outputDir = tempDir.resolve("extracted");
            ZipUtil.unzip(zipFile, outputDir);

            Path extractedFile = outputDir.resolve("hello.txt");
            assertThat(extractedFile).exists();
            assertThat(Files.readString(extractedFile)).isEqualTo("Hello, Zip!");
        }

        @Test
        @DisplayName("null源路径抛出NullPointerException")
        void testNullSource() {
            assertThatThrownBy(() -> ZipUtil.zip((Path) null, tempDir.resolve("out.zip")))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null目标路径抛出NullPointerException")
        void testNullTarget() {
            assertThatThrownBy(() -> ZipUtil.zip(tempDir.resolve("in.txt"), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("不存在的源文件抛出OpenIOOperationException")
        void testNonExistentSource() {
            Path nonExistent = tempDir.resolve("nonexistent.txt");
            Path target = tempDir.resolve("out.zip");

            assertThatThrownBy(() -> ZipUtil.zip(nonExistent, target))
                    .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("zip目录测试")
    class DirectoryTests {

        @Test
        @DisplayName("压缩和解压缩目录")
        void testZipUnzipDirectory() throws Exception {
            // Create directory structure
            Path dir = tempDir.resolve("mydir");
            Files.createDirectories(dir.resolve("sub"));
            Files.writeString(dir.resolve("file1.txt"), "File 1");
            Files.writeString(dir.resolve("sub/file2.txt"), "File 2");

            Path zipFile = tempDir.resolve("dir.zip");
            ZipUtil.zip(dir, zipFile);
            assertThat(zipFile).exists();

            Path outputDir = tempDir.resolve("extracted");
            ZipUtil.unzip(zipFile, outputDir);

            assertThat(outputDir.resolve("mydir/file1.txt")).exists();
            assertThat(outputDir.resolve("mydir/sub/file2.txt")).exists();
            assertThat(Files.readString(outputDir.resolve("mydir/file1.txt"))).isEqualTo("File 1");
            assertThat(Files.readString(outputDir.resolve("mydir/sub/file2.txt"))).isEqualTo("File 2");
        }
    }

    @Nested
    @DisplayName("zip多文件测试")
    class MultipleFilesTests {

        @Test
        @DisplayName("压缩多个文件")
        void testZipMultipleFiles() throws Exception {
            Path file1 = tempDir.resolve("a.txt");
            Path file2 = tempDir.resolve("b.txt");
            Files.writeString(file1, "AAA");
            Files.writeString(file2, "BBB");

            Path zipFile = tempDir.resolve("multi.zip");
            ZipUtil.zip(List.of(file1, file2), zipFile);
            assertThat(zipFile).exists();

            Path outputDir = tempDir.resolve("extracted");
            ZipUtil.unzip(zipFile, outputDir);

            assertThat(Files.readString(outputDir.resolve("a.txt"))).isEqualTo("AAA");
            assertThat(Files.readString(outputDir.resolve("b.txt"))).isEqualTo("BBB");
        }
    }

    @Nested
    @DisplayName("list 测试")
    class ListTests {

        @Test
        @DisplayName("列出zip条目")
        void testListEntries() throws Exception {
            Path file = tempDir.resolve("data.txt");
            Files.writeString(file, "Some data here");

            Path zipFile = tempDir.resolve("list.zip");
            ZipUtil.zip(file, zipFile);

            List<ZipEntryInfo> entries = ZipUtil.list(zipFile);

            assertThat(entries).hasSize(1);
            assertThat(entries.getFirst().name()).isEqualTo("data.txt");
            assertThat(entries.getFirst().isDirectory()).isFalse();
            assertThat(entries.getFirst().size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("列出包含目录的zip条目")
        void testListDirectoryEntries() throws Exception {
            Path dir = tempDir.resolve("listdir");
            Files.createDirectories(dir.resolve("sub"));
            Files.writeString(dir.resolve("root.txt"), "root");
            Files.writeString(dir.resolve("sub/nested.txt"), "nested");

            Path zipFile = tempDir.resolve("listdir.zip");
            ZipUtil.zip(dir, zipFile);

            List<ZipEntryInfo> entries = ZipUtil.list(zipFile);

            assertThat(entries).isNotEmpty();
            assertThat(entries.stream().map(ZipEntryInfo::name))
                    .anyMatch(name -> name.contains("root.txt"))
                    .anyMatch(name -> name.contains("nested.txt"));
        }

        @Test
        @DisplayName("null路径抛出NullPointerException")
        void testNullPath() {
            assertThatThrownBy(() -> ZipUtil.list(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("readEntry 测试")
    class ReadEntryTests {

        @Test
        @DisplayName("读取单个条目")
        void testReadEntry() throws Exception {
            Path file = tempDir.resolve("entry.txt");
            String content = "Entry content here";
            Files.writeString(file, content);

            Path zipFile = tempDir.resolve("read.zip");
            ZipUtil.zip(file, zipFile);

            byte[] data = ZipUtil.readEntry(zipFile, "entry.txt");

            assertThat(new String(data, StandardCharsets.UTF_8)).isEqualTo(content);
        }

        @Test
        @DisplayName("读取不存在的条目抛出OpenIOOperationException")
        void testReadNonExistentEntry() throws Exception {
            Path file = tempDir.resolve("exists.txt");
            Files.writeString(file, "data");

            Path zipFile = tempDir.resolve("read2.zip");
            ZipUtil.zip(file, zipFile);

            assertThatThrownBy(() -> ZipUtil.readEntry(zipFile, "nonexistent.txt"))
                    .isInstanceOf(OpenIOOperationException.class);
        }

        @Test
        @DisplayName("null参数抛出NullPointerException")
        void testNullArgs() {
            assertThatThrownBy(() -> ZipUtil.readEntry(null, "name"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> ZipUtil.readEntry(tempDir.resolve("a.zip"), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("containsEntry 测试")
    class ContainsEntryTests {

        @Test
        @DisplayName("检查条目是否存在")
        void testContainsEntry() throws Exception {
            Path file = tempDir.resolve("check.txt");
            Files.writeString(file, "check");

            Path zipFile = tempDir.resolve("contains.zip");
            ZipUtil.zip(file, zipFile);

            assertThat(ZipUtil.containsEntry(zipFile, "check.txt")).isTrue();
            assertThat(ZipUtil.containsEntry(zipFile, "missing.txt")).isFalse();
        }

        @Test
        @DisplayName("null参数抛出NullPointerException")
        void testNullArgs() {
            assertThatThrownBy(() -> ZipUtil.containsEntry(null, "name"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> ZipUtil.containsEntry(tempDir.resolve("a.zip"), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("ZipBuilder 测试")
    class ZipBuilderTests {

        @Test
        @DisplayName("使用Builder添加文件")
        void testBuilderAddFile() throws Exception {
            Path file = tempDir.resolve("builder.txt");
            Files.writeString(file, "Builder test");

            Path zipFile = tempDir.resolve("builder.zip");
            ZipUtil.builder()
                    .addFile(file)
                    .writeTo(zipFile);

            assertThat(ZipUtil.containsEntry(zipFile, "builder.txt")).isTrue();
            assertThat(new String(ZipUtil.readEntry(zipFile, "builder.txt"), StandardCharsets.UTF_8))
                    .isEqualTo("Builder test");
        }

        @Test
        @DisplayName("使用Builder添加自定义条目名的文件")
        void testBuilderAddFileCustomName() throws Exception {
            Path file = tempDir.resolve("original.txt");
            Files.writeString(file, "Custom name");

            Path zipFile = tempDir.resolve("custom.zip");
            ZipUtil.builder()
                    .addFile(file, "renamed.txt")
                    .writeTo(zipFile);

            assertThat(ZipUtil.containsEntry(zipFile, "renamed.txt")).isTrue();
            assertThat(ZipUtil.containsEntry(zipFile, "original.txt")).isFalse();
        }

        @Test
        @DisplayName("使用Builder添加目录")
        void testBuilderAddDirectory() throws Exception {
            Path dir = tempDir.resolve("builderdir");
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("inside.txt"), "Inside dir");

            Path zipFile = tempDir.resolve("builderdir.zip");
            ZipUtil.builder()
                    .addDirectory(dir)
                    .writeTo(zipFile);

            assertThat(zipFile).exists();
            List<ZipEntryInfo> entries = ZipUtil.list(zipFile);
            assertThat(entries).isNotEmpty();
            assertThat(entries.stream().map(ZipEntryInfo::name))
                    .anyMatch(name -> name.contains("inside.txt"));
        }

        @Test
        @DisplayName("使用Builder添加字节数据")
        void testBuilderAddBytes() throws Exception {
            byte[] data = {1, 2, 3, 4, 5};

            Path zipFile = tempDir.resolve("bytes.zip");
            ZipUtil.builder()
                    .addBytes("data.bin", data)
                    .writeTo(zipFile);

            assertThat(ZipUtil.readEntry(zipFile, "data.bin")).isEqualTo(data);
        }

        @Test
        @DisplayName("使用Builder添加字符串")
        void testBuilderAddString() throws Exception {
            Path zipFile = tempDir.resolve("string.zip");
            ZipUtil.builder()
                    .addString("note.txt", "Hello Builder")
                    .writeTo(zipFile);

            assertThat(new String(ZipUtil.readEntry(zipFile, "note.txt"), StandardCharsets.UTF_8))
                    .isEqualTo("Hello Builder");
        }

        @Test
        @DisplayName("使用Builder指定字符集添加字符串")
        void testBuilderAddStringWithCharset() throws Exception {
            Path zipFile = tempDir.resolve("charset.zip");
            String chinese = "你好世界";
            ZipUtil.builder()
                    .addString("chinese.txt", chinese, StandardCharsets.UTF_8)
                    .writeTo(zipFile);

            assertThat(new String(ZipUtil.readEntry(zipFile, "chinese.txt"), StandardCharsets.UTF_8))
                    .isEqualTo(chinese);
        }

        @Test
        @DisplayName("使用Builder设置注释和压缩级别")
        void testBuilderCommentAndLevel() throws Exception {
            Path zipFile = tempDir.resolve("options.zip");
            ZipUtil.builder()
                    .addString("test.txt", "test")
                    .comment("Test archive")
                    .compressionLevel(9)
                    .writeTo(zipFile);

            assertThat(zipFile).exists();
            assertThat(ZipUtil.containsEntry(zipFile, "test.txt")).isTrue();
        }

        @Test
        @DisplayName("无效压缩级别抛出IllegalArgumentException")
        void testInvalidCompressionLevel() {
            assertThatThrownBy(() -> ZipUtil.builder().compressionLevel(-1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> ZipUtil.builder().compressionLevel(10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("混合添加多种条目")
        void testBuilderMixed() throws Exception {
            Path file = tempDir.resolve("mixed.txt");
            Files.writeString(file, "Mixed file");

            Path zipFile = tempDir.resolve("mixed.zip");
            ZipUtil.builder()
                    .addFile(file)
                    .addString("text.txt", "Text entry")
                    .addBytes("binary.bin", new byte[]{0x0A, 0x0B})
                    .compressionLevel(5)
                    .writeTo(zipFile);

            assertThat(ZipUtil.containsEntry(zipFile, "mixed.txt")).isTrue();
            assertThat(ZipUtil.containsEntry(zipFile, "text.txt")).isTrue();
            assertThat(ZipUtil.containsEntry(zipFile, "binary.bin")).isTrue();
        }
    }

    @Nested
    @DisplayName("安全检查测试")
    class SecurityTests {

        @Test
        @DisplayName("路径穿越条目被拒绝")
        void testPathTraversalRejected() throws Exception {
            // Create a malicious zip with path traversal entry
            Path maliciousZip = tempDir.resolve("malicious.zip");
            try (OutputStream fos = Files.newOutputStream(maliciousZip);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                ZipEntry entry = new ZipEntry("../../../etc/passwd");
                zos.putNextEntry(entry);
                zos.write("malicious content".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            Path outputDir = tempDir.resolve("safe_extract");

            assertThatThrownBy(() -> ZipUtil.unzip(maliciousZip, outputDir))
                    .isInstanceOf(OpenIOOperationException.class)
                    .hasMessageContaining("Path traversal");
        }

        @Test
        @DisplayName("entry name过长被拒绝")
        void testEntryNameTooLong() throws Exception {
            // Create a zip with an overly long entry name
            Path longNameZip = tempDir.resolve("longname.zip");
            String longName = "a".repeat(ZipUtil.MAX_ENTRY_NAME_LENGTH + 1) + ".txt";
            try (OutputStream fos = Files.newOutputStream(longNameZip);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                ZipEntry entry = new ZipEntry(longName);
                zos.putNextEntry(entry);
                zos.write("data".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            Path outputDir = tempDir.resolve("longname_extract");

            assertThatThrownBy(() -> ZipUtil.unzip(longNameZip, outputDir))
                    .isInstanceOf(OpenIOOperationException.class)
                    .hasMessageContaining("maximum length");
        }

        @Test
        @DisplayName("null参数unzip抛出NullPointerException")
        void testNullUnzip() {
            assertThatThrownBy(() -> ZipUtil.unzip(null, tempDir))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> ZipUtil.unzip(tempDir.resolve("a.zip"), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("ZipBuilder条目名校验测试")
    class ZipBuilderEntryNameValidationTests {

        @Test
        @DisplayName("路径穿越条目名被拒绝")
        void testPathTraversalEntryNameRejected() {
            ZipBuilder builder = ZipUtil.builder();

            assertThatThrownBy(() -> builder.addBytes("../etc/passwd", new byte[]{1}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("path traversal");
        }

        @Test
        @DisplayName("嵌套路径穿越条目名被拒绝")
        void testNestedPathTraversalRejected() {
            ZipBuilder builder = ZipUtil.builder();

            assertThatThrownBy(() -> builder.addBytes("foo/../../etc/passwd", new byte[]{1}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("path traversal");
        }

        @Test
        @DisplayName("绝对路径条目名被拒绝")
        void testAbsoluteEntryNameRejected() {
            ZipBuilder builder = ZipUtil.builder();

            assertThatThrownBy(() -> builder.addBytes("/etc/passwd", new byte[]{1}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("absolute");
        }

        @Test
        @DisplayName("反斜杠绝对路径条目名被拒绝")
        void testBackslashAbsoluteEntryNameRejected() {
            ZipBuilder builder = ZipUtil.builder();

            assertThatThrownBy(() -> builder.addBytes("\\etc\\passwd", new byte[]{1}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("absolute");
        }

        @Test
        @DisplayName("null字节条目名被拒绝")
        void testNullByteEntryNameRejected() {
            ZipBuilder builder = ZipUtil.builder();

            assertThatThrownBy(() -> builder.addBytes("file\0.txt", new byte[]{1}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null bytes");
        }

        @Test
        @DisplayName("空条目名被拒绝")
        void testEmptyEntryNameRejected() {
            ZipBuilder builder = ZipUtil.builder();

            assertThatThrownBy(() -> builder.addBytes("", new byte[]{1}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("正常条目名通过校验")
        void testValidEntryNameAccepted(@TempDir Path tempDir) throws Exception {
            Path output = tempDir.resolve("valid.zip");

            ZipUtil.builder()
                    .addBytes("data/file.txt", "hello".getBytes(StandardCharsets.UTF_8))
                    .addString("nested/dir/readme.md", "# Title")
                    .writeTo(output);

            assertThat(output).exists();
            assertThat(ZipUtil.containsEntry(output, "data/file.txt")).isTrue();
        }

        @Test
        @DisplayName("含..但非穿越的条目名通过校验")
        void testDoubleDotInFilenameAccepted(@TempDir Path tempDir) throws Exception {
            Path output = tempDir.resolve("dots.zip");

            ZipUtil.builder()
                    .addBytes("data..backup.txt", "content".getBytes(StandardCharsets.UTF_8))
                    .writeTo(output);

            assertThat(output).exists();
            assertThat(ZipUtil.containsEntry(output, "data..backup.txt")).isTrue();
        }

        @Test
        @DisplayName("addFile自定义名也校验")
        void testAddFileWithCustomNameValidated(@TempDir Path tempDir) throws Exception {
            Path dummyFile = tempDir.resolve("dummy.txt");
            java.nio.file.Files.writeString(dummyFile, "dummy");

            ZipBuilder builder = ZipUtil.builder();

            assertThatThrownBy(() -> builder.addFile(dummyFile, "../evil.txt"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("path traversal");
        }

        @Test
        @DisplayName("addString也校验条目名")
        void testAddStringValidated() {
            ZipBuilder builder = ZipUtil.builder();

            assertThatThrownBy(() -> builder.addString("../evil.txt", "content"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("path traversal");
        }
    }
}
