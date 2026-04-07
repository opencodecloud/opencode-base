package cloud.opencode.base.csv.security;

import cloud.opencode.base.csv.CsvConfig;
import cloud.opencode.base.csv.exception.OpenCsvException;

/**
 * CSV Security - Security utilities for CSV processing
 * CSV安全 - CSV处理的安全工具
 *
 * <p>Provides protection against common CSV security threats including
 * formula injection (CSV injection / DDE attacks) and resource exhaustion
 * via limit validation.</p>
 * <p>提供针对常见CSV安全威胁的保护，包括公式注入（CSV注入/DDE攻击）
 * 和通过限制验证防止资源耗尽。</p>
 *
 * <p><strong>Formula Injection | 公式注入:</strong></p>
 * <p>When CSV files are opened in spreadsheet applications (Excel, LibreOffice Calc),
 * fields starting with certain characters ({@code =}, {@code +}, {@code -}, {@code @},
 * {@code \t}, {@code \r}) may be interpreted as formulas, leading to code execution.</p>
 * <p>当CSV文件在电子表格应用程序中打开时，以某些字符开头的字段可能被解释为公式，
 * 导致代码执行。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean dangerous = CsvSecurity.isFormulaInjection("=SUM(A1:A10)"); // true
 * String safe = CsvSecurity.sanitize("=cmd|' /C calc'"); // "'=cmd|' /C calc'"
 * CsvSecurity.validateLimits(config, 100, 10, 256); // no exception
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvSecurity {

    private CsvSecurity() {
        // Utility class, no instances
    }

    /**
     * Checks if a value starts with a character that could trigger formula injection
     * 检查值是否以可能触发公式注入的字符开头
     *
     * <p>Characters checked: {@code =}, {@code +}, {@code -}, {@code @},
     * {@code \t} (tab), {@code \r} (carriage return).</p>
     * <p>检查的字符：{@code =}、{@code +}、{@code -}、{@code @}、
     * {@code \t}（制表符）、{@code \r}（回车符）。</p>
     *
     * @param value the value to check | 要检查的值
     * @return true if the value could trigger formula injection | 如果值可能触发公式注入返回true
     */
    public static boolean isFormulaInjection(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        char first = value.charAt(0);
        return first == '=' || first == '+' || first == '-'
                || first == '@' || first == '\t' || first == '\r';
    }

    /**
     * Sanitizes a value by prepending a single quote if formula injection is detected
     * 如果检测到公式注入，通过在前面添加单引号来净化值
     *
     * <p>The prepended single quote causes spreadsheet applications to treat the
     * field as a text literal rather than a formula.</p>
     * <p>前置的单引号使电子表格应用程序将字段视为文本而非公式。</p>
     *
     * @param value the value to sanitize | 要净化的值
     * @return the sanitized value, or the original if no injection detected | 净化后的值，如果未检测到注入则返回原始值
     */
    public static String sanitize(String value) {
        if (isFormulaInjection(value)) {
            return "'" + value;
        }
        return value;
    }

    /**
     * Validates that the given counts do not exceed the configured limits
     * 验证给定的计数不超过配置的限制
     *
     * @param config      the CSV configuration | CSV配置
     * @param rowCount    the number of rows | 行数
     * @param columnCount the number of columns | 列数
     * @param fieldSize   the field size in characters | 字段大小（字符数）
     * @throws OpenCsvException if any limit is exceeded | 如果超出任何限制
     */
    public static void validateLimits(CsvConfig config, int rowCount, int columnCount, int fieldSize) {
        if (rowCount > config.maxRows()) {
            throw new OpenCsvException(
                    "Row count " + rowCount + " exceeds maximum " + config.maxRows());
        }
        if (columnCount > config.maxColumns()) {
            throw new OpenCsvException(
                    "Column count " + columnCount + " exceeds maximum " + config.maxColumns());
        }
        if (fieldSize > config.maxFieldSize()) {
            throw new OpenCsvException(
                    "Field size " + fieldSize + " exceeds maximum " + config.maxFieldSize());
        }
    }
}
