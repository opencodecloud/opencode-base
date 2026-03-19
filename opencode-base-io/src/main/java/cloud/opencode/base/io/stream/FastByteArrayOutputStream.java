package cloud.opencode.base.io.stream;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Fast Byte Array Output Stream
 * 高性能字节数组输出流
 *
 * <p>High-performance byte array output stream that minimizes
 * unnecessary array copying operations.</p>
 * <p>高性能字节数组输出流，最小化不必要的数组复制操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Efficient memory usage - 高效内存使用</li>
 *   <li>Direct buffer access - 直接缓冲区访问</li>
 *   <li>No synchronization overhead - 无同步开销</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FastByteArrayOutputStream out = new FastByteArrayOutputStream();
 * out.write("Hello".getBytes());
 * byte[] data = out.toByteArray();
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
public class FastByteArrayOutputStream extends OutputStream {

    private static final int DEFAULT_INITIAL_CAPACITY = 256;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private byte[] buffer;
    private int count;

    /**
     * Creates a stream with default capacity
     * 创建默认容量的流
     */
    public FastByteArrayOutputStream() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Creates a stream with specified initial capacity
     * 创建指定初始容量的流
     *
     * @param initialCapacity the initial capacity | 初始容量
     */
    public FastByteArrayOutputStream(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Negative initial capacity: " + initialCapacity);
        }
        this.buffer = new byte[initialCapacity];
        this.count = 0;
    }

    @Override
    public void write(int b) {
        ensureCapacity(count + 1);
        buffer[count++] = (byte) b;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        ensureCapacity(count + len);
        System.arraycopy(b, off, buffer, count, len);
        count += len;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > buffer.length) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        int oldCapacity = buffer.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }
        buffer = Arrays.copyOf(buffer, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    /**
     * Gets the current size
     * 获取当前大小
     *
     * @return byte count | 字节数
     */
    public int size() {
        return count;
    }

    /**
     * Gets the byte array (may share internal buffer)
     * 获取字节数组（可能共享内部缓冲区）
     *
     * <p>Note: If the buffer is exactly the right size, the internal
     * buffer is returned directly. Modifying it will affect this stream.</p>
     * <p>注意：如果缓冲区大小刚好合适，将直接返回内部缓冲区。
     * 修改它会影响此流。</p>
     *
     * @return byte array | 字节数组
     */
    public byte[] toByteArray() {
        if (count == buffer.length) {
            return buffer;
        }
        return Arrays.copyOf(buffer, count);
    }

    /**
     * Gets a copy of the byte array
     * 获取字节数组副本
     *
     * @return byte array copy | 字节数组副本
     */
    public byte[] toByteArrayCopy() {
        return Arrays.copyOf(buffer, count);
    }

    /**
     * Writes content to another output stream
     * 将内容写入另一个输出流
     *
     * @param out the target output stream | 目标输出流
     */
    public void writeTo(OutputStream out) {
        try {
            out.write(buffer, 0, count);
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(e);
        }
    }

    /**
     * Resets the stream
     * 重置流
     */
    public void reset() {
        count = 0;
    }

    /**
     * Converts to input stream
     * 转换为输入流
     *
     * @return input stream | 输入流
     */
    public InputStream toInputStream() {
        return new ByteArrayInputStream(buffer, 0, count);
    }

    /**
     * Gets the internal buffer (for advanced use)
     * 获取内部缓冲区（高级用法）
     *
     * @return internal buffer | 内部缓冲区
     */
    public byte[] getBuffer() {
        return buffer;
    }

    @Override
    public String toString() {
        return new String(buffer, 0, count);
    }

    /**
     * Converts to string with charset
     * 使用字符集转换为字符串
     *
     * @param charsetName the charset name | 字符集名称
     * @return string | 字符串
     */
    public String toString(String charsetName) {
        try {
            return new String(buffer, 0, count, charsetName);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new OpenIOOperationException("Unsupported charset: " + charsetName, e);
        }
    }
}
