package cloud.opencode.base.pool.policy;

/**
 * ValidationPolicy - Validation Policy Record (JDK 25 Record)
 * ValidationPolicy - 验证策略记录 (JDK 25 Record)
 *
 * <p>Configures when objects should be validated.</p>
 * <p>配置何时应验证对象。</p>
 *
 * <p><strong>Validation Points | 验证点:</strong></p>
 * <ul>
 *   <li>On borrow - Before returning to client - 借出时 - 返回给客户端前</li>
 *   <li>On return - After client returns - 归还时 - 客户端归还后</li>
 *   <li>On create - After object creation - 创建时 - 对象创建后</li>
 *   <li>While idle - During eviction runs - 空闲时 - 驱逐运行期间</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable validation at four lifecycle points: borrow, return, create, idle - 四个生命周期点可配置验证：借出、归还、创建、空闲</li>
 *   <li>Preset policies: none, onBorrow, recommended, strict - 预设策略：无验证、借出验证、推荐、严格</li>
 *   <li>Immutable JDK 25 record for thread-safe sharing - 不可变JDK 25记录，线程安全共享</li>
 *   <li>Convenience method to check if any validation is enabled - 便捷方法检查是否启用了任何验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ValidationPolicy policy = new ValidationPolicy(true, false, false, true);
 * // Validate on borrow and while idle, skip return and create validation
 *
 * ValidationPolicy strict = ValidationPolicy.strict();
 * // Validate at all points
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 * @param testOnBorrow   validate before borrowing - 借出前验证
 * @param testOnReturn   validate after returning - 归还后验证
 * @param testOnCreate   validate after creation - 创建后验证
 * @param testWhileIdle  validate during eviction - 驱逐期间验证
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public record ValidationPolicy(
        boolean testOnBorrow,
        boolean testOnReturn,
        boolean testOnCreate,
        boolean testWhileIdle
) {

    /**
     * Creates a no-validation policy.
     * 创建无验证策略。
     *
     * @return the policy - 策略
     */
    public static ValidationPolicy none() {
        return new ValidationPolicy(false, false, false, false);
    }

    /**
     * Creates a borrow-only validation policy.
     * 创建仅借出验证策略。
     *
     * @return the policy - 策略
     */
    public static ValidationPolicy onBorrow() {
        return new ValidationPolicy(true, false, false, false);
    }

    /**
     * Creates a recommended validation policy (borrow + idle).
     * 创建推荐的验证策略（借出 + 空闲）。
     *
     * @return the policy - 策略
     */
    public static ValidationPolicy recommended() {
        return new ValidationPolicy(true, false, false, true);
    }

    /**
     * Creates a strict validation policy (all validations).
     * 创建严格的验证策略（所有验证）。
     *
     * @return the policy - 策略
     */
    public static ValidationPolicy strict() {
        return new ValidationPolicy(true, true, true, true);
    }

    /**
     * Checks if any validation is enabled.
     * 检查是否启用了任何验证。
     *
     * @return true if any validation is enabled - 如果启用了任何验证返回true
     */
    public boolean hasAnyValidation() {
        return testOnBorrow || testOnReturn || testOnCreate || testWhileIdle;
    }
}
