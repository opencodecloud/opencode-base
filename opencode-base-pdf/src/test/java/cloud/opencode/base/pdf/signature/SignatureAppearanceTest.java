package cloud.opencode.base.pdf.signature;

import cloud.opencode.base.pdf.font.PdfFont;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * SignatureAppearance 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("SignatureAppearance 测试")
class SignatureAppearanceTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("defaultAppearance 创建默认外观")
        void testDefaultAppearance() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance();

            assertThat(appearance).isNotNull();
            assertThat(appearance.getFontSize()).isEqualTo(12f);
            assertThat(appearance.getTextColor()).isEqualTo(Color.BLACK);
            assertThat(appearance.isShowSignerName()).isTrue();
            assertThat(appearance.isShowDate()).isTrue();
            assertThat(appearance.isShowReason()).isFalse();
            assertThat(appearance.isShowLocation()).isFalse();
        }

        @Test
        @DisplayName("imageOnly 创建仅图像外观")
        void testImageOnly() {
            Path imagePath = Path.of("/signature.png");
            SignatureAppearance appearance = SignatureAppearance.imageOnly(imagePath);

            assertThat(appearance.getImagePath()).isEqualTo(imagePath);
            assertThat(appearance.isShowSignerName()).isFalse();
            assertThat(appearance.isShowDate()).isFalse();
        }

        @Test
        @DisplayName("textOnly 创建仅文本外观")
        void testTextOnly() {
            SignatureAppearance appearance = SignatureAppearance.textOnly();

            assertThat(appearance).isNotNull();
            assertThat(appearance.getImagePath()).isNull();
            assertThat(appearance.getImageBytes()).isNull();
        }
    }

    @Nested
    @DisplayName("图像设置测试")
    class ImageSettingTests {

        @Test
        @DisplayName("image(Path) 设置图像路径")
        void testImageFromPath() {
            Path imagePath = Path.of("/images/sig.png");
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .image(imagePath);

            assertThat(appearance.getImagePath()).isEqualTo(imagePath);
            assertThat(appearance.getImageBytes()).isNull();
        }

        @Test
        @DisplayName("image(byte[]) 设置图像字节")
        void testImageFromBytes() {
            byte[] imageData = new byte[]{1, 2, 3, 4, 5};
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .image(imageData);

            assertThat(appearance.getImageBytes()).isEqualTo(imageData);
            assertThat(appearance.getImagePath()).isNull();
        }

        @Test
        @DisplayName("image(Path) null 抛出异常")
        void testImagePathNull() {
            assertThatThrownBy(() -> SignatureAppearance.defaultAppearance().image((Path) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("image(byte[]) null 抛出异常")
        void testImageBytesNull() {
            assertThatThrownBy(() -> SignatureAppearance.defaultAppearance().image((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getImageBytes 返回副本")
        void testImageBytesCopy() {
            byte[] original = new byte[]{1, 2, 3};
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance().image(original);

            byte[] retrieved = appearance.getImageBytes();
            retrieved[0] = 99;

            assertThat(appearance.getImageBytes()[0]).isEqualTo((byte) 1);
        }
    }

    @Nested
    @DisplayName("文本设置测试")
    class TextSettingTests {

        @Test
        @DisplayName("description 设置描述")
        void testDescription() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .description("Digitally signed by John Doe");

            assertThat(appearance.getDescription()).isEqualTo("Digitally signed by John Doe");
        }

        @Test
        @DisplayName("font 设置字体")
        void testFont() {
            PdfFont font = PdfFont.helveticaBold();
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .font(font);

            assertThat(appearance.getFont()).isEqualTo(font);
        }

        @Test
        @DisplayName("fontSize 设置字体大小")
        void testFontSize() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .fontSize(14f);

            assertThat(appearance.getFontSize()).isEqualTo(14f);
        }

        @Test
        @DisplayName("fontSize 负值抛出异常")
        void testFontSizeNegative() {
            assertThatThrownBy(() -> SignatureAppearance.defaultAppearance().fontSize(-1f))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Font size must be positive");
        }

        @Test
        @DisplayName("fontSize 零值抛出异常")
        void testFontSizeZero() {
            assertThatThrownBy(() -> SignatureAppearance.defaultAppearance().fontSize(0f))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("textColor 设置文本颜色")
        void testTextColor() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .textColor(Color.BLUE);

            assertThat(appearance.getTextColor()).isEqualTo(Color.BLUE);
        }

        @Test
        @DisplayName("textColor null 抛出异常")
        void testTextColorNull() {
            assertThatThrownBy(() -> SignatureAppearance.defaultAppearance().textColor(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("背景和边框测试")
    class BackgroundBorderTests {

        @Test
        @DisplayName("backgroundColor 设置背景颜色")
        void testBackgroundColor() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .backgroundColor(Color.LIGHT_GRAY);

            assertThat(appearance.getBackgroundColor()).isEqualTo(Color.LIGHT_GRAY);
        }

        @Test
        @DisplayName("backgroundColor 可以为 null")
        void testBackgroundColorNull() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .backgroundColor(null);

            assertThat(appearance.getBackgroundColor()).isNull();
        }

        @Test
        @DisplayName("border 设置边框")
        void testBorder() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .border(2f, Color.RED);

            assertThat(appearance.getBorderWidth()).isEqualTo(2f);
            assertThat(appearance.getBorderColor()).isEqualTo(Color.RED);
        }
    }

    @Nested
    @DisplayName("显示选项测试")
    class ShowOptionsTests {

        @Test
        @DisplayName("showSignerName 设置是否显示签名者")
        void testShowSignerName() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .showSignerName(false);

            assertThat(appearance.isShowSignerName()).isFalse();
        }

        @Test
        @DisplayName("showDate 设置是否显示日期")
        void testShowDate() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .showDate(false);

            assertThat(appearance.isShowDate()).isFalse();
        }

        @Test
        @DisplayName("showReason 设置是否显示原因")
        void testShowReason() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .showReason(true);

            assertThat(appearance.isShowReason()).isTrue();
        }

        @Test
        @DisplayName("showLocation 设置是否显示位置")
        void testShowLocation() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .showLocation(true);

            assertThat(appearance.isShowLocation()).isTrue();
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
                .description("Signed Document")
                .font(PdfFont.timesRoman())
                .fontSize(10f)
                .textColor(Color.DARK_GRAY)
                .backgroundColor(Color.WHITE)
                .border(1f, Color.BLACK)
                .showSignerName(true)
                .showDate(true)
                .showReason(true)
                .showLocation(true);

            assertThat(appearance.getDescription()).isEqualTo("Signed Document");
            assertThat(appearance.getFontSize()).isEqualTo(10f);
            assertThat(appearance.getTextColor()).isEqualTo(Color.DARK_GRAY);
            assertThat(appearance.getBackgroundColor()).isEqualTo(Color.WHITE);
            assertThat(appearance.getBorderWidth()).isEqualTo(1f);
            assertThat(appearance.isShowSignerName()).isTrue();
            assertThat(appearance.isShowDate()).isTrue();
            assertThat(appearance.isShowReason()).isTrue();
            assertThat(appearance.isShowLocation()).isTrue();
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认字体大小为 12")
        void testDefaultFontSize() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance();

            assertThat(appearance.getFontSize()).isEqualTo(12f);
        }

        @Test
        @DisplayName("默认文本颜色为黑色")
        void testDefaultTextColor() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance();

            assertThat(appearance.getTextColor()).isEqualTo(Color.BLACK);
        }

        @Test
        @DisplayName("默认无背景颜色")
        void testDefaultNoBackgroundColor() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance();

            assertThat(appearance.getBackgroundColor()).isNull();
        }

        @Test
        @DisplayName("默认边框宽度为 0")
        void testDefaultBorderWidth() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance();

            assertThat(appearance.getBorderWidth()).isEqualTo(0f);
        }

        @Test
        @DisplayName("默认无字体")
        void testDefaultNoFont() {
            SignatureAppearance appearance = SignatureAppearance.defaultAppearance();

            assertThat(appearance.getFont()).isNull();
        }
    }
}
