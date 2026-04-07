package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Tail Reader Utility
 * 尾部读取器工具
 *
 * <p>Efficient utility for reading the last N lines or bytes of a file using
 * backward scanning with {@link RandomAccessFile}. Avoids reading the entire
 * file into memory.</p>
 * <p>使用{@link RandomAccessFile}反向扫描高效读取文件末尾N行或N字节的工具。
 * 避免将整个文件读入内存。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Read last N lines efficiently - 高效读取最后N行</li>
 *   <li>Read last N bytes - 读取最后N个字节</li>
 *   <li>Configurable charset - 可配置的字符集</li>
 *   <li>Block-based backward scanning - 基于块的反向扫描</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Read last 10 lines (UTF-8)
 * List<String> lines = TailReader.lastLines(path, 10);
 *
 * // Read last 10 lines with specific charset
 * List<String> lines = TailReader.lastLines(path, 10, StandardCharsets.ISO_8859_1);
 *
 * // Read last 1024 bytes
 * byte[] tail = TailReader.lastBytes(path, 1024);
 * }</pre>
 *
 * <p><strong>Limitations | 限制:</strong></p>
 * <ul>
 *   <li>Line detection relies on newline byte (0x0A). This works reliably for
 *       single-byte charsets and UTF-8, but may not work correctly for charsets
 *       where 0x0A can appear as part of a multi-byte sequence (e.g., some legacy
 *       CJK encodings). - 行检测依赖换行字节(0x0A)。这对于单字节字符集和UTF-8可靠工作，
 *       但对于0x0A可能出现在多字节序列中的字符集可能无法正确工作。</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
public final class TailReader {

    /**
     * Default block size for backward scanning (8 KB)
     * 反向扫描的默认块大小（8 KB）
     */
    static final int DEFAULT_BLOCK_SIZE = 8192;

    private TailReader() {
        // Utility class
    }

    /**
     * Reads the last N lines of a file using UTF-8 charset
     * 使用UTF-8字符集读取文件的最后N行
     *
     * @param path  the file path | 文件路径
     * @param count the number of lines to read | 要读取的行数
     * @return the last N lines (or fewer if file has less) | 最后N行（如果文件行数不足则返回全部）
     * @throws IllegalArgumentException    if count is not positive | 当count不为正数时抛出
     * @throws NullPointerException        if path is null | 当path为null时抛出
     * @throws OpenIOOperationException    if an I/O error occurs | 当发生I/O错误时抛出
     */
    public static List<String> lastLines(Path path, int count) {
        return lastLines(path, count, StandardCharsets.UTF_8);
    }

    /**
     * Reads the last N lines of a file with the specified charset
     * 使用指定字符集读取文件的最后N行
     *
     * @param path    the file path | 文件路径
     * @param count   the number of lines to read | 要读取的行数
     * @param charset the charset to decode the file | 解码文件的字符集
     * @return the last N lines (or fewer if file has less) | 最后N行（如果文件行数不足则返回全部）
     * @throws IllegalArgumentException    if count is not positive | 当count不为正数时抛出
     * @throws NullPointerException        if path or charset is null | 当path或charset为null时抛出
     * @throws OpenIOOperationException    if an I/O error occurs | 当发生I/O错误时抛出
     */
    public static List<String> lastLines(Path path, int count, Charset charset) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive, got: " + count);
        }

        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            long fileLength = raf.length();
            if (fileLength == 0) {
                return List.of();
            }

            // Scan backward to find newlines
            long pos = fileLength;
            int newlineCount = 0;
            // Track whether the file ends with a newline
            boolean endsWithNewline = false;

            byte[] block = new byte[DEFAULT_BLOCK_SIZE];

            while (pos > 0 && newlineCount <= count) {
                long blockStart = Math.max(0, pos - DEFAULT_BLOCK_SIZE);
                int blockSize = (int) (pos - blockStart);

                raf.seek(blockStart);
                raf.readFully(block, 0, blockSize);

                for (int i = blockSize - 1; i >= 0; i--) {
                    if (block[i] == '\n') {
                        // If this is the very last byte, note it but don't count as a line separator yet
                        if (blockStart + i == fileLength - 1) {
                            endsWithNewline = true;
                            continue;
                        }
                        newlineCount++;
                        if (newlineCount == count) {
                            // The lines start right after this newline
                            long startOffset = blockStart + i + 1;
                            return readLinesFrom(raf, startOffset, fileLength, charset, endsWithNewline);
                        }
                    }
                }

                pos = blockStart;
            }

            // If we reach here, the file has fewer lines than count — return all lines
            return readLinesFrom(raf, 0, fileLength, charset, endsWithNewline);

        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Reads the last N bytes of a file
     * 读取文件的最后N个字节
     *
     * @param path  the file path | 文件路径
     * @param count the number of bytes to read | 要读取的字节数
     * @return the last N bytes (or fewer if file is smaller) | 最后N个字节（如果文件更小则返回全部）
     * @throws IllegalArgumentException    if count is not positive | 当count不为正数时抛出
     * @throws NullPointerException        if path is null | 当path为null时抛出
     * @throws OpenIOOperationException    if an I/O error occurs | 当发生I/O错误时抛出
     */
    public static byte[] lastBytes(Path path, int count) {
        Objects.requireNonNull(path, "path must not be null");
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive, got: " + count);
        }

        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            long fileLength = raf.length();
            int toRead = (int) Math.min(count, fileLength);
            byte[] result = new byte[toRead];

            raf.seek(fileLength - toRead);
            raf.readFully(result);

            return result;
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Reads lines from a specific offset to the end of file.
     * 从指定偏移量读取行到文件末尾。
     */
    private static List<String> readLinesFrom(RandomAccessFile raf, long startOffset,
                                               long fileLength, Charset charset,
                                               boolean endsWithNewline) throws IOException {
        long longLength = fileLength - startOffset;
        if (longLength > Integer.MAX_VALUE - 8) {
            throw new OpenIOOperationException("tail", null,
                    String.format("Tail region too large: %d bytes exceeds maximum array size", longLength));
        }
        int length = (int) longLength;
        byte[] data = new byte[length];
        raf.seek(startOffset);
        raf.readFully(data);

        String content = new String(data, charset);

        // Remove trailing newline if present (it doesn't represent an extra line)
        if (endsWithNewline && content.endsWith("\n")) {
            content = content.substring(0, content.length() - 1);
        }
        // Also handle \r\n
        if (content.endsWith("\r")) {
            content = content.substring(0, content.length() - 1);
        }

        if (content.isEmpty()) {
            return List.of();
        }

        // Split by newline, preserving empty strings between newlines
        String[] parts = content.split("\n", -1);
        List<String> lines = new ArrayList<>(parts.length);
        for (String part : parts) {
            // Remove trailing \r for Windows-style line endings
            if (part.endsWith("\r")) {
                lines.add(part.substring(0, part.length() - 1));
            } else {
                lines.add(part);
            }
        }
        return Collections.unmodifiableList(lines);
    }
}
