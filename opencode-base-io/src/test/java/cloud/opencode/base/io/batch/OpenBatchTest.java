package cloud.opencode.base.io.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenBatch 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("OpenBatch 测试")
class OpenBatchTest {

    @TempDir
    Path tempDir;

    Path sourceDir;
    Path targetDir;

    @BeforeEach
    void setUp() throws IOException {
        sourceDir = tempDir.resolve("source");
        targetDir = tempDir.resolve("target");
        Files.createDirectories(sourceDir);
        Files.createDirectories(targetDir);
    }

    @Nested
    @DisplayName("copyAll方法测试")
    class CopyAllTests {

        @Test
        @DisplayName("复制所有文件")
        void testCopyAll() throws IOException {
            Path file1 = Files.createFile(sourceDir.resolve("file1.txt"));
            Path file2 = Files.createFile(sourceDir.resolve("file2.txt"));
            Files.writeString(file1, "content1");
            Files.writeString(file2, "content2");

            BatchResult result = OpenBatch.copyAll(List.of(file1, file2), targetDir);

            assertThat(result.isAllSuccess()).isTrue();
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(Files.exists(targetDir.resolve("file1.txt"))).isTrue();
            assertThat(Files.exists(targetDir.resolve("file2.txt"))).isTrue();
        }

        @Test
        @DisplayName("带过滤器复制")
        void testCopyAllWithFilter() throws IOException {
            Path file1 = Files.createFile(sourceDir.resolve("file1.txt"));
            Path file2 = Files.createFile(sourceDir.resolve("file2.log"));

            BatchResult result = OpenBatch.copyAll(
                List.of(file1, file2),
                targetDir,
                path -> path.toString().endsWith(".txt")
            );

            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.skippedCount()).isEqualTo(1);
            assertThat(Files.exists(targetDir.resolve("file1.txt"))).isTrue();
            assertThat(Files.exists(targetDir.resolve("file2.log"))).isFalse();
        }

        @Test
        @DisplayName("复制不存在的文件记录失败")
        void testCopyNonexistent() {
            Path nonexistent = sourceDir.resolve("nonexistent.txt");

            BatchResult result = OpenBatch.copyAll(List.of(nonexistent), targetDir);

            assertThat(result.hasFailures()).isTrue();
            assertThat(result.failureCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("带CopyOption复制")
        void testCopyAllWithOptions() throws IOException {
            Path source = Files.createFile(sourceDir.resolve("file.txt"));
            Path existing = Files.createFile(targetDir.resolve("file.txt"));
            Files.writeString(source, "new content");
            Files.writeString(existing, "old content");

            BatchResult result = OpenBatch.copyAll(
                List.of(source),
                targetDir,
                StandardCopyOption.REPLACE_EXISTING
            );

            assertThat(result.isAllSuccess()).isTrue();
            assertThat(Files.readString(targetDir.resolve("file.txt"))).isEqualTo("new content");
        }
    }

    @Nested
    @DisplayName("moveAll方法测试")
    class MoveAllTests {

        @Test
        @DisplayName("移动所有文件")
        void testMoveAll() throws IOException {
            Path file1 = Files.createFile(sourceDir.resolve("file1.txt"));
            Path file2 = Files.createFile(sourceDir.resolve("file2.txt"));

            BatchResult result = OpenBatch.moveAll(List.of(file1, file2), targetDir);

            assertThat(result.isAllSuccess()).isTrue();
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(Files.exists(file1)).isFalse();
            assertThat(Files.exists(file2)).isFalse();
            assertThat(Files.exists(targetDir.resolve("file1.txt"))).isTrue();
            assertThat(Files.exists(targetDir.resolve("file2.txt"))).isTrue();
        }

        @Test
        @DisplayName("带过滤器移动")
        void testMoveAllWithFilter() throws IOException {
            Path file1 = Files.createFile(sourceDir.resolve("move.txt"));
            Path file2 = Files.createFile(sourceDir.resolve("keep.txt"));

            BatchResult result = OpenBatch.moveAll(
                List.of(file1, file2),
                targetDir,
                path -> path.getFileName().toString().startsWith("move")
            );

            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.skippedCount()).isEqualTo(1);
            assertThat(Files.exists(file2)).isTrue();
        }
    }

    @Nested
    @DisplayName("deleteAll方法测试")
    class DeleteAllTests {

        @Test
        @DisplayName("删除所有文件")
        void testDeleteAll() throws IOException {
            Path file1 = Files.createFile(sourceDir.resolve("file1.txt"));
            Path file2 = Files.createFile(sourceDir.resolve("file2.txt"));

            BatchResult result = OpenBatch.deleteAll(List.of(file1, file2));

            assertThat(result.isAllSuccess()).isTrue();
            assertThat(Files.exists(file1)).isFalse();
            assertThat(Files.exists(file2)).isFalse();
        }

        @Test
        @DisplayName("带过滤器删除")
        void testDeleteAllWithFilter() throws IOException {
            Path file1 = Files.createFile(sourceDir.resolve("delete.tmp"));
            Path file2 = Files.createFile(sourceDir.resolve("keep.txt"));

            BatchResult result = OpenBatch.deleteAll(
                List.of(file1, file2),
                path -> path.toString().endsWith(".tmp")
            );

            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.skippedCount()).isEqualTo(1);
            assertThat(Files.exists(file1)).isFalse();
            assertThat(Files.exists(file2)).isTrue();
        }

        @Test
        @DisplayName("删除目录")
        void testDeleteDirectory() throws IOException {
            Path dir = Files.createDirectory(sourceDir.resolve("subdir"));
            Files.createFile(dir.resolve("file.txt"));

            BatchResult result = OpenBatch.deleteAll(List.of(dir));

            assertThat(result.isAllSuccess()).isTrue();
            assertThat(Files.exists(dir)).isFalse();
        }
    }

    @Nested
    @DisplayName("copyGlob方法测试")
    class CopyGlobTests {

        @Test
        @DisplayName("按glob模式复制")
        void testCopyGlob() throws IOException {
            Files.createFile(sourceDir.resolve("file1.txt"));
            Files.createFile(sourceDir.resolve("file2.txt"));
            Files.createFile(sourceDir.resolve("file3.log"));

            BatchResult result = OpenBatch.copyGlob(sourceDir, "*.txt", targetDir);

            assertThat(result.successCount()).isEqualTo(2);
            assertThat(Files.exists(targetDir.resolve("file1.txt"))).isTrue();
            assertThat(Files.exists(targetDir.resolve("file2.txt"))).isTrue();
            assertThat(Files.exists(targetDir.resolve("file3.log"))).isFalse();
        }
    }

    @Nested
    @DisplayName("moveGlob方法测试")
    class MoveGlobTests {

        @Test
        @DisplayName("按glob模式移动")
        void testMoveGlob() throws IOException {
            Files.createFile(sourceDir.resolve("data1.csv"));
            Files.createFile(sourceDir.resolve("data2.csv"));
            Files.createFile(sourceDir.resolve("other.txt"));

            BatchResult result = OpenBatch.moveGlob(sourceDir, "*.csv", targetDir);

            assertThat(result.successCount()).isEqualTo(2);
            assertThat(Files.exists(sourceDir.resolve("other.txt"))).isTrue();
        }
    }

    @Nested
    @DisplayName("deleteGlob方法测试")
    class DeleteGlobTests {

        @Test
        @DisplayName("按glob模式删除")
        void testDeleteGlob() throws IOException {
            Files.createFile(sourceDir.resolve("temp1.tmp"));
            Files.createFile(sourceDir.resolve("temp2.tmp"));
            Files.createFile(sourceDir.resolve("keep.txt"));

            BatchResult result = OpenBatch.deleteGlob(sourceDir, "*.tmp");

            assertThat(result.successCount()).isEqualTo(2);
            assertThat(Files.exists(sourceDir.resolve("keep.txt"))).isTrue();
        }
    }

    @Nested
    @DisplayName("parallel方法测试")
    class ParallelTests {

        @Test
        @DisplayName("并行复制")
        void testParallelCopy() throws IOException {
            for (int i = 0; i < 10; i++) {
                Files.createFile(sourceDir.resolve("file" + i + ".txt"));
            }
            List<Path> files = OpenBatch.collectFiles(sourceDir);

            BatchResult result = OpenBatch.parallel()
                .parallelism(4)
                .copyAll(files, targetDir);

            assertThat(result.isAllSuccess()).isTrue();
            assertThat(result.successCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("并行删除")
        void testParallelDelete() throws IOException {
            for (int i = 0; i < 10; i++) {
                Files.createFile(sourceDir.resolve("file" + i + ".txt"));
            }
            List<Path> files = OpenBatch.collectFiles(sourceDir);

            BatchResult result = OpenBatch.parallel()
                .parallelism(4)
                .deleteAll(files);

            assertThat(result.isAllSuccess()).isTrue();
            assertThat(result.successCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("并行移动")
        void testParallelMove() throws IOException {
            for (int i = 0; i < 5; i++) {
                Files.createFile(sourceDir.resolve("file" + i + ".txt"));
            }
            List<Path> files = OpenBatch.collectFiles(sourceDir);

            BatchResult result = OpenBatch.parallel()
                .moveAll(files, targetDir);

            assertThat(result.isAllSuccess()).isTrue();
            assertThat(result.successCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("带进度回调")
        void testWithProgressCallback() throws IOException {
            for (int i = 0; i < 5; i++) {
                Files.createFile(sourceDir.resolve("file" + i + ".txt"));
            }
            List<Path> files = OpenBatch.collectFiles(sourceDir);
            AtomicInteger progressCount = new AtomicInteger(0);

            BatchResult result = OpenBatch.parallel()
                .onProgress((path, current, total) -> progressCount.incrementAndGet())
                .copyAll(files, targetDir);

            assertThat(result.isAllSuccess()).isTrue();
            assertThat(progressCount.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("stopOnError选项")
        void testStopOnError() throws IOException {
            Path file = Files.createFile(sourceDir.resolve("file.txt"));
            Path nonexistent = sourceDir.resolve("nonexistent.txt");

            BatchResult result = OpenBatch.parallel()
                .stopOnError(true)
                .copyAll(List.of(nonexistent, file), targetDir);

            // 可能会有跳过的
            assertThat(result.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("自定义操作")
        void testCustomExecute() throws IOException {
            for (int i = 0; i < 3; i++) {
                Files.createFile(sourceDir.resolve("file" + i + ".txt"));
            }
            List<Path> files = OpenBatch.collectFiles(sourceDir);
            AtomicInteger counter = new AtomicInteger(0);

            BatchResult result = OpenBatch.parallel()
                .execute("custom", files, path -> counter.incrementAndGet());

            assertThat(result.isAllSuccess()).isTrue();
            assertThat(counter.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("空集合")
        void testEmptyCollection() {
            BatchResult result = OpenBatch.parallel()
                .copyAll(List.of(), targetDir);

            assertThat(result.totalCount()).isEqualTo(0);
            assertThat(result.isAllSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("collectFiles方法测试")
    class CollectFilesTests {

        @Test
        @DisplayName("收集目录中所有文件")
        void testCollectFiles() throws IOException {
            Files.createFile(sourceDir.resolve("file1.txt"));
            Files.createFile(sourceDir.resolve("file2.txt"));
            Path subdir = Files.createDirectory(sourceDir.resolve("subdir"));
            Files.createFile(subdir.resolve("file3.txt"));

            List<Path> files = OpenBatch.collectFiles(sourceDir);

            assertThat(files).hasSize(3);
        }
    }

    @Nested
    @DisplayName("collectGlob方法测试")
    class CollectGlobTests {

        @Test
        @DisplayName("按glob模式收集")
        void testCollectGlob() throws IOException {
            Files.createFile(sourceDir.resolve("file1.txt"));
            Files.createFile(sourceDir.resolve("file2.txt"));
            Files.createFile(sourceDir.resolve("other.log"));

            List<Path> files = OpenBatch.collectGlob(sourceDir, "*.txt");

            assertThat(files).hasSize(2);
        }
    }

    @Nested
    @DisplayName("collectByExtension方法测试")
    class CollectByExtensionTests {

        @Test
        @DisplayName("按扩展名收集")
        void testCollectByExtension() throws IOException {
            Files.createFile(sourceDir.resolve("file1.java"));
            Files.createFile(sourceDir.resolve("file2.java"));
            Files.createFile(sourceDir.resolve("file3.txt"));

            List<Path> files = OpenBatch.collectByExtension(sourceDir, "java");

            assertThat(files).hasSize(2);
        }

        @Test
        @DisplayName("多扩展名收集")
        void testCollectByMultipleExtensions() throws IOException {
            Files.createFile(sourceDir.resolve("file1.java"));
            Files.createFile(sourceDir.resolve("file2.txt"));
            Files.createFile(sourceDir.resolve("file3.xml"));

            List<Path> files = OpenBatch.collectByExtension(sourceDir, "java", "txt");

            assertThat(files).hasSize(2);
        }
    }

    @Nested
    @DisplayName("默认并行度测试")
    class DefaultParallelismTests {

        @Test
        @DisplayName("默认并行度等于处理器数量")
        void testDefaultParallelism() {
            assertThat(OpenBatch.DEFAULT_PARALLELISM)
                .isEqualTo(Runtime.getRuntime().availableProcessors());
        }
    }
}
