/**
 * OpenCode Base Rules Module
 * OpenCode 基础规则引擎模块
 *
 * <p>Provides lightweight business rule engine with DSL, decision tables and hot reload.</p>
 * <p>提供轻量级业务规则引擎，支持DSL、决策表和热更新。</p>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-rules V1.0.0
 */
module cloud.opencode.base.rules {
    // Required modules
    requires cloud.opencode.base.core;
    requires java.logging;

    // Optional modules
    requires static cloud.opencode.base.expression;

    // Export packages
    exports cloud.opencode.base.rules;
    exports cloud.opencode.base.rules.model;
    exports cloud.opencode.base.rules.dsl;
    exports cloud.opencode.base.rules.engine;
    exports cloud.opencode.base.rules.condition;
    exports cloud.opencode.base.rules.action;
    exports cloud.opencode.base.rules.conflict;
    exports cloud.opencode.base.rules.listener;
    exports cloud.opencode.base.rules.decision;
    exports cloud.opencode.base.rules.exception;
    exports cloud.opencode.base.rules.spi;
}
