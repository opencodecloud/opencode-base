/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Log Context - MDC and NDC Context Management
 * 日志上下文 - MDC 和 NDC 上下文管理
 *
 * <p>This package provides thread-local context management for enriching
 * log messages with contextual information like trace IDs, user IDs, etc.</p>
 * <p>本包提供线程本地上下文管理，用于通过追踪 ID、用户 ID 等上下文信息丰富日志消息。</p>
 *
 * <h2>Key Classes | 核心类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.log.context.MDC} - Mapped Diagnostic Context (key-value)</li>
 *   <li>{@link cloud.opencode.base.log.context.NDC} - Nested Diagnostic Context (stack-based)</li>
 *   <li>{@link cloud.opencode.base.log.context.LogContext} - Unified context management</li>
 * </ul>
 *
 * <h2>MDC Usage | MDC 使用</h2>
 * <pre>{@code
 * // Basic usage
 * MDC.put("traceId", "abc-123");
 * MDC.put("userId", "user456");
 * log.info("Processing request"); // Will include traceId and userId
 * MDC.clear();
 *
 * // AutoCloseable scope (recommended)
 * try (var scope = MDC.scope("requestId", requestId)) {
 *     processRequest();
 * } // Automatically cleaned up
 *
 * // Lambda execution
 * MDC.runWith(Map.of("key", "value"), () -> {
 *     // MDC context active here
 * });
 * }</pre>
 *
 * <h2>NDC Usage | NDC 使用</h2>
 * <pre>{@code
 * NDC.push("entering method A");
 * NDC.push("processing item X");
 * log.debug("Current operation"); // Stack: [entering method A, processing item X]
 * NDC.pop();
 * NDC.pop();
 *
 * // AutoCloseable scope
 * try (var scope = NDC.scope("operation-name")) {
 *     performOperation();
 * }
 * }</pre>
 *
 * <h2>LogContext for Standard Keys | LogContext 标准键</h2>
 * <pre>{@code
 * LogContext.setTraceId("trace-123");
 * LogContext.setUserId("user-456");
 * LogContext.setTenantId("tenant-789");
 *
 * // Async context propagation
 * ContextSnapshot snapshot = LogContext.capture();
 * executor.submit(() -> snapshot.runWith(() -> {
 *     // MDC context restored here
 * }));
 * }</pre>
 *
 * @author OpenCode Cloud Group
 * @see cloud.opencode.base.log.context.MDC
 * @see cloud.opencode.base.log.context.NDC
 * @see cloud.opencode.base.log.context.LogContext
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
package cloud.opencode.base.log.context;
