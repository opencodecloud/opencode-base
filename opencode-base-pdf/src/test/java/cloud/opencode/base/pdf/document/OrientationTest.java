package cloud.opencode.base.pdf.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Orientation 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("Orientation 测试")
class OrientationTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含 PORTRAIT 和 LANDSCAPE")
        void testAllOrientationsExist() {
            assertThat(Orientation.values()).containsExactly(
                Orientation.PORTRAIT,
                Orientation.LANDSCAPE
            );
        }

        @Test
        @DisplayName("valueOf 方法正常工作")
        void testValueOf() {
            assertThat(Orientation.valueOf("PORTRAIT")).isEqualTo(Orientation.PORTRAIT);
            assertThat(Orientation.valueOf("LANDSCAPE")).isEqualTo(Orientation.LANDSCAPE);
        }
    }

    @Nested
    @DisplayName("apply 方法测试")
    class ApplyTests {

        @Test
        @DisplayName("PORTRAIT 保持原始尺寸")
        void testPortraitApply() {
            float[] dimensions = Orientation.PORTRAIT.apply(595f, 842f);

            assertThat(dimensions).hasSize(2);
            assertThat(dimensions[0]).isEqualTo(595f); // width unchanged
            assertThat(dimensions[1]).isEqualTo(842f); // height unchanged
        }

        @Test
        @DisplayName("LANDSCAPE 交换宽高")
        void testLandscapeApply() {
            float[] dimensions = Orientation.LANDSCAPE.apply(595f, 842f);

            assertThat(dimensions).hasSize(2);
            assertThat(dimensions[0]).isEqualTo(842f); // width becomes original height
            assertThat(dimensions[1]).isEqualTo(595f); // height becomes original width
        }

        @Test
        @DisplayName("PORTRAIT 与 A4 配合使用")
        void testPortraitWithA4() {
            float[] dimensions = Orientation.PORTRAIT.apply(
                PageSize.A4.getWidth(),
                PageSize.A4.getHeight()
            );

            assertThat(dimensions[0]).isEqualTo(595f);
            assertThat(dimensions[1]).isEqualTo(842f);
        }

        @Test
        @DisplayName("LANDSCAPE 与 A4 配合使用")
        void testLandscapeWithA4() {
            float[] dimensions = Orientation.LANDSCAPE.apply(
                PageSize.A4.getWidth(),
                PageSize.A4.getHeight()
            );

            assertThat(dimensions[0]).isEqualTo(842f);
            assertThat(dimensions[1]).isEqualTo(595f);
        }

        @Test
        @DisplayName("PORTRAIT 保持正方形不变")
        void testPortraitWithSquare() {
            float[] dimensions = Orientation.PORTRAIT.apply(500f, 500f);

            assertThat(dimensions[0]).isEqualTo(500f);
            assertThat(dimensions[1]).isEqualTo(500f);
        }

        @Test
        @DisplayName("LANDSCAPE 正方形交换后相同")
        void testLandscapeWithSquare() {
            float[] dimensions = Orientation.LANDSCAPE.apply(500f, 500f);

            assertThat(dimensions[0]).isEqualTo(500f);
            assertThat(dimensions[1]).isEqualTo(500f);
        }

        @Test
        @DisplayName("使用零值")
        void testWithZeroValues() {
            float[] portrait = Orientation.PORTRAIT.apply(0f, 0f);
            float[] landscape = Orientation.LANDSCAPE.apply(0f, 0f);

            assertThat(portrait[0]).isEqualTo(0f);
            assertThat(portrait[1]).isEqualTo(0f);
            assertThat(landscape[0]).isEqualTo(0f);
            assertThat(landscape[1]).isEqualTo(0f);
        }

        @Test
        @DisplayName("使用负值")
        void testWithNegativeValues() {
            // -100 > -200, so PORTRAIT swaps them: returns {height, width}
            float[] portrait = Orientation.PORTRAIT.apply(-100f, -200f);

            assertThat(portrait[0]).isEqualTo(-200f);
            assertThat(portrait[1]).isEqualTo(-100f);
        }
    }

    @Nested
    @DisplayName("枚举特性测试")
    class EnumFeatureTests {

        @Test
        @DisplayName("ordinal 值正确")
        void testOrdinal() {
            assertThat(Orientation.PORTRAIT.ordinal()).isEqualTo(0);
            assertThat(Orientation.LANDSCAPE.ordinal()).isEqualTo(1);
        }

        @Test
        @DisplayName("name 方法返回正确名称")
        void testName() {
            assertThat(Orientation.PORTRAIT.name()).isEqualTo("PORTRAIT");
            assertThat(Orientation.LANDSCAPE.name()).isEqualTo("LANDSCAPE");
        }

        @Test
        @DisplayName("toString 返回名称")
        void testToString() {
            assertThat(Orientation.PORTRAIT.toString()).isEqualTo("PORTRAIT");
            assertThat(Orientation.LANDSCAPE.toString()).isEqualTo("LANDSCAPE");
        }
    }
}
