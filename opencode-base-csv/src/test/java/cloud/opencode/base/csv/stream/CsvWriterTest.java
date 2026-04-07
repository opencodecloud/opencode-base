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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link CsvWriter}.
 */
@DisplayName("CsvWriter - 流式写入器测试")
class CsvWriterTest {

    @Nested
    @DisplayName("标题和行写入")
    class HeaderAndRowWriting {

        @Test
        @DisplayName("写入标题和数据行")
        void writeHeaderAndRows() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeHeader("name", "age", "city")
                      .writeRow("Alice", "30", "Beijing")
                      .writeRow("Bob", "25", "Shanghai");
            }

            assertThat(sw.toString()).isEqualTo("name,age,city\nAlice,30,Beijing\nBob,25,Shanghai");
        }

        @Test
        @DisplayName("只写入数据行")
        void writeOnlyRows() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeRow("a", "b", "c")
                      .writeRow("1", "2", "3");
            }

            assertThat(sw.toString()).isEqualTo("a,b,c\n1,2,3");
        }

        @Test
        @DisplayName("使用 List 写入标题")
        void writeHeaderFromList() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeHeader(List.of("x", "y", "z"));
            }

            assertThat(sw.toString()).isEqualTo("x,y,z");
        }

        @Test
        @DisplayName("使用 CsvRow 写入行")
        void writeRowFromCsvRow() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();
            CsvRow row = CsvRow.of("a", "b", "c");

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeRow(row);
            }

            assertThat(sw.toString()).isEqualTo("a,b,c");
        }

        @Test
        @DisplayName("使用 List 写入行")
        void writeRowFromList() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeRow(List.of("a", "b", "c"));
            }

            assertThat(sw.toString()).isEqualTo("a,b,c");
        }
    }

    @Nested
    @DisplayName("流畅 API")
    class FluentApi {

        @Test
        @DisplayName("链式调用")
        void chainedCalls() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                CsvWriter result = writer.writeHeader("h1", "h2")
                                         .writeRow("v1", "v2");

                assertThat(result).isSameAs(writer);
            }
        }
    }

    @Nested
    @DisplayName("文档写入")
    class DocumentWriting {

        @Test
        @DisplayName("写入完整文档")
        void writeDocument() {
            CsvDocument doc = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .addRow("Bob", "25")
                    .build();

            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeDocument(doc);
            }

            assertThat(sw.toString()).isEqualTo("name,age\nAlice,30\nBob,25");
        }

        @Test
        @DisplayName("写入无标题文档")
        void writeDocumentWithoutHeaders() {
            CsvDocument doc = CsvDocument.builder()
                    .addRow("a", "b")
                    .addRow("1", "2")
                    .build();

            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeDocument(doc);
            }

            assertThat(sw.toString()).isEqualTo("a,b\n1,2");
        }
    }

    @Nested
    @DisplayName("自定义配置")
    class CustomConfig {

        @Test
        @DisplayName("分号分隔符")
        void semicolonDelimiter() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().delimiter(';').lineSeparator("\n").build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeRow("a", "b", "c");
            }

            assertThat(sw.toString()).isEqualTo("a;b;c");
        }

        @Test
        @DisplayName("CRLF 行分隔符")
        void crlfLineSeparator() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\r\n").build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeRow("a", "b")
                      .writeRow("1", "2");
            }

            assertThat(sw.toString()).isEqualTo("a,b\r\n1,2");
        }

        @Test
        @DisplayName("需要引用的字段被正确引用")
        void fieldsRequiringQuoting() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeRow("a,b", "c\"d", "e\nf");
            }

            assertThat(sw.toString()).isEqualTo("\"a,b\",\"c\"\"d\",\"e\nf\"");
        }
    }

    @Nested
    @DisplayName("公式防护")
    class FormulaProtection {

        @Test
        @DisplayName("启用公式防护")
        void formulaProtectionEnabled() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder()
                    .formulaProtection(true)
                    .lineSeparator("\n")
                    .build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeRow("=SUM(A1)", "normal");
            }

            String result = sw.toString();
            // The formula should be sanitized (not starting with =)
            assertThat(result).doesNotStartWith("=");
        }
    }

    @Nested
    @DisplayName("关闭和刷新")
    class CloseAndFlush {

        @Test
        @DisplayName("close 会刷新数据")
        void closeFlushes() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();
            CsvWriter writer = CsvWriter.of(sw, config);
            writer.writeRow("a", "b");
            writer.close();

            assertThat(sw.toString()).isEqualTo("a,b");
        }

        @Test
        @DisplayName("关闭后写入抛出异常")
        void writeAfterCloseThrows() {
            StringWriter sw = new StringWriter();
            CsvWriter writer = CsvWriter.of(sw, CsvConfig.DEFAULT);
            writer.close();

            assertThatThrownBy(() -> writer.writeRow("a"))
                    .isInstanceOf(CsvWriteException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("多次关闭不抛异常")
        void multipleCloseIsSafe() {
            StringWriter sw = new StringWriter();
            CsvWriter writer = CsvWriter.of(sw, CsvConfig.DEFAULT);
            writer.close();
            writer.close(); // should not throw
        }

        @Test
        @DisplayName("flush 方法正常工作")
        void flushWorks() {
            StringWriter sw = new StringWriter();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            try (CsvWriter writer = CsvWriter.of(sw, config)) {
                writer.writeRow("a", "b");
                writer.flush();

                assertThat(sw.toString()).isEqualTo("a,b");
            }
        }
    }

    @Nested
    @DisplayName("OutputStream 工厂方法")
    class OutputStreamFactory {

        @Test
        @DisplayName("从 OutputStream 创建")
        void fromOutputStream() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CsvConfig config = CsvConfig.builder().lineSeparator("\n").build();

            try (CsvWriter writer = CsvWriter.of(baos, config)) {
                writer.writeRow("hello", "world");
            }

            assertThat(baos.toString(StandardCharsets.UTF_8)).isEqualTo("hello,world");
        }
    }

    @Nested
    @DisplayName("异常情况")
    class ErrorCases {

        @Test
        @DisplayName("null Writer 抛出异常")
        void nullWriterThrows() {
            assertThatThrownBy(() -> CsvWriter.of((java.io.Writer) null, CsvConfig.DEFAULT))
                    .isInstanceOf(CsvWriteException.class);
        }

        @Test
        @DisplayName("null CsvRow 抛出异常")
        void nullRowThrows() {
            StringWriter sw = new StringWriter();
            try (CsvWriter writer = CsvWriter.of(sw, CsvConfig.DEFAULT)) {
                assertThatThrownBy(() -> writer.writeRow((CsvRow) null))
                        .isInstanceOf(CsvWriteException.class);
            }
        }

        @Test
        @DisplayName("null CsvDocument 抛出异常")
        void nullDocumentThrows() {
            StringWriter sw = new StringWriter();
            try (CsvWriter writer = CsvWriter.of(sw, CsvConfig.DEFAULT)) {
                assertThatThrownBy(() -> writer.writeDocument(null))
                        .isInstanceOf(CsvWriteException.class);
            }
        }

        @Test
        @DisplayName("null 标题列表抛出异常")
        void nullHeadersThrows() {
            StringWriter sw = new StringWriter();
            try (CsvWriter writer = CsvWriter.of(sw, CsvConfig.DEFAULT)) {
                assertThatThrownBy(() -> writer.writeHeader((List<String>) null))
                        .isInstanceOf(CsvWriteException.class);
            }
        }
    }
}
