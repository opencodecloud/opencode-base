package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Set;

/**
 * File Copier Utility
 * 文件复制器
 *
 * <p>Fluent API for copying files and directories with various options.
 * Supports recursive copying and file attribute preservation.</p>
 * <p>用于复制文件和目录的流式API。
 * 支持递归复制和文件属性保留。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API - 流式API</li>
 *   <li>Recursive copy - 递归复制</li>
 *   <li>Attribute preservation - 属性保留</li>
 *   <li>Replace existing option - 替换现有文件选项</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Copy file
 * FileCopier.copy(source, target);
 *
 * // Copy with replace
 * FileCopier.from(source).to(target).replaceExisting().execute();
 *
 * // Copy directory recursively
 * FileCopier.from(sourceDir).to(targetDir).recursive().execute();
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
public final class FileCopier {

    private final Path source;
    private Path target;
    private boolean replaceExisting = false;
    private boolean copyAttributes = false;
    private boolean recursive = false;

    private FileCopier(Path source) {
        this.source = source;
    }

    /**
     * Creates a file copier from source
     * 从源创建文件复制器
     *
     * @param source the source path | 源路径
     * @return file copier | 文件复制器
     */
    public static FileCopier from(Path source) {
        return new FileCopier(source);
    }

    /**
     * Creates a file copier from source string
     * 从源字符串创建文件复制器
     *
     * @param source the source path string | 源路径字符串
     * @return file copier | 文件复制器
     */
    public static FileCopier from(String source) {
        return new FileCopier(Path.of(source));
    }

    /**
     * Copies a file directly
     * 直接复制文件
     *
     * @param source the source path | 源路径
     * @param target the target path | 目标路径
     * @return target path | 目标路径
     */
    public static Path copy(Path source, Path target) {
        return from(source).to(target).execute();
    }

    /**
     * Copies a file with replace option
     * 使用替换选项复制文件
     *
     * @param source the source path | 源路径
     * @param target the target path | 目标路径
     * @return target path | 目标路径
     */
    public static Path copyReplace(Path source, Path target) {
        return from(source).to(target).replaceExisting().execute();
    }

    /**
     * Sets the target path
     * 设置目标路径
     *
     * @param target the target path | 目标路径
     * @return this | 当前对象
     */
    public FileCopier to(Path target) {
        this.target = target;
        return this;
    }

    /**
     * Sets the target path string
     * 设置目标路径字符串
     *
     * @param target the target path string | 目标路径字符串
     * @return this | 当前对象
     */
    public FileCopier to(String target) {
        this.target = Path.of(target);
        return this;
    }

    /**
     * Enables replace existing
     * 启用替换现有文件
     *
     * @return this | 当前对象
     */
    public FileCopier replaceExisting() {
        this.replaceExisting = true;
        return this;
    }

    /**
     * Enables copy attributes
     * 启用复制属性
     *
     * @return this | 当前对象
     */
    public FileCopier copyAttributes() {
        this.copyAttributes = true;
        return this;
    }

    /**
     * Enables recursive copy for directories
     * 启用目录递归复制
     *
     * @return this | 当前对象
     */
    public FileCopier recursive() {
        this.recursive = true;
        return this;
    }

    /**
     * Executes the copy operation
     * 执行复制操作
     *
     * @return target path | 目标路径
     */
    public Path execute() {
        if (target == null) {
            throw new IllegalStateException("Target path not set");
        }

        try {
            if (Files.isDirectory(source) && recursive) {
                copyDirectoryRecursively();
            } else {
                copyFile();
            }
            return target;
        } catch (IOException e) {
            throw OpenIOOperationException.copyFailed(source, target, e);
        }
    }

    private void copyFile() throws IOException {
        // Ensure parent directories exist
        Path parent = target.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        CopyOption[] options = getCopyOptions();
        Files.copy(source, target, options);
    }

    private void copyDirectoryRecursively() throws IOException {
        Set<FileVisitOption> visitOptions = EnumSet.noneOf(FileVisitOption.class);
        CopyOption[] copyOptions = getCopyOptions();

        Files.walkFileTree(source, visitOptions, Integer.MAX_VALUE, new SimpleFileVisitor<>() {
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
                Files.copy(file, targetFile, copyOptions);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private CopyOption[] getCopyOptions() {
        if (replaceExisting && copyAttributes) {
            return new CopyOption[]{StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES};
        } else if (replaceExisting) {
            return new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
        } else if (copyAttributes) {
            return new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};
        }
        return new CopyOption[0];
    }

    /**
     * Gets the source path
     * 获取源路径
     *
     * @return source path | 源路径
     */
    public Path getSource() {
        return source;
    }

    /**
     * Gets the target path
     * 获取目标路径
     *
     * @return target path | 目标路径
     */
    public Path getTarget() {
        return target;
    }
}
