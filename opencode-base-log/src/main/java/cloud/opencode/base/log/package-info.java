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
 * OpenCode Base Log - Lightweight Logging Facade
 * OpenCode 基础日志 - 轻量级日志门面
 *
 * <p>This package provides a lightweight logging facade with SPI mechanism
 * for pluggable log engines. It supports multiple logging frameworks like
 * SLF4J, Log4j2, and JUL through a unified API.</p>
 * <p>本包提供轻量级日志门面，支持 SPI 可插拔日志引擎。
 * 通过统一 API 支持 SLF4J、Log4j2、JUL 等多种日志框架。</p>
 *
 * <h2>Key Features | 核心特性</h2>
 * <ul>
 *   <li>Zero-configuration static logging via {@link cloud.opencode.base.log.OpenLog}</li>
 *   <li>Instance-based logging via {@link cloud.opencode.base.log.LoggerFactory}</li>
 *   <li>Lambda lazy evaluation for expensive log messages</li>
 *   <li>MDC/NDC context management</li>
 *   <li>Marker-based categorization</li>
 *   <li>Performance logging with StopWatch</li>
 *   <li>Audit logging for compliance</li>
 *   <li>Structured JSON logging</li>
 *   <li>Data masking for sensitive information</li>
 *   <li>Sampled logging for high-frequency events</li>
 * </ul>
 *
 * <h2>Quick Start | 快速开始</h2>
 * <pre>{@code
 * // Static logging (zero-config)
 * import static cloud.opencode.base.log.OpenLog.*;
 *
 * info("Application started");
 * warn("Low memory: {}MB", availableMemory);
 * error("Failed to connect", exception);
 *
 * // Instance-based logging
 * Logger log = LoggerFactory.getLogger(MyClass.class);
 * log.info("Processing request: {}", requestId);
 *
 * // Lambda lazy evaluation
 * log.debug(() -> "Expensive: " + computeExpensiveValue());
 * }</pre>
 *
 * <h2>Package Structure | 包结构</h2>
 * <ul>
 *   <li>{@code cloud.opencode.base.log} - Core logging classes</li>
 *   <li>{@code cloud.opencode.base.log.spi} - SPI interfaces and providers</li>
 *   <li>{@code cloud.opencode.base.log.context} - MDC/NDC context management</li>
 *   <li>{@code cloud.opencode.base.log.marker} - Marker support</li>
 *   <li>{@code cloud.opencode.base.log.perf} - Performance logging</li>
 *   <li>{@code cloud.opencode.base.log.audit} - Audit logging</li>
 *   <li>{@code cloud.opencode.base.log.enhance} - Enhanced logging features</li>
 *   <li>{@code cloud.opencode.base.log.exception} - Exception classes</li>
 * </ul>
 *
 * @author OpenCode Cloud Group
 * @see cloud.opencode.base.log.OpenLog
 * @see cloud.opencode.base.log.LoggerFactory
 * @see cloud.opencode.base.log.Logger
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
package cloud.opencode.base.log;
