package cloud.opencode.base.oauth2.grant;

import java.time.Instant;
import java.util.Objects;

/**
 * Device Code Response Record
 * 设备码响应记录
 *
 * <p>Represents the response from a device authorization endpoint (RFC 8628).</p>
 * <p>表示设备授权端点的响应（RFC 8628）。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get device code response
 * DeviceCodeResponse response = client.requestDeviceCode();
 *
 * // Display to user
 * System.out.println("Please visit: " + response.verificationUri());
 * System.out.println("Enter code: " + response.userCode());
 *
 * // Poll for token
 * while (!response.isExpired()) {
 *     Thread.sleep(response.interval() * 1000);
 *     Optional<OAuth2Token> token = client.pollToken(response.deviceCode());
 *     if (token.isPresent()) {
 *         // Success!
 *         break;
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Represents OAuth2 device code grant response - 表示OAuth2设备码授权响应</li>
 *   <li>Contains device code, user code, and verification URI - 包含设备码、用户码和验证URI</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://tools.ietf.org/html/rfc8628">RFC 8628</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public record DeviceCodeResponse(
        String deviceCode,
        String userCode,
        String verificationUri,
        String verificationUriComplete,
        int expiresIn,
        int interval,
        Instant createdAt
) {

    /**
     * Default polling interval in seconds
     * 默认轮询间隔（秒）
     */
    public static final int DEFAULT_INTERVAL = 5;

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造器
     */
    public DeviceCodeResponse {
        Objects.requireNonNull(deviceCode, "deviceCode cannot be null");
        Objects.requireNonNull(userCode, "userCode cannot be null");
        Objects.requireNonNull(verificationUri, "verificationUri cannot be null");
        if (interval <= 0) {
            interval = DEFAULT_INTERVAL;
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Check if the device code has expired
     * 检查设备码是否已过期
     *
     * @return true if expired | 已过期返回 true
     */
    public boolean isExpired() {
        return Instant.now().isAfter(createdAt.plusSeconds(expiresIn));
    }

    /**
     * Get the expiration time
     * 获取过期时间
     *
     * @return the expiration instant | 过期时间
     */
    public Instant expiresAt() {
        return createdAt.plusSeconds(expiresIn);
    }

    /**
     * Get remaining time in seconds
     * 获取剩余时间（秒）
     *
     * @return remaining seconds, or 0 if expired | 剩余秒数，已过期则返回 0
     */
    public long remainingSeconds() {
        long remaining = expiresIn - (Instant.now().getEpochSecond() - createdAt.getEpochSecond());
        return Math.max(0, remaining);
    }

    /**
     * Check if verification URI complete is available
     * 检查完整验证 URI 是否可用
     *
     * @return true if available | 可用返回 true
     */
    public boolean hasVerificationUriComplete() {
        return verificationUriComplete != null && !verificationUriComplete.isBlank();
    }

    /**
     * Get the best verification URI (complete if available, otherwise basic)
     * 获取最佳验证 URI（如果可用则返回完整的，否则返回基本的）
     *
     * @return the verification URI | 验证 URI
     */
    public String getBestVerificationUri() {
        return hasVerificationUriComplete() ? verificationUriComplete : verificationUri;
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * DeviceCodeResponse Builder
     * DeviceCodeResponse 构建器
     */
    public static class Builder {
        private String deviceCode;
        private String userCode;
        private String verificationUri;
        private String verificationUriComplete;
        private int expiresIn;
        private int interval = DEFAULT_INTERVAL;
        private Instant createdAt = Instant.now();

        /**
         * Set the device code
         * 设置设备码
         *
         * @param deviceCode the device code | 设备码
         * @return this builder | 此构建器
         */
        public Builder deviceCode(String deviceCode) {
            this.deviceCode = deviceCode;
            return this;
        }

        /**
         * Set the user code
         * 设置用户码
         *
         * @param userCode the user code | 用户码
         * @return this builder | 此构建器
         */
        public Builder userCode(String userCode) {
            this.userCode = userCode;
            return this;
        }

        /**
         * Set the verification URI
         * 设置验证 URI
         *
         * @param verificationUri the verification URI | 验证 URI
         * @return this builder | 此构建器
         */
        public Builder verificationUri(String verificationUri) {
            this.verificationUri = verificationUri;
            return this;
        }

        /**
         * Set the complete verification URI (with user code embedded)
         * 设置完整验证 URI（包含用户码）
         *
         * @param verificationUriComplete the complete verification URI | 完整验证 URI
         * @return this builder | 此构建器
         */
        public Builder verificationUriComplete(String verificationUriComplete) {
            this.verificationUriComplete = verificationUriComplete;
            return this;
        }

        /**
         * Set the expiration time in seconds
         * 设置过期时间（秒）
         *
         * @param expiresIn the expiration time in seconds | 过期时间（秒）
         * @return this builder | 此构建器
         */
        public Builder expiresIn(int expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        /**
         * Set the polling interval in seconds
         * 设置轮询间隔（秒）
         *
         * @param interval the polling interval | 轮询间隔
         * @return this builder | 此构建器
         */
        public Builder interval(int interval) {
            this.interval = interval;
            return this;
        }

        /**
         * Set the creation time
         * 设置创建时间
         *
         * @param createdAt the creation time | 创建时间
         * @return this builder | 此构建器
         */
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * Build the DeviceCodeResponse
         * 构建 DeviceCodeResponse
         *
         * @return the response | 响应
         */
        public DeviceCodeResponse build() {
            return new DeviceCodeResponse(
                    deviceCode,
                    userCode,
                    verificationUri,
                    verificationUriComplete,
                    expiresIn,
                    interval,
                    createdAt
            );
        }
    }
}
