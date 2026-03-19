package cloud.opencode.base.test.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random Data - Utilities for generating random data
 * 随机数据 - 生成随机数据的工具
 *
 * <p>Provides thread-safe random data generation utilities.</p>
 * <p>提供线程安全的随机数据生成工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>UUID generation (full, short, compact) - UUID生成（完整、短、紧凑）</li>
 *   <li>Random bytes in hex, base64, URL-safe base64 formats - 十六进制、base64、URL安全base64格式随机字节</li>
 *   <li>Hash generation (MD5, SHA-256, SHA-512) - 哈希生成</li>
 *   <li>Token and code generation (API keys, access tokens, verification codes) - 令牌和验证码生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String uuid = RandomData.uuid();
 * String hex = RandomData.hex(16);
 * String base64 = RandomData.base64(24);
 * byte[] bytes = RandomData.bytes(32);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ThreadLocalRandom) - 线程安全: 是（使用ThreadLocalRandom）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class RandomData {

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private RandomData() {
    }

    // ============ UUID Generation | UUID生成 ============

    /**
     * Generates random UUID.
     * 生成随机UUID。
     *
     * @return the UUID string | UUID字符串
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates short UUID (first 8 characters).
     * 生成短UUID（前8个字符）。
     *
     * @return the short UUID | 短UUID
     */
    public static String shortUuid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates compact UUID (no dashes).
     * 生成紧凑UUID（无连字符）。
     *
     * @return the compact UUID | 紧凑UUID
     */
    public static String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // ============ Bytes Generation | 字节生成 ============

    /**
     * Generates random bytes.
     * 生成随机字节。
     *
     * @param length the length | 长度
     * @return the bytes | 字节数组
     */
    public static byte[] bytes(int length) {
        byte[] bytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }

    /**
     * Generates random bytes as hex string.
     * 生成十六进制字符串形式的随机字节。
     *
     * @param byteLength number of bytes (output will be 2x length) | 字节数（输出长度为2倍）
     * @return the hex string | 十六进制字符串
     */
    public static String hex(int byteLength) {
        byte[] bytes = bytes(byteLength);
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Generates random bytes as base64 string.
     * 生成Base64字符串形式的随机字节。
     *
     * @param byteLength number of bytes | 字节数
     * @return the base64 string | Base64字符串
     */
    public static String base64(int byteLength) {
        return Base64.getEncoder().encodeToString(bytes(byteLength));
    }

    /**
     * Generates random bytes as URL-safe base64 string.
     * 生成URL安全的Base64字符串形式的随机字节。
     *
     * @param byteLength number of bytes | 字节数
     * @return the URL-safe base64 string | URL安全的Base64字符串
     */
    public static String base64Url(int byteLength) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes(byteLength));
    }

    // ============ Hash Generation | 哈希生成 ============

    /**
     * Generates random MD5 hash.
     * 生成随机MD5哈希。
     *
     * @return the MD5 hash | MD5哈希
     */
    public static String md5() {
        return hash("MD5", bytes(16));
    }

    /**
     * Generates random SHA-256 hash.
     * 生成随机SHA-256哈希。
     *
     * @return the SHA-256 hash | SHA-256哈希
     */
    public static String sha256() {
        return hash("SHA-256", bytes(32));
    }

    /**
     * Generates random SHA-512 hash.
     * 生成随机SHA-512哈希。
     *
     * @return the SHA-512 hash | SHA-512哈希
     */
    public static String sha512() {
        return hash("SHA-512", bytes(64));
    }

    private static String hash(String algorithm, byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(data);
            return hex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm not available: " + algorithm, e);
        }
    }

    private static String hex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    // ============ Token Generation | 令牌生成 ============

    /**
     * Generates random API key.
     * 生成随机API密钥。
     *
     * @return the API key | API密钥
     */
    public static String apiKey() {
        return "ak_" + base64Url(24);
    }

    /**
     * Generates random secret key.
     * 生成随机密钥。
     *
     * @return the secret key | 密钥
     */
    public static String secretKey() {
        return "sk_" + base64Url(32);
    }

    /**
     * Generates random access token.
     * 生成随机访问令牌。
     *
     * @return the access token | 访问令牌
     */
    public static String accessToken() {
        return base64Url(32);
    }

    /**
     * Generates random refresh token.
     * 生成随机刷新令牌。
     *
     * @return the refresh token | 刷新令牌
     */
    public static String refreshToken() {
        return base64Url(48);
    }

    // ============ Code Generation | 验证码生成 ============

    /**
     * Generates random numeric code.
     * 生成随机数字验证码。
     *
     * @param length the length | 长度
     * @return the code | 验证码
     */
    public static String numericCode(int length) {
        var random = ThreadLocalRandom.current();
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Generates random alphanumeric code.
     * 生成随机字母数字验证码。
     *
     * @param length the length | 长度
     * @return the code | 验证码
     */
    public static String alphanumericCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Removed ambiguous chars
        var random = ThreadLocalRandom.current();
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ============ Sequence Generation | 序列生成 ============

    /**
     * Generates random sequence ID.
     * 生成随机序列ID。
     *
     * @param prefix the prefix | 前缀
     * @return the sequence ID | 序列ID
     */
    public static String sequenceId(String prefix) {
        return prefix + System.currentTimeMillis() + numericCode(4);
    }

    /**
     * Generates random order number.
     * 生成随机订单号。
     *
     * @return the order number | 订单号
     */
    public static String orderNumber() {
        return sequenceId("ORD");
    }

    /**
     * Generates random transaction ID.
     * 生成随机交易ID。
     *
     * @return the transaction ID | 交易ID
     */
    public static String transactionId() {
        return sequenceId("TXN");
    }
}
