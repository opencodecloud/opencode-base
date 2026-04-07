package cloud.opencode.base.neural.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link Quantization}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("Quantization — INT8 对称量化")
class QuantizationTest {

    // ==================== quantize ====================

    @Nested
    @DisplayName("quantize — float32 → int8 量化")
    class QuantizeTest {

        @Test
        @DisplayName("基本量化: [1.0, -1.0, 0.5] 的 scale 和量化值正确")
        void quantizeBasicValues() {
            float[] data = {1.0f, -1.0f, 0.5f};

            Quantization.QuantizedData result = Quantization.quantize(data);

            // maxAbs = 1.0, scale = 1.0 / 127
            float expectedScale = 1.0f / 127.0f;
            assertThat(result.scale()).isCloseTo(expectedScale, within(1e-7f));

            // quantized[0] = round(1.0 / scale) = round(127) = 127
            assertThat(result.data()[0]).isEqualTo((byte) 127);
            // quantized[1] = round(-1.0 / scale) = round(-127) = -127
            assertThat(result.data()[1]).isEqualTo((byte) -127);
            // quantized[2] = round(0.5 / scale) = round(63.5) = 64
            assertThat(result.data()[2]).isEqualTo((byte) 64);
        }

        @Test
        @DisplayName("全零输入: scale=0，量化值全为 0，无除零异常")
        void quantizeAllZeros() {
            float[] data = {0.0f, 0.0f, 0.0f};

            Quantization.QuantizedData result = Quantization.quantize(data);

            assertThat(result.scale()).isEqualTo(0.0f);
            assertThat(result.data()).containsExactly((byte) 0, (byte) 0, (byte) 0);
        }

        @Test
        @DisplayName("null 输入 → NullPointerException")
        void quantizeNullThrows() {
            assertThatThrownBy(() -> Quantization.quantize(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== dequantize ====================

    @Nested
    @DisplayName("dequantize — int8 → float32 反量化")
    class DequantizeTest {

        @Test
        @DisplayName("量化→反量化往返误差 < 1%")
        void roundtripErrorWithinOnePercent() {
            float[] original = {1.0f, -1.0f, 0.5f, -0.25f, 0.75f};

            Quantization.QuantizedData quantized = Quantization.quantize(original);
            float[] restored = Quantization.dequantize(quantized.data(), quantized.scale());

            assertThat(restored).hasSize(original.length);
            for (int i = 0; i < original.length; i++) {
                float error = Math.abs(restored[i] - original[i]);
                float relativeError = (original[i] == 0.0f) ? error : error / Math.abs(original[i]);
                assertThat(relativeError)
                        .as("Element %d: original=%.6f, restored=%.6f", i, original[i], restored[i])
                        .isLessThan(0.01f);
            }
        }

        @Test
        @DisplayName("null 输入 → NullPointerException")
        void dequantizeNullThrows() {
            assertThatThrownBy(() -> Quantization.dequantize(null, 1.0f))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== gemmQuantized ====================

    @Nested
    @DisplayName("gemmQuantized — 混合精度矩阵乘法")
    class GemmQuantizedTest {

        @Test
        @DisplayName("结果与 Blas.matmul 一致（误差 < 0.5/元素）")
        void gemmQuantizedMatchesBlas() {
            // A: 2x3 float32
            float[] a = {1.0f, 2.0f, 3.0f,
                         4.0f, 5.0f, 6.0f};
            // B: 3x2 float32
            float[] b = {0.5f, -0.5f,
                         1.0f, -1.0f,
                         0.25f, -0.25f};

            // Reference result via Blas.matmul
            float[] expected = Blas.matmul(a, 2, 3, b, 3, 2);

            // Quantize B
            Quantization.QuantizedData bq = Quantization.quantize(b);

            // Mixed-precision GEMM
            float[] c = new float[2 * 2];
            Quantization.gemmQuantized(a, 2, 3, bq.data(), bq.scale(), 3, 2, c);

            assertThat(c).hasSize(expected.length);
            for (int i = 0; i < expected.length; i++) {
                assertThat(c[i])
                        .as("Element %d: expected=%.4f, actual=%.4f", i, expected[i], c[i])
                        .isCloseTo(expected[i], within(0.5f));
            }
        }

        @Test
        @DisplayName("null 输入 → NullPointerException")
        void gemmQuantizedNullAThrows() {
            assertThatThrownBy(() ->
                    Quantization.gemmQuantized(null, 1, 1, new byte[]{1}, 1.0f, 1, 1, new float[1]))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null B → NullPointerException")
        void gemmQuantizedNullBThrows() {
            assertThatThrownBy(() ->
                    Quantization.gemmQuantized(new float[]{1.0f}, 1, 1, null, 1.0f, 1, 1, new float[1]))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null C → NullPointerException")
        void gemmQuantizedNullCThrows() {
            assertThatThrownBy(() ->
                    Quantization.gemmQuantized(new float[]{1.0f}, 1, 1, new byte[]{1}, 1.0f, 1, 1, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("维度不匹配 → NeuralException")
        void gemmQuantizedDimensionMismatch() {
            assertThatThrownBy(() ->
                    Quantization.gemmQuantized(new float[]{1.0f, 2.0f}, 1, 2,
                            new byte[]{1, 2, 3}, 1.0f, 3, 1,
                            new float[1]))
                    .isInstanceOf(cloud.opencode.base.neural.exception.NeuralException.class);
        }
    }
}
