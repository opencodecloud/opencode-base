package cloud.opencode.base.crypto.policy;

import cloud.opencode.base.crypto.exception.OpenCryptoException;

/**
 * Exception thrown when a cryptographic operation violates the configured policy.
 * 当加密操作违反配置的策略时抛出的异常。
 *
 * <p>Contains the algorithm that violated the policy and a descriptive message
 * explaining why the operation was denied.</p>
 * <p>包含违反策略的算法以及解释操作被拒绝原因的描述性消息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries violated algorithm context - 携带违规算法上下文</li>
 *   <li>Integrates with CryptoPolicy enforcement - 与 CryptoPolicy 执行集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new PolicyViolationException("MD5", "check", "Algorithm MD5 is not allowed by current policy");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.3
 */
public final class PolicyViolationException extends OpenCryptoException {

    private final String violatedAlgorithm;

    /**
     * Constructs a PolicyViolationException with algorithm, operation, and message.
     * 使用算法、操作和消息构造策略违规异常。
     *
     * @param algorithm the algorithm that violated the policy | 违反策略的算法
     * @param operation the operation being performed | 正在执行的操作
     * @param message the detail message | 详细消息
     */
    public PolicyViolationException(String algorithm, String operation, String message) {
        super(algorithm, operation, message);
        this.violatedAlgorithm = algorithm;
    }

    /**
     * Returns the algorithm that violated the policy.
     * 返回违反策略的算法。
     *
     * @return the violated algorithm name | 违规算法名称
     */
    public String getViolatedAlgorithm() {
        return violatedAlgorithm;
    }
}
