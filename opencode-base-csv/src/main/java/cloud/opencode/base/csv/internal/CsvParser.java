/*
 * Copyright 2025 Leon Soo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.opencode.base.csv.internal;

import cloud.opencode.base.csv.CsvConfig;
import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.CsvParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * RFC 4180 Compliant CSV Parser - State Machine Implementation
 * RFC 4180 兼容 CSV 解析器 - 状态机实现
 *
 * <p>Parses CSV input character-by-character using a finite state machine.
 * Handles quoted fields, embedded delimiters, escaped quotes, and mixed line endings.</p>
 * <p>使用有限状态机逐字符解析 CSV 输入。
 * 处理带引号字段、嵌入分隔符、转义引号和混合行尾。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless static methods) - 线程安全: 是（无状态静态方法）</li>
 *   <li>Enforces maxRows, maxColumns, maxFieldSize limits - 强制执行行数、列数、字段大小限制</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvParser {

    /**
     * Parser states for the finite state machine.
     * 有限状态机的解析器状态。
     */
    private enum State {
        /** Start of a new field - 新字段的开始 */
        START_FIELD,
        /** Inside an unquoted field - 在未引用字段内 */
        IN_UNQUOTED,
        /** Inside a quoted field - 在引用字段内 */
        IN_QUOTED,
        /** Encountered a quote inside a quoted field - 在引用字段内遇到引号 */
        QUOTE_IN_QUOTED,
        /** End of a field (delimiter consumed) - 字段结束（分隔符已消费） */
        END_OF_FIELD,
        /** End of a row (line ending consumed) - 行结束（行尾已消费） */
        END_OF_ROW
    }

    private CsvParser() {
        // utility class
    }

    /**
     * Parses a CSV string into a CsvDocument.
     * 将 CSV 字符串解析为 CsvDocument。
     *
     * @param input  the CSV string - CSV 字符串
     * @param config the parsing configuration - 解析配置
     * @return the parsed document - 解析后的文档
     * @throws CsvParseException if the input is malformed - 如果输入格式错误
     */
    public static CsvDocument parse(String input, CsvConfig config) {
        if (input == null) {
            throw new CsvParseException("CSV input must not be null", 0, 0, null);
        }
        try {
            return parse(new StringReader(input), config);
        } catch (CsvParseException e) {
            throw e;
        }
    }

    /**
     * Parses CSV from a Reader into a CsvDocument.
     * 从 Reader 解析 CSV 为 CsvDocument。
     *
     * @param reader the input reader - 输入读取器
     * @param config the parsing configuration - 解析配置
     * @return the parsed document - 解析后的文档
     * @throws CsvParseException if the input is malformed or an I/O error occurs - 如果输入格式错误或发生 I/O 错误
     */
    public static CsvDocument parse(Reader reader, CsvConfig config) {
        if (reader == null) {
            throw new CsvParseException("Reader must not be null", 0, 0, null);
        }
        if (config == null) {
            config = CsvConfig.DEFAULT;
        }

        final char delimiter = config.delimiter();
        final char quoteChar = config.quoteChar();
        final boolean trimFields = config.trimFields();
        final boolean skipEmptyRows = config.skipEmptyRows();
        final boolean hasHeader = config.hasHeader();
        final int maxRows = config.maxRows();
        final int maxColumns = config.maxColumns();
        final int maxFieldSize = config.maxFieldSize();

        List<String> headers = null;
        List<CsvRow> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder field = new StringBuilder();

        State state = State.START_FIELD;
        int lineNumber = 1;
        int columnNumber = 0;
        int rowCount = 0;
        StringBuilder lineContent = new StringBuilder();

        try {
            int ch;
            while ((ch = reader.read()) != -1) {
                char c = (char) ch;
                columnNumber++;

                // Track line content for error reporting (capped to prevent OOM on long lines)
                if (c != '\n' && c != '\r' && lineContent.length() < 200) {
                    lineContent.append(c);
                }

                switch (state) {
                    case START_FIELD -> {
                        if (c == quoteChar) {
                            state = State.IN_QUOTED;
                        } else if (c == delimiter) {
                            // Empty field
                            addField(currentRow, field, trimFields, maxFieldSize, lineNumber, columnNumber);
                            field.setLength(0);
                            state = State.START_FIELD;
                        } else if (c == '\r') {
                            // Possible CRLF - empty field at end of row
                            addField(currentRow, field, trimFields, maxFieldSize, lineNumber, columnNumber);
                            field.setLength(0);
                            state = State.END_OF_ROW;
                            // Peek for LF
                            reader.mark(1);
                            int next = reader.read();
                            if (next != '\n') {
                                if (next != -1) {
                                    reader.reset();
                                }
                            }
                            rowCount = finalizeRow(currentRow, rows, headers, hasHeader,
                                    skipEmptyRows, rowCount, lineNumber, maxRows, maxColumns);
                            if (headers == null && hasHeader) {
                                headers = new ArrayList<>(currentRow);
                                rowCount = 0; // don't count header as data row
                            }
                            currentRow = new ArrayList<>();
                            lineNumber++;
                            columnNumber = 0;
                            lineContent.setLength(0);
                            state = State.START_FIELD;
                        } else if (c == '\n') {
                            // Empty field at end of row
                            addField(currentRow, field, trimFields, maxFieldSize, lineNumber, columnNumber);
                            field.setLength(0);
                            rowCount = finalizeRow(currentRow, rows, headers, hasHeader,
                                    skipEmptyRows, rowCount, lineNumber, maxRows, maxColumns);
                            if (headers == null && hasHeader) {
                                headers = new ArrayList<>(currentRow);
                                rowCount = 0;
                            }
                            currentRow = new ArrayList<>();
                            lineNumber++;
                            columnNumber = 0;
                            lineContent.setLength(0);
                            state = State.START_FIELD;
                        } else {
                            field.append(c);
                            state = State.IN_UNQUOTED;
                        }
                    }

                    case IN_UNQUOTED -> {
                        if (c == delimiter) {
                            addField(currentRow, field, trimFields, maxFieldSize, lineNumber, columnNumber);
                            field.setLength(0);
                            state = State.START_FIELD;
                        } else if (c == '\r') {
                            addField(currentRow, field, trimFields, maxFieldSize, lineNumber, columnNumber);
                            field.setLength(0);
                            // Peek for LF
                            reader.mark(1);
                            int next = reader.read();
                            if (next != '\n') {
                                if (next != -1) {
                                    reader.reset();
                                }
                            }
                            rowCount = finalizeRow(currentRow, rows, headers, hasHeader,
                                    skipEmptyRows, rowCount, lineNumber, maxRows, maxColumns);
                            if (headers == null && hasHeader) {
                                headers = new ArrayList<>(currentRow);
                                rowCount = 0;
                            }
                            currentRow = new ArrayList<>();
                            lineNumber++;
                            columnNumber = 0;
                            lineContent.setLength(0);
                            state = State.START_FIELD;
                        } else if (c == '\n') {
                            addField(currentRow, field, trimFields, maxFieldSize, lineNumber, columnNumber);
                            field.setLength(0);
                            rowCount = finalizeRow(currentRow, rows, headers, hasHeader,
                                    skipEmptyRows, rowCount, lineNumber, maxRows, maxColumns);
                            if (headers == null && hasHeader) {
                                headers = new ArrayList<>(currentRow);
                                rowCount = 0;
                            }
                            currentRow = new ArrayList<>();
                            lineNumber++;
                            columnNumber = 0;
                            lineContent.setLength(0);
                            state = State.START_FIELD;
                        } else if (c == quoteChar) {
                            // Quote in unquoted field is treated as literal per lenient parsing
                            field.append(c);
                        } else {
                            field.append(c);
                        }
                    }

                    case IN_QUOTED -> {
                        if (c == quoteChar) {
                            state = State.QUOTE_IN_QUOTED;
                        } else {
                            // Track newlines inside quoted fields
                            if (c == '\n') {
                                lineNumber++;
                                columnNumber = 0;
                                lineContent.setLength(0);
                            } else if (c == '\r') {
                                // Peek for LF inside quoted field
                                reader.mark(1);
                                int next = reader.read();
                                if (next == '\n') {
                                    field.append('\r');
                                    field.append('\n');
                                    lineNumber++;
                                    columnNumber = 0;
                                    lineContent.setLength(0);
                                    continue;
                                } else {
                                    if (next != -1) {
                                        reader.reset();
                                    }
                                    lineNumber++;
                                    columnNumber = 0;
                                    lineContent.setLength(0);
                                }
                            }
                            field.append(c);
                            validateFieldSize(field, maxFieldSize, lineNumber, columnNumber);
                        }
                    }

                    case QUOTE_IN_QUOTED -> {
                        if (c == quoteChar) {
                            // Escaped quote (doubled)
                            field.append(quoteChar);
                            validateFieldSize(field, maxFieldSize, lineNumber, columnNumber);
                            state = State.IN_QUOTED;
                        } else if (c == delimiter) {
                            addField(currentRow, field, trimFields, maxFieldSize, lineNumber, columnNumber);
                            field.setLength(0);
                            state = State.START_FIELD;
                        } else if (c == '\r') {
                            addField(currentRow, field, trimFields, maxFieldSize, lineNumber, columnNumber);
                            field.setLength(0);
                            reader.mark(1);
                            int next = reader.read();
                            if (next != '\n') {
                                if (next != -1) {
                                    reader.reset();
                                }
                            }
                            rowCount = finalizeRow(currentRow, rows, headers, hasHeader,
                                    skipEmptyRows, rowCount, lineNumber, maxRows, maxColumns);
                            if (headers == null && hasHeader) {
                                headers = new ArrayList<>(currentRow);
                                rowCount = 0;
                            }
                            currentRow = new ArrayList<>();
                            lineNumber++;
                            columnNumber = 0;
                            lineContent.setLength(0);
                            state = State.START_FIELD;
                        } else if (c == '\n') {
                            addField(currentRow, field, trimFields, maxFieldSize, lineNumber, columnNumber);
                            field.setLength(0);
                            rowCount = finalizeRow(currentRow, rows, headers, hasHeader,
                                    skipEmptyRows, rowCount, lineNumber, maxRows, maxColumns);
                            if (headers == null && hasHeader) {
                                headers = new ArrayList<>(currentRow);
                                rowCount = 0;
                            }
                            currentRow = new ArrayList<>();
                            lineNumber++;
                            columnNumber = 0;
                            lineContent.setLength(0);
                            state = State.START_FIELD;
                        } else {
                            // Character after closing quote that is not delimiter or newline
                            // RFC 4180 doesn't allow this; we throw a parse error
                            throw new CsvParseException(
                                    "Unexpected character '" + c + "' after closing quote",
                                    lineNumber, columnNumber, lineContent.toString());
                        }
                    }

                    default -> throw new CsvParseException(
                            "Unexpected parser state: " + state,
                            lineNumber, columnNumber, lineContent.toString());
                }
            }

            // Handle remaining data after EOF
            if (state == State.IN_QUOTED) {
                throw new CsvParseException(
                        "Unterminated quoted field at end of input",
                        lineNumber, columnNumber, lineContent.toString());
            }

            // Finalize the last field/row if there's data
            if (state == State.IN_UNQUOTED || state == State.START_FIELD
                    || state == State.QUOTE_IN_QUOTED) {
                // If we have any field content or any fields already in current row,
                // or we started a new field (which means trailing delimiter)
                if (field.length() > 0 || !currentRow.isEmpty()) {
                    addField(currentRow, field, trimFields, maxFieldSize, lineNumber, columnNumber);
                    field.setLength(0);
                    rowCount = finalizeRow(currentRow, rows, headers, hasHeader,
                            skipEmptyRows, rowCount, lineNumber, maxRows, maxColumns);
                    if (headers == null && hasHeader) {
                        headers = new ArrayList<>(currentRow);
                    }
                }
            }

        } catch (CsvParseException e) {
            throw e;
        } catch (IOException e) {
            throw new CsvParseException(
                    "I/O error while parsing CSV: " + e.getMessage(),
                    lineNumber, columnNumber, lineContent.toString());
        }

        // Build document
        CsvDocument.Builder builder = CsvDocument.builder();
        if (headers != null) {
            builder.header(headers);
        }
        for (CsvRow row : rows) {
            builder.addRow(row);
        }
        return builder.build();
    }

    /**
     * Adds the current field to the row, applying trimming and size validation.
     * 将当前字段添加到行中，应用修剪和大小验证。
     */
    private static void addField(List<String> row, StringBuilder field,
                                  boolean trim, int maxFieldSize,
                                  int lineNumber, int columnNumber) {
        String value = field.toString();
        if (trim) {
            value = value.trim();
        }
        if (maxFieldSize > 0 && value.length() > maxFieldSize) {
            throw new CsvParseException(
                    "Field size " + value.length() + " exceeds maximum " + maxFieldSize,
                    lineNumber, columnNumber, null);
        }
        row.add(value);
    }

    /**
     * Validates field size during accumulation inside quoted fields.
     * 在引用字段累积过程中验证字段大小。
     */
    private static void validateFieldSize(StringBuilder field, int maxFieldSize,
                                           int lineNumber, int columnNumber) {
        if (maxFieldSize > 0 && field.length() > maxFieldSize) {
            throw new CsvParseException(
                    "Field size " + field.length() + " exceeds maximum " + maxFieldSize,
                    lineNumber, columnNumber, null);
        }
    }

    /**
     * Finalizes a row: validates limits, adds to rows list or stores as header.
     * Returns updated row count.
     * 完成一行的处理：验证限制，添加到行列表或存储为标题。
     * 返回更新的行计数。
     */
    private static int finalizeRow(List<String> currentRow, List<CsvRow> rows,
                                    List<String> existingHeaders, boolean hasHeader,
                                    boolean skipEmptyRows, int rowCount,
                                    int lineNumber, int maxRows, int maxColumns) {
        // Skip empty rows if configured
        if (skipEmptyRows && isEmptyRow(currentRow)) {
            return rowCount;
        }

        // Validate column count
        if (maxColumns > 0 && currentRow.size() > maxColumns) {
            throw new CsvParseException(
                    "Column count " + currentRow.size() + " exceeds maximum " + maxColumns,
                    lineNumber, 0, null);
        }

        // If this is the header row, don't add to rows
        if (hasHeader && existingHeaders == null) {
            // This will be stored as header by the caller
            return rowCount;
        }

        rowCount++;

        // Validate row count
        if (maxRows > 0 && rowCount > maxRows) {
            throw new CsvParseException(
                    "Row count " + rowCount + " exceeds maximum " + maxRows,
                    lineNumber, 0, null);
        }

        rows.add(CsvRow.of(rowCount, currentRow));
        return rowCount;
    }

    /**
     * Checks if a row is empty (all fields are empty strings).
     * 检查行是否为空（所有字段都是空字符串）。
     */
    private static boolean isEmptyRow(List<String> row) {
        if (row.isEmpty()) {
            return true;
        }
        for (String field : row) {
            if (!field.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
