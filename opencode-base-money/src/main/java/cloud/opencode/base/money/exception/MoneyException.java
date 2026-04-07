package cloud.opencode.base.money.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * Money Exception - Base exception for all money-related errors
 * 金额异常基类 - 所有金额相关错误的基础异常类
 *
 * <p>Extends {@link OpenException} to integrate with the OpenCode unified exception hierarchy.
 * Carries a {@link MoneyErrorCode} for fine-grained error classification.</p>
 * <p>继承 {@link OpenException} 以融入 OpenCode 统一异常体系。
 * 携带 {@link MoneyErrorCode} 用于细粒度错误分类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified exception base for money module - 金额模块统一异常基类</li>
 *   <li>Carries MoneyErrorCode for error classification - 携带MoneyErrorCode用于错误分类</li>
 *   <li>Integrates with OpenException component/errorCode model - 集成OpenException组件/错误码模型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new MoneyException("Operation failed", MoneyErrorCode.UNKNOWN);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public class MoneyException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Component name for OpenException
     * OpenException 的组件名称
     */
    private static final String COMPONENT = "money";

    private final MoneyErrorCode moneyErrorCode;

    /**
     * Create money exception with message
     * 创建带消息的金额异常
     *
     * @param message the error message | 错误消息
     */
    public MoneyException(String message) {
        super(COMPONENT, String.valueOf(MoneyErrorCode.UNKNOWN.getCode()), message);
        this.moneyErrorCode = MoneyErrorCode.UNKNOWN;
    }

    /**
     * Create money exception with message and error code
     * 创建带消息和错误码的金额异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public MoneyException(String message, MoneyErrorCode errorCode) {
        super(COMPONENT, String.valueOf((errorCode != null ? errorCode : MoneyErrorCode.UNKNOWN).getCode()), message);
        this.moneyErrorCode = errorCode != null ? errorCode : MoneyErrorCode.UNKNOWN;
    }

    /**
     * Create money exception with message, cause, and error code
     * 创建带消息、原因和错误码的金额异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     * @param errorCode the error code | 错误码
     */
    public MoneyException(String message, Throwable cause, MoneyErrorCode errorCode) {
        super(COMPONENT, String.valueOf((errorCode != null ? errorCode : MoneyErrorCode.UNKNOWN).getCode()), message, cause);
        this.moneyErrorCode = errorCode != null ? errorCode : MoneyErrorCode.UNKNOWN;
    }

    /**
     * Create money exception with message and cause
     * 创建带消息和原因的金额异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public MoneyException(String message, Throwable cause) {
        super(COMPONENT, String.valueOf(MoneyErrorCode.UNKNOWN.getCode()), message, cause);
        this.moneyErrorCode = MoneyErrorCode.UNKNOWN;
    }

    /**
     * Get money error code
     * 获取金额错误码
     *
     * @return the money error code | 金额错误码
     */
    public MoneyErrorCode getMoneyErrorCode() {
        return moneyErrorCode;
    }
}
