package cloud.opencode.base.neural.norm;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link MinMaxNormalizer}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("MinMaxNormalizer — 最小-最大归一化器")
class MinMaxNormalizerTest {

    private static final float EPSILON = 1e-5f;

    @Nested
    @DisplayName("默认范围 [0, 1]")
    class DefaultRangeTest {

        @Test
        @DisplayName("基本归一化 [0, 10] → [0, 1]")
        void basicNormalization() {
            MinMaxNormalizer norm = new MinMaxNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{0, 5, 10}, Shape.of(3));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();
            assertThat(result[0]).isCloseTo(0.0f, within(EPSILON));
            assertThat(result[1]).isCloseTo(0.5f, within(EPSILON));
            assertThat(result[2]).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("2D 数据逐特征归一化")
        void twoDimensionalPerFeature() {
            MinMaxNormalizer norm = new MinMaxNormalizer();
            // 3 samples, 2 features
            // Feature 0: [1, 3, 5] → min=1, max=5
            // Feature 1: [10, 20, 30] → min=10, max=30
            Tensor data = Tensor.fromFloat(new float[]{
                    1, 10,
                    3, 20,
                    5, 30
            }, Shape.of(3, 2));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();
            // Feature 0: (1-1)/(5-1)=0, (3-1)/4=0.5, (5-1)/4=1
            assertThat(result[0]).isCloseTo(0.0f, within(EPSILON));
            assertThat(result[2]).isCloseTo(0.5f, within(EPSILON));
            assertThat(result[4]).isCloseTo(1.0f, within(EPSILON));
            // Feature 1: (10-10)/20=0, (20-10)/20=0.5, (30-10)/20=1
            assertThat(result[1]).isCloseTo(0.0f, within(EPSILON));
            assertThat(result[3]).isCloseTo(0.5f, within(EPSILON));
            assertThat(result[5]).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("denormalize 恢复原始数据")
        void denormalizeRoundTrip() {
            MinMaxNormalizer norm = new MinMaxNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{2, 4, 6, 8}, Shape.of(4));
            norm.fit(data);
            Tensor normalized = norm.normalize(data);
            float[] recovered = norm.denormalize(normalized).toFloatArray();
            assertThat(recovered[0]).isCloseTo(2.0f, within(EPSILON));
            assertThat(recovered[1]).isCloseTo(4.0f, within(EPSILON));
            assertThat(recovered[2]).isCloseTo(6.0f, within(EPSILON));
            assertThat(recovered[3]).isCloseTo(8.0f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("自定义目标范围")
    class CustomRangeTest {

        @Test
        @DisplayName("归一化到 [-1, 1]")
        void negativeOneToOne() {
            MinMaxNormalizer norm = new MinMaxNormalizer(-1.0f, 1.0f);
            Tensor data = Tensor.fromFloat(new float[]{0, 5, 10}, Shape.of(3));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();
            assertThat(result[0]).isCloseTo(-1.0f, within(EPSILON));
            assertThat(result[1]).isCloseTo(0.0f, within(EPSILON));
            assertThat(result[2]).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("targetMin >= targetMax 抛异常")
        void invalidTargetRange() {
            assertThatThrownBy(() -> new MinMaxNormalizer(1.0f, 0.0f))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("targetMin");
        }

        @Test
        @DisplayName("targetMin == targetMax 抛异常")
        void equalTargetRange() {
            assertThatThrownBy(() -> new MinMaxNormalizer(5.0f, 5.0f))
                    .isInstanceOf(NeuralException.class);
        }
    }

    @Nested
    @DisplayName("边界情况")
    class EdgeCaseTest {

        @Test
        @DisplayName("常量特征 → 输出 targetMin")
        void constantFeature() {
            MinMaxNormalizer norm = new MinMaxNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{7, 7, 7}, Shape.of(3));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();
            for (float v : result) {
                assertThat(v).isCloseTo(0.0f, within(EPSILON));
            }
        }

        @Test
        @DisplayName("单个样本")
        void singleSample() {
            MinMaxNormalizer norm = new MinMaxNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{42.0f}, Shape.of(1));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();
            assertThat(result[0]).isCloseTo(0.0f, within(EPSILON));
        }

        @Test
        @DisplayName("大值数据")
        void largeValues() {
            MinMaxNormalizer norm = new MinMaxNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{1e6f, 2e6f, 3e6f}, Shape.of(3));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();
            assertThat(result[0]).isCloseTo(0.0f, within(EPSILON));
            assertThat(result[2]).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("负值数据")
        void negativeValues() {
            MinMaxNormalizer norm = new MinMaxNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{-10, -5, 0}, Shape.of(3));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();
            assertThat(result[0]).isCloseTo(0.0f, within(EPSILON));
            assertThat(result[1]).isCloseTo(0.5f, within(EPSILON));
            assertThat(result[2]).isCloseTo(1.0f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("验证")
    class ValidationTest {

        @Test
        @DisplayName("未拟合时 normalize 抛异常")
        void normalizeBeforeFit() {
            MinMaxNormalizer norm = new MinMaxNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            assertThatThrownBy(() -> norm.normalize(data))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("fitted");
        }

        @Test
        @DisplayName("未拟合时 denormalize 抛异常")
        void denormalizeBeforeFit() {
            MinMaxNormalizer norm = new MinMaxNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            assertThatThrownBy(() -> norm.denormalize(data))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("fitted");
        }

        @Test
        @DisplayName("null 数据抛异常")
        void nullData() {
            MinMaxNormalizer norm = new MinMaxNormalizer();
            assertThatThrownBy(() -> norm.fit(null))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("null");
        }
    }
}
