package cloud.opencode.base.neural.tensor;

/**
 * Tensor Data Type Enumeration
 * 张量数据类型枚举
 *
 * <p>Defines the supported element data types for tensors, each with a fixed byte size.
 * Currently supported types include single/double precision floats, 32/64-bit integers,
 * and unsigned 8-bit integers.</p>
 * <p>定义张量支持的元素数据类型，每种类型具有固定的字节大小。
 * 当前支持的类型包括单精度/双精度浮点数、32/64位整数和无符号8位整数。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public enum TensorType {

    /**
     * 32-bit floating point (4 bytes)
     * 32位浮点数（4字节）
     */
    FLOAT32(4),

    /**
     * 64-bit floating point (8 bytes)
     * 64位双精度浮点数（8字节）
     */
    FLOAT64(8),

    /**
     * 32-bit signed integer (4 bytes)
     * 32位有符号整数（4字节）
     */
    INT32(4),

    /**
     * 64-bit signed integer (8 bytes)
     * 64位有符号整数（8字节）
     */
    INT64(8),

    /**
     * Unsigned 8-bit integer (1 byte)
     * 无符号8位整数（1字节）
     */
    UINT8(1);

    private final int byteSize;

    TensorType(int byteSize) {
        this.byteSize = byteSize;
    }

    /**
     * Get the byte size of this data type
     * 获取此数据类型的字节大小
     *
     * @return byte size per element | 每个元素的字节大小
     */
    public int byteSize() {
        return byteSize;
    }
}
