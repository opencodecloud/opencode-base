package cloud.opencode.base.neural.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * Neural Exception
 * 神经网络异常基类
 *
 * <p>Base exception class for all neural network operations.
 * Extends {@link OpenException} to integrate with the OpenCode unified exception hierarchy.</p>
 * <p>所有神经网络操作异常的基类。
 * 继承 {@link OpenException} 以集成 OpenCode 统一异常体系。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see OpenException
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public class NeuralException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final NeuralErrorCode neuralErrorCode;

    public NeuralException(String message, NeuralErrorCode errorCode) {
        super("neural", String.valueOf(errorCode.getCode()), message, null);
        this.neuralErrorCode = errorCode;
    }

    public NeuralException(String message, Throwable cause, NeuralErrorCode errorCode) {
        super("neural", String.valueOf(errorCode.getCode()), message, cause);
        this.neuralErrorCode = errorCode;
    }

    public NeuralException(String message) {
        this(message, NeuralErrorCode.UNKNOWN);
    }

    public NeuralException(String message, Throwable cause) {
        this(message, cause, NeuralErrorCode.UNKNOWN);
    }

    /**
     * Get the neural-specific error code enum
     * 获取神经网络特定错误码枚举
     *
     * @return the neural error code | 神经网络错误码
     */
    public NeuralErrorCode getNeuralErrorCode() {
        return neuralErrorCode;
    }
}
