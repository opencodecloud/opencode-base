package cloud.opencode.base.io.hex;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Hex Dump Utility
 * 十六进制转储工具
 *
 * <p>Utility class for formatting binary data as hexadecimal dump output.
 * Produces standard hex dump format with offset, hex bytes, and ASCII representation.</p>
 * <p>用于将二进制数据格式化为十六进制转储输出的工具类。
 * 生成包含偏移量、十六进制字节和ASCII表示的标准十六进制转储格式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Standard hex dump format (16 bytes per line) - 标准十六进制转储格式（每行16字节）</li>
 *   <li>Format byte arrays, InputStreams, and files - 格式化字节数组、输入流和文件</li>
 *   <li>Hex encoding/decoding (toHex/fromHex) - 十六进制编码/解码</li>
 *   <li>Configurable read limits for streams and files - 可配置的流和文件读取限制</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Format a byte array as hex dump
 * String dump = HexDump.format("Hello, World!".getBytes());
 * // 00000000  48 65 6C 6C 6F 2C 20 57  6F 72 6C 64 21           |Hello, World!|
 *
 * // Convert to/from hex string
 * String hex = HexDump.toHex(new byte[]{0x0A, 0x0B});  // "0a0b"
 * byte[] data = HexDump.fromHex("0a0b");                // {0x0A, 0x0B}
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Input validation on all methods - 所有方法均有输入验证</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
public final class HexDump {

    /**
     * Number of bytes displayed per line in hex dump format
     * 十六进制转储格式中每行显示的字节数
     */
    public static final int BYTES_PER_LINE = 16;

    /**
     * Default maximum bytes to read for unbounded operations (1 MB)
     * 无界操作的默认最大读取字节数（1 MB）
     */
    public static final int MAX_DEFAULT_BYTES = 1024 * 1024;

    private static final char[] HEX_LOWER = "0123456789abcdef".toCharArray();
    private static final char[] HEX_UPPER = "0123456789ABCDEF".toCharArray();

    private HexDump() {
        throw new AssertionError("No instances");
    }

    // ==================== Hex Dump Format | 十六进制转储格式 ====================

    /**
     * Formats an entire byte array as hex dump
     * 将整个字节数组格式化为十六进制转储
     *
     * @param data the byte array to format | 要格式化的字节数组
     * @return hex dump string | 十六进制转储字符串
     * @throws NullPointerException if data is null | 当data为null时抛出
     */
    public static String format(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");
        return format(data, 0, data.length);
    }

    /**
     * Formats a range of a byte array as hex dump
     * 将字节数组的指定范围格式化为十六进制转储
     *
     * @param data   the byte array | 字节数组
     * @param offset the start offset | 起始偏移量
     * @param length the number of bytes to format | 要格式化的字节数
     * @return hex dump string | 十六进制转储字符串
     * @throws NullPointerException      if data is null | 当data为null时抛出
     * @throws IllegalArgumentException if range is invalid | 当范围无效时抛出
     */
    public static String format(byte[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        if (offset < 0 || length < 0 || offset + length > data.length) {
            throw new IllegalArgumentException(
                    String.format("Invalid range: offset=%d, length=%d, array length=%d",
                            offset, length, data.length));
        }
        if (length == 0) {
            return "";
        }
        return formatDump(data, offset, length);
    }

    /**
     * Formats data from an InputStream as hex dump, reading up to MAX_DEFAULT_BYTES
     * 从InputStream格式化数据为十六进制转储，最多读取MAX_DEFAULT_BYTES字节
     *
     * @param input the input stream | 输入流
     * @return hex dump string | 十六进制转储字符串
     * @throws NullPointerException      if input is null | 当input为null时抛出
     * @throws OpenIOOperationException if an IO error occurs | 当IO错误发生时抛出
     */
    public static String format(InputStream input) {
        return format(input, MAX_DEFAULT_BYTES);
    }

    /**
     * Formats data from an InputStream as hex dump with a byte limit
     * 从InputStream格式化数据为十六进制转储，带字节限制
     *
     * @param input    the input stream | 输入流
     * @param maxBytes maximum bytes to read | 最大读取字节数
     * @return hex dump string | 十六进制转储字符串
     * @throws NullPointerException      if input is null | 当input为null时抛出
     * @throws IllegalArgumentException if maxBytes is negative | 当maxBytes为负时抛出
     * @throws OpenIOOperationException if an IO error occurs | 当IO错误发生时抛出
     */
    public static String format(InputStream input, int maxBytes) {
        Objects.requireNonNull(input, "input must not be null");
        if (maxBytes < 0) {
            throw new IllegalArgumentException("maxBytes must not be negative: " + maxBytes);
        }
        if (maxBytes == 0) {
            return "";
        }
        try {
            byte[] data = input.readNBytes(maxBytes);
            if (data.length == 0) {
                return "";
            }
            return formatDump(data, 0, data.length);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(e);
        }
    }

    /**
     * Formats the contents of a file as hex dump, reading up to MAX_DEFAULT_BYTES
     * 将文件内容格式化为十六进制转储，最多读取MAX_DEFAULT_BYTES字节
     *
     * @param path the file path | 文件路径
     * @return hex dump string | 十六进制转储字符串
     * @throws NullPointerException      if path is null | 当path为null时抛出
     * @throws OpenIOOperationException if an IO error occurs or file not found | 当IO错误或文件未找到时抛出
     */
    public static String format(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        try (InputStream in = Files.newInputStream(path)) {
            byte[] data = in.readNBytes(MAX_DEFAULT_BYTES);
            if (data.length == 0) {
                return "";
            }
            return formatDump(data, 0, data.length);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Formats a range of a file as hex dump
     * 将文件的指定范围格式化为十六进制转储
     *
     * @param path   the file path | 文件路径
     * @param offset the byte offset to start reading from | 开始读取的字节偏移量
     * @param length the number of bytes to read | 要读取的字节数
     * @return hex dump string | 十六进制转储字符串
     * @throws NullPointerException      if path is null | 当path为null时抛出
     * @throws IllegalArgumentException if offset or length is negative | 当offset或length为负时抛出
     * @throws OpenIOOperationException if an IO error occurs | 当IO错误发生时抛出
     */
    public static String format(Path path, long offset, int length) {
        Objects.requireNonNull(path, "path must not be null");
        if (offset < 0) {
            throw new IllegalArgumentException("offset must not be negative: " + offset);
        }
        if (length < 0) {
            throw new IllegalArgumentException("length must not be negative: " + length);
        }
        if (length == 0) {
            return "";
        }
        try (InputStream in = Files.newInputStream(path)) {
            in.skipNBytes(offset);
            byte[] data = in.readNBytes(length);
            if (data.length == 0) {
                return "";
            }
            return formatDump(data, 0, data.length, offset);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    // ==================== Hex String Conversion | 十六进制字符串转换 ====================

    /**
     * Converts a byte array to a lowercase hex string (no formatting)
     * 将字节数组转换为小写十六进制字符串（无格式化）
     *
     * @param data the byte array | 字节数组
     * @return lowercase hex string | 小写十六进制字符串
     * @throws NullPointerException if data is null | 当data为null时抛出
     */
    public static String toHex(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");
        return toHex(data, 0, data.length);
    }

    /**
     * Converts a range of a byte array to a lowercase hex string
     * 将字节数组的指定范围转换为小写十六进制字符串
     *
     * @param data   the byte array | 字节数组
     * @param offset the start offset | 起始偏移量
     * @param length the number of bytes | 字节数
     * @return lowercase hex string | 小写十六进制字符串
     * @throws NullPointerException      if data is null | 当data为null时抛出
     * @throws IllegalArgumentException if range is invalid | 当范围无效时抛出
     */
    public static String toHex(byte[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        if (offset < 0 || length < 0 || offset + length > data.length) {
            throw new IllegalArgumentException(
                    String.format("Invalid range: offset=%d, length=%d, array length=%d",
                            offset, length, data.length));
        }
        return encodeHex(data, offset, length, HEX_LOWER);
    }

    /**
     * Converts a byte array to an uppercase hex string (no formatting)
     * 将字节数组转换为大写十六进制字符串（无格式化）
     *
     * @param data the byte array | 字节数组
     * @return uppercase hex string | 大写十六进制字符串
     * @throws NullPointerException if data is null | 当data为null时抛出
     */
    public static String toHexUpper(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");
        return encodeHex(data, 0, data.length, HEX_UPPER);
    }

    /**
     * Parses a hex string back to a byte array
     * 将十六进制字符串解析回字节数组
     *
     * <p>The hex string must have even length and contain only valid hex characters (0-9, a-f, A-F).</p>
     * <p>十六进制字符串必须为偶数长度，且仅包含有效的十六进制字符（0-9, a-f, A-F）。</p>
     *
     * @param hex the hex string | 十六进制字符串
     * @return decoded byte array | 解码后的字节数组
     * @throws NullPointerException      if hex is null | 当hex为null时抛出
     * @throws IllegalArgumentException if hex has odd length or contains invalid characters | 当hex长度为奇数或包含无效字符时抛出
     */
    public static byte[] fromHex(String hex) {
        Objects.requireNonNull(hex, "hex string must not be null");
        if (hex.isEmpty()) {
            return new byte[0];
        }
        if ((hex.length() & 1) != 0) {
            throw new IllegalArgumentException("Hex string must have even length, got: " + hex.length());
        }
        int len = hex.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = hexCharToDigit(hex.charAt(i));
            int low = hexCharToDigit(hex.charAt(i + 1));
            result[i / 2] = (byte) ((high << 4) | low);
        }
        return result;
    }

    // ==================== Internal Methods | 内部方法 ====================

    /**
     * Core hex dump formatting with base offset of 0.
     */
    private static String formatDump(byte[] data, int offset, int length) {
        return formatDump(data, offset, length, 0L);
    }

    /**
     * Core hex dump formatting.
     *
     * @param data       the byte data
     * @param offset     the offset into data
     * @param length     the number of bytes
     * @param baseOffset the display offset for the first line
     */
    private static String formatDump(byte[] data, int offset, int length, long baseOffset) {
        int lines = (length + BYTES_PER_LINE - 1) / BYTES_PER_LINE;
        // Each line: 8 (offset) + 2 (spaces) + 48 (hex: 16*3 - 1 + extra space) + 2 + 16 (ascii) + 1 (|) + 1 (|) + 1 (\n)
        // Estimate ~80 chars per line
        StringBuilder sb = new StringBuilder(lines * 80);

        for (int lineStart = 0; lineStart < length; lineStart += BYTES_PER_LINE) {
            int lineBytes = Math.min(BYTES_PER_LINE, length - lineStart);
            long displayOffset = baseOffset + lineStart;

            // Offset column: 8 hex digits (lookup table, no String.format)
            for (int shift = 28; shift >= 0; shift -= 4) {
                sb.append(HEX_UPPER[(int) (displayOffset >>> shift) & 0x0F]);
            }
            sb.append("  ");

            // Hex columns
            for (int i = 0; i < BYTES_PER_LINE; i++) {
                if (i == 8) {
                    sb.append(' ');
                }
                if (i < lineBytes) {
                    int b = data[offset + lineStart + i] & 0xFF;
                    sb.append(HEX_UPPER[b >>> 4]);
                    sb.append(HEX_UPPER[b & 0x0F]);
                } else {
                    sb.append("  ");
                }
                if (i < BYTES_PER_LINE - 1) {
                    sb.append(' ');
                }
            }

            sb.append("  |");

            // ASCII column
            for (int i = 0; i < lineBytes; i++) {
                int b = data[offset + lineStart + i] & 0xFF;
                sb.append((b >= 0x20 && b <= 0x7E) ? (char) b : '.');
            }

            sb.append('|');

            if (lineStart + BYTES_PER_LINE < length) {
                sb.append('\n');
            }
        }

        return sb.toString();
    }

    /**
     * Encodes bytes to hex string using the given alphabet.
     */
    private static String encodeHex(byte[] data, int offset, int length, char[] alphabet) {
        char[] chars = new char[length * 2];
        for (int i = 0; i < length; i++) {
            int b = data[offset + i] & 0xFF;
            chars[i * 2] = alphabet[b >>> 4];
            chars[i * 2 + 1] = alphabet[b & 0x0F];
        }
        return new String(chars);
    }

    /**
     * Converts a hex character to its digit value.
     */
    private static int hexCharToDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return 10 + (c - 'a');
        }
        if (c >= 'A' && c <= 'F') {
            return 10 + (c - 'A');
        }
        throw new IllegalArgumentException("Invalid hex character: '" + c + "'");
    }
}
