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
import java.util.function.Consumer;

/**
 * Saga Step - A single step in a saga transaction
 * Saga 步骤 - saga 事务中的单个步骤
 *
 * @param <T> the context type - 上下文类型
 * @param name the step name - 步骤名称
 * @param action the action to execute - 要执行的操作
 * @param compensation the compensation action for rollback - 用于回滚的补偿操作
 * @param timeout the timeout for this step - 此步骤的超时
 * @param maxRetries the maximum number of retries - 最大重试次数
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Individual saga step definition - 单个saga步骤定义</li>
 *   <li>Action and compensation pairing - 操作和补偿配对</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SagaStep<OrderContext> step = SagaStep.of(
 *     "reserve-inventory",
 *     ctx -> inventoryService.reserve(ctx),
 *     ctx -> inventoryService.release(ctx)
 * );
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
public record SagaStep<T>(
        String name,
        Consumer<T> action,
        Consumer<T> compensation,
        Duration timeout,
        int maxRetries
) {
    /**
     * Creates a simple step with only an action.
     * 创建只有操作的简单步骤。
     *
     * @param name the step name - 步骤名称
     * @param action the action - 操作
     * @param <T> the context type - 上下文类型
     * @return the step - 步骤
     */
    public static <T> SagaStep<T> of(String name, Consumer<T> action) {
        return new SagaStep<>(name, action, null, null, 0);
    }

    /**
     * Creates a step with action and compensation.
     * 创建带有操作和补偿的步骤。
     *
     * @param name the step name - 步骤名称
     * @param action the action - 操作
     * @param compensation the compensation - 补偿
     * @param <T> the context type - 上下文类型
     * @return the step - 步骤
     */
    public static <T> SagaStep<T> of(String name, Consumer<T> action, Consumer<T> compensation) {
        return new SagaStep<>(name, action, compensation, null, 0);
    }
}
