package cloud.opencode.base.classloader.leak;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Global singleton detector for ClassLoader memory leaks
 * ClassLoader 内存泄漏的全局单例检测器
 *
 * <p>Uses {@link PhantomReference} and a {@link ReferenceQueue} to detect ClassLoader
 * instances that are garbage-collected without being explicitly closed (untracked).
 * A virtual-thread daemon continuously polls the reference queue and generates
 * {@link LeakReport} entries for any leaked ClassLoaders.</p>
 *
 * <p>使用 {@link PhantomReference} 和 {@link ReferenceQueue} 检测未显式关闭（取消追踪）
 * 就被垃圾回收的 ClassLoader 实例。一个虚拟线程守护进程持续轮询引用队列，
 * 并为任何泄漏的 ClassLoader 生成 {@link LeakReport} 条目。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public final class LeakDetector {

    private static final System.Logger LOGGER = System.getLogger(LeakDetector.class.getName());
    private static final int MAX_LEAK_REPORTS = 1000;

    /**
     * Lazy holder pattern — instance is created only when first accessed.
     * 延迟持有者模式 — 实例仅在首次访问时创建。
     */
    private static final class Holder {
        static final LeakDetector INSTANCE = new LeakDetector();
    }

    private final ReferenceQueue<ClassLoader> referenceQueue = new ReferenceQueue<>();

    /**
     * Map from PhantomReference to tracking info.
     * Keyed by PhantomReference (unique per registration) to avoid identity hash code collisions.
     * 从 PhantomReference 到追踪信息的映射。
     * 以 PhantomReference（每次注册唯一）为 key，避免 identityHashCode 碰撞。
     */
    private final ConcurrentHashMap<PhantomReference<ClassLoader>, TrackingInfo> trackingMap = new ConcurrentHashMap<>();

    /**
     * Reverse lookup: ClassLoader identity → PhantomReference, for untrack().
     * Guarded by synchronized(reverseLock).
     * 反向查找：ClassLoader 身份 → PhantomReference，用于 untrack()。
     * 由 synchronized(reverseLock) 保护。
     */
    private final WeakHashMap<ClassLoader, PhantomReference<ClassLoader>> reverseMap = new WeakHashMap<>();
    private final Object reverseLock = new Object();

    private final List<LeakReport> leakReports = new CopyOnWriteArrayList<>();

    /**
     * Internal tracking metadata for a registered ClassLoader
     * 已注册 ClassLoader 的内部追踪元数据
     */
    private record TrackingInfo(
            String name,
            LeakDetection level,
            StackTraceElement[] creationStack,
            long createdAtNanos
    ) {
    }

    private final Thread pollThread;

    private LeakDetector() {
        pollThread = Thread.ofVirtual()
                .name("leak-detector-poll")
                .start(this::pollLoop);
    }

    /**
     * Shutdown the leak detector poll thread
     * 关闭泄漏检测器轮询线程
     *
     * <p>Interrupts the poll thread. Primarily useful for orderly shutdown
     * in application servers or test environments.</p>
     * <p>中断轮询线程。主要用于应用服务器或测试环境中的有序关闭。</p>
     */
    public void shutdown() {
        pollThread.interrupt();
    }

    /**
     * Returns the global singleton instance (lazy-initialized on first call)
     * 返回全局单例实例（首次调用时延迟初始化）
     *
     * @return the singleton LeakDetector | 单例 LeakDetector
     */
    public static LeakDetector getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Register a ClassLoader for leak tracking
     * 注册一个 ClassLoader 进行泄漏追踪
     *
     * <p>If the level is {@link LeakDetection#DISABLED}, this method is a no-op.</p>
     * <p>如果级别为 {@link LeakDetection#DISABLED}，此方法不执行任何操作。</p>
     *
     * @param classLoader the ClassLoader to track | 要追踪的 ClassLoader
     * @param name        a descriptive name for the ClassLoader | ClassLoader 的描述性名称
     * @param level       the detection level | 检测级别
     * @throws NullPointerException if any argument is null | 当任何参数为 null 时
     */
    public void track(ClassLoader classLoader, String name, LeakDetection level) {
        Objects.requireNonNull(classLoader, "classLoader must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(level, "level must not be null");

        if (level == LeakDetection.DISABLED) {
            return;
        }

        StackTraceElement[] creationStack = (level == LeakDetection.PARANOID)
                ? Thread.currentThread().getStackTrace()
                : new StackTraceElement[0];

        PhantomReference<ClassLoader> ref = new PhantomReference<>(classLoader, referenceQueue);

        TrackingInfo info = new TrackingInfo(name, level, creationStack, System.nanoTime());

        trackingMap.put(ref, info);
        synchronized (reverseLock) {
            reverseMap.put(classLoader, ref);
        }
    }

    /**
     * Unregister a ClassLoader from leak tracking (called on close)
     * 取消注册 ClassLoader 的泄漏追踪（在 close 时调用）
     *
     * @param classLoader the ClassLoader to untrack | 要取消追踪的 ClassLoader
     * @throws NullPointerException if classLoader is null | 当 classLoader 为 null 时
     */
    public void untrack(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader must not be null");

        PhantomReference<ClassLoader> ref;
        synchronized (reverseLock) {
            ref = reverseMap.remove(classLoader);
        }
        if (ref != null) {
            trackingMap.remove(ref);
            ref.clear();
        }
    }

    /**
     * Returns the list of detected leak reports
     * 返回已检测到的泄漏报告列表
     *
     * @return unmodifiable list of leak reports | 不可修改的泄漏报告列表
     */
    public List<LeakReport> getLeakReports() {
        return List.copyOf(leakReports);
    }

    /**
     * Returns the number of currently tracked ClassLoaders
     * 返回当前被追踪的 ClassLoader 数量
     *
     * @return tracked count | 追踪数量
     */
    public int getTrackedCount() {
        return trackingMap.size();
    }

    /**
     * Clear all leak reports
     * 清除所有泄漏报告
     */
    public void clearReports() {
        leakReports.clear();
    }

    /**
     * Daemon poll loop that processes enqueued phantom references
     * 守护轮询循环，处理入队的虚引用
     */
    private void pollLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                var ref = referenceQueue.remove(); // blocks until available
                if (ref instanceof PhantomReference<?> phantomRef) {
                    @SuppressWarnings("unchecked")
                    PhantomReference<ClassLoader> clRef = (PhantomReference<ClassLoader>) phantomRef;
                    TrackingInfo info = trackingMap.remove(clRef);
                    if (info != null) {
                        LeakReport report = new LeakReport(
                                info.name(),
                                info.level(),
                                info.creationStack(),
                                0,
                                info.createdAtNanos()
                        );
                        // Cap report list to prevent unbounded growth
                        if (leakReports.size() < MAX_LEAK_REPORTS) {
                            leakReports.add(report);
                        }
                        LOGGER.log(System.Logger.Level.WARNING,
                                "ClassLoader leak detected: {0}", report);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
