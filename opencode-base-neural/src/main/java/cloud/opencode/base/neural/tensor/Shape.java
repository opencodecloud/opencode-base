package cloud.opencode.base.neural.tensor;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.TensorException;

import java.util.Arrays;

/**
 * Tensor Shape Descriptor
 * 张量形状描述符
 *
 * <p>Immutable descriptor for a tensor's dimensionality. Automatically computes row-major
 * strides and total element count. Supports common shape transformations such as
 * {@link #reshape(int...)}, {@link #transpose(int...)}, {@link #squeeze(int)},
 * and {@link #unsqueeze(int)}.</p>
 * <p>不可变的张量维度描述符。自动计算行主序步幅和总元素数。支持常用的形状变换操作，
 * 如 {@link #reshape(int...)}、{@link #transpose(int...)}、{@link #squeeze(int)}
 * 和 {@link #unsqueeze(int)}。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Tensor
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class Shape {

    private final int[] dims;
    private final int[] strides;
    private final int size;

    private Shape(int[] dims, int[] strides, int size) {
        this.dims = dims;
        this.strides = strides;
        this.size = size;
    }

    /**
     * Create a shape from dimension sizes
     * 从维度大小创建形状
     *
     * @param dims dimension sizes (each must be &gt;= 1) | 维度大小（每个必须 &gt;= 1）
     * @return a new Shape | 新的形状
     * @throws TensorException if any dimension is non-positive or total size overflows |
     *                         如果任何维度非正或总大小溢出
     */
    public static Shape of(int... dims) {
        if (dims == null || dims.length == 0) {
            throw new TensorException("Dims must not be empty", NeuralErrorCode.INVALID_SHAPE);
        }
        int[] copy = dims.clone();
        for (int i = 0; i < copy.length; i++) {
            if (copy[i] <= 0) {
                throw new TensorException(
                        "Dimension " + i + " must be positive, got " + copy[i],
                        NeuralErrorCode.INVALID_SHAPE);
            }
        }
        int totalSize = computeSize(copy);
        int[] strides = computeStrides(copy);
        return new Shape(copy, strides, totalSize);
    }

    /**
     * Create a scalar shape (rank 0, size 1)
     * 创建标量形状（0阶，大小为1）
     *
     * @return a scalar Shape | 标量形状
     */
    public static Shape scalar() {
        return new Shape(new int[0], new int[0], 1);
    }

    /**
     * Get the rank (number of dimensions)
     * 获取阶数（维度数量）
     *
     * @return rank | 阶数
     */
    public int rank() {
        return dims.length;
    }

    /**
     * Get the size of a specific dimension
     * 获取指定维度的大小
     *
     * @param axis dimension index | 维度索引
     * @return dimension size | 维度大小
     * @throws TensorException if axis is out of range | 如果轴超出范围
     */
    public int dim(int axis) {
        int resolved = resolveAxis(axis, dims.length);
        return dims[resolved];
    }

    /**
     * Get the total number of elements
     * 获取总元素数
     *
     * @return total element count | 总元素数
     */
    public int size() {
        return size;
    }

    /**
     * Get a defensive copy of dimension sizes
     * 获取维度大小的防御性拷贝
     *
     * @return copy of dimension array | 维度数组的拷贝
     */
    public int[] dims() {
        return dims.clone();
    }

    /**
     * Get a defensive copy of strides
     * 获取步幅的防御性拷贝
     *
     * @return copy of strides array | 步幅数组的拷贝
     */
    public int[] strides() {
        return strides.clone();
    }

    /**
     * Reshape to new dimensions, preserving total element count.
     * Supports exactly one dimension set to -1, which is inferred automatically.
     * 重塑为新维度，保持总元素数不变。支持恰好一个维度设置为 -1，将自动推断。
     *
     * @param newDims new dimension sizes | 新维度大小
     * @return reshaped Shape | 重塑后的形状
     * @throws TensorException if sizes are incompatible | 如果大小不兼容
     */
    public Shape reshape(int... newDims) {
        if (newDims == null || newDims.length == 0) {
            throw new TensorException("New dims must not be empty", NeuralErrorCode.INVALID_SHAPE);
        }
        int[] resolved = newDims.clone();
        int inferIndex = -1;
        long knownProduct = 1;

        for (int i = 0; i < resolved.length; i++) {
            if (resolved[i] == -1) {
                if (inferIndex != -1) {
                    throw new TensorException(
                            "At most one dimension can be -1", NeuralErrorCode.INVALID_SHAPE);
                }
                inferIndex = i;
            } else if (resolved[i] <= 0) {
                throw new TensorException(
                        "Dimension " + i + " must be positive or -1, got " + resolved[i],
                        NeuralErrorCode.INVALID_SHAPE);
            } else {
                knownProduct = Math.multiplyExact(knownProduct, resolved[i]);
            }
        }

        if (inferIndex != -1) {
            if (knownProduct == 0 || size % knownProduct != 0) {
                throw new TensorException(
                        "Cannot infer dimension: total size " + size +
                                " not divisible by known product " + knownProduct,
                        NeuralErrorCode.INVALID_SHAPE);
            }
            resolved[inferIndex] = (int) (size / knownProduct);
        }

        int newSize = computeSize(resolved);
        if (newSize != size) {
            throw new TensorException(
                    "Cannot reshape from size " + size + " to size " + newSize,
                    NeuralErrorCode.SHAPE_MISMATCH);
        }
        int[] newStrides = computeStrides(resolved);
        return new Shape(resolved, newStrides, newSize);
    }

    /**
     * Transpose (permute) dimensions according to the given axis order
     * 根据给定的轴顺序转置（排列）维度
     *
     * @param axes permutation of [0, rank) | [0, rank) 的排列
     * @return transposed Shape | 转置后的形状
     * @throws TensorException if axes is not a valid permutation | 如果轴不是有效排列
     */
    public Shape transpose(int... axes) {
        if (axes == null || axes.length != dims.length) {
            throw new TensorException(
                    "Axes length must equal rank " + dims.length,
                    NeuralErrorCode.INVALID_SHAPE);
        }
        boolean[] seen = new boolean[dims.length];
        for (int axis : axes) {
            if (axis < 0 || axis >= dims.length || seen[axis]) {
                throw new TensorException(
                        "Invalid permutation: " + Arrays.toString(axes),
                        NeuralErrorCode.INVALID_SHAPE);
            }
            seen[axis] = true;
        }
        int[] newDims = new int[dims.length];
        int[] newStrides = new int[dims.length];
        for (int i = 0; i < dims.length; i++) {
            newDims[i] = dims[axes[i]];
            newStrides[i] = strides[axes[i]];
        }
        return new Shape(newDims, newStrides, size);
    }

    /**
     * Remove a dimension of size 1
     * 移除大小为1的维度
     *
     * @param axis the axis to squeeze (supports negative indexing) | 要压缩的轴（支持负索引）
     * @return squeezed Shape | 压缩后的形状
     * @throws TensorException if dimension is not 1 | 如果维度不为1
     */
    public Shape squeeze(int axis) {
        int resolved = resolveAxis(axis, dims.length);
        if (dims[resolved] != 1) {
            throw new TensorException(
                    "Cannot squeeze axis " + resolved + " with size " + dims[resolved],
                    NeuralErrorCode.INVALID_SHAPE);
        }
        if (dims.length == 1) {
            return scalar();
        }
        int[] newDims = new int[dims.length - 1];
        int[] newStrides = new int[dims.length - 1];
        int j = 0;
        for (int i = 0; i < dims.length; i++) {
            if (i != resolved) {
                newDims[j] = dims[i];
                newStrides[j] = strides[i];
                j++;
            }
        }
        return new Shape(newDims, newStrides, size);
    }

    /**
     * Insert a dimension of size 1 at the given axis
     * 在给定轴位置插入大小为1的维度
     *
     * @param axis insertion position (supports negative indexing, range [-(rank+1), rank]) |
     *             插入位置（支持负索引，范围 [-(rank+1), rank]）
     * @return unsqueezed Shape | 扩展后的形状
     */
    public Shape unsqueeze(int axis) {
        int newRank = dims.length + 1;
        int resolved;
        if (axis < 0) {
            resolved = axis + newRank;
        } else {
            resolved = axis;
        }
        if (resolved < 0 || resolved > dims.length) {
            throw new TensorException(
                    "Axis " + axis + " out of range for unsqueeze on rank " + dims.length,
                    NeuralErrorCode.INDEX_OUT_OF_BOUNDS);
        }
        int[] newDims = new int[newRank];
        int[] newStrides = new int[newRank];
        int j = 0;
        for (int i = 0; i < newRank; i++) {
            if (i == resolved) {
                newDims[i] = 1;
                // stride for size-1 dim: product of dims after it (or 1 if at end)
                newStrides[i] = (resolved < dims.length) ? strides[resolved] : 1;
            } else {
                newDims[i] = dims[j];
                newStrides[i] = strides[j];
                j++;
            }
        }
        return new Shape(newDims, newStrides, size);
    }

    /**
     * Compute the broadcast-compatible shape of two shapes
     * 计算两个形状的广播兼容形状
     *
     * <p>Follows NumPy broadcasting rules: dimensions are compared from the trailing end,
     * each pair must either be equal or one of them must be 1.</p>
     * <p>遵循 NumPy 广播规则：从末尾维度开始比较，每对维度必须相等或其中一个为1。</p>
     *
     * @param a first shape | 第一个形状
     * @param b second shape | 第二个形状
     * @return broadcast shape | 广播后的形状
     * @throws TensorException if shapes are not broadcast-compatible | 如果形状不兼容广播
     */
    public static Shape broadcast(Shape a, Shape b) {
        int maxRank = Math.max(a.dims.length, b.dims.length);
        int[] result = new int[maxRank];
        for (int i = 0; i < maxRank; i++) {
            int dimA = (i < a.dims.length) ? a.dims[a.dims.length - 1 - i] : 1;
            int dimB = (i < b.dims.length) ? b.dims[b.dims.length - 1 - i] : 1;
            if (dimA != dimB && dimA != 1 && dimB != 1) {
                throw new TensorException(
                        "Shapes " + a + " and " + b + " are not broadcast-compatible",
                        NeuralErrorCode.SHAPE_MISMATCH);
            }
            result[maxRank - 1 - i] = Math.max(dimA, dimB);
        }
        return Shape.of(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shape other)) return false;
        return Arrays.equals(dims, other.dims);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(dims);
    }

    /**
     * Return string representation, e.g. "[1, 3, 224, 224]"
     * 返回字符串表示，例如 "[1, 3, 224, 224]"
     *
     * @return string representation | 字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < dims.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(dims[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    /**
     * Resolve a possibly-negative axis index
     * 解析可能为负的轴索引
     */
    private int resolveAxis(int axis, int rank) {
        int resolved = axis < 0 ? axis + rank : axis;
        if (resolved < 0 || resolved >= rank) {
            throw new TensorException(
                    "Axis " + axis + " out of range for rank " + rank,
                    NeuralErrorCode.INDEX_OUT_OF_BOUNDS);
        }
        return resolved;
    }

    /**
     * Compute total element count with overflow checking
     * 计算总元素数（带溢出检查）
     */
    private static int computeSize(int[] dims) {
        if (dims.length == 0) return 1;
        long product = 1;
        for (int d : dims) {
            product *= d;
            if (product > Integer.MAX_VALUE) {
                throw new TensorException(
                        "Total size exceeds Integer.MAX_VALUE",
                        NeuralErrorCode.INVALID_SHAPE);
            }
        }
        return (int) product;
    }

    /**
     * Compute row-major strides: stride[i] = product(dims[i+1..n])
     * 计算行主序步幅：stride[i] = product(dims[i+1..n])
     */
    private static int[] computeStrides(int[] dims) {
        int[] strides = new int[dims.length];
        if (dims.length > 0) {
            strides[dims.length - 1] = 1;
            for (int i = dims.length - 2; i >= 0; i--) {
                strides[i] = strides[i + 1] * dims[i + 1];
            }
        }
        return strides;
    }
}
