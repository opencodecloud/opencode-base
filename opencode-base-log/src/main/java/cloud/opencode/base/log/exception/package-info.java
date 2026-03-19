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
 * Log Exceptions - Exception Classes for Log Module
 * 日志异常 - 日志模块的异常类
 *
 * <p>This package contains exception classes specific to the logging module,
 * providing clear error information for logging-related failures.</p>
 * <p>本包包含日志模块特定的异常类，为日志相关的失败提供清晰的错误信息。</p>
 *
 * <h2>Key Classes | 核心类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.log.exception.OpenLogException} - Base log exception</li>
 * </ul>
 *
 * <h2>Factory Methods | 工厂方法</h2>
 * <pre>{@code
 * // Provider not found
 * throw OpenLogException.providerNotFound("SLF4J");
 *
 * // Initialization failed
 * throw OpenLogException.initializationFailed("Failed to load provider", cause);
 *
 * // Invalid configuration
 * throw OpenLogException.invalidConfig("Invalid log level: UNKNOWN");
 *
 * // Adapter not found
 * throw OpenLogException.adapterNotFound("MDCAdapter");
 * }</pre>
 *
 * <h2>Exception Hierarchy | 异常层次</h2>
 * <pre>
 * OpenException (from opencode-base-core)
 *   └── OpenLogException
 * </pre>
 *
 * @author OpenCode Cloud Group
 * @see cloud.opencode.base.log.exception.OpenLogException
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
package cloud.opencode.base.log.exception;
