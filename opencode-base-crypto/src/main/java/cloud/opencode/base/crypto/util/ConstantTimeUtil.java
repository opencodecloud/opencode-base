package cloud.opencode.base.crypto.util;

import java.nio.charset.StandardCharsets;

/**
 * Constant-time comparison utilities to prevent timing attacks - All operations execute in constant time regardless of input values
 * 常量时间比较工具，防止时序攻击 - 所有操作都以恒定时间执行，与输入值无关
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Constant-time comparison to prevent timing attacks - 常量时间比较以防止时序攻击</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean equal = ConstantTimeUtil.equals(a, b);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - 时间复杂度: O(n)，n为数据长度，始终执行完整比较</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class ConstantTimeUtil {

    private ConstantTimeUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Constant-time byte array equality comparison
     * 常量时间字节数组相等性比较
     *
     * <p>This method executes in constant time to prevent timing attacks.
     * It compares every byte regardless of whether a difference is found early.
     * 此方法以恒定时间执行以防止时序攻击。无论是否提前发现差异，它都会比较每个字节。
     *
     * @param a first byte array
     * @param b second byte array
     * @return true if arrays are equal in length and content, false otherwise
     */
    public static boolean equals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return a == b;
        }

        // Length comparison must also be constant-time aware
        int lengthA = a.length;
        int lengthB = b.length;

        // Use the shorter length to avoid array index out of bounds
        int minLength = Math.min(lengthA, lengthB);

        // XOR accumulator - will be 0 only if all bytes are equal
        int result = lengthA ^ lengthB;

        // Compare all bytes in constant time
        for (int i = 0; i < minLength; i++) {
            result |= a[i] ^ b[i];
        }

        // If lengths differ, also XOR remaining bytes with zero
        // This ensures we always iterate through all bytes
        for (int i = minLength; i < lengthA; i++) {
            result |= a[i];
        }

        for (int i = minLength; i < lengthB; i++) {
            result |= b[i];
        }

        return result == 0;
    }

    /**
     * Constant-time string equality comparison
     * 常量时间字符串相等性比较
     *
     * <p>This method executes in constant time to prevent timing attacks.
     * Strings are converted to UTF-8 bytes and compared byte by byte.
     * 此方法以恒定时间执行以防止时序攻击。字符串转换为UTF-8字节并逐字节比较。
     *
     * @param a first string
     * @param b second string
     * @return true if strings are equal, false otherwise
     */
    public static boolean equals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }

        byte[] bytesA = a.getBytes(StandardCharsets.UTF_8);
        byte[] bytesB = b.getBytes(StandardCharsets.UTF_8);

        boolean result = equals(bytesA, bytesB);

        // Securely erase the byte arrays
        SecureEraser.erase(bytesA);
        SecureEraser.erase(bytesB);

        return result;
    }

    /**
     * Constant-time byte array comparison
     * 常量时间字节数组比较
     *
     * <p>This method executes in constant time to prevent timing attacks.
     * Returns a comparison result similar to {@link java.util.Arrays#compare}.
     * 此方法以恒定时间执行以防止时序攻击。返回类似于{@link java.util.Arrays#compare}的比较结果。
     *
     * @param a first byte array
     * @param b second byte array
     * @return negative if a &lt; b, zero if a == b, positive if a &gt; b
     * @throws NullPointerException if either array is null
     */
    public static int compare(byte[] a, byte[] b) {
        if (a == null || b == null) {
            throw new NullPointerException("Arrays must not be null");
        }

        int lengthA = a.length;
        int lengthB = b.length;
        int minLength = Math.min(lengthA, lengthB);

        // Comparison result accumulator
        int result = 0;
        int foundDifference = 0; // Flag to track if we found a difference

        // Compare all bytes in constant time
        for (int i = 0; i < minLength; i++) {
            int byteA = a[i] & 0xFF;
            int byteB = b[i] & 0xFF;
            int diff = byteA - byteB;

            // Use bit manipulation to avoid branching
            // If we haven't found a difference yet, use this difference
            // Otherwise, keep the previous result
            int mask = (foundDifference - 1); // All 1s if foundDifference == 0, all 0s otherwise
            result = (result & ~mask) | (diff & mask);

            // Update foundDifference flag using bitwise OR
            // Once set to non-zero, it stays non-zero
            foundDifference |= diff;
        }

        // If all compared bytes are equal, compare lengths
        // Use bit manipulation to avoid branching
        int lengthDiff = lengthA - lengthB;
        // Arithmetic right shift produces all-1s (-1) when foundDifference == 0, all-0s when != 0
        int mask = (foundDifference | -foundDifference) >> 31; // 0 if foundDifference == 0, -1 if foundDifference != 0
        // We want: if no difference found (mask==0), use lengthDiff; else use result
        result = (result & mask) | (lengthDiff & ~mask);

        return result;
    }
}
