package cloud.opencode.base.core;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Optional;

/**
 * Charset Utility Class - Charset conversion, detection and common charset constants
 * 字符集工具类 - 字符集转换、检测和常用字符集常量
 *
 * <p>Provides charset conversion, detection, BOM handling and Reader/Writer creation.</p>
 * <p>提供字符集转换、检测、BOM 处理和 Reader/Writer 创建功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Charset constants (UTF_8, GBK, ISO_8859_1, etc.) - 字符集常量</li>
 *   <li>Charset retrieval (charset, charsetOptional) - 字符集获取</li>
 *   <li>Conversion (toBytes, toString, convert) - 转换</li>
 *   <li>Detection (isSupported, canEncode, detect) - 检测</li>
 *   <li>BOM handling (hasBom, removeBom, addBom) - BOM 处理</li>
 *   <li>Reader/Writer creation (newReader, newWriter) - Reader/Writer 创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Charset constants - 字符集常量
 * Charset utf8 = OpenCharset.UTF_8;
 * Charset gbk = OpenCharset.GBK();
 *
 * // Conversion - 转换
 * byte[] bytes = OpenCharset.toBytes("Hello", OpenCharset.UTF_8);
 * String str = OpenCharset.gbkToUtf8(gbkString);
 *
 * // Detection - 检测
 * boolean valid = OpenCharset.isSupported("GBK");
 * Charset detected = OpenCharset.detect(bytes);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (lazy initialization with double-checked locking) - 线程安全: 是 (双重检查锁懒加载)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenCharset {

    private OpenCharset() {
    }

    // ==================== 常用字符集常量 ====================

    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    public static final Charset UTF_16 = StandardCharsets.UTF_16;
    public static final Charset UTF_16BE = StandardCharsets.UTF_16BE;
    public static final Charset UTF_16LE = StandardCharsets.UTF_16LE;
    public static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;
    public static final Charset US_ASCII = StandardCharsets.US_ASCII;

    // 中文字符集（懒加载）
    private static volatile Charset GBK_CHARSET;
    private static volatile Charset GB2312_CHARSET;
    private static volatile Charset GB18030_CHARSET;

    public static Charset GBK() {
        if (GBK_CHARSET == null) {
            synchronized (OpenCharset.class) {
                if (GBK_CHARSET == null) {
                    GBK_CHARSET = Charset.forName("GBK");
                }
            }
        }
        return GBK_CHARSET;
    }

    public static Charset GB2312() {
        if (GB2312_CHARSET == null) {
            synchronized (OpenCharset.class) {
                if (GB2312_CHARSET == null) {
                    GB2312_CHARSET = Charset.forName("GB2312");
                }
            }
        }
        return GB2312_CHARSET;
    }

    public static Charset GB18030() {
        if (GB18030_CHARSET == null) {
            synchronized (OpenCharset.class) {
                if (GB18030_CHARSET == null) {
                    GB18030_CHARSET = Charset.forName("GB18030");
                }
            }
        }
        return GB18030_CHARSET;
    }

    // ==================== 字符集名称常量 ====================

    public static final String UTF_8_NAME = "UTF-8";
    public static final String GBK_NAME = "GBK";
    public static final String ISO_8859_1_NAME = "ISO-8859-1";

    // UTF-8 BOM
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    // ==================== 字符集获取 ====================

    /**
     * Gets
     * 获取字符集，null 安全（返回 UTF-8）
     */
    public static Charset charset(String charsetName) {
        return charset(charsetName, UTF_8);
    }

    /**
     * Gets
     * 获取字符集，带默认值
     */
    public static Charset charset(String charsetName, Charset defaultCharset) {
        if (charsetName == null || charsetName.isEmpty()) {
            return defaultCharset;
        }
        try {
            return Charset.forName(charsetName);
        } catch (UnsupportedCharsetException e) {
            return defaultCharset;
        }
    }

    /**
     * Safely gets the charset
     * 安全获取字符集
     */
    public static Optional<Charset> charsetOptional(String charsetName) {
        if (charsetName == null || charsetName.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Charset.forName(charsetName));
        } catch (UnsupportedCharsetException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets
     * 获取系统默认字符集
     */
    public static Charset defaultCharset() {
        return Charset.defaultCharset();
    }

    // ==================== 字符集转换 ====================

    /**
     * Converts a string to a byte array (UTF-8)
     * 字符串转字节数组（UTF-8）
     */
    public static byte[] toBytes(String str) {
        return toBytes(str, UTF_8);
    }

    /**
     * Converts a string to a byte array (specified charset)
     * 字符串转字节数组（指定字符集）
     */
    public static byte[] toBytes(String str, Charset charset) {
        if (str == null) {
            return null;
        }
        return str.getBytes(charset != null ? charset : UTF_8);
    }

    /**
     * Converts a string to a byte array (specified charset name)
     * 字符串转字节数组（指定字符集名称）
     */
    public static byte[] toBytes(String str, String charsetName) {
        return toBytes(str, charset(charsetName));
    }

    /**
     * Converts a byte array to a string (UTF-8)
     * 字节数组转字符串（UTF-8）
     */
    public static String toString(byte[] bytes) {
        return toString(bytes, UTF_8);
    }

    /**
     * Converts a byte array to a string (specified charset)
     * 字节数组转字符串（指定字符集）
     */
    public static String toString(byte[] bytes, Charset charset) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, charset != null ? charset : UTF_8);
    }

    /**
     * Converts a byte array to a string (specified charset name)
     * 字节数组转字符串（指定字符集名称）
     */
    public static String toString(byte[] bytes, String charsetName) {
        return toString(bytes, charset(charsetName));
    }

    /**
     * Converts
     * 转换字符串编码
     */
    public static String convert(String str, Charset sourceCharset, Charset targetCharset) {
        if (str == null) {
            return null;
        }
        byte[] bytes = str.getBytes(sourceCharset);
        return new String(bytes, targetCharset);
    }

    /**
     * Converts
     * 转换字符串编码（字符集名称）
     */
    public static String convert(String str, String sourceCharset, String targetCharset) {
        return convert(str, charset(sourceCharset), charset(targetCharset));
    }

    // ==================== 字符集检测 ====================

    /**
     * Checks
     * 检查字符集名称是否有效
     */
    public static boolean isSupported(String charsetName) {
        if (charsetName == null || charsetName.isEmpty()) {
            return false;
        }
        try {
            Charset.forName(charsetName);
            return true;
        } catch (UnsupportedCharsetException e) {
            return false;
        }
    }

    /**
     * Checks
     * 检查字符串是否可用指定字符集编码
     */
    public static boolean canEncode(String str, Charset charset) {
        if (str == null || charset == null) {
            return false;
        }
        return charset.newEncoder().canEncode(str);
    }

    /**
     * Detects the likely charset of a byte array (simple detection)
     * 检测字节数组可能的字符集（简单检测）
     */
    public static Charset detect(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return UTF_8;
        }
        // 检查 BOM
        if (hasBom(bytes)) {
            return UTF_8;
        }
        // 检查是否为有效 UTF-8
        if (isValidUtf8(bytes)) {
            return UTF_8;
        }
        // 默认返回 GBK（中文环境常用）
        return GBK();
    }

    private static boolean isValidUtf8(byte[] bytes) {
        int i = 0;
        while (i < bytes.length) {
            int b = bytes[i] & 0xFF;
            if (b < 0x80) {
                i++;
            } else if ((b & 0xE0) == 0xC0) {
                if (i + 1 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80) {
                    return false;
                }
                i += 2;
            } else if ((b & 0xF0) == 0xE0) {
                if (i + 2 >= bytes.length ||
                        (bytes[i + 1] & 0xC0) != 0x80 ||
                        (bytes[i + 2] & 0xC0) != 0x80) {
                    return false;
                }
                i += 3;
            } else if ((b & 0xF8) == 0xF0) {
                if (i + 3 >= bytes.length ||
                        (bytes[i + 1] & 0xC0) != 0x80 ||
                        (bytes[i + 2] & 0xC0) != 0x80 ||
                        (bytes[i + 3] & 0xC0) != 0x80) {
                    return false;
                }
                i += 4;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks
     * 检查是否包含非 ASCII 字符
     */
    public static boolean hasNonAscii(String str) {
        if (str == null) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 127) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks
     * 检查是否为纯 ASCII 字符串
     */
    public static boolean isAscii(String str) {
        if (str == null) {
            return false;
        }
        return !hasNonAscii(str);
    }

    // ==================== 常用转换 ====================

    /**
     * GBK 转 UTF-8
     */
    public static String gbkToUtf8(String gbkStr) {
        return convert(gbkStr, GBK(), UTF_8);
    }

    /**
     * UTF-8 转 GBK
     */
    public static String utf8ToGbk(String utf8Str) {
        return convert(utf8Str, UTF_8, GBK());
    }

    /**
     * ISO-8859-1 转 UTF-8
     */
    public static String iso8859ToUtf8(String isoStr) {
        return convert(isoStr, ISO_8859_1, UTF_8);
    }

    // ==================== BOM 处理 ====================

    /**
     * Removes the UTF-8 BOM header
     * 移除 UTF-8 BOM 头
     */
    public static byte[] removeBom(byte[] bytes) {
        if (bytes == null || bytes.length < 3) {
            return bytes;
        }
        if (hasBom(bytes)) {
            byte[] result = new byte[bytes.length - 3];
            System.arraycopy(bytes, 3, result, 0, result.length);
            return result;
        }
        return bytes;
    }

    /**
     * Checks
     * 检查是否有 UTF-8 BOM
     */
    public static boolean hasBom(byte[] bytes) {
        return bytes != null && bytes.length >= 3 &&
                bytes[0] == UTF8_BOM[0] &&
                bytes[1] == UTF8_BOM[1] &&
                bytes[2] == UTF8_BOM[2];
    }

    /**
     * Adds
     * 添加 UTF-8 BOM
     */
    public static byte[] addBom(byte[] bytes) {
        if (bytes == null) {
            return UTF8_BOM.clone();
        }
        if (hasBom(bytes)) {
            return bytes;
        }
        byte[] result = new byte[bytes.length + 3];
        System.arraycopy(UTF8_BOM, 0, result, 0, 3);
        System.arraycopy(bytes, 0, result, 3, bytes.length);
        return result;
    }

    // ==================== Reader/Writer 创建 ====================

    /**
     * Creates
     * 创建 UTF-8 Reader
     */
    public static Reader newReader(InputStream in) {
        return newReader(in, UTF_8);
    }

    /**
     * Creates
     * 创建指定字符集的 Reader
     */
    public static Reader newReader(InputStream in, Charset charset) {
        return new InputStreamReader(in, charset != null ? charset : UTF_8);
    }

    /**
     * Creates
     * 创建 UTF-8 Writer
     */
    public static Writer newWriter(OutputStream out) {
        return newWriter(out, UTF_8);
    }

    /**
     * Creates
     * 创建指定字符集的 Writer
     */
    public static Writer newWriter(OutputStream out, Charset charset) {
        return new OutputStreamWriter(out, charset != null ? charset : UTF_8);
    }
}
