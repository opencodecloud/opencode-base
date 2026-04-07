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
package cloud.opencode.base.csv.stream;

import cloud.opencode.base.csv.CsvConfig;
import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.CsvParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Streaming CSV Reader - Lazy Row-by-Row Parsing
 * 流式 CSV 读取器 - 逐行懒加载解析
 *
 * <p>Reads CSV data incrementally from a Reader, producing one {@link CsvRow}
 * at a time. This avoids loading the entire document into memory, making it
 * suitable for processing large CSV files.</p>
 * <p>从 Reader 增量读取 CSV 数据，每次产生一个 {@link CsvRow}。
 * 这避免了将整个文档加载到内存中，适合处理大型 CSV 文件。</p>
 *
 * <p><strong>Usage | 使用方式:</strong></p>
 * <pre>{@code
 * try (CsvReader reader = CsvReader.of(path, CsvConfig.DEFAULT)) {
 *     reader.stream()
 *           .filter(row -> !row.fields().get(0).isEmpty())
 *           .forEach(System.out::println);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (single-threaded sequential access) - 线程安全: 否（单线程顺序访问）</li>
 *   <li>Enforces maxRows, maxColumns, maxFieldSize limits - 强制执行行数、列数、字段大小限制</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvReader implements Iterable<CsvRow>, AutoCloseable {

    /**
     * Parser states for the incremental state machine.
     * 增量状态机的解析器状态。
     */
    private enum State {
        START_FIELD,
        IN_UNQUOTED,
        IN_QUOTED,
        QUOTE_IN_QUOTED
    }

    private final Reader reader;
    private final CsvConfig config;
    private final char delimiter;
    private final char quoteChar;
    private final boolean trimFields;
    private final boolean skipEmptyRows;
    private final int maxRows;
    private final int maxColumns;
    private final int maxFieldSize;

    private List<String> headers;
    private boolean headerRead;
    private int rowCount;
    private int lineNumber;
    private boolean closed;
    private boolean eof;

    /**
     * Constructs a CsvReader from a Reader and configuration.
     * 使用 Reader 和配置构造 CsvReader。
     *
     * @param reader the input reader - 输入读取器
     * @param config the CSV configuration - CSV 配置
     */
    public CsvReader(Reader reader, CsvConfig config) {
        if (reader == null) {
            throw new CsvParseException("Reader must not be null", 0, 0, null);
        }
        this.config = config != null ? config : CsvConfig.DEFAULT;
        // Wrap in BufferedReader for mark/reset support
        this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
        this.delimiter = this.config.delimiter();
        this.quoteChar = this.config.quoteChar();
        this.trimFields = this.config.trimFields();
        this.skipEmptyRows = this.config.skipEmptyRows();
        this.maxRows = this.config.maxRows();
        this.maxColumns = this.config.maxColumns();
        this.maxFieldSize = this.config.maxFieldSize();
        this.headerRead = false;
        this.rowCount = 0;
        this.lineNumber = 1;
        this.closed = false;
        this.eof = false;
    }

    /**
     * Creates a CsvReader from an InputStream.
     * 从 InputStream 创建 CsvReader。
     *
     * @param input  the input stream - 输入流
     * @param config the CSV configuration - CSV 配置
     * @return a new CsvReader - 新的 CsvReader
     */
    public static CsvReader of(InputStream input, CsvConfig config) {
        if (input == null) {
            throw new CsvParseException("InputStream must not be null", 0, 0, null);
        }
        CsvConfig cfg = config != null ? config : CsvConfig.DEFAULT;
        return new CsvReader(new BufferedReader(new InputStreamReader(input, cfg.charset())), cfg);
    }

    /**
     * Creates a CsvReader from a file Path.
     * 从文件路径创建 CsvReader。
     *
     * @param file   the file path - 文件路径
     * @param config the CSV configuration - CSV 配置
     * @return a new CsvReader - 新的 CsvReader
     */
    public static CsvReader of(Path file, CsvConfig config) {
        if (file == null) {
            throw new CsvParseException("Path must not be null", 0, 0, null);
        }
        CsvConfig cfg = config != null ? config : CsvConfig.DEFAULT;
        try {
            return new CsvReader(Files.newBufferedReader(file, cfg.charset()), cfg);
        } catch (IOException e) {
            throw new CsvParseException(
                    "Failed to open file: " + file + ": " + e.getMessage(),
                    0, 0, null);
        }
    }

    /**
     * Creates a CsvReader from a Reader.
     * 从 Reader 创建 CsvReader。
     *
     * @param reader the input reader - 输入读取器
     * @param config the CSV configuration - CSV 配置
     * @return a new CsvReader - 新的 CsvReader
     */
    public static CsvReader of(Reader reader, CsvConfig config) {
        return new CsvReader(reader, config);
    }

    /**
     * Returns the header row. Reads it on first call if the config has hasHeader=true.
     * 返回标题行。如果配置了 hasHeader=true，则在首次调用时读取。
     *
     * @return the header list, or an empty list if no headers - 标题列表，无标题时返回空列表
     * @throws CsvParseException if reading fails - 如果读取失败
     */
    public List<String> headers() {
        ensureOpen();
        ensureHeaderRead();
        return headers != null ? List.copyOf(headers) : List.of();
    }

    /**
     * Returns an Iterator over CsvRows. Each call returns the same logical iteration
     * (the reader is stateful and can only be iterated once).
     * 返回 CsvRow 的迭代器。每次调用返回相同的逻辑迭代
     * （读取器是有状态的，只能迭代一次）。
     *
     * @return a row iterator - 行迭代器
     */
    @Override
    public Iterator<CsvRow> iterator() {
        ensureOpen();
        ensureHeaderRead();
        return new RowIterator();
    }

    /**
     * Returns a Stream of CsvRows for functional-style processing.
     * 返回 CsvRow 的 Stream，用于函数式处理。
     *
     * @return a stream of rows - 行流
     */
    public Stream<CsvRow> stream() {
        ensureOpen();
        ensureHeaderRead();
        Spliterator<CsvRow> spliterator = Spliterators.spliteratorUnknownSize(
                new RowIterator(),
                Spliterator.ORDERED | Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Reads all remaining rows and returns a complete CsvDocument.
     * 读取所有剩余行并返回完整的 CsvDocument。
     *
     * @return the complete document - 完整文档
     * @throws CsvParseException if reading fails - 如果读取失败
     */
    public CsvDocument readAll() {
        ensureOpen();
        ensureHeaderRead();

        CsvDocument.Builder builder = CsvDocument.builder();
        if (headers != null && !headers.isEmpty()) {
            builder.header(headers);
        }

        CsvRow row;
        while ((row = readNextRow()) != null) {
            builder.addRow(row);
        }

        return builder.build();
    }

    /**
     * Closes this reader and the underlying Reader.
     * 关闭此读取器和底层 Reader。
     */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            try {
                reader.close();
            } catch (IOException e) {
                // Suppress close exceptions
            }
        }
    }

    /**
     * Ensures the header row has been read (if configured).
     * 确保标题行已被读取（如果已配置）。
     */
    private void ensureHeaderRead() {
        if (!headerRead) {
            headerRead = true;
            if (config.hasHeader()) {
                List<String> firstRow = readNextFields();
                if (firstRow != null) {
                    headers = firstRow;
                } else {
                    headers = List.of();
                }
            }
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new CsvParseException("CsvReader is closed", lineNumber, 0, null);
        }
    }

    /**
     * Reads the next row from the reader, returning null at EOF.
     * 从读取器读取下一行，在 EOF 时返回 null。
     */
    private CsvRow readNextRow() {
        while (true) {
            List<String> fields = readNextFields();
            if (fields == null) {
                return null;
            }

            // Skip empty rows if configured
            if (skipEmptyRows && isEmptyRow(fields)) {
                continue;
            }

            rowCount++;

            // Validate row count
            if (maxRows > 0 && rowCount > maxRows) {
                throw new CsvParseException(
                        "Row count " + rowCount + " exceeds maximum " + maxRows,
                        lineNumber, 0, null);
            }

            // Validate column count
            if (maxColumns > 0 && fields.size() > maxColumns) {
                throw new CsvParseException(
                        "Column count " + fields.size() + " exceeds maximum " + maxColumns,
                        lineNumber, 0, null);
            }

            return CsvRow.of(rowCount, fields);
        }
    }

    /**
     * Reads the next set of fields (one logical row) from the reader.
     * A logical row may span multiple physical lines if a field is quoted and contains newlines.
     * Returns null at EOF.
     * 从读取器读取下一组字段（一个逻辑行）。
     * 如果字段被引用且包含换行符，则逻辑行可能跨越多个物理行。
     * 在 EOF 时返回 null。
     */
    private List<String> readNextFields() {
        if (eof) {
            return null;
        }

        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        State state = State.START_FIELD;
        boolean rowComplete = false;
        boolean hasData = false;

        try {
            while (!rowComplete) {
                int ch = reader.read();
                if (ch == -1) {
                    eof = true;
                    // Handle remaining data
                    if (state == State.IN_QUOTED) {
                        throw new CsvParseException(
                                "Unterminated quoted field at end of input",
                                lineNumber, 0, null);
                    }
                    if (hasData || field.length() > 0 || !fields.isEmpty()) {
                        addField(fields, field);
                        rowComplete = true;
                    } else {
                        return null;
                    }
                    break;
                }

                hasData = true;
                char c = (char) ch;

                switch (state) {
                    case START_FIELD -> {
                        if (c == quoteChar) {
                            state = State.IN_QUOTED;
                        } else if (c == delimiter) {
                            addField(fields, field);
                            field.setLength(0);
                            // Stay in START_FIELD
                        } else if (c == '\r') {
                            addField(fields, field);
                            field.setLength(0);
                            // Consume optional LF
                            reader.mark(1);
                            int next = reader.read();
                            if (next != '\n') {
                                if (next == -1) {
                                    eof = true;
                                } else {
                                    reader.reset();
                                }
                            }
                            lineNumber++;
                            rowComplete = true;
                        } else if (c == '\n') {
                            addField(fields, field);
                            field.setLength(0);
                            lineNumber++;
                            rowComplete = true;
                        } else {
                            field.append(c);
                            state = State.IN_UNQUOTED;
                        }
                    }

                    case IN_UNQUOTED -> {
                        if (c == delimiter) {
                            addField(fields, field);
                            field.setLength(0);
                            state = State.START_FIELD;
                        } else if (c == '\r') {
                            addField(fields, field);
                            field.setLength(0);
                            reader.mark(1);
                            int next = reader.read();
                            if (next != '\n') {
                                if (next == -1) {
                                    eof = true;
                                } else {
                                    reader.reset();
                                }
                            }
                            lineNumber++;
                            rowComplete = true;
                        } else if (c == '\n') {
                            addField(fields, field);
                            field.setLength(0);
                            lineNumber++;
                            rowComplete = true;
                        } else {
                            field.append(c);
                            validateFieldSize(field);
                        }
                    }

                    case IN_QUOTED -> {
                        if (c == quoteChar) {
                            state = State.QUOTE_IN_QUOTED;
                        } else {
                            if (c == '\n') {
                                lineNumber++;
                            } else if (c == '\r') {
                                reader.mark(1);
                                int next = reader.read();
                                if (next == '\n') {
                                    field.append('\r');
                                    field.append('\n');
                                    lineNumber++;
                                    continue;
                                } else {
                                    if (next == -1) {
                                        eof = true;
                                    } else {
                                        reader.reset();
                                    }
                                    lineNumber++;
                                }
                            }
                            field.append(c);
                            validateFieldSize(field);
                        }
                    }

                    case QUOTE_IN_QUOTED -> {
                        if (c == quoteChar) {
                            // Escaped quote
                            field.append(quoteChar);
                            validateFieldSize(field);
                            state = State.IN_QUOTED;
                        } else if (c == delimiter) {
                            addField(fields, field);
                            field.setLength(0);
                            state = State.START_FIELD;
                        } else if (c == '\r') {
                            addField(fields, field);
                            field.setLength(0);
                            reader.mark(1);
                            int next = reader.read();
                            if (next != '\n') {
                                if (next == -1) {
                                    eof = true;
                                } else {
                                    reader.reset();
                                }
                            }
                            lineNumber++;
                            rowComplete = true;
                        } else if (c == '\n') {
                            addField(fields, field);
                            field.setLength(0);
                            lineNumber++;
                            rowComplete = true;
                        } else {
                            throw new CsvParseException(
                                    "Unexpected character '" + c + "' after closing quote",
                                    lineNumber, 0, null);
                        }
                    }
                }
            }

        } catch (CsvParseException e) {
            throw e;
        } catch (IOException e) {
            throw new CsvParseException(
                    "I/O error while reading CSV: " + e.getMessage(),
                    lineNumber, 0, null);
        }

        return fields.isEmpty() && !hasData ? null : fields;
    }

    /**
     * Adds the accumulated field to the fields list, applying trimming.
     * 将累积的字段添加到字段列表中，应用修剪。
     */
    private void addField(List<String> fields, StringBuilder field) {
        String value = field.toString();
        if (trimFields) {
            value = value.trim();
        }
        if (maxFieldSize > 0 && value.length() > maxFieldSize) {
            throw new CsvParseException(
                    "Field size " + value.length() + " exceeds maximum " + maxFieldSize,
                    lineNumber, 0, null);
        }
        fields.add(value);
    }

    /**
     * Validates field size during accumulation.
     * 在累积过程中验证字段大小。
     */
    private void validateFieldSize(StringBuilder field) {
        if (maxFieldSize > 0 && field.length() > maxFieldSize) {
            throw new CsvParseException(
                    "Field size " + field.length() + " exceeds maximum " + maxFieldSize,
                    lineNumber, 0, null);
        }
    }

    /**
     * Checks if all fields in a row are empty.
     * 检查行中的所有字段是否为空。
     */
    private static boolean isEmptyRow(List<String> fields) {
        if (fields.isEmpty()) {
            return true;
        }
        for (String f : fields) {
            if (!f.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Iterator that lazily reads rows from the underlying reader.
     * 从底层读取器懒加载读取行的迭代器。
     */
    private final class RowIterator implements Iterator<CsvRow> {

        private CsvRow nextRow;
        private boolean fetched;

        @Override
        public boolean hasNext() {
            if (!fetched) {
                nextRow = readNextRow();
                fetched = true;
            }
            return nextRow != null;
        }

        @Override
        public CsvRow next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more CSV rows");
            }
            CsvRow result = nextRow;
            nextRow = null;
            fetched = false;
            return result;
        }
    }
}
