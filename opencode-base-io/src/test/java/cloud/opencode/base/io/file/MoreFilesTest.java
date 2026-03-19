package cloud.opencode.base.io.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * MoreFiles 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("MoreFiles 测试")
class MoreFilesTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("writeAtomically方法测试")
    class WriteAtomicallyTests {

        @Test
        @DisplayName("原子写入字符串(UTF-8)")
        void testWriteAtomicallyString() throws Exception {
            Path file = tempDir.resolve("atomic.txt");

            MoreFiles.writeAtomically(file, "Hello, World!");

            assertThat(Files.readString(file)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("原子写入字符串(指定字符集)")
        void testWriteAtomicallyStringWithCharset() throws Exception {
            Path file = tempDir.resolve("atomic.txt");

            MoreFiles.writeAtomically(file, "你好", StandardCharsets.UTF_8);

            assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("你好");
        }

        @Test
        @DisplayName("原子写入字节数组")
        void testWriteAtomicallyBytes() throws Exception {
            Path file = tempDir.resolve("atomic.bin");
            byte[] data = {1, 2, 3, 4, 5};

            MoreFiles.writeAtomically(file, data);

            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }

        @Test
        @DisplayName("原子写入行(UTF-8)")
        void testWriteAtomicallyLines() throws Exception {
            Path file = tempDir.resolve("atomic.txt");

            MoreFiles.writeAtomically(file, List.of("line1", "line2", "line3"));

            List<String> lines = Files.readAllLines(file);
            assertThat(lines).containsExactly("line1", "line2", "line3");
        }

        @Test
        @DisplayName("原子写入行(指定字符集)")
        void testWriteAtomicallyLinesWithCharset() throws Exception {
            Path file = tempDir.resolve("atomic.txt");

            MoreFiles.writeAtomically(file, List.of("第一行", "第二行"), StandardCharsets.UTF_8);

            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            assertThat(lines).containsExactly("第一行", "第二行");
        }

        @Test
        @DisplayName("原子写入从InputStream")
        void testWriteAtomicallyInputStream() throws Exception {
            Path file = tempDir.resolve("atomic.bin");
            byte[] data = {10, 20, 30};
            ByteArrayInputStream input = new ByteArrayInputStream(data);

            MoreFiles.writeAtomically(file, input);

            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }

        @Test
        @DisplayName("创建父目录")
        void testWriteAtomicallyCreatesParent() throws Exception {
            Path file = tempDir.resolve("subdir/atomic.txt");

            MoreFiles.writeAtomically(file, "content");

            assertThat(Files.exists(file.getParent())).isTrue();
            assertThat(Files.readString(file)).isEqualTo("content");
        }

        @Test
        @DisplayName("null路径抛出异常")
        void testWriteAtomicallyNullPath() {
            assertThatThrownBy(() -> MoreFiles.writeAtomically(null, "content"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null内容抛出异常")
        void testWriteAtomicallyNullContent() {
            Path file = tempDir.resolve("test.txt");
            assertThatThrownBy(() -> MoreFiles.writeAtomically(file, (String) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("touch方法测试")
    class TouchTests {

        @Test
        @DisplayName("创建新文件")
        void testTouchCreateNew() throws Exception {
            Path file = tempDir.resolve("new.txt");

            MoreFiles.touch(file);

            assertThat(Files.exists(file)).isTrue();
            assertThat(Files.size(file)).isEqualTo(0);
        }

        @Test
        @DisplayName("更新已有文件时间戳")
        void testTouchUpdateTimestamp() throws Exception {
            Path file = tempDir.resolve("existing.txt");
            Files.createFile(file);
            long oldTime = Files.getLastModifiedTime(file).toMillis();
            Thread.sleep(100);

            MoreFiles.touch(file);

            long newTime = Files.getLastModifiedTime(file).toMillis();
            assertThat(newTime).isGreaterThanOrEqualTo(oldTime);
        }

        @Test
        @DisplayName("创建父目录")
        void testTouchCreatesParent() throws Exception {
            Path file = tempDir.resolve("subdir/new.txt");

            MoreFiles.touch(file);

            assertThat(Files.exists(file.getParent())).isTrue();
            assertThat(Files.exists(file)).isTrue();
        }

        @Test
        @DisplayName("null路径抛出异常")
        void testTouchNullPath() {
            assertThatThrownBy(() -> MoreFiles.touch(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deleteRecursively方法测试")
    class DeleteRecursivelyTests {

        @Test
        @DisplayName("递归删除目录")
        void testDeleteRecursively() throws Exception {
            Path dir = tempDir.resolve("toDelete");
            Files.createDirectories(dir.resolve("sub1"));
            Files.createDirectories(dir.resolve("sub2"));
            Files.createFile(dir.resolve("file.txt"));
            Files.createFile(dir.resolve("sub1/file.txt"));

            MoreFiles.deleteRecursively(dir);

            assertThat(Files.exists(dir)).isFalse();
        }

        @Test
        @DisplayName("使用过滤器删除")
        void testDeleteRecursivelyWithFilter() throws Exception {
            Path dir = tempDir.resolve("filtered");
            Files.createDirectories(dir);
            Files.createFile(dir.resolve("keep.txt"));
            Files.createFile(dir.resolve("delete.tmp"));

            MoreFiles.deleteRecursively(dir, p -> p.toString().endsWith(".tmp"));

            assertThat(Files.exists(dir.resolve("keep.txt"))).isTrue();
            assertThat(Files.exists(dir.resolve("delete.tmp"))).isFalse();
        }

        @Test
        @DisplayName("不存在的路径不抛异常")
        void testDeleteRecursivelyNotExists() {
            Path notExists = tempDir.resolve("not-exists");

            assertThatCode(() -> MoreFiles.deleteRecursively(notExists))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("deleteIfExists方法测试")
    class DeleteIfExistsTests {

        @Test
        @DisplayName("删除存在的文件返回true")
        void testDeleteIfExistsTrue() throws Exception {
            Path file = tempDir.resolve("to-delete.txt");
            Files.createFile(file);

            boolean result = MoreFiles.deleteIfExists(file);

            assertThat(result).isTrue();
            assertThat(Files.exists(file)).isFalse();
        }

        @Test
        @DisplayName("不存在的文件返回false")
        void testDeleteIfExistsFalse() {
            Path file = tempDir.resolve("not-exists.txt");

            boolean result = MoreFiles.deleteIfExists(file);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteDirectoryIfEmpty方法测试")
    class DeleteDirectoryIfEmptyTests {

        @Test
        @DisplayName("删除空目录")
        void testDeleteDirectoryIfEmptySuccess() throws Exception {
            Path dir = tempDir.resolve("emptyDir");
            Files.createDirectory(dir);

            boolean result = MoreFiles.deleteDirectoryIfEmpty(dir);

            assertThat(result).isTrue();
            assertThat(Files.exists(dir)).isFalse();
        }

        @Test
        @DisplayName("非空目录返回false")
        void testDeleteDirectoryIfEmptyNonEmpty() throws Exception {
            Path dir = tempDir.resolve("nonEmptyDir");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("file.txt"));

            boolean result = MoreFiles.deleteDirectoryIfEmpty(dir);

            assertThat(result).isFalse();
            assertThat(Files.exists(dir)).isTrue();
        }

        @Test
        @DisplayName("文件返回false")
        void testDeleteDirectoryIfEmptyFile() throws Exception {
            Path file = tempDir.resolve("file.txt");
            Files.createFile(file);

            boolean result = MoreFiles.deleteDirectoryIfEmpty(file);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("fileTree方法测试")
    class FileTreeTests {

        @Test
        @DisplayName("遍历文件树")
        void testFileTree() throws Exception {
            Path dir = tempDir.resolve("tree");
            Files.createDirectories(dir.resolve("sub"));
            Files.createFile(dir.resolve("file1.txt"));
            Files.createFile(dir.resolve("sub/file2.txt"));

            List<Path> paths = MoreFiles.fileTree(dir).toList();

            assertThat(paths).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("设置最大深度")
        void testFileTreeMaxDepth() throws Exception {
            Path dir = tempDir.resolve("deep");
            Files.createDirectories(dir.resolve("level1/level2/level3"));
            Files.createFile(dir.resolve("level1/level2/level3/deep.txt"));

            List<Path> shallow = MoreFiles.fileTree(dir).maxDepth(1).toList();
            List<Path> deep = MoreFiles.fileTree(dir).maxDepth(10).toList();

            assertThat(deep.size()).isGreaterThanOrEqualTo(shallow.size());
        }

        @Test
        @DisplayName("仅文件")
        void testFileTreeFilesOnly() throws Exception {
            Path dir = tempDir.resolve("mixed");
            Files.createDirectories(dir.resolve("subdir"));
            Files.createFile(dir.resolve("file.txt"));

            List<Path> files = MoreFiles.fileTree(dir).filesOnly().toList();

            assertThat(files).allSatisfy(p -> assertThat(Files.isRegularFile(p)).isTrue());
        }

        @Test
        @DisplayName("仅目录")
        void testFileTreeDirectoriesOnly() throws Exception {
            Path dir = tempDir.resolve("mixed2");
            Files.createDirectories(dir.resolve("subdir"));
            Files.createFile(dir.resolve("file.txt"));

            List<Path> dirs = MoreFiles.fileTree(dir).directoriesOnly().toList();

            assertThat(dirs).allSatisfy(p -> assertThat(Files.isDirectory(p)).isTrue());
        }

        @Test
        @DisplayName("glob过滤")
        void testFileTreeGlob() throws Exception {
            Path dir = tempDir.resolve("glob");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("test.txt"));
            Files.createFile(dir.resolve("test.log"));

            List<Path> txtFiles = MoreFiles.fileTree(dir).glob("*.txt").toList();

            assertThat(txtFiles).hasSize(1);
            assertThat(txtFiles.get(0).getFileName().toString()).endsWith(".txt");
        }

        @Test
        @DisplayName("count方法")
        void testFileTreeCount() throws Exception {
            Path dir = tempDir.resolve("count");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("a.txt"));
            Files.createFile(dir.resolve("b.txt"));
            Files.createFile(dir.resolve("c.txt"));

            long count = MoreFiles.fileTree(dir).filesOnly().count();

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("forEach方法")
        void testFileTreeForEach() throws Exception {
            Path dir = tempDir.resolve("foreach");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("test.txt"));

            java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger();
            MoreFiles.fileTree(dir).forEach(p -> count.incrementAndGet());

            assertThat(count.get()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("listFilesRecursively方法测试")
    class ListFilesRecursivelyTests {

        @Test
        @DisplayName("递归列出文件")
        void testListFilesRecursively() throws Exception {
            Path dir = tempDir.resolve("recursive");
            Files.createDirectories(dir.resolve("sub"));
            Files.createFile(dir.resolve("file1.txt"));
            Files.createFile(dir.resolve("sub/file2.txt"));

            try (Stream<Path> files = MoreFiles.listFilesRecursively(dir)) {
                assertThat(files.count()).isEqualTo(2);
            }
        }
    }

    @Nested
    @DisplayName("listDirectoriesRecursively方法测试")
    class ListDirectoriesRecursivelyTests {

        @Test
        @DisplayName("递归列出目录")
        void testListDirectoriesRecursively() throws Exception {
            Path dir = tempDir.resolve("dirs");
            Files.createDirectories(dir.resolve("sub1"));
            Files.createDirectories(dir.resolve("sub2"));

            try (Stream<Path> dirs = MoreFiles.listDirectoriesRecursively(dir)) {
                assertThat(dirs.count()).isGreaterThanOrEqualTo(2);
            }
        }
    }

    @Nested
    @DisplayName("walkFileTree方法测试")
    class WalkFileTreeTests {

        @Test
        @DisplayName("遍历并应用操作")
        void testWalkFileTree() throws Exception {
            Path dir = tempDir.resolve("walk");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("file.txt"));

            java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger();
            MoreFiles.walkFileTree(dir, p -> count.incrementAndGet());

            assertThat(count.get()).isGreaterThan(0);
        }
    }

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

            assertThat(MoreFiles.contentEquals(file1, file2)).isTrue();
        }

        @Test
        @DisplayName("不同内容返回false")
        void testContentEqualsFalse() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.writeString(file1, "content1");
            Files.writeString(file2, "content2");

            assertThat(MoreFiles.contentEquals(file1, file2)).isFalse();
        }

        @Test
        @DisplayName("相同文件返回true")
        void testContentEqualsSameFile() throws Exception {
            Path file = tempDir.resolve("same.txt");
            Files.writeString(file, "content");

            assertThat(MoreFiles.contentEquals(file, file)).isTrue();
        }

        @Test
        @DisplayName("不同大小快速返回false")
        void testContentEqualsDifferentSize() throws Exception {
            Path file1 = tempDir.resolve("small.txt");
            Path file2 = tempDir.resolve("large.txt");
            Files.writeString(file1, "small");
            Files.writeString(file2, "much larger content");

            assertThat(MoreFiles.contentEquals(file1, file2)).isFalse();
        }

        @Test
        @DisplayName("空文件相等")
        void testContentEqualsEmptyFiles() throws Exception {
            Path file1 = tempDir.resolve("empty1.txt");
            Path file2 = tempDir.resolve("empty2.txt");
            Files.createFile(file1);
            Files.createFile(file2);

            assertThat(MoreFiles.contentEquals(file1, file2)).isTrue();
        }
    }

    @Nested
    @DisplayName("isEmptyDirectory方法测试")
    class IsEmptyDirectoryTests {

        @Test
        @DisplayName("空目录返回true")
        void testIsEmptyDirectoryTrue() throws Exception {
            Path dir = tempDir.resolve("empty");
            Files.createDirectory(dir);

            assertThat(MoreFiles.isEmptyDirectory(dir)).isTrue();
        }

        @Test
        @DisplayName("非空目录返回false")
        void testIsEmptyDirectoryFalse() throws Exception {
            Path dir = tempDir.resolve("notEmpty");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("file.txt"));

            assertThat(MoreFiles.isEmptyDirectory(dir)).isFalse();
        }

        @Test
        @DisplayName("文件返回false")
        void testIsEmptyDirectoryFile() throws Exception {
            Path file = tempDir.resolve("file.txt");
            Files.createFile(file);

            assertThat(MoreFiles.isEmptyDirectory(file)).isFalse();
        }
    }

    @Nested
    @DisplayName("createParentDirectories方法测试")
    class CreateParentDirectoriesTests {

        @Test
        @DisplayName("创建父目录")
        void testCreateParentDirectories() {
            Path file = tempDir.resolve("a/b/c/file.txt");

            Path result = MoreFiles.createParentDirectories(file);

            assertThat(Files.exists(file.getParent())).isTrue();
            assertThat(result).isEqualTo(file);
        }

        @Test
        @DisplayName("已存在的父目录不报错")
        void testCreateParentDirectoriesExists() throws Exception {
            Path file = tempDir.resolve("existing/file.txt");
            Files.createDirectories(file.getParent());

            assertThatCode(() -> MoreFiles.createParentDirectories(file))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getFileExtension方法测试")
    class GetFileExtensionTests {

        @Test
        @DisplayName("返回扩展名(带点)")
        void testGetFileExtension() {
            Path file = Path.of("test.txt");

            assertThat(MoreFiles.getFileExtension(file)).isEqualTo(".txt");
        }

        @Test
        @DisplayName("无扩展名返回空字符串")
        void testGetFileExtensionNone() {
            Path file = Path.of("noextension");

            assertThat(MoreFiles.getFileExtension(file)).isEmpty();
        }

        @Test
        @DisplayName("多个点取最后一个")
        void testGetFileExtensionMultipleDots() {
            Path file = Path.of("file.backup.tar.gz");

            assertThat(MoreFiles.getFileExtension(file)).isEqualTo(".gz");
        }
    }

    @Nested
    @DisplayName("getNameWithoutExtension方法测试")
    class GetNameWithoutExtensionTests {

        @Test
        @DisplayName("返回不带扩展名的文件名")
        void testGetNameWithoutExtension() {
            Path file = Path.of("test.txt");

            assertThat(MoreFiles.getNameWithoutExtension(file)).isEqualTo("test");
        }

        @Test
        @DisplayName("无扩展名返回完整名称")
        void testGetNameWithoutExtensionNone() {
            Path file = Path.of("noextension");

            assertThat(MoreFiles.getNameWithoutExtension(file)).isEqualTo("noextension");
        }

        @Test
        @DisplayName("多个点保留除最后一个外的部分")
        void testGetNameWithoutExtensionMultipleDots() {
            Path file = Path.of("file.backup.tar.gz");

            assertThat(MoreFiles.getNameWithoutExtension(file)).isEqualTo("file.backup.tar");
        }
    }
}
