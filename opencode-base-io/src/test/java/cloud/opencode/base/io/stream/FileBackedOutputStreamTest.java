package cloud.opencode.base.io.stream;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * FileBackedOutputStream 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
@DisplayName("FileBackedOutputStream 测试")
class FileBackedOutputStreamTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("正阈值创建成功")
        void testValidThreshold() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(100)) {
                assertThat(out.size()).isEqualTo(0);
                assertThat(out.isInMemory()).isTrue();
            }
        }

        @Test
        @DisplayName("零阈值抛出异常")
        void testZeroThreshold() {
            assertThatThrownBy(() -> new FileBackedOutputStream(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("负阈值抛出异常")
        void testNegativeThreshold() {
            assertThatThrownBy(() -> new FileBackedOutputStream(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("自定义临时目录")
        void testCustomTempDir(@TempDir Path tempDir) throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(10, tempDir)) {
                // Write enough to spill to disk
                out.write(new byte[20]);
                assertThat(out.isInMemory()).isFalse();
                assertThat(out.getFile()).isNotNull();
                assertThat(out.getFile().getParent()).isEqualTo(tempDir);
            }
        }
    }

    @Nested
    @DisplayName("内存模式写入测试")
    class InMemoryWriteTests {

        @Test
        @DisplayName("写入低于阈值保持内存模式")
        void testWriteBelowThreshold() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(100)) {
                out.write(new byte[]{1, 2, 3, 4, 5});

                assertThat(out.isInMemory()).isTrue();
                assertThat(out.size()).isEqualTo(5);
                assertThat(out.getFile()).isNull();
            }
        }

        @Test
        @DisplayName("写入单字节")
        void testWriteSingleByte() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(100)) {
                out.write(42);

                assertThat(out.size()).isEqualTo(1);
                assertThat(out.isInMemory()).isTrue();

                InputStream in = out.getInputStream();
                assertThat(in.read()).isEqualTo(42);
            }
        }

        @Test
        @DisplayName("多次写入累积低于阈值")
        void testMultipleWritesBelowThreshold() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(100)) {
                out.write(new byte[]{1, 2, 3});
                out.write(new byte[]{4, 5, 6});
                out.write(new byte[]{7, 8, 9});

                assertThat(out.isInMemory()).isTrue();
                assertThat(out.size()).isEqualTo(9);
            }
        }

        @Test
        @DisplayName("正好在阈值边界保持内存模式")
        void testExactThreshold() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(10)) {
                out.write(new byte[10]);

                assertThat(out.isInMemory()).isTrue();
                assertThat(out.size()).isEqualTo(10);
            }
        }
    }

    @Nested
    @DisplayName("溢出到磁盘测试")
    class SpillToDiskTests {

        @Test
        @DisplayName("超过阈值溢出到磁盘")
        void testSpillToDisk() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(10)) {
                out.write(new byte[11]);

                assertThat(out.isInMemory()).isFalse();
                assertThat(out.size()).isEqualTo(11);
                assertThat(out.getFile()).isNotNull();
                assertThat(Files.exists(out.getFile())).isTrue();
            }
        }

        @Test
        @DisplayName("多次写入累积超过阈值")
        void testMultipleWritesExceedingThreshold() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(10)) {
                out.write(new byte[]{1, 2, 3, 4, 5});
                assertThat(out.isInMemory()).isTrue();

                out.write(new byte[]{6, 7, 8, 9, 10});
                assertThat(out.isInMemory()).isTrue();

                out.write(new byte[]{11});
                assertThat(out.isInMemory()).isFalse();
            }
        }

        @Test
        @DisplayName("溢出后继续写入到文件")
        void testWriteAfterSpill() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(10)) {
                out.write(new byte[15]);
                out.write(new byte[]{1, 2, 3});

                assertThat(out.isInMemory()).isFalse();
                assertThat(out.size()).isEqualTo(18);
            }
        }

        @Test
        @DisplayName("单字节写入超过阈值")
        void testSingleByteSpill() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(5)) {
                for (int i = 0; i < 6; i++) {
                    out.write(i);
                }

                assertThat(out.isInMemory()).isFalse();
                assertThat(out.size()).isEqualTo(6);
            }
        }
    }

    @Nested
    @DisplayName("getInputStream方法测试")
    class GetInputStreamTests {

        @Test
        @DisplayName("内存模式获取InputStream")
        void testGetInputStreamInMemory() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(100)) {
                byte[] data = {10, 20, 30, 40, 50};
                out.write(data);

                InputStream in = out.getInputStream();
                byte[] result = in.readAllBytes();
                in.close();

                assertThat(result).containsExactly(10, 20, 30, 40, 50);
            }
        }

        @Test
        @DisplayName("磁盘模式获取InputStream")
        void testGetInputStreamOnDisk() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(5)) {
                byte[] data = new byte[20];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) (i + 1);
                }
                out.write(data);
                out.flush();

                InputStream in = out.getInputStream();
                byte[] result = in.readAllBytes();
                in.close();

                assertThat(result).hasSize(20);
                assertThat(result[0]).isEqualTo((byte) 1);
                assertThat(result[19]).isEqualTo((byte) 20);
            }
        }

        @Test
        @DisplayName("空流获取InputStream")
        void testGetInputStreamEmpty() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(100)) {
                InputStream in = out.getInputStream();
                byte[] result = in.readAllBytes();
                in.close();

                assertThat(result).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("size方法测试")
    class SizeTests {

        @Test
        @DisplayName("初始大小为0")
        void testInitialSize() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(100)) {
                assertThat(out.size()).isEqualTo(0);
            }
        }

        @Test
        @DisplayName("内存模式大小跟踪")
        void testSizeInMemory() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(100)) {
                out.write(new byte[50]);
                assertThat(out.size()).isEqualTo(50);

                out.write(new byte[30]);
                assertThat(out.size()).isEqualTo(80);
            }
        }

        @Test
        @DisplayName("溢出后大小跟踪")
        void testSizeAfterSpill() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(10)) {
                out.write(new byte[15]);
                assertThat(out.size()).isEqualTo(15);

                out.write(new byte[10]);
                assertThat(out.size()).isEqualTo(25);
            }
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("内存模式重置")
        void testResetInMemory() throws IOException {
            FileBackedOutputStream out = new FileBackedOutputStream(100);
            out.write(new byte[50]);

            out.reset();

            assertThat(out.size()).isEqualTo(0);
            assertThat(out.isInMemory()).isTrue();
            assertThat(out.getFile()).isNull();
        }

        @Test
        @DisplayName("磁盘模式重置删除临时文件")
        void testResetDeletesTempFile() throws IOException {
            FileBackedOutputStream out = new FileBackedOutputStream(10);
            out.write(new byte[20]);
            Path tempFile = out.getFile();
            assertThat(tempFile).isNotNull();
            assertThat(Files.exists(tempFile)).isTrue();

            out.reset();

            assertThat(out.size()).isEqualTo(0);
            assertThat(out.isInMemory()).isTrue();
            assertThat(out.getFile()).isNull();
            assertThat(Files.exists(tempFile)).isFalse();
        }

        @Test
        @DisplayName("重置后可继续写入")
        void testWriteAfterReset() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(100)) {
                out.write(new byte[]{1, 2, 3});
                out.reset();
                out.write(new byte[]{4, 5});

                assertThat(out.size()).isEqualTo(2);

                InputStream in = out.getInputStream();
                byte[] result = in.readAllBytes();
                in.close();

                assertThat(result).containsExactly(4, 5);
            }
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("关闭删除临时文件")
        void testCloseDeletesTempFile() throws IOException {
            FileBackedOutputStream out = new FileBackedOutputStream(10);
            out.write(new byte[20]);
            Path tempFile = out.getFile();
            assertThat(Files.exists(tempFile)).isTrue();

            out.close();

            assertThat(Files.exists(tempFile)).isFalse();
        }

        @Test
        @DisplayName("关闭后写入抛出IOException")
        void testWriteAfterClose() throws IOException {
            FileBackedOutputStream out = new FileBackedOutputStream(100);
            out.close();

            assertThatThrownBy(() -> out.write(1))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("多次关闭不抛异常")
        void testDoubleClose() throws IOException {
            FileBackedOutputStream out = new FileBackedOutputStream(100);
            out.close();
            out.close(); // should not throw
        }

        @Test
        @DisplayName("内存模式关闭")
        void testCloseInMemory() throws IOException {
            FileBackedOutputStream out = new FileBackedOutputStream(100);
            out.write(new byte[]{1, 2, 3});
            out.close();
            // no exception expected
        }
    }

    @Nested
    @DisplayName("isInMemory方法测试")
    class IsInMemoryTests {

        @Test
        @DisplayName("初始状态为内存模式")
        void testInitiallyInMemory() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(100)) {
                assertThat(out.isInMemory()).isTrue();
            }
        }

        @Test
        @DisplayName("溢出后切换为磁盘模式")
        void testSwitchesToDiskMode() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(10)) {
                assertThat(out.isInMemory()).isTrue();

                out.write(new byte[11]);

                assertThat(out.isInMemory()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("getFile方法测试")
    class GetFileTests {

        @Test
        @DisplayName("内存模式返回null")
        void testGetFileInMemory() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(100)) {
                out.write(new byte[10]);
                assertThat(out.getFile()).isNull();
            }
        }

        @Test
        @DisplayName("磁盘模式返回路径")
        void testGetFileOnDisk() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(10)) {
                out.write(new byte[20]);
                Path file = out.getFile();

                assertThat(file).isNotNull();
                assertThat(Files.exists(file)).isTrue();
                assertThat(file.getFileName().toString()).startsWith("opencode-fbos-");
                assertThat(file.getFileName().toString()).endsWith(".tmp");
            }
        }
    }

    @Nested
    @DisplayName("数据完整性测试")
    class DataIntegrityTests {

        @Test
        @DisplayName("内存模式数据完整性")
        void testInMemoryDataIntegrity() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(1000)) {
                byte[] data = new byte[500];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) (i % 256);
                }
                out.write(data);

                InputStream in = out.getInputStream();
                byte[] result = in.readAllBytes();
                in.close();

                assertThat(result).isEqualTo(data);
            }
        }

        @Test
        @DisplayName("磁盘模式数据完整性")
        void testOnDiskDataIntegrity() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(50)) {
                byte[] data = new byte[200];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) (i % 256);
                }
                out.write(data);
                out.flush();

                InputStream in = out.getInputStream();
                byte[] result = in.readAllBytes();
                in.close();

                assertThat(result).isEqualTo(data);
            }
        }

        @Test
        @DisplayName("增量写入后数据完整性")
        void testIncrementalWriteIntegrity() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(20)) {
                out.write(new byte[]{1, 2, 3, 4, 5});
                out.write(new byte[]{6, 7, 8, 9, 10});
                out.write(new byte[]{11, 12, 13, 14, 15});
                // This one triggers spill (total=20, adding 5 more)
                out.write(new byte[]{16, 17, 18, 19, 20});
                out.write(new byte[]{21, 22, 23, 24, 25});
                out.flush();

                InputStream in = out.getInputStream();
                byte[] result = in.readAllBytes();
                in.close();

                byte[] expected = new byte[25];
                for (int i = 0; i < 25; i++) {
                    expected[i] = (byte) (i + 1);
                }
                assertThat(result).isEqualTo(expected);
            }
        }
    }

    @Nested
    @DisplayName("自定义临时目录测试")
    class CustomTempDirTests {

        @Test
        @DisplayName("自定义临时目录中创建文件")
        void testCustomTempDir(@TempDir Path tempDir) throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(10, tempDir)) {
                out.write(new byte[20]);

                Path file = out.getFile();
                assertThat(file).isNotNull();
                assertThat(file.getParent()).isEqualTo(tempDir);
            }
        }

        @Test
        @DisplayName("null临时目录使用系统默认")
        void testNullTempDir() throws IOException {
            try (FileBackedOutputStream out = new FileBackedOutputStream(10, null)) {
                out.write(new byte[20]);

                assertThat(out.getFile()).isNotNull();
                assertThat(Files.exists(out.getFile())).isTrue();
            }
        }
    }
}
