/**
 * OpenCode Base Core Module
 * OpenCode 核心模块
 *
 * <p>Provides core utilities and common functionality for the OpenCode Base library.</p>
 * <p>为 OpenCode Base 库提供核心工具和通用功能。</p>
 *
 * @since JDK 25, opencode-base-core V1.0.0
 */
module cloud.opencode.base.core {

    // Required modules
    requires static java.sql;

    // Export all public packages
    exports cloud.opencode.base.core;
    exports cloud.opencode.base.core.assertion;
    exports cloud.opencode.base.core.bean;
    exports cloud.opencode.base.core.builder;
    exports cloud.opencode.base.core.convert;
    exports cloud.opencode.base.core.exception;
    exports cloud.opencode.base.core.func;
    exports cloud.opencode.base.core.primitives;
    exports cloud.opencode.base.core.random;
    exports cloud.opencode.base.core.reflect;
    exports cloud.opencode.base.core.singleton;
    exports cloud.opencode.base.core.spi;
    exports cloud.opencode.base.core.stream;
    exports cloud.opencode.base.core.thread;
    exports cloud.opencode.base.core.compare;
    exports cloud.opencode.base.core.container;
    exports cloud.opencode.base.core.tuple;
    exports cloud.opencode.base.core.annotation;
    exports cloud.opencode.base.core.page;

    // Internal packages - not exported
    // cloud.opencode.base.core.internal
    // cloud.opencode.base.core.convert.impl
}
