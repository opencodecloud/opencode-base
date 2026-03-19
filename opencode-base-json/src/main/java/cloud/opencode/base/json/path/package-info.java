/**
 * JSON Path - JSONPath and JSON Pointer Support
 * JSON Path - JSONPath 和 JSON Pointer 支持
 *
 * <p>This package provides JSONPath (XPath-style) and JSON Pointer (RFC 6901)
 * for accessing nested JSON data without full deserialization.</p>
 * <p>本包提供 JSONPath（XPath 风格）和 JSON Pointer（RFC 6901）用于访问嵌套 JSON 数据，
 * 无需完全反序列化。</p>
 *
 * <p><strong>Key Classes | 核心类:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.json.path.JsonPath} - JSONPath tool - JSONPath 工具</li>
 *   <li>{@link cloud.opencode.base.json.path.JsonPointer} - JSON Pointer (RFC 6901) - JSON 指针</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // JSONPath
 * List<String> titles = JsonPath.read(json, "$.store.book[*].title");
 *
 * // JSON Pointer
 * String city = JsonPointer.read(json, "/address/city");
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
package cloud.opencode.base.json.path;
