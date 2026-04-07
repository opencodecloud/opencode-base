package cloud.opencode.base.neural.internal;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;

import java.util.Objects;

/**
 * Pure Java BLAS (Basic Linear Algebra Subprograms)
 * 纯 Java BLAS（基础线性代数子程序）
 *
 * <p>Provides GEMM (General Matrix Multiply) operations optimized for neural network
 * inference. Uses cache-friendly blocked/tiled algorithms with loop unrolling and
 * automatic parallelization for large matrices.</p>
 * <p>提供针对神经网络推理优化的 GEMM（通用矩阵乘法）操作。使用缓存友好的分块算法，
 * 结合循环展开和大矩阵的自动并行化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>GEMM: C = alpha * op(A) @ op(B) + beta * C - GEMM: C = alpha * op(A) @ op(B) + beta * C</li>
 *   <li>Transpose support for both A and B - 支持 A 和 B 的转置</li>
 *   <li>Convenience matmul: C = A @ B - 便捷矩阵乘法: C = A @ B</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>64x64 tile blocking for L1/L2 cache utilization - 64x64 分块利用 L1/L2 缓存</li>
 *   <li>i-k-j loop order for cache-friendly B access - i-k-j 循环顺序优化 B 的缓存访问</li>
 *   <li>4x inner loop unrolling - 内层循环4倍展开</li>
 *   <li>ForkJoinPool parallelism for M*K > 65536 - 当 M*K > 65536 时使用 ForkJoinPool 并行</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class Blas {

    private Blas() {}

    /** Tile/block size for cache-friendly access. 缓存友好访问的分块大小。 */
    private static final int TILE = 64;

    /**
     * General Matrix Multiply: C = alpha * op(A) @ op(B) + beta * C.
     * 通用矩阵乘法: C = alpha * op(A) @ op(B) + beta * C。
     *
     * <p>Where op(X) = X if transX is false, or X^T if transX is true.</p>
     * <p>其中 op(X) = X（如果 transX 为 false），或 X^T（如果 transX 为 true）。</p>
     *
     * <p>After transpose resolution:
     * op(A) has dimensions [M x K], op(B) has dimensions [K x N],
     * and C has dimensions [M x N].</p>
     * <p>转置处理后: op(A) 维度为 [M x K]，op(B) 维度为 [K x N]，C 维度为 [M x N]。</p>
     *
     * @param alpha  scalar multiplier for A @ B | A @ B 的标量乘子
     * @param a      matrix A in row-major order | 行主序矩阵 A
     * @param aRows  number of rows in A (before transpose) | A 的行数（转置前）
     * @param aCols  number of columns in A (before transpose) | A 的列数（转置前）
     * @param transA whether to transpose A | 是否转置 A
     * @param b      matrix B in row-major order | 行主序矩阵 B
     * @param bRows  number of rows in B (before transpose) | B 的行数（转置前）
     * @param bCols  number of columns in B (before transpose) | B 的列数（转置前）
     * @param transB whether to transpose B | 是否转置 B
     * @param beta   scalar multiplier for C | C 的标量乘子
     * @param c      output matrix C in row-major order [M x N] | 行主序输出矩阵 C [M x N]
     * @throws NullPointerException if a, b, or c is null | 如果 a、b 或 c 为 null
     * @throws NeuralException      if dimensions are invalid or incompatible | 如果维度无效或不兼容
     */
    public static void gemm(float alpha,
                            float[] a, int aRows, int aCols, boolean transA,
                            float[] b, int bRows, int bCols, boolean transB,
                            float beta, float[] c) {
        Objects.requireNonNull(a, "matrix A must not be null");
        Objects.requireNonNull(b, "matrix B must not be null");
        Objects.requireNonNull(c, "matrix C must not be null");

        // Resolve effective dimensions after transpose
        int m = transA ? aCols : aRows;  // rows of op(A)
        int k = transA ? aRows : aCols;  // cols of op(A) = rows of op(B)
        int n = transB ? bRows : bCols;  // cols of op(B)
        int kB = transB ? bCols : bRows; // rows of op(B)

        if (k != kB) {
            throw new NeuralException(
                    "Inner dimensions mismatch: op(A) is [" + m + "x" + k
                            + "], op(B) is [" + kB + "x" + n + "]",
                    NeuralErrorCode.SHAPE_MISMATCH);
        }

        validateArrayLength(a, aRows, aCols, "A");
        validateArrayLength(b, bRows, bCols, "B");
        if (c.length < (long) m * n) {
            throw new NeuralException(
                    "C length " + c.length + " < M*N " + ((long) m * n),
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (m <= 0 || k <= 0 || n <= 0) {
            throw new NeuralException(
                    "Dimensions must be > 0: M=" + m + ", K=" + k + ", N=" + n,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }

        // Scale C by beta (use long to avoid m*n int overflow)
        int mn = (int) ((long) m * n);
        if (beta == 0.0f) {
            java.util.Arrays.fill(c, 0, mn, 0.0f);
        } else if (beta != 1.0f) {
            for (int i = 0; i < mn; i++) {
                c[i] *= beta;
            }
        }

        // GEMM kernel — choose parallel or sequential
        if (ParallelEngine.shouldParallelize((long) m * k > Integer.MAX_VALUE ? Integer.MAX_VALUE : m * k)) {
            if (!transA && !transB) {
                ParallelEngine.parallelFor(0, m, i -> {
                    gemmRowBlockNN(alpha, a, aCols, b, bCols, c, n, k, i, Math.min(i + 1, m));
                });
            } else if (!transA && transB) {
                ParallelEngine.parallelFor(0, m, i -> {
                    gemmRowBlockNT(alpha, a, aCols, b, bCols, c, n, k, i, Math.min(i + 1, m));
                });
            } else {
                ParallelEngine.parallelFor(0, m, i -> {
                    gemmRowBlock(alpha, a, aRows, aCols, transA, b, bRows, bCols, transB, c, n, k, i, Math.min(i + 1, m));
                });
            }
        } else {
            gemmTiled(alpha, a, aRows, aCols, transA, b, bRows, bCols, transB, c, m, n, k);
        }
    }

    /**
     * Simple matrix multiply: C = A @ B.
     * 简单矩阵乘法: C = A @ B。
     *
     * <p>Convenience method that allocates and returns the result matrix.</p>
     * <p>便捷方法，分配并返回结果矩阵。</p>
     *
     * @param a     matrix A [aRows x aCols] in row-major order | 行主序矩阵 A [aRows x aCols]
     * @param aRows number of rows in A | A 的行数
     * @param aCols number of columns in A | A 的列数
     * @param b     matrix B [bRows x bCols] in row-major order | 行主序矩阵 B [bRows x bCols]
     * @param bRows number of rows in B | B 的行数
     * @param bCols number of columns in B | B 的列数
     * @return the result matrix C [aRows x bCols] | 结果矩阵 C [aRows x bCols]
     * @throws NullPointerException if a or b is null | 如果 a 或 b 为 null
     * @throws NeuralException      if dimensions are incompatible | 如果维度不兼容
     */
    public static float[] matmul(float[] a, int aRows, int aCols,
                                  float[] b, int bRows, int bCols) {
        long outSize = (long) aRows * bCols;
        if (outSize > Integer.MAX_VALUE) {
            throw new NeuralException(
                    "Output matrix too large: " + aRows + " x " + bCols,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        float[] c = new float[(int) outSize];
        gemm(1.0f, a, aRows, aCols, false,
             b, bRows, bCols, false,
             0.0f, c);
        return c;
    }

    /**
     * Tiled GEMM kernel with i-k-j loop order and 4x unrolling.
     * 分块 GEMM 内核，采用 i-k-j 循环顺序和4倍展开。
     */
    private static void gemmTiled(float alpha,
                                   float[] a, int aRows, int aCols, boolean transA,
                                   float[] b, int bRows, int bCols, boolean transB,
                                   float[] c, int m, int n, int k) {
        // Fast paths: direct indexing without getElement() branch
        if (!transA && !transB) {
            gemmTiledNN(alpha, a, aCols, b, bCols, c, m, n, k);
            return;
        }
        if (!transA && transB) {
            gemmTiledNT(alpha, a, aCols, b, bCols, c, m, n, k);
            return;
        }
        for (int ii = 0; ii < m; ii += TILE) {
            int iEnd = Math.min(ii + TILE, m);
            for (int kk = 0; kk < k; kk += TILE) {
                int kEnd = Math.min(kk + TILE, k);
                for (int jj = 0; jj < n; jj += TILE) {
                    int jEnd = Math.min(jj + TILE, n);
                    for (int i = ii; i < iEnd; i++) {
                        for (int p = kk; p < kEnd; p++) {
                            float aVal = alpha * getElement(a, aRows, aCols, transA, i, p);
                            int j = jj;
                            int jLimit = jEnd - 3;
                            for (; j < jLimit; j += 4) {
                                c[i * n + j]     += aVal * getElement(b, bRows, bCols, transB, p, j);
                                c[i * n + j + 1] += aVal * getElement(b, bRows, bCols, transB, p, j + 1);
                                c[i * n + j + 2] += aVal * getElement(b, bRows, bCols, transB, p, j + 2);
                                c[i * n + j + 3] += aVal * getElement(b, bRows, bCols, transB, p, j + 3);
                            }
                            for (; j < jEnd; j++) {
                                c[i * n + j] += aVal * getElement(b, bRows, bCols, transB, p, j);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Specialized tiled GEMM for transA=false, transB=false (most common case).
     * Eliminates getElement() branch overhead with direct array indexing.
     * 针对 transA=false, transB=false（最常见情况）的特化分块 GEMM。
     * 通过直接数组索引消除 getElement() 分支开销。
     */
    private static void gemmTiledNN(float alpha, float[] a, int aCols,
                                     float[] b, int bCols,
                                     float[] c, int m, int n, int k) {
        for (int ii = 0; ii < m; ii += TILE) {
            int iEnd = Math.min(ii + TILE, m);
            for (int kk = 0; kk < k; kk += TILE) {
                int kEnd = Math.min(kk + TILE, k);
                for (int jj = 0; jj < n; jj += TILE) {
                    int jEnd = Math.min(jj + TILE, n);
                    for (int i = ii; i < iEnd; i++) {
                        int ciBase = i * n;
                        int aiBase = i * aCols;
                        for (int p = kk; p < kEnd; p++) {
                            float aVal = alpha * a[aiBase + p];
                            int bpBase = p * bCols;
                            int j = jj;
                            int jLimit = jEnd - 3;
                            for (; j < jLimit; j += 4) {
                                c[ciBase + j]     += aVal * b[bpBase + j];
                                c[ciBase + j + 1] += aVal * b[bpBase + j + 1];
                                c[ciBase + j + 2] += aVal * b[bpBase + j + 2];
                                c[ciBase + j + 3] += aVal * b[bpBase + j + 3];
                            }
                            for (; j < jEnd; j++) {
                                c[ciBase + j] += aVal * b[bpBase + j];
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Specialized tiled GEMM for transA=false, transB=true (used by LinearOp).
     * For transB, B[p][j] = b[j * bCols + p], so we use i-j-k loop order
     * where each (i,j) accumulates a dot product along k, keeping B access sequential.
     * 针对 transA=false, transB=true（LinearOp 使用）的特化分块 GEMM。
     */
    private static void gemmTiledNT(float alpha, float[] a, int aCols,
                                     float[] b, int bCols,
                                     float[] c, int m, int n, int k) {
        // transB: op(B)[p][j] = b[j * bCols + p], i.e. row j of B is at b[j * bCols]
        for (int ii = 0; ii < m; ii += TILE) {
            int iEnd = Math.min(ii + TILE, m);
            for (int jj = 0; jj < n; jj += TILE) {
                int jEnd = Math.min(jj + TILE, n);
                for (int kk = 0; kk < k; kk += TILE) {
                    int kEnd = Math.min(kk + TILE, k);
                    for (int i = ii; i < iEnd; i++) {
                        int ciBase = i * n;
                        int aiBase = i * aCols;
                        for (int j = jj; j < jEnd; j++) {
                            int bjBase = j * bCols;  // row j of B (transposed)
                            float sum = 0.0f;
                            int p = kk;
                            int pLimit = kEnd - 3;
                            for (; p < pLimit; p += 4) {
                                sum += a[aiBase + p]     * b[bjBase + p]
                                     + a[aiBase + p + 1] * b[bjBase + p + 1]
                                     + a[aiBase + p + 2] * b[bjBase + p + 2]
                                     + a[aiBase + p + 3] * b[bjBase + p + 3];
                            }
                            for (; p < kEnd; p++) {
                                sum += a[aiBase + p] * b[bjBase + p];
                            }
                            c[ciBase + j] += alpha * sum;
                        }
                    }
                }
            }
        }
    }

    /**
     * Compute a block of rows [iStart, iEnd) for parallel GEMM.
     * 计算并行 GEMM 的行块 [iStart, iEnd)。
     */
    private static void gemmRowBlock(float alpha,
                                      float[] a, int aRows, int aCols, boolean transA,
                                      float[] b, int bRows, int bCols, boolean transB,
                                      float[] c, int n, int k,
                                      int iStart, int iEnd) {
        for (int kk = 0; kk < k; kk += TILE) {
            int kEnd = Math.min(kk + TILE, k);
            for (int jj = 0; jj < n; jj += TILE) {
                int jEndTile = Math.min(jj + TILE, n);
                for (int i = iStart; i < iEnd; i++) {
                    for (int p = kk; p < kEnd; p++) {
                        float aVal = alpha * getElement(a, aRows, aCols, transA, i, p);
                        int j = jj;
                        int jLimit = jEndTile - 3;
                        for (; j < jLimit; j += 4) {
                            c[i * n + j]     += aVal * getElement(b, bRows, bCols, transB, p, j);
                            c[i * n + j + 1] += aVal * getElement(b, bRows, bCols, transB, p, j + 1);
                            c[i * n + j + 2] += aVal * getElement(b, bRows, bCols, transB, p, j + 2);
                            c[i * n + j + 3] += aVal * getElement(b, bRows, bCols, transB, p, j + 3);
                        }
                        for (; j < jEndTile; j++) {
                            c[i * n + j] += aVal * getElement(b, bRows, bCols, transB, p, j);
                        }
                    }
                }
            }
        }
    }

    /**
     * Specialized parallel row block for transA=false, transB=false.
     * 针对 transA=false, transB=false 的特化并行行块。
     */
    private static void gemmRowBlockNN(float alpha,
                                        float[] a, int aCols,
                                        float[] b, int bCols,
                                        float[] c, int n, int k,
                                        int iStart, int iEnd) {
        for (int kk = 0; kk < k; kk += TILE) {
            int kEnd = Math.min(kk + TILE, k);
            for (int jj = 0; jj < n; jj += TILE) {
                int jEndTile = Math.min(jj + TILE, n);
                for (int i = iStart; i < iEnd; i++) {
                    int ciBase = i * n;
                    int aiBase = i * aCols;
                    for (int p = kk; p < kEnd; p++) {
                        float aVal = alpha * a[aiBase + p];
                        int bpBase = p * bCols;
                        int j = jj;
                        int jLimit = jEndTile - 3;
                        for (; j < jLimit; j += 4) {
                            c[ciBase + j]     += aVal * b[bpBase + j];
                            c[ciBase + j + 1] += aVal * b[bpBase + j + 1];
                            c[ciBase + j + 2] += aVal * b[bpBase + j + 2];
                            c[ciBase + j + 3] += aVal * b[bpBase + j + 3];
                        }
                        for (; j < jEndTile; j++) {
                            c[ciBase + j] += aVal * b[bpBase + j];
                        }
                    }
                }
            }
        }
    }

    /**
     * Specialized parallel row block for transA=false, transB=true.
     * Uses i-j-k loop order for cache-friendly access to transposed B.
     * 针对 transA=false, transB=true 的特化并行行块。
     */
    private static void gemmRowBlockNT(float alpha,
                                        float[] a, int aCols,
                                        float[] b, int bCols,
                                        float[] c, int n, int k,
                                        int iStart, int iEnd) {
        for (int jj = 0; jj < n; jj += TILE) {
            int jEndTile = Math.min(jj + TILE, n);
            for (int kk = 0; kk < k; kk += TILE) {
                int kEnd = Math.min(kk + TILE, k);
                for (int i = iStart; i < iEnd; i++) {
                    int ciBase = i * n;
                    int aiBase = i * aCols;
                    for (int j = jj; j < jEndTile; j++) {
                        int bjBase = j * bCols;
                        float sum = 0.0f;
                        int p = kk;
                        int pLimit = kEnd - 3;
                        for (; p < pLimit; p += 4) {
                            sum += a[aiBase + p]     * b[bjBase + p]
                                 + a[aiBase + p + 1] * b[bjBase + p + 1]
                                 + a[aiBase + p + 2] * b[bjBase + p + 2]
                                 + a[aiBase + p + 3] * b[bjBase + p + 3];
                        }
                        for (; p < kEnd; p++) {
                            sum += a[aiBase + p] * b[bjBase + p];
                        }
                        c[ciBase + j] += alpha * sum;
                    }
                }
            }
        }
    }

    /**
     * Get element from matrix with optional transpose.
     * 从矩阵获取元素（可选转置）。
     */
    private static float getElement(float[] mat, int rows, int cols, boolean trans,
                                     int row, int col) {
        if (trans) {
            return mat[col * cols + row];
        }
        return mat[row * cols + col];
    }

    /**
     * Validate array length for matrix dimensions.
     * 验证矩阵维度的数组长度。
     */
    private static void validateArrayLength(float[] mat, int rows, int cols, String name) {
        if (rows <= 0 || cols <= 0) {
            throw new NeuralException(
                    "Matrix " + name + " dimensions must be > 0: rows=" + rows + ", cols=" + cols,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (mat.length < (long) rows * cols) {
            throw new NeuralException(
                    "Matrix " + name + " length " + mat.length + " < rows*cols " + ((long) rows * cols),
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
    }
}
