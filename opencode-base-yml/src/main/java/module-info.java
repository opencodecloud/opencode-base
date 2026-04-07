/**
 * OpenCode Base YML Module
 * OpenCode 基础 YAML 模块
 *
 * <p>Provides lightweight YAML processing capabilities via SPI mechanism,
 * supporting SnakeYAML and other implementations.</p>
 * <p>通过 SPI 机制提供轻量级 YAML 处理能力，支持 SnakeYAML 等实现。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>OpenYml - Unified facade (load/dump/merge/bind) - 统一门面</li>
 *   <li>Path access - a.b.c nested property access - 路径访问</li>
 *   <li>Placeholder - ${key:default} syntax - 占位符解析</li>
 *   <li>Profile merge - Multi-environment config merge - Profile 合并</li>
 *   <li>YmlBinder - Bean binding with annotations - Bean 绑定</li>
 *   <li>Multi-document - --- separated documents - 多文档支持</li>
 *   <li>Security - YAML bomb protection - 安全防护</li>
 *   <li>SPI - Pluggable engine (SnakeYAML, etc.) - SPI 可插拔</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-yml V1.0.0
 */
module cloud.opencode.base.yml {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export all public packages
    exports cloud.opencode.base.yml;
    exports cloud.opencode.base.yml.bind;
    exports cloud.opencode.base.yml.exception;
    exports cloud.opencode.base.yml.merge;
    exports cloud.opencode.base.yml.path;
    exports cloud.opencode.base.yml.placeholder;
    exports cloud.opencode.base.yml.security;
    exports cloud.opencode.base.yml.schema;
    exports cloud.opencode.base.yml.spi;
    exports cloud.opencode.base.yml.diff;
    exports cloud.opencode.base.yml.transform;
    exports cloud.opencode.base.yml.include;
    exports cloud.opencode.base.yml.profile;

    // SPI uses
    uses cloud.opencode.base.yml.spi.YmlProvider;

    // SPI provides
    provides cloud.opencode.base.yml.spi.YmlProvider
        with cloud.opencode.base.yml.internal.BuiltinYmlProvider;

}
