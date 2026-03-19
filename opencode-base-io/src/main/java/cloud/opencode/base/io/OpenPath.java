package cloud.opencode.base.io;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Path Utility Class
 * 路径工具类
 *
 * <p>Utility class for path operations including extension handling,
 * normalization, and path manipulation.</p>
 * <p>用于路径操作的工具类，包括扩展名处理、规范化和路径操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extension handling - 扩展名处理</li>
 *   <li>Path normalization - 路径规范化</li>
 *   <li>Path manipulation - 路径操作</li>
 *   <li>Unique file generation - 唯一文件生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get extension
 * String ext = OpenPath.getExtension(path); // "txt"
 *
 * // Get name without extension
 * String name = OpenPath.getNameWithoutExtension(path); // "file"
 *
 * // Change extension
 * Path newPath = OpenPath.changeExtension(path, "md");
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
public final class OpenPath {

    private OpenPath() {
    }

    /**
     * Gets the file extension
     * 获取文件扩展名
     *
     * @param path the file path | 文件路径
     * @return extension without dot, empty string if none | 不含点的扩展名，无扩展名返回空字符串
     */
    public static String getExtension(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return "";
        }
        return getExtension(fileName.toString());
    }

    /**
     * Gets the file extension from filename
     * 从文件名获取扩展名
     *
     * @param filename the filename | 文件名
     * @return extension without dot | 不含点的扩展名
     */
    public static String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1);
    }

    /**
     * Gets filename without extension
     * 获取不含扩展名的文件名
     *
     * @param path the file path | 文件路径
     * @return name without extension | 不含扩展名的文件名
     */
    public static String getNameWithoutExtension(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return "";
        }
        return getNameWithoutExtension(fileName.toString());
    }

    /**
     * Gets filename without extension from filename string
     * 从文件名字符串获取不含扩展名的文件名
     *
     * @param filename the filename | 文件名
     * @return name without extension | 不含扩展名的文件名
     */
    public static String getNameWithoutExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex <= 0) {
            return filename;
        }
        return filename.substring(0, dotIndex);
    }

    /**
     * Changes file extension
     * 更改文件扩展名
     *
     * @param path         the original path | 原路径
     * @param newExtension the new extension (without dot) | 新扩展名（不含点）
     * @return new path | 新路径
     */
    public static Path changeExtension(Path path, String newExtension) {
        String nameWithoutExt = getNameWithoutExtension(path);
        String newName = newExtension.isEmpty() ? nameWithoutExt : nameWithoutExt + "." + newExtension;
        Path parent = path.getParent();
        return parent != null ? parent.resolve(newName) : Path.of(newName);
    }

    /**
     * Normalizes path
     * 规范化路径
     *
     * @param path the path | 路径
     * @return normalized path | 规范化后的路径
     */
    public static Path normalize(Path path) {
        return path.normalize();
    }

    /**
     * Gets relative path
     * 获取相对路径
     *
     * @param base   the base path | 基准路径
     * @param target the target path | 目标路径
     * @return relative path | 相对路径
     */
    public static Path relativize(Path base, Path target) {
        return base.relativize(target);
    }

    /**
     * Resolves path
     * 解析路径
     *
     * @param base  the base path | 基准路径
     * @param other the other path | 其他路径
     * @return resolved path | 解析后的路径
     */
    public static Path resolve(Path base, String other) {
        return base.resolve(other);
    }

    /**
     * Gets parent directory
     * 获取父目录
     *
     * @param path the path | 路径
     * @return parent path | 父路径
     */
    public static Path getParent(Path path) {
        return path.getParent();
    }

    /**
     * Gets root path
     * 获取根路径
     *
     * @param path the path | 路径
     * @return root path | 根路径
     */
    public static Path getRoot(Path path) {
        return path.getRoot();
    }

    /**
     * Checks if path is absolute
     * 检查是否为绝对路径
     *
     * @param path the path | 路径
     * @return true if absolute | 如果是绝对路径返回true
     */
    public static boolean isAbsolute(Path path) {
        return path.isAbsolute();
    }

    /**
     * Converts to absolute path
     * 转换为绝对路径
     *
     * @param path the path | 路径
     * @return absolute path | 绝对路径
     */
    public static Path toAbsolute(Path path) {
        return path.toAbsolutePath();
    }

    /**
     * Gets real path (resolves symbolic links)
     * 获取真实路径（解析符号链接）
     *
     * @param path the path | 路径
     * @return real path | 真实路径
     */
    public static Path toRealPath(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException e) {
            throw OpenIOOperationException.invalidPath(path.toString(), e);
        }
    }

    /**
     * Checks if child is a sub path of parent
     * 检查子路径是否为父路径的子路径
     *
     * @param parent the parent path | 父路径
     * @param child  the child path | 子路径
     * @return true if sub path | 如果是子路径返回true
     */
    public static boolean isSubPath(Path parent, Path child) {
        Path normalizedParent = parent.toAbsolutePath().normalize();
        Path normalizedChild = child.toAbsolutePath().normalize();
        return normalizedChild.startsWith(normalizedParent);
    }

    /**
     * Generates a unique filename in directory
     * 在目录中生成唯一文件名
     *
     * @param dir       the directory | 目录
     * @param baseName  the base name | 基础名
     * @param extension the extension | 扩展名
     * @return unique file path | 唯一文件路径
     */
    public static Path uniqueFile(Path dir, String baseName, String extension) {
        String ext = extension.startsWith(".") ? extension : "." + extension;
        Path candidate = dir.resolve(baseName + ext);

        if (!Files.exists(candidate)) {
            return candidate;
        }

        int counter = 1;
        while (true) {
            candidate = dir.resolve(baseName + "_" + counter + ext);
            if (!Files.exists(candidate)) {
                return candidate;
            }
            counter++;
            if (counter > 10000) {
                throw new OpenIOOperationException("Failed to generate unique filename after 10000 attempts");
            }
        }
    }

    /**
     * Gets file name from path
     * 从路径获取文件名
     *
     * @param path the path | 路径
     * @return file name | 文件名
     */
    public static String getFileName(Path path) {
        Path fileName = path.getFileName();
        return fileName != null ? fileName.toString() : "";
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
     * Gets path name count
     * 获取路径名称数量
     *
     * @param path the path | 路径
     * @return name count | 名称数量
     */
    public static int getNameCount(Path path) {
        return path.getNameCount();
    }

    /**
     * Joins multiple path segments
     * 连接多个路径段
     *
     * @param first the first path | 第一个路径
     * @param more  the more paths | 更多路径
     * @return joined path | 连接后的路径
     */
    public static Path join(String first, String... more) {
        return Path.of(first, more);
    }

    /**
     * Joins path with more segments
     * 连接路径与更多段
     *
     * @param base the base path | 基准路径
     * @param more the more paths | 更多路径
     * @return joined path | 连接后的路径
     */
    public static Path join(Path base, String... more) {
        Path result = base;
        for (String segment : more) {
            result = result.resolve(segment);
        }
        return result;
    }
}
