package cloud.opencode.base.io;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenPath 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("OpenPath 测试")
class OpenPathTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("getExtension(Path)方法测试")
    class GetExtensionPathTests {

        @Test
        @DisplayName("返回扩展名")
        void testGetExtension() {
            Path path = Path.of("file.txt");

            String ext = OpenPath.getExtension(path);

            assertThat(ext).isEqualTo("txt");
        }

        @Test
        @DisplayName("无扩展名返回空字符串")
        void testGetExtensionNoExt() {
            Path path = Path.of("noextension");

            String ext = OpenPath.getExtension(path);

            assertThat(ext).isEmpty();
        }

        @Test
        @DisplayName("隐藏文件返回空字符串")
        void testGetExtensionHiddenFile() {
            Path path = Path.of(".hidden");

            String ext = OpenPath.getExtension(path);

            assertThat(ext).isEmpty();
        }

        @Test
        @DisplayName("以点结尾返回空字符串")
        void testGetExtensionEndsWithDot() {
            Path path = Path.of("file.");

            String ext = OpenPath.getExtension(path);

            assertThat(ext).isEmpty();
        }
    }

    @Nested
    @DisplayName("getExtension(String)方法测试")
    class GetExtensionStringTests {

        @Test
        @DisplayName("返回扩展名")
        void testGetExtension() {
            String ext = OpenPath.getExtension("file.txt");

            assertThat(ext).isEqualTo("txt");
        }

        @Test
        @DisplayName("null返回空字符串")
        void testGetExtensionNull() {
            String ext = OpenPath.getExtension((String) null);

            assertThat(ext).isEmpty();
        }

        @Test
        @DisplayName("空字符串返回空字符串")
        void testGetExtensionEmpty() {
            String ext = OpenPath.getExtension("");

            assertThat(ext).isEmpty();
        }

        @Test
        @DisplayName("多个点返回最后的扩展名")
        void testGetExtensionMultipleDots() {
            String ext = OpenPath.getExtension("archive.tar.gz");

            assertThat(ext).isEqualTo("gz");
        }
    }

    @Nested
    @DisplayName("getNameWithoutExtension(Path)方法测试")
    class GetNameWithoutExtensionPathTests {

        @Test
        @DisplayName("返回不含扩展名的文件名")
        void testGetNameWithoutExtension() {
            Path path = Path.of("file.txt");

            String name = OpenPath.getNameWithoutExtension(path);

            assertThat(name).isEqualTo("file");
        }

        @Test
        @DisplayName("无扩展名返回完整文件名")
        void testGetNameWithoutExtensionNoExt() {
            Path path = Path.of("noextension");

            String name = OpenPath.getNameWithoutExtension(path);

            assertThat(name).isEqualTo("noextension");
        }

        @Test
        @DisplayName("隐藏文件返回完整文件名")
        void testGetNameWithoutExtensionHiddenFile() {
            Path path = Path.of(".hidden");

            String name = OpenPath.getNameWithoutExtension(path);

            assertThat(name).isEqualTo(".hidden");
        }
    }

    @Nested
    @DisplayName("getNameWithoutExtension(String)方法测试")
    class GetNameWithoutExtensionStringTests {

        @Test
        @DisplayName("返回不含扩展名的文件名")
        void testGetNameWithoutExtension() {
            String name = OpenPath.getNameWithoutExtension("file.txt");

            assertThat(name).isEqualTo("file");
        }

        @Test
        @DisplayName("null返回空字符串")
        void testGetNameWithoutExtensionNull() {
            String name = OpenPath.getNameWithoutExtension((String) null);

            assertThat(name).isEmpty();
        }

        @Test
        @DisplayName("空字符串返回空字符串")
        void testGetNameWithoutExtensionEmpty() {
            String name = OpenPath.getNameWithoutExtension("");

            assertThat(name).isEmpty();
        }
    }

    @Nested
    @DisplayName("changeExtension方法测试")
    class ChangeExtensionTests {

        @Test
        @DisplayName("更改扩展名")
        void testChangeExtension() {
            Path path = tempDir.resolve("file.txt");

            Path result = OpenPath.changeExtension(path, "md");

            assertThat(result.getFileName().toString()).isEqualTo("file.md");
        }

        @Test
        @DisplayName("移除扩展名")
        void testChangeExtensionToEmpty() {
            Path path = tempDir.resolve("file.txt");

            Path result = OpenPath.changeExtension(path, "");

            assertThat(result.getFileName().toString()).isEqualTo("file");
        }

        @Test
        @DisplayName("无父目录时更改扩展名")
        void testChangeExtensionNoParent() {
            Path path = Path.of("file.txt");

            Path result = OpenPath.changeExtension(path, "md");

            assertThat(result.toString()).isEqualTo("file.md");
        }
    }

    @Nested
    @DisplayName("normalize方法测试")
    class NormalizeTests {

        @Test
        @DisplayName("规范化路径")
        void testNormalize() {
            Path path = Path.of("a/b/../c");

            Path result = OpenPath.normalize(path);

            assertThat(result.toString()).isEqualTo("a/c");
        }
    }

    @Nested
    @DisplayName("relativize方法测试")
    class RelativizeTests {

        @Test
        @DisplayName("获取相对路径")
        void testRelativize() {
            Path base = tempDir;
            Path target = tempDir.resolve("sub/file.txt");

            Path result = OpenPath.relativize(base, target);

            assertThat(result.toString()).isEqualTo("sub/file.txt");
        }
    }

    @Nested
    @DisplayName("resolve方法测试")
    class ResolveTests {

        @Test
        @DisplayName("解析路径")
        void testResolve() {
            Path base = tempDir;

            Path result = OpenPath.resolve(base, "file.txt");

            assertThat(result).isEqualTo(tempDir.resolve("file.txt"));
        }
    }

    @Nested
    @DisplayName("getParent方法测试")
    class GetParentTests {

        @Test
        @DisplayName("获取父目录")
        void testGetParent() {
            Path path = tempDir.resolve("file.txt");

            Path parent = OpenPath.getParent(path);

            assertThat(parent).isEqualTo(tempDir);
        }
    }

    @Nested
    @DisplayName("getRoot方法测试")
    class GetRootTests {

        @Test
        @DisplayName("获取根路径")
        void testGetRoot() {
            Path path = tempDir.toAbsolutePath();

            Path root = OpenPath.getRoot(path);

            assertThat(root).isNotNull();
        }
    }

    @Nested
    @DisplayName("isAbsolute方法测试")
    class IsAbsoluteTests {

        @Test
        @DisplayName("绝对路径返回true")
        void testIsAbsoluteTrue() {
            Path path = tempDir.toAbsolutePath();

            assertThat(OpenPath.isAbsolute(path)).isTrue();
        }

        @Test
        @DisplayName("相对路径返回false")
        void testIsAbsoluteFalse() {
            Path path = Path.of("relative/path");

            assertThat(OpenPath.isAbsolute(path)).isFalse();
        }
    }

    @Nested
    @DisplayName("toAbsolute方法测试")
    class ToAbsoluteTests {

        @Test
        @DisplayName("转换为绝对路径")
        void testToAbsolute() {
            Path path = Path.of("relative");

            Path result = OpenPath.toAbsolute(path);

            assertThat(result.isAbsolute()).isTrue();
        }
    }

    @Nested
    @DisplayName("toRealPath方法测试")
    class ToRealPathTests {

        @Test
        @DisplayName("获取真实路径")
        void testToRealPath() throws Exception {
            Path file = tempDir.resolve("real.txt");
            Files.createFile(file);

            Path result = OpenPath.toRealPath(file);

            assertThat(result).isNotNull();
            assertThat(result.isAbsolute()).isTrue();
        }

        @Test
        @DisplayName("不存在的文件抛出异常")
        void testToRealPathNotExists() {
            Path path = tempDir.resolve("notexists.txt");

            assertThatThrownBy(() -> OpenPath.toRealPath(path))
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("isSubPath方法测试")
    class IsSubPathTests {

        @Test
        @DisplayName("子路径返回true")
        void testIsSubPathTrue() {
            Path parent = tempDir;
            Path child = tempDir.resolve("sub/file.txt");

            assertThat(OpenPath.isSubPath(parent, child)).isTrue();
        }

        @Test
        @DisplayName("非子路径返回false")
        void testIsSubPathFalse() {
            Path parent = tempDir.resolve("a");
            Path child = tempDir.resolve("b");

            assertThat(OpenPath.isSubPath(parent, child)).isFalse();
        }
    }

    @Nested
    @DisplayName("uniqueFile方法测试")
    class UniqueFileTests {

        @Test
        @DisplayName("生成唯一文件名")
        void testUniqueFile() {
            Path result = OpenPath.uniqueFile(tempDir, "test", "txt");

            assertThat(result.getFileName().toString()).isEqualTo("test.txt");
        }

        @Test
        @DisplayName("文件存在时生成带计数器的文件名")
        void testUniqueFileExists() throws Exception {
            Files.createFile(tempDir.resolve("test.txt"));

            Path result = OpenPath.uniqueFile(tempDir, "test", "txt");

            assertThat(result.getFileName().toString()).isEqualTo("test_1.txt");
        }

        @Test
        @DisplayName("扩展名带点也能正确处理")
        void testUniqueFileWithDot() {
            Path result = OpenPath.uniqueFile(tempDir, "test", ".txt");

            assertThat(result.getFileName().toString()).isEqualTo("test.txt");
        }
    }

    @Nested
    @DisplayName("getFileName方法测试")
    class GetFileNameTests {

        @Test
        @DisplayName("获取文件名")
        void testGetFileName() {
            Path path = tempDir.resolve("file.txt");

            String name = OpenPath.getFileName(path);

            assertThat(name).isEqualTo("file.txt");
        }

        @Test
        @DisplayName("根路径返回空字符串")
        void testGetFileNameRoot() {
            Path root = Path.of("/");

            String name = OpenPath.getFileName(root);

            assertThat(name).isEmpty();
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

            assertThat(OpenPath.isSameFile(file, file)).isTrue();
        }

        @Test
        @DisplayName("不同文件返回false")
        void testIsSameFileFalse() throws Exception {
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.createFile(file1);
            Files.createFile(file2);

            assertThat(OpenPath.isSameFile(file1, file2)).isFalse();
        }
    }

    @Nested
    @DisplayName("getNameCount方法测试")
    class GetNameCountTests {

        @Test
        @DisplayName("获取路径名称数量")
        void testGetNameCount() {
            Path path = Path.of("a/b/c/file.txt");

            int count = OpenPath.getNameCount(path);

            assertThat(count).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("join方法测试")
    class JoinTests {

        @Test
        @DisplayName("连接字符串路径")
        void testJoinStrings() {
            Path result = OpenPath.join("a", "b", "c");

            assertThat(result.toString()).isEqualTo("a/b/c");
        }

        @Test
        @DisplayName("连接Path与字符串")
        void testJoinPathWithStrings() {
            Path result = OpenPath.join(tempDir, "sub", "file.txt");

            assertThat(result).isEqualTo(tempDir.resolve("sub/file.txt"));
        }
    }
}
