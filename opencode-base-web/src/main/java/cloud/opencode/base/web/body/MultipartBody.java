package cloud.opencode.base.web.body;

import cloud.opencode.base.web.http.ContentType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Multipart Body - multipart/form-data Request Body Builder
 * Multipart 请求体 - multipart/form-data 请求体构建器
 *
 * <p>Builds multipart/form-data encoded request bodies for HTTP file uploads
 * and mixed form submissions using the Builder pattern.</p>
 * <p>使用 Builder 模式构建 multipart/form-data 编码的请求体，用于 HTTP 文件上传和混合表单提交。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Builder pattern for fluent construction - 构建器模式支持流式构建</li>
 *   <li>Text field support - 文本字段支持</li>
 *   <li>File upload with byte[], Path, or InputStream - 文件上传支持 byte[]、Path 或 InputStream</li>
 *   <li>Automatic content type detection for Path - Path 自动检测内容类型</li>
 *   <li>UUID-based boundary generation - 基于 UUID 的 boundary 生成</li>
 *   <li>Immutable after construction - 构建后不可变</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build multipart body with text and file
 * MultipartBody body = MultipartBody.builder()
 *     .addField("name", "John")
 *     .addFile("avatar", "avatar.png", "image/png", imageBytes)
 *     .addFile("document", Path.of("report.pdf"))
 *     .build();
 *
 * String contentType = body.getContentType();
 * BodyPublisher publisher = body.getBodyPublisher();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (name and value must not be null) - 空值安全: 否（名称和值不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.3
 */
public final class MultipartBody implements RequestBody {

    private final String boundary;
    private final List<Part> parts;
    private final byte[] bodyBytes;

    private MultipartBody(String boundary, List<Part> parts) {
        this.boundary = boundary;
        this.parts = Collections.unmodifiableList(parts);
        this.bodyBytes = encode(boundary, this.parts);
    }

    /**
     * Creates a new builder with a random UUID boundary.
     * 创建使用随机 UUID boundary 的新构建器。
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder(UUID.randomUUID().toString());
    }

    /**
     * Creates a new builder with the specified boundary.
     * 创建使用指定 boundary 的新构建器。
     *
     * @param boundary the boundary string | boundary 字符串
     * @return the builder | 构建器
     */
    public static Builder builder(String boundary) {
        if (boundary == null || boundary.isBlank()) {
            throw new IllegalArgumentException("Boundary must not be null or blank");
        }
        if (boundary.length() > 70) {
            throw new IllegalArgumentException(
                    "Boundary must not exceed 70 characters per RFC 2046: length=" + boundary.length());
        }
        return new Builder(boundary);
    }

    @Override
    public String getContentType() {
        return ContentType.MULTIPART_FORM_DATA + "; boundary=" + boundary;
    }

    @Override
    public BodyPublisher getBodyPublisher() {
        return BodyPublishers.ofByteArray(bodyBytes);
    }

    @Override
    public long getContentLength() {
        return bodyBytes.length;
    }

    /**
     * Gets the boundary string.
     * 获取 boundary 字符串。
     *
     * @return the boundary | boundary
     */
    public String getBoundary() {
        return boundary;
    }

    /**
     * Gets the immutable list of parts.
     * 获取不可变的 part 列表。
     *
     * @return the parts | part 列表
     */
    public List<Part> getParts() {
        return parts;
    }

    @Override
    public String toString() {
        return "MultipartBody{boundary=" + boundary + ", parts=" + parts.size() + "}";
    }

    // ==================== Encoding ====================

    private static byte[] encode(String boundary, List<Part> parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] crlf = "\r\n".getBytes(StandardCharsets.UTF_8);

        try {
            for (Part part : parts) {
                out.write(boundaryBytes);
                out.write(crlf);

                // Content-Disposition header
                StringBuilder disposition = new StringBuilder();
                disposition.append("Content-Disposition: form-data; name=\"").append(part.name()).append("\"");
                if (part.fileName() != null) {
                    disposition.append("; filename=\"").append(part.fileName()).append("\"");
                }
                out.write(disposition.toString().getBytes(StandardCharsets.UTF_8));
                out.write(crlf);

                // Content-Type header (for file parts)
                if (part.contentType() != null) {
                    out.write(("Content-Type: " + part.contentType()).getBytes(StandardCharsets.UTF_8));
                    out.write(crlf);
                }

                // Empty line before body
                out.write(crlf);

                // Body — access field directly via record component to avoid defensive clone
                out.write(part.data);
                out.write(crlf);
            }

            // Closing boundary
            out.write(boundaryBytes);
            out.write("--".getBytes(StandardCharsets.UTF_8));
            out.write(crlf);
        } catch (IOException e) {
            // ByteArrayOutputStream doesn't throw IOException
            throw new UncheckedIOException(e);
        }

        return out.toByteArray();
    }

    // ==================== Part Record ====================

    /**
     * Multipart Part - Represents a single part in a multipart body
     * Multipart Part - 表示 multipart 请求体中的单个 part
     *
     * @param name the field name | 字段名
     * @param fileName the file name (null for text fields) | 文件名（文本字段为 null）
     * @param contentType the content type (null for text fields) | 内容类型（文本字段为 null）
     * @param data the raw bytes | 原始字节
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-web V1.0.3
     */
    public record Part(String name, String fileName, String contentType, byte[] data) {

        /**
         * Creates a Part with defensive copy of data.
         * 创建 Part 并对数据进行防御性复制。
         */
        public Part {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Part name must not be null or blank");
            }
            validateNoCrLf("name", name);
            if (fileName != null) {
                validateNoCrLf("fileName", fileName);
            }
            if (contentType != null) {
                validateNoCrLf("contentType", contentType);
            }
            if (data == null) {
                throw new IllegalArgumentException("Part data must not be null");
            }
            data = data.clone();
        }

        private static void validateNoCrLf(String field, String value) {
            if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
                throw new IllegalArgumentException(
                        "Part " + field + " must not contain CR or LF characters");
            }
        }

        /**
         * Returns a defensive copy of the data.
         * 返回数据的防御性副本。
         *
         * @return the data copy | 数据副本
         */
        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for MultipartBody
     * MultipartBody 构建器
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-web V1.0.3
     */
    public static final class Builder {

        private final String boundary;
        private final List<Part> parts = new ArrayList<>();

        private Builder(String boundary) {
            this.boundary = boundary;
        }

        /**
         * Adds a text field.
         * 添加文本字段。
         *
         * @param name the field name | 字段名
         * @param value the field value | 字段值
         * @return this builder | 此构建器
         */
        public Builder addField(String name, String value) {
            if (value == null) {
                throw new IllegalArgumentException("Field value must not be null");
            }
            parts.add(new Part(name, null, null, value.getBytes(StandardCharsets.UTF_8)));
            return this;
        }

        /**
         * Adds a file with explicit content type and byte data.
         * 添加指定内容类型和字节数据的文件。
         *
         * @param name the field name | 字段名
         * @param fileName the file name | 文件名
         * @param contentType the content type | 内容类型
         * @param data the file data | 文件数据
         * @return this builder | 此构建器
         */
        public Builder addFile(String name, String fileName, String contentType, byte[] data) {
            if (fileName == null || fileName.isBlank()) {
                throw new IllegalArgumentException("File name must not be null or blank");
            }
            if (contentType == null || contentType.isBlank()) {
                throw new IllegalArgumentException("Content type must not be null or blank");
            }
            parts.add(new Part(name, fileName, contentType, data));
            return this;
        }

        /**
         * Adds a file from a Path with automatic content type detection.
         * 从 Path 添加文件，自动检测内容类型。
         *
         * @param name the field name | 字段名
         * @param file the file path | 文件路径
         * @return this builder | 此构建器
         * @throws UncheckedIOException if the file cannot be read | 如果文件无法读取
         */
        public Builder addFile(String name, Path file) {
            if (file == null) {
                throw new IllegalArgumentException("File path must not be null");
            }
            try {
                String fileName = file.getFileName().toString();
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = ContentType.APPLICATION_OCTET_STREAM;
                }
                byte[] data = Files.readAllBytes(file);
                parts.add(new Part(name, fileName, contentType, data));
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read file: " + file, e);
            }
            return this;
        }

        /**
         * Adds a file from an InputStream.
         * 从 InputStream 添加文件。
         *
         * @param name the field name | 字段名
         * @param fileName the file name | 文件名
         * @param contentType the content type | 内容类型
         * @param stream the input stream | 输入流
         * @return this builder | 此构建器
         * @throws UncheckedIOException if the stream cannot be read | 如果流无法读取
         */
        public Builder addFile(String name, String fileName, String contentType, InputStream stream) {
            if (stream == null) {
                throw new IllegalArgumentException("InputStream must not be null");
            }
            if (fileName == null || fileName.isBlank()) {
                throw new IllegalArgumentException("File name must not be null or blank");
            }
            if (contentType == null || contentType.isBlank()) {
                throw new IllegalArgumentException("Content type must not be null or blank");
            }
            try {
                byte[] data = stream.readAllBytes();
                parts.add(new Part(name, fileName, contentType, data));
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read input stream", e);
            }
            return this;
        }

        /**
         * Builds the MultipartBody.
         * 构建 MultipartBody。
         *
         * @return the multipart body | multipart 请求体
         */
        public MultipartBody build() {
            return new MultipartBody(boundary, new ArrayList<>(parts));
        }
    }
}
