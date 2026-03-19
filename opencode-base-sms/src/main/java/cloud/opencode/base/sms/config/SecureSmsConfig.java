package cloud.opencode.base.sms.config;

import java.util.Arrays;

/**
 * Secure SMS Config
 * 安全短信配置
 *
 * <p>Wrapper for SMS config that protects sensitive data.</p>
 * <p>保护敏感数据的短信配置包装器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Stores credentials as char[] for secure clearing - 使用char[]存储凭据以便安全清除</li>
 *   <li>Implements AutoCloseable for credential cleanup - 实现AutoCloseable用于凭据清理</li>
 *   <li>Volatile cleared flag for thread visibility - volatile标志确保线程可见性</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (SecureSmsConfig config = new SecureSmsConfig(httpConfig)) {
 *     String apiUrl = config.getApiUrl();
 *     // use config...
 * } // credentials are cleared on close
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (synchronized access to credentials) - 线程安全: 是（同步访问凭据）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public final class SecureSmsConfig implements AutoCloseable {

    private final String name;
    private final String apiUrl;
    private final char[] appId;
    private final char[] appKey;
    private final String signName;
    private final Object lock = new Object();
    private volatile boolean cleared = false;

    /**
     * Create secure config from HTTP config
     * 从HTTP配置创建安全配置
     *
     * @param config the HTTP config | HTTP配置
     */
    public SecureSmsConfig(HttpSmsConfig config) {
        this.name = config.name();
        this.apiUrl = config.apiUrl();
        this.appId = config.appId() != null ? config.appId().toCharArray() : null;
        this.appKey = config.appKey() != null ? config.appKey().toCharArray() : null;
        this.signName = config.signName();
    }

    /**
     * Get name
     * 获取名称
     *
     * @return the name | 名称
     */
    public String getName() {
        return name;
    }

    /**
     * Get API URL
     * 获取API地址
     *
     * @return the API URL | API地址
     */
    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Get app ID
     * 获取应用ID
     *
     * @return the app ID | 应用ID
     * @throws IllegalStateException if already cleared | 如果已清除则抛出异常
     */
    public String getAppId() {
        synchronized (lock) {
            checkCleared();
            return appId != null ? new String(appId) : null;
        }
    }

    /**
     * Get app key
     * 获取应用密钥
     *
     * @return the app key | 应用密钥
     * @throws IllegalStateException if already cleared | 如果已清除则抛出异常
     */
    public String getAppKey() {
        synchronized (lock) {
            checkCleared();
            return appKey != null ? new String(appKey) : null;
        }
    }

    /**
     * Get sign name
     * 获取签名名称
     *
     * @return the sign name | 签名名称
     */
    public String getSignName() {
        return signName;
    }

    /**
     * Clear sensitive data
     * 清除敏感数据
     */
    public void clear() {
        synchronized (lock) {
            if (!cleared) {
                if (appId != null) {
                    Arrays.fill(appId, '\0');
                }
                if (appKey != null) {
                    Arrays.fill(appKey, '\0');
                }
                cleared = true;
            }
        }
    }

    /**
     * Check if cleared
     * 检查是否已清除
     */
    private void checkCleared() {
        if (cleared) {
            throw new IllegalStateException("Sensitive data has been cleared");
        }
    }

    @Override
    public void close() {
        clear();
    }

    @Override
    public String toString() {
        return String.format("SecureSmsConfig{name='%s', apiUrl='%s', signName='%s'}",
            name, apiUrl, signName);
    }
}
