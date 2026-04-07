package cloud.opencode.base.reflect.sealed;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.util.*;
import java.util.function.Function;

/**
 * Type-Safe Sealed Class Dispatcher
 * 类型安全的密封类分发器
 *
 * <p>Provides exhaustive, type-safe dispatch over sealed class hierarchies,
 * similar to pattern matching but with compile-time handler registration
 * and runtime exhaustiveness validation.</p>
 * <p>提供密封类层次结构上的穷举式、类型安全分发，
 * 类似于模式匹配，但具有编译时处理器注册和运行时穷举性验证。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exhaustive dispatch validation - 穷举分发验证</li>
 *   <li>Multi-level sealed hierarchy support - 多层密封层次结构支持</li>
 *   <li>Safe dispatch with Optional return - 安全分发（返回Optional）</li>
 *   <li>Default handler fallback - 默认处理器回退</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * sealed interface Shape permits Circle, Rectangle {}
 * record Circle(double radius) implements Shape {}
 * record Rectangle(double w, double h) implements Shape {}
 *
 * var dispatcher = SealedDispatcher.builder(Shape.class, Double.class)
 *     .on(Circle.class, c -> Math.PI * c.radius() * c.radius())
 *     .on(Rectangle.class, r -> r.w() * r.h())
 *     .build();
 *
 * double area = dispatcher.dispatch(new Circle(5.0)); // 78.54...
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after build) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (dispatch throws NullPointerException for null input) - 空值安全: 否（null输入抛出NullPointerException）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(h) per dispatch where h is the hierarchy depth - 时间复杂度: 每次分发 O(h)，h为层次结构深度</li>
 *   <li>Space complexity: O(n) where n is the number of handlers - 空间复杂度: O(n)，n为处理器数量</li>
 * </ul>
 *
 * @param <T> the sealed type | 密封类型
 * @param <R> the result type | 结果类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see SealedUtil
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
public final class SealedDispatcher<T, R> {

    private final Class<T> sealedType;
    private final Map<Class<?>, Function<?, R>> handlers;
    private final Function<? super T, ? extends R> defaultHandler;

    private SealedDispatcher(Class<T> sealedType,
                             Map<Class<?>, Function<?, R>> handlers,
                             Function<? super T, ? extends R> defaultHandler) {
        this.sealedType = sealedType;
        this.handlers = Map.copyOf(handlers);
        this.defaultHandler = defaultHandler;
    }

    // ==================== Factory | 工厂方法 ====================

    /**
     * Creates a new builder for the given sealed type and result type
     * 为给定的密封类型和结果类型创建新的构建器
     *
     * @param sealedType the sealed class | 密封类
     * @param resultType the result class | 结果类
     * @param <T>        the sealed type | 密封类型
     * @param <R>        the result type | 结果类型
     * @return a new builder | 新的构建器
     * @throws OpenReflectException if sealedType is not sealed | 如果sealedType不是密封类
     */
    public static <T, R> Builder<T, R> builder(Class<T> sealedType, Class<R> resultType) {
        Objects.requireNonNull(sealedType, "sealedType must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
        if (!SealedUtil.isSealed(sealedType)) {
            throw new OpenReflectException(sealedType, null, "dispatch",
                    "Class is not sealed: " + sealedType.getName());
        }
        return new Builder<>(sealedType);
    }

    // ==================== Dispatch | 分发 ====================

    /**
     * Dispatches the value to the matching handler
     * 将值分发到匹配的处理器
     *
     * <p>Looks up the handler by the runtime class of the value.
     * For multi-level sealed hierarchies, walks up the class hierarchy
     * checking supertypes if no direct handler is found.</p>
     * <p>通过值的运行时类查找处理器。
     * 对于多层密封层次结构，如果没有直接匹配的处理器，会向上遍历类层次结构。</p>
     *
     * @param value the value to dispatch | 要分发的值
     * @return the result | 结果
     * @throws NullPointerException if value is null | 如果值为null
     * @throws OpenReflectException if no handler matches | 如果没有匹配的处理器
     */
    @SuppressWarnings("unchecked")
    public R dispatch(T value) {
        Objects.requireNonNull(value, "dispatch value must not be null");
        Function<T, R> handler = (Function<T, R>) findHandler(value.getClass());
        if (handler != null) {
            return handler.apply(value);
        }
        if (defaultHandler != null) {
            return defaultHandler.apply(value);
        }
        throw new OpenReflectException(sealedType, null, "dispatch",
                "No handler found for type: " + value.getClass().getName());
    }

    /**
     * Dispatches the value safely, returning an Optional
     * 安全分发值，返回Optional
     *
     * <p>Similar to {@link #dispatch(Object)} but returns {@link Optional#empty()}
     * instead of throwing when no handler matches.</p>
     * <p>与 {@link #dispatch(Object)} 类似，但在没有匹配处理器时返回 {@link Optional#empty()}
     * 而不是抛出异常。</p>
     *
     * @param value the value to dispatch | 要分发的值
     * @return Optional containing the result, or empty | 包含结果的Optional，或空
     * @throws NullPointerException if value is null | 如果值为null
     */
    @SuppressWarnings("unchecked")
    public Optional<R> dispatchSafe(T value) {
        Objects.requireNonNull(value, "dispatch value must not be null");
        Function<T, R> handler = (Function<T, R>) findHandler(value.getClass());
        if (handler != null) {
            return Optional.ofNullable(handler.apply(value));
        }
        if (defaultHandler != null) {
            return Optional.ofNullable(defaultHandler.apply(value));
        }
        return Optional.empty();
    }

    /**
     * Finds the handler for the given class, walking up the hierarchy if needed
     * 查找给定类的处理器，必要时向上遍历层次结构
     */
    private Function<?, R> findHandler(Class<?> clazz) {
        // Direct match
        Function<?, R> handler = handlers.get(clazz);
        if (handler != null) {
            return handler;
        }
        // BFS over supertypes bounded to the sealed hierarchy
        Deque<Class<?>> queue = new ArrayDeque<>();
        Set<Class<?>> visited = new HashSet<>();
        visited.add(clazz);
        addSupertypes(clazz, queue, visited);
        while (!queue.isEmpty()) {
            Class<?> candidate = queue.poll();
            handler = handlers.get(candidate);
            if (handler != null) {
                return handler;
            }
            addSupertypes(candidate, queue, visited);
        }
        return null;
    }

    private void addSupertypes(Class<?> clazz, Deque<Class<?>> queue, Set<Class<?>> visited) {
        // Add superclass (bounded to sealed hierarchy)
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class
                && sealedType.isAssignableFrom(superclass) && visited.add(superclass)) {
            queue.add(superclass);
        }
        // Add interfaces (bounded to sealed hierarchy)
        for (Class<?> iface : clazz.getInterfaces()) {
            if (sealedType.isAssignableFrom(iface) && visited.add(iface)) {
                queue.add(iface);
            }
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for SealedDispatcher
     * SealedDispatcher 构建器
     *
     * <p>Registers handlers for each permitted subclass and validates
     * exhaustiveness at build time.</p>
     * <p>为每个许可子类注册处理器，并在构建时验证穷举性。</p>
     *
     * @param <T> the sealed type | 密封类型
     * @param <R> the result type | 结果类型
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-reflect V1.0.3
     */
    public static final class Builder<T, R> {

        private final Class<T> sealedType;
        private final Map<Class<?>, Function<?, R>> handlers = new LinkedHashMap<>();
        private Function<? super T, ? extends R> defaultHandler;

        private Builder(Class<T> sealedType) {
            this.sealedType = sealedType;
        }

        /**
         * Registers a handler for a specific subtype
         * 为特定子类型注册处理器
         *
         * @param subtype the subtype class | 子类型类
         * @param handler the handler function | 处理函数
         * @param <S>     the subtype | 子类型
         * @return this builder | 此构建器
         * @throws OpenReflectException if subtype is not assignable from sealed type | 如果子类型不可从密封类型赋值
         */
        public <S extends T> Builder<T, R> on(Class<S> subtype, Function<? super S, ? extends R> handler) {
            Objects.requireNonNull(subtype, "subtype must not be null");
            Objects.requireNonNull(handler, "handler must not be null");
            if (!sealedType.isAssignableFrom(subtype)) {
                throw new OpenReflectException(sealedType, subtype.getName(), "dispatch",
                        subtype.getName() + " is not a subtype of " + sealedType.getName());
            }
            @SuppressWarnings("unchecked")
            Function<?, R> rawHandler = (Function<?, R>) handler;
            handlers.put(subtype, rawHandler);
            return this;
        }

        /**
         * Registers a default handler as fallback
         * 注册默认处理器作为回退
         *
         * <p>When a default handler is provided, exhaustiveness checking
         * is skipped at build time.</p>
         * <p>当提供默认处理器时，构建时跳过穷举性检查。</p>
         *
         * @param defaultHandler the default handler | 默认处理器
         * @return this builder | 此构建器
         */
        public Builder<T, R> orElse(Function<? super T, ? extends R> defaultHandler) {
            Objects.requireNonNull(defaultHandler, "defaultHandler must not be null");
            this.defaultHandler = defaultHandler;
            return this;
        }

        /**
         * Builds the dispatcher
         * 构建分发器
         *
         * <p>If no default handler is registered, validates that all concrete
         * leaf classes in the sealed hierarchy have handlers. Throws
         * {@link OpenReflectException} if any are missing.</p>
         * <p>如果未注册默认处理器，验证密封层次结构中的所有具体叶类都有处理器。
         * 如果有缺失则抛出 {@link OpenReflectException}。</p>
         *
         * @return the built dispatcher | 构建的分发器
         * @throws OpenReflectException if exhaustiveness check fails | 如果穷举性检查失败
         */
        public SealedDispatcher<T, R> build() {
            if (defaultHandler == null) {
                validateExhaustiveness();
            }
            return new SealedDispatcher<>(sealedType, handlers, defaultHandler);
        }

        private void validateExhaustiveness() {
            List<Class<?>> concreteTypes = SealedUtil.getLeafClasses(sealedType);
            List<String> missing = new ArrayList<>();
            for (Class<?> concrete : concreteTypes) {
                if (!hasHandler(concrete)) {
                    missing.add(concrete.getName());
                }
            }
            if (!missing.isEmpty()) {
                throw new OpenReflectException(sealedType, null, "dispatch",
                        "Non-exhaustive dispatch: missing handlers for " + missing);
            }
        }

        /**
         * Checks if a concrete type has a handler registered, either directly
         * or via a sealed intermediate type that covers it.
         */
        private boolean hasHandler(Class<?> concreteType) {
            if (handlers.containsKey(concreteType)) {
                return true;
            }
            // Check if any registered handler type is a supertype of this concrete type
            for (Class<?> handlerType : handlers.keySet()) {
                if (handlerType.isAssignableFrom(concreteType)) {
                    return true;
                }
            }
            return false;
        }
    }
}
