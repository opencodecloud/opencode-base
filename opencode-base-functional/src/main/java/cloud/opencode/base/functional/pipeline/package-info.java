/**
 * Pipeline - Composable data transformation pipelines
 * 管道 - 可组合的数据转换管道
 *
 * <p>Provides a fluent API for building reusable data transformation pipelines
 * with support for validation, logging, and parallel execution.</p>
 * <p>提供构建可复用数据转换管道的流式 API，支持验证、日志和并行执行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.functional.pipeline.Pipeline} - Pipeline builder</li>
 *   <li>{@link cloud.opencode.base.functional.pipeline.PipeUtil} - Pipeline utilities</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a reusable pipeline
 * Pipeline<String, User> userPipeline = Pipeline.<String>start()
 *     .then(String::trim)
 *     .peek(s -> log.debug("Processing: {}", s))
 *     .thenTry(this::parseJson)
 *     .then(this::mapToUser);
 *
 * // Execute
 * User user = userPipeline.execute(jsonString);
 *
 * // Batch execute with Virtual Threads
 * List<User> users = userPipeline.executeBatch(jsonStrings);
 *
 * // Async execution
 * CompletableFuture<User> future = userPipeline.executeAsync(jsonString);
 * }</pre>
 *
 * <p><strong>vs Stream API | 与 Stream API 的区别:</strong></p>
 * <ul>
 *   <li>Pipeline: Reusable, single-value focused - 可复用，单值导向</li>
 *   <li>Stream: One-time, collection focused - 一次性，集合导向</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
package cloud.opencode.base.functional.pipeline;
