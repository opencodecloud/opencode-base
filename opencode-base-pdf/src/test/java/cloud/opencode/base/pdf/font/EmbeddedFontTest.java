package cloud.opencode.base.pdf.font;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * EmbeddedFont 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("EmbeddedFont 测试")
class EmbeddedFontTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("fromFile 创建嵌入字体")
        void testFromFile() {
            Path fontPath = Path.of("/fonts/myfont.ttf");
            EmbeddedFont font = EmbeddedFont.fromFile("MyFont", fontPath);

            assertThat(font).isNotNull();
            assertThat(font.getName()).isEqualTo("MyFont");
            assertThat(font.getFontPath()).isEqualTo(fontPath);
        }

        @Test
        @DisplayName("fromBytes 创建嵌入字体")
        void testFromBytes() {
            byte[] data = new byte[]{1, 2, 3, 4};
            EmbeddedFont font = EmbeddedFont.fromBytes("ByteFont", data, EmbeddedFont.FontType.TRUETYPE);

            assertThat(font).isNotNull();
            assertThat(font.getName()).isEqualTo("ByteFont");
            assertThat(font.getFontData()).isEqualTo(data);
            assertThat(font.getType()).isEqualTo(EmbeddedFont.FontType.TRUETYPE);
        }

        @Test
        @DisplayName("fromFile null 名称抛出异常")
        void testFromFileNullName() {
            assertThatThrownBy(() -> EmbeddedFont.fromFile(null, Path.of("/test.ttf")))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fromBytes null 名称抛出异常")
        void testFromBytesNullName() {
            assertThatThrownBy(() -> EmbeddedFont.fromBytes(null, new byte[]{1}, EmbeddedFont.FontType.TRUETYPE))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("FontType 枚举测试")
    class FontTypeTests {

        @Test
        @DisplayName("包含所有字体类型")
        void testAllFontTypes() {
            assertThat(EmbeddedFont.FontType.values()).containsExactly(
                EmbeddedFont.FontType.TRUETYPE,
                EmbeddedFont.FontType.OPENTYPE,
                EmbeddedFont.FontType.TRUETYPE_COLLECTION
            );
        }

        @Test
        @DisplayName("valueOf 方法正常工作")
        void testValueOf() {
            assertThat(EmbeddedFont.FontType.valueOf("TRUETYPE")).isEqualTo(EmbeddedFont.FontType.TRUETYPE);
            assertThat(EmbeddedFont.FontType.valueOf("OPENTYPE")).isEqualTo(EmbeddedFont.FontType.OPENTYPE);
        }
    }

    @Nested
    @DisplayName("PdfFont 接口测试")
    class PdfFontInterfaceTests {

        @Test
        @DisplayName("实现 PdfFont 接口")
        void testImplementsPdfFont() {
            EmbeddedFont font = EmbeddedFont.fromFile("Test", Path.of("/test.ttf"));

            assertThat(font).isInstanceOf(PdfFont.class);
        }

        @Test
        @DisplayName("getName 返回设置的名称")
        void testGetName() {
            EmbeddedFont font = EmbeddedFont.fromFile("CustomFont", Path.of("/test.ttf"));

            assertThat(font.getName()).isEqualTo("CustomFont");
        }

        @Test
        @DisplayName("getPdfName 返回名称")
        void testGetPdfName() {
            EmbeddedFont font = EmbeddedFont.fromFile("CustomFont", Path.of("/test.ttf"));

            assertThat(font.getPdfName()).isEqualTo("CustomFont");
        }

        @Test
        @DisplayName("isEmbedded 返回 true")
        void testIsEmbedded() {
            EmbeddedFont font = EmbeddedFont.fromFile("Test", Path.of("/test.ttf"));

            assertThat(font.isEmbedded()).isTrue();
        }
    }

    @Nested
    @DisplayName("Getter 方法测试")
    class GetterTests {

        @Test
        @DisplayName("getFontPath 返回设置的路径")
        void testGetFontPath() {
            Path path = Path.of("/fonts/custom.ttf");
            EmbeddedFont font = EmbeddedFont.fromFile("Custom", path);

            assertThat(font.getFontPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("getFontData 返回设置的数据")
        void testGetFontData() {
            byte[] data = new byte[]{10, 20, 30};
            EmbeddedFont font = EmbeddedFont.fromBytes("Data", data, EmbeddedFont.FontType.OPENTYPE);

            assertThat(font.getFontData()).isEqualTo(data);
        }

        @Test
        @DisplayName("getType 返回设置的类型")
        void testGetType() {
            EmbeddedFont font = EmbeddedFont.fromBytes(
                "Test", new byte[]{1}, EmbeddedFont.FontType.TRUETYPE_COLLECTION);

            assertThat(font.getType()).isEqualTo(EmbeddedFont.FontType.TRUETYPE_COLLECTION);
        }

        @Test
        @DisplayName("getFontPath 从字节创建时返回 null")
        void testGetFontPathFromBytes() {
            EmbeddedFont font = EmbeddedFont.fromBytes("Test", new byte[]{1}, EmbeddedFont.FontType.TRUETYPE);

            assertThat(font.getFontPath()).isNull();
        }

        @Test
        @DisplayName("getFontData 从路径创建时返回 null")
        void testGetFontDataFromPath() {
            EmbeddedFont font = EmbeddedFont.fromFile("Test", Path.of("/test.ttf"));

            assertThat(font.getFontData()).isNull();
        }
    }

    @Nested
    @DisplayName("字体类型检测测试")
    class FontTypeDetectionTests {

        @Test
        @DisplayName("TTF 文件检测为 TRUETYPE")
        void testTtfDetection() {
            EmbeddedFont font = EmbeddedFont.fromFile("Test", Path.of("/fonts/test.ttf"));

            assertThat(font.getType()).isEqualTo(EmbeddedFont.FontType.TRUETYPE);
        }

        @Test
        @DisplayName("OTF 文件检测为 OPENTYPE")
        void testOtfDetection() {
            EmbeddedFont font = EmbeddedFont.fromFile("Test", Path.of("/fonts/test.otf"));

            assertThat(font.getType()).isEqualTo(EmbeddedFont.FontType.OPENTYPE);
        }

        @Test
        @DisplayName("TTC 文件检测为 TRUETYPE_COLLECTION")
        void testTtcDetection() {
            EmbeddedFont font = EmbeddedFont.fromFile("Test", Path.of("/fonts/test.ttc"));

            assertThat(font.getType()).isEqualTo(EmbeddedFont.FontType.TRUETYPE_COLLECTION);
        }

        @Test
        @DisplayName("未知扩展名默认为 TRUETYPE")
        void testUnknownExtension() {
            EmbeddedFont font = EmbeddedFont.fromFile("Test", Path.of("/fonts/test.xyz"));

            assertThat(font.getType()).isEqualTo(EmbeddedFont.FontType.TRUETYPE);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("空字节数组")
        void testEmptyByteArray() {
            byte[] empty = new byte[0];
            EmbeddedFont font = EmbeddedFont.fromBytes("Empty", empty, EmbeddedFont.FontType.TRUETYPE);

            assertThat(font.getFontData()).isEmpty();
        }

        @Test
        @DisplayName("getFontData 返回副本")
        void testGetFontDataReturnsCopy() {
            byte[] data = new byte[]{1, 2, 3};
            EmbeddedFont font = EmbeddedFont.fromBytes("Test", data, EmbeddedFont.FontType.TRUETYPE);

            byte[] retrieved = font.getFontData();
            retrieved[0] = 99;

            assertThat(font.getFontData()[0]).isEqualTo((byte) 1);
        }

        @Test
        @DisplayName("大小写不敏感的扩展名检测")
        void testCaseInsensitiveExtension() {
            EmbeddedFont fontTTF = EmbeddedFont.fromFile("Test", Path.of("/fonts/test.TTF"));
            EmbeddedFont fontOTF = EmbeddedFont.fromFile("Test", Path.of("/fonts/test.OTF"));

            assertThat(fontTTF.getType()).isEqualTo(EmbeddedFont.FontType.TRUETYPE);
            assertThat(fontOTF.getType()).isEqualTo(EmbeddedFont.FontType.OPENTYPE);
        }
    }

    @Nested
    @DisplayName("final 类测试")
    class FinalClassTests {

        @Test
        @DisplayName("EmbeddedFont 是 final 类")
        void testIsFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(EmbeddedFont.class.getModifiers())).isTrue();
        }
    }
}
