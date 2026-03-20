
package cloud.opencode.base.serialization.xml;

import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * XmlSerializer - XML Serialization using JAXB
 * XML 序列化器（使用 JAXB）
 *
 * <p>Provides XML serialization using JAXB (Jakarta XML Binding).
 * Objects should be annotated with JAXB annotations for best results.</p>
 * <p>使用 JAXB (Jakarta XML Binding) 提供 XML 序列化。
 * 对象应使用 JAXB 注解以获得最佳效果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Human-readable output - 人类可读的输出</li>
 *   <li>Standard XML format - 标准 XML 格式</li>
 *   <li>JAXB annotation support - JAXB 注解支持</li>
 *   <li>Schema validation capable - 可进行 Schema 验证</li>
 * </ul>
 *
 * <p><strong>Limitations | 限制:</strong></p>
 * <ul>
 *   <li>Limited generic type support - 有限的泛型类型支持</li>
 *   <li>Requires JAXB annotations for complex types - 复杂类型需要 JAXB 注解</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * XmlSerializer serializer = new XmlSerializer();
 *
 * // Serialize
 * byte[] data = serializer.serialize(user);
 *
 * // Deserialize
 * User restored = serializer.deserialize(data, User.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (JAXB context cached) - 线程安全: 是（JAXB 上下文已缓存）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = object graph size - O(n), n为对象图大小</li>
 *   <li>Space complexity: O(n) for XML output - XML输出 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public class XmlSerializer implements Serializer {

    /**
     * Format name
     * 格式名称
     */
    public static final String FORMAT = "xml";

    /**
     * JAXB context cache
     * JAXB 上下文缓存
     */
    private final ConcurrentMap<Class<?>, JAXBContext> contextCache = new ConcurrentHashMap<>();

    /**
     * Thread-local secure XMLInputFactory with XXE protection disabled.
     * XMLInputFactory is not thread-safe, so each thread gets its own instance.
     * 线程本地的安全XMLInputFactory，禁用XXE保护。
     * XMLInputFactory不是线程安全的，因此每个线程获取自己的实例。
     */
    private static final ThreadLocal<XMLInputFactory> SECURE_XML_INPUT_FACTORY =
        ThreadLocal.withInitial(XmlSerializer::createSecureXmlInputFactory);

    /**
     * Creates a secure XMLInputFactory with XXE protection.
     * 创建带XXE防护的安全XMLInputFactory。
     */
    private static XMLInputFactory createSecureXmlInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        // Disable external entities to prevent XXE attacks
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        // Prevent entity expansion attacks
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        return factory;
    }

    // ==================== Serializer Implementation | 序列化器实现 ====================

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return new byte[0];
        }

        try {
            JAXBContext context = getOrCreateContext(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                marshaller.marshal(obj, bos);
                return bos.toByteArray();
            }
        } catch (JAXBException | java.io.IOException e) {
            throw OpenSerializationException.serializeFailed(obj, FORMAT, e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> type) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            JAXBContext context = getOrCreateContext(type);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Use secure XMLStreamReader with XXE protection
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            XMLStreamReader xmlReader = SECURE_XML_INPUT_FACTORY.get().createXMLStreamReader(bis);
            try {
                Object result = unmarshaller.unmarshal(xmlReader);
                return type.cast(result);
            } finally {
                xmlReader.close();
            }
        } catch (JAXBException | XMLStreamException e) {
            throw OpenSerializationException.deserializeFailed(data, type, FORMAT, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, TypeReference<T> typeRef) {
        // XML has limited generic type support, fallback to raw type
        return (T) deserialize(data, typeRef.getRawType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Type type) {
        if (type instanceof Class<?> clazz) {
            return (T) deserialize(data, clazz);
        }
        throw OpenSerializationException.unsupportedType(type, FORMAT);
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public String getMimeType() {
        return "application/xml";
    }

    @Override
    public boolean isTextBased() {
        return true;
    }

    // ==================== Helper Methods | 辅助方法 ====================

    /**
     * Gets or creates a JAXB context for the given class.
     * 获取或创建给定类的 JAXB 上下文。
     */
    private JAXBContext getOrCreateContext(Class<?> clazz) throws JAXBException {
        return contextCache.computeIfAbsent(clazz, c -> {
            try {
                return JAXBContext.newInstance(c);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Serializes an object to XML string.
     * 将对象序列化为 XML 字符串。
     *
     * @param obj the object to serialize - 要序列化的对象
     * @return the XML string - XML 字符串
     */
    public String serializeToString(Object obj) {
        byte[] bytes = serialize(obj);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Deserializes from XML string.
     * 从 XML 字符串反序列化。
     *
     * @param xml  the XML string - XML 字符串
     * @param type the target type - 目标类型
     * @param <T>  the target type - 目标类型
     * @return the deserialized object - 反序列化后的对象
     */
    public <T> T deserialize(String xml, Class<T> type) {
        if (xml == null || xml.isEmpty()) {
            return null;
        }

        try {
            JAXBContext context = getOrCreateContext(type);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Use secure XMLStreamReader with XXE protection
            XMLStreamReader xmlReader = SECURE_XML_INPUT_FACTORY.get().createXMLStreamReader(new StringReader(xml));
            try {
                Object result = unmarshaller.unmarshal(xmlReader);
                return type.cast(result);
            } finally {
                xmlReader.close();
            }
        } catch (JAXBException | XMLStreamException e) {
            throw OpenSerializationException.deserializeFailed(xml.getBytes(StandardCharsets.UTF_8), type, FORMAT, e);
        }
    }
}
