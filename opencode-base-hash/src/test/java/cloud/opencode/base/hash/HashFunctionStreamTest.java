package cloud.opencode.base.hash;

import cloud.opencode.base.hash.exception.OpenHashException;
import cloud.opencode.base.hash.function.MessageDigestHashFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * HashFunction 流式哈希测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("HashFunction 流式哈希测试")
class HashFunctionStreamTest {

    private final HashFunction sha256 = MessageDigestHashFunction.sha256();

    @Nested
    @DisplayName("hashInputStream 测试")
    class HashInputStreamTest {

        @Test
        @DisplayName("SHA-256流式哈希与直接hashBytes一致")
        void testStreamMatchesDirectHashBytes() {
            byte[] data = "Hello, World!".getBytes();
            HashCode expected = sha256.hashBytes(data);

            InputStream is = new ByteArrayInputStream(data);
            HashCode actual = sha256.hashInputStream(is);

            assertThat(actual.toHex()).isEqualTo(expected.toHex());
        }

        @Test
        @DisplayName("空流的哈希")
        void testEmptyStream() {
            byte[] empty = new byte[0];
            HashCode expected = sha256.hashBytes(empty);

            InputStream is = new ByteArrayInputStream(empty);
            HashCode actual = sha256.hashInputStream(is);

            assertThat(actual.toHex()).isEqualTo(expected.toHex());
        }

        @Test
        @DisplayName("大数据（超过8KB）多块读取")
        void testLargeInputMultiChunk() {
            byte[] data = new byte[32768]; // 32KB, well over the 8KB buffer
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 251);
            }
            HashCode expected = sha256.hashBytes(data);

            InputStream is = new ByteArrayInputStream(data);
            HashCode actual = sha256.hashInputStream(is);

            assertThat(actual.toHex()).isEqualTo(expected.toHex());
        }

        @Test
        @DisplayName("null输入流抛出NullPointerException")
        void testNullStreamThrows() {
            assertThatThrownBy(() -> sha256.hashInputStream(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("hashFile 测试")
    class HashFileTest {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("SHA-256文件哈希与直接hashBytes一致")
        void testFileMatchesDirectHashBytes() throws IOException {
            byte[] content = "The quick brown fox jumps over the lazy dog".getBytes();
            Path file = tempDir.resolve("test.txt");
            Files.write(file, content);

            HashCode expected = sha256.hashBytes(content);
            HashCode actual = sha256.hashFile(file);

            assertThat(actual.toHex()).isEqualTo(expected.toHex());
        }

        @Test
        @DisplayName("不存在的文件抛出OpenHashException")
        void testNonexistentFileThrows() {
            Path nonexistent = tempDir.resolve("does-not-exist.bin");

            assertThatThrownBy(() -> sha256.hashFile(nonexistent))
                    .isInstanceOf(OpenHashException.class)
                    .hasMessageContaining("Failed to hash file");
        }

        @Test
        @DisplayName("null路径抛出NullPointerException")
        void testNullPathThrows() {
            assertThatThrownBy(() -> sha256.hashFile(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
