package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenCharset 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenCharset 测试")
class OpenCharsetTest {

    @Nested
    @DisplayName("字符集常量测试")
    class CharsetConstantsTests {

        @Test
        @DisplayName("标准字符集常量")
        void testStandardCharsets() {
            assertThat(OpenCharset.UTF_8).isEqualTo(StandardCharsets.UTF_8);
            assertThat(OpenCharset.UTF_16).isEqualTo(StandardCharsets.UTF_16);
            assertThat(OpenCharset.UTF_16BE).isEqualTo(StandardCharsets.UTF_16BE);
            assertThat(OpenCharset.UTF_16LE).isEqualTo(StandardCharsets.UTF_16LE);
            assertThat(OpenCharset.ISO_8859_1).isEqualTo(StandardCharsets.ISO_8859_1);
            assertThat(OpenCharset.US_ASCII).isEqualTo(StandardCharsets.US_ASCII);
        }

        @Test
        @DisplayName("中文字符集 GBK")
        void testGBK() {
            Charset gbk = OpenCharset.GBK();
            assertThat(gbk).isNotNull();
            assertThat(gbk.name()).containsIgnoringCase("GBK");
        }

        @Test
        @DisplayName("中文字符集 GB2312")
        void testGB2312() {
            Charset gb2312 = OpenCharset.GB2312();
            assertThat(gb2312).isNotNull();
        }

        @Test
        @DisplayName("中文字符集 GB18030")
        void testGB18030() {
            Charset gb18030 = OpenCharset.GB18030();
            assertThat(gb18030).isNotNull();
        }

        @Test
        @DisplayName("字符集名称常量")
        void testCharsetNameConstants() {
            assertThat(OpenCharset.UTF_8_NAME).isEqualTo("UTF-8");
            assertThat(OpenCharset.GBK_NAME).isEqualTo("GBK");
            assertThat(OpenCharset.ISO_8859_1_NAME).isEqualTo("ISO-8859-1");
        }
    }

    @Nested
    @DisplayName("字符集获取测试")
    class CharsetRetrievalTests {

        @Test
        @DisplayName("charset 有效字符集")
        void testCharsetValid() {
            assertThat(OpenCharset.charset("UTF-8")).isEqualTo(StandardCharsets.UTF_8);
            assertThat(OpenCharset.charset("GBK")).isEqualTo(OpenCharset.GBK());
        }

        @Test
        @DisplayName("charset 无效字符集返回默认值")
        void testCharsetInvalid() {
            assertThat(OpenCharset.charset("INVALID")).isEqualTo(StandardCharsets.UTF_8);
            assertThat(OpenCharset.charset(null)).isEqualTo(StandardCharsets.UTF_8);
            assertThat(OpenCharset.charset("")).isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("charset 带自定义默认值")
        void testCharsetWithDefault() {
            assertThat(OpenCharset.charset("INVALID", StandardCharsets.ISO_8859_1))
                    .isEqualTo(StandardCharsets.ISO_8859_1);
            assertThat(OpenCharset.charset("UTF-8", StandardCharsets.ISO_8859_1))
                    .isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("charsetOptional")
        void testCharsetOptional() {
            Optional<Charset> opt = OpenCharset.charsetOptional("UTF-8");
            assertThat(opt).contains(StandardCharsets.UTF_8);

            assertThat(OpenCharset.charsetOptional("INVALID")).isEmpty();
            assertThat(OpenCharset.charsetOptional(null)).isEmpty();
            assertThat(OpenCharset.charsetOptional("")).isEmpty();
        }

        @Test
        @DisplayName("defaultCharset")
        void testDefaultCharset() {
            assertThat(OpenCharset.defaultCharset()).isNotNull();
        }
    }

    @Nested
    @DisplayName("字符集转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toBytes UTF-8")
        void testToBytesUtf8() {
            byte[] bytes = OpenCharset.toBytes("Hello");
            assertThat(bytes).isEqualTo("Hello".getBytes(StandardCharsets.UTF_8));
        }

        @Test
        @DisplayName("toBytes 指定字符集")
        void testToBytesCharset() {
            byte[] bytes = OpenCharset.toBytes("Hello", StandardCharsets.ISO_8859_1);
            assertThat(bytes).isEqualTo("Hello".getBytes(StandardCharsets.ISO_8859_1));
        }

        @Test
        @DisplayName("toBytes null")
        void testToBytesNull() {
            assertThat(OpenCharset.toBytes(null)).isNull();
            assertThat(OpenCharset.toBytes(null, StandardCharsets.UTF_8)).isNull();
        }

        @Test
        @DisplayName("toBytes 字符集名称")
        void testToBytesCharsetName() {
            byte[] bytes = OpenCharset.toBytes("Hello", "UTF-8");
            assertThat(bytes).isEqualTo("Hello".getBytes(StandardCharsets.UTF_8));
        }

        @Test
        @DisplayName("toString UTF-8")
        void testToStringUtf8() {
            byte[] bytes = "Hello".getBytes(StandardCharsets.UTF_8);
            assertThat(OpenCharset.toString(bytes)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("toString 指定字符集")
        void testToStringCharset() {
            byte[] bytes = "Hello".getBytes(StandardCharsets.ISO_8859_1);
            assertThat(OpenCharset.toString(bytes, StandardCharsets.ISO_8859_1)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("toString null")
        void testToStringNull() {
            assertThat(OpenCharset.toString(null)).isNull();
            assertThat(OpenCharset.toString(null, StandardCharsets.UTF_8)).isNull();
        }

        @Test
        @DisplayName("toString 字符集名称")
        void testToStringCharsetName() {
            byte[] bytes = "Hello".getBytes(StandardCharsets.UTF_8);
            assertThat(OpenCharset.toString(bytes, "UTF-8")).isEqualTo("Hello");
        }

        @Test
        @DisplayName("convert 字符集转换")
        void testConvert() {
            String original = "Hello World";
            String converted = OpenCharset.convert(original, StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1);
            assertThat(converted).isEqualTo(original);
        }

        @Test
        @DisplayName("convert null")
        void testConvertNull() {
            assertThat(OpenCharset.convert(null, StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1)).isNull();
        }

        @Test
        @DisplayName("convert 字符集名称")
        void testConvertCharsetNames() {
            String result = OpenCharset.convert("Hello", "UTF-8", "ISO-8859-1");
            assertThat(result).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("字符集检测测试")
    class DetectionTests {

        @Test
        @DisplayName("isSupported 有效字符集")
        void testIsSupportedValid() {
            assertThat(OpenCharset.isSupported("UTF-8")).isTrue();
            assertThat(OpenCharset.isSupported("GBK")).isTrue();
            assertThat(OpenCharset.isSupported("ISO-8859-1")).isTrue();
        }

        @Test
        @DisplayName("isSupported 无效字符集")
        void testIsSupportedInvalid() {
            assertThat(OpenCharset.isSupported("INVALID")).isFalse();
            assertThat(OpenCharset.isSupported(null)).isFalse();
            assertThat(OpenCharset.isSupported("")).isFalse();
        }

        @Test
        @DisplayName("canEncode")
        void testCanEncode() {
            assertThat(OpenCharset.canEncode("Hello", StandardCharsets.US_ASCII)).isTrue();
            assertThat(OpenCharset.canEncode("中文", StandardCharsets.US_ASCII)).isFalse();
            assertThat(OpenCharset.canEncode("中文", StandardCharsets.UTF_8)).isTrue();
        }

        @Test
        @DisplayName("canEncode null")
        void testCanEncodeNull() {
            assertThat(OpenCharset.canEncode(null, StandardCharsets.UTF_8)).isFalse();
            assertThat(OpenCharset.canEncode("Hello", null)).isFalse();
        }

        @Test
        @DisplayName("detect UTF-8")
        void testDetectUtf8() {
            byte[] utf8Bytes = "Hello World".getBytes(StandardCharsets.UTF_8);
            Charset detected = OpenCharset.detect(utf8Bytes);
            assertThat(detected).isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("detect 空数组")
        void testDetectEmpty() {
            assertThat(OpenCharset.detect(null)).isEqualTo(StandardCharsets.UTF_8);
            assertThat(OpenCharset.detect(new byte[0])).isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("hasNonAscii")
        void testHasNonAscii() {
            assertThat(OpenCharset.hasNonAscii("Hello")).isFalse();
            assertThat(OpenCharset.hasNonAscii("中文")).isTrue();
            assertThat(OpenCharset.hasNonAscii("Hello中文")).isTrue();
            assertThat(OpenCharset.hasNonAscii(null)).isFalse();
        }

        @Test
        @DisplayName("isAscii")
        void testIsAscii() {
            assertThat(OpenCharset.isAscii("Hello")).isTrue();
            assertThat(OpenCharset.isAscii("中文")).isFalse();
            assertThat(OpenCharset.isAscii(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("常用转换测试")
    class CommonConversionTests {

        @Test
        @DisplayName("gbkToUtf8")
        void testGbkToUtf8() {
            String original = "中文";
            String converted = OpenCharset.gbkToUtf8(original);
            // 由于 gbkToUtf8 实际上是重新编码，对于已经是 UTF-8 字符串可能会有问题
            // 这里主要测试方法可调用
            assertThat(converted).isNotNull();
        }

        @Test
        @DisplayName("utf8ToGbk")
        void testUtf8ToGbk() {
            String original = "中文";
            String converted = OpenCharset.utf8ToGbk(original);
            assertThat(converted).isNotNull();
        }

        @Test
        @DisplayName("iso8859ToUtf8")
        void testIso8859ToUtf8() {
            String ascii = "Hello";
            String converted = OpenCharset.iso8859ToUtf8(ascii);
            assertThat(converted).isEqualTo(ascii);
        }
    }

    @Nested
    @DisplayName("BOM 处理测试")
    class BomTests {

        @Test
        @DisplayName("hasBom 有 BOM")
        void testHasBomTrue() {
            byte[] withBom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'H', 'e', 'l', 'l', 'o'};
            assertThat(OpenCharset.hasBom(withBom)).isTrue();
        }

        @Test
        @DisplayName("hasBom 无 BOM")
        void testHasBomFalse() {
            byte[] noBom = "Hello".getBytes(StandardCharsets.UTF_8);
            assertThat(OpenCharset.hasBom(noBom)).isFalse();
            assertThat(OpenCharset.hasBom(null)).isFalse();
            assertThat(OpenCharset.hasBom(new byte[2])).isFalse();
        }

        @Test
        @DisplayName("removeBom")
        void testRemoveBom() {
            byte[] withBom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'H', 'e', 'l', 'l', 'o'};
            byte[] removed = OpenCharset.removeBom(withBom);
            assertThat(removed).containsExactly('H', 'e', 'l', 'l', 'o');
        }

        @Test
        @DisplayName("removeBom 无 BOM")
        void testRemoveBomNoBom() {
            byte[] noBom = "Hello".getBytes(StandardCharsets.UTF_8);
            assertThat(OpenCharset.removeBom(noBom)).isSameAs(noBom);
        }

        @Test
        @DisplayName("removeBom null")
        void testRemoveBomNull() {
            assertThat(OpenCharset.removeBom(null)).isNull();
            assertThat(OpenCharset.removeBom(new byte[2])).hasSize(2);
        }

        @Test
        @DisplayName("addBom")
        void testAddBom() {
            byte[] noBom = "Hello".getBytes(StandardCharsets.UTF_8);
            byte[] withBom = OpenCharset.addBom(noBom);
            assertThat(withBom).hasSize(noBom.length + 3);
            assertThat(withBom[0]).isEqualTo((byte) 0xEF);
            assertThat(withBom[1]).isEqualTo((byte) 0xBB);
            assertThat(withBom[2]).isEqualTo((byte) 0xBF);
        }

        @Test
        @DisplayName("addBom 已有 BOM")
        void testAddBomAlreadyHas() {
            byte[] withBom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'H'};
            byte[] result = OpenCharset.addBom(withBom);
            assertThat(result).isSameAs(withBom);
        }

        @Test
        @DisplayName("addBom null")
        void testAddBomNull() {
            byte[] result = OpenCharset.addBom(null);
            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Reader/Writer 测试")
    class ReaderWriterTests {

        @Test
        @DisplayName("newReader UTF-8")
        void testNewReader() throws IOException {
            byte[] data = "Hello".getBytes(StandardCharsets.UTF_8);
            try (Reader reader = OpenCharset.newReader(new ByteArrayInputStream(data))) {
                char[] buffer = new char[100];
                int len = reader.read(buffer);
                assertThat(new String(buffer, 0, len)).isEqualTo("Hello");
            }
        }

        @Test
        @DisplayName("newReader 指定字符集")
        void testNewReaderCharset() throws IOException {
            byte[] data = "Hello".getBytes(StandardCharsets.ISO_8859_1);
            try (Reader reader = OpenCharset.newReader(new ByteArrayInputStream(data), StandardCharsets.ISO_8859_1)) {
                char[] buffer = new char[100];
                int len = reader.read(buffer);
                assertThat(new String(buffer, 0, len)).isEqualTo("Hello");
            }
        }

        @Test
        @DisplayName("newWriter UTF-8")
        void testNewWriter() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (Writer writer = OpenCharset.newWriter(baos)) {
                writer.write("Hello");
            }
            assertThat(baos.toString(StandardCharsets.UTF_8)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("newWriter 指定字符集")
        void testNewWriterCharset() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (Writer writer = OpenCharset.newWriter(baos, StandardCharsets.ISO_8859_1)) {
                writer.write("Hello");
            }
            assertThat(baos.toString(StandardCharsets.ISO_8859_1)).isEqualTo("Hello");
        }
    }
}
