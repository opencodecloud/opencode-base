package cloud.opencode.base.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Resource Loader - Test resource loading utility
 * 资源加载器 - 测试资源加载工具
 *
 * <p>Utility class for loading test resources from classpath or filesystem.</p>
 * <p>用于从类路径或文件系统加载测试资源的工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load resources from classpath - 从类路径加载资源</li>
 *   <li>Load resources from filesystem - 从文件系统加载资源</li>
 *   <li>Support for text, bytes, properties - 支持文本、字节、属性文件</li>
 *   <li>Support for different charsets - 支持不同字符集</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Load text file from classpath
 * String content = ResourceLoader.loadString("test-data.json");
 *
 * // Load as lines
 * List<String> lines = ResourceLoader.loadLines("test-data.txt");
 *
 * // Load properties
 * Properties props = ResourceLoader.loadProperties("test-config.properties");
 *
 * // Load bytes
 * byte[] bytes = ResourceLoader.loadBytes("test-image.png");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (throws IllegalArgumentException for null/missing resources) - 空值安全: 是（对空值/缺失资源抛出IllegalArgumentException）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class ResourceLoader {

    private ResourceLoader() {
    }

    // ==================== String Loading | 字符串加载 ====================

    /**
     * Loads a resource as a string using UTF-8 encoding.
     * 使用 UTF-8 编码将资源加载为字符串。
     *
     * @param resourcePath the resource path | 资源路径
     * @return the content as string | 内容字符串
     * @throws IllegalArgumentException if resource not found | 如果资源未找到
     */
    public static String loadString(String resourcePath) {
        return loadString(resourcePath, StandardCharsets.UTF_8);
    }

    /**
     * Loads a resource as a string with specified charset.
     * 使用指定字符集将资源加载为字符串。
     *
     * @param resourcePath the resource path | 资源路径
     * @param charset      the charset | 字符集
     * @return the content as string | 内容字符串
     * @throws IllegalArgumentException if resource not found | 如果资源未找到
     */
    public static String loadString(String resourcePath, Charset charset) {
        try (InputStream is = getResourceStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load resource: " + resourcePath, e);
        }
    }

    /**
     * Loads a resource as a string, returning Optional.
     * 将资源加载为字符串，返回 Optional。
     *
     * @param resourcePath the resource path | 资源路径
     * @return the content as Optional | 内容 Optional
     */
    public static Optional<String> loadStringOptional(String resourcePath) {
        try {
            return Optional.of(loadString(resourcePath));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // ==================== Lines Loading | 行加载 ====================

    /**
     * Loads a resource as a list of lines.
     * 将资源加载为行列表。
     *
     * @param resourcePath the resource path | 资源路径
     * @return the lines | 行列表
     * @throws IllegalArgumentException if resource not found | 如果资源未找到
     */
    public static List<String> loadLines(String resourcePath) {
        return loadLines(resourcePath, StandardCharsets.UTF_8);
    }

    /**
     * Loads a resource as a list of lines with specified charset.
     * 使用指定字符集将资源加载为行列表。
     *
     * @param resourcePath the resource path | 资源路径
     * @param charset      the charset | 字符集
     * @return the lines | 行列表
     * @throws IllegalArgumentException if resource not found | 如果资源未找到
     */
    public static List<String> loadLines(String resourcePath, Charset charset) {
        try (InputStream is = getResourceStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load resource: " + resourcePath, e);
        }
    }

    // ==================== Bytes Loading | 字节加载 ====================

    /**
     * Loads a resource as a byte array.
     * 将资源加载为字节数组。
     *
     * @param resourcePath the resource path | 资源路径
     * @return the bytes | 字节数组
     * @throws IllegalArgumentException if resource not found | 如果资源未找到
     */
    public static byte[] loadBytes(String resourcePath) {
        try (InputStream is = getResourceStream(resourcePath)) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load resource: " + resourcePath, e);
        }
    }

    // ==================== Properties Loading | 属性加载 ====================

    /**
     * Loads a properties file.
     * 加载属性文件。
     *
     * @param resourcePath the resource path | 资源路径
     * @return the properties | 属性
     * @throws IllegalArgumentException if resource not found | 如果资源未找到
     */
    public static Properties loadProperties(String resourcePath) {
        Properties props = new Properties();
        try (InputStream is = getResourceStream(resourcePath)) {
            props.load(is);
            return props;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load properties: " + resourcePath, e);
        }
    }

    // ==================== Stream/URL Access | 流/URL访问 ====================

    /**
     * Gets an InputStream for the resource.
     * 获取资源的输入流。
     *
     * @param resourcePath the resource path | 资源路径
     * @return the input stream | 输入流
     * @throws IllegalArgumentException if resource not found | 如果资源未找到
     */
    public static InputStream getResourceStream(String resourcePath) {
        InputStream is = ResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        }
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        return is;
    }

    /**
     * Gets the URL for the resource.
     * 获取资源的 URL。
     *
     * @param resourcePath the resource path | 资源路径
     * @return the URL or empty | URL 或空
     */
    public static Optional<URL> getResourceURL(String resourcePath) {
        URL url = ResourceLoader.class.getClassLoader().getResource(resourcePath);
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        }
        return Optional.ofNullable(url);
    }

    /**
     * Checks if a resource exists.
     * 检查资源是否存在。
     *
     * @param resourcePath the resource path | 资源路径
     * @return true if exists | 如果存在返回 true
     */
    public static boolean exists(String resourcePath) {
        return getResourceURL(resourcePath).isPresent();
    }

    // ==================== File Loading | 文件加载 ====================

    /**
     * Loads a file from filesystem as string.
     * 从文件系统加载文件为字符串。
     *
     * @param path the file path | 文件路径
     * @return the content | 内容
     * @throws IllegalArgumentException if file not found | 如果文件未找到
     */
    public static String loadFile(Path path) {
        return loadFile(path, StandardCharsets.UTF_8);
    }

    /**
     * Loads a file from filesystem as string with charset.
     * 使用字符集从文件系统加载文件为字符串。
     *
     * @param path    the file path | 文件路径
     * @param charset the charset | 字符集
     * @return the content | 内容
     * @throws IllegalArgumentException if file not found | 如果文件未找到
     */
    public static String loadFile(Path path, Charset charset) {
        try {
            return Files.readString(path, charset);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load file: " + path, e);
        }
    }

    /**
     * Loads file lines from filesystem.
     * 从文件系统加载文件行。
     *
     * @param path the file path | 文件路径
     * @return the lines | 行列表
     * @throws IllegalArgumentException if file not found | 如果文件未找到
     */
    public static List<String> loadFileLines(Path path) {
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load file: " + path, e);
        }
    }

    /**
     * Loads file bytes from filesystem.
     * 从文件系统加载文件字节。
     *
     * @param path the file path | 文件路径
     * @return the bytes | 字节数组
     * @throws IllegalArgumentException if file not found | 如果文件未找到
     */
    public static byte[] loadFileBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load file: " + path, e);
        }
    }
}
