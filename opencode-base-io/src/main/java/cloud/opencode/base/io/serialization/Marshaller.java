package cloud.opencode.base.io.serialization;

/**
 * Marshaller - Message Serialization Interface
 * 编组器 - 消息序列化接口
 *
 * <p>Defines how messages are serialized to and deserialized from byte arrays.</p>
 * <p>定义消息如何序列化为字节数组以及从字节数组反序列化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bidirectional serialization: marshal (object to bytes) and unmarshal (bytes to object) - 双向序列化</li>
 *   <li>Generic type parameter for type safety - 泛型参数保证类型安全</li>
 *   <li>Simple SPI for custom serialization strategies - 自定义序列化策略的简单SPI</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Marshaller<MyMessage> marshaller = new JsonMarshaller<>(MyMessage.class);
 * byte[] data = marshaller.marshal(message);
 * MyMessage restored = marshaller.unmarshal(data);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @param <T> the message type - 消息类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public interface Marshaller<T> {

    /**
     * Serializes a message to bytes.
     * 将消息序列化为字节。
     *
     * @param value the message to serialize - 要序列化的消息
     * @return the serialized bytes - 序列化的字节
     */
    byte[] marshal(T value);

    /**
     * Deserializes a message from bytes.
     * 从字节反序列化消息。
     *
     * @param data the bytes to deserialize - 要反序列化的字节
     * @return the deserialized message - 反序列化的消息
     */
    T unmarshal(byte[] data);
}
