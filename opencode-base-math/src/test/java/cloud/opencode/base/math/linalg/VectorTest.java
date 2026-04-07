package cloud.opencode.base.math.linalg;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Vector}.
 */
@DisplayName("Vector - 不可变数学向量测试")
class VectorTest {

    @Nested
    @DisplayName("创建")
    class Creation {

        @Test
        @DisplayName("of 创建向量")
        void ofCreatesVector() {
            Vector v = Vector.of(1.0, 2.0, 3.0);
            assertThat(v.dimension()).isEqualTo(3);
            assertThat(v.get(0)).isEqualTo(1.0);
            assertThat(v.get(1)).isEqualTo(2.0);
            assertThat(v.get(2)).isEqualTo(3.0);
        }

        @Test
        @DisplayName("of 防御性拷贝")
        void ofDefensiveCopy() {
            double[] arr = {1.0, 2.0};
            Vector v = Vector.of(arr);
            arr[0] = 999.0;
            assertThat(v.get(0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("of 空数组抛出异常")
        void ofEmptyThrows() {
            assertThatThrownBy(() -> Vector.of(new double[0]))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("of null 抛出异常")
        void ofNullThrows() {
            assertThatThrownBy(() -> Vector.of((double[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("zero 创建零向量")
        void zeroCreatesZeroVector() {
            Vector v = Vector.zero(4);
            assertThat(v.dimension()).isEqualTo(4);
            for (int i = 0; i < 4; i++) {
                assertThat(v.get(i)).isEqualTo(0.0);
            }
        }

        @Test
        @DisplayName("zero 非正维度抛出异常")
        void zeroNonPositiveThrows() {
            assertThatThrownBy(() -> Vector.zero(0)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> Vector.zero(-1)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("unit 创建单位向量")
        void unitCreatesUnitVector() {
            Vector v = Vector.unit(3, 1);
            assertThat(v.get(0)).isEqualTo(0.0);
            assertThat(v.get(1)).isEqualTo(1.0);
            assertThat(v.get(2)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("unit 索引越界抛出异常")
        void unitOutOfBoundsThrows() {
            assertThatThrownBy(() -> Vector.unit(3, 3)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> Vector.unit(3, -1)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("一维向量")
        void oneDimensional() {
            Vector v = Vector.of(42.0);
            assertThat(v.dimension()).isEqualTo(1);
            assertThat(v.get(0)).isEqualTo(42.0);
        }
    }

    @Nested
    @DisplayName("元素访问")
    class Access {

        @Test
        @DisplayName("get 索引越界抛出异常")
        void getOutOfBoundsThrows() {
            Vector v = Vector.of(1.0, 2.0);
            assertThatThrownBy(() -> v.get(-1)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> v.get(2)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("toArray 防御性拷贝")
        void toArrayDefensiveCopy() {
            Vector v = Vector.of(1.0, 2.0);
            double[] arr = v.toArray();
            arr[0] = 999.0;
            assertThat(v.get(0)).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("算术运算")
    class Arithmetic {

        @Test
        @DisplayName("向量加法")
        void add() {
            Vector a = Vector.of(1.0, 2.0, 3.0);
            Vector b = Vector.of(4.0, 5.0, 6.0);
            Vector c = a.add(b);
            assertThat(c.get(0)).isEqualTo(5.0);
            assertThat(c.get(1)).isEqualTo(7.0);
            assertThat(c.get(2)).isEqualTo(9.0);
        }

        @Test
        @DisplayName("向量减法")
        void subtract() {
            Vector a = Vector.of(4.0, 5.0, 6.0);
            Vector b = Vector.of(1.0, 2.0, 3.0);
            Vector c = a.subtract(b);
            assertThat(c.get(0)).isEqualTo(3.0);
            assertThat(c.get(1)).isEqualTo(3.0);
            assertThat(c.get(2)).isEqualTo(3.0);
        }

        @Test
        @DisplayName("维度不匹配加法抛出异常")
        void addDimensionMismatchThrows() {
            Vector a = Vector.of(1.0, 2.0);
            Vector b = Vector.of(1.0, 2.0, 3.0);
            assertThatThrownBy(() -> a.add(b)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("维度不匹配减法抛出异常")
        void subtractDimensionMismatchThrows() {
            Vector a = Vector.of(1.0, 2.0);
            Vector b = Vector.of(1.0, 2.0, 3.0);
            assertThatThrownBy(() -> a.subtract(b)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("标量乘法")
        void scale() {
            Vector v = Vector.of(1.0, -2.0, 3.0);
            Vector s = v.scale(2.0);
            assertThat(s.get(0)).isEqualTo(2.0);
            assertThat(s.get(1)).isEqualTo(-4.0);
            assertThat(s.get(2)).isEqualTo(6.0);
        }

        @Test
        @DisplayName("零标量乘法")
        void scaleByZero() {
            Vector v = Vector.of(1.0, 2.0, 3.0);
            Vector z = v.scale(0.0);
            for (int i = 0; i < z.dimension(); i++) {
                assertThat(z.get(i)).isEqualTo(0.0);
            }
        }

        @Test
        @DisplayName("取反")
        void negate() {
            Vector v = Vector.of(1.0, -2.0, 0.0);
            Vector n = v.negate();
            assertThat(n.get(0)).isEqualTo(-1.0);
            assertThat(n.get(1)).isEqualTo(2.0);
            assertThat(n.get(2)).isEqualTo(-0.0);
        }

        @Test
        @DisplayName("null 参数抛出异常")
        void nullArgumentThrows() {
            Vector v = Vector.of(1.0);
            assertThatThrownBy(() -> v.add(null)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> v.subtract(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("点积与叉积")
    class DotAndCross {

        @Test
        @DisplayName("点积计算")
        void dot() {
            Vector a = Vector.of(1.0, 2.0, 3.0);
            Vector b = Vector.of(4.0, 5.0, 6.0);
            assertThat(a.dot(b)).isCloseTo(32.0, within(1e-10));
        }

        @Test
        @DisplayName("正交向量点积为零")
        void dotOrthogonal() {
            Vector a = Vector.of(1.0, 0.0, 0.0);
            Vector b = Vector.of(0.0, 1.0, 0.0);
            assertThat(a.dot(b)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("点积维度不匹配抛出异常")
        void dotDimensionMismatchThrows() {
            Vector a = Vector.of(1.0, 2.0);
            Vector b = Vector.of(1.0, 2.0, 3.0);
            assertThatThrownBy(() -> a.dot(b)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("叉积计算")
        void cross() {
            Vector a = Vector.of(1.0, 0.0, 0.0);
            Vector b = Vector.of(0.0, 1.0, 0.0);
            Vector c = a.cross(b);
            assertThat(c.get(0)).isCloseTo(0.0, within(1e-10));
            assertThat(c.get(1)).isCloseTo(0.0, within(1e-10));
            assertThat(c.get(2)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("叉积反交换律")
        void crossAntiCommutative() {
            Vector a = Vector.of(1.0, 2.0, 3.0);
            Vector b = Vector.of(4.0, 5.0, 6.0);
            Vector ab = a.cross(b);
            Vector ba = b.cross(a);
            for (int i = 0; i < 3; i++) {
                assertThat(ab.get(i)).isCloseTo(-ba.get(i), within(1e-10));
            }
        }

        @Test
        @DisplayName("叉积自身为零")
        void crossSelfIsZero() {
            Vector a = Vector.of(1.0, 2.0, 3.0);
            Vector c = a.cross(a);
            for (int i = 0; i < 3; i++) {
                assertThat(c.get(i)).isCloseTo(0.0, within(1e-10));
            }
        }

        @Test
        @DisplayName("非三维叉积抛出异常")
        void crossNon3DThrows() {
            Vector a = Vector.of(1.0, 2.0);
            Vector b = Vector.of(3.0, 4.0);
            assertThatThrownBy(() -> a.cross(b)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("叉积一方非三维抛出异常")
        void crossMixed3DThrows() {
            Vector a = Vector.of(1.0, 2.0, 3.0);
            Vector b = Vector.of(1.0, 2.0);
            assertThatThrownBy(() -> a.cross(b)).isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("范数与归一化")
    class NormAndNormalize {

        @Test
        @DisplayName("模长计算")
        void magnitude() {
            Vector v = Vector.of(3.0, 4.0);
            assertThat(v.magnitude()).isCloseTo(5.0, within(1e-10));
        }

        @Test
        @DisplayName("零向量模长为零")
        void magnitudeZero() {
            assertThat(Vector.zero(3).magnitude()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("单位向量模长为1")
        void unitMagnitude() {
            Vector v = Vector.unit(5, 2);
            assertThat(v.magnitude()).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("归一化向量模长为1")
        void normalizeHasUnitMagnitude() {
            Vector v = Vector.of(3.0, 4.0);
            Vector n = v.normalize();
            assertThat(n.magnitude()).isCloseTo(1.0, within(1e-10));
            assertThat(n.get(0)).isCloseTo(0.6, within(1e-10));
            assertThat(n.get(1)).isCloseTo(0.8, within(1e-10));
        }

        @Test
        @DisplayName("零向量归一化抛出异常")
        void normalizeZeroThrows() {
            assertThatThrownBy(() -> Vector.zero(3).normalize())
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("角度与距离")
    class AngleAndDistance {

        @Test
        @DisplayName("正交向量夹角为 PI/2")
        void angleOrthogonal() {
            Vector a = Vector.of(1.0, 0.0);
            Vector b = Vector.of(0.0, 1.0);
            assertThat(a.angle(b)).isCloseTo(Math.PI / 2, within(1e-10));
        }

        @Test
        @DisplayName("平行向量夹角为 0")
        void angleParallel() {
            Vector a = Vector.of(1.0, 2.0);
            Vector b = Vector.of(2.0, 4.0);
            assertThat(a.angle(b)).isCloseTo(0.0, within(1e-7));
        }

        @Test
        @DisplayName("反平行向量夹角为 PI")
        void angleAntiParallel() {
            Vector a = Vector.of(1.0, 0.0);
            Vector b = Vector.of(-1.0, 0.0);
            assertThat(a.angle(b)).isCloseTo(Math.PI, within(1e-10));
        }

        @Test
        @DisplayName("零向量夹角抛出异常")
        void angleZeroVectorThrows() {
            Vector a = Vector.of(1.0, 2.0);
            Vector z = Vector.zero(2);
            assertThatThrownBy(() -> a.angle(z)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> z.angle(a)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("维度不匹配夹角抛出异常")
        void angleDimensionMismatchThrows() {
            Vector a = Vector.of(1.0, 2.0);
            Vector b = Vector.of(1.0, 2.0, 3.0);
            assertThatThrownBy(() -> a.angle(b)).isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("欧几里得距离")
        void distanceTo() {
            Vector a = Vector.of(1.0, 0.0);
            Vector b = Vector.of(4.0, 4.0);
            assertThat(a.distanceTo(b)).isCloseTo(5.0, within(1e-10));
        }

        @Test
        @DisplayName("自身距离为零")
        void distanceToSelf() {
            Vector v = Vector.of(1.0, 2.0, 3.0);
            assertThat(v.distanceTo(v)).isCloseTo(0.0, within(1e-10));
        }
    }

    @Nested
    @DisplayName("equals / hashCode / toString")
    class EqualsHashCodeToString {

        @Test
        @DisplayName("相等向量")
        void equalsTrue() {
            Vector a = Vector.of(1.0, 2.0, 3.0);
            Vector b = Vector.of(1.0, 2.0, 3.0);
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不等向量")
        void equalsFalse() {
            Vector a = Vector.of(1.0, 2.0);
            Vector b = Vector.of(1.0, 3.0);
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("不同维度不等")
        void equalsDifferentDimension() {
            Vector a = Vector.of(1.0, 2.0);
            Vector b = Vector.of(1.0, 2.0, 3.0);
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("与非 Vector 对象不等")
        void equalsNonVector() {
            Vector a = Vector.of(1.0);
            assertThat(a).isNotEqualTo("not a vector");
            assertThat(a).isNotEqualTo(null);
        }

        @Test
        @DisplayName("toString 包含分量")
        void toStringContainsComponents() {
            Vector v = Vector.of(1.0, 2.0);
            assertThat(v.toString()).contains("Vector");
            assertThat(v.toString()).contains("1.0");
            assertThat(v.toString()).contains("2.0");
        }
    }

    @Nested
    @DisplayName("高维向量")
    class HighDimensional {

        @Test
        @DisplayName("高维向量运算")
        void highDimensionalOperations() {
            int dim = 1000;
            double[] a = new double[dim];
            double[] b = new double[dim];
            for (int i = 0; i < dim; i++) {
                a[i] = i;
                b[i] = dim - i;
            }
            Vector va = Vector.of(a);
            Vector vb = Vector.of(b);

            Vector sum = va.add(vb);
            for (int i = 0; i < dim; i++) {
                assertThat(sum.get(i)).isCloseTo(dim, within(1e-10));
            }

            assertThat(va.dimension()).isEqualTo(dim);
        }

        @Test
        @DisplayName("高维点积")
        void highDimensionalDot() {
            Vector a = Vector.of(1.0, 1.0, 1.0, 1.0, 1.0);
            Vector b = Vector.of(2.0, 2.0, 2.0, 2.0, 2.0);
            assertThat(a.dot(b)).isCloseTo(10.0, within(1e-10));
        }
    }
}
