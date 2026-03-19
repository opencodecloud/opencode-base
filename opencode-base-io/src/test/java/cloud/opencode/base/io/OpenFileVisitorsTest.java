package cloud.opencode.base.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenFileVisitors 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("OpenFileVisitors 测试")
class OpenFileVisitorsTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("deleteVisitor方法测试")
    class DeleteVisitorTests {

        @Test
        @DisplayName("删除目录及内容")
        void testDeleteVisitor() throws Exception {
            Path dir = tempDir.resolve("todelete");
            Files.createDirectories(dir.resolve("sub"));
            Files.createFile(dir.resolve("file.txt"));
            Files.createFile(dir.resolve("sub/nested.txt"));

            FileVisitor<Path> visitor = OpenFileVisitors.deleteVisitor();
            Files.walkFileTree(dir, visitor);

            assertThat(Files.exists(dir)).isFalse();
        }

        @Test
        @DisplayName("删除空目录")
        void testDeleteVisitorEmptyDir() throws Exception {
            Path dir = tempDir.resolve("emptydir");
            Files.createDirectory(dir);

            FileVisitor<Path> visitor = OpenFileVisitors.deleteVisitor();
            Files.walkFileTree(dir, visitor);

            assertThat(Files.exists(dir)).isFalse();
        }
    }

    @Nested
    @DisplayName("copyVisitor方法测试")
    class CopyVisitorTests {

        @Test
        @DisplayName("复制目录及内容")
        void testCopyVisitor() throws Exception {
            Path sourceDir = tempDir.resolve("source");
            Path targetDir = tempDir.resolve("target");
            Files.createDirectories(sourceDir.resolve("sub"));
            Files.writeString(sourceDir.resolve("file.txt"), "content");
            Files.writeString(sourceDir.resolve("sub/nested.txt"), "nested");

            FileVisitor<Path> visitor = OpenFileVisitors.copyVisitor(sourceDir, targetDir);
            Files.walkFileTree(sourceDir, visitor);

            assertThat(Files.readString(targetDir.resolve("file.txt"))).isEqualTo("content");
            assertThat(Files.readString(targetDir.resolve("sub/nested.txt"))).isEqualTo("nested");
        }

        @Test
        @DisplayName("复制并替换")
        void testCopyVisitorWithReplace() throws Exception {
            Path sourceDir = tempDir.resolve("source");
            Path targetDir = tempDir.resolve("target");
            Files.createDirectories(sourceDir);
            Files.createDirectories(targetDir);
            Files.writeString(sourceDir.resolve("file.txt"), "new");
            Files.writeString(targetDir.resolve("file.txt"), "old");

            FileVisitor<Path> visitor = OpenFileVisitors.copyVisitor(sourceDir, targetDir, StandardCopyOption.REPLACE_EXISTING);
            Files.walkFileTree(sourceDir, visitor);

            assertThat(Files.readString(targetDir.resolve("file.txt"))).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("actionVisitor方法测试")
    class ActionVisitorTests {

        @Test
        @DisplayName("对每个文件执行操作")
        void testActionVisitor() throws Exception {
            Path dir = tempDir.resolve("actiondir");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("file1.txt"));
            Files.createFile(dir.resolve("file2.txt"));

            List<Path> visited = new ArrayList<>();
            FileVisitor<Path> visitor = OpenFileVisitors.actionVisitor(visited::add);
            Files.walkFileTree(dir, visitor);

            assertThat(visited).hasSize(2);
        }
    }

    @Nested
    @DisplayName("filteredVisitor方法测试")
    class FilteredVisitorTests {

        @Test
        @DisplayName("只处理匹配的文件")
        void testFilteredVisitor() throws Exception {
            Path dir = tempDir.resolve("filterdir");
            Files.createDirectory(dir);
            Files.write(dir.resolve("small.txt"), new byte[10]);
            Files.write(dir.resolve("large.txt"), new byte[100]);

            List<Path> visited = new ArrayList<>();
            FileVisitor<Path> visitor = OpenFileVisitors.filteredVisitor(
                (path, attrs) -> attrs.size() > 50,
                visited::add
            );
            Files.walkFileTree(dir, visitor);

            assertThat(visited).hasSize(1);
            assertThat(visited.get(0).getFileName().toString()).isEqualTo("large.txt");
        }

        @Test
        @DisplayName("过滤文件类型")
        void testFilteredVisitorByExtension() throws Exception {
            Path dir = tempDir.resolve("extdir");
            Files.createDirectory(dir);
            Files.createFile(dir.resolve("file.txt"));
            Files.createFile(dir.resolve("file.log"));

            List<Path> visited = new ArrayList<>();
            FileVisitor<Path> visitor = OpenFileVisitors.filteredVisitor(
                (path, attrs) -> path.toString().endsWith(".txt"),
                visited::add
            );
            Files.walkFileTree(dir, visitor);

            assertThat(visited).hasSize(1);
        }
    }

    @Nested
    @DisplayName("countingVisitor方法测试")
    class CountingVisitorTests {

        @Test
        @DisplayName("统计文件数量")
        void testCountingVisitorFileCount() throws Exception {
            Path dir = tempDir.resolve("countdir");
            Files.createDirectories(dir.resolve("sub"));
            Files.createFile(dir.resolve("file1.txt"));
            Files.createFile(dir.resolve("file2.txt"));
            Files.createFile(dir.resolve("sub/file3.txt"));

            OpenFileVisitors.CountingVisitor visitor = OpenFileVisitors.countingVisitor();
            Files.walkFileTree(dir, visitor);

            assertThat(visitor.getFileCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("统计目录数量")
        void testCountingVisitorDirectoryCount() throws Exception {
            Path dir = tempDir.resolve("countdir2");
            Files.createDirectories(dir.resolve("sub1"));
            Files.createDirectories(dir.resolve("sub2"));

            OpenFileVisitors.CountingVisitor visitor = OpenFileVisitors.countingVisitor();
            Files.walkFileTree(dir, visitor);

            assertThat(visitor.getDirectoryCount()).isEqualTo(3); // dir + sub1 + sub2
        }

        @Test
        @DisplayName("统计总数量")
        void testCountingVisitorTotalCount() throws Exception {
            Path dir = tempDir.resolve("countdir3");
            Files.createDirectories(dir.resolve("sub"));
            Files.createFile(dir.resolve("file1.txt"));
            Files.createFile(dir.resolve("sub/file2.txt"));

            OpenFileVisitors.CountingVisitor visitor = OpenFileVisitors.countingVisitor();
            Files.walkFileTree(dir, visitor);

            assertThat(visitor.getTotalCount()).isEqualTo(visitor.getFileCount() + visitor.getDirectoryCount());
        }
    }

    @Nested
    @DisplayName("sizeVisitor方法测试")
    class SizeVisitorTests {

        @Test
        @DisplayName("计算总大小")
        void testSizeVisitor() throws Exception {
            Path dir = tempDir.resolve("sizedir");
            Files.createDirectory(dir);
            Files.write(dir.resolve("file1.txt"), new byte[50]);
            Files.write(dir.resolve("file2.txt"), new byte[100]);

            OpenFileVisitors.SizeVisitor visitor = OpenFileVisitors.sizeVisitor();
            Files.walkFileTree(dir, visitor);

            assertThat(visitor.getTotalSize()).isEqualTo(150);
        }

        @Test
        @DisplayName("空目录大小为0")
        void testSizeVisitorEmptyDir() throws Exception {
            Path dir = tempDir.resolve("emptysizedir");
            Files.createDirectory(dir);

            OpenFileVisitors.SizeVisitor visitor = OpenFileVisitors.sizeVisitor();
            Files.walkFileTree(dir, visitor);

            assertThat(visitor.getTotalSize()).isEqualTo(0);
        }

        @Test
        @DisplayName("递归计算嵌套目录大小")
        void testSizeVisitorNested() throws Exception {
            Path dir = tempDir.resolve("nestedir");
            Files.createDirectories(dir.resolve("sub"));
            Files.write(dir.resolve("a.txt"), new byte[30]);
            Files.write(dir.resolve("sub/b.txt"), new byte[70]);

            OpenFileVisitors.SizeVisitor visitor = OpenFileVisitors.sizeVisitor();
            Files.walkFileTree(dir, visitor);

            assertThat(visitor.getTotalSize()).isEqualTo(100);
        }
    }
}
