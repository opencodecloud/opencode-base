package cloud.opencode.base.parallel.batch;

import cloud.opencode.base.parallel.exception.OpenParallelException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Batch Processor - Parallel Batch Processing Utility
 * 批处理器 - 并行批处理工具
 *
 * <p>Provides configurable batch processing with parallel execution,
 * concurrency control, and progress tracking.</p>
 * <p>提供可配置的批处理，支持并行执行、并发控制和进度跟踪。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * BatchProcessor.builder()
 *     .batchSize(100)
 *     .parallelism(10)
 *     .build()
 *     .process(items, batch -> repository.saveAll(batch));
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable batch size and parallelism - 可配置的批次大小和并行度</li>
 *   <li>Progress tracking - 进度跟踪</li>
 *   <li>Error handling per batch - 每批次错误处理</li>
 *   <li>Virtual thread execution - 虚拟线程执行</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n/b * T) where b is batch size and T is per-batch processor time; parallel execution reduces wall time to O(n/b/p * T) with parallelism p - 时间复杂度: O(n/b * T)，b 为批次大小，T 为每批处理时间；并行度 p 时实际耗时降至 O(n/b/p * T)</li>
 *   <li>Space complexity: O(n/b) - CompletableFuture list proportional to batch count; batches themselves reference input subLists - 空间复杂度: O(n/b) - CompletableFuture 列表与批次数成正比；批次引用输入子列表</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class BatchProcessor implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(BatchProcessor.class.getName());

    private final int batchSize;
    private final int parallelism;
    private final Duration timeout;
    private final ExecutorService executor;
    private final boolean stopOnError;
    private final boolean ownsExecutor;

    private BatchProcessor(Builder builder) {
        this.batchSize = builder.batchSize;
        this.parallelism = builder.parallelism;
        this.timeout = builder.timeout;
        this.ownsExecutor = builder.executor == null;
        // Create a new virtual thread executor per instance to avoid sharing a static
        // executor that could be shut down by one instance and break others
        this.executor = this.ownsExecutor ? Executors.newVirtualThreadPerTaskExecutor() : builder.executor;
        this.stopOnError = builder.stopOnError;
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a default batch processor.
     * 创建默认批处理器。
     *
     * @return the processor - 处理器
     */
    public static BatchProcessor defaultProcessor() {
        return new Builder().build();
    }

    // ==================== Processing ====================

    /**
     * Processes items in batches.
     * 批量处理项目。
     *
     * @param items     the items to process - 要处理的项目
     * @param processor the batch processor - 批处理器
     * @param <T>       the item type - 项目类型
     */
    public <T> void process(List<T> items, Consumer<List<T>> processor) {
        List<List<T>> batches = PartitionUtil.partition(items, batchSize);
        processBatches(batches, processor);
    }

    /**
     * Processes items in batches with result collection.
     * 批量处理项目并收集结果。
     *
     * @param items     the items to process - 要处理的项目
     * @param processor the batch processor - 批处理器
     * @param <T>       the item type - 项目类型
     * @param <R>       the result type - 结果类型
     * @return the results - 结果
     */
    public <T, R> List<R> processAndCollect(List<T> items, Function<List<T>, List<R>> processor) {
        List<List<T>> batches = PartitionUtil.partition(items, batchSize);
        return processBatchesAndCollect(batches, processor);
    }

    /**
     * Processes items in batches with progress callback.
     * 批量处理项目并回调进度。
     *
     * @param items            the items to process - 要处理的项目
     * @param processor        the batch processor - 批处理器
     * @param progressCallback the progress callback - 进度回调
     * @param <T>              the item type - 项目类型
     */
    public <T> void processWithProgress(List<T> items, Consumer<List<T>> processor,
                                         Consumer<BatchProgress> progressCallback) {
        List<List<T>> batches = PartitionUtil.partition(items, batchSize);
        AtomicInteger completed = new AtomicInteger(0);
        int total = batches.size();

        Semaphore semaphore = new Semaphore(parallelism);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (List<T> batch : batches) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    semaphore.acquire();
                    try {
                        processor.accept(batch);
                        int done = completed.incrementAndGet();
                        try {
                            progressCallback.accept(new BatchProgress(done, total, batch.size()));
                        } catch (Exception callbackEx) {
                            // Progress callback exceptions must not crash the processing thread
                            LOGGER.log(System.Logger.Level.WARNING, "Progress callback failed", callbackEx);
                        }
                    } finally {
                        semaphore.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new OpenParallelException("Batch processing interrupted", e);
                }
            }, executor);
            futures.add(future);
        }

        waitForCompletion(futures);
    }

    private <T> void processBatches(List<List<T>> batches, Consumer<List<T>> processor) {
        Semaphore semaphore = new Semaphore(parallelism);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (List<T> batch : batches) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                boolean acquired = false;
                try {
                    semaphore.acquire();
                    acquired = true;
                    processor.accept(batch);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new OpenParallelException("Batch processing interrupted", e);
                } catch (Exception e) {
                    errors.add(e);
                    if (stopOnError) {
                        throw new OpenParallelException("Batch processing failed", e);
                    }
                } finally {
                    if (acquired) {
                        semaphore.release();
                    }
                }
            }, executor);
            futures.add(future);
        }

        waitForCompletion(futures);

        if (!errors.isEmpty()) {
            throw OpenParallelException.partialFailure(errors, batches.size());
        }
    }

    private <T, R> List<R> processBatchesAndCollect(List<List<T>> batches,
                                                     Function<List<T>, List<R>> processor) {
        Semaphore semaphore = new Semaphore(parallelism);
        List<CompletableFuture<List<R>>> futures = new ArrayList<>();

        for (List<T> batch : batches) {
            CompletableFuture<List<R>> future = CompletableFuture.supplyAsync(() -> {
                boolean acquired = false;
                try {
                    semaphore.acquire();
                    acquired = true;
                    return processor.apply(batch);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new OpenParallelException("Batch processing interrupted", e);
                } finally {
                    if (acquired) {
                        semaphore.release();
                    }
                }
            }, executor);
            futures.add(future);
        }

        List<R> results = new ArrayList<>();
        for (CompletableFuture<List<R>> future : futures) {
            results.addAll(future.join());
        }
        return results;
    }

    private void waitForCompletion(List<CompletableFuture<Void>> futures) {
        CompletableFuture<Void> all = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        try {
            if (timeout != null) {
                all.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } else {
                all.join();
            }
        } catch (java.util.concurrent.TimeoutException e) {
            futures.forEach(f -> f.cancel(true));
            throw OpenParallelException.timeout(timeout);
        } catch (Exception e) {
            throw new OpenParallelException("Batch processing failed", e);
        }
    }

    // ==================== Getters ====================

    /**
     * Gets the batch size.
     * 获取批大小。
     *
     * @return the batch size - 批大小
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Gets the parallelism.
     * 获取并行度。
     *
     * @return the parallelism - 并行度
     */
    public int getParallelism() {
        return parallelism;
    }

    // ==================== Lifecycle ====================

    /**
     * Shuts down the internal executor if it is not user-provided.
     * User-provided executors are not managed by this processor.
     * 如果不是用户提供的执行器，则关闭内部执行器。
     * 用户提供的执行器不由此处理器管理。
     */
    @Override
    public void close() {
        if (ownsExecutor) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }
    }

    // ==================== Progress ====================

    /**
     * Batch progress information.
     * 批处理进度信息。
     */
    public record BatchProgress(int completedBatches, int totalBatches, int lastBatchSize) {

        /**
         * Gets the progress percentage.
         * 获取进度百分比。
         *
         * @return the percentage (0-100) - 百分比
         */
        public int percentage() {
            return totalBatches == 0 ? 100 : (completedBatches * 100) / totalBatches;
        }

        /**
         * Checks if processing is complete.
         * 检查处理是否完成。
         *
         * @return true if complete - 如果完成返回 true
         */
        public boolean isComplete() {
            return completedBatches >= totalBatches;
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for BatchProcessor.
     * BatchProcessor 的构建器。
     */
    public static final class Builder {
        private int batchSize = 100;
        private int parallelism = Runtime.getRuntime().availableProcessors();
        private Duration timeout;
        private ExecutorService executor;
        private boolean stopOnError = false;

        private Builder() {
        }

        /**
         * Sets the batch size.
         * 设置批大小。
         *
         * @param batchSize the batch size - 批大小
         * @return this builder - 此构建器
         */
        public Builder batchSize(int batchSize) {
            if (batchSize <= 0) {
                throw new IllegalArgumentException("Batch size must be positive: " + batchSize);
            }
            this.batchSize = batchSize;
            return this;
        }

        /**
         * Sets the parallelism.
         * 设置并行度。
         *
         * @param parallelism the parallelism - 并行度
         * @return this builder - 此构建器
         */
        public Builder parallelism(int parallelism) {
            if (parallelism <= 0) {
                throw new IllegalArgumentException("Parallelism must be positive: " + parallelism);
            }
            this.parallelism = parallelism;
            return this;
        }

        /**
         * Sets the timeout.
         * 设置超时。
         *
         * @param timeout the timeout - 超时
         * @return this builder - 此构建器
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets the executor.
         * 设置执行器。
         *
         * @param executor the executor - 执行器
         * @return this builder - 此构建器
         */
        public Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Sets whether to stop on error.
         * 设置是否在错误时停止。
         *
         * @param stopOnError whether to stop - 是否停止
         * @return this builder - 此构建器
         */
        public Builder stopOnError(boolean stopOnError) {
            this.stopOnError = stopOnError;
            return this;
        }

        /**
         * Builds the processor.
         * 构建处理器。
         *
         * @return the processor - 处理器
         */
        public BatchProcessor build() {
            return new BatchProcessor(this);
        }
    }
}
