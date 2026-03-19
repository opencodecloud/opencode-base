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
 * Log Markers - Log Event Categorization
 * 日志标记 - 日志事件分类
 *
 * <p>This package provides marker support for categorizing and filtering
 * log events. Markers allow semantic tagging of log messages for better
 * organization and filtering.</p>
 * <p>本包提供标记支持，用于分类和过滤日志事件。
 * 标记允许对日志消息进行语义标记，以便更好地组织和过滤。</p>
 *
 * <h2>Key Classes | 核心类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.log.marker.Marker} - Marker interface</li>
 *   <li>{@link cloud.opencode.base.log.marker.Markers} - Marker factory and predefined markers</li>
 * </ul>
 *
 * <h2>Predefined Markers | 预定义标记</h2>
 * <ul>
 *   <li>{@code SECURITY} - Security-related events (login, access control)</li>
 *   <li>{@code PERFORMANCE} - Performance-related events (slow queries)</li>
 *   <li>{@code AUDIT} - Audit events (data changes, compliance)</li>
 *   <li>{@code BUSINESS} - Business logic events</li>
 *   <li>{@code SYSTEM} - System events (startup, shutdown)</li>
 *   <li>{@code DATABASE} - Database operations</li>
 *   <li>{@code NETWORK} - Network operations</li>
 * </ul>
 *
 * <h2>Usage Examples | 使用示例</h2>
 * <pre>{@code
 * // Using predefined markers
 * log.info(Markers.SECURITY, "User {} logged in from {}", userId, ip);
 * log.warn(Markers.PERFORMANCE, "Slow query detected: {}ms", duration);
 * log.info(Markers.AUDIT, "Data updated by {}", userId);
 *
 * // Creating custom markers
 * Marker customMarker = Markers.getMarker("PAYMENT");
 * log.info(customMarker, "Payment processed: {}", transactionId);
 *
 * // Marker hierarchy
 * Marker parent = Markers.getMarker("TRANSACTION");
 * Marker child = Markers.getMarker("PAYMENT");
 * parent.add(child);
 * // child.contains(parent) returns true
 * }</pre>
 *
 * <h2>Filtering by Marker | 按标记过滤</h2>
 * <p>Configure your logging framework to filter by marker:</p>
 * <pre>
 * &lt;!-- Logback example --&gt;
 * &lt;filter class="ch.qos.logback.core.filter.EvaluatorFilter"&gt;
 *     &lt;evaluator class="ch.qos.logback.classic.boolex.OnMarkerEvaluator"&gt;
 *         &lt;marker&gt;SECURITY&lt;/marker&gt;
 *     &lt;/evaluator&gt;
 *     &lt;onMatch&gt;ACCEPT&lt;/onMatch&gt;
 * &lt;/filter&gt;
 * </pre>
 *
 * @author OpenCode Cloud Group
 * @see cloud.opencode.base.log.marker.Marker
 * @see cloud.opencode.base.log.marker.Markers
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
package cloud.opencode.base.log.marker;
