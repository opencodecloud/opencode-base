/**
 * OpenCode Base Event Module
 * OpenCode 基础事件模块
 *
 * <p>Provides event-driven architecture support based on JDK 25 virtual threads,
 * including publish/subscribe pattern, event dispatching, saga pattern, and event store.</p>
 * <p>提供基于 JDK 25 虚拟线程的事件驱动架构支持，包括发布/订阅模式、事件分发、
 * Saga 模式和事件存储。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Publish/Subscribe Pattern - 发布/订阅模式</li>
 *   <li>Async Event Dispatching (Virtual Threads) - 异步事件分发（虚拟线程）</li>
 *   <li>Event Handler Registration - 事件处理器注册</li>
 *   <li>Saga Pattern Support - Saga 模式支持</li>
 *   <li>Event Store - 事件存储</li>
 *   <li>Event Security - 事件安全</li>
 *   <li>Event Serialization - 事件序列化</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
module cloud.opencode.base.event {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Optional: Serialization support for event persistence
    requires static cloud.opencode.base.serialization;

    // Export public API packages
    exports cloud.opencode.base.event;
    exports cloud.opencode.base.event.annotation;
    exports cloud.opencode.base.event.dispatcher;
    exports cloud.opencode.base.event.exception;
    exports cloud.opencode.base.event.handler;
    exports cloud.opencode.base.event.saga;
    exports cloud.opencode.base.event.security;
    exports cloud.opencode.base.event.serialization;
    exports cloud.opencode.base.event.store;
    exports cloud.opencode.base.event.monitor;
}
