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
 * Tests for {@link L2Normalizer}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("L2Normalizer — L2 归一化器")
class L2NormalizerTest {

    private static final float EPSILON = 1e-5f;

    @Nested
    @DisplayName("1D 归一化")
    class OneDimensionalTest {

        @Test
        @DisplayName("1D 向量归一化到单位范数")
        void unitNorm1D() {
            L2Normalizer norm = new L2Normalizer();
            Tensor data = Tensor.fromFloat(new float[]{3, 4}, Shape.of(2));
            float[] result = norm.normalize(data).toFloatArray();
            // norm = 5, result = [0.6, 0.8]
            assertThat(result[0]).isCloseTo(0.6f, within(EPSILON));
            assertThat(result[1]).isCloseTo(0.8f, within(EPSILON));
            // Verify L2 norm = 1
            double normVal = Math.sqrt(result[0] * result[0] + result[1] * result[1]);
            assertThat((float) normVal).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("已归一化的向量保持不变")
        void alreadyNormalized() {
            L2Normalizer norm = new L2Normalizer();
            float invSqrt2 = (float) (1.0 / Math.sqrt(2));
            Tensor data = Tensor.fromFloat(new float[]{invSqrt2, invSqrt2}, Shape.of(2));
            float[] result = norm.normalize(data).toFloatArray();
            assertThat(result[0]).isCloseTo(invSqrt2, within(EPSILON));
            assertThat(result[1]).isCloseTo(invSqrt2, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("2D 逐行归一化")
    class TwoDimensionalTest {

        @Test
        @DisplayName("每行独立归一化到单位范数")
        void perRowNormalization() {
            L2Normalizer norm = new L2Normalizer();
            // Row 0: [3, 4] → norm=5 → [0.6, 0.8]
            // Row 1: [0, 5] → norm=5 → [0, 1]
            Tensor data = Tensor.fromFloat(new float[]{
                    3, 4,
                    0, 5
            }, Shape.of(2, 2));
            float[] result = norm.normalize(data).toFloatArray();

            // Row 0
            assertThat(result[0]).isCloseTo(0.6f, within(EPSILON));
            assertThat(result[1]).isCloseTo(0.8f, within(EPSILON));

            // Row 1
            assertThat(result[2]).isCloseTo(0.0f, within(EPSILON));
            assertThat(result[3]).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("3行数据每行L2范数为1")
        void threeRowsAllUnitNorm() {
            L2Normalizer norm = new L2Normalizer();
            Tensor data = Tensor.fromFloat(new float[]{
                    1, 2, 3,
                    4, 5, 6,
                    7, 8, 9
            }, Shape.of(3, 3));
            float[] result = norm.normalize(data).toFloatArray();

            for (int row = 0; row < 3; row++) {
                double normSq = 0;
                for (int col = 0; col < 3; col++) {
                    float v = result[row * 3 + col];
                    normSq += v * v;
                }
                assertThat((float) Math.sqrt(normSq)).isCloseTo(1.0f, within(EPSILON));
            }
        }
    }

    @Nested
    @DisplayName("边界情况")
    class EdgeCaseTest {

        @Test
        @DisplayName("零向量 → 返回零")
        void zeroVector() {
            L2Normalizer norm = new L2Normalizer();
            Tensor data = Tensor.fromFloat(new float[]{0, 0, 0}, Shape.of(3));
            float[] result = norm.normalize(data).toFloatArray();
            for (float v : result) {
                assertThat(v).isEqualTo(0.0f);
            }
        }

        @Test
        @DisplayName("2D 中零行 → 该行返回零")
        void zeroRow() {
            L2Normalizer norm = new L2Normalizer();
            Tensor data = Tensor.fromFloat(new float[]{
                    3, 4,
                    0, 0
            }, Shape.of(2, 2));
            float[] result = norm.normalize(data).toFloatArray();
            // Row 0 normalized
            assertThat(result[0]).isCloseTo(0.6f, within(EPSILON));
            assertThat(result[1]).isCloseTo(0.8f, within(EPSILON));
            // Row 1 zero
            assertThat(result[2]).isEqualTo(0.0f);
            assertThat(result[3]).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("单元素张量")
        void singleElement() {
            L2Normalizer norm = new L2Normalizer();
            Tensor data = Tensor.fromFloat(new float[]{5.0f}, Shape.of(1));
            float[] result = norm.normalize(data).toFloatArray();
            assertThat(result[0]).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("负值正确归一化")
        void negativeValues() {
            L2Normalizer norm = new L2Normalizer();
            Tensor data = Tensor.fromFloat(new float[]{-3, 4}, Shape.of(2));
            float[] result = norm.normalize(data).toFloatArray();
            assertThat(result[0]).isCloseTo(-0.6f, within(EPSILON));
            assertThat(result[1]).isCloseTo(0.8f, within(EPSILON));
        }

        @Test
        @DisplayName("大值数据不溢出")
        void largeValues() {
            L2Normalizer norm = new L2Normalizer();
            Tensor data = Tensor.fromFloat(new float[]{1e18f, 1e18f}, Shape.of(2));
            float[] result = norm.normalize(data).toFloatArray();
            double normVal = Math.sqrt(result[0] * result[0] + result[1] * result[1]);
            assertThat((float) normVal).isCloseTo(1.0f, within(0.01f));
        }
    }

    @Nested
    @DisplayName("fit 与 denormalize")
    class FitAndDenormalizeTest {

        @Test
        @DisplayName("fit 是 no-op，不抛异常")
        void fitIsNoOp() {
            L2Normalizer norm = new L2Normalizer();
            // Should not throw
            norm.fit(Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3)));
            norm.fit(null); // no-op, should not throw
        }

        @Test
        @DisplayName("denormalize 抛 UnsupportedOperationException")
        void denormalizeThrows() {
            L2Normalizer norm = new L2Normalizer();
            Tensor data = Tensor.fromFloat(new float[]{1, 0}, Shape.of(2));
            assertThatThrownBy(() -> norm.denormalize(data))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("not invertible");
        }
    }

    @Nested
    @DisplayName("验证")
    class ValidationTest {

        @Test
        @DisplayName("null 数据抛异常")
        void nullData() {
            L2Normalizer norm = new L2Normalizer();
            assertThatThrownBy(() -> norm.normalize(null))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("线程安全")
    class ThreadSafetyTest {

        @Test
        @DisplayName("多线程并发归一化无异常")
        void concurrentNormalization() throws InterruptedException {
            L2Normalizer norm = new L2Normalizer();
            Thread[] threads = new Thread[8];
            boolean[] errors = new boolean[1];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    try {
                        for (int j = 0; j < 100; j++) {
                            Tensor data = Tensor.fromFloat(new float[]{3, 4}, Shape.of(2));
                            float[] result = norm.normalize(data).toFloatArray();
                            if (Math.abs(result[0] - 0.6f) > EPSILON ||
                                    Math.abs(result[1] - 0.8f) > EPSILON) {
                                errors[0] = true;
                            }
                        }
                    } catch (Exception e) {
                        errors[0] = true;
                    }
                });
                threads[i].start();
            }
            for (Thread t : threads) t.join();
            assertThat(errors[0]).isFalse();
        }
    }
}
