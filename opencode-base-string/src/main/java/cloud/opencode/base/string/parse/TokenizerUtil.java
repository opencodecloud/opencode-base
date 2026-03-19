package cloud.opencode.base.string.parse;

import java.util.*;

/**
 * Tokenizer Utility - Provides string tokenization methods.
 * 分词器工具 - 提供字符串分词方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Default whitespace tokenization - 默认空白分词</li>
 *   <li>Custom delimiter tokenization - 自定义分隔符分词</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<String> tokens = TokenizerUtil.tokenize("hello world"); // ["hello", "world"]
 * List<String> custom = TokenizerUtil.tokenize("a;b;c", ";");  // ["a", "b", "c"]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = input length - O(n), n为输入长度</li>
 *   <li>Space complexity: O(t) where t = number of tokens - O(t), t为词元数</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class TokenizerUtil {
    private TokenizerUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static List<String> tokenize(String str) {
        return tokenize(str, " \\t\\n\\r\\f");
    }

    public static List<String> tokenize(String str, String delimiters) {
        if (str == null) return List.of();
        
        StringTokenizer tokenizer = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }
        return tokens;
    }
}
