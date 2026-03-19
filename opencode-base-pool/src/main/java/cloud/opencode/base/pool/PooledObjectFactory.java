package cloud.opencode.base.pool;

import cloud.opencode.base.pool.exception.OpenPoolException;

/**
 * PooledObjectFactory - Pooled Object Factory Interface
 * PooledObjectFactory - 池化对象工厂接口
 *
 * <p>Factory interface for creating and managing pooled objects throughout
 * their lifecycle: creation, activation, validation, passivation, and destruction.</p>
 * <p>用于在整个生命周期中创建和管理池化对象的工厂接口：创建、激活、验证、钝化和销毁。</p>
 *
 * <p><strong>Object Lifecycle | 对象生命周期:</strong></p>
 * <pre>{@code
 * makeObject()      -> Create new object
 * activateObject()  -> Prepare object for use (before borrow)
 * validateObject()  -> Check if object is valid
 * passivateObject() -> Reset object state (after return)
 * destroyObject()   -> Cleanup and release resources
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Object creation - 对象创建</li>
 *   <li>Object activation before borrow - 借用前激活对象</li>
 *   <li>Object validation - 对象验证</li>
 *   <li>Object passivation after return - 归还后钝化对象</li>
 *   <li>Object destruction - 对象销毁</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PooledObjectFactory<Connection> factory = new PooledObjectFactory<>() {
 *     @Override
 *     public PooledObject<Connection> makeObject() {
 *         return new DefaultPooledObject<>(DriverManager.getConnection(url));
 *     }
 *
 *     @Override
 *     public boolean validateObject(PooledObject<Connection> obj) {
 *         return obj.getObject().isValid(1);
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
public interface PooledObjectFactory<T> {

    /**
     * Creates a new pooled object.
     * 创建新的池化对象。
     *
     * <p>Called when the pool needs to create a new object.</p>
     * <p>当池需要创建新对象时调用。</p>
     *
     * @return the new pooled object - 新的池化对象
     * @throws OpenPoolException if creation fails - 如果创建失败
     */
    PooledObject<T> makeObject() throws OpenPoolException;

    /**
     * Destroys a pooled object.
     * 销毁池化对象。
     *
     * <p>Called when the object is being removed from the pool.</p>
     * <p>当对象从池中移除时调用。</p>
     *
     * @param obj the object to destroy - 要销毁的对象
     * @throws OpenPoolException if destruction fails - 如果销毁失败
     */
    void destroyObject(PooledObject<T> obj) throws OpenPoolException;

    /**
     * Validates a pooled object.
     * 验证池化对象。
     *
     * <p>Called to check if the object is still valid for use.</p>
     * <p>调用以检查对象是否仍可使用。</p>
     *
     * @param obj the object to validate - 要验证的对象
     * @return true if valid, false otherwise - 有效返回true，否则返回false
     */
    boolean validateObject(PooledObject<T> obj);

    /**
     * Activates a pooled object.
     * 激活池化对象。
     *
     * <p>Called before the object is borrowed from the pool.</p>
     * <p>在对象从池中借出前调用。</p>
     *
     * @param obj the object to activate - 要激活的对象
     * @throws OpenPoolException if activation fails - 如果激活失败
     */
    void activateObject(PooledObject<T> obj) throws OpenPoolException;

    /**
     * Passivates a pooled object.
     * 钝化池化对象。
     *
     * <p>Called after the object is returned to the pool.</p>
     * <p>在对象归还到池后调用。</p>
     *
     * @param obj the object to passivate - 要钝化的对象
     * @throws OpenPoolException if passivation fails - 如果钝化失败
     */
    void passivateObject(PooledObject<T> obj) throws OpenPoolException;
}
