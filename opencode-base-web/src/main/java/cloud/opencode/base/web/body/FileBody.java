package cloud.opencode.base.web.body;

import cloud.opencode.base.web.http.ContentType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * File Body - File Request Body
 * 文件请求体 - 文件请求体
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>File-based HTTP request body - 基于文件的 HTTP 请求体</li>
 *   <li>Automatic content type detection - 自动检测内容类型</li>
 *   <li>Support for common file extensions - 支持常见文件扩展名</li>
 *   <li>Lazy body publisher creation - 延迟创建 body publisher</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Auto-detect content type
 * FileBody body = FileBody.of(Path.of("document.pdf"));
 *
 * // Explicit content type
 * FileBody body = FileBody.of(Path.of("data.bin"), "application/octet-stream");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (file path must not be null) - 空值安全: 否（文件路径不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class FileBody implements RequestBody {

    private static final Map<String, String> EXTENSION_CONTENT_TYPES = Map.ofEntries(
            Map.entry("txt", ContentType.TEXT_PLAIN),
            Map.entry("html", ContentType.TEXT_HTML),
            Map.entry("htm", ContentType.TEXT_HTML),
            Map.entry("css", ContentType.TEXT_CSS),
            Map.entry("js", ContentType.TEXT_JAVASCRIPT),
            Map.entry("json", ContentType.APPLICATION_JSON),
            Map.entry("xml", ContentType.APPLICATION_XML),
            Map.entry("pdf", ContentType.APPLICATION_PDF),
            Map.entry("zip", ContentType.APPLICATION_ZIP),
            Map.entry("png", ContentType.IMAGE_PNG),
            Map.entry("jpg", ContentType.IMAGE_JPEG),
            Map.entry("jpeg", ContentType.IMAGE_JPEG),
            Map.entry("gif", ContentType.IMAGE_GIF),
            Map.entry("svg", ContentType.IMAGE_SVG),
            Map.entry("webp", ContentType.IMAGE_WEBP),
            Map.entry("mp3", ContentType.AUDIO_MPEG),
            Map.entry("mp4", ContentType.VIDEO_MP4)
    );

    private final Path file;
    private final String contentType;
    private final long contentLength;

    private FileBody(Path file, String contentType) {
        this.file = file;
        this.contentType = contentType;
        try {
            this.contentLength = Files.size(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file size: " + file, e);
        }
    }

    public static FileBody of(Path file) {
        return new FileBody(file, detectContentType(file));
    }

    public static FileBody of(Path file, String contentType) {
        return new FileBody(file, contentType);
    }

    @Override
    public String getContentType() { return contentType; }

    @Override
    public BodyPublisher getBodyPublisher() {
        try {
            return BodyPublishers.ofFile(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create body publisher for file: " + file, e);
        }
    }

    @Override
    public long getContentLength() { return contentLength; }

    public Path getFile() { return file; }
    public String getFileName() { return file.getFileName().toString(); }
    public boolean exists() { return Files.exists(file); }

    public static String detectContentType(Path file) {
        String fileName = file.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            String extension = fileName.substring(dotIndex + 1).toLowerCase();
            String type = EXTENSION_CONTENT_TYPES.get(extension);
            if (type != null) return type;
        }
        try {
            String probed = Files.probeContentType(file);
            if (probed != null) return probed;
        } catch (IOException ignored) {}
        return ContentType.APPLICATION_OCTET_STREAM;
    }

    @Override
    public String toString() {
        return "FileBody{file=" + file + ", contentType=" + contentType + ", size=" + contentLength + "}";
    }
}
