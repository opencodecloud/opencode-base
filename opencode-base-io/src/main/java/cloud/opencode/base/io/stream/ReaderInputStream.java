package cloud.opencode.base.io.stream;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Reader to InputStream Adapter
 * Reader到InputStream的适配器
 *
 * <p>Adapts a {@link Reader} to behave as an {@link InputStream} by encoding
 * characters to bytes using a {@link CharsetEncoder}. This is the inverse of
 * {@link java.io.InputStreamReader}.</p>
 * <p>通过使用{@link CharsetEncoder}将字符编码为字节，将{@link Reader}适配为
 * {@link InputStream}。这是{@link java.io.InputStreamReader}的逆操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Converts Reader to InputStream - 将Reader转换为InputStream</li>
 *   <li>Configurable charset encoding - 可配置的字符集编码</li>
 *   <li>Efficient buffered conversion - 高效的缓冲转换</li>
 *   <li>Proper encoder flushing on stream end - 流结束时正确刷新编码器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert a StringReader to InputStream (UTF-8)
 * Reader reader = new StringReader("Hello World");
 * try (InputStream is = new ReaderInputStream(reader)) {
 *     byte[] data = is.readAllBytes();
 * }
 *
 * // With specific charset
 * try (InputStream is = new ReaderInputStream(reader, StandardCharsets.ISO_8859_1)) {
 *     process(is);
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
 * @since JDK 25, opencode-base-io V1.0.3
 */
public class ReaderInputStream extends InputStream {

    private static final int CHAR_BUFFER_SIZE = 1024;

    private final Reader reader;
    private final CharsetEncoder encoder;
    private final CharBuffer charBuffer;
    private final ByteBuffer byteBuffer;
    private boolean endOfReader;
    private boolean encoderEndOfInputSignaled;
    private boolean endOfEncoder;

    /**
     * Creates a ReaderInputStream with the specified charset encoder
     * 使用指定的字符集编码器创建ReaderInputStream
     *
     * @param reader  the reader to adapt | 要适配的Reader
     * @param encoder the charset encoder | 字符集编码器
     * @throws NullPointerException if reader or encoder is null | 当reader或encoder为null时抛出
     */
    public ReaderInputStream(Reader reader, CharsetEncoder encoder) {
        this.reader = Objects.requireNonNull(reader, "reader must not be null");
        this.encoder = Objects.requireNonNull(encoder, "encoder must not be null");
        this.charBuffer = CharBuffer.allocate(CHAR_BUFFER_SIZE);
        this.charBuffer.flip(); // start empty, ready for reading
        float maxBytesPerChar = encoder.maxBytesPerChar();
        int byteBufferSize = (int) Math.ceil(CHAR_BUFFER_SIZE * maxBytesPerChar);
        this.byteBuffer = ByteBuffer.allocate(byteBufferSize);
        this.byteBuffer.flip(); // start empty, ready for reading
    }

    /**
     * Creates a ReaderInputStream with the specified charset
     * 使用指定的字符集创建ReaderInputStream
     *
     * @param reader  the reader to adapt | 要适配的Reader
     * @param charset the charset for encoding | 编码使用的字符集
     * @throws NullPointerException if reader or charset is null | 当reader或charset为null时抛出
     */
    public ReaderInputStream(Reader reader, Charset charset) {
        this(reader, Objects.requireNonNull(charset, "charset must not be null").newEncoder());
    }

    /**
     * Creates a ReaderInputStream with UTF-8 encoding
     * 使用UTF-8编码创建ReaderInputStream
     *
     * @param reader the reader to adapt | 要适配的Reader
     * @throws NullPointerException if reader is null | 当reader为null时抛出
     */
    public ReaderInputStream(Reader reader) {
        this(reader, StandardCharsets.UTF_8);
    }

    /**
     * Reads a single byte from the stream
     * 从流中读取单个字节
     *
     * @return the byte read, or -1 if end of stream | 读取的字节，如果到达流末尾则返回-1
     * @throws OpenIOOperationException if an I/O error occurs | 当发生I/O错误时抛出
     */
    @Override
    public int read() throws IOException {
        byte[] single = new byte[1];
        int result = read(single, 0, 1);
        if (result == -1) {
            return -1;
        }
        return single[0] & 0xFF;
    }

    /**
     * Reads bytes into the specified buffer
     * 将字节读入指定的缓冲区
     *
     * @param b   the buffer to read into | 读入的缓冲区
     * @param off the start offset in the buffer | 缓冲区中的起始偏移量
     * @param len the maximum number of bytes to read | 要读取的最大字节数
     * @return the number of bytes read, or -1 if end of stream | 读取的字节数，如果到达流末尾则返回-1
     * @throws IOException              if an I/O error occurs | 当发生I/O错误时抛出
     * @throws IndexOutOfBoundsException if off or len is invalid | 当off或len无效时抛出
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) {
            return 0;
        }

        int totalRead = 0;

        while (totalRead < len) {
            // 1. Drain any remaining bytes from byteBuffer
            if (byteBuffer.hasRemaining()) {
                int toRead = Math.min(len - totalRead, byteBuffer.remaining());
                byteBuffer.get(b, off + totalRead, toRead);
                totalRead += toRead;
                continue;
            }

            // 2. If encoder is done, no more data
            if (endOfEncoder) {
                break;
            }

            // 3. Fill byteBuffer by encoding from charBuffer
            fillByteBuffer();
        }

        return totalRead == 0 ? -1 : totalRead;
    }

    /**
     * Fills the byte buffer by encoding characters from the char buffer.
     * 通过从字符缓冲区编码字符来填充字节缓冲区。
     */
    private void fillByteBuffer() throws IOException {
        byteBuffer.clear();

        // Ensure charBuffer has data
        if (!charBuffer.hasRemaining() && !endOfReader) {
            charBuffer.clear();
            int charsRead = reader.read(charBuffer);
            if (charsRead == -1) {
                endOfReader = true;
            }
            charBuffer.flip();
        }

        if (endOfReader && !encoderEndOfInputSignaled) {
            // Signal end-of-input to encoder (required before flush)
            CoderResult result = encoder.encode(charBuffer, byteBuffer, true);
            if (result.isError()) {
                result.throwException();
            }
            encoderEndOfInputSignaled = true;
            // If charBuffer still has remaining, we need more rounds of encode
            if (!charBuffer.hasRemaining()) {
                // Now flush — only mark done if flush completes (not OVERFLOW)
                CoderResult flushResult = encoder.flush(byteBuffer);
                if (!flushResult.isOverflow()) {
                    endOfEncoder = true;
                }
            }
            byteBuffer.flip();
            return;
        }

        if (encoderEndOfInputSignaled && !endOfEncoder) {
            // Continue flushing — only mark done if flush completes (not OVERFLOW)
            CoderResult flushResult = encoder.flush(byteBuffer);
            if (!flushResult.isOverflow()) {
                endOfEncoder = true;
            }
            byteBuffer.flip();
            return;
        }

        if (endOfEncoder) {
            byteBuffer.flip();
            return;
        }

        // Encode chars to bytes (not end of input yet)
        CoderResult result = encoder.encode(charBuffer, byteBuffer, false);
        if (result.isError()) {
            result.throwException();
        }

        byteBuffer.flip();
    }

    /**
     * Closes the stream and the underlying reader
     * 关闭流和底层的Reader
     *
     * @throws IOException if an I/O error occurs | 当发生I/O错误时抛出
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }
}
