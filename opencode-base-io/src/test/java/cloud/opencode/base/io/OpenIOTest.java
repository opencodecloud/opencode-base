package cloud.opencode.base.io;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenIO 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("OpenIO 测试")
class OpenIOTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("readBytes方法测试")
    class ReadBytesTests {

        @Test
        @DisplayName("读取文件为字节数组")
        void testReadBytes() throws Exception {
            Path file = tempDir.resolve("test.bin");
            byte[] data = {1, 2, 3, 4, 5};
            Files.write(file, data);

            byte[] result = OpenIO.readBytes(file);

            assertThat(result).isEqualTo(data);
        }

        @Test
        @DisplayName("不存在的文件抛出异常")
        void testReadBytesNotExists() {
            Path file = tempDir.resolve("notexists.bin");

            assertThatThrownBy(() -> OpenIO.readBytes(file))
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("readString方法测试")
    class ReadStringTests {

        @Test
        @DisplayName("读取文件为字符串")
        void testReadString() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello, World!");

            String result = OpenIO.readString(file);

            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("使用指定字符集读取")
        void testReadStringWithCharset() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "你好", StandardCharsets.UTF_8);

            String result = OpenIO.readString(file, StandardCharsets.UTF_8);

            assertThat(result).isEqualTo("你好");
        }
    }

    @Nested
    @DisplayName("readLines方法测试")
    class ReadLinesTests {

        @Test
        @DisplayName("读取文件为行列表")
        void testReadLines() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "line1\nline2\nline3");

            List<String> lines = OpenIO.readLines(file);

            assertThat(lines).containsExactly("line1", "line2", "line3");
        }

        @Test
        @DisplayName("使用指定字符集读取")
        void testReadLinesWithCharset() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "第一行\n第二行", StandardCharsets.UTF_8);

            List<String> lines = OpenIO.readLines(file, StandardCharsets.UTF_8);

            assertThat(lines).containsExactly("第一行", "第二行");
        }
    }

    @Nested
    @DisplayName("lines方法测试")
    class LinesTests {

        @Test
        @DisplayName("返回行流")
        void testLines() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "a\nb\nc");

            try (Stream<String> lines = OpenIO.lines(file)) {
                assertThat(lines.toList()).containsExactly("a", "b", "c");
            }
        }

        @Test
        @DisplayName("使用指定字符集返回行流")
        void testLinesWithCharset() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "甲\n乙", StandardCharsets.UTF_8);

            try (Stream<String> lines = OpenIO.lines(file, StandardCharsets.UTF_8)) {
                assertThat(lines.toList()).containsExactly("甲", "乙");
            }
        }
    }

    @Nested
    @DisplayName("readFirstLine方法测试")
    class ReadFirstLineTests {

        @Test
        @DisplayName("读取首行")
        void testReadFirstLine() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "first\nsecond\nthird");

            String firstLine = OpenIO.readFirstLine(file);

            assertThat(firstLine).isEqualTo("first");
        }

        @Test
        @DisplayName("空文件返回null")
        void testReadFirstLineEmpty() throws Exception {
            Path file = tempDir.resolve("empty.txt");
            Files.createFile(file);

            String firstLine = OpenIO.readFirstLine(file);

            assertThat(firstLine).isNull();
        }
    }

    @Nested
    @DisplayName("writeBytes方法测试")
    class WriteBytesTests {

        @Test
        @DisplayName("写入字节数组")
        void testWriteBytes() throws Exception {
            Path file = tempDir.resolve("test.bin");
            byte[] data = {10, 20, 30};

            OpenIO.writeBytes(file, data);

            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }

        @Test
        @DisplayName("使用选项写入")
        void testWriteBytesWithOptions() throws Exception {
            Path file = tempDir.resolve("test.bin");
            Files.write(file, new byte[]{1, 2});

            OpenIO.writeBytes(file, new byte[]{3, 4}, StandardOpenOption.APPEND);

            assertThat(Files.readAllBytes(file)).containsExactly(1, 2, 3, 4);
        }
    }

    @Nested
    @DisplayName("writeString方法测试")
    class WriteStringTests {

        @Test
        @DisplayName("写入字符串")
        void testWriteString() throws Exception {
            Path file = tempDir.resolve("test.txt");

            OpenIO.writeString(file, "Hello");

            assertThat(Files.readString(file)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("使用指定字符集写入")
        void testWriteStringWithCharset() throws Exception {
            Path file = tempDir.resolve("test.txt");

            OpenIO.writeString(file, "你好", StandardCharsets.UTF_8);

            assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("你好");
        }
    }

    @Nested
    @DisplayName("writeLines方法测试")
    class WriteLinesTests {

        @Test
        @DisplayName("写入行")
        void testWriteLines() throws Exception {
            Path file = tempDir.resolve("test.txt");

            OpenIO.writeLines(file, List.of("line1", "line2"));

            assertThat(Files.readAllLines(file)).containsExactly("line1", "line2");
        }

        @Test
        @DisplayName("使用指定字符集写入行")
        void testWriteLinesWithCharset() throws Exception {
            Path file = tempDir.resolve("test.txt");

            OpenIO.writeLines(file, List.of("甲", "乙"), StandardCharsets.UTF_8);

            assertThat(Files.readAllLines(file, StandardCharsets.UTF_8)).containsExactly("甲", "乙");
        }
    }

    @Nested
    @DisplayName("append方法测试")
    class AppendTests {

        @Test
        @DisplayName("追加内容")
        void testAppend() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello");

            OpenIO.append(file, ", World!");

            assertThat(Files.readString(file)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("文件不存在时创建")
        void testAppendCreates() throws Exception {
            Path file = tempDir.resolve("new.txt");

            OpenIO.append(file, "content");

            assertThat(Files.readString(file)).isEqualTo("content");
        }
    }

    @Nested
    @DisplayName("appendLines方法测试")
    class AppendLinesTests {

        @Test
        @DisplayName("追加行")
        void testAppendLines() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "first\n");

            OpenIO.appendLines(file, List.of("second", "third"));

            List<String> lines = Files.readAllLines(file);
            assertThat(lines).containsExactly("first", "second", "third");
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("存在的文件返回true")
        void testExistsTrue() throws Exception {
            Path file = tempDir.resolve("exists.txt");
            Files.createFile(file);

            assertThat(OpenIO.exists(file)).isTrue();
        }

        @Test
        @DisplayName("不存在的文件返回false")
        void testExistsFalse() {
            Path file = tempDir.resolve("notexists.txt");

            assertThat(OpenIO.exists(file)).isFalse();
        }
    }

    @Nested
    @DisplayName("isFile方法测试")
    class IsFileTests {

        @Test
        @DisplayName("文件返回true")
        void testIsFileTrue() throws Exception {
            Path file = tempDir.resolve("file.txt");
            Files.createFile(file);

            assertThat(OpenIO.isFile(file)).isTrue();
        }

        @Test
        @DisplayName("目录返回false")
        void testIsFileFalse() {
            assertThat(OpenIO.isFile(tempDir)).isFalse();
        }
    }

    @Nested
    @DisplayName("isDirectory方法测试")
    class IsDirectoryTests {

        @Test
        @DisplayName("目录返回true")
        void testIsDirectoryTrue() {
            assertThat(OpenIO.isDirectory(tempDir)).isTrue();
        }

        @Test
        @DisplayName("文件返回false")
        void testIsDirectoryFalse() throws Exception {
            Path file = tempDir.resolve("file.txt");
            Files.createFile(file);

            assertThat(OpenIO.isDirectory(file)).isFalse();
        }
    }

    @Nested
    @DisplayName("isEmptyDirectory方法测试")
    class IsEmptyDirectoryTests {

        @Test
        @DisplayName("空目录返回true")
        void testIsEmptyDirectoryTrue() throws Exception {
            Path emptyDir = tempDir.resolve("empty");
            Files.createDirectory(emptyDir);

            assertThat(OpenIO.isEmptyDirectory(emptyDir)).isTrue();
        }

        @Test
        @DisplayName("非空目录返回false")
        void testIsEmptyDirectoryFalse() throws Exception {
            Path dir = tempDir.resolve("nonempty");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("file.txt"));

            assertThat(OpenIO.isEmptyDirectory(dir)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSymbolicLink方法测试")
    class IsSymbolicLinkTests {

        @Test
        @DisplayName("普通文件返回false")
        void testIsSymbolicLinkFalse() throws Exception {
            Path file = tempDir.resolve("regular.txt");
            Files.createFile(file);

            assertThat(OpenIO.isSymbolicLink(file)).isFalse();
        }
    }

    @Nested
    @DisplayName("isHidden方法测试")
    class IsHiddenTests {

        @Test
        @DisplayName("普通文件返回false")
        void testIsHiddenFalse() throws Exception {
            Path file = tempDir.resolve("visible.txt");
            Files.createFile(file);

            assertThat(OpenIO.isHidden(file)).isFalse();
        }
    }

    @Nested
    @DisplayName("isReadable和isWritable方法测试")
    class ReadableWritableTests {

        @Test
        @DisplayName("可读文件返回true")
        void testIsReadable() throws Exception {
            Path file = tempDir.resolve("readable.txt");
            Files.createFile(file);

            assertThat(OpenIO.isReadable(file)).isTrue();
        }

        @Test
        @DisplayName("可写文件返回true")
        void testIsWritable() throws Exception {
            Path file = tempDir.resolve("writable.txt");
            Files.createFile(file);

            assertThat(OpenIO.isWritable(file)).isTrue();
        }
    }

    @Nested
    @DisplayName("isSameFile方法测试")
    class IsSameFileTests {

        @Test
        @DisplayName("同一文件返回true")
        void testIsSameFileTrue() throws Exception {
            Path file = tempDir.resolve("same.txt");
            Files.createFile(file);

            assertThat(OpenIO.isSameFile(file, file)).isTrue();
        }

        @Test
        @DisplayName("不同文件返回false")
        void testIsSameFileFalse() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.createFile(file1);
            Files.createFile(file2);

            assertThat(OpenIO.isSameFile(file1, file2)).isFalse();
        }
    }

    @Nested
    @DisplayName("createFile方法测试")
    class CreateFileTests {

        @Test
        @DisplayName("创建文件")
        void testCreateFile() {
            Path file = tempDir.resolve("newfile.txt");

            Path result = OpenIO.createFile(file);

            assertThat(result).isEqualTo(file);
            assertThat(Files.exists(file)).isTrue();
        }

        @Test
        @DisplayName("文件已存在时抛出异常")
        void testCreateFileExists() throws Exception {
            Path file = tempDir.resolve("existing.txt");
            Files.createFile(file);

            assertThatThrownBy(() -> OpenIO.createFile(file))
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("createDirectory方法测试")
    class CreateDirectoryTests {

        @Test
        @DisplayName("创建目录")
        void testCreateDirectory() {
            Path dir = tempDir.resolve("newdir");

            Path result = OpenIO.createDirectory(dir);

            assertThat(result).isEqualTo(dir);
            assertThat(Files.isDirectory(dir)).isTrue();
        }
    }

    @Nested
    @DisplayName("createDirectories方法测试")
    class CreateDirectoriesTests {

        @Test
        @DisplayName("创建多级目录")
        void testCreateDirectories() {
            Path dir = tempDir.resolve("a/b/c");

            Path result = OpenIO.createDirectories(dir);

            assertThat(result).isEqualTo(dir);
            assertThat(Files.isDirectory(dir)).isTrue();
        }
    }

    @Nested
    @DisplayName("delete方法测试")
    class DeleteTests {

        @Test
        @DisplayName("删除文件")
        void testDelete() throws Exception {
            Path file = tempDir.resolve("todelete.txt");
            Files.createFile(file);

            OpenIO.delete(file);

            assertThat(Files.exists(file)).isFalse();
        }

        @Test
        @DisplayName("删除不存在的文件抛出异常")
        void testDeleteNotExists() {
            Path file = tempDir.resolve("notexists.txt");

            assertThatThrownBy(() -> OpenIO.delete(file))
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("deleteIfExists方法测试")
    class DeleteIfExistsTests {

        @Test
        @DisplayName("存在时删除返回true")
        void testDeleteIfExistsTrue() throws Exception {
            Path file = tempDir.resolve("todelete.txt");
            Files.createFile(file);

            boolean result = OpenIO.deleteIfExists(file);

            assertThat(result).isTrue();
            assertThat(Files.exists(file)).isFalse();
        }

        @Test
        @DisplayName("不存在时返回false")
        void testDeleteIfExistsFalse() {
            Path file = tempDir.resolve("notexists.txt");

            boolean result = OpenIO.deleteIfExists(file);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteRecursively方法测试")
    class DeleteRecursivelyTests {

        @Test
        @DisplayName("递归删除目录")
        void testDeleteRecursively() throws Exception {
            Path dir = tempDir.resolve("todelete");
            Files.createDirectories(dir.resolve("sub"));
            Files.createFile(dir.resolve("file.txt"));
            Files.createFile(dir.resolve("sub/nested.txt"));

            OpenIO.deleteRecursively(dir);

            assertThat(Files.exists(dir)).isFalse();
        }

        @Test
        @DisplayName("删除不存在的目录不抛异常")
        void testDeleteRecursivelyNotExists() {
            Path dir = tempDir.resolve("notexists");

            assertThatCode(() -> OpenIO.deleteRecursively(dir)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("copy方法测试")
    class CopyTests {

        @Test
        @DisplayName("复制文件")
        void testCopy() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("target.txt");
            Files.writeString(source, "content");

            Path result = OpenIO.copy(source, target);

            assertThat(result).isEqualTo(target);
            assertThat(Files.readString(target)).isEqualTo("content");
        }
    }

    @Nested
    @DisplayName("copyRecursively方法测试")
    class CopyRecursivelyTests {

        @Test
        @DisplayName("递归复制目录")
        void testCopyRecursively() throws Exception {
            Path sourceDir = tempDir.resolve("source");
            Path targetDir = tempDir.resolve("target");
            Files.createDirectories(sourceDir.resolve("sub"));
            Files.writeString(sourceDir.resolve("file.txt"), "content");
            Files.writeString(sourceDir.resolve("sub/nested.txt"), "nested");

            OpenIO.copyRecursively(sourceDir, targetDir);

            assertThat(Files.readString(targetDir.resolve("file.txt"))).isEqualTo("content");
            assertThat(Files.readString(targetDir.resolve("sub/nested.txt"))).isEqualTo("nested");
        }
    }

    @Nested
    @DisplayName("move方法测试")
    class MoveTests {

        @Test
        @DisplayName("移动文件")
        void testMove() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("target.txt");
            Files.writeString(source, "content");

            Path result = OpenIO.move(source, target);

            assertThat(result).isEqualTo(target);
            assertThat(Files.exists(source)).isFalse();
            assertThat(Files.readString(target)).isEqualTo("content");
        }
    }

    @Nested
    @DisplayName("size方法测试")
    class SizeTests {

        @Test
        @DisplayName("返回文件大小")
        void testSize() throws Exception {
            Path file = tempDir.resolve("test.txt");
            byte[] data = new byte[100];
            Files.write(file, data);

            long size = OpenIO.size(file);

            assertThat(size).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("directorySize方法测试")
    class DirectorySizeTests {

        @Test
        @DisplayName("返回目录总大小")
        void testDirectorySize() throws Exception {
            Path dir = tempDir.resolve("sizedir");
            Files.createDirectories(dir);
            Files.write(dir.resolve("file1.txt"), new byte[50]);
            Files.write(dir.resolve("file2.txt"), new byte[50]);

            long size = OpenIO.directorySize(dir);

            assertThat(size).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("getLastModifiedTime和setLastModifiedTime方法测试")
    class LastModifiedTimeTests {

        @Test
        @DisplayName("获取最后修改时间")
        void testGetLastModifiedTime() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);

            Instant time = OpenIO.getLastModifiedTime(file);

            assertThat(time).isNotNull();
        }

        @Test
        @DisplayName("设置最后修改时间")
        void testSetLastModifiedTime() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            Instant newTime = Instant.now().minusSeconds(3600);

            OpenIO.setLastModifiedTime(file, newTime);

            Instant result = OpenIO.getLastModifiedTime(file);
            assertThat(result).isEqualTo(newTime);
        }
    }

    @Nested
    @DisplayName("list方法测试")
    class ListTests {

        @Test
        @DisplayName("列出目录内容")
        void testList() throws Exception {
            Path dir = tempDir.resolve("listdir");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("a.txt"));
            Files.createFile(dir.resolve("b.txt"));

            try (Stream<Path> paths = OpenIO.list(dir)) {
                assertThat(paths.count()).isEqualTo(2);
            }
        }
    }

    @Nested
    @DisplayName("walk方法测试")
    class WalkTests {

        @Test
        @DisplayName("遍历目录树")
        void testWalk() throws Exception {
            Path dir = tempDir.resolve("walkdir");
            Files.createDirectories(dir.resolve("sub"));
            Files.createFile(dir.resolve("a.txt"));
            Files.createFile(dir.resolve("sub/b.txt"));

            try (Stream<Path> paths = OpenIO.walk(dir)) {
                assertThat(paths.count()).isGreaterThanOrEqualTo(3);
            }
        }

        @Test
        @DisplayName("限制深度遍历")
        void testWalkWithDepth() throws Exception {
            Path dir = tempDir.resolve("depthdir");
            Files.createDirectories(dir.resolve("sub"));
            Files.createFile(dir.resolve("a.txt"));
            Files.createFile(dir.resolve("sub/b.txt"));

            try (Stream<Path> paths = OpenIO.walk(dir, 1)) {
                long count = paths.count();
                assertThat(count).isLessThan(4);
            }
        }
    }

    @Nested
    @DisplayName("glob方法测试")
    class GlobTests {

        @Test
        @DisplayName("匹配glob模式")
        void testGlob() throws Exception {
            Path dir = tempDir.resolve("globdir");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("file1.txt"));
            Files.createFile(dir.resolve("file2.txt"));
            Files.createFile(dir.resolve("file.log"));

            try (Stream<Path> paths = OpenIO.glob(dir, "*.txt")) {
                assertThat(paths.count()).isEqualTo(2);
            }
        }
    }

    @Nested
    @DisplayName("find方法测试")
    class FindTests {

        @Test
        @DisplayName("匹配正则表达式")
        void testFindRegex() throws Exception {
            Path dir = tempDir.resolve("finddir");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("test1.txt"));
            Files.createFile(dir.resolve("test2.txt"));
            Files.createFile(dir.resolve("other.txt"));

            try (Stream<Path> paths = OpenIO.find(dir, "test\\d\\.txt")) {
                assertThat(paths.count()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("使用自定义匹配器")
        void testFindWithMatcher() throws Exception {
            Path dir = tempDir.resolve("matchdir");
            Files.createDirectory(dir);
            Files.write(dir.resolve("small.txt"), new byte[10]);
            Files.write(dir.resolve("large.txt"), new byte[100]);

            try (Stream<Path> paths = OpenIO.find(dir, Integer.MAX_VALUE,
                    (path, attrs) -> attrs.isRegularFile() && attrs.size() > 50)) {
                assertThat(paths.count()).isEqualTo(1);
            }
        }
    }
}
