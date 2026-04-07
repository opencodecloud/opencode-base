package cloud.opencode.base.lunar.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;
import java.util.Objects;

/**
 * Lunar Exception
 * 农历异常基类
 *
 * <p>Base exception for all lunar calendar operations.
 * Extends {@link OpenException} with component name "Lunar".</p>
 * <p>所有农历操作的异常基类。继承 {@link OpenException}，组件名为 "Lunar"。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Error code association (LunarErrorCode) - 错误码关联</li>
 *   <li>OpenException hierarchy integration - OpenException 体系集成</li>
 *   <li>Cause chaining support - 原因链支持</li>
 *   <li>Base class for all lunar exceptions - 所有农历异常的基类</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new LunarException("Operation failed");
 * throw new LunarException("Conversion error", LunarErrorCode.CONVERSION_FAILED);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (message must not be null) - 空值安全: 否（消息不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public class LunarException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Component name for OpenException
     * OpenException 的组件名称
     */
    private static final String COMPONENT = "Lunar";

    private final LunarErrorCode lunarErrorCode;

    /**
     * Create lunar exception
     * 创建农历异常
     *
     * @param message the error message | 错误消息
     */
    public LunarException(String message) {
        super(COMPONENT, LunarErrorCode.UNKNOWN.toStringCode(), message);
        this.lunarErrorCode = LunarErrorCode.UNKNOWN;
    }

    /**
     * Create lunar exception with error code
     * 创建带错误码的农历异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public LunarException(String message, LunarErrorCode errorCode) {
        super(COMPONENT, Objects.requireNonNull(errorCode, "errorCode must not be null").toStringCode(), message);
        this.lunarErrorCode = errorCode;
    }

    /**
     * Create lunar exception with cause
     * 创建带原因的农历异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public LunarException(String message, Throwable cause) {
        super(COMPONENT, LunarErrorCode.UNKNOWN.toStringCode(), message, cause);
        this.lunarErrorCode = LunarErrorCode.UNKNOWN;
    }

    /**
     * Create lunar exception with cause and error code
     * 创建带原因和错误码的农历异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     * @param errorCode the error code | 错误码
     */
    public LunarException(String message, Throwable cause, LunarErrorCode errorCode) {
        super(COMPONENT, Objects.requireNonNull(errorCode, "errorCode must not be null").toStringCode(), message, cause);
        this.lunarErrorCode = errorCode;
    }

    /**
     * Get lunar error code
     * 获取农历错误码
     *
     * @return the lunar error code | 农历错误码
     */
    public LunarErrorCode getLunarErrorCode() {
        return lunarErrorCode;
    }
}
