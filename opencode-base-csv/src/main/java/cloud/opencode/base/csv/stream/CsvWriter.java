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
import cloud.opencode.base.csv.exception.CsvWriteException;
import cloud.opencode.base.csv.internal.CsvFormatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Streaming CSV Writer - Incremental Row-by-Row Writing
 * 流式 CSV 写入器 - 逐行增量写入
 *
 * <p>Writes CSV data incrementally to a Writer, formatting each row
 * as it is written. Supports fluent API for convenient chaining.</p>
 * <p>增量地将 CSV 数据写入 Writer，在写入时格式化每一行。
 * 支持流畅 API 以便于链式调用。</p>
 *
 * <p><strong>Usage | 使用方式:</strong></p>
 * <pre>{@code
 * try (CsvWriter writer = CsvWriter.of(path, CsvConfig.DEFAULT)) {
 *     writer.writeHeader("name", "age", "city")
 *           .writeRow("Alice", "30", "Beijing")
 *           .writeRow("Bob", "25", "Shanghai");
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (single-threaded sequential access) - 线程安全: 否（单线程顺序访问）</li>
 *   <li>Applies formula injection protection if configured - 如果配置了则应用公式注入防护</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvWriter implements AutoCloseable {

    private final Writer writer;
    private final CsvConfig config;
    private final String lineSeparator;
    private boolean closed;
    private boolean hasWrittenRow;

    /**
     * Constructs a CsvWriter from a Writer and configuration.
     * 使用 Writer 和配置构造 CsvWriter。
     *
     * @param writer the output writer - 输出写入器
     * @param config the CSV configuration - CSV 配置
     */
    public CsvWriter(Writer writer, CsvConfig config) {
        if (writer == null) {
            throw new CsvWriteException("Writer must not be null");
        }
        this.config = config != null ? config : CsvConfig.DEFAULT;
        this.writer = writer;
        this.lineSeparator = this.config.lineSeparator();
        this.closed = false;
        this.hasWrittenRow = false;
    }

    /**
     * Creates a CsvWriter from an OutputStream.
     * 从 OutputStream 创建 CsvWriter。
     *
     * @param output the output stream - 输出流
     * @param config the CSV configuration - CSV 配置
     * @return a new CsvWriter - 新的 CsvWriter
     */
    public static CsvWriter of(OutputStream output, CsvConfig config) {
        if (output == null) {
            throw new CsvWriteException("OutputStream must not be null");
        }
        CsvConfig cfg = config != null ? config : CsvConfig.DEFAULT;
        return new CsvWriter(new BufferedWriter(new OutputStreamWriter(output, cfg.charset())), cfg);
    }

    /**
     * Creates a CsvWriter from a file Path.
     * 从文件路径创建 CsvWriter。
     *
     * @param file   the file path - 文件路径
     * @param config the CSV configuration - CSV 配置
     * @return a new CsvWriter - 新的 CsvWriter
     */
    public static CsvWriter of(Path file, CsvConfig config) {
        if (file == null) {
            throw new CsvWriteException("Path must not be null");
        }
        CsvConfig cfg = config != null ? config : CsvConfig.DEFAULT;
        try {
            return new CsvWriter(Files.newBufferedWriter(file, cfg.charset()), cfg);
        } catch (IOException e) {
            throw new CsvWriteException("Failed to open file: " + file + ": " + e.getMessage(), e);
        }
    }

    /**
     * Creates a CsvWriter from a Writer.
     * 从 Writer 创建 CsvWriter。
     *
     * @param writer the output writer - 输出写入器
     * @param config the CSV configuration - CSV 配置
     * @return a new CsvWriter - 新的 CsvWriter
     */
    public static CsvWriter of(Writer writer, CsvConfig config) {
        return new CsvWriter(writer, config);
    }

    /**
     * Writes a header row.
     * 写入标题行。
     *
     * @param headers the header field names - 标题字段名
     * @return this writer for fluent chaining - 此写入器，用于流畅链式调用
     * @throws CsvWriteException if writing fails - 如果写入失败
     */
    public CsvWriter writeHeader(String... headers) {
        return writeHeader(Arrays.asList(headers));
    }

    /**
     * Writes a header row.
     * 写入标题行。
     *
     * @param headers the header field names - 标题字段名列表
     * @return this writer for fluent chaining - 此写入器，用于流畅链式调用
     * @throws CsvWriteException if writing fails - 如果写入失败
     */
    public CsvWriter writeHeader(List<String> headers) {
        ensureOpen();
        if (headers == null) {
            throw new CsvWriteException("Headers must not be null");
        }
        writeFieldList(headers);
        return this;
    }

    /**
     * Writes a data row from varargs.
     * 从可变参数写入数据行。
     *
     * @param fields the field values - 字段值
     * @return this writer for fluent chaining - 此写入器，用于流畅链式调用
     * @throws CsvWriteException if writing fails - 如果写入失败
     */
    public CsvWriter writeRow(String... fields) {
        return writeRow(Arrays.asList(fields));
    }

    /**
     * Writes a data row from a CsvRow.
     * 从 CsvRow 写入数据行。
     *
     * @param row the row to write - 要写入的行
     * @return this writer for fluent chaining - 此写入器，用于流畅链式调用
     * @throws CsvWriteException if writing fails - 如果写入失败
     */
    public CsvWriter writeRow(CsvRow row) {
        if (row == null) {
            throw new CsvWriteException("CsvRow must not be null");
        }
        return writeRow(row.fields());
    }

    /**
     * Writes a data row from a list of field values.
     * 从字段值列表写入数据行。
     *
     * @param fields the field values - 字段值列表
     * @return this writer for fluent chaining - 此写入器，用于流畅链式调用
     * @throws CsvWriteException if writing fails - 如果写入失败
     */
    public CsvWriter writeRow(List<String> fields) {
        ensureOpen();
        if (fields == null) {
            throw new CsvWriteException("Fields must not be null");
        }
        writeFieldList(fields);
        return this;
    }

    /**
     * Writes a complete CsvDocument (headers + all rows).
     * 写入完整的 CsvDocument（标题 + 所有行）。
     *
     * @param doc the document to write - 要写入的文档
     * @return this writer for fluent chaining - 此写入器，用于流畅链式调用
     * @throws CsvWriteException if writing fails - 如果写入失败
     */
    public CsvWriter writeDocument(CsvDocument doc) {
        ensureOpen();
        if (doc == null) {
            throw new CsvWriteException("CsvDocument must not be null");
        }

        List<String> headers = doc.headers();
        if (headers != null && !headers.isEmpty()) {
            writeHeader(headers);
        }

        for (CsvRow row : doc.rows()) {
            writeRow(row);
        }

        return this;
    }

    /**
     * Flushes the underlying writer.
     * 刷新底层写入器。
     *
     * @throws CsvWriteException if flushing fails - 如果刷新失败
     */
    public void flush() {
        ensureOpen();
        try {
            writer.flush();
        } catch (IOException e) {
            throw new CsvWriteException("Failed to flush: " + e.getMessage(), e);
        }
    }

    /**
     * Flushes and closes the underlying writer.
     * 刷新并关闭底层写入器。
     */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            try {
                writer.flush();
            } catch (IOException e) {
                // Best-effort flush before close
            }
            try {
                writer.close();
            } catch (IOException e) {
                // Suppress close exceptions
            }
        }
    }

    /**
     * Writes a list of fields as a single CSV row.
     * 将字段列表写入为单个 CSV 行。
     */
    private void writeFieldList(List<String> fields) {
        try {
            if (hasWrittenRow) {
                writer.write(lineSeparator);
            }

            char delimiter = config.delimiter();
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) {
                    writer.write(delimiter);
                }
                writer.write(CsvFormatter.formatField(fields.get(i), config));
            }

            hasWrittenRow = true;
        } catch (IOException e) {
            throw new CsvWriteException("I/O error while writing CSV row: " + e.getMessage(), e);
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new CsvWriteException("CsvWriter is closed");
        }
    }
}
