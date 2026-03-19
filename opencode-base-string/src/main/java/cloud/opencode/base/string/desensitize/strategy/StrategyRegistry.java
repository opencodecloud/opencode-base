package cloud.opencode.base.string.desensitize.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Strategy Registry - Registry for desensitization strategy lookup.
 * 策略注册表 - 用于脱敏策略查找的注册表。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Singleton strategy registry - 单例策略注册表</li>
 *   <li>Built-in strategies (mobile, email, name, etc.) - 内置策略</li>
 *   <li>Custom strategy registration - 自定义策略注册</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * StrategyRegistry registry = StrategyRegistry.getInstance();
 * DesensitizeStrategy strategy = registry.get("mobile");
 * registry.register("custom", s -> s.substring(0, 2) + "***");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap) - 线程安全: 是（ConcurrentHashMap）</li>
 *   <li>Null-safe: No (keys must not be null) - 空值安全: 否（键不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class StrategyRegistry {
    private static final StrategyRegistry INSTANCE = new StrategyRegistry();
    private final Map<String, DesensitizeStrategy> strategies = new ConcurrentHashMap<>();

    private StrategyRegistry() {
        registerDefaultStrategies();
    }

    public static StrategyRegistry getInstance() {
        return INSTANCE;
    }

    private void registerDefaultStrategies() {
        register("mobile", str -> maskMiddle(str, 3, 4, '*'));
        register("idCard", str -> maskMiddle(str, 6, 4, '*'));
        register("email", str -> maskEmail(str));
        register("bankCard", str -> maskMiddle(str, 4, 4, '*'));
        register("name", str -> maskName(str));
        register("password", str -> "******");
    }

    public void register(String name, DesensitizeStrategy strategy) {
        strategies.put(name, strategy);
    }

    public DesensitizeStrategy get(String name) {
        return strategies.get(name);
    }

    private static String maskMiddle(String str, int startKeep, int endKeep, char maskChar) {
        if (str == null || str.length() <= startKeep + endKeep) return str;
        int maskLen = str.length() - startKeep - endKeep;
        return str.substring(0, startKeep) + String.valueOf(maskChar).repeat(maskLen) + str.substring(str.length() - endKeep);
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        if (parts[0].length() <= 1) return email;
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    private static String maskName(String name) {
        if (name == null || name.length() <= 1) return name;
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
}
