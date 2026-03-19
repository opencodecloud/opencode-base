package cloud.opencode.base.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenIOException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenIOException 测试")
class OpenIOExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageConstructor() {
            OpenIOException ex = new OpenIOException("IO failed");

            assertThat(ex.getMessage()).isEqualTo("[Core] (IO_ERROR) IO failed");
            assertThat(ex.getComponent()).isEqualTo("Core");
            assertThat(ex.getErrorCode()).isEqualTo("IO_ERROR");
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            IOException cause = new IOException("Original");
            OpenIOException ex = new OpenIOException("IO failed", cause);

            assertThat(ex.getMessage()).isEqualTo("[Core] (IO_ERROR) IO failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("自定义错误码构造")
        void testCustomErrorCodeConstructor() {
            IOException cause = new IOException("Original");
            OpenIOException ex = new OpenIOException("CUSTOM_IO_ERROR", "Custom IO failed", cause);

            assertThat(ex.getMessage()).isEqualTo("[Core] (CUSTOM_IO_ERROR) Custom IO failed");
            assertThat(ex.getErrorCode()).isEqualTo("CUSTOM_IO_ERROR");
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("wrap 包装 IOException")
        void testWrap() {
            IOException cause = new IOException("Original error");
            OpenIOException ex = OpenIOException.wrap(cause);

            assertThat(ex.getMessage()).isEqualTo("[Core] (IO_ERROR) Original error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("readFailed(Path) 创建读取失败异常")
        void testReadFailedPath() {
            Path path = Path.of("/tmp/test.txt");
            IOException cause = new IOException("Original");
            OpenIOException ex = OpenIOException.readFailed(path, cause);

            assertThat(ex.getMessage()).contains("IO_READ_FAILED");
            assertThat(ex.getMessage()).contains("/tmp/test.txt");
            assertThat(ex.getErrorCode()).isEqualTo("IO_READ_FAILED");
        }

        @Test
        @DisplayName("readFailed(String) 创建读取失败异常")
        void testReadFailedString() {
            IOException cause = new IOException("Original");
            OpenIOException ex = OpenIOException.readFailed("classpath:config.yml", cause);

            assertThat(ex.getMessage()).contains("IO_READ_FAILED");
            assertThat(ex.getMessage()).contains("classpath:config.yml");
        }

        @Test
        @DisplayName("writeFailed(Path) 创建写入失败异常")
        void testWriteFailedPath() {
            Path path = Path.of("/tmp/output.txt");
            IOException cause = new IOException("Original");
            OpenIOException ex = OpenIOException.writeFailed(path, cause);

            assertThat(ex.getMessage()).contains("IO_WRITE_FAILED");
            assertThat(ex.getMessage()).contains("/tmp/output.txt");
            assertThat(ex.getErrorCode()).isEqualTo("IO_WRITE_FAILED");
        }

        @Test
        @DisplayName("writeFailed(String) 创建写入失败异常")
        void testWriteFailedString() {
            IOException cause = new IOException("Original");
            OpenIOException ex = OpenIOException.writeFailed("http://example.com/api", cause);

            assertThat(ex.getMessage()).contains("IO_WRITE_FAILED");
            assertThat(ex.getMessage()).contains("http://example.com/api");
        }

        @Test
        @DisplayName("fileNotFound(Path) 创建文件未找到异常")
        void testFileNotFoundPath() {
            Path path = Path.of("/nonexistent/file.txt");
            OpenIOException ex = OpenIOException.fileNotFound(path);

            assertThat(ex.getMessage()).contains("IO_FILE_NOT_FOUND");
            assertThat(ex.getMessage()).contains("/nonexistent/file.txt");
            assertThat(ex.getErrorCode()).isEqualTo("IO_FILE_NOT_FOUND");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("fileNotFound(String) 创建文件未找到异常")
        void testFileNotFoundString() {
            OpenIOException ex = OpenIOException.fileNotFound("missing-resource");

            assertThat(ex.getMessage()).contains("IO_FILE_NOT_FOUND");
            assertThat(ex.getMessage()).contains("missing-resource");
        }

        @Test
        @DisplayName("closeFailed 创建关闭失败异常")
        void testCloseFailed() {
            IOException cause = new IOException("Close error");
            OpenIOException ex = OpenIOException.closeFailed("DatabaseConnection", cause);

            assertThat(ex.getMessage()).contains("IO_CLOSE_FAILED");
            assertThat(ex.getMessage()).contains("DatabaseConnection");
            assertThat(ex.getErrorCode()).isEqualTo("IO_CLOSE_FAILED");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是 OpenException 的子类")
        void testExtendsOpenException() {
            OpenIOException ex = new OpenIOException("Test");
            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("是 RuntimeException 的子类")
        void testExtendsRuntimeException() {
            OpenIOException ex = new OpenIOException("Test");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
