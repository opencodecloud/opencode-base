package cloud.opencode.base.crypto.otp;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

/**
 * RFC 6238 TOTP (Time-Based One-Time Password) implementation
 * RFC 6238 TOTP（基于时间的一次性密码）实现
 *
 * <p>Generates and verifies time-based one-time passwords as defined in RFC 6238.
 * Built on top of HOTP (RFC 4226) with time-step-based counter derivation.</p>
 * <p>生成和验证基于时间的一次性密码，符合 RFC 6238 规范。
 * 基于 HOTP（RFC 4226），使用时间步长计算计数器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Time-based OTP generation per RFC 6238 - 符合 RFC 6238 的基于时间的 OTP 生成</li>
 *   <li>Configurable time period and digit length - 可配置时间步长和位数</li>
 *   <li>Time window verification for clock skew tolerance - 时间窗口验证以容忍时钟偏差</li>
 *   <li>otpauth:// URI generation for QR code provisioning - 生成 otpauth:// URI 用于二维码配置</li>
 *   <li>Builder pattern for flexible configuration - Builder 模式灵活配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default SHA-1, 30s period, 6 digits
 * Totp totp = Totp.sha1();
 * String code = totp.generate(secret);
 * boolean valid = totp.verify(secret, code);
 *
 * // Custom configuration
 * Totp totp = Totp.builder()
 *     .algorithm("HmacSHA256")
 *     .period(60)
 *     .digits(8)
 *     .build();
 *
 * // Generate otpauth URI for QR code
 * String uri = Totp.generateUri("MyApp", "user@example.com", secret);
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
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6238">RFC 6238</a>
 * @since JDK 25, opencode-base-crypto V1.0.3
 */
public final class Totp {

    private static final int DEFAULT_PERIOD = 30;
    private static final int DEFAULT_DIGITS = 6;
    private static final int DEFAULT_WINDOW = 1;

    private final Hotp hotp;
    private final int period;
    private final int digits;

    private Totp(Hotp hotp, int period, int digits) {
        this.hotp = hotp;
        this.period = period;
        this.digits = digits;
    }

    /**
     * Creates a TOTP instance with default settings (SHA-1, 30s period, 6 digits).
     * 创建默认配置的 TOTP 实例（SHA-1、30 秒步长、6 位）
     *
     * @return a new Totp instance | 新的 Totp 实例
     */
    public static Totp sha1() {
        return new Totp(Hotp.sha1(), DEFAULT_PERIOD, DEFAULT_DIGITS);
    }

    /**
     * Creates a TOTP instance using SHA-256 (30s period, 6 digits).
     * 创建使用 SHA-256 的 TOTP 实例（30 秒步长、6 位）
     *
     * @return a new Totp instance | 新的 Totp 实例
     */
    public static Totp sha256() {
        return new Totp(Hotp.sha256(), DEFAULT_PERIOD, DEFAULT_DIGITS);
    }

    /**
     * Creates a TOTP instance using SHA-512 (30s period, 6 digits).
     * 创建使用 SHA-512 的 TOTP 实例（30 秒步长、6 位）
     *
     * @return a new Totp instance | 新的 Totp 实例
     */
    public static Totp sha512() {
        return new Totp(Hotp.sha512(), DEFAULT_PERIOD, DEFAULT_DIGITS);
    }

    /**
     * Creates a new Builder for configuring a TOTP instance.
     * 创建新的 Builder 用于配置 TOTP 实例
     *
     * @return a new Builder | 新的 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Generates a TOTP code for the current time.
     * 生成当前时间的 TOTP 验证码
     *
     * @param secret the shared secret key | 共享密钥
     * @return the generated TOTP code | 生成的 TOTP 验证码
     */
    public String generate(byte[] secret) {
        return generate(secret, Instant.now());
    }

    /**
     * Generates a TOTP code for the specified time.
     * 生成指定时间的 TOTP 验证码
     *
     * @param secret the shared secret key | 共享密钥
     * @param time the time instant to generate for | 生成 TOTP 的时间点
     * @return the generated TOTP code | 生成的 TOTP 验证码
     */
    public String generate(byte[] secret, Instant time) {
        Objects.requireNonNull(time, "time must not be null");
        long counter = timeToCounter(time);
        return hotp.generate(secret, counter, digits);
    }

    /**
     * Verifies a TOTP code against the current time with default window size (1).
     * 使用默认窗口大小（1）验证当前时间的 TOTP 验证码
     *
     * @param secret the shared secret key | 共享密钥
     * @param code the TOTP code to verify | 待验证的 TOTP 验证码
     * @return true if the code is valid | 验证码有效返回 true
     */
    public boolean verify(byte[] secret, String code) {
        return verify(secret, code, DEFAULT_WINDOW);
    }

    /**
     * Verifies a TOTP code against the current time with the specified window size.
     * 使用指定窗口大小验证当前时间的 TOTP 验证码
     *
     * @param secret the shared secret key | 共享密钥
     * @param code the TOTP code to verify | 待验证的 TOTP 验证码
     * @param windowSize the number of time steps to check before and after (>=0) | 前后检查的时间步数（>=0）
     * @return true if the code is valid within the window | 在窗口内验证码有效返回 true
     */
    public boolean verify(byte[] secret, String code, int windowSize) {
        return verify(secret, code, Instant.now(), windowSize);
    }

    /**
     * Verifies a TOTP code against the specified time with the given window size.
     * 使用指定时间和窗口大小验证 TOTP 验证码
     *
     * @param secret the shared secret key | 共享密钥
     * @param code the TOTP code to verify | 待验证的 TOTP 验证码
     * @param time the time instant to verify against | 验证的时间点
     * @param windowSize the number of time steps to check before and after (>=0) | 前后检查的时间步数（>=0）
     * @return true if the code is valid within the window | 在窗口内验证码有效返回 true
     * @throws IllegalArgumentException if windowSize is negative | 当窗口大小为负数时抛出
     */
    public boolean verify(byte[] secret, String code, Instant time, int windowSize) {
        Objects.requireNonNull(secret, "secret must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(time, "time must not be null");
        if (windowSize < 0) {
            throw new IllegalArgumentException("windowSize must be non-negative, got: " + windowSize);
        }

        long counter = timeToCounter(time);
        // Iterate all window positions without short-circuit to preserve constant-time behavior.
        // Do NOT add break/return inside this loop — it would leak which time step matched.
        boolean matched = false;
        for (long i = -windowSize; i <= windowSize; i++) {
            long checkCounter = counter + i;
            if (checkCounter < 0) {
                continue;
            }
            String expected = hotp.generate(secret, checkCounter, digits);
            if (Hotp.constantTimeEquals(expected, code)) {
                matched = true;
            }
        }
        return matched;
    }

    /**
     * Returns the time period in seconds.
     * 返回时间步长（秒）
     *
     * @return the period in seconds | 时间步长（秒）
     */
    public int period() {
        return period;
    }

    /**
     * Returns the number of digits in generated codes.
     * 返回生成验证码的位数
     *
     * @return the number of digits | 位数
     */
    public int digits() {
        return digits;
    }

    /**
     * Returns the HMAC algorithm name.
     * 返回 HMAC 算法名称
     *
     * @return the algorithm name | 算法名称
     */
    public String algorithm() {
        return hotp.algorithm();
    }

    /**
     * Generates an otpauth:// URI with default settings (SHA-1, 6 digits, 30s period).
     * 生成使用默认设置的 otpauth:// URI（SHA-1、6 位、30 秒步长）
     *
     * @param issuer the issuer name (e.g., company name) | 发行方名称
     * @param account the account name (e.g., email) | 账户名称
     * @param secret the shared secret key | 共享密钥
     * @return the otpauth URI | otpauth URI
     */
    public static String generateUri(String issuer, String account, byte[] secret) {
        return generateUri(issuer, account, secret, "SHA1", DEFAULT_DIGITS, DEFAULT_PERIOD);
    }

    /**
     * Generates an otpauth:// URI with full configuration parameters.
     * 生成包含完整配置参数的 otpauth:// URI
     *
     * <p>The generated URI follows the Google Authenticator key URI format:
     * {@code otpauth://totp/Issuer:account?secret=BASE32&issuer=Issuer&algorithm=SHA1&digits=6&period=30}</p>
     *
     * @param issuer the issuer name (e.g., company name) | 发行方名称
     * @param account the account name (e.g., email) | 账户名称
     * @param secret the shared secret key | 共享密钥
     * @param algorithm the hash algorithm (SHA1, SHA256, SHA512) | 哈希算法
     * @param digits the number of digits (6-8) | 位数（6-8）
     * @param period the time period in seconds | 时间步长（秒）
     * @return the otpauth URI | otpauth URI
     */
    public static String generateUri(String issuer, String account, byte[] secret,
                                     String algorithm, int digits, int period) {
        Objects.requireNonNull(issuer, "issuer must not be null");
        Objects.requireNonNull(account, "account must not be null");
        Objects.requireNonNull(secret, "secret must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        String base32Secret = TotpSecret.toBase32(secret);
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8).replace("+", "%20");
        String encodedAccount = URLEncoder.encode(account, StandardCharsets.UTF_8).replace("+", "%20");

        return "otpauth://totp/" + encodedIssuer + ":" + encodedAccount
                + "?secret=" + base32Secret
                + "&issuer=" + encodedIssuer
                + "&algorithm=" + algorithm
                + "&digits=" + digits
                + "&period=" + period;
    }

    /**
     * Converts a time instant to a counter value based on the configured period.
     * 根据配置的时间步长将时间点转换为计数器值
     */
    private long timeToCounter(Instant time) {
        long epochSecond = time.getEpochSecond();
        if (epochSecond < 0) {
            throw new IllegalArgumentException("time must not be before the Unix epoch");
        }
        return epochSecond / period;
    }

    /**
     * Builder for constructing Totp instances with custom configuration.
     * 用于自定义配置构建 Totp 实例的 Builder
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-crypto V1.0.3
     */
    public static final class Builder {
        private String algorithm = "HmacSHA1";
        private int period = DEFAULT_PERIOD;
        private int digits = DEFAULT_DIGITS;

        private Builder() {
        }

        /**
         * Sets the HMAC algorithm.
         * 设置 HMAC 算法
         *
         * @param algorithm the HMAC algorithm name (e.g., "HmacSHA1", "HmacSHA256") | HMAC 算法名称
         * @return this Builder | 当前 Builder
         */
        public Builder algorithm(String algorithm) {
            this.algorithm = Objects.requireNonNull(algorithm, "algorithm must not be null");
            return this;
        }

        /**
         * Sets the time period in seconds.
         * 设置时间步长（秒）
         *
         * @param period the period in seconds (must be positive) | 时间步长（必须为正数）
         * @return this Builder | 当前 Builder
         * @throws IllegalArgumentException if period is not positive | 当步长不为正数时抛出
         */
        public Builder period(int period) {
            if (period <= 0) {
                throw new IllegalArgumentException("period must be positive, got: " + period);
            }
            this.period = period;
            return this;
        }

        /**
         * Sets the number of digits in the generated code.
         * 设置生成验证码的位数
         *
         * @param digits the number of digits (6-8) | 位数（6-8）
         * @return this Builder | 当前 Builder
         * @throws IllegalArgumentException if digits is not 6-8 | 当位数不在 6-8 范围时抛出
         */
        public Builder digits(int digits) {
            if (digits < 6 || digits > 8) {
                throw new IllegalArgumentException("digits must be between 6 and 8, got: " + digits);
            }
            this.digits = digits;
            return this;
        }

        /**
         * Builds the Totp instance with the configured settings.
         * 使用配置的设置构建 Totp 实例
         *
         * @return a new Totp instance | 新的 Totp 实例
         */
        public Totp build() {
            return new Totp(Hotp.of(algorithm), period, digits);
        }
    }
}
