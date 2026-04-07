package cloud.opencode.base.classloader.loader;

/**
 * Listener for class hot-swap events
 * 类热替换事件监听器
 *
 * <p>Functional interface that receives notifications when a class is hot-swapped
 * to a new version.</p>
 * <p>函数式接口，当类被热替换为新版本时接收通知。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see HotSwapClassLoader
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@FunctionalInterface
public interface HotSwapListener {

    /**
     * Called when a class is hot-swapped
     * 当类被热替换时调用
     *
     * @param className  class name | 类名
     * @param oldVersion old version number | 旧版本号
     * @param newVersion new version number | 新版本号
     */
    void onSwap(String className, int oldVersion, int newVersion);
}
