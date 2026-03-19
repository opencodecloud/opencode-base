package cloud.opencode.base.string.desensitize.handler;

import java.util.*;

/**
 * Collection Desensitize Handler - Handles desensitization of Collection fields.
 * 集合脱敏处理器 - 处理集合字段的脱敏操作。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>List desensitization - List脱敏</li>
 *   <li>Set desensitization - Set脱敏</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<?> masked = CollectionHandler.handle(List.of("secret1", "secret2"));
 * // ["***", "***"]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class CollectionHandler {
    private CollectionHandler() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static List<?> handle(List<?> list) {
        if (list == null) return null;
        return list.stream().map(item -> "***").toList();
    }

    public static Set<?> handle(Set<?> set) {
        if (set == null) return null;
        return set.stream().map(item -> "***").collect(java.util.stream.Collectors.toSet());
    }
}
