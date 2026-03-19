package cloud.opencode.base.crypto.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Secure memory erasure utilities to prevent sensitive data leakage - Overwrites memory with zeros to prevent data recovery
 * 安全内存擦除工具，防止敏感数据泄露 - 用零覆盖内存以防止数据恢复
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Secure memory erasure for sensitive data - 敏感数据的安全内存擦除</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SecureEraser.erase(sensitiveBytes);
 * SecureEraser.erase(sensitiveChars);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class SecureEraser {

    private SecureEraser() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Securely erase byte array by overwriting with zeros
     * 通过用零覆盖来安全擦除字节数组
     *
     * <p>This method overwrites the entire array with zeros to prevent
     * sensitive data from remaining in memory. The array reference remains
     * valid but all data is zeroed.
     * 此方法用零覆盖整个数组，以防止敏感数据残留在内存中。数组引用保持有效，但所有数据都被清零。
     *
     * @param data byte array to erase (can be null)
     */
    public static void erase(byte[] data) {
        if (data == null) {
            return;
        }

        Arrays.fill(data, (byte) 0);
    }

    /**
     * Securely erase char array by overwriting with zeros
     * 通过用零覆盖来安全擦除字符数组
     *
     * <p>This method is particularly important for erasing passwords and
     * other sensitive character data that should not remain in memory.
     * 此方法对于擦除密码和其他不应保留在内存中的敏感字符数据特别重要。
     *
     * @param data char array to erase (can be null)
     */
    public static void erase(char[] data) {
        if (data == null) {
            return;
        }

        Arrays.fill(data, '\0');
    }

    /**
     * Securely erase ByteBuffer by overwriting with zeros
     * 通过用零覆盖来安全擦除ByteBuffer
     *
     * <p>This method works with both direct and heap ByteBuffers.
     * The buffer's position and limit are preserved.
     * 此方法适用于直接和堆ByteBuffer。缓冲区的位置和限制被保留。
     *
     * @param buffer ByteBuffer to erase (can be null)
     */
    public static void erase(ByteBuffer buffer) {
        if (buffer == null) {
            return;
        }

        // Save current position and limit
        int originalPosition = buffer.position();
        int originalLimit = buffer.limit();

        try {
            // Reset to beginning and set limit to capacity
            buffer.clear();

            // Overwrite with zeros
            while (buffer.hasRemaining()) {
                buffer.put((byte) 0);
            }
        } finally {
            // Restore original position and limit
            buffer.limit(originalLimit);
            buffer.position(originalPosition);
        }
    }

    /**
     * Securely erase int array by overwriting with zeros
     * 通过用零覆盖来安全擦除整数数组
     *
     * @param data int array to erase (can be null)
     */
    public static void erase(int[] data) {
        if (data == null) {
            return;
        }

        Arrays.fill(data, 0);
    }

    /**
     * Securely erase long array by overwriting with zeros
     * 通过用零覆盖来安全擦除长整数数组
     *
     * @param data long array to erase (can be null)
     */
    public static void erase(long[] data) {
        if (data == null) {
            return;
        }

        Arrays.fill(data, 0L);
    }
}
