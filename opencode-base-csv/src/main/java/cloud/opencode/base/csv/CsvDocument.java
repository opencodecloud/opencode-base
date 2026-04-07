package cloud.opencode.base.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * CSV Document - Immutable representation of an entire CSV document
 * CSV文档 - 整个CSV文档的不可变表示
 *
 * <p>Represents a complete CSV document with optional headers and rows.
 * All instances are immutable and thread-safe. Use the {@link Builder}
 * to construct documents programmatically.</p>
 * <p>表示包含可选标题和行的完整CSV文档。所有实例都是不可变且线程安全的。
 * 使用 {@link Builder} 以编程方式构建文档。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable document model - 不可变文档模型</li>
 *   <li>Optional header row - 可选标题行</li>
 *   <li>Index and name-based column access - 基于索引和名称的列访问</li>
 *   <li>Sub-document extraction - 子文档提取</li>
 *   <li>Stream API support - 流API支持</li>
 *   <li>Builder pattern - Builder模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CsvDocument doc = CsvDocument.builder()
 *     .header("name", "age", "role")
 *     .addRow("Alice", "30", "Engineer")
 *     .addRow("Bob", "25", "Designer")
 *     .build();
 *
 * String name = doc.getRow(0).get(0);              // "Alice"
 * List<String> ages = doc.getColumn("age");          // ["30", "25"]
 * CsvDocument sub = doc.subDocument(0, 1);           // first row only
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Headers and rows are never null - 空值安全: 标题和行永不为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvDocument {

    private final List<String> headers;
    private final List<CsvRow> rows;

    private CsvDocument(List<String> headers, List<CsvRow> rows) {
        this.headers = List.copyOf(headers);
        this.rows = List.copyOf(rows);
    }

    // ==================== Accessors | 访问方法 ====================

    /**
     * Gets the header names
     * 获取标题名称
     *
     * @return unmodifiable list of headers, empty if no header row | 不可修改的标题列表，无标题行时为空
     */
    public List<String> headers() {
        return headers;
    }

    /**
     * Gets all rows
     * 获取所有行
     *
     * @return unmodifiable list of rows | 不可修改的行列表
     */
    public List<CsvRow> rows() {
        return rows;
    }

    /**
     * Gets a row by index
     * 通过索引获取行
     *
     * @param index the 0-based row index | 0起始行索引
     * @return the row | 行
     * @throws IndexOutOfBoundsException if index is out of range | 如果索引越界
     */
    public CsvRow getRow(int index) {
        return rows.get(index);
    }

    /**
     * Gets all values from a column by header name
     * 通过标题名获取列的所有值
     *
     * @param name the header name | 标题名
     * @return unmodifiable list of values | 不可修改的值列表
     * @throws IllegalArgumentException if header name is not found | 如果标题名未找到
     */
    public List<String> getColumn(String name) {
        Objects.requireNonNull(name, "column name must not be null");
        int index = headers.indexOf(name);
        if (index < 0) {
            throw new IllegalArgumentException("Header not found: " + name);
        }
        return getColumn(index);
    }

    /**
     * Gets all values from a column by index
     * 通过索引获取列的所有值
     *
     * @param index the 0-based column index | 0起始列索引
     * @return unmodifiable list of values | 不可修改的值列表
     * @throws IndexOutOfBoundsException if index is negative | 如果索引为负
     */
    public List<String> getColumn(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Column index must not be negative: " + index);
        }
        List<String> values = new ArrayList<>(rows.size());
        for (CsvRow row : rows) {
            if (index < row.size()) {
                values.add(row.get(index));
            } else {
                values.add(null);
            }
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Gets the number of data rows (excluding header)
     * 获取数据行数（不含标题）
     *
     * @return the row count | 行数
     */
    public int rowCount() {
        return rows.size();
    }

    /**
     * Gets the number of columns based on headers or the first row
     * 获取基于标题或第一行的列数
     *
     * @return the column count, or 0 if empty | 列数，如果为空返回0
     */
    public int columnCount() {
        if (!headers.isEmpty()) {
            return headers.size();
        }
        if (!rows.isEmpty()) {
            return rows.getFirst().size();
        }
        return 0;
    }

    /**
     * Checks if the document has no rows
     * 检查文档是否没有行
     *
     * @return true if empty | 如果为空返回true
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    /**
     * Returns a stream of rows
     * 返回行的流
     *
     * @return a stream of CsvRow | CsvRow的流
     */
    public Stream<CsvRow> stream() {
        return rows.stream();
    }

    /**
     * Extracts a sub-document containing a range of rows
     * 提取包含行范围的子文档
     *
     * <p>The sub-document shares the same headers as this document.</p>
     * <p>子文档与此文档共享相同的标题。</p>
     *
     * @param fromRow inclusive start index | 包含的起始索引
     * @param toRow   exclusive end index | 不包含的结束索引
     * @return the sub-document | 子文档
     * @throws IndexOutOfBoundsException if the range is invalid | 如果范围无效
     */
    public CsvDocument subDocument(int fromRow, int toRow) {
        if (fromRow < 0 || toRow > rows.size() || fromRow > toRow) {
            throw new IndexOutOfBoundsException(
                    "Invalid range [" + fromRow + ", " + toRow + ") for document with " + rows.size() + " rows");
        }
        List<CsvRow> subRows = rows.subList(fromRow, toRow);
        return new CsvDocument(headers, subRows);
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CsvDocument that)) return false;
        return headers.equals(that.headers) && rows.equals(that.rows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, rows);
    }

    /**
     * Returns a string preview of the document (first 5 rows)
     * 返回文档的字符串预览（前5行）
     *
     * @return string preview | 字符串预览
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CsvDocument{");
        sb.append("headers=").append(headers);
        sb.append(", rowCount=").append(rows.size());
        int preview = Math.min(rows.size(), 5);
        if (preview > 0) {
            sb.append(", rows=[");
            for (int i = 0; i < preview; i++) {
                if (i > 0) sb.append(", ");
                sb.append(rows.get(i));
            }
            if (rows.size() > 5) {
                sb.append(", ...(").append(rows.size() - 5).append(" more)");
            }
            sb.append(']');
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Creates a new builder
     * 创建新的构建器
     *
     * @return a new Builder instance | 新的Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for CsvDocument
     * CsvDocument构建器
     *
     * <p>Provides a fluent API for constructing CsvDocument instances.</p>
     * <p>提供用于构建CsvDocument实例的流式API。</p>
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-csv V1.0.3
     */
    public static final class Builder {

        private List<String> headers = List.of();
        private final List<CsvRow> rows = new ArrayList<>();

        private Builder() {
        }

        /**
         * Sets the header names
         * 设置标题名称
         *
         * @param headers the header names | 标题名称
         * @return this builder | 此构建器
         */
        public Builder header(String... headers) {
            this.headers = List.of(headers);
            return this;
        }

        /**
         * Sets the header names from a list
         * 从列表设置标题名称
         *
         * @param headers the header names | 标题名称
         * @return this builder | 此构建器
         */
        public Builder header(List<String> headers) {
            this.headers = List.copyOf(headers);
            return this;
        }

        /**
         * Adds a row from field values
         * 从字段值添加行
         *
         * @param fields the field values | 字段值
         * @return this builder | 此构建器
         */
        public Builder addRow(String... fields) {
            rows.add(CsvRow.of(rows.size() + 1, fields));
            return this;
        }

        /**
         * Adds an existing CsvRow
         * 添加现有的CsvRow
         *
         * @param row the row to add | 要添加的行
         * @return this builder | 此构建器
         * @throws NullPointerException if row is null | 如果row为null
         */
        public Builder addRow(CsvRow row) {
            Objects.requireNonNull(row, "row must not be null");
            rows.add(row);
            return this;
        }

        /**
         * Builds the CsvDocument
         * 构建CsvDocument
         *
         * @return the CsvDocument instance | CsvDocument实例
         */
        public CsvDocument build() {
            return new CsvDocument(headers, rows);
        }
    }
}
