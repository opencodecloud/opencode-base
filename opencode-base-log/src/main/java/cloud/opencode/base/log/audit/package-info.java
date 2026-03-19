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
 * Audit Logging - Compliance and Security Event Recording
 * 审计日志 - 合规和安全事件记录
 *
 * <p>This package provides audit logging capabilities for recording user
 * actions, security events, and data changes for compliance and analysis.</p>
 * <p>本包提供审计日志功能，用于记录用户操作、安全事件和数据更改，
 * 以满足合规性和分析需求。</p>
 *
 * <h2>Key Classes | 核心类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.log.audit.AuditLog} - Static audit logging utility</li>
 *   <li>{@link cloud.opencode.base.log.audit.AuditEvent} - Immutable audit event record</li>
 *   <li>{@link cloud.opencode.base.log.audit.AuditLogger} - Custom logger interface</li>
 * </ul>
 *
 * <h2>Simple Audit Logging | 简单审计日志</h2>
 * <pre>{@code
 * // Basic logging
 * AuditLog.log("user123", "LOGIN", "system", "SUCCESS");
 *
 * // Login/Logout events
 * AuditLog.logLogin("user123", true, "192.168.1.1");
 * AuditLog.logLogout("user123");
 *
 * // Data change events
 * AuditLog.logDataChange("admin", "User", "user456", "UPDATE", oldUser, newUser);
 * }</pre>
 *
 * <h2>Builder Pattern | 构建器模式</h2>
 * <pre>{@code
 * AuditLog.event("UPDATE_USER")
 *     .userId("admin")
 *     .target("User")
 *     .targetId("user456")
 *     .success()
 *     .ip("192.168.1.1")
 *     .detail("field", "email")
 *     .detail("oldValue", "old@example.com")
 *     .detail("newValue", "new@example.com")
 *     .build();  // Automatically logged
 * }</pre>
 *
 * <h2>Custom Audit Logger | 自定义审计记录器</h2>
 * <pre>{@code
 * // Implement custom logger (e.g., database storage)
 * public class DatabaseAuditLogger implements AuditLogger {
 *     @Override
 *     public void log(AuditEvent event) {
 *         auditRepository.save(event);
 *     }
 * }
 *
 * // Register custom logger
 * AuditLog.setLogger(new DatabaseAuditLogger());
 * }</pre>
 *
 * <h2>Audit Event Fields | 审计事件字段</h2>
 * <ul>
 *   <li>{@code eventId} - Unique event identifier</li>
 *   <li>{@code timestamp} - Event timestamp</li>
 *   <li>{@code userId} - User who performed the action</li>
 *   <li>{@code action} - Action type (LOGIN, UPDATE, DELETE, etc.)</li>
 *   <li>{@code target} - Target entity type</li>
 *   <li>{@code targetId} - Target entity ID</li>
 *   <li>{@code result} - SUCCESS or FAILURE</li>
 *   <li>{@code ip} - Client IP address</li>
 *   <li>{@code userAgent} - Client user agent</li>
 *   <li>{@code details} - Additional key-value details</li>
 * </ul>
 *
 * @author OpenCode Cloud Group
 * @see cloud.opencode.base.log.audit.AuditLog
 * @see cloud.opencode.base.log.audit.AuditEvent
 * @see cloud.opencode.base.log.audit.AuditLogger
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
package cloud.opencode.base.log.audit;
