package cloud.opencode.base.image.responsive;

import cloud.opencode.base.image.Image;
import cloud.opencode.base.image.ImageFormat;
import cloud.opencode.base.image.OpenImage;
import cloud.opencode.base.image.exception.ImageIOException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Responsive Multi-Size Image Generator
 * 响应式多尺寸图片生成器
 *
 * <p>Generates multiple image variants at different sizes and formats from a single source image.
 * Useful for creating responsive image sets for web and mobile applications.</p>
 * <p>从单个源图片生成多种不同尺寸和格式的图片变体。
 * 适用于为 Web 和移动应用程序创建响应式图片集。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Generate multiple size variants from a single source - 从单个源生成多种尺寸变体</li>
 *   <li>Output in multiple formats (JPEG, PNG, etc.) - 输出多种格式（JPEG、PNG 等）</li>
 *   <li>Configurable compression quality - 可配置的压缩质量</li>
 *   <li>Batch processing with parallel execution - 支持并行执行的批量处理</li>
 *   <li>Error handling per source in batch mode - 批量模式下每个源的错误处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ResponsiveBuilder.of(source)
 *     .variant(64, 64, "icon")
 *     .variant(200, 200, "thumb")
 *     .formats(ImageFormat.JPEG, ImageFormat.PNG)
 *     .quality(0.85f)
 *     .generate(outputDir);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder pattern) - 线程安全: 否（构建器模式）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class ResponsiveBuilder {

    /**
     * Variant configuration describing a target image size and its name.
     * 变体配置，描述目标图片尺寸及其名称。
     *
     * @param width  the target width in pixels (must be positive) | 目标宽度（像素，必须为正数）
     * @param height the target height in pixels (must be positive) | 目标高度（像素，必须为正数）
     * @param name   the variant name used in output filenames | 用于输出文件名的变体名称
     */
    public record Variant(int width, int height, String name) {

        /**
         * Compact constructor with validation.
         * 带验证的紧凑构造器。
         */
        public Variant {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException(
                        "Width and height must be positive, got width=" + width + ", height=" + height);
            }
            Objects.requireNonNull(name, "Variant name must not be null");
        }
    }

    /**
     * Result of a single variant generation, containing the output path, variant info, format, and file size.
     * 单个变体生成的结果，包含输出路径、变体信息、格式和文件大小。
     *
     * @param path    the output file path | 输出文件路径
     * @param variant the variant that was generated | 生成的变体
     * @param format  the image format used | 使用的图片格式
     * @param fileSize the file size in bytes | 文件大小（字节）
     */
    public record GenerateResult(Path path, Variant variant, ImageFormat format, long fileSize) {

        /**
         * Compact constructor with validation.
         * 带验证的紧凑构造器。
         */
        public GenerateResult {
            Objects.requireNonNull(path, "Path must not be null");
            Objects.requireNonNull(variant, "Variant must not be null");
            Objects.requireNonNull(format, "Format must not be null");
        }
    }

    private final Image source;
    private final List<Variant> variants = new ArrayList<>();
    private ImageFormat[] formats;
    private float quality = 0.85f;

    private ResponsiveBuilder(Image source) {
        this.source = Objects.requireNonNull(source, "Source image must not be null");
    }

    /**
     * Create a ResponsiveBuilder from a file path.
     * 从文件路径创建 ResponsiveBuilder。
     *
     * @param source the source image path | 源图片路径
     * @return a new builder instance | 新的构建器实例
     * @throws ImageIOException if reading the image fails | 如果读取图片失败
     */
    public static ResponsiveBuilder of(Path source) throws ImageIOException {
        Objects.requireNonNull(source, "Source path must not be null");
        return new ResponsiveBuilder(Image.from(source));
    }

    /**
     * Create a ResponsiveBuilder from an Image instance.
     * 从 Image 实例创建 ResponsiveBuilder。
     *
     * @param source the source image | 源图片
     * @return a new builder instance | 新的构建器实例
     */
    public static ResponsiveBuilder of(Image source) {
        Objects.requireNonNull(source, "Source image must not be null");
        return new ResponsiveBuilder(source.copy());
    }

    /**
     * Add a variant configuration.
     * 添加一个变体配置。
     *
     * @param width  the target width | 目标宽度
     * @param height the target height | 目标高度
     * @param name   the variant name for output filenames | 用于输出文件名的变体名称
     * @return this builder for chaining | 用于链式调用的构建器
     */
    public ResponsiveBuilder variant(int width, int height, String name) {
        variants.add(new Variant(width, height, name));
        return this;
    }

    /**
     * Set the output formats. If not called, defaults to JPEG.
     * 设置输出格式。如果未调用，默认为 JPEG。
     *
     * @param formats the output formats | 输出格式
     * @return this builder for chaining | 用于链式调用的构建器
     */
    public ResponsiveBuilder formats(ImageFormat... formats) {
        Objects.requireNonNull(formats, "Formats must not be null");
        this.formats = formats.clone();
        return this;
    }

    /**
     * Set the compression quality.
     * 设置压缩质量。
     *
     * @param quality quality from 0.0 to 1.0 (default 0.85) | 质量（0.0 到 1.0，默认 0.85）
     * @return this builder for chaining | 用于链式调用的构建器
     * @throws IllegalArgumentException if quality is out of range | 如果质量超出范围
     */
    public ResponsiveBuilder quality(float quality) {
        if (quality < 0.0f || quality > 1.0f) {
            throw new IllegalArgumentException(
                    "Quality must be between 0.0 and 1.0, got " + quality);
        }
        this.quality = quality;
        return this;
    }

    /**
     * Generate all variant/format combinations and write them to the output directory.
     * 生成所有变体/格式组合并写入输出目录。
     *
     * <p>Output filenames follow the pattern: {@code {variantName}.{formatExtension}}</p>
     * <p>输出文件名遵循以下模式：{@code {变体名称}.{格式扩展名}}</p>
     *
     * @param outputDir the output directory | 输出目录
     * @return list of generation results | 生成结果列表
     * @throws IllegalStateException if no variants are configured | 如果未配置变体
     * @throws ImageIOException      if image processing fails | 如果图片处理失败
     */
    public List<GenerateResult> generate(Path outputDir) throws ImageIOException {
        Objects.requireNonNull(outputDir, "Output directory must not be null");
        if (variants.isEmpty()) {
            throw new IllegalStateException("At least one variant must be configured");
        }

        ImageFormat[] effectiveFormats = resolveFormats();

        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new ImageIOException("Failed to create output directory: " + outputDir, e);
        }

        List<GenerateResult> results = new ArrayList<>();

        for (Variant variant : variants) {
            Image resized = source.copy()
                    .resizeToFit(variant.width(), variant.height())
                    .compress(quality);

            for (ImageFormat fmt : effectiveFormats) {
                String fileName = variant.name() + "." + fmt.getExtension();
                Path outputPath = outputDir.resolve(fileName);
                resized.save(outputPath, fmt);

                long fileSize;
                try {
                    fileSize = Files.size(outputPath);
                } catch (IOException e) {
                    throw new ImageIOException("Failed to read file size: " + outputPath, e);
                }

                results.add(new GenerateResult(outputPath, variant, fmt, fileSize));
            }
        }

        return Collections.unmodifiableList(results);
    }

    /**
     * Create a batch builder for processing multiple source images.
     * 创建用于处理多个源图片的批量构建器。
     *
     * @param sources the list of source image paths | 源图片路径列表
     * @return a new batch builder | 新的批量构建器
     */
    public static BatchBuilder batch(List<Path> sources) {
        Objects.requireNonNull(sources, "Sources list must not be null");
        return new BatchBuilder(sources);
    }

    private ImageFormat[] resolveFormats() {
        if (formats == null || formats.length == 0) {
            return new ImageFormat[]{ImageFormat.JPEG};
        }
        return formats;
    }

    /**
     * Batch Builder for processing multiple source images with the same variant/format configuration.
     * 批量构建器，使用相同的变体/格式配置处理多个源图片。
     *
     * <p>Supports parallel processing and per-source error handling.</p>
     * <p>支持并行处理和每个源的错误处理。</p>
     *
     * <p><strong>Usage Examples | 使用示例:</strong></p>
     * <pre>{@code
     * ResponsiveBuilder.batch(List.of(path1, path2))
     *     .variant(64, 64, "icon")
     *     .formats(ImageFormat.JPEG)
     *     .parallel(4)
     *     .onError(e -> log.warn("Skipping: {}", e.getMessage()))
     *     .generate(outputDir);
     * }</pre>
     *
     * <p><strong>Security | 安全性:</strong></p>
     * <ul>
     *   <li>Thread-safe: No (builder pattern) - 线程安全: 否（构建器模式）</li>
     * </ul>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-image V2.0.0
     */
    public static final class BatchBuilder {

        private final List<Path> sources;
        private final List<Variant> variants = new ArrayList<>();
        private ImageFormat[] formats;
        private float quality = 0.85f;
        private int threads = 1;
        private Consumer<Exception> errorHandler;

        private BatchBuilder(List<Path> sources) {
            this.sources = new ArrayList<>(sources);
        }

        /**
         * Add a variant configuration.
         * 添加变体配置。
         *
         * @param width  the target width | 目标宽度
         * @param height the target height | 目标高度
         * @param name   the variant name | 变体名称
         * @return this builder for chaining | 用于链式调用的构建器
         */
        public BatchBuilder variant(int width, int height, String name) {
            variants.add(new Variant(width, height, name));
            return this;
        }

        /**
         * Set the output formats.
         * 设置输出格式。
         *
         * @param formats the output formats | 输出格式
         * @return this builder for chaining | 用于链式调用的构建器
         */
        public BatchBuilder formats(ImageFormat... formats) {
            Objects.requireNonNull(formats, "Formats must not be null");
            this.formats = formats.clone();
            return this;
        }

        /**
         * Set the compression quality.
         * 设置压缩质量。
         *
         * @param quality quality from 0.0 to 1.0 | 质量（0.0 到 1.0）
         * @return this builder for chaining | 用于链式调用的构建器
         */
        public BatchBuilder quality(float quality) {
            if (quality < 0.0f || quality > 1.0f) {
                throw new IllegalArgumentException(
                        "Quality must be between 0.0 and 1.0, got " + quality);
            }
            this.quality = quality;
            return this;
        }

        /**
         * Set the number of parallel threads for batch processing.
         * 设置批量处理的并行线程数。
         *
         * @param threads number of threads (must be positive, default 1) | 线程数（必须为正数，默认 1）
         * @return this builder for chaining | 用于链式调用的构建器
         * @throws IllegalArgumentException if threads is not positive | 如果线程数非正数
         */
        public BatchBuilder parallel(int threads) {
            if (threads <= 0) {
                throw new IllegalArgumentException("Thread count must be positive, got " + threads);
            }
            this.threads = threads;
            return this;
        }

        /**
         * Set an error handler for per-source failures. When set, processing continues
         * for remaining sources after a failure.
         * 设置每个源失败时的错误处理器。设置后，失败后继续处理剩余的源。
         *
         * @param handler the error handler | 错误处理器
         * @return this builder for chaining | 用于链式调用的构建器
         */
        public BatchBuilder onError(Consumer<Exception> handler) {
            Objects.requireNonNull(handler, "Error handler must not be null");
            this.errorHandler = handler;
            return this;
        }

        /**
         * Generate all variants for all sources. Creates a subdirectory per source file name
         * under the given output directory.
         * 为所有源生成所有变体。在给定输出目录下为每个源文件名创建子目录。
         *
         * @param outputDir the root output directory | 根输出目录
         * @return list of results per source (outer list corresponds to sources) | 每个源的结果列表
         * @throws IllegalStateException if no variants configured | 如果未配置变体
         * @throws ImageIOException      if processing fails and no error handler is set | 如果处理失败且未设置错误处理器
         */
        public List<List<GenerateResult>> generate(Path outputDir) throws ImageIOException {
            Objects.requireNonNull(outputDir, "Output directory must not be null");
            if (variants.isEmpty()) {
                throw new IllegalStateException("At least one variant must be configured");
            }

            if (sources.isEmpty()) {
                return Collections.emptyList();
            }

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            try {
                List<Future<List<GenerateResult>>> futures = new ArrayList<>();

                for (Path sourcePath : sources) {
                    futures.add(executor.submit(() -> processSource(sourcePath, outputDir)));
                }

                List<List<GenerateResult>> allResults = new ArrayList<>();
                for (Future<List<GenerateResult>> future : futures) {
                    try {
                        allResults.add(future.get());
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        Exception ex = cause instanceof Exception exc ? exc : new RuntimeException(cause);
                        if (errorHandler != null) {
                            errorHandler.accept(ex);
                            allResults.add(Collections.emptyList());
                        } else {
                            // Cancel remaining futures
                            for (Future<List<GenerateResult>> f : futures) {
                                f.cancel(true);
                            }
                            if (ex instanceof ImageIOException ioe) {
                                throw ioe;
                            }
                            throw new ImageIOException("Batch processing failed", ex);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        for (Future<List<GenerateResult>> f : futures) {
                            f.cancel(true);
                        }
                        throw new ImageIOException("Batch processing interrupted", e);
                    }
                }

                return Collections.unmodifiableList(allResults);
            } finally {
                executor.shutdownNow();
            }
        }

        private List<GenerateResult> processSource(Path sourcePath, Path outputDir) throws ImageIOException {
            String sourceName = stripExtension(sourcePath.getFileName().toString());
            Path sourceOutputDir = outputDir.resolve(sourceName);

            ResponsiveBuilder builder = ResponsiveBuilder.of(sourcePath);
            for (Variant v : variants) {
                builder.variant(v.width(), v.height(), v.name());
            }
            if (formats != null && formats.length > 0) {
                builder.formats(formats);
            }
            builder.quality(quality);

            return builder.generate(sourceOutputDir);
        }

        private static String stripExtension(String fileName) {
            int dotIndex = fileName.lastIndexOf('.');
            return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        }
    }
}
