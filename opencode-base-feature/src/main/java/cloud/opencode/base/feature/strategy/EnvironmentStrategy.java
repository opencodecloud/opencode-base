package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment-Based Enable Strategy
 * 基于环境的启用策略
 *
 * <p>Strategy that enables/disables features based on the deployment environment.</p>
 * <p>根据部署环境启用/禁用功能的策略。</p>
 *
 * <p><strong>Environment Resolution Order | 环境解析顺序:</strong></p>
 * <ol>
 *   <li>Context attribute "environment" - 上下文属性 "environment"</li>
 *   <li>System property "app.environment" - 系统属性 "app.environment"</li>
 *   <li>Environment variable "APP_ENVIRONMENT" - 环境变量 "APP_ENVIRONMENT"</li>
 *   <li>Default: "default" - 默认: "default"</li>
 * </ol>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EnvironmentStrategy strategy = EnvironmentStrategy.builder()
 *     .dev(true)
 *     .staging(true)
 *     .prod(false)
 *     .defaultState(false)
 *     .build();
 *
 * Feature feature = Feature.builder("debug-mode")
 *     .strategy(strategy)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
public class EnvironmentStrategy implements EnableStrategy {

    private static final String ENV_ATTRIBUTE = "environment";
    private static final String ENV_PROPERTY = "app.environment";

    private final Map<String, Boolean> environmentStates;
    private final boolean defaultState;

    private EnvironmentStrategy(Map<String, Boolean> environmentStates, boolean defaultState) {
        this.environmentStates = Map.copyOf(environmentStates);
        this.defaultState = defaultState;
    }

    /**
     * Evaluate if the feature is enabled for the current environment
     * 评估功能是否在当前环境中启用
     *
     * @param feature the feature being evaluated | 正在评估的功能
     * @param context the evaluation context | 评估上下文
     * @return true if enabled for the current environment | 如果在当前环境中启用返回true
     */
    @Override
    public boolean isEnabled(Feature feature, FeatureContext context) {
        String env = resolveEnvironment(context);
        return environmentStates.getOrDefault(env, defaultState);
    }

    private String resolveEnvironment(FeatureContext context) {
        if (context == null) {
            return resolveFromSystemOrDefault();
        }
        // 1. Check context attribute
        Object raw = context.getAttribute(ENV_ATTRIBUTE);
        if (raw instanceof String env && !env.isBlank()) {
            return env;
        } else if (raw != null) {
            String env = raw.toString();
            if (!env.isBlank()) {
                return env;
            }
        }
        // 2. Fall through to system/env
        return resolveFromSystemOrDefault();
    }

    private String resolveFromSystemOrDefault() {
        // Check system property
        String env = System.getProperty(ENV_PROPERTY);
        if (env != null && !env.isBlank()) {
            return env;
        }
        // Check environment variable
        env = System.getenv("APP_ENVIRONMENT");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return "default";
    }

    /**
     * Get the environment states map
     * 获取环境状态映射
     *
     * @return immutable map of environment to enabled state | 环境到启用状态的不可变映射
     */
    public Map<String, Boolean> getEnvironmentStates() {
        return environmentStates;
    }

    /**
     * Get the default state
     * 获取默认状态
     *
     * @return default state | 默认状态
     */
    public boolean getDefaultState() {
        return defaultState;
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return new builder | 新的构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for EnvironmentStrategy
     * EnvironmentStrategy构建器
     */
    public static class Builder {
        private final Map<String, Boolean> states = new HashMap<>();
        private boolean defaultState = false;

        /**
         * Set state for a specific environment
         * 设置特定环境的状态
         *
         * @param env     the environment name | 环境名称
         * @param enabled whether enabled | 是否启用
         * @return this builder | 此构建器
         */
        public Builder environment(String env, boolean enabled) {
            if (env == null || env.isBlank()) {
                throw new IllegalArgumentException("Environment name cannot be null or blank");
            }
            states.put(env, enabled);
            return this;
        }

        /**
         * Set state for dev environment
         * 设置开发环境的状态
         *
         * @param enabled whether enabled | 是否启用
         * @return this builder | 此构建器
         */
        public Builder dev(boolean enabled) {
            return environment("dev", enabled);
        }

        /**
         * Set state for staging environment
         * 设置预发布环境的状态
         *
         * @param enabled whether enabled | 是否启用
         * @return this builder | 此构建器
         */
        public Builder staging(boolean enabled) {
            return environment("staging", enabled);
        }

        /**
         * Set state for prod environment
         * 设置生产环境的状态
         *
         * @param enabled whether enabled | 是否启用
         * @return this builder | 此构建器
         */
        public Builder prod(boolean enabled) {
            return environment("prod", enabled);
        }

        /**
         * Set state for test environment
         * 设置测试环境的状态
         *
         * @param enabled whether enabled | 是否启用
         * @return this builder | 此构建器
         */
        public Builder test(boolean enabled) {
            return environment("test", enabled);
        }

        /**
         * Set default state for unrecognized environments
         * 设置未识别环境的默认状态
         *
         * @param enabled whether enabled by default | 默认是否启用
         * @return this builder | 此构建器
         */
        public Builder defaultState(boolean enabled) {
            this.defaultState = enabled;
            return this;
        }

        /**
         * Build the strategy
         * 构建策略
         *
         * @return new EnvironmentStrategy | 新的EnvironmentStrategy
         */
        public EnvironmentStrategy build() {
            return new EnvironmentStrategy(states, defaultState);
        }
    }
}
