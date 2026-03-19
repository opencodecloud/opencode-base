package cloud.opencode.base.classloader.resource;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * URL Resource - Resource from URL
 * URL 资源 - 从 URL 加载的资源
 *
 * <p>Represents a resource that can be loaded from a URL.</p>
 * <p>表示可以从 URL 加载的资源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load resources from URL - 从 URL 加载资源</li>
 *   <li>Support HTTP/HTTPS/File protocols - 支持 HTTP/HTTPS/File 协议</li>
 *   <li>Connection configuration - 连接配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Resource resource = new UrlResource("https://example.com/config.yml");
 * Resource resource = new UrlResource(new URL("https://example.com/config.yml"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public class UrlResource extends AbstractResource {

    private final URI uri;
    private final URL url;

    /**
     * Create URL resource from URL string
     * 从 URL 字符串创建 URL 资源
     *
     * @param url URL string | URL 字符串
     * @throws MalformedURLException if URL is invalid | URL 无效时抛出
     */
    public UrlResource(String url) throws MalformedURLException {
        Objects.requireNonNull(url, "URL must not be null");
        try {
            this.uri = URI.create(url);
            this.url = uri.toURL();
        } catch (Exception e) {
            throw new MalformedURLException("Invalid URL: " + url);
        }
    }

    /**
     * Create URL resource from URL
     * 从 URL 创建 URL 资源
     *
     * @param url URL object | URL 对象
     */
    public UrlResource(URL url) {
        Objects.requireNonNull(url, "URL must not be null");
        this.url = url;
        try {
            this.uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    /**
     * Create URL resource from URI
     * 从 URI 创建 URL 资源
     *
     * @param uri URI object | URI 对象
     * @throws MalformedURLException if URI cannot be converted to URL | URI 无法转换为 URL 时抛出
     */
    public UrlResource(URI uri) throws MalformedURLException {
        Objects.requireNonNull(uri, "URI must not be null");
        this.uri = uri;
        this.url = uri.toURL();
    }

    @Override
    public boolean exists() {
        try {
            if ("file".equals(url.getProtocol())) {
                return new File(uri).exists();
            }
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            if (connection instanceof HttpURLConnection httpConn) {
                try {
                    httpConn.setRequestMethod("HEAD");
                    int responseCode = httpConn.getResponseCode();
                    return responseCode == HttpURLConnection.HTTP_OK;
                } finally {
                    httpConn.disconnect();
                }
            }
            connection.connect();
            try {
                return true;
            } finally {
                try {
                    connection.getInputStream().close();
                } catch (IOException ignored) {
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isFile() {
        return "file".equals(url.getProtocol());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        try {
            return connection.getInputStream();
        } catch (IOException e) {
            throw new OpenClassLoaderException("Failed to open connection to: " + url, e);
        }
    }

    @Override
    public URL getUrl() throws IOException {
        return url;
    }

    @Override
    public URI getUri() throws IOException {
        return uri;
    }

    @Override
    public Optional<File> getFile() {
        if ("file".equals(url.getProtocol())) {
            return Optional.of(new File(uri));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Path> getPath() {
        if ("file".equals(url.getProtocol())) {
            return Optional.of(Path.of(uri));
        }
        return Optional.empty();
    }

    @Override
    public long contentLength() throws IOException {
        URLConnection connection = url.openConnection();
        try {
            return connection.getContentLengthLong();
        } finally {
            if (connection instanceof HttpURLConnection httpConn) {
                httpConn.disconnect();
            } else {
                try {
                    connection.getInputStream().close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public long lastModified() throws IOException {
        URLConnection connection = url.openConnection();
        try {
            return connection.getLastModified();
        } finally {
            if (connection instanceof HttpURLConnection httpConn) {
                httpConn.disconnect();
            } else {
                try {
                    connection.getInputStream().close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public String getFilename() {
        String path = url.getPath();
        if (path == null || path.isEmpty()) {
            return null;
        }
        int lastSlash = path.lastIndexOf('/');
        return lastSlash != -1 ? path.substring(lastSlash + 1) : path;
    }

    @Override
    public String getDescription() {
        return "url:" + url;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        try {
            return new UrlResource(uri.resolve(relativePath));
        } catch (Exception e) {
            throw new IOException("Cannot create relative resource: " + relativePath, e);
        }
    }
}
