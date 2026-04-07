/**
 * OpenCode Base Web Module
 * OpenCode 基础 Web 模块
 *
 * <p>Provides lightweight, framework-agnostic Web utilities including
 * unified response format, pagination, request context, and result encryption.</p>
 * <p>提供轻量级、框架无关的 Web 工具，包括统一响应格式、分页、请求上下文和响应加密。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Result - Unified response (immutable Record) - 统一响应（不可变Record）</li>
 *   <li>Results - Response builder factory - 响应构建工厂</li>
 *   <li>ResultCode - Response code interface with SPI - 响应码接口（SPI扩展）</li>
 *   <li>PageRequest/PageResult - Pagination support - 分页支持</li>
 *   <li>RequestContextHolder - Thread-safe context with async support - 线程安全上下文</li>
 *   <li>ResultEncryptor - Response encryption SPI - 响应加密SPI</li>
 *   <li>ExceptionConverter - Exception to Result conversion - 异常转换</li>
 *   <li>TraceId integration - 链路追踪集成</li>
 * </ul>
 *
 * <p><strong>Design Principles | 设计原则:</strong></p>
 * <ul>
 *   <li>Framework agnostic - No Spring/Jakarta EE dependency - 框架无关</li>
 *   <li>Immutable Records - Thread-safe by design - 不可变Record</li>
 *   <li>SPI extensible - Result codes and encryption - SPI可扩展</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-web V1.0.0
 */
module cloud.opencode.base.web {
    // Required modules
    requires transitive cloud.opencode.base.core;
    requires transitive cloud.opencode.base.json;
    requires transitive cloud.opencode.base.crypto;
    requires java.net.http;

    // Note: TraceId integration with opencode-base-log is handled dynamically
    // via reflection in TraceIdResolver (graceful fallback when log module unavailable)

    // Export public API packages
    exports cloud.opencode.base.web;
    exports cloud.opencode.base.web.context;
    exports cloud.opencode.base.web.crypto;
    exports cloud.opencode.base.web.exception;
    exports cloud.opencode.base.web.page;
    exports cloud.opencode.base.web.spi;
    exports cloud.opencode.base.web.util;
    exports cloud.opencode.base.web.url;
    exports cloud.opencode.base.web.http;
    exports cloud.opencode.base.web.cookie;
    exports cloud.opencode.base.web.sse;
    exports cloud.opencode.base.web.body;
    exports cloud.opencode.base.web.problem;
    exports cloud.opencode.base.web.cache;
    exports cloud.opencode.base.web.cors;
    exports cloud.opencode.base.web.security;
    exports cloud.opencode.base.web.ratelimit;

    // SPI: Allow ServiceLoader to find provider implementations
    uses cloud.opencode.base.web.spi.ResultCodeProvider;
    uses cloud.opencode.base.web.spi.ResultCustomizer;
    uses cloud.opencode.base.web.crypto.ResultEncryptor;
    uses cloud.opencode.base.web.crypto.EncryptionKeyResolver;
}
