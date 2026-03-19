package cloud.opencode.base.hash;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Streaming hash calculator interface
 * 流式哈希计算器接口
 *
 * <p>Provides a fluent API for incrementally building hash values by
 * adding data piece by piece. This is useful for hashing large data
 * or combining multiple values into a single hash.</p>
 * <p>提供流畅的API，通过逐块添加数据来增量构建哈希值。
 * 这对于哈希大数据或将多个值组合成单个哈希非常有用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent method chaining - 流畅的方法链</li>
 *   <li>Multiple data type support - 多种数据类型支持</li>
 *   <li>Object hashing via Funnel - 通过Funnel的对象哈希</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Hasher hasher = OpenHash.murmur3_128().newHasher();
 * HashCode hash = hasher
 *     .putUtf8("Hello")
 *     .putInt(42)
 *     .putLong(System.currentTimeMillis())
 *     .hash();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (stateful) - 线程安全: 否（有状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public interface Hasher {

    /**
     * Adds a byte to the hash computation
     * 向哈希计算添加一个字节
     *
     * @param b byte value | 字节值
     * @return this hasher | 此hasher
     */
    Hasher putByte(byte b);

    /**
     * Adds a byte array to the hash computation
     * 向哈希计算添加字节数组
     *
     * @param bytes byte array | 字节数组
     * @return this hasher | 此hasher
     */
    Hasher putBytes(byte[] bytes);

    /**
     * Adds a portion of a byte array to the hash computation
     * 向哈希计算添加字节数组的一部分
     *
     * @param bytes  byte array | 字节数组
     * @param offset starting offset | 起始偏移
     * @param length number of bytes | 字节数
     * @return this hasher | 此hasher
     */
    Hasher putBytes(byte[] bytes, int offset, int length);

    /**
     * Adds a ByteBuffer to the hash computation
     * 向哈希计算添加ByteBuffer
     *
     * @param buffer byte buffer | 字节缓冲区
     * @return this hasher | 此hasher
     */
    Hasher putBytes(ByteBuffer buffer);

    /**
     * Adds a short value to the hash computation
     * 向哈希计算添加short值
     *
     * @param s short value | short值
     * @return this hasher | 此hasher
     */
    Hasher putShort(short s);

    /**
     * Adds an int value to the hash computation
     * 向哈希计算添加int值
     *
     * @param i int value | int值
     * @return this hasher | 此hasher
     */
    Hasher putInt(int i);

    /**
     * Adds a long value to the hash computation
     * 向哈希计算添加long值
     *
     * @param l long value | long值
     * @return this hasher | 此hasher
     */
    Hasher putLong(long l);

    /**
     * Adds a float value to the hash computation
     * 向哈希计算添加float值
     *
     * @param f float value | float值
     * @return this hasher | 此hasher
     */
    Hasher putFloat(float f);

    /**
     * Adds a double value to the hash computation
     * 向哈希计算添加double值
     *
     * @param d double value | double值
     * @return this hasher | 此hasher
     */
    Hasher putDouble(double d);

    /**
     * Adds a boolean value to the hash computation
     * 向哈希计算添加boolean值
     *
     * @param b boolean value | boolean值
     * @return this hasher | 此hasher
     */
    Hasher putBoolean(boolean b);

    /**
     * Adds a char value to the hash computation
     * 向哈希计算添加char值
     *
     * @param c char value | char值
     * @return this hasher | 此hasher
     */
    Hasher putChar(char c);

    /**
     * Adds a string with specified charset to the hash computation
     * 向哈希计算添加指定字符集的字符串
     *
     * @param charSequence string | 字符串
     * @param charset      character set | 字符集
     * @return this hasher | 此hasher
     */
    Hasher putString(CharSequence charSequence, Charset charset);

    /**
     * Adds a UTF-8 string to the hash computation
     * 向哈希计算添加UTF-8字符串
     *
     * @param charSequence string | 字符串
     * @return this hasher | 此hasher
     */
    default Hasher putUtf8(CharSequence charSequence) {
        return putString(charSequence, StandardCharsets.UTF_8);
    }

    /**
     * Adds an object using a Funnel to the hash computation
     * 使用Funnel向哈希计算添加对象
     *
     * @param instance object instance | 对象实例
     * @param funnel   serialization funnel | 序列化通道
     * @param <T>      object type | 对象类型
     * @return this hasher | 此hasher
     */
    <T> Hasher putObject(T instance, Funnel<? super T> funnel);

    /**
     * Computes the hash value
     * 计算哈希值
     *
     * <p>This method can only be called once. After calling hash(),
     * this Hasher should not be used again.</p>
     * <p>此方法只能调用一次。调用hash()后，此Hasher不应再使用。</p>
     *
     * @return computed hash code | 计算的哈希码
     */
    HashCode hash();
}
