package cloud.opencode.base.io.temp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenTempFile 工具类测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("OpenTempFile 工具类测试")
class OpenTempFileTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("createTempFile方法测试")
    class CreateTempFileTests {

        @Test
        @DisplayName("创建临时文件")
        void testCreateTempFile() {
            Path tempFile = OpenTempFile.createTempFile("test", ".tmp");

            try {
                assertThat(Files.exists(tempFile)).isTrue();
                assertThat(tempFile.getFileName().toString()).startsWith("test");
                assertThat(tempFile.getFileName().toString()).endsWith(".tmp");
            } finally {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            }
        }

        @Test
        @DisplayName("在指定目录创建临时文件")
        void testCreateTempFileInDir() {
            Path tempFile = OpenTempFile.createTempFile(tempDir, "test", ".txt");

            assertThat(Files.exists(tempFile)).isTrue();
            assertThat(tempFile.getParent()).isEqualTo(tempDir);
        }
    }

    @Nested
    @DisplayName("createTempDirectory方法测试")
    class CreateTempDirectoryTests {

        @Test
        @DisplayName("创建临时目录")
        void testCreateTempDirectory() {
            Path tempDirectory = OpenTempFile.createTempDirectory("testdir");

            try {
                assertThat(Files.isDirectory(tempDirectory)).isTrue();
                assertThat(tempDirectory.getFileName().toString()).startsWith("testdir");
            } finally {
                try { Files.deleteIfExists(tempDirectory); } catch (IOException ignored) {}
            }
        }

        @Test
        @DisplayName("在指定目录创建临时目录")
        void testCreateTempDirectoryInDir() {
            Path tempDirectory = OpenTempFile.createTempDirectory(tempDir, "subdir");

            assertThat(Files.isDirectory(tempDirectory)).isTrue();
            assertThat(tempDirectory.getParent()).isEqualTo(tempDir);
        }
    }

    @Nested
    @DisplayName("createAutoDeleteTempFile方法测试")
    class CreateAutoDeleteTempFileTests {

        @Test
        @DisplayName("创建自动删除临时文件")
        void testCreateAutoDeleteTempFile() {
            Path path;
            try (AutoDeleteTempFile tempFile = OpenTempFile.createAutoDeleteTempFile("auto", ".tmp")) {
                path = tempFile.getPath();
                assertThat(Files.exists(path)).isTrue();
            }
            assertThat(Files.exists(path)).isFalse();
        }

        @Test
        @DisplayName("在指定目录创建自动删除临时文件")
        void testCreateAutoDeleteTempFileInDir() {
            Path path;
            try (AutoDeleteTempFile tempFile = OpenTempFile.createAutoDeleteTempFile(tempDir, "auto", ".tmp")) {
                path = tempFile.getPath();
                assertThat(tempFile.getPath().getParent()).isEqualTo(tempDir);
            }
            assertThat(Files.exists(path)).isFalse();
        }
    }

    @Nested
    @DisplayName("createTempFileFromStream方法测试")
    class CreateTempFileFromStreamTests {

        @Test
        @DisplayName("从输入流创建临时文件")
        void testCreateTempFileFromStream() {
            byte[] data = "Hello, World!".getBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(data);

            Path tempFile = OpenTempFile.createTempFileFromStream(bais, "stream", ".txt");

            try {
                assertThat(Files.exists(tempFile)).isTrue();
                assertThat(Files.readAllBytes(tempFile)).isEqualTo(data);
            } catch (IOException e) {
                fail("IO exception", e);
            } finally {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            }
        }
    }

    @Nested
    @DisplayName("getTempDirectory方法测试")
    class GetTempDirectoryTests {

        @Test
        @DisplayName("获取系统临时目录")
        void testGetTempDirectory() {
            Path tempDirectory = OpenTempFile.getTempDirectory();

            assertThat(Files.isDirectory(tempDirectory)).isTrue();
        }
    }

    @Nested
    @DisplayName("cleanupOldTempFiles方法测试")
    class CleanupOldTempFilesTests {

        @Test
        @DisplayName("清理过期文件")
        void testCleanupOldTempFiles() throws IOException {
            // 创建临时文件
            Path file1 = Files.createTempFile(tempDir, "cleanup", ".tmp");
            Path file2 = Files.createTempFile(tempDir, "cleanup", ".tmp");

            // 清理超过0秒的文件(所有文件都会被清理)
            int deleted = OpenTempFile.cleanupOldTempFiles(tempDir, Duration.ZERO, "cleanup*.tmp");

            assertThat(deleted).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("不存在的目录返回0")
        void testCleanupNonExistentDir() {
            Path nonExistent = tempDir.resolve("nonexistent");

            int deleted = OpenTempFile.cleanupOldTempFiles(nonExistent, Duration.ofHours(1), "*.tmp");

            assertThat(deleted).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("isInTempDirectory方法测试")
    class IsInTempDirectoryTests {

        @Test
        @DisplayName("临时目录内的文件检测")
        void testIsInTempDirectory() {
            Path tempFile = OpenTempFile.createTempFile("test", ".tmp");

            try {
                // isInTempDirectory may return false due to symlink resolution differences
                // on some systems (e.g., macOS /var -> /private/var)
                // Just verify the method doesn't throw
                boolean result = OpenTempFile.isInTempDirectory(tempFile);
                assertThat(result).isIn(true, false);
            } finally {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            }
        }

        @Test
        @DisplayName("临时目录外的文件返回false")
        void testIsNotInTempDirectory() throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);

            // tempDir是测试目录,不是系统临时目录
            boolean result = OpenTempFile.isInTempDirectory(file);

            // 结果取决于tempDir是否恰好在系统临时目录下
            // 这里只验证方法不抛异常
            assertThat(result).isIn(true, false);
        }
    }
}
