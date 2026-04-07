package cloud.opencode.base.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * CSV Row - Immutable representation of a single CSV row
 * CSV行 - 单行CSV数据的不可变表示
 *
 * <p>Represents a single row in a CSV document. Each row contains an ordered list
 * of string field values and an optional 1-based row number for location tracking.
 * All instances are immutable and thread-safe.</p>
 * <p>表示CSV文档中的单行。每行包含一个有序的字符串字段值列表和一个可选的1起始行号用于位置跟踪。
 * 所有实例都是不可变且线程安全的。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable field values - 不可变字段值</li>
 *   <li>1-based row number tracking - 1起始行号跟踪</li>
 *   <li>Index and header-based field access - 基于索引和标题的字段访问</li>
 *   <li>Conversion to Map with headers - 使用标题转换为Map</li>
 *   <li>Stream support - 流支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CsvRow row = CsvRow.of("Alice", "30", "Engineer");
 * String name = row.get(0);
 *
 * CsvRow numbered = CsvRow.of(1, "Bob", "25", "Designer");
 * Map<String, String> map = numbered.toMap(List.of("name", "age", "role"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Fields may contain null - 空值安全: 字段可能包含null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvRow {

    /** Row number indicating unknown position | 表示未知位置的行号 */
    public static final int UNKNOWN_ROW = -1;

    private final List<String> fields;
    private final int rowNumber;

    private CsvRow(int rowNumber, List<String> fields) {
        this.rowNumber = rowNumber;
        this.fields = List.copyOf(fields);
    }

    // ==================== Static Factories | 静态工厂方法 ====================

    /**
     * Creates a row from field values with unknown row number
     * 创建字段值行（行号未知）
     *
     * @param fields the field values | 字段值
     * @return the row | 行
     */
    public static CsvRow of(String... fields) {
        return new CsvRow(UNKNOWN_ROW, List.of(fields));
    }

    /**
     * Creates a row from field values with a specific row number
     * 创建指定行号的字段值行
     *
     * @param rowNumber the 1-based row number | 1起始行号
     * @param fields    the field values | 字段值
     * @return the row | 行
     */
    public static CsvRow of(int rowNumber, String... fields) {
        return new CsvRow(rowNumber, List.of(fields));
    }

    /**
     * Creates a row from a list of field values with a specific row number
     * 从字段值列表创建指定行号的行
     *
     * @param rowNumber the 1-based row number | 1起始行号
     * @param fields    the field values | 字段值
     * @return the row | 行
     * @throws NullPointerException if fields is null | 如果fields为null
     */
    public static CsvRow of(int rowNumber, List<String> fields) {
        Objects.requireNonNull(fields, "fields must not be null");
        return new CsvRow(rowNumber, fields);
    }

    // ==================== Accessors | 访问方法 ====================

    /**
     * Gets a field value by index
     * 通过索引获取字段值
     *
     * @param index the 0-based field index | 0起始字段索引
     * @return the field value | 字段值
     * @throws IndexOutOfBoundsException if index is out of range | 如果索引越界
     */
    public String get(int index) {
        return fields.get(index);
    }

    /**
     * Gets a field value by header name using the parent document's headers
     * 使用父文档的标题通过标题名获取字段值
     *
     * @param headerName the header name | 标题名
     * @param parent     the parent document providing headers | 提供标题的父文档
     * @return the field value | 字段值
     * @throws IllegalArgumentException if header name is not found | 如果标题名未找到
     * @throws IndexOutOfBoundsException if the header index exceeds this row's fields | 如果标题索引超过此行字段数
     */
    public String get(String headerName, CsvDocument parent) {
        Objects.requireNonNull(headerName, "headerName must not be null");
        Objects.requireNonNull(parent, "parent must not be null");
        List<String> headers = parent.headers();
        int index = headers.indexOf(headerName);
        if (index < 0) {
            throw new IllegalArgumentException("Header not found: " + headerName);
        }
        return fields.get(index);
    }

    /**
     * Gets the number of fields in this row
     * 获取此行的字段数
     *
     * @return the number of fields | 字段数
     */
    public int size() {
        return fields.size();
    }

    /**
     * Checks if all fields are empty or the row has no fields
     * 检查所有字段是否为空或行没有字段
     *
     * @return true if empty | 如果为空返回true
     */
    public boolean isEmpty() {
        if (fields.isEmpty()) {
            return true;
        }
        for (String field : fields) {
            if (field != null && !field.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the 1-based row number
     * 获取1起始行号
     *
     * @return the row number, or -1 if unknown | 行号，未知则返回-1
     */
    public int rowNumber() {
        return rowNumber;
    }

    /**
     * Returns an unmodifiable list of field values
     * 返回字段值的不可修改列表
     *
     * @return unmodifiable field list | 不可修改的字段列表
     */
    public List<String> values() {
        return fields;
    }

    /**
     * Returns an unmodifiable list of field values (alias for {@link #values()})
     * 返回字段值的不可修改列表（{@link #values()} 的别名）
     *
     * @return unmodifiable field list | 不可修改的字段列表
     */
    public List<String> fields() {
        return fields;
    }

    /**
     * Returns a stream of field values
     * 返回字段值的流
     *
     * @return a stream of field values | 字段值的流
     */
    public Stream<String> stream() {
        return fields.stream();
    }

    /**
     * Converts this row to a map using the provided headers as keys
     * 使用提供的标题作为键将此行转换为Map
     *
     * <p>If this row has fewer fields than headers, missing fields map to null.
     * If this row has more fields than headers, extra fields are ignored.</p>
     * <p>如果此行的字段少于标题，缺少的字段映射为null。
     * 如果此行的字段多于标题，多余的字段被忽略。</p>
     *
     * @param headers the header names | 标题名
     * @return an unmodifiable map of header-to-value | 不可修改的标题到值的Map
     * @throws NullPointerException if headers is null | 如果headers为null
     */
    public Map<String, String> toMap(List<String> headers) {
        Objects.requireNonNull(headers, "headers must not be null");
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String value = i < fields.size() ? fields.get(i) : null;
            map.put(headers.get(i), value);
        }
        return Collections.unmodifiableMap(map);
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CsvRow that)) return false;
        return rowNumber == that.rowNumber && fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowNumber, fields);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CsvRow");
        if (rowNumber >= 0) {
            sb.append('[').append(rowNumber).append(']');
        }
        sb.append('{');
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(fields.get(i));
        }
        sb.append('}');
        return sb.toString();
    }
}
