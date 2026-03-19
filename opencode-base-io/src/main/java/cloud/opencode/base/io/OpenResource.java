package cloud.opencode.base.io;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import cloud.opencode.base.io.resource.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * Resource Loading Utility Class
 * 资源加载工具类
 *
 * <p>Utility class for loading resources from classpath, filesystem,
 * and URLs with a unified API.</p>
 * <p>用于从类路径、文件系统和URL加载资源的工具类，提供统一的API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Classpath resource loading - 类路径资源加载</li>
 *   <li>Filesystem resource loading - 文件系统资源加载</li>
 *   <li>URL resource loading - URL资源加载</li>
 *   <li>Properties loading - Properties加载</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Read classpath resource
 * String config = OpenResource.readString("application.properties");
 *
 * // Read properties
 * Properties props = OpenResource.readProperties("config.properties");
 *
 * // Check existence
 * if (OpenResource.exists("optional.xml")) {
 *     // ...
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
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class OpenResource {

    private static final ResourceLoader DEFAULT_LOADER = ResourceLoader.getDefault();

    private OpenResource() {
    }

    // ==================== Resource Location | 资源定位 ====================

    /**
     * Gets resource URL
     * 获取资源URL
     *
     * @param resourceName the resource name | 资源名称
     * @return URL or null if not found | URL，未找到返回null
     */
    public static URL getResource(String resourceName) {
        return getClassLoader().getResource(normalizePath(resourceName));
    }

    /**
     * Gets resource URL (required)
     * 获取资源URL（必须存在）
     *
     * @param resourceName the resource name | 资源名称
     * @return URL | URL
     * @throws OpenIOOperationException if not found | 如果未找到
     */
    public static URL getResourceRequired(String resourceName) {
        URL url = getResource(resourceName);
        if (url == null) {
            throw OpenIOOperationException.resourceNotFound(resourceName);
        }
        return url;
    }

    /**
     * Gets all matching resource URLs
     * 获取所有匹配的资源URL
     *
     * @param resourceName the resource name | 资源名称
     * @return list of URLs | URL列表
     */
    public static List<URL> getResources(String resourceName) {
        try {
            Enumeration<URL> urls = getClassLoader().getResources(normalizePath(resourceName));
            List<URL> result = new ArrayList<>();
            while (urls.hasMoreElements()) {
                result.add(urls.nextElement());
            }
            return result;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Checks if resource exists
     * 检查资源是否存在
     *
     * @param resourceName the resource name | 资源名称
     * @return true if exists | 如果存在返回true
     */
    public static boolean exists(String resourceName) {
        return getResource(resourceName) != null;
    }

    // ==================== Resource Reading | 资源读取 ====================

    /**
     * Gets resource input stream
     * 获取资源输入流
     *
     * @param resourceName the resource name | 资源名称
     * @return input stream | 输入流
     * @throws OpenIOOperationException if not found | 如果未找到
     */
    public static InputStream getStream(String resourceName) {
        InputStream is = getClassLoader().getResourceAsStream(normalizePath(resourceName));
        if (is == null) {
            throw OpenIOOperationException.resourceNotFound(resourceName);
        }
        return is;
    }

    /**
     * Reads resource as byte array
     * 读取资源为字节数组
     *
     * @param resourceName the resource name | 资源名称
     * @return byte array | 字节数组
     */
    public static byte[] readBytes(String resourceName) {
        try (InputStream is = getStream(resourceName)) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to read resource: " + resourceName, e);
        }
    }

    /**
     * Reads resource as string
     * 读取资源为字符串
     *
     * @param resourceName the resource name | 资源名称
     * @param charset      the charset | 字符集
     * @return content string | 内容字符串
     */
    public static String readString(String resourceName, Charset charset) {
        return new String(readBytes(resourceName), charset);
    }

    /**
     * Reads resource as UTF-8 string
     * 读取资源为UTF-8字符串
     *
     * @param resourceName the resource name | 资源名称
     * @return content string | 内容字符串
     */
    public static String readString(String resourceName) {
        return readString(resourceName, StandardCharsets.UTF_8);
    }

    /**
     * Reads resource as lines
     * 读取资源为行列表
     *
     * @param resourceName the resource name | 资源名称
     * @param charset      the charset | 字符集
     * @return list of lines | 行列表
     */
    public static List<String> readLines(String resourceName, Charset charset) {
        String content = readString(resourceName, charset);
        return Arrays.asList(content.split("\\R"));
    }

    /**
     * Reads resource as lines with UTF-8
     * 使用UTF-8读取资源为行列表
     *
     * @param resourceName the resource name | 资源名称
     * @return list of lines | 行列表
     */
    public static List<String> readLines(String resourceName) {
        return readLines(resourceName, StandardCharsets.UTF_8);
    }

    /**
     * Reads resource as Properties
     * 读取资源为Properties
     *
     * @param resourceName the resource name | 资源名称
     * @return Properties object | Properties对象
     */
    public static Properties readProperties(String resourceName) {
        Properties props = new Properties();
        try (InputStream is = getStream(resourceName)) {
            if (resourceName.endsWith(".xml")) {
                props.loadFromXML(is);
            } else {
                props.load(is);
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to read properties: " + resourceName, e);
        }
        return props;
    }

    // ==================== Resource Objects | 资源对象 ====================

    /**
     * Creates a classpath resource
     * 创建类路径资源
     *
     * @param path the path | 路径
     * @return resource | 资源
     */
    public static Resource classPathResource(String path) {
        return new ClassPathResource(path);
    }

    /**
     * Creates a filesystem resource
     * 创建文件系统资源
     *
     * @param path the path | 路径
     * @return resource | 资源
     */
    public static Resource fileResource(Path path) {
        return new FileSystemResource(path);
    }

    /**
     * Creates a filesystem resource from string
     * 从字符串创建文件系统资源
     *
     * @param path the path string | 路径字符串
     * @return resource | 资源
     */
    public static Resource fileResource(String path) {
        return new FileSystemResource(path);
    }

    /**
     * Creates a URL resource
     * 创建URL资源
     *
     * @param url the URL | URL
     * @return resource | 资源
     */
    public static Resource urlResource(URL url) {
        return new UrlResource(url);
    }

    /**
     * Creates a URL resource from string
     * 从字符串创建URL资源
     *
     * @param url the URL string | URL字符串
     * @return resource | 资源
     */
    public static Resource urlResource(String url) {
        return new UrlResource(url);
    }

    /**
     * Gets the resource loader
     * 获取资源加载器
     *
     * @return resource loader | 资源加载器
     */
    public static ResourceLoader getResourceLoader() {
        return DEFAULT_LOADER;
    }

    /**
     * Loads resource using resource loader
     * 使用资源加载器加载资源
     *
     * @param location the location (classpath:, file:, or URL) | 位置
     * @return resource | 资源
     */
    public static Resource load(String location) {
        return DEFAULT_LOADER.getResource(location);
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = OpenResource.class.getClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
        }
        return cl;
    }

    private static String normalizePath(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }
}
