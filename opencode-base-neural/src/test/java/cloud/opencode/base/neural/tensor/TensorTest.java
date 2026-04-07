package cloud.opencode.base.neural.tensor;

import cloud.opencode.base.neural.exception.TensorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link Tensor}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("Tensor — 核心张量类")
class TensorTest {

    private static final float EPSILON = 1e-6f;

    @Nested
    @DisplayName("创建与数据访问")
    class CreationTest {

        @Test
        @DisplayName("fromFloat → toFloatArray 往返一致")
        void fromFloatRoundTrip() {
            float[] data = {1, 2, 3, 4, 5, 6};
            Tensor t = Tensor.fromFloat(data, Shape.of(2, 3));
            assertThat(t.toFloatArray()).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        @DisplayName("fromFloat 复制数据，修改原数组不影响张量")
        void fromFloatCopiesData() {
            float[] data = {1, 2, 3};
            Tensor t = Tensor.fromFloat(data, Shape.of(3));
            data[0] = 999;
            assertThat(t.getFloat(0)).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("zeros 创建全零张量")
        void zerosCreatesZeroTensor() {
            Tensor t = Tensor.zeros(Shape.of(2, 3));
            assertThat(t.size()).isEqualTo(6);
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    assertThat(t.getFloat(i, j)).isEqualTo(0.0f);
                }
            }
        }

        @Test
        @DisplayName("ones 创建全一张量")
        void onesCreatesOneTensor() {
            Tensor t = Tensor.ones(Shape.of(3, 2));
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 2; j++) {
                    assertThat(t.getFloat(i, j)).isEqualTo(1.0f);
                }
            }
        }

        @Test
        @DisplayName("getFloat / setFloat 正确读写")
        void getSetFloat() {
            Tensor t = Tensor.zeros(Shape.of(2, 3));
            t.setFloat(42.0f, 1, 2);
            assertThat(t.getFloat(1, 2)).isEqualTo(42.0f);
            assertThat(t.getFloat(0, 0)).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("数据长度与形状不匹配应抛出异常")
        void dataSizeMismatchShouldThrow() {
            assertThatThrownBy(() -> Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(2, 3)))
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("索引越界应抛出异常")
        void indexOutOfBoundsShouldThrow() {
            Tensor t = Tensor.zeros(Shape.of(2, 3));
            assertThatThrownBy(() -> t.getFloat(2, 0))
                    .isInstanceOf(TensorException.class);
            assertThatThrownBy(() -> t.getFloat(0, 3))
                    .isInstanceOf(TensorException.class);
            assertThatThrownBy(() -> t.getFloat(-1, 0))
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("type 返回 FLOAT32")
        void typeIsFloat32() {
            Tensor t = Tensor.zeros(Shape.of(1));
            assertThat(t.type()).isEqualTo(TensorType.FLOAT32);
        }

        @Test
        @DisplayName("null 数据应抛出异常")
        void nullDataShouldThrow() {
            assertThatThrownBy(() -> Tensor.fromFloat(null, Shape.of(1)))
                    .isInstanceOf(TensorException.class);
        }
    }

    @Nested
    @DisplayName("reshape / transpose / flatten")
    class TransformTest {

        @Test
        @DisplayName("reshape 共享数据")
        void reshapeSharesData() {
            float[] data = {1, 2, 3, 4, 5, 6};
            Tensor t = Tensor.fromFloat(data, Shape.of(2, 3));
            Tensor reshaped = t.reshape(3, 2);
            assertThat(reshaped.shape().dims()).containsExactly(3, 2);
            assertThat(reshaped.getFloat(0, 0)).isEqualTo(1.0f);
            assertThat(reshaped.getFloat(2, 1)).isEqualTo(6.0f);

            // Shared data: modifying one affects the other
            t.setFloat(99.0f, 0, 0);
            assertThat(reshaped.getFloat(0, 0)).isEqualTo(99.0f);
        }

        @Test
        @DisplayName("flatten 展平为一维")
        void flattenTo1D() {
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(2, 3));
            Tensor flat = t.flatten();
            assertThat(flat.shape().rank()).isEqualTo(1);
            assertThat(flat.shape().dim(0)).isEqualTo(6);
            assertThat(flat.toFloatArray()).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        @DisplayName("transpose 2D")
        void transpose2D() {
            // [[1,2,3],[4,5,6]] -> [[1,4],[2,5],[3,6]]
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(2, 3));
            Tensor transposed = t.transpose(1, 0);
            assertThat(transposed.shape().dims()).containsExactly(3, 2);
            assertThat(transposed.getFloat(0, 0)).isEqualTo(1.0f);
            assertThat(transposed.getFloat(0, 1)).isEqualTo(4.0f);
            assertThat(transposed.getFloat(1, 0)).isEqualTo(2.0f);
            assertThat(transposed.getFloat(2, 1)).isEqualTo(6.0f);
        }

        @Test
        @DisplayName("transpose 后 toFloatArray 正确")
        void transposeToFloatArray() {
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(2, 3));
            Tensor transposed = t.transpose(1, 0);
            // Row-major of transposed: [1,4,2,5,3,6]
            assertThat(transposed.toFloatArray()).containsExactly(1, 4, 2, 5, 3, 6);
        }

        @Test
        @DisplayName("squeeze 和 unsqueeze")
        void squeezeUnsqueeze() {
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(1, 3));
            Tensor squeezed = t.squeeze(0);
            assertThat(squeezed.shape().dims()).containsExactly(3);
            assertThat(squeezed.toFloatArray()).containsExactly(1, 2, 3);

            Tensor unsqueezed = squeezed.unsqueeze(0);
            assertThat(unsqueezed.shape().dims()).containsExactly(1, 3);
            assertThat(unsqueezed.toFloatArray()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("contiguous 对连续张量返回自身")
        void contiguousReturnsSelf() {
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            assertThat(t.contiguous()).isSameAs(t);
        }

        @Test
        @DisplayName("非连续张量 contiguous 返回新拷贝")
        void nonContiguousContiguousReturnsCopy() {
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(2, 3));
            Tensor transposed = t.transpose(1, 0);
            assertThat(transposed.isContiguous()).isFalse();
            Tensor contiguous = transposed.contiguous();
            assertThat(contiguous.isContiguous()).isTrue();
            assertThat(contiguous.toFloatArray()).containsExactly(1, 4, 2, 5, 3, 6);
        }
    }

    @Nested
    @DisplayName("逐元素运算")
    class ElementWiseTest {

        @Test
        @DisplayName("add 逐元素加法")
        void addSameShape() {
            Tensor a = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            Tensor b = Tensor.fromFloat(new float[]{4, 5, 6}, Shape.of(3));
            Tensor result = a.add(b);
            assertThat(result.toFloatArray()).containsExactly(5, 7, 9);
        }

        @Test
        @DisplayName("sub 逐元素减法")
        void subSameShape() {
            Tensor a = Tensor.fromFloat(new float[]{10, 20, 30}, Shape.of(3));
            Tensor b = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            Tensor result = a.sub(b);
            assertThat(result.toFloatArray()).containsExactly(9, 18, 27);
        }

        @Test
        @DisplayName("mul 逐元素乘法")
        void mulSameShape() {
            Tensor a = Tensor.fromFloat(new float[]{2, 3, 4}, Shape.of(3));
            Tensor b = Tensor.fromFloat(new float[]{5, 6, 7}, Shape.of(3));
            Tensor result = a.mul(b);
            assertThat(result.toFloatArray()).containsExactly(10, 18, 28);
        }

        @Test
        @DisplayName("div 逐元素除法")
        void divSameShape() {
            Tensor a = Tensor.fromFloat(new float[]{10, 20, 30}, Shape.of(3));
            Tensor b = Tensor.fromFloat(new float[]{2, 4, 5}, Shape.of(3));
            Tensor result = a.div(b);
            assertThat(result.toFloatArray()).containsExactly(5, 5, 6);
        }

        @Test
        @DisplayName("形状不匹配应抛出异常")
        void shapeMismatchShouldThrow() {
            Tensor a = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            Tensor b = Tensor.fromFloat(new float[]{1, 2, 3, 4}, Shape.of(4));
            assertThatThrownBy(() -> a.add(b))
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("2D 逐元素运算")
        void elementWise2D() {
            Tensor a = Tensor.fromFloat(new float[]{1, 2, 3, 4}, Shape.of(2, 2));
            Tensor b = Tensor.fromFloat(new float[]{10, 20, 30, 40}, Shape.of(2, 2));
            Tensor result = a.add(b);
            assertThat(result.toFloatArray()).containsExactly(11, 22, 33, 44);
        }
    }

    @Nested
    @DisplayName("矩阵乘法")
    class MatmulTest {

        @Test
        @DisplayName("2x3 @ 3x2 = 2x2")
        void matmul2x3Times3x2() {
            // A = [[1,2,3],[4,5,6]]
            Tensor a = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(2, 3));
            // B = [[7,8],[9,10],[11,12]]
            Tensor b = Tensor.fromFloat(new float[]{7, 8, 9, 10, 11, 12}, Shape.of(3, 2));
            Tensor result = a.matmul(b);

            assertThat(result.shape().dims()).containsExactly(2, 2);
            // [1*7+2*9+3*11, 1*8+2*10+3*12] = [58, 64]
            // [4*7+5*9+6*11, 4*8+5*10+6*12] = [139, 154]
            assertThat(result.getFloat(0, 0)).isCloseTo(58.0f, within(EPSILON));
            assertThat(result.getFloat(0, 1)).isCloseTo(64.0f, within(EPSILON));
            assertThat(result.getFloat(1, 0)).isCloseTo(139.0f, within(EPSILON));
            assertThat(result.getFloat(1, 1)).isCloseTo(154.0f, within(EPSILON));
        }

        @Test
        @DisplayName("单位矩阵乘法")
        void matmulIdentity() {
            Tensor a = Tensor.fromFloat(new float[]{1, 2, 3, 4}, Shape.of(2, 2));
            Tensor eye = TensorFactory.eye(2);
            Tensor result = a.matmul(eye);
            assertThat(result.toFloatArray()).containsExactly(1, 2, 3, 4);
        }

        @Test
        @DisplayName("非2D应抛出异常")
        void non2DShouldThrow() {
            Tensor a = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(6));
            Tensor b = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(6));
            assertThatThrownBy(() -> a.matmul(b))
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("内维不匹配应抛出异常")
        void innerDimMismatchShouldThrow() {
            Tensor a = Tensor.fromFloat(new float[]{1, 2, 3, 4}, Shape.of(2, 2));
            Tensor b = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(3, 2));
            assertThatThrownBy(() -> a.matmul(b))
                    .isInstanceOf(TensorException.class);
        }
    }

    @Nested
    @DisplayName("归约运算")
    class ReductionTest {

        @Test
        @DisplayName("sum 所有元素")
        void sumAll() {
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(2, 3));
            Tensor result = t.sum();
            assertThat(result.toFloatArray()[0]).isCloseTo(21.0f, within(EPSILON));
        }

        @Test
        @DisplayName("sum 沿轴0")
        void sumAlongAxis0() {
            // [[1,2,3],[4,5,6]] -> sum axis 0 -> [5,7,9]
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(2, 3));
            Tensor result = t.sum(0);
            assertThat(result.shape().dims()).containsExactly(3);
            assertThat(result.toFloatArray()).usingComparatorWithPrecision(EPSILON)
                    .containsExactly(5.0f, 7.0f, 9.0f);
        }

        @Test
        @DisplayName("sum 沿轴1")
        void sumAlongAxis1() {
            // [[1,2,3],[4,5,6]] -> sum axis 1 -> [6, 15]
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(2, 3));
            Tensor result = t.sum(1);
            assertThat(result.shape().dims()).containsExactly(2);
            assertThat(result.toFloatArray()).usingComparatorWithPrecision(EPSILON)
                    .containsExactly(6.0f, 15.0f);
        }

        @Test
        @DisplayName("mean 所有元素")
        void meanAll() {
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(2, 3));
            Tensor result = t.mean();
            assertThat(result.toFloatArray()[0]).isCloseTo(3.5f, within(EPSILON));
        }

        @Test
        @DisplayName("mean 沿轴0")
        void meanAlongAxis0() {
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3, 4, 5, 6}, Shape.of(2, 3));
            Tensor result = t.mean(0);
            assertThat(result.toFloatArray()).usingComparatorWithPrecision(EPSILON)
                    .containsExactly(2.5f, 3.5f, 4.5f);
        }

        @Test
        @DisplayName("argmax 沿轴1")
        void argmaxAlongAxis1() {
            // [[1,3,2],[6,4,5]] -> argmax axis 1 -> [1, 0]
            Tensor t = Tensor.fromFloat(new float[]{1, 3, 2, 6, 4, 5}, Shape.of(2, 3));
            Tensor result = t.argmax(1);
            assertThat(result.shape().dims()).containsExactly(2);
            assertThat(result.toFloatArray()).containsExactly(1.0f, 0.0f);
        }

        @Test
        @DisplayName("argmax 沿轴0")
        void argmaxAlongAxis0() {
            // [[1,5],[3,2]] -> argmax axis 0 -> [1, 0]
            Tensor t = Tensor.fromFloat(new float[]{1, 5, 3, 2}, Shape.of(2, 2));
            Tensor result = t.argmax(0);
            assertThat(result.shape().dims()).containsExactly(2);
            assertThat(result.toFloatArray()).containsExactly(1.0f, 0.0f);
        }
    }

    @Nested
    @DisplayName("生命周期")
    class LifecycleTest {

        @Test
        @DisplayName("close 后访问应抛出异常")
        void closePreventsAccess() {
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            assertThat(t.isClosed()).isFalse();
            t.close();
            assertThat(t.isClosed()).isTrue();
            assertThatThrownBy(() -> t.getFloat(0))
                    .isInstanceOf(TensorException.class);
            assertThatThrownBy(t::toFloatArray)
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("close 后 reshape 应抛出异常")
        void closePreventReshape() {
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3, 4}, Shape.of(2, 2));
            t.close();
            assertThatThrownBy(() -> t.reshape(4))
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("close 后 add 应抛出异常")
        void closePreventsAdd() {
            Tensor a = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            Tensor b = Tensor.fromFloat(new float[]{4, 5, 6}, Shape.of(3));
            a.close();
            assertThatThrownBy(() -> a.add(b))
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("try-with-resources 正常关闭")
        void tryWithResources() {
            Tensor t;
            try (Tensor tensor = Tensor.fromFloat(new float[]{1}, Shape.of(1))) {
                t = tensor;
                assertThat(t.isClosed()).isFalse();
            }
            assertThat(t.isClosed()).isTrue();
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("toString 格式正确")
        void toStringFormat() {
            Tensor t = Tensor.zeros(Shape.of(1, 3, 224, 224));
            assertThat(t.toString()).isEqualTo("Tensor(shape=[1, 3, 224, 224], type=FLOAT32)");
        }
    }
}
