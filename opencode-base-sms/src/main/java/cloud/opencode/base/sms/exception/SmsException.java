package cloud.opencode.base.sms.exception;

/**
 * SMS Exception
 * 短信异常
 *
 * <p>Base exception for SMS operations.</p>
 * <p>短信操作的基础异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Error code classification - 错误码分类</li>
 *   <li>Extends RuntimeException - 继承RuntimeException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new SmsException(SmsErrorCode.SEND_FAILED, "Send failed for phone");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public class SmsException extends RuntimeException {

    private final SmsErrorCode errorCode;

    public SmsException(SmsErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public SmsException(SmsErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SmsException(SmsErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public SmsException(SmsErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public SmsErrorCode getErrorCode() {
        return errorCode;
    }

    public int getCode() {
        return errorCode.getCode();
    }
}
