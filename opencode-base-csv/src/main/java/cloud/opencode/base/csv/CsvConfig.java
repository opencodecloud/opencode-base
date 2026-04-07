package cloud.opencode.base.csv;

import cloud.opencode.base.csv.exception.OpenCsvException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * CSV Configuration - Immutable configuration for CSV parsing and writing
 * CSV配置 - CSV解析和写入的不可变配置
 *
 * <p>Holds all parameters that control CSV processing behavior, including delimiters,
 * quoting, character set, size limits, and security options. All instances are
 * immutable and thread-safe.</p>
 * <p>持有控制CSV处理行为的所有参数，包括分隔符、引用、字符集、大小限制和安全选项。
 * 所有实例都是不可变且线程安全的。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 4180 defaults - RFC 4180 默认值</li>
 *   <li>Builder pattern for fluent construction - Builder模式流式构建</li>
 *   <li>Configurable size limits for DoS protection - 可配置大小限制防止DoS</li>
 *   <li>Formula injection protection toggle - 公式注入保护开关</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use defaults
 * CsvConfig config = CsvConfig.DEFAULT;
 *
 * // Custom config
 * CsvConfig config = CsvConfig.builder()
 *     .delimiter(';')
 *     .charset(StandardCharsets.ISO_8859_1)
 *     .trimFields(true)
 *     .formulaProtection(true)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (constructor validates) - 空值安全: 是（构造时校验）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvConfig {

    /** Default delimiter: comma per RFC 4180 | 默认分隔符：逗号（RFC 4180） */
    public static final char DEFAULT_DELIMITER = ',';

    /** Default quote character: double quote per RFC 4180 | 默认引用字符：双引号（RFC 4180） */
    public static final char DEFAULT_QUOTE_CHAR = '"';

    /** Default escape character: double quote per RFC 4180 | 默认转义字符：双引号（RFC 4180） */
    public static final char DEFAULT_ESCAPE_CHAR = '"';

    /** Default line separator: CRLF per RFC 4180 | 默认行分隔符：CRLF（RFC 4180） */
    public static final String DEFAULT_LINE_SEPARATOR = "\r\n";

    /** Default max rows | 默认最大行数 */
    public static final int DEFAULT_MAX_ROWS = 1_000_000;

    /** Default max columns | 默认最大列数 */
    public static final int DEFAULT_MAX_COLUMNS = 10_000;

    /** Default max field size: 1 MB | 默认最大字段大小：1 MB */
    public static final int DEFAULT_MAX_FIELD_SIZE = 1_048_576;

    /**
     * Default configuration instance with RFC 4180 settings
     * 使用RFC 4180设置的默认配置实例
     */
    public static final CsvConfig DEFAULT = builder().build();

    private final char delimiter;
    private final char quoteChar;
    private final char escapeChar;
    private final String lineSeparator;
    private final Charset charset;
    private final boolean hasHeader;
    private final boolean trimFields;
    private final boolean skipEmptyRows;
    private final int maxRows;
    private final int maxColumns;
    private final int maxFieldSize;
    private final boolean formulaProtection;
    private final String nullString;

    private CsvConfig(Builder builder) {
        this.delimiter = builder.delimiter;
        this.quoteChar = builder.quoteChar;
        this.escapeChar = builder.escapeChar;
        this.lineSeparator = Objects.requireNonNull(builder.lineSeparator, "lineSeparator must not be null");
        this.charset = Objects.requireNonNull(builder.charset, "charset must not be null");
        this.hasHeader = builder.hasHeader;
        this.trimFields = builder.trimFields;
        this.skipEmptyRows = builder.skipEmptyRows;
        this.maxRows = builder.maxRows;
        this.maxColumns = builder.maxColumns;
        this.maxFieldSize = builder.maxFieldSize;
        this.formulaProtection = builder.formulaProtection;
        this.nullString = Objects.requireNonNull(builder.nullString, "nullString must not be null");

        validate();
    }

    private void validate() {
        if (maxRows <= 0) {
            throw OpenCsvException.parseError("maxRows must be positive, got: " + maxRows, -1, -1);
        }
        if (maxColumns <= 0) {
            throw OpenCsvException.parseError("maxColumns must be positive, got: " + maxColumns, -1, -1);
        }
        if (maxFieldSize <= 0) {
            throw OpenCsvException.parseError("maxFieldSize must be positive, got: " + maxFieldSize, -1, -1);
        }
        if (lineSeparator.isEmpty()) {
            throw OpenCsvException.parseError("lineSeparator must not be empty", -1, -1);
        }
        if (delimiter == quoteChar) {
            throw OpenCsvException.parseError(
                    "delimiter and quoteChar must be different, both are: '" + delimiter + "'", -1, -1);
        }
    }

    // ==================== Accessors | 访问方法 ====================

    /**
     * Gets the field delimiter character
     * 获取字段分隔符字符
     *
     * @return the delimiter | 分隔符
     */
    public char delimiter() {
        return delimiter;
    }

    /**
     * Gets the quote character
     * 获取引用字符
     *
     * @return the quote character | 引用字符
     */
    public char quoteChar() {
        return quoteChar;
    }

    /**
     * Gets the escape character
     * 获取转义字符
     *
     * @return the escape character | 转义字符
     */
    public char escapeChar() {
        return escapeChar;
    }

    /**
     * Gets the line separator
     * 获取行分隔符
     *
     * @return the line separator | 行分隔符
     */
    public String lineSeparator() {
        return lineSeparator;
    }

    /**
     * Gets the character set
     * 获取字符集
     *
     * @return the charset | 字符集
     */
    public Charset charset() {
        return charset;
    }

    /**
     * Returns whether the CSV has a header row
     * 返回CSV是否有标题行
     *
     * @return true if header present | 如果有标题行返回true
     */
    public boolean hasHeader() {
        return hasHeader;
    }

    /**
     * Returns whether fields should be trimmed
     * 返回字段是否应被修剪
     *
     * @return true if trimming enabled | 如果启用修剪返回true
     */
    public boolean trimFields() {
        return trimFields;
    }

    /**
     * Returns whether empty rows should be skipped
     * 返回是否应跳过空行
     *
     * @return true if skipping empty rows | 如果跳过空行返回true
     */
    public boolean skipEmptyRows() {
        return skipEmptyRows;
    }

    /**
     * Gets the maximum number of rows allowed
     * 获取允许的最大行数
     *
     * @return the max rows | 最大行数
     */
    public int maxRows() {
        return maxRows;
    }

    /**
     * Gets the maximum number of columns allowed
     * 获取允许的最大列数
     *
     * @return the max columns | 最大列数
     */
    public int maxColumns() {
        return maxColumns;
    }

    /**
     * Gets the maximum field size in bytes
     * 获取最大字段大小（字节）
     *
     * @return the max field size | 最大字段大小
     */
    public int maxFieldSize() {
        return maxFieldSize;
    }

    /**
     * Returns whether formula injection protection is enabled
     * 返回是否启用公式注入保护
     *
     * @return true if protection enabled | 如果启用保护返回true
     */
    public boolean formulaProtection() {
        return formulaProtection;
    }

    /**
     * Gets the string to write for null values
     * 获取null值的写入字符串
     *
     * @return the null string | null值字符串
     */
    public String nullString() {
        return nullString;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CsvConfig that)) return false;
        return delimiter == that.delimiter
                && quoteChar == that.quoteChar
                && escapeChar == that.escapeChar
                && hasHeader == that.hasHeader
                && trimFields == that.trimFields
                && skipEmptyRows == that.skipEmptyRows
                && maxRows == that.maxRows
                && maxColumns == that.maxColumns
                && maxFieldSize == that.maxFieldSize
                && formulaProtection == that.formulaProtection
                && lineSeparator.equals(that.lineSeparator)
                && charset.equals(that.charset)
                && nullString.equals(that.nullString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delimiter, quoteChar, escapeChar, lineSeparator, charset,
                hasHeader, trimFields, skipEmptyRows, maxRows, maxColumns, maxFieldSize,
                formulaProtection, nullString);
    }

    @Override
    public String toString() {
        return "CsvConfig{delimiter='" + delimiter
                + "', quoteChar='" + quoteChar
                + "', hasHeader=" + hasHeader
                + ", charset=" + charset
                + ", maxRows=" + maxRows
                + ", formulaProtection=" + formulaProtection
                + '}';
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for CsvConfig
     * CsvConfig构建器
     *
     * <p>Provides a fluent API for constructing CsvConfig instances.</p>
     * <p>提供用于构建CsvConfig实例的流式API。</p>
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-csv V1.0.3
     */
    public static final class Builder {

        private char delimiter = DEFAULT_DELIMITER;
        private char quoteChar = DEFAULT_QUOTE_CHAR;
        private char escapeChar = DEFAULT_ESCAPE_CHAR;
        private String lineSeparator = DEFAULT_LINE_SEPARATOR;
        private Charset charset = StandardCharsets.UTF_8;
        private boolean hasHeader = true;
        private boolean trimFields = false;
        private boolean skipEmptyRows = false;
        private int maxRows = DEFAULT_MAX_ROWS;
        private int maxColumns = DEFAULT_MAX_COLUMNS;
        private int maxFieldSize = DEFAULT_MAX_FIELD_SIZE;
        private boolean formulaProtection = false;
        private String nullString = "";

        private Builder() {
        }

        /**
         * Sets the field delimiter
         * 设置字段分隔符
         *
         * @param delimiter the delimiter character | 分隔符字符
         * @return this builder | 此构建器
         */
        public Builder delimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * Sets the quote character
         * 设置引用字符
         *
         * @param quoteChar the quote character | 引用字符
         * @return this builder | 此构建器
         */
        public Builder quoteChar(char quoteChar) {
            this.quoteChar = quoteChar;
            return this;
        }

        /**
         * Sets the escape character
         * 设置转义字符
         *
         * @param escapeChar the escape character | 转义字符
         * @return this builder | 此构建器
         */
        public Builder escapeChar(char escapeChar) {
            this.escapeChar = escapeChar;
            return this;
        }

        /**
         * Sets the line separator
         * 设置行分隔符
         *
         * @param lineSeparator the line separator | 行分隔符
         * @return this builder | 此构建器
         */
        public Builder lineSeparator(String lineSeparator) {
            this.lineSeparator = lineSeparator;
            return this;
        }

        /**
         * Sets the character set
         * 设置字符集
         *
         * @param charset the charset | 字符集
         * @return this builder | 此构建器
         */
        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        /**
         * Sets whether the CSV has a header row
         * 设置CSV是否有标题行
         *
         * @param hasHeader true if header present | 如果有标题行为true
         * @return this builder | 此构建器
         */
        public Builder hasHeader(boolean hasHeader) {
            this.hasHeader = hasHeader;
            return this;
        }

        /**
         * Sets whether fields should be trimmed
         * 设置是否修剪字段
         *
         * @param trimFields true to enable trimming | 为true启用修剪
         * @return this builder | 此构建器
         */
        public Builder trimFields(boolean trimFields) {
            this.trimFields = trimFields;
            return this;
        }

        /**
         * Sets whether empty rows should be skipped
         * 设置是否跳过空行
         *
         * @param skipEmptyRows true to skip empty rows | 为true跳过空行
         * @return this builder | 此构建器
         */
        public Builder skipEmptyRows(boolean skipEmptyRows) {
            this.skipEmptyRows = skipEmptyRows;
            return this;
        }

        /**
         * Sets the maximum number of rows
         * 设置最大行数
         *
         * @param maxRows the max rows | 最大行数
         * @return this builder | 此构建器
         */
        public Builder maxRows(int maxRows) {
            this.maxRows = maxRows;
            return this;
        }

        /**
         * Sets the maximum number of columns
         * 设置最大列数
         *
         * @param maxColumns the max columns | 最大列数
         * @return this builder | 此构建器
         */
        public Builder maxColumns(int maxColumns) {
            this.maxColumns = maxColumns;
            return this;
        }

        /**
         * Sets the maximum field size in bytes
         * 设置最大字段大小（字节）
         *
         * @param maxFieldSize the max field size | 最大字段大小
         * @return this builder | 此构建器
         */
        public Builder maxFieldSize(int maxFieldSize) {
            this.maxFieldSize = maxFieldSize;
            return this;
        }

        /**
         * Sets whether formula injection protection is enabled
         * 设置是否启用公式注入保护
         *
         * @param formulaProtection true to enable | 为true启用
         * @return this builder | 此构建器
         */
        public Builder formulaProtection(boolean formulaProtection) {
            this.formulaProtection = formulaProtection;
            return this;
        }

        /**
         * Sets the string to write for null values
         * 设置null值的写入字符串
         *
         * @param nullString the null string | null值字符串
         * @return this builder | 此构建器
         */
        public Builder nullString(String nullString) {
            this.nullString = nullString;
            return this;
        }

        /**
         * Builds the CsvConfig
         * 构建CsvConfig
         *
         * @return the CsvConfig instance | CsvConfig实例
         * @throws OpenCsvException if configuration is invalid | 如果配置无效
         */
        public CsvConfig build() {
            return new CsvConfig(this);
        }
    }
}
