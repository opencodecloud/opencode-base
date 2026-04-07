package cloud.opencode.base.pool.factory;

import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.PooledObjectFactory;
import cloud.opencode.base.pool.exception.OpenPoolException;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * SimplePooledObjectFactory - Functional Pooled Object Factory
 * SimplePooledObjectFactory - 函数式池化对象工厂
 *
 * <p>A simplified {@link PooledObjectFactory} implementation that accepts
 * {@link Supplier}, {@link Consumer}, and {@link Predicate} for the common
 * use case where subclassing {@link BasePooledObjectFactory} is overkill.</p>
 * <p>简化的 {@link PooledObjectFactory} 实现，接受 {@link Supplier}、
 * {@link Consumer} 和 {@link Predicate}，适用于继承
 * {@link BasePooledObjectFactory} 过于繁重的常见场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional-style factory creation - 函数式工厂创建</li>
 *   <li>Builder pattern for optional parameters - 可选参数的建造者模式</li>
 *   <li>Convenient static factory methods - 便捷的静态工厂方法</li>
 *   <li>Default no-op for optional lifecycle hooks - 可选生命周期钩子默认空操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simplest case: creator only
 * PooledObjectFactory<StringBuilder> factory =
 *     SimplePooledObjectFactory.of(StringBuilder::new);
 *
 * // With destroyer
 * PooledObjectFactory<Connection> factory =
 *     SimplePooledObjectFactory.of(
 *         () -> DriverManager.getConnection(url),
 *         Connection::close
 *     );
 *
 * // Full builder
 * PooledObjectFactory<Connection> factory =
 *     SimplePooledObjectFactory.<Connection>builder(() -> DriverManager.getConnection(url))
 *         .destroyer(Connection::close)
 *         .validator(conn -> conn.isValid(1))
 *         .activator(conn -> conn.setAutoCommit(true))
 *         .passivator(conn -> conn.setAutoCommit(false))
 *         .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是 (构造后不可变)</li>
 *   <li>Null-safe: Creator must not be null - 空值安全: 创建者不能为空</li>
 * </ul>
 *
 * @param <T> the type of object being pooled - 池化对象类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.3
 */
public final class SimplePooledObjectFactory<T> implements PooledObjectFactory<T> {

    private static final Consumer<?> NO_OP_CONSUMER = obj -> { };
    private static final Predicate<?> ALWAYS_TRUE = obj -> true;

    private final Supplier<T> creator;
    private final Consumer<T> destroyer;
    private final Predicate<T> validator;
    private final Consumer<T> activator;
    private final Consumer<T> passivator;

    private SimplePooledObjectFactory(Supplier<T> creator,
                                      Consumer<T> destroyer,
                                      Predicate<T> validator,
                                      Consumer<T> activator,
                                      Consumer<T> passivator) {
        this.creator = creator;
        this.destroyer = destroyer;
        this.validator = validator;
        this.activator = activator;
        this.passivator = passivator;
    }

    /**
     * Creates a factory with only a creator.
     * 仅使用创建者创建工厂。
     *
     * <p>Destroyer is no-op, validator always returns true,
     * activator and passivator are no-op.</p>
     * <p>销毁器为空操作，验证器始终返回 true，
     * 激活器和钝化器为空操作。</p>
     *
     * @param <T>     the type of object being pooled - 池化对象类型
     * @param creator the object creator (must not be null) - 对象创建者（不能为空）
     * @return the factory - 工厂
     * @throws NullPointerException if creator is null - 如果创建者为空
     */
    @SuppressWarnings("unchecked")
    public static <T> SimplePooledObjectFactory<T> of(Supplier<T> creator) {
        Objects.requireNonNull(creator, "creator must not be null");
        return new SimplePooledObjectFactory<>(
                creator,
                (Consumer<T>) NO_OP_CONSUMER,
                (Predicate<T>) ALWAYS_TRUE,
                (Consumer<T>) NO_OP_CONSUMER,
                (Consumer<T>) NO_OP_CONSUMER
        );
    }

    /**
     * Creates a factory with a creator and destroyer.
     * 使用创建者和销毁器创建工厂。
     *
     * <p>Validator always returns true, activator and passivator are no-op.</p>
     * <p>验证器始终返回 true，激活器和钝化器为空操作。</p>
     *
     * @param <T>       the type of object being pooled - 池化对象类型
     * @param creator   the object creator (must not be null) - 对象创建者（不能为空）
     * @param destroyer the object destroyer (must not be null) - 对象销毁器（不能为空）
     * @return the factory - 工厂
     * @throws NullPointerException if creator or destroyer is null - 如果创建者或销毁器为空
     */
    @SuppressWarnings("unchecked")
    public static <T> SimplePooledObjectFactory<T> of(Supplier<T> creator, Consumer<T> destroyer) {
        Objects.requireNonNull(creator, "creator must not be null");
        Objects.requireNonNull(destroyer, "destroyer must not be null");
        return new SimplePooledObjectFactory<>(
                creator,
                destroyer,
                (Predicate<T>) ALWAYS_TRUE,
                (Consumer<T>) NO_OP_CONSUMER,
                (Consumer<T>) NO_OP_CONSUMER
        );
    }

    /**
     * Creates a builder with the specified creator.
     * 使用指定的创建者创建建造者。
     *
     * @param <T>     the type of object being pooled - 池化对象类型
     * @param creator the object creator (must not be null) - 对象创建者（不能为空）
     * @return the builder - 建造者
     * @throws NullPointerException if creator is null - 如果创建者为空
     */
    public static <T> Builder<T> builder(Supplier<T> creator) {
        return new Builder<>(creator);
    }

    // ==================== PooledObjectFactory Implementation ====================

    /**
     * {@inheritDoc}
     *
     * <p>Creates a new object using the configured creator supplier.</p>
     * <p>使用配置的创建者供应器创建新对象。</p>
     */
    @Override
    public PooledObject<T> makeObject() throws OpenPoolException {
        try {
            T obj = creator.get();
            return new DefaultPooledObject<>(obj);
        } catch (Exception e) {
            throw new OpenPoolException("Failed to create pooled object", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Destroys the object using the configured destroyer consumer.</p>
     * <p>使用配置的销毁器消费者销毁对象。</p>
     */
    @Override
    public void destroyObject(PooledObject<T> obj) throws OpenPoolException {
        try {
            destroyer.accept(obj.getObject());
        } catch (Exception e) {
            throw new OpenPoolException("Failed to destroy pooled object", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Validates the object using the configured validator predicate.</p>
     * <p>使用配置的验证器谓词验证对象。</p>
     */
    @Override
    public boolean validateObject(PooledObject<T> obj) {
        try {
            return validator.test(obj.getObject());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Activates the object using the configured activator consumer.</p>
     * <p>使用配置的激活器消费者激活对象。</p>
     */
    @Override
    public void activateObject(PooledObject<T> obj) throws OpenPoolException {
        try {
            activator.accept(obj.getObject());
        } catch (Exception e) {
            throw new OpenPoolException("Failed to activate pooled object", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Passivates the object using the configured passivator consumer.</p>
     * <p>使用配置的钝化器消费者钝化对象。</p>
     */
    @Override
    public void passivateObject(PooledObject<T> obj) throws OpenPoolException {
        try {
            passivator.accept(obj.getObject());
        } catch (Exception e) {
            throw new OpenPoolException("Failed to passivate pooled object", e);
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for SimplePooledObjectFactory.
     * SimplePooledObjectFactory 的建造者。
     *
     * <p>Provides a fluent API for configuring all lifecycle callbacks.</p>
     * <p>提供用于配置所有生命周期回调的流式 API。</p>
     *
     * @param <T> the type of object being pooled - 池化对象类型
     */
    public static final class Builder<T> {

        private final Supplier<T> creator;
        private Consumer<T> destroyer;
        private Predicate<T> validator;
        private Consumer<T> activator;
        private Consumer<T> passivator;

        @SuppressWarnings("unchecked")
        private Builder(Supplier<T> creator) {
            this.creator = Objects.requireNonNull(creator, "creator must not be null");
            this.destroyer = (Consumer<T>) NO_OP_CONSUMER;
            this.validator = (Predicate<T>) ALWAYS_TRUE;
            this.activator = (Consumer<T>) NO_OP_CONSUMER;
            this.passivator = (Consumer<T>) NO_OP_CONSUMER;
        }

        /**
         * Sets the destroyer callback.
         * 设置销毁器回调。
         *
         * @param destroyer the destroyer (must not be null) - 销毁器（不能为空）
         * @return this builder - 此建造者
         * @throws NullPointerException if destroyer is null - 如果销毁器为空
         */
        public Builder<T> destroyer(Consumer<T> destroyer) {
            this.destroyer = Objects.requireNonNull(destroyer, "destroyer must not be null");
            return this;
        }

        /**
         * Sets the validator callback.
         * 设置验证器回调。
         *
         * @param validator the validator (must not be null) - 验证器（不能为空）
         * @return this builder - 此建造者
         * @throws NullPointerException if validator is null - 如果验证器为空
         */
        public Builder<T> validator(Predicate<T> validator) {
            this.validator = Objects.requireNonNull(validator, "validator must not be null");
            return this;
        }

        /**
         * Sets the activator callback.
         * 设置激活器回调。
         *
         * @param activator the activator (must not be null) - 激活器（不能为空）
         * @return this builder - 此建造者
         * @throws NullPointerException if activator is null - 如果激活器为空
         */
        public Builder<T> activator(Consumer<T> activator) {
            this.activator = Objects.requireNonNull(activator, "activator must not be null");
            return this;
        }

        /**
         * Sets the passivator callback.
         * 设置钝化器回调。
         *
         * @param passivator the passivator (must not be null) - 钝化器（不能为空）
         * @return this builder - 此建造者
         * @throws NullPointerException if passivator is null - 如果钝化器为空
         */
        public Builder<T> passivator(Consumer<T> passivator) {
            this.passivator = Objects.requireNonNull(passivator, "passivator must not be null");
            return this;
        }

        /**
         * Builds the factory.
         * 构建工厂。
         *
         * @return the configured factory - 配置好的工厂
         */
        public SimplePooledObjectFactory<T> build() {
            return new SimplePooledObjectFactory<>(creator, destroyer, validator, activator, passivator);
        }
    }
}
