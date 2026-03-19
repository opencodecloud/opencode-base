package cloud.opencode.base.core;

/**
 * Bit Manipulation Utility Class - Set, clear, flip, test, count, rotate and mask operations
 * 位操作工具类 - 位设置、清除、翻转、测试、计数、旋转和掩码操作
 *
 * <p>Provides comprehensive bit manipulation functions for int and long types.</p>
 * <p>提供全面的位操作功能，支持 int 和 long 类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bit setting (setBit, clearBit, flipBit) - 位设置</li>
 *   <li>Bit testing (testBit) - 位测试</li>
 *   <li>Bit counting (countBits, countLeadingZeros, countTrailingZeros) - 位计数</li>
 *   <li>Bit rotation (rotateLeft, rotateRight) - 位旋转</li>
 *   <li>Bit reversal (reverse, reverseBytes) - 位反转</li>
 *   <li>Bit field operations (extractField, insertField) - 位字段操作</li>
 *   <li>Mask creation (createMask, createMaskLong) - 掩码创建</li>
 *   <li>Power of two checks (isPowerOfTwo, nextPowerOfTwo) - 2 的幂检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Set/Test bit - 设置/测试位
 * int value = OpenBit.setBit(0, 3);      // 8 (binary: 1000)
 * boolean set = OpenBit.testBit(8, 3);   // true
 *
 * // Count bits - 计算位数
 * int ones = OpenBit.countBits(0b1010);  // 2
 *
 * // Power of two - 2 的幂
 * boolean isPow2 = OpenBit.isPowerOfTwo(16); // true
 * int next = OpenBit.nextPowerOfTwo(7);      // 8
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: N/A (primitive types) - 空值安全: 不适用 (原始类型)</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenBit {

    private OpenBit() {
    }

    // ==================== 位设置 ====================

    /**
     * Sets the bit at the specified position
     * 设置指定位置的位
     *
     * @param value    the original value | 原始值
     * @param position the bit position (0-31) | 位置（0-31）
     * @return the value with the bit set | 设置后的值
     */
    public static int setBit(int value, int position) {
        return value | (1 << position);
    }

    /**
     * Sets the bit at the specified position (long)
     * 设置指定位置的位（long）
     */
    public static long setBit(long value, int position) {
        return value | (1L << position);
    }

    // ==================== 位清除 ====================

    /**
     * Clears the bit at the specified position
     * 清除指定位置的位
     */
    public static int clearBit(int value, int position) {
        return value & ~(1 << position);
    }

    /**
     * Clears the bit at the specified position (long)
     * 清除指定位置的位（long）
     */
    public static long clearBit(long value, int position) {
        return value & ~(1L << position);
    }

    // ==================== 位翻转 ====================

    /**
     * Flips the bit at the specified position
     * 翻转指定位置的位
     */
    public static int flipBit(int value, int position) {
        return value ^ (1 << position);
    }

    /**
     * Flips the bit at the specified position (long)
     * 翻转指定位置的位（long）
     */
    public static long flipBit(long value, int position) {
        return value ^ (1L << position);
    }

    // ==================== 位测试 ====================

    /**
     * Tests whether the bit at the specified position is set
     * 测试指定位置的位是否被设置
     */
    public static boolean testBit(int value, int position) {
        return (value & (1 << position)) != 0;
    }

    /**
     * Tests whether the bit at the specified position is set (long)
     * 测试指定位置的位是否被设置（long）
     */
    public static boolean testBit(long value, int position) {
        return (value & (1L << position)) != 0;
    }

    // ==================== 位计数 ====================

    /**
     * Counts the number of set bits (number of 1s)
     * 计算设置的位数（1 的个数）
     */
    public static int countBits(int value) {
        return Integer.bitCount(value);
    }

    /**
     * Counts the number of set bits (long)
     * 计算设置的位数（long）
     */
    public static int countBits(long value) {
        return Long.bitCount(value);
    }

    /**
     * Counts the number of leading zeros
     * 计算前导零数量
     */
    public static int countLeadingZeros(int value) {
        return Integer.numberOfLeadingZeros(value);
    }

    /**
     * Counts the number of leading zeros (long)
     * 计算前导零数量（long）
     */
    public static int countLeadingZeros(long value) {
        return Long.numberOfLeadingZeros(value);
    }

    /**
     * Counts the number of trailing zeros
     * 计算尾随零数量
     */
    public static int countTrailingZeros(int value) {
        return Integer.numberOfTrailingZeros(value);
    }

    /**
     * Counts the number of trailing zeros (long)
     * 计算尾随零数量（long）
     */
    public static int countTrailingZeros(long value) {
        return Long.numberOfTrailingZeros(value);
    }

    // ==================== 位旋转 ====================

    /**
     * Rotates bits to the left
     * 左旋转位
     */
    public static int rotateLeft(int value, int distance) {
        return Integer.rotateLeft(value, distance);
    }

    /**
     * Rotates bits to the left (long)
     * 左旋转位（long）
     */
    public static long rotateLeft(long value, int distance) {
        return Long.rotateLeft(value, distance);
    }

    /**
     * Rotates bits to the right
     * 右旋转位
     */
    public static int rotateRight(int value, int distance) {
        return Integer.rotateRight(value, distance);
    }

    /**
     * Rotates bits to the right (long)
     * 右旋转位（long）
     */
    public static long rotateRight(long value, int distance) {
        return Long.rotateRight(value, distance);
    }

    // ==================== 位反转 ====================

    /**
     * Reverses all bits
     * 反转所有位
     */
    public static int reverse(int value) {
        return Integer.reverse(value);
    }

    /**
     * Reverses all bits (long)
     * 反转所有位（long）
     */
    public static long reverse(long value) {
        return Long.reverse(value);
    }

    /**
     * Reverses the byte order
     * 反转字节顺序
     */
    public static int reverseBytes(int value) {
        return Integer.reverseBytes(value);
    }

    /**
     * Reverses the byte order (long)
     * 反转字节顺序（long）
     */
    public static long reverseBytes(long value) {
        return Long.reverseBytes(value);
    }

    // ==================== 位字段操作 ====================

    /**
     * Extracts a bit field
     * 提取位字段
     *
     * @param value    the original value | 原始值
     * @param position the start position | 起始位置
     * @param length   the length | 长度
     * @return the extracted value | 提取的值
     */
    public static int extractField(int value, int position, int length) {
        if (length >= 32) {
            return value >> position;
        }
        int mask = (1 << length) - 1;
        return (value >> position) & mask;
    }

    /**
     * Extracts a bit field (long)
     * 提取位字段（long）
     */
    public static long extractField(long value, int position, int length) {
        if (length >= 64) {
            return value >> position;
        }
        long mask = (1L << length) - 1;
        return (value >> position) & mask;
    }

    /**
     * Inserts a bit field
     * 插入位字段
     *
     * @param value      the original value | 原始值
     * @param fieldValue the field value to insert | 要插入的字段值
     * @param position   the start position | 起始位置
     * @param length     the length | 长度
     * @return the value after insertion | 插入后的值
     */
    public static int insertField(int value, int fieldValue, int position, int length) {
        if (length >= 32) {
            return fieldValue << position;
        }
        int mask = (1 << length) - 1;
        int clearMask = ~(mask << position);
        return (value & clearMask) | ((fieldValue & mask) << position);
    }

    /**
     * Inserts a bit field (long)
     * 插入位字段（long）
     */
    public static long insertField(long value, long fieldValue, int position, int length) {
        if (length >= 64) {
            return fieldValue << position;
        }
        long mask = (1L << length) - 1;
        long clearMask = ~(mask << position);
        return (value & clearMask) | ((fieldValue & mask) << position);
    }

    // ==================== 位掩码 ====================

    /**
     * Creates a bit mask
     * 创建位掩码
     *
     * @param bits the number of bits | 位数
     * @return the mask | 掩码
     */
    public static int createMask(int bits) {
        if (bits <= 0) return 0;
        if (bits >= 32) return -1;
        return (1 << bits) - 1;
    }

    /**
     * Creates a bit mask (long)
     * 创建位掩码（long）
     */
    public static long createMaskLong(int bits) {
        if (bits <= 0) return 0L;
        if (bits >= 64) return -1L;
        return (1L << bits) - 1;
    }

    // ==================== 2 的幂检查 ====================

    /**
     * Checks whether the value is a power of two
     * 检查是否为 2 的幂
     */
    public static boolean isPowerOfTwo(int value) {
        return value > 0 && (value & (value - 1)) == 0;
    }

    /**
     * Checks whether the value is a power of two (long)
     * 检查是否为 2 的幂（long）
     */
    public static boolean isPowerOfTwo(long value) {
        return value > 0 && (value & (value - 1)) == 0;
    }

    /**
     * Rounds up to the next power of two
     * 向上舍入到下一个 2 的幂
     */
    public static int nextPowerOfTwo(int value) {
        if (value <= 1) return 1;
        if (value > (1 << 30)) {
            throw new ArithmeticException("Next power of two overflows int: " + value);
        }
        return Integer.highestOneBit(value - 1) << 1;
    }

    /**
     * Rounds up to the next power of two (long)
     * 向上舍入到下一个 2 的幂（long）
     */
    public static long nextPowerOfTwo(long value) {
        if (value <= 1) return 1;
        if (value > (1L << 62)) {
            throw new ArithmeticException("Next power of two overflows long: " + value);
        }
        return Long.highestOneBit(value - 1) << 1;
    }

    /**
     * Gets the position of the highest set bit
     * 获取最高有效位的位置
     */
    public static int highestOneBitPosition(int value) {
        return 31 - Integer.numberOfLeadingZeros(value);
    }

    /**
     * Gets the position of the highest set bit (long)
     * 获取最高有效位的位置（long）
     */
    public static int highestOneBitPosition(long value) {
        return 63 - Long.numberOfLeadingZeros(value);
    }

    /**
     * Gets the position of the lowest set bit
     * 获取最低有效位的位置
     */
    public static int lowestOneBitPosition(int value) {
        return Integer.numberOfTrailingZeros(value);
    }

    /**
     * Gets the position of the lowest set bit (long)
     * 获取最低有效位的位置（long）
     */
    public static int lowestOneBitPosition(long value) {
        return Long.numberOfTrailingZeros(value);
    }
}
