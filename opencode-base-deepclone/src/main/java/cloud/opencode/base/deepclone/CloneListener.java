package cloud.opencode.base.deepclone;

/**
 * Listener interface for clone lifecycle events
 * 克隆生命周期事件的监听器接口
 *
 * <p>Implementations can observe and react to events during the cloning process,
 * such as before and after an object is cloned, or when an error occurs.</p>
 * <p>实现可以观察和响应克隆过程中的事件，如对象克隆前后，或发生错误时。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.3
 */
public interface CloneListener {

    /**
     * Called before an object is cloned
     * 在对象克隆之前调用
     *
     * @param original the original object | 原始对象
     * @param context  the clone context | 克隆上下文
     */
    default void beforeClone(Object original, CloneContext context) {
        // Default no-op
    }

    /**
     * Called after an object is cloned
     * 在对象克隆之后调用
     *
     * @param original the original object | 原始对象
     * @param cloned   the cloned object | 克隆的对象
     * @param context  the clone context | 克隆上下文
     */
    default void afterClone(Object original, Object cloned, CloneContext context) {
        // Default no-op
    }

    /**
     * Called when a clone error occurs
     * 当克隆错误发生时调用
     *
     * @param original the original object | 原始对象
     * @param error    the error | 错误
     * @param context  the clone context | 克隆上下文
     */
    default void onError(Object original, Throwable error, CloneContext context) {
        // Default no-op
    }
}
