package cloud.opencode.base.image.kernel;

import cloud.opencode.base.image.exception.ImageOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * IntegralImage 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("IntegralImage 积分图测试")
class IntegralImageTest {

    // 4x4 test image:
    // 1  2  3  4
    // 5  6  7  8
    // 9  10 11 12
    // 13 14 15 16
    private IntegralImage integral;
    private int[] gray;

    @BeforeEach
    void setUp() {
        gray = new int[]{
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        };
        integral = new IntegralImage(gray, 4, 4);
    }

    @Nested
    @DisplayName("regionSum 区域求和测试")
    class RegionSumTests {

        @Test
        @DisplayName("全图求和等于所有像素之和")
        void fullImageSum() {
            long expected = 0;
            for (int v : gray) {
                expected += v;
            }
            assertThat(integral.regionSum(0, 0, 3, 3)).isEqualTo(expected);
            assertThat(expected).isEqualTo(136L); // 1+2+...+16 = 136
        }

        @Test
        @DisplayName("单像素求和")
        void singlePixelSum() {
            assertThat(integral.regionSum(0, 0, 0, 0)).isEqualTo(1);
            assertThat(integral.regionSum(1, 1, 1, 1)).isEqualTo(6);
            assertThat(integral.regionSum(3, 3, 3, 3)).isEqualTo(16);
        }

        @Test
        @DisplayName("2x2 子区域求和")
        void subRegionSum() {
            // Top-left 2x2: 1+2+5+6 = 14
            assertThat(integral.regionSum(0, 0, 1, 1)).isEqualTo(14);
            // Bottom-right 2x2: 11+12+15+16 = 54
            assertThat(integral.regionSum(2, 2, 3, 3)).isEqualTo(54);
            // Center 2x2: 6+7+10+11 = 34
            assertThat(integral.regionSum(1, 1, 2, 2)).isEqualTo(34);
        }

        @Test
        @DisplayName("单行求和")
        void singleRowSum() {
            // First row: 1+2+3+4 = 10
            assertThat(integral.regionSum(0, 0, 3, 0)).isEqualTo(10);
            // Last row: 13+14+15+16 = 58
            assertThat(integral.regionSum(0, 3, 3, 3)).isEqualTo(58);
        }

        @Test
        @DisplayName("单列求和")
        void singleColumnSum() {
            // First column: 1+5+9+13 = 28
            assertThat(integral.regionSum(0, 0, 0, 3)).isEqualTo(28);
        }
    }

    @Nested
    @DisplayName("regionMean 区域均值测试")
    class RegionMeanTests {

        @Test
        @DisplayName("全图均值正确")
        void fullImageMean() {
            // Mean of 1..16 = 136/16 = 8.5
            assertThat(integral.regionMean(0, 0, 3, 3)).isEqualTo(8.5);
        }

        @Test
        @DisplayName("单像素均值等于像素值")
        void singlePixelMean() {
            assertThat(integral.regionMean(2, 1, 2, 1)).isEqualTo(7.0);
        }

        @Test
        @DisplayName("2x2 子区域均值")
        void subRegionMean() {
            // Top-left 2x2: 14/4 = 3.5
            assertThat(integral.regionMean(0, 0, 1, 1)).isEqualTo(3.5);
        }
    }

    @Nested
    @DisplayName("regionVariance 区域方差测试")
    class RegionVarianceTests {

        @Test
        @DisplayName("全图方差正确")
        void fullImageVariance() {
            // Variance of 1..16: E[X^2] - (E[X])^2
            // E[X] = 8.5, E[X^2] = (1+4+9+16+25+36+49+64+81+100+121+144+169+196+225+256)/16 = 1496/16 = 93.5
            // Var = 93.5 - 72.25 = 21.25
            assertThat(integral.regionVariance(0, 0, 3, 3)).isCloseTo(21.25, within(1e-10));
        }

        @Test
        @DisplayName("单像素方差为 0")
        void singlePixelVarianceIsZero() {
            assertThat(integral.regionVariance(1, 1, 1, 1)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("均匀区域方差为 0")
        void uniformRegionVarianceIsZero() {
            int[] uniform = {42, 42, 42, 42};
            IntegralImage ii = new IntegralImage(uniform, 2, 2);
            assertThat(ii.regionVariance(0, 0, 1, 1)).isCloseTo(0.0, within(1e-10));
        }
    }

    @Nested
    @DisplayName("属性访问测试")
    class PropertyTests {

        @Test
        @DisplayName("getWidth 和 getHeight 正确")
        void dimensionsCorrect() {
            assertThat(integral.getWidth()).isEqualTo(4);
            assertThat(integral.getHeight()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("参数校验测试")
    class ValidationTests {

        @Test
        @DisplayName("null 像素数组抛出异常")
        void nullPixelsThrows() {
            assertThatThrownBy(() -> new IntegralImage(null, 4, 4))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("非正数尺寸抛出异常")
        void invalidDimensionsThrow() {
            assertThatThrownBy(() -> new IntegralImage(new int[4], 0, 4))
                    .isInstanceOf(ImageOperationException.class);
            assertThatThrownBy(() -> new IntegralImage(new int[4], 4, -1))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("像素数组长度不匹配抛出异常")
        void pixelLengthMismatchThrows() {
            assertThatThrownBy(() -> new IntegralImage(new int[5], 2, 2))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("区域坐标越界抛出异常")
        void outOfBoundsRegionThrows() {
            assertThatThrownBy(() -> integral.regionSum(-1, 0, 3, 3))
                    .isInstanceOf(ImageOperationException.class);
            assertThatThrownBy(() -> integral.regionSum(0, 0, 4, 3))
                    .isInstanceOf(ImageOperationException.class);
            assertThatThrownBy(() -> integral.regionSum(2, 0, 1, 3))
                    .isInstanceOf(ImageOperationException.class);
        }
    }
}
