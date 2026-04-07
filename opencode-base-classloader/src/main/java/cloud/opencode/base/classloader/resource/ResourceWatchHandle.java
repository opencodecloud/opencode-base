package cloud.opencode.base.classloader.resource;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Resource Watch Handle - Handle for a resource watch registration
 * 资源监听句柄 - 资源监听注册的句柄
 *
 * <p>AutoCloseable handle that allows cancelling a resource watch registration.
 * Closing the handle removes the associated watch callback.</p>
 * <p>可自动关闭的句柄，允许取消资源监听注册。关闭句柄会移除关联的监听回调。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (ResourceWatchHandle handle = watcher.watch(path, event -> {
 *     System.out.println("File changed: " + event.type());
 * })) {
 *     // handle is auto-closed when leaving try block
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public class ResourceWatchHandle implements AutoCloseable {

    private final Runnable cancelAction;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Create a new ResourceWatchHandle
     * 创建新的 ResourceWatchHandle
     *
     * @param cancelAction action to run on close | 关闭时执行的动作
     * @throws NullPointerException if cancelAction is null | 如果 cancelAction 为 null 则抛出
     */
    public ResourceWatchHandle(Runnable cancelAction) {
        this.cancelAction = Objects.requireNonNull(cancelAction, "Cancel action must not be null");
    }

    /**
     * Close the handle and cancel the watch registration.
     * Idempotent: subsequent calls are no-ops.
     * 关闭句柄并取消监听注册。幂等：后续调用无操作。
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            cancelAction.run();
        }
    }

    /**
     * Check if this handle has been closed
     * 检查句柄是否已关闭
     *
     * @return true if closed | 已关闭返回 true
     */
    public boolean isClosed() {
        return closed.get();
    }
}
