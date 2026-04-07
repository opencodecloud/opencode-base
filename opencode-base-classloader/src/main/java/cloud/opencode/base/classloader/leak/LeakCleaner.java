package cloud.opencode.base.classloader.leak;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for cleaning up resources tied to a ClassLoader to prevent memory leaks
 * 用于清理绑定到 ClassLoader 的资源以防止内存泄漏的工具类
 *
 * <p>When a web application or plugin is unloaded, its ClassLoader should become eligible
 * for garbage collection. However, certain JDK and library resources (JDBC drivers,
 * ThreadLocals, shutdown hooks, timers) can pin the ClassLoader in memory. This utility
 * provides best-effort cleanup of these resources.</p>
 *
 * <p>当 Web 应用程序或插件被卸载时，其 ClassLoader 应该变得可被垃圾回收。
 * 但是，某些 JDK 和库资源（JDBC 驱动、ThreadLocal、关闭钩子、计时器）
 * 可能会将 ClassLoader 钉在内存中。此工具提供这些资源的尽力清理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JDBC driver deregistration (public API) - JDBC 驱动注销（公共 API）</li>
 *   <li>ThreadLocal cleanup (reflection-based, best-effort) - ThreadLocal 清理（基于反射，尽力而为）</li>
 *   <li>Shutdown hook removal (reflection-based, best-effort) - 关闭钩子移除（基于反射，尽力而为）</li>
 *   <li>Timer thread cancellation (best-effort) - 计时器线程取消（尽力而为）</li>
 *   <li>Combined cleanup with aggregated report - 组合清理与汇总报告</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Clean all resources tied to a ClassLoader
 * CleanupReport report = LeakCleaner.cleanAll(myClassLoader);
 * System.out.println("Drivers removed: " + report.jdbcDriversRemoved());
 *
 * // Clean only JDBC drivers
 * int count = LeakCleaner.cleanJdbcDrivers(myClassLoader);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是 (无状态工具类)</li>
 *   <li>Uses reflection where necessary; gracefully degrades under module restrictions -
 *       必要时使用反射；在模块限制下优雅降级</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public final class LeakCleaner {

    private static final System.Logger LOGGER = System.getLogger(LeakCleaner.class.getName());

    private LeakCleaner() {
        throw new AssertionError("No instances");
    }

    /**
     * Run all cleanup operations for the given ClassLoader and return an aggregated report
     * 对给定的 ClassLoader 运行所有清理操作并返回汇总报告
     *
     * <p>Each cleanup operation is independent — a failure in one does not prevent
     * the others from executing. All errors are collected in the returned report.</p>
     *
     * <p>每个清理操作都是独立的 — 一个操作的失败不会阻止其他操作的执行。
     * 所有错误都收集在返回的报告中。</p>
     *
     * @param classLoader the ClassLoader whose resources should be cleaned |
     *                    应清理其资源的 ClassLoader
     * @return aggregated cleanup report | 汇总的清理报告
     * @throws NullPointerException if classLoader is null | 当 classLoader 为 null 时
     */
    public static CleanupReport cleanAll(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader must not be null");

        List<String> errors = new ArrayList<>();

        int jdbcCount = 0;
        try {
            jdbcCount = cleanJdbcDrivers(classLoader);
        } catch (Exception e) {
            String msg = "Failed to clean JDBC drivers: " + e.getMessage();
            LOGGER.log(System.Logger.Level.WARNING, msg, e);
            errors.add(msg);
        }

        int threadLocalCount = 0;
        try {
            threadLocalCount = cleanThreadLocals(classLoader);
        } catch (Exception e) {
            String msg = "Failed to clean ThreadLocals: " + e.getMessage();
            LOGGER.log(System.Logger.Level.WARNING, msg, e);
            errors.add(msg);
        }

        int hookCount = 0;
        try {
            hookCount = cleanShutdownHooks(classLoader);
        } catch (Exception e) {
            String msg = "Failed to clean shutdown hooks: " + e.getMessage();
            LOGGER.log(System.Logger.Level.WARNING, msg, e);
            errors.add(msg);
        }

        int timerCount = 0;
        try {
            timerCount = cleanTimers(classLoader);
        } catch (Exception e) {
            String msg = "Failed to clean timers: " + e.getMessage();
            LOGGER.log(System.Logger.Level.WARNING, msg, e);
            errors.add(msg);
        }

        return new CleanupReport(threadLocalCount, jdbcCount, hookCount, timerCount, errors);
    }

    /**
     * Deregister JDBC drivers loaded by the given ClassLoader
     * 注销由给定 ClassLoader 加载的 JDBC 驱动
     *
     * <p>Uses the public {@link DriverManager#drivers()} API (JDK 9+) to enumerate
     * registered drivers and deregisters those whose class was loaded by the
     * specified ClassLoader.</p>
     *
     * <p>使用公共 {@link DriverManager#drivers()} API（JDK 9+）枚举已注册的驱动，
     * 并注销由指定 ClassLoader 加载的驱动。</p>
     *
     * @param classLoader the ClassLoader whose drivers should be removed |
     *                    应移除其驱动的 ClassLoader
     * @return the number of drivers deregistered | 已注销的驱动数
     * @throws NullPointerException if classLoader is null | 当 classLoader 为 null 时
     */
    public static int cleanJdbcDrivers(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader must not be null");
        int count = 0;

        // Collect drivers first to avoid ConcurrentModificationException
        List<Driver> toRemove = new ArrayList<>();
        Iterator<Driver> it = DriverManager.drivers().iterator();
        while (it.hasNext()) {
            Driver driver = it.next();
            if (driver.getClass().getClassLoader() == classLoader) {
                toRemove.add(driver);
            }
        }

        for (Driver driver : toRemove) {
            try {
                DriverManager.deregisterDriver(driver);
                count++;
                LOGGER.log(System.Logger.Level.INFO,
                        "Deregistered JDBC driver: {0}", driver.getClass().getName());
            } catch (SQLException e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Failed to deregister JDBC driver {0}: {1}",
                        driver.getClass().getName(), e.getMessage());
            }
        }
        return count;
    }

    /**
     * Clean ThreadLocal entries that reference classes loaded by the given ClassLoader
     * 清理引用由给定 ClassLoader 加载的类的 ThreadLocal 条目
     *
     * <p>Iterates all live threads and uses reflection to access each thread's
     * {@code threadLocals} field. If the module system prevents access, this method
     * logs a warning and returns 0.</p>
     *
     * <p>遍历所有活动线程，使用反射访问每个线程的 {@code threadLocals} 字段。
     * 如果模块系统阻止访问，此方法记录警告并返回 0。</p>
     *
     * @param classLoader the ClassLoader whose ThreadLocal entries should be cleared |
     *                    应清除其 ThreadLocal 条目的 ClassLoader
     * @return the number of ThreadLocal entries cleared | 已清除的 ThreadLocal 条目数
     * @throws NullPointerException if classLoader is null | 当 classLoader 为 null 时
     */
    public static int cleanThreadLocals(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader must not be null");
        int count = 0;

        Field threadLocalsField;
        Field inheritableThreadLocalsField;
        Field tableField;
        Field entryValueField;
        try {
            threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);

            inheritableThreadLocalsField = Thread.class.getDeclaredField("inheritableThreadLocals");
            inheritableThreadLocalsField.setAccessible(true);

            // ThreadLocalMap is an inner class of ThreadLocal
            Class<?> threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            tableField = threadLocalMapClass.getDeclaredField("table");
            tableField.setAccessible(true);

            // Entry extends WeakReference<ThreadLocal<?>>
            Class<?> entryClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap$Entry");
            entryValueField = entryClass.getDeclaredField("value");
            entryValueField.setAccessible(true);
        } catch (NoSuchFieldException | ClassNotFoundException | InaccessibleObjectException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Cannot access ThreadLocal internals (module system restriction): {0}", e.getMessage());
            return 0;
        }

        for (Thread thread : enumerateThreads()) {
            count += cleanThreadLocalMap(thread, threadLocalsField, tableField, entryValueField, classLoader);
            count += cleanThreadLocalMap(thread, inheritableThreadLocalsField, tableField, entryValueField, classLoader);
        }
        return count;
    }

    /**
     * Enumerate all live threads without capturing stack traces
     * 枚举所有活动线程，不捕获栈跟踪
     *
     * <p>Uses ThreadGroup traversal, which is much cheaper than
     * Thread.getAllStackTraces() that performs a STW stack dump.</p>
     * <p>使用 ThreadGroup 遍历，比执行 STW 栈转储的
     * Thread.getAllStackTraces() 代价低得多。</p>
     */
    private static Thread[] enumerateThreads() {
        ThreadGroup root = Thread.currentThread().getThreadGroup();
        while (root.getParent() != null) {
            root = root.getParent();
        }
        int estimate = root.activeCount() + 16;
        Thread[] threads;
        int count;
        do {
            threads = new Thread[estimate];
            count = root.enumerate(threads, true);
            if (count < threads.length) {
                break;
            }
            // Double estimate; cap at 1M to prevent runaway allocation
            estimate = Math.min(estimate * 2, 1_000_000);
        } while (true);
        return java.util.Arrays.copyOf(threads, count);
    }

    /**
     * Clean a single ThreadLocal map for a thread
     * 为线程清理单个 ThreadLocal 映射
     */
    private static int cleanThreadLocalMap(Thread thread, Field mapField, Field tableField,
                                            Field entryValueField, ClassLoader classLoader) {
        int count = 0;
        try {
            Object map = mapField.get(thread);
            if (map == null) {
                return 0;
            }
            Object[] table = (Object[]) tableField.get(map);
            if (table == null) {
                return 0;
            }
            for (int i = 0; i < table.length; i++) {
                Object entry = table[i];
                if (entry == null) {
                    continue;
                }
                Object value = entryValueField.get(entry);
                if (value != null && isLoadedBy(value.getClass(), classLoader)) {
                    // Null the entire table slot to release both the WeakReference key
                    // and the value, matching Tomcat's JreMemoryLeakPreventionListener approach
                    table[i] = null;
                    count++;
                }
            }
        } catch (IllegalAccessException | InaccessibleObjectException e) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Cannot access ThreadLocal map for thread {0}: {1}",
                    thread.getName(), e.getMessage());
        }
        return count;
    }

    /**
     * Remove shutdown hooks registered by the given ClassLoader
     * 移除由给定 ClassLoader 注册的关闭钩子
     *
     * <p>Uses reflection to access the shutdown hooks registry in the Runtime.
     * If the module system prevents access, this method returns 0 with errors logged.</p>
     *
     * <p>使用反射访问 Runtime 中的关闭钩子注册表。
     * 如果模块系统阻止访问，此方法返回 0 并记录错误。</p>
     *
     * @param classLoader the ClassLoader whose shutdown hooks should be removed |
     *                    应移除其关闭钩子的 ClassLoader
     * @return the number of shutdown hooks removed | 已移除的关闭钩子数
     * @throws NullPointerException if classLoader is null | 当 classLoader 为 null 时
     */
    public static int cleanShutdownHooks(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader must not be null");
        int count = 0;

        // Shutdown hooks are Thread objects registered via Runtime.addShutdownHook
        // We can try to find and remove hooks whose Runnable/Thread was loaded by our CL
        // This is best-effort as the internal API varies by JDK version
        try {
            Class<?> shutdownClass = Class.forName("java.lang.ApplicationShutdownHooks");
            Field hooksField = shutdownClass.getDeclaredField("hooks");
            hooksField.setAccessible(true);

            @SuppressWarnings("unchecked")
            java.util.IdentityHashMap<Thread, Thread> hooks =
                    (java.util.IdentityHashMap<Thread, Thread>) hooksField.get(null);

            if (hooks == null) {
                return 0;
            }

            // Collect hooks to remove (synchronize on class as JDK does internally)
            List<Thread> toRemove;
            synchronized (shutdownClass) {
                toRemove = new ArrayList<>();
                for (Thread hook : hooks.keySet()) {
                    if (isLoadedBy(hook.getClass(), classLoader)) {
                        toRemove.add(hook);
                    }
                }
            }

            // Use public API to remove — respects JDK shutdown state machine
            for (Thread hook : toRemove) {
                try {
                    Runtime.getRuntime().removeShutdownHook(hook);
                    count++;
                    LOGGER.log(System.Logger.Level.INFO,
                            "Removed shutdown hook: {0}", hook.getClass().getName());
                } catch (IllegalStateException e) {
                    // JVM shutdown already in progress, hooks are running
                    LOGGER.log(System.Logger.Level.DEBUG,
                            "Cannot remove hook during shutdown: {0}", hook.getClass().getName());
                }
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException |
                 InaccessibleObjectException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Cannot access shutdown hooks (module system restriction): {0}", e.getMessage());
        }
        return count;
    }

    /**
     * Cancel timer threads loaded by the given ClassLoader
     * 取消由给定 ClassLoader 加载的计时器线程
     *
     * <p>Scans all live threads for those whose class was loaded by the given ClassLoader
     * and whose name or class suggests they are timer threads. Interrupts matching threads.</p>
     *
     * <p>扫描所有活动线程，查找其类由给定 ClassLoader 加载且其名称或类表明是计时器线程的线程。
     * 中断匹配的线程。</p>
     *
     * @param classLoader the ClassLoader whose timer threads should be cancelled |
     *                    应取消其计时器线程的 ClassLoader
     * @return the number of timer threads cancelled | 已取消的计时器线程数
     * @throws NullPointerException if classLoader is null | 当 classLoader 为 null 时
     */
    public static int cleanTimers(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader must not be null");
        int count = 0;

        for (Thread thread : enumerateThreads()) {
            if (!thread.isAlive()) {
                continue;
            }
            // Only target threads whose class was loaded by the target ClassLoader
            // (not threads that merely have it as context classloader — too broad)
            if (isLoadedBy(thread.getClass(), classLoader)) {
                String threadName = thread.getName();
                String className = thread.getClass().getName();
                // Match standard JDK Timer thread class name exactly, plus threads
                // whose class itself was loaded from the target CL with timer-like names
                if (className.equals("java.util.TimerThread") ||
                        className.contains("Timer") || className.contains("timer")) {
                    thread.interrupt();
                    count++;
                    LOGGER.log(System.Logger.Level.INFO,
                            "Interrupted timer thread: {0} ({1})", threadName, className);
                }
            }
        }
        return count;
    }

    /**
     * Check if a class was loaded by the given ClassLoader (identity match up the parent chain)
     * 检查类是否由给定的 ClassLoader 加载（沿父链进行身份匹配）
     *
     * @param clazz       the class to check | 要检查的类
     * @param classLoader the ClassLoader to check against | 要检查的 ClassLoader
     * @return true if the class's loader or any of its ancestors is the given ClassLoader |
     *         如果类的加载器或其任何祖先是给定的 ClassLoader 则返回 true
     */
    private static boolean isLoadedBy(Class<?> clazz, ClassLoader classLoader) {
        ClassLoader cl = clazz.getClassLoader();
        while (cl != null) {
            if (cl == classLoader) {
                return true;
            }
            cl = cl.getParent();
        }
        return false;
    }
}
