package cloud.opencode.base.neural.exception;

import java.io.Serial;

/**
 * Model Load Exception
 * 模型加载异常
 *
 * <p>Exception thrown when loading a .ocm model file fails.</p>
 * <p>当加载 .ocm 模型文件失败时抛出的异常。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public class ModelLoadException extends NeuralException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ModelLoadException(String message) {
        super(message, NeuralErrorCode.MODEL_LOAD_FAILED);
    }

    public ModelLoadException(String message, Throwable cause) {
        super(message, cause, NeuralErrorCode.MODEL_LOAD_FAILED);
    }

    public ModelLoadException(String message, NeuralErrorCode errorCode) {
        super(message, errorCode);
    }
}
