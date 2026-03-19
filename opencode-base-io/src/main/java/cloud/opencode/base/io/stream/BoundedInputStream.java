package cloud.opencode.base.io.stream;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Bounded Input Stream
 * 限制大小输入流
 *
 * <p>Input stream wrapper that limits the number of bytes that can be read.
 * Useful for preventing reading excessively large data.</p>
 * <p>限制可读取字节数的输入流包装器。
 * 用于防止读取过大的数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Limit maximum read bytes - 限制最大读取字节数</li>
 *   <li>Optional exception on exceed - 超出时可选抛出异常</li>
 *   <li>Track read progress - 追踪读取进度</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (BoundedInputStream bounded = new BoundedInputStream(input, 1024 * 1024)) {
 *     byte[] data = bounded.readAllBytes(); // Max 1MB
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public class BoundedInputStream extends FilterInputStream {

    private final long maxSize;
    private final boolean throwOnExceeding;
    private long bytesRead;
    private long mark;

    /**
     * Creates a bounded input stream
     * 创建限制大小输入流
     *
     * @param in      the underlying input stream | 底层输入流
     * @param maxSize the maximum bytes to read | 最大读取字节数
     */
    public BoundedInputStream(InputStream in, long maxSize) {
        this(in, maxSize, false);
    }

    /**
     * Creates a bounded input stream with exception option
     * 创建带异常选项的限制大小输入流
     *
     * @param in               the underlying input stream | 底层输入流
     * @param maxSize          the maximum bytes to read | 最大读取字节数
     * @param throwOnExceeding whether to throw when limit exceeded | 超出限制时是否抛出异常
     */
    public BoundedInputStream(InputStream in, long maxSize, boolean throwOnExceeding) {
        super(in);
        if (maxSize < 0) {
            throw new IllegalArgumentException("Max size must be non-negative");
        }
        this.maxSize = maxSize;
        this.throwOnExceeding = throwOnExceeding;
        this.bytesRead = 0;
        this.mark = 0;
    }

    @Override
    public int read() throws IOException {
        if (bytesRead >= maxSize) {
            if (throwOnExceeding) {
                throw new OpenIOOperationException("size", null,
                        String.format("Stream size limit exceeded: max %d bytes", maxSize));
            }
            return -1;
        }
        int result = super.read();
        if (result != -1) {
            bytesRead++;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (bytesRead >= maxSize) {
            if (throwOnExceeding) {
                throw new OpenIOOperationException("size", null,
                        String.format("Stream size limit exceeded: max %d bytes", maxSize));
            }
            return -1;
        }

        long remaining = maxSize - bytesRead;
        int actualLen = (int) Math.min(len, remaining);

        int result = super.read(b, off, actualLen);
        if (result > 0) {
            bytesRead += result;
        }
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long remaining = maxSize - bytesRead;
        long toSkip = Math.min(n, remaining);
        long skipped = super.skip(toSkip);
        bytesRead += skipped;
        return skipped;
    }

    @Override
    public int available() throws IOException {
        long remaining = maxSize - bytesRead;
        int available = super.available();
        return (int) Math.min(available, remaining);
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        mark = bytesRead;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        bytesRead = mark;
    }

    /**
     * Gets the number of bytes read
     * 获取已读取字节数
     *
     * @return bytes read | 已读取字节数
     */
    public long getBytesRead() {
        return bytesRead;
    }

    /**
     * Gets the remaining bytes that can be read
     * 获取剩余可读字节数
     *
     * @return remaining bytes | 剩余字节数
     */
    public long getRemaining() {
        return Math.max(0, maxSize - bytesRead);
    }

    /**
     * Gets the maximum size limit
     * 获取最大大小限制
     *
     * @return max size | 最大大小
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * Checks if the limit has been reached
     * 检查是否已达到限制
     *
     * @return true if limit reached | 如果达到限制返回true
     */
    public boolean isLimitReached() {
        return bytesRead >= maxSize;
    }
}
