
package cloud.opencode.base.serialization;

import java.util.Objects;

/**
 * SerializerInfo - Serializer Capability and Metadata Descriptor
 * 序列化器能力与元数据描述符
 *
 * <p>An immutable record that describes a serializer's capabilities, including its format,
 * MIME type, whether it is text-based, and optional features like streaming and compression support.</p>
 * <p>一个不可变记录，描述序列化器的能力，包括格式、MIME 类型、是否基于文本，
 * 以及流式处理和压缩支持等可选功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Format identification - 格式标识</li>
 *   <li>MIME type metadata - MIME 类型元数据</li>
 *   <li>Capability flags (text, streaming, compression) - 能力标志（文本、流式、压缩）</li>
 *   <li>Human-readable description - 可读描述</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get info from a serializer
 * SerializerInfo info = serializer.info();
 * System.out.println(info.format());       // "json"
 * System.out.println(info.mimeType());     // "application/json"
 * System.out.println(info.textBased());    // true
 *
 * // Create directly
 * SerializerInfo info = new SerializerInfo("json", "application/json", true, true, false, "JSON serializer");
 *
 * // List all serializer infos
 * List<SerializerInfo> all = OpenSerializer.listSerializers();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param format              the serialization format name (e.g., "json", "xml", "kryo") | 序列化格式名称
 * @param mimeType            the MIME type (e.g., "application/json") | MIME 类型
 * @param textBased           whether the serializer produces text output | 是否产生文本输出
 * @param supportsStreaming   whether the serializer natively supports streaming | 是否原生支持流式处理
 * @param supportsCompression whether the serializer supports built-in compression | 是否支持内置压缩
 * @param description         a human-readable description | 可读描述
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
public record SerializerInfo(
        String format,
        String mimeType,
        boolean textBased,
        boolean supportsStreaming,
        boolean supportsCompression,
        String description
) {

    /**
     * Canonical constructor with validation.
     * 带验证的规范构造函数。
     *
     * @param format              the serialization format name | 序列化格式名称
     * @param mimeType            the MIME type | MIME 类型
     * @param textBased           whether text-based | 是否基于文本
     * @param supportsStreaming   whether streaming is supported | 是否支持流式
     * @param supportsCompression whether compression is supported | 是否支持压缩
     * @param description         the description | 描述
     */
    public SerializerInfo {
        Objects.requireNonNull(format, "Format must not be null");
        Objects.requireNonNull(mimeType, "MIME type must not be null");
        if (description == null) {
            description = "";
        }
    }

    @Override
    public String toString() {
        return "SerializerInfo[format=" + format
                + ", mimeType=" + mimeType
                + ", textBased=" + textBased
                + ", streaming=" + supportsStreaming
                + ", compression=" + supportsCompression
                + ", description=" + description
                + "]";
    }
}
