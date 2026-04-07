package cloud.opencode.base.image.security;

import cloud.opencode.base.image.Image;
import cloud.opencode.base.image.ImageFormat;
import cloud.opencode.base.image.ImageInfo;
import cloud.opencode.base.image.OpenImage;
import cloud.opencode.base.image.exception.*;
import cloud.opencode.base.image.validation.ImageValidator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Safe Image Service
 * 安全图片服务
 *
 * <p>Thread-safe image processing service with security controls.</p>
 * <p>带安全控制的线程安全图片处理服务。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Size validation | 大小验证</li>
 *   <li>Timeout control | 超时控制</li>
 *   <li>Concurrency limits | 并发限制</li>
 *   <li>Memory estimation | 内存估算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SafeImageService service = SafeImageService.builder()
 *     .maxFileSize(5_000_000)
 *     .maxWidth(4000)
 *     .maxHeight(4000)
 *     .timeout(Duration.ofSeconds(30))
 *     .maxConcurrent(10)
 *     .build();
 *
 * Image image = service.read(path);
 * service.process(image, img -> img.resize(800, 600));
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public class SafeImageService implements AutoCloseable {

    private final long maxFileSize;
    private final int maxWidth;
    private final int maxHeight;
    private final Duration timeout;
    private final int maxConcurrent;
    private final Semaphore semaphore;
    private final ExecutorService executor;
    private final AtomicInteger activeCount;
    private volatile boolean closed;

    /**
     * Create safe image service
     * 创建安全图片服务
     *
     * @param maxFileSize maximum file size in bytes | 最大文件大小（字节）
     * @param maxWidth maximum width | 最大宽度
     * @param maxHeight maximum height | 最大高度
     * @param timeout operation timeout | 操作超时时间
     * @param maxConcurrent maximum concurrent operations | 最大并发操作数
     */
    public SafeImageService(long maxFileSize, int maxWidth, int maxHeight,
                            Duration timeout, int maxConcurrent) {
        this.maxFileSize = maxFileSize;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.timeout = timeout;
        this.maxConcurrent = maxConcurrent;
        this.semaphore = new Semaphore(maxConcurrent);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.activeCount = new AtomicInteger(0);
    }

    /**
     * Create builder
     * 创建构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create with default settings
     * 使用默认设置创建
     *
     * @return the service | 服务
     */
    public static SafeImageService createDefault() {
        return builder().build();
    }

    /**
     * Read image safely from path
     * 从路径安全读取图片
     *
     * @param path the file path | 文件路径
     * @return the image | 图片
     * @throws ImageException if reading or validation fails | 如果读取或验证失败
     */
    public Image read(Path path) throws ImageException {
        // Validate path
        SafePathUtil.validatePath(path);

        // Validate image
        ImageValidator.validate(path, maxFileSize, maxWidth, maxHeight);

        return executeWithTimeout(() -> OpenImage.read(path));
    }

    /**
     * Read image safely from bytes
     * 从字节数组安全读取图片
     *
     * @param bytes the image bytes | 图片字节数组
     * @return the image | 图片
     * @throws ImageException if reading or validation fails | 如果读取或验证失败
     */
    public Image read(byte[] bytes) throws ImageException {
        // Validate
        ImageValidator.validate(bytes, maxFileSize, maxWidth, maxHeight);

        return executeWithTimeout(() -> OpenImage.read(bytes));
    }

    /**
     * Process image with operation
     * 使用操作处理图片
     *
     * @param image the image | 图片
     * @param operation the operation | 操作
     * @return the processed image | 处理后的图片
     * @throws ImageException if processing fails | 如果处理失败
     */
    public Image process(Image image, ImageOperation operation) throws ImageException {
        return executeWithTimeout(() -> {
            BufferedImage result = operation.apply(image.getBufferedImage());
            return new Image(result, image.getFormat());
        });
    }

    /**
     * Save image safely
     * 安全保存图片
     *
     * @param image the image | 图片
     * @param path the output path | 输出路径
     * @param baseDir the base directory for validation | 用于验证的基础目录
     * @throws ImageException if saving fails | 如果保存失败
     */
    public void save(Image image, Path path, Path baseDir) throws ImageException {
        SafePathUtil.validatePath(path, baseDir);
        SafePathUtil.ensureParentExists(path);

        executeWithTimeout(() -> {
            image.save(path);
            return null;
        });
    }

    /**
     * Get image info safely
     * 安全获取图片信息
     *
     * @param path the file path | 文件路径
     * @return the image info | 图片信息
     * @throws ImageException if reading fails | 如果读取失败
     */
    public ImageInfo getInfo(Path path) throws ImageException {
        SafePathUtil.validatePath(path);
        return executeWithTimeout(() -> OpenImage.getInfo(path));
    }

    /**
     * Get active operation count
     * 获取活动操作数
     *
     * @return the count | 数量
     */
    public int getActiveCount() {
        return activeCount.get();
    }

    /**
     * Get available permits
     * 获取可用许可数
     *
     * @return the available permits | 可用许可数
     */
    public int getAvailablePermits() {
        return semaphore.availablePermits();
    }

    /**
     * Execute operation with timeout and concurrency control
     * 使用超时和并发控制执行操作
     */
    private <T> T executeWithTimeout(Callable<T> operation) throws ImageException {
        if (closed) {
            throw new ImageOperationException("SafeImageService has been closed");
        }
        if (!semaphore.tryAcquire()) {
            throw ImageResourceException.tooManyRequests();
        }

        activeCount.incrementAndGet();
        try {
            Future<T> future = executor.submit(operation);
            try {
                return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new ImageTimeoutException("Operation timed out", timeout);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ImageException ie) {
                    throw ie;
                }
                throw new ImageOperationException("Operation failed", cause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ImageOperationException("Operation interrupted", e);
            }
        } finally {
            activeCount.decrementAndGet();
            semaphore.release();
        }
    }

    @Override
    public void close() {
        closed = true;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Safe Image Service Builder
     * 安全图片服务构建器
     */
    public static class Builder {
        private long maxFileSize = ImageValidator.DEFAULT_MAX_FILE_SIZE;
        private int maxWidth = ImageValidator.DEFAULT_MAX_WIDTH;
        private int maxHeight = ImageValidator.DEFAULT_MAX_HEIGHT;
        private Duration timeout = Duration.ofSeconds(30);
        private int maxConcurrent = Runtime.getRuntime().availableProcessors() * 2;

        /**
         * Set maximum file size
         * 设置最大文件大小
         *
         * @param maxFileSize the max size in bytes | 最大大小（字节）
         * @return this builder | 构建器
         */
        public Builder maxFileSize(long maxFileSize) {
            this.maxFileSize = maxFileSize;
            return this;
        }

        /**
         * Set maximum width
         * 设置最大宽度
         *
         * @param maxWidth the max width | 最大宽度
         * @return this builder | 构建器
         */
        public Builder maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        /**
         * Set maximum height
         * 设置最大高度
         *
         * @param maxHeight the max height | 最大高度
         * @return this builder | 构建器
         */
        public Builder maxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        /**
         * Set maximum dimensions
         * 设置最大尺寸
         *
         * @param maxWidth the max width | 最大宽度
         * @param maxHeight the max height | 最大高度
         * @return this builder | 构建器
         */
        public Builder maxDimensions(int maxWidth, int maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            return this;
        }

        /**
         * Set operation timeout
         * 设置操作超时时间
         *
         * @param timeout the timeout | 超时时间
         * @return this builder | 构建器
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Set maximum concurrent operations
         * 设置最大并发操作数
         *
         * @param maxConcurrent the max concurrent | 最大并发数
         * @return this builder | 构建器
         */
        public Builder maxConcurrent(int maxConcurrent) {
            this.maxConcurrent = maxConcurrent;
            return this;
        }

        /**
         * Build the service
         * 构建服务
         *
         * @return the service | 服务
         */
        public SafeImageService build() {
            return new SafeImageService(maxFileSize, maxWidth, maxHeight, timeout, maxConcurrent);
        }
    }
}
