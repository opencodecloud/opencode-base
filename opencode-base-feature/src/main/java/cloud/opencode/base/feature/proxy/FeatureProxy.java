package cloud.opencode.base.feature.proxy;

import cloud.opencode.base.feature.FeatureContext;
import cloud.opencode.base.feature.OpenFeature;
import cloud.opencode.base.feature.annotation.FeatureToggle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Feature Proxy
 * 功能代理
 *
 * <p>Creates dynamic proxies that intercept methods annotated with @FeatureToggle.</p>
 * <p>创建动态代理，拦截带有@FeatureToggle注解的方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dynamic proxy creation - 动态代理创建</li>
 *   <li>Method-level feature gating - 方法级功能门控</li>
 *   <li>Context-aware evaluation - 上下文感知评估</li>
 *   <li>Default value support - 默认值支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Define interface with @FeatureToggle
 * public interface PaymentService {
 *     @FeatureToggle("new-payment")
 *     void processPayment(Order order);
 *
 *     @FeatureToggle(value = "refund-v2", defaultEnabled = false)
 *     void processRefund(Order order);
 * }
 *
 * // Create implementation
 * PaymentService impl = new PaymentServiceImpl();
 *
 * // Create feature-gated proxy
 * PaymentService proxy = FeatureProxy.create(PaymentService.class, impl);
 *
 * // Methods are only executed if feature is enabled
 * proxy.processPayment(order); // Only runs if "new-payment" is enabled
 *
 * // With context supplier
 * PaymentService proxy = FeatureProxy.builder(PaymentService.class, impl)
 *     .contextSupplier(() -> FeatureContext.ofUser(getCurrentUserId()))
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public final class FeatureProxy {

    private FeatureProxy() {
        // Utility class
    }

    /**
     * Create a feature-gated proxy for an interface
     * 为接口创建功能门控代理
     *
     * @param interfaceType the interface class | 接口类
     * @param target        the target implementation | 目标实现
     * @param <T>           the interface type | 接口类型
     * @return proxied instance | 代理实例
     */
    public static <T> T create(Class<T> interfaceType, T target) {
        return create(interfaceType, target, OpenFeature.getInstance());
    }

    /**
     * Create a feature-gated proxy with custom OpenFeature instance
     * 使用自定义OpenFeature实例创建功能门控代理
     *
     * @param interfaceType the interface class | 接口类
     * @param target        the target implementation | 目标实现
     * @param features      the OpenFeature instance | OpenFeature实例
     * @param <T>           the interface type | 接口类型
     * @return proxied instance | 代理实例
     */
    public static <T> T create(Class<T> interfaceType, T target, OpenFeature features) {
        return builder(interfaceType, target)
                .features(features)
                .build();
    }

    /**
     * Create a builder for more configuration options
     * 创建构建器以获得更多配置选项
     *
     * @param interfaceType the interface class | 接口类
     * @param target        the target implementation | 目标实现
     * @param <T>           the interface type | 接口类型
     * @return new builder | 新的构建器
     */
    public static <T> Builder<T> builder(Class<T> interfaceType, T target) {
        return new Builder<>(interfaceType, target);
    }

    /**
     * Builder for FeatureProxy
     * FeatureProxy构建器
     *
     * @param <T> the interface type | 接口类型
     */
    public static class Builder<T> {
        private final Class<T> interfaceType;
        private final T target;
        private OpenFeature features = OpenFeature.getInstance();
        private Supplier<FeatureContext> contextSupplier = FeatureContext::empty;
        private DisabledBehavior disabledBehavior = DisabledBehavior.RETURN_DEFAULT;

        Builder(Class<T> interfaceType, T target) {
            if (interfaceType == null) {
                throw new IllegalArgumentException("Interface type cannot be null");
            }
            if (!interfaceType.isInterface()) {
                throw new IllegalArgumentException("Type must be an interface: " + interfaceType.getName());
            }
            if (target == null) {
                throw new IllegalArgumentException("Target cannot be null");
            }
            this.interfaceType = interfaceType;
            this.target = target;
        }

        /**
         * Set the OpenFeature instance
         * 设置OpenFeature实例
         *
         * @param features the OpenFeature instance | OpenFeature实例
         * @return this builder | 此构建器
         */
        public Builder<T> features(OpenFeature features) {
            this.features = features != null ? features : OpenFeature.getInstance();
            return this;
        }

        /**
         * Set the context supplier for dynamic context
         * 设置动态上下文的上下文供应器
         *
         * @param contextSupplier supplier that provides context for each method call | 为每次方法调用提供上下文的供应器
         * @return this builder | 此构建器
         */
        public Builder<T> contextSupplier(Supplier<FeatureContext> contextSupplier) {
            this.contextSupplier = contextSupplier != null ? contextSupplier : FeatureContext::empty;
            return this;
        }

        /**
         * Set behavior when feature is disabled
         * 设置功能禁用时的行为
         *
         * @param behavior the behavior | 行为
         * @return this builder | 此构建器
         */
        public Builder<T> whenDisabled(DisabledBehavior behavior) {
            this.disabledBehavior = behavior != null ? behavior : DisabledBehavior.RETURN_DEFAULT;
            return this;
        }

        /**
         * Build the proxy
         * 构建代理
         *
         * @return proxied instance | 代理实例
         */
        @SuppressWarnings("unchecked")
        public T build() {
            return (T) Proxy.newProxyInstance(
                    interfaceType.getClassLoader(),
                    new Class<?>[]{interfaceType},
                    new FeatureInvocationHandler<>(target, features, contextSupplier, disabledBehavior)
            );
        }
    }

    /**
     * Behavior when feature is disabled
     * 功能禁用时的行为
     */
    public enum DisabledBehavior {
        /**
         * Return default value (null for objects, 0 for primitives, false for boolean)
         * 返回默认值（对象为null，基本类型为0，布尔为false）
         */
        RETURN_DEFAULT,

        /**
         * Throw FeatureDisabledException
         * 抛出FeatureDisabledException
         */
        THROW_EXCEPTION,

        /**
         * Skip method silently (same as RETURN_DEFAULT but clearer intent)
         * 静默跳过方法（与RETURN_DEFAULT相同，但意图更清晰）
         */
        SKIP
    }

    /**
     * Invocation handler for feature-gated proxies
     * 功能门控代理的调用处理器
     */
    private static class FeatureInvocationHandler<T> implements InvocationHandler {
        private final T target;
        private final OpenFeature features;
        private final Supplier<FeatureContext> contextSupplier;
        private final DisabledBehavior disabledBehavior;
        private final Map<Method, FeatureToggle> annotationCache = new ConcurrentHashMap<>();

        FeatureInvocationHandler(T target, OpenFeature features,
                                  Supplier<FeatureContext> contextSupplier,
                                  DisabledBehavior disabledBehavior) {
            this.target = target;
            this.features = features;
            this.contextSupplier = contextSupplier;
            this.disabledBehavior = disabledBehavior;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Handle Object methods
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(target, args);
            }

            // Check for @FeatureToggle annotation
            FeatureToggle toggle = getFeatureToggle(method);
            if (toggle == null) {
                // No annotation, execute normally
                return method.invoke(target, args);
            }

            // Evaluate feature
            String featureKey = toggle.value();
            FeatureContext context = contextSupplier.get();
            boolean enabled;

            if (features.exists(featureKey)) {
                enabled = features.isEnabled(featureKey, context);
            } else {
                enabled = toggle.defaultEnabled();
            }

            if (enabled) {
                return method.invoke(target, args);
            }

            // Feature is disabled
            return handleDisabled(method, featureKey);
        }

        private FeatureToggle getFeatureToggle(Method method) {
            return annotationCache.computeIfAbsent(method, m -> {
                // Check method first
                FeatureToggle toggle = m.getAnnotation(FeatureToggle.class);
                if (toggle != null) {
                    return toggle;
                }
                // Check declaring class
                return m.getDeclaringClass().getAnnotation(FeatureToggle.class);
            });
        }

        private Object handleDisabled(Method method, String featureKey) {
            return switch (disabledBehavior) {
                case THROW_EXCEPTION -> throw new FeatureDisabledException(featureKey, method.getName());
                case RETURN_DEFAULT, SKIP -> getDefaultValue(method.getReturnType());
            };
        }

        private Object getDefaultValue(Class<?> type) {
            if (type == void.class || type == Void.class) {
                return null;
            }
            if (type.isPrimitive()) {
                if (type == boolean.class) return false;
                if (type == byte.class) return (byte) 0;
                if (type == short.class) return (short) 0;
                if (type == int.class) return 0;
                if (type == long.class) return 0L;
                if (type == float.class) return 0.0f;
                if (type == double.class) return 0.0d;
                if (type == char.class) return '\0';
            }
            return null;
        }
    }

    /**
     * Exception thrown when feature is disabled and THROW_EXCEPTION behavior is set
     * 当功能禁用且设置了THROW_EXCEPTION行为时抛出的异常
     */
    public static class FeatureDisabledException extends RuntimeException {
        private final String featureKey;
        private final String methodName;

        public FeatureDisabledException(String featureKey, String methodName) {
            super("Feature '" + featureKey + "' is disabled, cannot execute method: " + methodName);
            this.featureKey = featureKey;
            this.methodName = methodName;
        }

        public String getFeatureKey() {
            return featureKey;
        }

        public String getMethodName() {
            return methodName;
        }
    }
}
