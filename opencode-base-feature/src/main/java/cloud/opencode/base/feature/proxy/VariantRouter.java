package cloud.opencode.base.feature.proxy;

import cloud.opencode.base.feature.FeatureContext;
import cloud.opencode.base.feature.annotation.FeatureVariant;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Variant Router
 * 变体路由器
 *
 * <p>Routes method calls to appropriate variants for A/B testing.</p>
 * <p>将方法调用路由到适当的A/B测试变体。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple variant registration - 多变体注册</li>
 *   <li>Consistent user routing - 一致性用户路由</li>
 *   <li>Percentage-based traffic split - 基于百分比的流量分配</li>
 *   <li>Context-aware selection - 上下文感知选择</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Define variant implementations
 * CheckoutService variantA = new CheckoutServiceV1();
 * CheckoutService variantB = new CheckoutServiceV2();
 *
 * // Create router with percentage-based traffic split
 * VariantRouter<CheckoutService> router = VariantRouter.<CheckoutService>builder("checkout-flow")
 *     .variant("A", variantA, 50)  // 50% traffic
 *     .variant("B", variantB, 50)  // 50% traffic
 *     .build();
 *
 * // Route based on user context
 * FeatureContext ctx = FeatureContext.ofUser("user123");
 * CheckoutService service = router.route(ctx);
 * service.checkout(order);
 *
 * // Or use direct invocation
 * router.execute(ctx, s -> s.checkout(order));
 * }</pre>
 *
 * @param <T> the variant type | 变体类型
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public final class VariantRouter<T> {

    private final String featureKey;
    private final Map<String, VariantEntry<T>> variants;
    private final String defaultVariant;
    private final String salt;

    private VariantRouter(Builder<T> builder) {
        this.featureKey = builder.featureKey;
        this.variants = new LinkedHashMap<>(builder.variants);
        this.defaultVariant = builder.defaultVariant;
        this.salt = builder.salt;
    }

    /**
     * Create a builder for the router
     * 创建路由器构建器
     *
     * @param featureKey the feature key for the A/B test | A/B测试的功能键
     * @param <T>        the variant type | 变体类型
     * @return new builder | 新的构建器
     */
    public static <T> Builder<T> builder(String featureKey) {
        return new Builder<>(featureKey);
    }

    /**
     * Create router from annotated methods
     * 从注解方法创建路由器
     *
     * @param featureKey the feature key | 功能键
     * @param target     the target object containing @FeatureVariant methods | 包含@FeatureVariant方法的目标对象
     * @param <T>        the target type | 目标类型
     * @return variant router | 变体路由器
     */
    public static <T> VariantRouter<MethodVariant<T>> fromAnnotations(String featureKey, T target) {
        Objects.requireNonNull(featureKey, "featureKey must not be null");
        Objects.requireNonNull(target, "target must not be null");

        Builder<MethodVariant<T>> builder = builder(featureKey);

        for (Method method : target.getClass().getMethods()) {
            FeatureVariant annotation = method.getAnnotation(FeatureVariant.class);
            if (annotation != null && annotation.feature().equals(featureKey)) {
                builder.variant(
                    annotation.variant(),
                    new MethodVariant<>(target, method),
                    annotation.percentage()
                );
            }
        }

        return builder.build();
    }

    /**
     * Route to a variant based on context
     * 根据上下文路由到变体
     *
     * @param context the feature context | 功能上下文
     * @return the selected variant | 选择的变体
     * @throws NoVariantException if no variants are registered | 如果没有注册变体
     */
    public T route(FeatureContext context) {
        if (variants.isEmpty()) {
            throw new NoVariantException(featureKey);
        }

        String selectedVariant = selectVariant(context);
        VariantEntry<T> entry = variants.get(selectedVariant);
        return entry != null ? entry.implementation : variants.values().iterator().next().implementation;
    }

    /**
     * Route with empty context
     * 使用空上下文路由
     *
     * @return the selected variant | 选择的变体
     */
    public T route() {
        return route(FeatureContext.empty());
    }

    /**
     * Route for a specific user
     * 为特定用户路由
     *
     * @param userId the user ID | 用户ID
     * @return the selected variant | 选择的变体
     */
    public T routeForUser(String userId) {
        return route(FeatureContext.ofUser(userId));
    }

    /**
     * Execute action on selected variant
     * 在选择的变体上执行操作
     *
     * @param context the feature context | 功能上下文
     * @param action  the action to execute | 要执行的操作
     * @param <R>     the return type | 返回类型
     * @return the result | 结果
     */
    public <R> R execute(FeatureContext context, java.util.function.Function<T, R> action) {
        return action.apply(route(context));
    }

    /**
     * Execute void action on selected variant
     * 在选择的变体上执行无返回值操作
     *
     * @param context the feature context | 功能上下文
     * @param action  the action to execute | 要执行的操作
     */
    public void executeVoid(FeatureContext context, java.util.function.Consumer<T> action) {
        action.accept(route(context));
    }

    /**
     * Get a specific variant by ID
     * 根据ID获取特定变体
     *
     * @param variantId the variant identifier | 变体标识符
     * @return optional containing the variant | 包含变体的Optional
     */
    public Optional<T> getVariant(String variantId) {
        VariantEntry<T> entry = variants.get(variantId);
        return entry != null ? Optional.of(entry.implementation) : Optional.empty();
    }

    /**
     * Get the variant that would be selected for a context
     * 获取将为上下文选择的变体ID
     *
     * @param context the feature context | 功能上下文
     * @return the variant identifier | 变体标识符
     */
    public String getSelectedVariantId(FeatureContext context) {
        return selectVariant(context);
    }

    /**
     * Get all registered variant IDs
     * 获取所有注册的变体ID
     *
     * @return set of variant IDs | 变体ID集合
     */
    public java.util.Set<String> getVariantIds() {
        return java.util.Collections.unmodifiableSet(variants.keySet());
    }

    /**
     * Get the feature key
     * 获取功能键
     *
     * @return the feature key | 功能键
     */
    public String getFeatureKey() {
        return featureKey;
    }

    /**
     * Select variant using consistent hashing
     * 使用一致性哈希选择变体
     */
    private String selectVariant(FeatureContext context) {
        if (variants.isEmpty()) {
            return defaultVariant;
        }

        // If only one variant, return it
        if (variants.size() == 1) {
            return variants.keySet().iterator().next();
        }

        // Check for explicit variant override in context
        Object overrideObj = context.attributes().get("variant");
        if (overrideObj instanceof String override && variants.containsKey(override)) {
            return override;
        }

        // Use consistent hashing for variant selection
        String userId = context.userId();
        if (userId == null || userId.isEmpty()) {
            // Random selection when no user ID
            return selectByPercentage(Math.abs((int) System.nanoTime()) % 100);
        }

        // Consistent hash based on user ID
        String hashInput = featureKey + ":" + userId + ":" + salt;
        int hash = computeHash(hashInput);
        return selectByPercentage(hash);
    }

    /**
     * Select variant based on percentage distribution
     * 根据百分比分布选择变体
     */
    private String selectByPercentage(int value) {
        int cumulative = 0;
        for (Map.Entry<String, VariantEntry<T>> entry : variants.entrySet()) {
            cumulative += entry.getValue().percentage;
            if (value < cumulative) {
                return entry.getKey();
            }
        }
        // Fallback to last variant (handles rounding)
        return variants.keySet().stream().reduce((first, second) -> second).orElse(defaultVariant);
    }

    /**
     * Compute consistent hash value (0-99)
     * 计算一致性哈希值 (0-99)
     */
    private int computeHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            int value = ((hash[0] & 0xFF) << 24) |
                        ((hash[1] & 0xFF) << 16) |
                        ((hash[2] & 0xFF) << 8) |
                        (hash[3] & 0xFF);
            return (value & Integer.MAX_VALUE) % 100;
        } catch (NoSuchAlgorithmException e) {
            return (input.hashCode() & Integer.MAX_VALUE) % 100;
        }
    }

    /**
     * Variant entry containing implementation and traffic percentage
     * 包含实现和流量百分比的变体条目
     */
    private record VariantEntry<T>(T implementation, int percentage) {}

    /**
     * Builder for VariantRouter
     * VariantRouter构建器
     *
     * @param <T> the variant type | 变体类型
     */
    public static class Builder<T> {
        private final String featureKey;
        private final Map<String, VariantEntry<T>> variants = new LinkedHashMap<>();
        private String defaultVariant;
        private String salt = "";

        Builder(String featureKey) {
            if (featureKey == null || featureKey.isBlank()) {
                throw new IllegalArgumentException("Feature key cannot be null or blank");
            }
            this.featureKey = featureKey;
        }

        /**
         * Add a variant with equal percentage distribution
         * 添加具有平均百分比分布的变体
         *
         * @param variantId      the variant identifier | 变体标识符
         * @param implementation the variant implementation | 变体实现
         * @return this builder | 此构建器
         */
        public Builder<T> variant(String variantId, T implementation) {
            return variant(variantId, implementation, 0); // Percentage normalized at build
        }

        /**
         * Add a variant with specified percentage
         * 添加具有指定百分比的变体
         *
         * @param variantId      the variant identifier | 变体标识符
         * @param implementation the variant implementation | 变体实现
         * @param percentage     the traffic percentage (0-100) | 流量百分比 (0-100)
         * @return this builder | 此构建器
         */
        public Builder<T> variant(String variantId, T implementation, int percentage) {
            if (variantId == null || variantId.isBlank()) {
                throw new IllegalArgumentException("Variant ID cannot be null or blank");
            }
            if (implementation == null) {
                throw new IllegalArgumentException("Variant implementation cannot be null");
            }
            variants.put(variantId, new VariantEntry<>(implementation, Math.max(0, Math.min(100, percentage))));
            if (defaultVariant == null) {
                defaultVariant = variantId;
            }
            return this;
        }

        /**
         * Add variant from supplier
         * 从供应器添加变体
         *
         * @param variantId  the variant identifier | 变体标识符
         * @param supplier   the variant supplier | 变体供应器
         * @param percentage the traffic percentage | 流量百分比
         * @return this builder | 此构建器
         */
        public Builder<T> variant(String variantId, Supplier<T> supplier, int percentage) {
            return variant(variantId, supplier.get(), percentage);
        }

        /**
         * Set the default variant
         * 设置默认变体
         *
         * @param variantId the default variant ID | 默认变体ID
         * @return this builder | 此构建器
         */
        public Builder<T> defaultVariant(String variantId) {
            this.defaultVariant = variantId;
            return this;
        }

        /**
         * Set salt for consistent hashing
         * 设置一致性哈希的盐
         *
         * @param salt the salt value | 盐值
         * @return this builder | 此构建器
         */
        public Builder<T> salt(String salt) {
            this.salt = salt != null ? salt : "";
            return this;
        }

        /**
         * Build the router
         * 构建路由器
         *
         * @return the variant router | 变体路由器
         */
        public VariantRouter<T> build() {
            if (variants.isEmpty()) {
                throw new IllegalStateException("At least one variant must be registered");
            }

            // Normalize percentages if not all specified
            normalizePercentages();

            return new VariantRouter<>(this);
        }

        /**
         * Normalize percentages to sum to 100
         * 规范化百分比使其总和为100
         */
        private void normalizePercentages() {
            int totalSpecified = 0;
            int unspecifiedCount = 0;

            for (VariantEntry<T> entry : variants.values()) {
                if (entry.percentage > 0) {
                    totalSpecified += entry.percentage;
                } else {
                    unspecifiedCount++;
                }
            }

            if (unspecifiedCount > 0) {
                int remaining = Math.max(0, 100 - totalSpecified);
                int perVariant = remaining / unspecifiedCount;
                int extra = remaining % unspecifiedCount;

                Map<String, VariantEntry<T>> normalized = new LinkedHashMap<>();
                int index = 0;
                for (Map.Entry<String, VariantEntry<T>> entry : variants.entrySet()) {
                    VariantEntry<T> ve = entry.getValue();
                    if (ve.percentage == 0) {
                        int pct = perVariant + (index < extra ? 1 : 0);
                        normalized.put(entry.getKey(), new VariantEntry<>(ve.implementation, pct));
                        index++;
                    } else {
                        normalized.put(entry.getKey(), ve);
                    }
                }
                variants.clear();
                variants.putAll(normalized);
            }
        }
    }

    /**
     * Wrapper for method-based variants
     * 方法变体的包装器
     *
     * @param <T> the target type | 目标类型
     */
    public static class MethodVariant<T> {
        private final T target;
        private final Method method;

        MethodVariant(T target, Method method) {
            this.target = target;
            this.method = method;
        }

        /**
         * Invoke the variant method
         * 调用变体方法
         *
         * @param args the method arguments | 方法参数
         * @return the result | 结果
         * @throws Exception if invocation fails | 如果调用失败
         */
        public Object invoke(Object... args) throws Exception {
            return method.invoke(target, args);
        }

        /**
         * Get the method name
         * 获取方法名
         *
         * @return method name | 方法名
         */
        public String getMethodName() {
            return method.getName();
        }
    }

    /**
     * Exception thrown when no variant is available
     * 当没有可用变体时抛出的异常
     */
    public static class NoVariantException extends RuntimeException {
        private final String featureKey;

        public NoVariantException(String featureKey) {
            super("No variant registered for feature: " + featureKey);
            this.featureKey = featureKey;
        }

        public String getFeatureKey() {
            return featureKey;
        }
    }
}
