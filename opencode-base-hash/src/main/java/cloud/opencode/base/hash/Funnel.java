package cloud.opencode.base.hash;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Object serialization funnel for hashing
 * 用于哈希的对象序列化通道
 *
 * <p>Defines how to serialize an object's data into a Hasher.
 * This allows custom objects to be hashed by extracting their
 * relevant fields into the hash computation.</p>
 * <p>定义如何将对象的数据序列化到Hasher中。
 * 这允许通过将相关字段提取到哈希计算中来哈希自定义对象。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom object serialization - 自定义对象序列化</li>
 *   <li>Built-in funnels for common types - 常见类型的内置funnel</li>
 *   <li>Lambda-friendly functional interface - Lambda友好的函数式接口</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Define a funnel for User
 * Funnel<User> userFunnel = (user, into) -> {
 *     into.putUtf8(user.getName())
 *         .putInt(user.getAge())
 *         .putUtf8(user.getEmail());
 * };
 *
 * // Hash a user object
 * HashCode hash = OpenHash.murmur3_128()
 *     .hashObject(user, userFunnel);
 * }</pre>
 *
 * @param <T> the type of object this funnel can serialize | 此funnel可以序列化的对象类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@FunctionalInterface
public interface Funnel<T> extends Serializable {

    /**
     * Serializes object data into a Hasher
     * 将对象数据序列化到Hasher中
     *
     * @param from source object | 源对象
     * @param into target hasher | 目标hasher
     */
    void funnel(T from, Hasher into);

    // ==================== Built-in Funnels | 内置Funnel ====================

    /**
     * String funnel (UTF-8 encoding)
     * 字符串Funnel（UTF-8编码）
     */
    Funnel<CharSequence> STRING_FUNNEL = (from, into) ->
            into.putString(from, StandardCharsets.UTF_8);

    /**
     * Byte array funnel
     * 字节数组Funnel
     */
    Funnel<byte[]> BYTE_ARRAY_FUNNEL = (from, into) ->
            into.putBytes(from);

    /**
     * Integer funnel
     * Integer Funnel
     */
    Funnel<Integer> INTEGER_FUNNEL = (from, into) ->
            into.putInt(from);

    /**
     * Long funnel
     * Long Funnel
     */
    Funnel<Long> LONG_FUNNEL = (from, into) ->
            into.putLong(from);

    /**
     * Double funnel
     * Double Funnel
     */
    Funnel<Double> DOUBLE_FUNNEL = (from, into) ->
            into.putDouble(from);

    /**
     * Boolean funnel
     * Boolean Funnel
     */
    Funnel<Boolean> BOOLEAN_FUNNEL = (from, into) ->
            into.putBoolean(from);

    /**
     * Character funnel
     * Character Funnel
     */
    Funnel<Character> CHARACTER_FUNNEL = (from, into) ->
            into.putChar(from);
}
