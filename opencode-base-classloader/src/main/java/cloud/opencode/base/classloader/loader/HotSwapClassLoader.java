package cloud.opencode.base.classloader.loader;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HotSwap ClassLoader - Supports dynamic class replacement
 * 热替换类加载器 - 支持类的动态替换
 *
 * <p>ClassLoader that supports hot swapping of classes at runtime.</p>
 * <p>支持运行时热替换类的类加载器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load classes from bytecode - 从字节码加载类</li>
 *   <li>Reload modified classes - 重新加载修改的类</li>
 *   <li>Version tracking - 版本跟踪</li>
 *   <li>Class unloading - 类卸载</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HotSwapClassLoader loader = HotSwapClassLoader.create();
 * Class<?> v1 = loader.loadClass("MyClass", bytecodeV1);
 * // After modification
 * Class<?> v2 = loader.reloadClass("MyClass", classFile);
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
public class HotSwapClassLoader extends ClassLoader implements AutoCloseable {

    private final ConcurrentHashMap<String, ClassInfo> classInfoMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> versionMap = new ConcurrentHashMap<>();
    private volatile boolean closed = false;

    private record ClassInfo(Class<?> clazz, byte[] bytecode, int version, ClassLoader definingLoader) {}

    /**
     * Create HotSwap classloader with default parent
     * 使用默认父加载器创建热替换类加载器
     */
    public HotSwapClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Create HotSwap classloader with specified parent
     * 使用指定父加载器创建热替换类加载器
     *
     * @param parent parent classloader | 父类加载器
     */
    public HotSwapClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Create HotSwap classloader with default parent
     * 使用默认父加载器创建热替换类加载器
     *
     * @return new HotSwapClassLoader | 新的热替换类加载器
     */
    public static HotSwapClassLoader create() {
        return new HotSwapClassLoader();
    }

    /**
     * Create HotSwap classloader with specified parent
     * 使用指定父加载器创建热替换类加载器
     *
     * @param parent parent classloader | 父类加载器
     * @return new HotSwapClassLoader | 新的热替换类加载器
     */
    public static HotSwapClassLoader create(ClassLoader parent) {
        return new HotSwapClassLoader(parent);
    }

    // ==================== Class Loading | 类加载 ====================

    /**
     * Load or reload class from bytecode
     * 从字节码加载或重新加载类
     *
     * @param name     class name | 类名
     * @param bytecode class bytecode | 类字节码
     * @return loaded class | 加载的类
     */
    public Class<?> loadClass(String name, byte[] bytecode) {
        checkNotClosed();
        Objects.requireNonNull(name, "Class name must not be null");
        Objects.requireNonNull(bytecode, "Bytecode must not be null");

        int version = versionMap.computeIfAbsent(name, k -> new AtomicInteger(0)).incrementAndGet();
        // Use a disposable child classloader for each definition to allow reloading
        // the same class name. JVM does not allow defineClass() for the same name
        // on the same ClassLoader instance.
        byte[] bytecodeCopy = bytecode.clone();
        SingleClassLoader child = new SingleClassLoader(this, name, bytecodeCopy);
        Class<?> clazz = child.defineClassFromBytecode();
        classInfoMap.put(name, new ClassInfo(clazz, bytecodeCopy, version, child));
        return clazz;
    }

    /**
     * Reload class from file
     * 从文件重新加载类
     *
     * @param name      class name | 类名
     * @param classFile class file path | 类文件路径
     * @return reloaded class | 重新加载的类
     */
    public Class<?> reloadClass(String name, Path classFile) {
        checkNotClosed();
        try {
            byte[] bytecode = Files.readAllBytes(classFile);
            return loadClass(name, bytecode);
        } catch (IOException e) {
            throw OpenClassLoaderException.classLoadFailed(name, e);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        checkNotClosed();

        // Check if we have it loaded
        ClassInfo info = classInfoMap.get(name);
        if (info != null) {
            return info.clazz();
        }

        // Delegate to parent
        return super.loadClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        ClassInfo info = classInfoMap.get(name);
        if (info != null) {
            return info.clazz();
        }
        throw new ClassNotFoundException(name);
    }

    // ==================== Version Management | 版本管理 ====================

    /**
     * Get current version of class
     * 获取类的当前版本
     *
     * @param className class name | 类名
     * @return version number or 0 if not loaded | 版本号，未加载返回 0
     */
    public int getVersion(String className) {
        AtomicInteger version = versionMap.get(className);
        return version != null ? version.get() : 0;
    }

    /**
     * Get all loaded class names
     * 获取所有已加载的类名
     *
     * @return set of class names | 类名集合
     */
    public Set<String> getLoadedClassNames() {
        return Set.copyOf(classInfoMap.keySet());
    }

    /**
     * Check if class is loaded
     * 检查类是否已加载
     *
     * @param className class name | 类名
     * @return true if loaded | 已加载返回 true
     */
    public boolean isLoaded(String className) {
        return classInfoMap.containsKey(className);
    }

    /**
     * Get bytecode of loaded class
     * 获取已加载类的字节码
     *
     * @param className class name | 类名
     * @return optional bytecode | 可选的字节码
     */
    public Optional<byte[]> getBytecode(String className) {
        ClassInfo info = classInfoMap.get(className);
        return info != null ? Optional.of(info.bytecode().clone()) : Optional.empty();
    }

    // ==================== Class Management | 类管理 ====================

    /**
     * Unload class (will be reloaded on next access)
     * 卸载类（下次访问时重新加载）
     *
     * @param className class name | 类名
     */
    public void unloadClass(String className) {
        checkNotClosed();
        classInfoMap.remove(className);
        // Note: version is kept for tracking
    }

    /**
     * Clear all loaded classes
     * 清除所有已加载的类
     */
    public void clear() {
        checkNotClosed();
        classInfoMap.clear();
        versionMap.clear();
    }

    // ==================== Lifecycle | 生命周期 ====================

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            classInfoMap.clear();
            versionMap.clear();
        }
    }

    /**
     * Check if classloader is closed
     * 检查类加载器是否已关闭
     *
     * @return true if closed | 已关闭返回 true
     */
    public boolean isClosed() {
        return closed;
    }

    private void checkNotClosed() {
        if (closed) {
            throw OpenClassLoaderException.classLoaderClosed();
        }
    }

    /**
     * Disposable single-class classloader used for hot-swap reloading.
     * 用于热替换重新加载的一次性单类类加载器。
     *
     * <p>Each class definition gets its own classloader instance so the JVM
     * does not reject duplicate class definitions on the same loader.</p>
     */
    private static final class SingleClassLoader extends ClassLoader {
        private final String className;
        private final byte[] bytecode;

        SingleClassLoader(ClassLoader parent, String className, byte[] bytecode) {
            super(parent);
            this.className = className;
            this.bytecode = bytecode;
        }

        Class<?> defineClassFromBytecode() {
            return defineClass(className, bytecode, 0, bytecode.length);
        }
    }
}
