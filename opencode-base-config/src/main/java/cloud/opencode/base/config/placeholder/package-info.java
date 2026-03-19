/**
 * Placeholder Resolution Package
 * 占位符解析包
 *
 * <p>This package provides placeholder resolution capabilities for
 * replacing ${key} placeholders in configuration values.</p>
 * <p>此包提供占位符解析能力，用于替换配置值中的${key}占位符。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Basic placeholder: ${key} - 基本占位符</li>
 *   <li>Default values: ${key:default} - 带默认值的占位符</li>
 *   <li>Nested placeholders: ${${env}.db.url} - 嵌套占位符</li>
 *   <li>Recursion depth limit - 递归深度限制</li>
 *   <li>Circular reference detection - 循环引用检测</li>
 * </ul>
 *
 * <p><strong>Components | 组件:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.config.placeholder.PlaceholderResolver} - Main placeholder resolver - 主占位符解析器</li>
 *   <li>{@link cloud.opencode.base.config.placeholder.ExpressionEvaluator} - Expression evaluator - 表达式求值器</li>
 * </ul>
 *
 * <p><strong>Placeholder Syntax | 占位符语法:</strong></p>
 * <pre>
 * ${key}           - Simple reference - 简单引用
 * ${key:default}   - With default value - 带默认值
 * ${key:-default}  - With default if empty - 空值时使用默认值
 * ${${env}.url}    - Nested reference - 嵌套引用
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Configuration file
 * app.name=MyApp
 * app.version=1.0.0
 * app.title=${app.name} v${app.version}
 * log.level=${LOG_LEVEL:INFO}
 * database.url=jdbc:mysql://${database.host:localhost}:${database.port:3306}/${database.name}
 *
 * // Result after resolution
 * app.title=MyApp v1.0.0
 * log.level=INFO (or value from LOG_LEVEL env var)
 * database.url=jdbc:mysql://localhost:3306/mydb
 * }</pre>
 *
 * <p><strong>Error Handling | 错误处理:</strong></p>
 * <ul>
 *   <li>Missing key without default → {@link cloud.opencode.base.config.OpenConfigException}</li>
 *   <li>Recursion too deep → {@link cloud.opencode.base.config.OpenConfigException}</li>
 *   <li>Circular reference → {@link cloud.opencode.base.config.OpenConfigException}</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
package cloud.opencode.base.config.placeholder;
