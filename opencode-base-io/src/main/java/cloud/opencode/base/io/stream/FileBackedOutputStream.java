package cloud.opencode.base.io.stream;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * File-Backed Output Stream
 * 文件后备输出流
 *
 * <p>An OutputStream that starts writing to an in-memory buffer and automatically
 * spills to a temporary file when a configurable byte threshold is exceeded.
 * This is useful for processing data of unknown size without risking
 * OutOfMemoryError for large inputs.</p>
 * <p>一个先向内存缓冲区写入数据的输出流，当超过可配置的字节阈值时自动溢出到临时文件。
 * 这对于处理大小未知的数据非常有用，可以避免大输入导致的OutOfMemoryError。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic memory-to-disk spillover - 自动内存到磁盘溢出</li>
 *   <li>Configurable byte threshold - 可配置的字节阈值</li>
 *   <li>Custom temporary directory support - 支持自定义临时目录</li>
 *   <li>Read-back via getInputStream() - 通过getInputStream()回读数据</li>
 *   <li>Automatic temp file cleanup on close/reset - 关闭/重置时自动清理临时文件</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Write data that may exceed memory threshold
 * try (FileBackedOutputStream out = new FileBackedOutputStream(1024 * 1024)) {
 *     out.write(largeData);
 *     InputStream in = out.getInputStream();
 *     // process the data from input stream
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (single-writer) - 线程安全: 否（单写入者）</li>
 *   <li>Temp files are deleted on close/reset - 关闭/重置时删除临时文件</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
public class FileBackedOutputStream extends OutputStream {

    private static final int DEFAULT_INITIAL_CAPACITY = 256;
    private static final int MAX_MEMORY_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private final int threshold;
    private final Path tempDir;

    /** In-memory buffer, null after switchover to file. */
    private byte[] memoryBuffer;
    /** Number of valid bytes in memoryBuffer. */
    private int memoryCount;

    /** Temp file path, null when in memory mode. */
    private Path tempFile;
    /** File output stream, null when in memory mode. */
    private OutputStream fileOutputStream;

    /** Total bytes written across memory and file. */
    private long totalCount;
    /** Whether the stream has been closed. */
    private boolean closed;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Creates a file-backed output stream with default temp directory
     * 创建使用默认临时目录的文件后备输出流
     *
     * @param threshold the byte threshold before spilling to disk | 溢出到磁盘前的字节阈值
     * @throws IllegalArgumentException if threshold is not positive | 当阈值非正数时抛出
     */
    public FileBackedOutputStream(int threshold) {
        this(threshold, null);
    }

    /**
     * Creates a file-backed output stream with custom temp directory
     * 创建使用自定义临时目录的文件后备输出流
     *
     * @param threshold the byte threshold before spilling to disk | 溢出到磁盘前的字节阈值
     * @param tempDir   the directory for temp files, or null for system default | 临时文件目录，null使用系统默认
     * @throws IllegalArgumentException if threshold is not positive | 当阈值非正数时抛出
     */
    public FileBackedOutputStream(int threshold, Path tempDir) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("Threshold must be positive: " + threshold);
        }
        this.threshold = threshold;
        this.tempDir = tempDir;
        this.memoryBuffer = new byte[Math.min(threshold, DEFAULT_INITIAL_CAPACITY)];
        this.memoryCount = 0;
        this.totalCount = 0;
        this.closed = false;
    }

    // ==================== OutputStream Methods | 输出流方法 ====================

    @Override
    public void write(int b) throws IOException {
        ensureOpen();
        if (fileOutputStream != null) {
            fileOutputStream.write(b);
            totalCount++;
            return;
        }
        if (memoryCount + 1 > threshold) {
            switchToFile();
            fileOutputStream.write(b);
            totalCount++;
        } else {
            ensureMemoryCapacity(memoryCount + 1);
            memoryBuffer[memoryCount++] = (byte) b;
            totalCount++;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException("byte array is null");
        }
        if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        ensureOpen();
        if (fileOutputStream != null) {
            fileOutputStream.write(b, off, len);
            totalCount += len;
            return;
        }
        if (memoryCount + len > threshold) {
            switchToFile();
            fileOutputStream.write(b, off, len);
            totalCount += len;
        } else {
            ensureMemoryCapacity(memoryCount + len);
            System.arraycopy(b, off, memoryBuffer, memoryCount, len);
            memoryCount += len;
            totalCount += len;
        }
    }

    @Override
    public void flush() throws IOException {
        ensureOpen();
        if (fileOutputStream != null) {
            fileOutputStream.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        try {
            if (fileOutputStream != null) {
                fileOutputStream.close();
                fileOutputStream = null;
            }
        } finally {
            deleteTempFile();
            memoryBuffer = null;
        }
    }

    // ==================== Public API | 公共接口 ====================

    /**
     * Returns an InputStream to read all written data
     * 返回用于读取所有已写入数据的InputStream
     *
     * <p>If data is in memory, returns a ByteArrayInputStream.
     * If data has spilled to disk, flushes the file output stream
     * and returns a FileInputStream.</p>
     * <p>如果数据在内存中，返回ByteArrayInputStream。
     * 如果数据已溢出到磁盘，刷新文件输出流并返回FileInputStream。</p>
     *
     * <p>After calling this method, no further writes should be performed.
     * The returned InputStream must be closed by the caller. When data has
     * spilled to disk, the temp file is deleted when this stream is closed
     * (not when getInputStream()'s result is closed).</p>
     * <p>调用此方法后，不应再进行写入操作。返回的InputStream必须由调用方关闭。
     * 当数据已溢出到磁盘时，临时文件在此流关闭时删除。</p>
     *
     * @return an InputStream over all written data | 包含所有已写入数据的InputStream
     * @throws OpenIOOperationException if an IO error occurs | 当IO错误发生时抛出
     */
    public InputStream getInputStream() {
        if (tempFile != null) {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    fileOutputStream = null;
                }
                return new FileInputStream(tempFile.toFile());
            } catch (IOException e) {
                deleteTempFile();
                throw new OpenIOOperationException("getInputStream", null,
                        "Failed to open input stream from temp file", e);
            }
        }
        byte[] data = (memoryBuffer != null)
                ? Arrays.copyOf(memoryBuffer, memoryCount)
                : new byte[0];
        return new ByteArrayInputStream(data);
    }

    /**
     * Checks whether data is still in memory
     * 检查数据是否仍在内存中
     *
     * @return true if data is in memory, false if spilled to disk | 如果数据在内存中返回true，溢出到磁盘返回false
     */
    public boolean isInMemory() {
        return tempFile == null;
    }

    /**
     * Returns the total number of bytes written
     * 返回已写入的总字节数
     *
     * @return total bytes written | 已写入的总字节数
     */
    public long size() {
        return totalCount;
    }

    /**
     * Returns the temp file path, or null if data is in memory
     * 返回临时文件路径，如果数据在内存中则返回null
     *
     * @return the temp file path, or null | 临时文件路径，或null
     */
    public Path getFile() {
        return tempFile;
    }

    /**
     * Resets the stream, clearing all data and deleting temp file if exists
     * 重置流，清除所有数据并删除临时文件（如果存在）
     *
     * <p>After reset, the stream returns to memory mode and can accept new writes.</p>
     * <p>重置后，流恢复为内存模式，可以接受新的写入。</p>
     *
     * @throws OpenIOOperationException if temp file deletion fails | 当临时文件删除失败时抛出
     */
    public void reset() {
        try {
            if (fileOutputStream != null) {
                fileOutputStream.close();
                fileOutputStream = null;
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("reset", null,
                    "Failed to close file output stream during reset", e);
        }
        deleteTempFile();
        this.memoryBuffer = new byte[Math.min(threshold, DEFAULT_INITIAL_CAPACITY)];
        this.memoryCount = 0;
        this.totalCount = 0;
        this.closed = false;
    }

    // ==================== Internal Methods | 内部方法 ====================

    /**
     * Ensures the stream is open.
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
    }

    /**
     * Ensures the in-memory buffer has at least the given capacity.
     */
    private void ensureMemoryCapacity(int minCapacity) {
        if (minCapacity > memoryBuffer.length) {
            int oldCapacity = memoryBuffer.length;
            // Guard against left-shift overflow: cap doubled value
            int doubled = (oldCapacity <= (Integer.MAX_VALUE >> 1)) ? oldCapacity << 1 : Integer.MAX_VALUE;
            int newCapacity = Math.max(doubled, minCapacity);
            if (newCapacity > MAX_MEMORY_ARRAY_SIZE) {
                if (minCapacity > MAX_MEMORY_ARRAY_SIZE) {
                    newCapacity = Integer.MAX_VALUE;
                } else {
                    newCapacity = MAX_MEMORY_ARRAY_SIZE;
                }
            }
            memoryBuffer = Arrays.copyOf(memoryBuffer, newCapacity);
        }
    }

    /**
     * Switches from memory mode to file mode, writing current buffer to temp file.
     */
    private void switchToFile() throws IOException {
        try {
            tempFile = (tempDir != null)
                    ? Files.createTempFile(tempDir, "opencode-fbos-", ".tmp")
                    : Files.createTempFile("opencode-fbos-", ".tmp");

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile.toFile()));
            try {
                if (memoryCount > 0) {
                    bos.write(memoryBuffer, 0, memoryCount);
                }
            } catch (IOException e) {
                bos.close();
                deleteTempFile();
                throw e;
            }
            this.fileOutputStream = bos;
            this.memoryBuffer = null;
            this.memoryCount = 0;
        } catch (IOException e) {
            throw new IOException("Failed to create temp file for spillover", e);
        }
    }

    /**
     * Deletes the temp file if it exists.
     */
    private void deleteTempFile() {
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // best effort cleanup
            }
            tempFile = null;
        }
    }
}
