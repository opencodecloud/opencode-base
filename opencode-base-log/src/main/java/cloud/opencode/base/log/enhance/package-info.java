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
 * Enhanced Logging - Advanced Logging Features
 * 增强日志 - 高级日志功能
 *
 * <p>This package provides enhanced logging capabilities including
 * structured JSON logging, data masking, and sampled logging.</p>
 * <p>本包提供增强的日志功能，包括结构化 JSON 日志、数据脱敏和采样日志。</p>
 *
 * <h2>Key Classes | 核心类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.log.enhance.StructuredLog} - JSON-style structured logging</li>
 *   <li>{@link cloud.opencode.base.log.enhance.LogMasking} - Sensitive data masking</li>
 *   <li>{@link cloud.opencode.base.log.enhance.SampledLog} - Rate-limited sampled logging</li>
 *   <li>{@link cloud.opencode.base.log.enhance.VirtualThreadContext} - Virtual Thread context propagation (JDK 25+)</li>
 *   <li>{@link cloud.opencode.base.log.enhance.ScopedLogContext} - ScopedValue-based log context (JDK 25+)</li>
 *   <li>{@link cloud.opencode.base.log.enhance.ConditionalLog} - Environment and rate-based conditional logging</li>
 *   <li>{@link cloud.opencode.base.log.enhance.LogMetrics} - Logging with built-in metrics collection</li>
 *   <li>{@link cloud.opencode.base.log.enhance.ExceptionLog} - Smart exception logging with root cause analysis</li>
 * </ul>
 *
 * <h2>Structured Logging | 结构化日志</h2>
 * <pre>{@code
 * StructuredLog.info()
 *     .message("User login successful")
 *     .field("userId", "user123")
 *     .field("ip", "192.168.1.1")
 *     .field("duration", 150)
 *     .traceId("abc-123")
 *     .log();
 *
 * // Output: {"message":"User login successful","traceId":"abc-123",
 * //          "userId":"user123","ip":"192.168.1.1","duration":150}
 * }</pre>
 *
 * <h2>Data Masking | 数据脱敏</h2>
 * <pre>{@code
 * // Predefined masking strategies
 * LogMasking.mask("13812345678", MaskingStrategy.PHONE);    // 138****5678
 * LogMasking.mask("test@example.com", MaskingStrategy.EMAIL); // t***@example.com
 * LogMasking.mask("110101199001011234", MaskingStrategy.ID_CARD); // 110***********1234
 * LogMasking.mask("secret123", MaskingStrategy.PASSWORD);    // [PROTECTED]
 *
 * // Register field-based masking
 * LogMasking.registerRule("phone", MaskingStrategy.PHONE);
 * LogMasking.registerRule("password", MaskingStrategy.PASSWORD);
 * String masked = LogMasking.maskByField("phone", "13812345678");
 * }</pre>
 *
 * <h2>Sampled Logging | 采样日志</h2>
 * <pre>{@code
 * // Probability-based: 1% of messages
 * SampledLogger sampled = SampledLog.sample(0.01);
 * sampled.info("High frequency event: {}", eventId);
 *
 * // Time-based: at most once per 5 seconds
 * SampledLogger rateLimited = SampledLog.sampleByTime(Duration.ofSeconds(5));
 * rateLimited.warn("Rate limited warning");
 *
 * // Count-based: every 100th message
 * SampledLogger countBased = SampledLog.sampleByCount(100);
 * countBased.debug("Processing item {}", itemId);
 * }</pre>
 *
 * <h2>Masking Strategies | 脱敏策略</h2>
 * <ul>
 *   <li>{@code FULL} - Complete masking: ******</li>
 *   <li>{@code PHONE} - Phone number: 138****5678</li>
 *   <li>{@code EMAIL} - Email: u***@example.com</li>
 *   <li>{@code ID_CARD} - ID card: 110***********1234</li>
 *   <li>{@code BANK_CARD} - Bank card: ************1234</li>
 *   <li>{@code PASSWORD} - Password: [PROTECTED]</li>
 *   <li>{@code NAME} - Name: 张*三</li>
 *   <li>{@code ADDRESS} - Address: 北京市海淀区****</li>
 * </ul>
 *
 * @author OpenCode Cloud Group
 * @see cloud.opencode.base.log.enhance.StructuredLog
 * @see cloud.opencode.base.log.enhance.LogMasking
 * @see cloud.opencode.base.log.enhance.SampledLog
 * @see cloud.opencode.base.log.enhance.VirtualThreadContext
 * @see cloud.opencode.base.log.enhance.ScopedLogContext
 * @see cloud.opencode.base.log.enhance.ConditionalLog
 * @see cloud.opencode.base.log.enhance.LogMetrics
 * @see cloud.opencode.base.log.enhance.ExceptionLog
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
package cloud.opencode.base.log.enhance;
