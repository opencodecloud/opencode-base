package cloud.opencode.base.classloader.conflict;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

/**
 * JAR version conflict detection utility
 * JAR 版本冲突检测工具
 *
 * <p>Scans JAR files to detect duplicate class definitions across multiple JARs.
 * This is useful for diagnosing classpath conflicts that can cause {@code NoSuchMethodError},
 * {@code ClassCastException}, and other runtime issues.</p>
 * <p>扫描 JAR 文件以检测多个 JAR 中的重复类定义。这对于诊断可能导致 {@code NoSuchMethodError}、
 * {@code ClassCastException} 和其他运行时问题的类路径冲突非常有用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Detect duplicate classes across JARs - 检测 JAR 间的重复类</li>
 *   <li>Scan directories for JAR files - 扫描目录中的 JAR 文件</li>
 *   <li>Extract version info from MANIFEST.MF - 从 MANIFEST.MF 提取版本信息</li>
 *   <li>Graceful handling of corrupt JARs - 优雅处理损坏的 JAR</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Detect conflicts between specific JARs
 * ConflictReport report = JarConflictDetector.detect(
 *     Path.of("/libs/guava-31.jar"),
 *     Path.of("/libs/guava-32.jar")
 * );
 *
 * // Detect conflicts in a directory
 * ConflictReport report = JarConflictDetector.detectInDirectory(Path.of("/libs"));
 *
 * if (report.hasConflicts()) {
 *     System.out.println(report.summary());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Path traversal protection for directory scanning - 目录扫描的路径遍历保护</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public final class JarConflictDetector {

    private static final System.Logger LOGGER = System.getLogger(JarConflictDetector.class.getName());

    private JarConflictDetector() {
        // Utility class
    }

    /**
     * Detect class conflicts across the specified JAR files
     * 检测指定 JAR 文件之间的类冲突
     *
     * @param jarPaths paths to JAR files to scan | 要扫描的 JAR 文件路径
     * @return conflict report | 冲突报告
     * @throws NullPointerException if jarPaths is null or contains null | 如果 jarPaths 为 null 或包含 null
     */
    public static ConflictReport detect(Path... jarPaths) {
        Objects.requireNonNull(jarPaths, "jarPaths must not be null");
        return detect(List.of(jarPaths));
    }

    /**
     * Detect class conflicts across the specified JAR files
     * 检测指定 JAR 文件之间的类冲突
     *
     * @param jarPaths collection of paths to JAR files to scan | 要扫描的 JAR 文件路径集合
     * @return conflict report | 冲突报告
     * @throws NullPointerException if jarPaths is null or contains null | 如果 jarPaths 为 null 或包含 null
     */
    public static ConflictReport detect(Collection<Path> jarPaths) {
        Objects.requireNonNull(jarPaths, "jarPaths must not be null");
        for (Path p : jarPaths) {
            Objects.requireNonNull(p, "JAR path must not be null");
        }

        // className → List<JarInfo>
        Map<String, List<JarInfo>> classToJars = new LinkedHashMap<>();
        int totalClasses = 0;

        for (Path jarPath : jarPaths) {
            try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                String version = extractVersion(jarFile);
                String name = jarPath.getFileName().toString();
                JarInfo jarInfo = new JarInfo(jarPath, version, name);

                List<String> classNames = extractClassNames(jarFile);
                totalClasses += classNames.size();

                for (String className : classNames) {
                    classToJars.computeIfAbsent(className, _ -> new ArrayList<>()).add(jarInfo);
                }
            } catch (IOException e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Skipping corrupt/invalid JAR: {0} ({1})", jarPath, e.getMessage());
            }
        }

        // Filter: keep only entries where class appears in 2+ JARs
        Map<String, List<JarInfo>> conflicts = new LinkedHashMap<>();
        for (var entry : classToJars.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.put(entry.getKey(), entry.getValue());
            }
        }

        return new ConflictReport(conflicts, conflicts.size(), totalClasses);
    }

    /**
     * Detect class conflicts among all JAR files in a directory
     * 检测目录中所有 JAR 文件之间的类冲突
     *
     * @param directory the directory to scan | 要扫描的目录
     * @return conflict report | 冲突报告
     * @throws NullPointerException if directory is null | 如果 directory 为 null
     */
    public static ConflictReport detectInDirectory(Path directory) {
        return detectInDirectory(directory, "*.jar");
    }

    /**
     * Detect class conflicts among JAR files matching a glob pattern in a directory
     * 检测目录中匹配 glob 模式的 JAR 文件之间的类冲突
     *
     * @param directory   the directory to scan | 要扫描的目录
     * @param globPattern glob pattern for filtering (e.g. "*.jar") | 用于过滤的 glob 模式
     * @return conflict report | 冲突报告
     * @throws NullPointerException if directory or globPattern is null | 如果 directory 或 globPattern 为 null
     */
    public static ConflictReport detectInDirectory(Path directory, String globPattern) {
        Objects.requireNonNull(directory, "directory must not be null");
        Objects.requireNonNull(globPattern, "globPattern must not be null");

        Path normalizedDir = directory.toAbsolutePath().normalize();

        if (!Files.isDirectory(normalizedDir)) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Not a directory: {0}", normalizedDir);
            return new ConflictReport(Map.of(), 0, 0);
        }

        List<Path> jarFiles = new ArrayList<>();
        PathMatcher matcher = normalizedDir.getFileSystem().getPathMatcher("glob:" + globPattern);

        try (Stream<Path> entries = Files.list(normalizedDir)) {
            entries.filter(Files::isRegularFile)
                    .filter(p -> {
                        // Path traversal protection: ensure file is within the directory
                        Path normalized = p.toAbsolutePath().normalize();
                        if (!normalized.startsWith(normalizedDir)) {
                            LOGGER.log(System.Logger.Level.WARNING,
                                    "Skipping path outside directory: {0}", p);
                            return false;
                        }
                        return true;
                    })
                    .filter(p -> {
                        // Case-insensitive .jar extension matching
                        String fileName = p.getFileName().toString();
                        if (fileName.toLowerCase(java.util.Locale.ROOT).endsWith(".jar")) {
                            return matcher.matches(p.getFileName());
                        }
                        return false;
                    })
                    .forEach(jarFiles::add);
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to list directory: {0}", normalizedDir);
            return new ConflictReport(Map.of(), 0, 0);
        }

        return detect(jarFiles);
    }

    /**
     * Extract version from MANIFEST.MF (Implementation-Version, then Bundle-Version)
     * 从 MANIFEST.MF 提取版本（先 Implementation-Version，再 Bundle-Version）
     *
     * @param jarFile the already-opened JAR file | 已打开的 JAR 文件
     * @return version string or null | 版本字符串或 null
     * @throws IOException if manifest cannot be read | 如果无法读取清单
     */
    private static String extractVersion(JarFile jarFile) throws IOException {
        Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            return null;
        }
        var attrs = manifest.getMainAttributes();
        if (attrs == null) {
            return null;
        }
        String version = attrs.getValue("Implementation-Version");
        if (version != null && !version.isBlank()) {
            return version.strip();
        }
        version = attrs.getValue("Bundle-Version");
        if (version != null && !version.isBlank()) {
            return version.strip();
        }
        return null;
    }

    /**
     * Extract class names from an already-opened JAR file
     * 从已打开的 JAR 文件提取类名
     *
     * @param jarFile the already-opened JAR file | 已打开的 JAR 文件
     * @return list of fully-qualified class names | 完全限定类名列表
     */
    private static List<String> extractClassNames(JarFile jarFile) {
        List<String> classNames = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            if (entry.isDirectory() || !entryName.endsWith(".class")) {
                continue;
            }
            if (entryName.startsWith("META-INF/")) {
                continue;
            }
            if (entryName.equals("module-info.class") || entryName.endsWith("/module-info.class")) {
                continue;
            }

            String className = entryName.substring(0, entryName.length() - ".class".length())
                    .replace('/', '.');
            classNames.add(className);
        }
        return classNames;
    }
}
