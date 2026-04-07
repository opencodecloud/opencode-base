package cloud.opencode.base.hash;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Hash function interface
 * 哈希函数接口
 *
 * <p>Defines the contract for hash functions that can compute hash codes
 * for various input types including bytes, strings, and objects.</p>
 * <p>定义哈希函数的契约，可以计算各种输入类型的哈希码，包括字节、字符串和对象。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Byte array hashing - 字节数组哈希</li>
 *   <li>String hashing with charset - 带字符集的字符串哈希</li>
 *   <li>Primitive type hashing - 原始类型哈希</li>
 *   <li>Object hashing via Funnel - 通过Funnel的对象哈希</li>
 *   <li>Streaming hash via Hasher - 通过Hasher的流式哈希</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HashFunction murmur = OpenHash.murmur3_128();
 * HashCode hash = murmur.hashUtf8("Hello World");
 * System.out.println(hash.toHex());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public interface HashFunction {

    /**
     * Creates a new Hasher instance for streaming hash computation
     * 创建新的Hasher实例用于流式哈希计算
     *
     * @return a new Hasher (stateful, not thread-safe) | 新的Hasher（有状态，非线程安全）
     */
    Hasher newHasher();

    /**
     * Creates a Hasher with expected input size hint
     * 创建带预期输入大小提示的Hasher
     *
     * @param expectedInputSize expected input size in bytes | 预期输入大小（字节）
     * @return a new Hasher | 新的Hasher
     */
    Hasher newHasher(int expectedInputSize);

    /**
     * Computes hash of a byte array
     * 计算字节数组的哈希
     *
     * @param input input bytes | 输入字节
     * @return hash code | 哈希码
     */
    HashCode hashBytes(byte[] input);

    /**
     * Computes hash of a byte array portion
     * 计算字节数组部分的哈希
     *
     * @param input  input bytes | 输入字节
     * @param offset starting offset | 起始偏移
     * @param length number of bytes to hash | 要哈希的字节数
     * @return hash code | 哈希码
     */
    HashCode hashBytes(byte[] input, int offset, int length);

    /**
     * Computes hash of a string with specified charset
     * 计算指定字符集字符串的哈希
     *
     * @param input   input string | 输入字符串
     * @param charset character set | 字符集
     * @return hash code | 哈希码
     */
    default HashCode hashString(CharSequence input, Charset charset) {
        return hashBytes(input.toString().getBytes(charset));
    }

    /**
     * Computes hash of a UTF-8 string
     * 计算UTF-8字符串的哈希
     *
     * @param input input string | 输入字符串
     * @return hash code | 哈希码
     */
    default HashCode hashUtf8(CharSequence input) {
        return hashString(input, StandardCharsets.UTF_8);
    }

    /**
     * Computes hash of an int value
     * 计算int值的哈希
     *
     * @param input int value | int值
     * @return hash code | 哈希码
     */
    HashCode hashInt(int input);

    /**
     * Computes hash of a long value
     * 计算long值的哈希
     *
     * @param input long value | long值
     * @return hash code | 哈希码
     */
    HashCode hashLong(long input);

    /**
     * Computes hash of an object using a Funnel
     * 使用Funnel计算对象的哈希
     *
     * @param instance object instance | 对象实例
     * @param funnel   serialization funnel | 序列化通道
     * @param <T>      object type | 对象类型
     * @return hash code | 哈希码
     */
    <T> HashCode hashObject(T instance, Funnel<? super T> funnel);

    /**
     * Gets the number of bits in the hash output
     * 获取哈希输出的位数
     *
     * @return number of bits | 位数
     */
    int bits();

    /**
     * Gets the algorithm name
     * 获取算法名称
     *
     * @return algorithm name | 算法名称
     */
    String name();

    /**
     * Computes hash of an InputStream
     * 计算输入流的哈希
     *
     * <p>Reads the stream in 8KB chunks without loading the entire content into memory.
     * The stream is NOT closed by this method.</p>
     * <p>以8KB块读取流，不会将全部内容加载到内存。此方法不关闭流。</p>
     *
     * @param inputStream input stream | 输入流
     * @return hash code | 哈希码
     * @throws OpenHashException if reading fails | 如果读取失败
     */
    default HashCode hashInputStream(java.io.InputStream inputStream) {
        java.util.Objects.requireNonNull(inputStream, "inputStream");
        try {
            Hasher hasher = newHasher();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                hasher.putBytes(buffer, 0, read);
            }
            return hasher.hash();
        } catch (java.io.IOException e) {
            throw new cloud.opencode.base.hash.exception.OpenHashException(
                    "Failed to hash input stream", e);
        }
    }

    /**
     * Computes hash of a file
     * 计算文件的哈希
     *
     * <p>Reads the file in 8KB chunks without loading the entire content into memory.</p>
     * <p>以8KB块读取文件，不会将全部内容加载到内存。</p>
     *
     * @param path file path | 文件路径
     * @return hash code | 哈希码
     * @throws OpenHashException if reading fails | 如果读取失败
     */
    default HashCode hashFile(java.nio.file.Path path) {
        java.util.Objects.requireNonNull(path, "path");
        try (java.io.InputStream is = java.nio.file.Files.newInputStream(path)) {
            return hashInputStream(is);
        } catch (cloud.opencode.base.hash.exception.OpenHashException e) {
            throw e;
        } catch (java.io.IOException e) {
            throw new cloud.opencode.base.hash.exception.OpenHashException(
                    "Failed to hash file: " + path.getFileName() + " - " + e.getMessage(), e);
        }
    }
}
