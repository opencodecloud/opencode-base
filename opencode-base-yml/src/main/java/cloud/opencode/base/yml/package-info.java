

/**
 * OpenCode Base YML - YAML Processing Library
 * OpenCode Base YML - YAML 处理库
 *
 * <p>This package provides comprehensive YAML parsing, binding, and manipulation utilities.</p>
 * <p>此包提供全面的 YAML 解析、绑定和操作工具。</p>
 *
 * <h2>Key Features | 主要特性</h2>
 * <ul>
 *   <li>YAML parsing and writing with SPI support | 支持 SPI 的 YAML 解析和写入</li>
 *   <li>Object binding with annotations | 带注解的对象绑定</li>
 *   <li>Placeholder resolution (${...}) | 占位符解析（${...}）</li>
 *   <li>Document merging | 文档合并</li>
 *   <li>Path-based access | 基于路径的访问</li>
 *   <li>Security features | 安全特性</li>
 * </ul>
 *
 * <h2>Quick Start | 快速开始</h2>
 * <pre>{@code
 * // Parse YAML
 * YmlDocument doc = OpenYml.parse("""
 *     server:
 *       port: 8080
 *       host: localhost
 *     """);
 *
 * // Access values
 * int port = doc.getInt("server.port");
 * String host = doc.getString("server.host");
 *
 * // Bind to object
 * ServerConfig config = OpenYml.bind(doc, "server", ServerConfig.class);
 *
 * // Write YAML
 * String yaml = OpenYml.dump(doc);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
package cloud.opencode.base.yml;
