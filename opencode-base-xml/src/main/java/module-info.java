/**
 * OpenCode Base XML Module
 * OpenCode 基础 XML 模块
 *
 * <p>Provides lightweight XML processing capabilities based on JDK built-in APIs,
 * including DOM/SAX/StAX parsing, XPath queries, Bean binding, and XSLT transformation.</p>
 * <p>提供基于 JDK 内置 API 的轻量级 XML 处理能力，包括 DOM/SAX/StAX 解析、XPath 查询、Bean 绑定和 XSLT 转换。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>OpenXml - Unified facade for all XML operations - 统一门面</li>
 *   <li>DOM/SAX/StAX - Multiple parsing modes - 多解析模式</li>
 *   <li>XPath - Powerful query support - XPath 查询</li>
 *   <li>XmlBinder - Bean binding with annotations (JAXB style) - Bean 绑定</li>
 *   <li>XmlBuilder - Fluent API for building XML - 流式构建</li>
 *   <li>XsltTransformer - XSLT transformation - XSLT 转换</li>
 *   <li>SchemaValidator - XSD/DTD validation - Schema 验证</li>
 *   <li>XmlSecurity - XXE protection (default enabled) - XXE 防护</li>
 *   <li>Namespace support - 命名空间支持</li>
 * </ul>
 *
 * <p><strong>Parser Selection Guide | 解析模式选择:</strong></p>
 * <ul>
 *   <li>DOM - Small files (&lt;10MB), random access, XPath - 小文件、随机访问</li>
 *   <li>SAX - Large files, read-only, sequential - 大文件、只读、顺序处理</li>
 *   <li>StAX - Large files, streaming, best performance - 大文件、流式、最佳性能</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-xml V1.0.3
 */
module cloud.opencode.base.xml {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // JDK XML modules
    requires java.xml;

    // Export public API packages
    exports cloud.opencode.base.xml;
    exports cloud.opencode.base.xml.bind;
    exports cloud.opencode.base.xml.bind.adapter;
    exports cloud.opencode.base.xml.bind.annotation;
    exports cloud.opencode.base.xml.builder;
    exports cloud.opencode.base.xml.dom;
    exports cloud.opencode.base.xml.exception;
    exports cloud.opencode.base.xml.namespace;
    exports cloud.opencode.base.xml.sax;
    exports cloud.opencode.base.xml.security;
    exports cloud.opencode.base.xml.stax;
    exports cloud.opencode.base.xml.transform;
    exports cloud.opencode.base.xml.validate;
    exports cloud.opencode.base.xml.xpath;
    exports cloud.opencode.base.xml.diff;
    exports cloud.opencode.base.xml.merge;
    exports cloud.opencode.base.xml.path;
    exports cloud.opencode.base.xml.splitter;
    exports cloud.opencode.base.xml.canonical;
}
