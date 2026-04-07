package cloud.opencode.base.math.linalg;

import cloud.opencode.base.math.exception.MathException;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable mathematical matrix backed by a {@code double[][]} array.
 * 不可变数学矩阵，底层使用 {@code double[][]} 数组存储。
 *
 * <p>All operations return new {@code Matrix} instances; the original is never modified.
 * Thread-safe by virtue of immutability.</p>
 * <p>所有运算均返回新的 {@code Matrix} 实例，原始对象不会被修改。
 * 因不可变性而天然线程安全。</p>
 *
 * <p>For determinant and inverse of matrices larger than 3x3, LU decomposition with
 * partial pivoting is used for numerical stability and performance.</p>
 * <p>对于大于 3x3 的矩阵，其行列式和逆矩阵计算使用带部分主元选取的 LU 分解，
 * 以保证数值稳定性和性能。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class Matrix {

    private static final double EPSILON = 1e-12;

    private final double[][] data;
    private final int rows;
    private final int cols;

    private Matrix(double[][] data, int rows, int cols) {
        this.data = data;
        this.rows = rows;
        this.cols = cols;
    }

    /**
     * Creates a matrix from the given 2D array (defensive copy, validates rectangular).
     * 根据给定二维数组创建矩阵（防御性拷贝，验证是否为矩形）。
     *
     * @param data the matrix data / 矩阵数据
     * @return a new matrix / 新矩阵
     * @throws MathException if data is null, empty, or not rectangular
     *                       / 如果数据为 null、为空或不是矩形
     */
    private static final int MAX_DIMENSION = 4096;

    public static Matrix of(double[][] data) {
        Objects.requireNonNull(data, "data must not be null / 数据不能为 null");
        if (data.length == 0) {
            throw new MathException("Matrix must have at least one row / 矩阵至少需要一行");
        }
        if (data.length > MAX_DIMENSION) {
            throw new MathException("Matrix rows exceed max " + MAX_DIMENSION + " / 矩阵行数超限");
        }
        Objects.requireNonNull(data[0], "Row 0 must not be null / 第 0 行不能为 null");
        int cols = data[0].length;
        if (cols == 0) {
            throw new MathException("Matrix must have at least one column / 矩阵至少需要一列");
        }
        if (cols > MAX_DIMENSION) {
            throw new MathException("Matrix cols exceed max " + MAX_DIMENSION + " / 矩阵列数超限");
        }
        double[][] copy = new double[data.length][cols];
        for (int i = 0; i < data.length; i++) {
            Objects.requireNonNull(data[i], "Row " + i + " must not be null / 第 " + i + " 行不能为 null");
            if (data[i].length != cols) {
                throw new MathException("Row " + i + " has " + data[i].length
                        + " columns, expected " + cols + " / 第 " + i + " 行有 "
                        + data[i].length + " 列，期望 " + cols + " 列");
            }
            for (int j = 0; j < cols; j++) {
                if (!Double.isFinite(data[i][j])) {
                    throw new MathException("Element (" + i + "," + j + ") is not finite: " + data[i][j]
                            + " / 元素 (" + i + "," + j + ") 不是有限值");
                }
                copy[i][j] = data[i][j];
            }
        }
        return new Matrix(copy, data.length, cols);
    }

    /**
     * Creates an identity matrix of size n.
     * 创建 n 阶单位矩阵。
     *
     * @param n the size / 阶数
     * @return the identity matrix / 单位矩阵
     * @throws MathException if n is not positive / 如果 n 不是正整数
     */
    public static Matrix identity(int n) {
        if (n <= 0) {
            throw new MathException("Size must be positive: " + n + " / 阶数必须为正整数: " + n);
        }
        if (n > MAX_DIMENSION) {
            throw new MathException("Matrix size exceeds max " + MAX_DIMENSION + " / 矩阵大小超限");
        }
        double[][] d = new double[n][n];
        for (int i = 0; i < n; i++) {
            d[i][i] = 1.0;
        }
        return new Matrix(d, n, n);
    }

    /**
     * Creates a zero matrix with the given dimensions.
     * 创建指定维度的零矩阵。
     *
     * @param rows the number of rows / 行数
     * @param cols the number of columns / 列数
     * @return the zero matrix / 零矩阵
     * @throws MathException if rows or cols is not positive / 如果行数或列数不是正整数
     */
    public static Matrix zero(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new MathException("Dimensions must be positive: " + rows + "x" + cols
                    + " / 维度必须为正整数: " + rows + "x" + cols);
        }
        if (rows > MAX_DIMENSION || cols > MAX_DIMENSION) {
            throw new MathException("Matrix dimensions exceed max " + MAX_DIMENSION + " / 矩阵维度超限");
        }
        return new Matrix(new double[rows][cols], rows, cols);
    }

    /**
     * Returns the number of rows.
     * 返回行数。
     *
     * @return the number of rows / 行数
     */
    public int rows() {
        return rows;
    }

    /**
     * Returns the number of columns.
     * 返回列数。
     *
     * @return the number of columns / 列数
     */
    public int cols() {
        return cols;
    }

    /**
     * Returns the element at the given row and column.
     * 返回指定行列处的元素。
     *
     * @param row the row index (zero-based) / 行索引（从 0 开始）
     * @param col the column index (zero-based) / 列索引（从 0 开始）
     * @return the element value / 元素值
     * @throws MathException if the indices are out of bounds / 如果索引越界
     */
    public double get(int row, int col) {
        checkBounds(row, col);
        return data[row][col];
    }

    /**
     * Returns the element-wise sum of this matrix and another.
     * 返回此矩阵与另一个矩阵的逐元素之和。
     *
     * @param other the other matrix / 另一个矩阵
     * @return the sum matrix / 求和矩阵
     * @throws MathException if dimensions do not match / 如果维度不匹配
     */
    public Matrix add(Matrix other) {
        Objects.requireNonNull(other, "other must not be null / 另一个矩阵不能为 null");
        requireSameDimensions(other);
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = data[i][j] + other.data[i][j];
            }
        }
        return new Matrix(result, rows, cols);
    }

    /**
     * Returns the element-wise difference of this matrix and another.
     * 返回此矩阵与另一个矩阵的逐元素之差。
     *
     * @param other the other matrix / 另一个矩阵
     * @return the difference matrix / 差值矩阵
     * @throws MathException if dimensions do not match / 如果维度不匹配
     */
    public Matrix subtract(Matrix other) {
        Objects.requireNonNull(other, "other must not be null / 另一个矩阵不能为 null");
        requireSameDimensions(other);
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = data[i][j] - other.data[i][j];
            }
        }
        return new Matrix(result, rows, cols);
    }

    /**
     * Returns the product of this matrix and another.
     * 返回此矩阵与另一个矩阵的乘积。
     *
     * @param other the other matrix / 另一个矩阵
     * @return the product matrix / 乘积矩阵
     * @throws MathException if inner dimensions do not match / 如果内部维度不匹配
     */
    public Matrix multiply(Matrix other) {
        Objects.requireNonNull(other, "other must not be null / 另一个矩阵不能为 null");
        if (this.cols != other.rows) {
            throw new MathException("Cannot multiply " + rows + "x" + cols + " by "
                    + other.rows + "x" + other.cols + ": inner dimensions mismatch"
                    + " / 无法将 " + rows + "x" + cols + " 与 "
                    + other.rows + "x" + other.cols + " 相乘：内部维度不匹配");
        }
        double[][] result = new double[this.rows][other.cols];
        for (int i = 0; i < this.rows; i++) {
            for (int k = 0; k < this.cols; k++) {
                double aik = data[i][k];
                for (int j = 0; j < other.cols; j++) {
                    result[i][j] += aik * other.data[k][j];
                }
            }
        }
        return new Matrix(result, this.rows, other.cols);
    }

    /**
     * Returns the result of multiplying this matrix by a vector.
     * 返回此矩阵与向量相乘的结果。
     *
     * @param v the vector / 向量
     * @return the resulting vector / 结果向量
     * @throws MathException if the vector dimension does not match the number of columns
     *                       / 如果向量维度与列数不匹配
     */
    public Vector multiplyVector(Vector v) {
        Objects.requireNonNull(v, "vector must not be null / 向量不能为 null");
        if (cols != v.dimension()) {
            throw new MathException("Cannot multiply " + rows + "x" + cols
                    + " matrix by vector of dimension " + v.dimension()
                    + " / 无法将 " + rows + "x" + cols
                    + " 矩阵与维度为 " + v.dimension() + " 的向量相乘");
        }
        double[] result = new double[rows];
        for (int i = 0; i < rows; i++) {
            double sum = 0.0;
            for (int j = 0; j < cols; j++) {
                sum += data[i][j] * v.get(j);
            }
            result[i] = sum;
        }
        return Vector.of(result);
    }

    /**
     * Returns this matrix multiplied by a scalar.
     * 返回此矩阵乘以标量的结果。
     *
     * @param scalar the scalar / 标量
     * @return the scaled matrix / 缩放后的矩阵
     */
    public Matrix scalarMultiply(double scalar) {
        if (!Double.isFinite(scalar)) {
            throw new MathException("Scalar must be finite: " + scalar + " / 标量必须有限");
        }
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = data[i][j] * scalar;
            }
        }
        return new Matrix(result, rows, cols);
    }

    /**
     * Returns the transpose of this matrix.
     * 返回此矩阵的转置。
     *
     * @return the transposed matrix / 转置矩阵
     */
    public Matrix transpose() {
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = data[i][j];
            }
        }
        return new Matrix(result, cols, rows);
    }

    /**
     * Returns the determinant of this square matrix.
     * 返回此方阵的行列式。
     *
     * <p>Uses LU decomposition with partial pivoting for matrices larger than 3x3.</p>
     * <p>对于大于 3x3 的矩阵使用带部分主元选取的 LU 分解。</p>
     *
     * @return the determinant / 行列式
     * @throws MathException if the matrix is not square / 如果矩阵不是方阵
     */
    public double determinant() {
        requireSquare();
        int n = rows;
        if (n == 1) {
            return data[0][0];
        }
        if (n == 2) {
            return data[0][0] * data[1][1] - data[0][1] * data[1][0];
        }
        if (n == 3) {
            return data[0][0] * (data[1][1] * data[2][2] - data[1][2] * data[2][1])
                 - data[0][1] * (data[1][0] * data[2][2] - data[1][2] * data[2][0])
                 + data[0][2] * (data[1][0] * data[2][1] - data[1][1] * data[2][0]);
        }
        // LU decomposition with partial pivoting
        return luDeterminant(n);
    }

    /**
     * Returns the inverse of this square matrix using LU decomposition with partial pivoting.
     * 使用带部分主元选取的 LU 分解返回此方阵的逆矩阵。
     *
     * <p>Uses in-place LU decomposition followed by forward/back substitution for each
     * column of the identity matrix, reducing memory usage by ~50% compared to
     * the augmented matrix approach.</p>
     * <p>使用原地 LU 分解，再对单位矩阵的每一列进行前向/回代求解，
     * 相比增广矩阵方法减少约 50% 内存使用。</p>
     *
     * @return the inverse matrix / 逆矩阵
     * @throws MathException if the matrix is not square or is singular
     *                       / 如果矩阵不是方阵或是奇异矩阵
     */
    public Matrix inverse() {
        requireSquare();
        int n = rows;

        // LU decomposition with partial pivoting
        double[][] lu = new double[n][n];
        int[] perm = new int[n];
        luDecompose(data, lu, perm, n, true);

        // Solve for each column of the inverse via forward + back substitution
        // PA = LU, so solve LUx = P*e_c for each column c
        double[][] result = new double[n][n];
        double[] pb = new double[n]; // reuse single work array across all columns
        for (int c = 0; c < n; c++) {
            // Permuted identity column: (P*e_c)[i] = 1 if perm[i]==c, else 0
            for (int i = 0; i < n; i++) {
                pb[i] = (perm[i] == c) ? 1.0 : 0.0;
            }

            // Forward substitution (Ly = Pb), L has unit diagonal
            for (int i = 1; i < n; i++) {
                double sum = 0.0;
                for (int j = 0; j < i; j++) {
                    sum += lu[i][j] * pb[j];
                }
                pb[i] -= sum;
            }

            // Back substitution (Ux = y)
            for (int i = n - 1; i >= 0; i--) {
                double sum = 0.0;
                for (int j = i + 1; j < n; j++) {
                    sum += lu[i][j] * pb[j];
                }
                pb[i] = (pb[i] - sum) / lu[i][i];
            }

            for (int i = 0; i < n; i++) {
                result[i][c] = pb[i];
            }
        }
        return new Matrix(result, n, n);
    }

    /**
     * Returns the trace (sum of diagonal elements) of this square matrix.
     * 返回此方阵的迹（对角线元素之和）。
     *
     * @return the trace / 迹
     * @throws MathException if the matrix is not square / 如果矩阵不是方阵
     */
    public double trace() {
        requireSquare();
        double sum = 0.0;
        for (int i = 0; i < rows; i++) {
            sum += data[i][i];
        }
        return sum;
    }

    /**
     * Returns {@code true} if this matrix is square.
     * 如果此矩阵是方阵，返回 {@code true}。
     *
     * @return whether the matrix is square / 是否为方阵
     */
    public boolean isSquare() {
        return rows == cols;
    }

    /**
     * Returns {@code true} if this matrix is symmetric (A = A^T).
     * 如果此矩阵是对称的（A = A^T），返回 {@code true}。
     *
     * @return whether the matrix is symmetric / 是否对称
     */
    public boolean isSymmetric() {
        if (!isSquare()) return false;
        for (int i = 0; i < rows; i++) {
            for (int j = i + 1; j < cols; j++) {
                if (Double.compare(data[i][j], data[j][i]) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a defensive copy of the internal data as a 2D array.
     * 返回内部数据的防御性拷贝（二维数组）。
     *
     * @return a copy of the data / 数据的拷贝
     */
    public double[][] toArray() {
        double[][] copy = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(data[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matrix other)) return false;
        if (rows != other.rows || cols != other.cols) return false;
        for (int i = 0; i < rows; i++) {
            if (!Arrays.equals(data[i], other.data[i])) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 31 * rows + cols;
        for (int i = 0; i < rows; i++) {
            h = 31 * h + Arrays.hashCode(data[i]);
        }
        return h;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Matrix[").append(rows).append("x").append(cols).append("]\n");
        for (int i = 0; i < rows; i++) {
            sb.append(Arrays.toString(data[i]));
            if (i < rows - 1) sb.append('\n');
        }
        return sb.toString();
    }

    // ---- internal helpers ----

    private void checkBounds(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new MathException("Index (" + row + ", " + col + ") out of bounds for "
                    + rows + "x" + cols + " matrix / 索引 (" + row + ", " + col
                    + ") 超出 " + rows + "x" + cols + " 矩阵范围");
        }
    }

    private void requireSquare() {
        if (rows != cols) {
            throw new MathException("Matrix is not square: " + rows + "x" + cols
                    + " / 矩阵不是方阵: " + rows + "x" + cols);
        }
    }

    private void requireSameDimensions(Matrix other) {
        if (this.rows != other.rows || this.cols != other.cols) {
            throw new MathException("Dimension mismatch: " + rows + "x" + cols + " vs "
                    + other.rows + "x" + other.cols
                    + " / 维度不匹配: " + rows + "x" + cols + " 与 "
                    + other.rows + "x" + other.cols);
        }
    }

    /**
     * Computes determinant via LU decomposition with partial pivoting.
     */
    private double luDeterminant(int n) {
        double[][] lu = new double[n][n];
        int[] perm = new int[n]; // not used for determinant, but required by shared method
        int swaps = luDecompose(data, lu, perm, n, false);
        if (swaps < 0) {
            return 0.0; // Singular
        }
        double det = (swaps % 2 == 0) ? 1.0 : -1.0;
        for (int i = 0; i < n; i++) {
            det *= lu[i][i];
        }
        return det;
    }

    /**
     * Shared LU decomposition with partial pivoting.
     *
     * @param src        source data (not modified)
     * @param lu         output LU matrix (pre-allocated n×n)
     * @param perm       output permutation array (pre-allocated n)
     * @param n          matrix dimension
     * @param throwOnSingular if true, throw on singular; if false, return -1
     * @return number of row swaps, or -1 if singular and throwOnSingular is false
     */
    private static int luDecompose(double[][] src, double[][] lu, int[] perm, int n,
                                   boolean throwOnSingular) {
        for (int i = 0; i < n; i++) {
            System.arraycopy(src[i], 0, lu[i], 0, n);
            perm[i] = i;
        }
        int swaps = 0;
        for (int col = 0; col < n; col++) {
            int maxRow = col;
            double maxVal = Math.abs(lu[col][col]);
            for (int row = col + 1; row < n; row++) {
                double absVal = Math.abs(lu[row][col]);
                if (absVal > maxVal) {
                    maxVal = absVal;
                    maxRow = row;
                }
            }
            if (maxVal < EPSILON) {
                if (throwOnSingular) {
                    throw new MathException("Matrix is singular and cannot be inverted"
                            + " / 矩阵是奇异的，无法求逆");
                }
                return -1;
            }
            if (maxRow != col) {
                double[] tmpRow = lu[col];
                lu[col] = lu[maxRow];
                lu[maxRow] = tmpRow;
                int tmpIdx = perm[col];
                perm[col] = perm[maxRow];
                perm[maxRow] = tmpIdx;
                swaps++;
            }
            for (int row = col + 1; row < n; row++) {
                double factor = lu[row][col] / lu[col][col];
                lu[row][col] = factor; // store L factor in lower triangle
                for (int j = col + 1; j < n; j++) {
                    lu[row][j] -= factor * lu[col][j];
                }
            }
        }
        return swaps;
    }
}
