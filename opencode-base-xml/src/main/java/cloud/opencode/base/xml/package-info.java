

/**
 * OpenCode Base XML Module - Comprehensive XML processing library
 * OpenCode Base XML 模块 - 全面的 XML 处理库
 *
 * <p>This module provides a complete set of XML processing utilities including:</p>
 * <p>此模块提供完整的 XML 处理工具集，包括：</p>
 *
 * <ul>
 *   <li>DOM parsing and manipulation - DOM 解析和操作</li>
 *   <li>SAX event-driven parsing - SAX 事件驱动解析</li>
 *   <li>StAX pull-mode parsing - StAX 拉模式解析</li>
 *   <li>XPath query support - XPath 查询支持</li>
 *   <li>XML-to-Bean binding (JAXB-style) - XML 到 Bean 绑定（JAXB 风格）</li>
 *   <li>XSLT transformation - XSLT 转换</li>
 *   <li>Schema/DTD validation - Schema/DTD 验证</li>
 *   <li>XXE attack protection - XXE 攻击防护</li>
 *   <li>Namespace handling - 命名空间处理</li>
 * </ul>
 *
 * <p><strong>Quick Start | 快速开始:</strong></p>
 * <pre>{@code
 * // Parse XML
 * XmlDocument doc = OpenXml.parse(xml);
 *
 * // Build XML
 * String xml = OpenXml.builder("root")
 *     .element("name", "value")
 *     .toXml(4);
 *
 * // XPath query
 * String value = OpenXml.xpath(doc, "//name/text()");
 *
 * // Validate
 * boolean valid = OpenXml.isWellFormed(xml);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
package cloud.opencode.base.xml;
