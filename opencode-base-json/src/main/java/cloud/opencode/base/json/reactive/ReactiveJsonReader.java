package cloud.opencode.base.json.reactive;

import java.io.Closeable;
import java.io.InputStream;
import java.util.concurrent.Flow;

/**
 * Reactive JSON Reader - Reactive streaming JSON parser interface
 * 响应式JSON读取器 - 响应式流式JSON解析器接口
 *
 * <p>This interface provides a reactive (push-based) API for reading JSON using
 * Java's Flow API. It enables non-blocking processing of large JSON documents
 * with backpressure support.</p>
 * <p>此接口使用Java的Flow API提供响应式（推送式）JSON读取API。
 * 它支持对大型JSON文档进行非阻塞处理，并支持背压。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Reactive Streams compatible via java.util.concurrent.Flow</li>
 *   <li>Backpressure support for memory-efficient processing</li>
 *   <li>Integration with Virtual Threads for concurrent processing</li>
 *   <li>Streaming array element processing</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * try (ReactiveJsonReader reader = ReactiveJsonReader.create(
 *         new FileInputStream("large-data.json"))) {
 *
 *     reader.readArrayElements(User.class)
 *         .subscribe(new Flow.Subscriber<>() {
 *             private Flow.Subscription subscription;
 *
 *             @Override
 *             public void onSubscribe(Flow.Subscription s) {
 *                 this.subscription = s;
 *                 s.request(10); // Request first batch
 *             }
 *
 *             @Override
 *             public void onNext(User user) {
 *                 // Process with Virtual Thread
 *                 Thread.startVirtualThread(() -> processUser(user));
 *                 subscription.request(1); // Request next
 *             }
 *
 *             @Override
 *             public void onError(Throwable t) {
 *                 t.printStackTrace();
 *             }
 *
 *             @Override
 *             public void onComplete() {
 *                 System.out.println("Processing complete");
 *             }
 *         });
 * }
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see java.util.concurrent.Flow
 * @since JDK 25, opencode-base-json V1.0.0
 */
public interface ReactiveJsonReader extends Closeable {

    /**
     * Creates a reactive JSON reader from an input stream.
     * 从输入流创建响应式JSON读取器。
     *
     * @param input the input stream | 输入流
     * @return the reader | 读取器
     */
    static ReactiveJsonReader create(InputStream input) {
        return new DefaultReactiveJsonReader(input);
    }

    /**
     * Creates a reactive JSON reader with custom buffer size.
     * 使用自定义缓冲区大小创建响应式JSON读取器。
     *
     * @param input      the input stream | 输入流
     * @param bufferSize the buffer size | 缓冲区大小
     * @return the reader | 读取器
     */
    static ReactiveJsonReader create(InputStream input, int bufferSize) {
        return new DefaultReactiveJsonReader(input, bufferSize);
    }

    /**
     * Reads objects from the JSON stream as a reactive publisher.
     * 从JSON流中读取对象作为响应式发布者。
     *
     * @param clazz the class type | 类类型
     * @param <T>   the element type | 元素类型
     * @return a publisher of objects | 对象发布者
     */
    <T> Flow.Publisher<T> readValues(Class<T> clazz);

    /**
     * Reads objects with specified batch size for backpressure control.
     * 以指定的批量大小读取对象以控制背压。
     *
     * @param clazz     the class type | 类类型
     * @param batchSize the batch size for prefetching | 预取批量大小
     * @param <T>       the element type | 元素类型
     * @return a publisher of objects | 对象发布者
     */
    <T> Flow.Publisher<T> readValues(Class<T> clazz, int batchSize);

    /**
     * Reads JSON array elements as a reactive publisher.
     * 将JSON数组元素作为响应式发布者读取。
     *
     * <p>Assumes the JSON starts with an array. Each element is
     * deserialized and published individually.</p>
     * <p>假设JSON以数组开头。每个元素单独反序列化并发布。</p>
     *
     * @param elementType the element type | 元素类型
     * @param <T>         the element type | 元素类型
     * @return a publisher of array elements | 数组元素发布者
     */
    <T> Flow.Publisher<T> readArrayElements(Class<T> elementType);

    /**
     * Reads JSON array elements with batch size control.
     * 以批量大小控制读取JSON数组元素。
     *
     * @param elementType the element type | 元素类型
     * @param batchSize   the batch size | 批量大小
     * @param <T>         the element type | 元素类型
     * @return a publisher of array elements | 数组元素发布者
     */
    <T> Flow.Publisher<T> readArrayElements(Class<T> elementType, int batchSize);

    /**
     * Checks if the reader is open.
     * 检查读取器是否打开。
     *
     * @return true if open | 如果打开返回true
     */
    boolean isOpen();

    /**
     * Gets the number of elements read so far.
     * 获取到目前为止读取的元素数。
     *
     * @return the count | 计数
     */
    long getElementsRead();
}
