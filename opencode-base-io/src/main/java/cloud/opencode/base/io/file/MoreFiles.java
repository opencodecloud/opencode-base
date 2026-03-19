/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * More File Utilities - Advanced file operations
 * 更多文件工具 - 高级文件操作
 *
 * <p>Provides advanced file operations including atomic writes,
 * enhanced file tree traversal, and secure file operations.</p>
 * <p>提供高级文件操作，包括原子写入、增强的文件树遍历和安全文件操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Atomic write operations - 原子写入操作</li>
 *   <li>Enhanced file tree traversal - 增强的文件树遍历</li>
 *   <li>Secure delete - 安全删除</li>
 *   <li>Touch operation - 创建或更新时间戳</li>
 *   <li>File equality checking - 文件相等性检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Atomic write (safe for concurrent access)
 * MoreFiles.writeAtomically(path, "content");
 *
 * // Touch file (create if not exists, update timestamp if exists)
 * MoreFiles.touch(path);
 *
 * // Delete recursively with filter
 * MoreFiles.deleteRecursively(dir, path -> path.toString().endsWith(".tmp"));
 *
 * // File tree as stream with options
 * try (Stream<Path> files = MoreFiles.fileTree(dir)
 *         .maxDepth(3)
 *         .filter(Files::isRegularFile)
 *         .stream()) {
 *     files.forEach(System.out::println);
 * }
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
 * @since JDK 25, opencode-base-io V1.2.0
 */
public final class MoreFiles {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private MoreFiles() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // ==================== Atomic Write Operations | 原子写入操作 ====================

    /**
     * Writes content atomically to a file.
     * 原子地将内容写入文件。
     *
     * <p>This method writes to a temporary file first, then atomically
     * moves it to the target location. This ensures that readers never
     * see partially written content.</p>
     * <p>此方法先写入临时文件，然后原子地移动到目标位置。
     * 这确保读者永远不会看到部分写入的内容。</p>
     *
     * @param path    the target file path | 目标文件路径
     * @param content the content to write | 要写入的内容
     * @param charset the charset | 字符集
     */
    public static void writeAtomically(Path path, String content, Charset charset) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(charset, "charset must not be null");

        writeAtomically(path, content.getBytes(charset));
    }

    /**
     * Writes content atomically to a file (UTF-8).
     * 原子地将内容写入文件（UTF-8）。
     *
     * @param path    the target file path | 目标文件路径
     * @param content the content to write | 要写入的内容
     */
    public static void writeAtomically(Path path, String content) {
        writeAtomically(path, content, StandardCharsets.UTF_8);
    }

    /**
     * Writes bytes atomically to a file.
     * 原子地将字节写入文件。
     *
     * @param path the target file path | 目标文件路径
     * @param data the bytes to write | 要写入的字节
     */
    public static void writeAtomically(Path path, byte[] data) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(data, "data must not be null");

        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new OpenIOOperationException("createDirectories", parent.toString(),
                        "Failed to create parent directories", e);
            }
        }

        try {
            Path tempFile = Files.createTempFile(
                    parent != null ? parent : Path.of("."),
                    path.getFileName().toString(),
                    ".tmp"
            );

            try {
                Files.write(tempFile, data);
                Files.move(tempFile, path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // Clean up temp file on failure
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
                throw e;
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("atomicWrite", path.toString(),
                    "Failed to write file atomically", e);
        }
    }

    /**
     * Writes lines atomically to a file.
     * 原子地将行写入文件。
     *
     * @param path    the target file path | 目标文件路径
     * @param lines   the lines to write | 要写入的行
     * @param charset the charset | 字符集
     */
    public static void writeAtomically(Path path, Iterable<String> lines, Charset charset) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(lines, "lines must not be null");
        Objects.requireNonNull(charset, "charset must not be null");

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append(System.lineSeparator());
        }
        writeAtomically(path, sb.toString(), charset);
    }

    /**
     * Writes lines atomically to a file (UTF-8).
     * 原子地将行写入文件（UTF-8）。
     *
     * @param path  the target file path | 目标文件路径
     * @param lines the lines to write | 要写入的行
     */
    public static void writeAtomically(Path path, Iterable<String> lines) {
        writeAtomically(path, lines, StandardCharsets.UTF_8);
    }

    /**
     * Writes from an InputStream atomically to a file.
     * 原子地从 InputStream 写入文件。
     *
     * @param path  the target file path | 目标文件路径
     * @param input the input stream | 输入流
     */
    public static void writeAtomically(Path path, InputStream input) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(input, "input must not be null");

        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new OpenIOOperationException("createDirectories", parent.toString(),
                        "Failed to create parent directories", e);
            }
        }

        try {
            Path tempFile = Files.createTempFile(
                    parent != null ? parent : Path.of("."),
                    path.getFileName().toString(),
                    ".tmp"
            );

            try {
                try (OutputStream out = Files.newOutputStream(tempFile)) {
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
                Files.move(tempFile, path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
                throw e;
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("atomicWrite", path.toString(),
                    "Failed to write file atomically from stream", e);
        }
    }

    // ==================== Touch Operation | Touch 操作 ====================

    /**
     * Creates an empty file or updates its last modified time.
     * 创建空文件或更新其最后修改时间。
     *
     * <p>Similar to Unix 'touch' command.</p>
     * <p>类似于 Unix 的 'touch' 命令。</p>
     *
     * @param path the file path | 文件路径
     */
    public static void touch(Path path) {
        Objects.requireNonNull(path, "path must not be null");

        try {
            if (Files.exists(path)) {
                Files.setLastModifiedTime(path, java.nio.file.attribute.FileTime.fromMillis(
                        System.currentTimeMillis()));
            } else {
                Path parent = path.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("touch", path.toString(),
                    "Failed to touch file", e);
        }
    }

    // ==================== Delete Operations | 删除操作 ====================

    /**
     * Deletes a file or directory recursively.
     * 递归删除文件或目录。
     *
     * @param path the path to delete | 要删除的路径
     */
    public static void deleteRecursively(Path path) {
        deleteRecursively(path, p -> true);
    }

    /**
     * Deletes files recursively that match the filter.
     * 递归删除匹配过滤器的文件。
     *
     * @param path   the path to delete from | 要删除的起始路径
     * @param filter the filter for files to delete | 要删除的文件过滤器
     */
    public static void deleteRecursively(Path path, Predicate<Path> filter) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(filter, "filter must not be null");

        if (!Files.exists(path)) {
            return;
        }

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (filter.test(file)) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) {
                        throw exc;
                    }
                    // Delete directory if empty and matches filter
                    if (filter.test(dir) && isEmptyDirectory(dir)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new OpenIOOperationException("deleteRecursively", path.toString(),
                    "Failed to delete recursively", e);
        }
    }

    /**
     * Deletes if exists, returning whether deletion occurred.
     * 如果存在则删除，返回是否发生删除。
     *
     * @param path the path to delete | 要删除的路径
     * @return true if the file was deleted | 如果文件被删除返回 true
     */
    public static boolean deleteIfExists(Path path) {
        Objects.requireNonNull(path, "path must not be null");

        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new OpenIOOperationException("delete", path.toString(),
                    "Failed to delete file", e);
        }
    }

    /**
     * Deletes a directory only if it's empty.
     * 仅当目录为空时删除。
     *
     * @param path the directory path | 目录路径
     * @return true if deleted | 如果已删除返回 true
     */
    public static boolean deleteDirectoryIfEmpty(Path path) {
        Objects.requireNonNull(path, "path must not be null");

        if (!Files.isDirectory(path)) {
            return false;
        }

        if (!isEmptyDirectory(path)) {
            return false;
        }

        return deleteIfExists(path);
    }

    // ==================== File Tree Operations | 文件树操作 ====================

    /**
     * Creates a file tree traversal builder.
     * 创建文件树遍历构建器。
     *
     * @param root the root directory | 根目录
     * @return a FileTreeTraversal builder | FileTreeTraversal 构建器
     */
    public static FileTreeTraversal fileTree(Path root) {
        return new FileTreeTraversal(root);
    }

    /**
     * Returns all files (not directories) under the path recursively.
     * 递归返回路径下的所有文件（不包括目录）。
     *
     * @param path the root path | 根路径
     * @return stream of files | 文件流
     */
    public static Stream<Path> listFilesRecursively(Path path) {
        return fileTree(path).filter(Files::isRegularFile).stream();
    }

    /**
     * Returns all directories under the path recursively.
     * 递归返回路径下的所有目录。
     *
     * @param path the root path | 根路径
     * @return stream of directories | 目录流
     */
    public static Stream<Path> listDirectoriesRecursively(Path path) {
        return fileTree(path).filter(Files::isDirectory).stream();
    }

    /**
     * Walks the file tree and applies the action to each file.
     * 遍历文件树并对每个文件应用操作。
     *
     * @param path   the root path | 根路径
     * @param action the action to apply | 要应用的操作
     */
    public static void walkFileTree(Path path, Consumer<Path> action) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(action, "action must not be null");

        try (Stream<Path> stream = fileTree(path).stream()) {
            stream.forEach(action);
        }
    }

    // ==================== File Equality | 文件相等性 ====================

    /**
     * Checks if two files have equal content.
     * 检查两个文件内容是否相等。
     *
     * @param path1 first file | 第一个文件
     * @param path2 second file | 第二个文件
     * @return true if content is equal | 如果内容相等返回 true
     */
    public static boolean contentEquals(Path path1, Path path2) {
        Objects.requireNonNull(path1, "path1 must not be null");
        Objects.requireNonNull(path2, "path2 must not be null");

        try {
            // Quick checks
            if (Files.isSameFile(path1, path2)) {
                return true;
            }

            long size1 = Files.size(path1);
            long size2 = Files.size(path2);
            if (size1 != size2) {
                return false;
            }

            if (size1 == 0) {
                return true;
            }

            // Compare content
            try (InputStream in1 = Files.newInputStream(path1);
                 InputStream in2 = Files.newInputStream(path2)) {

                byte[] buf1 = new byte[DEFAULT_BUFFER_SIZE];
                byte[] buf2 = new byte[DEFAULT_BUFFER_SIZE];

                while (true) {
                    int read1 = in1.read(buf1);
                    int read2 = in2.read(buf2);

                    if (read1 != read2) {
                        return false;
                    }
                    if (read1 == -1) {
                        return true;
                    }
                    if (!Arrays.equals(buf1, 0, read1, buf2, 0, read2)) {
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("contentEquals", path1 + ", " + path2,
                    "Failed to compare file contents", e);
        }
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Checks if a directory is empty.
     * 检查目录是否为空。
     *
     * @param path the directory path | 目录路径
     * @return true if empty | 如果为空返回 true
     */
    public static boolean isEmptyDirectory(Path path) {
        Objects.requireNonNull(path, "path must not be null");

        if (!Files.isDirectory(path)) {
            return false;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            return !stream.iterator().hasNext();
        } catch (IOException e) {
            throw new OpenIOOperationException("isEmptyDirectory", path.toString(),
                    "Failed to check if directory is empty", e);
        }
    }

    /**
     * Creates parent directories if they don't exist.
     * 如果父目录不存在则创建。
     *
     * @param path the file path | 文件路径
     * @return the path | 路径
     */
    public static Path createParentDirectories(Path path) {
        Objects.requireNonNull(path, "path must not be null");

        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new OpenIOOperationException("createDirectories", parent.toString(),
                        "Failed to create parent directories", e);
            }
        }
        return path;
    }

    /**
     * Gets file extension (with dot).
     * 获取文件扩展名（带点）。
     *
     * @param path the file path | 文件路径
     * @return the extension or empty string | 扩展名或空字符串
     */
    public static String getFileExtension(Path path) {
        Objects.requireNonNull(path, "path must not be null");

        String filename = path.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot) : "";
    }

    /**
     * Gets filename without extension.
     * 获取不带扩展名的文件名。
     *
     * @param path the file path | 文件路径
     * @return the filename without extension | 不带扩展名的文件名
     */
    public static String getNameWithoutExtension(Path path) {
        Objects.requireNonNull(path, "path must not be null");

        String filename = path.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    // ==================== File Tree Traversal Builder | 文件树遍历构建器 ====================

    /**
     * File Tree Traversal Builder
     * 文件树遍历构建器
     *
     * <p>Provides fluent API for configuring file tree traversal.</p>
     * <p>提供用于配置文件树遍历的流式 API。</p>
     */
    public static final class FileTreeTraversal {

        private final Path root;
        private int maxDepth = Integer.MAX_VALUE;
        private Set<FileVisitOption> options = EnumSet.noneOf(FileVisitOption.class);
        private Predicate<Path> filter = p -> true;
        private BiPredicate<Path, BasicFileAttributes> matcher = (p, a) -> true;

        private FileTreeTraversal(Path root) {
            this.root = Objects.requireNonNull(root, "root must not be null");
        }

        /**
         * Sets maximum depth to traverse.
         * 设置最大遍历深度。
         *
         * @param maxDepth maximum depth | 最大深度
         * @return this builder | 此构建器
         */
        public FileTreeTraversal maxDepth(int maxDepth) {
            if (maxDepth < 0) {
                throw new IllegalArgumentException("maxDepth must be non-negative");
            }
            this.maxDepth = maxDepth;
            return this;
        }

        /**
         * Follows symbolic links.
         * 跟随符号链接。
         *
         * @return this builder | 此构建器
         */
        public FileTreeTraversal followSymlinks() {
            this.options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            return this;
        }

        /**
         * Sets file filter.
         * 设置文件过滤器。
         *
         * @param filter the filter | 过滤器
         * @return this builder | 此构建器
         */
        public FileTreeTraversal filter(Predicate<Path> filter) {
            this.filter = Objects.requireNonNull(filter, "filter must not be null");
            return this;
        }

        /**
         * Sets BiPredicate matcher with file attributes.
         * 设置带文件属性的 BiPredicate 匹配器。
         *
         * @param matcher the matcher | 匹配器
         * @return this builder | 此构建器
         */
        public FileTreeTraversal matcher(BiPredicate<Path, BasicFileAttributes> matcher) {
            this.matcher = Objects.requireNonNull(matcher, "matcher must not be null");
            return this;
        }

        /**
         * Filters for regular files only.
         * 仅过滤常规文件。
         *
         * @return this builder | 此构建器
         */
        public FileTreeTraversal filesOnly() {
            this.filter = Files::isRegularFile;
            return this;
        }

        /**
         * Filters for directories only.
         * 仅过滤目录。
         *
         * @return this builder | 此构建器
         */
        public FileTreeTraversal directoriesOnly() {
            this.filter = Files::isDirectory;
            return this;
        }

        /**
         * Filters by glob pattern.
         * 按 glob 模式过滤。
         *
         * @param pattern the glob pattern | glob 模式
         * @return this builder | 此构建器
         */
        public FileTreeTraversal glob(String pattern) {
            Objects.requireNonNull(pattern, "pattern must not be null");
            PathMatcher pm = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            this.filter = p -> pm.matches(p.getFileName());
            return this;
        }

        /**
         * Returns stream of matching paths.
         * 返回匹配路径的流。
         *
         * @return stream of paths | 路径流
         */
        public Stream<Path> stream() {
            try {
                return Files.find(root, maxDepth, (path, attrs) ->
                        filter.test(path) && matcher.test(path, attrs), options.toArray(FileVisitOption[]::new));
            } catch (IOException e) {
                throw new OpenIOOperationException("fileTree", root.toString(),
                        "Failed to create file tree stream", e);
            }
        }

        /**
         * Collects all matching paths to a list.
         * 收集所有匹配路径到列表。
         *
         * @return list of paths | 路径列表
         */
        public List<Path> toList() {
            try (Stream<Path> stream = stream()) {
                return stream.toList();
            }
        }

        /**
         * Applies action to each matching path.
         * 对每个匹配路径应用操作。
         *
         * @param action the action | 操作
         */
        public void forEach(Consumer<Path> action) {
            try (Stream<Path> stream = stream()) {
                stream.forEach(action);
            }
        }

        /**
         * Counts matching paths.
         * 计算匹配路径的数量。
         *
         * @return count | 数量
         */
        public long count() {
            try (Stream<Path> stream = stream()) {
                return stream.count();
            }
        }
    }
}
