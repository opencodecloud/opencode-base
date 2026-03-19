/**
 * OpenCode Base Serialization Module
 * OpenCode 基础序列化模块
 *
 * <p>Provides unified serialization/deserialization capabilities
 * with support for multiple formats via SPI mechanism.</p>
 * <p>提供统一的序列化/反序列化能力，通过 SPI 机制支持多种格式。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified API - OpenSerializer facade - 统一 API 门面</li>
 *   <li>Multiple Formats - JSON, XML, JDK, Kryo, Protobuf - 多格式支持</li>
 *   <li>TypeReference - Generic type support - 泛型类型支持</li>
 *   <li>Compression - GZIP, LZ4, Snappy, ZSTD - 压缩支持</li>
 *   <li>SPI Extension - Pluggable serializers - 可插拔序列化器</li>
 *   <li>Deep Copy &amp; Type Conversion - 深拷贝与类型转换</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
module cloud.opencode.base.serialization {
    // Required modules
    requires transitive cloud.opencode.base.core;
    requires cloud.opencode.base.json;

    // Optional dependencies (available at compile time, may be absent at runtime)
    // Note: cloud.opencode.base.deepclone is detected via reflection (no module-info)
    requires static com.esotericsoftware.kryo;
    requires static com.google.protobuf;
    requires static jakarta.xml.bind;

    // Export public API packages
    exports cloud.opencode.base.serialization;
    exports cloud.opencode.base.serialization.binary;
    exports cloud.opencode.base.serialization.compress;
    exports cloud.opencode.base.serialization.exception;
    exports cloud.opencode.base.serialization.json;
    exports cloud.opencode.base.serialization.spi;
    exports cloud.opencode.base.serialization.xml;

    // SPI: Allow ServiceLoader to find SerializerProvider implementations
    uses cloud.opencode.base.serialization.spi.SerializerProvider;

    // Provide built-in serializers via SPI
    provides cloud.opencode.base.serialization.spi.SerializerProvider with
            cloud.opencode.base.serialization.json.JsonSerializerProvider,
            cloud.opencode.base.serialization.xml.XmlSerializerProvider,
            cloud.opencode.base.serialization.binary.JdkSerializerProvider,
            cloud.opencode.base.serialization.binary.KryoSerializerProvider,
            cloud.opencode.base.serialization.binary.ProtobufSerializerProvider;
}
