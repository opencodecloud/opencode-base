package cloud.opencode.base.captcha.security;

import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.support.CaptchaStrength;

/**
 * Anti-Bot Strategy - Adaptive CAPTCHA difficulty strategy
 * 反机器人策略 - 自适应验证码难度策略
 *
 * <p>This class provides strategies for adjusting CAPTCHA difficulty based on
 * detected behavior patterns.</p>
 * <p>此类提供根据检测到的行为模式调整验证码难度的策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Adaptive difficulty adjustment - 自适应难度调整</li>
 *   <li>Behavior-based strategy selection - 基于行为的策略选择</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AntiBotStrategy strategy = new AntiBotStrategy(analyzer);
 * CaptchaStrength strength = strategy.recommendStrength(clientId);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable state) - 线程安全: 否（可变状态）</li>
 *   <li>Null-safe: No (analyzer must not be null) - 空值安全: 否（分析器不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class AntiBotStrategy {

    private final BehaviorAnalyzer analyzer;
    private CaptchaStrength baseStrength = CaptchaStrength.MEDIUM;
    private CaptchaType baseType = CaptchaType.ALPHANUMERIC;

    /**
     * Creates a new strategy with the given analyzer.
     * 使用给定的分析器创建新策略。
     *
     * @param analyzer the behavior analyzer | 行为分析器
     */
    public AntiBotStrategy(BehaviorAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * Creates a new strategy with a new analyzer.
     * 使用新的分析器创建新策略。
     *
     * @return the strategy | 策略
     */
    public static AntiBotStrategy create() {
        return new AntiBotStrategy(new BehaviorAnalyzer());
    }

    /**
     * Sets the base strength.
     * 设置基础强度。
     *
     * @param strength the base strength | 基础强度
     * @return this strategy | 此策略
     */
    public AntiBotStrategy withBaseStrength(CaptchaStrength strength) {
        this.baseStrength = strength;
        return this;
    }

    /**
     * Sets the base type.
     * 设置基础类型。
     *
     * @param type the base type | 基础类型
     * @return this strategy | 此策略
     */
    public AntiBotStrategy withBaseType(CaptchaType type) {
        this.baseType = type;
        return this;
    }

    /**
     * Determines the recommended CAPTCHA strength for a client.
     * 确定客户端的推荐验证码强度。
     *
     * @param clientId the client identifier | 客户端标识符
     * @return the recommended strength | 推荐的强度
     */
    public CaptchaStrength recommendStrength(String clientId) {
        BehaviorAnalyzer.ClientBehavior behavior = analyzer.getBehavior(clientId);

        if (behavior == null) {
            return baseStrength;
        }

        int failures = behavior.getRecentFailures();
        int attempts = behavior.getTotalAttempts();

        // Increase difficulty based on failure rate
        if (failures > 3 || (attempts > 5 && failures > attempts / 2)) {
            return CaptchaStrength.EXTREME;
        }
        if (failures > 1) {
            return CaptchaStrength.HARD;
        }
        if (attempts > 10 && failures == 0) {
            // Good behavior - can reduce difficulty
            return CaptchaStrength.EASY;
        }

        return baseStrength;
    }

    /**
     * Determines the recommended CAPTCHA type for a client.
     * 确定客户端的推荐验证码类型。
     *
     * @param clientId the client identifier | 客户端标识符
     * @return the recommended type | 推荐的类型
     */
    public CaptchaType recommendType(String clientId) {
        BehaviorAnalyzer.ClientBehavior behavior = analyzer.getBehavior(clientId);

        if (behavior == null) {
            return baseType;
        }

        int failures = behavior.getRecentFailures();

        // Escalate to interactive CAPTCHA for suspicious behavior
        if (failures > 5) {
            return CaptchaType.CLICK;
        }
        if (failures > 3) {
            return CaptchaType.SLIDER;
        }

        return baseType;
    }

    /**
     * Checks if a client should be blocked.
     * 检查客户端是否应被阻止。
     *
     * @param clientId the client identifier | 客户端标识符
     * @return true if should be blocked | 如果应该阻止返回 true
     */
    public boolean shouldBlock(String clientId) {
        BehaviorAnalyzer.ClientBehavior behavior = analyzer.getBehavior(clientId);

        if (behavior == null) {
            return false;
        }

        // Block if too many failures
        return behavior.getRecentFailures() > 10;
    }

    /**
     * Gets the behavior analyzer.
     * 获取行为分析器。
     *
     * @return the analyzer | 分析器
     */
    public BehaviorAnalyzer getAnalyzer() {
        return analyzer;
    }
}
