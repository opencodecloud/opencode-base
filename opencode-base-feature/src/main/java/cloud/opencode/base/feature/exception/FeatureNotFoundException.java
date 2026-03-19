package cloud.opencode.base.feature.exception;

/**
 * Feature Not Found Exception
 * 功能不存在异常
 *
 * <p>Exception thrown when a feature is not found.</p>
 * <p>当功能不存在时抛出的异常。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new FeatureNotFoundException("unknown-feature");
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Specific exception for missing feature lookups - 功能查找缺失的特定异常</li>
 *   <li>Carries the missing feature name for diagnostics - 携带缺失功能名称用于诊断</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class FeatureNotFoundException extends FeatureException {

    /**
     * Create exception for feature key
     * 为功能键创建异常
     *
     * @param featureKey the feature key | 功能键
     */
    public FeatureNotFoundException(String featureKey) {
        super("Feature not found: " + featureKey, null, featureKey, FeatureErrorCode.NOT_FOUND);
    }
}
