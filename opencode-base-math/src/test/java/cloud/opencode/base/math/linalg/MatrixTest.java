package cloud.opencode.base.math.linalg;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Matrix}.
 */
@DisplayName("Matrix - 不可变数学矩阵测试")
class MatrixTest {

    @Nested
    @DisplayName("创建")
    class Creation {

        @Test
        @DisplayName("of 创建矩阵")
        void ofCreatesMatrix() {
            Matrix m = Matrix.of(new double[][]{
                    {1, 2, 3},
                    {4, 5, 6}
            });
            assertThat(m.rows()).isEqualTo(2);
            assertThat(m.cols()).isEqualTo(3);
            assertThat(m.get(0, 0)).isEqualTo(1.0);
            assertThat(m.get(1, 2)).isEqualTo(6.0);
        }

        @Test
        @DisplayName("of 防御性拷贝")
        void ofDefensiveCopy() {
            double[][] data = {{1, 2}, {3, 4}};
            Matrix m = Matrix.of(data);
            data[0][0] = 999;
            assertThat(m.get(0, 0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("of null 抛出异常")
        void ofNullThrows() {
            assertThatThrownBy(() -> Matrix.of(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of 空数组抛出异常")
        void ofEmptyThrows() {
            assertThatThrownBy(() -> Matrix.of(new double[0][0]))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("of 非矩形数组抛出异常")
        void ofNonRectangularThrows() {
            assertThatThrownBy(() -> Matrix.of(new double[][]{{1, 2}, {3}}))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("of null 行抛出异常")
        void ofNullRowThrows() {
            assertThatThrownBy(() -> Matrix.of(new double[][]{{1, 2}, null}))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of 空列抛出异常")
        void ofEmptyColsThrows() {
            assertThatThrownBy(() -> Matrix.of(new double[][]{{}}))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("identity 创建单位矩阵")
        void identityCreates() {
            Matrix I = Matrix.identity(3);
            assertThat(I.rows()).isEqualTo(3);
            assertThat(I.cols()).isEqualTo(3);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    assertThat(I.get(i, j)).isEqualTo(i == j ? 1.0 : 0.0);
                }
            }
        }

        @Test
        @DisplayName("identity 非正阶数抛出异常")
        void identityNonPositiveThrows() {
            assertThatThrownBy(() -> Matrix.identity(0)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> Matrix.identity(-1)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("zero 创建零矩阵")
        void zeroCreates() {
            Matrix z = Matrix.zero(2, 3);
            assertThat(z.rows()).isEqualTo(2);
            assertThat(z.cols()).isEqualTo(3);
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    assertThat(z.get(i, j)).isEqualTo(0.0);
                }
            }
        }

        @Test
        @DisplayName("zero 非正维度抛出异常")
        void zeroNonPositiveThrows() {
            assertThatThrownBy(() -> Matrix.zero(0, 1)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> Matrix.zero(1, 0)).isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("元素访问")
    class Access {

        @Test
        @DisplayName("get 索引越界抛出异常")
        void getOutOfBoundsThrows() {
            Matrix m = Matrix.identity(2);
            assertThatThrownBy(() -> m.get(-1, 0)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> m.get(0, -1)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> m.get(2, 0)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> m.get(0, 2)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("toArray 防御性拷贝")
        void toArrayDefensiveCopy() {
            Matrix m = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            double[][] arr = m.toArray();
            arr[0][0] = 999;
            assertThat(m.get(0, 0)).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("算术运算")
    class Arithmetic {

        @Test
        @DisplayName("矩阵加法")
        void add() {
            Matrix a = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix b = Matrix.of(new double[][]{{5, 6}, {7, 8}});
            Matrix c = a.add(b);
            assertThat(c.get(0, 0)).isEqualTo(6.0);
            assertThat(c.get(0, 1)).isEqualTo(8.0);
            assertThat(c.get(1, 0)).isEqualTo(10.0);
            assertThat(c.get(1, 1)).isEqualTo(12.0);
        }

        @Test
        @DisplayName("矩阵减法")
        void subtract() {
            Matrix a = Matrix.of(new double[][]{{5, 6}, {7, 8}});
            Matrix b = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix c = a.subtract(b);
            assertThat(c.get(0, 0)).isEqualTo(4.0);
            assertThat(c.get(0, 1)).isEqualTo(4.0);
            assertThat(c.get(1, 0)).isEqualTo(4.0);
            assertThat(c.get(1, 1)).isEqualTo(4.0);
        }

        @Test
        @DisplayName("维度不匹配加法抛出异常")
        void addDimensionMismatchThrows() {
            Matrix a = Matrix.of(new double[][]{{1, 2}});
            Matrix b = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            assertThatThrownBy(() -> a.add(b)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("标量乘法")
        void scalarMultiply() {
            Matrix m = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix r = m.scalarMultiply(3.0);
            assertThat(r.get(0, 0)).isEqualTo(3.0);
            assertThat(r.get(0, 1)).isEqualTo(6.0);
            assertThat(r.get(1, 0)).isEqualTo(9.0);
            assertThat(r.get(1, 1)).isEqualTo(12.0);
        }

        @Test
        @DisplayName("null 参数抛出异常")
        void nullArgumentThrows() {
            Matrix m = Matrix.identity(2);
            assertThatThrownBy(() -> m.add(null)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> m.subtract(null)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> m.multiply(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("矩阵乘法")
    class Multiplication {

        @Test
        @DisplayName("方阵乘法")
        void squareMultiply() {
            Matrix a = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix b = Matrix.of(new double[][]{{5, 6}, {7, 8}});
            Matrix c = a.multiply(b);
            assertThat(c.get(0, 0)).isCloseTo(19.0, within(1e-10));
            assertThat(c.get(0, 1)).isCloseTo(22.0, within(1e-10));
            assertThat(c.get(1, 0)).isCloseTo(43.0, within(1e-10));
            assertThat(c.get(1, 1)).isCloseTo(50.0, within(1e-10));
        }

        @Test
        @DisplayName("非方阵乘法")
        void nonSquareMultiply() {
            Matrix a = Matrix.of(new double[][]{{1, 2, 3}, {4, 5, 6}});  // 2x3
            Matrix b = Matrix.of(new double[][]{{7, 8}, {9, 10}, {11, 12}});  // 3x2
            Matrix c = a.multiply(b);  // 2x2
            assertThat(c.rows()).isEqualTo(2);
            assertThat(c.cols()).isEqualTo(2);
            assertThat(c.get(0, 0)).isCloseTo(58.0, within(1e-10));
            assertThat(c.get(0, 1)).isCloseTo(64.0, within(1e-10));
            assertThat(c.get(1, 0)).isCloseTo(139.0, within(1e-10));
            assertThat(c.get(1, 1)).isCloseTo(154.0, within(1e-10));
        }

        @Test
        @DisplayName("内部维度不匹配抛出异常")
        void innerDimensionMismatchThrows() {
            Matrix a = Matrix.of(new double[][]{{1, 2}});  // 1x2
            Matrix b = Matrix.of(new double[][]{{1, 2}});  // 1x2
            assertThatThrownBy(() -> a.multiply(b)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("单位矩阵乘法恒等")
        void identityMultiply() {
            Matrix a = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix I = Matrix.identity(2);
            Matrix ai = a.multiply(I);
            Matrix ia = I.multiply(a);
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    assertThat(ai.get(i, j)).isCloseTo(a.get(i, j), within(1e-10));
                    assertThat(ia.get(i, j)).isCloseTo(a.get(i, j), within(1e-10));
                }
            }
        }

        @Test
        @DisplayName("矩阵-向量乘法")
        void multiplyVector() {
            Matrix m = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Vector v = Vector.of(5.0, 6.0);
            Vector r = m.multiplyVector(v);
            assertThat(r.dimension()).isEqualTo(2);
            assertThat(r.get(0)).isCloseTo(17.0, within(1e-10));
            assertThat(r.get(1)).isCloseTo(39.0, within(1e-10));
        }

        @Test
        @DisplayName("矩阵-向量维度不匹配抛出异常")
        void multiplyVectorDimensionMismatchThrows() {
            Matrix m = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Vector v = Vector.of(1.0, 2.0, 3.0);
            assertThatThrownBy(() -> m.multiplyVector(v)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("矩阵-向量 null 抛出异常")
        void multiplyVectorNullThrows() {
            Matrix m = Matrix.identity(2);
            assertThatThrownBy(() -> m.multiplyVector(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("转置")
    class Transpose {

        @Test
        @DisplayName("方阵转置")
        void squareTranspose() {
            Matrix m = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix t = m.transpose();
            assertThat(t.get(0, 0)).isEqualTo(1.0);
            assertThat(t.get(0, 1)).isEqualTo(3.0);
            assertThat(t.get(1, 0)).isEqualTo(2.0);
            assertThat(t.get(1, 1)).isEqualTo(4.0);
        }

        @Test
        @DisplayName("非方阵转置")
        void nonSquareTranspose() {
            Matrix m = Matrix.of(new double[][]{{1, 2, 3}, {4, 5, 6}});  // 2x3
            Matrix t = m.transpose();  // 3x2
            assertThat(t.rows()).isEqualTo(3);
            assertThat(t.cols()).isEqualTo(2);
            assertThat(t.get(0, 0)).isEqualTo(1.0);
            assertThat(t.get(2, 1)).isEqualTo(6.0);
        }

        @Test
        @DisplayName("双重转置等于自身")
        void doubleTranspose() {
            Matrix m = Matrix.of(new double[][]{{1, 2, 3}, {4, 5, 6}});
            assertThat(m.transpose().transpose()).isEqualTo(m);
        }
    }

    @Nested
    @DisplayName("行列式")
    class Determinant {

        @Test
        @DisplayName("1x1 行列式")
        void det1x1() {
            Matrix m = Matrix.of(new double[][]{{5}});
            assertThat(m.determinant()).isCloseTo(5.0, within(1e-10));
        }

        @Test
        @DisplayName("2x2 行列式")
        void det2x2() {
            Matrix m = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            assertThat(m.determinant()).isCloseTo(-2.0, within(1e-10));
        }

        @Test
        @DisplayName("3x3 行列式")
        void det3x3() {
            Matrix m = Matrix.of(new double[][]{
                    {6, 1, 1},
                    {4, -2, 5},
                    {2, 8, 7}
            });
            assertThat(m.determinant()).isCloseTo(-306.0, within(1e-10));
        }

        @Test
        @DisplayName("4x4 行列式（LU分解路径）")
        void det4x4() {
            Matrix m = Matrix.of(new double[][]{
                    {1, 2, 3, 4},
                    {5, 6, 7, 8},
                    {2, 6, 4, 8},
                    {3, 1, 1, 2}
            });
            // Known determinant
            assertThat(m.determinant()).isCloseTo(72.0, within(1e-8));
        }

        @Test
        @DisplayName("单位矩阵行列式为1")
        void detIdentity() {
            assertThat(Matrix.identity(5).determinant()).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("奇异矩阵行列式为0")
        void detSingular() {
            Matrix m = Matrix.of(new double[][]{
                    {1, 2, 3},
                    {4, 5, 6},
                    {7, 8, 9}
            });
            assertThat(m.determinant()).isCloseTo(0.0, within(1e-8));
        }

        @Test
        @DisplayName("非方阵行列式抛出异常")
        void detNonSquareThrows() {
            Matrix m = Matrix.of(new double[][]{{1, 2, 3}, {4, 5, 6}});
            assertThatThrownBy(m::determinant).isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("逆矩阵")
    class Inverse {

        @Test
        @DisplayName("2x2 逆矩阵")
        void inverse2x2() {
            Matrix m = Matrix.of(new double[][]{{4, 7}, {2, 6}});
            Matrix inv = m.inverse();
            // det = 24 - 14 = 10
            assertThat(inv.get(0, 0)).isCloseTo(0.6, within(1e-10));
            assertThat(inv.get(0, 1)).isCloseTo(-0.7, within(1e-10));
            assertThat(inv.get(1, 0)).isCloseTo(-0.2, within(1e-10));
            assertThat(inv.get(1, 1)).isCloseTo(0.4, within(1e-10));
        }

        @Test
        @DisplayName("3x3 逆矩阵")
        void inverse3x3() {
            Matrix m = Matrix.of(new double[][]{
                    {1, 2, 3},
                    {0, 1, 4},
                    {5, 6, 0}
            });
            Matrix inv = m.inverse();
            // Verify A * A^-1 = I
            Matrix product = m.multiply(inv);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    double expected = (i == j) ? 1.0 : 0.0;
                    assertThat(product.get(i, j)).isCloseTo(expected, within(1e-10));
                }
            }
        }

        @Test
        @DisplayName("A * A^-1 = I 恒等性")
        void inverseTimesOriginalIsIdentity() {
            Matrix m = Matrix.of(new double[][]{
                    {2, 1, 1},
                    {1, 3, 2},
                    {1, 0, 0}
            });
            Matrix inv = m.inverse();
            Matrix product = m.multiply(inv);
            Matrix I = Matrix.identity(3);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    assertThat(product.get(i, j)).isCloseTo(I.get(i, j), within(1e-10));
                }
            }
        }

        @Test
        @DisplayName("单位矩阵的逆是自身")
        void inverseOfIdentity() {
            Matrix I = Matrix.identity(4);
            Matrix inv = I.inverse();
            assertThat(inv).isEqualTo(I);
        }

        @Test
        @DisplayName("奇异矩阵求逆抛出异常")
        void inverseSingularThrows() {
            Matrix m = Matrix.of(new double[][]{
                    {1, 2},
                    {2, 4}
            });
            assertThatThrownBy(m::inverse).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("非方阵求逆抛出异常")
        void inverseNonSquareThrows() {
            Matrix m = Matrix.of(new double[][]{{1, 2, 3}});
            assertThatThrownBy(m::inverse).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("4x4 逆矩阵验证")
        void inverse4x4() {
            Matrix m = Matrix.of(new double[][]{
                    {1, 0, 2, -1},
                    {3, 0, 0, 5},
                    {2, 1, 4, -3},
                    {1, 0, 5, 0}
            });
            Matrix inv = m.inverse();
            Matrix product = m.multiply(inv);
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    double expected = (i == j) ? 1.0 : 0.0;
                    assertThat(product.get(i, j)).isCloseTo(expected, within(1e-9));
                }
            }
        }
    }

    @Nested
    @DisplayName("迹")
    class Trace {

        @Test
        @DisplayName("迹计算")
        void traceComputation() {
            Matrix m = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            assertThat(m.trace()).isCloseTo(5.0, within(1e-10));
        }

        @Test
        @DisplayName("单位矩阵的迹等于阶数")
        void traceOfIdentity() {
            assertThat(Matrix.identity(5).trace()).isCloseTo(5.0, within(1e-10));
        }

        @Test
        @DisplayName("非方阵求迹抛出异常")
        void traceNonSquareThrows() {
            Matrix m = Matrix.of(new double[][]{{1, 2, 3}});
            assertThatThrownBy(m::trace).isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("属性判断")
    class Properties {

        @Test
        @DisplayName("isSquare 判断")
        void isSquare() {
            assertThat(Matrix.identity(3).isSquare()).isTrue();
            assertThat(Matrix.of(new double[][]{{1, 2, 3}}).isSquare()).isFalse();
        }

        @Test
        @DisplayName("isSymmetric 对称矩阵")
        void isSymmetricTrue() {
            Matrix m = Matrix.of(new double[][]{
                    {1, 2, 3},
                    {2, 4, 5},
                    {3, 5, 6}
            });
            assertThat(m.isSymmetric()).isTrue();
        }

        @Test
        @DisplayName("isSymmetric 非对称矩阵")
        void isSymmetricFalse() {
            Matrix m = Matrix.of(new double[][]{
                    {1, 2},
                    {3, 4}
            });
            assertThat(m.isSymmetric()).isFalse();
        }

        @Test
        @DisplayName("isSymmetric 非方阵返回 false")
        void isSymmetricNonSquare() {
            Matrix m = Matrix.of(new double[][]{{1, 2, 3}, {4, 5, 6}});
            assertThat(m.isSymmetric()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals / hashCode / toString")
    class EqualsHashCodeToString {

        @Test
        @DisplayName("相等矩阵")
        void equalsTrue() {
            Matrix a = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix b = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不等矩阵")
        void equalsFalse() {
            Matrix a = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix b = Matrix.of(new double[][]{{1, 2}, {3, 5}});
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("不同维度不等")
        void equalsDifferentDimensions() {
            Matrix a = Matrix.of(new double[][]{{1, 2}});
            Matrix b = Matrix.of(new double[][]{{1}, {2}});
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("与非 Matrix 对象不等")
        void equalsNonMatrix() {
            Matrix m = Matrix.identity(2);
            assertThat(m).isNotEqualTo("not a matrix");
            assertThat(m).isNotEqualTo(null);
        }

        @Test
        @DisplayName("toString 包含维度信息")
        void toStringContainsDimensions() {
            Matrix m = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            String s = m.toString();
            assertThat(s).contains("Matrix");
            assertThat(s).contains("2x2");
        }
    }

    @Nested
    @DisplayName("综合性质验证")
    class IntegrationProperties {

        @Test
        @DisplayName("(A*B)^T = B^T * A^T")
        void transposeOfProduct() {
            Matrix a = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix b = Matrix.of(new double[][]{{5, 6}, {7, 8}});
            Matrix abT = a.multiply(b).transpose();
            Matrix bTaT = b.transpose().multiply(a.transpose());
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    assertThat(abT.get(i, j)).isCloseTo(bTaT.get(i, j), within(1e-10));
                }
            }
        }

        @Test
        @DisplayName("det(A*B) = det(A) * det(B)")
        void determinantOfProduct() {
            Matrix a = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix b = Matrix.of(new double[][]{{5, 6}, {7, 8}});
            double detAB = a.multiply(b).determinant();
            double detA_detB = a.determinant() * b.determinant();
            assertThat(detAB).isCloseTo(detA_detB, within(1e-10));
        }

        @Test
        @DisplayName("标量乘法结合律 c*(A+B) = c*A + c*B")
        void scalarDistributive() {
            Matrix a = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix b = Matrix.of(new double[][]{{5, 6}, {7, 8}});
            double c = 3.0;
            Matrix left = a.add(b).scalarMultiply(c);
            Matrix right = a.scalarMultiply(c).add(b.scalarMultiply(c));
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    assertThat(left.get(i, j)).isCloseTo(right.get(i, j), within(1e-10));
                }
            }
        }

        @Test
        @DisplayName("零矩阵加法恒等")
        void zeroAddIdentity() {
            Matrix a = Matrix.of(new double[][]{{1, 2}, {3, 4}});
            Matrix z = Matrix.zero(2, 2);
            assertThat(a.add(z)).isEqualTo(a);
        }

        @Test
        @DisplayName("1x1 矩阵完整操作")
        void oneByOneMatrix() {
            Matrix m = Matrix.of(new double[][]{{5}});
            assertThat(m.determinant()).isCloseTo(5.0, within(1e-10));
            assertThat(m.trace()).isCloseTo(5.0, within(1e-10));
            assertThat(m.isSquare()).isTrue();
            assertThat(m.isSymmetric()).isTrue();
            Matrix inv = m.inverse();
            assertThat(inv.get(0, 0)).isCloseTo(0.2, within(1e-10));
        }
    }
}
