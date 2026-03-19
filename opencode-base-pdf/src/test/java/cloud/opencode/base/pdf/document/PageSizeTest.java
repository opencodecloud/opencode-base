package cloud.opencode.base.pdf.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * PageSize 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PageSize 测试")
class PageSizeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含所有标准页面大小")
        void testAllPageSizesExist() {
            assertThat(PageSize.values()).containsExactly(
                PageSize.A0, PageSize.A1, PageSize.A2, PageSize.A3, PageSize.A4, PageSize.A5, PageSize.A6,
                PageSize.B4, PageSize.B5,
                PageSize.LETTER, PageSize.LEGAL, PageSize.TABLOID, PageSize.EXECUTIVE,
                PageSize.POSTCARD
            );
        }

        @Test
        @DisplayName("valueOf 方法正常工作")
        void testValueOf() {
            assertThat(PageSize.valueOf("A4")).isEqualTo(PageSize.A4);
            assertThat(PageSize.valueOf("LETTER")).isEqualTo(PageSize.LETTER);
        }
    }

    @Nested
    @DisplayName("A系列尺寸测试")
    class ASeriesTests {

        @Test
        @DisplayName("A4 尺寸正确 (595 x 842 点)")
        void testA4Size() {
            assertThat(PageSize.A4.getWidth()).isEqualTo(595f);
            assertThat(PageSize.A4.getHeight()).isEqualTo(842f);
        }

        @Test
        @DisplayName("A3 尺寸正确")
        void testA3Size() {
            assertThat(PageSize.A3.getWidth()).isEqualTo(842f);
            assertThat(PageSize.A3.getHeight()).isEqualTo(1191f);
        }

        @Test
        @DisplayName("A5 尺寸正确")
        void testA5Size() {
            assertThat(PageSize.A5.getWidth()).isEqualTo(420f);
            assertThat(PageSize.A5.getHeight()).isEqualTo(595f);
        }

        @Test
        @DisplayName("A0 尺寸正确")
        void testA0Size() {
            assertThat(PageSize.A0.getWidth()).isEqualTo(2384f);
            assertThat(PageSize.A0.getHeight()).isEqualTo(3370f);
        }

        @Test
        @DisplayName("A1 尺寸正确")
        void testA1Size() {
            assertThat(PageSize.A1.getWidth()).isEqualTo(1684f);
            assertThat(PageSize.A1.getHeight()).isEqualTo(2384f);
        }

        @Test
        @DisplayName("A2 尺寸正确")
        void testA2Size() {
            assertThat(PageSize.A2.getWidth()).isEqualTo(1191f);
            assertThat(PageSize.A2.getHeight()).isEqualTo(1684f);
        }

        @Test
        @DisplayName("A6 尺寸正确")
        void testA6Size() {
            assertThat(PageSize.A6.getWidth()).isEqualTo(298f);
            assertThat(PageSize.A6.getHeight()).isEqualTo(420f);
        }
    }

    @Nested
    @DisplayName("B系列尺寸测试")
    class BSeriesTests {

        @Test
        @DisplayName("B4 尺寸正确")
        void testB4Size() {
            assertThat(PageSize.B4.getWidth()).isEqualTo(709f);
            assertThat(PageSize.B4.getHeight()).isEqualTo(1001f);
        }

        @Test
        @DisplayName("B5 尺寸正确")
        void testB5Size() {
            assertThat(PageSize.B5.getWidth()).isEqualTo(499f);
            assertThat(PageSize.B5.getHeight()).isEqualTo(709f);
        }
    }

    @Nested
    @DisplayName("美国尺寸测试")
    class USSeriesTests {

        @Test
        @DisplayName("Letter 尺寸正确 (612 x 792 点)")
        void testLetterSize() {
            assertThat(PageSize.LETTER.getWidth()).isEqualTo(612f);
            assertThat(PageSize.LETTER.getHeight()).isEqualTo(792f);
        }

        @Test
        @DisplayName("Legal 尺寸正确")
        void testLegalSize() {
            assertThat(PageSize.LEGAL.getWidth()).isEqualTo(612f);
            assertThat(PageSize.LEGAL.getHeight()).isEqualTo(1008f);
        }

        @Test
        @DisplayName("Tabloid 尺寸正确")
        void testTabloidSize() {
            assertThat(PageSize.TABLOID.getWidth()).isEqualTo(792f);
            assertThat(PageSize.TABLOID.getHeight()).isEqualTo(1224f);
        }

        @Test
        @DisplayName("Executive 尺寸正确")
        void testExecutiveSize() {
            assertThat(PageSize.EXECUTIVE.getWidth()).isEqualTo(522f);
            assertThat(PageSize.EXECUTIVE.getHeight()).isEqualTo(756f);
        }
    }

    @Nested
    @DisplayName("其他尺寸测试")
    class OtherSizesTests {

        @Test
        @DisplayName("Postcard 尺寸正确")
        void testPostcardSize() {
            assertThat(PageSize.POSTCARD.getWidth()).isEqualTo(283f);
            assertThat(PageSize.POSTCARD.getHeight()).isEqualTo(420f);
        }
    }

    @Nested
    @DisplayName("单位转换测试")
    class UnitConversionTests {

        @Test
        @DisplayName("A4 毫米转换正确")
        void testA4MillimetersConversion() {
            // A4 应该是 210 x 297 mm
            assertThat(PageSize.A4.getWidthMm()).isCloseTo(210f, within(1f));
            assertThat(PageSize.A4.getHeightMm()).isCloseTo(297f, within(1f));
        }

        @Test
        @DisplayName("Letter 英寸转换正确")
        void testLetterInchesConversion() {
            // Letter 应该是 8.5 x 11 inches
            assertThat(PageSize.LETTER.getWidthInches()).isCloseTo(8.5f, within(0.01f));
            assertThat(PageSize.LETTER.getHeightInches()).isCloseTo(11f, within(0.01f));
        }

        @ParameterizedTest
        @EnumSource(PageSize.class)
        @DisplayName("所有页面大小的毫米转换")
        void testAllPageSizesMillimeters(PageSize size) {
            assertThat(size.getWidthMm()).isPositive();
            assertThat(size.getHeightMm()).isPositive();
        }

        @ParameterizedTest
        @EnumSource(PageSize.class)
        @DisplayName("所有页面大小的英寸转换")
        void testAllPageSizesInches(PageSize size) {
            assertThat(size.getWidthInches()).isPositive();
            assertThat(size.getHeightInches()).isPositive();
        }
    }

    @Nested
    @DisplayName("rotate 方法测试")
    class RotateTests {

        @Test
        @DisplayName("rotate 交换宽高")
        void testRotate() {
            float[] rotated = PageSize.A4.rotate();

            assertThat(rotated).hasSize(2);
            assertThat(rotated[0]).isEqualTo(PageSize.A4.getHeight()); // 宽变为原高
            assertThat(rotated[1]).isEqualTo(PageSize.A4.getWidth());  // 高变为原宽
        }

        @ParameterizedTest
        @EnumSource(PageSize.class)
        @DisplayName("所有页面大小的旋转")
        void testRotateAllSizes(PageSize size) {
            float[] rotated = size.rotate();

            assertThat(rotated[0]).isEqualTo(size.getHeight());
            assertThat(rotated[1]).isEqualTo(size.getWidth());
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("custom 创建自定义尺寸")
        void testCustom() {
            float[] custom = PageSize.custom(500f, 700f);

            assertThat(custom).hasSize(2);
            assertThat(custom[0]).isEqualTo(500f);
            assertThat(custom[1]).isEqualTo(700f);
        }

        @Test
        @DisplayName("customMm 从毫米创建")
        void testCustomMm() {
            float[] custom = PageSize.customMm(210f, 297f);

            assertThat(custom).hasSize(2);
            assertThat(custom[0]).isCloseTo(595f, within(1f)); // A4 width
            assertThat(custom[1]).isCloseTo(842f, within(1f)); // A4 height
        }

        @Test
        @DisplayName("customInches 从英寸创建")
        void testCustomInches() {
            float[] custom = PageSize.customInches(8.5f, 11f);

            assertThat(custom).hasSize(2);
            assertThat(custom[0]).isEqualTo(612f); // Letter width
            assertThat(custom[1]).isEqualTo(792f); // Letter height
        }
    }

    @Nested
    @DisplayName("纵横比测试")
    class AspectRatioTests {

        @ParameterizedTest
        @EnumSource(PageSize.class)
        @DisplayName("所有页面高度大于宽度（纵向）")
        void testPortraitOrientation(PageSize size) {
            assertThat(size.getHeight()).isGreaterThan(size.getWidth());
        }
    }
}
