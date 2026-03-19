

/**
 * XML Security - Security configuration for XML parsers
 * XML 安全 - XML 解析器的安全配置
 *
 * <p>This package provides utilities for secure XML processing:</p>
 * <p>此包提供安全 XML 处理的工具：</p>
 *
 * <ul>
 *   <li>{@link cloud.opencode.base.xml.security.XmlSecurity} - Security configuration - 安全配置</li>
 *   <li>{@link cloud.opencode.base.xml.security.SecureParserFactory} - Secure parser factory - 安全解析器工厂</li>
 * </ul>
 *
 * <p><strong>Security Features | 安全特性:</strong></p>
 * <ul>
 *   <li>XXE (XML External Entity) attack prevention - XXE 攻击防护</li>
 *   <li>DTD processing control - DTD 处理控制</li>
 *   <li>External entity resolution blocking - 外部实体解析阻止</li>
 *   <li>Entity expansion limits - 实体扩展限制</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
package cloud.opencode.base.xml.security;
