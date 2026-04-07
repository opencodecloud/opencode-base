package cloud.opencode.base.neural.exception;

import java.io.Serial;

/**
 * Tensor Exception
 * 张量操作异常
 *
 * <p>Exception thrown when tensor operations fail (shape mismatch, invalid index, etc.).</p>
 * <p>当张量操作失败时抛出的异常（形状不匹配、无效索引等）。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public class TensorException extends NeuralException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TensorException(String message) {
        super(message, NeuralErrorCode.INVALID_PARAMETERS);
    }

    public TensorException(String message, NeuralErrorCode errorCode) {
        super(message, errorCode);
    }

    public TensorException(String message, Throwable cause) {
        super(message, cause, NeuralErrorCode.INVALID_PARAMETERS);
    }
}
