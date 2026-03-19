package cloud.opencode.base.io;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * File and Directory Operations Utility Class
 * 文件和目录操作工具类
 *
 * <p>Primary facade for file and directory operations based on NIO.2 API.
 * Provides convenient methods for common IO operations with unchecked exceptions.</p>
 * <p>基于NIO.2 API的文件和目录操作主门面类。
 * 提供便捷的常用IO操作方法，使用非受检异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>File reading and writing - 文件读写</li>
 *   <li>File and directory operations - 文件和目录操作</li>
 *   <li>Directory traversal - 目录遍历</li>
 *   <li>Pattern matching (glob/regex) - 模式匹配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Read file
 * String content = OpenIO.readString(path);
 * List<String> lines = OpenIO.readLines(path);
 *
 * // Write file
 * OpenIO.writeString(path, "Hello World");
 * OpenIO.writeLines(path, List.of("line1", "line2"));
 *
 * // File operations
 * OpenIO.copy(source, target);
 * OpenIO.deleteRecursively(dir);
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
public final class OpenIO {

    private OpenIO() {
    }

    // ==================== Read Operations | 读取操作 ====================

    /**
     * Reads file as byte array
     * 读取文件为字节数组
     *
     * @param path the file path | 文件路径
     * @return byte array | 字节数组
     */
    public static byte[] readBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Reads file as string
     * 读取文件为字符串
     *
     * @param path    the file path | 文件路径
     * @param charset the charset | 字符集
     * @return content string | 内容字符串
     */
    public static String readString(Path path, Charset charset) {
        try {
            return Files.readString(path, charset);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Reads file as UTF-8 string
     * 读取文件为UTF-8字符串
     *
     * @param path the file path | 文件路径
     * @return content string | 内容字符串
     */
    public static String readString(Path path) {
        return readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Reads file as list of lines
     * 读取文件为行列表
     *
     * @param path    the file path | 文件路径
     * @param charset the charset | 字符集
     * @return list of lines | 行列表
     */
    public static List<String> readLines(Path path, Charset charset) {
        try {
            return Files.readAllLines(path, charset);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Reads file as list of lines (UTF-8)
     * 读取文件为行列表（UTF-8）
     *
     * @param path the file path | 文件路径
     * @return list of lines | 行列表
     */
    public static List<String> readLines(Path path) {
        return readLines(path, StandardCharsets.UTF_8);
    }

    /**
     * Returns a stream of lines from file
     * 返回文件的行流
     *
     * @param path    the file path | 文件路径
     * @param charset the charset | 字符集
     * @return stream of lines | 行流
     */
    public static Stream<String> lines(Path path, Charset charset) {
        try {
            return Files.lines(path, charset);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Returns a stream of lines from file (UTF-8)
     * 返回文件的行流（UTF-8）
     *
     * @param path the file path | 文件路径
     * @return stream of lines | 行流
     */
    public static Stream<String> lines(Path path) {
        return lines(path, StandardCharsets.UTF_8);
    }

    /**
     * Reads the first line of file
     * 读取文件首行
     *
     * @param path the file path | 文件路径
     * @return first line or null | 首行或null
     */
    public static String readFirstLine(Path path) {
        try (Stream<String> lines = lines(path)) {
            return lines.findFirst().orElse(null);
        }
    }

    // ==================== Write Operations | 写入操作 ====================

    /**
     * Writes byte array to file
     * 写入字节数组到文件
     *
     * @param path    the file path | 文件路径
     * @param bytes   the byte array | 字节数组
     * @param options the open options | 打开选项
     */
    public static void writeBytes(Path path, byte[] bytes, OpenOption... options) {
        try {
            Files.write(path, bytes, options);
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(path, e);
        }
    }

    /**
     * Writes string to file
     * 写入字符串到文件
     *
     * @param path    the file path | 文件路径
     * @param content the content | 内容
     * @param charset the charset | 字符集
     * @param options the open options | 打开选项
     */
    public static void writeString(Path path, CharSequence content, Charset charset, OpenOption... options) {
        try {
            Files.writeString(path, content, charset, options);
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(path, e);
        }
    }

    /**
     * Writes string to file (UTF-8)
     * 写入字符串到文件（UTF-8）
     *
     * @param path    the file path | 文件路径
     * @param content the content | 内容
     * @param options the open options | 打开选项
     */
    public static void writeString(Path path, CharSequence content, OpenOption... options) {
        writeString(path, content, StandardCharsets.UTF_8, options);
    }

    /**
     * Writes lines to file
     * 写入行到文件
     *
     * @param path    the file path | 文件路径
     * @param lines   the lines | 行
     * @param charset the charset | 字符集
     * @param options the open options | 打开选项
     */
    public static void writeLines(Path path, Iterable<? extends CharSequence> lines, Charset charset, OpenOption... options) {
        try {
            Files.write(path, lines, charset, options);
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(path, e);
        }
    }

    /**
     * Writes lines to file (UTF-8)
     * 写入行到文件（UTF-8）
     *
     * @param path    the file path | 文件路径
     * @param lines   the lines | 行
     * @param options the open options | 打开选项
     */
    public static void writeLines(Path path, Iterable<? extends CharSequence> lines, OpenOption... options) {
        writeLines(path, lines, StandardCharsets.UTF_8, options);
    }

    /**
     * Appends content to file
     * 追加内容到文件
     *
     * @param path    the file path | 文件路径
     * @param content the content | 内容
     */
    public static void append(Path path, CharSequence content) {
        writeString(path, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Appends lines to file
     * 追加行到文件
     *
     * @param path  the file path | 文件路径
     * @param lines the lines | 行
     */
    public static void appendLines(Path path, Iterable<? extends CharSequence> lines) {
        writeLines(path, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    // ==================== File Operations | 文件操作 ====================

    /**
     * Checks if path exists
     * 检查路径是否存在
     *
     * @param path    the path | 路径
     * @param options the link options | 链接选项
     * @return true if exists | 如果存在返回true
     */
    public static boolean exists(Path path, LinkOption... options) {
        return Files.exists(path, options);
    }

    /**
     * Checks if path is a file
     * 检查路径是否为文件
     *
     * @param path the path | 路径
     * @return true if file | 如果是文件返回true
     */
    public static boolean isFile(Path path) {
        return Files.isRegularFile(path);
    }

    /**
     * Checks if path is a directory
     * 检查路径是否为目录
     *
     * @param path the path | 路径
     * @return true if directory | 如果是目录返回true
     */
    public static boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    /**
     * Checks if directory is empty
     * 检查目录是否为空
     *
     * @param path the directory path | 目录路径
     * @return true if empty | 如果为空返回true
     */
    public static boolean isEmptyDirectory(Path path) {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            return !ds.iterator().hasNext();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if path is a symbolic link
     * 检查路径是否为符号链接
     *
     * @param path the path | 路径
     * @return true if symbolic link | 如果是符号链接返回true
     */
    public static boolean isSymbolicLink(Path path) {
        return Files.isSymbolicLink(path);
    }

    /**
     * Checks if path is hidden
     * 检查路径是否隐藏
     *
     * @param path the path | 路径
     * @return true if hidden | 如果隐藏返回true
     */
    public static boolean isHidden(Path path) {
        try {
            return Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if path is readable
     * 检查路径是否可读
     *
     * @param path the path | 路径
     * @return true if readable | 如果可读返回true
     */
    public static boolean isReadable(Path path) {
        return Files.isReadable(path);
    }

    /**
     * Checks if path is writable
     * 检查路径是否可写
     *
     * @param path the path | 路径
     * @return true if writable | 如果可写返回true
     */
    public static boolean isWritable(Path path) {
        return Files.isWritable(path);
    }

    /**
     * Checks if two paths point to the same file
     * 检查两个路径是否指向同一文件
     *
     * @param path1 the first path | 第一个路径
     * @param path2 the second path | 第二个路径
     * @return true if same file | 如果是同一文件返回true
     */
    public static boolean isSameFile(Path path1, Path path2) {
        try {
            return Files.isSameFile(path1, path2);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Creates a file
     * 创建文件
     *
     * @param path the file path | 文件路径
     * @return created path | 创建的路径
     */
    public static Path createFile(Path path) {
        try {
            return Files.createFile(path);
        } catch (IOException e) {
            throw OpenIOOperationException.createFileFailed(path, e);
        }
    }

    /**
     * Creates a directory
     * 创建目录
     *
     * @param path the directory path | 目录路径
     * @return created path | 创建的路径
     */
    public static Path createDirectory(Path path) {
        try {
            return Files.createDirectory(path);
        } catch (IOException e) {
            throw OpenIOOperationException.createDirectoryFailed(path, e);
        }
    }

    /**
     * Creates directories including parents
     * 创建目录（包含父目录）
     *
     * @param path the directory path | 目录路径
     * @return created path | 创建的路径
     */
    public static Path createDirectories(Path path) {
        try {
            return Files.createDirectories(path);
        } catch (IOException e) {
            throw OpenIOOperationException.createDirectoryFailed(path, e);
        }
    }

    /**
     * Deletes a file or empty directory
     * 删除文件或空目录
     *
     * @param path the path | 路径
     */
    public static void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw OpenIOOperationException.deleteFailed(path, e);
        }
    }

    /**
     * Deletes file if exists
     * 删除文件（如果存在）
     *
     * @param path the path | 路径
     * @return true if deleted | 如果删除成功返回true
     */
    public static boolean deleteIfExists(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            throw OpenIOOperationException.deleteFailed(path, e);
        }
    }

    /**
     * Deletes directory recursively
     * 递归删除目录
     *
     * @param path the directory path | 目录路径
     */
    public static void deleteRecursively(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try {
            Files.walkFileTree(path, OpenFileVisitors.deleteVisitor());
        } catch (IOException e) {
            throw OpenIOOperationException.deleteFailed(path, e);
        }
    }

    /**
     * Copies a file
     * 复制文件
     *
     * @param source  the source path | 源路径
     * @param target  the target path | 目标路径
     * @param options the copy options | 复制选项
     * @return target path | 目标路径
     */
    public static Path copy(Path source, Path target, CopyOption... options) {
        try {
            return Files.copy(source, target, options);
        } catch (IOException e) {
            throw OpenIOOperationException.copyFailed(source, target, e);
        }
    }

    /**
     * Copies directory recursively
     * 递归复制目录
     *
     * @param source  the source directory | 源目录
     * @param target  the target directory | 目标目录
     * @param options the copy options | 复制选项
     */
    public static void copyRecursively(Path source, Path target, CopyOption... options) {
        try {
            Files.walkFileTree(source, OpenFileVisitors.copyVisitor(source, target, options));
        } catch (IOException e) {
            throw OpenIOOperationException.copyFailed(source, target, e);
        }
    }

    /**
     * Moves a file
     * 移动文件
     *
     * @param source  the source path | 源路径
     * @param target  the target path | 目标路径
     * @param options the copy options | 复制选项
     * @return target path | 目标路径
     */
    public static Path move(Path source, Path target, CopyOption... options) {
        try {
            return Files.move(source, target, options);
        } catch (IOException e) {
            throw OpenIOOperationException.moveFailed(source, target, e);
        }
    }

    /**
     * Gets file size
     * 获取文件大小
     *
     * @param path the file path | 文件路径
     * @return size in bytes | 字节大小
     */
    public static long size(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Gets directory size recursively
     * 递归获取目录大小
     *
     * @param path the directory path | 目录路径
     * @return total size in bytes | 总字节大小
     */
    public static long directorySize(Path path) {
        try {
            OpenFileVisitors.SizeVisitor visitor = OpenFileVisitors.sizeVisitor();
            Files.walkFileTree(path, visitor);
            return visitor.getTotalSize();
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Gets last modified time
     * 获取最后修改时间
     *
     * @param path the path | 路径
     * @return last modified time | 最后修改时间
     */
    public static Instant getLastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    /**
     * Sets last modified time
     * 设置最后修改时间
     *
     * @param path the path | 路径
     * @param time the time | 时间
     */
    public static void setLastModifiedTime(Path path, Instant time) {
        try {
            Files.setLastModifiedTime(path, FileTime.from(time));
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(path, e);
        }
    }

    // ==================== Directory Traversal | 目录遍历 ====================

    /**
     * Lists directory contents
     * 列出目录内容
     *
     * @param dir the directory path | 目录路径
     * @return stream of paths | 路径流
     */
    public static Stream<Path> list(Path dir) {
        try {
            return Files.list(dir);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(dir, e);
        }
    }

    /**
     * Walks directory tree
     * 遍历目录树
     *
     * @param start    the start directory | 起始目录
     * @param maxDepth the max depth | 最大深度
     * @return stream of paths | 路径流
     */
    public static Stream<Path> walk(Path start, int maxDepth) {
        try {
            return Files.walk(start, maxDepth);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(start, e);
        }
    }

    /**
     * Walks directory tree with unlimited depth
     * 遍历目录树（无深度限制）
     *
     * @param start the start directory | 起始目录
     * @return stream of paths | 路径流
     */
    public static Stream<Path> walk(Path start) {
        return walk(start, Integer.MAX_VALUE);
    }

    /**
     * Finds files matching glob pattern
     * 查找匹配glob模式的文件
     *
     * @param start   the start directory | 起始目录
     * @param pattern the glob pattern | glob模式
     * @return stream of matching paths | 匹配的路径流
     */
    public static Stream<Path> glob(Path start, String pattern) {
        PathMatcher matcher = start.getFileSystem().getPathMatcher("glob:" + pattern);
        return walk(start).filter(p -> matcher.matches(p.getFileName()));
    }

    /**
     * Finds files matching regex pattern
     * 查找匹配正则表达式的文件
     *
     * @param start the start directory | 起始目录
     * @param regex the regex pattern | 正则表达式
     * @return stream of matching paths | 匹配的路径流
     */
    public static Stream<Path> find(Path start, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return walk(start).filter(p -> {
            Path fileName = p.getFileName();
            return fileName != null && pattern.matcher(fileName.toString()).matches();
        });
    }

    /**
     * Finds files with custom matcher
     * 使用自定义匹配器查找文件
     *
     * @param start    the start directory | 起始目录
     * @param maxDepth the max depth | 最大深度
     * @param matcher  the matcher | 匹配器
     * @return stream of matching paths | 匹配的路径流
     */
    public static Stream<Path> find(Path start, int maxDepth, java.util.function.BiPredicate<Path, BasicFileAttributes> matcher) {
        try {
            return Files.find(start, maxDepth, matcher);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(start, e);
        }
    }
}
