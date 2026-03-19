package cloud.opencode.base.string.unicode;

/**
 * Chinese Character Utility - Provides Chinese character detection and manipulation.
 * 中文字符工具 - 提供中文字符检测和操作方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Chinese character detection - 中文字符检测</li>
 *   <li>Simplified/Traditional conversion - 简繁转换</li>
 *   <li>Chinese string containment check - 中文字符串包含检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean isCN = OpenChinese.isChinese('中');            // true
 * boolean has = OpenChinese.containsChinese("abc中文");   // true
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
public final class OpenChinese {
    private OpenChinese() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String toTraditional(String str) {
        // Simplified implementation - would need a full conversion table
        if (str == null) return null;
        return str; // Placeholder - requires conversion table
    }

    public static String toSimplified(String str) {
        // Simplified implementation - would need a full conversion table
        if (str == null) return null;
        return str; // Placeholder - requires conversion table
    }

    public static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }

    public static boolean containsChinese(String str) {
        if (str == null) return false;
        for (char c : str.toCharArray()) {
            if (isChinese(c)) return true;
        }
        return false;
    }

    public static boolean isAllChinese(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (!isChinese(c)) return false;
        }
        return true;
    }
}
