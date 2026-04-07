package cloud.opencode.base.neural.exception;

import java.io.Serial;

/**
 * Model Format Exception
 * 模型格式异常
 *
 * <p>Exception thrown when a .ocm model file has invalid format or structure.</p>
 * <p>当 .ocm 模型文件格式或结构无效时抛出的异常。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public class ModelFormatException extends NeuralException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ModelFormatException(String message) {
        super(message, NeuralErrorCode.MODEL_FORMAT_ERROR);
    }

    public ModelFormatException(String message, Throwable cause) {
        super(message, cause, NeuralErrorCode.MODEL_FORMAT_ERROR);
    }
}
