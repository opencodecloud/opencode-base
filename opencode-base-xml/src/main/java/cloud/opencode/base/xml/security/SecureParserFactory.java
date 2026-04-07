package cloud.opencode.base.xml.security;

import cloud.opencode.base.xml.exception.OpenXmlException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.io.InputStream;
import java.io.Reader;

/**
 * Secure Parser Factory - Factory for creating secure XML parsers
 * 安全解析器工厂 - 创建安全 XML 解析器的工厂
 *
 * <p>This class provides cached, thread-safe factories for creating secure XML parsers.
 * The factories are pre-configured with security settings to prevent XXE attacks.</p>
 * <p>此类提供缓存的线程安全工厂，用于创建安全的 XML 解析器。工厂已预配置安全设置以防止 XXE 攻击。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cached, thread-safe factories for secure XML parsers - 缓存的线程安全安全 XML 解析器工厂</li>
 *   <li>Pre-configured XXE attack prevention - 预配置的 XXE 攻击防护</li>
 *   <li>Supports DOM, SAX, StAX, and Transformer creation - 支持 DOM、SAX、StAX 和 Transformer 创建</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>The factories are thread-safe and can be reused. However, the parsers created
 * from them are NOT thread-safe and should be used by a single thread at a time.</p>
 * <p>工厂是线程安全的，可以重复使用。但是，从它们创建的解析器不是线程安全的，应该一次只由一个线程使用。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a secure DocumentBuilder
 * DocumentBuilder builder = SecureParserFactory.createDocumentBuilder();
 *
 * // Create a secure SAXParser
 * SAXParser parser = SecureParserFactory.createSAXParser();
 *
 * // Create a secure XMLStreamReader
 * XMLStreamReader reader = SecureParserFactory.createXMLStreamReader(inputStream);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for factory creation - 工厂创建 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class SecureParserFactory {

    // Thread-local secure factories (DocumentBuilderFactory and SAXParserFactory are not thread-safe)
    private static final ThreadLocal<DocumentBuilderFactory> DOCUMENT_BUILDER_FACTORY =
        ThreadLocal.withInitial(XmlSecurity::createSecureDocumentBuilderFactory);

    private static final ThreadLocal<SAXParserFactory> SAX_PARSER_FACTORY =
        ThreadLocal.withInitial(XmlSecurity::createSecureSAXParserFactory);

    // XMLInputFactory and TransformerFactory are NOT thread-safe, must use ThreadLocal
    private static final ThreadLocal<XMLInputFactory> XML_INPUT_FACTORY =
        ThreadLocal.withInitial(XmlSecurity::createSecureXMLInputFactory);

    private static final ThreadLocal<TransformerFactory> TRANSFORMER_FACTORY =
        ThreadLocal.withInitial(XmlSecurity::createSecureTransformerFactory);

    // Namespace-aware factories (thread-local)
    private static final ThreadLocal<DocumentBuilderFactory> NS_DOCUMENT_BUILDER_FACTORY =
        ThreadLocal.withInitial(() -> {
            DocumentBuilderFactory factory = XmlSecurity.createSecureDocumentBuilderFactory();
            factory.setNamespaceAware(true);
            return factory;
        });

    private static final ThreadLocal<SAXParserFactory> NS_SAX_PARSER_FACTORY =
        ThreadLocal.withInitial(() -> {
            SAXParserFactory factory = XmlSecurity.createSecureSAXParserFactory();
            factory.setNamespaceAware(true);
            return factory;
        });

    private SecureParserFactory() {
        // Utility class
    }

    /**
     * Creates a secure DocumentBuilder.
     * 创建安全的 DocumentBuilder。
     *
     * @return a secure DocumentBuilder | 安全的 DocumentBuilder
     */
    public static DocumentBuilder createDocumentBuilder() {
        try {
            return DOCUMENT_BUILDER_FACTORY.get().newDocumentBuilder();
        } catch (Exception e) {
            throw new OpenXmlException("Failed to create DocumentBuilder", e);
        }
    }

    /**
     * Creates a namespace-aware secure DocumentBuilder.
     * 创建支持命名空间的安全 DocumentBuilder。
     *
     * @return a namespace-aware secure DocumentBuilder | 支持命名空间的安全 DocumentBuilder
     */
    public static DocumentBuilder createNamespaceAwareDocumentBuilder() {
        try {
            return NS_DOCUMENT_BUILDER_FACTORY.get().newDocumentBuilder();
        } catch (Exception e) {
            throw new OpenXmlException("Failed to create namespace-aware DocumentBuilder", e);
        }
    }

    /**
     * Creates a secure SAXParser.
     * 创建安全的 SAXParser。
     *
     * @return a secure SAXParser | 安全的 SAXParser
     */
    public static SAXParser createSAXParser() {
        try {
            return SAX_PARSER_FACTORY.get().newSAXParser();
        } catch (Exception e) {
            throw new OpenXmlException("Failed to create SAXParser", e);
        }
    }

    /**
     * Creates a namespace-aware secure SAXParser.
     * 创建支持命名空间的安全 SAXParser。
     *
     * @return a namespace-aware secure SAXParser | 支持命名空间的安全 SAXParser
     */
    public static SAXParser createNamespaceAwareSAXParser() {
        try {
            return NS_SAX_PARSER_FACTORY.get().newSAXParser();
        } catch (Exception e) {
            throw new OpenXmlException("Failed to create namespace-aware SAXParser", e);
        }
    }

    /**
     * Creates a secure XMLStreamReader from an InputStream.
     * 从 InputStream 创建安全的 XMLStreamReader。
     *
     * @param input the input stream | 输入流
     * @return a secure XMLStreamReader | 安全的 XMLStreamReader
     */
    public static XMLStreamReader createXMLStreamReader(InputStream input) {
        try {
            return XML_INPUT_FACTORY.get().createXMLStreamReader(input);
        } catch (Exception e) {
            throw new OpenXmlException("Failed to create XMLStreamReader", e);
        }
    }

    /**
     * Creates a secure XMLStreamReader from a Reader.
     * 从 Reader 创建安全的 XMLStreamReader。
     *
     * @param reader the reader | 读取器
     * @return a secure XMLStreamReader | 安全的 XMLStreamReader
     */
    public static XMLStreamReader createXMLStreamReader(Reader reader) {
        try {
            return XML_INPUT_FACTORY.get().createXMLStreamReader(reader);
        } catch (Exception e) {
            throw new OpenXmlException("Failed to create XMLStreamReader", e);
        }
    }

    /**
     * Creates a secure Transformer.
     * 创建安全的 Transformer。
     *
     * @return a secure Transformer | 安全的 Transformer
     */
    public static Transformer createTransformer() {
        try {
            return TRANSFORMER_FACTORY.get().newTransformer();
        } catch (Exception e) {
            throw new OpenXmlException("Failed to create Transformer", e);
        }
    }

    /**
     * Returns the shared secure DocumentBuilderFactory.
     * 返回共享的安全 DocumentBuilderFactory。
     *
     * @return the factory | 工厂
     */
    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        return DOCUMENT_BUILDER_FACTORY.get();
    }

    /**
     * Returns the shared namespace-aware secure DocumentBuilderFactory.
     * 返回共享的支持命名空间的安全 DocumentBuilderFactory。
     *
     * @return the factory | 工厂
     */
    public static DocumentBuilderFactory getNamespaceAwareDocumentBuilderFactory() {
        return NS_DOCUMENT_BUILDER_FACTORY.get();
    }

    /**
     * Returns the shared secure SAXParserFactory.
     * 返回共享的安全 SAXParserFactory。
     *
     * @return the factory | 工厂
     */
    public static SAXParserFactory getSAXParserFactory() {
        return SAX_PARSER_FACTORY.get();
    }

    /**
     * Returns the shared namespace-aware secure SAXParserFactory.
     * 返回共享的支持命名空间的安全 SAXParserFactory。
     *
     * @return the factory | 工厂
     */
    public static SAXParserFactory getNamespaceAwareSAXParserFactory() {
        return NS_SAX_PARSER_FACTORY.get();
    }

    /**
     * Returns the shared secure XMLInputFactory.
     * 返回共享的安全 XMLInputFactory。
     *
     * @return the factory | 工厂
     */
    public static XMLInputFactory getXMLInputFactory() {
        return XML_INPUT_FACTORY.get();
    }

    /**
     * Creates and returns a new secure XMLInputFactory.
     * 创建并返回新的安全 XMLInputFactory。
     *
     * <p>Use this when you need a separate factory instance.</p>
     * <p>当需要单独的工厂实例时使用此方法。</p>
     *
     * @return a new secure XMLInputFactory | 新的安全 XMLInputFactory
     */
    public static XMLInputFactory createXMLInputFactory() {
        return XmlSecurity.createSecureXMLInputFactory();
    }

    /**
     * Returns the shared secure TransformerFactory.
     * 返回共享的安全 TransformerFactory。
     *
     * @return the factory | 工厂
     */
    public static TransformerFactory getTransformerFactory() {
        return TRANSFORMER_FACTORY.get();
    }

    /**
     * Removes all ThreadLocal instances to prevent memory leaks.
     * 移除所有 ThreadLocal 实例以防止内存泄漏。
     *
     * <p>Call this method when the thread is done using XML parsers,
     * especially in thread pool or web container environments.</p>
     * <p>当线程完成 XML 解析器使用后调用此方法，特别是在线程池或 Web 容器环境中。</p>
     */
    public static void cleanup() {
        DOCUMENT_BUILDER_FACTORY.remove();
        SAX_PARSER_FACTORY.remove();
        XML_INPUT_FACTORY.remove();
        TRANSFORMER_FACTORY.remove();
        NS_DOCUMENT_BUILDER_FACTORY.remove();
        NS_SAX_PARSER_FACTORY.remove();
    }
}
