package cloud.opencode.base.deepclone;

/**
 * Clone policy enum defining the cloning behavior
 * 克隆策略枚举，定义克隆行为
 *
 * <p>Different policies control how strictly the cloner handles
 * errors and edge cases during the cloning process.</p>
 * <p>不同的策略控制克隆器在克隆过程中如何严格处理错误和边界情况。</p>
 *
 * <p><strong>Policies | 策略:</strong></p>
 * <ul>
 *   <li>{@link #STANDARD} - Default behavior, balanced error handling - 默认行为，平衡的错误处理</li>
 *   <li>{@link #STRICT} - Fail fast on any error - 任何错误时快速失败</li>
 *   <li>{@link #LENIENT} - Best effort, skip errors - 尽力而为，跳过错误</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.3
 */
public enum ClonePolicy {

    /**
     * Standard cloning policy with balanced error handling
     * 标准克隆策略，平衡的错误处理
     */
    STANDARD,

    /**
     * Strict cloning policy that fails fast on any error
     * 严格克隆策略，任何错误时快速失败
     */
    STRICT,

    /**
     * Lenient cloning policy that skips errors and continues
     * 宽松克隆策略，跳过错误并继续
     */
    LENIENT
}
