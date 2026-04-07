/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Chunked File Processor - Memory-efficient large file processing
 * 大文件分块处理器 - 内存高效的大文件处理
 *
 * <p>Provides memory-efficient processing of large files using chunked reading,
 * memory mapping, and parallel processing with virtual threads.</p>
 * <p>提供使用分块读取、内存映射和虚拟线程并行处理的大文件内存高效处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Chunked reading with configurable buffer size - 可配置缓冲区大小的分块读取</li>
 *   <li>Memory-mapped file processing - 内存映射文件处理</li>
 *   <li>Parallel chunk processing with virtual threads - 虚拟线程并行块处理</li>
 *   <li>Stream-based chunk iteration - 基于流的块迭代</li>
 *   <li>Progress tracking and callbacks - 进度跟踪和回调</li>
 *   <li>Resume support for interrupted processing - 中断处理的恢复支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple chunked processing
 * ChunkedFileProcessor.process(largePath, 1024 * 1024, chunk -> {
 *     // Process each 1MB chunk
 *     processData(chunk.data());
 * });
 *
 * // With progress tracking
 * ChunkedFileProcessor.builder()
 *     .path(largePath)
 *     .chunkSize(8 * 1024 * 1024)  // 8MB chunks
 *     .onProgress((processed, total) -> {
 *         System.out.printf("Progress: %.1f%%\n", 100.0 * processed / total);
 *     })
 *     .process(chunk -> saveChunk(chunk));
 *
 * // Parallel processing
 * List<Result> results = ChunkedFileProcessor.builder()
 *     .path(largePath)
 *     .parallel(true)
 *     .processAndCollect(chunk -> analyzeChunk(chunk));
 *
 * // Stream-based iteration
 * try (Stream<Chunk> chunks = ChunkedFileProcessor.streamChunks(path, chunkSize)) {
 *     chunks.filter(c -> c.index() < 10)
 *           .forEach(this::process);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, parallel mode uses virtual threads safely - 线程安全: 是，并行模式安全使用虚拟线程</li>
 *   <li>Null-safe: No, path and processor must not be null - 空值安全: 否，路径和处理器不可为null</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n=file size; parallel mode divides work across virtual threads for I/O-bound speedup - 时间复杂度: O(n)，n 为文件大小；并行模式通过虚拟线程分配工作以加速 I/O 操作</li>
 *   <li>Space complexity: O(k) where k=chunk buffer size; memory-efficient as only one chunk (or p chunks in parallel) is loaded at a time - 空间复杂度: O(k)，k 为块缓冲区大小；内存高效，每次只加载一个块（并行时为 p 个块）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see FileReader
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class ChunkedFileProcessor {

    /** Default chunk size: 4MB */
    public static final int DEFAULT_CHUNK_SIZE = 4 * 1024 * 1024;

    /** Minimum chunk size: 4KB */
    public static final int MIN_CHUNK_SIZE = 4 * 1024;

    /** Maximum chunk size for memory mapping: 1GB */
    public static final long MAX_MMAP_SIZE = 1024L * 1024L * 1024L;

    private ChunkedFileProcessor() {}

    // ==================== Quick Methods | 快速方法 ====================

    /**
     * Processes a file in chunks with the default chunk size.
     * 使用默认块大小分块处理文件。
     *
     * @param path the file path - 文件路径
     * @param processor the chunk processor - 块处理器
     */
    public static void process(Path path, Consumer<Chunk> processor) {
        process(path, DEFAULT_CHUNK_SIZE, processor);
    }

    /**
     * Processes a file in chunks.
     * 分块处理文件。
     *
     * @param path the file path - 文件路径
     * @param chunkSize the chunk size in bytes - 块大小（字节）
     * @param processor the chunk processor - 块处理器
     */
    public static void process(Path path, int chunkSize, Consumer<Chunk> processor) {
        builder().path(path).chunkSize(chunkSize).process(processor);
    }

    /**
     * Creates a stream of chunks from a file.
     * 从文件创建块流。
     *
     * @param path the file path - 文件路径
     * @param chunkSize the chunk size in bytes - 块大小（字节）
     * @return a stream of chunks - 块流
     */
    public static Stream<Chunk> streamChunks(Path path, int chunkSize) {
        return builder().path(path).chunkSize(chunkSize).stream();
    }

    /**
     * Counts the number of chunks in a file.
     * 计算文件中的块数。
     *
     * @param path the file path - 文件路径
     * @param chunkSize the chunk size - 块大小
     * @return the number of chunks - 块数
     */
    public static long countChunks(Path path, int chunkSize) {
        try {
            long fileSize = Files.size(path);
            return (fileSize + chunkSize - 1) / chunkSize;
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to get file size: " + path, e);
        }
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Chunk Record | 块记录 ====================

    /**
     * Represents a chunk of file data.
     * 表示文件数据块。
     *
     * @param index the chunk index (0-based) - 块索引（从0开始）
     * @param offset the byte offset in the file - 文件中的字节偏移
     * @param data the chunk data - 块数据
     * @param size the actual data size (may be less than data.length for last chunk) - 实际数据大小
     * @param isLast whether this is the last chunk - 是否是最后一块
     */
    public record Chunk(
            long index,
            long offset,
            byte[] data,
            int size,
            boolean isLast
    ) {
        /**
         * Compact constructor - defensive copy of mutable byte array.
         * 紧凑构造器 - 可变字节数组的防御性复制。
         */
        public Chunk {
            data = data.clone();
        }

        /**
         * Gets a read-only ByteBuffer view of the data.
         * 获取数据的只读ByteBuffer视图。
         *
         * @return a read-only ByteBuffer - 只读ByteBuffer
         */
        public ByteBuffer toByteBuffer() {
            return ByteBuffer.wrap(data, 0, size).asReadOnlyBuffer();
        }

        /**
         * Gets the actual data as a trimmed byte array.
         * 获取修剪后的实际数据字节数组。
         *
         * @return the data bytes - 数据字节
         */
        public byte[] bytes() {
            return Arrays.copyOf(data, size);
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for ChunkedFileProcessor operations.
     * ChunkedFileProcessor 操作构建器。
     */
    public static final class Builder {
        private Path path;
        private int chunkSize = DEFAULT_CHUNK_SIZE;
        private boolean parallel = false;
        private int parallelism = Runtime.getRuntime().availableProcessors();
        private boolean useMemoryMapping = false;
        private long startOffset = 0;
        private long maxBytes = Long.MAX_VALUE;
        private BiConsumer<Long, Long> progressCallback;
        private long progressInterval = 1024 * 1024; // Report every 1MB by default

        private Builder() {}

        /**
         * Sets the file path.
         * 设置文件路径。
         *
         * @param path the file path - 文件路径
         * @return this builder
         */
        public Builder path(Path path) {
            this.path = Objects.requireNonNull(path, "path must not be null");
            return this;
        }

        /**
         * Sets the chunk size.
         * 设置块大小。
         *
         * @param size the chunk size in bytes - 块大小（字节）
         * @return this builder
         */
        public Builder chunkSize(int size) {
            if (size < MIN_CHUNK_SIZE) {
                throw new IllegalArgumentException("Chunk size must be at least " + MIN_CHUNK_SIZE + " bytes");
            }
            this.chunkSize = size;
            return this;
        }

        /**
         * Enables parallel processing with virtual threads.
         * 启用虚拟线程并行处理。
         *
         * @param parallel whether to enable parallel processing - 是否启用并行处理
         * @return this builder
         */
        public Builder parallel(boolean parallel) {
            this.parallel = parallel;
            return this;
        }

        /**
         * Sets the parallelism level for parallel processing.
         * 设置并行处理的并行度。
         *
         * @param level the parallelism level - 并行度
         * @return this builder
         */
        public Builder parallelism(int level) {
            if (level < 1) {
                throw new IllegalArgumentException("Parallelism must be at least 1");
            }
            this.parallelism = level;
            return this;
        }

        /**
         * Enables memory-mapped file processing.
         * 启用内存映射文件处理。
         *
         * @param use whether to use memory mapping - 是否使用内存映射
         * @return this builder
         */
        public Builder useMemoryMapping(boolean use) {
            this.useMemoryMapping = use;
            return this;
        }

        /**
         * Sets the starting offset for reading.
         * 设置读取的起始偏移量。
         *
         * @param offset the starting offset - 起始偏移量
         * @return this builder
         */
        public Builder startOffset(long offset) {
            if (offset < 0) {
                throw new IllegalArgumentException("Offset must not be negative");
            }
            this.startOffset = offset;
            return this;
        }

        /**
         * Sets the maximum bytes to read.
         * 设置最大读取字节数。
         *
         * @param maxBytes the maximum bytes - 最大字节数
         * @return this builder
         */
        public Builder maxBytes(long maxBytes) {
            if (maxBytes < 0) {
                throw new IllegalArgumentException("Max bytes must not be negative");
            }
            this.maxBytes = maxBytes;
            return this;
        }

        /**
         * Sets a progress callback.
         * 设置进度回调。
         *
         * @param callback the callback (bytesProcessed, totalBytes) - 回调（已处理字节数，总字节数）
         * @return this builder
         */
        public Builder onProgress(BiConsumer<Long, Long> callback) {
            this.progressCallback = callback;
            return this;
        }

        /**
         * Sets the progress reporting interval.
         * 设置进度报告间隔。
         *
         * @param bytes the interval in bytes - 间隔（字节）
         * @return this builder
         */
        public Builder progressInterval(long bytes) {
            this.progressInterval = bytes;
            return this;
        }

        /**
         * Processes the file with the given processor.
         * 使用给定的处理器处理文件。
         *
         * @param processor the chunk processor - 块处理器
         */
        public void process(Consumer<Chunk> processor) {
            Objects.requireNonNull(path, "path must be set");
            Objects.requireNonNull(processor, "processor must not be null");

            if (parallel) {
                processParallel(processor);
            } else if (useMemoryMapping) {
                processMemoryMapped(processor);
            } else {
                processSequential(processor);
            }
        }

        /**
         * Processes the file and collects results.
         * 处理文件并收集结果。
         *
         * @param processor the chunk processor that returns a result - 返回结果的块处理器
         * @param <T> the result type - 结果类型
         * @return list of results - 结果列表
         */
        public <T> java.util.List<T> processAndCollect(Function<Chunk, T> processor) {
            Objects.requireNonNull(path, "path must be set");
            Objects.requireNonNull(processor, "processor must not be null");

            java.util.List<T> results = new java.util.ArrayList<>();

            if (parallel) {
                // Use ConcurrentLinkedQueue for thread-safe collection
                java.util.concurrent.ConcurrentLinkedQueue<T> queue = new java.util.concurrent.ConcurrentLinkedQueue<>();
                processParallel(chunk -> queue.add(processor.apply(chunk)));
                results.addAll(queue);
            } else {
                process(chunk -> results.add(processor.apply(chunk)));
            }

            return results;
        }

        /**
         * Creates a stream of chunks.
         * 创建块流。
         *
         * @return a stream of chunks - 块流
         */
        public Stream<Chunk> stream() {
            Objects.requireNonNull(path, "path must be set");
            return StreamSupport.stream(new ChunkSpliterator(), false);
        }

        // ==================== Processing Implementations ====================

        private void processSequential(Consumer<Chunk> processor) {
            try {
                long fileSize = Files.size(path);
                long effectiveSize = Math.min(fileSize - startOffset, maxBytes);
                if (effectiveSize <= 0) return;

                long totalChunks = (effectiveSize + chunkSize - 1) / chunkSize;
                AtomicLong processedBytes = new AtomicLong(0);
                long lastReportedBytes = 0;

                try (InputStream in = Files.newInputStream(path)) {
                    // Skip to start offset
                    long toSkip = startOffset;
                    while (toSkip > 0) {
                        long skipped = in.skip(toSkip);
                        if (skipped == 0) {
                            if (in.read() == -1) break;
                            toSkip--;
                        } else {
                            toSkip -= skipped;
                        }
                    }

                    long chunkIndex = 0;
                    long currentOffset = startOffset;
                    long remaining = effectiveSize;

                    while (remaining > 0) {
                        int toRead = (int) Math.min(chunkSize, remaining);
                        byte[] buffer = new byte[toRead];
                        int bytesRead = readFully(in, buffer, toRead);

                        if (bytesRead <= 0) break;

                        boolean isLast = (chunkIndex == totalChunks - 1) || (remaining - bytesRead <= 0);
                        Chunk chunk = new Chunk(chunkIndex, currentOffset, buffer, bytesRead, isLast);
                        processor.accept(chunk);

                        currentOffset += bytesRead;
                        remaining -= bytesRead;
                        chunkIndex++;

                        // Progress reporting
                        long processed = processedBytes.addAndGet(bytesRead);
                        if (progressCallback != null && processed - lastReportedBytes >= progressInterval) {
                            progressCallback.accept(processed, effectiveSize);
                            lastReportedBytes = processed;
                        }
                    }

                    // Final progress report
                    if (progressCallback != null) {
                        progressCallback.accept(processedBytes.get(), effectiveSize);
                    }
                }
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to process file: " + path, e);
            }
        }

        private void processParallel(Consumer<Chunk> processor) {
            try {
                long fileSize = Files.size(path);
                long effectiveSize = Math.min(fileSize - startOffset, maxBytes);
                if (effectiveSize <= 0) return;

                long totalChunks = (effectiveSize + chunkSize - 1) / chunkSize;
                AtomicLong processedBytes = new AtomicLong(0);

                // Process in batches to avoid creating millions of futures upfront.
                // Each batch submits up to 'parallelism' tasks, then waits for all
                // to complete before submitting the next batch.
                int batchSize = parallelism;

                try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    long i = 0;
                    while (i < totalChunks) {
                        int currentBatchSize = (int) Math.min(batchSize, totalChunks - i);
                        CompletableFuture<?>[] batch = new CompletableFuture<?>[currentBatchSize];

                        for (int b = 0; b < currentBatchSize; b++) {
                            final long chunkIndex = i + b;
                            final long chunkOffset = startOffset + (chunkIndex * chunkSize);
                            final int chunkLength = (int) Math.min(chunkSize, effectiveSize - (chunkIndex * chunkSize));
                            final boolean isLast = (chunkIndex == totalChunks - 1);

                            batch[b] = CompletableFuture.runAsync(() -> {
                                byte[] data = readChunk(chunkOffset, chunkLength);
                                Chunk chunk = new Chunk(chunkIndex, chunkOffset, data, data.length, isLast);
                                processor.accept(chunk);

                                if (progressCallback != null) {
                                    long processed = processedBytes.addAndGet(chunkLength);
                                    progressCallback.accept(processed, effectiveSize);
                                }
                            }, executor);
                        }

                        // Wait for current batch to complete before submitting next
                        CompletableFuture.allOf(batch).join();
                        i += currentBatchSize;
                    }
                }
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to process file in parallel: " + path, e);
            }
        }

        private void processMemoryMapped(Consumer<Chunk> processor) {
            try {
                long fileSize = Files.size(path);
                long effectiveSize = Math.min(fileSize - startOffset, maxBytes);
                if (effectiveSize <= 0) return;

                try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
                    long position = startOffset;
                    long remaining = effectiveSize;
                    long chunkIndex = 0;

                    while (remaining > 0) {
                        long mapSize = Math.min(Math.min(chunkSize, remaining), MAX_MMAP_SIZE);
                        MappedByteBuffer mappedBuffer = channel.map(
                                FileChannel.MapMode.READ_ONLY, position, mapSize);

                        byte[] data = new byte[(int) mapSize];
                        mappedBuffer.get(data);

                        boolean isLast = remaining <= mapSize;
                        Chunk chunk = new Chunk(chunkIndex, position, data, data.length, isLast);
                        processor.accept(chunk);

                        position += mapSize;
                        remaining -= mapSize;
                        chunkIndex++;

                        if (progressCallback != null) {
                            progressCallback.accept(position - startOffset, effectiveSize);
                        }
                    }
                }
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to memory-map file: " + path, e);
            }
        }

        private byte[] readChunk(long offset, int length) {
            try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
                raf.seek(offset);
                byte[] data = new byte[length];
                int bytesRead = 0;
                while (bytesRead < length) {
                    int read = raf.read(data, bytesRead, length - bytesRead);
                    if (read == -1) break;
                    bytesRead += read;
                }
                if (bytesRead < length) {
                    byte[] trimmed = new byte[bytesRead];
                    System.arraycopy(data, 0, trimmed, 0, bytesRead);
                    return trimmed;
                }
                return data;
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to read chunk at offset " + offset, e);
            }
        }

        private int readFully(InputStream in, byte[] buffer, int length) throws IOException {
            int totalRead = 0;
            while (totalRead < length) {
                int read = in.read(buffer, totalRead, length - totalRead);
                if (read == -1) break;
                totalRead += read;
            }
            return totalRead;
        }

        // ==================== Spliterator ====================

        private class ChunkSpliterator extends java.util.Spliterators.AbstractSpliterator<Chunk> {
            private final long totalChunks;
            private final long fileSize;
            private final long effectiveSize;
            private long currentChunk = 0;
            private InputStream inputStream;

            ChunkSpliterator() {
                super(Long.MAX_VALUE, java.util.Spliterator.ORDERED | java.util.Spliterator.NONNULL);
                try {
                    this.fileSize = Files.size(path);
                    this.effectiveSize = Math.min(fileSize - startOffset, maxBytes);
                    this.totalChunks = effectiveSize > 0 ? (effectiveSize + chunkSize - 1) / chunkSize : 0;
                } catch (IOException e) {
                    throw new OpenIOOperationException("Failed to get file size: " + path, e);
                }
            }

            @Override
            public boolean tryAdvance(Consumer<? super Chunk> action) {
                if (currentChunk >= totalChunks) {
                    closeStream();
                    return false;
                }

                try {
                    if (inputStream == null) {
                        inputStream = Files.newInputStream(path);
                        long toSkip = startOffset;
                        while (toSkip > 0) {
                            long skipped = inputStream.skip(toSkip);
                            if (skipped == 0) {
                                if (inputStream.read() == -1) break;
                                toSkip--;
                            } else {
                                toSkip -= skipped;
                            }
                        }
                    }

                    long offset = startOffset + (currentChunk * chunkSize);
                    int toRead = (int) Math.min(chunkSize, effectiveSize - (currentChunk * chunkSize));
                    byte[] buffer = new byte[toRead];
                    int bytesRead = readFully(inputStream, buffer, toRead);

                    if (bytesRead <= 0) {
                        closeStream();
                        return false;
                    }

                    boolean isLast = currentChunk == totalChunks - 1;
                    Chunk chunk = new Chunk(currentChunk, offset, buffer, bytesRead, isLast);
                    action.accept(chunk);

                    currentChunk++;
                    return true;
                } catch (IOException e) {
                    closeStream();
                    throw new OpenIOOperationException("Failed to read chunk", e);
                }
            }

            private void closeStream() {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ignored) {
                    }
                    inputStream = null;
                }
            }
        }
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Writes data to a file in chunks.
     * 分块写入数据到文件。
     *
     * @param path the file path - 文件路径
     * @param data the data to write - 要写入的数据
     * @param chunkSize the chunk size - 块大小
     * @param progressCallback optional progress callback - 可选的进度回调
     */
    public static void writeInChunks(Path path, byte[] data, int chunkSize,
                                     BiConsumer<Long, Long> progressCallback) {
        try (OutputStream out = Files.newOutputStream(path,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

            long total = data.length;
            long written = 0;

            while (written < total) {
                int toWrite = (int) Math.min(chunkSize, total - written);
                out.write(data, (int) written, toWrite);
                written += toWrite;

                if (progressCallback != null) {
                    progressCallback.accept(written, total);
                }
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to write file: " + path, e);
        }
    }

    /**
     * Copies a file in chunks.
     * 分块复制文件。
     *
     * @param source the source path - 源路径
     * @param target the target path - 目标路径
     * @param chunkSize the chunk size - 块大小
     * @param progressCallback optional progress callback - 可选的进度回调
     */
    public static void copyInChunks(Path source, Path target, int chunkSize,
                                    BiConsumer<Long, Long> progressCallback) {
        try {
            long fileSize = Files.size(source);

            try (InputStream in = Files.newInputStream(source);
                 OutputStream out = Files.newOutputStream(target,
                         StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

                byte[] buffer = new byte[chunkSize];
                long copied = 0;

                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    copied += read;

                    if (progressCallback != null) {
                        progressCallback.accept(copied, fileSize);
                    }
                }
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to copy file: " + source + " to " + target, e);
        }
    }
}
