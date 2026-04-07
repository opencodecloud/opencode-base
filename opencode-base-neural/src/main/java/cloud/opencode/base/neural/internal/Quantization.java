package cloud.opencode.base.neural.internal;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;

import java.util.Objects;

/**
 * INT8 Symmetric Quantization Utilities
 * INT8 对称量化工具
 *
 * <p>Provides symmetric quantization of float32 tensors to int8 representation and
 * mixed-precision GEMM (float32 activations x int8 weights). Symmetric quantization
 * maps the range [-maxAbs, +maxAbs] to [-127, +127] using a single scale factor.</p>
 * <p>提供 float32 张量到 int8 表示的对称量化，以及混合精度 GEMM（float32 激活值 x int8 权重）。
 * 对称量化使用单一缩放因子将范围 [-maxAbs, +maxAbs] 映射到 [-127, +127]。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Symmetric quantize: float32 → int8 with scale factor - 对称量化: float32 → int8 带缩放因子</li>
 *   <li>Dequantize: int8 → float32 reconstruction - 反量化: int8 → float32 重建</li>
 *   <li>Mixed-precision GEMM: float32 @ int8 → float32 - 混合精度 GEMM: float32 @ int8 → float32</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Quantized GEMM reduces memory bandwidth by ~4x for weight matrix - 量化 GEMM 将权重矩阵的内存带宽降低约 4 倍</li>
 *   <li>Scale multiplication deferred to outer loop for efficiency - 缩放乘法推迟到外层循环以提高效率</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Blas
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class Quantization {

    private Quantization() {}

    /**
     * Quantize float32 tensor to int8 using symmetric quantization.
     * 使用对称量化将 float32 张量量化为 int8。
     *
     * <p>The scale factor is computed as {@code max(abs(data)) / 127}. Each element is
     * rounded and clamped to the range [-127, 127]. If all values are zero, scale is 0
     * and all quantized values are 0.</p>
     * <p>缩放因子计算为 {@code max(abs(data)) / 127}。每个元素经过四舍五入并裁剪到 [-127, 127] 范围。
     * 如果所有值为零，则缩放因子为 0，所有量化值为 0。</p>
     *
     * @param data float32 values to quantize | 要量化的 float32 值
     * @return quantized data with scale factor | 带缩放因子的量化数据
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if data is empty | 如果 data 为空
     */
    public static QuantizedData quantize(float[] data) {
        Objects.requireNonNull(data, "data must not be null");
        if (data.length == 0) {
            throw new NeuralException("data must not be empty", NeuralErrorCode.INVALID_PARAMETERS);
        }

        // Find max absolute value
        float maxAbs = 0.0f;
        for (float v : data) {
            float abs = Math.abs(v);
            if (abs > maxAbs) {
                maxAbs = abs;
            }
        }

        // Handle all-zero input
        if (maxAbs == 0.0f) {
            return new QuantizedData(new byte[data.length], 0.0f);
        }

        float scale = maxAbs / 127.0f;
        float invScale = 1.0f / scale;
        byte[] quantized = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            int val = Math.round(data[i] * invScale);
            quantized[i] = (byte) Math.clamp(val, -127, 127);
        }

        return new QuantizedData(quantized, scale);
    }

    /**
     * Dequantize int8 data back to float32.
     * 将 int8 数据反量化为 float32。
     *
     * <p>Each element is reconstructed as {@code quantized[i] * scale}.</p>
     * <p>每个元素重建为 {@code quantized[i] * scale}。</p>
     *
     * @param quantized int8 quantized values | int8 量化值
     * @param scale     the scale factor from quantization | 量化时的缩放因子
     * @return reconstructed float32 values | 重建的 float32 值
     * @throws NullPointerException if quantized is null | 如果 quantized 为 null
     */
    public static float[] dequantize(byte[] quantized, float scale) {
        Objects.requireNonNull(quantized, "quantized must not be null");

        float[] result = new float[quantized.length];
        for (int i = 0; i < quantized.length; i++) {
            result[i] = quantized[i] * scale;
        }
        return result;
    }

    /**
     * Mixed-precision GEMM: C = A(float32) @ B(int8) with B's scale factor.
     * 混合精度 GEMM: C = A(float32) @ B(int8) 使用 B 的缩放因子。
     *
     * <p>Computes the matrix product where activations (A) remain in float32 and weights (B)
     * are quantized to int8. The inner loop accumulates {@code A[i,k] * B_quantized[k,j]}
     * and the result is multiplied by bScale at the end for efficiency.</p>
     * <p>计算矩阵乘积，其中激活值（A）保持 float32，权重（B）量化为 int8。
     * 内层循环累加 {@code A[i,k] * B_quantized[k,j]}，结果最后乘以 bScale 以提高效率。</p>
     *
     * @param a          activation matrix A [aRows x aCols] in row-major order | 行主序激活矩阵 A [aRows x aCols]
     * @param aRows      number of rows in A | A 的行数
     * @param aCols      number of columns in A (must equal bRows) | A 的列数（必须等于 bRows）
     * @param bQuantized quantized weight matrix B [bRows x bCols] in row-major order | 行主序量化权重矩阵 B [bRows x bCols]
     * @param bScale     scale factor for B from quantization | B 的量化缩放因子
     * @param bRows      number of rows in B | B 的行数
     * @param bCols      number of columns in B | B 的列数
     * @param c          output matrix C [aRows x bCols] in row-major order (overwritten) | 行主序输出矩阵 C [aRows x bCols]（覆盖写入）
     * @throws NullPointerException if a, bQuantized, or c is null | 如果 a、bQuantized 或 c 为 null
     * @throws NeuralException      if dimensions are invalid or incompatible | 如果维度无效或不兼容
     */
    public static void gemmQuantized(float[] a, int aRows, int aCols,
                                      byte[] bQuantized, float bScale, int bRows, int bCols,
                                      float[] c) {
        Objects.requireNonNull(a, "matrix A must not be null");
        Objects.requireNonNull(bQuantized, "quantized matrix B must not be null");
        Objects.requireNonNull(c, "matrix C must not be null");

        if (aRows <= 0 || aCols <= 0 || bRows <= 0 || bCols <= 0) {
            throw new NeuralException(
                    "Dimensions must be > 0: aRows=" + aRows + ", aCols=" + aCols
                            + ", bRows=" + bRows + ", bCols=" + bCols,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (aCols != bRows) {
            throw new NeuralException(
                    "Inner dimensions mismatch: A is [" + aRows + "x" + aCols
                            + "], B is [" + bRows + "x" + bCols + "]",
                    NeuralErrorCode.SHAPE_MISMATCH);
        }
        if (a.length < (long) aRows * aCols) {
            throw new NeuralException(
                    "A length " + a.length + " < aRows*aCols " + ((long) aRows * aCols),
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (bQuantized.length < (long) bRows * bCols) {
            throw new NeuralException(
                    "B length " + bQuantized.length + " < bRows*bCols " + ((long) bRows * bCols),
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (c.length < (long) aRows * bCols) {
            throw new NeuralException(
                    "C length " + c.length + " < aRows*bCols " + ((long) aRows * bCols),
                    NeuralErrorCode.INVALID_PARAMETERS);
        }

        // C[i,j] = bScale * sum_k(A[i,k] * B_quantized[k,j])
        // Defer scale multiplication to after accumulation for efficiency
        for (int i = 0; i < aRows; i++) {
            int aRowOffset = i * aCols;
            int cRowOffset = i * bCols;
            for (int j = 0; j < bCols; j++) {
                float sum = 0.0f;
                for (int k = 0; k < aCols; k++) {
                    sum += a[aRowOffset + k] * bQuantized[k * bCols + j];
                }
                c[cRowOffset + j] = sum * bScale;
            }
        }
    }

    /**
     * Result holder for quantized data.
     * 量化数据结果容器。
     *
     * @param data  int8 quantized values | int8 量化值
     * @param scale scale factor (maxAbs / 127) | 缩放因子 (maxAbs / 127)
     */
    public record QuantizedData(byte[] data, float scale) {}
}
