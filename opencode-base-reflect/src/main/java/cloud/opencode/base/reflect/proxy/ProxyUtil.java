package cloud.opencode.base.reflect.proxy;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy Utility Class
 * 代理工具类
 *
 * <p>Provides low-level proxy operation utilities with caching.</p>
 * <p>提供带缓存的底层代理操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Proxy class creation with caching - 带缓存的代理类创建</li>
 *   <li>Proxy detection and handler extraction - 代理检测和处理器提取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean isProxy = ProxyUtil.isProxy(obj);
 * InvocationHandler handler = ProxyUtil.getInvocationHandler(proxy);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap for caching) - 线程安全: 是（使用ConcurrentHashMap缓存）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for cached proxy class lookups; proxy detection and handler extraction O(1) - 时间复杂度: 缓存命中时代理类查找为 O(1)；代理检测和处理器提取 O(1)</li>
 *   <li>Space complexity: O(1) per cached proxy class entry - 空间复杂度: O(1)，每个缓存代理类条目</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class ProxyUtil {

    /**
     * Composite key for proxy class cache (weak ClassLoader + interface)
     * 代理类缓存的复合键（弱引用ClassLoader + 接口）
     */
    private static final class ProxyClassKey {
        private final WeakReference<ClassLoader> loaderRef;
        private final Class<?> iface;
        private final int hashCode;

        ProxyClassKey(ClassLoader loader, Class<?> iface) {
            this.loaderRef = new WeakReference<>(loader);
            this.iface = iface;
            this.hashCode = System.identityHashCode(loader) * 31 + iface.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProxyClassKey that)) return false;
            ClassLoader thisLoader = this.loaderRef.get();
            ClassLoader thatLoader = that.loaderRef.get();
            return thisLoader != null && thisLoader == thatLoader && iface == that.iface;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    private static final Map<ProxyClassKey, Class<?>> PROXY_CLASS_CACHE = new ConcurrentHashMap<>();

    private ProxyUtil() {
    }

    // ==================== Proxy Class Creation | 代理类创建 ====================

    /**
     * Gets or creates proxy class (cached)
     * 获取或创建代理类（缓存）
     *
     * @param loader     the class loader | 类加载器
     * @param interfaces the interfaces | 接口
     * @return the proxy class | 代理类
     */
    public static Class<?> getProxyClass(ClassLoader loader, Class<?>... interfaces) {
        if (interfaces.length == 1) {
            ProxyClassKey key = new ProxyClassKey(loader, interfaces[0]);
            return PROXY_CLASS_CACHE.computeIfAbsent(key,
                    k -> Proxy.getProxyClass(loader, interfaces[0]));
        }
        return Proxy.getProxyClass(loader, interfaces);
    }

    /**
     * Creates a proxy instance
     * 创建代理实例
     *
     * @param loader     the class loader | 类加载器
     * @param interfaces the interfaces | 接口
     * @param handler    the invocation handler | 调用处理器
     * @return the proxy instance | 代理实例
     */
    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler handler) {
        return Proxy.newProxyInstance(loader, interfaces, handler);
    }

    /**
     * Creates a proxy instance for a single interface
     * 为单个接口创建代理实例
     *
     * @param interfaceClass the interface class | 接口类
     * @param handler        the invocation handler | 调用处理器
     * @param <T>            the interface type | 接口类型
     * @return the proxy instance | 代理实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(Class<T> interfaceClass, InvocationHandler handler) {
        ClassLoader loader = interfaceClass.getClassLoader();
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        return (T) Proxy.newProxyInstance(loader, new Class<?>[]{interfaceClass}, handler);
    }

    // ==================== Proxy Inspection | 代理检查 ====================

    /**
     * Checks if a class is a proxy class
     * 检查类是否为代理类
     *
     * @param clazz the class | 类
     * @return true if proxy class | 如果是代理类返回true
     */
    public static boolean isProxyClass(Class<?> clazz) {
        return clazz != null && Proxy.isProxyClass(clazz);
    }

    /**
     * Checks if an object is a proxy instance
     * 检查对象是否为代理实例
     *
     * @param obj the object | 对象
     * @return true if proxy | 如果是代理返回true
     */
    public static boolean isProxy(Object obj) {
        return obj != null && Proxy.isProxyClass(obj.getClass());
    }

    /**
     * Gets the invocation handler of a proxy
     * 获取代理的调用处理器
     *
     * @param proxy the proxy | 代理
     * @return the invocation handler | 调用处理器
     */
    public static InvocationHandler getInvocationHandler(Object proxy) {
        return Proxy.getInvocationHandler(proxy);
    }

    /**
     * Gets the invocation handler safely
     * 安全获取调用处理器
     *
     * @param proxy the proxy | 代理
     * @return Optional of handler | 处理器的Optional
     */
    public static Optional<InvocationHandler> getInvocationHandlerSafe(Object proxy) {
        if (!isProxy(proxy)) {
            return Optional.empty();
        }
        return Optional.of(Proxy.getInvocationHandler(proxy));
    }

    // ==================== Interface Operations | 接口操作 ====================

    /**
     * Gets interfaces implemented by a proxy
     * 获取代理实现的接口
     *
     * @param proxy the proxy | 代理
     * @return array of interfaces | 接口数组
     */
    public static Class<?>[] getProxyInterfaces(Object proxy) {
        if (!isProxy(proxy)) {
            return new Class<?>[0];
        }
        return proxy.getClass().getInterfaces();
    }

    /**
     * Gets interfaces implemented by a proxy as list
     * 获取代理实现的接口列表
     *
     * @param proxy the proxy | 代理
     * @return list of interfaces | 接口列表
     */
    public static List<Class<?>> getProxyInterfaceList(Object proxy) {
        return Arrays.asList(getProxyInterfaces(proxy));
    }

    /**
     * Checks if proxy implements an interface
     * 检查代理是否实现接口
     *
     * @param proxy          the proxy | 代理
     * @param interfaceClass the interface class | 接口类
     * @return true if implements | 如果实现返回true
     */
    public static boolean implementsInterface(Object proxy, Class<?> interfaceClass) {
        for (Class<?> iface : getProxyInterfaces(proxy)) {
            if (iface.equals(interfaceClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates that a class is an interface suitable for proxying
     * 验证类是否为适合代理的接口
     *
     * @param clazz the class | 类
     * @return true if valid | 如果有效返回true
     */
    public static boolean isProxyable(Class<?> clazz) {
        return clazz != null && clazz.isInterface();
    }

    /**
     * Validates multiple interfaces for proxying
     * 验证多个接口是否适合代理
     *
     * @param interfaces the interfaces | 接口
     * @return true if all valid | 如果全部有效返回true
     */
    public static boolean areProxyable(Class<?>... interfaces) {
        if (interfaces == null || interfaces.length == 0) {
            return false;
        }
        for (Class<?> iface : interfaces) {
            if (!isProxyable(iface)) {
                return false;
            }
        }
        return true;
    }

    // ==================== Method Utilities | 方法工具 ====================

    /**
     * Checks if a method is from Object class
     * 检查方法是否来自Object类
     *
     * @param method the method | 方法
     * @return true if Object method | 如果是Object方法返回true
     */
    public static boolean isObjectMethod(Method method) {
        return method.getDeclaringClass() == Object.class;
    }

    /**
     * Checks if a method is equals
     * 检查方法是否为equals
     *
     * @param method the method | 方法
     * @return true if equals | 如果是equals返回true
     */
    public static boolean isEqualsMethod(Method method) {
        return "equals".equals(method.getName()) &&
               method.getParameterCount() == 1 &&
               method.getParameterTypes()[0] == Object.class;
    }

    /**
     * Checks if a method is hashCode
     * 检查方法是否为hashCode
     *
     * @param method the method | 方法
     * @return true if hashCode | 如果是hashCode返回true
     */
    public static boolean isHashCodeMethod(Method method) {
        return "hashCode".equals(method.getName()) && method.getParameterCount() == 0;
    }

    /**
     * Checks if a method is toString
     * 检查方法是否为toString
     *
     * @param method the method | 方法
     * @return true if toString | 如果是toString返回true
     */
    public static boolean isToStringMethod(Method method) {
        return "toString".equals(method.getName()) && method.getParameterCount() == 0;
    }

    // ==================== Default Value | 默认值 ====================

    /**
     * Gets the default return value for a type
     * 获取类型的默认返回值
     *
     * @param returnType the return type | 返回类型
     * @return the default value | 默认值
     */
    public static Object getDefaultReturnValue(Class<?> returnType) {
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) return false;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0f;
        if (returnType == double.class) return 0d;
        if (returnType == char.class) return '\0';
        return null;
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Clears proxy class cache
     * 清除代理类缓存
     */
    public static void clearCache() {
        PROXY_CLASS_CACHE.clear();
    }

    /**
     * Gets cache size
     * 获取缓存大小
     *
     * @return the cache size | 缓存大小
     */
    public static int getCacheSize() {
        return PROXY_CLASS_CACHE.size();
    }
}
