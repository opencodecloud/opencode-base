package cloud.opencode.base.csv;

import cloud.opencode.base.csv.exception.OpenCsvException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CsvConfig 测试
 */
@DisplayName("CsvConfig 测试")
class CsvConfigTest {

    @Nested
    @DisplayName("默认配置")
    class DefaultConfig {

        @Test
        @DisplayName("默认分隔符为逗号")
        void defaultDelimiter() {
            assertThat(CsvConfig.DEFAULT.delimiter()).isEqualTo(',');
        }

        @Test
        @DisplayName("默认引用字符为双引号")
        void defaultQuoteChar() {
            assertThat(CsvConfig.DEFAULT.quoteChar()).isEqualTo('"');
        }

        @Test
        @DisplayName("默认转义字符为双引号")
        void defaultEscapeChar() {
            assertThat(CsvConfig.DEFAULT.escapeChar()).isEqualTo('"');
        }

        @Test
        @DisplayName("默认行分隔符为CRLF")
        void defaultLineSeparator() {
            assertThat(CsvConfig.DEFAULT.lineSeparator()).isEqualTo("\r\n");
        }

        @Test
        @DisplayName("默认字符集为UTF-8")
        void defaultCharset() {
            assertThat(CsvConfig.DEFAULT.charset()).isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("默认有标题行")
        void defaultHasHeader() {
            assertThat(CsvConfig.DEFAULT.hasHeader()).isTrue();
        }

        @Test
        @DisplayName("默认不修剪字段")
        void defaultTrimFields() {
            assertThat(CsvConfig.DEFAULT.trimFields()).isFalse();
        }

        @Test
        @DisplayName("默认不跳过空行")
        void defaultSkipEmptyRows() {
            assertThat(CsvConfig.DEFAULT.skipEmptyRows()).isFalse();
        }

        @Test
        @DisplayName("默认最大行数为1,000,000")
        void defaultMaxRows() {
            assertThat(CsvConfig.DEFAULT.maxRows()).isEqualTo(1_000_000);
        }

        @Test
        @DisplayName("默认最大列数为10,000")
        void defaultMaxColumns() {
            assertThat(CsvConfig.DEFAULT.maxColumns()).isEqualTo(10_000);
        }

        @Test
        @DisplayName("默认最大字段大小为1MB")
        void defaultMaxFieldSize() {
            assertThat(CsvConfig.DEFAULT.maxFieldSize()).isEqualTo(1_048_576);
        }

        @Test
        @DisplayName("默认不启用公式保护")
        void defaultFormulaProtection() {
            assertThat(CsvConfig.DEFAULT.formulaProtection()).isFalse();
        }

        @Test
        @DisplayName("默认null字符串为空字符串")
        void defaultNullString() {
            assertThat(CsvConfig.DEFAULT.nullString()).isEmpty();
        }
    }

    @Nested
    @DisplayName("自定义配置")
    class CustomConfig {

        @Test
        @DisplayName("自定义分隔符")
        void customDelimiter() {
            CsvConfig config = CsvConfig.builder().delimiter(';').build();
            assertThat(config.delimiter()).isEqualTo(';');
        }

        @Test
        @DisplayName("自定义字符集")
        void customCharset() {
            CsvConfig config = CsvConfig.builder()
                    .charset(StandardCharsets.ISO_8859_1)
                    .build();
            assertThat(config.charset()).isEqualTo(StandardCharsets.ISO_8859_1);
        }

        @Test
        @DisplayName("自定义所有字段")
        void customAllFields() {
            CsvConfig config = CsvConfig.builder()
                    .delimiter('\t')
                    .quoteChar('\'')
                    .escapeChar('\\')
                    .lineSeparator("\n")
                    .charset(StandardCharsets.US_ASCII)
                    .hasHeader(false)
                    .trimFields(true)
                    .skipEmptyRows(true)
                    .maxRows(100)
                    .maxColumns(50)
                    .maxFieldSize(4096)
                    .formulaProtection(true)
                    .nullString("NULL")
                    .build();

            assertThat(config.delimiter()).isEqualTo('\t');
            assertThat(config.quoteChar()).isEqualTo('\'');
            assertThat(config.escapeChar()).isEqualTo('\\');
            assertThat(config.lineSeparator()).isEqualTo("\n");
            assertThat(config.charset()).isEqualTo(StandardCharsets.US_ASCII);
            assertThat(config.hasHeader()).isFalse();
            assertThat(config.trimFields()).isTrue();
            assertThat(config.skipEmptyRows()).isTrue();
            assertThat(config.maxRows()).isEqualTo(100);
            assertThat(config.maxColumns()).isEqualTo(50);
            assertThat(config.maxFieldSize()).isEqualTo(4096);
            assertThat(config.formulaProtection()).isTrue();
            assertThat(config.nullString()).isEqualTo("NULL");
        }
    }

    @Nested
    @DisplayName("配置验证")
    class Validation {

        @Test
        @DisplayName("maxRows为零时抛出异常")
        void maxRowsZero() {
            assertThatThrownBy(() -> CsvConfig.builder().maxRows(0).build())
                    .isInstanceOf(OpenCsvException.class)
                    .hasMessageContaining("maxRows");
        }

        @Test
        @DisplayName("maxRows为负数时抛出异常")
        void maxRowsNegative() {
            assertThatThrownBy(() -> CsvConfig.builder().maxRows(-1).build())
                    .isInstanceOf(OpenCsvException.class)
                    .hasMessageContaining("maxRows");
        }

        @Test
        @DisplayName("maxColumns为零时抛出异常")
        void maxColumnsZero() {
            assertThatThrownBy(() -> CsvConfig.builder().maxColumns(0).build())
                    .isInstanceOf(OpenCsvException.class)
                    .hasMessageContaining("maxColumns");
        }

        @Test
        @DisplayName("maxFieldSize为零时抛出异常")
        void maxFieldSizeZero() {
            assertThatThrownBy(() -> CsvConfig.builder().maxFieldSize(0).build())
                    .isInstanceOf(OpenCsvException.class)
                    .hasMessageContaining("maxFieldSize");
        }

        @Test
        @DisplayName("分隔符和引用字符相同时抛出异常")
        void delimiterSameAsQuoteChar() {
            assertThatThrownBy(() -> CsvConfig.builder()
                    .delimiter('"')
                    .quoteChar('"')
                    .build())
                    .isInstanceOf(OpenCsvException.class)
                    .hasMessageContaining("delimiter")
                    .hasMessageContaining("quoteChar");
        }

        @Test
        @DisplayName("lineSeparator为null时抛出异常")
        void lineSeparatorNull() {
            assertThatThrownBy(() -> CsvConfig.builder().lineSeparator(null).build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("charset为null时抛出异常")
        void charsetNull() {
            assertThatThrownBy(() -> CsvConfig.builder().charset(null).build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("nullString为null时抛出异常")
        void nullStringNull() {
            assertThatThrownBy(() -> CsvConfig.builder().nullString(null).build())
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("equals和hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("相同配置相等")
        void sameConfigEquals() {
            CsvConfig a = CsvConfig.builder().delimiter(';').build();
            CsvConfig b = CsvConfig.builder().delimiter(';').build();
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不同配置不相等")
        void differentConfigNotEquals() {
            CsvConfig a = CsvConfig.builder().delimiter(';').build();
            CsvConfig b = CsvConfig.builder().delimiter('\t').build();
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("与自身相等")
        void selfEquals() {
            CsvConfig config = CsvConfig.DEFAULT;
            assertThat(config).isEqualTo(config);
        }

        @Test
        @DisplayName("与null不相等")
        void notEqualsNull() {
            assertThat(CsvConfig.DEFAULT).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("toString包含关键信息")
        void toStringContainsKeyInfo() {
            String str = CsvConfig.DEFAULT.toString();
            assertThat(str).contains("CsvConfig");
            assertThat(str).contains("delimiter");
            assertThat(str).contains("hasHeader");
        }
    }
}
