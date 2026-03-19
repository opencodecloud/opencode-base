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

import java.time.Duration;
import java.time.Instant;

/**
 * Saga Result - The result of a saga execution
 * Saga 结果 - saga 执行的结果
 *
 * @param <T> the context type - 上下文类型
 * @param sagaId the unique saga execution ID - 唯一的 saga 执行 ID
 * @param sagaName the saga name - saga 名称
 * @param status the final status - 最终状态
 * @param context the saga context - saga 上下文
 * @param startTime the execution start time - 执行开始时间
 * @param endTime the execution end time - 执行结束时间
 * @param failedStep the name of the failed step (if any) - 失败步骤的名称（如果有）
 * @param error the error that caused failure (if any) - 导致失败的错误（如果有）
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Saga execution result tracking - Saga执行结果跟踪</li>
 *   <li>Duration and error information - 时长和错误信息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SagaResult<MyContext> result = saga.execute(context);
 * if (result.isSuccess()) {
 *     System.out.println("Completed in " + result.getDuration());
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
public record SagaResult<T>(
        String sagaId,
        String sagaName,
        SagaStatus status,
        T context,
        Instant startTime,
        Instant endTime,
        String failedStep,
        Throwable error
) {
    /**
     * Checks if the saga completed successfully.
     * 检查 saga 是否成功完成。
     *
     * @return true if completed - 如果完成返回 true
     */
    public boolean isSuccess() {
        return status == SagaStatus.COMPLETED;
    }

    /**
     * Checks if the saga failed and was compensated.
     * 检查 saga 是否失败并已补偿。
     *
     * @return true if compensated - 如果已补偿返回 true
     */
    public boolean isCompensated() {
        return status == SagaStatus.COMPENSATED;
    }

    /**
     * Checks if the saga failed.
     * 检查 saga 是否失败。
     *
     * @return true if failed - 如果失败返回 true
     */
    public boolean isFailed() {
        return status == SagaStatus.FAILED || status == SagaStatus.COMPENSATED;
    }

    /**
     * Gets the execution duration.
     * 获取执行时长。
     *
     * @return the duration - 时长
     */
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    /**
     * Gets the error message if any.
     * 获取错误消息（如果有）。
     *
     * @return the error message or null - 错误消息或 null
     */
    public String getErrorMessage() {
        return error != null ? error.getMessage() : null;
    }
}
