package cloud.opencode.base.crypto.util;

import java.util.Arrays;

/**
 * Secure byte array wrapper with automatic memory erasure on close
 * 安全字节数组包装器，关闭时自动擦除内存
 *
 * <p>Wraps sensitive byte data (keys, plaintext, etc.) and ensures the
 * underlying memory is zeroed when the wrapper is closed. Implements
 * {@link AutoCloseable} for use with try-with-resources.</p>
 * <p>包装敏感字节数据（密钥、明文等），确保在关闭包装器时底层内存被清零。
 * 实现 {@link AutoCloseable} 以支持 try-with-resources 用法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Defensive copy via {@code of()} or zero-copy via {@code wrap()} - 通过 of() 防御性拷贝或通过 wrap() 零拷贝</li>
 *   <li>Automatic memory erasure on close - 关闭时自动擦除内存</li>
 *   <li>Constant-time equality comparison - 常量时间相等性比较</li>
 *   <li>Safe toString that never leaks content - 安全的 toString 不泄露内容</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (SecureBytes key = SecureBytes.of(rawKey)) {
 *     byte[] copy = key.getBytes();
 *     // use copy...
 * } // rawKey copy is zeroed here
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: No (null input throws NullPointerException) - 空值安全: 否（空输入抛出 NullPointerException）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see SecureEraser
 * @see ConstantTimeUtil
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.3
 */
public final class SecureBytes implements AutoCloseable {

    private final byte[] data;
    private volatile boolean closed;

    private SecureBytes(byte[] data) {
        this.data = data;
        this.closed = false;
    }

    /**
     * Create a SecureBytes instance with a defensive copy of the input data.
     * 创建 SecureBytes 实例，对输入数据进行防御性拷贝。
     *
     * <p>The caller retains ownership of the original array.</p>
     * <p>调用者保留对原始数组的所有权。</p>
     *
     * @param data the byte array to copy | 要拷贝的字节数组
     * @return a new SecureBytes wrapping a copy of the data | 包装数据副本的新 SecureBytes
     * @throws NullPointerException if data is null | 当 data 为 null 时抛出
     */
    public static SecureBytes of(byte[] data) {
        if (data == null) {
            throw new NullPointerException("data must not be null");
        }
        return new SecureBytes(data.clone());
    }

    /**
     * Create a SecureBytes instance that takes ownership of the given array (zero-copy).
     * 创建 SecureBytes 实例，接管给定数组的所有权（零拷贝）。
     *
     * <p>The caller must not modify or reference the array after calling this method.</p>
     * <p>调用者在调用此方法后不得修改或引用该数组。</p>
     *
     * @param data the byte array to take ownership of | 要接管所有权的字节数组
     * @return a new SecureBytes wrapping the data directly | 直接包装数据的新 SecureBytes
     * @throws NullPointerException if data is null | 当 data 为 null 时抛出
     */
    public static SecureBytes wrap(byte[] data) {
        if (data == null) {
            throw new NullPointerException("data must not be null");
        }
        return new SecureBytes(data);
    }

    /**
     * Return a defensive copy of the internal byte data.
     * 返回内部字节数据的防御性拷贝。
     *
     * @return a copy of the data | 数据的副本
     * @throws IllegalStateException if this SecureBytes has been closed | 当此 SecureBytes 已关闭时抛出
     */
    public byte[] getBytes() {
        ensureOpen();
        return data.clone();
    }

    /**
     * Return a direct reference to the internal byte data (hot-path optimization).
     * 返回内部字节数据的直接引用（热路径优化）。
     *
     * <p><strong>Warning:</strong> The caller must not modify the returned array
     * or retain a reference beyond the lifetime of this SecureBytes.</p>
     * <p><strong>警告：</strong>调用者不得修改返回的数组，也不得在此 SecureBytes 生命周期之外保留引用。</p>
     *
     * @return direct reference to internal data | 内部数据的直接引用
     * @throws IllegalStateException if this SecureBytes has been closed | 当此 SecureBytes 已关闭时抛出
     */
    public byte[] getBytesUnsafe() {
        ensureOpen();
        return data;
    }

    /**
     * Return the length of the byte data.
     * 返回字节数据的长度。
     *
     * @return length in bytes | 字节长度
     * @throws IllegalStateException if this SecureBytes has been closed | 当此 SecureBytes 已关闭时抛出
     */
    public int length() {
        ensureOpen();
        return data.length;
    }

    /**
     * Check whether this SecureBytes has been closed.
     * 检查此 SecureBytes 是否已关闭。
     *
     * @return true if closed, false otherwise | 如果已关闭返回 true，否则返回 false
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Close this SecureBytes by zeroing the internal data.
     * 通过清零内部数据来关闭此 SecureBytes。
     *
     * <p>After calling this method, all accessor methods will throw
     * {@link IllegalStateException}. Calling close multiple times is safe.</p>
     * <p>调用此方法后，所有访问器方法将抛出 {@link IllegalStateException}。
     * 多次调用 close 是安全的。</p>
     */
    @Override
    public void close() {
        if (!closed) {
            Arrays.fill(data, (byte) 0);
            closed = true;
        }
    }

    /**
     * Constant-time equality comparison using {@link ConstantTimeUtil}.
     * 使用 {@link ConstantTimeUtil} 进行常量时间相等性比较。
     *
     * @param o the object to compare | 要比较的对象
     * @return true if equal, false otherwise | 如果相等返回 true，否则返回 false
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SecureBytes other)) {
            return false;
        }
        // Always run constant-time comparison; XOR closed flags to avoid branching
        int closedFlag = (this.closed ? 1 : 0) | (other.closed ? 1 : 0);
        boolean contentEqual = ConstantTimeUtil.equals(this.data, other.data);
        return contentEqual && (closedFlag == 0);
    }

    /**
     * Returns a constant hash code to prevent leaking key content via hash-based data structures.
     * 返回常量哈希码，防止通过基于哈希的数据结构泄露密钥内容。
     *
     * <p>SecureBytes should not be used as HashMap keys. A constant hash code
     * eliminates the timing oracle that a data-dependent hash would create.</p>
     *
     * @return constant hash code | 常量哈希码
     */
    @Override
    public int hashCode() {
        return 31;
    }

    /**
     * Return a safe string representation that does not leak content.
     * 返回不泄露内容的安全字符串表示。
     *
     * @return "SecureBytes[length=N]" or "SecureBytes[closed]" | "SecureBytes[length=N]" 或 "SecureBytes[closed]"
     */
    @Override
    public String toString() {
        if (closed) {
            return "SecureBytes[closed]";
        }
        return "SecureBytes[length=" + data.length + "]";
    }

    /**
     * Ensure this SecureBytes has not been closed.
     * 确保此 SecureBytes 未被关闭。
     *
     * @throws IllegalStateException if closed | 当已关闭时抛出
     */
    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("SecureBytes has been closed");
        }
    }
}
