/**
 * OpenCode Base JSON Module
 * OpenCode 基础 JSON 模块
 *
 * <p>Provides unified JSON processing API with pluggable provider support.</p>
 * <p>提供统一的 JSON 处理 API，支持可插拔的提供者。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Serialization/Deserialization - 序列化/反序列化</li>
 *   <li>Tree Model (JsonNode) - 树模型</li>
 *   <li>Streaming API - 流式 API</li>
 *   <li>JSONPath &amp; JSON Pointer - JSONPath 和 JSON Pointer</li>
 *   <li>JSON Patch (RFC 6902) &amp; Merge Patch (RFC 7396) - JSON Patch 和 Merge Patch</li>
 *   <li>JSON Schema Validation - JSON Schema 验证</li>
 *   <li>JSON Diff - JSON 差异比较</li>
 *   <li>Custom Type Adapters - 自定义类型适配器</li>
 *   <li>Security Features - 安全特性</li>
 *   <li>Reactive Streams Support - 响应式流支持</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-json V1.0.0
 */
module cloud.opencode.base.json {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.json;
    exports cloud.opencode.base.json.adapter;
    exports cloud.opencode.base.json.annotation;
    exports cloud.opencode.base.json.diff;
    exports cloud.opencode.base.json.exception;
    exports cloud.opencode.base.json.patch;
    exports cloud.opencode.base.json.path;
    exports cloud.opencode.base.json.reactive;
    exports cloud.opencode.base.json.schema;
    exports cloud.opencode.base.json.security;
    exports cloud.opencode.base.json.spi;
    exports cloud.opencode.base.json.stream;

    // SPI: Allow ServiceLoader to find JsonProvider implementations
    uses cloud.opencode.base.json.spi.JsonProvider;

    // Built-in provider registration
    provides cloud.opencode.base.json.spi.JsonProvider
            with cloud.opencode.base.json.internal.BuiltinJsonProvider;
}
