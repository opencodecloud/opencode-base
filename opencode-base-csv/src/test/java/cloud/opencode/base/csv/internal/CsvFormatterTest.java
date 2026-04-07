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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link CsvFormatter}.
 */
@DisplayName("CsvFormatter - RFC 4180 格式化器测试")
class CsvFormatterTest {

    @Nested
    @DisplayName("文档格式化")
    class DocumentFormatting {

        @Test
        @DisplayName("格式化简单文档")
        void formatSimpleDocument() {
            CsvDocument doc = CsvDocument.builder()
                    .addRow("a", "b", "c")
                    .addRow("1", "2", "3")
                    .build();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            String result = CsvFormatter.format(doc, config);

            assertThat(result).isEqualTo("a,b,c\n1,2,3\n");
        }

        @Test
        @DisplayName("格式化带标题的文档")
        void formatDocumentWithHeaders() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age", "city")
                    .addRow("Alice", "30", "Beijing")
                    .addRow("Bob", "25", "Shanghai")
                    .build();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            String result = CsvFormatter.format(doc, config);

            assertThat(result).isEqualTo("name,age,city\nAlice,30,Beijing\nBob,25,Shanghai\n");
        }

        @Test
        @DisplayName("null 文档抛出异常")
        void formatNullDocumentThrows() {
            assertThatThrownBy(() -> CsvFormatter.format((CsvDocument) null, CsvConfig.DEFAULT))
                    .isInstanceOf(CsvWriteException.class);
        }
    }

    @Nested
    @DisplayName("Writer 输出")
    class WriterOutput {

        @Test
        @DisplayName("格式化到 Writer")
        void formatToWriter() {
            CsvDocument doc = CsvDocument.builder()
                    .addRow("a", "b", "c")
                    .build();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();
            StringWriter sw = new StringWriter();

            CsvFormatter.format(doc, sw, config);

            assertThat(sw.toString()).isEqualTo("a,b,c\n");
        }

        @Test
        @DisplayName("null Writer 抛出异常")
        void formatToNullWriterThrows() {
            CsvDocument doc = CsvDocument.builder().addRow("a").build();
            assertThatThrownBy(() -> CsvFormatter.format(doc, (java.io.Writer) null, CsvConfig.DEFAULT))
                    .isInstanceOf(CsvWriteException.class);
        }
    }

    @Nested
    @DisplayName("行格式化")
    class RowFormatting {

        @Test
        @DisplayName("格式化单行")
        void formatSingleRow() {
            CsvRow row = CsvRow.of("a", "b", "c");
            CsvConfig config = CsvConfig.DEFAULT;

            String result = CsvFormatter.formatRow(row, config);

            assertThat(result).isEqualTo("a,b,c");
        }

        @Test
        @DisplayName("null 行抛出异常")
        void formatNullRowThrows() {
            assertThatThrownBy(() -> CsvFormatter.formatRow(null, CsvConfig.DEFAULT))
                    .isInstanceOf(CsvWriteException.class);
        }
    }

    @Nested
    @DisplayName("字段格式化")
    class FieldFormatting {

        @Test
        @DisplayName("普通字段不加引号")
        void plainFieldNotQuoted() {
            assertThat(CsvFormatter.formatField("hello", CsvConfig.DEFAULT))
                    .isEqualTo("hello");
        }

        @Test
        @DisplayName("包含分隔符的字段加引号")
        void fieldWithDelimiterIsQuoted() {
            assertThat(CsvFormatter.formatField("a,b", CsvConfig.DEFAULT))
                    .isEqualTo("\"a,b\"");
        }

        @Test
        @DisplayName("包含换行的字段加引号")
        void fieldWithNewlineIsQuoted() {
            assertThat(CsvFormatter.formatField("a\nb", CsvConfig.DEFAULT))
                    .isEqualTo("\"a\nb\"");
        }

        @Test
        @DisplayName("包含回车的字段加引号")
        void fieldWithCarriageReturnIsQuoted() {
            assertThat(CsvFormatter.formatField("a\rb", CsvConfig.DEFAULT))
                    .isEqualTo("\"a\rb\"");
        }

        @Test
        @DisplayName("包含引号字符的字段加引号并转义")
        void fieldWithQuoteIsQuotedAndEscaped() {
            assertThat(CsvFormatter.formatField("a\"b", CsvConfig.DEFAULT))
                    .isEqualTo("\"a\"\"b\"");
        }

        @Test
        @DisplayName("空字段不加引号")
        void emptyFieldNotQuoted() {
            assertThat(CsvFormatter.formatField("", CsvConfig.DEFAULT))
                    .isEqualTo("");
        }

        @Test
        @DisplayName("null 字段使用 nullString")
        void nullFieldUsesNullString() {
            CsvConfig config = CsvConfig.builder().nullString("NULL").build();

            assertThat(CsvFormatter.formatField(null, config))
                    .isEqualTo("NULL");
        }

        @Test
        @DisplayName("null 字段默认为空字符串")
        void nullFieldDefaultsToEmpty() {
            assertThat(CsvFormatter.formatField(null, CsvConfig.DEFAULT))
                    .isEqualTo("");
        }
    }

    @Nested
    @DisplayName("自定义分隔符")
    class CustomDelimiters {

        @Test
        @DisplayName("分号分隔符")
        void semicolonDelimiter() {
            CsvConfig config = CsvConfig.builder().delimiter(';').lineSeparator("\n").build();
            CsvDocument doc = CsvDocument.builder()
                    .addRow("a", "b", "c")
                    .build();

            String result = CsvFormatter.format(doc, config);

            assertThat(result).isEqualTo("a;b;c\n");
        }

        @Test
        @DisplayName("自定义引号字符")
        void customQuoteChar() {
            CsvConfig config = CsvConfig.builder().quoteChar('\'').build();

            assertThat(CsvFormatter.formatField("a,b", config))
                    .isEqualTo("'a,b'");
        }

        @Test
        @DisplayName("自定义引号转义")
        void customQuoteEscaping() {
            CsvConfig config = CsvConfig.builder().quoteChar('\'').build();

            assertThat(CsvFormatter.formatField("a'b", config))
                    .isEqualTo("'a''b'");
        }
    }

    @Nested
    @DisplayName("公式注入防护")
    class FormulaProtection {

        @Test
        @DisplayName("启用公式防护时清理危险字段")
        void formulaProtectionSanitizesField() {
            CsvConfig config = CsvConfig.builder().formulaProtection(true).build();

            String result = CsvFormatter.formatField("=SUM(A1:A10)", config);

            // CsvSecurity.sanitize should prefix with a single quote
            assertThat(result).doesNotStartWith("=");
        }

        @Test
        @DisplayName("禁用公式防护时不清理")
        void noFormulaProtectionPassesThrough() {
            CsvConfig config = CsvConfig.builder().formulaProtection(false).build();

            String result = CsvFormatter.formatField("=SUM(A1:A10)", config);

            // Without protection, the formula is passed through (may be quoted)
            assertThat(result).contains("=SUM(A1:A10)");
        }
    }

    @Nested
    @DisplayName("CRLF 行分隔符")
    class CrlfLineSeparator {

        @Test
        @DisplayName("CRLF 行分隔符")
        void crlfLineSeparator() {
            CsvDocument doc = CsvDocument.builder()
                    .addRow("a", "b")
                    .addRow("1", "2")
                    .build();
            CsvConfig config = CsvConfig.builder().lineSeparator("\r\n").build();

            String result = CsvFormatter.format(doc, config);

            assertThat(result).isEqualTo("a,b\r\n1,2\r\n");
        }
    }

    @Nested
    @DisplayName("默认配置")
    class DefaultConfig {

        @Test
        @DisplayName("null 配置使用默认值")
        void nullConfigUsesDefault() {
            String result = CsvFormatter.formatField("hello", null);

            assertThat(result).isEqualTo("hello");
        }
    }
}
