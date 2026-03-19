package cloud.opencode.base.io.checksum;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.CRC32;

/**
 * Checksum Utility Class
 * 校验和工具类
 *
 * <p>Utility class for calculating checksums and hash values.
 * Supports CRC32, MD5, SHA-1, SHA-256 and other algorithms.</p>
 * <p>用于计算校验和和哈希值的工具类。
 * 支持CRC32、MD5、SHA-1、SHA-256等算法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>CRC32 checksum - CRC32校验和</li>
 *   <li>MD5 hash - MD5哈希</li>
 *   <li>SHA-256 hash - SHA-256哈希</li>
 *   <li>Hash verification - 哈希验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // CRC32
 * long crc = OpenChecksum.crc32(path);
 *
 * // MD5
 * String md5 = OpenChecksum.md5(path);
 *
 * // SHA-256
 * String sha256 = OpenChecksum.sha256(path);
 *
 * // Verify
 * boolean valid = OpenChecksum.verify(path, expectedHash, "SHA-256");
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
public final class OpenChecksum {

    private static final int BUFFER_SIZE = 8192;
    private static final HexFormat HEX_FORMAT = HexFormat.of().withLowerCase();

    private OpenChecksum() {
    }

    // ==================== CRC32 ====================

    /**
     * Calculates CRC32 checksum for a file
     * 计算文件的CRC32校验和
     *
     * @param path the file path | 文件路径
     * @return CRC32 value | CRC32值
     */
    public static long crc32(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return crc32(is);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Calculates CRC32 checksum for an input stream
     * 计算输入流的CRC32校验和
     *
     * @param input the input stream | 输入流
     * @return CRC32 value | CRC32值
     */
    public static long crc32(InputStream input) {
        CRC32 crc = new CRC32();
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        try {
            while ((read = input.read(buffer)) != -1) {
                crc.update(buffer, 0, read);
            }
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(e);
        }
        return crc.getValue();
    }

    /**
     * Calculates CRC32 checksum for byte array
     * 计算字节数组的CRC32校验和
     *
     * @param data the data | 数据
     * @return CRC32 value | CRC32值
     */
    public static long crc32(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    // ==================== MD5 ====================

    /**
     * Calculates MD5 hash for a file
     * 计算文件的MD5哈希
     *
     * @param path the file path | 文件路径
     * @return MD5 hex string | MD5十六进制字符串
     */
    public static String md5(Path path) {
        return digest(path, "MD5");
    }

    /**
     * Calculates MD5 hash for an input stream
     * 计算输入流的MD5哈希
     *
     * @param input the input stream | 输入流
     * @return MD5 hex string | MD5十六进制字符串
     */
    public static String md5(InputStream input) {
        return digest(input, "MD5");
    }

    /**
     * Calculates MD5 hash for byte array
     * 计算字节数组的MD5哈希
     *
     * @param data the data | 数据
     * @return MD5 hex string | MD5十六进制字符串
     */
    public static String md5(byte[] data) {
        return digest(data, "MD5");
    }

    // ==================== SHA-1 ====================

    /**
     * Calculates SHA-1 hash for a file
     * 计算文件的SHA-1哈希
     *
     * @param path the file path | 文件路径
     * @return SHA-1 hex string | SHA-1十六进制字符串
     */
    public static String sha1(Path path) {
        return digest(path, "SHA-1");
    }

    /**
     * Calculates SHA-1 hash for an input stream
     * 计算输入流的SHA-1哈希
     *
     * @param input the input stream | 输入流
     * @return SHA-1 hex string | SHA-1十六进制字符串
     */
    public static String sha1(InputStream input) {
        return digest(input, "SHA-1");
    }

    /**
     * Calculates SHA-1 hash for byte array
     * 计算字节数组的SHA-1哈希
     *
     * @param data the data | 数据
     * @return SHA-1 hex string | SHA-1十六进制字符串
     */
    public static String sha1(byte[] data) {
        return digest(data, "SHA-1");
    }

    // ==================== SHA-256 ====================

    /**
     * Calculates SHA-256 hash for a file
     * 计算文件的SHA-256哈希
     *
     * @param path the file path | 文件路径
     * @return SHA-256 hex string | SHA-256十六进制字符串
     */
    public static String sha256(Path path) {
        return digest(path, "SHA-256");
    }

    /**
     * Calculates SHA-256 hash for an input stream
     * 计算输入流的SHA-256哈希
     *
     * @param input the input stream | 输入流
     * @return SHA-256 hex string | SHA-256十六进制字符串
     */
    public static String sha256(InputStream input) {
        return digest(input, "SHA-256");
    }

    /**
     * Calculates SHA-256 hash for byte array
     * 计算字节数组的SHA-256哈希
     *
     * @param data the data | 数据
     * @return SHA-256 hex string | SHA-256十六进制字符串
     */
    public static String sha256(byte[] data) {
        return digest(data, "SHA-256");
    }

    // ==================== SHA-512 ====================

    /**
     * Calculates SHA-512 hash for a file
     * 计算文件的SHA-512哈希
     *
     * @param path the file path | 文件路径
     * @return SHA-512 hex string | SHA-512十六进制字符串
     */
    public static String sha512(Path path) {
        return digest(path, "SHA-512");
    }

    /**
     * Calculates SHA-512 hash for an input stream
     * 计算输入流的SHA-512哈希
     *
     * @param input the input stream | 输入流
     * @return SHA-512 hex string | SHA-512十六进制字符串
     */
    public static String sha512(InputStream input) {
        return digest(input, "SHA-512");
    }

    /**
     * Calculates SHA-512 hash for byte array
     * 计算字节数组的SHA-512哈希
     *
     * @param data the data | 数据
     * @return SHA-512 hex string | SHA-512十六进制字符串
     */
    public static String sha512(byte[] data) {
        return digest(data, "SHA-512");
    }

    // ==================== Generic Digest ====================

    /**
     * Calculates digest for a file using specified algorithm
     * 使用指定算法计算文件的摘要
     *
     * @param path      the file path | 文件路径
     * @param algorithm the algorithm (MD5, SHA-1, SHA-256, etc.) | 算法
     * @return hex string | 十六进制字符串
     */
    public static String digest(Path path, String algorithm) {
        try (InputStream is = Files.newInputStream(path)) {
            return digest(is, algorithm);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Calculates digest for an input stream
     * 计算输入流的摘要
     *
     * @param input     the input stream | 输入流
     * @param algorithm the algorithm | 算法
     * @return hex string | 十六进制字符串
     */
    public static String digest(InputStream input, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
            return HEX_FORMAT.formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw OpenIOOperationException.checksumFailed(algorithm, e);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(e);
        }
    }

    /**
     * Calculates digest for byte array
     * 计算字节数组的摘要
     *
     * @param data      the data | 数据
     * @param algorithm the algorithm | 算法
     * @return hex string | 十六进制字符串
     */
    public static String digest(byte[] data, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return HEX_FORMAT.formatHex(md.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw OpenIOOperationException.checksumFailed(algorithm, e);
        }
    }

    /**
     * Calculates digest and returns Checksum object
     * 计算摘要并返回Checksum对象
     *
     * @param path      the file path | 文件路径
     * @param algorithm the algorithm | 算法
     * @return Checksum object | Checksum对象
     */
    public static Checksum calculate(Path path, String algorithm) {
        try (InputStream is = Files.newInputStream(path)) {
            return calculate(is, algorithm);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Calculates digest and returns Checksum object
     * 计算摘要并返回Checksum对象
     *
     * @param input     the input stream | 输入流
     * @param algorithm the algorithm | 算法
     * @return Checksum object | Checksum对象
     */
    public static Checksum calculate(InputStream input, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
            return new Checksum(algorithm, md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw OpenIOOperationException.checksumFailed(algorithm, e);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(e);
        }
    }

    // ==================== Verification ====================

    /**
     * Verifies file checksum
     * 验证文件校验和
     *
     * @param path         the file path | 文件路径
     * @param expectedHash the expected hash hex string | 期望的哈希十六进制字符串
     * @param algorithm    the algorithm (MD5, SHA-256, etc.) | 算法
     * @return true if match | 如果匹配返回true
     */
    public static boolean verify(Path path, String expectedHash, String algorithm) {
        String actualHash = digest(path, algorithm);
        return actualHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * Verifies input stream checksum
     * 验证输入流校验和
     *
     * @param input        the input stream | 输入流
     * @param expectedHash the expected hash hex string | 期望的哈希十六进制字符串
     * @param algorithm    the algorithm | 算法
     * @return true if match | 如果匹配返回true
     */
    public static boolean verify(InputStream input, String expectedHash, String algorithm) {
        String actualHash = digest(input, algorithm);
        return actualHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * Verifies byte array checksum
     * 验证字节数组校验和
     *
     * @param data         the data | 数据
     * @param expectedHash the expected hash hex string | 期望的哈希十六进制字符串
     * @param algorithm    the algorithm | 算法
     * @return true if match | 如果匹配返回true
     */
    public static boolean verify(byte[] data, String expectedHash, String algorithm) {
        String actualHash = digest(data, algorithm);
        return actualHash.equalsIgnoreCase(expectedHash);
    }
}
