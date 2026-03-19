package cloud.opencode.base.reflect.scan;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Resource Information Holder
 * 资源信息持有者
 *
 * <p>Holds information about a classpath resource.</p>
 * <p>持有类路径资源的信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Resource name and URL access - 资源名和URL访问</li>
 *   <li>Resource content loading - 资源内容加载</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ResourceInfo info = new ResourceInfo("config.properties", classLoader);
 * URL url = info.url();
 * String content = info.readAsString(StandardCharsets.UTF_8);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (resource name and class loader must be non-null) - 空值安全: 否（资源名和类加载器须非空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class ResourceInfo {

    private final String resourceName;
    private final ClassLoader classLoader;

    /**
     * Creates a ResourceInfo
     * 创建ResourceInfo
     *
     * @param resourceName the resource name | 资源名
     * @param classLoader  the class loader | 类加载器
     */
    public ResourceInfo(String resourceName, ClassLoader classLoader) {
        this.resourceName = Objects.requireNonNull(resourceName, "resourceName must not be null");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader must not be null");
    }

    /**
     * Gets the resource name
     * 获取资源名
     *
     * @return the resource name | 资源名
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Gets the simple name (last part of path)
     * 获取简单名称（路径最后部分）
     *
     * @return the simple name | 简单名称
     */
    public String getSimpleName() {
        int lastSlash = resourceName.lastIndexOf('/');
        return lastSlash >= 0 ? resourceName.substring(lastSlash + 1) : resourceName;
    }

    /**
     * Gets the package name (directory path)
     * 获取包名（目录路径）
     *
     * @return the package name | 包名
     */
    public String getPackageName() {
        int lastSlash = resourceName.lastIndexOf('/');
        return lastSlash >= 0 ? resourceName.substring(0, lastSlash).replace('/', '.') : "";
    }

    /**
     * Gets the class loader
     * 获取类加载器
     *
     * @return the class loader | 类加载器
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Gets the URL for this resource
     * 获取此资源的URL
     *
     * @return the URL or null | URL或null
     */
    public URL getUrl() {
        return classLoader.getResource(resourceName);
    }

    /**
     * Opens an input stream for this resource
     * 为此资源打开输入流
     *
     * @return the input stream | 输入流
     * @throws IOException if resource cannot be opened | 如果资源无法打开
     */
    public InputStream openStream() throws IOException {
        InputStream is = classLoader.getResourceAsStream(resourceName);
        if (is == null) {
            throw new IOException("Resource not found: " + resourceName);
        }
        return is;
    }

    /**
     * Reads the resource as bytes
     * 读取资源为字节数组
     *
     * @return the bytes | 字节数组
     * @throws IOException if resource cannot be read | 如果资源无法读取
     */
    public byte[] readBytes() throws IOException {
        try (InputStream is = openStream()) {
            return is.readAllBytes();
        }
    }

    /**
     * Reads the resource as string
     * 读取资源为字符串
     *
     * @return the string | 字符串
     * @throws IOException if resource cannot be read | 如果资源无法读取
     */
    public String readString() throws IOException {
        return readString(StandardCharsets.UTF_8);
    }

    /**
     * Reads the resource as string with charset
     * 读取资源为字符串（指定字符集）
     *
     * @param charset the charset | 字符集
     * @return the string | 字符串
     * @throws IOException if resource cannot be read | 如果资源无法读取
     */
    public String readString(Charset charset) throws IOException {
        return new String(readBytes(), charset);
    }

    /**
     * Checks if this is a class file
     * 检查是否为类文件
     *
     * @return true if class file | 如果是类文件返回true
     */
    public boolean isClassFile() {
        return resourceName.endsWith(".class");
    }

    /**
     * Checks if this is a properties file
     * 检查是否为属性文件
     *
     * @return true if properties file | 如果是属性文件返回true
     */
    public boolean isPropertiesFile() {
        return resourceName.endsWith(".properties");
    }

    /**
     * Gets the file extension
     * 获取文件扩展名
     *
     * @return the extension (without dot) or empty | 扩展名（不含点）或空
     */
    public String getExtension() {
        String name = getSimpleName();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceInfo that)) return false;
        return resourceName.equals(that.resourceName) && classLoader.equals(that.classLoader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceName, classLoader);
    }

    @Override
    public String toString() {
        return "ResourceInfo[" + resourceName + "]";
    }
}
