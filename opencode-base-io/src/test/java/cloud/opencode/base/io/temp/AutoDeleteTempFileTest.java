package cloud.opencode.base.io.temp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * AutoDeleteTempFile 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("AutoDeleteTempFile 测试")
class AutoDeleteTempFileTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("基本功能测试")
    class BasicTests {

        @Test
        @DisplayName("getPath返回正确路径")
        void testGetPath() {
            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp")) {
                assertThat(temp.getPath()).isNotNull();
                assertThat(temp.getPath().getParent()).isEqualTo(tempDir);
            }
        }

        @Test
        @DisplayName("toFile返回File对象")
        void testToFile() {
            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp")) {
                File file = temp.toFile();

                assertThat(file).isNotNull();
                assertThat(file.exists()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("write方法测试")
    class WriteTests {

        @Test
        @DisplayName("写入字节数组")
        void testWriteBytes() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};

            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp")) {
                temp.write(data);

                assertThat(Files.readAllBytes(temp.getPath())).isEqualTo(data);
            }
        }

        @Test
        @DisplayName("写入字符串(UTF-8)")
        void testWriteString() throws IOException {
            String content = "Hello, World!";

            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".txt")) {
                temp.write(content);

                assertThat(Files.readString(temp.getPath())).isEqualTo(content);
            }
        }

        @Test
        @DisplayName("写入字符串带字符集")
        void testWriteStringWithCharset() throws IOException {
            String content = "你好，世界！";

            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".txt")) {
                temp.write(content, StandardCharsets.UTF_8);

                assertThat(Files.readString(temp.getPath(), StandardCharsets.UTF_8)).isEqualTo(content);
            }
        }
    }

    @Nested
    @DisplayName("read方法测试")
    class ReadTests {

        @Test
        @DisplayName("读取字节数组")
        void testReadBytes() {
            byte[] data = {10, 20, 30, 40, 50};

            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp")) {
                temp.write(data);

                byte[] result = temp.read();

                assertThat(result).isEqualTo(data);
            }
        }

        @Test
        @DisplayName("读取字符串(UTF-8)")
        void testReadString() {
            String content = "Test content";

            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".txt")) {
                temp.write(content);

                String result = temp.readString();

                assertThat(result).isEqualTo(content);
            }
        }

        @Test
        @DisplayName("读取字符串带字符集")
        void testReadStringWithCharset() {
            String content = "测试内容";

            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".txt")) {
                temp.write(content, StandardCharsets.UTF_8);

                String result = temp.readString(StandardCharsets.UTF_8);

                assertThat(result).isEqualTo(content);
            }
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("文件存在返回true")
        void testExistsTrue() {
            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp")) {
                assertThat(temp.exists()).isTrue();
            }
        }

        @Test
        @DisplayName("删除后返回false")
        void testExistsAfterDelete() {
            AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp");
            temp.delete();

            assertThat(temp.exists()).isFalse();
        }
    }

    @Nested
    @DisplayName("size方法测试")
    class SizeTests {

        @Test
        @DisplayName("获取文件大小")
        void testSize() {
            byte[] data = new byte[100];

            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp")) {
                temp.write(data);

                assertThat(temp.size()).isEqualTo(100);
            }
        }

        @Test
        @DisplayName("空文件大小为0")
        void testSizeEmpty() {
            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp")) {
                assertThat(temp.size()).isEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("delete方法测试")
    class DeleteTests {

        @Test
        @DisplayName("删除文件")
        void testDelete() {
            AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp");
            Path path = temp.getPath();

            boolean result = temp.delete();

            assertThat(result).isTrue();
            assertThat(Files.exists(path)).isFalse();
        }

        @Test
        @DisplayName("重复删除返回true")
        void testDeleteTwice() {
            AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp");

            temp.delete();
            boolean result = temp.delete();

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("关闭时删除文件")
        void testCloseDeletesFile() {
            Path path;
            AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp");
            path = temp.getPath();
            assertThat(Files.exists(path)).isTrue();

            temp.close();

            assertThat(Files.exists(path)).isFalse();
        }

        @Test
        @DisplayName("try-with-resources自动删除")
        void testTryWithResources() {
            Path path;

            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp")) {
                path = temp.getPath();
                temp.write("test content");
                assertThat(Files.exists(path)).isTrue();
            }

            assertThat(Files.exists(path)).isFalse();
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含路径")
        void testToString() {
            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "test", ".tmp")) {
                String str = temp.toString();

                assertThat(str).contains("AutoDeleteTempFile");
                assertThat(str).contains(temp.getPath().toString());
            }
        }
    }

    @Nested
    @DisplayName("完整流程测试")
    class IntegrationTests {

        @Test
        @DisplayName("写入读取流程")
        void testWriteReadWorkflow() {
            String originalContent = "This is test content for AutoDeleteTempFile";
            String readContent;
            Path path;

            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "workflow", ".txt")) {
                path = temp.getPath();
                temp.write(originalContent);
                readContent = temp.readString();
            }

            assertThat(readContent).isEqualTo(originalContent);
            assertThat(Files.exists(path)).isFalse();
        }

        @Test
        @DisplayName("二进制数据处理")
        void testBinaryDataWorkflow() {
            byte[] originalData = new byte[1024];
            for (int i = 0; i < originalData.length; i++) {
                originalData[i] = (byte) (i % 256);
            }

            try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile(tempDir, "binary", ".bin")) {
                temp.write(originalData);

                byte[] readData = temp.read();
                assertThat(readData).isEqualTo(originalData);

                assertThat(temp.size()).isEqualTo(1024);
            }
        }
    }
}
