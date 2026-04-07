package cloud.opencode.base.neural.tensor;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.TensorException;
import cloud.opencode.base.neural.internal.Blas;

import java.util.Arrays;

/**
 * Core Tensor Class
 * 核心张量类
 *
 * <p>A multi-dimensional array of float32 values backed by a flat {@code float[]} array.
 * Supports zero-copy views via offset and custom strides for operations like
 * {@link #reshape(int...)}, {@link #transpose(int...)}, and {@link #squeeze(int)}.</p>
 * <p>由扁平 {@code float[]} 数组支撑的多维浮点32数组。
 * 通过偏移量和自定义步幅支持零拷贝视图，用于 {@link #reshape(int...)}、
 * {@link #transpose(int...)} 和 {@link #squeeze(int)} 等操作。</p>
 *
 * <p>Implements {@link AutoCloseable} to support explicit resource management.
 * Once closed, any data access will throw {@link TensorException}.</p>
 * <p>实现 {@link AutoCloseable} 以支持显式资源管理。
 * 关闭后任何数据访问将抛出 {@link TensorException}。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: No. External synchronization required for concurrent access.
 *       线程安全: 否。并发访问需要外部同步。</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Shape
 * @see TensorType
 * @see TensorFactory
 * @see TensorMath
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class Tensor implements AutoCloseable {

    private final float[] data;
    private final Shape shape;
    private final int offset;
    private final int[] strides;
    private final boolean contiguous;
    private volatile boolean closed;

    /**
     * Internal constructor
     * 内部构造器
     */
    Tensor(float[] data, Shape shape, int offset, int[] strides) {
        this.data = data;
        this.shape = shape;
        this.offset = offset;
        this.strides = strides;
        this.contiguous = computeContiguous(shape, offset, strides);
        this.closed = false;
    }

    // ==================== Creation Methods | 创建方法 ====================

    /**
     * Create a tensor filled with zeros
     * 创建全零张量
     *
     * @param shape tensor shape | 张量形状
     * @return zero-filled tensor | 全零张量
     */
    public static Tensor zeros(Shape shape) {
        return new Tensor(new float[shape.size()], shape, 0, shape.strides());
    }

    /**
     * Create a tensor filled with ones
     * 创建全一张量
     *
     * @param shape tensor shape | 张量形状
     * @return one-filled tensor | 全一张量
     */
    public static Tensor ones(Shape shape) {
        float[] data = new float[shape.size()];
        Arrays.fill(data, 1.0f);
        return new Tensor(data, shape, 0, shape.strides());
    }

    /**
     * Create a tensor from a float array with the given shape
     * 从浮点数组和给定形状创建张量
     *
     * @param data  source data (will be copied) | 源数据（将被复制）
     * @param shape tensor shape | 张量形状
     * @return new tensor | 新张量
     * @throws TensorException if data length does not match shape size |
     *                         如果数据长度与形状大小不匹配
     */
    public static Tensor fromFloat(float[] data, Shape shape) {
        if (data == null) {
            throw new TensorException("Data must not be null", NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (data.length != shape.size()) {
            throw new TensorException(
                    "Data length " + data.length + " does not match shape size " + shape.size(),
                    NeuralErrorCode.SHAPE_MISMATCH);
        }
        return new Tensor(data.clone(), shape, 0, shape.strides());
    }

    /**
     * Wrap an existing float array as a tensor without copying.
     * 将已有的浮点数组包装为张量而不复制。
     *
     * <p><strong>Ownership transfer:</strong> The caller transfers ownership of the array
     * to this tensor — the array must not be modified externally after this call.
     * This method is intended for performance-critical internal paths where the
     * array is freshly allocated and no external references exist.</p>
     * <p><strong>所有权转移：</strong>调用方将数组的所有权转移给此张量 —
     * 调用后不得从外部修改该数组。此方法用于性能关键的内部路径，
     * 其中数组是新分配的且不存在外部引用。</p>
     *
     * @param data  owned data array (must not be modified after this call) |
     *              拥有的数据数组（调用后不得修改）
     * @param shape tensor shape | 张量形状
     * @return new tensor wrapping the array | 包装该数组的新张量
     */
    public static Tensor wrap(float[] data, Shape shape) {
        return new Tensor(data, shape, 0, shape.strides());
    }

    /**
     * Create a scalar tensor containing a single float value (zero-copy).
     * 创建包含单个浮点值的标量张量（零拷贝）。
     *
     * @param value the scalar value | 标量值
     * @return scalar tensor of shape [1] | 形状为 [1] 的标量张量
     */
    public static Tensor scalar(float value) {
        return new Tensor(new float[]{value}, Shape.of(1), 0, new int[]{1});
    }

    // ==================== Metadata | 元数据 ====================

    /**
     * Get the shape of this tensor
     * 获取此张量的形状
     *
     * @return tensor shape | 张量形状
     */
    public Shape shape() {
        return shape;
    }

    /**
     * Get the data type (always FLOAT32 for now)
     * 获取数据类型（当前始终为 FLOAT32）
     *
     * @return tensor type | 张量类型
     */
    public TensorType type() {
        return TensorType.FLOAT32;
    }

    /**
     * Get total number of elements
     * 获取总元素数
     *
     * @return element count | 元素数
     */
    public int size() {
        return shape.size();
    }

    /**
     * Check if data is laid out contiguously in row-major order
     * 检查数据是否以行主序连续排列
     *
     * @return true if contiguous | 如果连续则返回 true
     */
    public boolean isContiguous() {
        return contiguous;
    }

    private static boolean computeContiguous(Shape shape, int offset, int[] strides) {
        int rank = shape.rank();
        if (rank == 0) return true;
        int expected = 1;
        for (int i = rank - 1; i >= 0; i--) {
            if (strides[i] != expected) return false;
            expected *= shape.dim(i);
        }
        return offset == 0;
    }

    // ==================== Data Access | 数据访问 ====================

    /**
     * Get a single float value by multi-dimensional indices
     * 通过多维索引获取单个浮点值
     *
     * @param indices multi-dimensional indices | 多维索引
     * @return float value at the given position | 给定位置的浮点值
     * @throws TensorException if tensor is closed or indices are invalid |
     *                         如果张量已关闭或索引无效
     */
    public float getFloat(int... indices) {
        ensureOpen();
        return data[physicalIndex(indices)];
    }

    /**
     * Set a single float value by multi-dimensional indices
     * 通过多维索引设置单个浮点值
     *
     * @param value   the value to set | 要设置的值
     * @param indices multi-dimensional indices | 多维索引
     * @throws TensorException if tensor is closed or indices are invalid |
     *                         如果张量已关闭或索引无效
     */
    public void setFloat(float value, int... indices) {
        ensureOpen();
        data[physicalIndex(indices)] = value;
    }

    /**
     * Copy all elements to a new float array in row-major order
     * 按行主序将所有元素复制到新的浮点数组
     *
     * @return a new float array containing all elements | 包含所有元素的新浮点数组
     * @throws TensorException if tensor is closed | 如果张量已关闭
     */
    public float[] toFloatArray() {
        ensureOpen();
        if (isContiguous()) {
            return Arrays.copyOf(data, data.length);
        }
        int total = size();
        float[] result = new float[total];
        int[] idx = new int[shape.rank()];
        for (int i = 0; i < total; i++) {
            result[i] = data[physicalIndexFromFlat(idx)];
            incrementIndex(idx);
        }
        return result;
    }

    /**
     * Get a direct reference to the underlying data array (package-private for internal use)
     * 获取底层数据数组的直接引用（包私有，供内部使用）
     *
     * @return the backing data array | 底层数据数组
     */
    float[] data() {
        return data;
    }

    /**
     * Get the offset into the data array
     * 获取数据数组中的偏移量
     *
     * @return data offset | 数据偏移量
     */
    int dataOffset() {
        return offset;
    }

    /**
     * Get the strides array (package-private)
     * 获取步幅数组（包私有）
     *
     * @return strides | 步幅
     */
    int[] tensorStrides() {
        return strides;
    }

    // ==================== Reshape / Transform | 重塑 / 变换 ====================

    /**
     * Reshape this tensor, returning a new Tensor sharing the same data when contiguous
     * 重塑此张量，当连续时返回共享相同数据的新张量
     *
     * @param dims new dimension sizes (supports -1 inference) | 新维度大小（支持 -1 推断）
     * @return reshaped tensor | 重塑后的张量
     */
    public Tensor reshape(int... dims) {
        ensureOpen();
        Shape newShape = shape.reshape(dims);
        if (isContiguous()) {
            return new Tensor(data, newShape, 0, newShape.strides());
        }
        return Tensor.fromFloat(toFloatArray(), newShape);
    }

    /**
     * Transpose (permute) dimensions
     * 转置（排列）维度
     *
     * @param axes permutation of [0, rank) | [0, rank) 的排列
     * @return transposed tensor (zero-copy view) | 转置后的张量（零拷贝视图）
     */
    public Tensor transpose(int... axes) {
        ensureOpen();
        Shape newShape = shape.transpose(axes);
        int[] newStrides = new int[axes.length];
        for (int i = 0; i < axes.length; i++) {
            newStrides[i] = strides[axes[i]];
        }
        return new Tensor(data, newShape, offset, newStrides);
    }

    /**
     * Flatten to 1D tensor
     * 展平为一维张量
     *
     * @return 1D tensor | 一维张量
     */
    public Tensor flatten() {
        ensureOpen();
        return reshape(-1);
    }

    /**
     * Squeeze: remove a dimension of size 1
     * 压缩：移除大小为1的维度
     *
     * @param axis the axis to squeeze | 要压缩的轴
     * @return squeezed tensor (zero-copy view) | 压缩后的张量（零拷贝视图）
     */
    public Tensor squeeze(int axis) {
        ensureOpen();
        int resolvedAxis = axis < 0 ? axis + shape.rank() : axis;
        Shape newShape = shape.squeeze(axis);
        int[] newStrides = new int[strides.length - 1];
        int j = 0;
        for (int i = 0; i < strides.length; i++) {
            if (i != resolvedAxis) {
                newStrides[j++] = strides[i];
            }
        }
        return new Tensor(data, newShape, offset, newStrides);
    }

    /**
     * Unsqueeze: insert a dimension of size 1 at the given axis
     * 扩展：在给定轴位置插入大小为1的维度
     *
     * @param axis insertion position | 插入位置
     * @return unsqueezed tensor (zero-copy view) | 扩展后的张量（零拷贝视图）
     */
    public Tensor unsqueeze(int axis) {
        ensureOpen();
        int resolvedAxis = axis < 0 ? axis + shape.rank() + 1 : axis;
        Shape newShape = shape.unsqueeze(axis);
        int[] newStrides = new int[strides.length + 1];
        int j = 0;
        for (int i = 0; i < newStrides.length; i++) {
            if (i == resolvedAxis) {
                newStrides[i] = (resolvedAxis < strides.length) ? strides[resolvedAxis] : 1;
            } else {
                newStrides[i] = strides[j++];
            }
        }
        return new Tensor(data, newShape, offset, newStrides);
    }

    /**
     * Return a contiguous copy if this tensor is non-contiguous, otherwise return this
     * 如果此张量非连续则返回连续拷贝，否则返回自身
     *
     * @return contiguous tensor | 连续张量
     */
    public Tensor contiguous() {
        ensureOpen();
        if (isContiguous()) {
            return this;
        }
        // Create a fresh shape from dims to get canonical row-major strides
        Shape freshShape = (shape.rank() == 0) ? Shape.scalar() : Shape.of(shape.dims());
        return Tensor.fromFloat(toFloatArray(), freshShape);
    }

    // ==================== Element-wise Operations | 逐元素运算 ====================

    /**
     * Element-wise addition (same shape required)
     * 逐元素加法（要求形状相同）
     *
     * @param other the other tensor | 另一个张量
     * @return result tensor | 结果张量
     */
    public Tensor add(Tensor other) {
        return elementWise(other, Float::sum);
    }

    /**
     * Element-wise subtraction (same shape required)
     * 逐元素减法（要求形状相同）
     *
     * @param other the other tensor | 另一个张量
     * @return result tensor | 结果张量
     */
    public Tensor sub(Tensor other) {
        return elementWise(other, (a, b) -> a - b);
    }

    /**
     * Element-wise multiplication (same shape required)
     * 逐元素乘法（要求形状相同）
     *
     * @param other the other tensor | 另一个张量
     * @return result tensor | 结果张量
     */
    public Tensor mul(Tensor other) {
        return elementWise(other, (a, b) -> a * b);
    }

    /**
     * Element-wise division (same shape required)
     * 逐元素除法（要求形状相同）
     *
     * @param other the other tensor | 另一个张量
     * @return result tensor | 结果张量
     */
    public Tensor div(Tensor other) {
        return elementWise(other, (a, b) -> a / b);
    }

    // ==================== Reduction Operations | 归约运算 ====================

    /**
     * Sum along the specified axes. If no axes given, sum all elements.
     * 沿指定轴求和。如果未指定轴，则对所有元素求和。
     *
     * @param axes axes to reduce | 要归约的轴
     * @return reduced tensor | 归约后的张量
     */
    public Tensor sum(int... axes) {
        ensureOpen();
        if (axes == null || axes.length == 0) {
            // Sum all elements — contiguous fast path
            double total = 0;
            int n = size();
            if (isContiguous()) {
                for (int i = 0; i < n; i++) {
                    total += data[i];
                }
            } else {
                int[] idx = new int[shape.rank()];
                for (int i = 0; i < n; i++) {
                    total += data[physicalIndexFromFlat(idx)];
                    incrementIndex(idx);
                }
            }
            return Tensor.wrap(new float[]{(float) total}, Shape.of(1));
        }
        return reduceAlongAxes(axes, true);
    }

    /**
     * Mean along the specified axes. If no axes given, mean of all elements.
     * 沿指定轴求均值。如果未指定轴，则对所有元素求均值。
     *
     * @param axes axes to reduce | 要归约的轴
     * @return reduced tensor | 归约后的张量
     */
    public Tensor mean(int... axes) {
        ensureOpen();
        if (axes == null || axes.length == 0) {
            double total = 0;
            int n = size();
            if (isContiguous()) {
                for (int i = 0; i < n; i++) {
                    total += data[i];
                }
            } else {
                int[] idx = new int[shape.rank()];
                for (int i = 0; i < n; i++) {
                    total += data[physicalIndexFromFlat(idx)];
                    incrementIndex(idx);
                }
            }
            return Tensor.wrap(new float[]{(float) (total / n)}, Shape.of(1));
        }
        Tensor sumResult = reduceAlongAxes(axes, true);
        int reduceCount = 1;
        for (int axis : axes) {
            int resolved = axis < 0 ? axis + shape.rank() : axis;
            reduceCount *= shape.dim(resolved);
        }
        float[] resultData = sumResult.toFloatArray();
        for (int i = 0; i < resultData.length; i++) {
            resultData[i] /= reduceCount;
        }
        return Tensor.wrap(resultData, sumResult.shape());
    }

    /**
     * Argmax along the specified axis
     * 沿指定轴求最大值索引
     *
     * @param axis the axis to reduce | 要归约的轴
     * @return tensor of argmax indices (as float) | 最大值索引张量（以浮点数表示）
     */
    public Tensor argmax(int axis) {
        ensureOpen();
        int resolved = axis < 0 ? axis + shape.rank() : axis;
        if (resolved < 0 || resolved >= shape.rank()) {
            throw new TensorException(
                    "Axis " + axis + " out of range for rank " + shape.rank(),
                    NeuralErrorCode.INDEX_OUT_OF_BOUNDS);
        }

        int[] resultDims = new int[shape.rank() - 1];
        int j = 0;
        for (int i = 0; i < shape.rank(); i++) {
            if (i != resolved) {
                resultDims[j++] = shape.dim(i);
            }
        }
        Shape resultShape = resultDims.length == 0 ? Shape.of(1) : Shape.of(resultDims);
        float[] resultData = new float[resultShape.size()];

        int axisSize = shape.dim(resolved);
        int axisStride = strides[resolved];
        int[] srcIdx = new int[shape.rank()];
        int[] dstIdx = new int[resultDims.length];

        for (int i = 0; i < resultShape.size(); i++) {
            // Map flat result index to source indices (skip the reduced axis)
            if (resultDims.length > 0) {
                int remainder = i;
                for (int d = resultDims.length - 1; d >= 0; d--) {
                    dstIdx[d] = remainder % resultDims[d];
                    remainder /= resultDims[d];
                }
            }
            // Set source indices from dstIdx, skipping the reduced axis
            int di = 0;
            for (int d = 0; d < shape.rank(); d++) {
                if (d == resolved) {
                    srcIdx[d] = 0;
                } else {
                    srcIdx[d] = dstIdx[di++];
                }
            }

            // Compute base physical index once, then stride along axis
            int baseIdx = physicalIndexFromFlat(srcIdx);
            float maxVal = data[baseIdx];
            int maxIdx = 0;
            for (int k = 1; k < axisSize; k++) {
                float val = data[baseIdx + k * axisStride];
                if (val > maxVal) {
                    maxVal = val;
                    maxIdx = k;
                }
            }
            resultData[i] = maxIdx;
        }

        return Tensor.wrap(resultData, resultShape);
    }

    // ==================== Matrix Multiply | 矩阵乘法 ====================

    /**
     * Matrix multiplication (2D only): this (M x K) @ other (K x N) = result (M x N)
     * 矩阵乘法（仅限二维）：this (M x K) @ other (K x N) = result (M x N)
     *
     * @param other the right-hand matrix | 右侧矩阵
     * @return result matrix | 结果矩阵
     * @throws TensorException if tensors are not 2D or inner dimensions don't match |
     *                         如果张量不是二维或内维不匹配
     */
    public Tensor matmul(Tensor other) {
        ensureOpen();
        other.ensureOpen();
        if (shape.rank() != 2 || other.shape.rank() != 2) {
            throw new TensorException(
                    "matmul requires 2D tensors, got ranks " + shape.rank() + " and " + other.shape.rank(),
                    NeuralErrorCode.SHAPE_MISMATCH);
        }
        int m = shape.dim(0);
        int k = shape.dim(1);
        int k2 = other.shape.dim(0);
        int n = other.shape.dim(1);
        if (k != k2) {
            throw new TensorException(
                    "matmul inner dimensions mismatch: " + k + " vs " + k2,
                    NeuralErrorCode.SHAPE_MISMATCH);
        }

        // Delegate to optimized BLAS GEMM for contiguous tensors
        if (isContiguous() && other.isContiguous()) {
            float[] c = Blas.matmul(data, m, k, other.data, k, n);
            return Tensor.wrap(c, Shape.of(m, n));
        }

        // Fallback for non-contiguous: materialize then delegate
        float[] aData = toFloatArray();
        float[] bData = other.toFloatArray();
        float[] c = Blas.matmul(aData, m, k, bData, k, n);
        return Tensor.wrap(c, Shape.of(m, n));
    }

    // ==================== Lifecycle | 生命周期 ====================

    /**
     * Close this tensor, releasing the reference to the data array
     * 关闭此张量，释放对数据数组的引用
     */
    @Override
    public void close() {
        closed = true;
    }

    /**
     * Check if this tensor has been closed
     * 检查此张量是否已关闭
     *
     * @return true if closed | 如果已关闭则返回 true
     */
    public boolean isClosed() {
        return closed;
    }

    // ==================== Object Methods | 对象方法 ====================

    /**
     * Return string representation, e.g. "Tensor(shape=[1, 3, 224, 224], type=FLOAT32)"
     * 返回字符串表示
     *
     * @return string representation | 字符串表示
     */
    @Override
    public String toString() {
        return "Tensor(shape=" + shape + ", type=" + type() + ")";
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    private void ensureOpen() {
        if (closed) {
            throw new TensorException("Tensor is closed", NeuralErrorCode.TENSOR_CLOSED);
        }
    }

    /**
     * Compute physical index in the data array from multi-dimensional indices
     */
    private int physicalIndex(int... indices) {
        int rank = shape.rank();
        if (indices.length != rank) {
            throw new TensorException(
                    "Expected " + rank + " indices, got " + indices.length,
                    NeuralErrorCode.INDEX_OUT_OF_BOUNDS);
        }
        int idx = offset;
        for (int i = 0; i < rank; i++) {
            int dim = shape.dim(i);
            if (indices[i] < 0 || indices[i] >= dim) {
                throw new TensorException(
                        "Index " + indices[i] + " out of range for dimension " + i + " (size " + dim + ")",
                        NeuralErrorCode.INDEX_OUT_OF_BOUNDS);
            }
            idx += indices[i] * strides[i];
        }
        return idx;
    }

    /**
     * Compute physical index from a multi-dimensional index array (no bounds check — caller must ensure validity)
     */
    private int physicalIndexFromFlat(int[] indices) {
        int idx = offset;
        for (int i = 0; i < indices.length; i++) {
            idx += indices[i] * strides[i];
        }
        return idx;
    }

    /**
     * Increment a multi-dimensional index in row-major order
     */
    private void incrementIndex(int[] idx) {
        for (int i = idx.length - 1; i >= 0; i--) {
            idx[i]++;
            if (idx[i] < shape.dim(i)) {
                return;
            }
            idx[i] = 0;
        }
    }

    /**
     * Element-wise operation helper
     */
    private Tensor elementWise(Tensor other, FloatBinaryOp op) {
        ensureOpen();
        other.ensureOpen();
        if (!shape.equals(other.shape)) {
            throw new TensorException(
                    "Shape mismatch for element-wise op: " + shape + " vs " + other.shape,
                    NeuralErrorCode.SHAPE_MISMATCH);
        }
        int n = size();
        float[] result = new float[n];
        // Fast path: both tensors contiguous — direct array access, no index computation
        if (isContiguous() && other.isContiguous()) {
            for (int i = 0; i < n; i++) {
                result[i] = op.apply(data[i], other.data[i]);
            }
        } else {
            int[] idx = new int[shape.rank()];
            for (int i = 0; i < n; i++) {
                int physA = physicalIndexFromFlat(idx);
                int physB = other.physicalIndexFromFlat(idx);
                result[i] = op.apply(data[physA], other.data[physB]);
                incrementIndex(idx);
            }
        }
        return new Tensor(result, shape, 0, shape.strides());
    }

    /**
     * Reduce along specified axes (sum)
     */
    private Tensor reduceAlongAxes(int[] axes, boolean keepDims) {
        // Resolve and sort axes
        boolean[] reduceAxis = new boolean[shape.rank()];
        for (int axis : axes) {
            int resolved = axis < 0 ? axis + shape.rank() : axis;
            if (resolved < 0 || resolved >= shape.rank()) {
                throw new TensorException(
                        "Axis " + axis + " out of range for rank " + shape.rank(),
                        NeuralErrorCode.INDEX_OUT_OF_BOUNDS);
            }
            reduceAxis[resolved] = true;
        }

        // Compute result shape
        int resultRank = 0;
        for (int i = 0; i < shape.rank(); i++) {
            if (!reduceAxis[i]) resultRank++;
        }
        int[] resultDims;
        if (resultRank == 0) {
            resultDims = new int[]{1};
        } else {
            resultDims = new int[resultRank];
            int j = 0;
            for (int i = 0; i < shape.rank(); i++) {
                if (!reduceAxis[i]) {
                    resultDims[j++] = shape.dim(i);
                }
            }
        }
        Shape resultShape = Shape.of(resultDims);
        float[] resultData = new float[resultShape.size()];

        // Pre-compute result strides for efficient index mapping (avoids recomputing multipliers per element)
        int[] resultStrides = new int[shape.rank()];
        {
            int mult = 1;
            for (int d = resultDims.length - 1, sd = shape.rank() - 1; sd >= 0; sd--) {
                if (!reduceAxis[sd]) {
                    resultStrides[sd] = mult;
                    mult *= resultDims[d];
                    d--;
                }
            }
        }

        // Iterate over all source elements
        int rank = shape.rank();
        int[] srcIdx = new int[rank];
        for (int i = 0; i < size(); i++) {
            // Compute result flat index using pre-computed strides (O(rank) without conditional multiplier rebuild)
            int resultFlatIdx = 0;
            for (int sd = 0; sd < rank; sd++) {
                resultFlatIdx += srcIdx[sd] * resultStrides[sd];
            }
            resultData[resultFlatIdx] += data[physicalIndexFromFlat(srcIdx)];
            incrementIndex(srcIdx);
        }

        return new Tensor(resultData, resultShape, 0, resultShape.strides());
    }

    /**
     * Functional interface for element-wise binary operations
     * 逐元素二元运算的函数式接口
     */
    @FunctionalInterface
    private interface FloatBinaryOp {
        float apply(float a, float b);
    }
}
