package cloud.opencode.base.observability.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * ObservabilityException - Exception for observability component errors
 * ObservabilityException - 可观测性组件异常
 *
 * <p>Thrown when observability operations fail, such as invalid metric configuration,
 * registry overflow, or timer/histogram recording errors.</p>
 * <p>当可观测性操作失败时抛出，如无效的指标配置、注册表溢出或计时器/直方图记录错误。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public class ObservabilityException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with message.
     * 创建带消息的异常。
     *
     * @param message the error message | 错误消息
     */
    public ObservabilityException(String message) {
        super("Observability", null, message);
    }

    /**
     * Creates an exception with message and cause.
     * 创建带消息和原因的异常。
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原始异常
     */
    public ObservabilityException(String message, Throwable cause) {
        super("Observability", null, message, cause);
    }

    /**
     * Creates an exception with error code and message.
     * 创建带错误码和消息的异常。
     *
     * @param errorCode the error code | 错误码
     * @param message   the error message | 错误消息
     */
    public ObservabilityException(String errorCode, String message) {
        super("Observability", errorCode, message);
    }

    /**
     * Creates an exception with error code, message and cause.
     * 创建带错误码、消息和原因的异常。
     *
     * @param errorCode the error code | 错误码
     * @param message   the error message | 错误消息
     * @param cause     the cause | 原始异常
     */
    public ObservabilityException(String errorCode, String message, Throwable cause) {
        super("Observability", errorCode, message, cause);
    }
}
