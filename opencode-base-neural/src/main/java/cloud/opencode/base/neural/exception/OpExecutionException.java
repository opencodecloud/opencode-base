package cloud.opencode.base.neural.exception;

import java.io.Serial;

/**
 * Operator Execution Exception
 * 算子执行异常
 *
 * <p>Exception thrown when a neural network operator fails during forward computation.</p>
 * <p>当神经网络算子在前向计算中失败时抛出的异常。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public class OpExecutionException extends NeuralException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String opType;

    public OpExecutionException(String message, String opType) {
        super(message, NeuralErrorCode.OP_EXECUTION_FAILED);
        this.opType = opType;
    }

    public OpExecutionException(String message, String opType, Throwable cause) {
        super(message, cause, NeuralErrorCode.OP_EXECUTION_FAILED);
        this.opType = opType;
    }

    public OpExecutionException(String message, NeuralErrorCode errorCode) {
        super(message, errorCode);
        this.opType = null;
    }

    public String getOpType() {
        return opType;
    }
}
