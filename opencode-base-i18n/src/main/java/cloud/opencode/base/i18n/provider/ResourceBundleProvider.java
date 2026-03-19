package cloud.opencode.base.i18n.provider;

import cloud.opencode.base.i18n.spi.MessageProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ResourceBundle-based message provider
 * 基于ResourceBundle的消息提供者
 *
 * <p>Loads messages from .properties files using Java's ResourceBundle mechanism.</p>
 * <p>使用Java的ResourceBundle机制从.properties文件加载消息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Properties file loading - properties文件加载</li>
 *   <li>Multiple base names support - 多基础名称支持</li>
 *   <li>Caching support - 缓存支持</li>
 *   <li>Custom ClassLoader - 自定义ClassLoader</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ResourceBundleProvider provider = new ResourceBundleProvider("i18n/messages");
 * Optional<String> msg = provider.getMessageTemplate("user.welcome", Locale.CHINESE);
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
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public class ResourceBundleProvider implements MessageProvider {

    private final List<String> baseNames;
    private final ClassLoader classLoader;
    private final Map<String, ResourceBundle> bundleCache = new ConcurrentHashMap<>();
    private final Set<Locale> supportedLocales = ConcurrentHashMap.newKeySet();
    private boolean useCache = true;
    private String defaultEncoding = "UTF-8";

    /**
     * Creates a provider with single base name
     * 使用单个基础名称创建提供者
     *
     * @param baseName the resource base name | 资源基础名称
     */
    public ResourceBundleProvider(String baseName) {
        this(List.of(baseName), Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a provider with multiple base names
     * 使用多个基础名称创建提供者
     *
     * @param baseNames the resource base names | 资源基础名称列表
     */
    public ResourceBundleProvider(List<String> baseNames) {
        this(baseNames, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a provider with base name and class loader
     * 使用基础名称和类加载器创建提供者
     *
     * @param baseName    the resource base name | 资源基础名称
     * @param classLoader the class loader | 类加载器
     */
    public ResourceBundleProvider(String baseName, ClassLoader classLoader) {
        this(List.of(baseName), classLoader);
    }

    /**
     * Creates a provider with multiple base names and class loader
     * 使用多个基础名称和类加载器创建提供者
     *
     * @param baseNames   the resource base names | 资源基础名称列表
     * @param classLoader the class loader | 类加载器
     */
    public ResourceBundleProvider(List<String> baseNames, ClassLoader classLoader) {
        this.baseNames = new ArrayList<>(baseNames);
        this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
    }

    @Override
    public Optional<String> getMessageTemplate(String key, Locale locale) {
        for (String baseName : baseNames) {
            ResourceBundle bundle = getBundle(baseName, locale);
            if (bundle != null && bundle.containsKey(key)) {
                return Optional.of(bundle.getString(key));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean containsMessage(String key, Locale locale) {
        for (String baseName : baseNames) {
            ResourceBundle bundle = getBundle(baseName, locale);
            if (bundle != null && bundle.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getKeys(Locale locale) {
        Set<String> allKeys = new HashSet<>();
        for (String baseName : baseNames) {
            ResourceBundle bundle = getBundle(baseName, locale);
            if (bundle != null) {
                allKeys.addAll(bundle.keySet());
            }
        }
        return allKeys;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return Collections.unmodifiableSet(supportedLocales);
    }

    @Override
    public void refresh() {
        bundleCache.clear();
        ResourceBundle.clearCache(classLoader);
    }

    /**
     * Sets whether to use cache
     * 设置是否使用缓存
     *
     * @param useCache whether to cache | 是否缓存
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
        if (!useCache) {
            bundleCache.clear();
        }
    }

    /**
     * Sets the default encoding
     * 设置默认编码
     *
     * @param encoding the encoding | 编码
     */
    public void setDefaultEncoding(String encoding) {
        this.defaultEncoding = encoding;
    }

    /**
     * Adds a base name
     * 添加基础名称
     *
     * @param baseName the base name | 基础名称
     */
    public void addBaseName(String baseName) {
        baseNames.add(baseName);
    }

    private ResourceBundle getBundle(String baseName, Locale locale) {
        String cacheKey = baseName + "_" + locale.toString();

        if (useCache) {
            ResourceBundle cached = bundleCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, classLoader);
            if (useCache) {
                bundleCache.put(cacheKey, bundle);
            }
            supportedLocales.add(bundle.getLocale());
            return bundle;
        } catch (MissingResourceException e) {
            return null;
        }
    }
}
