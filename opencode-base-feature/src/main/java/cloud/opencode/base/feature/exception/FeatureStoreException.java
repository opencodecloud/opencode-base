package cloud.opencode.base.feature.exception;

/**
 * Feature Store Exception
 * 功能存储异常
 *
 * <p>Exception thrown for feature store errors.</p>
 * <p>功能存储错误时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Persistence failed - 持久化失败</li>
 *   <li>Load failed - 加载失败</li>
 *   <li>Connection error - 连接错误</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Storage error reporting for feature persistence - 功能持久化的存储错误报告</li>
 *   <li>Extends FeatureException with error codes - 使用错误代码扩展FeatureException</li>
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
public class FeatureStoreException extends FeatureException {

    /**
     * Create store exception with message
     * 使用消息创建存储异常
     *
     * @param message the error message | 错误消息
     */
    public FeatureStoreException(String message) {
        super(message, FeatureErrorCode.STORE_ERROR);
    }

    /**
     * Create store exception with message and cause
     * 使用消息和原因创建存储异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public FeatureStoreException(String message, Throwable cause) {
        super(message, cause, null, FeatureErrorCode.STORE_ERROR);
    }

    /**
     * Create store exception with all parameters
     * 使用所有参数创建存储异常
     *
     * @param message    the error message | 错误消息
     * @param cause      the cause | 原因
     * @param featureKey the feature key | 功能键
     * @param errorCode  the error code | 错误码
     */
    public FeatureStoreException(String message, Throwable cause, String featureKey, FeatureErrorCode errorCode) {
        super(message, cause, featureKey, errorCode);
    }
}
