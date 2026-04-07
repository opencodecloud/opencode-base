package cloud.opencode.base.io.stream;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Writer to OutputStream Adapter
 * Writer到OutputStream的适配器
 *
 * <p>Adapts a {@link Writer} to behave as an {@link OutputStream} by decoding
 * bytes to characters using a {@link CharsetDecoder}. This is the inverse of
 * {@link java.io.OutputStreamWriter}.</p>
 * <p>通过使用{@link CharsetDecoder}将字节解码为字符，将{@link Writer}适配为
 * {@link OutputStream}。这是{@link java.io.OutputStreamWriter}的逆操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Converts Writer to OutputStream - 将Writer转换为OutputStream</li>
 *   <li>Configurable charset decoding - 可配置的字符集解码</li>
 *   <li>Buffered byte accumulation - 缓冲字节累积</li>
 *   <li>Proper decoder flushing on close - 关闭时正确刷新解码器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert a StringWriter to OutputStream (UTF-8)
 * StringWriter sw = new StringWriter();
 * try (OutputStream os = new WriterOutputStream(sw)) {
 *     os.write("Hello".getBytes(StandardCharsets.UTF_8));
 * }
 * String result = sw.toString();
 *
 * // With specific charset
 * try (OutputStream os = new WriterOutputStream(sw, StandardCharsets.ISO_8859_1)) {
 *     os.write(data);
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
public class WriterOutputStream extends OutputStream {

    private static final int BYTE_BUFFER_SIZE = 1024;
    private static final int CHAR_BUFFER_SIZE = 1024;

    private final Writer writer;
    private final CharsetDecoder decoder;
    private final ByteBuffer byteBuffer;
    private final CharBuffer charBuffer;

    /**
     * Creates a WriterOutputStream with the specified charset decoder
     * 使用指定的字符集解码器创建WriterOutputStream
     *
     * @param writer  the writer to adapt | 要适配的Writer
     * @param decoder the charset decoder | 字符集解码器
     * @throws NullPointerException if writer or decoder is null | 当writer或decoder为null时抛出
     */
    public WriterOutputStream(Writer writer, CharsetDecoder decoder) {
        this.writer = Objects.requireNonNull(writer, "writer must not be null");
        this.decoder = Objects.requireNonNull(decoder, "decoder must not be null");
        this.byteBuffer = ByteBuffer.allocate(BYTE_BUFFER_SIZE);
        this.charBuffer = CharBuffer.allocate(CHAR_BUFFER_SIZE);
    }

    /**
     * Creates a WriterOutputStream with the specified charset
     * 使用指定的字符集创建WriterOutputStream
     *
     * @param writer  the writer to adapt | 要适配的Writer
     * @param charset the charset for decoding | 解码使用的字符集
     * @throws NullPointerException if writer or charset is null | 当writer或charset为null时抛出
     */
    public WriterOutputStream(Writer writer, Charset charset) {
        this(writer, Objects.requireNonNull(charset, "charset must not be null").newDecoder());
    }

    /**
     * Creates a WriterOutputStream with UTF-8 decoding
     * 使用UTF-8解码创建WriterOutputStream
     *
     * @param writer the writer to adapt | 要适配的Writer
     * @throws NullPointerException if writer is null | 当writer为null时抛出
     */
    public WriterOutputStream(Writer writer) {
        this(writer, StandardCharsets.UTF_8);
    }

    /**
     * Writes a single byte to the stream
     * 向流中写入单个字节
     *
     * @param b the byte to write | 要写入的字节
     * @throws IOException if an I/O error occurs | 当发生I/O错误时抛出
     */
    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    /**
     * Writes bytes from the specified buffer
     * 从指定的缓冲区写入字节
     *
     * @param b   the buffer containing bytes to write | 包含要写入字节的缓冲区
     * @param off the start offset in the buffer | 缓冲区中的起始偏移量
     * @param len the number of bytes to write | 要写入的字节数
     * @throws IOException               if an I/O error occurs | 当发生I/O错误时抛出
     * @throws IndexOutOfBoundsException  if off or len is invalid | 当off或len无效时抛出
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);

        int remaining = len;
        int offset = off;

        while (remaining > 0) {
            int space = byteBuffer.remaining();
            int toWrite = Math.min(remaining, space);
            byteBuffer.put(b, offset, toWrite);
            offset += toWrite;
            remaining -= toWrite;

            if (!byteBuffer.hasRemaining()) {
                decodeAndWrite(false);
            }
        }
    }

    /**
     * Flushes the stream by decoding any buffered bytes and flushing the writer
     * 通过解码所有缓冲的字节并刷新Writer来刷新流
     *
     * @throws IOException if an I/O error occurs | 当发生I/O错误时抛出
     */
    @Override
    public void flush() throws IOException {
        decodeAndWrite(false);
        writer.flush();
    }

    /**
     * Closes the stream, flushing any remaining bytes, and closes the writer
     * 关闭流，刷新所有剩余字节，并关闭Writer
     *
     * @throws IOException if an I/O error occurs | 当发生I/O错误时抛出
     */
    @Override
    public void close() throws IOException {
        IOException flushError = null;
        try {
            decodeAndWrite(true);
            flushCharBuffer();
            writer.flush();
        } catch (IOException e) {
            flushError = e;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                if (flushError != null) {
                    e.addSuppressed(flushError);
                }
                throw e;
            }
        }
        if (flushError != null) {
            throw flushError;
        }
    }

    /**
     * Decodes bytes from byteBuffer to charBuffer and writes to writer.
     * 将字节从byteBuffer解码到charBuffer并写入writer。
     *
     * @param endOfInput true if this is the last data | 如果这是最后的数据则为true
     */
    private void decodeAndWrite(boolean endOfInput) throws IOException {
        byteBuffer.flip();

        while (true) {
            CoderResult result = decoder.decode(byteBuffer, charBuffer, endOfInput);
            if (result.isError()) {
                result.throwException();
            }
            flushCharBuffer();

            if (result.isUnderflow()) {
                break;
            }
        }

        if (endOfInput) {
            // Flush the decoder
            while (true) {
                CoderResult result = decoder.flush(charBuffer);
                flushCharBuffer();
                if (result.isUnderflow()) {
                    break;
                }
            }
        }

        byteBuffer.compact();
    }

    /**
     * Writes the contents of the char buffer to the writer.
     * 将字符缓冲区的内容写入writer。
     */
    private void flushCharBuffer() throws IOException {
        charBuffer.flip();
        if (charBuffer.hasRemaining()) {
            writer.write(charBuffer.toString());
        }
        charBuffer.clear();
    }
}
