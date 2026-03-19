package cloud.opencode.base.io.batch;

import cloud.opencode.base.io.OpenIO;
import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Batch and Parallel File Operations Utility Class
 * 批量和并行文件操作工具类
 *
 * <p>Provides batch operations for copying, moving, and deleting multiple files
 * with parallel execution support and progress tracking.</p>
 * <p>提供批量复制、移动和删除多个文件的操作，支持并行执行和进度跟踪。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Batch copy/move/delete operations - 批量复制/移动/删除</li>
 *   <li>Parallel execution with configurable parallelism - 可配置并行度的并行执行</li>
 *   <li>Progress callbacks - 进度回调</li>
 *   <li>Failure handling and result collection - 失败处理和结果收集</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Batch copy files
 * List<Path> files = List.of(path1, path2, path3);
 * BatchResult result = OpenBatch.copyAll(files, targetDir);
 *
 * // Parallel delete with progress
 * BatchResult result = OpenBatch.parallel()
 *     .parallelism(4)
 *     .onProgress((path, index, total) -> System.out.println("Processing: " + path))
 *     .deleteAll(files);
 *
 * // Batch move with filter
 * BatchResult result = OpenBatch.moveAll(files, targetDir,
 *     path -> path.toString().endsWith(".txt"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, parallel operations use virtual threads - 线程安全: 是，并行操作使用虚拟线程</li>
 *   <li>Null-safe: No, paths and arguments must not be null - 空值安全: 否，路径和参数不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class OpenBatch {

    /**
     * Default parallelism (number of processors)
     * 默认并行度（处理器数量）
     */
    public static final int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors();

    private OpenBatch() {
    }

    // ==================== Sequential Batch Operations | 顺序批量操作 ====================

    /**
     * Copy all files to target directory
     * 复制所有文件到目标目录
     *
     * @param sources source files | 源文件
     * @param targetDir target directory | 目标目录
     * @param options copy options | 复制选项
     * @return batch result | 批量结果
     */
    public static BatchResult copyAll(Collection<Path> sources, Path targetDir, CopyOption... options) {
        return copyAll(sources, targetDir, path -> true, options);
    }

    /**
     * Copy files matching filter to target directory
     * 复制匹配过滤器的文件到目标目录
     *
     * @param sources source files | 源文件
     * @param targetDir target directory | 目标目录
     * @param filter file filter | 文件过滤器
     * @param options copy options | 复制选项
     * @return batch result | 批量结果
     */
    public static BatchResult copyAll(Collection<Path> sources, Path targetDir,
                                      Predicate<Path> filter, CopyOption... options) {
        ensureDirectory(targetDir);
        BatchResult.Builder builder = BatchResult.builder("copy");

        for (Path source : sources) {
            if (!filter.test(source)) {
                builder.skipped();
                continue;
            }
            try {
                Path target = targetDir.resolve(source.getFileName());
                Files.copy(source, target, options);
                builder.success();
            } catch (Exception e) {
                builder.failure(source, e);
            }
        }
        return builder.build();
    }

    /**
     * Move all files to target directory
     * 移动所有文件到目标目录
     *
     * @param sources source files | 源文件
     * @param targetDir target directory | 目标目录
     * @param options move options | 移动选项
     * @return batch result | 批量结果
     */
    public static BatchResult moveAll(Collection<Path> sources, Path targetDir, CopyOption... options) {
        return moveAll(sources, targetDir, path -> true, options);
    }

    /**
     * Move files matching filter to target directory
     * 移动匹配过滤器的文件到目标目录
     *
     * @param sources source files | 源文件
     * @param targetDir target directory | 目标目录
     * @param filter file filter | 文件过滤器
     * @param options move options | 移动选项
     * @return batch result | 批量结果
     */
    public static BatchResult moveAll(Collection<Path> sources, Path targetDir,
                                      Predicate<Path> filter, CopyOption... options) {
        ensureDirectory(targetDir);
        BatchResult.Builder builder = BatchResult.builder("move");

        for (Path source : sources) {
            if (!filter.test(source)) {
                builder.skipped();
                continue;
            }
            try {
                Path target = targetDir.resolve(source.getFileName());
                Files.move(source, target, options);
                builder.success();
            } catch (Exception e) {
                builder.failure(source, e);
            }
        }
        return builder.build();
    }

    /**
     * Delete all files
     * 删除所有文件
     *
     * @param paths files to delete | 要删除的文件
     * @return batch result | 批量结果
     */
    public static BatchResult deleteAll(Collection<Path> paths) {
        return deleteAll(paths, path -> true);
    }

    /**
     * Delete files matching filter
     * 删除匹配过滤器的文件
     *
     * @param paths files to delete | 要删除的文件
     * @param filter file filter | 文件过滤器
     * @return batch result | 批量结果
     */
    public static BatchResult deleteAll(Collection<Path> paths, Predicate<Path> filter) {
        BatchResult.Builder builder = BatchResult.builder("delete");

        for (Path path : paths) {
            if (!filter.test(path)) {
                builder.skipped();
                continue;
            }
            try {
                if (Files.isDirectory(path)) {
                    OpenIO.deleteRecursively(path);
                } else {
                    Files.deleteIfExists(path);
                }
                builder.success();
            } catch (Exception e) {
                builder.failure(path, e);
            }
        }
        return builder.build();
    }

    // ==================== Glob/Pattern Based Batch Operations | 基于模式的批量操作 ====================

    /**
     * Copy files matching glob pattern
     * 复制匹配 glob 模式的文件
     *
     * @param sourceDir source directory | 源目录
     * @param pattern glob pattern | glob 模式
     * @param targetDir target directory | 目标目录
     * @param options copy options | 复制选项
     * @return batch result | 批量结果
     */
    public static BatchResult copyGlob(Path sourceDir, String pattern, Path targetDir, CopyOption... options) {
        List<Path> files = collectGlob(sourceDir, pattern);
        return copyAll(files, targetDir, options);
    }

    /**
     * Move files matching glob pattern
     * 移动匹配 glob 模式的文件
     *
     * @param sourceDir source directory | 源目录
     * @param pattern glob pattern | glob 模式
     * @param targetDir target directory | 目标目录
     * @param options move options | 移动选项
     * @return batch result | 批量结果
     */
    public static BatchResult moveGlob(Path sourceDir, String pattern, Path targetDir, CopyOption... options) {
        List<Path> files = collectGlob(sourceDir, pattern);
        return moveAll(files, targetDir, options);
    }

    /**
     * Delete files matching glob pattern
     * 删除匹配 glob 模式的文件
     *
     * @param dir directory | 目录
     * @param pattern glob pattern | glob 模式
     * @return batch result | 批量结果
     */
    public static BatchResult deleteGlob(Path dir, String pattern) {
        List<Path> files = collectGlob(dir, pattern);
        return deleteAll(files);
    }

    // ==================== Parallel Builder | 并行构建器 ====================

    /**
     * Create a parallel batch operation builder
     * 创建并行批量操作构建器
     *
     * @return builder | 构建器
     */
    public static ParallelBuilder parallel() {
        return new ParallelBuilder();
    }

    /**
     * Parallel Batch Operation Builder
     * 并行批量操作构建器
     */
    public static class ParallelBuilder {
        private int parallelism = DEFAULT_PARALLELISM;
        private ProgressCallback progressCallback;
        private boolean stopOnError = false;

        /**
         * Set parallelism level
         * 设置并行度
         *
         * @param parallelism number of parallel threads | 并行线程数
         * @return this builder | 构建器
         */
        public ParallelBuilder parallelism(int parallelism) {
            this.parallelism = Math.max(1, parallelism);
            return this;
        }

        /**
         * Set progress callback
         * 设置进度回调
         *
         * @param callback callback function | 回调函数
         * @return this builder | 构建器
         */
        public ParallelBuilder onProgress(ProgressCallback callback) {
            this.progressCallback = callback;
            return this;
        }

        /**
         * Stop on first error
         * 遇到第一个错误时停止
         *
         * @param stop whether to stop | 是否停止
         * @return this builder | 构建器
         */
        public ParallelBuilder stopOnError(boolean stop) {
            this.stopOnError = stop;
            return this;
        }

        /**
         * Copy all files in parallel
         * 并行复制所有文件
         *
         * @param sources source files | 源文件
         * @param targetDir target directory | 目标目录
         * @param options copy options | 复制选项
         * @return batch result | 批量结果
         */
        public BatchResult copyAll(Collection<Path> sources, Path targetDir, CopyOption... options) {
            ensureDirectory(targetDir);
            return executeParallel("copy", sources, path -> {
                Path target = targetDir.resolve(path.getFileName());
                Files.copy(path, target, options);
            });
        }

        /**
         * Move all files in parallel
         * 并行移动所有文件
         *
         * @param sources source files | 源文件
         * @param targetDir target directory | 目标目录
         * @param options move options | 移动选项
         * @return batch result | 批量结果
         */
        public BatchResult moveAll(Collection<Path> sources, Path targetDir, CopyOption... options) {
            ensureDirectory(targetDir);
            return executeParallel("move", sources, path -> {
                Path target = targetDir.resolve(path.getFileName());
                Files.move(path, target, options);
            });
        }

        /**
         * Delete all files in parallel
         * 并行删除所有文件
         *
         * @param paths files to delete | 要删除的文件
         * @return batch result | 批量结果
         */
        public BatchResult deleteAll(Collection<Path> paths) {
            return executeParallel("delete", paths, path -> {
                if (Files.isDirectory(path)) {
                    OpenIO.deleteRecursively(path);
                } else {
                    Files.deleteIfExists(path);
                }
            });
        }

        /**
         * Execute custom operation in parallel
         * 并行执行自定义操作
         *
         * @param operation operation name | 操作名称
         * @param paths paths to process | 要处理的路径
         * @param action action to perform | 要执行的操作
         * @return batch result | 批量结果
         */
        public BatchResult execute(String operation, Collection<Path> paths, PathAction action) {
            return executeParallel(operation, paths, action);
        }

        private BatchResult executeParallel(String operation, Collection<Path> paths, PathAction action) {
            BatchResult.Builder builder = BatchResult.builder(operation);
            List<Path> pathList = List.copyOf(paths);
            int total = pathList.size();

            if (total == 0) {
                return builder.build();
            }

            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            java.util.concurrent.atomic.AtomicInteger index = new java.util.concurrent.atomic.AtomicInteger(0);
            java.util.concurrent.atomic.AtomicBoolean stopped = new java.util.concurrent.atomic.AtomicBoolean(false);
            Semaphore semaphore = new Semaphore(parallelism);

            List<CompletableFuture<Void>> futures = pathList.stream()
                .map(path -> CompletableFuture.runAsync(() -> {
                    if (stopped.get()) {
                        builder.incrementSkipped();
                        return;
                    }

                    try {
                        semaphore.acquire();
                        try {
                            int currentIndex = index.incrementAndGet();
                            if (progressCallback != null) {
                                progressCallback.onProgress(path, currentIndex, total);
                            }
                            action.execute(path);
                            builder.incrementSuccess();
                        } finally {
                            semaphore.release();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        builder.recordFailure(path, e);
                    } catch (Exception e) {
                        builder.recordFailure(path, e);
                        if (stopOnError) {
                            stopped.set(true);
                        }
                    }
                }, executor))
                .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.close();

            return builder.build();
        }
    }

    // ==================== Functional Interfaces | 函数式接口 ====================

    /**
     * Progress callback interface
     * 进度回调接口
     */
    @FunctionalInterface
    public interface ProgressCallback {
        /**
         * Called for each processed item
         * 每个处理项调用
         *
         * @param path current path | 当前路径
         * @param current current index (1-based) | 当前索引（从1开始）
         * @param total total count | 总数
         */
        void onProgress(Path path, int current, int total);
    }

    /**
     * Path action interface
     * 路径操作接口
     */
    @FunctionalInterface
    public interface PathAction {
        /**
         * Execute action on path
         * 在路径上执行操作
         *
         * @param path the path | 路径
         * @throws Exception if operation fails | 如果操作失败
         */
        void execute(Path path) throws Exception;
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Collect files from directory
     * 从目录收集文件
     *
     * @param dir directory | 目录
     * @return list of files | 文件列表
     */
    public static List<Path> collectFiles(Path dir) {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(dir, e);
        }
    }

    /**
     * Collect files matching glob pattern
     * 收集匹配 glob 模式的文件
     *
     * @param dir directory | 目录
     * @param pattern glob pattern | glob 模式
     * @return list of files | 文件列表
     */
    public static List<Path> collectGlob(Path dir, String pattern) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> matcher.matches(path.getFileName()))
                .toList();
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(dir, e);
        }
    }

    /**
     * Collect files by extension
     * 按扩展名收集文件
     *
     * @param dir directory | 目录
     * @param extensions extensions (without dot) | 扩展名（不含点）
     * @return list of files | 文件列表
     */
    public static List<Path> collectByExtension(Path dir, String... extensions) {
        java.util.Set<String> extSet = java.util.Set.of(extensions);
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String ext = cloud.opencode.base.io.OpenPath.getExtension(path);
                    return extSet.contains(ext.toLowerCase());
                })
                .toList();
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(dir, e);
        }
    }

    private static void ensureDirectory(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            throw OpenIOOperationException.createDirectoryFailed(dir, e);
        }
    }
}
