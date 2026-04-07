package cloud.opencode.base.feature.testing;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import cloud.opencode.base.feature.OpenFeature;
import cloud.opencode.base.feature.store.InMemoryFeatureStore;
import cloud.opencode.base.feature.strategy.AlwaysOffStrategy;
import cloud.opencode.base.feature.strategy.AlwaysOnStrategy;

/**
 * Test Feature Manager
 * 测试功能管理器
 *
 * <p>Convenience class for managing features in tests.</p>
 * <p>用于在测试中管理功能的便捷类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Simple feature registration - 简单功能注册</li>
 *   <li>Enable/disable all features - 启用/禁用所有功能</li>
 *   <li>AutoCloseable for try-with-resources - 支持AutoCloseable以使用try-with-resources</li>
 *   <li>Delegates to OpenFeature - 委托给OpenFeature</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (TestFeatureManager manager = new TestFeatureManager()) {
 *     manager.withFeature("dark-mode", true)
 *            .withFeature("beta-feature", false);
 *
 *     assertTrue(manager.isEnabled("dark-mode"));
 *     assertFalse(manager.isEnabled("beta-feature"));
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe OpenFeature) - 线程安全: 是（委托给线程安全的OpenFeature）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
public class TestFeatureManager implements AutoCloseable {

    private final OpenFeature delegate;

    /**
     * Create a new test feature manager
     * 创建新的测试功能管理器
     */
    public TestFeatureManager() {
        this.delegate = OpenFeature.create(new InMemoryFeatureStore());
    }

    /**
     * Register a feature as enabled or disabled
     * 将功能注册为启用或禁用
     *
     * @param key     the feature key | 功能键
     * @param enabled whether the feature is enabled | 功能是否启用
     * @return this manager for fluent API | 此管理器以支持流式API
     */
    public TestFeatureManager withFeature(String key, boolean enabled) {
        delegate.register(Feature.builder(key).defaultEnabled(enabled)
            .strategy(enabled ? AlwaysOnStrategy.INSTANCE : AlwaysOffStrategy.INSTANCE).build());
        return this;
    }

    /**
     * Register a feature with full definition
     * 使用完整定义注册功能
     *
     * @param feature the feature | 功能
     * @return this manager for fluent API | 此管理器以支持流式API
     */
    public TestFeatureManager withFeature(Feature feature) {
        delegate.register(feature);
        return this;
    }

    /**
     * Enable a feature, registering it if it does not exist
     * 启用功能，如果不存在则注册
     *
     * @param key the feature key | 功能键
     */
    public void enable(String key) {
        delegate.register(Feature.builder(key).defaultEnabled(true)
            .strategy(AlwaysOnStrategy.INSTANCE).build());
    }

    /**
     * Disable a feature, registering it if it does not exist
     * 禁用功能，如果不存在则注册
     *
     * @param key the feature key | 功能键
     */
    public void disable(String key) {
        delegate.register(Feature.builder(key).defaultEnabled(false)
            .strategy(AlwaysOffStrategy.INSTANCE).build());
    }

    /**
     * Enable all registered features
     * 启用所有已注册的功能
     */
    public void enableAll() {
        delegate.getAllKeys().forEach(this::enable);
    }

    /**
     * Disable all registered features
     * 禁用所有已注册的功能
     */
    public void disableAll() {
        delegate.getAllKeys().forEach(this::disable);
    }

    /**
     * Check if a feature is enabled
     * 检查功能是否启用
     *
     * @param key the feature key | 功能键
     * @return true if enabled | 如果启用返回true
     */
    public boolean isEnabled(String key) {
        return delegate.isEnabled(key);
    }

    /**
     * Check if a feature is enabled with context
     * 使用上下文检查功能是否启用
     *
     * @param key     the feature key | 功能键
     * @param context the evaluation context | 评估上下文
     * @return true if enabled | 如果启用返回true
     */
    public boolean isEnabled(String key, FeatureContext context) {
        return delegate.isEnabled(key, context);
    }

    /**
     * Get the underlying OpenFeature delegate
     * 获取底层OpenFeature委托
     *
     * @return the delegate | 委托
     */
    public OpenFeature getDelegate() {
        return delegate;
    }

    /**
     * Close and clear all features
     * 关闭并清除所有功能
     */
    @Override
    public void close() {
        delegate.clear();
    }
}
