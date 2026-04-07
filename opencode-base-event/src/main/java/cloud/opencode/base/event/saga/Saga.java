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

import cloud.opencode.base.event.exception.EventException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Saga - Distributed Transaction Orchestration Pattern
 * Saga - 分布式事务编排模式
 *
 * <p>Implements the Saga pattern for managing distributed transactions through
 * a sequence of local transactions with compensating actions for rollback.</p>
 * <p>实现 Saga 模式，通过一系列本地事务和补偿操作来管理分布式事务。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Step-based transaction orchestration - 基于步骤的事务编排</li>
 *   <li>Automatic compensation on failure - 失败时自动补偿</li>
 *   <li>Async execution support - 异步执行支持</li>
 *   <li>Timeout handling - 超时处理</li>
 *   <li>Retry policies - 重试策略</li>
 *   <li>State persistence - 状态持久化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Define a saga for order processing
 * Saga<OrderContext> orderSaga = Saga.<OrderContext>builder()
 *     .name("order-processing")
 *     .step("reserve-inventory")
 *         .action(ctx -> inventoryService.reserve(ctx.getOrderId(), ctx.getItems()))
 *         .compensation(ctx -> inventoryService.release(ctx.getOrderId()))
 *         .timeout(Duration.ofSeconds(30))
 *         .retries(3)
 *         .build()
 *     .step("process-payment")
 *         .action(ctx -> paymentService.charge(ctx.getOrderId(), ctx.getAmount()))
 *         .compensation(ctx -> paymentService.refund(ctx.getOrderId()))
 *         .timeout(Duration.ofSeconds(60))
 *         .build()
 *     .step("ship-order")
 *         .action(ctx -> shippingService.ship(ctx.getOrderId()))
 *         .compensation(ctx -> shippingService.cancel(ctx.getOrderId()))
 *         .build()
 *     .onSuccess(ctx -> notificationService.notifyOrderComplete(ctx))
 *     .onFailure((ctx, error) -> notificationService.notifyOrderFailed(ctx, error))
 *     .build();
 *
 * // Execute the saga
 * SagaResult<OrderContext> result = orderSaga.execute(new OrderContext(orderId, items, amount));
 *
 * // Or execute async
 * CompletableFuture<SagaResult<OrderContext>> future = orderSaga.executeAsync(context);
 * }</pre>
 *
 * @param <T> the saga context type - saga 上下文类型
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see SagaStep
 * @see SagaResult
 * @since JDK 25, opencode-base-event V1.0.0
 */
public final class Saga<T> {

    private static final System.Logger LOGGER = System.getLogger(Saga.class.getName());
    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(
                VIRTUAL_EXECUTOR::close, "saga-executor-shutdown"));
    }

    private final String name;
    private final List<SagaStep<T>> steps;
    private final java.util.function.Consumer<T> onSuccess;
    private final java.util.function.BiConsumer<T, Throwable> onFailure;
    private final Duration globalTimeout;

    private Saga(Builder<T> builder) {
        this.name = builder.name;
        this.steps = Collections.unmodifiableList(new ArrayList<>(builder.steps));
        this.onSuccess = builder.onSuccess;
        this.onFailure = builder.onFailure;
        this.globalTimeout = builder.globalTimeout;
    }

    // ==================== Execution | 执行 ====================

    /**
     * Executes the saga synchronously.
     * 同步执行 saga。
     *
     * @param context the saga context - saga 上下文
     * @return the saga result - saga 结果
     */
    public SagaResult<T> execute(T context) {
        String sagaId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<CompletedStep<T>> completedSteps = new ArrayList<>();

        try {
            for (SagaStep<T> step : steps) {
                // Check global timeout before executing each step
                if (globalTimeout != null) {
                    Duration elapsed = Duration.between(startTime, Instant.now());
                    if (elapsed.compareTo(globalTimeout) >= 0) {
                        Exception timeoutEx = new SagaTimeoutException(
                                "Saga '" + name + "' global timeout exceeded (" + globalTimeout + ")", null);
                        compensate(completedSteps, context);
                        SagaResult<T> result = new SagaResult<>(
                                sagaId, name, SagaStatus.COMPENSATED, context,
                                startTime, Instant.now(), step.name(), timeoutEx);
                        if (onFailure != null) {
                            try { onFailure.accept(context, timeoutEx); }
                            catch (Exception callbackEx) {
                                LOGGER.log(System.Logger.Level.WARNING,
                                        "Saga onFailure callback threw exception", callbackEx);
                            }
                        }
                        return result;
                    }
                }
                try {
                    // Compute effective timeout: use step timeout, or remaining global timeout
                    Duration effectiveTimeout = step.timeout();
                    if (effectiveTimeout == null && globalTimeout != null) {
                        Duration remaining = globalTimeout.minus(Duration.between(startTime, Instant.now()));
                        if (!remaining.isNegative() && !remaining.isZero()) {
                            effectiveTimeout = remaining;
                        }
                    } else if (effectiveTimeout != null && globalTimeout != null) {
                        Duration remaining = globalTimeout.minus(Duration.between(startTime, Instant.now()));
                        if (remaining.compareTo(effectiveTimeout) < 0) {
                            effectiveTimeout = remaining;
                        }
                    }
                    executeStep(step, context, effectiveTimeout);
                    completedSteps.add(new CompletedStep<>(step, Instant.now()));
                } catch (Exception e) {
                    // Step failed - compensate
                    compensate(completedSteps, context);

                    SagaResult<T> result = new SagaResult<>(
                            sagaId,
                            name,
                            SagaStatus.COMPENSATED,
                            context,
                            startTime,
                            Instant.now(),
                            step.name(),
                            e
                    );

                    if (onFailure != null) {
                        try {
                            onFailure.accept(context, e);
                        } catch (Exception callbackEx) {
                            LOGGER.log(System.Logger.Level.WARNING,
                                    "Saga onFailure callback threw exception", callbackEx);
                        }
                    }

                    return result;
                }
            }

            // All steps succeeded
            SagaResult<T> result = new SagaResult<>(
                    sagaId,
                    name,
                    SagaStatus.COMPLETED,
                    context,
                    startTime,
                    Instant.now(),
                    null,
                    null
            );

            if (onSuccess != null) {
                try {
                    onSuccess.accept(context);
                } catch (Exception callbackEx) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "Saga onSuccess callback threw exception", callbackEx);
                }
            }

            return result;

        } catch (Exception e) {
            return new SagaResult<>(
                    sagaId,
                    name,
                    SagaStatus.FAILED,
                    context,
                    startTime,
                    Instant.now(),
                    null,
                    e
            );
        }
    }

    /**
     * Executes the saga asynchronously.
     * 异步执行 saga。
     *
     * @param context the saga context - saga 上下文
     * @return CompletableFuture with saga result - 包含 saga 结果的 CompletableFuture
     */
    public CompletableFuture<SagaResult<T>> executeAsync(T context) {
        return CompletableFuture.supplyAsync(() -> execute(context), VIRTUAL_EXECUTOR);
    }

    // ==================== Info | 信息 ====================

    /**
     * Gets the saga name.
     * 获取 saga 名称。
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the number of steps.
     * 获取步骤数量。
     */
    public int getStepCount() {
        return steps.size();
    }

    /**
     * Gets the step names.
     * 获取步骤名称。
     */
    public List<String> getStepNames() {
        return steps.stream().map(SagaStep::name).toList();
    }

    // ==================== Private Methods | 私有方法 ====================

    private void executeStep(SagaStep<T> step, T context, Duration effectiveTimeout) throws Exception {
        int retries = step.maxRetries();
        Exception lastException = null;

        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                if (effectiveTimeout != null && !effectiveTimeout.isNegative() && !effectiveTimeout.isZero()) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            step.action().accept(context);
                        } catch (Exception e) {
                            throw new EventException("Saga step action failed", e);
                        }
                    }, VIRTUAL_EXECUTOR);

                    future.get(effectiveTimeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
                } else {
                    step.action().accept(context);
                }
                return; // Success

            } catch (java.util.concurrent.TimeoutException e) {
                lastException = new SagaTimeoutException("Step '" + step.name() + "' timed out", e);
            } catch (Exception e) {
                lastException = e instanceof RuntimeException && e.getCause() instanceof Exception cause
                        ? cause
                        : e;

                if (attempt < retries) {
                    // Wait before retry
                    Thread.sleep(Math.min(1000L * (attempt + 1), 10000L));
                }
            }
        }

        throw lastException;
    }

    private void compensate(List<CompletedStep<T>> completedSteps, T context) {
        // Compensate in reverse order
        Collections.reverse(completedSteps);

        for (CompletedStep<T> completed : completedSteps) {
            SagaStep<T> step = completed.step();
            if (step.compensation() != null) {
                try {
                    step.compensation().accept(context);
                } catch (Exception e) {
                    // Log but continue compensating other steps
                    // 记录日志但继续补偿其他步骤
                    LOGGER.log(System.Logger.Level.ERROR,
                            "Compensation failed for step: {0}", step.name(), e);
                }
            }
        }
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @param <T> the context type - 上下文类型
     * @return the builder - 构建器
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for Saga.
     * Saga 构建器。
     *
     * @param <T> the context type - 上下文类型
     */
    public static final class Builder<T> {
        private String name = "saga";
        private final List<SagaStep<T>> steps = new ArrayList<>();
        private java.util.function.Consumer<T> onSuccess;
        private java.util.function.BiConsumer<T, Throwable> onFailure;
        private Duration globalTimeout;

        private Builder() {}

        public Builder<T> name(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        public StepBuilder<T> step(String stepName) {
            return new StepBuilder<>(this, stepName);
        }

        public Builder<T> addStep(SagaStep<T> step) {
            this.steps.add(step);
            return this;
        }

        public Builder<T> onSuccess(java.util.function.Consumer<T> handler) {
            this.onSuccess = handler;
            return this;
        }

        public Builder<T> onFailure(java.util.function.BiConsumer<T, Throwable> handler) {
            this.onFailure = handler;
            return this;
        }

        public Builder<T> globalTimeout(Duration timeout) {
            this.globalTimeout = timeout;
            return this;
        }

        public Saga<T> build() {
            if (steps.isEmpty()) {
                throw new IllegalStateException("Saga must have at least one step");
            }
            return new Saga<>(this);
        }
    }

    /**
     * Builder for SagaStep.
     * SagaStep 构建器。
     *
     * @param <T> the context type - 上下文类型
     */
    public static final class StepBuilder<T> {
        private final Builder<T> parent;
        private final String stepName;
        private java.util.function.Consumer<T> action;
        private java.util.function.Consumer<T> compensation;
        private Duration timeout;
        private int maxRetries = 0;

        private StepBuilder(Builder<T> parent, String stepName) {
            this.parent = parent;
            this.stepName = stepName;
        }

        public StepBuilder<T> action(java.util.function.Consumer<T> action) {
            this.action = Objects.requireNonNull(action);
            return this;
        }

        public StepBuilder<T> compensation(java.util.function.Consumer<T> compensation) {
            this.compensation = compensation;
            return this;
        }

        public StepBuilder<T> timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public StepBuilder<T> retries(int maxRetries) {
            this.maxRetries = Math.max(0, maxRetries);
            return this;
        }

        public Builder<T> build() {
            if (action == null) {
                throw new IllegalStateException("Step action is required");
            }
            SagaStep<T> step = new SagaStep<>(stepName, action, compensation, timeout, maxRetries);
            parent.steps.add(step);
            return parent;
        }
    }

    // ==================== Nested Types | 嵌套类型 ====================

    private record CompletedStep<T>(SagaStep<T> step, Instant completedAt) {}

    /**
     * Exception thrown when a saga step times out.
     * saga 步骤超时时抛出的异常。
     */
    public static class SagaTimeoutException extends Exception {
        public SagaTimeoutException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
