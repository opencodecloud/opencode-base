package cloud.opencode.base.pool.factory;

import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.PooledObjectFactory;
import cloud.opencode.base.pool.exception.OpenPoolException;

/**
 * BasePooledObjectFactory - Base Pooled Object Factory
 * BasePooledObjectFactory - 基础池化对象工厂
 *
 * <p>Abstract base class for PooledObjectFactory implementations.
 * Subclasses only need to implement the create() method.</p>
 * <p>PooledObjectFactory实现的抽象基类。子类只需实现create()方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Simplified factory implementation - 简化的工厂实现</li>
 *   <li>Default no-op lifecycle methods - 默认空操作的生命周期方法</li>
 *   <li>Automatic object wrapping - 自动对象包装</li>
 *   <li>Override only what you need - 只覆盖需要的方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PooledObjectFactory<StringBuilder> factory = new BasePooledObjectFactory<>() {
 *     @Override
 *     protected StringBuilder create() {
 *         return new StringBuilder();
 *     }
 *
 *     @Override
 *     public void passivateObject(PooledObject<StringBuilder> obj) {
 *         obj.getObject().setLength(0);  // Reset on return
 *     }
 * };
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @param <T> the type of object being pooled - 池化对象类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public abstract class BasePooledObjectFactory<T> implements PooledObjectFactory<T> {

    /**
     * Creates the actual object instance.
     * 创建实际的对象实例。
     *
     * <p>Subclasses must implement this method to create the object.</p>
     * <p>子类必须实现此方法来创建对象。</p>
     *
     * @return the new object - 新对象
     * @throws OpenPoolException if creation fails - 如果创建失败
     */
    protected abstract T create() throws OpenPoolException;

    /**
     * Wraps an object in a PooledObject wrapper.
     * 将对象包装在PooledObject包装器中。
     *
     * @param obj the object to wrap - 要包装的对象
     * @return the wrapped object - 包装后的对象
     */
    protected PooledObject<T> wrap(T obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public PooledObject<T> makeObject() throws OpenPoolException {
        return wrap(create());
    }

    @Override
    public void destroyObject(PooledObject<T> obj) throws OpenPoolException {
        // Default: no-op, subclasses can override
    }

    @Override
    public boolean validateObject(PooledObject<T> obj) {
        return true;
    }

    @Override
    public void activateObject(PooledObject<T> obj) throws OpenPoolException {
        // Default: no-op, subclasses can override
    }

    @Override
    public void passivateObject(PooledObject<T> obj) throws OpenPoolException {
        // Default: no-op, subclasses can override
    }
}
