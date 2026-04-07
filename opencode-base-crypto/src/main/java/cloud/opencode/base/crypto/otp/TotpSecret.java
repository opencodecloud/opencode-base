package cloud.opencode.base.crypto.otp;

import java.security.SecureRandom;
import java.util.Objects;

/**
 * OTP secret key management utilities for TOTP/HOTP
 * TOTP/HOTP 的 OTP 密钥管理工具
 *
 * <p>Provides secure random key generation and Base32 encoding/decoding
 * for use with TOTP and HOTP one-time password schemes.</p>
 * <p>提供安全随机密钥生成和 Base32 编解码，用于 TOTP 和 HOTP 一次性密码方案。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cryptographically secure random key generation - 密码学安全的随机密钥生成</li>
 *   <li>RFC 4648 Base32 encoding without padding - RFC 4648 Base32 编码（无填充）</li>
 *   <li>Case-insensitive Base32 decoding - 大小写不敏感的 Base32 解码</li>
 *   <li>Tolerant decoding (ignores spaces and hyphens) - 宽容解码（忽略空格和连字符）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * byte[] secret = TotpSecret.generate();
 * String base32 = TotpSecret.toBase32(secret);
 * byte[] decoded = TotpSecret.fromBase32(base32);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（校验输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc4648">RFC 4648 - Base32</a>
 * @since JDK 25, opencode-base-crypto V1.0.3
 */
public final class TotpSecret {

    private static final int DEFAULT_SECRET_LENGTH = 20;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final char[] BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private static final int[] BASE32_DECODE_TABLE = new int[128];

    static {
        java.util.Arrays.fill(BASE32_DECODE_TABLE, -1);
        for (int i = 0; i < BASE32_ALPHABET.length; i++) {
            BASE32_DECODE_TABLE[BASE32_ALPHABET[i]] = i;
            BASE32_DECODE_TABLE[Character.toLowerCase(BASE32_ALPHABET[i])] = i;
        }
    }

    private TotpSecret() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Generates a 20-byte (160-bit) cryptographically secure random secret key.
     * 生成 20 字节（160 位）密码学安全的随机密钥
     *
     * @return a 20-byte random secret | 20 字节随机密钥
     */
    public static byte[] generate() {
        return generate(DEFAULT_SECRET_LENGTH);
    }

    /**
     * Generates a cryptographically secure random secret key of the specified length.
     * 生成指定长度的密码学安全的随机密钥
     *
     * @param length the key length in bytes (must be positive) | 密钥长度（字节，必须为正数）
     * @return a random secret of the specified length | 指定长度的随机密钥
     * @throws IllegalArgumentException if length is not positive | 当长度不为正数时抛出
     */
    public static byte[] generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive, got: " + length);
        }
        byte[] secret = new byte[length];
        SECURE_RANDOM.nextBytes(secret);
        return secret;
    }

    /**
     * Encodes a byte array to Base32 string (RFC 4648, no padding).
     * 将字节数组编码为 Base32 字符串（RFC 4648，无填充）
     *
     * @param data the data to encode | 待编码数据
     * @return the Base32 encoded string | Base32 编码字符串
     * @throws NullPointerException if data is null | 当数据为空时抛出
     */
    public static String toBase32(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");
        if (data.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;

        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                sb.append(BASE32_ALPHABET[(buffer >> bitsLeft) & 0x1F]);
            }
        }

        if (bitsLeft > 0) {
            sb.append(BASE32_ALPHABET[(buffer << (5 - bitsLeft)) & 0x1F]);
        }

        return sb.toString();
    }

    /**
     * Decodes a Base32 string to a byte array. Case-insensitive, ignores spaces and hyphens.
     * 将 Base32 字符串解码为字节数组。大小写不敏感，忽略空格和连字符。
     *
     * @param base32 the Base32 string to decode | 待解码的 Base32 字符串
     * @return the decoded byte array | 解码后的字节数组
     * @throws NullPointerException if base32 is null | 当字符串为空时抛出
     * @throws IllegalArgumentException if the string contains invalid Base32 characters | 当字符串包含无效 Base32 字符时抛出
     */
    public static byte[] fromBase32(String base32) {
        Objects.requireNonNull(base32, "base32 must not be null");

        // Strip spaces, hyphens, and padding
        String cleaned = base32.replaceAll("[\\s\\-=]", "");
        if (cleaned.isEmpty()) {
            return new byte[0];
        }

        byte[] output = new byte[cleaned.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int outputIndex = 0;

        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c >= 128 || BASE32_DECODE_TABLE[c] == -1) {
                throw new IllegalArgumentException(
                        "Invalid Base32 character: '" + c + "' at position " + i);
            }
            buffer = (buffer << 5) | BASE32_DECODE_TABLE[c];
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                if (outputIndex < output.length) {
                    output[outputIndex++] = (byte) ((buffer >> bitsLeft) & 0xFF);
                }
            }
        }

        if (outputIndex < output.length) {
            byte[] trimmed = new byte[outputIndex];
            System.arraycopy(output, 0, trimmed, 0, outputIndex);
            return trimmed;
        }
        return output;
    }
}
