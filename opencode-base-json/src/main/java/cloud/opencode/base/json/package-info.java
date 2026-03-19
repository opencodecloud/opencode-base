/**
 * OpenCode JSON - Unified JSON Processing Facade
 * OpenCode JSON - 统一 JSON 处理门面
 *
 * <p>This package provides a unified facade API for JSON serialization/deserialization
 * with SPI mechanism supporting multiple JSON engines (Jackson, Gson, Fastjson2).</p>
 * <p>本包提供统一的 JSON 序列化/反序列化门面 API，通过 SPI 机制支持多种 JSON 引擎
 * （Jackson、Gson、Fastjson2）。</p>
 *
 * <p><strong>Key Classes | 核心类:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.json.OpenJson} - Static facade entry point - 静态门面入口</li>
 *   <li>{@link cloud.opencode.base.json.TypeReference} - Generic type reference - 泛型类型引用</li>
 *   <li>{@link cloud.opencode.base.json.JsonConfig} - JSON configuration - JSON 配置</li>
 *   <li>{@link cloud.opencode.base.json.JsonNode} - Generic JSON node - 通用 JSON 节点</li>
 * </ul>
 *
 * <p><strong>Sub-packages | 子包:</strong></p>
 * <ul>
 *   <li>{@code spi} - SPI provider interfaces - SPI 提供者接口</li>
 *   <li>{@code path} - JSONPath and JsonPointer - JSONPath 和 JsonPointer</li>
 *   <li>{@code stream} - Streaming API - 流式 API</li>
 *   <li>{@code annotation} - JSON annotations - JSON 注解</li>
 *   <li>{@code exception} - JSON exceptions - JSON 异常</li>
 *   <li>{@code patch} - JSON Patch/Merge Patch - JSON 补丁</li>
 *   <li>{@code schema} - JSON Schema validation - JSON Schema 验证</li>
 *   <li>{@code diff} - JSON diff comparison - JSON 差异比较</li>
 *   <li>{@code security} - Security features - 安全特性</li>
 *   <li>{@code adapter} - Type adapters - 类型适配器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Serialize
 * String json = OpenJson.toJson(user);
 *
 * // Deserialize
 * User user = OpenJson.fromJson(json, User.class);
 *
 * // Generic type
 * List<User> users = OpenJson.fromJson(json, new TypeReference<List<User>>() {});
 *
 * // JSONPath
 * String name = OpenJson.read(json, "$.user.name");
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
package cloud.opencode.base.json;
