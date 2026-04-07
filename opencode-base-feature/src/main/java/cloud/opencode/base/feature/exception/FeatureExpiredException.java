package cloud.opencode.base.feature.exception;

/**
 * Feature Expired Exception
 * 功能过期异常
 *
 * <p>Exception thrown when a feature has expired.</p>
 * <p>当功能已过期时抛出的异常。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new FeatureExpiredException("my-feature");
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Specific exception for expired features - 过期功能的特定异常</li>
 *   <li>Carries the expired feature key for diagnostics - 携带过期功能键用于诊断</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
public class FeatureExpiredException extends FeatureException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * Create exception for expired feature
     * 为过期功能创建异常
     *
     * @param featureKey the feature key | 功能键
     */
    public FeatureExpiredException(String featureKey) {
        super("Feature expired: " + featureKey, null, featureKey, FeatureErrorCode.EXPIRED);
    }
}
