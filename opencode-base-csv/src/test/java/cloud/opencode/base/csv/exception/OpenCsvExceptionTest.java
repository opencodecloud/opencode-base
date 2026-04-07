package cloud.opencode.base.csv.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenCsvException 测试
 */
@DisplayName("OpenCsvException 测试")
class OpenCsvExceptionTest {

    @Nested
    @DisplayName("继承关系")
    class Hierarchy {

        @Test
        @DisplayName("继承OpenException")
        void extendsOpenException() {
            OpenCsvException ex = new OpenCsvException("test");
            assertThat(ex).isInstanceOf(OpenException.class);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("CsvParseException继承OpenCsvException")
        void parseExtendsOpenCsv() {
            CsvParseException ex = CsvParseException.of("msg", 1, 2, "content");
            assertThat(ex).isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("CsvBindException继承OpenCsvException")
        void bindExtendsOpenCsv() {
            CsvBindException ex = CsvBindException.of(String.class, "field", null);
            assertThat(ex).isInstanceOf(OpenCsvException.class);
        }

        @Test
        @DisplayName("CsvWriteException继承OpenCsvException")
        void writeExtendsOpenCsv() {
            CsvWriteException ex = CsvWriteException.of("msg", null);
            assertThat(ex).isInstanceOf(OpenCsvException.class);
        }
    }

    @Nested
    @DisplayName("构造方法")
    class Constructors {

        @Test
        @DisplayName("仅消息构造")
        void messageOnly() {
            OpenCsvException ex = new OpenCsvException("test error");
            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.getLine()).isEqualTo(-1);
            assertThat(ex.getColumn()).isEqualTo(-1);
        }

        @Test
        @DisplayName("消息和原因构造")
        void messageAndCause() {
            Throwable cause = new IOException("io fail");
            OpenCsvException ex = new OpenCsvException("test", cause);
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("消息和位置构造")
        void messageAndLocation() {
            OpenCsvException ex = new OpenCsvException("error", 5, 10);
            assertThat(ex.getLine()).isEqualTo(5);
            assertThat(ex.getColumn()).isEqualTo(10);
            assertThat(ex.getMessage()).contains("line 5");
            assertThat(ex.getMessage()).contains("column 10");
        }

        @Test
        @DisplayName("完整构造")
        void fullConstructor() {
            Throwable cause = new RuntimeException("root");
            OpenCsvException ex = new OpenCsvException("msg", 3, 7, cause);
            assertThat(ex.getLine()).isEqualTo(3);
            assertThat(ex.getColumn()).isEqualTo(7);
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("位置信息")
    class LocationInfo {

        @Test
        @DisplayName("有行信息时hasLineInfo返回true")
        void hasLineInfoTrue() {
            OpenCsvException ex = new OpenCsvException("msg", 1, -1);
            assertThat(ex.hasLineInfo()).isTrue();
            assertThat(ex.hasColumnInfo()).isFalse();
        }

        @Test
        @DisplayName("有列信息时hasColumnInfo返回true")
        void hasColumnInfoTrue() {
            OpenCsvException ex = new OpenCsvException("msg", -1, 5);
            assertThat(ex.hasLineInfo()).isFalse();
            assertThat(ex.hasColumnInfo()).isTrue();
        }

        @Test
        @DisplayName("无位置信息时消息不含位置标记")
        void noLocationInfo() {
            OpenCsvException ex = new OpenCsvException("plain error");
            assertThat(ex.getMessage()).doesNotContain("line");
            assertThat(ex.getMessage()).doesNotContain("column");
        }

        @Test
        @DisplayName("仅有行号时消息只含行标记")
        void onlyLineInfo() {
            OpenCsvException ex = new OpenCsvException("err", 3, -1);
            assertThat(ex.getMessage()).contains("line 3");
            assertThat(ex.getMessage()).doesNotContain("column");
        }
    }

    @Nested
    @DisplayName("组件信息")
    class ComponentInfo {

        @Test
        @DisplayName("组件名称为CSV")
        void componentIsCsv() {
            OpenCsvException ex = new OpenCsvException("msg");
            assertThat(ex.getComponent()).isEqualTo("CSV");
        }

        @Test
        @DisplayName("getMessage包含[CSV]前缀")
        void messageContainsPrefix() {
            OpenCsvException ex = new OpenCsvException("detail");
            assertThat(ex.getMessage()).startsWith("[CSV]");
        }
    }

    @Nested
    @DisplayName("工厂方法")
    class FactoryMethods {

        @Test
        @DisplayName("parseError创建解析异常")
        void parseError() {
            OpenCsvException ex = OpenCsvException.parseError("bad syntax", 10, 5);
            assertThat(ex.getMessage()).contains("Parse error");
            assertThat(ex.getMessage()).contains("bad syntax");
            assertThat(ex.getLine()).isEqualTo(10);
            assertThat(ex.getColumn()).isEqualTo(5);
        }

        @Test
        @DisplayName("writeError创建写入异常")
        void writeError() {
            IOException cause = new IOException("disk full");
            OpenCsvException ex = OpenCsvException.writeError("cannot write", cause);
            assertThat(ex.getMessage()).contains("Write error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("bindError创建绑定异常")
        void bindError() {
            IllegalArgumentException cause = new IllegalArgumentException("type mismatch");
            OpenCsvException ex = OpenCsvException.bindError("bind failed", cause);
            assertThat(ex.getMessage()).contains("Bind error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("ioError创建IO异常")
        void ioError() {
            IOException cause = new IOException("file not found");
            OpenCsvException ex = OpenCsvException.ioError("read failed", cause);
            assertThat(ex.getMessage()).contains("I/O error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("CsvParseException")
    class ParseExceptionTests {

        @Test
        @DisplayName("携带行内容")
        void carriesLineContent() {
            CsvParseException ex = CsvParseException.of("unclosed quote", 5, 23, "field1,\"unclosed");
            assertThat(ex.getLineContent()).isEqualTo("field1,\"unclosed");
            assertThat(ex.getLine()).isEqualTo(5);
            assertThat(ex.getColumn()).isEqualTo(23);
        }

        @Test
        @DisplayName("行内容可为null")
        void lineContentNull() {
            CsvParseException ex = CsvParseException.of("error", 1, 1, null);
            assertThat(ex.getLineContent()).isNull();
        }
    }

    @Nested
    @DisplayName("CsvBindException")
    class BindExceptionTests {

        @Test
        @DisplayName("携带目标类型和字段名")
        void carriesTypeAndField() {
            CsvBindException ex = CsvBindException.of(Integer.class, "age",
                    new NumberFormatException("not a number"));
            assertThat(ex.getTargetType()).isEqualTo(Integer.class);
            assertThat(ex.getFieldName()).isEqualTo("age");
            assertThat(ex.getMessage()).contains("age");
            assertThat(ex.getMessage()).contains("Integer");
        }

        @Test
        @DisplayName("原因可为null")
        void causeNull() {
            CsvBindException ex = CsvBindException.of(String.class, "name", null);
            assertThat(ex.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("CsvWriteException")
    class WriteExceptionTests {

        @Test
        @DisplayName("工厂方法创建")
        void factoryMethod() {
            IOException cause = new IOException("flush failed");
            CsvWriteException ex = CsvWriteException.of("write failed", cause);
            assertThat(ex.getMessage()).contains("write failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("仅消息构造")
        void messageOnly() {
            CsvWriteException ex = new CsvWriteException("error");
            assertThat(ex.getMessage()).contains("error");
            assertThat(ex.getCause()).isNull();
        }
    }
}
