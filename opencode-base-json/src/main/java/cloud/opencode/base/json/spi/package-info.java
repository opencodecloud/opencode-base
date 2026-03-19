/**
 * JSON SPI - Service Provider Interface for JSON Engines
 * JSON SPI - JSON 引擎的服务提供者接口
 *
 * <p>This package defines the SPI interfaces for pluggable JSON engine implementations.
 * Supports Jackson, Gson, Fastjson2 and custom implementations.</p>
 * <p>本包定义可插拔 JSON 引擎实现的 SPI 接口。支持 Jackson、Gson、Fastjson2 和自定义实现。</p>
 *
 * <p><strong>Key Interfaces | 核心接口:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.json.spi.JsonProvider} - JSON provider interface - JSON 提供者接口</li>
 *   <li>{@link cloud.opencode.base.json.spi.JsonProviderFactory} - Provider factory - 提供者工厂</li>
 *   <li>{@link cloud.opencode.base.json.spi.JsonFeature} - Feature enum - 特性枚举</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
package cloud.opencode.base.json.spi;
