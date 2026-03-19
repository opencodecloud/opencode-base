package cloud.opencode.base.json.reactive;

import java.io.Closeable;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * Reactive JSON Writer - Reactive streaming JSON writer interface
 * 响应式JSON写入器 - 响应式流式JSON写入器接口
 *
 * <p>This interface provides a reactive API for writing JSON using
 * Java's Flow API. It enables non-blocking serialization of object
 * streams to JSON output.</p>
 * <p>此接口使用Java的Flow API提供响应式JSON写入API。
 * 它支持将对象流非阻塞地序列化为JSON输出。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Reactive Streams compatible via java.util.concurrent.Flow</li>
 *   <li>Writes object streams as JSON arrays or objects</li>
 *   <li>Non-blocking I/O support</li>
 *   <li>Configurable formatting options</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * // Writing a stream of users as a JSON array
 * Flow.Publisher<User> userStream = getUserStream();
 *
 * try (ReactiveJsonWriter writer = ReactiveJsonWriter.create(
 *         new FileOutputStream("users.json"))) {
 *
 *     CompletableFuture<Void> future = writer.writeAsArray(userStream);
 *     future.join(); // Wait for completion
 * }
 *
 * // Non-blocking write
 * writer.writeAsArray(userStream)
 *     .thenRun(() -> System.out.println("Write complete"))
 *     .exceptionally(ex -> {
 *         ex.printStackTrace();
 *         return null;
 *     });
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see java.util.concurrent.Flow
 * @since JDK 25, opencode-base-json V1.0.0
 */
public interface ReactiveJsonWriter extends Closeable {

    /**
     * Creates a reactive JSON writer to an output stream.
     * 创建写入输出流的响应式JSON写入器。
     *
     * @param output the output stream | 输出流
     * @return the writer | 写入器
     */
    static ReactiveJsonWriter create(OutputStream output) {
        return new DefaultReactiveJsonWriter(output);
    }

    /**
     * Creates a reactive JSON writer with custom buffer size.
     * 使用自定义缓冲区大小创建响应式JSON写入器。
     *
     * @param output     the output stream | 输出流
     * @param bufferSize the buffer size | 缓冲区大小
     * @return the writer | 写入器
     */
    static ReactiveJsonWriter create(OutputStream output, int bufferSize) {
        return new DefaultReactiveJsonWriter(output, bufferSize);
    }

    /**
     * Creates a reactive JSON writer with pretty printing.
     * 创建带美化输出的响应式JSON写入器。
     *
     * @param output the output stream | 输出流
     * @return the writer | 写入器
     */
    static ReactiveJsonWriter createPretty(OutputStream output) {
        return new DefaultReactiveJsonWriter(output, true);
    }

    /**
     * Writes objects from a publisher as JSON.
     * 将发布者的对象写入为JSON。
     *
     * <p>Each object is written as a separate JSON value (NDJSON format).</p>
     * <p>每个对象作为单独的JSON值写入（NDJSON格式）。</p>
     *
     * @param source the source publisher | 源发布者
     * @param <T>    the element type | 元素类型
     * @return a future that completes when writing is done | 写入完成时完成的future
     */
    <T> CompletableFuture<Void> write(Flow.Publisher<T> source);

    /**
     * Writes objects from a publisher as a JSON array.
     * 将发布者的对象写入为JSON数组。
     *
     * <p>Objects are enclosed in array brackets and separated by commas.</p>
     * <p>对象被括在数组括号中，并用逗号分隔。</p>
     *
     * @param source the source publisher | 源发布者
     * @param <T>    the element type | 元素类型
     * @return a future that completes when writing is done | 写入完成时完成的future
     */
    <T> CompletableFuture<Void> writeAsArray(Flow.Publisher<T> source);

    /**
     * Writes a single object as JSON.
     * 将单个对象写入为JSON。
     *
     * @param object the object to write | 要写入的对象
     * @param <T>    the object type | 对象类型
     * @return a future that completes when writing is done | 写入完成时完成的future
     */
    <T> CompletableFuture<Void> writeObject(T object);

    /**
     * Flushes any buffered content.
     * 刷新任何缓冲的内容。
     */
    void flush();

    /**
     * Checks if the writer is open.
     * 检查写入器是否打开。
     *
     * @return true if open | 如果打开返回true
     */
    boolean isOpen();

    /**
     * Gets the number of elements written so far.
     * 获取到目前为止写入的元素数。
     *
     * @return the count | 计数
     */
    long getElementsWritten();

    /**
     * Gets the number of bytes written so far.
     * 获取到目前为止写入的字节数。
     *
     * @return the byte count | 字节计数
     */
    long getBytesWritten();
}
