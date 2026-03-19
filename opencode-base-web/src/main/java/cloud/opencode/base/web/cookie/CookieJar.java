package cloud.opencode.base.web.cookie;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

/**
 * Cookie Jar - HTTP Cookie Storage and Management
 * Cookie 存储 - HTTP Cookie 存储和管理
 *
 * <p>A high-level wrapper over {@link java.net.CookieManager} providing a developer-friendly
 * API for HTTP cookie management.</p>
 * <p>对 {@link java.net.CookieManager} 的高层封装，提供开发者友好的 HTTP Cookie 管理 API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>In-memory cookie storage - 内存Cookie存储</li>
 *   <li>Configurable cookie policies - 可配置的Cookie策略</li>
 *   <li>JDK CookieManager integration - JDK CookieManager集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CookieJar jar = CookieJar.inMemory();
 * jar.add(uri, cookie);
 * List<HttpCookie> cookies = jar.cookies(uri);
 * jar.clear();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (delegates to CookieManager) - 否（委托给CookieManager）</li>
 *   <li>Null-safe: No - 否</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class CookieJar {

    private final CookieManager manager;

    private CookieJar(CookieManager manager) {
        this.manager = manager;
    }

    public static CookieJar inMemory() {
        return new CookieJar(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }

    public static CookieJar inMemory(CookiePolicy policy) {
        return new CookieJar(new CookieManager(null, policy));
    }

    public static CookieJar strict() {
        return new CookieJar(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
    }

    public static CookieJar disabled() {
        return new CookieJar(new CookieManager(null, CookiePolicy.ACCEPT_NONE));
    }

    public List<HttpCookie> cookies(URI uri) {
        return manager.getCookieStore().get(uri);
    }

    public List<HttpCookie> allCookies() {
        return manager.getCookieStore().getCookies();
    }

    public void add(URI uri, HttpCookie cookie) {
        manager.getCookieStore().add(uri, cookie);
    }

    public void clear() {
        manager.getCookieStore().removeAll();
    }

    public int size() {
        return manager.getCookieStore().getCookies().size();
    }

    public CookieManager asCookieManager() {
        return manager;
    }

    @Override
    public String toString() {
        return "CookieJar{cookies=" + manager.getCookieStore().getCookies().size() + "}";
    }
}
