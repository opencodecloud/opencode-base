package cloud.opencode.base.io.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * ChunkedFileProcessor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("ChunkedFileProcessor 测试")
class ChunkedFileProcessorTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("DEFAULT_CHUNK_SIZE常量")
        void testDefaultChunkSize() {
            assertThat(ChunkedFileProcessor.DEFAULT_CHUNK_SIZE).isEqualTo(4 * 1024 * 1024);
        }

        @Test
        @DisplayName("MIN_CHUNK_SIZE常量")
        void testMinChunkSize() {
            assertThat(ChunkedFileProcessor.MIN_CHUNK_SIZE).isEqualTo(4 * 1024);
        }

        @Test
        @DisplayName("MAX_MMAP_SIZE常量")
        void testMaxMmapSize() {
            assertThat(ChunkedFileProcessor.MAX_MMAP_SIZE).isEqualTo(1024L * 1024L * 1024L);
        }
    }

    @Nested
    @DisplayName("process方法测试")
    class ProcessTests {

        @Test
        @DisplayName("使用默认块大小处理")
        void testProcessDefaultChunkSize() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[100];
            Files.write(file, data);
            AtomicInteger count = new AtomicInteger();

            ChunkedFileProcessor.process(file, chunk -> count.incrementAndGet());

            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("使用指定块大小处理")
        void testProcessCustomChunkSize() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[10000];
            Files.write(file, data);
            AtomicInteger count = new AtomicInteger();

            ChunkedFileProcessor.process(file, 4096, chunk -> count.incrementAndGet());

            assertThat(count.get()).isEqualTo(3); // 10000 / 4096 = 3 chunks
        }
    }

    @Nested
    @DisplayName("streamChunks方法测试")
    class StreamChunksTests {

        @Test
        @DisplayName("创建块流")
        void testStreamChunks() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[8192];
            Files.write(file, data);

            try (Stream<ChunkedFileProcessor.Chunk> chunks =
                    ChunkedFileProcessor.streamChunks(file, 4096)) {
                assertThat(chunks.count()).isEqualTo(2);
            }
        }
    }

    @Nested
    @DisplayName("countChunks方法测试")
    class CountChunksTests {

        @Test
        @DisplayName("计算块数")
        void testCountChunks() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[10000];
            Files.write(file, data);

            long count = ChunkedFileProcessor.countChunks(file, 4096);

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("空文件")
        void testCountChunksEmpty() throws Exception {
            Path file = tempDir.resolve("empty.bin");
            Files.createFile(file);

            long count = ChunkedFileProcessor.countChunks(file, 4096);

            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Chunk记录测试")
    class ChunkTests {

        @Test
        @DisplayName("toByteBuffer方法")
        void testChunkToByteBuffer() {
            byte[] data = {1, 2, 3, 4, 5};
            ChunkedFileProcessor.Chunk chunk = new ChunkedFileProcessor.Chunk(0, 0, data, 5, true);

            ByteBuffer buffer = chunk.toByteBuffer();

            assertThat(buffer.remaining()).isEqualTo(5);
        }

        @Test
        @DisplayName("bytes方法返回完整数据")
        void testChunkBytesFullSize() {
            byte[] data = {1, 2, 3, 4, 5};
            ChunkedFileProcessor.Chunk chunk = new ChunkedFileProcessor.Chunk(0, 0, data, 5, true);

            byte[] bytes = chunk.bytes();

            assertThat(bytes).isEqualTo(data);
        }

        @Test
        @DisplayName("bytes方法返回截断数据")
        void testChunkBytesTruncated() {
            byte[] data = {1, 2, 3, 4, 5, 0, 0, 0};
            ChunkedFileProcessor.Chunk chunk = new ChunkedFileProcessor.Chunk(0, 0, data, 5, true);

            byte[] bytes = chunk.bytes();

            assertThat(bytes).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("记录字段访问")
        void testChunkFields() {
            byte[] data = {1, 2, 3};
            ChunkedFileProcessor.Chunk chunk = new ChunkedFileProcessor.Chunk(2, 100, data, 3, true);

            assertThat(chunk.index()).isEqualTo(2);
            assertThat(chunk.offset()).isEqualTo(100);
            assertThat(chunk.data()).isEqualTo(data);
            assertThat(chunk.size()).isEqualTo(3);
            assertThat(chunk.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("基本构建器使用")
        void testBasicBuilder() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[1000];
            Files.write(file, data);
            AtomicInteger count = new AtomicInteger();

            ChunkedFileProcessor.builder()
                .path(file)
                .chunkSize(4096)
                .process(chunk -> count.incrementAndGet());

            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("进度回调")
        void testProgressCallback() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[10000];
            Files.write(file, data);
            AtomicLong lastProcessed = new AtomicLong();

            ChunkedFileProcessor.builder()
                .path(file)
                .chunkSize(4096)
                .onProgress((processed, total) -> lastProcessed.set(processed))
                .process(chunk -> {});

            assertThat(lastProcessed.get()).isEqualTo(10000);
        }

        @Test
        @DisplayName("startOffset参数")
        void testStartOffset() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[10000];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }
            Files.write(file, data);
            List<ChunkedFileProcessor.Chunk> chunks = new ArrayList<>();

            ChunkedFileProcessor.builder()
                .path(file)
                .chunkSize(4096)
                .startOffset(1000)
                .process(chunks::add);

            assertThat(chunks.get(0).offset()).isEqualTo(1000);
        }

        @Test
        @DisplayName("maxBytes参数")
        void testMaxBytes() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[10000];
            Files.write(file, data);
            AtomicLong totalBytes = new AtomicLong();

            ChunkedFileProcessor.builder()
                .path(file)
                .chunkSize(4096)
                .maxBytes(5000)
                .process(chunk -> totalBytes.addAndGet(chunk.size()));

            assertThat(totalBytes.get()).isEqualTo(5000);
        }

        @Test
        @DisplayName("块大小太小抛出异常")
        void testChunkSizeTooSmall() {
            assertThatThrownBy(() -> ChunkedFileProcessor.builder().chunkSize(100))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("parallelism参数")
        void testParallelism() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[100000];
            Files.write(file, data);
            AtomicInteger count = new AtomicInteger();

            ChunkedFileProcessor.builder()
                .path(file)
                .chunkSize(10000)
                .parallel(true)
                .parallelism(4)
                .process(chunk -> count.incrementAndGet());

            assertThat(count.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("processAndCollect方法")
        void testProcessAndCollect() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[10000];
            Files.write(file, data);

            List<Long> offsets = ChunkedFileProcessor.builder()
                .path(file)
                .chunkSize(4096)
                .processAndCollect(chunk -> chunk.offset());

            assertThat(offsets).hasSize(3);
        }

        @Test
        @DisplayName("stream方法")
        void testStream() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[10000];
            Files.write(file, data);

            try (Stream<ChunkedFileProcessor.Chunk> stream =
                    ChunkedFileProcessor.builder()
                        .path(file)
                        .chunkSize(4096)
                        .stream()) {
                assertThat(stream.count()).isEqualTo(3);
            }
        }

        @Test
        @DisplayName("负偏移量抛出异常")
        void testNegativeOffset() {
            assertThatThrownBy(() -> ChunkedFileProcessor.builder().startOffset(-1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负maxBytes抛出异常")
        void testNegativeMaxBytes() {
            assertThatThrownBy(() -> ChunkedFileProcessor.builder().maxBytes(-1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负parallelism抛出异常")
        void testNegativeParallelism() {
            assertThatThrownBy(() -> ChunkedFileProcessor.builder().parallelism(0))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("内存映射处理测试")
    class MemoryMappedTests {

        @Test
        @DisplayName("使用内存映射处理")
        void testMemoryMapped() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = new byte[10000];
            Files.write(file, data);
            AtomicInteger count = new AtomicInteger();

            ChunkedFileProcessor.builder()
                .path(file)
                .chunkSize(4096)
                .useMemoryMapping(true)
                .process(chunk -> count.incrementAndGet());

            assertThat(count.get()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("writeInChunks方法测试")
    class WriteInChunksTests {

        @Test
        @DisplayName("分块写入")
        void testWriteInChunks() throws Exception {
            Path file = tempDir.resolve("output.bin");
            byte[] data = new byte[10000];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            ChunkedFileProcessor.writeInChunks(file, data, 4096, null);

            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }

        @Test
        @DisplayName("分块写入带进度回调")
        void testWriteInChunksWithProgress() throws Exception {
            Path file = tempDir.resolve("output.bin");
            byte[] data = new byte[10000];
            AtomicLong lastWritten = new AtomicLong();

            ChunkedFileProcessor.writeInChunks(file, data, 4096,
                (written, total) -> lastWritten.set(written));

            assertThat(lastWritten.get()).isEqualTo(10000);
        }
    }

    @Nested
    @DisplayName("copyInChunks方法测试")
    class CopyInChunksTests {

        @Test
        @DisplayName("分块复制")
        void testCopyInChunks() throws Exception {
            Path source = tempDir.resolve("source.bin");
            Path target = tempDir.resolve("target.bin");
            byte[] data = new byte[10000];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }
            Files.write(source, data);

            ChunkedFileProcessor.copyInChunks(source, target, 4096, null);

            assertThat(Files.readAllBytes(target)).isEqualTo(data);
        }

        @Test
        @DisplayName("分块复制带进度回调")
        void testCopyInChunksWithProgress() throws Exception {
            Path source = tempDir.resolve("source.bin");
            Path target = tempDir.resolve("target.bin");
            byte[] data = new byte[10000];
            Files.write(source, data);
            AtomicLong lastCopied = new AtomicLong();

            ChunkedFileProcessor.copyInChunks(source, target, 4096,
                (copied, total) -> lastCopied.set(copied));

            assertThat(lastCopied.get()).isEqualTo(10000);
        }
    }
}
