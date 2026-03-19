package cloud.opencode.base.io.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenIOOperationException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("OpenIOOperationException 测试")
class OpenIOOperationExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("单参数构造函数")
        void testMessageConstructor() {
            OpenIOOperationException ex = new OpenIOOperationException("Test error");

            assertThat(ex.getMessage()).contains("Test error");
            assertThat(ex.operation()).isNull();
            assertThat(ex.path()).isNull();
        }

        @Test
        @DisplayName("带原因的构造函数")
        void testMessageAndCauseConstructor() {
            IOException cause = new IOException("IO error");
            OpenIOOperationException ex = new OpenIOOperationException("Test error", cause);

            assertThat(ex.getMessage()).contains("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.operation()).isNull();
            assertThat(ex.path()).isNull();
        }

        @Test
        @DisplayName("带操作和路径的构造函数")
        void testOperationPathMessageConstructor() {
            OpenIOOperationException ex = new OpenIOOperationException("read", "/test/path", "Test error");

            assertThat(ex.getMessage()).contains("Test error");
            assertThat(ex.operation()).isEqualTo("read");
            assertThat(ex.path()).isEqualTo("/test/path");
        }

        @Test
        @DisplayName("完整参数构造函数")
        void testFullConstructor() {
            IOException cause = new IOException("IO error");
            OpenIOOperationException ex = new OpenIOOperationException("write", "/test/path", "Test error", cause);

            assertThat(ex.getMessage()).contains("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.operation()).isEqualTo("write");
            assertThat(ex.path()).isEqualTo("/test/path");
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("fileNotFound方法")
        void testFileNotFound() {
            Path path = Path.of("/test/file.txt");
            OpenIOOperationException ex = OpenIOOperationException.fileNotFound(path);

            assertThat(ex.operation()).isEqualTo("read");
            assertThat(ex.path()).isEqualTo(path.toString());
            assertThat(ex.getMessage()).contains("File not found");
        }

        @Test
        @DisplayName("resourceNotFound方法")
        void testResourceNotFound() {
            OpenIOOperationException ex = OpenIOOperationException.resourceNotFound("config.properties");

            assertThat(ex.operation()).isEqualTo("load");
            assertThat(ex.path()).isEqualTo("config.properties");
            assertThat(ex.getMessage()).contains("Resource not found");
        }

        @Test
        @DisplayName("readFailed(Path, Throwable)方法")
        void testReadFailedWithPath() {
            Path path = Path.of("/test/file.txt");
            IOException cause = new IOException("Cannot read");
            OpenIOOperationException ex = OpenIOOperationException.readFailed(path, cause);

            assertThat(ex.operation()).isEqualTo("read");
            assertThat(ex.path()).isEqualTo(path.toString());
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("readFailed(Throwable)方法")
        void testReadFailedStream() {
            IOException cause = new IOException("Stream error");
            OpenIOOperationException ex = OpenIOOperationException.readFailed(cause);

            assertThat(ex.operation()).isEqualTo("read");
            assertThat(ex.path()).isNull();
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("writeFailed(Path, Throwable)方法")
        void testWriteFailedWithPath() {
            Path path = Path.of("/test/file.txt");
            IOException cause = new IOException("Cannot write");
            OpenIOOperationException ex = OpenIOOperationException.writeFailed(path, cause);

            assertThat(ex.operation()).isEqualTo("write");
            assertThat(ex.path()).isEqualTo(path.toString());
        }

        @Test
        @DisplayName("writeFailed(Throwable)方法")
        void testWriteFailedStream() {
            IOException cause = new IOException("Stream error");
            OpenIOOperationException ex = OpenIOOperationException.writeFailed(cause);

            assertThat(ex.operation()).isEqualTo("write");
            assertThat(ex.path()).isNull();
        }

        @Test
        @DisplayName("copyFailed方法")
        void testCopyFailed() {
            Path source = Path.of("/source/file.txt");
            Path target = Path.of("/target/file.txt");
            IOException cause = new IOException("Copy error");
            OpenIOOperationException ex = OpenIOOperationException.copyFailed(source, target, cause);

            assertThat(ex.operation()).isEqualTo("copy");
            assertThat(ex.path()).isEqualTo(source.toString());
            assertThat(ex.getMessage()).contains(source.toString()).contains(target.toString());
        }

        @Test
        @DisplayName("moveFailed方法")
        void testMoveFailed() {
            Path source = Path.of("/source/file.txt");
            Path target = Path.of("/target/file.txt");
            IOException cause = new IOException("Move error");
            OpenIOOperationException ex = OpenIOOperationException.moveFailed(source, target, cause);

            assertThat(ex.operation()).isEqualTo("move");
            assertThat(ex.getMessage()).contains("move");
        }

        @Test
        @DisplayName("deleteFailed方法")
        void testDeleteFailed() {
            Path path = Path.of("/test/file.txt");
            IOException cause = new IOException("Delete error");
            OpenIOOperationException ex = OpenIOOperationException.deleteFailed(path, cause);

            assertThat(ex.operation()).isEqualTo("delete");
            assertThat(ex.path()).isEqualTo(path.toString());
        }

        @Test
        @DisplayName("createFileFailed方法")
        void testCreateFileFailed() {
            Path path = Path.of("/test/file.txt");
            IOException cause = new IOException("Create error");
            OpenIOOperationException ex = OpenIOOperationException.createFileFailed(path, cause);

            assertThat(ex.operation()).isEqualTo("createFile");
            assertThat(ex.path()).isEqualTo(path.toString());
        }

        @Test
        @DisplayName("createDirectoryFailed方法")
        void testCreateDirectoryFailed() {
            Path path = Path.of("/test/dir");
            IOException cause = new IOException("Create dir error");
            OpenIOOperationException ex = OpenIOOperationException.createDirectoryFailed(path, cause);

            assertThat(ex.operation()).isEqualTo("createDirectory");
            assertThat(ex.path()).isEqualTo(path.toString());
        }

        @Test
        @DisplayName("streamOperationFailed方法")
        void testStreamOperationFailed() {
            IOException cause = new IOException("Stream error");
            OpenIOOperationException ex = OpenIOOperationException.streamOperationFailed("copy", cause);

            assertThat(ex.operation()).isEqualTo("copy");
            assertThat(ex.getMessage()).contains("Stream operation failed");
        }

        @Test
        @DisplayName("sizeLimitExceeded方法")
        void testSizeLimitExceeded() {
            OpenIOOperationException ex = OpenIOOperationException.sizeLimitExceeded(1000, 2000);

            assertThat(ex.operation()).isEqualTo("read");
            assertThat(ex.getMessage()).contains("1000").contains("2000");
        }

        @Test
        @DisplayName("invalidPath方法")
        void testInvalidPath() {
            IllegalArgumentException cause = new IllegalArgumentException("Bad path");
            OpenIOOperationException ex = OpenIOOperationException.invalidPath("invalid:path", cause);

            assertThat(ex.operation()).isEqualTo("path");
            assertThat(ex.path()).isEqualTo("invalid:path");
        }

        @Test
        @DisplayName("watchFailed方法")
        void testWatchFailed() {
            Path path = Path.of("/test/dir");
            IOException cause = new IOException("Watch error");
            OpenIOOperationException ex = OpenIOOperationException.watchFailed(path, cause);

            assertThat(ex.operation()).isEqualTo("watch");
            assertThat(ex.path()).isEqualTo(path.toString());
        }

        @Test
        @DisplayName("checksumFailed方法")
        void testChecksumFailed() {
            Exception cause = new Exception("Algorithm error");
            OpenIOOperationException ex = OpenIOOperationException.checksumFailed("SHA-512", cause);

            assertThat(ex.operation()).isEqualTo("checksum");
            assertThat(ex.getMessage()).contains("SHA-512");
        }
    }

    @Nested
    @DisplayName("异常继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承RuntimeException")
        void testIsRuntimeException() {
            OpenIOOperationException ex = new OpenIOOperationException("Test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
