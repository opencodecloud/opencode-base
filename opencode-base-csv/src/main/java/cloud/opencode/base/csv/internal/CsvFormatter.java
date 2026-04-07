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
import cloud.opencode.base.csv.exception.CsvWriteException;
import cloud.opencode.base.csv.security.CsvSecurity;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * RFC 4180 Compliant CSV Formatter
 * RFC 4180 兼容 CSV 格式化器
 *
 * <p>Formats CSV documents, rows, and fields according to RFC 4180 rules.
 * Fields are quoted when they contain the delimiter, quote character, or newline characters.
 * Quote characters within fields are escaped by doubling.</p>
 * <p>根据 RFC 4180 规则格式化 CSV 文档、行和字段。
 * 当字段包含分隔符、引号字符或换行字符时，字段会被引用。
 * 字段中的引号字符通过加倍来转义。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless static methods) - 线程安全: 是（无状态静态方法）</li>
 *   <li>Supports formula injection protection - 支持公式注入防护</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvFormatter {

    private CsvFormatter() {
        // utility class
    }

    /**
     * Formats a CsvDocument to a String.
     * 将 CsvDocument 格式化为字符串。
     *
     * @param doc    the document to format - 要格式化的文档
     * @param config the formatting configuration - 格式化配置
     * @return the formatted CSV string - 格式化的 CSV 字符串
     * @throws CsvWriteException if formatting fails - 如果格式化失败
     */
    public static String format(CsvDocument doc, CsvConfig config) {
        if (doc == null) {
            throw new CsvWriteException("CsvDocument must not be null");
        }
        if (config == null) {
            config = CsvConfig.DEFAULT;
        }

        StringBuilder sb = new StringBuilder();
        String lineSep = config.lineSeparator();

        // Write headers if present
        List<String> headers = doc.headers();
        if (headers != null && !headers.isEmpty()) {
            appendRow(sb, headers, config);
            sb.append(lineSep);
        }

        // Write data rows
        List<CsvRow> rows = doc.rows();
        for (int i = 0; i < rows.size(); i++) {
            appendRow(sb, rows.get(i).fields(), config);
            if (i < rows.size() - 1) {
                sb.append(lineSep);
            }
        }

        // Append trailing line separator if there are headers and no rows,
        // or always end with line separator if there are rows
        if (!rows.isEmpty()) {
            sb.append(lineSep);
        }

        return sb.toString();
    }

    /**
     * Formats a CsvDocument and writes it to a Writer.
     * 将 CsvDocument 格式化并写入 Writer。
     *
     * @param doc    the document to format - 要格式化的文档
     * @param writer the output writer - 输出写入器
     * @param config the formatting configuration - 格式化配置
     * @throws CsvWriteException if formatting or I/O fails - 如果格式化或 I/O 失败
     */
    public static void format(CsvDocument doc, Writer writer, CsvConfig config) {
        if (doc == null) {
            throw new CsvWriteException("CsvDocument must not be null");
        }
        if (writer == null) {
            throw new CsvWriteException("Writer must not be null");
        }
        if (config == null) {
            config = CsvConfig.DEFAULT;
        }

        String lineSep = config.lineSeparator();

        try {
            // Write headers if present
            List<String> headers = doc.headers();
            if (headers != null && !headers.isEmpty()) {
                writeRow(writer, headers, config);
                writer.write(lineSep);
            }

            // Write data rows
            List<CsvRow> rows = doc.rows();
            for (int i = 0; i < rows.size(); i++) {
                writeRow(writer, rows.get(i).fields(), config);
                if (i < rows.size() - 1) {
                    writer.write(lineSep);
                }
            }

            if (!rows.isEmpty()) {
                writer.write(lineSep);
            }

        } catch (IOException e) {
            throw new CsvWriteException("I/O error while writing CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Formats a single CsvRow to a String.
     * 将单个 CsvRow 格式化为字符串。
     *
     * @param row    the row to format - 要格式化的行
     * @param config the formatting configuration - 格式化配置
     * @return the formatted row string - 格式化的行字符串
     * @throws CsvWriteException if formatting fails - 如果格式化失败
     */
    public static String formatRow(CsvRow row, CsvConfig config) {
        if (row == null) {
            throw new CsvWriteException("CsvRow must not be null");
        }
        if (config == null) {
            config = CsvConfig.DEFAULT;
        }
        StringBuilder sb = new StringBuilder();
        appendRow(sb, row.fields(), config);
        return sb.toString();
    }

    /**
     * Formats a single field according to RFC 4180 rules.
     * 根据 RFC 4180 规则格式化单个字段。
     *
     * <p>Quoting rules:</p>
     * <ul>
     *   <li>Null fields are replaced with the config's nullString</li>
     *   <li>Fields containing delimiter, quote char, CR, or LF are quoted</li>
     *   <li>Quote characters within fields are escaped by doubling</li>
     *   <li>Formula injection protection is applied if enabled</li>
     * </ul>
     *
     * @param field  the field value (may be null) - 字段值（可能为 null）
     * @param config the formatting configuration - 格式化配置
     * @return the formatted field string - 格式化的字段字符串
     */
    public static String formatField(String field, CsvConfig config) {
        if (config == null) {
            config = CsvConfig.DEFAULT;
        }

        // Handle null fields
        if (field == null) {
            String nullStr = config.nullString();
            return nullStr != null ? nullStr : "";
        }

        // Apply formula injection protection if enabled
        String value = field;
        if (config.formulaProtection()) {
            value = CsvSecurity.sanitize(value);
        }

        char delimiter = config.delimiter();
        char quoteChar = config.quoteChar();

        // Single-pass: check if quoting needed and build quoted result simultaneously
        // Avoids scanning the string twice (once for check, once for escape)
        StringBuilder sb = null;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == delimiter || c == quoteChar || c == '\n' || c == '\r') {
                // Quoting needed — allocate builder and copy chars seen so far
                sb = new StringBuilder(value.length() + 4);
                sb.append(quoteChar);
                // Copy prefix before this position
                for (int j = 0; j < i; j++) {
                    char pj = value.charAt(j);
                    if (pj == quoteChar) {
                        sb.append(quoteChar);
                    }
                    sb.append(pj);
                }
                // Append current char (with escape if needed)
                if (c == quoteChar) {
                    sb.append(quoteChar);
                }
                sb.append(c);
                // Continue with remaining chars
                for (int k = i + 1; k < value.length(); k++) {
                    char ck = value.charAt(k);
                    if (ck == quoteChar) {
                        sb.append(quoteChar);
                    }
                    sb.append(ck);
                }
                sb.append(quoteChar);
                return sb.toString();
            }
        }

        // No quoting needed
        return value;
    }

    /**
     * Appends a row of fields to a StringBuilder.
     * 将一行字段追加到 StringBuilder。
     */
    private static void appendRow(StringBuilder sb, List<String> fields, CsvConfig config) {
        char delimiter = config.delimiter();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(formatField(fields.get(i), config));
        }
    }

    /**
     * Writes a row of fields to a Writer.
     * 将一行字段写入 Writer。
     */
    private static void writeRow(Writer writer, List<String> fields, CsvConfig config)
            throws IOException {
        char delimiter = config.delimiter();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                writer.write(delimiter);
            }
            writer.write(formatField(fields.get(i), config));
        }
    }
}
