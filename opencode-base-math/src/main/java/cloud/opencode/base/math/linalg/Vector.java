package cloud.opencode.base.math.linalg;

import cloud.opencode.base.math.exception.MathException;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable mathematical vector backed by a {@code double} array.
 * 不可变数学向量，底层使用 {@code double} 数组存储。
 *
 * <p>All operations return new {@code Vector} instances; the original is never modified.
 * Thread-safe by virtue of immutability.</p>
 * <p>所有运算均返回新的 {@code Vector} 实例，原始对象不会被修改。
 * 因不可变性而天然线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class Vector {

    private final double[] data;

    private Vector(double[] data) {
        this.data = data;
    }

    /**
     * Creates a vector from the given components (defensive copy).
     * 根据给定分量创建向量（防御性拷贝）。
     *
     * @param components the vector components / 向量分量
     * @return a new vector / 新向量
     * @throws MathException if components is null or empty / 如果分量为 null 或为空
     */
    private static final int MAX_DIMENSION = 1_000_000;

    public static Vector of(double... components) {
        Objects.requireNonNull(components, "components must not be null / 分量不能为 null");
        if (components.length == 0) {
            throw new MathException("Vector must have at least one dimension / 向量至少需要一个维度");
        }
        if (components.length > MAX_DIMENSION) {
            throw new MathException("Vector dimension exceeds max " + MAX_DIMENSION + " / 向量维度超限");
        }
        for (int i = 0; i < components.length; i++) {
            if (!Double.isFinite(components[i])) {
                throw new MathException("Component [" + i + "] is not finite: " + components[i]
                        + " / 分量 [" + i + "] 不是有限值");
            }
        }
        return new Vector(components.clone());
    }

    /**
     * Creates a zero vector of the given dimension.
     * 创建指定维度的零向量。
     *
     * @param dimension the number of dimensions / 维度数
     * @return a zero vector / 零向量
     * @throws MathException if dimension is not positive / 如果维度不是正整数
     */
    public static Vector zero(int dimension) {
        if (dimension <= 0) {
            throw new MathException("Dimension must be positive: " + dimension
                    + " / 维度必须为正整数: " + dimension);
        }
        if (dimension > MAX_DIMENSION) {
            throw new MathException("Vector dimension exceeds max " + MAX_DIMENSION + " / 向量维度超限");
        }
        return new Vector(new double[dimension]);
    }

    /**
     * Creates a unit vector with 1.0 at the given index and 0.0 elsewhere.
     * 创建在指定索引处为 1.0、其余为 0.0 的单位向量。
     *
     * @param dimension the number of dimensions / 维度数
     * @param index     the index of the 1.0 component (zero-based) / 值为 1.0 的分量索引（从 0 开始）
     * @return a unit vector / 单位向量
     * @throws MathException if dimension is not positive or index is out of bounds
     *                       / 如果维度不是正整数或索引越界
     */
    public static Vector unit(int dimension, int index) {
        if (dimension <= 0) {
            throw new MathException("Dimension must be positive: " + dimension
                    + " / 维度必须为正整数: " + dimension);
        }
        if (index < 0 || index >= dimension) {
            throw new MathException("Index " + index + " out of bounds for dimension " + dimension
                    + " / 索引 " + index + " 超出维度 " + dimension + " 的范围");
        }
        double[] d = new double[dimension];
        d[index] = 1.0;
        return new Vector(d);
    }

    /**
     * Returns the dimension (number of components) of this vector.
     * 返回此向量的维度（分量数量）。
     *
     * @return the dimension / 维度
     */
    public int dimension() {
        return data.length;
    }

    /**
     * Returns the component at the given index.
     * 返回指定索引处的分量。
     *
     * @param index the zero-based index / 从 0 开始的索引
     * @return the component value / 分量值
     * @throws MathException if the index is out of bounds / 如果索引越界
     */
    public double get(int index) {
        if (index < 0 || index >= data.length) {
            throw new MathException("Index " + index + " out of bounds for dimension " + data.length
                    + " / 索引 " + index + " 超出维度 " + data.length + " 的范围");
        }
        return data[index];
    }

    /**
     * Returns the element-wise sum of this vector and another.
     * 返回此向量与另一个向量的逐元素之和。
     *
     * @param other the other vector / 另一个向量
     * @return the sum vector / 求和向量
     * @throws MathException if dimensions do not match / 如果维度不匹配
     */
    public Vector add(Vector other) {
        Objects.requireNonNull(other, "other must not be null / 另一个向量不能为 null");
        requireSameDimension(other);
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i] + other.data[i];
        }
        return new Vector(result);
    }

    /**
     * Returns the element-wise difference of this vector and another.
     * 返回此向量与另一个向量的逐元素之差。
     *
     * @param other the other vector / 另一个向量
     * @return the difference vector / 差值向量
     * @throws MathException if dimensions do not match / 如果维度不匹配
     */
    public Vector subtract(Vector other) {
        Objects.requireNonNull(other, "other must not be null / 另一个向量不能为 null");
        requireSameDimension(other);
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i] - other.data[i];
        }
        return new Vector(result);
    }

    /**
     * Returns this vector scaled by the given scalar.
     * 返回此向量乘以给定标量的结果。
     *
     * @param scalar the scalar multiplier / 标量乘数
     * @return the scaled vector / 缩放后的向量
     */
    public Vector scale(double scalar) {
        if (!Double.isFinite(scalar)) {
            throw new MathException("Scalar must be finite: " + scalar + " / 标量必须有限");
        }
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i] * scalar;
        }
        return new Vector(result);
    }

    /**
     * Returns the negation of this vector (all components multiplied by -1).
     * 返回此向量的取反（所有分量乘以 -1）。
     *
     * @return the negated vector / 取反后的向量
     */
    public Vector negate() {
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = -data[i];
        }
        return new Vector(result);
    }

    /**
     * Returns the dot product of this vector and another.
     * 返回此向量与另一个向量的点积。
     *
     * @param other the other vector / 另一个向量
     * @return the dot product / 点积
     * @throws MathException if dimensions do not match / 如果维度不匹配
     */
    public double dot(Vector other) {
        Objects.requireNonNull(other, "other must not be null / 另一个向量不能为 null");
        requireSameDimension(other);
        double sum = 0.0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i] * other.data[i];
        }
        return sum;
    }

    /**
     * Returns the cross product of this 3D vector and another 3D vector.
     * 返回此三维向量与另一个三维向量的叉积。
     *
     * @param other the other 3D vector / 另一个三维向量
     * @return the cross product vector / 叉积向量
     * @throws MathException if either vector is not 3-dimensional / 如果任一向量不是三维的
     */
    public Vector cross(Vector other) {
        Objects.requireNonNull(other, "other must not be null / 另一个向量不能为 null");
        if (data.length != 3 || other.data.length != 3) {
            throw new MathException("Cross product is defined only for 3D vectors, got dimensions "
                    + data.length + " and " + other.data.length
                    + " / 叉积仅适用于三维向量，当前维度为 " + data.length + " 和 " + other.data.length);
        }
        double x = data[1] * other.data[2] - data[2] * other.data[1];
        double y = data[2] * other.data[0] - data[0] * other.data[2];
        double z = data[0] * other.data[1] - data[1] * other.data[0];
        return new Vector(new double[]{x, y, z});
    }

    /**
     * Returns the Euclidean norm (magnitude) of this vector.
     * 返回此向量的欧几里得范数（模长）。
     *
     * @return the magnitude / 模长
     */
    public double magnitude() {
        double sum = 0.0;
        for (double v : data) {
            sum += v * v;
        }
        return Math.sqrt(sum);
    }

    /**
     * Returns the unit vector in the same direction as this vector.
     * 返回与此向量同方向的单位向量。
     *
     * @return the normalized vector / 归一化向量
     * @throws MathException if this is a zero vector / 如果此向量为零向量
     */
    public Vector normalize() {
        double mag = magnitude();
        if (mag == 0.0) {
            throw new MathException("Cannot normalize a zero vector / 无法对零向量进行归一化");
        }
        return scale(1.0 / mag);
    }

    /**
     * Returns the angle in radians between this vector and another.
     * 返回此向量与另一个向量之间的弧度角。
     *
     * @param other the other vector / 另一个向量
     * @return the angle in radians in [0, PI] / 弧度角，范围 [0, PI]
     * @throws MathException if dimensions do not match or either vector is zero
     *                       / 如果维度不匹配或任一向量为零向量
     */
    public double angle(Vector other) {
        Objects.requireNonNull(other, "other must not be null / 另一个向量不能为 null");
        requireSameDimension(other);
        // Single pass: compute dot product and both magnitudes simultaneously
        double sumA2 = 0.0, sumB2 = 0.0, sumAB = 0.0;
        for (int i = 0; i < data.length; i++) {
            double a = data[i], b = other.data[i];
            sumA2 += a * a;
            sumB2 += b * b;
            sumAB += a * b;
        }
        double magA = Math.sqrt(sumA2);
        double magB = Math.sqrt(sumB2);
        if (magA == 0.0 || magB == 0.0) {
            throw new MathException("Cannot compute angle with a zero vector / 无法计算零向量的夹角");
        }
        double cosTheta = Math.max(-1.0, Math.min(1.0, sumAB / (magA * magB)));
        return Math.acos(cosTheta);
    }

    /**
     * Returns the Euclidean distance from this vector to another.
     * 返回此向量到另一个向量的欧几里得距离。
     *
     * @param other the other vector / 另一个向量
     * @return the Euclidean distance / 欧几里得距离
     * @throws MathException if dimensions do not match / 如果维度不匹配
     */
    public double distanceTo(Vector other) {
        Objects.requireNonNull(other, "other must not be null / 另一个向量不能为 null");
        requireSameDimension(other);
        double sum = 0.0;
        for (int i = 0; i < data.length; i++) {
            double d = data[i] - other.data[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    /**
     * Returns a defensive copy of the internal components array.
     * 返回内部分量数组的防御性拷贝。
     *
     * @return a copy of the components / 分量的拷贝
     */
    public double[] toArray() {
        return data.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector other)) return false;
        return Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        return "Vector" + Arrays.toString(data);
    }

    // ---- internal helpers ----

    private void requireSameDimension(Vector other) {
        if (data.length != other.data.length) {
            throw new MathException("Dimension mismatch: " + data.length + " vs " + other.data.length
                    + " / 维度不匹配: " + data.length + " 与 " + other.data.length);
        }
    }
}
