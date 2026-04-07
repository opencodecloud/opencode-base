package cloud.opencode.base.classloader.loader;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
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
 *   <li>Version rollback - 版本回退</li>
 *   <li>Hot-swap event notification - 热替换事件通知</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HotSwapClassLoader loader = HotSwapClassLoader.create();
 * loader.addListener((name, oldV, newV) ->
 *     System.out.println(name + ": v" + oldV + " -> v" + newV));
 * Class<?> v1 = loader.loadClass("MyClass", bytecodeV1);
 * // After modification
 * Class<?> v2 = loader.loadClass("MyClass", bytecodeV2);
 * // Rollback to v1
 * Optional<Class<?>> rolled = loader.rollback("MyClass");
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

    private static final int DEFAULT_MAX_HISTORY_VERSIONS = 5;

    private final ConcurrentHashMap<String, ClassInfo> classInfoMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> versionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Deque<ClassInfo>> historyMap = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<HotSwapListener> listeners = new CopyOnWriteArrayList<>();
    private final int maxHistoryVersions;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private record ClassInfo(Class<?> clazz, byte[] bytecode, int version, ClassLoader definingLoader) {}

    /**
     * Create HotSwap classloader with default parent
     * 使用默认父加载器创建热替换类加载器
     */
    public HotSwapClassLoader() {
        this(Thread.currentThread().getContextClassLoader(), DEFAULT_MAX_HISTORY_VERSIONS);
    }

    /**
     * Create HotSwap classloader with specified parent
     * 使用指定父加载器创建热替换类加载器
     *
     * @param parent parent classloader | 父类加载器
     */
    public HotSwapClassLoader(ClassLoader parent) {
        this(parent, DEFAULT_MAX_HISTORY_VERSIONS);
    }

    /**
     * Create HotSwap classloader with specified parent and max history versions
     * 使用指定父加载器和最大历史版本数创建热替换类加载器
     *
     * @param parent             parent classloader | 父类加载器
     * @param maxHistoryVersions max history versions to keep | 保留的最大历史版本数
     */
    public HotSwapClassLoader(ClassLoader parent, int maxHistoryVersions) {
        super(parent);
        if (maxHistoryVersions < 0) {
            throw new IllegalArgumentException("maxHistoryVersions must be non-negative");
        }
        this.maxHistoryVersions = maxHistoryVersions;
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

    /**
     * Create HotSwap classloader with default parent and specified max history versions
     * 使用默认父加载器和指定最大历史版本数创建热替换类加载器
     *
     * @param maxHistoryVersions max history versions to keep | 保留的最大历史版本数
     * @return new HotSwapClassLoader | 新的热替换类加载器
     */
    public static HotSwapClassLoader create(int maxHistoryVersions) {
        return new HotSwapClassLoader(Thread.currentThread().getContextClassLoader(), maxHistoryVersions);
    }

    /**
     * Create HotSwap classloader with specified parent and max history versions
     * 使用指定父加载器和最大历史版本数创建热替换类加载器
     *
     * @param parent             parent classloader | 父类加载器
     * @param maxHistoryVersions max history versions to keep | 保留的最大历史版本数
     * @return new HotSwapClassLoader | 新的热替换类加载器
     */
    public static HotSwapClassLoader create(ClassLoader parent, int maxHistoryVersions) {
        return new HotSwapClassLoader(parent, maxHistoryVersions);
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

        // Use compute() to atomically read current info, push history, and store new version.
        // This prevents concurrent loadClass calls for the same name from corrupting history.
        byte[] bytecodeCopy = bytecode.clone();
        int[] versions = new int[2]; // [0]=oldVersion, [1]=newVersion

        ClassInfo newInfo = classInfoMap.compute(name, (n, currentInfo) -> {
            if (currentInfo != null) {
                versions[0] = currentInfo.version();
                Deque<ClassInfo> history = historyMap.computeIfAbsent(n, k -> new ConcurrentLinkedDeque<>());
                history.addLast(currentInfo);
                while (history.size() > maxHistoryVersions) {
                    history.removeFirst();
                }
            }

            int version = versionMap.computeIfAbsent(n, k -> new AtomicInteger(0)).incrementAndGet();
            versions[1] = version;
            // Use a disposable child classloader for each definition to allow reloading
            // the same class name. JVM does not allow defineClass() for the same name
            // on the same ClassLoader instance.
            SingleClassLoader child = new SingleClassLoader(HotSwapClassLoader.this, n, bytecodeCopy);
            Class<?> clazz = child.defineClassFromBytecode();
            return new ClassInfo(clazz, bytecodeCopy, version, child);
        });

        // Notify listeners outside the compute (exceptions must not affect loading)
        for (HotSwapListener listener : listeners) {
            try {
                listener.onSwap(name, versions[0], versions[1]);
            } catch (Exception ignored) {
                // Listener exceptions must not affect class loading
            }
        }

        return newInfo.clazz();
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

    // ==================== Rollback | 版本回退 ====================

    /**
     * Rollback to previous version of a class
     * 回退到类的上一个版本
     *
     * @param className class name | 类名
     * @return the previous version class, or empty if no history | 上一个版本的类，无历史返回 empty
     */
    public Optional<Class<?>> rollback(String className) {
        checkNotClosed();
        Objects.requireNonNull(className, "Class name must not be null");

        // Use compute() to atomically swap current with previous from history.
        // This prevents concurrent rollback/loadClass from corrupting state.
        int[] versions = new int[2]; // [0]=oldVersion, [1]=restoredVersion
        Class<?>[] result = new Class<?>[1];

        classInfoMap.compute(className, (name, currentInfo) -> {
            Deque<ClassInfo> history = historyMap.get(name);
            if (history == null || history.isEmpty()) {
                return currentInfo; // no change
            }

            ClassInfo previous = history.removeLast();
            versions[0] = currentInfo != null ? currentInfo.version() : 0;
            versions[1] = previous.version();
            result[0] = previous.clazz();

            versionMap.computeIfAbsent(name, k -> new AtomicInteger(0)).set(previous.version());
            return previous;
        });

        if (result[0] == null) {
            return Optional.empty();
        }

        // Notify listeners about rollback (outside compute)
        for (HotSwapListener listener : listeners) {
            try {
                listener.onSwap(className, versions[0], versions[1]);
            } catch (Exception ignored) {
                // Listener exceptions must not affect rollback
            }
        }

        return Optional.of(result[0]);
    }

    /**
     * Get version history count for a class
     * 获取类的历史版本数量
     *
     * @param className class name | 类名
     * @return history count | 历史版本数量
     */
    public int getHistoryCount(String className) {
        Deque<ClassInfo> history = historyMap.get(className);
        return history != null ? history.size() : 0;
    }

    // ==================== Listeners | 事件监听 ====================

    /**
     * Add hot-swap event listener
     * 添加热替换事件监听器
     *
     * @param listener listener to add | 要添加的监听器
     */
    public void addListener(HotSwapListener listener) {
        Objects.requireNonNull(listener, "Listener must not be null");
        listeners.add(listener);
    }

    /**
     * Remove hot-swap event listener
     * 移除热替换事件监听器
     *
     * @param listener listener to remove | 要移除的监听器
     */
    public void removeListener(HotSwapListener listener) {
        Objects.requireNonNull(listener, "Listener must not be null");
        listeners.remove(listener);
    }

    /**
     * Get max history versions setting
     * 获取最大历史版本设置
     *
     * @return max history versions | 最大历史版本数
     */
    public int getMaxHistoryVersions() {
        return maxHistoryVersions;
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
        historyMap.clear();
    }

    // ==================== Lifecycle | 生命周期 ====================

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            classInfoMap.clear();
            versionMap.clear();
            historyMap.clear();
            listeners.clear();
        }
    }

    /**
     * Check if classloader is closed
     * 检查类加载器是否已关闭
     *
     * @return true if closed | 已关闭返回 true
     */
    public boolean isClosed() {
        return closed.get();
    }

    private void checkNotClosed() {
        if (closed.get()) {
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
