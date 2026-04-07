package cloud.opencode.base.io.resource;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

/**
 * URL Resource Implementation
 * URL资源实现
 *
 * <p>Resource implementation for resources accessible via URL.
 * Supports http, https, ftp, file and other URL protocols.</p>
 * <p>用于通过URL访问资源的资源实现。
 * 支持http、https、ftp、file和其他URL协议。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>URL-based resource access - 基于URL的资源访问</li>
 *   <li>Multiple protocol support - 多协议支持</li>
 *   <li>Immutable and thread-safe - 不可变且线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * UrlResource resource = new UrlResource(new URL("https://example.com/config.json"));
 * String content = resource.readString();
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class UrlResource implements Resource {

    /**
     * Allowed URL protocols to mitigate SSRF risk
     * 允许的URL协议，用于降低SSRF风险
     */
    private static final Set<String> ALLOWED_PROTOCOLS = Set.of("http", "https", "file", "jar");

    private final URL url;
    private final URI uri;

    /**
     * Creates a URL resource from URL
     * 从URL创建URL资源
     *
     * @param url the URL | URL
     */
    public UrlResource(URL url) {
        Objects.requireNonNull(url, "URL must not be null");
        validateProtocol(url.getProtocol());
        this.url = url;
        this.uri = toURI(url);
    }

    /**
     * Creates a URL resource from URI
     * 从URI创建URL资源
     *
     * @param uri the URI | URI
     */
    public UrlResource(URI uri) {
        Objects.requireNonNull(uri, "URI must not be null");
        if (uri.getScheme() != null) {
            validateProtocol(uri.getScheme());
        }
        try {
            this.url = uri.toURL();
            this.uri = uri;
        } catch (MalformedURLException e) {
            throw new OpenIOOperationException("Failed to convert URI to URL: " + uri, e);
        }
    }

    /**
     * Creates a URL resource from string
     * 从字符串创建URL资源
     *
     * @param urlString the URL string | URL字符串
     */
    public UrlResource(String urlString) {
        Objects.requireNonNull(urlString, "URL string must not be null");
        try {
            this.uri = URI.create(urlString);
            if (this.uri.getScheme() != null) {
                validateProtocol(this.uri.getScheme());
            }
            this.url = this.uri.toURL();
        } catch (MalformedURLException e) {
            throw new OpenIOOperationException("Invalid URL: " + urlString, e);
        }
    }

    private static void validateProtocol(String protocol) {
        if (protocol != null && !ALLOWED_PROTOCOLS.contains(protocol.toLowerCase())) {
            throw new OpenIOOperationException("Unsupported URL protocol (SSRF protection): " + protocol
                    + ". Allowed: " + ALLOWED_PROTOCOLS, null);
        }
    }

    private static URI toURI(URL url) {
        try {
            return url.toURI();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean exists() {
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof HttpURLConnection httpConn) {
                try {
                    httpConn.setRequestMethod("HEAD");
                    int code = httpConn.getResponseCode();
                    return code >= 200 && code < 400;
                } finally {
                    httpConn.disconnect();
                }
            }
            // For non-HTTP URLs, try to open connection
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
    public boolean isReadable() {
        return exists();
    }

    @Override
    public InputStream getInputStream() {
        try {
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            return connection.getInputStream();
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to open URL connection: " + url, e);
        }
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public Path getPath() {
        if ("file".equals(url.getProtocol())) {
            try {
                return Path.of(uri);
            } catch (Exception e) {
                // Path not available
            }
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "URL:" + url;
    }

    @Override
    public String getFilename() {
        String path = url.getPath();
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    @Override
    public long contentLength() {
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof HttpURLConnection httpConn) {
                try {
                    httpConn.setRequestMethod("HEAD");
                    return httpConn.getContentLengthLong();
                } finally {
                    httpConn.disconnect();
                }
            }
            try {
                return connection.getContentLengthLong();
            } finally {
                try {
                    connection.getInputStream().close();
                } catch (IOException ignored) {
                }
            }
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public long lastModified() {
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof HttpURLConnection httpConn) {
                try {
                    httpConn.setRequestMethod("HEAD");
                    return httpConn.getLastModified();
                } finally {
                    httpConn.disconnect();
                }
            }
            try {
                return connection.getLastModified();
            } finally {
                try {
                    connection.getInputStream().close();
                } catch (IOException ignored) {
                }
            }
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public Resource createRelative(String relativePath) {
        try {
            URL relativeUrl = new URL(url, relativePath);
            return new UrlResource(relativeUrl);
        } catch (MalformedURLException e) {
            throw new OpenIOOperationException("Failed to create relative URL: " + relativePath, e);
        }
    }

    /**
     * Gets the URI
     * 获取URI
     *
     * @return URI | URI
     */
    public URI getURI() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UrlResource that)) return false;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
