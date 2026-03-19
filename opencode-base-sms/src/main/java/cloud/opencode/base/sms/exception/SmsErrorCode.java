package cloud.opencode.base.sms.exception;

/**
 * SMS Error Code
 * 短信错误码
 *
 * <p>Error codes for SMS operations.</p>
 * <p>短信操作的错误码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized error codes (send, phone, template, provider, content) - 分类错误码</li>
 *   <li>Numeric code and message per error - 每个错误包含数字码和消息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SmsErrorCode code = SmsErrorCode.SEND_FAILED;
 * int numCode = code.getCode();      // 1001
 * String msg = code.getMessage();     // "Failed to send SMS"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is immutable) - 线程安全: 是（枚举不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public enum SmsErrorCode {

    // 1xxx - Send errors
    SEND_FAILED(1001, "Failed to send SMS"),
    SEND_TIMEOUT(1002, "SMS send timeout"),
    SEND_RATE_LIMITED(1003, "SMS send rate limited"),
    NETWORK_ERROR(1004, "Network error occurred"),
    CONNECTION_TIMEOUT(1005, "Connection timeout"),
    READ_TIMEOUT(1006, "Read timeout"),

    // 2xxx - Phone number errors
    INVALID_PHONE_NUMBER(2001, "Invalid phone number"),
    PHONE_NUMBER_BLOCKED(2002, "Phone number is blocked"),
    UNSUPPORTED_COUNTRY(2003, "Country code not supported"),

    // 3xxx - Template errors
    TEMPLATE_NOT_FOUND(3001, "SMS template not found"),
    TEMPLATE_INVALID(3002, "SMS template is invalid"),
    TEMPLATE_VARIABLE_MISSING(3003, "Template variable is missing"),

    // 4xxx - Provider errors
    PROVIDER_NOT_CONFIGURED(4001, "SMS provider not configured"),
    PROVIDER_ERROR(4002, "SMS provider returned error"),
    PROVIDER_UNAVAILABLE(4003, "SMS provider is unavailable"),

    // 5xxx - Content errors
    MESSAGE_TOO_LONG(5001, "SMS message is too long"),
    MESSAGE_EMPTY(5002, "SMS message is empty"),
    INVALID_ENCODING(5003, "Invalid message encoding");

    private final int code;
    private final String message;

    SmsErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
