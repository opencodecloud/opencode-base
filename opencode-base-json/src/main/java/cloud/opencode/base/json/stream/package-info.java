/**
 * JSON Stream - Streaming API for Large JSON Processing
 * JSON Stream - 大 JSON 处理的流式 API
 *
 * <p>This package provides streaming API for processing large JSON files
 * without loading everything into memory.</p>
 * <p>本包提供流式 API 用于处理大型 JSON 文件，无需将所有内容加载到内存中。</p>
 *
 * <p><strong>Key Interfaces | 核心接口:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.json.stream.JsonReader} - Streaming reader - 流式读取器</li>
 *   <li>{@link cloud.opencode.base.json.stream.JsonWriter} - Streaming writer - 流式写入器</li>
 *   <li>{@link cloud.opencode.base.json.stream.JsonToken} - Token enum - Token 枚举</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (JsonReader reader = JsonReader.create(inputStream)) {
 *     while (reader.nextToken() != null) {
 *         User user = reader.readValueAs(User.class);
 *         process(user);
 *     }
 * }
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
package cloud.opencode.base.json.stream;
