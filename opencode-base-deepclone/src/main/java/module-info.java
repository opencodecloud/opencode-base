/**
 * OpenCode Base DeepClone Module
 * OpenCode 基础深拷贝模块
 *
 * <p>Provides deep cloning utilities based on JDK 25, supporting multiple clone strategies
 * including serialization, reflection, and custom SPI-based strategies.</p>
 * <p>提供基于 JDK 25 的深拷贝工具，支持多种克隆策略，包括序列化、反射和自定义 SPI 策略。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple Clone Strategies - 多种克隆策略</li>
 *   <li>Annotation-Driven Configuration - 注解驱动配置</li>
 *   <li>Circular Reference Handling - 循环引用处理</li>
 *   <li>SPI Extension Point - SPI 扩展点</li>
 *   <li>Type Handler Registry - 类型处理器注册</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
module cloud.opencode.base.deepclone {
    // Required modules
    requires transitive cloud.opencode.base.core;
    requires jdk.unsupported;

    // Export public API packages
    exports cloud.opencode.base.deepclone;
    exports cloud.opencode.base.deepclone.annotation;
    exports cloud.opencode.base.deepclone.cloner;
    exports cloud.opencode.base.deepclone.contract;
    exports cloud.opencode.base.deepclone.exception;
    exports cloud.opencode.base.deepclone.handler;
    exports cloud.opencode.base.deepclone.internal;
    exports cloud.opencode.base.deepclone.spi;
    exports cloud.opencode.base.deepclone.strategy;

    // SPI: Clone strategy providers
    uses cloud.opencode.base.deepclone.spi.CloneStrategyProvider;
}
