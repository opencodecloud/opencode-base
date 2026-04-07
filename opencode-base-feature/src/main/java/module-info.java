/**
 * OpenCode Base Feature Module
 * OpenCode 基础特性开关模块
 *
 * <p>Provides feature toggle/flag support based on JDK 25, enabling grayscale release,
 * A/B testing, dynamic configuration, and expression-based feature strategies.</p>
 * <p>提供基于 JDK 25 的特性开关支持，实现灰度发布、A/B 测试、动态配置和基于表达式的特性策略。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Feature Toggle / Feature Flag - 特性开关</li>
 *   <li>Grayscale Release - 灰度发布</li>
 *   <li>A/B Testing - A/B 测试</li>
 *   <li>Expression-Based Strategies - 基于表达式的策略</li>
 *   <li>Feature Audit Log - 特性审计日志</li>
 *   <li>AOP Proxy Integration - AOP 代理集成</li>
 *   <li>Cached Feature Store - 缓存特性存储</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
module cloud.opencode.base.feature {
    // Required modules
    requires transitive cloud.opencode.base.core;
    requires java.logging;

    // Optional: Cache support for feature store
    requires static cloud.opencode.base.cache;
    // Optional: Expression evaluation for feature strategies
    requires static cloud.opencode.base.expression;

    // Export public API packages
    exports cloud.opencode.base.feature;
    exports cloud.opencode.base.feature.annotation;
    exports cloud.opencode.base.feature.audit;
    exports cloud.opencode.base.feature.exception;
    exports cloud.opencode.base.feature.listener;
    exports cloud.opencode.base.feature.proxy;
    exports cloud.opencode.base.feature.security;
    exports cloud.opencode.base.feature.store;
    exports cloud.opencode.base.feature.strategy;
    exports cloud.opencode.base.feature.lifecycle;
    exports cloud.opencode.base.feature.testing;
}
