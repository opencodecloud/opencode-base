package cloud.opencode.base.classloader.index;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class Index Reader - Loads a pre-built class index from the classpath
 * 类索引读取器 - 从 classpath 加载预构建的类索引
 *
 * <p>Reads the JSON index file produced by {@link ClassIndexWriter} and
 * validates it against the current classpath hash for staleness detection.</p>
 * <p>读取由 {@link ClassIndexWriter} 生成的 JSON 索引文件，
 * 并通过当前 classpath 哈希值进行陈旧检测验证。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Optional<ClassIndex> index = ClassIndexReader.load();
 * if (index.isPresent() && ClassIndexReader.isValid(index.get())) {
 *     // use index
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
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public final class ClassIndexReader {

    private static final System.Logger LOGGER = System.getLogger(ClassIndexReader.class.getName());

    /**
     * Per-ClassLoader cache for the loaded index.
     * Different ClassLoaders may have different class-index.json files (e.g. OSGi, multi-WAR).
     * 每个 ClassLoader 的索引缓存。
     * 不同 ClassLoader 可能有不同的 class-index.json 文件（如 OSGi、多 WAR 环境）。
     */
    private static final ConcurrentHashMap<ClassLoader, ClassIndex> indexCache = new ConcurrentHashMap<>();
    private static final Object CACHE_LOCK = new Object();

    private ClassIndexReader() {
        // Utility class
    }

    /**
     * Load the class index from the default classpath location using the context class loader
     * 使用上下文类加载器从默认 classpath 位置加载类索引
     *
     * @return the loaded index, or empty if not found or version incompatible | 加载的索引，未找到或版本不兼容则返回 empty
     */
    public static Optional<ClassIndex> load() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassIndexReader.class.getClassLoader();
        }
        return load(cl);
    }

    /**
     * Load the class index from the default classpath location using the specified class loader
     * 使用指定的类加载器从默认 classpath 位置加载类索引
     *
     * @param classLoader class loader to use for resource lookup | 用于资源查找的类加载器
     * @return the loaded index, or empty if not found or version incompatible | 加载的索引，未找到或版本不兼容则返回 empty
     */
    public static Optional<ClassIndex> load(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "ClassLoader must not be null");

        // Check per-ClassLoader cache first (lock-free fast path)
        ClassIndex cached = indexCache.get(classLoader);
        if (cached != null) {
            return Optional.of(cached);
        }

        synchronized (CACHE_LOCK) {
            // Re-check inside lock
            cached = indexCache.get(classLoader);
            if (cached != null) {
                return Optional.of(cached);
            }

            try {
                URL url = classLoader.getResource(ClassIndex.INDEX_LOCATION);
                if (url == null) {
                    LOGGER.log(System.Logger.Level.DEBUG, "Class index not found at: " + ClassIndex.INDEX_LOCATION);
                    return Optional.empty();
                }

                String json;
                try (InputStream is = url.openStream()) {
                    json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }

                ClassIndex index = fromJson(json);

                // Version check
                if (index.version() != ClassIndex.CURRENT_VERSION) {
                    LOGGER.log(System.Logger.Level.INFO,
                            "Class index version mismatch: expected " + ClassIndex.CURRENT_VERSION
                                    + ", found " + index.version());
                    return Optional.empty();
                }

                indexCache.put(classLoader, index);
                return Optional.of(index);

            } catch (IOException e) {
                LOGGER.log(System.Logger.Level.WARNING, "Failed to load class index", e);
                return Optional.empty();
            }
        }
    }

    /**
     * Check whether the index is still valid by comparing its classpath hash with the current one.
     * If invalid, the cached index is automatically cleared so that subsequent {@link #load()} calls
     * will re-read from classpath.
     * 通过比较 classpath 哈希值与当前值来检查索引是否仍然有效。
     * 若无效，自动清除缓存索引，以便后续 {@link #load()} 调用重新从 classpath 读取。
     *
     * @param index the class index to validate | 要验证的类索引
     * @return true if the classpath hash matches | 如果 classpath 哈希值匹配则返回 true
     */
    public static boolean isValid(ClassIndex index) {
        Objects.requireNonNull(index, "ClassIndex must not be null");
        String currentHash = ClassIndexWriter.computeClasspathHash();
        boolean valid = index.classpathHash().equals(currentHash);
        if (!valid) {
            invalidateCache();
        }
        return valid;
    }

    /**
     * Invalidate the singleton cache, forcing the next {@link #load()} call to re-read the index
     * from classpath. Useful when the classpath changes at runtime (e.g. hot deployment).
     * 使单例缓存失效，强制下一次 {@link #load()} 调用从 classpath 重新读取索引。
     * 在运行时 classpath 变化（如热部署）时有用。
     */
    public static void invalidateCache() {
        indexCache.clear();
    }

    /**
     * Clear the singleton cache (useful for testing)
     * 清除单例缓存（便于测试）
     *
     * @deprecated Use {@link #invalidateCache()} instead
     */
    @Deprecated(since = "1.0.3", forRemoval = true)
    public static void clearCache() {
        invalidateCache();
    }

    // ==================== JSON Parsing | JSON 解析 ====================

    /**
     * Parse a ClassIndex from JSON string.
     * Minimal hand-written parser — no external library needed.
     */
    static ClassIndex fromJson(String json) {
        int version = 0;
        String timestamp = "";
        String classpathHash = "";
        List<ClassIndexEntry> entries = new ArrayList<>();

        // Extract top-level fields
        version = parseIntField(json, "version");
        timestamp = parseStringField(json, "timestamp");
        classpathHash = parseStringField(json, "classpathHash");

        // Extract entries array
        int entriesStart = json.indexOf("\"entries\"");
        if (entriesStart >= 0) {
            int arrayStart = json.indexOf('[', entriesStart);
            int arrayEnd = findMatchingBracket(json, arrayStart);
            if (arrayStart >= 0 && arrayEnd >= 0) {
                String arrayContent = json.substring(arrayStart + 1, arrayEnd);
                entries = parseEntries(arrayContent);
            }
        }

        return new ClassIndex(version, timestamp, classpathHash, entries);
    }

    private static List<ClassIndexEntry> parseEntries(String arrayContent) {
        List<ClassIndexEntry> entries = new ArrayList<>();
        int pos = 0;

        while (pos < arrayContent.length()) {
            int objStart = arrayContent.indexOf('{', pos);
            if (objStart < 0) break;
            int objEnd = findMatchingBrace(arrayContent, objStart);
            if (objEnd < 0) break;

            String objJson = arrayContent.substring(objStart, objEnd + 1);
            entries.add(parseEntry(objJson));
            pos = objEnd + 1;
        }

        return entries;
    }

    private static ClassIndexEntry parseEntry(String json) {
        String className = parseStringField(json, "className");
        String superClassName = parseNullableStringField(json, "superClassName");
        List<String> interfaceNames = parseStringArrayField(json, "interfaceNames");
        List<String> annotationNames = parseStringArrayField(json, "annotationNames");
        int modifiers = parseIntField(json, "modifiers");
        boolean isInterface = parseBooleanField(json, "isInterface");
        boolean isAbstract = parseBooleanField(json, "isAbstract");
        boolean isEnum = parseBooleanField(json, "isEnum");
        boolean isRecord = parseBooleanField(json, "isRecord");
        boolean isSealed = parseBooleanField(json, "isSealed");

        return new ClassIndexEntry(className, superClassName, interfaceNames, annotationNames,
                modifiers, isInterface, isAbstract, isEnum, isRecord, isSealed);
    }

    private static String parseStringField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return "";
        int colonIdx = json.indexOf(':', idx + key.length());
        if (colonIdx < 0) return "";
        int quoteStart = json.indexOf('"', colonIdx + 1);
        if (quoteStart < 0) return "";
        int quoteEnd = findClosingQuote(json, quoteStart + 1);
        if (quoteEnd < 0) return "";
        return unescapeJson(json.substring(quoteStart + 1, quoteEnd));
    }

    private static String parseNullableStringField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int colonIdx = json.indexOf(':', idx + key.length());
        if (colonIdx < 0) return null;

        // Skip whitespace after colon
        int valueStart = colonIdx + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart + 3 < json.length()
                && json.charAt(valueStart) == 'n'
                && json.charAt(valueStart + 1) == 'u'
                && json.charAt(valueStart + 2) == 'l'
                && json.charAt(valueStart + 3) == 'l') {
            return null;
        }

        int quoteStart = json.indexOf('"', colonIdx + 1);
        if (quoteStart < 0) return null;
        int quoteEnd = findClosingQuote(json, quoteStart + 1);
        if (quoteEnd < 0) return null;
        return unescapeJson(json.substring(quoteStart + 1, quoteEnd));
    }

    private static int parseIntField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return 0;
        int colonIdx = json.indexOf(':', idx + key.length());
        if (colonIdx < 0) return 0;

        int numStart = colonIdx + 1;
        while (numStart < json.length() && Character.isWhitespace(json.charAt(numStart))) {
            numStart++;
        }

        int numEnd = numStart;
        while (numEnd < json.length() && (Character.isDigit(json.charAt(numEnd)) || json.charAt(numEnd) == '-')) {
            numEnd++;
        }

        try {
            return Integer.parseInt(json.substring(numStart, numEnd));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean parseBooleanField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return false;
        int colonIdx = json.indexOf(':', idx + key.length());
        if (colonIdx < 0) return false;

        int valueStart = colonIdx + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        return valueStart + 3 < json.length()
                && json.charAt(valueStart) == 't'
                && json.charAt(valueStart + 1) == 'r'
                && json.charAt(valueStart + 2) == 'u'
                && json.charAt(valueStart + 3) == 'e';
    }

    private static List<String> parseStringArrayField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return List.of();
        int bracketStart = json.indexOf('[', idx + key.length());
        if (bracketStart < 0) return List.of();
        int bracketEnd = findMatchingBracket(json, bracketStart);
        if (bracketEnd < 0) return List.of();

        String content = json.substring(bracketStart + 1, bracketEnd).trim();
        if (content.isEmpty()) return List.of();

        List<String> result = new ArrayList<>();
        int pos = 0;
        while (pos < content.length()) {
            int qs = content.indexOf('"', pos);
            if (qs < 0) break;
            int qe = findClosingQuote(content, qs + 1);
            if (qe < 0) break;
            result.add(unescapeJson(content.substring(qs + 1, qe)));
            pos = qe + 1;
        }
        return result;
    }

    private static int findClosingQuote(String s, int fromIndex) {
        for (int i = fromIndex; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++; // skip escaped char
            } else if (c == '"') {
                return i;
            }
        }
        return -1;
    }

    private static int findMatchingBracket(String s, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                i = findClosingQuote(s, i + 1);
                if (i < 0) return -1;
            } else if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static int findMatchingBrace(String s, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                i = findClosingQuote(s, i + 1);
                if (i < 0) return -1;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static String unescapeJson(String s) {
        if (s.indexOf('\\') < 0) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (i + 4 < s.length()) {
                            String hex = s.substring(i + 1, i + 5);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException ignored) {
                                sb.append('\\');
                                sb.append('u');
                            }
                        }
                    }
                    default -> { sb.append('\\'); sb.append(next); }
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
