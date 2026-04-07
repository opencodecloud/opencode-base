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
 * Tests for {@link ZScoreNormalizer}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("ZScoreNormalizer — Z分数归一化器")
class ZScoreNormalizerTest {

    private static final float EPSILON = 1e-4f;

    @Nested
    @DisplayName("基本功能")
    class BasicTest {

        @Test
        @DisplayName("标准化后均值接近0、标准差接近1")
        void standardizeMeanAndStd() {
            ZScoreNormalizer norm = new ZScoreNormalizer();
            // Data: [2, 4, 4, 4, 5, 5, 7, 9] → mean=5, std=2
            Tensor data = Tensor.fromFloat(new float[]{2, 4, 4, 4, 5, 5, 7, 9}, Shape.of(8));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();

            // Check mean ≈ 0
            double mean = 0;
            for (float v : result) mean += v;
            mean /= result.length;
            assertThat((float) mean).isCloseTo(0.0f, within(EPSILON));

            // Check std ≈ 1
            double variance = 0;
            for (float v : result) variance += (v - mean) * (v - mean);
            variance /= result.length;
            assertThat((float) Math.sqrt(variance)).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("简单数据的精确值验证")
        void exactValues() {
            ZScoreNormalizer norm = new ZScoreNormalizer();
            // Data: [1, 3] → mean=2, std=1
            Tensor data = Tensor.fromFloat(new float[]{1, 3}, Shape.of(2));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();
            assertThat(result[0]).isCloseTo(-1.0f, within(EPSILON));
            assertThat(result[1]).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("2D 数据逐特征标准化")
        void twoDimensionalPerFeature() {
            ZScoreNormalizer norm = new ZScoreNormalizer();
            // 3 samples, 2 features
            // Feature 0: [1, 3, 5] → mean=3, std=sqrt(8/3)
            // Feature 1: [10, 10, 10] → mean=10, std=0 → output 0
            Tensor data = Tensor.fromFloat(new float[]{
                    1, 10,
                    3, 10,
                    5, 10
            }, Shape.of(3, 2));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();

            // Feature 1 (constant) should be 0
            assertThat(result[1]).isCloseTo(0.0f, within(EPSILON));
            assertThat(result[3]).isCloseTo(0.0f, within(EPSILON));
            assertThat(result[5]).isCloseTo(0.0f, within(EPSILON));

            // Feature 0: normalized mean should be 0
            float f0Mean = (result[0] + result[2] + result[4]) / 3;
            assertThat(f0Mean).isCloseTo(0.0f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("反归一化")
    class DenormalizeTest {

        @Test
        @DisplayName("denormalize 恢复原始数据")
        void roundTrip() {
            ZScoreNormalizer norm = new ZScoreNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{10, 20, 30, 40}, Shape.of(4));
            norm.fit(data);
            Tensor normalized = norm.normalize(data);
            float[] recovered = norm.denormalize(normalized).toFloatArray();
            assertThat(recovered[0]).isCloseTo(10.0f, within(EPSILON));
            assertThat(recovered[1]).isCloseTo(20.0f, within(EPSILON));
            assertThat(recovered[2]).isCloseTo(30.0f, within(EPSILON));
            assertThat(recovered[3]).isCloseTo(40.0f, within(EPSILON));
        }

        @Test
        @DisplayName("常量特征的反归一化返回原始常量值")
        void denormalizeConstantFeature() {
            ZScoreNormalizer norm = new ZScoreNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{5, 5, 5}, Shape.of(3));
            norm.fit(data);
            Tensor normalized = norm.normalize(data);
            float[] recovered = norm.denormalize(normalized).toFloatArray();
            for (float v : recovered) {
                assertThat(v).isCloseTo(5.0f, within(EPSILON));
            }
        }
    }

    @Nested
    @DisplayName("边界情况")
    class EdgeCaseTest {

        @Test
        @DisplayName("常量数据 → 输出全0")
        void constantData() {
            ZScoreNormalizer norm = new ZScoreNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{42, 42, 42}, Shape.of(3));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();
            for (float v : result) {
                assertThat(v).isCloseTo(0.0f, within(EPSILON));
            }
        }

        @Test
        @DisplayName("单个样本")
        void singleSample() {
            ZScoreNormalizer norm = new ZScoreNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{7.0f}, Shape.of(1));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();
            // Single sample: mean=7, std=0 → output 0
            assertThat(result[0]).isCloseTo(0.0f, within(EPSILON));
        }

        @Test
        @DisplayName("大值数据")
        void largeValues() {
            ZScoreNormalizer norm = new ZScoreNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{1e6f, 2e6f, 3e6f}, Shape.of(3));
            norm.fit(data);
            float[] result = norm.normalize(data).toFloatArray();
            double mean = 0;
            for (float v : result) mean += v;
            mean /= result.length;
            assertThat((float) mean).isCloseTo(0.0f, within(0.01f));
        }
    }

    @Nested
    @DisplayName("验证")
    class ValidationTest {

        @Test
        @DisplayName("未拟合时 normalize 抛异常")
        void normalizeBeforeFit() {
            ZScoreNormalizer norm = new ZScoreNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            assertThatThrownBy(() -> norm.normalize(data))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("fitted");
        }

        @Test
        @DisplayName("未拟合时 denormalize 抛异常")
        void denormalizeBeforeFit() {
            ZScoreNormalizer norm = new ZScoreNormalizer();
            Tensor data = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            assertThatThrownBy(() -> norm.denormalize(data))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("fitted");
        }

        @Test
        @DisplayName("null 数据抛异常")
        void nullData() {
            ZScoreNormalizer norm = new ZScoreNormalizer();
            assertThatThrownBy(() -> norm.fit(null))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("null");
        }
    }
}
