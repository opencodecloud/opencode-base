package cloud.opencode.base.neural.tensor;

import cloud.opencode.base.neural.exception.TensorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Shape}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("Shape — 张量形状描述符")
class ShapeTest {

    @Nested
    @DisplayName("创建与基本属性")
    class CreationTest {

        @Test
        @DisplayName("of(1,3,224,224) — rank=4, size=150528")
        void ofImageShape() {
            Shape shape = Shape.of(1, 3, 224, 224);
            assertThat(shape.rank()).isEqualTo(4);
            assertThat(shape.size()).isEqualTo(1 * 3 * 224 * 224);
            assertThat(shape.dim(0)).isEqualTo(1);
            assertThat(shape.dim(1)).isEqualTo(3);
            assertThat(shape.dim(2)).isEqualTo(224);
            assertThat(shape.dim(3)).isEqualTo(224);
        }

        @Test
        @DisplayName("scalar — rank=0, size=1")
        void scalarShape() {
            Shape shape = Shape.scalar();
            assertThat(shape.rank()).isEqualTo(0);
            assertThat(shape.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("dims() 返回防御性拷贝")
        void dimsReturnsDefensiveCopy() {
            Shape shape = Shape.of(2, 3);
            int[] dims = shape.dims();
            dims[0] = 999;
            assertThat(shape.dim(0)).isEqualTo(2);
        }

        @Test
        @DisplayName("strides() 返回防御性拷贝")
        void stridesReturnsDefensiveCopy() {
            Shape shape = Shape.of(2, 3);
            int[] strides = shape.strides();
            strides[0] = 999;
            assertThat(shape.strides()[0]).isEqualTo(3);
        }

        @Test
        @DisplayName("行主序步幅计算正确")
        void rowMajorStrides() {
            Shape shape = Shape.of(2, 3, 4);
            assertThat(shape.strides()).containsExactly(12, 4, 1);
        }

        @Test
        @DisplayName("非正维度应抛出异常")
        void nonPositiveDimShouldThrow() {
            assertThatThrownBy(() -> Shape.of(2, 0, 3))
                    .isInstanceOf(TensorException.class);
            assertThatThrownBy(() -> Shape.of(-1))
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("空维度应抛出异常")
        void emptyDimsShouldThrow() {
            assertThatThrownBy(() -> Shape.of())
                    .isInstanceOf(TensorException.class);
        }
    }

    @Nested
    @DisplayName("reshape 操作")
    class ReshapeTest {

        @Test
        @DisplayName("reshape 保持总元素数")
        void reshapePreservesSize() {
            Shape shape = Shape.of(2, 3, 4);
            Shape reshaped = shape.reshape(6, 4);
            assertThat(reshaped.rank()).isEqualTo(2);
            assertThat(reshaped.size()).isEqualTo(24);
            assertThat(reshaped.dim(0)).isEqualTo(6);
            assertThat(reshaped.dim(1)).isEqualTo(4);
        }

        @Test
        @DisplayName("reshape(-1) 推断维度")
        void reshapeWithInference() {
            Shape shape = Shape.of(2, 3, 4);
            Shape flat = shape.reshape(-1);
            assertThat(flat.rank()).isEqualTo(1);
            assertThat(flat.dim(0)).isEqualTo(24);
        }

        @Test
        @DisplayName("reshape(2, -1) 推断第二个维度")
        void reshapePartialInference() {
            Shape shape = Shape.of(2, 3, 4);
            Shape reshaped = shape.reshape(2, -1);
            assertThat(reshaped.dim(1)).isEqualTo(12);
        }

        @Test
        @DisplayName("不兼容的reshape应抛出异常")
        void incompatibleReshapeShouldThrow() {
            Shape shape = Shape.of(2, 3);
            assertThatThrownBy(() -> shape.reshape(4, 4))
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("多个-1应抛出异常")
        void multipleInferShouldThrow() {
            Shape shape = Shape.of(2, 3, 4);
            assertThatThrownBy(() -> shape.reshape(-1, -1))
                    .isInstanceOf(TensorException.class);
        }
    }

    @Nested
    @DisplayName("transpose 操作")
    class TransposeTest {

        @Test
        @DisplayName("2D转置交换维度")
        void transpose2D() {
            Shape shape = Shape.of(3, 4);
            Shape transposed = shape.transpose(1, 0);
            assertThat(transposed.dim(0)).isEqualTo(4);
            assertThat(transposed.dim(1)).isEqualTo(3);
        }

        @Test
        @DisplayName("3D转置排列维度")
        void transpose3D() {
            Shape shape = Shape.of(2, 3, 4);
            Shape transposed = shape.transpose(2, 0, 1);
            assertThat(transposed.dims()).containsExactly(4, 2, 3);
        }

        @Test
        @DisplayName("无效排列应抛出异常")
        void invalidPermutationShouldThrow() {
            Shape shape = Shape.of(2, 3);
            assertThatThrownBy(() -> shape.transpose(0, 0))
                    .isInstanceOf(TensorException.class);
        }
    }

    @Nested
    @DisplayName("squeeze / unsqueeze 操作")
    class SqueezeUnsqueezeTest {

        @Test
        @DisplayName("squeeze 移除大小为1的维度")
        void squeezeRemovesSizeOneDim() {
            Shape shape = Shape.of(1, 3, 1, 224);
            Shape squeezed = shape.squeeze(0);
            assertThat(squeezed.dims()).containsExactly(3, 1, 224);
        }

        @Test
        @DisplayName("squeeze 非1维度应抛出异常")
        void squeezeNonOneShouldThrow() {
            Shape shape = Shape.of(2, 3);
            assertThatThrownBy(() -> shape.squeeze(0))
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("unsqueeze 插入维度")
        void unsqueezeInsertsDim() {
            Shape shape = Shape.of(3, 4);
            Shape unsqueezed = shape.unsqueeze(0);
            assertThat(unsqueezed.dims()).containsExactly(1, 3, 4);
            assertThat(unsqueezed.size()).isEqualTo(12);
        }

        @Test
        @DisplayName("unsqueeze 在末尾插入")
        void unsqueezeAtEnd() {
            Shape shape = Shape.of(3, 4);
            Shape unsqueezed = shape.unsqueeze(2);
            assertThat(unsqueezed.dims()).containsExactly(3, 4, 1);
        }

        @Test
        @DisplayName("squeeze 负索引")
        void squeezeNegativeIndex() {
            Shape shape = Shape.of(3, 1);
            Shape squeezed = shape.squeeze(-1);
            assertThat(squeezed.dims()).containsExactly(3);
        }
    }

    @Nested
    @DisplayName("broadcast 操作")
    class BroadcastTest {

        @Test
        @DisplayName("相同形状广播")
        void sameShapeBroadcast() {
            Shape a = Shape.of(3, 4);
            Shape b = Shape.of(3, 4);
            Shape result = Shape.broadcast(a, b);
            assertThat(result.dims()).containsExactly(3, 4);
        }

        @Test
        @DisplayName("标量广播到矩阵")
        void scalarBroadcastToMatrix() {
            Shape a = Shape.of(3, 4);
            Shape b = Shape.of(1);
            Shape result = Shape.broadcast(a, b);
            assertThat(result.dims()).containsExactly(3, 4);
        }

        @Test
        @DisplayName("(3,1) 与 (1,4) 广播为 (3,4)")
        void crossBroadcast() {
            Shape a = Shape.of(3, 1);
            Shape b = Shape.of(1, 4);
            Shape result = Shape.broadcast(a, b);
            assertThat(result.dims()).containsExactly(3, 4);
        }

        @Test
        @DisplayName("不兼容形状广播应抛出异常")
        void incompatibleBroadcastShouldThrow() {
            Shape a = Shape.of(3, 4);
            Shape b = Shape.of(2, 4);
            assertThatThrownBy(() -> Shape.broadcast(a, b))
                    .isInstanceOf(TensorException.class);
        }
    }

    @Nested
    @DisplayName("equals / hashCode / toString")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("相同维度的Shape相等")
        void equalShapes() {
            Shape a = Shape.of(2, 3, 4);
            Shape b = Shape.of(2, 3, 4);
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不同维度的Shape不相等")
        void unequalShapes() {
            Shape a = Shape.of(2, 3);
            Shape b = Shape.of(3, 2);
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("toString 格式正确")
        void toStringFormat() {
            Shape shape = Shape.of(1, 3, 224, 224);
            assertThat(shape.toString()).isEqualTo("[1, 3, 224, 224]");
        }

        @Test
        @DisplayName("scalar toString")
        void scalarToString() {
            assertThat(Shape.scalar().toString()).isEqualTo("[]");
        }
    }
}
