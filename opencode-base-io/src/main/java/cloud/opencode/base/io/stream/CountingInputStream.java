package cloud.opencode.base.io.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Counting Input Stream
 * 计数输入流
 *
 * <p>Input stream wrapper that counts the number of bytes read.
 * Useful for progress tracking and statistics.</p>
 * <p>统计读取字节数的输入流包装器。
 * 用于进度追踪和统计。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Count bytes read - 统计读取字节数</li>
 *   <li>Reset counter - 重置计数器</li>
 *   <li>Progress tracking - 进度追踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (CountingInputStream counting = new CountingInputStream(input)) {
 *     byte[] data = counting.readAllBytes();
 *     System.out.println("Read " + counting.getCount() + " bytes");
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
public class CountingInputStream extends FilterInputStream {

    private long count;
    private long mark;

    /**
     * Creates a counting input stream
     * 创建计数输入流
     *
     * @param in the underlying input stream | 底层输入流
     */
    public CountingInputStream(InputStream in) {
        super(in);
        this.count = 0;
        this.mark = 0;
    }

    @Override
    public int read() throws IOException {
        int result = super.read();
        if (result != -1) {
            count++;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result > 0) {
            count += result;
        }
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = super.skip(n);
        count += skipped;
        return skipped;
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        mark = count;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        count = mark;
    }

    /**
     * Gets the number of bytes read
     * 获取已读取字节数
     *
     * @return byte count | 字节数
     */
    public long getCount() {
        return count;
    }

    /**
     * Resets the counter and returns the previous count
     * 重置计数器并返回之前的计数
     *
     * @return previous count | 之前的计数
     */
    public long resetCount() {
        long previous = count;
        count = 0;
        mark = 0;
        return previous;
    }
}
