package cloud.opencode.base.json.reactive;

import cloud.opencode.base.json.OpenJson;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default Reactive JSON Writer - Default implementation of ReactiveJsonWriter
 * 默认响应式JSON写入器 - ReactiveJsonWriter的默认实现
 *
 * <p>Provides reactive streaming JSON writing using Virtual Threads
 * for non-blocking I/O operations.</p>
 * <p>使用虚拟线程提供非阻塞I/O操作的响应式流式JSON写入。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Reactive Streams compatible JSON writing - 兼容响应式流的JSON写入</li>
 *   <li>NDJSON and JSON array output formats - NDJSON和JSON数组输出格式</li>
 *   <li>Optional pretty printing support - 可选的美化打印支持</li>
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
final class DefaultReactiveJsonWriter implements ReactiveJsonWriter {

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final byte[] ARRAY_START = "[".getBytes(StandardCharsets.UTF_8);
    private static final byte[] ARRAY_END = "]".getBytes(StandardCharsets.UTF_8);
    private static final byte[] COMMA = ",".getBytes(StandardCharsets.UTF_8);
    private static final byte[] NEWLINE = "\n".getBytes(StandardCharsets.UTF_8);

    private final OutputStream output;
    private final boolean prettyPrint;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicLong elementsWritten = new AtomicLong(0);
    private final AtomicLong bytesWritten = new AtomicLong(0);

    /**
     * Creates a writer with default settings.
     * 使用默认设置创建写入器。
     *
     * @param output the output stream | 输出流
     */
    DefaultReactiveJsonWriter(OutputStream output) {
        this(output, DEFAULT_BUFFER_SIZE, false);
    }

    /**
     * Creates a writer with custom buffer size.
     * 使用自定义缓冲区大小创建写入器。
     *
     * @param output     the output stream | 输出流
     * @param bufferSize the buffer size | 缓冲区大小
     */
    DefaultReactiveJsonWriter(OutputStream output, int bufferSize) {
        this(output, bufferSize, false);
    }

    /**
     * Creates a writer with pretty printing option.
     * 使用美化输出选项创建写入器。
     *
     * @param output      the output stream | 输出流
     * @param prettyPrint whether to pretty print | 是否美化输出
     */
    DefaultReactiveJsonWriter(OutputStream output, boolean prettyPrint) {
        this(output, DEFAULT_BUFFER_SIZE, prettyPrint);
    }

    /**
     * Creates a writer with all options.
     * 使用所有选项创建写入器。
     *
     * @param output      the output stream | 输出流
     * @param bufferSize  the buffer size | 缓冲区大小
     * @param prettyPrint whether to pretty print | 是否美化输出
     */
    DefaultReactiveJsonWriter(OutputStream output, int bufferSize, boolean prettyPrint) {
        Objects.requireNonNull(output, "output cannot be null");
        this.output = output instanceof BufferedOutputStream bos ? bos : new BufferedOutputStream(output, bufferSize);
        this.prettyPrint = prettyPrint;
    }

    @Override
    public <T> CompletableFuture<Void> write(Flow.Publisher<T> source) {
        Objects.requireNonNull(source, "source cannot be null");
        checkOpen();

        CompletableFuture<Void> future = new CompletableFuture<>();

        source.subscribe(new Flow.Subscriber<>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T item) {
                try {
                    String json = prettyPrint ? OpenJson.toPrettyJson(item) : OpenJson.toJson(item);
                    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                    synchronized (output) {
                        output.write(bytes);
                        output.write(NEWLINE);
                        bytesWritten.addAndGet(bytes.length + NEWLINE.length);
                        elementsWritten.incrementAndGet();
                    }
                } catch (IOException e) {
                    subscription.cancel();
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                try {
                    flush();
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    @Override
    public <T> CompletableFuture<Void> writeAsArray(Flow.Publisher<T> source) {
        Objects.requireNonNull(source, "source cannot be null");
        checkOpen();

        CompletableFuture<Void> future = new CompletableFuture<>();

        source.subscribe(new Flow.Subscriber<>() {
            private Flow.Subscription subscription;
            private boolean first = true;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                try {
                    synchronized (output) {
                        output.write(ARRAY_START);
                        bytesWritten.addAndGet(ARRAY_START.length);
                        if (prettyPrint) {
                            output.write(NEWLINE);
                            bytesWritten.addAndGet(NEWLINE.length);
                        }
                    }
                    subscription.request(Long.MAX_VALUE);
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onNext(T item) {
                try {
                    String json = prettyPrint ? OpenJson.toPrettyJson(item) : OpenJson.toJson(item);
                    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                    synchronized (output) {
                        if (!first) {
                            output.write(COMMA);
                            bytesWritten.addAndGet(COMMA.length);
                        }
                        first = false;
                        if (prettyPrint) {
                            output.write(NEWLINE);
                            bytesWritten.addAndGet(NEWLINE.length);
                        }
                        output.write(bytes);
                        bytesWritten.addAndGet(bytes.length);
                        elementsWritten.incrementAndGet();
                    }
                } catch (IOException e) {
                    subscription.cancel();
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                try {
                    synchronized (output) {
                        if (prettyPrint) {
                            output.write(NEWLINE);
                            bytesWritten.addAndGet(NEWLINE.length);
                        }
                        output.write(ARRAY_END);
                        bytesWritten.addAndGet(ARRAY_END.length);
                    }
                    flush();
                    future.complete(null);
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    @Override
    public <T> CompletableFuture<Void> writeObject(T object) {
        Objects.requireNonNull(object, "object cannot be null");
        checkOpen();

        return CompletableFuture.runAsync(() -> {
            try {
                String json = prettyPrint ? OpenJson.toPrettyJson(object) : OpenJson.toJson(object);
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                synchronized (output) {
                    output.write(bytes);
                    bytesWritten.addAndGet(bytes.length);
                }
                elementsWritten.incrementAndGet();
                flush();
            } catch (IOException e) {
                throw OpenJsonProcessingException.ioError("Failed to write object", e);
            }
        });
    }

    @Override
    public void flush() {
        try {
            output.flush();
        } catch (IOException e) {
            throw OpenJsonProcessingException.ioError("Failed to flush output", e);
        }
    }

    @Override
    public boolean isOpen() {
        return !closed.get();
    }

    @Override
    public long getElementsWritten() {
        return elementsWritten.get();
    }

    @Override
    public long getBytesWritten() {
        return bytesWritten.get();
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            flush();
            output.close();
        }
    }

    private void checkOpen() {
        if (closed.get()) {
            throw new IllegalStateException("Writer is closed");
        }
    }
}
