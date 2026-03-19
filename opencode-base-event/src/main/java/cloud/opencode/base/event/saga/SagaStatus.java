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

package cloud.opencode.base.event.saga;

/**
 * Saga Status - The status of a saga execution
 * Saga 状态 - saga 执行的状态
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Saga lifecycle status tracking - Saga生命周期状态跟踪</li>
 *   <li>Seven distinct states - 七种不同状态</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SagaStatus status = result.status();
 * if (status == SagaStatus.COMPENSATED) {
 *     // Handle compensation
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public enum SagaStatus {

    /** Saga is currently running */
    RUNNING,

    /** Saga completed successfully */
    COMPLETED,

    /** Saga failed and compensation was executed */
    COMPENSATED,

    /** Saga failed (compensation may have failed too) */
    FAILED,

    /** Saga was cancelled */
    CANCELLED,

    /** Saga is pending execution */
    PENDING,

    /** Saga is paused */
    PAUSED
}
