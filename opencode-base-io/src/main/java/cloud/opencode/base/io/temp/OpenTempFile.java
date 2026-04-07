package cloud.opencode.base.io.temp;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;

/**
 * Temp File Utility Class
 * 临时文件工具类
 *
 * <p>Utility class for creating and managing temporary files and directories.
 * Provides convenient methods for temp file operations.</p>
 * <p>用于创建和管理临时文件和目录的工具类。
 * 提供便捷的临时文件操作方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create temp files and directories - 创建临时文件和目录</li>
 *   <li>Auto-delete temp files - 自动删除临时文件</li>
 *   <li>Cleanup old temp files - 清理过期临时文件</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple temp file
 * Path temp = OpenTempFile.createTempFile("data", ".tmp");
 *
 * // Auto-delete temp file
 * try (AutoDeleteTempFile temp = OpenTempFile.createAutoDeleteTempFile("data", ".json")) {
 *     temp.write(content);
 * } // Automatically deleted
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
public final class OpenTempFile {

    private OpenTempFile() {
    }

    /**
     * Creates a temporary file
     * 创建临时文件
     *
     * @param prefix the prefix | 前缀
     * @param suffix the suffix | 后缀
     * @return temp file path | 临时文件路径
     */
    public static Path createTempFile(String prefix, String suffix) {
        try {
            return Files.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to create temp file", e);
        }
    }

    /**
     * Creates a temporary file in specified directory
     * 在指定目录创建临时文件
     *
     * @param dir    the directory | 目录
     * @param prefix the prefix | 前缀
     * @param suffix the suffix | 后缀
     * @return temp file path | 临时文件路径
     */
    public static Path createTempFile(Path dir, String prefix, String suffix) {
        try {
            return Files.createTempFile(dir, prefix, suffix);
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to create temp file in: " + dir, e);
        }
    }

    /**
     * Creates a temporary directory
     * 创建临时目录
     *
     * @param prefix the prefix | 前缀
     * @return temp directory path | 临时目录路径
     */
    public static Path createTempDirectory(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to create temp directory", e);
        }
    }

    /**
     * Creates a temporary directory in specified parent
     * 在指定父目录创建临时目录
     *
     * @param dir    the parent directory | 父目录
     * @param prefix the prefix | 前缀
     * @return temp directory path | 临时目录路径
     */
    public static Path createTempDirectory(Path dir, String prefix) {
        try {
            return Files.createTempDirectory(dir, prefix);
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to create temp directory in: " + dir, e);
        }
    }

    /**
     * Creates an auto-delete temporary file
     * 创建自动删除临时文件
     *
     * @param prefix the prefix | 前缀
     * @param suffix the suffix | 后缀
     * @return auto delete temp file | 自动删除临时文件
     */
    public static AutoDeleteTempFile createAutoDeleteTempFile(String prefix, String suffix) {
        Path path = createTempFile(prefix, suffix);
        return new AutoDeleteTempFile(path);
    }

    /**
     * Creates an auto-delete temporary file in specified directory
     * 在指定目录创建自动删除临时文件
     *
     * @param dir    the directory | 目录
     * @param prefix the prefix | 前缀
     * @param suffix the suffix | 后缀
     * @return auto delete temp file | 自动删除临时文件
     */
    public static AutoDeleteTempFile createAutoDeleteTempFile(Path dir, String prefix, String suffix) {
        Path path = createTempFile(dir, prefix, suffix);
        return new AutoDeleteTempFile(path);
    }

    /**
     * Creates a temporary file from input stream
     * 从输入流创建临时文件
     *
     * @param input  the input stream | 输入流
     * @param prefix the prefix | 前缀
     * @param suffix the suffix | 后缀
     * @return temp file path | 临时文件路径
     */
    public static Path createTempFileFromStream(InputStream input, String prefix, String suffix) {
        Path path = createTempFile(prefix, suffix);
        try {
            Files.copy(input, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return path;
        } catch (IOException e) {
            // Cleanup on failure
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
            throw new OpenIOOperationException("Failed to create temp file from stream", e);
        }
    }

    /**
     * Gets the system temp directory
     * 获取系统临时目录
     *
     * @return temp directory path | 临时目录路径
     */
    public static Path getTempDirectory() {
        return Path.of(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Cleans up old temporary files
     * 清理过期临时文件
     *
     * @param dir     the directory | 目录
     * @param maxAge  the maximum age | 最大存活时间
     * @param pattern the file name pattern (glob) | 文件名模式（glob）
     * @return number of files deleted | 删除的文件数
     */
    public static int cleanupOldTempFiles(Path dir, Duration maxAge, String pattern) {
        if (!Files.isDirectory(dir)) {
            return 0;
        }

        Instant cutoff = Instant.now().minus(maxAge);
        int deleted = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, pattern)) {
            for (Path file : stream) {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                    if (attrs.lastModifiedTime().toInstant().isBefore(cutoff)) {
                        if (Files.deleteIfExists(file)) {
                            deleted++;
                        }
                    }
                } catch (IOException e) {
                    // Ignore errors for individual files
                }
            }
        } catch (IOException e) {
            // Ignore directory access errors
        }

        return deleted;
    }

    /**
     * Cleans up all files matching pattern in temp directory
     * 清理临时目录中匹配模式的所有文件
     *
     * @param maxAge  the maximum age | 最大存活时间
     * @param pattern the file name pattern (glob) | 文件名模式（glob）
     * @return number of files deleted | 删除的文件数
     */
    public static int cleanupOldTempFiles(Duration maxAge, String pattern) {
        return cleanupOldTempFiles(getTempDirectory(), maxAge, pattern);
    }

    /**
     * Checks if a path is in the temp directory
     * 检查路径是否在临时目录中
     *
     * @param path the path | 路径
     * @return true if in temp dir | 如果在临时目录中返回true
     */
    public static boolean isInTempDirectory(Path path) {
        try {
            Path tempDir = getTempDirectory().toRealPath();
            Path realPath = path.toRealPath();
            return realPath.startsWith(tempDir);
        } catch (IOException e) {
            return false;
        }
    }
}
