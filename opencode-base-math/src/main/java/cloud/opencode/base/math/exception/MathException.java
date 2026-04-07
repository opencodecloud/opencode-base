package cloud.opencode.base.math.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * MathException - Base exception for the math module
 * 数学模块基础异常
 *
 * <p>All exceptions thrown by the opencode-base-math module extend this class,
 * which in turn extends {@link OpenException} for unified exception handling.</p>
 * <p>opencode-base-math 模块抛出的所有异常均继承此类，
 * 而此类继承自 {@link OpenException} 以实现统一异常处理。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public class MathException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a math exception with a message.
     * 创建数学异常
     *
     * @param message the error message / 错误消息
     */
    public MathException(String message) {
        super("math", null, message);
    }

    /**
     * Creates a math exception with a message and cause.
     * 创建数学异常（带原因）
     *
     * @param message the error message / 错误消息
     * @param cause   the root cause / 原始异常
     */
    public MathException(String message, Throwable cause) {
        super("math", null, message, cause);
    }
}
