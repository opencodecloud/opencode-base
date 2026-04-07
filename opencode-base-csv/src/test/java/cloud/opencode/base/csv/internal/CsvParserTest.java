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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link CsvParser}.
 */
@DisplayName("CsvParser - RFC 4180 解析器测试")
class CsvParserTest {

    @Nested
    @DisplayName("基本解析")
    class BasicParsing {

        @Test
        @DisplayName("解析简单 CSV - 3列3行")
        void parseSimpleCsv() {
            String csv = "a,b,c\n1,2,3\n4,5,6\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(3);
            assertThat(doc.rows().get(0).fields()).containsExactly("a", "b", "c");
            assertThat(doc.rows().get(1).fields()).containsExactly("1", "2", "3");
            assertThat(doc.rows().get(2).fields()).containsExactly("4", "5", "6");
        }

        @Test
        @DisplayName("解析空 CSV")
        void parseEmptyCsv() {
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse("", config);

            assertThat(doc.rows()).isEmpty();
            assertThat(doc.headers()).isEmpty();
        }

        @Test
        @DisplayName("解析单列 CSV")
        void parseSingleColumn() {
            String csv = "value1\nvalue2\nvalue3\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(3);
            assertThat(doc.rows().get(0).fields()).containsExactly("value1");
            assertThat(doc.rows().get(1).fields()).containsExactly("value2");
            assertThat(doc.rows().get(2).fields()).containsExactly("value3");
        }

        @Test
        @DisplayName("解析空字段")
        void parseEmptyFields() {
            String csv = "a,,c\n,b,\n,,\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(3);
            assertThat(doc.rows().get(0).fields()).containsExactly("a", "", "c");
            assertThat(doc.rows().get(1).fields()).containsExactly("", "b", "");
            assertThat(doc.rows().get(2).fields()).containsExactly("", "", "");
        }

        @Test
        @DisplayName("null 输入抛出异常")
        void parseNullThrows() {
            assertThatThrownBy(() -> CsvParser.parse((String) null, CsvConfig.DEFAULT))
                    .isInstanceOf(CsvParseException.class);
        }
    }

    @Nested
    @DisplayName("标题行解析")
    class HeaderParsing {

        @Test
        @DisplayName("解析带标题的 CSV")
        void parseWithHeader() {
            String csv = "name,age,city\nAlice,30,Beijing\nBob,25,Shanghai\n";
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.headers()).containsExactly("name", "age", "city");
            assertThat(doc.rows()).hasSize(2);
            assertThat(doc.rows().get(0).fields()).containsExactly("Alice", "30", "Beijing");
            assertThat(doc.rows().get(1).fields()).containsExactly("Bob", "25", "Shanghai");
        }

        @Test
        @DisplayName("无标题模式")
        void parseWithoutHeader() {
            String csv = "Alice,30,Beijing\nBob,25,Shanghai\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.headers()).isEmpty();
            assertThat(doc.rows()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("引用字段解析")
    class QuotedFields {

        @Test
        @DisplayName("引用字段包含逗号")
        void quotedFieldWithComma() {
            String csv = "\"a,b\",c,d\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(1);
            assertThat(doc.rows().get(0).fields()).containsExactly("a,b", "c", "d");
        }

        @Test
        @DisplayName("引用字段包含换行")
        void quotedFieldWithNewline() {
            String csv = "\"line1\nline2\",b,c\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(1);
            assertThat(doc.rows().get(0).fields()).containsExactly("line1\nline2", "b", "c");
        }

        @Test
        @DisplayName("引用字段包含 CRLF 换行")
        void quotedFieldWithCrLf() {
            String csv = "\"line1\r\nline2\",b,c\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(1);
            assertThat(doc.rows().get(0).fields()).containsExactly("line1\r\nline2", "b", "c");
        }

        @Test
        @DisplayName("转义引号 - 双引号变单引号")
        void escapedQuotes() {
            String csv = "\"a\"\"b\",c,d\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(1);
            assertThat(doc.rows().get(0).fields()).containsExactly("a\"b", "c", "d");
        }

        @Test
        @DisplayName("空引用字段")
        void emptyQuotedField() {
            String csv = "\"\",b,c\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(1);
            assertThat(doc.rows().get(0).fields()).containsExactly("", "b", "c");
        }

        @Test
        @DisplayName("未终止的引用字段抛出异常")
        void unterminatedQuotedField() {
            String csv = "\"abc";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            assertThatThrownBy(() -> CsvParser.parse(csv, config))
                    .isInstanceOf(CsvParseException.class)
                    .hasMessageContaining("Unterminated quoted field");
        }

        @Test
        @DisplayName("引号后出现非法字符抛出异常")
        void invalidCharAfterQuote() {
            String csv = "\"abc\"x,d\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            assertThatThrownBy(() -> CsvParser.parse(csv, config))
                    .isInstanceOf(CsvParseException.class)
                    .hasMessageContaining("Unexpected character");
        }
    }

    @Nested
    @DisplayName("自定义分隔符")
    class CustomDelimiters {

        @Test
        @DisplayName("Tab 分隔符")
        void tabDelimiter() {
            String csv = "a\tb\tc\n1\t2\t3\n";
            CsvConfig config = CsvConfig.builder().delimiter('\t').hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(2);
            assertThat(doc.rows().get(0).fields()).containsExactly("a", "b", "c");
            assertThat(doc.rows().get(1).fields()).containsExactly("1", "2", "3");
        }

        @Test
        @DisplayName("分号分隔符")
        void semicolonDelimiter() {
            String csv = "a;b;c\n1;2;3\n";
            CsvConfig config = CsvConfig.builder().delimiter(';').hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(2);
            assertThat(doc.rows().get(0).fields()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("自定义引号字符")
        void customQuoteChar() {
            String csv = "'a,b',c,d\n";
            CsvConfig config = CsvConfig.builder().quoteChar('\'').hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(1);
            assertThat(doc.rows().get(0).fields()).containsExactly("a,b", "c", "d");
        }
    }

    @Nested
    @DisplayName("行尾处理")
    class LineEndings {

        @Test
        @DisplayName("LF 行尾")
        void lfLineEnding() {
            String csv = "a,b\n1,2\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(2);
        }

        @Test
        @DisplayName("CRLF 行尾")
        void crlfLineEnding() {
            String csv = "a,b\r\n1,2\r\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(2);
            assertThat(doc.rows().get(0).fields()).containsExactly("a", "b");
            assertThat(doc.rows().get(1).fields()).containsExactly("1", "2");
        }

        @Test
        @DisplayName("混合行尾")
        void mixedLineEndings() {
            String csv = "a,b\r\n1,2\n3,4\r\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(3);
        }

        @Test
        @DisplayName("无尾部换行")
        void noTrailingNewline() {
            String csv = "a,b\n1,2";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(2);
            assertThat(doc.rows().get(1).fields()).containsExactly("1", "2");
        }
    }

    @Nested
    @DisplayName("字段处理选项")
    class FieldOptions {

        @Test
        @DisplayName("修剪字段空白")
        void trimFields() {
            String csv = " a , b , c \n 1 , 2 , 3 \n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).trimFields(true).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows().get(0).fields()).containsExactly("a", "b", "c");
            assertThat(doc.rows().get(1).fields()).containsExactly("1", "2", "3");
        }

        @Test
        @DisplayName("跳过空行")
        void skipEmptyRows() {
            String csv = "a,b\n\n1,2\n\n3,4\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).skipEmptyRows(true).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(3);
            assertThat(doc.rows().get(0).fields()).containsExactly("a", "b");
            assertThat(doc.rows().get(1).fields()).containsExactly("1", "2");
            assertThat(doc.rows().get(2).fields()).containsExactly("3", "4");
        }
    }

    @Nested
    @DisplayName("安全限制")
    class SecurityLimits {

        @Test
        @DisplayName("超过最大行数限制")
        void maxRowsExceeded() {
            String csv = "a\nb\nc\nd\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).maxRows(2).build();

            assertThatThrownBy(() -> CsvParser.parse(csv, config))
                    .isInstanceOf(CsvParseException.class)
                    .hasMessageContaining("exceeds maximum");
        }

        @Test
        @DisplayName("超过最大列数限制")
        void maxColumnsExceeded() {
            String csv = "a,b,c,d,e\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).maxColumns(3).build();

            assertThatThrownBy(() -> CsvParser.parse(csv, config))
                    .isInstanceOf(CsvParseException.class)
                    .hasMessageContaining("exceeds maximum");
        }

        @Test
        @DisplayName("最大字段大小限制")
        void maxFieldSizeExceeded() {
            String csv = "abcdefghij,b\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).maxFieldSize(5).build();

            assertThatThrownBy(() -> CsvParser.parse(csv, config))
                    .isInstanceOf(CsvParseException.class)
                    .hasMessageContaining("Field size");
        }

        @Test
        @DisplayName("引用字段内超过最大字段大小")
        void maxFieldSizeInQuotedField() {
            String csv = "\"abcdefghij\",b\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).maxFieldSize(5).build();

            assertThatThrownBy(() -> CsvParser.parse(csv, config))
                    .isInstanceOf(CsvParseException.class)
                    .hasMessageContaining("Field size");
        }
    }

    @Nested
    @DisplayName("Reader 输入")
    class ReaderInput {

        @Test
        @DisplayName("从 Reader 解析")
        void parseFromReader() {
            StringReader reader = new StringReader("a,b,c\n1,2,3\n");
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvDocument doc = CsvParser.parse(reader, config);

            assertThat(doc.rows()).hasSize(2);
        }

        @Test
        @DisplayName("null Reader 抛出异常")
        void parseNullReaderThrows() {
            assertThatThrownBy(() -> CsvParser.parse((java.io.Reader) null, CsvConfig.DEFAULT))
                    .isInstanceOf(CsvParseException.class);
        }
    }

    @Nested
    @DisplayName("行号追踪")
    class RowNumbers {

        @Test
        @DisplayName("行号从1开始递增")
        void rowNumbersStartAtOne() {
            String csv = "header\nrow1\nrow2\nrow3\n";
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            CsvDocument doc = CsvParser.parse(csv, config);

            assertThat(doc.rows()).hasSize(3);
            assertThat(doc.rows().get(0).rowNumber()).isEqualTo(1);
            assertThat(doc.rows().get(1).rowNumber()).isEqualTo(2);
            assertThat(doc.rows().get(2).rowNumber()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("默认配置")
    class DefaultConfig {

        @Test
        @DisplayName("null 配置使用默认值")
        void nullConfigUsesDefault() {
            String csv = "a,b\n1,2\n";

            CsvDocument doc = CsvParser.parse(csv, null);

            assertThat(doc.rows()).isNotEmpty();
        }
    }
}
