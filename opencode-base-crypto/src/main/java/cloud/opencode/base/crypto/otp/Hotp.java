package cloud.opencode.base.crypto.otp;

import cloud.opencode.base.crypto.exception.OpenCryptoException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * RFC 4226 HOTP (HMAC-Based One-Time Password) implementation
 * RFC 4226 HOTP（基于 HMAC 的一次性密码）实现
 *
 * <p>Generates and verifies HMAC-based one-time passwords as defined in RFC 4226.
 * Supports configurable HMAC algorithms (SHA-1, SHA-256, SHA-512) and digit lengths (6-8).</p>
 * <p>生成和验证基于 HMAC 的一次性密码，符合 RFC 4226 规范。
 * 支持可配置的 HMAC 算法（SHA-1、SHA-256、SHA-512）和位数（6-8）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HMAC-based OTP generation per RFC 4226 - 符合 RFC 4226 的 HMAC 一次性密码生成</li>
 *   <li>Configurable HMAC algorithm (SHA-1, SHA-256, SHA-512) - 可配置 HMAC 算法</li>
 *   <li>Configurable digit length (6-8) - 可配置位数（6-8）</li>
 *   <li>Constant-time verification to prevent timing attacks - 常量时间验证防止时序攻击</li>
 *   <li>Look-ahead window for counter synchronization - 前瞻窗口用于计数器同步</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Hotp hotp = Hotp.sha1();
 * String code = hotp.generate(secret, 0);
 * boolean valid = hotp.verify(secret, 0, code, 5);
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
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc4226">RFC 4226</a>
 * @since JDK 25, opencode-base-crypto V1.0.3
 */
public final class Hotp {

    private static final int DEFAULT_DIGITS = 6;
    private static final int MIN_DIGITS = 6;
    private static final int MAX_DIGITS = 8;
    private static final int MAX_LOOK_AHEAD = 100;
    private static final int[] DIGITS_POWER = {1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000};

    private final String algorithm;

    private Hotp(String algorithm) {
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm must not be null");
        // Validate algorithm is available
        try {
            Mac.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new OpenCryptoException(algorithm, "initialization",
                    "HMAC algorithm not available: " + algorithm, e);
        }
    }

    /**
     * Creates an HOTP instance using HmacSHA1 (default, compatible with most authenticator apps).
     * 创建使用 HmacSHA1 的 HOTP 实例（默认，兼容大多数认证器应用）
     *
     * @return a new Hotp instance using HmacSHA1 | 使用 HmacSHA1 的 Hotp 实例
     */
    public static Hotp sha1() {
        return new Hotp("HmacSHA1");
    }

    /**
     * Creates an HOTP instance using HmacSHA256.
     * 创建使用 HmacSHA256 的 HOTP 实例
     *
     * @return a new Hotp instance using HmacSHA256 | 使用 HmacSHA256 的 Hotp 实例
     */
    public static Hotp sha256() {
        return new Hotp("HmacSHA256");
    }

    /**
     * Creates an HOTP instance using HmacSHA512.
     * 创建使用 HmacSHA512 的 HOTP 实例
     *
     * @return a new Hotp instance using HmacSHA512 | 使用 HmacSHA512 的 Hotp 实例
     */
    public static Hotp sha512() {
        return new Hotp("HmacSHA512");
    }

    /**
     * Creates an HOTP instance using a custom HMAC algorithm.
     * 创建使用自定义 HMAC 算法的 HOTP 实例
     *
     * @param algorithm the HMAC algorithm name (e.g., "HmacSHA1") | HMAC 算法名称
     * @return a new Hotp instance | 新的 Hotp 实例
     * @throws OpenCryptoException if the algorithm is not available | 当算法不可用时抛出
     */
    public static Hotp of(String algorithm) {
        return new Hotp(algorithm);
    }

    /**
     * Generates a 6-digit one-time password for the given secret and counter.
     * 为给定密钥和计数器生成 6 位一次性密码
     *
     * @param secret the shared secret key | 共享密钥
     * @param counter the counter value | 计数器值
     * @return the generated OTP code | 生成的 OTP 验证码
     * @throws OpenCryptoException if generation fails | 当生成失败时抛出
     */
    public String generate(byte[] secret, long counter) {
        return generate(secret, counter, DEFAULT_DIGITS);
    }

    /**
     * Generates a one-time password with the specified number of digits.
     * 生成指定位数的一次性密码
     *
     * @param secret the shared secret key | 共享密钥
     * @param counter the counter value | 计数器值
     * @param digits the number of digits (6-8) | 位数（6-8）
     * @return the generated OTP code | 生成的 OTP 验证码
     * @throws OpenCryptoException if generation fails | 当生成失败时抛出
     * @throws IllegalArgumentException if digits is not 6-8 or secret is null | 当位数不在 6-8 范围或密钥为空时抛出
     */
    public String generate(byte[] secret, long counter, int digits) {
        Objects.requireNonNull(secret, "secret must not be null");
        if (digits < MIN_DIGITS || digits > MAX_DIGITS) {
            throw new IllegalArgumentException(
                    "digits must be between " + MIN_DIGITS + " and " + MAX_DIGITS + ", got: " + digits);
        }

        byte[] hash = hmac(secret, counterToBytes(counter));
        int truncated = dynamicTruncate(hash);
        int otp = truncated % DIGITS_POWER[digits];

        return padLeft(otp, digits);
    }

    /**
     * Verifies a one-time password against the given secret and counter (6 digits, no look-ahead).
     * 验证一次性密码（6 位，无前瞻窗口）
     *
     * @param secret the shared secret key | 共享密钥
     * @param counter the expected counter value | 期望的计数器值
     * @param code the OTP code to verify | 待验证的 OTP 验证码
     * @return true if the code is valid | 验证码有效返回 true
     * @throws OpenCryptoException if verification fails | 当验证失败时抛出
     */
    public boolean verify(byte[] secret, long counter, String code) {
        return verify(secret, counter, code, 0);
    }

    /**
     * Verifies a one-time password with a look-ahead window for counter synchronization.
     * 使用前瞻窗口验证一次性密码，用于计数器同步
     *
     * @param secret the shared secret key | 共享密钥
     * @param counter the expected counter value | 期望的计数器值
     * @param code the OTP code to verify | 待验证的 OTP 验证码
     * @param lookAhead the number of counter values to check ahead (0-100) | 前瞻窗口大小（0-100）
     * @return true if the code matches any counter in [counter, counter+lookAhead] | 验证码匹配任一计数器值时返回 true
     * @throws OpenCryptoException if verification fails | 当验证失败时抛出
     * @throws IllegalArgumentException if lookAhead is negative or code is null | 当前瞻窗口为负数或验证码为空时抛出
     */
    public boolean verify(byte[] secret, long counter, String code, int lookAhead) {
        Objects.requireNonNull(secret, "secret must not be null");
        Objects.requireNonNull(code, "code must not be null");
        if (lookAhead < 0 || lookAhead > MAX_LOOK_AHEAD) {
            throw new IllegalArgumentException(
                    "lookAhead must be in range [0, " + MAX_LOOK_AHEAD + "], got: " + lookAhead);
        }

        int digits = code.length();
        if (digits < MIN_DIGITS || digits > MAX_DIGITS) {
            return false;
        }

        // Iterate all counter values without short-circuit to preserve constant-time behavior.
        // Do NOT add break/return inside this loop — it would leak which counter position matched.
        boolean matched = false;
        for (int i = 0; i <= lookAhead; i++) {
            String expected = generate(secret, counter + i, digits);
            if (constantTimeEquals(expected, code)) {
                matched = true;
            }
        }
        return matched;
    }

    /**
     * Returns the HMAC algorithm used by this instance.
     * 返回此实例使用的 HMAC 算法
     *
     * @return the algorithm name | 算法名称
     */
    public String algorithm() {
        return algorithm;
    }

    /**
     * Converts a counter value to an 8-byte big-endian byte array.
     * 将计数器值转换为 8 字节大端序字节数组
     */
    private static byte[] counterToBytes(long counter) {
        byte[] data = new byte[8];
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (counter & 0xFF);
            counter >>= 8;
        }
        return data;
    }

    /**
     * Computes HMAC of the data using the secret and configured algorithm.
     * 使用密钥和配置的算法计算数据的 HMAC
     */
    private byte[] hmac(byte[] secret, byte[] data) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret, algorithm));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            throw new OpenCryptoException(algorithm, "hmac",
                    "HMAC algorithm not available: " + algorithm, e);
        } catch (InvalidKeyException e) {
            throw new OpenCryptoException(algorithm, "hmac",
                    "Invalid key for HMAC computation", e);
        }
    }

    /**
     * Performs dynamic truncation as defined in RFC 4226 Section 5.3.
     * 执行 RFC 4226 第 5.3 节定义的动态截断
     */
    private static int dynamicTruncate(byte[] hash) {
        int offset = hash[hash.length - 1] & 0x0F;
        return ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);
    }

    /**
     * Zero-pads an OTP integer to the requested digit length.
     * 将 OTP 整数零填充到指定位数
     */
    private static String padLeft(int otp, int digits) {
        char[] buf = new char[digits];
        for (int i = digits - 1; i >= 0; i--) {
            buf[i] = (char) ('0' + otp % 10);
            otp /= 10;
        }
        return new String(buf);
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     * Uses char-level XOR to avoid byte[] allocation overhead.
     * 常量时间字符串比较，防止时序攻击。使用 char 级 XOR 避免 byte[] 分配开销。
     */
    static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
