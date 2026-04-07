package cloud.opencode.base.i18n.key;

/**
 * Interface for enum-based i18n error codes with automatic key derivation
 * 基于枚举的 i18n 错误码接口，支持自动键推导
 *
 * <p>Extends {@link I18nKey} for use in enum types. Automatically derives the
 * message key from the enum class name and constant name using dot-case convention,
 * eliminating the need to manually specify keys.</p>
 * <p>扩展 {@link I18nKey} 用于枚举类型。从枚举类名和常量名使用点分命名法自动推导消息键，
 * 无需手动指定键。</p>
 *
 * <p><strong>Key Derivation Rules | 键推导规则:</strong></p>
 * <ul>
 *   <li>Class name {@code CamelCase} → {@code camel.case}</li>
 *   <li>Enum name {@code UPPER_SNAKE} → {@code upper.snake}</li>
 *   <li>Combined: {@code className.enumName}</li>
 *   <li>Example: {@code ErrorCode.USER_NOT_FOUND} → {@code error.code.user.not.found}</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic key derivation from enum name - 从枚举名自动推导键</li>
 *   <li>Inherits all I18nKey message retrieval methods - 继承所有 I18nKey 消息获取方法</li>
 *   <li>Can override {@link #key()} for custom key - 可覆盖 {@link #key()} 自定义键</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public enum ErrorCode implements I18nEnum {
 *     USER_NOT_FOUND,      // key: "error.code.user.not.found"
 *     INVALID_INPUT,       // key: "error.code.invalid.input"
 *     ACCESS_DENIED;       // key: "error.code.access.denied"
 * }
 *
 * // Usage
 * String msg = ErrorCode.USER_NOT_FOUND.get(userId);
 * String key = ErrorCode.INVALID_INPUT.key();  // "error.code.invalid.input"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public interface I18nEnum extends I18nKey {

    /** Cached derived keys (enum instances are singletons → safe to cache by identity) */
    java.util.concurrent.ConcurrentHashMap<I18nEnum, String> KEY_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Returns the enum constant name (provided by {@link Enum#name()})
     * 返回枚举常量名称（由 {@link Enum#name()} 提供）
     *
     * @return the enum name in UPPER_SNAKE_CASE | UPPER_SNAKE_CASE 格式的枚举名
     */
    String name();

    /**
     * Derives the message key from the enum class name and constant name
     * 从枚举类名和常量名推导消息键
     *
     * <p>Conversion: {@code CamelCase.UPPER_SNAKE} → {@code camel.case.upper.snake}</p>
     * <p>转换：{@code CamelCase.UPPER_SNAKE} → {@code camel.case.upper.snake}</p>
     *
     * @return the derived message key | 推导的消息键
     */
    @Override
    default String key() {
        return KEY_CACHE.computeIfAbsent(this, k -> {
            String className = k.getClass().getSimpleName();
            String enumName  = ((Enum<?>) k).name();
            return toDotCase(className) + "." + enumName.toLowerCase(java.util.Locale.ROOT).replace('_', '.');
        });
    }

    /**
     * Converts a CamelCase class name to dot.case
     * 将驼峰命名的类名转换为点分命名
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toDotCase("ErrorCode")   = "error.code"
     * toDotCase("HttpStatus")  = "http.status"
     * toDotCase("I18nKey")     = "i18n.key"
     * </pre>
     */
    private String toDotCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) return camelCase;
        StringBuilder sb = new StringBuilder(camelCase.length() + 4);
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('.');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }
}
