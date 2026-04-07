package cloud.opencode.base.io.compress;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * GzipUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
@DisplayName("GzipUtil 测试")
class GzipUtilTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("compress/decompress byte[] 测试")
    class ByteArrayTests {

        @Test
        @DisplayName("压缩和解压缩字节数组 - 往返测试")
        void testRoundTrip() {
            byte[] original = "Hello, Gzip World!".getBytes(StandardCharsets.UTF_8);

            byte[] compressed = GzipUtil.compress(original);
            byte[] decompressed = GzipUtil.decompress(compressed);

            assertThat(decompressed).isEqualTo(original);
        }

        @Test
        @DisplayName("压缩后数据应以Gzip魔数开头")
        void testCompressedHasMagicBytes() {
            byte[] original = "test data".getBytes(StandardCharsets.UTF_8);

            byte[] compressed = GzipUtil.compress(original);

            assertThat(compressed.length).isGreaterThanOrEqualTo(2);
            assertThat(compressed[0] & 0xFF).isEqualTo(0x1f);
            assertThat(compressed[1] & 0xFF).isEqualTo(0x8b);
        }

        @Test
        @DisplayName("空数据压缩和解压缩")
        void testEmptyData() {
            byte[] original = new byte[0];

            byte[] compressed = GzipUtil.compress(original);
            byte[] decompressed = GzipUtil.decompress(compressed);

            assertThat(decompressed).isEmpty();
        }

        @Test
        @DisplayName("大数据压缩和解压缩 (1MB)")
        void testLargeData() {
            byte[] original = new byte[1024 * 1024];
            Arrays.fill(original, (byte) 'A');

            byte[] compressed = GzipUtil.compress(original);
            byte[] decompressed = GzipUtil.decompress(compressed);

            assertThat(decompressed).isEqualTo(original);
            // Repetitive data should compress well
            assertThat(compressed.length).isLessThan(original.length);
        }

        @Test
        @DisplayName("null输入抛出NullPointerException")
        void testNullCompress() {
            assertThatThrownBy(() -> GzipUtil.compress((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null输入解压缩抛出NullPointerException")
        void testNullDecompress() {
            assertThatThrownBy(() -> GzipUtil.decompress((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("无效数据解压缩抛出OpenIOOperationException")
        void testInvalidDecompress() {
            byte[] invalid = {0x00, 0x01, 0x02, 0x03};

            assertThatThrownBy(() -> GzipUtil.decompress(invalid))
                    .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("compress/decompress Path 测试")
    class FileTests {

        @Test
        @DisplayName("文件压缩和解压缩 - 往返测试")
        void testFileRoundTrip() throws Exception {
            Path source = tempDir.resolve("source.txt");
            Path compressed = tempDir.resolve("source.txt.gz");
            Path decompressed = tempDir.resolve("output.txt");

            String content = "File compression test content!";
            Files.writeString(source, content);

            GzipUtil.compress(source, compressed);
            assertThat(compressed).exists();

            GzipUtil.decompress(compressed, decompressed);
            assertThat(decompressed).exists();

            String result = Files.readString(decompressed);
            assertThat(result).isEqualTo(content);
        }

        @Test
        @DisplayName("null源路径抛出NullPointerException")
        void testNullSource() {
            assertThatThrownBy(() -> GzipUtil.compress((Path) null, tempDir.resolve("out.gz")))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null目标路径抛出NullPointerException")
        void testNullTarget() {
            assertThatThrownBy(() -> GzipUtil.compress(tempDir.resolve("in.txt"), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("不存在的源文件抛出OpenIOOperationException")
        void testNonExistentSource() {
            Path nonExistent = tempDir.resolve("nonexistent.txt");
            Path target = tempDir.resolve("out.gz");

            assertThatThrownBy(() -> GzipUtil.compress(nonExistent, target))
                    .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("compress/decompress InputStream 测试")
    class InputStreamTests {

        @Test
        @DisplayName("输入流压缩和解压缩 - 往返测试")
        void testStreamRoundTrip() {
            byte[] original = "Stream compression test!".getBytes(StandardCharsets.UTF_8);

            byte[] compressed = GzipUtil.compress(new ByteArrayInputStream(original));
            byte[] decompressed = GzipUtil.decompress(new ByteArrayInputStream(compressed));

            assertThat(decompressed).isEqualTo(original);
        }

        @Test
        @DisplayName("null输入流压缩抛出NullPointerException")
        void testNullStreamCompress() {
            assertThatThrownBy(() -> GzipUtil.compress((InputStream) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null输入流解压缩抛出NullPointerException")
        void testNullStreamDecompress() {
            assertThatThrownBy(() -> GzipUtil.decompress((InputStream) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("compressStream/decompressStream 测试")
    class StreamWrapperTests {

        @Test
        @DisplayName("compressStream返回压缩流")
        void testCompressStream() throws Exception {
            byte[] original = "Compress stream test".getBytes(StandardCharsets.UTF_8);

            try (InputStream compressed = GzipUtil.compressStream(new ByteArrayInputStream(original))) {
                byte[] compressedBytes = compressed.readAllBytes();
                assertThat(GzipUtil.isGzipped(compressedBytes)).isTrue();

                byte[] decompressed = GzipUtil.decompress(compressedBytes);
                assertThat(decompressed).isEqualTo(original);
            }
        }

        @Test
        @DisplayName("decompressStream返回解压缩流")
        void testDecompressStream() throws Exception {
            byte[] original = "Decompress stream test".getBytes(StandardCharsets.UTF_8);
            byte[] compressed = GzipUtil.compress(original);

            try (InputStream decompressed = GzipUtil.decompressStream(new ByteArrayInputStream(compressed))) {
                byte[] result = decompressed.readAllBytes();
                assertThat(result).isEqualTo(original);
            }
        }

        @Test
        @DisplayName("null输入compressStream抛出NullPointerException")
        void testNullCompressStream() {
            assertThatThrownBy(() -> GzipUtil.compressStream(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null输入decompressStream抛出NullPointerException")
        void testNullDecompressStream() {
            assertThatThrownBy(() -> GzipUtil.decompressStream(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("isGzipped 测试")
    class IsGzippedTests {

        @Test
        @DisplayName("Gzip压缩数据返回true")
        void testGzippedData() {
            byte[] compressed = GzipUtil.compress("test".getBytes(StandardCharsets.UTF_8));

            assertThat(GzipUtil.isGzipped(compressed)).isTrue();
        }

        @Test
        @DisplayName("非Gzip数据返回false")
        void testNonGzippedData() {
            byte[] plain = "not gzipped".getBytes(StandardCharsets.UTF_8);

            assertThat(GzipUtil.isGzipped(plain)).isFalse();
        }

        @Test
        @DisplayName("空数据返回false")
        void testEmptyData() {
            assertThat(GzipUtil.isGzipped(new byte[0])).isFalse();
        }

        @Test
        @DisplayName("单字节数据返回false")
        void testSingleByte() {
            assertThat(GzipUtil.isGzipped(new byte[]{0x1f})).isFalse();
        }

        @Test
        @DisplayName("null数据抛出NullPointerException")
        void testNullData() {
            assertThatThrownBy(() -> GzipUtil.isGzipped((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("文件Gzip检测")
        void testGzippedFile() throws Exception {
            Path source = tempDir.resolve("test.txt");
            Path gzFile = tempDir.resolve("test.txt.gz");
            Files.writeString(source, "test content");

            GzipUtil.compress(source, gzFile);

            assertThat(GzipUtil.isGzipped(gzFile)).isTrue();
            assertThat(GzipUtil.isGzipped(source)).isFalse();
        }

        @Test
        @DisplayName("null路径抛出NullPointerException")
        void testNullPath() {
            assertThatThrownBy(() -> GzipUtil.isGzipped((Path) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("不存在的文件抛出OpenIOOperationException")
        void testNonExistentFile() {
            Path nonExistent = tempDir.resolve("nonexistent.gz");

            assertThatThrownBy(() -> GzipUtil.isGzipped(nonExistent))
                    .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("Gzip炸弹防护测试")
    class GzipBombProtectionTests {

        @Test
        @DisplayName("解压缩带maxSize参数正常工作")
        void testDecompressWithMaxSize() {
            byte[] original = "Hello World".getBytes(StandardCharsets.UTF_8);
            byte[] compressed = GzipUtil.compress(original);

            byte[] result = GzipUtil.decompress(compressed, 1024);
            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("解压缩超出maxSize抛出异常")
        void testDecompressExceedsMaxSize() {
            byte[] original = new byte[1024];
            Arrays.fill(original, (byte) 'A');
            byte[] compressed = GzipUtil.compress(original);

            assertThatThrownBy(() -> GzipUtil.decompress(compressed, 100))
                    .isInstanceOf(OpenIOOperationException.class)
                    .hasMessageContaining("exceeds maximum size limit");
        }

        @Test
        @DisplayName("maxSize非正数抛出IllegalArgumentException")
        void testDecompressZeroMaxSize() {
            byte[] compressed = GzipUtil.compress(new byte[]{1});

            assertThatThrownBy(() -> GzipUtil.decompress(compressed, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> GzipUtil.decompress(compressed, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("InputStream解压缩带maxSize正常工作")
        void testDecompressStreamWithMaxSize() {
            byte[] original = "Test data".getBytes(StandardCharsets.UTF_8);
            byte[] compressed = GzipUtil.compress(original);

            byte[] result = GzipUtil.decompress(new ByteArrayInputStream(compressed), 1024);
            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("InputStream解压缩超出maxSize抛出异常")
        void testDecompressStreamExceedsMaxSize() {
            byte[] original = new byte[1024];
            Arrays.fill(original, (byte) 'B');
            byte[] compressed = GzipUtil.compress(original);

            assertThatThrownBy(() -> GzipUtil.decompress(new ByteArrayInputStream(compressed), 100))
                    .isInstanceOf(OpenIOOperationException.class)
                    .hasMessageContaining("exceeds maximum size limit");
        }

        @Test
        @DisplayName("文件解压缩带maxSize正常工作")
        void testDecompressFileWithMaxSize() throws Exception {
            byte[] original = "File content".getBytes(StandardCharsets.UTF_8);
            Path source = tempDir.resolve("input.txt");
            Path compressed = tempDir.resolve("input.txt.gz");
            Path output = tempDir.resolve("output.txt");

            Files.write(source, original);
            GzipUtil.compress(source, compressed);

            GzipUtil.decompress(compressed, output, 1024);
            assertThat(output).exists();
            assertThat(Files.readAllBytes(output)).isEqualTo(original);
        }

        @Test
        @DisplayName("文件解压缩超出maxSize抛出异常")
        void testDecompressFileExceedsMaxSize() throws Exception {
            byte[] original = new byte[1024];
            Arrays.fill(original, (byte) 'C');
            Path source = tempDir.resolve("big.txt");
            Path compressed = tempDir.resolve("big.txt.gz");
            Path output = tempDir.resolve("big-output.txt");

            Files.write(source, original);
            GzipUtil.compress(source, compressed);

            assertThatThrownBy(() -> GzipUtil.decompress(compressed, output, 100))
                    .isInstanceOf(OpenIOOperationException.class)
                    .hasMessageContaining("exceeds maximum size limit");
        }

        @Test
        @DisplayName("默认解压缩使用DEFAULT_MAX_DECOMPRESSED_SIZE")
        void testDefaultMaxDecompressedSizeConstant() {
            assertThat(GzipUtil.DEFAULT_MAX_DECOMPRESSED_SIZE).isEqualTo(256L * 1024 * 1024);
        }
    }
}
