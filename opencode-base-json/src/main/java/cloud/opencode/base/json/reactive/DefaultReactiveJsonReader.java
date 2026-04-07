package cloud.opencode.base.json.reactive;

import cloud.opencode.base.json.OpenJson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default Reactive JSON Reader - Default implementation of ReactiveJsonReader
 * 默认响应式JSON读取器 - ReactiveJsonReader的默认实现
 *
 * <p>Provides reactive streaming JSON reading using Virtual Threads
 * for non-blocking I/O operations.</p>
 * <p>使用虚拟线程提供非阻塞I/O操作的响应式流式JSON读取。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Reactive Streams compatible JSON reading - 兼容响应式流的JSON读取</li>
 *   <li>Virtual Thread-based non-blocking I/O - 基于虚拟线程的非阻塞I/O</li>
 *   <li>Bounded input size limit (16 MB) for security - 有界输入大小限制（16 MB）确保安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // See class-level documentation for usage
 * // 参见类级文档了解用法
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
final class DefaultReactiveJsonReader implements ReactiveJsonReader {

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int DEFAULT_BATCH_SIZE = 64;
    private static final long DEFAULT_MAX_INPUT_SIZE = 16L * 1024 * 1024; // 16 MB

    private final InputStream input;
    private final int bufferSize;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicLong elementsRead = new AtomicLong(0);

    /**
     * Creates a reader with default buffer size.
     * 使用默认缓冲区大小创建读取器。
     *
     * @param input the input stream | 输入流
     */
    DefaultReactiveJsonReader(InputStream input) {
        this(input, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a reader with custom buffer size.
     * 使用自定义缓冲区大小创建读取器。
     *
     * @param input      the input stream | 输入流
     * @param bufferSize the buffer size | 缓冲区大小
     */
    DefaultReactiveJsonReader(InputStream input, int bufferSize) {
        Objects.requireNonNull(input, "input cannot be null");
        this.input = input instanceof BufferedInputStream bis ? bis : new BufferedInputStream(input, bufferSize);
        this.bufferSize = bufferSize;
    }

    @Override
    public <T> Flow.Publisher<T> readValues(Class<T> clazz) {
        return readValues(clazz, DEFAULT_BATCH_SIZE);
    }

    @Override
    public <T> Flow.Publisher<T> readValues(Class<T> clazz, int batchSize) {
        Objects.requireNonNull(clazz, "clazz cannot be null");
        checkOpen();

        SubmissionPublisher<T> publisher = new SubmissionPublisher<>();

        Thread.startVirtualThread(() -> {
            try {
                // Read entire content with size limit and parse as array
                byte[] content = readBounded(input, DEFAULT_MAX_INPUT_SIZE);
                String json = new String(content, StandardCharsets.UTF_8);

                // Parse as array of objects
                java.util.List<T> items = OpenJson.fromJsonArray(json, clazz);
                for (T item : items) {
                    if (closed.get()) {
                        break;
                    }
                    publisher.submit(item);
                    elementsRead.incrementAndGet();
                }
                publisher.close();
            } catch (Exception e) {
                publisher.closeExceptionally(e);
            }
        });

        return publisher;
    }

    @Override
    public <T> Flow.Publisher<T> readArrayElements(Class<T> elementType) {
        return readArrayElements(elementType, DEFAULT_BATCH_SIZE);
    }

    @Override
    public <T> Flow.Publisher<T> readArrayElements(Class<T> elementType, int batchSize) {
        Objects.requireNonNull(elementType, "elementType cannot be null");
        checkOpen();

        SubmissionPublisher<T> publisher = new SubmissionPublisher<>();

        Thread.startVirtualThread(() -> {
            try {
                // Read entire content with size limit
                byte[] content = readBounded(input, DEFAULT_MAX_INPUT_SIZE);
                String json = new String(content, StandardCharsets.UTF_8);

                // Parse as array
                java.util.List<T> items = OpenJson.fromJsonArray(json, elementType);
                for (T item : items) {
                    if (closed.get()) {
                        break;
                    }
                    publisher.submit(item);
                    elementsRead.incrementAndGet();
                }
                publisher.close();
            } catch (Exception e) {
                publisher.closeExceptionally(e);
            }
        });

        return publisher;
    }

    @Override
    public boolean isOpen() {
        return !closed.get();
    }

    @Override
    public long getElementsRead() {
        return elementsRead.get();
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            input.close();
        }
    }

    private static byte[] readBounded(InputStream in, long maxSize) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        long totalRead = 0;
        int n;
        while ((n = in.read(buf)) != -1) {
            totalRead += n;
            if (totalRead > maxSize) {
                throw new IOException("Input exceeds maximum allowed size of " + maxSize + " bytes");
            }
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }

    private void checkOpen() {
        if (closed.get()) {
            throw new IllegalStateException("Reader is closed");
        }
    }
}
