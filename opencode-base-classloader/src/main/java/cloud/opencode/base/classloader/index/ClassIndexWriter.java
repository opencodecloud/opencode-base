package cloud.opencode.base.classloader.index;

import cloud.opencode.base.classloader.metadata.ClassMetadata;
import cloud.opencode.base.classloader.metadata.MetadataReader;
import cloud.opencode.base.classloader.scanner.ClassScanner;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/**
 * Class Index Writer - Generates a pre-built class index at build time
 * 类索引写入器 - 在构建时生成预构建的类索引
 *
 * <p>Scans specified packages using {@link ClassScanner} and {@link MetadataReader},
 * then writes a JSON index file to the output directory.</p>
 * <p>使用 {@link ClassScanner} 和 {@link MetadataReader} 扫描指定包，
 * 然后将 JSON 索引文件写入输出目录。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ClassIndexWriter.builder()
 *     .addPackage("com.example")
 *     .outputDir(Path.of("target/classes"))
 *     .generate();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder pattern, single-use) - 线程安全: 否 (构建器模式，一次性使用)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public final class ClassIndexWriter {

    private final List<String> packages = new ArrayList<>();
    private ClassLoader classLoader;
    private Path outputDir;

    private ClassIndexWriter() {
        // Use builder()
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return new builder instance | 新的构建器实例
     */
    public static ClassIndexWriter builder() {
        return new ClassIndexWriter();
    }

    /**
     * Add a package to scan
     * 添加要扫描的包
     *
     * @param packageName package name to scan | 要扫描的包名
     * @return this builder | 此构建器
     */
    public ClassIndexWriter addPackage(String packageName) {
        Objects.requireNonNull(packageName, "Package name must not be null");
        this.packages.add(packageName);
        return this;
    }

    /**
     * Set the class loader for scanning
     * 设置用于扫描的类加载器
     *
     * @param classLoader class loader to use | 要使用的类加载器
     * @return this builder | 此构建器
     */
    public ClassIndexWriter classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * Set the output directory for the index file
     * 设置索引文件的输出目录
     *
     * @param outputDir output directory path (e.g. target/classes) | 输出目录路径
     * @return this builder | 此构建器
     */
    public ClassIndexWriter outputDir(Path outputDir) {
        Objects.requireNonNull(outputDir, "Output directory must not be null");
        this.outputDir = outputDir;
        return this;
    }

    /**
     * Generate the class index and write it to the output directory
     * 生成类索引并写入输出目录
     *
     * @return the generated class index | 生成的类索引
     * @throws IOException if writing fails | 写入失败时抛出
     */
    public ClassIndex generate() throws IOException {
        if (packages.isEmpty()) {
            throw new IllegalStateException("At least one package must be specified");
        }
        if (outputDir == null) {
            throw new IllegalStateException("Output directory must be specified");
        }

        ClassLoader cl = this.classLoader;
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = ClassIndexWriter.class.getClassLoader();
            }
        }

        // Step a: Scan class names using ClassScanner
        ClassScanner scanner = ClassScanner.of(packages.toArray(String[]::new))
                .classLoader(cl)
                .includeInnerClasses(false);

        List<String> classNames = scanner.classNameStream().sorted().toList();

        // Step b: Build ClassIndexEntry for each class via MetadataReader
        List<ClassIndexEntry> entries = new ArrayList<>();
        for (String className : classNames) {
            try {
                ClassMetadata metadata = MetadataReader.read(className);
                entries.add(toEntry(metadata));
            } catch (Exception e) {
                // Skip classes that cannot be read
                System.getLogger(ClassIndexWriter.class.getName())
                        .log(System.Logger.Level.DEBUG, "Skipping class: " + className, e);
            }
        }

        // Step c: Compute classpath hash
        String classpathHash = computeClasspathHash();

        // Step d: Build ClassIndex record
        ClassIndex index = new ClassIndex(
                ClassIndex.CURRENT_VERSION,
                Instant.now().toString(),
                classpathHash,
                entries
        );

        // Step e: Write JSON to outputDir/META-INF/opencode/class-index.json
        Path indexFile = outputDir.resolve(ClassIndex.INDEX_LOCATION);
        Files.createDirectories(indexFile.getParent());
        String json = toJson(index);
        Files.writeString(indexFile, json, StandardCharsets.UTF_8);

        return index;
    }

    // ==================== Private Methods | 私有方法 ====================

    private static ClassIndexEntry toEntry(ClassMetadata metadata) {
        List<String> annotationNames = metadata.annotations().stream()
                .map(a -> a.annotationType())
                .toList();

        return new ClassIndexEntry(
                metadata.className(),
                metadata.superClassName(),
                metadata.interfaceNames(),
                annotationNames,
                metadata.modifiers(),
                metadata.isInterface(),
                metadata.isAbstract(),
                metadata.isEnum(),
                metadata.isRecord(),
                metadata.isSealed()
        );
    }

    /**
     * Compute a SHA-256 hash of the current classpath for staleness detection
     * 计算当前 classpath 的 SHA-256 哈希值用于陈旧检测
     *
     * @return hex-encoded SHA-256 hash | 十六进制编码的 SHA-256 哈希值
     */
    public static String computeClasspathHash() {
        String classpath = System.getProperty("java.class.path", "");
        if (classpath.isEmpty()) {
            return "";
        }

        String separator = System.getProperty("path.separator");
        String[] entries = classpath.split(separator.equals("|") ? "\\|" : java.util.regex.Pattern.quote(separator));
        Arrays.sort(entries);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String entry : entries) {
                Path path = Path.of(entry);
                long size = 0;
                long lastModified = 0;
                try {
                    if (Files.exists(path)) {
                        size = Files.size(path);
                        lastModified = Files.getLastModifiedTime(path).toMillis();
                    }
                } catch (IOException ignored) {
                    // Use defaults
                }
                String line = entry + ":" + size + ":" + lastModified;
                digest.update(line.getBytes(StandardCharsets.UTF_8));
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available
            throw new AssertionError("SHA-256 not available", e);
        }
    }

    /**
     * Serialize ClassIndex to JSON using StringBuilder (no external library).
     */
    static String toJson(ClassIndex index) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"version\": ").append(index.version()).append(",\n");
        sb.append("  \"timestamp\": \"").append(escapeJson(index.timestamp())).append("\",\n");
        sb.append("  \"classpathHash\": \"").append(escapeJson(index.classpathHash())).append("\",\n");
        sb.append("  \"entries\": [");

        List<ClassIndexEntry> entries = index.entries();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\n");
            appendEntry(sb, entries.get(i));
        }

        if (!entries.isEmpty()) {
            sb.append("\n  ");
        }
        sb.append("]\n");
        sb.append("}");
        return sb.toString();
    }

    private static void appendEntry(StringBuilder sb, ClassIndexEntry entry) {
        sb.append("    {\n");
        sb.append("      \"className\": \"").append(escapeJson(entry.className())).append("\",\n");
        sb.append("      \"superClassName\": ");
        if (entry.superClassName() != null) {
            sb.append("\"").append(escapeJson(entry.superClassName())).append("\"");
        } else {
            sb.append("null");
        }
        sb.append(",\n");
        sb.append("      \"interfaceNames\": ").append(toJsonArray(entry.interfaceNames())).append(",\n");
        sb.append("      \"annotationNames\": ").append(toJsonArray(entry.annotationNames())).append(",\n");
        sb.append("      \"modifiers\": ").append(entry.modifiers()).append(",\n");
        sb.append("      \"isInterface\": ").append(entry.isInterface()).append(",\n");
        sb.append("      \"isAbstract\": ").append(entry.isAbstract()).append(",\n");
        sb.append("      \"isEnum\": ").append(entry.isEnum()).append(",\n");
        sb.append("      \"isRecord\": ").append(entry.isRecord()).append(",\n");
        sb.append("      \"isSealed\": ").append(entry.isSealed()).append("\n");
        sb.append("    }");
    }

    private static String toJsonArray(List<String> items) {
        if (items.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(escapeJson(items.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    /**
     * Escape a string for JSON output
     * 为 JSON 输出转义字符串
     *
     * <p>Shared utility used across the classloader module to avoid code duplication.</p>
     * <p>跨 classloader 模块共享的工具方法，避免代码重复。</p>
     *
     * @param value input string | 输入字符串
     * @return escaped JSON string, or empty string if null | 转义后的 JSON 字符串，null 时返回空字符串
     */
    public static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append("\\u");
                        sb.append(HEX_DIGITS[(c >> 12) & 0xF]);
                        sb.append(HEX_DIGITS[(c >> 8) & 0xF]);
                        sb.append(HEX_DIGITS[(c >> 4) & 0xF]);
                        sb.append(HEX_DIGITS[c & 0xF]);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
