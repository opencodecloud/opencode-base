package cloud.opencode.base.io;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * File Visitors Utility Class
 * 文件访问器工具类
 *
 * <p>Utility class providing pre-built FileVisitor implementations
 * for common file tree operations.</p>
 * <p>提供用于常见文件树操作的预构建FileVisitor实现的工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Delete visitor - 删除访问器</li>
 *   <li>Copy visitor - 复制访问器</li>
 *   <li>Custom action visitor - 自定义操作访问器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Delete directory recursively
 * Files.walkFileTree(dir, OpenFileVisitors.deleteVisitor());
 *
 * // Copy directory
 * Files.walkFileTree(source, OpenFileVisitors.copyVisitor(source, target));
 *
 * // Custom action
 * Files.walkFileTree(dir, OpenFileVisitors.actionVisitor(
 *     path -> System.out.println("Found: " + path)
 * ));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class OpenFileVisitors {

    private OpenFileVisitors() {
    }

    /**
     * Creates a delete visitor that removes all files and directories
     * 创建删除所有文件和目录的删除访问器
     *
     * @return file visitor | 文件访问器
     */
    public static FileVisitor<Path> deleteVisitor() {
        return new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        };
    }

    /**
     * Creates a copy visitor that copies files and directories
     * 创建复制文件和目录的复制访问器
     *
     * @param source  the source directory | 源目录
     * @param target  the target directory | 目标目录
     * @param options the copy options | 复制选项
     * @return file visitor | 文件访问器
     */
    public static FileVisitor<Path> copyVisitor(Path source, Path target, CopyOption... options) {
        return new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, options);
                return FileVisitResult.CONTINUE;
            }
        };
    }

    /**
     * Creates an action visitor that performs an action on each file
     * 创建对每个文件执行操作的操作访问器
     *
     * @param action the action to perform | 要执行的操作
     * @return file visitor | 文件访问器
     */
    public static FileVisitor<Path> actionVisitor(Consumer<Path> action) {
        return new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                action.accept(file);
                return FileVisitResult.CONTINUE;
            }
        };
    }

    /**
     * Creates a filtered action visitor
     * 创建带过滤器的操作访问器
     *
     * @param filter the filter predicate | 过滤谓词
     * @param action the action to perform | 要执行的操作
     * @return file visitor | 文件访问器
     */
    public static FileVisitor<Path> filteredVisitor(BiPredicate<Path, BasicFileAttributes> filter, Consumer<Path> action) {
        return new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (filter.test(file, attrs)) {
                    action.accept(file);
                }
                return FileVisitResult.CONTINUE;
            }
        };
    }

    /**
     * Creates a counting visitor that counts files and directories
     * 创建统计文件和目录的计数访问器
     *
     * @return counting visitor | 计数访问器
     */
    public static CountingVisitor countingVisitor() {
        return new CountingVisitor();
    }

    /**
     * Creates a size calculating visitor
     * 创建计算大小的访问器
     *
     * @return size visitor | 大小访问器
     */
    public static SizeVisitor sizeVisitor() {
        return new SizeVisitor();
    }

    /**
     * Counting Visitor
     * 计数访问器
     */
    public static class CountingVisitor extends SimpleFileVisitor<Path> {
        private long fileCount = 0;
        private long directoryCount = 0;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            fileCount++;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            directoryCount++;
            return FileVisitResult.CONTINUE;
        }

        /**
         * Gets file count
         * 获取文件数量
         *
         * @return file count | 文件数量
         */
        public long getFileCount() {
            return fileCount;
        }

        /**
         * Gets directory count
         * 获取目录数量
         *
         * @return directory count | 目录数量
         */
        public long getDirectoryCount() {
            return directoryCount;
        }

        /**
         * Gets total count
         * 获取总数量
         *
         * @return total count | 总数量
         */
        public long getTotalCount() {
            return fileCount + directoryCount;
        }
    }

    /**
     * Size Visitor
     * 大小访问器
     */
    public static class SizeVisitor extends SimpleFileVisitor<Path> {
        private long totalSize = 0;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            totalSize += attrs.size();
            return FileVisitResult.CONTINUE;
        }

        /**
         * Gets total size
         * 获取总大小
         *
         * @return total size in bytes | 总字节大小
         */
        public long getTotalSize() {
            return totalSize;
        }
    }
}
