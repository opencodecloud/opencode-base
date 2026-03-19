package cloud.opencode.base.string.parse;

import java.util.*;

/**
 * String Parse Facade - Unified entry point for string parsing operations.
 * 字符串解析门面 - 字符串解析操作的统一入口。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>String tokenization - 字符串分词</li>
 *   <li>CSV parsing - CSV解析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<String> tokens = OpenParse.tokenize("hello world java");
 * List<List<String>> csv = OpenParse.parseCsv("a,b\n1,2");
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
public final class OpenParse {
    private OpenParse() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static List<String> tokenize(String str) {
        return TokenizerUtil.tokenize(str);
    }

    public static List<String> tokenize(String str, String delimiters) {
        return TokenizerUtil.tokenize(str, delimiters);
    }

    public static List<List<String>> parseCsv(String csvContent) {
        return CsvUtil.parse(csvContent);
    }

    public static List<Map<String, String>> parseCsvWithHeader(String csvContent) {
        return CsvUtil.parseWithHeader(csvContent);
    }
}
