package cloud.opencode.base.io.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * FileCopier 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("FileCopier 测试")
class FileCopierTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("静态copy方法测试")
    class StaticCopyTests {

        @Test
        @DisplayName("复制文件")
        void testCopy() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("target.txt");
            Files.writeString(source, "content");

            Path result = FileCopier.copy(source, target);

            assertThat(result).isEqualTo(target);
            assertThat(Files.readString(target)).isEqualTo("content");
        }
    }

    @Nested
    @DisplayName("静态copyReplace方法测试")
    class StaticCopyReplaceTests {

        @Test
        @DisplayName("复制并替换")
        void testCopyReplace() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("target.txt");
            Files.writeString(source, "new content");
            Files.writeString(target, "old content");

            FileCopier.copyReplace(source, target);

            assertThat(Files.readString(target)).isEqualTo("new content");
        }
    }

    @Nested
    @DisplayName("from方法测试")
    class FromTests {

        @Test
        @DisplayName("从Path创建")
        void testFromPath() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Files.createFile(source);

            FileCopier copier = FileCopier.from(source);

            assertThat(copier.getSource()).isEqualTo(source);
        }

        @Test
        @DisplayName("从字符串创建")
        void testFromString() {
            String source = tempDir.resolve("source.txt").toString();

            FileCopier copier = FileCopier.from(source);

            assertThat(copier.getSource()).isNotNull();
        }
    }

    @Nested
    @DisplayName("to方法测试")
    class ToTests {

        @Test
        @DisplayName("设置Path目标")
        void testToPath() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("target.txt");
            Files.createFile(source);

            FileCopier copier = FileCopier.from(source).to(target);

            assertThat(copier.getTarget()).isEqualTo(target);
        }

        @Test
        @DisplayName("设置字符串目标")
        void testToString() throws Exception {
            Path source = tempDir.resolve("source.txt");
            String target = tempDir.resolve("target.txt").toString();
            Files.createFile(source);

            FileCopier copier = FileCopier.from(source).to(target);

            assertThat(copier.getTarget()).isNotNull();
        }
    }

    @Nested
    @DisplayName("replaceExisting方法测试")
    class ReplaceExistingTests {

        @Test
        @DisplayName("替换已存在的文件")
        void testReplaceExisting() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("target.txt");
            Files.writeString(source, "new");
            Files.writeString(target, "old");

            FileCopier.from(source).to(target).replaceExisting().execute();

            assertThat(Files.readString(target)).isEqualTo("new");
        }

        @Test
        @DisplayName("不替换时已存在文件抛出异常")
        void testNoReplaceExisting() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("target.txt");
            Files.createFile(source);
            Files.createFile(target);

            assertThatThrownBy(() -> FileCopier.from(source).to(target).execute())
                .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("copyAttributes方法测试")
    class CopyAttributesTests {

        @Test
        @DisplayName("复制文件属性")
        void testCopyAttributes() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("target.txt");
            Files.createFile(source);

            FileCopier.from(source).to(target).copyAttributes().execute();

            assertThat(Files.exists(target)).isTrue();
        }
    }

    @Nested
    @DisplayName("recursive方法测试")
    class RecursiveTests {

        @Test
        @DisplayName("递归复制目录")
        void testRecursiveCopy() throws Exception {
            Path sourceDir = tempDir.resolve("source");
            Path targetDir = tempDir.resolve("target");
            Files.createDirectories(sourceDir.resolve("sub"));
            Files.writeString(sourceDir.resolve("file.txt"), "content");
            Files.writeString(sourceDir.resolve("sub/nested.txt"), "nested");

            FileCopier.from(sourceDir).to(targetDir).recursive().execute();

            assertThat(Files.readString(targetDir.resolve("file.txt"))).isEqualTo("content");
            assertThat(Files.readString(targetDir.resolve("sub/nested.txt"))).isEqualTo("nested");
        }
    }

    @Nested
    @DisplayName("execute方法测试")
    class ExecuteTests {

        @Test
        @DisplayName("未设置目标抛出异常")
        void testExecuteNoTarget() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Files.createFile(source);

            assertThatThrownBy(() -> FileCopier.from(source).execute())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target path not set");
        }

        @Test
        @DisplayName("创建父目录")
        void testExecuteCreatesParent() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("sub/dir/target.txt");
            Files.writeString(source, "content");

            FileCopier.from(source).to(target).execute();

            assertThat(Files.exists(target)).isTrue();
            assertThat(Files.readString(target)).isEqualTo("content");
        }

        @Test
        @DisplayName("返回目标路径")
        void testExecuteReturnsTarget() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("target.txt");
            Files.createFile(source);

            Path result = FileCopier.from(source).to(target).execute();

            assertThat(result).isEqualTo(target);
        }
    }

    @Nested
    @DisplayName("getSource和getTarget方法测试")
    class GettersTests {

        @Test
        @DisplayName("getSource返回源路径")
        void testGetSource() {
            Path source = tempDir.resolve("source.txt");
            FileCopier copier = FileCopier.from(source);

            assertThat(copier.getSource()).isEqualTo(source);
        }

        @Test
        @DisplayName("getTarget返回目标路径")
        void testGetTarget() {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("target.txt");
            FileCopier copier = FileCopier.from(source).to(target);

            assertThat(copier.getTarget()).isEqualTo(target);
        }

        @Test
        @DisplayName("未设置时getTarget返回null")
        void testGetTargetNull() {
            Path source = tempDir.resolve("source.txt");
            FileCopier copier = FileCopier.from(source);

            assertThat(copier.getTarget()).isNull();
        }
    }

    @Nested
    @DisplayName("组合选项测试")
    class CombinedOptionsTests {

        @Test
        @DisplayName("replaceExisting和copyAttributes组合")
        void testReplaceAndCopyAttributes() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path target = tempDir.resolve("target.txt");
            Files.writeString(source, "new");
            Files.writeString(target, "old");

            FileCopier.from(source)
                .to(target)
                .replaceExisting()
                .copyAttributes()
                .execute();

            assertThat(Files.readString(target)).isEqualTo("new");
        }

        @Test
        @DisplayName("递归复制并替换")
        void testRecursiveAndReplace() throws Exception {
            Path sourceDir = tempDir.resolve("source");
            Path targetDir = tempDir.resolve("target");
            Files.createDirectories(sourceDir);
            Files.createDirectories(targetDir);
            Files.writeString(sourceDir.resolve("file.txt"), "new");
            Files.writeString(targetDir.resolve("file.txt"), "old");

            FileCopier.from(sourceDir)
                .to(targetDir)
                .recursive()
                .replaceExisting()
                .execute();

            assertThat(Files.readString(targetDir.resolve("file.txt"))).isEqualTo("new");
        }
    }
}
