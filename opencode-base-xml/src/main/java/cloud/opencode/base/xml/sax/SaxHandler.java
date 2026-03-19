package cloud.opencode.base.xml.sax;

import java.util.Map;

/**
 * SAX Handler Interface - Handles SAX parsing events
 * SAX 处理器接口 - 处理 SAX 解析事件
 *
 * <p>This interface defines callbacks for SAX parsing events with a simplified API.</p>
 * <p>此接口使用简化 API 定义 SAX 解析事件的回调。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Simplified SAX event callback interface - 简化的 SAX 事件回调接口</li>
 *   <li>Default no-op implementations for all methods - 所有方法的默认无操作实现</li>
 *   <li>Element start/end and text content callbacks - 元素开始/结束和文本内容回调</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implement custom handler
 * SaxHandler handler = new SaxHandler() {
 *     @Override
 *     public void startElement(String name, Map<String, String> attrs) {
 *         System.out.println("Start: " + name);
 *     }
 *     @Override
 *     public void text(String text) {
 *         System.out.println("Text: " + text);
 *     }
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes (default no-op methods) - 空值安全: 是（默认无操作方法）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public interface SaxHandler {

    /**
     * Called when document parsing starts.
     * 当文档解析开始时调用。
     */
    default void startDocument() {}

    /**
     * Called when document parsing ends.
     * 当文档解析结束时调用。
     */
    default void endDocument() {}

    /**
     * Called when an element starts.
     * 当元素开始时调用。
     *
     * @param uri        the namespace URI | 命名空间 URI
     * @param localName  the local name | 本地名称
     * @param qName      the qualified name | 限定名称
     * @param attributes the element attributes | 元素属性
     */
    default void startElement(String uri, String localName, String qName,
                              Map<String, String> attributes) {}

    /**
     * Called when an element ends.
     * 当元素结束时调用。
     *
     * @param uri       the namespace URI | 命名空间 URI
     * @param localName the local name | 本地名称
     * @param qName     the qualified name | 限定名称
     */
    default void endElement(String uri, String localName, String qName) {}

    /**
     * Called for character content.
     * 为字符内容调用。
     *
     * @param content the character content | 字符内容
     */
    default void characters(String content) {}

    /**
     * Called for processing instructions.
     * 为处理指令调用。
     *
     * @param target the target | 目标
     * @param data   the data | 数据
     */
    default void processingInstruction(String target, String data) {}

    /**
     * Called when a parsing error occurs.
     * 当发生解析错误时调用。
     *
     * @param e the parse exception | 解析异常
     */
    default void error(SaxParseException e) {
        throw e;
    }

    /**
     * Called for parsing warnings.
     * 为解析警告调用。
     *
     * @param e the parse exception | 解析异常
     */
    default void warning(SaxParseException e) {}
}
