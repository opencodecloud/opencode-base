package cloud.opencode.base.neural.internal;

import cloud.opencode.base.neural.exception.NeuralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Blas}.
 */
@DisplayName("Blas — 纯 Java GEMM 测试")
class BlasTest {

    private static final float EPSILON = 1e-5f;

    @Nested
    @DisplayName("matmul — 矩阵乘法")
    class MatmulTest {

        @Test
        @DisplayName("2x3 @ 3x2 = 已知 2x2 结果")
        void matmul2x3Times3x2() {
            // A = [[1,2,3],[4,5,6]]  B = [[7,8],[9,10],[11,12]]
            float[] a = {1, 2, 3, 4, 5, 6};
            float[] b = {7, 8, 9, 10, 11, 12};

            float[] c = Blas.matmul(a, 2, 3, b, 3, 2);

            // C = [[1*7+2*9+3*11, 1*8+2*10+3*12], [4*7+5*9+6*11, 4*8+5*10+6*12]]
            //   = [[58, 64], [139, 154]]
            assertThat(c).hasSize(4);
            assertThat(c[0]).isCloseTo(58f, within(EPSILON));
            assertThat(c[1]).isCloseTo(64f, within(EPSILON));
            assertThat(c[2]).isCloseTo(139f, within(EPSILON));
            assertThat(c[3]).isCloseTo(154f, within(EPSILON));
        }

        @Test
        @DisplayName("单位矩阵乘法: A @ I = A")
        void matmulIdentity() {
            float[] a = {1, 2, 3, 4};
            float[] identity = {1, 0, 0, 1};

            float[] c = Blas.matmul(a, 2, 2, identity, 2, 2);

            assertThat(c[0]).isCloseTo(1f, within(EPSILON));
            assertThat(c[1]).isCloseTo(2f, within(EPSILON));
            assertThat(c[2]).isCloseTo(3f, within(EPSILON));
            assertThat(c[3]).isCloseTo(4f, within(EPSILON));
        }

        @Test
        @DisplayName("单元素矩阵: [3] @ [4] = [12]")
        void matmulSingleElement() {
            float[] a = {3};
            float[] b = {4};

            float[] c = Blas.matmul(a, 1, 1, b, 1, 1);

            assertThat(c).hasSize(1);
            assertThat(c[0]).isCloseTo(12f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("gemm — 通用矩阵乘法")
    class GemmTest {

        @Test
        @DisplayName("transA=true: A^T @ B")
        void gemmTransposeA() {
            // A = [[1,4],[2,5],[3,6]] (3x2), transA -> op(A) = [[1,2,3],[4,5,6]] (2x3)
            // B = [[7,8],[9,10],[11,12]] (3x2)
            // op(A) @ B = [[58,64],[139,154]]
            float[] a = {1, 4, 2, 5, 3, 6};
            float[] b = {7, 8, 9, 10, 11, 12};
            float[] c = new float[4];

            Blas.gemm(1.0f, a, 3, 2, true, b, 3, 2, false, 0.0f, c);

            assertThat(c[0]).isCloseTo(58f, within(EPSILON));
            assertThat(c[1]).isCloseTo(64f, within(EPSILON));
            assertThat(c[2]).isCloseTo(139f, within(EPSILON));
            assertThat(c[3]).isCloseTo(154f, within(EPSILON));
        }

        @Test
        @DisplayName("transB=true: A @ B^T")
        void gemmTransposeB() {
            // A = [[1,2,3],[4,5,6]] (2x3)
            // B = [[7,9,11],[8,10,12]] (2x3), transB -> op(B) = [[7,8],[9,10],[11,12]] (3x2)
            // A @ op(B) = [[58,64],[139,154]]
            float[] a = {1, 2, 3, 4, 5, 6};
            float[] b = {7, 9, 11, 8, 10, 12};
            float[] c = new float[4];

            Blas.gemm(1.0f, a, 2, 3, false, b, 2, 3, true, 0.0f, c);

            assertThat(c[0]).isCloseTo(58f, within(EPSILON));
            assertThat(c[1]).isCloseTo(64f, within(EPSILON));
            assertThat(c[2]).isCloseTo(139f, within(EPSILON));
            assertThat(c[3]).isCloseTo(154f, within(EPSILON));
        }

        @Test
        @DisplayName("alpha 和 beta 缩放: C = 2*A@B + 3*C")
        void gemmAlphaBeta() {
            float[] a = {1, 2, 3, 4};
            float[] b = {5, 6, 7, 8};
            float[] c = {1, 1, 1, 1};

            // C = 2*(A@B) + 3*C
            // A@B = [[1*5+2*7, 1*6+2*8], [3*5+4*7, 3*6+4*8]] = [[19,22],[43,50]]
            // 2*A@B = [[38,44],[86,100]]
            // 3*C = [[3,3],[3,3]]
            // result = [[41,47],[89,103]]
            Blas.gemm(2.0f, a, 2, 2, false, b, 2, 2, false, 3.0f, c);

            assertThat(c[0]).isCloseTo(41f, within(EPSILON));
            assertThat(c[1]).isCloseTo(47f, within(EPSILON));
            assertThat(c[2]).isCloseTo(89f, within(EPSILON));
            assertThat(c[3]).isCloseTo(103f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("异常处理")
    class ExceptionTest {

        @Test
        @DisplayName("null 矩阵 A 抛出 NullPointerException")
        void nullMatrixA() {
            assertThatNullPointerException().isThrownBy(() ->
                    Blas.matmul(null, 2, 3, new float[6], 3, 2));
        }

        @Test
        @DisplayName("null 矩阵 B 抛出 NullPointerException")
        void nullMatrixB() {
            assertThatNullPointerException().isThrownBy(() ->
                    Blas.matmul(new float[6], 2, 3, null, 3, 2));
        }

        @Test
        @DisplayName("维度不匹配抛出 NeuralException")
        void dimensionMismatch() {
            float[] a = {1, 2, 3, 4, 5, 6};
            float[] b = {1, 2, 3, 4};

            assertThatThrownBy(() -> Blas.matmul(a, 2, 3, b, 2, 2))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("mismatch");
        }
    }
}
