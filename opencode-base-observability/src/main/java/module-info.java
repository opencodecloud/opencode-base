/**
 * OpenCode Base Observability Module
 * OpenCode 基础可观测性模块
 *
 * <p>Provides lightweight, framework-agnostic observability primitives for JDK 25+ applications.
 * Includes tracing (Tracer/Span), slow-operation logging, and optional OpenTelemetry integration
 * via reflection — no hard OTel dependency required.</p>
 * <p>为 JDK 25+ 应用提供轻量级、框架无关的可观测性原语。
 * 包括追踪（Tracer/Span）、慢操作日志，以及通过反射的可选 OpenTelemetry 集成
 * — 无需硬依赖 OTel。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Framework-agnostic Tracer/Span API - 框架无关的 Tracer/Span API</li>
 *   <li>OpenTelemetry integration via reflection (optional) - 通过反射的 OpenTelemetry 集成（可选）</li>
 *   <li>Bounded slow-log collector with statistics - 有界慢日志收集器与统计</li>
 *   <li>Zero-overhead no-op implementations - 零开销空操作实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
module cloud.opencode.base.observability {
    requires transitive cloud.opencode.base.core;

    exports cloud.opencode.base.observability;
    exports cloud.opencode.base.observability.context;
    exports cloud.opencode.base.observability.exception;
    exports cloud.opencode.base.observability.health;
    exports cloud.opencode.base.observability.metric;
}
