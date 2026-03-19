package cloud.opencode.base.string.parse;

import java.util.*;
import java.util.regex.Pattern;

/**
 * CSV Utility - Provides CSV parsing and formatting methods.
 * CSV工具 - 提供CSV解析和格式化方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>CSV content parsing to rows/columns - CSV内容解析为行列</li>
 *   <li>Header-based parsing to Map list - 基于表头解析为Map列表</li>
 *   <li>CSV generation from row data - 从行数据生成CSV</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<List<String>> rows = CsvUtil.parse("name,age\nAlice,30\nBob,25");
 * List<Map<String, String>> data = CsvUtil.parseWithHeader("name,age\nAlice,30");
 * String csv = CsvUtil.toCsv(List.of(List.of("a", "b"), List.of("1", "2")));
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
 *   <li>Space complexity: O(n) for parsed output - 解析输出 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class CsvUtil {
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\n");

    private CsvUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static List<List<String>> parse(String csvContent) {
        if (csvContent == null) return List.of();
        
        List<List<String>> rows = new ArrayList<>();
        String[] lines = NEWLINE_PATTERN.split(csvContent);
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            rows.add(parseLine(line));
        }
        
        return rows;
    }

    public static List<Map<String, String>> parseWithHeader(String csvContent) {
        List<List<String>> rows = parse(csvContent);
        if (rows.isEmpty()) return List.of();
        
        List<String> headers = rows.get(0);
        List<Map<String, String>> result = new ArrayList<>();
        
        for (int i = 1; i < rows.size(); i++) {
            Map<String, String> row = new LinkedHashMap<>();
            List<String> values = rows.get(i);
            for (int j = 0; j < headers.size() && j < values.size(); j++) {
                row.put(headers.get(j), values.get(j));
            }
            result.add(row);
        }
        
        return result;
    }

    private static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString().trim());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        
        fields.add(field.toString().trim());
        return fields;
    }

    public static String toCsv(List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        for (List<String> row : rows) {
            sb.append(String.join(",", row.stream()
                .map(CsvUtil::escapeCsv)
                .toList()))
              .append("\n");
        }
        return sb.toString();
    }

    private static String escapeCsv(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
