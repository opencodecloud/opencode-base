package cloud.opencode.base.image.morphology;

import cloud.opencode.base.image.exception.ImageOperationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * StructuringElement 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("StructuringElement 结构元素测试")
class StructuringElementTest {

    @Nested
    @DisplayName("rect 矩形结构元素测试")
    class RectTests {

        @Test
        @DisplayName("3x3 矩形所有掩码值为 true")
        void rect3x3AllTrue() {
            StructuringElement se = StructuringElement.rect(3, 3);
            assertThat(se.width()).isEqualTo(3);
            assertThat(se.height()).isEqualTo(3);
            assertThat(se.anchorX()).isEqualTo(1);
            assertThat(se.anchorY()).isEqualTo(1);

            boolean[] mask = se.mask();
            assertThat(mask).hasSize(9);
            for (boolean b : mask) {
                assertThat(b).isTrue();
            }
        }

        @Test
        @DisplayName("5x3 矩形所有掩码值为 true")
        void rect5x3AllTrue() {
            StructuringElement se = StructuringElement.rect(5, 3);
            assertThat(se.width()).isEqualTo(5);
            assertThat(se.height()).isEqualTo(3);
            assertThat(se.anchorX()).isEqualTo(2);
            assertThat(se.anchorY()).isEqualTo(1);

            boolean[] mask = se.mask();
            assertThat(mask).hasSize(15);
            for (boolean b : mask) {
                assertThat(b).isTrue();
            }
        }

        @Test
        @DisplayName("1x1 矩形有效")
        void rect1x1Valid() {
            StructuringElement se = StructuringElement.rect(1, 1);
            assertThat(se.width()).isEqualTo(1);
            assertThat(se.height()).isEqualTo(1);
            assertThat(se.mask()).hasSize(1);
            assertThat(se.mask()[0]).isTrue();
        }
    }

    @Nested
    @DisplayName("ellipse 椭圆形结构元素测试")
    class EllipseTests {

        @Test
        @DisplayName("5x5 椭圆形掩码正确")
        void ellipse5x5ShapeCorrect() {
            StructuringElement se = StructuringElement.ellipse(5, 5);
            assertThat(se.width()).isEqualTo(5);
            assertThat(se.height()).isEqualTo(5);

            boolean[] mask = se.mask();
            assertThat(mask).hasSize(25);

            // Center should be true
            assertThat(mask[2 * 5 + 2]).isTrue();

            // Corners of 5x5 should be outside the ellipse
            assertThat(mask[0 * 5 + 0]).isFalse(); // top-left corner
            assertThat(mask[0 * 5 + 4]).isFalse(); // top-right corner
            assertThat(mask[4 * 5 + 0]).isFalse(); // bottom-left corner
            assertThat(mask[4 * 5 + 4]).isFalse(); // bottom-right corner

            // Center row and column should be true
            assertThat(mask[2 * 5 + 0]).isTrue(); // left of center row
            assertThat(mask[2 * 5 + 4]).isTrue(); // right of center row
            assertThat(mask[0 * 5 + 2]).isTrue(); // top of center column
            assertThat(mask[4 * 5 + 2]).isTrue(); // bottom of center column
        }

        @Test
        @DisplayName("3x3 椭圆形掩码正确—十字形加角")
        void ellipse3x3ShapeCorrect() {
            StructuringElement se = StructuringElement.ellipse(3, 3);
            boolean[] mask = se.mask();
            assertThat(mask).hasSize(9);

            // Center is true
            assertThat(mask[1 * 3 + 1]).isTrue();
            // Cross positions are true
            assertThat(mask[0 * 3 + 1]).isTrue();
            assertThat(mask[1 * 3 + 0]).isTrue();
            assertThat(mask[1 * 3 + 2]).isTrue();
            assertThat(mask[2 * 3 + 1]).isTrue();
        }
    }

    @Nested
    @DisplayName("cross 十字形结构元素测试")
    class CrossTests {

        @Test
        @DisplayName("3x3 十字形掩码正确")
        void cross3ShapeCorrect() {
            StructuringElement se = StructuringElement.cross(3);
            assertThat(se.width()).isEqualTo(3);
            assertThat(se.height()).isEqualTo(3);
            assertThat(se.anchorX()).isEqualTo(1);
            assertThat(se.anchorY()).isEqualTo(1);

            boolean[] mask = se.mask();
            // Expected pattern:
            // F T F
            // T T T
            // F T F
            assertThat(mask[0]).isFalse(); // (0,0)
            assertThat(mask[1]).isTrue();  // (1,0) center column
            assertThat(mask[2]).isFalse(); // (2,0)
            assertThat(mask[3]).isTrue();  // (0,1) center row
            assertThat(mask[4]).isTrue();  // (1,1) center
            assertThat(mask[5]).isTrue();  // (2,1) center row
            assertThat(mask[6]).isFalse(); // (0,2)
            assertThat(mask[7]).isTrue();  // (1,2) center column
            assertThat(mask[8]).isFalse(); // (2,2)
        }

        @Test
        @DisplayName("5x5 十字形掩码正确")
        void cross5ShapeCorrect() {
            StructuringElement se = StructuringElement.cross(5);
            boolean[] mask = se.mask();
            assertThat(mask).hasSize(25);

            // Verify center row (y=2) is all true
            for (int x = 0; x < 5; x++) {
                assertThat(mask[2 * 5 + x]).isTrue();
            }
            // Verify center column (x=2) is all true
            for (int y = 0; y < 5; y++) {
                assertThat(mask[y * 5 + 2]).isTrue();
            }
            // Verify corner is false
            assertThat(mask[0 * 5 + 0]).isFalse();
            assertThat(mask[0 * 5 + 4]).isFalse();
        }
    }

    @Nested
    @DisplayName("验证与异常测试")
    class ValidationTests {

        @Test
        @DisplayName("偶数宽度抛出异常")
        void evenWidthThrows() {
            assertThatThrownBy(() -> StructuringElement.rect(4, 3))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("width");
        }

        @Test
        @DisplayName("偶数高度抛出异常")
        void evenHeightThrows() {
            assertThatThrownBy(() -> StructuringElement.rect(3, 4))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("height");
        }

        @Test
        @DisplayName("宽度为零抛出异常")
        void zeroWidthThrows() {
            assertThatThrownBy(() -> StructuringElement.rect(0, 3))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("负数高度抛出异常")
        void negativeHeightThrows() {
            assertThatThrownBy(() -> StructuringElement.rect(3, -1))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("null 掩码抛出异常")
        void nullMaskThrows() {
            assertThatThrownBy(() -> new StructuringElement(null, 3, 3, 1, 1))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("掩码长度不匹配抛出异常")
        void maskLengthMismatchThrows() {
            boolean[] wrongMask = new boolean[5];
            assertThatThrownBy(() -> new StructuringElement(wrongMask, 3, 3, 1, 1))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("length");
        }

        @Test
        @DisplayName("锚点越界抛出异常")
        void anchorOutOfBoundsThrows() {
            boolean[] mask = new boolean[9];
            assertThatThrownBy(() -> new StructuringElement(mask, 3, 3, 3, 1))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("anchorX");
        }

        @Test
        @DisplayName("掩码是防御性副本")
        void maskIsDefensiveCopy() {
            StructuringElement se = StructuringElement.rect(3, 3);
            boolean[] mask = se.mask();
            mask[0] = false;
            // Original should not be affected
            assertThat(se.mask()[0]).isTrue();
        }
    }
}
