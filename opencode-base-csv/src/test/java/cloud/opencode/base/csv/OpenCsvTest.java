package cloud.opencode.base.csv;

import cloud.opencode.base.csv.diff.CsvChange;
import cloud.opencode.base.csv.stream.CsvReader;
import cloud.opencode.base.csv.stream.CsvWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for OpenCsv facade
 * OpenCsv门面测试
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-csv V1.0.3
 */
@DisplayName("OpenCsv - CSV处理门面")
class OpenCsvTest {

    private static final String SAMPLE_CSV = "name,age,email\r\nAlice,30,alice@test.com\r\nBob,25,bob@test.com\r\n";

    record PersonRecord(String name, int age, String email) {
    }

    static class Person {
        private String name;
        private int age;
        private String email;

        public Person() {
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // ==================== Parse Methods | 解析方法 ====================

    @Nested
    @DisplayName("解析方法")
    class ParseMethods {

        @Test
        @DisplayName("解析CSV字符串")
        void parseString() {
            CsvDocument doc = OpenCsv.parse(SAMPLE_CSV);

            assertThat(doc.headers()).containsExactly("name", "age", "email");
            assertThat(doc.rowCount()).isEqualTo(2);
            assertThat(doc.getRow(0).get(0)).isEqualTo("Alice");
            assertThat(doc.getRow(1).get(0)).isEqualTo("Bob");
        }

        @Test
        @DisplayName("解析CSV字符串带配置")
        void parseStringWithConfig() {
            String csv = "name;age\r\nAlice;30\r\n";
            CsvConfig config = CsvConfig.builder().delimiter(';').build();

            CsvDocument doc = OpenCsv.parse(csv, config);

            assertThat(doc.headers()).containsExactly("name", "age");
            assertThat(doc.getRow(0).get(0)).isEqualTo("Alice");
        }

        @Test
        @DisplayName("解析CSV文件")
        void parseFile(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.csv");
            Files.writeString(file, SAMPLE_CSV);

            CsvDocument doc = OpenCsv.parseFile(file);

            assertThat(doc.headers()).containsExactly("name", "age", "email");
            assertThat(doc.rowCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("解析CSV文件带字符集")
        void parseFileWithCharset(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.csv");
            Files.writeString(file, SAMPLE_CSV, StandardCharsets.UTF_8);

            CsvDocument doc = OpenCsv.parseFile(file, StandardCharsets.UTF_8);

            assertThat(doc.rowCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("从输入流解析")
        void parseInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    SAMPLE_CSV.getBytes(StandardCharsets.UTF_8));

            CsvDocument doc = OpenCsv.parse(bais);

            assertThat(doc.rowCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("从Reader解析")
        void parseReader() {
            StringReader reader = new StringReader(SAMPLE_CSV);

            CsvDocument doc = OpenCsv.parse(reader);

            assertThat(doc.rowCount()).isEqualTo(2);
        }
    }

    // ==================== Write Methods | 写入方法 ====================

    @Nested
    @DisplayName("写入方法")
    class WriteMethods {

        @Test
        @DisplayName("导出为字符串往返一致")
        void dumpRoundTrip() {
            CsvDocument doc = OpenCsv.parse(SAMPLE_CSV);

            String output = OpenCsv.dump(doc);
            CsvDocument reparsed = OpenCsv.parse(output);

            assertThat(reparsed.headers()).isEqualTo(doc.headers());
            assertThat(reparsed.rowCount()).isEqualTo(doc.rowCount());
            for (int i = 0; i < doc.rowCount(); i++) {
                assertThat(reparsed.getRow(i).fields()).isEqualTo(doc.getRow(i).fields());
            }
        }

        @Test
        @DisplayName("文件写入往返一致")
        void writeFileRoundTrip(@TempDir Path tempDir) throws IOException {
            CsvDocument doc = OpenCsv.parse(SAMPLE_CSV);
            Path file = tempDir.resolve("output.csv");

            OpenCsv.writeFile(doc, file);
            CsvDocument reparsed = OpenCsv.parseFile(file);

            assertThat(reparsed.headers()).isEqualTo(doc.headers());
            assertThat(reparsed.rowCount()).isEqualTo(doc.rowCount());
        }

        @Test
        @DisplayName("写入OutputStream")
        void writeOutputStream() {
            CsvDocument doc = OpenCsv.parse(SAMPLE_CSV);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            OpenCsv.write(doc, baos);

            String output = baos.toString(StandardCharsets.UTF_8);
            assertThat(output).contains("Alice");
            assertThat(output).contains("Bob");
        }

        @Test
        @DisplayName("写入Writer")
        void writeWriter() {
            CsvDocument doc = OpenCsv.parse(SAMPLE_CSV);
            StringWriter sw = new StringWriter();

            OpenCsv.write(doc, sw);

            assertThat(sw.toString()).contains("Alice");
            assertThat(sw.toString()).contains("Bob");
        }
    }

    // ==================== Binding Methods | 绑定方法 ====================

    @Nested
    @DisplayName("绑定方法")
    class BindingMethods {

        @Test
        @DisplayName("字符串绑定到对象")
        void bindStringToObjects() {
            List<PersonRecord> result = OpenCsv.bind(SAMPLE_CSV, PersonRecord.class);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("Alice");
            assertThat(result.get(0).age()).isEqualTo(30);
        }

        @Test
        @DisplayName("文件绑定到对象")
        void bindFileToObjects(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.csv");
            Files.writeString(file, SAMPLE_CSV);

            List<PersonRecord> result = OpenCsv.bindFile(file, PersonRecord.class);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("对象转CSV字符串往返一致")
        void dumpObjectsRoundTrip() {
            List<PersonRecord> original = List.of(
                    new PersonRecord("Alice", 30, "alice@test.com"),
                    new PersonRecord("Bob", 25, "bob@test.com")
            );

            String csv = OpenCsv.dumpObjects(original, PersonRecord.class);
            List<PersonRecord> reparsed = OpenCsv.bind(csv, PersonRecord.class);

            assertThat(reparsed).hasSize(2);
            assertThat(reparsed.get(0).name()).isEqualTo("Alice");
            assertThat(reparsed.get(0).age()).isEqualTo(30);
            assertThat(reparsed.get(1).name()).isEqualTo("Bob");
        }

        @Test
        @DisplayName("fromObjects生成CSV文档")
        void fromObjectsCreatesDocument() {
            List<PersonRecord> records = List.of(
                    new PersonRecord("Alice", 30, "alice@test.com")
            );

            CsvDocument doc = OpenCsv.fromObjects(records, PersonRecord.class);

            assertThat(doc.headers()).containsExactly("name", "age", "email");
            assertThat(doc.rowCount()).isEqualTo(1);
        }
    }

    // ==================== Streaming Methods | 流式方法 ====================

    @Nested
    @DisplayName("流式读写方法")
    class StreamingMethods {

        @Test
        @DisplayName("流式读取")
        void streamingReader() throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    SAMPLE_CSV.getBytes(StandardCharsets.UTF_8));

            try (CsvReader reader = OpenCsv.reader(bais)) {
                CsvDocument doc = reader.readAll();
                assertThat(doc.rowCount()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("流式写入")
        void streamingWriter() throws IOException {
            StringWriter sw = new StringWriter();

            try (CsvWriter writer = OpenCsv.writer(sw)) {
                writer.writeHeader("name", "age");
                writer.writeRow("Alice", "30");
                writer.writeRow("Bob", "25");
            }

            String result = sw.toString();
            assertThat(result).contains("name");
            assertThat(result).contains("Alice");
        }

        @Test
        @DisplayName("文件流式读写往返")
        void fileStreamRoundTrip(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("stream.csv");

            try (CsvWriter writer = OpenCsv.writer(file)) {
                writer.writeHeader("name", "age");
                writer.writeRow("Alice", "30");
            }

            try (CsvReader reader = OpenCsv.reader(file)) {
                CsvDocument doc = reader.readAll();
                assertThat(doc.headers()).containsExactly("name", "age");
                assertThat(doc.rowCount()).isEqualTo(1);
                assertThat(doc.getRow(0).get(0)).isEqualTo("Alice");
            }
        }
    }

    // ==================== Utility Methods | 工具方法 ====================

    @Nested
    @DisplayName("工具方法")
    class UtilityMethods {

        @Test
        @DisplayName("isValid检查有效CSV")
        void isValidTrue() {
            assertThat(OpenCsv.isValid(SAMPLE_CSV)).isTrue();
        }

        @Test
        @DisplayName("isValid检查null和空字符串")
        void isValidFalseForNullAndEmpty() {
            assertThat(OpenCsv.isValid(null)).isFalse();
            assertThat(OpenCsv.isValid("")).isFalse();
        }

        @Test
        @DisplayName("rowCount计算行数")
        void rowCountReturnsCorrectCount() {
            assertThat(OpenCsv.rowCount(SAMPLE_CSV)).isEqualTo(2);
        }

        @Test
        @DisplayName("headers提取标题")
        void headersExtraction() {
            List<String> headers = OpenCsv.headers(SAMPLE_CSV);

            assertThat(headers).containsExactly("name", "age", "email");
        }

        @Test
        @DisplayName("headers使用自定义分隔符")
        void headersWithCustomDelimiter() {
            String csv = "name;age;email\r\nAlice;30;a@b.com\r\n";
            CsvConfig config = CsvConfig.builder().delimiter(';').build();

            List<String> headers = OpenCsv.headers(csv, config);

            assertThat(headers).containsExactly("name", "age", "email");
        }
    }

    // ==================== Diff Methods | 差异方法 ====================

    @Nested
    @DisplayName("差异方法")
    class DiffMethods {

        @Test
        @DisplayName("diff委托到CsvDiff")
        void diffDelegates() {
            CsvDocument original = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .build();

            CsvDocument modified = CsvDocument.builder()
                    .header("name", "age")
                    .addRow("Alice", "31")
                    .addRow("Bob", "25")
                    .build();

            List<CsvChange> changes = OpenCsv.diff(original, modified);

            assertThat(changes).hasSize(2);
        }

        @Test
        @DisplayName("diffByKey委托到CsvDiff")
        void diffByKeyDelegates() {
            CsvDocument original = CsvDocument.builder()
                    .header("id", "name")
                    .addRow("1", "Alice")
                    .build();

            CsvDocument modified = CsvDocument.builder()
                    .header("id", "name")
                    .addRow("1", "Alice Updated")
                    .build();

            List<CsvChange> changes = OpenCsv.diffByKey(original, modified, "id");

            assertThat(changes).hasSize(1);
            assertThat(changes.get(0).type()).isEqualTo(CsvChange.ChangeType.MODIFIED);
        }
    }

    // ==================== Builder / Config | 构建器/配置 ====================

    @Nested
    @DisplayName("构建器和配置方法")
    class BuilderAndConfig {

        @Test
        @DisplayName("builder创建文档构建器")
        void builderCreatesDocumentBuilder() {
            CsvDocument doc = OpenCsv.builder()
                    .header("name", "age")
                    .addRow("Alice", "30")
                    .build();

            assertThat(doc.headers()).containsExactly("name", "age");
            assertThat(doc.rowCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("config创建配置构建器")
        void configCreatesConfigBuilder() {
            CsvConfig config = OpenCsv.config()
                    .delimiter(';')
                    .trimFields(true)
                    .build();

            assertThat(config.delimiter()).isEqualTo(';');
            assertThat(config.trimFields()).isTrue();
        }
    }

    // ==================== Null Safety | 空值安全 ====================

    @Nested
    @DisplayName("空值安全检查")
    class NullSafety {

        @Test
        @DisplayName("parse(String)不接受null")
        void parseStringRejectsNull() {
            assertThatThrownBy(() -> OpenCsv.parse((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("dump不接受null文档")
        void dumpRejectsNull() {
            assertThatThrownBy(() -> OpenCsv.dump(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("parseFile不接受null路径")
        void parseFileRejectsNull() {
            assertThatThrownBy(() -> OpenCsv.parseFile(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
