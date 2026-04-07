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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link CsvReader}.
 */
@DisplayName("CsvReader - 流式读取器测试")
class CsvReaderTest {

    @Nested
    @DisplayName("标题读取")
    class HeaderReading {

        @Test
        @DisplayName("读取标题行")
        void readHeaders() {
            String csv = "name,age,city\nAlice,30,Beijing\n";
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            try (CsvReader reader = CsvReader.of(new StringReader(csv), config)) {
                List<String> headers = reader.headers();

                assertThat(headers).containsExactly("name", "age", "city");
            }
        }

        @Test
        @DisplayName("无标题模式返回空列表")
        void noHeaderReturnsEmpty() {
            String csv = "Alice,30,Beijing\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            try (CsvReader reader = CsvReader.of(new StringReader(csv), config)) {
                assertThat(reader.headers()).isEmpty();
            }
        }

        @Test
        @DisplayName("多次调用 headers 返回缓存值")
        void headersCached() {
            String csv = "name,age\nAlice,30\n";
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            try (CsvReader reader = CsvReader.of(new StringReader(csv), config)) {
                List<String> first = reader.headers();
                List<String> second = reader.headers();

                assertThat(first).isEqualTo(second);
            }
        }
    }

    @Nested
    @DisplayName("迭代器")
    class IteratorTests {

        @Test
        @DisplayName("迭代所有行")
        void iterateAllRows() {
            String csv = "name,age\nAlice,30\nBob,25\n";
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            List<CsvRow> rows = new ArrayList<>();
            try (CsvReader reader = CsvReader.of(new StringReader(csv), config)) {
                for (CsvRow row : reader) {
                    rows.add(row);
                }
            }

            assertThat(rows).hasSize(2);
            assertThat(rows.get(0).fields()).containsExactly("Alice", "30");
            assertThat(rows.get(1).fields()).containsExactly("Bob", "25");
        }

        @Test
        @DisplayName("空数据无迭代")
        void emptyDataNoIteration() {
            String csv = "name,age\n";
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            try (CsvReader reader = CsvReader.of(new StringReader(csv), config)) {
                Iterator<CsvRow> it = reader.iterator();

                assertThat(it.hasNext()).isFalse();
            }
        }

        @Test
        @DisplayName("迭代结束后抛出 NoSuchElementException")
        void noSuchElementAfterEnd() {
            String csv = "a\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            try (CsvReader reader = CsvReader.of(new StringReader(csv), config)) {
                Iterator<CsvRow> it = reader.iterator();
                it.next(); // consume the one row

                assertThatThrownBy(it::next)
                        .isInstanceOf(NoSuchElementException.class);
            }
        }
    }

    @Nested
    @DisplayName("Stream API")
    class StreamApi {

        @Test
        @DisplayName("使用 Stream 处理行")
        void streamProcessing() {
            String csv = "name,age\nAlice,30\nBob,25\nCharlie,35\n";
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            try (CsvReader reader = CsvReader.of(new StringReader(csv), config)) {
                long count = reader.stream().count();

                assertThat(count).isEqualTo(3);
            }
        }

        @Test
        @DisplayName("Stream 过滤")
        void streamFilter() {
            String csv = "val\nAlice\n\nBob\n";
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            try (CsvReader reader = CsvReader.of(new StringReader(csv), config)) {
                List<String> names = reader.stream()
                        .map(row -> row.fields().get(0))
                        .filter(name -> !name.isEmpty())
                        .toList();

                assertThat(names).containsExactly("Alice", "Bob");
            }
        }

        @Test
        @DisplayName("Stream 是惰性的")
        void streamIsLazy() {
            // Build a moderately large CSV
            StringBuilder sb = new StringBuilder();
            sb.append("id\n");
            for (int i = 0; i < 1000; i++) {
                sb.append(i).append('\n');
            }
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            try (CsvReader reader = CsvReader.of(new StringReader(sb.toString()), config)) {
                // Only take 5 - should not read all 1000 rows
                List<CsvRow> rows = reader.stream().limit(5).toList();

                assertThat(rows).hasSize(5);
            }
        }
    }

    @Nested
    @DisplayName("readAll 方法")
    class ReadAll {

        @Test
        @DisplayName("readAll 返回完整文档")
        void readAllReturnsDocument() {
            String csv = "name,age\nAlice,30\nBob,25\n";
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            try (CsvReader reader = CsvReader.of(new StringReader(csv), config)) {
                CsvDocument doc = reader.readAll();

                assertThat(doc.headers()).containsExactly("name", "age");
                assertThat(doc.rows()).hasSize(2);
            }
        }

        @Test
        @DisplayName("readAll 无标题")
        void readAllNoHeaders() {
            String csv = "a,b\n1,2\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            try (CsvReader reader = CsvReader.of(new StringReader(csv), config)) {
                CsvDocument doc = reader.readAll();

                assertThat(doc.headers()).isEmpty();
                assertThat(doc.rows()).hasSize(2);
            }
        }
    }

    @Nested
    @DisplayName("关闭行为")
    class CloseBehavior {

        @Test
        @DisplayName("关闭后操作抛出异常")
        void operationAfterCloseThrows() {
            String csv = "a\n1\n";
            CsvConfig config = CsvConfig.builder().hasHeader(false).build();

            CsvReader reader = CsvReader.of(new StringReader(csv), config);
            reader.close();

            assertThatThrownBy(reader::headers)
                    .isInstanceOf(CsvParseException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("多次关闭不抛异常")
        void multipleCloseIsSafe() {
            CsvReader reader = CsvReader.of(new StringReader("a\n"), CsvConfig.DEFAULT);
            reader.close();
            reader.close(); // should not throw
        }
    }

    @Nested
    @DisplayName("InputStream 工厂方法")
    class InputStreamFactory {

        @Test
        @DisplayName("从 InputStream 创建")
        void fromInputStream() {
            byte[] data = "name,age\nAlice,30\n".getBytes(StandardCharsets.UTF_8);
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            try (CsvReader reader = CsvReader.of(new ByteArrayInputStream(data), config)) {
                CsvDocument doc = reader.readAll();

                assertThat(doc.headers()).containsExactly("name", "age");
                assertThat(doc.rows()).hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("引用字段跨行")
    class QuotedFieldsAcrossLines {

        @Test
        @DisplayName("引用字段包含换行")
        void quotedFieldWithNewline() {
            String csv = "name,bio\n\"Alice\",\"Line 1\nLine 2\"\n";
            CsvConfig config = CsvConfig.builder().hasHeader(true).build();

            try (CsvReader reader = CsvReader.of(new StringReader(csv), config)) {
                CsvDocument doc = reader.readAll();

                assertThat(doc.rows()).hasSize(1);
                assertThat(doc.rows().get(0).fields().get(1)).isEqualTo("Line 1\nLine 2");
            }
        }
    }

    @Nested
    @DisplayName("安全限制")
    class SecurityLimits {

        @Test
        @DisplayName("超过最大行数限制")
        void maxRowsExceeded() {
            StringBuilder csv = new StringBuilder("val\n");
            for (int i = 0; i < 5; i++) {
                csv.append(i).append('\n');
            }
            CsvConfig config = CsvConfig.builder().hasHeader(true).maxRows(3).build();

            try (CsvReader reader = CsvReader.of(new StringReader(csv.toString()), config)) {
                assertThatThrownBy(reader::readAll)
                        .isInstanceOf(CsvParseException.class)
                        .hasMessageContaining("exceeds maximum");
            }
        }
    }
}
