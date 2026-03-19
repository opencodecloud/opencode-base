/**
 * OpenCode Base Config Module
 * OpenCode 基础配置模块
 *
 * <p>Provides flexible configuration management based on JDK 25, supporting multiple sources
 * (properties, YAML, environment, system), type conversion, placeholder resolution,
 * hot reload, and JDK 25 structured configuration binding.</p>
 * <p>提供基于 JDK 25 的灵活配置管理，支持多数据源（properties、YAML、环境变量、系统属性）、
 * 类型转换、占位符解析、热重载和 JDK 25 结构化配置绑定。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple Config Sources - 多配置数据源</li>
 *   <li>Type-Safe Binding - 类型安全绑定</li>
 *   <li>Placeholder Resolution - 占位符解析</li>
 *   <li>Hot Reload - 热重载</li>
 *   <li>Config Validation - 配置校验</li>
 *   <li>JDK 25 Record Binding - JDK 25 Record 绑定</li>
 *   <li>Advanced Config (Composite, Layered) - 高级配置（组合、分层）</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
module cloud.opencode.base.config {
    // Required modules
    requires transitive cloud.opencode.base.core;
    requires java.net.http;

    // Optional: String module for placeholder resolution
    requires static cloud.opencode.base.string;
    // Optional: YAML module for YAML config source
    requires static cloud.opencode.base.yml;

    // Export public API packages
    exports cloud.opencode.base.config;
    exports cloud.opencode.base.config.advanced;
    exports cloud.opencode.base.config.bind;
    exports cloud.opencode.base.config.converter;
    exports cloud.opencode.base.config.jdk25;
    exports cloud.opencode.base.config.placeholder;
    exports cloud.opencode.base.config.source;
    exports cloud.opencode.base.config.validation;

    // Internal packages - not exported
    // cloud.opencode.base.config.internal
    // cloud.opencode.base.config.converter.impl
}
