package cloud.opencode.base.io;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stream Processing Utility Class
 * 流处理工具类
 *
 * <p>Utility class for stream operations including copying, reading,
 * writing, and stream transformation.</p>
 * <p>用于流操作的工具类，包括复制、读取、写入和流转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Stream copying - 流复制</li>
 *   <li>Stream reading - 流读取</li>
 *   <li>Stream transformation - 流转换</li>
 *   <li>Safe closing - 安全关闭</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Copy stream
 * long copied = OpenStream.copy(input, output);
 *
 * // Read as bytes
 * byte[] data = OpenStream.toByteArray(input);
 *
 * // Read as string
 * String content = OpenStream.toString(input);
 *
 * // Safe close
 * OpenStream.closeQuietly(stream1, stream2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class OpenStream {

    /**
     * Default buffer size (8KB)
     * 默认缓冲区大小（8KB）
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private OpenStream() {
    }

    // ==================== Copy Operations | 复制操作 ====================

    /**
     * Copies input stream to output stream
     * 复制输入流到输出流
     *
     * @param input  the input stream | 输入流
     * @param output the output stream | 输出流
     * @return bytes copied | 复制的字节数
     */
    public static long copy(InputStream input, OutputStream output) {
        return copy(input, output, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies input stream to output stream with buffer size
     * 使用指定缓冲区大小复制输入流到输出流
     *
     * @param input      the input stream | 输入流
     * @param output     the output stream | 输出流
     * @param bufferSize the buffer size | 缓冲区大小
     * @return bytes copied | 复制的字节数
     */
    public static long copy(InputStream input, OutputStream output, int bufferSize) {
        try {
            byte[] buffer = new byte[bufferSize];
            long count = 0;
            int n;
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
                count += n;
            }
            return count;
        } catch (IOException e) {
            throw OpenIOOperationException.streamOperationFailed("copy", e);
        }
    }

    /**
     * Copies Reader to Writer
     * 复制Reader到Writer
     *
     * @param reader the reader | Reader
     * @param writer the writer | Writer
     * @return characters copied | 复制的字符数
     */
    public static long copy(Reader reader, Writer writer) {
        try {
            char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            long count = 0;
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
                count += n;
            }
            return count;
        } catch (IOException e) {
            throw OpenIOOperationException.streamOperationFailed("copy", e);
        }
    }

    /**
     * Copies input stream to file
     * 复制输入流到文件
     *
     * @param input the input stream | 输入流
     * @param file  the target file | 目标文件
     * @return bytes copied | 复制的字节数
     */
    public static long copyToFile(InputStream input, Path file) {
        try (OutputStream output = Files.newOutputStream(file)) {
            return copy(input, output);
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(file, e);
        }
    }

    // ==================== Read Operations | 读取操作 ====================

    /**
     * Reads input stream as byte array
     * 读取输入流为字节数组
     *
     * @param input the input stream | 输入流
     * @return byte array | 字节数组
     */
    public static byte[] toByteArray(InputStream input) {
        try {
            return input.readAllBytes();
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(e);
        }
    }

    /**
     * Reads input stream as byte array with size limit
     * 读取输入流为字节数组（带大小限制）
     *
     * @param input   the input stream | 输入流
     * @param maxSize the max size | 最大大小
     * @return byte array | 字节数组
     */
    public static byte[] toByteArray(InputStream input, int maxSize) {
        try {
            return input.readNBytes(maxSize);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(e);
        }
    }

    /**
     * Reads input stream as string
     * 读取输入流为字符串
     *
     * @param input   the input stream | 输入流
     * @param charset the charset | 字符集
     * @return content string | 内容字符串
     */
    public static String toString(InputStream input, Charset charset) {
        return new String(toByteArray(input), charset);
    }

    /**
     * Reads input stream as UTF-8 string
     * 读取输入流为UTF-8字符串
     *
     * @param input the input stream | 输入流
     * @return content string | 内容字符串
     */
    public static String toString(InputStream input) {
        return toString(input, StandardCharsets.UTF_8);
    }

    /**
     * Reads reader as string
     * 读取Reader为字符串
     *
     * @param reader the reader | Reader
     * @return content string | 内容字符串
     */
    public static String toString(Reader reader) {
        try {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            int n;
            while ((n = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, n);
            }
            return sb.toString();
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(e);
        }
    }

    // ==================== Write Operations | 写入操作 ====================

    /**
     * Writes byte array to output stream
     * 写入字节数组到输出流
     *
     * @param data   the data | 数据
     * @param output the output stream | 输出流
     */
    public static void write(byte[] data, OutputStream output) {
        try {
            output.write(data);
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(e);
        }
    }

    /**
     * Writes string to output stream
     * 写入字符串到输出流
     *
     * @param data    the data | 数据
     * @param output  the output stream | 输出流
     * @param charset the charset | 字符集
     */
    public static void write(String data, OutputStream output, Charset charset) {
        write(data.getBytes(charset), output);
    }

    /**
     * Writes string to writer
     * 写入字符串到Writer
     *
     * @param data   the data | 数据
     * @param writer the writer | Writer
     */
    public static void write(String data, Writer writer) {
        try {
            writer.write(data);
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(e);
        }
    }

    // ==================== Close Operations | 关闭操作 ====================

    /**
     * Closes safely, ignoring exceptions
     * 安全关闭，忽略异常
     *
     * @param closeable the closeable | 可关闭对象
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Closes multiple closeables safely
     * 安全关闭多个可关闭对象
     *
     * @param closeables the closeables | 可关闭对象数组
     */
    public static void closeQuietly(Closeable... closeables) {
        if (closeables != null) {
            for (Closeable closeable : closeables) {
                closeQuietly(closeable);
            }
        }
    }

    // ==================== Transform Operations | 转换操作 ====================

    /**
     * Wraps as buffered input stream
     * 包装为缓冲输入流
     *
     * @param input the input stream | 输入流
     * @return buffered input stream | 缓冲输入流
     */
    public static BufferedInputStream buffer(InputStream input) {
        if (input instanceof BufferedInputStream bis) {
            return bis;
        }
        return new BufferedInputStream(input, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Wraps as buffered output stream
     * 包装为缓冲输出流
     *
     * @param output the output stream | 输出流
     * @return buffered output stream | 缓冲输出流
     */
    public static BufferedOutputStream buffer(OutputStream output) {
        if (output instanceof BufferedOutputStream bos) {
            return bos;
        }
        return new BufferedOutputStream(output, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Converts input stream to reader
     * 将输入流转换为Reader
     *
     * @param input   the input stream | 输入流
     * @param charset the charset | 字符集
     * @return reader | Reader
     */
    public static Reader toReader(InputStream input, Charset charset) {
        return new BufferedReader(new InputStreamReader(input, charset));
    }

    /**
     * Converts output stream to writer
     * 将输出流转换为Writer
     *
     * @param output  the output stream | 输出流
     * @param charset the charset | 字符集
     * @return writer | Writer
     */
    public static Writer toWriter(OutputStream output, Charset charset) {
        return new BufferedWriter(new OutputStreamWriter(output, charset));
    }

    /**
     * Converts string to input stream
     * 将字符串转换为输入流
     *
     * @param data    the data | 数据
     * @param charset the charset | 字符集
     * @return input stream | 输入流
     */
    public static InputStream toInputStream(String data, Charset charset) {
        return new ByteArrayInputStream(data.getBytes(charset));
    }

    /**
     * Converts byte array to input stream
     * 将字节数组转换为输入流
     *
     * @param data the data | 数据
     * @return input stream | 输入流
     */
    public static InputStream toInputStream(byte[] data) {
        return new ByteArrayInputStream(data);
    }

    // ==================== Skip and Drain | 跳过和排空 ====================

    /**
     * Skips bytes from input stream
     * 从输入流跳过字节
     *
     * @param input the input stream | 输入流
     * @param skip  bytes to skip | 要跳过的字节数
     * @return actual bytes skipped | 实际跳过的字节数
     */
    public static long skip(InputStream input, long skip) {
        try {
            return input.skip(skip);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(e);
        }
    }

    /**
     * Drains all remaining bytes from input stream
     * 排空输入流中的所有剩余字节
     *
     * @param input the input stream | 输入流
     * @return bytes drained | 排空的字节数
     */
    public static long drain(InputStream input) {
        try {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            long count = 0;
            int n;
            while ((n = input.read(buffer)) != -1) {
                count += n;
            }
            return count;
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(e);
        }
    }

    // ==================== Compare Operations | 比较操作 ====================

    /**
     * Compares two input streams for content equality
     * 比较两个输入流的内容是否相同
     *
     * @param input1 the first input stream | 第一个输入流
     * @param input2 the second input stream | 第二个输入流
     * @return true if equal | 如果相同返回true
     */
    public static boolean contentEquals(InputStream input1, InputStream input2) {
        try {
            byte[] buf1 = new byte[DEFAULT_BUFFER_SIZE];
            byte[] buf2 = new byte[DEFAULT_BUFFER_SIZE];
            while (true) {
                int read1 = readFully(input1, buf1);
                int read2 = readFully(input2, buf2);
                if (read1 != read2) {
                    return false;
                }
                if (read1 == -1) {
                    return true;
                }
                if (!Arrays.equals(buf1, 0, read1, buf2, 0, read2)) {
                    return false;
                }
            }
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(e);
        }
    }

    private static int readFully(InputStream in, byte[] buf) throws IOException {
        int total = 0;
        while (total < buf.length) {
            int read = in.read(buf, total, buf.length - total);
            if (read == -1) {
                return total == 0 ? -1 : total;
            }
            total += read;
        }
        return total;
    }

    /**
     * Compares two readers for content equality
     * 比较两个Reader的内容是否相同
     *
     * @param reader1 the first reader | 第一个Reader
     * @param reader2 the second reader | 第二个Reader
     * @return true if equal | 如果相同返回true
     */
    public static boolean contentEquals(Reader reader1, Reader reader2) {
        try {
            char[] buf1 = new char[DEFAULT_BUFFER_SIZE];
            char[] buf2 = new char[DEFAULT_BUFFER_SIZE];
            while (true) {
                int read1 = readFullyChars(reader1, buf1);
                int read2 = readFullyChars(reader2, buf2);
                if (read1 != read2) {
                    return false;
                }
                if (read1 == -1) {
                    return true;
                }
                if (!Arrays.equals(buf1, 0, read1, buf2, 0, read2)) {
                    return false;
                }
            }
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(e);
        }
    }

    private static int readFullyChars(Reader reader, char[] buf) throws IOException {
        int total = 0;
        while (total < buf.length) {
            int read = reader.read(buf, total, buf.length - total);
            if (read == -1) {
                return total == 0 ? -1 : total;
            }
            total += read;
        }
        return total;
    }
}
