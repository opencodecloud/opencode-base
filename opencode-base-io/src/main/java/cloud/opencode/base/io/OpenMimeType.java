package cloud.opencode.base.io;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * MIME Type Detection Utility Class
 * MIME 类型检测工具类
 *
 * <p>Provides MIME type detection from file extension and content (magic numbers).</p>
 * <p>提供基于文件扩展名和内容（魔数）的 MIME 类型检测。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Detect by file extension - 按扩展名检测</li>
 *   <li>Detect by content (magic numbers) - 按内容检测（魔数）</li>
 *   <li>Comprehensive MIME type mappings - 完整的 MIME 类型映射</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Detect by extension
 * String mime = OpenMimeType.fromExtension("pdf");  // "application/pdf"
 *
 * // Detect from file (extension + content)
 * String mime = OpenMimeType.detect(Path.of("image.png"));
 *
 * // Detect from content only
 * String mime = OpenMimeType.fromContent(bytes);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility with static maps) - 线程安全: 是（使用静态映射的无状态工具类）</li>
 *   <li>Null-safe: Yes, null/unknown returns application/octet-stream - 空值安全: 是，null/未知返回application/octet-stream</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class OpenMimeType {

    /**
     * Default MIME type for unknown content
     * 未知内容的默认 MIME 类型
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * Common text MIME type
     * 常见文本 MIME 类型
     */
    public static final String TEXT_PLAIN = "text/plain";

    /**
     * Common HTML MIME type
     * 常见 HTML MIME 类型
     */
    public static final String TEXT_HTML = "text/html";

    /**
     * Common JSON MIME type
     * 常见 JSON MIME 类型
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * Common XML MIME type
     * 常见 XML MIME 类型
     */
    public static final String APPLICATION_XML = "application/xml";

    /**
     * Common PDF MIME type
     * 常见 PDF MIME 类型
     */
    public static final String APPLICATION_PDF = "application/pdf";

    private OpenMimeType() {
    }

    // ==================== Extension-based Detection | 基于扩展名检测 ====================

    /**
     * Extension to MIME type mapping
     * 扩展名到 MIME 类型映射
     */
    private static final Map<String, String> EXTENSION_MAP = Map.ofEntries(
        // Text
        Map.entry("txt", "text/plain"),
        Map.entry("html", "text/html"),
        Map.entry("htm", "text/html"),
        Map.entry("css", "text/css"),
        Map.entry("csv", "text/csv"),
        Map.entry("xml", "text/xml"),
        Map.entry("md", "text/markdown"),
        Map.entry("markdown", "text/markdown"),

        // Application
        Map.entry("json", "application/json"),
        Map.entry("js", "application/javascript"),
        Map.entry("mjs", "application/javascript"),
        Map.entry("pdf", "application/pdf"),
        Map.entry("zip", "application/zip"),
        Map.entry("gz", "application/gzip"),
        Map.entry("gzip", "application/gzip"),
        Map.entry("tar", "application/x-tar"),
        Map.entry("7z", "application/x-7z-compressed"),
        Map.entry("rar", "application/vnd.rar"),
        Map.entry("jar", "application/java-archive"),
        Map.entry("war", "application/java-archive"),
        Map.entry("class", "application/java-vm"),
        Map.entry("wasm", "application/wasm"),

        // Images
        Map.entry("jpg", "image/jpeg"),
        Map.entry("jpeg", "image/jpeg"),
        Map.entry("png", "image/png"),
        Map.entry("gif", "image/gif"),
        Map.entry("bmp", "image/bmp"),
        Map.entry("webp", "image/webp"),
        Map.entry("svg", "image/svg+xml"),
        Map.entry("ico", "image/x-icon"),
        Map.entry("tiff", "image/tiff"),
        Map.entry("tif", "image/tiff"),
        Map.entry("avif", "image/avif"),
        Map.entry("heic", "image/heic"),
        Map.entry("heif", "image/heif"),

        // Audio
        Map.entry("mp3", "audio/mpeg"),
        Map.entry("wav", "audio/wav"),
        Map.entry("ogg", "audio/ogg"),
        Map.entry("flac", "audio/flac"),
        Map.entry("aac", "audio/aac"),
        Map.entry("m4a", "audio/mp4"),
        Map.entry("wma", "audio/x-ms-wma"),

        // Video
        Map.entry("mp4", "video/mp4"),
        Map.entry("webm", "video/webm"),
        Map.entry("mkv", "video/x-matroska"),
        Map.entry("avi", "video/x-msvideo"),
        Map.entry("mov", "video/quicktime"),
        Map.entry("wmv", "video/x-ms-wmv"),
        Map.entry("flv", "video/x-flv"),
        Map.entry("m4v", "video/x-m4v"),

        // Fonts
        Map.entry("ttf", "font/ttf"),
        Map.entry("otf", "font/otf"),
        Map.entry("woff", "font/woff"),
        Map.entry("woff2", "font/woff2"),
        Map.entry("eot", "application/vnd.ms-fontobject"),

        // Documents
        Map.entry("doc", "application/msword"),
        Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        Map.entry("xls", "application/vnd.ms-excel"),
        Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        Map.entry("ppt", "application/vnd.ms-powerpoint"),
        Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
        Map.entry("odt", "application/vnd.oasis.opendocument.text"),
        Map.entry("ods", "application/vnd.oasis.opendocument.spreadsheet"),
        Map.entry("odp", "application/vnd.oasis.opendocument.presentation"),
        Map.entry("rtf", "application/rtf"),

        // Programming
        Map.entry("java", "text/x-java-source"),
        Map.entry("py", "text/x-python"),
        Map.entry("c", "text/x-c"),
        Map.entry("cpp", "text/x-c++"),
        Map.entry("h", "text/x-c"),
        Map.entry("hpp", "text/x-c++"),
        Map.entry("rs", "text/x-rust"),
        Map.entry("go", "text/x-go"),
        Map.entry("rb", "text/x-ruby"),
        Map.entry("php", "text/x-php"),
        Map.entry("ts", "text/typescript"),
        Map.entry("tsx", "text/typescript-jsx"),
        Map.entry("jsx", "text/jsx"),
        Map.entry("vue", "text/x-vue"),
        Map.entry("swift", "text/x-swift"),
        Map.entry("kt", "text/x-kotlin"),
        Map.entry("scala", "text/x-scala"),
        Map.entry("sh", "application/x-sh"),
        Map.entry("bash", "application/x-sh"),
        Map.entry("bat", "application/x-msdos-program"),
        Map.entry("ps1", "application/x-powershell"),
        Map.entry("sql", "application/sql"),

        // Configuration
        Map.entry("yaml", "text/yaml"),
        Map.entry("yml", "text/yaml"),
        Map.entry("toml", "text/toml"),
        Map.entry("ini", "text/plain"),
        Map.entry("properties", "text/x-java-properties"),
        Map.entry("env", "text/plain"),
        Map.entry("conf", "text/plain"),
        Map.entry("cfg", "text/plain")
    );

    /**
     * Magic number signatures for content detection
     * 用于内容检测的魔数签名
     */
    private static final MagicNumber[] MAGIC_NUMBERS = {
        // Images
        new MagicNumber("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
        new MagicNumber("image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
        new MagicNumber("image/gif", new byte[]{0x47, 0x49, 0x46, 0x38}), // GIF8
        new MagicNumber("image/bmp", new byte[]{0x42, 0x4D}), // BM
        new MagicNumber("image/tiff", new byte[]{0x49, 0x49, 0x2A, 0x00}), // II*.
        new MagicNumber("image/tiff", new byte[]{0x4D, 0x4D, 0x00, 0x2A}), // MM.*

        // Documents
        new MagicNumber("application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}), // %PDF
        new MagicNumber("application/zip", new byte[]{0x50, 0x4B, 0x03, 0x04}), // PK..
        new MagicNumber("application/gzip", new byte[]{0x1F, (byte) 0x8B}),
        new MagicNumber("application/x-7z-compressed", new byte[]{0x37, 0x7A, (byte) 0xBC, (byte) 0xAF, 0x27, 0x1C}),
        new MagicNumber("application/x-rar-compressed", new byte[]{0x52, 0x61, 0x72, 0x21, 0x1A, 0x07}), // Rar!..
        new MagicNumber("application/x-tar", new byte[]{0x75, 0x73, 0x74, 0x61, 0x72}, 257), // ustar at offset 257

        // Audio
        new MagicNumber("audio/mpeg", new byte[]{(byte) 0xFF, (byte) 0xFB}), // MP3 frame sync
        new MagicNumber("audio/mpeg", new byte[]{(byte) 0xFF, (byte) 0xFA}),
        new MagicNumber("audio/mpeg", new byte[]{0x49, 0x44, 0x33}), // ID3
        new MagicNumber("audio/wav", new byte[]{0x52, 0x49, 0x46, 0x46}), // RIFF (need further check for WAVE)
        new MagicNumber("audio/ogg", new byte[]{0x4F, 0x67, 0x67, 0x53}), // OggS
        new MagicNumber("audio/flac", new byte[]{0x66, 0x4C, 0x61, 0x43}), // fLaC

        // Video
        new MagicNumber("video/webm", new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3}), // EBML

        // Executables
        new MagicNumber("application/x-executable", new byte[]{0x7F, 0x45, 0x4C, 0x46}), // ELF
        new MagicNumber("application/x-msdos-program", new byte[]{0x4D, 0x5A}), // MZ (DOS/PE)

        // Java
        new MagicNumber("application/java-vm", new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE}), // Java class

        // XML/HTML (text-based, check start)
        new MagicNumber("text/xml", new byte[]{0x3C, 0x3F, 0x78, 0x6D, 0x6C}), // <?xml
        new MagicNumber("text/html", new byte[]{0x3C, 0x21, 0x44, 0x4F, 0x43, 0x54, 0x59, 0x50, 0x45}), // <!DOCTYPE
        new MagicNumber("text/html", new byte[]{0x3C, 0x68, 0x74, 0x6D, 0x6C}), // <html
    };

    /**
     * Get MIME type from file extension
     * 从文件扩展名获取 MIME 类型
     *
     * @param extension the file extension (without dot) | 文件扩展名（不含点）
     * @return MIME type or empty if unknown | MIME 类型，未知时返回空
     */
    public static Optional<String> fromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return Optional.empty();
        }
        String ext = extension.toLowerCase().replace(".", "");
        return Optional.ofNullable(EXTENSION_MAP.get(ext));
    }

    /**
     * Get MIME type from file extension with default
     * 从文件扩展名获取 MIME 类型（带默认值）
     *
     * @param extension the file extension | 文件扩展名
     * @param defaultMime default MIME type | 默认 MIME 类型
     * @return MIME type | MIME 类型
     */
    public static String fromExtension(String extension, String defaultMime) {
        return fromExtension(extension).orElse(defaultMime);
    }

    /**
     * Get MIME type from path extension
     * 从路径扩展名获取 MIME 类型
     *
     * @param path the file path | 文件路径
     * @return MIME type or empty if unknown | MIME 类型，未知时返回空
     */
    public static Optional<String> fromPath(Path path) {
        if (path == null) {
            return Optional.empty();
        }
        String extension = OpenPath.getExtension(path);
        return fromExtension(extension);
    }

    /**
     * Get MIME type from filename
     * 从文件名获取 MIME 类型
     *
     * @param filename the filename | 文件名
     * @return MIME type or empty if unknown | MIME 类型，未知时返回空
     */
    public static Optional<String> fromFilename(String filename) {
        if (filename == null) {
            return Optional.empty();
        }
        String extension = OpenPath.getExtension(filename);
        return fromExtension(extension);
    }

    // ==================== Content-based Detection | 基于内容检测 ====================

    /**
     * Detect MIME type from file content (magic numbers)
     * 从文件内容检测 MIME 类型（魔数）
     *
     * @param path the file path | 文件路径
     * @return MIME type or empty if unknown | MIME 类型，未知时返回空
     */
    public static Optional<String> fromContent(Path path) {
        try {
            byte[] header = readHeader(path, 512);
            return fromContent(header);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Detect MIME type from byte array content
     * 从字节数组内容检测 MIME 类型
     *
     * @param data the content bytes | 内容字节
     * @return MIME type or empty if unknown | MIME 类型，未知时返回空
     */
    public static Optional<String> fromContent(byte[] data) {
        if (data == null || data.length < 2) {
            return Optional.empty();
        }

        // Check WebP specially (RIFF....WEBP pattern)
        if (data.length >= 12 && matchesAt(data, 0, new byte[]{0x52, 0x49, 0x46, 0x46})
            && matchesAt(data, 8, new byte[]{0x57, 0x45, 0x42, 0x50})) {
            return Optional.of("image/webp");
        }

        // Check MP4/MOV (ftyp box)
        if (data.length >= 12 && matchesAt(data, 4, new byte[]{0x66, 0x74, 0x79, 0x70})) {
            // Check specific brand
            if (matchesAt(data, 8, new byte[]{0x69, 0x73, 0x6F, 0x6D}) // isom
                || matchesAt(data, 8, new byte[]{0x6D, 0x70, 0x34})) { // mp4
                return Optional.of("video/mp4");
            }
            if (matchesAt(data, 8, new byte[]{0x71, 0x74})) { // qt
                return Optional.of("video/quicktime");
            }
            if (matchesAt(data, 8, new byte[]{0x4D, 0x34, 0x41})) { // M4A
                return Optional.of("audio/mp4");
            }
            return Optional.of("video/mp4"); // Default to mp4
        }

        // Check standard magic numbers
        for (MagicNumber magic : MAGIC_NUMBERS) {
            if (data.length >= magic.offset + magic.signature.length) {
                if (matchesAt(data, magic.offset, magic.signature)) {
                    return Optional.of(magic.mimeType);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Detect MIME type from input stream
     * 从输入流检测 MIME 类型
     *
     * @param input the input stream (must support mark/reset) | 输入流（必须支持 mark/reset）
     * @return MIME type or empty if unknown | MIME 类型，未知时返回空
     */
    public static Optional<String> fromContent(InputStream input) {
        if (input == null) {
            return Optional.empty();
        }
        try {
            if (!input.markSupported()) {
                return Optional.empty();
            }
            input.mark(512);
            byte[] header = new byte[512];
            int read = input.read(header);
            input.reset();
            if (read < 2) {
                return Optional.empty();
            }
            byte[] data = read == 512 ? header : java.util.Arrays.copyOf(header, read);
            return fromContent(data);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    // ==================== Combined Detection | 综合检测 ====================

    /**
     * Detect MIME type from file (combines extension and content detection)
     * 从文件检测 MIME 类型（综合扩展名和内容检测）
     *
     * <p>Priority: content detection > extension detection > system probe</p>
     * <p>优先级：内容检测 > 扩展名检测 > 系统探测</p>
     *
     * @param path the file path | 文件路径
     * @return MIME type, defaults to application/octet-stream | MIME 类型，默认 application/octet-stream
     */
    public static String detect(Path path) {
        // Try content detection first
        Optional<String> contentType = fromContent(path);
        if (contentType.isPresent()) {
            return contentType.get();
        }

        // Try extension detection
        Optional<String> extType = fromPath(path);
        if (extType.isPresent()) {
            return extType.get();
        }

        // Try system probe
        try {
            String probed = Files.probeContentType(path);
            if (probed != null && !probed.isEmpty()) {
                return probed;
            }
        } catch (IOException ignored) {
        }

        return APPLICATION_OCTET_STREAM;
    }

    /**
     * Detect MIME type preferring extension over content
     * 优先使用扩展名检测 MIME 类型
     *
     * @param path the file path | 文件路径
     * @return MIME type, defaults to application/octet-stream | MIME 类型
     */
    public static String detectByExtension(Path path) {
        return fromPath(path).orElseGet(() -> {
            try {
                String probed = Files.probeContentType(path);
                return probed != null ? probed : APPLICATION_OCTET_STREAM;
            } catch (IOException e) {
                return APPLICATION_OCTET_STREAM;
            }
        });
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Check if MIME type is text-based
     * 检查 MIME 类型是否为文本类型
     *
     * @param mimeType the MIME type | MIME 类型
     * @return true if text-based | 如果是文本类型返回 true
     */
    public static boolean isText(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        return mimeType.startsWith("text/")
            || mimeType.equals("application/json")
            || mimeType.equals("application/xml")
            || mimeType.equals("application/javascript")
            || mimeType.endsWith("+xml")
            || mimeType.endsWith("+json");
    }

    /**
     * Check if MIME type is image
     * 检查 MIME 类型是否为图片
     *
     * @param mimeType the MIME type | MIME 类型
     * @return true if image | 如果是图片返回 true
     */
    public static boolean isImage(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Check if MIME type is audio
     * 检查 MIME 类型是否为音频
     *
     * @param mimeType the MIME type | MIME 类型
     * @return true if audio | 如果是音频返回 true
     */
    public static boolean isAudio(String mimeType) {
        return mimeType != null && mimeType.startsWith("audio/");
    }

    /**
     * Check if MIME type is video
     * 检查 MIME 类型是否为视频
     *
     * @param mimeType the MIME type | MIME 类型
     * @return true if video | 如果是视频返回 true
     */
    public static boolean isVideo(String mimeType) {
        return mimeType != null && mimeType.startsWith("video/");
    }

    /**
     * Check if MIME type is binary
     * 检查 MIME 类型是否为二进制
     *
     * @param mimeType the MIME type | MIME 类型
     * @return true if binary (not text) | 如果是二进制返回 true
     */
    public static boolean isBinary(String mimeType) {
        return !isText(mimeType);
    }

    /**
     * Get file extension for MIME type
     * 获取 MIME 类型对应的文件扩展名
     *
     * @param mimeType the MIME type | MIME 类型
     * @return extension or empty if unknown | 扩展名，未知时返回空
     */
    public static Optional<String> getExtension(String mimeType) {
        if (mimeType == null) {
            return Optional.empty();
        }
        String lower = mimeType.toLowerCase();
        for (Map.Entry<String, String> entry : EXTENSION_MAP.entrySet()) {
            if (entry.getValue().equals(lower)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    // ==================== Private Helpers | 私有辅助方法 ====================

    private static byte[] readHeader(Path path, int size) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            byte[] header = new byte[size];
            int read = is.read(header);
            return read == size ? header : java.util.Arrays.copyOf(header, Math.max(0, read));
        }
    }

    private static boolean matchesAt(byte[] data, int offset, byte[] signature) {
        if (data.length < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[offset + i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Magic number record
     * 魔数记录
     */
    private record MagicNumber(String mimeType, byte[] signature, int offset) {
        MagicNumber(String mimeType, byte[] signature) {
            this(mimeType, signature, 0);
        }
    }
}
