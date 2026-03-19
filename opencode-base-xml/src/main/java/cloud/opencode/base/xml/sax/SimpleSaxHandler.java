package cloud.opencode.base.xml.sax;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Simple SAX Handler - Lambda-friendly SAX handler
 * 简化 SAX 处理器 - Lambda 友好的 SAX 处理器
 *
 * <p>This class provides a fluent API for registering callbacks for SAX events.</p>
 * <p>此类提供流式 API，用于注册 SAX 事件的回调。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SimpleSaxHandler handler = SimpleSaxHandler.create()
 *     .onStart("user", (name, attrs) -> {
 *         System.out.println("User: " + attrs.get("id"));
 *     })
 *     .onText("name", text -> {
 *         System.out.println("Name: " + text);
 *     })
 *     .onEnd("user", name -> {
 *         System.out.println("End user");
 *     });
 *
 * SaxParser.createSecure()
 *     .handler(handler)
 *     .parse(xml);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lambda-friendly SAX handler with fluent API - Lambda 友好的 SAX 处理器，带流式 API</li>
 *   <li>Register callbacks by element name - 按元素名称注册回调</li>
 *   <li>Separate callbacks for start, end, and text events - 开始、结束和文本事件的分别回调</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable handler registration) - 线程安全: 否（可变处理器注册）</li>
 *   <li>Null-safe: No (throws on null callback) - 空值安全: 否（null 回调抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class SimpleSaxHandler implements SaxHandler {

    private final Map<String, BiConsumer<String, Map<String, String>>> startCallbacks = new HashMap<>();
    private final Map<String, Consumer<String>> endCallbacks = new HashMap<>();
    private final Map<String, Consumer<String>> textCallbacks = new HashMap<>();

    private BiConsumer<String, Map<String, String>> anyStartCallback;
    private Consumer<String> anyEndCallback;

    private String currentElement;
    private StringBuilder textBuffer;

    private SimpleSaxHandler() {
    }

    /**
     * Creates a new simple SAX handler.
     * 创建新的简化 SAX 处理器。
     *
     * @return a new handler | 新处理器
     */
    public static SimpleSaxHandler create() {
        return new SimpleSaxHandler();
    }

    /**
     * Registers a callback for element start.
     * 注册元素开始的回调。
     *
     * @param elementName the element name | 元素名称
     * @param callback    the callback (name, attributes) | 回调（名称，属性）
     * @return this handler for chaining | 此处理器以便链式调用
     */
    public SimpleSaxHandler onStart(String elementName,
                                    BiConsumer<String, Map<String, String>> callback) {
        startCallbacks.put(elementName, callback);
        return this;
    }

    /**
     * Registers a callback for element end.
     * 注册元素结束的回调。
     *
     * @param elementName the element name | 元素名称
     * @param callback    the callback (element name) | 回调（元素名称）
     * @return this handler for chaining | 此处理器以便链式调用
     */
    public SimpleSaxHandler onEnd(String elementName, Consumer<String> callback) {
        endCallbacks.put(elementName, callback);
        return this;
    }

    /**
     * Registers a callback for element text content.
     * 注册元素文本内容的回调。
     *
     * @param elementName the element name | 元素名称
     * @param callback    the callback (text content) | 回调（文本内容）
     * @return this handler for chaining | 此处理器以便链式调用
     */
    public SimpleSaxHandler onText(String elementName, Consumer<String> callback) {
        textCallbacks.put(elementName, callback);
        return this;
    }

    /**
     * Registers a callback for any element start.
     * 注册任意元素开始的回调。
     *
     * @param callback the callback | 回调
     * @return this handler for chaining | 此处理器以便链式调用
     */
    public SimpleSaxHandler onAnyStart(BiConsumer<String, Map<String, String>> callback) {
        this.anyStartCallback = callback;
        return this;
    }

    /**
     * Registers a callback for any element end.
     * 注册任意元素结束的回调。
     *
     * @param callback the callback | 回调
     * @return this handler for chaining | 此处理器以便链式调用
     */
    public SimpleSaxHandler onAnyEnd(Consumer<String> callback) {
        this.anyEndCallback = callback;
        return this;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Map<String, String> attributes) {
        String name = localName.isEmpty() ? qName : localName;
        currentElement = name;
        textBuffer = new StringBuilder();

        BiConsumer<String, Map<String, String>> callback = startCallbacks.get(name);
        if (callback != null) {
            callback.accept(name, attributes);
        }

        if (anyStartCallback != null) {
            anyStartCallback.accept(name, attributes);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        String name = localName.isEmpty() ? qName : localName;

        // Handle text callback
        Consumer<String> textCallback = textCallbacks.get(name);
        if (textCallback != null && textBuffer != null) {
            String text = textBuffer.toString().trim();
            if (!text.isEmpty()) {
                textCallback.accept(text);
            }
        }

        // Handle end callback
        Consumer<String> endCallback = endCallbacks.get(name);
        if (endCallback != null) {
            endCallback.accept(name);
        }

        if (anyEndCallback != null) {
            anyEndCallback.accept(name);
        }

        currentElement = null;
        textBuffer = null;
    }

    @Override
    public void characters(String content) {
        if (textBuffer != null) {
            textBuffer.append(content);
        }
    }
}
